package net.minecraft.server;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.koloboke.collect.set.hash.HashObjSets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.TravelAgent;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;
import co.aikar.timings.MinecraftTimings; // Paper
import co.aikar.timings.Timing; // Paper
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.plugin.PluginManager;
import org.torch.util.random.LightRandom;

public abstract class Entity implements ICommandListener {

    // CraftBukkit start
    private static final int CURRENT_LEVEL = 2;
    public static Random SHARED_RANDOM = new LightRandom(); // Paper
    static boolean isLevelAtLeast(NBTTagCompound tag, int level) {
        return tag.hasKey("Bukkit.updateLevel") && tag.getInt("Bukkit.updateLevel") >= level;
    }

    protected volatile CraftEntity bukkitEntity;

    EntityTrackerEntry tracker; // Paper
    public CraftEntity getBukkitEntity() {
        if (bukkitEntity == null) {
            bukkitEntity = CraftEntity.getEntity(world.getServer(), this);
        }
        return bukkitEntity;
    }
    // CraftBukikt end

    private static final Logger a = LogManager.getLogger();
    private static final List<ItemStack> b = Collections.emptyList();
    private static final AxisAlignedBB c = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    private static double f = 1.0D;
    private static int entityCount;
    private int id;
    public boolean i; public boolean blocksEntitySpawning() { return i; } // Paper - OBFHELPER
    public final List<Entity> passengers;
    protected int j;
    private Entity au;public void setVehicle(Entity entity) { this.au = entity; } // Paper // OBFHELPER
    public boolean attachedToPlayer;
    public World world;
    public double lastX;
    public double lastY;
    public double lastZ;
    public double locX;
    public double locY;
    public double locZ;
    // Paper start - getters to implement HopperPusher
    public double getX() {
        return locX;
    }

    public double getY() {
        return locY;
    }

    public double getZ() {
        return locZ;
    }
    // Paper end
    public double motX;
    public double motY;
    public double motZ;
    public float yaw;
    public float pitch;
    public float lastYaw;
    public float lastPitch;
    private AxisAlignedBB boundingBox;
    public boolean onGround;
    public boolean positionChanged;
    public boolean B;
    public boolean C;
    public boolean velocityChanged;
    protected boolean E;
    private boolean aw;
    public boolean dead;
    public float width;
    public float length;
    public float I;
    public float J;
    public float K;
    public float fallDistance;
    private int ax;
    public double M;
    public double N;
    public double O;
    public float P;
    public boolean noclip;
    public float R;
    protected Random random;
    public int ticksLived;
    public int fireTicks;
    public boolean inWater; // Spigot - protected -> public // PAIL
    public int noDamageTicks;
    protected boolean justCreated;
    protected boolean fireProof;
    protected DataWatcher datawatcher;
    protected static final DataWatcherObject<Byte> Z = DataWatcher.a(Entity.class, DataWatcherRegistry.a);
    private static final DataWatcherObject<Integer> az = DataWatcher.a(Entity.class, DataWatcherRegistry.b);
    private static final DataWatcherObject<String> aA = DataWatcher.a(Entity.class, DataWatcherRegistry.d);
    private static final DataWatcherObject<Boolean> aB = DataWatcher.a(Entity.class, DataWatcherRegistry.h);
    private static final DataWatcherObject<Boolean> aC = DataWatcher.a(Entity.class, DataWatcherRegistry.h);
    private static final DataWatcherObject<Boolean> aD = DataWatcher.a(Entity.class, DataWatcherRegistry.h);
    public boolean aa; public boolean isAddedToChunk() { return aa; } // Paper - OBFHELPER
    public int ab; public int getChunkX() { return ab; } // Paper - OBFHELPER
    public int ac; public int getChunkY() { return ac; } // Paper - OBFHELPER
    public int ad; public int getChunkZ() { return ad; } // Paper - OBFHELPER
    public boolean ah;
    public boolean impulse;
    public int portalCooldown;
    protected boolean ak; public boolean inPortal() { return ak; } // Paper - OBFHELPER
    protected int al;
    public int dimension;
    protected BlockPosition an;
    protected Vec3D ao;
    protected EnumDirection ap;
    private boolean invulnerable;
    protected UUID uniqueID;
    protected String ar;
    private final CommandObjectiveExecutor aF;
    public boolean glowing;
    private final Set<String> aG;
    private boolean aH;
    private double[] aI;
    private long aJ;
    public boolean valid; // CraftBukkit
    public org.bukkit.projectiles.ProjectileSource projectileSource; // CraftBukkit - For projectiles only
    public boolean forceExplosionKnockback; // CraftBukkit - SPIGOT-949
    public Timing tickTimer = MinecraftTimings.getEntityTimings(this); // Paper
    public Location origin; // Paper
    // Spigot start
    public final byte activationType = org.spigotmc.ActivationRange.initializeEntityActivationType(this);
    public final boolean defaultActivationState;
    public long activatedTick = Integer.MIN_VALUE;
    public boolean fromMobSpawner;
    protected int numCollisions = 0; // Paper
    public void inactiveTick() { }
    // Spigot end
    
    private boolean isInLava;
    private int lastLavaCheck = MinecraftServer.currentTick;

    public Entity(World world) {
        this.id = Entity.entityCount++;
        this.passengers = Lists.newArrayList();
        this.boundingBox = Entity.c;
        this.width = 0.6F;
        this.length = 1.8F;
        this.ax = 1;
        this.random = SHARED_RANDOM; // Paper
        this.fireTicks = -this.getMaxFireTicks();
        this.justCreated = true;
        this.uniqueID = MathHelper.a(this.random);
        this.ar = this.uniqueID.toString();
        this.aF = new CommandObjectiveExecutor();
        this.aG = HashObjSets.newMutableSet();
        this.aI = new double[] { 0.0D, 0.0D, 0.0D};
        this.world = world;
        this.setPosition(0.0D, 0.0D, 0.0D);
        if (world != null) {
            this.dimension = world.worldProvider.getDimensionManager().getDimensionID();
            // Spigot start
            this.defaultActivationState = org.spigotmc.ActivationRange.initializeEntityActivationState(this, world.spigotConfig);
        } else {
            this.defaultActivationState = false;
        }
        // Spigot end

        this.datawatcher = new DataWatcher(this);
        this.datawatcher.register(Entity.Z, Byte.valueOf((byte) 0));
        this.datawatcher.register(Entity.az, Integer.valueOf(300));
        this.datawatcher.register(Entity.aB, Boolean.valueOf(false));
        this.datawatcher.register(Entity.aA, "");
        this.datawatcher.register(Entity.aC, Boolean.valueOf(false));
        this.datawatcher.register(Entity.aD, Boolean.valueOf(false));
        this.i();
    }

    public int getId() {
        return this.id;
    }

    public void h(int i) {
        this.id = i;
    }

    public Set<String> P() {
        return this.aG;
    }

    public boolean a(String s) {
        if (this.aG.size() >= 1024) {
            return false;
        } else {
            this.aG.add(s);
            return true;
        }
    }

    public boolean b(String s) {
        return this.aG.remove(s);
    }

    public void Q() {
        this.die();
    }

    protected abstract void i();

    public DataWatcher getDataWatcher() {
        return this.datawatcher;
    }

    @Override
	public boolean equals(Object object) {
        return object instanceof Entity ? ((Entity) object).id == this.id : false;
    }

    @Override
	public int hashCode() {
        return this.id;
    }

    public void die() {
        this.dead = true;
    }

    public void b(boolean flag) {}

    public void setSize(float f, float f1) {
        if (f != this.width || f1 != this.length) {
            float f2 = this.width;

            this.width = f;
            this.length = f1;
            if (this.width < f2) {
                double d0 = f / 2.0D;

                this.a(new AxisAlignedBB(this.locX - d0, this.locY, this.locZ - d0, this.locX + d0, this.locY + this.length, this.locZ + d0));
                return;
            }

            AxisAlignedBB axisalignedbb = this.getBoundingBox();

            this.a(new AxisAlignedBB(axisalignedbb.a, axisalignedbb.b, axisalignedbb.c, axisalignedbb.a + this.width, axisalignedbb.b + this.length, axisalignedbb.c + this.width));
            if (this.width > f2 && !this.justCreated) {
                this.move(EnumMoveType.SELF, f2 - this.width, 0.0D, f2 - this.width);
            }
        }

    }

    public void setYawPitch(float f, float f1) {
        // CraftBukkit start - yaw was sometimes set to NaN, so we need to set it back to 0
        if (Float.isNaN(f)) {
            f = 0;
        }

        if (f == Float.POSITIVE_INFINITY || f == Float.NEGATIVE_INFINITY) {
            if (this instanceof EntityPlayer) {
                this.world.getServer().getLogger().warning(this.getName() + " was caught trying to crash the server with an invalid yaw");
                ((CraftPlayer) this.getBukkitEntity()).kickPlayer("Infinite yaw (Hacking?)"); //Spigot "Nope" -> Descriptive reason
            }
            f = 0;
        }

        // pitch was sometimes set to NaN, so we need to set it back to 0
        if (Float.isNaN(f1)) {
            f1 = 0;
        }

        if (f1 == Float.POSITIVE_INFINITY || f1 == Float.NEGATIVE_INFINITY) {
            if (this instanceof EntityPlayer) {
                this.world.getServer().getLogger().warning(this.getName() + " was caught trying to crash the server with an invalid pitch");
                ((CraftPlayer) this.getBukkitEntity()).kickPlayer("Infinite pitch (Hacking?)"); //Spigot "Nope" -> Descriptive reason
            }
            f1 = 0;
        }
        // CraftBukkit end

        this.yaw = f % 360.0F;
        this.pitch = f1 % 360.0F;
    }

    public void setPosition(double d0, double d1, double d2) {
        this.locX = d0;
        this.locY = d1;
        this.locZ = d2;
        float f = this.width / 2.0F;
        float f1 = this.length;

        this.a(new AxisAlignedBB(d0 - f, d1, d2 - f, d0 + f, d1 + f1, d2 + f));
    }

    public void A_() {
        this.setFlag(6, this.aO());

        this.U();
    }

    // CraftBukkit start
    public void postTick() {
        // No clean way to break out of ticking once the entity has been copied to a new world, so instead we move the portalling later in the tick cycle
        if (this.world instanceof WorldServer) {
            this.world.methodProfiler.a("portal");
            if (this.ak) {
                MinecraftServer minecraftserver = this.world.getMinecraftServer();

                if (true || minecraftserver.getAllowNether()) { // CraftBukkit
                    if (!this.isPassenger()) {
                        int i = this.V();

                        if (this.al++ >= i) {
                            this.al = i;
                            this.portalCooldown = this.aE();
                            byte b0;

                            if (this.world.worldProvider.getDimensionManager().getDimensionID() == -1) {
                                b0 = 0;
                            } else {
                                b0 = -1;
                            }

                            this.c(b0);
                        }
                    }

                    this.ak = false;
                }
            } else {
                if (this.al > 0) {
                    this.al -= 4;
                }

                if (this.al < 0) {
                    this.al = 0;
                }
            }

            this.H();
            
        }
    }
    // CraftBukkit end

