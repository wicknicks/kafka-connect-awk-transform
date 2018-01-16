package io.wicknicks.kafka.connect.awk.transform;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingInputStream extends InputStream {

    private final BlockingQueue<Byte> queue;
    private final long delay;
    private volatile boolean isStop = false;
    private Queue<Byte> drain = new LinkedList<>();

    public BlockingInputStream(BlockingQueue<Byte> queue) {
        this(queue, 2);
    }

    public BlockingInputStream(BlockingQueue<Byte> queue, long delayInSeconds) {
        this.queue = queue;
        this.delay = delayInSeconds;
    }

    @Override
    public int read() throws IOException {
        if (drain.size() > 0) {
            return drain.remove();
        }

        while ( !isStop ) {
            try {
                Byte elem = queue.poll(delay, TimeUnit.SECONDS);
                if (elem != null) {
                    drain.add(elem);
                    queue.drainTo(drain);
                    break;
                } else {
                    System.out.println("No bytes in queue...");
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new IOException(e);
            }
        }

        // signal end of stream is drain is empty
        if (drain.size() == 0) {
            return -1;
        }

        return drain.remove();
    }

    public void close() throws IOException {
        isStop = true;
    }

}
