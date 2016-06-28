package net.minecraft.server;

import java.util.Iterator;
import java.util.List;

import java.util.Queue;

// CraftBukkit start
import org.bukkit.entity.Player;
import org.bukkit.entity.Fish;
import org.bukkit.event.player.PlayerFishEvent;
// CraftBukkit end

public class EntityFishingHook extends Entity {

    private static final DataWatcherObject<Integer> c = DataWatcher.a(EntityFishingHook.class, DataWatcherRegistry.b);
    private int d = -1;
    private int e = -1;
    private int f = -1;
    private Block g;
    private boolean isInGround;
    public EntityHuman owner;
    private int at;
    private int au;
    private int av;
    private int aw;
    private int ax;
    private float ay;
    public Entity hooked;
    private int az;
    private double aA;
    private double aB;
    private double aC;
    private double aD;
    private double aE;

    public EntityFishingHook(World world) {
        super(world);
        this.setSize(0.25F, 0.25F);
        this.ah = true;
    }

    public EntityFishingHook(World world, EntityHuman entityhuman) {
        super(world);
        this.ah = true;
        this.owner = entityhuman;
        this.owner.hookedFish = this;
        this.setSize(0.25F, 0.25F);
        this.setPositionRotation(entityhuman.locX, entityhuman.locY + (double) entityhuman.getHeadHeight(), entityhuman.locZ, entityhuman.yaw, entityhuman.pitch);
        this.locX -= (double) (MathHelper.cos(this.yaw * 0.017453292F) * 0.16F);
        this.locY -= 0.10000000149011612D;
        this.locZ -= (double) (MathHelper.sin(this.yaw * 0.017453292F) * 0.16F);
        this.setPosition(this.locX, this.locY, this.locZ);
        float f = 0.4F;

        this.motX = (double) (-MathHelper.sin(this.yaw * 0.017453292F) * MathHelper.cos(this.pitch * 0.017453292F) * f);
        this.motZ = (double) (MathHelper.cos(this.yaw * 0.017453292F) * MathHelper.cos(this.pitch * 0.017453292F) * f);
        this.motY = (double) (-MathHelper.sin(this.pitch * 0.017453292F) * f);
        this.c(this.motX, this.motY, this.motZ, 1.5F, 1.0F);
    }

    protected void i() {
        this.getDataWatcher().register(EntityFishingHook.c, Integer.valueOf(0));
    }

    public void a(DataWatcherObject<?> datawatcherobject) {
        if (EntityFishingHook.c.equals(datawatcherobject)) {
            int i = ((Integer) this.getDataWatcher().get(EntityFishingHook.c)).intValue();

            if (i > 0 && this.hooked != null) {
                this.hooked = null;
            }
        }

        super.a(datawatcherobject);
    }

    public void c(double d0, double d1, double d2, float f, float f1) {
        float f2 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        d0 /= (double) f2;
        d1 /= (double) f2;
        d2 /= (double) f2;
        d0 += this.random.nextGaussian() * 0.007499999832361937D * (double) f1;
        d1 += this.random.nextGaussian() * 0.007499999832361937D * (double) f1;
        d2 += this.random.nextGaussian() * 0.007499999832361937D * (double) f1;
        d0 *= (double) f;
        d1 *= (double) f;
        d2 *= (double) f;
        this.motX = d0;
        this.motY = d1;
        this.motZ = d2;
        float f3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

        this.lastYaw = this.yaw = (float) (MathHelper.b(d0, d2) * 57.2957763671875D);
        this.lastPitch = this.pitch = (float) (MathHelper.b(d1, (double) f3) * 57.2957763671875D);
        this.at = 0;
    }

