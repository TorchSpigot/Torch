package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RecursiveTask;
import java.util.Map;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import com.destroystokyo.paper.exception.ServerInternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RecursiveTask;

// CraftBukkit start
import org.bukkit.Server;
import org.bukkit.craftbukkit.chunkio.ChunkIOExecutor;
import org.bukkit.event.world.ChunkUnloadEvent;
// CraftBukkit end

public class ChunkProviderServer implements IChunkProvider {

    private static final Logger a = LogManager.getLogger();
    //public final it.unimi.dsi.fastutil.longs.LongSet unloadQueue = new it.unimi.dsi.fastutil.longs.LongArraySet(); // PAIL: private -> public // Paper
	public final Set<Long> unloadQueue = Sets.newConcurrentHashSet(); // PAIL: private -> public
    public final ChunkGenerator chunkGenerator;
    private final IChunkLoader chunkLoader;
    // Paper start
    protected Chunk lastChunkByPos = null;
    public final Map<Long, Chunk> chunks = Maps.newConcurrentMap();
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
            this.unloadQueue.add(Long.valueOf(ChunkCoordIntPair.a(chunk.locX, chunk.locZ)));
            chunk.d = true;
        }

    }

    public void b() {
        Iterator iterator = this.chunks.values().iterator();

        while (iterator.hasNext()) {
            Chunk chunk = (Chunk) iterator.next();

            this.unload(chunk);
        }

    }

    @Nullable
    public Chunk getLoadedChunkAt(int i, int j) {
        long k = ChunkCoordIntPair.a(i, j);
        Chunk chunk = (Chunk) this.chunks.get(k);

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
                this.chunks.put(ChunkCoordIntPair.a(i, j), chunk);
                chunk.addEntities();
                chunk.loadNearby(this, this.chunkGenerator, false); // CraftBukkit
            }
        }

        return chunk;
    }

    // CraftBukkit start
    public Chunk getChunkIfLoaded(int x, int z) {
        return chunks.get(ChunkCoordIntPair.a(x, z));
    }
    // CraftBukkit end

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
        boolean newChunk = false;
        // CraftBukkit end

        if (chunk == null) {
            
            long k = ChunkCoordIntPair.a(i, j);

            try {
                chunk = this.chunkGenerator.getOrCreateChunk(i, j);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Exception generating new chunk");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Chunk to be generated");

                crashreportsystemdetails.a("Location", (Object) String.format("%d,%d", new Object[] { Integer.valueOf(i), Integer.valueOf(j)}));
                crashreportsystemdetails.a("Position hash", (Object) Long.valueOf(k));
                crashreportsystemdetails.a("Generator", (Object) this.chunkGenerator);
                throw new ReportedException(crashreport);
            }
            //newChunk = true; // CraftBukkit

            this.chunks.put(k, chunk);
            chunk.addEntities();

            chunk.loadNearby(this, this.chunkGenerator, true); // CraftBukkit
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
        try {
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
        try {
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
                if (i == 24 && !flag && false) { // Spigot
                    return false;
                }
            }
        }

        return true;
    }

    public void c() {
        this.chunkLoader.b();
    }

    public boolean unloadChunks() {
        if (!this.world.savingDisabled) {
            if (!this.unloadQueue.isEmpty()) {
                Iterator iterator = this.unloadQueue.iterator();

                for (int i = 0; i < 100 && iterator.hasNext(); iterator.remove()) {
                    Long olong = (Long) iterator.next();
                    Chunk chunk = (Chunk) this.chunks.get(olong);

                    if (chunk != null && chunk.d) {
                        chunk.setShouldUnload(false); // Paper
                        // CraftBukkit start
                        //ChunkUnloadEvent event = new ChunkUnloadEvent(chunk.bukkitChunk);
                        //this.world.getServer().getPluginManager().callEvent(event);
                        //if (event.isCancelled()) {
						// CraftBukkit start - move unload logic to own method
                        if (!unloadChunk(chunk, true)) {
                            continue;
                        }
                        chunk.lightingQueue.processUnload(); // Paper

						// Torch - backport
						/*
                        // Update neighbor counts
                        for (int x = -2; x < 3; x++) {
                            for (int z = -2; z < 3; z++) {
                                if (x == 0 && z == 0) {
                                    continue;
                                }

                                Chunk neighbor = MCUtil.getLoadedChunkWithoutMarkingActive(this, chunk.locX + x, chunk.locZ + z); // Paper
                                if (neighbor != null) {
                                    neighbor.setNeighborUnloaded(-x, -z);
                                    chunk.setNeighborUnloaded(x, z);
                                }
                            }
                        }
						*/
                        // CraftBukkit end

                        chunk.removeEntities();
                        //this.saveChunk(chunk);
                        //this.saveChunkNOP(chunk);
                        //this.chunks.remove(olong);
                        ++i;
                    }
                }
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
		ChunkUnloadEvent event = new ChunkUnloadEvent(chunk.bukkitChunk);
		this.world.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}

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

    public String getName() {
        return "ServerChunkCache: " + this.chunks.size() + " Drop: " + this.unloadQueue.size();
    }

    public List<BiomeBase.BiomeMeta> a(EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
        return this.chunkGenerator.getMobsFor(enumcreaturetype, blockposition);
    }

    @Nullable
    public BlockPosition a(World world, String s, BlockPosition blockposition) {
        return this.chunkGenerator.findNearestMapFeature(world, s, blockposition);
    }

    public int g() {
        return this.chunks.size();
    }

    public boolean isLoaded(int i, int j) {
        return this.chunks.containsKey(ChunkCoordIntPair.a(i, j));
    }
	
	/*
 class getChunkIL extends RecursiveTask<Chunk> {
     //Map<Long, Chunk> chunks;
     int x, z;
             
     getChunkIL (int x, int z) {
         //this.chunks = chunks;
         this.x = x;
         this.z = z;
     }
     
     @Override
     protected Chunk compute() {
         return chunks.get(ChunkCoordIntPair.a(x, z));
     }
              
 }*/
 /*
 class isL extends RecursiveTask<Boolean> {
     //Map<Long, Chunk> chunks;
     int i, j;
             
     isL (int i, int j) {
         //this.chunks = chunks;
         this.i = i;
         this.j = j;
     }
     
     @Override
     protected Boolean compute() {
         return chunks.containsKey(ChunkCoordIntPair.a(i, j));
     }
              
 }*/
}
