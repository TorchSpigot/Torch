package net.minecraft.server;

import org.torch.server.TorchBiomeCache;

import lombok.Getter;

public class BiomeCache implements org.torch.api.TorchServant {
	@Getter private final TorchBiomeCache reactor;
	
	/** chunkManager */
    private final WorldChunkManager a;
    /** lastCleanupTime */
    // private long b;
    /** cacheMap */
    // private final Long2ObjectMap<BiomeCache.a> c;
    /** cache */
    // private final List<BiomeCache.a> d;

    public BiomeCache(WorldChunkManager worldchunkmanager) {
    	reactor = new TorchBiomeCache(worldchunkmanager, this);
        
    	this.a = reactor.getChunkManager();
    }

    /* public BiomeCache.a a(int i, int j) {
        return new BiomeCache.a(i, j, reactor.requestCache(i, j));
    } */

    public BiomeBase a(int i, int j, BiomeBase biomebase) {
        return reactor.getBiome(i, j, biomebase);
    }

    @Deprecated public void a() {} // cleanupCache
    
    public BiomeBase[] b(int i, int j) {
        return reactor.requestCache(i, j);
    }

    /* public class a {
        public BiomeBase[] a; // biomes
        public int b; // xPos
        public int c; // zPos
        public long d; // lastAccessTime
        
        public a(int i, int j) {
        	this.b = i;
            this.c = j;
            this.a = new BiomeBase[256];
            BiomeCache.this.a.a(this.a, i << 4, j << 4, 16, 16, false);
        }
        
        //public a(int x, int z, BiomeBase[] cache) {
        //	a = cache;
        //	b = x;
        //	c = z;
        //}
        
        public BiomeBase a(int i, int j) {
            return this.a[i & 15 | (j & 15) << 4];
        }
    } */
}
