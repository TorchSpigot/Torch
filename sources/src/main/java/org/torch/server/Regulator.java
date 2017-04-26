package org.torch.server;

import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Queues;

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
	 * Starting the regulator or return the instance
	 */
	public static Regulator getInstance() {
		return LazyInstance.instance;
	}
	
	/**
	 * Returns the instance of the gegulator
	 */
	public static void post(Runnable runnable) {
		queue.add(runnable);
	}

	/**
	 * Executes tasks in the waiting queue
	 */
	@Override
	public void run() {
		while(TorchServer.getServer().isRunning()) {
			try {
				queue.take().run();
				//System.out.println("--------- DEBUG --- TASK PROCESS IN REGULATOR ---------");
			} catch (final Throwable t) {
				t.printStackTrace();
				TorchServer.getServer().safeShutdown();
			}
		}
	}

}