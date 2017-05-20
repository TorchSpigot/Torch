package org.torch.server;

import com.destroystokyo.paper.PaperConfig;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.minecraft.server.IAsyncChunkSaver;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.torch.api.Async;

public final class TorchIOThread { // TODO: configurable threads
    private final static Executor ioExecutor = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("File IO Thread - %1$d").setPriority(1).build());
    
    private final static AtomicBoolean isWaitingFinish = new AtomicBoolean(false);
    
    private final static AtomicInteger queuedChunkCounter = new AtomicInteger(0);
    
    private TorchIOThread() {}
    
    /**
     * Process a chunk, re-add to the queue if unsuccessful
     */
    private static void writeChunk(IAsyncChunkSaver chunkSaver)  {
        if (!chunkSaver.c()) { // PAIL: WriteNextIO() -> Returns if the write was unsuccessful
            queuedChunkCounter.decrementAndGet();
            
            if (PaperConfig.enableFileIOThreadSleep) {
                try {
                    Thread.sleep(isWaitingFinish.get() ? 0L : 2L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            
        } else {
            queuedChunkCounter.decrementAndGet();
            
            saveChunk(chunkSaver);
        }
    }
    
    /**
     * Posts a chunk for save
     */
    @Async
    public static void saveChunk(IAsyncChunkSaver chunkSaver)  {
        queuedChunkCounter.incrementAndGet();
        
        ioExecutor.execute(() -> writeChunk(chunkSaver));
    }
    
    /**
     * Waits for all chunks to be saved
     */
    public static void waitForFinish() throws InterruptedException {
        isWaitingFinish.getAndSet(true);
        
        while (queuedChunkCounter.get() != 0) Thread.sleep(9L);
        
        isWaitingFinish.getAndSet(false);
    }
}
