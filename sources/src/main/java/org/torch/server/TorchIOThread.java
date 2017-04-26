package org.torch.server;

import com.destroystokyo.paper.PaperConfig;
import com.google.common.collect.Queues;

import lombok.Getter;
import net.minecraft.server.*;

import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class TorchIOThread implements Runnable {
    /** The chunks need to save. */
    private final LinkedBlockingQueue<IAsyncChunkSaver> chunkSaverQueue = Queues.newLinkedBlockingQueue();
    // private volatile long writeQueuedCounter;
    // private volatile long savedChunkCounter;
    /** If is waiting to saving all chunks. */
    private volatile boolean isWaitingFinish;
    
    private static final class LazyInstance {
	private static TorchIOThread instance = new TorchIOThread();
    }

    private TorchIOThread() {
        Thread thread = new Thread(this, "File IO Thread");
        
        thread.setPriority(1);
        thread.start();
    }

    /**
     * Retrieves an instance of the threadedFileIOBase.
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
    	if (!chunkSaver.c()) { // c() -> WriteNextIO() -> Returns if the write was unsuccessful
        	// this.savedChunkCounter++;
        	
        	if (PaperConfig.enableFileIOThreadSleep) Thread.sleep(this.isWaitingFinish ? 0L : 2L); // Paper - Add toggle
        } else {
        	chunkSaverQueue.add(chunkSaver);
        }
    }
    
    public void queueChunkToSaving(IAsyncChunkSaver chunkSsaver) {
        if (!this.chunkSaverQueue.contains(chunkSsaver)) {
            // ++this.writeQueuedCounter;
            this.chunkSaverQueue.add(chunkSsaver);
        }
    }
    
    public void waitForFinish() throws InterruptedException {
        FileIOThread.getInstance().setIsWaitingFinish(this.isWaitingFinish = true);
        this.isWaitingFinish = true;
        
        while (!chunkSaverQueue.isEmpty()) {
            this.tryWriteChunk(chunkSaverQueue.take());
            // Thread.sleep(10L);
        }
        
        FileIOThread.getInstance().setIsWaitingFinish(this.isWaitingFinish = false);
    }
}
