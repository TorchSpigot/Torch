package net.minecraft.server;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
// CraftBukkit end

public class EntityWither extends EntityMonster implements IRangedEntity {

    private static final DataWatcherObject<Integer> a = DataWatcher.a(EntityWither.class, DataWatcherRegistry.b);
    private static final DataWatcherObject<Integer> b = DataWatcher.a(EntityWither.class, DataWatcherRegistry.b);
    private static final DataWatcherObject<Integer> c = DataWatcher.a(EntityWither.class, DataWatcherRegistry.b);
    private static final DataWatcherObject<Integer>[] bw = new DataWatcherObject[] { EntityWither.a, EntityWither.b, EntityWither.c};
    private static final DataWatcherObject<Integer> bx = DataWatcher.a(EntityWither.class, DataWatcherRegistry.b);
    private float[] by = new float[2];
    private float[] bz = new float[2];
    private float[] bA = new float[2];
    private float[] bB = new float[2];
    private int[] bC = new int[2];
    private int[] bD = new int[2];
    private int bE;
    private final BossBattleServer bF;
    private static final Predicate<Entity> bG = new Predicate() {
        public boolean a(@Nullable Entity entity) {
            return entity instanceof EntityLiving && ((EntityLiving) entity).getMonsterType() != EnumMonsterType.UNDEAD;
        }

        public boolean apply(Object object) {
            return this.a((Entity) object);
        }
    };

    public EntityWither(World world) {
        super(world);
        this.bF = (BossBattleServer) (new BossBattleServer(this.getScoreboardDisplayName(), BossBattle.BarColor.PURPLE, BossBattle.BarStyle.PROGRESS)).setDarkenSky(true);
        this.setHealth(this.getMaxHealth());
        this.setSize(0.9F, 3.5F);
        this.fireProof = true;
        ((Navigation) this.getNavigation()).c(true);
        this.b_ = 50;
    }

