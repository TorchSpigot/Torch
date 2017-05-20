package org.torch.server;

import lombok.Getter;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.ChunkGenerator;
import net.minecraft.server.ChunkProviderServer;
import net.minecraft.server.ChunkRegionLoader;
import net.minecraft.server.CrashReport;
import net.minecraft.server.CrashReportSystemDetails;
import net.minecraft.server.EnumCreatureType;
import net.minecraft.server.ExceptionWorldConflict;
import net.minecraft.server.IChunkLoader;
import net.minecraft.server.ReportedException;
import net.minecraft.server.World;
import net.minecraft.server.WorldServer;

import org.bukkit.craftbukkit.chunkio.ChunkIOExecutor;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.spigotmc.SlackActivityAccountant;
import org.torch.api.TorchReactor;

import com.destroystokyo.paper.exception.ServerInternalException;

import co.aikar.timings.Timing;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import static org.torch.server.TorchServer.logger;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

@Getter
public final class TorchChunkProvider implements net.minecraft.server.IChunkProvider, org.torch.api.IChunkProvider, TorchReactor {
    private final ChunkProviderServer servant;

    public static final double UNLOAD_QUEUE_RESIZE_FACTOR = 0.96;

    public final LongSet unloadQueue = new LongArraySet();

    // Paper - Chunk save stats
    private long lastQueuedSaves = 0L; 
    private long lastProcessedSaves = 0L;
    private long lastSaveStatPrinted = System.currentTimeMillis();

    protected Chunk lastChunkByPos = null;

    public final WorldServer world;
    
    /** Map of chunk Id's to Chunk instances */
    public Long2ObjectOpenHashMap<Chunk> chunks = new Long2ObjectOpenHashMap<Chunk>(8192) {
        private static final long serialVersionUID = -1L;

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
    };

    public TorchChunkProvider(WorldServer world, IChunkLoader loader, ChunkGenerator generator, ChunkProviderServer servant) {
        this.servant = servant;

        this.world = world;
    }

    public Collection<Chunk> getLoadedChunks() {
        return this.chunks.values();
    }

