package io.wicknicks.kafka.connect.awk.transform;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.transforms.Transformation;

import java.util.Map;
import java.util.Objects;

public class AwkTransform<R extends ConnectRecord<R>> implements Transformation<R> {

    private static final String AWK_ARGS = "awk.arguments";
    private static final String AWK_SCRIPT = "awk.script";

    @Override
    public R apply(R r) {
        Objects.requireNonNull(r);

        if (r.value() instanceof String) {
            processAwk((String) r.value());
        } else {
            throw new DataException("Expected value to be of type String, but found: ");
        }
        return null;
    }

    public R processAwk(String r) {
        return null;
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
        String script = null;
        if (map.containsKey(AWK_SCRIPT)) {
            script = String.valueOf(map.get(AWK_SCRIPT));
        } else {
            throw new ConfigException("Could not find " + AWK_SCRIPT + " config key");
        }

        String args = map.containsKey(AWK_ARGS) ? String.valueOf(map.get(AWK_ARGS)) : "";
    }

}
