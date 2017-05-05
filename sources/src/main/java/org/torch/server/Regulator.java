package org.torch.server;

import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.Queues;

@NotThreadSafe
public class Regulator extends Thread {
    private static final LinkedBlockingQueue<Runnable> queue = Queues.newLinkedBlockingQueue();

    private Regulator() {
        super("Torch Regulator Thread");
        setPriority(7);
        setDaemon(true);
        start();
    }

    private static final class LazyInstance {
        private static Regulator instance = new Regulator();
    }

    /**
     * Starting the regulator thread or return the instance
     */
    public static Regulator getInstance() {
        return LazyInstance.instance;
    }

    /**
     * Post a task
     */
    public static void post(Runnable runnable) {
        queue.add(runnable);
    }

    /**
     * Executes tasks in the waiting queue
     */
    @Override
    public void run() {
        try {
            while(TorchServer.getServer().isRunning()) queue.take().run();
        } catch (final Throwable t) {
            t.printStackTrace();
            TorchServer.getServer().safeShutdown();
        }
    }
}
