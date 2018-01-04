package io.wicknicks.kafka.connect.awk.transform;

import org.jawk.Awk;
import org.jawk.Main;

import java.io.FileInputStream;

public class TestJawk {

    public static void main(String[] args) throws Exception {
        String file = "/home/arjun/Sandbox/clones/kafka-connect-awk-transform/jawk/" +
                "src/test/resources/org/jawk/mail-list";

        new Main(new String[]{"{print $1}"},
                new FileInputStream(file), System.out, System.err);
    }

}
