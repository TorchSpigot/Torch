package org.torch.server;

import com.destroystokyo.paper.PaperConfig;
import com.google.common.collect.Queues;

import lombok.Getter;
import net.minecraft.server.*;

import java.util.concurrent.LinkedBlockingQueue;

import org.torch.api.TorchReactor;

@Getter
public final class TorchIOThread implements Runnable, TorchReactor {
    /** The chunks need to save. */
    private final LinkedBlockingQueue<IAsyncChunkSaver> chunkSaverQueue = Queues.newLinkedBlockingQueue();

    private static final class LazyInstance {
        private static TorchIOThread instance = new TorchIOThread();
    }

    private TorchIOThread() {
        Thread thread = new Thread(this, "File IO Thread");

        thread.setPriority(1);
        thread.start();
    }

    /**
     * Retrieves an instance of the TorchIOThread
     */
    public static TorchIOThread getInstance() {
        return TorchIOThread.LazyInstance.instance;
    }

    @Override
    public void run() {
        this.processQueuedChunks();
    }

    /**
     * Process the items that are in the queue
     */
    public void processQueuedChunks() {
        try {
            while (true) this.tryWriteChunk(chunkSaverQueue.take());
        } catch (Throwable t) {
            t.printStackTrace();
            TorchServer.getServer().safeShutdown();
        }
    }

    /**
     * Process a chunk, re-add to the queue if unsuccessful
     */
    private void tryWriteChunk(IAsyncChunkSaver chunkSaver) throws InterruptedException {
        if (!chunkSaver.c()) { // PAIL: WriteNextIO() -> Returns if the write was unsuccessful
            this.getServant().incrementSavedChunkCounter(); // Port

            if (PaperConfig.enableFileIOThreadSleep) Thread.sleep(this.getServant().isWaitingFinish() ? 0L : 2L); // Paper - Add toggle
        } else {
            chunkSaverQueue.add(chunkSaver);
        }
    }

    public void queueChunkToSaving(IAsyncChunkSaver chunkSsaver) {
        if (!this.chunkSaverQueue.contains(chunkSsaver)) {
            this.chunkSaverQueue.add(chunkSsaver);
            this.getServant().incrementWriteQueuedCounter(); // Port
        }
    }

    public void waitForFinish() throws InterruptedException {
        this.getServant().toggleWaitingFinish();

        while (!chunkSaverQueue.isEmpty()) {
            this.tryWriteChunk(chunkSaverQueue.take());
        }

        this.getServant().toggleWaitingFinish();
    }

    @Override
    public FileIOThread getServant() {
        return FileIOThread.a();
    }
}
