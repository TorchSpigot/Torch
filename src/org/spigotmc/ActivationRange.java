package org.spigotmc;

import java.util.List;
import co.aikar.timings.MinecraftTimings;
import net.minecraft.server.v1_9_R2.AxisAlignedBB;
import net.minecraft.server.v1_9_R2.Chunk;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityAmbient;
import net.minecraft.server.v1_9_R2.EntityAnimal;
import net.minecraft.server.v1_9_R2.EntityArrow;
import net.minecraft.server.v1_9_R2.EntityComplexPart;
import net.minecraft.server.v1_9_R2.EntityCreature;
import net.minecraft.server.v1_9_R2.EntityCreeper;
import net.minecraft.server.v1_9_R2.EntityEnderCrystal;
import net.minecraft.server.v1_9_R2.EntityEnderDragon;
import net.minecraft.server.v1_9_R2.EntityFallingBlock;
import net.minecraft.server.v1_9_R2.EntityFireball;
import net.minecraft.server.v1_9_R2.EntityFireworks;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.EntityMonster;
import net.minecraft.server.v1_9_R2.EntityProjectile;
import net.minecraft.server.v1_9_R2.EntitySheep;
import net.minecraft.server.v1_9_R2.EntitySlime;
import net.minecraft.server.v1_9_R2.EntityTNTPrimed;
import net.minecraft.server.v1_9_R2.EntityVillager;
import net.minecraft.server.v1_9_R2.EntityWeather;
import net.minecraft.server.v1_9_R2.EntityWither;
import net.minecraft.server.v1_9_R2.MCUtil;
import net.minecraft.server.v1_9_R2.MathHelper;
import net.minecraft.server.v1_9_R2.MinecraftServer;
import net.minecraft.server.v1_9_R2.World;

public class ActivationRange
{

    static AxisAlignedBB maxBB = new AxisAlignedBB( 0, 0, 0, 0, 0, 0 );
    static AxisAlignedBB miscBB = new AxisAlignedBB( 0, 0, 0, 0, 0, 0 );
    static AxisAlignedBB animalBB = new AxisAlignedBB( 0, 0, 0, 0, 0, 0 );
    static AxisAlignedBB monsterBB = new AxisAlignedBB( 0, 0, 0, 0, 0, 0 );

    /**
     * Initializes an entities type on construction to specify what group this
     * entity is in for activation ranges.
     *
     * @param entity
     * @return group id
     */
    public static byte initializeEntityActivationType(Entity entity)
    {
        if ( entity instanceof EntityMonster || entity instanceof EntitySlime )
        {
            return 1; // Monster
        } else if ( entity instanceof EntityCreature || entity instanceof EntityAmbient )
        {
            return 2; // Animal
        } else
        {
            return 3; // Misc
        }
    }

    /**
     * These entities are excluded from Activation range checks.
     *
     * @param entity Entity to initialize
     * @param config Spigot config to determine ranges
     * @return boolean If it should always tick.
     */
    public static boolean initializeEntityActivationState(Entity entity, SpigotWorldConfig config)
    {
        if ( ( entity.activationType == 3 && config.miscActivationRange == 0 )
                || ( entity.activationType == 2 && config.animalActivationRange == 0 )
                || ( entity.activationType == 1 && config.monsterActivationRange == 0 )
                || entity instanceof EntityHuman
                || entity instanceof EntityProjectile
                || entity instanceof EntityEnderDragon
                || entity instanceof EntityComplexPart
                || entity instanceof EntityWither
                || entity instanceof EntityFireball
                || entity instanceof EntityWeather
                || entity instanceof EntityTNTPrimed
                || entity instanceof EntityFallingBlock // Paper - Always tick falling blocks
                || entity instanceof EntityEnderCrystal
                || entity instanceof EntityFireworks )
        {
            return true;
        }

        return false;
    }

    /**
     * Find what entities are in range of the players in the world and set
     * active if in range.
     *
     * @param world
     */
    public static void activateEntities(World world)
    {
        MinecraftTimings.entityActivationCheckTimer.startTiming();
        final int miscActivationRange = world.spigotConfig.miscActivationRange;
        final int animalActivationRange = world.spigotConfig.animalActivationRange;
        final int monsterActivationRange = world.spigotConfig.monsterActivationRange;

        int maxRange = Math.max( monsterActivationRange, animalActivationRange );
        maxRange = Math.max( maxRange, miscActivationRange );
        maxRange = Math.min( ( world.spigotConfig.viewDistance << 4 ) - 8, maxRange );

        Chunk chunk; // Paper
        for ( EntityHuman player : world.players )
        {

            player.activatedTick = MinecraftServer.currentTick;
            maxBB = player.getBoundingBox().grow( maxRange, 256, maxRange );
            miscBB = player.getBoundingBox().grow( miscActivationRange, 256, miscActivationRange );
            animalBB = player.getBoundingBox().grow( animalActivationRange, 256, animalActivationRange );
            monsterBB = player.getBoundingBox().grow( monsterActivationRange, 256, monsterActivationRange );

            int i = MathHelper.floor( maxBB.a / 16.0D );
            int j = MathHelper.floor( maxBB.d / 16.0D );
            int k = MathHelper.floor( maxBB.c / 16.0D );
            int l = MathHelper.floor( maxBB.f / 16.0D );

            for ( int i1 = i; i1 <= j; ++i1 )
            {
                for ( int j1 = k; j1 <= l; ++j1 )
                {
                    if ( (chunk = MCUtil.getLoadedChunkWithoutMarkingActive(world, i1, j1 )) != null ) // Paper
                    {
                        activateChunkEntities( chunk ); // Paper
                    }
                }
            }
        }
        MinecraftTimings.entityActivationCheckTimer.stopTiming();
    }

