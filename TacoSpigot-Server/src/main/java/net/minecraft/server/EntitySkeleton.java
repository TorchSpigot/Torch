package net.minecraft.server;

import java.util.Calendar;
import javax.annotation.Nullable;
import org.bukkit.event.entity.EntityCombustEvent; // CraftBukkit

public class EntitySkeleton extends EntityMonster implements IRangedEntity {

    private static final DataWatcherObject<Integer> a = DataWatcher.a(EntitySkeleton.class, DataWatcherRegistry.b);
    private static final DataWatcherObject<Boolean> b = DataWatcher.a(EntitySkeleton.class, DataWatcherRegistry.h);
    private final PathfinderGoalBowShoot c = new PathfinderGoalBowShoot(this, 1.0D, 20, 15.0F);
    private final PathfinderGoalMeleeAttack bw = new PathfinderGoalMeleeAttack(this, 1.2D, false) { // CraftBukkit decompile error flag -> false
        public void d() {
            super.d();
            EntitySkeleton.this.a(false);
        }

        public void c() {
            super.c();
            EntitySkeleton.this.a(true);
        }
    };

    public EntitySkeleton(World world) {
        super(world);
        this.o();
    }

    protected void r() {
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalRestrictSun(this));
        this.goalSelector.a(3, new PathfinderGoalFleeSun(this, 1.0D));
        this.goalSelector.a(3, new PathfinderGoalAvoidTarget(this, EntityWolf.class, 6.0F, 1.0D, 1.2D));
        this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityIronGolem.class, true));
    }

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.25D);
    }

    protected void i() {
        super.i();
        this.datawatcher.register(EntitySkeleton.a, Integer.valueOf(0));
        this.datawatcher.register(EntitySkeleton.b, Boolean.valueOf(false));
    }

    protected SoundEffect G() {
        return SoundEffects.fi;
    }

    protected SoundEffect bS() {
        return SoundEffects.fn;
    }

    protected SoundEffect bT() {
        return SoundEffects.fj;
    }

    protected void a(BlockPosition blockposition, Block block) {
        this.a(SoundEffects.fp, 0.15F, 1.0F);
    }

    public boolean B(Entity entity) {
        if (super.B(entity)) {
            if (this.getSkeletonType() == 1 && entity instanceof EntityLiving) {
                ((EntityLiving) entity).addEffect(new MobEffect(MobEffects.WITHER, 200));
            }

            return true;
        } else {
            return false;
        }
    }

    public EnumMonsterType getMonsterType() {
        return EnumMonsterType.UNDEAD;
    }

    public void n() {
        if (this.world.B() && !this.world.isClientSide) {
            float f = this.e(1.0F);
            BlockPosition blockposition = this.bz() instanceof EntityBoat ? (new BlockPosition(this.locX, (double) Math.round(this.locY), this.locZ)).up() : new BlockPosition(this.locX, (double) Math.round(this.locY), this.locZ);

            if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.h(blockposition)) {
                boolean flag = true;
                ItemStack itemstack = this.getEquipment(EnumItemSlot.HEAD);

                if (itemstack != null) {
                    if (itemstack.e()) {
                        itemstack.setData(itemstack.h() + this.random.nextInt(2));
                        if (itemstack.h() >= itemstack.j()) {
                            this.b(itemstack);
                            this.setSlot(EnumItemSlot.HEAD, (ItemStack) null);
                        }
                    }

                    flag = false;
                }

                if (flag) {
                    // CraftBukkit start
                    EntityCombustEvent event = new EntityCombustEvent(this.getBukkitEntity(), 8);
                    this.world.getServer().getPluginManager().callEvent(event);

                    if (!event.isCancelled()) {
                        this.setOnFire(event.getDuration());
                    }
                    // CraftBukkit end
                }
            }
        }

        if (this.world.isClientSide) {
            this.b(this.getSkeletonType());
        }

        super.n();
    }

    public void aw() {
        super.aw();
        if (this.bz() instanceof EntityCreature) {
            EntityCreature entitycreature = (EntityCreature) this.bz();

            this.aN = entitycreature.aN;
        }

    }

    public void die(DamageSource damagesource) {
        // super.die(damagesource); // CraftBukkit
        if (damagesource.i() instanceof EntityArrow && damagesource.getEntity() instanceof EntityHuman) {
            EntityHuman entityhuman = (EntityHuman) damagesource.getEntity();
            double d0 = entityhuman.locX - this.locX;
            double d1 = entityhuman.locZ - this.locZ;

            if (d0 * d0 + d1 * d1 >= 2500.0D) {
                entityhuman.b((Statistic) AchievementList.v);
            }
        } else if (damagesource.getEntity() instanceof EntityCreeper && ((EntityCreeper) damagesource.getEntity()).isPowered() && ((EntityCreeper) damagesource.getEntity()).canCauseHeadDrop()) {
            ((EntityCreeper) damagesource.getEntity()).setCausedHeadDrop();
            this.a(new ItemStack(Items.SKULL, 1, this.getSkeletonType() == 1 ? 1 : 0), 0.0F);
        }
        super.die(damagesource); // CraftBukkit - moved from above

    }

    @Nullable
    protected MinecraftKey J() {
        return this.getSkeletonType() == 1 ? LootTables.al : LootTables.ak;
    }

    protected void a(DifficultyDamageScaler difficultydamagescaler) {
        super.a(difficultydamagescaler);
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Nullable
    public GroupDataEntity prepare(DifficultyDamageScaler difficultydamagescaler, @Nullable GroupDataEntity groupdataentity) {
        groupdataentity = super.prepare(difficultydamagescaler, groupdataentity);
        if (this.world.worldProvider instanceof WorldProviderHell && this.getRandom().nextInt(5) > 0) {
            this.goalSelector.a(4, this.bw);
            this.setSkeletonType(1);
            this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(4.0D);
        } else {
            this.goalSelector.a(4, this.c);
            this.a(difficultydamagescaler);
            this.b(difficultydamagescaler);
        }

        this.l(this.random.nextFloat() < 0.55F * difficultydamagescaler.c());
        if (this.getEquipment(EnumItemSlot.HEAD) == null) {
            Calendar calendar = this.world.ac();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.random.nextFloat() < 0.25F) {
                this.setSlot(EnumItemSlot.HEAD, new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
                this.dropChanceArmor[EnumItemSlot.HEAD.b()] = 0.0F;
            }
        }

        return groupdataentity;
    }

    public void o() {
        if (this.world != null && !this.world.isClientSide) {
            this.goalSelector.a((PathfinderGoal) this.bw);
            this.goalSelector.a((PathfinderGoal) this.c);
            ItemStack itemstack = this.getItemInMainHand();

            if (itemstack != null && itemstack.getItem() == Items.BOW) {
                byte b0 = 20;

                if (this.world.getDifficulty() != EnumDifficulty.HARD) {
                    b0 = 40;
                }

                this.c.b(b0);
                this.goalSelector.a(4, this.c);
            } else {
                this.goalSelector.a(4, this.bw);
            }
        }

    }

    public void a(EntityLiving entityliving, float f) {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.world, this);
        double d0 = entityliving.locX - this.locX;
        double d1 = entityliving.getBoundingBox().b + (double) (entityliving.length / 3.0F) - entitytippedarrow.locY;
        double d2 = entityliving.locZ - this.locZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);

        entitytippedarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float) (14 - this.world.getDifficulty().a() * 4));
        int i = EnchantmentManager.a(Enchantments.ARROW_DAMAGE, (EntityLiving) this);
        int j = EnchantmentManager.a(Enchantments.ARROW_KNOCKBACK, (EntityLiving) this);

        entitytippedarrow.c((double) (f * 2.0F) + this.random.nextGaussian() * 0.25D + (double) ((float) this.world.getDifficulty().a() * 0.11F));
        if (i > 0) {
            entitytippedarrow.c(entitytippedarrow.k() + (double) i * 0.5D + 0.5D);
        }

        if (j > 0) {
            entitytippedarrow.setKnockbackStrength(j);
        }

        if (EnchantmentManager.a(Enchantments.ARROW_FIRE, (EntityLiving) this) > 0 || this.getSkeletonType() == 1) {
            // CraftBukkit start - call EntityCombustEvent
            EntityCombustEvent event = new EntityCombustEvent(entitytippedarrow.getBukkitEntity(), 100);
            this.world.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                entitytippedarrow.setOnFire(event.getDuration());
            }
            // CraftBukkit end
        }

        // CraftBukkit start
        org.bukkit.event.entity.EntityShootBowEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callEntityShootBowEvent(this, this.getItemInMainHand(), entitytippedarrow, 0.8F);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            return;
        }

        if (event.getProjectile() == entitytippedarrow.getBukkitEntity()) {
            world.addEntity(entitytippedarrow);
        }
        // CraftBukkit end

        this.a(SoundEffects.fo, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        // this.world.addEntity(entitytippedarrow); // CraftBukkit - moved up
    }

    public int getSkeletonType() {
        return ((Integer) this.datawatcher.get(EntitySkeleton.a)).intValue();
    }

    public void setSkeletonType(int i) {
        this.datawatcher.set(EntitySkeleton.a, Integer.valueOf(i));
        this.fireProof = i == 1;
        this.b(i);
    }

    private void b(int i) {
        if (i == 1) {
            this.setSize(0.7F, 2.4F);
        } else {
            this.setSize(0.6F, 1.99F);
        }

    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.hasKeyOfType("SkeletonType", 99)) {
            byte b0 = nbttagcompound.getByte("SkeletonType");

            this.setSkeletonType(b0);
        }

        this.o();
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setByte("SkeletonType", (byte) this.getSkeletonType());
    }

    public void setSlot(EnumItemSlot enumitemslot, @Nullable ItemStack itemstack) {
        super.setSlot(enumitemslot, itemstack);
        if (!this.world.isClientSide && enumitemslot == EnumItemSlot.MAINHAND) {
            this.o();
        }

    }

    public float getHeadHeight() {
        return this.getSkeletonType() == 1 ? 2.1F : 1.74F;
    }

    public double ax() {
        return -0.35D;
    }

    public void a(boolean flag) {
        this.datawatcher.set(EntitySkeleton.b, Boolean.valueOf(flag));
    }
}