    /**
     * Post a chunk to unload queue
     */
    public void postChunkToUnload(Chunk chunk) {
        if (this.world.worldProvider.c(chunk.locX, chunk.locZ)) {
            this.unloadQueue.add(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunk.locX, chunk.locZ)));
            chunk.d = true; // PAIL: unloaded
        }
    }

    /**
     * Marks all chunks for unload, ignoring those near the spawn
     */
    public void unloadAllChunks() {
        ObjectIterator<Chunk> it = this.chunks.values().iterator();
        while (it.hasNext()) this.postChunkToUnload(it.next());
    }

    @Nullable
    public Chunk getChunkIfLoaded(int chunkX, int chunkZ, boolean markUnloaded) {
        return markUnloaded ? getLoadedChunkAt(chunkX, chunkZ) : chunks.get(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
    }

    @Override @Nullable
    public Chunk getLoadedChunkAt(int chunkX, int chunkZ) {
        Chunk chunk = this.chunks.get(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));

        if (chunk != null) chunk.d = false; // PAIL: unloaded
        return chunk;
    }

    @Nullable
    public Chunk getOrLoadChunkAt(int x, int z) {
        Chunk chunk = this.getLoadedChunkAt(x, z);

        if (chunk == null) {
            ChunkRegionLoader loader = null;

            if (servant.chunkLoader instanceof ChunkRegionLoader) {
                loader = (ChunkRegionLoader) servant.chunkLoader;
            }
            if (loader != null && loader.chunkExists(x, z)) {
                chunk = ChunkIOExecutor.syncChunkLoad(world, loader, servant, x, z);
            }
        }

        return chunk;
    }

    @Nullable
    public Chunk originalGetOrLoadChunkAt(int chunkX, int chunkZ) {
        Chunk chunk = this.getLoadedChunkAt(chunkX, chunkZ);

        if (chunk == null) {
            chunk = this.loadChunkFromFile(chunkX, chunkZ);
            if (chunk != null) {
                this.chunks.put(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ), chunk);
                chunk.addEntities();
                chunk.loadNearby(servant, servant.chunkGenerator, false);
            }
        }

        return chunk;
    }

    @Override
    public Chunk getChunkAt(int i, int j) {
        return getChunkAt(i, j, null);
    }

    public Chunk getChunkAt(int i, int j, Runnable runnable) {
        return getChunkAt(i, j, runnable, true);
    }

    public Chunk getChunkAt(int i, int j, Runnable runnable, boolean generate) {
        Chunk chunk = getChunkIfLoaded(i, j, false);
        ChunkRegionLoader loader = null;

        if (servant.chunkLoader instanceof ChunkRegionLoader) {
            loader = (ChunkRegionLoader) servant.chunkLoader;
        }

        // We can only use the queue for already generated chunks
        if (chunk == null && loader != null && loader.chunkExists(i, j)) {
            if (runnable != null) {
                ChunkIOExecutor.queueChunkLoad(world, loader, servant, i, j, runnable);
                return null;
            } else {
                chunk = ChunkIOExecutor.syncChunkLoad(world, loader, servant, i, j);
            }
        } else if (chunk == null && generate) {
            chunk = originalGetChunkAt(i, j);
        }

        // If we didn't load the chunk async and have a callback run it now
        if (runnable != null) runnable.run();

        return chunk;
    }

    public Chunk originalGetChunkAt(int chunkX, int chunkZ) {
        Chunk chunk = this.originalGetOrLoadChunkAt(chunkX, chunkZ);

        if (chunk == null) {
            world.timings.syncChunkLoadTimer.startTiming();
            long chunkPos = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);

            try {
                chunk = servant.chunkGenerator.getOrCreateChunk(chunkX, chunkZ);
            } catch (Throwable t) {
                CrashReport crashReport = CrashReport.a(t, "Exception generating new chunk");
                CrashReportSystemDetails details = crashReport.a("Chunk to be generated");

                details.a("Location", String.format("%d,%d", new Object[] { Integer.valueOf(chunkX), Integer.valueOf(chunkZ)}));
                details.a("Position hash", Long.valueOf(chunkPos));
                details.a("Generator", servant.chunkGenerator);
                throw new ReportedException(crashReport);
            }

            this.chunks.put(chunkPos, chunk);
            chunk.addEntities();
            chunk.loadNearby(servant, servant.chunkGenerator, true);
            
            world.timings.syncChunkLoadTimer.stopTiming();
        }

        return chunk;
    }

    @Nullable
    public Chunk loadChunkFromFile(int i, int j) {
        try {
            Chunk chunk = servant.chunkLoader.a(this.world, i, j);

            if (chunk != null) {
                chunk.setLastSaved(this.world.getTime());
                servant.chunkGenerator.recreateStructures(chunk, i, j);
            }

            return chunk;
        } catch (Throwable t) {
            logger.error("Couldn\'t load chunk", t);
            ServerInternalException.reportInternalException(t);
            return null;
        }
    }

    public void saveChunkExtraData(Chunk chunk) {
        try (Timing timed = world.timings.chunkSaveNop.startTiming()) {
            servant.chunkLoader.b(this.world, chunk); // PAIL: saveChunkExtraData
        } catch (Throwable t) {
            logger.error("Couldn\'t save entities", t);
            ServerInternalException.reportInternalException(t);
        }
    }

    public void saveChunkData(Chunk chunk) {
        try (Timing timed = world.timings.chunkSaveData.startTiming()) {
            chunk.setLastSaved(this.world.getTime());
            servant.chunkLoader.a(this.world, chunk); // PAIL: saveChunkData
        } catch (IOException io) {
            logger.error("Couldn\'t save chunk", io);
            ServerInternalException.reportInternalException(io);
        } catch (ExceptionWorldConflict conflict) {
            logger.error("Couldn\'t save chunk; already in use by another instance of Minecraft?", conflict);
            ServerInternalException.reportInternalException(conflict);
        }
    }

    public boolean saveChunks(boolean saveExtraData) {
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
                logger.info("[Chunk Save Stats] " + world.worldData.getName() +
                        " - Current: " + queueSize +
                        " - Queued: " + queuedDiff + timeStr +
                        " - Processed: " +processedDiff + timeStr
                        );
            }
        }

        if (queueSize > world.paperConfig.queueSizeAutoSaveThreshold) return false;
        int savedChunkCount = 0;

        for (Chunk chunk : this.chunks.values()) {
            if (saveExtraData) this.saveChunkExtraData(chunk);

            if (chunk.a(saveExtraData)) { // If the chunk needs saving
                this.saveChunkData(chunk);
                chunk.f(false); // PAIL: setModified(false)
                savedChunkCount++;

                // Paper - Incremental Auto Save - cap max per tick
                if (!saveExtraData && savedChunkCount >= world.paperConfig.maxAutoSaveChunksPerTick) return false;
            }
        }

        return true;
    }

    /**
     * Save extra data not associated with any Chunk.
     * Not saved during autosave, only during world unload. Currently unimplemented.
     */
    public void saveExtraData() {
        servant.chunkLoader.b(); // PAIL: saveExtraData()
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean unloadChunks() {
        if (this.world.savingDisabled) return false;

        if (!this.unloadQueue.isEmpty()) {
            SlackActivityAccountant activityAccountant = this.world.getMinecraftServer().slackActivityAccountant;
            activityAccountant.startActivity(0.5);

            // Paper - Make more aggressive
            int targetSize = Math.min(this.unloadQueue.size() - 100,  (int) (this.unloadQueue.size() * UNLOAD_QUEUE_RESIZE_FACTOR));

            for (Long chunkPos : this.unloadQueue) {
                this.unloadQueue.remove(chunkPos);
                Chunk chunk = this.chunks.get(chunkPos);

                if (chunk != null && chunk.d) { // PAIL: chunk.unloaded
                    chunk.setShouldUnload(false);

                    if (!unloadChunk(chunk, true)) continue;

                    if (this.unloadQueue.size() <= targetSize && activityAccountant.activityTimeIsExhausted()) break;
                }
            }

            activityAccountant.endActivity(); // Spigot
        }

        // Paper - delayed chunk unloads
        long now = System.currentTimeMillis();
        long unloadAfter = world.paperConfig.delayChunkUnloadsBy;

        if (unloadAfter > 0) {
            for (Chunk chunk : chunks.values()) { // TODO: Convert2streamapi
                if (chunk.scheduledForUnload != null && now - chunk.scheduledForUnload > unloadAfter) {
                    chunk.scheduledForUnload = null;
                    this.postChunkToUnload(chunk);
                }
            }
        }

        servant.chunkLoader.a(); // PAIL: chunkTick()

        return false;
    }

    public boolean unloadChunk(Chunk chunk, boolean save) {
        ChunkUnloadEvent event = new ChunkUnloadEvent(chunk.bukkitChunk, save);
        this.world.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;
        save = event.isSaveChunk();

        chunk.lightingQueue.processUnload();

        // Update neighbor counts
        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
                if (x == 0 && z == 0) continue;

                Chunk neighbor = this.getChunkIfLoaded(chunk.locX + x, chunk.locZ + z, false);
                if (neighbor != null) {
                    neighbor.setNeighborUnloaded(-x, -z);
                    chunk.setNeighborUnloaded(x, z);
                }
            }
        }

        chunk.removeEntities();

        if (save) {
            this.saveChunkData(chunk);
            this.saveChunkExtraData(chunk);
        }

        this.chunks.remove(chunk.chunkKey);
        return true;
    }

    /**
     * Returns if the world supports saving
     */
    public boolean canSave() {
        return !this.world.savingDisabled;
    }

    /**
     * Converts the instance data to a readable string
     */
    @Override
    public String getName() {
        return "ServerChunkCache: " + this.chunks.size() + " Drop: " + this.unloadQueue.size();
    }

    public List<BiomeBase.BiomeMeta> getPossibleCreatures(EnumCreatureType creatureType, BlockPosition position) {
        return servant.chunkGenerator.getMobsFor(creatureType, position);
    }

    @Nullable
    public BlockPosition findNearestMapFeature(World world, String structureName, BlockPosition position, boolean flag) {
        return servant.chunkGenerator.findNearestMapFeature(world, structureName, position, flag);
    }

    public int getLoadedChunkCount() {
        return this.chunks.size();
    }

    /**
     * Checks to see if a chunk exists at x, z
     */
    public boolean isLoaded(int chunkX, int chunkZ) {
        return this.chunks.containsKey(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
    }

    @Override @Deprecated public boolean e(int x, int z) { return this.isChunkGeneratedAt(x, z); } // Implement from net.minecraft.IChunkProvider
    @Override public boolean isChunkGeneratedAt(int chunkX, int chunkZ) {
        return this.chunks.containsKey(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)) || servant.chunkLoader.a(chunkX, chunkZ); // PAIL: a -> isChunkGeneratedAt(x, z)
    }
}
