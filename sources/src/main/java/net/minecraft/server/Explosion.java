package net.minecraft.server;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.koloboke.collect.map.hash.HashObjObjMaps;
import com.koloboke.collect.set.hash.HashObjSets;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.torch.util.random.LightRandom;
import org.bukkit.Location;
import org.bukkit.event.block.BlockExplodeEvent;
// CraftBukkit end

public class Explosion {

    private final boolean a;
    private final boolean b;
    private final Random c = new LightRandom();
    private final World world;
    private final double posX;
    private final double posY;
    private final double posZ;
    public final Entity source;
    private final float size;
    /** A list of ChunkPositions of blocks affected by this explosion */
    private final Set<BlockPosition> blocks = HashObjSets.newMutableSet(); // Torch - ArrayList -> HashSet
    /** playerKnockbackMap */
    private final Map<EntityHuman, Vec3D> k = HashObjObjMaps.newMutableMap();
    public boolean wasCanceled = false; // CraftBukkit - add field

    public Explosion(World world, Entity entity, double d0, double d1, double d2, float f, boolean flag, boolean flag1) {
        this.world = world;
        this.source = entity;
        this.size = (float) Math.max(f, 0.0); // CraftBukkit - clamp bad values
        this.posX = d0;
        this.posY = d1;
        this.posZ = d2;
        this.a = flag;
        this.b = flag1;
    }
    
