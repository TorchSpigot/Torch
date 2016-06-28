/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hose;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import net.minecraft.server.PlayerChunk;

/**
 *
 * @author softpak
 */
public class ChunkFlush_Pool extends RecursiveAction {//need to use callable
    List<PlayerChunk> chunkflush_list;
    
    public ChunkFlush_Pool(List<PlayerChunk> chunkflush_list){
        this.chunkflush_list = chunkflush_list;
    }
    
    @Override
    protected void compute() {
        List<RecursiveAction> forks = new LinkedList();
        for (PlayerChunk pc : chunkflush_list) {
            chunkflush task = new chunkflush(pc);
            forks.add(task);
            task.fork();
            //task.join();
        }
        /*
        for (RecursiveAction task : forks) {
            task.join();
        }*/
    }
    
    
    class chunkflush extends RecursiveAction {
        PlayerChunk pc;
        
        chunkflush(PlayerChunk pc) {
            this.pc = pc;
        }
        
        @Override
        protected void compute() {
            pc.d();
        }
                 
    }
}
