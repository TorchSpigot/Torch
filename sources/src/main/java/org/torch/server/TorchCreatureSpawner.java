package org.torch.server;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import lombok.Getter;

import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.craftbukkit.util.LongHashSet;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.spigotmc.AsyncCatcher;
import org.torch.api.TorchReactor;

import com.destroystokyo.paper.exception.ServerInternalException;

import net.minecraft.server.*;
import net.minecraft.server.BiomeBase.BiomeMeta;

@Getter
public final class TorchCreatureSpawner implements TorchReactor {
	private final SpawnerCreature servant;
	
	public static final int MOB_COUNT_DIV = (int) Math.pow(17.0D, 2.0D);
	/** The 17x17 area around the player where mobs can spawn */
    private final LongHashSet spawnableChunks = new LongHashSet();

    public TorchCreatureSpawner(SpawnerCreature legacy) {
    	servant = legacy;
    }
    
    /** Returns entity count only from chunks being processed in spawnableChunks */
    public int getEntityCount(WorldServer server, Class<?> creatureType) {
        // Paper - use entire world, not just active chunks. Spigot broke vanilla expectations.
    	return server
                .getChunkProviderServer().getReactor()
                .chunks.values()
                .stream()
                .collect(java.util.stream.Collectors.summingInt(chunk -> chunk.entityCount.getOrDefault(creatureType, 0)));
    }
    
