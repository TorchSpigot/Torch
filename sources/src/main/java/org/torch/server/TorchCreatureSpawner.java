package org.torch.server;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import lombok.Getter;

import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.spigotmc.AsyncCatcher;
import org.torch.api.TorchReactor;

import com.destroystokyo.paper.exception.ServerInternalException;
import com.koloboke.collect.set.hash.HashLongSet;
import com.koloboke.collect.set.hash.HashLongSets;
import net.minecraft.server.*;
import net.minecraft.server.BiomeBase.BiomeMeta;
import net.minecraft.server.BlockPosition.MutableBlockPosition;
import net.minecraft.server.EntityInsentient.EnumEntityPositionType;

@Getter
public final class TorchCreatureSpawner implements TorchReactor {
    /** The legacy */
    private final SpawnerCreature servant;

    // public static final int MOB_COUNT_DIV = (int) Math.pow(17.0D, 2.0D);
    /** The 17x17 area around the player where mobs can spawn */
    private final HashLongSet spawnableChunks = HashLongSets.newMutableSet();

    /** Collect the chunks that will actually be eligible for spawning, vanilla 8 */
    private static int RANGE;

    /** The chunk area covered by each player, vanilla 17x17 */
    private static int CHUNKS_PER_PLAYER = (RANGE * 2 + 1) * (RANGE * 2 + 1);

    public TorchCreatureSpawner(@Nullable SpawnerCreature legacy) {
        servant = legacy;
    }
    
    UUID uuid = UUID.randomUUID();

    /** Returns entity count only from chunks being processed in spawnableChunks */
    public int getEntityCount(WorldServer server, Class<?> creatureType) {
        // Paper - use entire world, not just active chunks. Spigot broke vanilla expectations.
        return server
                .getChunkProviderServer()
                .chunks.values()
                .parallelStream()
                .collect(java.util.stream.Collectors.summingInt(chunk -> chunk.entityCount.getOrDefault(creatureType, 0)));
    }

