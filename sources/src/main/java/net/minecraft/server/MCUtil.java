package net.minecraft.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.spigotmc.AsyncCatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class MCUtil {
    private static final Executor asyncExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Paper Async Task Handler Thread - %1$d").build());

    private MCUtil() {}

    /**
     * Quickly generate a stack trace for current location
     *
     * @return Stacktrace
     */
    public static String stack() {
        return ExceptionUtils.getFullStackTrace(new Throwable());
    }

    /**
     * Quickly generate a stack trace for current location with message
     *
     * @param str
     * @return Stacktrace
     */
    public static String stack(String str) {
        return ExceptionUtils.getFullStackTrace(new Throwable(str));
    }

    /**
     * Ensures the target code is running on the main thread
     * @param reason
     * @param run
     * @param <T>
     * @return
     */
    public static <T> T ensureMain(String reason, Supplier<T> run) {
        AsyncCatcher.catchOp(reason);
        /* new IllegalStateException( "Asynchronous " + reason + "! Blocking thread until it returns ").printStackTrace(); // This never return
        Waitable<T> wait = new Waitable<T>() {
            @Override
            protected T evaluate() {
                return run.get();
            }
        };
        MinecraftServer.getServer().processQueue.add(wait);
        try {
            return wait.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null; */
        return run.get();
    }

    /**
     * Calculates distance between 2 entities
     * @param e1
     * @param e2
     * @return
     */
    public static double distance(Entity e1, Entity e2) {
        return Math.sqrt(distanceSq(e1, e2));
    }


    /**
     * Calculates distance between 2 block positions
     * @param e1
     * @param e2
     * @return
     */
    public static double distance(BlockPosition e1, BlockPosition e2) {
        return Math.sqrt(distanceSq(e1, e2));
    }

    /**
     * Gets the distance between 2 positions
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(distanceSq(x1, y1, z1, x2, y2, z2));
    }

    /**
     * Get's the distance squared between 2 entities
     * @param e1
     * @param e2
     * @return
     */
    public static double distanceSq(Entity e1, Entity e2) {
        return distanceSq(e1.locX,e1.locY,e1.locZ, e2.locX,e2.locY,e2.locZ);
    }

    /**
     * Gets the distance sqaured between 2 block positions
     * @param pos1
     * @param pos2
     * @return
     */
    public static double distanceSq(BlockPosition pos1, BlockPosition pos2) {
        return distanceSq(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    /**
     * Gets the distance squared between 2 positions
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static double distanceSq(double x1, double y1, double z1, double x2, double y2, double z2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);
    }

    /**
     * Converts a NMS World/BlockPosition to Bukkit Location
     * @param world
     * @param pos
     * @return
     */
    public static Location toLocation(World world, BlockPosition pos) {
        return new Location(world.getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Converts an NMS entity's current location to a Bukkit Location
     * @param entity
     * @return
     */
    public static Location toLocation(Entity entity) {
        return new Location(entity.getWorld().getWorld(), entity.locX, entity.locY, entity.locZ);
    }

    public static BlockPosition toBlockPosition(Location loc) {
        return new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static boolean isEdgeOfChunk(BlockPosition pos) {
        final int modX = pos.getX() & 15;
        final int modZ = pos.getZ() & 15;
        return (modX == 0 || modX == 15 || modZ == 0 || modZ == 15);
    }

    /**
     * Gets a chunk without changing its boolean for should unload
     * @param world
     * @param x chunkX
     * @param z chunkZ
     * @return
     */
    @Nullable
    public static Chunk getLoadedChunkWithoutMarkingActive(World world, int x, int z) {
        return ((ChunkProviderServer) world.chunkProvider).chunks.get(ChunkCoordIntPair.chunkXZ2Int(x, z));
    }

    /**
     * Gets a chunk without changing its boolean for should unload
     * @param provider
     * @param x chunkX
     * @param z chunkZ
     * @return
     */
    @Nullable
    public static Chunk getLoadedChunkWithoutMarkingActive(IChunkProvider provider, int x, int z) {
        return ((ChunkProviderServer) provider).chunks.get(ChunkCoordIntPair.chunkXZ2Int(x, z));
    }

    /**
     * Posts a task to be executed asynchronously
     * @param run
     */
    public static void scheduleAsyncTask(Runnable run) {
        asyncExecutor.execute(run);
    }

    @Nullable
    public static TileEntityHopper getHopper(World world, BlockPosition pos) {
        Chunk chunk = world.getChunkIfLoaded(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk != null && chunk.getBlockData(pos).getBlock() == Blocks.HOPPER) {
            TileEntity tileEntity = chunk.getTileEntityImmediately(pos);
            if (tileEntity instanceof TileEntityHopper) {
                return (TileEntityHopper) tileEntity;
            }
        }
        return null;
    }

    @Nonnull
    public static World getNMSWorld(@Nonnull org.bukkit.World world) {
        return ((CraftWorld) world).getHandle();
    }

    public static World getNMSWorld(@Nonnull org.bukkit.entity.Entity entity) {
        return getNMSWorld(entity.getWorld());
    }
}
