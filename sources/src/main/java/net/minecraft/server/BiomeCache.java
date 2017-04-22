package net.minecraft.server;

import org.torch.server.TorchBiomeCache;

public class BiomeCache implements org.torch.api.TorchServant {
	private final TorchBiomeCache reactor;
	
	/** chunkManager */
    private final WorldChunkManager a;
    /** lastCleanupTime */
    private long b;
    /** cacheMap */
    // private final Long2ObjectMap<BiomeCache.a> c;
    /** cache */
    // private final List<BiomeCache.a> d;

    public BiomeCache(WorldChunkManager worldchunkmanager) {
    	reactor = new TorchBiomeCache(worldchunkmanager, this);
        
    	this.a = reactor.getChunkManager();
    	this.b = reactor.getLastCleanupTime();
    }

    public BiomeCache.a a(int i, int j) {
        return reactor.getBiomeCacheBlock(i, j).toLegacy();
    }

    public BiomeBase a(int i, int j, BiomeBase biomebase) {
        return reactor.getBiome(i, j, biomebase);
    }

    public void a() {
    	reactor.cleanupCache();
    }

    public BiomeBase[] b(int i, int j) {
        return reactor.getCachedBiomes(i, j);
    }

    public class a extends TorchBiomeCache.Block {
    	/** biomes */
        public BiomeBase[] a = new BiomeBase[256];
        /** xPos */
        public int b;
        /** zPos */
        public int c;
        /** lastAccessTime */
        public long d;
        
        public a(int i, int j) {
        	reactor.super(i, j);
        	
            this.b = super.xPos;
            this.c = super.zPos;
            this.d = super.lastAccessTime;
        }
        
        public BiomeBase a(int i, int j) {
            return super.getBiome(i, j);
        }
    }

	@Override
	public TorchBiomeCache getReactor() {
		return reactor;
	}
}