    public void U() {
        this.world.methodProfiler.a("entityBaseTick");
        if (this.isPassenger() && this.bB().dead) {
            this.stopRiding();
        }

        if (this.j > 0) {
            --this.j;
        }

        this.I = this.J;
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        this.lastPitch = this.pitch;
        this.lastYaw = this.yaw;
        // Moved up to postTick
        /*
        if (this.world instanceof WorldServer) {
            this.world.methodProfiler.a("portal");
            if (this.ak) {
                MinecraftServer minecraftserver = this.world.getMinecraftServer();

                if (true || minecraftserver.getAllowNether()) { // CraftBukkit
                    if (!this.isPassenger()) {
                        int i = this.V();

                        if (this.al++ >= i) {
                            this.al = i;
                            this.portalCooldown = this.aE();
                            byte b0;

                            if (this.world.worldProvider.getDimensionManager().getDimensionID() == -1) {
                                b0 = 0;
                            } else {
                                b0 = -1;
                            }

                            this.c(b0);
                        }
                    }

                    this.ak = false;
                }
            } else {
                if (this.al > 0) {
                    this.al -= 4;
                }

                if (this.al < 0) {
                    this.al = 0;
                }
            }

            this.H();
            
        }
        */

        this.am();
        this.ak();
        if (this.fireTicks > 0) {
            if (this.fireProof) {
                this.fireTicks -= 4;
                if (this.fireTicks < 0) {
                    this.extinguish();
                }
            } else {
                if (this.fireTicks % 20 == 0) {
                    this.damageEntity(DamageSource.BURN, 1.0F);
                }

                --this.fireTicks;
            }
        }

        if (this.ao()) {
            this.burnFromLava();
            this.fallDistance *= 0.5F;
        }

        // Paper start - Configurable nether ceiling damage
        // Extracted to own function
        /*
        if (this.locY < -64.0D) {
            this.Y();
        }
        */
        this.checkAndDoHeightDamage();
        // Paper end

        this.setFlag(0, this.fireTicks > 0);

        this.justCreated = false;
        
    }

    // Paper start - Configurable top of nether void damage
    private boolean paperNetherCheck() {
        return this.world.paperConfig.netherVoidTopDamage && this.world.getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER && this.locY >= 128.0D;
    }

    protected void checkAndDoHeightDamage() {
        if (this.locY < -64.0D || paperNetherCheck()) {
            this.kill();
        }
    }
    // Paper end

    protected void H() {
        if (this.portalCooldown > 0) {
            --this.portalCooldown;
        }

    }

    public int V() {
        return 1;
    }

    protected void burnFromLava() {
        if (!this.fireProof) {
            this.damageEntity(DamageSource.LAVA, 4.0F);

            // CraftBukkit start - Fallen in lava TODO: this event spams!
            if (this instanceof EntityLiving) {
                if (fireTicks <= 0) {
                    // not on fire yet
                    // TODO: shouldn't be sending null for the block
                    org.bukkit.block.Block damager = null; // ((WorldServer) this.l).getWorld().getBlockAt(i, j, k);
                    org.bukkit.entity.Entity damagee = this.getBukkitEntity();
                    EntityCombustEvent combustEvent = new org.bukkit.event.entity.EntityCombustByBlockEvent(damager, damagee, 15);
                    this.world.getServer().getPluginManager().callEvent(combustEvent);

                    if (!combustEvent.isCancelled()) {
                        this.setOnFire(combustEvent.getDuration());
                    }
                } else {
                    // This will be called every single tick the entity is in lava, so don't throw an event
                    this.setOnFire(15);
                }
                return;
            }
            // CraftBukkit end - we also don't throw an event unless the object in lava is living, to save on some event calls
            this.setOnFire(15);
        }
    }

    public void setOnFire(int i) {
        int j = i * 20;

        if (this instanceof EntityLiving) {
            j = EnchantmentProtection.a((EntityLiving) this, j);
        }

        if (this.fireTicks < j) {
            this.fireTicks = j;
        }

    }

    public void extinguish() {
        this.fireTicks = 0;
    }

    protected final void kill() { this.Y(); } // Paper - OBFHELPER
    protected void Y() {
        this.die();
    }

    public boolean c(double d0, double d1, double d2) {
        AxisAlignedBB axisalignedbb = this.getBoundingBox().d(d0, d1, d2);

        return this.b(axisalignedbb);
    }

    private boolean b(AxisAlignedBB axisalignedbb) {
        return this.world.getCubes(this, axisalignedbb).isEmpty() && !this.world.containsLiquid(axisalignedbb);
    }

    public void move(EnumMoveType enummovetype, double d0, double d1, double d2) {
        if (this.noclip) {
            this.a(this.getBoundingBox().d(d0, d1, d2));
            this.recalcPosition();
        } else {
            // CraftBukkit start - Don't do anything if we aren't moving
            // We need to do this regardless of whether or not we are moving thanks to portals
            try {
                this.checkBlockCollisions();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Checking entity block collision");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity being checked for collision");

                this.appendEntityCrashDetails(crashreportsystemdetails);
                throw new ReportedException(crashreport);
            }
            // Check if we're moving
            if (d0 == 0 && d1 == 0 && d2 == 0 && this.isVehicle() && this.isPassenger()) {
                return;
            }
            // CraftBukkit end
            if (enummovetype == EnumMoveType.PISTON) {
                long i = this.world.getTime();

                if (i != this.aJ) {
                    Arrays.fill(this.aI, 0.0D);
                    this.aJ = i;
                }

                int j;
                double d3;

                if (d0 != 0.0D) {
                    j = EnumDirection.EnumAxis.X.ordinal();
                    d3 = MathHelper.a(d0 + this.aI[j], -0.51D, 0.51D);
                    d0 = d3 - this.aI[j];
                    this.aI[j] = d3;
                    if (Math.abs(d0) <= 9.999999747378752E-6D) {
                        return;
                    }
                } else if (d1 != 0.0D) {
                    j = EnumDirection.EnumAxis.Y.ordinal();
                    d3 = MathHelper.a(d1 + this.aI[j], -0.51D, 0.51D);
                    d1 = d3 - this.aI[j];
                    this.aI[j] = d3;
                    if (Math.abs(d1) <= 9.999999747378752E-6D) {
                        return;
                    }
                } else {
                    if (d2 == 0.0D) {
                        return;
                    }

                    j = EnumDirection.EnumAxis.Z.ordinal();
                    d3 = MathHelper.a(d2 + this.aI[j], -0.51D, 0.51D);
                    d2 = d3 - this.aI[j];
                    this.aI[j] = d3;
                    if (Math.abs(d2) <= 9.999999747378752E-6D) {
                        return;
                    }
                }
            }

            this.world.methodProfiler.a("move");
            double d4 = this.locX;
            double d5 = this.locY;
            double d6 = this.locZ;

            if (this.E) {
                this.E = false;
                d0 *= 0.25D;
                d1 *= 0.05000000074505806D;
                d2 *= 0.25D;
                this.motX = 0.0D;
                this.motY = 0.0D;
                this.motZ = 0.0D;
            }

            double d7 = d0;
            double d8 = d1;
            double d9 = d2;

            if ((enummovetype == EnumMoveType.SELF || enummovetype == EnumMoveType.PLAYER) && this.onGround && this.isSneaking() && this instanceof EntityHuman) {
                for (double d10 = 0.05D; d0 != 0.0D && this.world.getCubes(this, this.getBoundingBox().d(d0, (-this.P), 0.0D)).isEmpty(); d7 = d0) {
                    if (d0 < 0.05D && d0 >= -0.05D) {
                        d0 = 0.0D;
                    } else if (d0 > 0.0D) {
                        d0 -= 0.05D;
                    } else {
                        d0 += 0.05D;
                    }
                }

                for (; d2 != 0.0D && this.world.getCubes(this, this.getBoundingBox().d(0.0D, (-this.P), d2)).isEmpty(); d9 = d2) {
                    if (d2 < 0.05D && d2 >= -0.05D) {
                        d2 = 0.0D;
                    } else if (d2 > 0.0D) {
                        d2 -= 0.05D;
                    } else {
                        d2 += 0.05D;
                    }
                }

                for (; d0 != 0.0D && d2 != 0.0D && this.world.getCubes(this, this.getBoundingBox().d(d0, (-this.P), d2)).isEmpty(); d9 = d2) {
                    if (d0 < 0.05D && d0 >= -0.05D) {
                        d0 = 0.0D;
                    } else if (d0 > 0.0D) {
                        d0 -= 0.05D;
                    } else {
                        d0 += 0.05D;
                    }

                    d7 = d0;
                    if (d2 < 0.05D && d2 >= -0.05D) {
                        d2 = 0.0D;
                    } else if (d2 > 0.0D) {
                        d2 -= 0.05D;
                    } else {
                        d2 += 0.05D;
                    }
                }
            }

            List list = this.world.getCubes(this, this.getBoundingBox().b(d0, d1, d2));
            AxisAlignedBB axisalignedbb = this.getBoundingBox();
            int k;
            int l;

            if (d1 != 0.0D) {
                k = 0;

                for (l = list.size(); k < l; ++k) {
                    d1 = ((AxisAlignedBB) list.get(k)).b(this.getBoundingBox(), d1);
                }

                this.a(this.getBoundingBox().d(0.0D, d1, 0.0D));
            }

            if (d0 != 0.0D) {
                k = 0;

                for (l = list.size(); k < l; ++k) {
                    d0 = ((AxisAlignedBB) list.get(k)).a(this.getBoundingBox(), d0);
                }

                if (d0 != 0.0D) {
                    this.a(this.getBoundingBox().d(d0, 0.0D, 0.0D));
                }
            }

            if (d2 != 0.0D) {
                k = 0;

                for (l = list.size(); k < l; ++k) {
                    d2 = ((AxisAlignedBB) list.get(k)).c(this.getBoundingBox(), d2);
                }

                if (d2 != 0.0D) {
                    this.a(this.getBoundingBox().d(0.0D, 0.0D, d2));
                }
            }

            boolean flag = this.onGround || d1 != d8 && d1 < 0.0D; // CraftBukkit - decompile error
            double d11;

            if (this.P > 0.0F && flag && (d7 != d0 || d9 != d2)) {
                double d12 = d0;
                double d13 = d1;
                double d14 = d2;
                AxisAlignedBB axisalignedbb1 = this.getBoundingBox();

                this.a(axisalignedbb);
                d1 = this.P;
                List list1 = this.world.getCubes(this, this.getBoundingBox().b(d7, d1, d9));
                AxisAlignedBB axisalignedbb2 = this.getBoundingBox();
                AxisAlignedBB axisalignedbb3 = axisalignedbb2.b(d7, 0.0D, d9);

                d11 = d1;
                int i1 = 0;

                for (int j1 = list1.size(); i1 < j1; ++i1) {
                    d11 = ((AxisAlignedBB) list1.get(i1)).b(axisalignedbb3, d11);
                }

                axisalignedbb2 = axisalignedbb2.d(0.0D, d11, 0.0D);
                double d15 = d7;
                int k1 = 0;

                for (int l1 = list1.size(); k1 < l1; ++k1) {
                    d15 = ((AxisAlignedBB) list1.get(k1)).a(axisalignedbb2, d15);
                }

                axisalignedbb2 = axisalignedbb2.d(d15, 0.0D, 0.0D);
                double d16 = d9;
                int i2 = 0;

                for (int j2 = list1.size(); i2 < j2; ++i2) {
                    d16 = ((AxisAlignedBB) list1.get(i2)).c(axisalignedbb2, d16);
                }

                axisalignedbb2 = axisalignedbb2.d(0.0D, 0.0D, d16);
                AxisAlignedBB axisalignedbb4 = this.getBoundingBox();
                double d17 = d1;
                int k2 = 0;

                for (int l2 = list1.size(); k2 < l2; ++k2) {
                    d17 = ((AxisAlignedBB) list1.get(k2)).b(axisalignedbb4, d17);
                }

                axisalignedbb4 = axisalignedbb4.d(0.0D, d17, 0.0D);
                double d18 = d7;
                int i3 = 0;

                for (int j3 = list1.size(); i3 < j3; ++i3) {
                    d18 = ((AxisAlignedBB) list1.get(i3)).a(axisalignedbb4, d18);
                }

                axisalignedbb4 = axisalignedbb4.d(d18, 0.0D, 0.0D);
                double d19 = d9;
                int k3 = 0;

                for (int l3 = list1.size(); k3 < l3; ++k3) {
                    d19 = ((AxisAlignedBB) list1.get(k3)).c(axisalignedbb4, d19);
                }

                axisalignedbb4 = axisalignedbb4.d(0.0D, 0.0D, d19);
                double d20 = d15 * d15 + d16 * d16;
                double d21 = d18 * d18 + d19 * d19;

                if (d20 > d21) {
                    d0 = d15;
                    d2 = d16;
                    d1 = -d11;
                    this.a(axisalignedbb2);
                } else {
                    d0 = d18;
                    d2 = d19;
                    d1 = -d17;
                    this.a(axisalignedbb4);
                }

                int i4 = 0;

                for (int j4 = list1.size(); i4 < j4; ++i4) {
                    d1 = ((AxisAlignedBB) list1.get(i4)).b(this.getBoundingBox(), d1);
                }

                this.a(this.getBoundingBox().d(0.0D, d1, 0.0D));
                if (d12 * d12 + d14 * d14 >= d0 * d0 + d2 * d2) {
                    d0 = d12;
                    d1 = d13;
                    d2 = d14;
                    this.a(axisalignedbb1);
                }
            }

            
            this.world.methodProfiler.a("rest");
            this.recalcPosition();
            this.positionChanged = d7 != d0 || d9 != d2;
            this.B = d1 != d8; // CraftBukkit - decompile error
            this.onGround = this.B && d8 < 0.0D;
            this.C = this.positionChanged || this.B;
            l = MathHelper.floor(this.locX);
            int k4 = MathHelper.floor(this.locY - 0.20000000298023224D);
            int l4 = MathHelper.floor(this.locZ);
            BlockPosition blockposition = new BlockPosition(l, k4, l4);
            IBlockData iblockdata = this.world.getType(blockposition);

            if (iblockdata.getMaterial() == Material.AIR) {
                BlockPosition blockposition1 = blockposition.down();
                IBlockData iblockdata1 = this.world.getType(blockposition1);
                Block block = iblockdata1.getBlock();

                if (block instanceof BlockFence || block instanceof BlockCobbleWall || block instanceof BlockFenceGate) {
                    iblockdata = iblockdata1;
                    blockposition = blockposition1;
                }
            }

            this.a(d1, this.onGround, iblockdata, blockposition);
            if (d7 != d0) {
                this.motX = 0.0D;
            }

            if (d9 != d2) {
                this.motZ = 0.0D;
            }

            Block block1 = iblockdata.getBlock();

            if (d8 != d1) {
                block1.a(this.world, this);
            }

            // CraftBukkit start
            if (positionChanged && getBukkitEntity() instanceof Vehicle) {
                Vehicle vehicle = (Vehicle) this.getBukkitEntity();
                org.bukkit.block.Block bl = this.world.getWorld().getBlockAt(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ));

                if (d6 > d0) {
                    bl = bl.getRelative(BlockFace.EAST);
                } else if (d6 < d0) {
                    bl = bl.getRelative(BlockFace.WEST);
                } else if (d8 > d2) {
                    bl = bl.getRelative(BlockFace.SOUTH);
                } else if (d8 < d2) {
                    bl = bl.getRelative(BlockFace.NORTH);
                }

                if (bl.getType() != org.bukkit.Material.AIR) {
                    VehicleBlockCollisionEvent event = new VehicleBlockCollisionEvent(vehicle, bl);
                    world.getServer().getPluginManager().callEvent(event);
                }
            }
            // CraftBukkit end

