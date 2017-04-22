package net.minecraft.server;

import com.destroystokyo.paper.PaperConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import com.destroystokyo.paper.exception.ServerInternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start
import org.bukkit.craftbukkit.chunkio.ChunkIOExecutor;
import org.bukkit.event.world.ChunkUnloadEvent;
// CraftBukkit end

public class ChunkProviderServer implements IChunkProvider {

    private static final Logger a = LogManager.getLogger();
    public final it.unimi.dsi.fastutil.longs.LongSet unloadQueue = new it.unimi.dsi.fastutil.longs.LongArraySet(); // PAIL: private -> public // Paper
    public final ChunkGenerator chunkGenerator;
    private final IChunkLoader chunkLoader;
    // Paper start - chunk save stats
    private long lastQueuedSaves = 0L; // Paper
    private long lastProcessedSaves = 0L; // Paper
    private long lastSaveStatPrinted = System.currentTimeMillis();
    // Paper end
    // Paper start
    protected Chunk lastChunkByPos = null;
    public Long2ObjectOpenHashMap<Chunk> chunks = new Long2ObjectOpenHashMap<Chunk>(8192) {

        @Override
        public Chunk get(long key) {
            if (lastChunkByPos != null && key == lastChunkByPos.chunkKey) {
                return lastChunkByPos;
            }
            return lastChunkByPos = super.get(key);
        }

        @Override
        public Chunk remove(long key) {
            if (lastChunkByPos != null && key == lastChunkByPos.chunkKey) {
                lastChunkByPos = null;
            }
            return super.remove(key);
        }
    }; // CraftBukkit
    // Paper end
    public final WorldServer world;

    public ChunkProviderServer(WorldServer worldserver, IChunkLoader ichunkloader, ChunkGenerator chunkgenerator) {
        this.world = worldserver;
        this.chunkLoader = ichunkloader;
        this.chunkGenerator = chunkgenerator;
    }

    public Collection<Chunk> a() {
        return this.chunks.values();
    }

