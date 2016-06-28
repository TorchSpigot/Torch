package com.destroystokyo.paper.antixray;

import net.minecraft.server.Chunk;
import net.minecraft.server.PacketDataSerializer;
import net.minecraft.server.PacketPlayOutMapChunk;

public class ObfuscatorRunnable implements Runnable {
 private final PacketPlayOutMapChunk packetPlayOutMapChunk;
 private final PacketDataSerializer packetDataSerializer;
 private final Chunk chunk;
 private final boolean writeSkyLightArray;
 private final int chunkSectionSelector;
 private final Chunk[] nearbyChunks;

 public ObfuscatorRunnable(PacketPlayOutMapChunk packetPlayOutMapChunk, PacketDataSerializer packetDataSerializer, Chunk chunk, boolean writeSkyLightArray, int chunkSectionSelector, Chunk[] nearbyChunks) {
     chunk.blocksLock.lock();
     chunk.dataLock.lock();

     if (nearbyChunks != null) {
         for (Chunk nearbyChunk : nearbyChunks) {
             if (nearbyChunk != null) {
                 nearbyChunk.blocksLock.lock();
             }
         }
     }

     this.packetPlayOutMapChunk = packetPlayOutMapChunk;
     this.packetDataSerializer = packetDataSerializer;
     this.chunk = chunk;
     this.nearbyChunks = nearbyChunks;
     this.writeSkyLightArray = writeSkyLightArray;
     this.chunkSectionSelector = chunkSectionSelector;
 }

 @Override
 public void run() {
     try {
         packetPlayOutMapChunk.setWrittenChunkSections(packetPlayOutMapChunk.a(packetDataSerializer, chunk, writeSkyLightArray, chunkSectionSelector, nearbyChunks));
         packetPlayOutMapChunk.setReady(true);
     } finally {
         chunk.blocksLock.unlock();
         chunk.dataLock.unlock();

         if (nearbyChunks != null) {
             for (Chunk nearbyChunk : nearbyChunks) {
                 if (nearbyChunk != null) {
                     nearbyChunk.blocksLock.unlock();
                 }
             }
         }
     }
 }
}