    /**
     * <b>PAIL: destoryBlocks</b>
     * <p>
     * Does the first part of the explosion (destroy blocks)
     */
    public void a() {
        if (this.size < 0.1F) return; // CraftBukkit
        
        for (int k = 0; k < 16; ++k) {
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
                        double d0 = k / 15.0F * 2.0F - 1.0F;
                        double d1 = i / 15.0F * 2.0F - 1.0F;
                        double d2 = j / 15.0F * 2.0F - 1.0F;
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        
                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        
                        double d4 = this.posX;
                        double d5 = this.posY;
                        double d6 = this.posZ;
                        
                        float rate = this.size * (0.7F + this.world.random.nextFloat() * 0.6F);
                        for (; rate > 0.0F; rate -= 0.22500001F) {
                            BlockPosition position = new BlockPosition(d4, d5, d6);
                            IBlockData iblockdata = this.world.getType(position);
                            
                            if (iblockdata.getMaterial() != Material.AIR) {
                                float f2 = this.source != null ? this.source.a(this, this.world, position, iblockdata) : iblockdata.getBlock().a((Entity) null);
                                
                                rate -= (f2 + 0.3F) * 0.3F;
                            }
                            
                            // CraftBukkit - don't wrap explosions
                            if (rate > 0.0F && (this.source == null || this.source.a(this, this.world, position, iblockdata, rate)) && position.getY() < 256 && position.getY() >= 0) {
                                blocks.add(position);
                            }
                            
                            d4 += d0 * 0.30000001192092896D;
                            d5 += d1 * 0.30000001192092896D;
                            d6 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }
        
        float expandSize = this.size * 2.0F;
        
        int maxX = MathHelper.floor(this.posX - expandSize - 1.0D);
        int minX = MathHelper.floor(this.posX + expandSize + 1.0D);
        int maxY = MathHelper.floor(this.posY - expandSize - 1.0D);
        int minY = MathHelper.floor(this.posY + expandSize + 1.0D);
        int minZ = MathHelper.floor(this.posZ - expandSize - 1.0D);
        int maxZ = MathHelper.floor(this.posZ + expandSize + 1.0D);
        
        // Paper start - Fix lag from explosions processing dead entities
        List<Entity> living = this.world.getEntities(this.source, new AxisAlignedBB(maxX, maxY, minZ, minX, minY, maxZ), new Predicate<Entity>() {
            @Override
            public boolean apply(Entity entity) {
                return IEntitySelector.d.apply(entity) && !entity.dead;
            }
        });
        // Paper end
        
        Vec3D vec3D = new Vec3D(this.posX, this.posY, this.posZ);
        
        for (int index = 0, size = living.size(); index < size; index++) {
            Entity entity = living.get(index);
            
            if (entity.bt()) continue; // PAIL: isImmuneToExplosions()
            
            double d7 = entity.e(this.posX, this.posY, this.posZ) / expandSize; // PAIL: e -> getDistance
            
            if (d7 <= 1.0D) {
                double offsetX = entity.locX - this.posX;
                double offsetY = entity.locY + entity.getHeadHeight() - this.posY;
                double offsetZ = entity.locZ - this.posZ;
                double norm = MathHelper.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
                
                if (norm != 0.0D) {
                    offsetX /= norm;
                    offsetY /= norm;
                    offsetZ /= norm;
                    double blockDensity = this.getBlockDensity(vec3D, entity.getBoundingBox()); // Paper - Optimize explosions
                    double d13 = (1.0D - d7) * blockDensity;
                    
                    // CraftBukkit start
                    CraftEventFactory.entityDamage = source;
                    entity.forceExplosionKnockback = false;
                    boolean wasDamaged = entity.damageEntity(DamageSource.explosion(this), ((int) ((d13 * d13 + d13) / 2.0D * 7.0D * expandSize + 1.0D)));
                    CraftEventFactory.entityDamage = null;
                    
                    if (!wasDamaged && !(entity instanceof EntityTNTPrimed || entity instanceof EntityFallingBlock) && !entity.forceExplosionKnockback) {
                        continue;
                    }
                    // CraftBukkit end
                    
                    double damageReduction = d13;
                    if (entity instanceof EntityLiving) {
                        damageReduction = entity instanceof EntityHuman && world.paperConfig.disableExplosionKnockback ? 0 : EnchantmentProtection.a((EntityLiving) entity, d13); // Paper - Disable explosion knockback
                    }
                    
                    // Paper start - Fix cannons
                    /* entity.motX += d8 * d14;
                    entity.motY += d9 * d14;
                    entity.motZ += d10 * d14; */
                    
                    // This impulse method sets the dirty flag, so clients will get an immediate velocity update
                    entity.addVelocity(offsetX * damageReduction, offsetY * damageReduction, offsetZ * damageReduction);
                    // Paper end
                    
                    if (entity instanceof EntityHuman) {
                        EntityHuman entityhuman = (EntityHuman) entity;

                        if (!entityhuman.isSpectator() && (!entityhuman.z() && !world.paperConfig.disableExplosionKnockback || !entityhuman.abilities.isFlying)) { // Paper - Disable explosion knockback
                            this.k.put(entityhuman, new Vec3D(offsetX * d13, offsetY * d13, offsetZ * d13));
                        }
                    }
                }
                
            }
        }

    }

    /**
     * <b>PAIL: applyPhysics</b>
     * <p>
     * Does the second part of the explosion (sound, particles, drop spawn)
     */
    public void a(boolean spawnParticles) {
        // PAIL: world.a -> world.playSound
        this.world.a(null, this.posX, this.posY, this.posZ, SoundEffects.bP, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F);
        
        if (this.size >= 2.0F && this.b) { // PAIL: b -> isSmoking
            this.world.addParticle(EnumParticle.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D, new int[0]);
        } else {
            this.world.addParticle(EnumParticle.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D, new int[0]);
        }
        
        if (this.b) { // PAIL: isSmoking
            // CraftBukkit start
            org.bukkit.World bukkitWorld = this.world.getWorld();
            org.bukkit.entity.Entity explode = this.source == null ? null : this.source.getBukkitEntity();
            Location location = new Location(bukkitWorld, this.posX, this.posY, this.posZ);
            
            List<org.bukkit.block.Block> blockList = Lists.newArrayList();
            for (BlockPosition position : this.blocks) {
                org.bukkit.block.Block bukkitBlock = bukkitWorld.getBlockAt(position.getX(), position.getY(), position.getZ());
                if (bukkitBlock.getType() != org.bukkit.Material.AIR) {
                    blockList.add(bukkitBlock);
                }
            }
            
            boolean cancelled;
            List<org.bukkit.block.Block> bukkitBlocks;
            float yield;
            
            if (explode != null) {
                EntityExplodeEvent event = new EntityExplodeEvent(explode, location, blockList, 1.0F / this.size);
                this.world.getServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            } else {
                BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, 1.0F / this.size);
                this.world.getServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            }
            
            if (cancelled) {
                this.wasCanceled = true;
                return;
            }
            
            this.blocks.clear();
            
            for (org.bukkit.block.Block bukkitBlock : bukkitBlocks) {
                BlockPosition coords = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
                blocks.add(coords);
                // CraftBukkit end
                
                // Torch start - moved up from underside, in case duplicate loop
                Block block = this.world.getType(coords).getBlock();
                
                if (spawnParticles) {
                    double randomX = coords.getX() + this.world.random.nextFloat();
                    double randomY = coords.getY() + this.world.random.nextFloat();
                    double randomZ = coords.getZ() + this.world.random.nextFloat();
                    double d3 = randomX - this.posX;
                    double d4 = randomY - this.posY;
                    double d5 = randomZ - this.posZ;
                    double d6 = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                    
                    d3 /= d6;
                    d4 /= d6;
                    d5 /= d6;
                    double d7 = 0.5D / (d6 / this.size + 0.1D);

                    d7 *= this.world.random.nextFloat() * this.world.random.nextFloat() + 0.3F;
                    d3 *= d7;
                    d4 *= d7;
                    d5 *= d7;
                    this.world.addParticle(EnumParticle.EXPLOSION_NORMAL, (randomX + this.posX) / 2.0D, (randomY + this.posY) / 2.0D, (randomZ + this.posZ) / 2.0D, d3, d4, d5, new int[0]);
                    this.world.addParticle(EnumParticle.SMOKE_NORMAL, randomX, randomY, randomZ, d3, d4, d5, new int[0]);
                }

                if (block.material != Material.AIR) {
                    if (block.a(this)) {
                        // CraftBukkit - add yield
                        block.dropNaturally(this.world, coords, this.world.getType(coords), yield, 0);
                    }

                    this.world.setTypeAndData(coords, Blocks.AIR.getBlockData(), 3);
                    block.wasExploded(this.world, coords, this);
                }
                
                if (this.a) { // Torch - Copied from underside, PAIL: isFlaming
                    if (this.world.getType(coords).getMaterial() == Material.AIR && this.world.getType(coords.down()).b() && this.c.nextInt(3) == 0) {
                        
                        if (!org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(this.world, coords.getX(), coords.getY(), coords.getZ(), this).isCancelled()) {
                            this.world.setTypeUpdate(coords, Blocks.FIRE.getBlockData());
                        }
                    }
                }
                // Torch end
            }
            
        } else if (this.a) { // PAIL: isFlaming
            // Flaming only, the concurrent way has been moved up
            for (BlockPosition position : this.blocks) {
                if (this.world.getType(position).getMaterial() == Material.AIR && this.world.getType(position.down()).b() && this.c.nextInt(3) == 0) {
                    
                    // CraftBukkit start - Ignition by explosion
                    if (!org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(this.world, position.getX(), position.getY(), position.getZ(), this).isCancelled()) {
                        this.world.setTypeUpdate(position, Blocks.FIRE.getBlockData());
                    }
                    // CraftBukkit end
                }
            }
        }

    }