    /**
     * Adds all chunks within the spawn radius of the players to spawnableChunks.
     * Returns number of spawnable chunks
     */
    public int findChunksForSpawning(WorldServer world, boolean spawnHostileCreatures, boolean spawnPassiveCreatures, boolean spawnOnSetTickRate) {
    	// Paper - At least until we figure out what is calling this async
    	AsyncCatcher.catchOp("check for eligible spawn chunks");
        if (!spawnHostileCreatures && !spawnPassiveCreatures) return 0;
        
        this.spawnableChunks.clear();
        int foundChunks = 0;
        
        for (EntityHuman player : world.players) {
        	if (player.isSpectator() || !player.affectsSpawning) continue;
        	
        	int chunkX = MathHelper.floor(player.locX / 16.0D);
        	int chunkZ = MathHelper.floor(player.locZ / 16.0D);
            
            byte spawnRange = world.spigotConfig.mobSpawnRange;
            spawnRange = (spawnRange > world.spigotConfig.viewDistance) ? (byte) world.spigotConfig.viewDistance : spawnRange;
            spawnRange = (spawnRange > 8) ? 8 : spawnRange;
            
            for (int levelX = -spawnRange; levelX <= spawnRange; levelX++) {
                for (int levelZ = -spawnRange; levelZ <= spawnRange; levelZ++) {
                    boolean reachedEdge = levelX == -spawnRange || levelX == spawnRange || levelZ == -spawnRange || levelZ == spawnRange;
                    ChunkCoordIntPair chunkPair = new ChunkCoordIntPair(levelX + chunkX, levelZ + chunkZ);
                    
                    // CraftBukkit - use LongHash and LongHashSet
                    long chunkCoords = LongHash.toLong(chunkPair.x, chunkPair.z);
                    if (!this.spawnableChunks.contains(chunkCoords)) {
                    	foundChunks++;
                        
                        if (!reachedEdge && world.getWorldBorder().isInBounds(chunkPair)) {
                            PlayerChunk currentChunk = world.getPlayerChunkMap().getChunk(chunkPair.x, chunkPair.z);
                            
                            if (currentChunk != null && currentChunk.e()) this.spawnableChunks.add(chunkCoords);
                        }
                    }
                }
            }
        }
        
        int spawnableChunks = 0;
        BlockPosition spawnPosition = world.getSpawn();
        for (EnumCreatureType creatureType : EnumCreatureType.values()) {
            // CraftBukkit - use per-world spawn limits
            int spawnLimit = creatureType.b();
            switch (creatureType) {
                case MONSTER:
                	spawnLimit = world.getWorld().getMonsterSpawnLimit();
                    break;
                case CREATURE:
                	spawnLimit = world.getWorld().getAnimalSpawnLimit();
                    break;
                case WATER_CREATURE:
                	spawnLimit = world.getWorld().getWaterAnimalSpawnLimit();
                    break;
                case AMBIENT:
                	spawnLimit = world.getWorld().getAmbientSpawnLimit();
                    break;
            }

            if (spawnLimit == 0) continue;
			int mobCount = 0;
			
            if ((!creatureType.d() || spawnPassiveCreatures) && (creatureType.d() || spawnHostileCreatures) && (!creatureType.e() || spawnOnSetTickRate)) {
            	// Paper - use 17x17 like vanilla (a at top of file)
                if ((mobCount = getEntityCount(world, creatureType.a())) <= spawnLimit * foundChunks / 289) {
                	
                    BlockPosition.MutableBlockPosition mutablePosition = new BlockPosition.MutableBlockPosition();
                    Iterator chunkIterator = this.spawnableChunks.iterator();
                    
                    int mobLimit = (spawnLimit * foundChunks / 256) - mobCount + 1; // Spigot - up to 1 more than limit
                    
                    try {
                    	LoopChunks:
                            while (chunkIterator.hasNext() && (mobLimit > 0)) { // Spigot - while more allowed
                                // CraftBukkit - Use LongHash and LongObjectHashMap
                                long key = ((Long) chunkIterator.next()).longValue();
                                BlockPosition randomPos = getRandomPosition(world, LongHash.msw(key), LongHash.lsw(key));
                                int randomX = randomPos.getX();
                                int randomY = randomPos.getY();
                                int randomZ = randomPos.getZ();
                                
                                if (!world.getType(randomPos).m()) {
                                    int spawnedEntity = 0;
                                    int reLoopTimes = 0;
                                    
                                    while (reLoopTimes < 3) {
                                        int j3 = randomX;
                                        int k3 = randomY;
                                        int l3 = randomZ;
                                        BiomeBase.BiomeMeta biomeMeta = null;
                                        GroupDataEntity entityGroup = null;
                                        int i4 = MathHelper.ceil(Math.random() * 4.0D);
                                        int j4 = 0;
                                        
                                        while (true) {
                                            if (j4 < i4) {
                                                ReLoop: {
                                                    j3 += world.random.nextInt(6) - world.random.nextInt(6);
                                                    k3 += world.random.nextInt(1) - world.random.nextInt(1);
                                                    l3 += world.random.nextInt(6) - world.random.nextInt(6);
                                                    mutablePosition.c(j3, k3, l3);
                                                    float f = j3 + 0.5F;
                                                    float f1 = l3 + 0.5F;
                                                    
                                                    if (!world.isPlayerNearby(f, k3, f1, 24.0D) && spawnPosition.distanceSquared(f, k3, f1) >= 576.0D) {
                                                        if (biomeMeta == null) {
                                                        	biomeMeta = world.getSpawnListEntryForTypeAt(creatureType, mutablePosition);
                                                        	
                                                            if (biomeMeta == null) break ReLoop;
                                                        }
                                                        
                                                        if (world.a(creatureType, biomeMeta, mutablePosition) && canCreatureTypeSpawnAtLocation(EntityPositionTypes.a(biomeMeta.b), world, mutablePosition)) {
                                                            EntityInsentient entity;
                                                            
                                                            entity = biomeMeta.b.getConstructor(new Class[] { World.class}).newInstance(new Object[] { world});
                                                            
                                                            entity.setPositionRotation(f, k3, f1, world.random.nextFloat() * 360.0F, 0.0F);
                                                            if (entity.isNotColliding() && entity.canSpawn()) {
                                                            	entityGroup = entity.prepare(world.D(new BlockPosition(entity)), entityGroup);
                                                            	
                                                                if (entity.canSpawn()) {
                                                                    if (world.addEntity(entity, SpawnReason.NATURAL)) {
                                                                    	++spawnedEntity;
                                                                    	mobLimit--;
                                                                    }
                                                                } else {
                                                                	entity.die();
                                                                }
                                                                
                                                                // Spigot - If we're past limit, stop spawn
                                                                if (mobLimit <= 0) continue LoopChunks;
                                                            }
                                                            
                                                            spawnableChunks += spawnedEntity;
                                                        }
                                                    }
                                                    
                                                    ++j4;
                                                    continue;
                                                }
                                            }
                                            
                                            ++reLoopTimes;
                                            break;
                                        }
                                    }
                                }
                            }
                    		
                    } catch (Throwable t) {
                        t.printStackTrace();
                        ServerInternalException.reportInternalException(t);
                        return spawnableChunks;
                    }
                    
                }
            }
        }

        return spawnableChunks;
    }
    
