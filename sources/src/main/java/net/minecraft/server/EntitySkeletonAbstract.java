package net.minecraft.server;

import java.util.Calendar;
import javax.annotation.Nullable;
import org.bukkit.event.entity.EntityCombustEvent; // CraftBukkit

public abstract class EntitySkeletonAbstract extends EntityMonster implements IRangedEntity {

    private static final DataWatcherObject<Boolean> a = DataWatcher.a(EntitySkeletonAbstract.class, DataWatcherRegistry.h);
    private final PathfinderGoalBowShoot b = new PathfinderGoalBowShoot(this, 1.0D, 20, 15.0F);
    private final PathfinderGoalMeleeAttack c = new PathfinderGoalMeleeAttack(this, 1.2D, false) { // CraftBukkit - decompile error
        @Override
		public void d() {
            super.d();
            EntitySkeletonAbstract.this.a(false);
        }

        @Override
		public void c() {
            super.c();
            EntitySkeletonAbstract.this.a(true);
        }
    };

    public EntitySkeletonAbstract(World world) {
        super(world);
        this.setSize(0.6F, 1.99F);
        this.dh();
    }

    @Override
	protected void r() {
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalRestrictSun(this));
        this.goalSelector.a(3, new PathfinderGoalFleeSun(this, 1.0D));
        this.goalSelector.a(3, new PathfinderGoalAvoidTarget(this, EntityWolf.class, 6.0F, 1.0D, 1.2D));
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityIronGolem.class, true));
    }

    @Override
	protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.25D);
    }

    @Override
	protected void i() {
        super.i();
        this.datawatcher.register(EntitySkeletonAbstract.a, Boolean.valueOf(false));
    }

    @Override
	protected void a(BlockPosition blockposition, Block block) {
        this.a(this.o(), 0.15F, 1.0F);
    }

    abstract SoundEffect o();

    @Override
	public EnumMonsterType getMonsterType() {
        return EnumMonsterType.UNDEAD;
    }

    @Override
	public void n() {
        if (this.world.B()) {
            float f = this.e(1.0F);
            BlockPosition blockposition = this.bB() instanceof EntityBoat ? (new BlockPosition(this.locX, Math.round(this.locY), this.locZ)).up() : new BlockPosition(this.locX, Math.round(this.locY), this.locZ);

            if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.h(blockposition)) {
                boolean flag = true;
                ItemStack itemstack = this.getEquipment(EnumItemSlot.HEAD);

                if (!itemstack.isEmpty()) {
                    if (itemstack.f()) {
                        itemstack.setData(itemstack.i() + this.random.nextInt(2));
                        if (itemstack.i() >= itemstack.k()) {
                            this.b(itemstack);
                            this.setSlot(EnumItemSlot.HEAD, ItemStack.a);
                        }
                    }

                    flag = false;
                }

                if (flag && !this.isInWater()) {
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

        super.n();
    }

    @Override
	public void aw() {
        super.aw();
        if (this.bB() instanceof EntityCreature) {
            EntityCreature entitycreature = (EntityCreature) this.bB();

            this.aN = entitycreature.aN;
        }

    }

    @Override
	public void die(DamageSource damagesource) {
        // super.die(damagesource); // CraftBukkit
        if (damagesource.i() instanceof EntityArrow && damagesource.getEntity() instanceof EntityHuman) {
            EntityHuman entityhuman = (EntityHuman) damagesource.getEntity();
            double d0 = entityhuman.locX - this.locX;
            double d1 = entityhuman.locZ - this.locZ;

            if (d0 * d0 + d1 * d1 >= 2500.0D) {
                entityhuman.b(AchievementList.v);
            }
        }
        super.die(damagesource); // CraftBukkit - moved from above

    }

    @Override
	protected void a(DifficultyDamageScaler difficultydamagescaler) {
        super.a(difficultydamagescaler);
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Override
	@Nullable
    public GroupDataEntity prepare(DifficultyDamageScaler difficultydamagescaler, @Nullable GroupDataEntity groupdataentity) {
        groupdataentity = super.prepare(difficultydamagescaler, groupdataentity);
        this.a(difficultydamagescaler);
        this.b(difficultydamagescaler);
        this.dh();
        this.m(this.random.nextFloat() < 0.55F * difficultydamagescaler.d());
        if (this.getEquipment(EnumItemSlot.HEAD).isEmpty()) {
            Calendar calendar = this.world.ac();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.random.nextFloat() < 0.25F) {
                this.setSlot(EnumItemSlot.HEAD, new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
                this.dropChanceArmor[EnumItemSlot.HEAD.b()] = 0.0F;
            }
        }

        return groupdataentity;
    }

    public void dh() {
        if (this.world != null) {
            this.goalSelector.a(this.c);
            this.goalSelector.a(this.b);
            ItemStack itemstack = this.getItemInMainHand();

            if (itemstack.getItem() == Items.BOW) {
                byte b0 = 20;

                if (this.world.getDifficulty() != EnumDifficulty.HARD) {
                    b0 = 40;
                }

                this.b.b(b0);
                this.goalSelector.a(4, this.b);
            } else {
                this.goalSelector.a(4, this.c);
            }

        }
    }

    @Override
	public void a(EntityLiving entityliving, float f) {
        EntityArrow entityarrow = this.a(f);
        double d0 = entityliving.locX - this.locX;
        double d1 = entityliving.getBoundingBox().b + entityliving.length / 3.0F - entityarrow.locY;
        double d2 = entityliving.locZ - this.locZ;
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

        entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, 14 - this.world.getDifficulty().a() * 4);
        // CraftBukkit start
        org.bukkit.event.entity.EntityShootBowEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callEntityShootBowEvent(this, this.getItemInMainHand(), entityarrow, 0.8F);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            return;
        }

        if (event.getProjectile() == entityarrow.getBukkitEntity()) {
            world.addEntity(entityarrow);
        }
        // CraftBukkit end
        this.a(SoundEffects.fV, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        // this.world.addEntity(entityarrow); // CraftBukkit - moved up
    }

    protected EntityArrow a(float f) {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.world, this);

        entitytippedarrow.a(this, f);
        return entitytippedarrow;
    }

    @Override
	public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.dh();
    }

    @Override
	public void setSlot(EnumItemSlot enumitemslot, ItemStack itemstack) {
        super.setSlot(enumitemslot, itemstack);
        if (enumitemslot == EnumItemSlot.MAINHAND) {
            this.dh();
        }

    }

    @Override
	public float getHeadHeight() {
        return 1.74F;
    }

    @Override
	public double ax() {
        return -0.6D;
    }

    public void a(boolean flag) {
        this.datawatcher.set(EntitySkeletonAbstract.a, Boolean.valueOf(flag));
    }
}
