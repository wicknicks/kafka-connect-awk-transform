package io.wicknicks.kafka.connect.awk.transform;

import org.jawk.Awk;
import org.jawk.Main;
import org.jawk.util.AwkParameters;
import org.jawk.util.AwkSettings;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TestJawk {

    public static void main(String[] args) throws Exception {
        String file = "/home/arjun/Sandbox/clones/kafka-connect-awk-transform/jawk/" +
                "src/test/resources/org/jawk/mail-list";

//        new Main(new String[]{"{print $1}"},
//                new FileInputStream(file), System.out, System.err);

        final BlockingQueue<Byte> queue = new ArrayBlockingQueue<>(100);
        BlockingInputStream bis = new BlockingInputStream(queue);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Thread t = new Thread(() -> {
                AwkParameters parameters = new AwkParameters(Main.class, null);
                AwkSettings settings = parameters.parseCommandLineArguments(new String[]{"{print $3}"});
                settings.setInput(bis);
                settings.setOutputStream(bos);
                Awk awk = new Awk();
                try {
                    awk.invoke(settings);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        t.start();

        System.out.println("Waiting... ");
        Thread.sleep(5000);

        FileInputStream fis = new FileInputStream(file);
        int b;
        while ( (b = fis.read()) != -1 ) {
            queue.put( (byte) b);
        }

        fis.close();
        bis.close();

        System.out.println("Waiting for all threads to exit.");
        t.join();

        System.out.println("Size of bos: " + bos.toByteArray().length);
        System.out.println("====== OUTPUT");
        System.out.println(new String(bos.toByteArray()));
    }

}
