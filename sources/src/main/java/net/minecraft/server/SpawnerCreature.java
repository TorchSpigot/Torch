package net.minecraft.server;

import java.util.Random;
import java.util.Set;

import lombok.Getter;

// CraftBukkit end
import org.torch.server.TorchCreatureSpawner;

public final class SpawnerCreature implements org.torch.api.TorchServant {
	@Getter private final TorchCreatureSpawner reactor;
	
    // private static final int a = TorchCreatureSpawner.MOB_COUNT_DIV;
    private final Set b; // CraftBukkit // Torch

    public SpawnerCreature() {
    	reactor = new TorchCreatureSpawner(this);
    	
    	b = reactor.getSpawnableChunks();
    }

    // Spigot start - get entity count only from chunks being processed in b
    private int getEntityCount(WorldServer server, Class oClass) {
    	return reactor.getEntityCount(server, oClass);
        // Paper end
        /* int i = 0;
        Iterator<Long> it = this.b.iterator();
        while ( it.hasNext() )
        {
            Long coord = it.next();
            int x = LongHash.msw( coord );
            int z = LongHash.lsw( coord );
            if ( !((ChunkProviderServer)server.chunkProvider).unloadQueue.contains( coord ) && server.isChunkLoaded( x, z, true ) )
            {
                i += server.getChunkAt( x, z ).entityCount.get( oClass );
            }
        }
        return i; */
    }
    // Spigot end

    public int a(WorldServer worldserver, boolean flag, boolean flag1, boolean flag2) {
        return reactor.findChunksForSpawning(worldserver, flag, flag1, flag2);
    }

    private static BlockPosition getRandomPosition(World world, int i, int j) {
        return TorchCreatureSpawner.createRandomPosition(world, i, j);
    }

    public static boolean a(IBlockData iblockdata) {
        return TorchCreatureSpawner.isValidEmptySpawnBlock(iblockdata);
    }

    public static boolean a(EntityInsentient.EnumEntityPositionType entityinsentient_enumentitypositiontype, World world, BlockPosition blockposition) {
        return TorchCreatureSpawner.canCreatureTypeSpawnAtLocation(entityinsentient_enumentitypositiontype, world, blockposition);
    }

    public static void a(World world, BiomeBase biomebase, int i, int j, int k, int l, Random random) {
        TorchCreatureSpawner.performWorldGenerateSpawning(world, biomebase, i, j, k, l, random);
    }
}