    public static BlockPosition getRandomPosition(World world, int x, int z) {
        Chunk chunk = world.getChunkAt(x, z);
        int k = x * 16 + world.random.nextInt(16);
        int l = z * 16 + world.random.nextInt(16);
        int i1 = MathHelper.roundUp(chunk.e(new BlockPosition(k, 0, l)) + 1, 16);
        int j1 = world.random.nextInt(i1 > 0 ? i1 : chunk.g() + 16 - 1);
        
        return new BlockPosition(k, j1, l);
    }
    
    public static boolean isValidEmptySpawnBlock(IBlockData blockData) {
        return blockData.l() ? false : (blockData.n() ? false : (blockData.getMaterial().isLiquid() ? false : !BlockMinecartTrackAbstract.i(blockData)));
    }
    
    public static boolean canCreatureTypeSpawnAtLocation(EntityInsentient.EnumEntityPositionType entityPosition, World world, BlockPosition position) {
        if (!world.getWorldBorder().a(position)) {
            return false;
        } else {
            IBlockData blockType = world.getType(position);
            
            if (entityPosition == EntityInsentient.EnumEntityPositionType.IN_WATER) {
                return blockType.getMaterial() == Material.WATER && world.getType(position.down()).getMaterial() == Material.WATER && !world.getType(position.up()).m();
            } else {
                BlockPosition downPosition = position.down();
                
                if (!world.getType(downPosition).r()) {
                    return false;
                } else {
                    Block downBlock = world.getType(downPosition).getBlock();
                    boolean downVaild = downBlock != Blocks.BEDROCK && downBlock != Blocks.BARRIER;
                    
                    return downVaild && isValidEmptySpawnBlock(blockType) && isValidEmptySpawnBlock(world.getType(position.up()));
                }
            }
        }
    }
    
    /**
     * Called during chunk generation to spawn initial creatures
     */
    public static void performWorldGenSpawning(World world, BiomeBase biome, int i, int j, int k, int l, Random random) {
        List<BiomeMeta> creatures = biome.getMobs(EnumCreatureType.CREATURE);
        if (creatures.isEmpty()) return;
        
        while (random.nextFloat() < biome.getSpawningChance()) {
            BiomeBase.BiomeMeta biomeMeta = WeightedRandom.a(world.random, creatures);
            int groupCount = biomeMeta.c + random.nextInt(1 + biomeMeta.d - biomeMeta.c);
            GroupDataEntity entityGroup = null;
            int x = i + random.nextInt(k);
            int z = j + random.nextInt(l);
            int l1 = x;
            int i2 = z;
            
            for (int j2 = 0; j2 < groupCount; ++j2) {
                boolean spawned = false;
                
                for (int loop = 0; !spawned && loop < 4; ++loop) {
                    BlockPosition position = world.getTopSolidOrLiquidBlock(new BlockPosition(x, 0, z));
                    
                    if (canCreatureTypeSpawnAtLocation(EntityInsentient.EnumEntityPositionType.ON_GROUND, world, position)) {
                        EntityInsentient entity;
                        
                        try {
                            entity = biomeMeta.b.getConstructor(new Class[] { World.class}).newInstance(new Object[] { world});
                        } catch (Throwable t) {
                            t.printStackTrace();
                            ServerInternalException.reportInternalException(t);
                            continue;
                        }
                        
                        entity.setPositionRotation(x + 0.5F, position.getY(), z + 0.5F, random.nextFloat() * 360.0F, 0.0F);
                        // CraftBukkit Added a reason for spawning this creature, moved entityinsentient.prepare(groupdataentity) up
                        entityGroup = entity.prepare(world.D(new BlockPosition(entity)), entityGroup);
                        world.addEntity(entity, SpawnReason.CHUNK_GEN);
                        
                        spawned = true;
                    }
                    
                    x += random.nextInt(5) - random.nextInt(5);

                    for (z += random.nextInt(5) - random.nextInt(5); x < i || x >= i + k || z < j || z >= j + k; z = i2 + random.nextInt(5) - random.nextInt(5)) {
                        x = l1 + random.nextInt(5) - random.nextInt(5);
                    }
                }
            }
        }
    }
}