    public void unload(Chunk chunk) {
        if (this.world.worldProvider.c(chunk.locX, chunk.locZ)) {
            this.unloadQueue.add(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunk.locX, chunk.locZ)));
            chunk.d = true;
        }

    }

    public void b() {
        ObjectIterator objectiterator = this.chunks.values().iterator();

        while (objectiterator.hasNext()) {
            Chunk chunk = (Chunk) objectiterator.next();

            this.unload(chunk);
        }

    }

    @Override
	@Nullable
    public Chunk getLoadedChunkAt(int i, int j) {
        long k = ChunkCoordIntPair.chunkXZ2Int(i, j);
        Chunk chunk = this.chunks.get(k);

        if (chunk != null) {
            chunk.d = false;
        }

        return chunk;
    }

    @Nullable
    public Chunk getOrLoadChunkAt(int i, int j) {
        Chunk chunk = this.getLoadedChunkAt(i, j);

        if (chunk == null) {
            // CraftBukkit start
            ChunkRegionLoader loader = null;

            if (this.chunkLoader instanceof ChunkRegionLoader) {
                loader = (ChunkRegionLoader) this.chunkLoader;
            }
            if (loader != null && loader.chunkExists(world, i, j)) {
                chunk = ChunkIOExecutor.syncChunkLoad(world, loader, this, i, j);
            }
        }

        return chunk;
    }

    @Nullable
    public Chunk originalGetOrLoadChunkAt(int i, int j) {
        // CraftBukkit end
        Chunk chunk = this.getLoadedChunkAt(i, j);

        if (chunk == null) {
            chunk = this.loadChunk(i, j);
            if (chunk != null) {
                this.chunks.put(ChunkCoordIntPair.chunkXZ2Int(i, j), chunk);
                chunk.addEntities();
                chunk.loadNearby(this, this.chunkGenerator, false); // CraftBukkit
            }
        }

        return chunk;
    }

    // CraftBukkit start
    public Chunk getChunkIfLoaded(int x, int z) {
        return chunks.get(ChunkCoordIntPair.chunkXZ2Int(x, z));
    }
    // CraftBukkit end

    @Override
	public Chunk getChunkAt(int i, int j) {
        return getChunkAt(i, j, null);
    }

    public Chunk getChunkAt(int i, int j, Runnable runnable) {
        return getChunkAt(i, j, runnable, true);
    }

    public Chunk getChunkAt(int i, int j, Runnable runnable, boolean generate) {
        Chunk chunk = getChunkIfLoaded(i, j);
        ChunkRegionLoader loader = null;

        if (this.chunkLoader instanceof ChunkRegionLoader) {
            loader = (ChunkRegionLoader) this.chunkLoader;

        }
        // We can only use the queue for already generated chunks
        if (chunk == null && loader != null && loader.chunkExists(world, i, j)) {
            if (runnable != null) {
                ChunkIOExecutor.queueChunkLoad(world, loader, this, i, j, runnable);
                return null;
            } else {
                chunk = ChunkIOExecutor.syncChunkLoad(world, loader, this, i, j);
            }
        } else if (chunk == null && generate) {
            chunk = originalGetChunkAt(i, j);
        }

        // If we didn't load the chunk async and have a callback run it now
        if (runnable != null) {
            runnable.run();
        }

        return chunk;
    }

    public Chunk originalGetChunkAt(int i, int j) {
        Chunk chunk = this.originalGetOrLoadChunkAt(i, j);
        // CraftBukkit end

        if (chunk == null) {
            world.timings.syncChunkLoadTimer.startTiming(); // Spigot
            long k = ChunkCoordIntPair.chunkXZ2Int(i, j);

            try {
                chunk = this.chunkGenerator.getOrCreateChunk(i, j);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Exception generating new chunk");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Chunk to be generated");

                crashreportsystemdetails.a("Location", String.format("%d,%d", new Object[] { Integer.valueOf(i), Integer.valueOf(j)}));
                crashreportsystemdetails.a("Position hash", Long.valueOf(k));
                crashreportsystemdetails.a("Generator", this.chunkGenerator);
                throw new ReportedException(crashreport);
            }

            this.chunks.put(k, chunk);
            chunk.addEntities();
            chunk.loadNearby(this, this.chunkGenerator, true); // CraftBukkit
            world.timings.syncChunkLoadTimer.stopTiming(); // Spigot
        }

        return chunk;
    }

    @Nullable
    public Chunk loadChunk(int i, int j) {
        try {
            Chunk chunk = this.chunkLoader.a(this.world, i, j);

            if (chunk != null) {
                chunk.setLastSaved(this.world.getTime());
                this.chunkGenerator.recreateStructures(chunk, i, j);
            }

            return chunk;
        } catch (Exception exception) {
            // Paper start
            String msg = "Couldn\'t load chunk";
            ChunkProviderServer.a.error(msg, exception);
            ServerInternalException.reportInternalException(exception);
            // Paper end
            return null;
        }
    }

    public void saveChunkNOP(Chunk chunk) {
        try (co.aikar.timings.Timing timed = world.timings.chunkSaveNop.startTiming()) {
            this.chunkLoader.b(this.world, chunk);
        } catch (Exception exception) {
            // Paper start
            String msg = "Couldn\'t save entities";
            ChunkProviderServer.a.error(msg, exception);
            ServerInternalException.reportInternalException(exception);
            // Paper end
        }

    }

    public void saveChunk(Chunk chunk) {
        try (co.aikar.timings.Timing timed = world.timings.chunkSaveData.startTiming()) {
            chunk.setLastSaved(this.world.getTime());
            this.chunkLoader.a(this.world, chunk);
        } catch (IOException ioexception) {
            // Paper start
            String msg = "Couldn\'t save chunk";
            ChunkProviderServer.a.error(msg, ioexception);
            ServerInternalException.reportInternalException(ioexception);
        } catch (ExceptionWorldConflict exceptionworldconflict) {
            String msg = "Couldn\'t save chunk; already in use by another instance of Minecraft?";
            ChunkProviderServer.a.error(msg, exceptionworldconflict);
            ServerInternalException.reportInternalException(exceptionworldconflict);
        }

    }

    public boolean a(boolean flag) {
        int i = 0;

        // CraftBukkit start
        // Paper start
        final ChunkRegionLoader chunkLoader = (ChunkRegionLoader) world.getChunkProviderServer().chunkLoader;
        final int queueSize = chunkLoader.getQueueSize();

        final long now = System.currentTimeMillis();
        final long timeSince = (now - lastSaveStatPrinted) / 1000;
        final Integer printRateSecs = Integer.getInteger("printSaveStats");
        if (printRateSecs != null && timeSince >= printRateSecs) {
            final String timeStr = "/" + timeSince  +"s";
            final long queuedSaves = chunkLoader.getQueuedSaves();
            long queuedDiff = queuedSaves - lastQueuedSaves;
            lastQueuedSaves = queuedSaves;

            final long processedSaves = chunkLoader.getProcessedSaves();
            long processedDiff = processedSaves - lastProcessedSaves;
            lastProcessedSaves = processedSaves;

            lastSaveStatPrinted = now;
            if (processedDiff > 0 || queueSize > 0 || queuedDiff > 0) {
                System.out.println("[Chunk Save Stats] " + world.worldData.getName() +
                    " - Current: " + queueSize +
                    " - Queued: " + queuedDiff + timeStr +
                    " - Processed: " +processedDiff + timeStr
                );
            }
        }

        if (queueSize > world.paperConfig.queueSizeAutoSaveThreshold){
            return false;
        }
        final int autoSaveLimit = world.paperConfig.maxAutoSaveChunksPerTick;
        // Paper end
        Iterator iterator = this.chunks.values().iterator();
        while (iterator.hasNext()) {
            Chunk chunk = (Chunk) iterator.next();
            // CraftBukkit end

            if (flag) {
                this.saveChunkNOP(chunk);
            }

            if (chunk.a(flag)) {
                this.saveChunk(chunk);
                chunk.f(false);
                ++i;
                if (!flag && i >= autoSaveLimit) { // Spigot - // Paper - Incremental Auto Save - cap max per tick
                    return false;
                }
            }
        }

        return true;
    }

    public void c() {
        this.chunkLoader.b();
    }

    private static final double UNLOAD_QUEUE_RESIZE_FACTOR = 0.96;

    @Override
	public boolean unloadChunks() {
        if (!this.world.savingDisabled) {
            if (!this.unloadQueue.isEmpty()) {
                // Spigot start
                org.spigotmc.SlackActivityAccountant activityAccountant = this.world.getMinecraftServer().slackActivityAccountant;
                activityAccountant.startActivity(0.5);
                int targetSize = Math.min(this.unloadQueue.size() - 100,  (int) (this.unloadQueue.size() * UNLOAD_QUEUE_RESIZE_FACTOR)); // Paper - Make more aggressive
                // Spigot end

                Iterator iterator = this.unloadQueue.iterator();

                while (iterator.hasNext()) { // Spigot
                    Long olong = (Long) iterator.next();
                    iterator.remove(); // Spigot
                    Chunk chunk = this.chunks.get(olong);

                    if (chunk != null && chunk.d) {
                        // CraftBukkit start - move unload logic to own method
                        chunk.setShouldUnload(false); // Paper
                        if (!unloadChunk(chunk, true)) {
                            continue;
                        }
                        // CraftBukkit end

                        // Spigot start
                        if (this.unloadQueue.size() <= targetSize && activityAccountant.activityTimeIsExhausted()) {
                            break;
                        }
                        // Spigot end
                    }
                }

                activityAccountant.endActivity(); // Spigot
            }
            // Paper start - delayed chunk unloads
            long now = System.currentTimeMillis();
            long unloadAfter = world.paperConfig.delayChunkUnloadsBy;
            if (unloadAfter > 0) {
                //noinspection Convert2streamapi
                for (Chunk chunk : chunks.values()) {
                    if (chunk.scheduledForUnload != null && now - chunk.scheduledForUnload > unloadAfter) {
                        chunk.scheduledForUnload = null;
                        unload(chunk);
                    }
                }
            }
            // Paper end

            this.chunkLoader.a();
        }

        return false;
    }

    // CraftBukkit start
    public boolean unloadChunk(Chunk chunk, boolean save) {
        ChunkUnloadEvent event = new ChunkUnloadEvent(chunk.bukkitChunk, save);
        this.world.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        save = event.isSaveChunk();
        chunk.lightingQueue.processUnload(); // Paper

        // Update neighbor counts
        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                Chunk neighbor = this.getChunkIfLoaded(chunk.locX + x, chunk.locZ + z);
                if (neighbor != null) {
                    neighbor.setNeighborUnloaded(-x, -z);
                    chunk.setNeighborUnloaded(x, z);
                }
            }
        }
        // Moved from unloadChunks above
        chunk.removeEntities();
        if (save) {
            this.saveChunk(chunk);
            this.saveChunkNOP(chunk);
        }
        this.chunks.remove(chunk.chunkKey);
        return true;
    }
    // CraftBukkit end

    public boolean e() {
        return !this.world.savingDisabled;
    }

    @Override
	public String getName() {
        return "ServerChunkCache: " + this.chunks.size() + " Drop: " + this.unloadQueue.size();
    }

    public List<BiomeBase.BiomeMeta> a(EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
        return this.chunkGenerator.getMobsFor(enumcreaturetype, blockposition);
    }

    @Nullable
    public BlockPosition a(World world, String s, BlockPosition blockposition, boolean flag) {
        return this.chunkGenerator.findNearestMapFeature(world, s, blockposition, flag);
    }

    public int g() {
        return this.chunks.size();
    }

    public boolean isLoaded(int i, int j) {
        return this.chunks.containsKey(ChunkCoordIntPair.chunkXZ2Int(i, j));
    }

    @Override
	public boolean e(int i, int j) {
        return this.chunks.containsKey(ChunkCoordIntPair.chunkXZ2Int(i, j)) || this.chunkLoader.a(i, j);
    }
}
