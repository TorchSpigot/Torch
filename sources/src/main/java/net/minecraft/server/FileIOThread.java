package net.minecraft.server;

import org.torch.server.TorchIOThread;

public class FileIOThread implements Runnable, org.torch.api.TorchServant {

    private static final FileIOThread a = new FileIOThread();
    // private final List<IAsyncChunkSaver> b; // Unsafe to port
    private volatile long c; public void incrementWriteQueuedCounter() { this.c++; }
    private volatile long d; public void incrementSavedChunkCounter() { this.d++; }
    private volatile boolean e; public void toggleWaitingFinish() { this.e = !this.e; } public boolean isWaitingFinish() { return this.e; }

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

    @Override
    public TorchIOThread getReactor() {
        return TorchIOThread.getInstance();
    }
}