            if (this.playStepSound() && (!this.onGround || !this.isSneaking() || !(this instanceof EntityHuman)) && !this.isPassenger()) {
                double d22 = this.locX - d4;
                double d23 = this.locY - d5;

                d11 = this.locZ - d6;
                if (block1 != Blocks.LADDER) {
                    d23 = 0.0D;
                }

                if (block1 != null && this.onGround) {
                    block1.stepOn(this.world, blockposition, this);
                }

                this.J = (float) (this.J + MathHelper.sqrt(d22 * d22 + d11 * d11) * 0.6D);
                this.K = (float) (this.K + MathHelper.sqrt(d22 * d22 + d23 * d23 + d11 * d11) * 0.6D);
                if (this.K > this.ax && iblockdata.getMaterial() != Material.AIR) {
                    this.ax = (int) this.K + 1;
                    if (this.isInWater()) {
                        Entity entity = this.isVehicle() && this.bw() != null ? this.bw() : this;
                        float f = entity == this ? 0.35F : 0.4F;
                        float f1 = MathHelper.sqrt(entity.motX * entity.motX * 0.20000000298023224D + entity.motY * entity.motY + entity.motZ * entity.motZ * 0.20000000298023224D) * f;

                        if (f1 > 1.0F) {
                            f1 = 1.0F;
                        }

                        this.a(this.aa(), f1, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                    } else {
                        this.a(blockposition, block1);
                    }
                }
            }

            // CraftBukkit start - Move to the top of the method
            /*
            try {
                this.checkBlockCollisions();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Checking entity block collision");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity being checked for collision");

                this.appendEntityCrashDetails(crashreportsystemdetails);
                throw new ReportedException(crashreport);
            }
            */
            // CraftBukkit end

            boolean flag1 = this.ai();

            if (this.world.e(this.getBoundingBox().shrink(0.001D)) && !this.isInWater()) {
            	this.burn(1);
                if (!flag1) {
                    ++this.fireTicks;
                    if (this.fireTicks == 0) {
                        // CraftBukkit start
                        EntityCombustEvent event = new org.bukkit.event.entity.EntityCombustByBlockEvent(null, getBukkitEntity(), 8);
                        world.getServer().getPluginManager().callEvent(event);

                        if (!event.isCancelled()) {
                            this.setOnFire(event.getDuration());
                        }
                        // CraftBukkit end
                    }
                }
            } else if (this.fireTicks <= 0) {
                this.fireTicks = -this.getMaxFireTicks();
            }

            if (flag1 && this.isBurning()) {
                this.a(SoundEffects.bQ, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                this.fireTicks = -this.getMaxFireTicks();
            }

            
        }
    }

    public void recalcPosition() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();