    public void m() {
        super.m();
        if (this.world.isClientSide) {
            int i = ((Integer) this.getDataWatcher().get(EntityFishingHook.c)).intValue();

            if (i > 0 && this.hooked == null) {
                this.hooked = this.world.getEntity(i - 1);
            }
        } else {
            ItemStack itemstack = this.owner.getItemInMainHand();

            if (this.owner.dead || !this.owner.isAlive() || itemstack == null || itemstack.getItem() != Items.FISHING_ROD || this.h(this.owner) > 1024.0D) {
                this.die();
                this.owner.hookedFish = null;
                return;
            }
        }

        if (this.hooked != null) {
            if (!this.hooked.dead) {
                this.locX = this.hooked.locX;
                double d0 = (double) this.hooked.length;

                this.locY = this.hooked.getBoundingBox().b + d0 * 0.8D;
                this.locZ = this.hooked.locZ;
                return;
            }

            this.hooked = null;
        }

        if (this.az > 0) {
            double d1 = this.locX + (this.aA - this.locX) / (double) this.az;
            double d2 = this.locY + (this.aB - this.locY) / (double) this.az;
            double d3 = this.locZ + (this.aC - this.locZ) / (double) this.az;
            double d4 = MathHelper.g(this.aD - (double) this.yaw);

            this.yaw = (float) ((double) this.yaw + d4 / (double) this.az);
            this.pitch = (float) ((double) this.pitch + (this.aE - (double) this.pitch) / (double) this.az);
            --this.az;
            this.setPosition(d1, d2, d3);
            this.setYawPitch(this.yaw, this.pitch);
        } else {
            if (this.isInGround) {
                if (this.world.getType(new BlockPosition(this.d, this.e, this.f)).getBlock() == this.g) {
                    ++this.at;
                    if (this.at == 1200) {
                        this.die();
                    }

                    return;
                }

                this.isInGround = false;
                this.motX *= (double) (this.random.nextFloat() * 0.2F);
                this.motY *= (double) (this.random.nextFloat() * 0.2F);
                this.motZ *= (double) (this.random.nextFloat() * 0.2F);
                this.at = 0;
                this.au = 0;
            } else {
                ++this.au;
            }

            double d5;
            double d6;

            if (!this.world.isClientSide) {
                Vec3D vec3d = new Vec3D(this.locX, this.locY, this.locZ);
                Vec3D vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
                MovingObjectPosition movingobjectposition = this.world.rayTrace(vec3d, vec3d1);

                vec3d = new Vec3D(this.locX, this.locY, this.locZ);
                vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
                if (movingobjectposition != null) {
                    vec3d1 = new Vec3D(movingobjectposition.pos.x, movingobjectposition.pos.y, movingobjectposition.pos.z);
                }

                Entity entity = null;
                List list = this.world.getEntities(this, this.getBoundingBox().a(this.motX, this.motY, this.motZ).g(1.0D));

                d5 = 0.0D;

                Iterator it = list.iterator();
          while (it.hasNext()) {
              Entity entity1 = (Entity) it.next();

              if (entity1.isInteractable() && (entity1 != this.owner || this.au >= 5)) {
                  AxisAlignedBB axisalignedbb = entity1.getBoundingBox().g(0.30000001192092896D);
                  MovingObjectPosition movingobjectposition1 = axisalignedbb.a(vec3d, vec3d1);

                  if (movingobjectposition1 != null) {
                      d6 = vec3d.distanceSquared(movingobjectposition1.pos);
                      if (d6 < d5 || d5 == 0.0D) {
                          entity = entity1;
                          d5 = d6;
                      }
                  }
              }
          }

                if (entity != null) {
                    movingobjectposition = new MovingObjectPosition(entity);
                }

                // Paper start - Allow fishing hooks to fly through vanished players the shooter can't see
                if (movingobjectposition != null && movingobjectposition.entity instanceof EntityPlayer && owner != null && owner instanceof EntityPlayer) {
                    if (!((EntityPlayer) owner).getBukkitEntity().canSee(((EntityPlayer) movingobjectposition.entity).getBukkitEntity())) {
                        movingobjectposition = null;
                    }
                }
                // Paper end

                if (movingobjectposition != null) {
                    org.bukkit.craftbukkit.event.CraftEventFactory.callProjectileHitEvent(this); // Craftbukkit - Call event
                    if (movingobjectposition.entity != null) {
                        this.hooked = movingobjectposition.entity;
                        this.getDataWatcher().set(EntityFishingHook.c, Integer.valueOf(this.hooked.getId() + 1));
                    } else {
                        this.isInGround = true;
                    }
                }
            }

            if (!this.isInGround) {
                this.move(this.motX, this.motY, this.motZ);
                float f = MathHelper.sqrt(this.motX * this.motX + this.motZ * this.motZ);

                this.yaw = (float) (MathHelper.b(this.motX, this.motZ) * 57.2957763671875D);

                for (this.pitch = (float) (MathHelper.b(this.motY, (double) f) * 57.2957763671875D); this.pitch - this.lastPitch < -180.0F; this.lastPitch -= 360.0F) {
                    ;
                }

                while (this.pitch - this.lastPitch >= 180.0F) {
                    this.lastPitch += 360.0F;
                }

                while (this.yaw - this.lastYaw < -180.0F) {
                    this.lastYaw -= 360.0F;
                }

                while (this.yaw - this.lastYaw >= 180.0F) {
                    this.lastYaw += 360.0F;
                }

                this.pitch = this.lastPitch + (this.pitch - this.lastPitch) * 0.2F;
                this.yaw = this.lastYaw + (this.yaw - this.lastYaw) * 0.2F;
                float f1 = 0.92F;

                if (this.onGround || this.positionChanged) {
                    f1 = 0.5F;
                }

                byte b0 = 5;
                double d7 = 0.0D;

                for (int k = 0; k < b0; ++k) {
                    AxisAlignedBB axisalignedbb1 = this.getBoundingBox();
                    double d8 = axisalignedbb1.e - axisalignedbb1.b;
                    double d9 = axisalignedbb1.b + d8 * (double) k / (double) b0;

                    d6 = axisalignedbb1.b + d8 * (double) (k + 1) / (double) b0;
                    AxisAlignedBB axisalignedbb2 = new AxisAlignedBB(axisalignedbb1.a, d9, axisalignedbb1.c, axisalignedbb1.d, d6, axisalignedbb1.f);

                    if (this.world.b(axisalignedbb2, Material.WATER)) {
                        d7 += 1.0D / (double) b0;
                    }
                }

                if (!this.world.isClientSide && d7 > 0.0D) {
                    WorldServer worldserver = (WorldServer) this.world;
                    int l = 1;
                    BlockPosition blockposition = (new BlockPosition(this)).up();

                    if (this.random.nextFloat() < 0.25F && this.world.isRainingAt(blockposition)) {
                        l = 2;
                    }

                    if (this.random.nextFloat() < 0.5F && !this.world.h(blockposition)) {
                        --l;
                    }

                    if (this.av > 0) {
                        --this.av;
                        if (this.av <= 0) {
                            this.aw = 0;
                            this.ax = 0;
                            // CraftBukkit start
                            PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player) this.owner.getBukkitEntity(), null, (Fish) this.getBukkitEntity(), PlayerFishEvent.State.FAILED_ATTEMPT);
                            this.world.getServer().getPluginManager().callEvent(playerFishEvent);
                            // CraftBukkit end
                        }
                    } else {
                        double d10;
                        Block block;
                        float f2;
                        float f3;
                        float f4;
                        double d11;

                        if (this.ax > 0) {
                            this.ax -= l;
                            if (this.ax <= 0) {
                                // CraftBukkit start
                                PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player) this.owner.getBukkitEntity(), null, (Fish) this.getBukkitEntity(), PlayerFishEvent.State.BITE);
                                this.world.getServer().getPluginManager().callEvent(playerFishEvent);
                                if (playerFishEvent.isCancelled()) {
                                    return;
                                }
                                // CraftBukkit end
                                this.motY -= 0.20000000298023224D;
                                this.a(SoundEffects.G, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                                f2 = (float) MathHelper.floor(this.getBoundingBox().b);
                                worldserver.a(EnumParticle.WATER_BUBBLE, this.locX, (double) (f2 + 1.0F), this.locZ, (int) (1.0F + this.width * 20.0F), (double) this.width, 0.0D, (double) this.width, 0.20000000298023224D, new int[0]);
                                worldserver.a(EnumParticle.WATER_WAKE, this.locX, (double) (f2 + 1.0F), this.locZ, (int) (1.0F + this.width * 20.0F), (double) this.width, 0.0D, (double) this.width, 0.20000000298023224D, new int[0]);
                                this.av = MathHelper.nextInt(this.random, 10, 30);
                            } else {
                                this.ay = (float) ((double) this.ay + this.random.nextGaussian() * 4.0D);
                                f2 = this.ay * 0.017453292F;
                                f3 = MathHelper.sin(f2);
                                f4 = MathHelper.cos(f2);
                                d6 = this.locX + (double) (f3 * (float) this.ax * 0.1F);
                                d11 = (double) ((float) MathHelper.floor(this.getBoundingBox().b) + 1.0F);
                                d10 = this.locZ + (double) (f4 * (float) this.ax * 0.1F);
                                block = worldserver.getType(new BlockPosition((int) d6, (int) d11 - 1, (int) d10)).getBlock();
                                if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                                    if (this.random.nextFloat() < 0.15F) {
                                        worldserver.a(EnumParticle.WATER_BUBBLE, d6, d11 - 0.10000000149011612D, d10, 1, (double) f3, 0.1D, (double) f4, 0.0D, new int[0]);
                                    }

                                    float f5 = f3 * 0.04F;
                                    float f6 = f4 * 0.04F;

                                    worldserver.a(EnumParticle.WATER_WAKE, d6, d11, d10, 0, (double) f6, 0.01D, (double) (-f5), 1.0D, new int[0]);
                                    worldserver.a(EnumParticle.WATER_WAKE, d6, d11, d10, 0, (double) (-f6), 0.01D, (double) f5, 1.0D, new int[0]);
                                }
                            }
                        } else if (this.aw > 0) {
                            this.aw -= l;
                            f2 = 0.15F;
                            if (this.aw < 20) {
                                f2 = (float) ((double) f2 + (double) (20 - this.aw) * 0.05D);
                            } else if (this.aw < 40) {
                                f2 = (float) ((double) f2 + (double) (40 - this.aw) * 0.02D);
                            } else if (this.aw < 60) {
                                f2 = (float) ((double) f2 + (double) (60 - this.aw) * 0.01D);
                            }

                            if (this.random.nextFloat() < f2) {
                                f3 = MathHelper.a(this.random, 0.0F, 360.0F) * 0.017453292F;
                                f4 = MathHelper.a(this.random, 25.0F, 60.0F);
                                d6 = this.locX + (double) (MathHelper.sin(f3) * f4 * 0.1F);
                                d11 = (double) ((float) MathHelper.floor(this.getBoundingBox().b) + 1.0F);
                                d10 = this.locZ + (double) (MathHelper.cos(f3) * f4 * 0.1F);
                                block = worldserver.getType(new BlockPosition((int) d6, (int) d11 - 1, (int) d10)).getBlock();
                                if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                                    worldserver.a(EnumParticle.WATER_SPLASH, d6, d11, d10, 2 + this.random.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D, new int[0]);
                                }
                            }

                            if (this.aw <= 0) {
                                this.ay = MathHelper.a(this.random, 0.0F, 360.0F);
                                this.ax = MathHelper.nextInt(this.random, 20, 80);
                            }
                        } else {
                            this.aw = MathHelper.nextInt(this.random, world.paperConfig.fishingMinTicks, world.paperConfig.fishingMaxTicks); // Paper - Configurable fishing time range
                            this.aw -= EnchantmentManager.g(this.owner) * 20 * 5;
                        }
                    }

                    if (this.av > 0) {
                        this.motY -= (double) (this.random.nextFloat() * this.random.nextFloat() * this.random.nextFloat()) * 0.2D;
                    }
                }

                d5 = d7 * 2.0D - 1.0D;
                this.motY += 0.03999999910593033D * d5;
                if (d7 > 0.0D) {
                    f1 = (float) ((double) f1 * 0.9D);
                    this.motY *= 0.8D;
                }

                this.motX *= (double) f1;
                this.motY *= (double) f1;
                this.motZ *= (double) f1;
                this.setPosition(this.locX, this.locY, this.locZ);
            }
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInt("xTile", this.d);
        nbttagcompound.setInt("yTile", this.e);
        nbttagcompound.setInt("zTile", this.f);
        MinecraftKey minecraftkey = (MinecraftKey) Block.REGISTRY.b(this.g);

        nbttagcompound.setString("inTile", minecraftkey == null ? "" : minecraftkey.toString());
        nbttagcompound.setByte("inGround", (byte) (this.isInGround ? 1 : 0));
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.d = nbttagcompound.getInt("xTile");
        this.e = nbttagcompound.getInt("yTile");
        this.f = nbttagcompound.getInt("zTile");
        if (nbttagcompound.hasKeyOfType("inTile", 8)) {
            this.g = Block.getByName(nbttagcompound.getString("inTile"));
        } else {
            this.g = Block.getById(nbttagcompound.getByte("inTile") & 255);
        }

        this.isInGround = nbttagcompound.getByte("inGround") == 1;
    }

    public int j() {
        if (this.world.isClientSide) {
            return 0;
        } else {
            int i = 0;

            if (this.hooked != null) {
                // CraftBukkit start
                PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player) this.owner.getBukkitEntity(), this.hooked.getBukkitEntity(), (Fish) this.getBukkitEntity(), PlayerFishEvent.State.CAUGHT_ENTITY);
                this.world.getServer().getPluginManager().callEvent(playerFishEvent);

                if (playerFishEvent.isCancelled()) {
                    return 0;
                }
                // CraftBukkit end
                this.k();
                this.world.broadcastEntityEffect(this, (byte) 31);
                i = this.hooked instanceof EntityItem ? 3 : 5;
            } else if (this.av > 0) {
                LootTableInfo.a loottableinfo_a = new LootTableInfo.a((WorldServer) this.world);

                loottableinfo_a.a((float) EnchantmentManager.f(this.owner) + this.owner.dc());
                Iterator iterator = this.world.ak().a(LootTables.am).a(this.random, loottableinfo_a.a()).iterator();

                while (iterator.hasNext()) {
                    ItemStack itemstack = (ItemStack) iterator.next();
                    EntityItem entityitem = new EntityItem(this.world, this.locX, this.locY, this.locZ, itemstack);
                    // CraftBukkit start
                    PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player) this.owner.getBukkitEntity(), entityitem.getBukkitEntity(), (Fish) this.getBukkitEntity(), PlayerFishEvent.State.CAUGHT_FISH);
                    playerFishEvent.setExpToDrop(this.random.nextInt(6) + 1);
                    this.world.getServer().getPluginManager().callEvent(playerFishEvent);

                    if (playerFishEvent.isCancelled()) {
                        return 0;
                    }
                    // CraftBukkit end
                    double d0 = this.owner.locX - this.locX;
                    double d1 = this.owner.locY - this.locY;
                    double d2 = this.owner.locZ - this.locZ;
                    double d3 = (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    double d4 = 0.1D;

                    entityitem.motX = d0 * d4;
                    entityitem.motY = d1 * d4 + (double) MathHelper.sqrt(d3) * 0.08D;
                    entityitem.motZ = d2 * d4;
                    this.world.addEntity(entityitem);
                    // CraftBukkit start - this.random.nextInt(6) + 1 -> playerFishEvent.getExpToDrop()
                    if (playerFishEvent.getExpToDrop() > 0) {
                        this.owner.world.addEntity(new EntityExperienceOrb(this.owner.world, this.owner.locX, this.owner.locY + 0.5D, this.owner.locZ + 0.5D, playerFishEvent.getExpToDrop()));
                    }
                    // CraftBukkit end                
                }

                i = 1;
            }

            if (this.isInGround) {
                // CraftBukkit start
                PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player) this.owner.getBukkitEntity(), null, (Fish) this.getBukkitEntity(), PlayerFishEvent.State.IN_GROUND);
                this.world.getServer().getPluginManager().callEvent(playerFishEvent);

                if (playerFishEvent.isCancelled()) {
                    return 0;
                }
                // CraftBukkit end
                i = 2;
            }
            // CraftBukkit start
            if (i == 0) {
                PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player) this.owner.getBukkitEntity(), null, (Fish) this.getBukkitEntity(), PlayerFishEvent.State.FAILED_ATTEMPT);
                this.world.getServer().getPluginManager().callEvent(playerFishEvent);
                if (playerFishEvent.isCancelled()) {
                    return 0;
                }
            }
            // CraftBukkit end

            this.die();
            this.owner.hookedFish = null;
            return i;
        }
    }

    protected void k() {
        double d0 = this.owner.locX - this.locX;
        double d1 = this.owner.locY - this.locY;
        double d2 = this.owner.locZ - this.locZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
        double d4 = 0.1D;

        this.hooked.motX += d0 * d4;
        this.hooked.motY += d1 * d4 + (double) MathHelper.sqrt(d3) * 0.08D;
        this.hooked.motZ += d2 * d4;
    }

    public void die() {
        super.die();
        if (this.owner != null) {
            this.owner.hookedFish = null;
        }

    }
}
