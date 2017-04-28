package org.torch.api;

import javax.annotation.Nullable;

import net.minecraft.server.Chunk;

public interface IChunkProvider {
    abstract @Nullable Chunk getLoadedChunkAt(int x, int z);
    
    abstract Chunk getChunkAt(int x, int j);
    
    abstract boolean isChunkGeneratedAt(int x, int z);
    
    abstract boolean unloadChunks();
    
    abstract String getName();
}
