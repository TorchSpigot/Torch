package net.minecraft.server;

import org.torch.server.TorchIOThread;

@Deprecated
public class FileIOThread implements Runnable {

    private static final FileIOThread a = new FileIOThread();
    // private final List<IAsyncChunkSaver> b; // Unsafe to port
    // private volatile long c;
    // private volatile long d;
    // private volatile boolean e;

    private FileIOThread() {}

    public static FileIOThread a() {
        return FileIOThread.a;
    }

    @Override
    public void run() {}

    private void c() {}

    public void a(IAsyncChunkSaver iasyncchunksaver) {
        TorchIOThread.saveChunk(iasyncchunksaver);
    }

    public void b() throws InterruptedException {
        TorchIOThread.waitForFinish();
    }
}
