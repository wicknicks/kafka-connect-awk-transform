package io.wicknicks.kafka.connect.awk.transform;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.transforms.Transformation;
import org.jawk.Awk;
import org.jawk.util.AwkParameters;
import org.jawk.util.AwkSettings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Objects;

public class AwkTransform<R extends ConnectRecord<R>> implements Transformation<R> {

    private static final String AWK_ARGS = "awk.arguments";
    private static final String AWK_SCRIPT = "awk.script";

    String[] args;

    @Override
    public R apply(R r) {
        Objects.requireNonNull(r);
        if (r.value() == null) {
            return null;
        }

        if (r.value() instanceof String) {
            String out = evaluateAwkInThread((String) r.value());
            return r.newRecord(r.topic(), r.kafkaPartition(), r.keySchema(), r.key(),
                        Schema.STRING_SCHEMA, out, r.timestamp());
        } else {
            throw new DataException("Expected value to be of type String, but found: ");
        }
    }

    public String evaluateAwkInThread(String input) {
        AwkParameters parameters = new AwkParameters(AwkTransform.class, null);
        AwkSettings settings = parameters.parseCommandLineArguments(args);
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        settings.setInput(new ByteArrayInputStream(input.getBytes()));
        settings.setOutputStream(bytesOut);
        Awk awk = new Awk();
        try {
            awk.invoke(settings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new String(bytesOut.toByteArray());
    }

    @Override
    public ConfigDef config() {
        String group = "Awk";
        int order = 1;
        return new ConfigDef().define(AWK_ARGS, ConfigDef.Type.STRING, ConfigDef.Importance.LOW,
                "The arguments to a typical awk command. For example, -F for field separator",

                group, order++, ConfigDef.Width.LONG, "Awk Args")
                .define(AWK_SCRIPT, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH,
                        "The awk script itself", group, order, ConfigDef.Width.LONG,
                        "Awk Script");
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {
        String script;
        if (map.containsKey(AWK_SCRIPT)) {
            script = String.valueOf(map.get(AWK_SCRIPT));
        } else {
            throw new ConfigException("Could not find " + AWK_SCRIPT + " config key");
        }

        String argString = map.containsKey(AWK_ARGS) ? String.valueOf(map.get(AWK_ARGS)).trim() : "";
        if (argString.length() > 0) {
            String[] argComponents = argString.split("\\s+");
            args = new String[argComponents.length + 1];
            System.arraycopy(argComponents, 0, args, 0, argComponents.length);
            args[args.length - 1] = script;
        } else {
            args = new String[]{script};
        }

    }

}
