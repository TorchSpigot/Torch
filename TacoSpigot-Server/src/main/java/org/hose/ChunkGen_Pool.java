/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hose;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import net.minecraft.server.WorldServer;

/**
 *
 * @author softpak
 */
public class ChunkGen_Pool extends RecursiveAction {
    WorldServer worldserver;
    List<int[]> position;
    
    public ChunkGen_Pool(WorldServer worldserver, List<int[]> position){
        this.worldserver = worldserver;
        this.position = position;
        
    }
    
    @Override
    protected void compute() {
        List<RecursiveAction> forks = new LinkedList();
        for (int[] pt : position) {
            chunkgen task = new chunkgen(worldserver, pt);
            forks.add(task);
            task.fork();
        }
        
    }
    
    
    class chunkgen extends RecursiveAction {
        WorldServer worldserver;
        int[] pt;
        
        chunkgen(WorldServer worldserver, int[] pt) {
            this.worldserver = worldserver;
            this.pt = pt;
        }
        
        @Override
        protected void compute() {
            worldserver.getChunkProviderServer().getChunkAt(pt[0], pt[1]);
        }
                 
    }
}
