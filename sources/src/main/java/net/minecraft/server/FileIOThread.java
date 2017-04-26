package net.minecraft.server;

import org.torch.server.TorchIOThread;

public class FileIOThread implements Runnable {

    private static final FileIOThread a = new FileIOThread();
    // These field are unsafe to port
    // private final List<IAsyncChunkSaver> b;
    // private volatile long c;
    // private volatile long d;
    private volatile boolean e; public void setIsWaitingFinish(boolean flag) { this.e = flag; } // OBFHELPER

    private FileIOThread() {
    	TorchIOThread.getInstance();
    }

    public static FileIOThread a() {
        return FileIOThread.a;
    }

    @Override
	public void run() {
    	TorchIOThread.getInstance().run();
    }

    private void c() {
    	TorchIOThread.getInstance().processQueuedChunks();
    }

    public void a(IAsyncChunkSaver iasyncchunksaver) {
    	TorchIOThread.getInstance().queueChunkToSaving(iasyncchunksaver);
    }

    public void b() throws InterruptedException {
    	TorchIOThread.getInstance().waitForFinish();
    }
}
