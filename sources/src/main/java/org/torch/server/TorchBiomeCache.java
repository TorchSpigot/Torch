package org.torch.server;

import lombok.Getter;
import net.minecraft.server.*;

import com.koloboke.collect.map.hash.HashLongObjMap;
import com.koloboke.collect.map.hash.HashLongObjMaps;
import com.koloboke.function.LongObjPredicate;
import java.util.concurrent.TimeUnit;

import org.torch.api.TorchReactor;

@Getter
public final class TorchBiomeCache implements TorchReactor {
	/** The legacy */
	private final BiomeCache servant;
	
	// Time in milliseconds. Immune to system time changes, DST, etc.
	// TODO: May tied to game tick rather than wall clock time
	private final static long CLEAN_INTERVAL = TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);
	private final static long EXPIRE_THRESHOLD = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);
	
    private final WorldChunkManager chunkManager;
    /** Used to remove all expired caches, refresh before check. */
	private final CacheExpirer oldEntriesRemover = new CacheExpirer();
    /** The last time this BiomeCache was cleaned, in milliseconds. */
    private long lastCleanupTime = System.currentTimeMillis();
    /**
	 * The map of keys to TorchBiomeCache. Keys are based on the chunk x, z coordinates as (x | z << 32).
	 */
    private final HashLongObjMap<TorchBiomeCache.Block> cacheMap = HashLongObjMaps.newMutableMap(4096);
    
    public TorchBiomeCache(WorldChunkManager worldChunkManager, BiomeCache legacy) {
    	servant = legacy;
        chunkManager = worldChunkManager;
    }
    
    /**
     * Returns a biome cache block at location specified
     */
    public TorchBiomeCache.Block getBiomeCacheBlock(int blockX, int blockZ) {
    	// Convert to chunk x, z
    	blockX >>= 4;
    	blockZ >>= 4;
    	
        long hash = ChunkCoordIntPair.chunkXZ2Int(blockX, blockZ);
        TorchBiomeCache.Block cachedBlock = this.cacheMap.get(hash);

        if (cachedBlock == null) {
        	cachedBlock = new TorchBiomeCache.Block(blockX, blockZ);
            this.cacheMap.put(hash, cachedBlock);
        }
        
        return cachedBlock;
    }
    
	/**
	 * Returns the BiomeBase related to the x, z position from the cache
	 */
    public BiomeBase getBiome(int blockX, int blockZ, BiomeBase defaultValue) {
        BiomeBase cachedBiome = this.getBiomeCacheBlock(blockX, blockZ).getBiome(blockX, blockZ);
        return cachedBiome != null ? cachedBiome : defaultValue;
    }
    
    /**
     * Removes BiomeCacheBlocks from this cache that haven't been accessed in at least 30 seconds
     */
    public void cleanupCache() {
        long currentMillis = System.currentTimeMillis();
		if ((currentMillis - this.lastCleanupTime) > CLEAN_INTERVAL) {
			this.lastCleanupTime = currentMillis;
			this.cacheMap.removeIf(this.oldEntriesRemover.refresh(currentMillis));
		}
    }
    
    private static class CacheExpirer implements LongObjPredicate<Block> {
		private long timeMark;
		public CacheExpirer refresh(final long currentTime) {
			this.timeMark = currentTime;
			return this;
		}
		// Return true to cause a removal of the entry in the map
		@Override
		public boolean test(long chunkXZ2Int, Block cachedBlock) {
			return (timeMark - cachedBlock.lastAccessTime) > EXPIRE_THRESHOLD;
		}
	}
    
    /**
     * Returns the array of cached biome types in the BiomeCacheBlock at the given location.
     */
    public BiomeBase[] getCachedBiomes(int x, int z) {
        return this.getBiomeCacheBlock(x, z).biomes;
    }
    
    public class Block {
    	/** The array of biome types stored in this BiomeCacheBlock. */
        public BiomeBase[] biomes = new BiomeBase[256];
        /** The x coordinate of the BiomeCacheBlock. */
        public int blockX;
        /** The z coordinate of the BiomeCacheBlock. */
        public int blockZ;
        /** The last time this BiomeCacheBlock was accessed, in milliseconds. */
        public long lastAccessTime = System.currentTimeMillis();
        
        public Block(int blockX, int blockZ) {
            this.blockX = blockX;
            this.blockZ = blockZ;
            chunkManager.a(this.biomes, blockX << 4, blockZ << 4, 16, 16, false); // Get biome gen at
        }
        
        /**
         * Returns the BiomeGenBase related to the x, z position from the cache block.
         */
        public BiomeBase getBiome(int blockX, int blockZ) {
            return this.biomes[blockX & 15 | (blockZ & 15) << 4];
        }
        
        public BiomeCache.a toLegacy() {
        	return (BiomeCache.a) this;
        }
    }

	@Override
	public BiomeCache getServant() {
		return servant;
	}
}