        this.locX = (axisalignedbb.a + axisalignedbb.d) / 2.0D;
        this.locY = axisalignedbb.b;
        this.locZ = (axisalignedbb.c + axisalignedbb.f) / 2.0D;
    }

    protected SoundEffect aa() {
        return SoundEffects.bU;
    }

    protected SoundEffect ab() {
        return SoundEffects.bT;
    }

    protected void checkBlockCollisions() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.d(axisalignedbb.a + 0.001D, axisalignedbb.b + 0.001D, axisalignedbb.c + 0.001D);
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition1 = BlockPosition.PooledBlockPosition.d(axisalignedbb.d - 0.001D, axisalignedbb.e - 0.001D, axisalignedbb.f - 0.001D);
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition2 = BlockPosition.PooledBlockPosition.s();

        if (this.world.areChunksLoadedBetween(blockposition_pooledblockposition, blockposition_pooledblockposition1)) {
            for (int i = blockposition_pooledblockposition.getX(); i <= blockposition_pooledblockposition1.getX(); ++i) {
                for (int j = blockposition_pooledblockposition.getY(); j <= blockposition_pooledblockposition1.getY(); ++j) {
                    for (int k = blockposition_pooledblockposition.getZ(); k <= blockposition_pooledblockposition1.getZ(); ++k) {
                        blockposition_pooledblockposition2.f(i, j, k);
                        IBlockData iblockdata = this.world.getType(blockposition_pooledblockposition2);

                        try {
                            iblockdata.getBlock().a(this.world, blockposition_pooledblockposition2, iblockdata, this);
                        } catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.a(throwable, "Colliding entity with block");
                            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being collided with");

                            CrashReportSystemDetails.a(crashreportsystemdetails, blockposition_pooledblockposition2, iblockdata);
                            throw new ReportedException(crashreport);
                        }
                    }
                }
            }
        }

        blockposition_pooledblockposition.t();
        blockposition_pooledblockposition1.t();
        blockposition_pooledblockposition2.t();
    }

    protected void a(BlockPosition blockposition, Block block) {
        SoundEffectType soundeffecttype = block.getStepSound();

        if (this.world.getType(blockposition.up()).getBlock() == Blocks.SNOW_LAYER) {
            soundeffecttype = Blocks.SNOW_LAYER.getStepSound();
            this.a(soundeffecttype.d(), soundeffecttype.a() * 0.15F, soundeffecttype.b());
        } else if (!block.getBlockData().getMaterial().isLiquid()) {
            this.a(soundeffecttype.d(), soundeffecttype.a() * 0.15F, soundeffecttype.b());
        }

    }

    public void a(SoundEffect soundeffect, float f, float f1) {
        if (!this.isSilent()) {
            this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, soundeffect, this.bC(), f, f1);
        }

    }

    public boolean isSilent() {
        return this.datawatcher.get(Entity.aC).booleanValue();
    }

    public void setSilent(boolean flag) {
        this.datawatcher.set(Entity.aC, Boolean.valueOf(flag));
    }

    public boolean isNoGravity() {
        return this.datawatcher.get(Entity.aD).booleanValue();
    }

    public void setNoGravity(boolean flag) {
        this.datawatcher.set(Entity.aD, Boolean.valueOf(flag));
    }

    protected boolean playStepSound() {
        return true;
    }

    protected void a(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {
        if (flag) {
            if (this.fallDistance > 0.0F) {
                iblockdata.getBlock().fallOn(this.world, blockposition, this, this.fallDistance);
            }

            this.fallDistance = 0.0F;
        } else if (d0 < 0.0D) {
            this.fallDistance = (float) (this.fallDistance - d0);
        }

    }

    @Nullable
    public AxisAlignedBB ag() {
        return null;
    }

    protected void burn(float i) { // CraftBukkit - int -> float
        if (!this.fireProof) {
            this.damageEntity(DamageSource.FIRE, i);
        }

    }

    public final boolean isFireProof() {
        return this.fireProof;
    }

    public void e(float f, float f1) {
        if (this.isVehicle()) {
            Iterator iterator = this.bx().iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                entity.e(f, f1);
            }
        }

    }

    public boolean ai() {
        if (this.inWater) {
            return true;
        } else {
            BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.d(this.locX, this.locY, this.locZ);

            if (!this.world.isRainingAt(blockposition_pooledblockposition) && !this.world.isRainingAt(blockposition_pooledblockposition.e(this.locX, this.locY + this.length, this.locZ))) {
                blockposition_pooledblockposition.t();
                return false;
            } else {
                blockposition_pooledblockposition.t();
                return true;
            }
        }
    }

    public boolean isInWater() {
        return this.inWater;
    }

    public boolean ak() {
        // Paper start - OBFHELPER
        return this.doWaterMovement();
    }

    public boolean doWaterMovement() {
        // Paper end
        if (this.bB() instanceof EntityBoat) {
            this.inWater = false;
        } else if (this.world.a(this.getBoundingBox().grow(0.0D, -0.4000000059604645D, 0.0D).shrink(0.001D), Material.WATER, this)) {
            if (!this.inWater && !this.justCreated) {
                this.al();
            }

            this.fallDistance = 0.0F;
            this.inWater = true;
            this.extinguish();
        } else {
            this.inWater = false;
        }

        return this.inWater;
    }

    protected void al() {
        Entity entity = this.isVehicle() && this.bw() != null ? this.bw() : this;
        float f = entity == this ? 0.2F : 0.9F;
        float f1 = MathHelper.sqrt(entity.motX * entity.motX * 0.20000000298023224D + entity.motY * entity.motY + entity.motZ * entity.motZ * 0.20000000298023224D) * f;

        if (f1 > 1.0F) {
            f1 = 1.0F;
        }

        this.a(this.ab(), f1, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
        float f2 = MathHelper.floor(this.getBoundingBox().b);

        int i;
        float f3;
        float f4;

        for (i = 0; i < 1.0F + this.width * 20.0F; ++i) {
            f3 = (this.random.nextFloat() * 2.0F - 1.0F) * this.width;
            f4 = (this.random.nextFloat() * 2.0F - 1.0F) * this.width;
            this.world.addParticle(EnumParticle.WATER_BUBBLE, this.locX + f3, f2 + 1.0F, this.locZ + f4, this.motX, this.motY - this.random.nextFloat() * 0.2F, this.motZ, new int[0]);
        }

        for (i = 0; i < 1.0F + this.width * 20.0F; ++i) {
            f3 = (this.random.nextFloat() * 2.0F - 1.0F) * this.width;
            f4 = (this.random.nextFloat() * 2.0F - 1.0F) * this.width;
            this.world.addParticle(EnumParticle.WATER_SPLASH, this.locX + f3, f2 + 1.0F, this.locZ + f4, this.motX, this.motY, this.motZ, new int[0]);
        }

    }

    public void am() {
        if (this.isSprinting() && !this.isInWater()) {
            this.an();
        }

    }

    protected void an() {
        int i = MathHelper.floor(this.locX);
        int j = MathHelper.floor(this.locY - 0.20000000298023224D);
        int k = MathHelper.floor(this.locZ);
        BlockPosition blockposition = new BlockPosition(i, j, k);
        IBlockData iblockdata = this.world.getType(blockposition);

        if (iblockdata.j() != EnumRenderType.INVISIBLE) {
            this.world.addParticle(EnumParticle.BLOCK_CRACK, this.locX + (this.random.nextFloat() - 0.5D) * this.width, this.getBoundingBox().b + 0.1D, this.locZ + (this.random.nextFloat() - 0.5D) * this.width, -this.motX * 4.0D, 1.5D, -this.motZ * 4.0D, new int[] { Block.getCombinedId(iblockdata)});
        }

    }

    public boolean a(Material material) {
        if (this.bB() instanceof EntityBoat) {
            return false;
        } else {
            double d0 = this.locY + this.getHeadHeight();
            BlockPosition blockposition = new BlockPosition(this.locX, d0, this.locZ);
            IBlockData iblockdata = this.world.getType(blockposition);

            if (iblockdata.getMaterial() == material) {
                float f = BlockFluids.e(iblockdata.getBlock().toLegacyData(iblockdata)) - 0.11111111F;
                float f1 = blockposition.getY() + 1 - f;
                boolean flag = d0 < f1;

                return !flag && this instanceof EntityHuman ? false : flag;
            } else {
                return false;
            }
        }
    }

    public boolean isInLava() { return this.ao(); } // OBFHELPER
    public boolean ao() {
    	// Only check for lava once per tick
        if (this.lastLavaCheck != MinecraftServer.currentTick) {
        	this.lastLavaCheck = MinecraftServer.currentTick;
        	this.isInLava = this.world.a(this.getBoundingBox().grow(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.LAVA);
        }
        return this.isInLava;
    }

    public void a(float f, float f1, float f2) {
        float f3 = f * f + f1 * f1;

        if (f3 >= 1.0E-4F) {
            f3 = MathHelper.c(f3);
            if (f3 < 1.0F) {
                f3 = 1.0F;
            }

            f3 = f2 / f3;
            f *= f3;
            f1 *= f3;
            float f4 = MathHelper.sin(this.yaw * 0.017453292F);
            float f5 = MathHelper.cos(this.yaw * 0.017453292F);

            this.motX += f * f5 - f1 * f4;
            this.motZ += f1 * f5 + f * f4;
        }
    }

    public float e(float f) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(MathHelper.floor(this.locX), 0, MathHelper.floor(this.locZ));

        if (this.world.isLoaded(blockposition_mutableblockposition)) {
            blockposition_mutableblockposition.p(MathHelper.floor(this.locY + this.getHeadHeight()));
            return this.world.n(blockposition_mutableblockposition);
        } else {
            return 0.0F;
        }
    }

    public void spawnIn(World world) {
        // CraftBukkit start
        if (world == null) {
            die();
            this.world = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();
            return;
        }
        // CraftBukkit end
        this.world = world;
    }

    public void setLocation(double d0, double d1, double d2, float f, float f1) {
        this.locX = MathHelper.a(d0, -3.0E7D, 3.0E7D);
        this.locY = d1;
        this.locZ = MathHelper.a(d2, -3.0E7D, 3.0E7D);
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        f1 = MathHelper.a(f1, -90.0F, 90.0F);
        this.yaw = f;
        this.pitch = f1;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        double d3 = this.lastYaw - f;

        if (d3 < -180.0D) {
            this.lastYaw += 360.0F;
        }

        if (d3 >= 180.0D) {
            this.lastYaw -= 360.0F;
        }

        this.setPosition(this.locX, this.locY, this.locZ);
        this.setYawPitch(f, f1);
    }

    public void setPositionRotation(BlockPosition blockposition, float f, float f1) {
        this.setPositionRotation(blockposition.getX() + 0.5D, blockposition.getY(), blockposition.getZ() + 0.5D, f, f1);
    }

    public void setPositionRotation(double d0, double d1, double d2, float f, float f1) {
        this.locX = d0;
        this.locY = d1;
        this.locZ = d2;
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        this.M = this.locX;
        this.N = this.locY;
        this.O = this.locZ;
        this.yaw = f;
        this.pitch = f1;
        this.setPosition(this.locX, this.locY, this.locZ);
    }

    public float g(Entity entity) {
        float f = (float) (this.locX - entity.locX);
        float f1 = (float) (this.locY - entity.locY);
        float f2 = (float) (this.locZ - entity.locZ);

        return MathHelper.c(f * f + f1 * f1 + f2 * f2);
    }

    public double d(double d0, double d1, double d2) {
        double d3 = this.locX - d0;
        double d4 = this.locY - d1;
        double d5 = this.locZ - d2;

        return d3 * d3 + d4 * d4 + d5 * d5;
    }

    public double c(BlockPosition blockposition) {
        return blockposition.distanceSquared(this.locX, this.locY, this.locZ);
    }

    public double d(BlockPosition blockposition) {
        return blockposition.g(this.locX, this.locY, this.locZ);
    }

    public double e(double d0, double d1, double d2) {
        // Paper start - OBFHELPER
        return this.getDistance(d0, d1, d2);
    }

    public double getDistance(double d0, double d1, double d2) {
        // Paper end
        double d3 = this.locX - d0;
        double d4 = this.locY - d1;
        double d5 = this.locZ - d2;

        return MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
    }

    public double h(Entity entity) {
        double d0 = this.locX - entity.locX;
        double d1 = this.locY - entity.locY;
        double d2 = this.locZ - entity.locZ;

        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public void d(EntityHuman entityhuman) {}

    public void collide(Entity entity) {
        if (!this.x(entity)) {
            if (!entity.noclip && !this.noclip) {
                double d0 = entity.locX - this.locX;
                double d1 = entity.locZ - this.locZ;
                double d2 = MathHelper.a(d0, d1);

                if (d2 >= 0.009999999776482582D) {
                    d2 = MathHelper.sqrt(d2);
                    d0 /= d2;
                    d1 /= d2;
                    double d3 = 1.0D / d2;

                    if (d3 > 1.0D) {
                        d3 = 1.0D;
                    }

                    d0 *= d3;
                    d1 *= d3;
                    d0 *= 0.05000000074505806D;
                    d1 *= 0.05000000074505806D;
                    d0 *= 1.0F - this.R;
                    d1 *= 1.0F - this.R;
                    if (!this.isVehicle()) {
                        this.f(-d0, 0.0D, -d1);
                    }

                    if (!entity.isVehicle()) {
                        entity.f(d0, 0.0D, d1);
                    }
                }

            }
        }
    }

    public void f(double d0, double d1, double d2) {
        // Paper start - OBFHELPER
        this.addVelocity(d0, d1, d2);
    }

    public void addVelocity(double d0, double d1, double d2) {
        this.motX += d0;
        this.motY += d1;
        this.motZ += d2;
        this.impulse = true;
    }

    protected void ap() {
        this.velocityChanged = true;
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else {
            this.ap();
            return false;
        }
    }

    public Vec3D f(float f) {
        if (f == 1.0F) {
            return this.f(this.pitch, this.yaw);
        } else {
            float f1 = this.lastPitch + (this.pitch - this.lastPitch) * f;
            float f2 = this.lastYaw + (this.yaw - this.lastYaw) * f;

            return this.f(f1, f2);
        }
    }

    protected final Vec3D f(float f, float f1) {
        float f2 = MathHelper.cos(-f1 * 0.017453292F - 3.1415927F);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - 3.1415927F);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);

        return new Vec3D(f3 * f4, f5, f2 * f4);
    }

    public Vec3D g(float f) {
        if (f == 1.0F) {
            return new Vec3D(this.locX, this.locY + this.getHeadHeight(), this.locZ);
        } else {
            double d0 = this.lastX + (this.locX - this.lastX) * f;
            double d1 = this.lastY + (this.locY - this.lastY) * f + this.getHeadHeight();
            double d2 = this.lastZ + (this.locZ - this.lastZ) * f;

            return new Vec3D(d0, d1, d2);
        }
    }

    public boolean isInteractable() {
        return false;
    }

    public boolean isCollidable() {
        return false;
    }

    public void b(Entity entity, int i) {}

    public boolean c(NBTTagCompound nbttagcompound) {
        String s = this.at();

        if (!this.dead && s != null) {
            nbttagcompound.setString("id", s);
            this.e(nbttagcompound);
            return true;
        } else {
            return false;
        }
    }

    public boolean d(NBTTagCompound nbttagcompound) {
        String s = this.at();

        if (!this.dead && s != null && !this.isPassenger()) {
            nbttagcompound.setString("id", s);
            this.e(nbttagcompound);
            return true;
        } else {
            return false;
        }
    }

    public static void b(DataConverterManager dataconvertermanager) {
        dataconvertermanager.a(DataConverterTypes.ENTITY, new DataInspector() {
            @Override
			public NBTTagCompound a(DataConverter dataconverter, NBTTagCompound nbttagcompound, int i) {
                if (nbttagcompound.hasKeyOfType("Passengers", 9)) {
                    NBTTagList nbttaglist = nbttagcompound.getList("Passengers", 10);

                    for (int j = 0; j < nbttaglist.size(); ++j) {
                        nbttaglist.a(j, dataconverter.a(DataConverterTypes.ENTITY, nbttaglist.get(j), i));
                    }
                }

                return nbttagcompound;
            }
        });
    }

    /** PAIL: writeToNBT */
    public NBTTagCompound e(NBTTagCompound compound) {
        try {
            compound.set("Pos", this.createDoubleList(new double[] { this.locX, this.locY, this.locZ }));
            compound.set("Motion", this.createDoubleList(new double[] { this.motX, this.motY, this.motZ }));

            // CraftBukkit start - Checking for NaN pitch/yaw and resetting to zero
            // TODO: make sure this is the best way to address this.
            if (Float.isNaN(this.yaw)) {
                this.yaw = 0;
            }
            
            if (Float.isNaN(this.pitch)) {
                this.pitch = 0;
            }
            // CraftBukkit end

            compound.set("Rotation", this.createFloatList(new float[] { this.yaw, this.pitch }));
            
            compound.setFloat("FallDistance", this.fallDistance);
            compound.setShort("Fire", (short) this.fireTicks);
            compound.setShort("Air", (short) this.getAirTicks());
            compound.setBoolean("OnGround", this.onGround);
            compound.setInt("Dimension", this.dimension);
            compound.setBoolean("Invulnerable", this.invulnerable);
            compound.setInt("PortalCooldown", this.portalCooldown);
            compound.setUUID("UUID", this.getUniqueID());
            
            // CraftBukkit start
            // PAIL: Check above UUID reads 1.8 properly, ie: UUIDMost / UUIDLeast
            compound.setLong("WorldUUIDLeast", this.world.getDataManager().getUUID().getLeastSignificantBits());
            compound.setLong("WorldUUIDMost", this.world.getDataManager().getUUID().getMostSignificantBits());
            
            compound.setInt("Bukkit.updateLevel", CURRENT_LEVEL);
            compound.setInt("Spigot.ticksLived", this.ticksLived);
            // CraftBukkit end
            
            if (this.hasCustomName()) {
                compound.setString("CustomName", this.getCustomName());
            }

            if (this.getCustomNameVisible()) {
                compound.setBoolean("CustomNameVisible", this.getCustomNameVisible());
            }

            this.aF.b(compound); // PAIL: cmdResultStats.writeStatsToNBT(compound);
            
            if (this.isSilent()) {
                compound.setBoolean("Silent", this.isSilent());
            }

            if (this.isNoGravity()) {
                compound.setBoolean("NoGravity", this.isNoGravity());
            }

            if (this.glowing) {
                compound.setBoolean("Glowing", this.glowing);
            }

            if (this.aG.size() > 0) { // PAIL: aG -> tags
                NBTTagList tagList = new NBTTagList();
                for (String tag : this.aG) tagList.add(new NBTTagString(tag));
                
                compound.set("Tags", tagList);
            }
            
            this.b(compound); // PAIL: writeEntityToNBT
            
            if (this.isVehicle()) {
                NBTTagList tagList = new NBTTagList();
                
                for (Entity passenger : this.bx()) { // PAIL: bx() -> getPassengers()
                    NBTTagCompound pCompound = new NBTTagCompound();

                    if (passenger.c(pCompound)) {
                        tagList.add(pCompound);
                    }
                }
                
                if (!tagList.isEmpty()) compound.set("Passengers", tagList);
            }

            // Paper start - Save the entity's origin location
            if (origin != null) {
                compound.set("Paper.Origin", this.createDoubleList(origin.getX(), origin.getY(), origin.getZ()));
            }
            // Paper end
            
            return compound;
        } catch (Throwable t) {
            CrashReport report = CrashReport.a(t, "Saving entity NBT");
            CrashReportSystemDetails details = report.a("Entity being saved");
            
            this.appendEntityCrashDetails(details);
            throw new ReportedException(report);
        }
    }

    public void f(NBTTagCompound nbttagcompound) {
        try {
            NBTTagList nbttaglist = nbttagcompound.getList("Pos", 6);
            NBTTagList nbttaglist1 = nbttagcompound.getList("Motion", 6);
            NBTTagList nbttaglist2 = nbttagcompound.getList("Rotation", 5);

            this.motX = nbttaglist1.e(0);
            this.motY = nbttaglist1.e(1);
            this.motZ = nbttaglist1.e(2);

            /* CraftBukkit start - Moved section down
            if (Math.abs(this.motX) > 10.0D) {
                this.motX = 0.0D;
            }

            if (Math.abs(this.motY) > 10.0D) {
                this.motY = 0.0D;
            }

            if (Math.abs(this.motZ) > 10.0D) {
                this.motZ = 0.0D;
            }
            // CraftBukkit end */

            this.locX = nbttaglist.e(0);
            this.locY = nbttaglist.e(1);
            this.locZ = nbttaglist.e(2);
            this.M = this.locX;
            this.N = this.locY;
            this.O = this.locZ;
            this.lastX = this.locX;
            this.lastY = this.locY;
            this.lastZ = this.locZ;
            this.yaw = nbttaglist2.f(0);
            this.pitch = nbttaglist2.f(1);
            this.lastYaw = this.yaw;
            this.lastPitch = this.pitch;
            this.h(this.yaw);
            this.i(this.yaw);
            this.fallDistance = nbttagcompound.getFloat("FallDistance");
            this.fireTicks = nbttagcompound.getShort("Fire");
            this.setAirTicks(nbttagcompound.getShort("Air"));
            this.onGround = nbttagcompound.getBoolean("OnGround");
            if (nbttagcompound.hasKey("Dimension")) {
                this.dimension = nbttagcompound.getInt("Dimension");
            }

            this.invulnerable = nbttagcompound.getBoolean("Invulnerable");
            this.portalCooldown = nbttagcompound.getInt("PortalCooldown");
            if (nbttagcompound.b("UUID")) {
                this.uniqueID = nbttagcompound.a("UUID");
                this.ar = this.uniqueID.toString();
            }

            this.setPosition(this.locX, this.locY, this.locZ);
            this.setYawPitch(this.yaw, this.pitch);
            if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
                this.setCustomName(nbttagcompound.getString("CustomName"));
            }

            this.setCustomNameVisible(nbttagcompound.getBoolean("CustomNameVisible"));
            this.aF.a(nbttagcompound);
            this.setSilent(nbttagcompound.getBoolean("Silent"));
            this.setNoGravity(nbttagcompound.getBoolean("NoGravity"));
            this.g(nbttagcompound.getBoolean("Glowing"));
            if (nbttagcompound.hasKeyOfType("Tags", 9)) {
                this.aG.clear();
                NBTTagList nbttaglist3 = nbttagcompound.getList("Tags", 8);
                int i = Math.min(nbttaglist3.size(), 1024);

                for (int j = 0; j < i; ++j) {
                    this.aG.add(nbttaglist3.getString(j));
                }
            }

            this.a(nbttagcompound);
            if (this.as()) {
                this.setPosition(this.locX, this.locY, this.locZ);
            }

            // CraftBukkit start
            if (this instanceof EntityLiving) {
                EntityLiving entity = (EntityLiving) this;

                this.ticksLived = nbttagcompound.getInt("Spigot.ticksLived");

                // Reset the persistence for tamed animals
                if (entity instanceof EntityTameableAnimal && !isLevelAtLeast(nbttagcompound, 2) && !nbttagcompound.getBoolean("PersistenceRequired")) {
                    EntityInsentient entityinsentient = (EntityInsentient) entity;
                    entityinsentient.persistent = !entityinsentient.isTypeNotPersistent();
                }
            }
            // CraftBukkit end

            // CraftBukkit start - Exempt Vehicles from notch's sanity check
            if (!(getBukkitEntity() instanceof Vehicle)) {
                if (Math.abs(this.motX) > 10.0D) {
                    this.motX = 0.0D;
                }

                if (Math.abs(this.motY) > 10.0D) {
                    this.motY = 0.0D;
                }

                if (Math.abs(this.motZ) > 10.0D) {
                    this.motZ = 0.0D;
                }
            }
            // CraftBukkit end

            // CraftBukkit start - Reset world
            if (this instanceof EntityPlayer) {
                Server server = Bukkit.getServer();
                org.bukkit.World bworld = null;

                // TODO: Remove World related checks, replaced with WorldUID
                String worldName = nbttagcompound.getString("world");

                if (nbttagcompound.hasKey("WorldUUIDMost") && nbttagcompound.hasKey("WorldUUIDLeast")) {
                    UUID uid = new UUID(nbttagcompound.getLong("WorldUUIDMost"), nbttagcompound.getLong("WorldUUIDLeast"));
                    bworld = server.getWorld(uid);
                } else {
                    bworld = server.getWorld(worldName);
                }

                if (bworld == null) {
                    EntityPlayer entityPlayer = (EntityPlayer) this;
                    bworld = ((org.bukkit.craftbukkit.CraftServer) server).getServer().getWorldServer(entityPlayer.dimension).getWorld();
                }

                spawnIn(bworld == null? null : ((CraftWorld) bworld).getHandle());
            }
            // CraftBukkit end

            // Paper start - Restore the entity's origin location
            NBTTagList originTag = nbttagcompound.getList("Paper.Origin", 6);
            if (!originTag.isEmpty()) {
                origin = new Location(world.getWorld(), originTag.e(0), originTag.e(1), originTag.e(2));
            }
            // Paper end

        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Loading entity NBT");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity being loaded");

            this.appendEntityCrashDetails(crashreportsystemdetails);
            throw new ReportedException(crashreport);
        }
    }

    protected boolean as() {
        return true;
    }

    @Nullable
    protected final String at() {
        MinecraftKey minecraftkey = EntityTypes.a(this);

        return minecraftkey == null ? null : minecraftkey.toString();
    }

    protected abstract void a(NBTTagCompound nbttagcompound);

    protected abstract void b(NBTTagCompound nbttagcompound);

    protected NBTTagList createDoubleList(double... adouble) { return a(adouble); } // Paper - OBFHELPER
    protected NBTTagList a(double... adouble) {
        NBTTagList nbttaglist = new NBTTagList();
        double[] adouble1 = adouble;
        int i = adouble.length;

        for (int j = 0; j < i; ++j) {
            double d0 = adouble1[j];

            nbttaglist.add(new NBTTagDouble(d0));
        }

        return nbttaglist;
    }

    protected NBTTagList createFloatList(float... afloat) { return a(afloat); } // OBFHELPER
    protected NBTTagList a(float... afloat) {
        NBTTagList nbttaglist = new NBTTagList();
        float[] afloat1 = afloat;
        int i = afloat.length;

        for (int j = 0; j < i; ++j) {
            float f = afloat1[j];

            nbttaglist.add(new NBTTagFloat(f));
        }

        return nbttaglist;
    }

    @Nullable
    public EntityItem a(Item item, int i) {
        return this.a(item, i, 0.0F);
    }

    @Nullable
    public EntityItem a(Item item, int i, float f) {
        return this.a(new ItemStack(item, i, 0), f);
    }

    @Nullable
    public EntityItem a(ItemStack itemstack, float f) {
        if (itemstack.isEmpty()) {
            return null;
        } else {
            // CraftBukkit start - Capture drops for death event
            if (this instanceof EntityLiving && !((EntityLiving) this).forceDrops) {
                ((EntityLiving) this).drops.add(org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(itemstack));
                return null;
            }
            // CraftBukkit end
            EntityItem entityitem = new EntityItem(this.world, this.locX, this.locY + f, this.locZ, itemstack);

            entityitem.q();
            this.world.addEntity(entityitem);
            return entityitem;
        }
    }

    public boolean isAlive() {
        return !this.dead;
    }

    public boolean inBlock() {
        if (this.noclip) {
            return false;
        } else {
            BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();

            for (int i = 0; i < 8; ++i) {
                int j = MathHelper.floor(this.locY + ((i >> 0) % 2 - 0.5F) * 0.1F + this.getHeadHeight());
                int k = MathHelper.floor(this.locX + ((i >> 1) % 2 - 0.5F) * this.width * 0.8F);
                int l = MathHelper.floor(this.locZ + ((i >> 2) % 2 - 0.5F) * this.width * 0.8F);

                if (blockposition_pooledblockposition.getX() != k || blockposition_pooledblockposition.getY() != j || blockposition_pooledblockposition.getZ() != l) {
                    blockposition_pooledblockposition.f(k, j, l);
                    if (this.world.getType(blockposition_pooledblockposition).s()) {
                        blockposition_pooledblockposition.t();
                        return true;
                    }
                }
            }

            blockposition_pooledblockposition.t();
            return false;
        }
    }

    public boolean b(EntityHuman entityhuman, EnumHand enumhand) {
        return false;
    }

    @Nullable
    public AxisAlignedBB j(Entity entity) {
        return null;
    }

    public void aw() {
        Entity entity = this.bB();

        if (this.isPassenger() && entity.dead) {
            this.stopRiding();
        } else {
            this.motX = 0.0D;
            this.motY = 0.0D;
            this.motZ = 0.0D;
            this.A_();
            if (this.isPassenger()) {
                entity.k(this);
            }
        }
    }

    public void k(Entity entity) {
        if (this.w(entity)) {
            entity.setPosition(this.locX, this.locY + this.ay() + entity.ax(), this.locZ);
        }
    }

    public double ax() {
        return 0.0D;
    }

    public double ay() {
        return this.length * 0.75D;
    }

    public boolean startRiding(Entity entity) {
        return this.a(entity, false);
    }

    public boolean a(Entity entity, boolean flag) {
        for (Entity entity1 = entity; entity1.au != null; entity1 = entity1.au) {
            if (entity1.au == this) {
                return false;
            }
        }

        if (!flag && (!this.n(entity) || !entity.q(this))) {
            return false;
        } else {
            if (this.isPassenger()) {
                this.stopRiding();
            }

            this.au = entity;
            this.au.o(this);
            return true;
        }
    }

    protected boolean n(Entity entity) {
        return this.j <= 0;
    }

    public void az() {
        for (int i = this.passengers.size() - 1; i >= 0; --i) {
            this.passengers.get(i).stopRiding();
        }

    }

    public void stopRiding() {
        if (this.au != null) {
            Entity entity = this.au;

            this.au = null;
            entity.p(this);
        }

    }

    protected void o(Entity entity) {
        if (entity == this) throw new IllegalArgumentException("Entities cannot become a passenger of themselves"); // Paper - issue 572
        if (entity.bB() != this) {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        } else {
            // CraftBukkit start
            com.google.common.base.Preconditions.checkState(!entity.passengers.contains(this), "Circular entity riding! %s %s", this, entity);

            CraftEntity craft = (CraftEntity) entity.getBukkitEntity().getVehicle();
            Entity orig = craft == null ? null : craft.getHandle();
            if (getBukkitEntity() instanceof Vehicle && entity.getBukkitEntity() instanceof LivingEntity && entity.world.isChunkLoaded((int) entity.locX >> 4, (int) entity.locZ >> 4, false)) { // Boolean not used
                VehicleEnterEvent event = new VehicleEnterEvent(
                        (Vehicle) getBukkitEntity(),
                         entity.getBukkitEntity()
                );
                Bukkit.getPluginManager().callEvent(event);
                CraftEntity craftn = (CraftEntity) entity.getBukkitEntity().getVehicle();
                Entity n = craftn == null ? null : craftn.getHandle();
                if (event.isCancelled() || n != orig) {
                    return;
                }
            }
            // CraftBukkit end
            // Spigot start
            org.spigotmc.event.entity.EntityMountEvent event = new org.spigotmc.event.entity.EntityMountEvent(entity.getBukkitEntity(), this.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            // Spigot end
            if (entity instanceof EntityHuman && !(this.bw() instanceof EntityHuman)) {
                this.passengers.add(0, entity);
            } else {
                this.passengers.add(entity);
            }

        }
    }

    protected void p(Entity entity) {
        if (entity.bB() == this) {
            throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
        } else {
            // CraftBukkit start
            entity.setVehicle(this); // Paper - Set the vehicle back for the event
            CraftEntity craft = (CraftEntity) entity.getBukkitEntity().getVehicle();
            Entity orig = craft == null ? null : craft.getHandle();
            if (getBukkitEntity() instanceof Vehicle && entity.getBukkitEntity() instanceof LivingEntity) {
                VehicleExitEvent event = new VehicleExitEvent(
                        (Vehicle) getBukkitEntity(),
                        (LivingEntity) entity.getBukkitEntity()
                );
                Bukkit.getPluginManager().callEvent(event);
                CraftEntity craftn = (CraftEntity) entity.getBukkitEntity().getVehicle();
                Entity n = craftn == null ? null : craftn.getHandle();
                if (event.isCancelled() || n != orig) {
                    return;
                }
            }
            // CraftBukkit end
            // Paper start - make EntityDismountEvent cancellable
            if (!new org.spigotmc.event.entity.EntityDismountEvent(entity.getBukkitEntity(), this.getBukkitEntity()).callEvent()) {
                return;
            }
            entity.setVehicle(null);
            // Paper end

            this.passengers.remove(entity);
            entity.j = 60;
        }
    }

    protected boolean q(Entity entity) {
        return this.bx().size() < 1;
    }

    public float aA() {
        return 0.0F;
    }

    public Vec3D aB() {
        return null;
    }

    public void e(BlockPosition blockposition) {
        if (this.portalCooldown > 0) {
            this.portalCooldown = this.aE();
        } else {
            if (!blockposition.equals(this.an)) {
                this.an = new BlockPosition(blockposition);
                ShapeDetector.ShapeDetectorCollection shapedetector_shapedetectorcollection = Blocks.PORTAL.c(this.world, this.an);
                double d0 = shapedetector_shapedetectorcollection.getFacing().k() == EnumDirection.EnumAxis.X ? (double) shapedetector_shapedetectorcollection.a().getZ() : (double) shapedetector_shapedetectorcollection.a().getX();
                double d1 = shapedetector_shapedetectorcollection.getFacing().k() == EnumDirection.EnumAxis.X ? this.locZ : this.locX;

                d1 = Math.abs(MathHelper.c(d1 - (shapedetector_shapedetectorcollection.getFacing().e().c() == EnumDirection.EnumAxisDirection.NEGATIVE ? 1 : 0), d0, d0 - shapedetector_shapedetectorcollection.d()));
                double d2 = MathHelper.c(this.locY - 1.0D, shapedetector_shapedetectorcollection.a().getY(), shapedetector_shapedetectorcollection.a().getY() - shapedetector_shapedetectorcollection.e());

                this.ao = new Vec3D(d1, d2, 0.0D);
                this.ap = shapedetector_shapedetectorcollection.getFacing();
            }

            this.ak = true;
        }
    }

    public int aE() {
        return 300;
    }

    public Iterable<ItemStack> aG() {
        return Entity.b;
    }

    public Iterable<ItemStack> getArmorItems() {
        return Entity.b;
    }

    public Iterable<ItemStack> aI() {
        return Iterables.concat(this.aG(), this.getArmorItems());
    }

    public void setEquipment(EnumItemSlot enumitemslot, ItemStack itemstack) {}

    public boolean isBurning() {
        return !this.fireProof && (this.fireTicks > 0);
    }

    public boolean isPassenger() {
        return this.bB() != null;
    }

    public boolean isVehicle() {
        return !this.bx().isEmpty();
    }

    public boolean isSneaking() {
        return this.getFlag(1);
    }

    public void setSneaking(boolean flag) {
        this.setFlag(1, flag);
    }

    public boolean isSprinting() {
        return this.getFlag(3);
    }

    public void setSprinting(boolean flag) {
        this.setFlag(3, flag);
    }

    public boolean aO() {
        return this.glowing;
    }

    public void g(boolean flag) {
        this.glowing = flag;
        this.setFlag(6, this.glowing);

    }

    public boolean isInvisible() {
        return this.getFlag(5);
    }

    @Nullable public ScoreboardTeamBase getTeam() { return this.aQ(); } // Paper - OBFHELPER
    @Nullable
    public ScoreboardTeamBase aQ() {
        if (!this.world.paperConfig.nonPlayerEntitiesOnScoreboards && !(this instanceof EntityHuman)) { return null; } // Paper
        return this.world.getScoreboard().getPlayerTeam(this.bf());
    }

    public boolean r(Entity entity) {
        return this.a(entity.aQ());
    }

    public boolean a(ScoreboardTeamBase scoreboardteambase) {
        return this.aQ() != null ? this.aQ().isAlly(scoreboardteambase) : false;
    }

    public void setInvisible(boolean flag) {
        this.setFlag(5, flag);
    }

    public boolean getFlag(int i) {
        return (this.datawatcher.get(Entity.Z).byteValue() & 1 << i) != 0;
    }

    public void setFlag(int i, boolean flag) {
        byte b0 = this.datawatcher.get(Entity.Z).byteValue();

        if (flag) {
            this.datawatcher.set(Entity.Z, Byte.valueOf((byte) (b0 | 1 << i)));
        } else {
            this.datawatcher.set(Entity.Z, Byte.valueOf((byte) (b0 & ~(1 << i))));
        }

    }

    public int getAirTicks() {
        return this.datawatcher.get(Entity.az).intValue();
    }

    public void setAirTicks(int i) {
        // CraftBukkit start
        EntityAirChangeEvent event = new EntityAirChangeEvent(this.getBukkitEntity(), i);
        event.getEntity().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        this.datawatcher.set(Entity.az, Integer.valueOf(event.getAmount()));
        // CraftBukkit end
    }

    public void onLightningStrike(EntityLightning entitylightning) {
        // CraftBukkit start
        final org.bukkit.entity.Entity thisBukkitEntity = this.getBukkitEntity();
        final org.bukkit.entity.Entity stormBukkitEntity = entitylightning.getBukkitEntity();
        final PluginManager pluginManager = Bukkit.getPluginManager();

        if (thisBukkitEntity instanceof Hanging) {
            HangingBreakByEntityEvent hangingEvent = new HangingBreakByEntityEvent((Hanging) thisBukkitEntity, stormBukkitEntity);
            pluginManager.callEvent(hangingEvent);

            if (hangingEvent.isCancelled()) {
                return;
            }
        }

        if (this.fireProof) {
            return;
        }
        CraftEventFactory.entityDamage = entitylightning;
        if (!this.damageEntity(DamageSource.LIGHTNING, 5.0F)) {
            CraftEventFactory.entityDamage = null;
            return;
        }
        // CraftBukkit end
        ++this.fireTicks;
        if (this.fireTicks == 0) {
            // CraftBukkit start - Call a combust event when lightning strikes
            EntityCombustByEntityEvent entityCombustEvent = new EntityCombustByEntityEvent(stormBukkitEntity, thisBukkitEntity, 8);
            pluginManager.callEvent(entityCombustEvent);
            if (!entityCombustEvent.isCancelled()) {
                this.setOnFire(entityCombustEvent.getDuration());
            }
            // CraftBukkit end
        }

    }

    public void b(EntityLiving entityliving) {}

    protected boolean i(double d0, double d1, double d2) {
        BlockPosition blockposition = new BlockPosition(d0, d1, d2);
        double d3 = d0 - blockposition.getX();
        double d4 = d1 - blockposition.getY();
        double d5 = d2 - blockposition.getZ();

        if (!this.world.a(this.getBoundingBox())) {
            return false;
        } else {
            EnumDirection enumdirection = EnumDirection.UP;
            double d6 = Double.MAX_VALUE;

            if (!this.world.t(blockposition.west()) && d3 < d6) {
                d6 = d3;
                enumdirection = EnumDirection.WEST;
            }

            if (!this.world.t(blockposition.east()) && 1.0D - d3 < d6) {
                d6 = 1.0D - d3;
                enumdirection = EnumDirection.EAST;
            }

            if (!this.world.t(blockposition.north()) && d5 < d6) {
                d6 = d5;
                enumdirection = EnumDirection.NORTH;
            }

            if (!this.world.t(blockposition.south()) && 1.0D - d5 < d6) {
                d6 = 1.0D - d5;
                enumdirection = EnumDirection.SOUTH;
            }

            if (!this.world.t(blockposition.up()) && 1.0D - d4 < d6) {
                d6 = 1.0D - d4;
                enumdirection = EnumDirection.UP;
            }

            float f = this.random.nextFloat() * 0.2F + 0.1F;
            float f1 = enumdirection.c().a();

            if (enumdirection.k() == EnumDirection.EnumAxis.X) {
                this.motX = f1 * f;
                this.motY *= 0.75D;
                this.motZ *= 0.75D;
            } else if (enumdirection.k() == EnumDirection.EnumAxis.Y) {
                this.motX *= 0.75D;
                this.motY = f1 * f;
                this.motZ *= 0.75D;
            } else if (enumdirection.k() == EnumDirection.EnumAxis.Z) {
                this.motX *= 0.75D;
                this.motY *= 0.75D;
                this.motZ = f1 * f;
            }

            return true;
        }
    }

    public void aS() {
        this.E = true;
        this.fallDistance = 0.0F;
    }

    @Override
	public String getName() {
        if (this.hasCustomName()) {
            return this.getCustomName();
        } else {
            String s = EntityTypes.b(this);

            if (s == null) {
                s = "generic";
            }

            return LocaleI18n.get("entity." + s + ".name");
        }
    }

    @Nullable
    public Entity[] aT() {
        return null;
    }

    public boolean s(Entity entity) {
        return this == entity;
    }

    public float getHeadRotation() {
        return 0.0F;
    }

    public void h(float f) {}

    public void i(float f) {}

    public boolean aV() {
        return true;
    }

    public boolean t(Entity entity) {
        return false;
    }

    @Override
	public String toString() {
        return String.format("%s[\'%s\'/%d, l=\'%s\', x=%.2f, y=%.2f, z=%.2f]", new Object[] { this.getClass().getSimpleName(), this.getName(), Integer.valueOf(this.id), this.world == null ? "~NULL~" : this.world.getWorldData().getName(), Double.valueOf(this.locX), Double.valueOf(this.locY), Double.valueOf(this.locZ)});
    }

    public boolean isInvulnerable(DamageSource damagesource) {
        return this.invulnerable && damagesource != DamageSource.OUT_OF_WORLD && !damagesource.u();
    }

    public boolean aW() {
        return this.invulnerable;
    }

    public void setInvulnerable(boolean flag) {
        this.invulnerable = flag;
    }

    public void u(Entity entity) {
        this.setPositionRotation(entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
    }

    private void a(Entity entity) {
        NBTTagCompound nbttagcompound = entity.e(new NBTTagCompound());

        nbttagcompound.remove("Dimension");
        this.f(nbttagcompound);
        this.portalCooldown = entity.portalCooldown;
        this.an = entity.an;
        this.ao = entity.ao;
        this.ap = entity.ap;
    }

    @Nullable
    public Entity c(int i) {
        if (!this.dead) {
            this.world.methodProfiler.a("changeDimension");
            MinecraftServer minecraftserver = this.B_();
            // CraftBukkit start - Move logic into new function "teleportTo(Location,boolean)"
            // int j = this.dimension;
            // WorldServer worldserver = minecraftserver.getWorldServer(j);
            // WorldServer worldserver1 = minecraftserver.getWorldServer(i);
            WorldServer exitWorld = null;
            if (this.dimension < CraftWorld.CUSTOM_DIMENSION_OFFSET) { // Plugins must specify exit from custom Bukkit worlds
                // Only target existing worlds (compensate for allow-nether/allow-end as false)
                for (WorldServer world : minecraftserver.worlds) {
                    if (world.dimension == i) {
                        exitWorld = world;
                    }
                }
            }

            BlockPosition blockposition = null; // PAIL: CHECK
            Location enter = this.getBukkitEntity().getLocation();
            Location exit;
            if (exitWorld != null) {
                if (blockposition != null) {
                    exit = new Location(exitWorld.getWorld(), blockposition.getX(), blockposition.getY(), blockposition.getZ());
                } else {
                    exit = minecraftserver.getPlayerList().calculateTarget(enter, minecraftserver.getWorldServer(i));
                }
            }
            else {
                exit = null;
            }
            boolean useTravelAgent = exitWorld != null && !(this.dimension == 1 && exitWorld.dimension == 1); // don't use agent for custom worlds or return from THE_END

            TravelAgent agent = exit != null ? (TravelAgent) ((CraftWorld) exit.getWorld()).getHandle().getTravelAgent() : org.bukkit.craftbukkit.CraftTravelAgent.DEFAULT; // return arbitrary TA to compensate for implementation dependent plugins
            EntityPortalEvent event = new EntityPortalEvent(this.getBukkitEntity(), enter, exit, agent);
            event.useTravelAgent(useTravelAgent);
            event.getEntity().getServer().getPluginManager().callEvent(event);
            if (event.isCancelled() || event.getTo() == null || event.getTo().getWorld() == null || !this.isAlive()) {
                return null;
            }
            exit = event.useTravelAgent() ? event.getPortalTravelAgent().findOrCreate(event.getTo()) : event.getTo();
            // Need to make sure the profiler state is reset afterwards (but we still want to time the call)
            Entity entity = this.teleportTo(exit, true);
            
            return entity;
        }
        return null;
    }

    public Entity teleportTo(Location exit, boolean portal) {
        if (!this.dead) { // Paper
            WorldServer worldserver = ((CraftWorld) getBukkitEntity().getLocation().getWorld()).getHandle();
            WorldServer worldserver1 = ((CraftWorld) exit.getWorld()).getHandle();
            int i = worldserver1.dimension;
            // CraftBukkit end

            this.dimension = i;
            /* CraftBukkit start - TODO: Check if we need this
            if (j == 1 && i == 1) {
                worldserver1 = minecraftserver.getWorldServer(0);
                this.dimension = 0;
            }
            // CraftBukkit end */

            this.world.kill(this);
            this.dead = false;
            this.world.methodProfiler.a("reposition");
            /* CraftBukkit start - Handled in calculateTarget
            BlockPosition blockposition;

            if (i == 1) {
                blockposition = worldserver1.getDimensionSpawn();
            } else {
                double d0 = this.locX;
                double d1 = this.locZ;
                double d2 = 8.0D;

                if (i == -1) {
                    d0 = MathHelper.a(d0 / 8.0D, worldserver1.getWorldBorder().b() + 16.0D, worldserver1.getWorldBorder().d() - 16.0D);
                    d1 = MathHelper.a(d1 / 8.0D, worldserver1.getWorldBorder().c() + 16.0D, worldserver1.getWorldBorder().e() - 16.0D);
                } else if (i == 0) {
                    d0 = MathHelper.a(d0 * 8.0D, worldserver1.getWorldBorder().b() + 16.0D, worldserver1.getWorldBorder().d() - 16.0D);
                    d1 = MathHelper.a(d1 * 8.0D, worldserver1.getWorldBorder().c() + 16.0D, worldserver1.getWorldBorder().e() - 16.0D);
                }

                d0 = (double) MathHelper.clamp((int) d0, -29999872, 29999872);
                d1 = (double) MathHelper.clamp((int) d1, -29999872, 29999872);
                float f = this.yaw;

                this.setPositionRotation(d0, this.locY, d1, 90.0F, 0.0F);
                PortalTravelAgent portaltravelagent = worldserver1.getTravelAgent();

                portaltravelagent.b(this, f);
                blockposition = new BlockPosition(this);
            }

            // CraftBukkit end */
            // CraftBukkit start - Ensure chunks are loaded in case TravelAgent is not used which would initially cause chunks to load during find/create
            // minecraftserver.getPlayerList().changeWorld(this, j, worldserver, worldserver1);
            worldserver1.getMinecraftServer().getPlayerList().repositionEntity(this, exit, portal);
            // worldserver.entityJoinedWorld(this, false); // Handled in repositionEntity
            // CraftBukkit end
            this.world.methodProfiler.c("reloading");
            Entity entity = EntityTypes.a(this.getClass(), worldserver1);

            if (entity != null) {
                entity.a(this);
                /* CraftBukkit start - We need to do this...
                if (j == 1 && i == 1) {
                    BlockPosition blockposition1 = worldserver1.q(worldserver1.getSpawn());

                    entity.setPositionRotation(blockposition1, entity.yaw, entity.pitch);
                } else {
                    entity.setPositionRotation(blockposition, entity.yaw, entity.pitch);
                }
                // CraftBukkit end */

                boolean flag = entity.attachedToPlayer;

                entity.attachedToPlayer = true;
                worldserver1.addEntity(entity);
                entity.attachedToPlayer = flag;
                worldserver1.entityJoinedWorld(entity, false);
                // CraftBukkit start - Forward the CraftEntity to the new entity
                this.getBukkitEntity().setHandle(entity);
                entity.bukkitEntity = this.getBukkitEntity();

                if (this instanceof EntityInsentient) {
                    ((EntityInsentient)this).unleash(true, false); // Unleash to prevent duping of leads.
                }
                // CraftBukkit end
            }

            this.dead = true;
            
            worldserver.m();
            worldserver1.m();
            //  // CraftBukkit: Moved up to keep balanced
            return entity;
        } else {
            return null;
        }
    }

    public boolean aX() {
        return true;
    }

    public float a(Explosion explosion, World world, BlockPosition blockposition, IBlockData iblockdata) {
        return iblockdata.getBlock().a(this);
    }

    public boolean a(Explosion explosion, World world, BlockPosition blockposition, IBlockData iblockdata, float f) {
        return true;
    }

    public int aY() {
        return 3;
    }

    public Vec3D getPortalOffset() {
        return this.ao;
    }

    public EnumDirection getPortalDirection() {
        return this.ap;
    }

    public boolean isIgnoreBlockTrigger() {
        return false;
    }

    public void appendEntityCrashDetails(CrashReportSystemDetails crashreportsystemdetails) {
        crashreportsystemdetails.a("Entity Type", new CrashReportCallable() {
            public String a() throws Exception {
                return EntityTypes.a(Entity.this) + " (" + Entity.this.getClass().getCanonicalName() + ")";
            }

            @Override
			public Object call() throws Exception {
                return this.a();
            }
        });
        crashreportsystemdetails.a("Entity ID", Integer.valueOf(this.id));
        crashreportsystemdetails.a("Entity Name", new CrashReportCallable() {
            public String a() throws Exception {
                return Entity.this.getName();
            }

            @Override
			public Object call() throws Exception {
                return this.a();
            }
        });
        crashreportsystemdetails.a("Entity\'s Exact location", String.format("%.2f, %.2f, %.2f", new Object[] { Double.valueOf(this.locX), Double.valueOf(this.locY), Double.valueOf(this.locZ)}));
        crashreportsystemdetails.a("Entity\'s Block location", CrashReportSystemDetails.a(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ)));
        crashreportsystemdetails.a("Entity\'s Momentum", String.format("%.2f, %.2f, %.2f", new Object[] { Double.valueOf(this.motX), Double.valueOf(this.motY), Double.valueOf(this.motZ)}));
        crashreportsystemdetails.a("Entity\'s Passengers", new CrashReportCallable() {
            public String a() throws Exception {
                return Entity.this.bx().toString();
            }

            @Override
			public Object call() throws Exception {
                return this.a();
            }
        });
        crashreportsystemdetails.a("Entity\'s Vehicle", new CrashReportCallable() {
            public String a() throws Exception {
                return Entity.this.bB().toString();
            }

            @Override
			public Object call() throws Exception {
                return this.a();
            }
        });
    }

    public void a(UUID uuid) {
        this.uniqueID = uuid;
        this.ar = this.uniqueID.toString();
    }

    public UUID getUniqueID() {
        return this.uniqueID;
    }

    public String getUUIDString() { return this.bf(); } // OBFHELPER
    public String bf() {
        return this.ar;
    }

    public boolean bg() {
        // Paper start - OBFHELPER
        return this.pushedByWater();
    }

    public boolean pushedByWater() {
        // Paper end
        return true;
    }

    @Override
	public IChatBaseComponent getScoreboardDisplayName() {
        ChatComponentText chatcomponenttext = new ChatComponentText(ScoreboardTeam.getPlayerDisplayName(this.aQ(), this.getName()));

        chatcomponenttext.getChatModifier().setChatHoverable(this.bn());
        chatcomponenttext.getChatModifier().setInsertion(this.bf());
        return chatcomponenttext;
    }

    public void setCustomName(String s) {
        // CraftBukkit start - Add a sane limit for name length
        if (s.length() > 256) {
            s = s.substring(0, 256);
        }
        // CraftBukkit end
        this.datawatcher.set(Entity.aA, s);
    }

    public String getCustomName() {
        return this.datawatcher.get(Entity.aA);
    }

    public boolean hasCustomName() {
        return !this.datawatcher.get(Entity.aA).isEmpty();
    }

    public void setCustomNameVisible(boolean flag) {
        this.datawatcher.set(Entity.aB, Boolean.valueOf(flag));
    }

    public boolean getCustomNameVisible() {
        return this.datawatcher.get(Entity.aB).booleanValue();
    }

    public void enderTeleportTo(double d0, double d1, double d2) {
        this.aH = true;
        this.setPositionRotation(d0, d1, d2, this.yaw, this.pitch);
        this.world.entityJoinedWorld(this, false);
    }

    public void a(DataWatcherObject<?> datawatcherobject) {}

    public EnumDirection getDirection() {
        return EnumDirection.fromType2(MathHelper.floor(this.yaw * 4.0F / 360.0F + 0.5D) & 3);
    }

    public EnumDirection bm() {
        return this.getDirection();
    }

    protected ChatHoverable bn() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        MinecraftKey minecraftkey = EntityTypes.a(this);

        nbttagcompound.setString("id", this.bf());
        if (minecraftkey != null) {
            nbttagcompound.setString("type", minecraftkey.toString());
        }

        nbttagcompound.setString("name", this.getName());
        return new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_ENTITY, new ChatComponentText(nbttagcompound.toString()));
    }

    public boolean a(EntityPlayer entityplayer) {
        return true;
    }

    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    public void a(AxisAlignedBB axisalignedbb) {
        // CraftBukkit start - block invalid bounding boxes
        double a = axisalignedbb.a,
                b = axisalignedbb.b,
                c = axisalignedbb.c,
                d = axisalignedbb.d,
                e = axisalignedbb.e,
                f = axisalignedbb.f;
        double len = axisalignedbb.d - axisalignedbb.a;
        if (len < 0) d = a;
        if (len > 64) d = a + 64.0;

        len = axisalignedbb.e - axisalignedbb.b;
        if (len < 0) e = b;
        if (len > 64) e = b + 64.0;

        len = axisalignedbb.f - axisalignedbb.c;
        if (len < 0) f = c;
        if (len > 64) f = c + 64.0;
        this.boundingBox = new AxisAlignedBB(a, b, c, d, e, f);
        // CraftBukkit end
    }

    public float getHeadHeight() {
        return this.length * 0.85F;
    }

    public boolean br() {
        return this.aw;
    }

    public void k(boolean flag) {
        this.aw = flag;
    }

    public boolean c(int i, ItemStack itemstack) {
        return false;
    }

    @Override
	public void sendMessage(IChatBaseComponent ichatbasecomponent) {}

    @Override
	public boolean a(int i, String s) {
        return true;
    }

    @Override
	public BlockPosition getChunkCoordinates() {
        return new BlockPosition(this.locX, this.locY + 0.5D, this.locZ);
    }

    @Override
	public Vec3D d() {
        return new Vec3D(this.locX, this.locY, this.locZ);
    }

    @Override
	public World getWorld() {
        return this.world;
    }

    @Override
	public Entity f() {
        return this;
    }

    @Override
	public boolean getSendCommandFeedback() {
        return false;
    }

    @Override
	public void a(CommandObjectiveExecutor.EnumCommandResult commandobjectiveexecutor_enumcommandresult, int i) {
        if (this.world != null) {
            this.aF.a(this.world.getMinecraftServer(), this, commandobjectiveexecutor_enumcommandresult, i);
        }

    }

    @Override
	@Nullable
    public MinecraftServer B_() {
        return this.world.getMinecraftServer();
    }

    public CommandObjectiveExecutor bs() {
        return this.aF;
    }

    public void v(Entity entity) {
        this.aF.a(entity.bs());
    }

    public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, EnumHand enumhand) {
        return EnumInteractionResult.PASS;
    }

    public boolean bt() {
        return false;
    }

    protected void a(EntityLiving entityliving, Entity entity) {
        if (entity instanceof EntityLiving) {
            EnchantmentManager.a((EntityLiving) entity, (Entity) entityliving);
        }

        EnchantmentManager.b(entityliving, entity);
    }

    public void b(EntityPlayer entityplayer) {}

    public void c(EntityPlayer entityplayer) {}

    public float a(EnumBlockRotation enumblockrotation) {
        float f = MathHelper.g(this.yaw);

        switch (enumblockrotation) {
        case CLOCKWISE_180:
            return f + 180.0F;

        case COUNTERCLOCKWISE_90:
            return f + 270.0F;

        case CLOCKWISE_90:
            return f + 90.0F;

        default:
            return f;
        }
    }

    public float a(EnumBlockMirror enumblockmirror) {
        float f = MathHelper.g(this.yaw);

        switch (enumblockmirror) {
        case LEFT_RIGHT:
            return -f;

        case FRONT_BACK:
            return 180.0F - f;

        default:
            return f;
        }
    }

    public boolean bu() {
        return false;
    }

    public boolean bv() {
        boolean flag = this.aH;

        this.aH = false;
        return flag;
    }

    @Nullable
    public Entity bw() {
        return null;
    }

    public List<Entity> bx() {
        return (List) (this.passengers.isEmpty() ? Collections.emptyList() : Lists.newArrayList(this.passengers));
    }

    public boolean w(Entity entity) {
        Iterator iterator = this.bx().iterator();

        Entity entity1;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            entity1 = (Entity) iterator.next();
        } while (!entity1.equals(entity));

        return true;
    }

    public Collection<Entity> by() {
        HashSet hashset = Sets.newHashSet();

        this.a(Entity.class, (Set) hashset);
        return hashset;
    }

    public <T extends Entity> Collection<T> b(Class<T> oclass) {
        HashSet hashset = Sets.newHashSet();

        this.a(oclass, (Set) hashset);
        return hashset;
    }

    private <T extends Entity> void a(Class<T> oclass, Set<T> set) {
        Entity entity;

        for (Iterator iterator = this.bx().iterator(); iterator.hasNext(); entity.a(oclass, set)) {
            entity = (Entity) iterator.next();
            if (oclass.isAssignableFrom(entity.getClass())) {
                set.add((T) entity); // CraftBukkit - decompile error
            }
        }

    }

    public Entity getVehicle() {
        Entity entity;

        for (entity = this; entity.isPassenger(); entity = entity.bB()) {
            ;
        }

        return entity;
    }

    public boolean x(Entity entity) {
        return this.getVehicle() == entity.getVehicle();
    }

    public boolean y(Entity entity) {
        Iterator iterator = this.bx().iterator();

        Entity entity1;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            entity1 = (Entity) iterator.next();
            if (entity1.equals(entity)) {
                return true;
            }
        } while (!entity1.y(entity));

        return true;
    }

    public boolean bA() {
        Entity entity = this.bw();

        return entity instanceof EntityHuman ? ((EntityHuman) entity).cR() : true;
    }

    @Nullable
    public Entity bB() {
        return this.au;
    }

    public EnumPistonReaction o_() {
        return EnumPistonReaction.NORMAL;
    }

    public SoundCategory bC() {
        return SoundCategory.NEUTRAL;
    }

    public int getMaxFireTicks() {
        return 1;
    }
}