    /**
     * Adds all chunks within the spawn radius of the players to spawnableChunks
     * Returns number of spawned entities
     */
    public int findChunksForSpawning(WorldServer world, boolean spawnHostileCreatures, boolean spawnPassiveCreatures, boolean spawnAnimals) {
        // Paper - At least until we figure out what is calling this async
        AsyncCatcher.catchOp("check for eligible spawn chunks");

        if ((!spawnHostileCreatures && !spawnPassiveCreatures) || world.players.isEmpty()) return 0;

        this.spawnableChunks.clear();
        int foundChunks = 0;

        if (RANGE == 0) {
            RANGE = world.spigotConfig.mobSpawnRange;
            RANGE = (RANGE > world.spigotConfig.viewDistance) ? world.spigotConfig.viewDistance : RANGE;
            RANGE = (RANGE > 8) ? 8 : RANGE;
        }

        for (final EntityHuman player : world.players) {
            if (player.isSpectator() || !player.affectsSpawning) continue;

            final int centerX = MathHelper.floor(player.locX / 16.0D);
            final int centerZ = MathHelper.floor(player.locZ / 16.0D);

            for (int levelX = -RANGE; levelX <= RANGE; levelX++) {
                for (int levelZ = -RANGE; levelZ <= RANGE; levelZ++) {
                    boolean reachedEdge = levelX == -RANGE || levelX == RANGE || levelZ == -RANGE || levelZ == RANGE;

                    ChunkCoordIntPair chunkPair = new ChunkCoordIntPair(levelX + centerX, levelZ + centerZ);
                    foundChunks++;

                    if (!reachedEdge && world.getWorldBorder().isInBounds(chunkPair)) {
                        PlayerChunk chunk = world.getPlayerChunkMap().getChunk(chunkPair.x, chunkPair.z);

                        if (chunk != null && chunk.isDone()) this.spawnableChunks.add(LongHash.toLong(chunkPair.x, chunkPair.z));
                    }
                }
            }

        }

        int spawnedTotal = 0;
        BlockPosition spawnPoint = world.getSpawn();
        
        MutableBlockPosition currentPos = new BlockPosition.MutableBlockPosition();

        for (EnumCreatureType type : EnumCreatureType.values()) {
            // CraftBukkit - use per-world spawn limits
            int spawnLimit = type.getMaxNumberOfCreature();
            switch (type) {
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

            if ((!type.isPeaceful() || spawnPassiveCreatures) && (type.isPeaceful() || spawnHostileCreatures) && (!type.isAnimal() || spawnAnimals)
                    && ((mobCount = getEntityCount(world, type.a())) <= spawnLimit * foundChunks / CHUNKS_PER_PLAYER)) {

                int mobLimit = (spawnLimit * foundChunks / 256) - mobCount + 1; // Spigot - up to 1 more than limit

                for (Long hash : this.spawnableChunks) {
                    if (mobLimit <= 0) return spawnedTotal;

                    BlockPosition randomPos = createRandomPosition(world, LongHash.msw(hash), LongHash.lsw(hash));
                    int randomX = randomPos.getX();
                    int randomY = randomPos.getY();
                    int randomZ = randomPos.getZ();

                    final IBlockData block = world.getType(randomPos);
                    if (!block.m() && block.getMaterial() == type.getCreatureMaterial()) {
                        int spawnedEntity = 0;
                        int research = 0;

                        while (research < 3) {
                            int cX = randomX;
                            int cY = randomY;
                            int cZ = randomZ;
                            final int noise = 6;
                            BiomeMeta spawnEntry = null;
                            GroupDataEntity entityData = null;

                            int respawn = 0;
                            while (true) {
                                if (respawn < 4) {
                                    SPAWN: {
                                    cX += world.random.nextInt(noise) - world.random.nextInt(noise);
                                    cY += world.random.nextInt(1) - world.random.nextInt(1);
                                    cZ += world.random.nextInt(noise) - world.random.nextInt(noise);
                                    currentPos.setValues(cX, cY, cZ);
                                    
                                    float xCoord = cX + 0.5F;
                                    float zCoord = cZ + 0.5F;

                                    if (!world.isPlayerNearby(xCoord, cY, zCoord, 24.0D) && spawnPoint.distanceSquared(xCoord, cY, zCoord) >= 576.0D) {
                                        if (spawnEntry == null) {
                                            spawnEntry = world.createRandomSpawnEntry(type, currentPos);

                                            if (spawnEntry == null) break SPAWN;
                                        }

                                        if (world.possibleToSpawn(type, spawnEntry, currentPos) && canCreatureTypeSpawnAtLocation(EntityPositionTypes.a(spawnEntry.entityClass()), world, currentPos)) {
                                            EntityInsentient entity;

                                            entity = createCreature(world, spawnEntry.entityClass());
                                            if (entity == null) return spawnedTotal;

                                            entity.setPositionRotation(xCoord, cY, zCoord, world.random.nextFloat() * 360.0F, 0.0F);

                                            if (entity.isNotColliding() && entity.canSpawn()) {
                                                entityData = entity.prepare(world.createDamageScaler(new BlockPosition(entity)), entityData);

                                                if (entity.canSpawn()) {
                                                    if (world.addEntity(entity, SpawnReason.NATURAL)) {

                                                        spawnedEntity++;
                                                        mobLimit--;
                                                    }
                                                } else {
                                                    entity.die();
                                                }

                                                if (mobLimit <= 0) return spawnedTotal; // Spigot - If we're past limit, stop spawn
                                            }

                                            spawnedTotal += spawnedEntity;
                                        }
                                    }

                                    respawn++;
                                    continue;
                                } // SPAWN LABEL END
                                }

                                research++;
                                break;
                            }
                        }
                    }
                }
                
            }
        }

        return spawnedTotal;
    }

    public static BlockPosition createRandomPosition(World world, int chunkX, int chunkZ) {
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        int x = chunkX * 16 + world.random.nextInt(16);
        int z = chunkZ * 16 + world.random.nextInt(16);
        final int y = world.random.nextInt(chunk == null ? world.getActualWorldHeight() : chunk.findFilledTop() + 16 - 1);

        return new BlockPosition(x, y, z);
    }

    public static boolean isValidEmptySpawnBlock(IBlockData blockData) {
        return blockData.l() ? false : (blockData.n() ? false : (blockData.getMaterial().isLiquid() ? false : !BlockMinecartTrackAbstract.i(blockData)));
    }

    /**
     * Returns whether or not the specified creature type can spawn at the specified location
     */
    public static boolean canCreatureTypeSpawnAtLocation(EnumEntityPositionType type, World world, BlockPosition position) {
        if (!world.getWorldBorder().a(position)) return false;

        IBlockData blockType = world.getType(position);
        if (type == EnumEntityPositionType.IN_WATER) {
            return blockType.getMaterial() == Material.WATER && world.getType(position.down()).getMaterial() == Material.WATER && !world.getType(position.up()).m();
        }

        BlockPosition down = position.down();
        if (!world.getType(down).r()) {
            return false;
        } else {
            Block downBlock = world.getType(down).getBlock();
            boolean downVaild = downBlock != Blocks.BEDROCK && downBlock != Blocks.BARRIER;

            return downVaild && isValidEmptySpawnBlock(blockType) && isValidEmptySpawnBlock(world.getType(position.up()));
        }
    }

    /**
     * Called during chunk generation to spawn initial creatures
     */
    public static void performWorldGenerateSpawning(World world, BiomeBase biome, int i, int j, int k, int l, Random random) {
        List<BiomeMeta> spawnableTypes = biome.getMobs(EnumCreatureType.CREATURE);

        if (spawnableTypes.isEmpty()) return;

        while (random.nextFloat() < biome.getSpawningChance()) {
            BiomeMeta randomEntry = WeightedRandom.a(world.random, spawnableTypes);

            int groupCount = randomEntry.c + random.nextInt(1 + randomEntry.d - randomEntry.c);
            GroupDataEntity entityData = null;

            int x = i + random.nextInt(k);
            int z = j + random.nextInt(l);
            int l1 = x;
            int i2 = z;

            for (int index = 0; index < groupCount; index++) {
                boolean spawned = false;

                for (int retry = 0; !spawned && retry < 4; retry++) {
                    BlockPosition topBlock = world.getTopSolidOrLiquidBlock(new BlockPosition(x, 0, z));

                    if (canCreatureTypeSpawnAtLocation(EnumEntityPositionType.ON_GROUND, world, topBlock)) {

                        EntityInsentient entity = createCreature(world, randomEntry.b);
                        if (entity == null) continue;

                        entity.setPositionRotation(x + 0.5F, topBlock.getY(), z + 0.5F, random.nextFloat() * 360.0F, 0.0F);

                        // CraftBukkit Added a reason for spawning this creature, moved entityinsentient.prepare(groupdataentity) up
                        entityData = entity.prepare(world.createDamageScaler(new BlockPosition(entity)), entityData);
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

    private static EntityInsentient createCreature(final World world, final Class<?> entityClass) {
        try {
            return (EntityInsentient) entityClass.getConstructor(World.class).newInstance(world);
        } catch (final Throwable t) {
            t.printStackTrace();
            ServerInternalException.reportInternalException(t);
        }

        return null;
    }
}
