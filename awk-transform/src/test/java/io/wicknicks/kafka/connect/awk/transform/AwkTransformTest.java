package io.wicknicks.kafka.connect.awk.transform;

import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AwkTransformTest {

    private static Logger log = LoggerFactory.getLogger(AwkTransformTest.class);

    private final AwkTransform<SinkRecord> awkTransform = new AwkTransform<>();

    @After
    public void cleanup() {
        awkTransform.close();
    }

    @Test
    public void testConfigWithScript() {
        awkTransform.configure(Collections.singletonMap("awk.script", "{print $1}"));
        Assert.assertTrue(true);
    }

    @Test(expected = ConfigException.class)
    public void testConfigFailWithoutScript() {
        awkTransform.configure(Collections.singletonMap("awk.args", "-F','"));
        Assert.assertFalse(true);
    }

    @Test
    public void testSimpleScript() throws Exception {
        awkTransform.configure(Collections.singletonMap("awk.script", "{print $1, $2}"));

        SinkRecord rsp = awkTransform.apply(valueRecord("Amelia       555-5553     amelia.zodiacusque@gmail.com    F\n"));
        Assert.assertNotNull(rsp);
        Assert.assertEquals("Amelia 555-5553\n", rsp.value());
    }

    @Test
    public void testSimpleScriptNullInput() throws Exception {
        awkTransform.configure(Collections.singletonMap("awk.script", "{print $1, $2}"));

        SinkRecord rsp = awkTransform.apply(valueRecord(null));
        Assert.assertNull(rsp);
    }

    @Test
    public void testSimpleScriptWithParams() throws Exception {
        Map<String, String> props = new HashMap<>();
        props.put("awk.script", "{print $1, $2}");
        props.put("awk.arguments", "-F ,");
        awkTransform.configure(props);

        SinkRecord rsp = awkTransform.apply(valueRecord("Amelia,555-5553,amelia.zodiacusque@gmail.com,F\n"));
        Assert.assertNotNull(rsp);
        Assert.assertEquals("Amelia 555-5553\n", rsp.value());
    }

    static SinkRecord valueRecord(String value) {
        return new SinkRecord("test", 0, null, "key", Schema.STRING_SCHEMA, value, 0);
    }

}