    public Map<EntityHuman, Vec3D> b() {
        return this.k;
    }

    @Nullable
    public EntityLiving getSource() {
        // CraftBukkit start - obtain Fireball shooter for explosion tracking
        return this.source == null ? null : (this.source instanceof EntityTNTPrimed ? ((EntityTNTPrimed) this.source).getSource() : (this.source instanceof EntityLiving ? (EntityLiving) this.source : (this.source instanceof EntityFireball ? ((EntityFireball) this.source).shooter : null)));
        // CraftBukkit end
    }

    public void clearBlocks() {
        this.blocks.clear();
    }

    public Set<BlockPosition> getBlocks() { // Torch
        return this.blocks;
    }

    // Paper start - Optimize explosions
    private float getBlockDensity(Vec3D vec3d, AxisAlignedBB aabb) {
        if (!this.world.paperConfig.optimizeExplosions) {
            return this.world.a(vec3d, aabb);
        }
        CacheKey key = new CacheKey(this, aabb);
        Float blockDensity = this.world.explosionDensityCache.get(key);
        if (blockDensity == null) {
            blockDensity = this.world.a(vec3d, aabb);
            this.world.explosionDensityCache.put(key, blockDensity);
        }

        return blockDensity;
    }

    static final class CacheKey {
        private final World world;
        private final double posX, posY, posZ;
        private final double minX, minY, minZ;
        private final double maxX, maxY, maxZ;

        public CacheKey(Explosion explosion, AxisAlignedBB aabb) {
            this.world = explosion.world;
            this.posX = explosion.posX;
            this.posY = explosion.posY;
            this.posZ = explosion.posZ;
            this.minX = aabb.a;
            this.minY = aabb.b;
            this.minZ = aabb.c;
            this.maxX = aabb.d;
            this.maxY = aabb.e;
            this.maxZ = aabb.f;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (Double.compare(cacheKey.posX, posX) != 0) return false;
            if (Double.compare(cacheKey.posY, posY) != 0) return false;
            if (Double.compare(cacheKey.posZ, posZ) != 0) return false;
            if (Double.compare(cacheKey.minX, minX) != 0) return false;
            if (Double.compare(cacheKey.minY, minY) != 0) return false;
            if (Double.compare(cacheKey.minZ, minZ) != 0) return false;
            if (Double.compare(cacheKey.maxX, maxX) != 0) return false;
            if (Double.compare(cacheKey.maxY, maxY) != 0) return false;
            if (Double.compare(cacheKey.maxZ, maxZ) != 0) return false;
            return world.equals(cacheKey.world);
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = world.hashCode();
            temp = Double.doubleToLongBits(posX);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(posY);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(posZ);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(minX);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(minY);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(minZ);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(maxX);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(maxY);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(maxZ);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }
    // Paper end
}