    protected void r() {
        this.goalSelector.a(0, new EntityWither.a());
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalArrowAttack(this, 1.0D, 40, 20.0F));
        this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityInsentient.class, 0, false, false, EntityWither.bG));
    }

    protected void i() {
        super.i();
        this.datawatcher.register(EntityWither.a, Integer.valueOf(0));
        this.datawatcher.register(EntityWither.b, Integer.valueOf(0));
        this.datawatcher.register(EntityWither.c, Integer.valueOf(0));
        this.datawatcher.register(EntityWither.bx, Integer.valueOf(0));
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setInt("Invul", this.da());
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.l(nbttagcompound.getInt("Invul"));
    }

    protected SoundEffect G() {
        return SoundEffects.gF;
    }

    protected SoundEffect bS() {
        return SoundEffects.gI;
    }

    protected SoundEffect bT() {
        return SoundEffects.gH;
    }

    public void n() {
        this.motY *= 0.6000000238418579D;
        double d0;
        double d1;
        double d2;

        if (!this.world.isClientSide && this.m(0) > 0) {
            Entity entity = this.world.getEntity(this.m(0));

            if (entity != null) {
                if (this.locY < entity.locY || !this.db() && this.locY < entity.locY + 5.0D) {
                    if (this.motY < 0.0D) {
                        this.motY = 0.0D;
                    }

                    this.motY += (0.5D - this.motY) * 0.6000000238418579D;
                }

                double d3 = entity.locX - this.locX;

                d0 = entity.locZ - this.locZ;
                d1 = d3 * d3 + d0 * d0;
                if (d1 > 9.0D) {
                    d2 = (double) MathHelper.sqrt(d1);
                    this.motX += (d3 / d2 * 0.5D - this.motX) * 0.6000000238418579D;
                    this.motZ += (d0 / d2 * 0.5D - this.motZ) * 0.6000000238418579D;
                }
            }
        }

        if (this.motX * this.motX + this.motZ * this.motZ > 0.05000000074505806D) {
            this.yaw = (float) MathHelper.b(this.motZ, this.motX) * 57.295776F - 90.0F;
        }

        super.n();

        int i;

        for (i = 0; i < 2; ++i) {
            this.bB[i] = this.bz[i];
            this.bA[i] = this.by[i];
        }

        int j;

        for (i = 0; i < 2; ++i) {
            j = this.m(i + 1);
            Entity entity1 = null;

            if (j > 0) {
                entity1 = this.world.getEntity(j);
            }

            if (entity1 != null) {
                d0 = this.n(i + 1);
                d1 = this.o(i + 1);
                d2 = this.p(i + 1);
                double d4 = entity1.locX - d0;
                double d5 = entity1.locY + (double) entity1.getHeadHeight() - d1;
                double d6 = entity1.locZ - d2;
                double d7 = (double) MathHelper.sqrt(d4 * d4 + d6 * d6);
                float f = (float) (MathHelper.b(d6, d4) * 57.2957763671875D) - 90.0F;
                float f1 = (float) (-(MathHelper.b(d5, d7) * 57.2957763671875D));

                this.by[i] = this.b(this.by[i], f1, 40.0F);
                this.bz[i] = this.b(this.bz[i], f, 10.0F);
            } else {
                this.bz[i] = this.b(this.bz[i], this.aN, 10.0F);
            }
        }

        boolean flag = this.db();

        for (j = 0; j < 3; ++j) {
            double d8 = this.n(j);
            double d9 = this.o(j);
            double d10 = this.p(j);

            this.world.addParticle(EnumParticle.SMOKE_NORMAL, d8 + this.random.nextGaussian() * 0.30000001192092896D, d9 + this.random.nextGaussian() * 0.30000001192092896D, d10 + this.random.nextGaussian() * 0.30000001192092896D, 0.0D, 0.0D, 0.0D, new int[0]);
            if (flag && this.world.random.nextInt(4) == 0) {
                this.world.addParticle(EnumParticle.SPELL_MOB, d8 + this.random.nextGaussian() * 0.30000001192092896D, d9 + this.random.nextGaussian() * 0.30000001192092896D, d10 + this.random.nextGaussian() * 0.30000001192092896D, 0.699999988079071D, 0.699999988079071D, 0.5D, new int[0]);
            }
        }

        if (this.da() > 0) {
            for (j = 0; j < 3; ++j) {
                this.world.addParticle(EnumParticle.SPELL_MOB, this.locX + this.random.nextGaussian(), this.locY + (double) (this.random.nextFloat() * 3.3F), this.locZ + this.random.nextGaussian(), 0.699999988079071D, 0.699999988079071D, 0.8999999761581421D, new int[0]);
            }
        }

    }

    protected void M() {
        int i;

        if (this.da() > 0) {
            i = this.da() - 1;
            if (i <= 0) {
                // CraftBukkit start
                // this.world.createExplosion(this, this.locX, this.locY + (double) this.getHeadHeight(), this.locZ, 7.0F, false, this.world.getGameRules().getBoolean("mobGriefing"));
                ExplosionPrimeEvent event = new ExplosionPrimeEvent(this.getBukkitEntity(), 7.0F, false);
                this.world.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    this.world.createExplosion(this, this.locX, this.locY + (double) this.getHeadHeight(), this.locZ, event.getRadius(), event.getFire(), this.world.getGameRules().getBoolean("mobGriefing"));
                }
                // CraftBukkit end

                // CraftBukkit start - Use relative location for far away sounds
                // this.world.a(1023, new BlockPosition(this), 0);
                // Paper start
                //int viewDistance = ((WorldServer) this.world).spigotConfig.viewDistance * 16; // Paper - updated to use worlds actual view distance incase we have to uncomment this due to removal of player view distance API
                for (EntityHuman human : world.players) {
                    EntityPlayer player = (EntityPlayer) human;
                    int viewDistance = player.getViewDistance();
                    // Paper end
                    double deltaX = this.locX - player.locX;
                    double deltaZ = this.locZ - player.locZ;
                    double distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
                    if ( world.spigotConfig.witherSpawnSoundRadius > 0 && distanceSquared > world.spigotConfig.witherSpawnSoundRadius * world.spigotConfig.witherSpawnSoundRadius ) continue; // Spigot
                    if (distanceSquared > viewDistance * viewDistance) {
                        double deltaLength = Math.sqrt(distanceSquared);
                        double relativeX = player.locX + (deltaX / deltaLength) * viewDistance;
                        double relativeZ = player.locZ + (deltaZ / deltaLength) * viewDistance;
                        player.playerConnection.sendPacket(new PacketPlayOutWorldEvent(1013, new BlockPosition((int) relativeX, (int) this.locY, (int) relativeZ), 0, true));
                    } else {
                        player.playerConnection.sendPacket(new PacketPlayOutWorldEvent(1013, new BlockPosition((int) this.locX, (int) this.locY, (int) this.locZ), 0, true));
                    }
                }
                // CraftBukkit end
            }

            this.l(i);
            if (this.ticksLived % 10 == 0) {
                this.heal(10.0F, EntityRegainHealthEvent.RegainReason.WITHER_SPAWN); // CraftBukkit
            }

        } else {
            super.M();

            int j;

            for (i = 1; i < 3; ++i) {
                if (this.ticksLived >= this.bC[i - 1]) {
                    this.bC[i - 1] = this.ticksLived + 10 + this.random.nextInt(10);
                    if (this.world.getDifficulty() == EnumDifficulty.NORMAL || this.world.getDifficulty() == EnumDifficulty.HARD) {
                        int k = i - 1;
                        int l = this.bD[i - 1];

                        this.bD[k] = this.bD[i - 1] + 1;
                        if (l > 15) {
                            float f = 10.0F;
                            float f1 = 5.0F;
                            double d0 = MathHelper.a(this.random, this.locX - (double) f, this.locX + (double) f);
                            double d1 = MathHelper.a(this.random, this.locY - (double) f1, this.locY + (double) f1);
                            double d2 = MathHelper.a(this.random, this.locZ - (double) f, this.locZ + (double) f);

                            this.a(i + 1, d0, d1, d2, true);
                            this.bD[i - 1] = 0;
                        }
                    }

                    j = this.m(i);
                    if (j > 0) {
                        Entity entity = this.world.getEntity(j);

                        if (entity != null && entity.isAlive() && this.h(entity) <= 900.0D && this.hasLineOfSight(entity)) {
                            if (entity instanceof EntityHuman && ((EntityHuman) entity).abilities.isInvulnerable) {
                                this.a(i, 0);
                            } else {
                                this.a(i + 1, (EntityLiving) entity);
                                this.bC[i - 1] = this.ticksLived + 40 + this.random.nextInt(20);
                                this.bD[i - 1] = 0;
                            }
                        } else {
                            this.a(i, 0);
                        }
                    } else {
                        List list = this.world.a(EntityLiving.class, this.getBoundingBox().grow(20.0D, 8.0D, 20.0D), Predicates.and(EntityWither.bG, IEntitySelector.e));

                        for (int i1 = 0; i1 < 10 && !list.isEmpty(); ++i1) {
                            EntityLiving entityliving = (EntityLiving) list.get(this.random.nextInt(list.size()));

                            if (entityliving != this && entityliving.isAlive() && this.hasLineOfSight(entityliving)) {
                                if (entityliving instanceof EntityHuman) {
                                    if (!((EntityHuman) entityliving).abilities.isInvulnerable) {
                                        this.a(i, entityliving.getId());
                                    }
                                } else {
                                    this.a(i, entityliving.getId());
                                }
                                break;
                            }

                            list.remove(entityliving);
                        }
                    }
                }
            }

            if (this.getGoalTarget() != null) {
                this.a(0, this.getGoalTarget().getId());
            } else {
                this.a(0, 0);
            }

            if (this.bE > 0) {
                --this.bE;
                if (this.bE == 0 && this.world.getGameRules().getBoolean("mobGriefing")) {
                    i = MathHelper.floor(this.locY);
                    j = MathHelper.floor(this.locX);
                    int j1 = MathHelper.floor(this.locZ);
                    boolean flag = false;

                    for (int k1 = -1; k1 <= 1; ++k1) {
                        for (int l1 = -1; l1 <= 1; ++l1) {
                            for (int i2 = 0; i2 <= 3; ++i2) {
                                int j2 = j + k1;
                                int k2 = i + i2;
                                int l2 = j1 + l1;
                                BlockPosition blockposition = new BlockPosition(j2, k2, l2);
                                IBlockData iblockdata = this.world.getType(blockposition);
                                Block block = iblockdata.getBlock();

                                if (iblockdata.getMaterial() != Material.AIR && a(block)) {
                                    // CraftBukkit start
                                    if (CraftEventFactory.callEntityChangeBlockEvent(this, j2, k2, l2, Blocks.AIR, 0).isCancelled()) {
                                        continue;
                                    }
                                    // CraftBukkit end
                                    flag = this.world.setAir(blockposition, true) || flag;
                                }
                            }
                        }
                    }

                    if (flag) {
                        this.world.a((EntityHuman) null, 1022, new BlockPosition(this), 0);
                    }
                }
            }

            if (this.ticksLived % 20 == 0) {
                this.heal(1.0F, EntityRegainHealthEvent.RegainReason.REGEN); // CraftBukkit
            }

            this.bF.setProgress(this.getHealth() / this.getMaxHealth());
        }
    }

    public static boolean a(Block block) {
        return block != Blocks.BEDROCK && block != Blocks.END_PORTAL && block != Blocks.END_PORTAL_FRAME && block != Blocks.COMMAND_BLOCK && block != Blocks.dc && block != Blocks.dd && block != Blocks.BARRIER;
    }

    public void o() {
        this.l(220);
        this.setHealth(this.getMaxHealth() / 3.0F);
    }

    public void aQ() {}

    public void b(EntityPlayer entityplayer) {
        super.b(entityplayer);
        this.bF.addPlayer(entityplayer);
    }

    public void c(EntityPlayer entityplayer) {
        super.c(entityplayer);
        this.bF.removePlayer(entityplayer);
    }

    private double n(int i) {
        if (i <= 0) {
            return this.locX;
        } else {
            float f = (this.aN + (float) (180 * (i - 1))) * 0.017453292F;
            float f1 = MathHelper.cos(f);

            return this.locX + (double) f1 * 1.3D;
        }
    }

    private double o(int i) {
        return i <= 0 ? this.locY + 3.0D : this.locY + 2.2D;
    }

    private double p(int i) {
        if (i <= 0) {
            return this.locZ;
        } else {
            float f = (this.aN + (float) (180 * (i - 1))) * 0.017453292F;
            float f1 = MathHelper.sin(f);

            return this.locZ + (double) f1 * 1.3D;
        }
    }

    private float b(float f, float f1, float f2) {
        float f3 = MathHelper.g(f1 - f);

        if (f3 > f2) {
            f3 = f2;
        }

        if (f3 < -f2) {
            f3 = -f2;
        }

        return f + f3;
    }

    private void a(int i, EntityLiving entityliving) {
        this.a(i, entityliving.locX, entityliving.locY + (double) entityliving.getHeadHeight() * 0.5D, entityliving.locZ, i == 0 && this.random.nextFloat() < 0.001F);
    }

    private void a(int i, double d0, double d1, double d2, boolean flag) {
        this.world.a((EntityHuman) null, 1024, new BlockPosition(this), 0);
        double d3 = this.n(i);
        double d4 = this.o(i);
        double d5 = this.p(i);
        double d6 = d0 - d3;
        double d7 = d1 - d4;
        double d8 = d2 - d5;
        EntityWitherSkull entitywitherskull = new EntityWitherSkull(this.world, this, d6, d7, d8);

        if (flag) {
            entitywitherskull.setCharged(true);
        }

        entitywitherskull.locY = d4;
        entitywitherskull.locX = d3;
        entitywitherskull.locZ = d5;
        this.world.addEntity(entitywitherskull);
    }

    public void a(EntityLiving entityliving, float f) {
        this.a(0, entityliving);
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else if (damagesource != DamageSource.DROWN && !(damagesource.getEntity() instanceof EntityWither)) {
            if (this.da() > 0 && damagesource != DamageSource.OUT_OF_WORLD) {
                return false;
            } else {
                Entity entity;

                if (this.db()) {
                    entity = damagesource.i();
                    if (entity instanceof EntityArrow) {
                        return false;
                    }
                }

                entity = damagesource.getEntity();
                if (entity != null && !(entity instanceof EntityHuman) && entity instanceof EntityLiving && ((EntityLiving) entity).getMonsterType() == this.getMonsterType()) {
                    return false;
                } else {
                    if (this.bE <= 0) {
                        this.bE = 20;
                    }

                    for (int i = 0; i < this.bD.length; ++i) {
                        this.bD[i] += 3;
                    }

                    return super.damageEntity(damagesource, f);
                }
            }
        } else {
            return false;
        }
    }

    protected void dropDeathLoot(boolean flag, int i) {
        EntityItem entityitem = this.a(Items.NETHER_STAR, 1);

        if (entityitem != null) {
            entityitem.v();
        }

        if (!this.world.isClientSide) {
            Iterator iterator = this.world.a(EntityHuman.class, this.getBoundingBox().grow(50.0D, 100.0D, 50.0D)).iterator();

            while (iterator.hasNext()) {
                EntityHuman entityhuman = (EntityHuman) iterator.next();

                entityhuman.b((Statistic) AchievementList.J);
            }
        }

    }

    protected void L() {
        this.ticksFarFromPlayer = 0;
    }

    public void e(float f, float f1) {}

    public void addEffect(MobEffect mobeffect) {}

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(300.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.6000000238418579D);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(40.0D);
        this.getAttributeInstance(GenericAttributes.g).setValue(4.0D);
    }

    public int da() {
        return ((Integer) this.datawatcher.get(EntityWither.bx)).intValue();
    }

    public void l(int i) {
        this.datawatcher.set(EntityWither.bx, Integer.valueOf(i));
    }

    public int m(int i) {
        return ((Integer) this.datawatcher.get(EntityWither.bw[i])).intValue();
    }

    public void a(int i, int j) {
        this.datawatcher.set(EntityWither.bw[i], Integer.valueOf(j));
    }

    public boolean db() {
        return this.getHealth() <= this.getMaxHealth() / 2.0F;
    }

    public EnumMonsterType getMonsterType() {
        return EnumMonsterType.UNDEAD;
    }

    protected boolean n(Entity entity) {
        return false;
    }

    public boolean aV() {
        return false;
    }

    class a extends PathfinderGoal {

        public a() {
            this.a(7);
        }

        public boolean a() {
            return EntityWither.this.da() > 0;
        }
    }
}