    /**
     * Checks for the activation state of all entities in this chunk.
     *
     * @param chunk
     */
    private static void activateChunkEntities(Chunk chunk)
    {
        for ( List<Entity> slice : chunk.entitySlices )
        {
            for ( Entity entity : slice )
            {
                if ( MinecraftServer.currentTick > entity.activatedTick )
                {
                    if ( entity.defaultActivationState )
                    {
                        entity.activatedTick = MinecraftServer.currentTick;
                        continue;
                    }
                    switch ( entity.activationType )
                    {
                        case 1:
                            if ( monsterBB.b( entity.getBoundingBox() ) )
                            {
                                entity.activatedTick = MinecraftServer.currentTick;
                            }
                            break;
                        case 2:
                            if ( animalBB.b( entity.getBoundingBox() ) )
                            {
                                entity.activatedTick = MinecraftServer.currentTick;
                            }
                            break;
                        case 3:
                        default:
                            if ( miscBB.b( entity.getBoundingBox() ) )
                            {
                                entity.activatedTick = MinecraftServer.currentTick;
                            }
                    }
                }
            }
        }
    }

    /**
     * If an entity is not in range, do some more checks to see if we should
     * give it a shot.
     *
     * @param entity
     * @return
     */
    public static boolean checkEntityImmunities(Entity entity)
    {
        // quick checks.
        if ( entity.inWater || entity.fireTicks > 0 )
        {
            return true;
        }
        if ( !( entity instanceof EntityArrow ) )
        {
            if ( !entity.onGround || !entity.passengers.isEmpty() || entity.isPassenger() )
            {
                return true;
            }
        } else if ( !( (EntityArrow) entity ).inGround )
        {
            return true;
        }
        // special cases.
        if ( entity instanceof EntityLiving )
        {
            EntityLiving living = (EntityLiving) entity;
            if ( /*TODO: Missed mapping? living.attackTicks > 0 || */ living.hurtTicks > 0 || living.effects.size() > 0 )
            {
                return true;
            }
            if ( entity instanceof EntityCreature && ( (EntityCreature) entity ).getGoalTarget() != null )
            {
                return true;
            }
            if ( entity instanceof EntityVillager && ( (EntityVillager) entity ).db()/* Getter for first boolean */ )
            {
                return true;
            }
            if ( entity instanceof EntityAnimal )
            {
                EntityAnimal animal = (EntityAnimal) entity;
                if ( animal.isBaby() || animal.isInLove() )
                {
                    return true;
                }
                if ( entity instanceof EntitySheep && ( (EntitySheep) entity ).isSheared() )
                {
                    return true;
                }
            }
            if (entity instanceof EntityCreeper && ((EntityCreeper) entity).isIgnited()) { // isExplosive
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the entity is active for this tick.
     *
     * @param entity
     * @return
     */
    public static boolean checkIfActive(Entity entity)
    {
        // Never safe to skip fireworks or entities not yet added to chunk
        // PAIL: inChunk
        if ( !entity.aa || entity instanceof EntityFireworks ) {
            return true;
        }

        boolean isActive = entity.activatedTick >= MinecraftServer.currentTick || entity.defaultActivationState;

        // Should this entity tick?
        if ( !isActive )
        {
            if ( ( MinecraftServer.currentTick - entity.activatedTick - 1 ) % 20 == 0 )
            {
                // Check immunities every 20 ticks.
                if ( checkEntityImmunities( entity ) )
                {
                    // Triggered some sort of immunity, give 20 full ticks before we check again.
                    entity.activatedTick = MinecraftServer.currentTick + 20;
                }
                isActive = true;
            }
            // Add a little performance juice to active entities. Skip 1/4 if not immune.
        } else if ( !entity.defaultActivationState && entity.ticksLived % 4 == 0 && !checkEntityImmunities( entity ) )
        {
            isActive = false;
        }
        int x = MathHelper.floor( entity.locX );
        int z = MathHelper.floor( entity.locZ );
        // Make sure not on edge of unloaded chunk
        Chunk chunk = entity.world.getChunkIfLoaded( x >> 4, z >> 4 );
        if ( isActive && !( chunk != null && chunk.areNeighborsLoaded( 1 ) ) )
        {
            isActive = false;
        }
        return isActive;
    }
}
