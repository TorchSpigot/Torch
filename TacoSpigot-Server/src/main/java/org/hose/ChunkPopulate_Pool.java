/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hose;

import java.util.Random;
import java.util.concurrent.RecursiveAction;
import org.bukkit.Chunk;
import org.bukkit.World;

/**
 *
 * @author softpak
 */
public class ChunkPopulate_Pool extends RecursiveAction {//need to use callable
    World world;
    Random random;
    Chunk chunk;
    org.bukkit.generator.BlockPopulator populator;
    
    public ChunkPopulate_Pool(World world, Random random, Chunk chunk, org.bukkit.generator.BlockPopulator populator){
        this.world = world;
        this.random = random;
        this.chunk = chunk;
        this.populator = populator;
    }
    
    @Override
    protected void compute() {
        populator.populate(world, random, chunk);
    }

}
