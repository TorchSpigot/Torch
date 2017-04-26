package org.torch.api;

import javax.annotation.Nullable;

import net.minecraft.server.Chunk;

public interface IChunkProvider {
    @Nullable Chunk getLoadedChunkAt(int x, int z);
    
    Chunk getChunkAt(int x, int j);
    
    boolean isChunkGeneratedAt(int x, int z);
    
    boolean unloadChunks();
    
    String getName();
}
