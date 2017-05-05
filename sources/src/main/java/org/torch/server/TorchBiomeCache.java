package org.torch.server;

import lombok.Getter;
import net.minecraft.server.*;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

import org.torch.api.TorchReactor;

@Getter
public final class TorchBiomeCache implements TorchReactor {
    /** The legacy */
    private final BiomeCache servant;
    private final WorldChunkManager chunkManager;

    // TODO: May tied to server tick rather than wall clock time / Configurable size
    /** Cached biome bases, ChunkCoordIntPair -> Biomes */
    Cache<Long, BiomeBase[]> caches = Caffeine.newBuilder().maximumSize(4096).expireAfterAccess(30, TimeUnit.SECONDS).build();

    public TorchBiomeCache(WorldChunkManager worldChunkManager, BiomeCache legacy) {
        servant = legacy;
        chunkManager = worldChunkManager;
    }

    /**
     * Returns a biome cache block at location specified
     */
    public BiomeBase[] requestCache(int blockX, int blockZ) {
        long hash = ChunkCoordIntPair.chunkXZ2Int(blockX >>= 4, blockZ >>= 4); // Convert to chunk x, z
        BiomeBase[] cachedBiomes = caches.getIfPresent(hash);

        if (cachedBiomes == null) {
            cachedBiomes = createCache(blockX, blockZ);
            caches.put(hash, cachedBiomes);
        }

        return cachedBiomes;
    }

    public BiomeBase[] createCache(int blockX, int blockZ) {
        return chunkManager.getBiomes(new BiomeBase[256], blockX << 4, blockZ << 4, 16, 16, false);
    }

    /**
     * Returns the BiomeBase related to the x, z position from the cache
     */
    public BiomeBase getBiome(int blockX, int blockZ, BiomeBase defaultValue) {
        BiomeBase cache = requestCache(blockX, blockZ) [blockX & 15 | (blockZ & 15) << 4];
        return cache != null ? cache : defaultValue;
    }
}
