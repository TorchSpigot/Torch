package net.minecraft.server;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

// CraftBukkit end
import org.torch.server.TorchChunkProvider;

import static org.torch.server.TorchServer.logger;

public class ChunkProviderServer implements IChunkProvider, org.torch.api.TorchServant {
	public final TorchChunkProvider reactor;
	
    private static final Logger a = logger;
    private static final double UNLOAD_QUEUE_RESIZE_FACTOR = TorchChunkProvider.UNLOAD_QUEUE_RESIZE_FACTOR;
    
    public final WorldServer world;
    public final ChunkGenerator chunkGenerator;
    public final IChunkLoader chunkLoader;
    
    // TODO: port
    // private long lastQueuedSaves = 0L;
    // private long lastProcessedSaves = 0L;
    // private long lastSaveStatPrinted = System.currentTimeMillis();
    // protected Chunk lastChunkByPos = null;
    
    public final it.unimi.dsi.fastutil.longs.LongSet unloadQueue;
    public Long2ObjectOpenHashMap<Chunk> chunks;

    public ChunkProviderServer(WorldServer worldserver, IChunkLoader ichunkloader, ChunkGenerator chunkgenerator) {
    	reactor = new TorchChunkProvider(worldserver, ichunkloader, chunkgenerator, this);
    	
        this.world = worldserver;
        this.chunkLoader = ichunkloader;
        this.chunkGenerator = chunkgenerator;
        
        this.unloadQueue = reactor.getUnloadQueue();
        this.chunks = reactor.getChunks();
    }
    
    public Collection<Chunk> a() {
        return reactor.getLoadedChunks();
    }

    public void unload(Chunk chunk) {
    	reactor.postChunkToUnload(chunk);
    }

    public void b() {
    	reactor.unloadAllChunks();
    }

    @Override
	@Nullable
    public Chunk getLoadedChunkAt(int i, int j) {
        return reactor.getLoadedChunkAt(i, j);
    }

    @Nullable
    public Chunk getOrLoadChunkAt(int i, int j) {
        return reactor.getOrLoadChunkAt(i, j);
    }

    @Nullable
    public Chunk originalGetOrLoadChunkAt(int i, int j) {
        return reactor.originalGetOrLoadChunkAt(i, j);
    }

    // CraftBukkit start
    public Chunk getChunkIfLoaded(int x, int z) {
        return reactor.getChunkIfLoaded(x, z, false);
    }
    // CraftBukkit end

    @Override
	public Chunk getChunkAt(int i, int j) {
        return reactor.getChunkAt(i, j);
    }

    public Chunk getChunkAt(int i, int j, Runnable runnable) {
        return reactor.getChunkAt(i, j, runnable);
    }

    public Chunk getChunkAt(int i, int j, Runnable runnable, boolean generate) {
        return reactor.getChunkAt(i, j, runnable, generate);
    }

    public Chunk originalGetChunkAt(int i, int j) {
        return reactor.originalGetChunkAt(i, j);
    }

    @Nullable
    public Chunk loadChunk(int i, int j) {
        return reactor.loadChunkFromFile(i, j);
    }

    public void saveChunkNOP(Chunk chunk) {
        reactor.saveChunkExtraData(chunk);
    }

    public void saveChunk(Chunk chunk) {
    	reactor.saveChunkData(chunk);
    }

    public boolean a(boolean flag) {
        return reactor.saveChunks(flag);
    }

    public void c() {
        this.chunkLoader.b();
    }

    @Override
	public boolean unloadChunks() {
        return reactor.unloadChunks();
    }

    // CraftBukkit start
    public boolean unloadChunk(Chunk chunk, boolean save) {
        return reactor.unloadChunk(chunk, save);
    }
    // CraftBukkit end

    public boolean e() {
        return reactor.canSave();
    }

    @Override
	public String getName() {
        return reactor.getName();
    }

    public List<BiomeBase.BiomeMeta> a(EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
        return reactor.getPossibleCreatures(enumcreaturetype, blockposition);
    }

    @Nullable
    public BlockPosition a(World world, String s, BlockPosition blockposition, boolean flag) {
        return reactor.findNearestMapFeature(world, s, blockposition, flag);
    }

    public int g() {
        return reactor.getLoadedChunkCount();
    }

    public boolean isLoaded(int i, int j) {
        return reactor.isLoaded(i, j);
    }

    @Override
	public boolean e(int i, int j) {
        return reactor.isChunkGeneratedAt(i, j);
    }

	@Override
	public TorchChunkProvider getReactor() {
		return reactor;
	}
}
