package net.minecraft.server;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

//CraftBukkit start
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityTargetEvent;
//CraftBukkit end

public class EntityZombie extends EntityMonster {

    protected static final IAttribute a = (new AttributeRanged((IAttribute) null, "zombie.spawnReinforcements", 0.0D, 0.0D, 1.0D)).a("Spawn Reinforcements Chance");
    private static final UUID b = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private final AttributeModifier c = new AttributeModifier(EntityZombie.b, "Baby speed boost", world.paperConfig.babyZombieMovementSpeed, 1); // Paper - Remove static - Make baby speed configurable
    private static final DataWatcherObject<Boolean> bw = DataWatcher.a(EntityZombie.class, DataWatcherRegistry.h);
    private static final DataWatcherObject<Integer> bx = DataWatcher.a(EntityZombie.class, DataWatcherRegistry.b);
    private static final DataWatcherObject<Boolean> by = DataWatcher.a(EntityZombie.class, DataWatcherRegistry.h);
    private final PathfinderGoalBreakDoor bz = new PathfinderGoalBreakDoor(this);
    private boolean bA;
    private float bB = -1.0F;
    private float bC;

    public EntityZombie(World world) {
        super(world);
        this.setSize(0.6F, 1.95F);
    }

    @Override
	protected void r() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalZombieAttack(this, 1.0D, false));
        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
        this.goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.dk();
    }

    protected void dk() {
        this.goalSelector.a(6, new PathfinderGoalMoveThroughVillage(this, 1.0D, false));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true, new Class[] { EntityPigZombie.class}));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        if ( world.spigotConfig.zombieAggressiveTowardsVillager ) this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityVillager.class, false)); // Spigot
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityIronGolem.class, true));
    }

    @Override
	protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(35.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.23000000417232513D);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.0D);
        this.getAttributeInstance(GenericAttributes.g).setValue(2.0D);
        this.getAttributeMap().b(EntityZombie.a).setValue(this.random.nextDouble() * 0.10000000149011612D);
    }

    @Override
	protected void i() {
        super.i();
        this.getDataWatcher().register(EntityZombie.bw, Boolean.valueOf(false));
        this.getDataWatcher().register(EntityZombie.bx, Integer.valueOf(0));
        this.getDataWatcher().register(EntityZombie.by, Boolean.valueOf(false));
    }

    public void a(boolean flag) {
        this.getDataWatcher().set(EntityZombie.by, Boolean.valueOf(flag));
    }

    public boolean dn() {
        return this.bA;
    }

    public void p(boolean flag) {
        if (this.bA != flag) {
            this.bA = flag;
            ((Navigation) this.getNavigation()).a(flag);
            if (flag) {
                this.goalSelector.a(1, this.bz);
            } else {
                this.goalSelector.a(this.bz);
            }
        }

    }

    @Override
	public boolean isBaby() {
        return this.getDataWatcher().get(EntityZombie.bw).booleanValue();
    }

    @Override
	protected int getExpValue(EntityHuman entityhuman) {
        if (this.isBaby()) {
            this.b_ = (int) (this.b_ * 2.5F);
        }

        return super.getExpValue(entityhuman);
    }

    public void setBaby(boolean flag) {
        this.getDataWatcher().set(EntityZombie.bw, Boolean.valueOf(flag));
        if (this.world != null && !this.world.isClientSide) {
            AttributeInstance attributeinstance = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);

            attributeinstance.c(this.c);
            if (flag) {
                attributeinstance.b(this.c);
            }
        }

        this.r(flag);
    }

    @Override
	public void a(DataWatcherObject<?> datawatcherobject) {
        if (EntityZombie.bw.equals(datawatcherobject)) {
            this.r(this.isBaby());
        }

        super.a(datawatcherobject);
    }

    @Override
	public void n() {
        if (this.world.B() && !this.world.isClientSide && !this.isBaby() && this.o()) {
            float f = this.e(1.0F);

            if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.h(new BlockPosition(this.locX, this.locY + this.getHeadHeight(), this.locZ))) {
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

    protected boolean o() {
        return true;
    }

    @Override
	public boolean damageEntity(DamageSource damagesource, float f) {
        if (super.damageEntity(damagesource, f)) {
            EntityLiving entityliving = this.getGoalTarget();

            if (entityliving == null && damagesource.getEntity() instanceof EntityLiving) {
                entityliving = (EntityLiving) damagesource.getEntity();
            }

            if (entityliving != null && this.world.getDifficulty() == EnumDifficulty.HARD && this.random.nextFloat() < this.getAttributeInstance(EntityZombie.a).getValue() && this.world.getGameRules().getBoolean("doMobSpawning")) {
                int i = MathHelper.floor(this.locX);
                int j = MathHelper.floor(this.locY);
                int k = MathHelper.floor(this.locZ);
                EntityZombie entityzombie = new EntityZombie(this.world);

                for (int l = 0; l < 50; ++l) {
                    int i1 = i + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                    int j1 = j + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                    int k1 = k + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);

                    if (this.world.getType(new BlockPosition(i1, j1 - 1, k1)).r() && !this.world.isLightLevel(new BlockPosition(i1, j1, k1), 10)) { // Paper
                        entityzombie.setPosition(i1, j1, k1);
                        if (!this.world.isPlayerNearby(i1, j1, k1, 7.0D) && this.world.a(entityzombie.getBoundingBox(), entityzombie) && this.world.getCubes(entityzombie, entityzombie.getBoundingBox()).isEmpty() && !this.world.containsLiquid(entityzombie.getBoundingBox())) {
                            this.world.addEntity(entityzombie, CreatureSpawnEvent.SpawnReason.REINFORCEMENTS); // CraftBukkit
                            entityzombie.setGoalTarget(entityliving, EntityTargetEvent.TargetReason.REINFORCEMENT_TARGET, true);
                            entityzombie.prepare(this.world.D(new BlockPosition(entityzombie)), (GroupDataEntity) null);
                            this.getAttributeInstance(EntityZombie.a).b(new AttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806D, 0));
                            entityzombie.getAttributeInstance(EntityZombie.a).b(new AttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806D, 0));
                            break;
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
	public boolean B(Entity entity) {
        boolean flag = super.B(entity);

        if (flag) {
            float f = this.world.D(new BlockPosition(this)).b();

            if (this.getItemInMainHand().isEmpty() && this.isBurning() && this.random.nextFloat() < f * 0.3F) {
                // CraftBukkit start
                EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), 2 * (int) f); // PAIL: fixme
                this.world.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    entity.setOnFire(event.getDuration());
                }
                // CraftBukkit end
            }
        }

        return flag;
    }

    @Override
	protected SoundEffect G() {
        return SoundEffects.ie;
    }

    @Override
	protected SoundEffect bW() {
        return SoundEffects.im;
    }

    @Override
	protected SoundEffect bX() {
        return SoundEffects.ii;
    }

    protected SoundEffect di() {
        return SoundEffects.is;
    }

    @Override
	protected void a(BlockPosition blockposition, Block block) {
        this.a(this.di(), 0.15F, 1.0F);
    }

    @Override
	public EnumMonsterType getMonsterType() {
        return EnumMonsterType.UNDEAD;
    }

    @Override
	@Nullable
    protected MinecraftKey J() {
        return LootTables.am;
    }

    @Override
	protected void a(DifficultyDamageScaler difficultydamagescaler) {
        super.a(difficultydamagescaler);
        if (this.random.nextFloat() < (this.world.getDifficulty() == EnumDifficulty.HARD ? 0.05F : 0.01F)) {
            int i = this.random.nextInt(3);

            if (i == 0) {
                this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else {
                this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            }
        }

    }

    public static void c(DataConverterManager dataconvertermanager) {
        EntityInsentient.a(dataconvertermanager, EntityZombie.class);
    }

    @Override
	public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        if (this.isBaby()) {
            nbttagcompound.setBoolean("IsBaby", true);
        }

        nbttagcompound.setBoolean("CanBreakDoors", this.dn());
    }

    @Override
	public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.getBoolean("IsBaby")) {
            this.setBaby(true);
        }

        this.p(nbttagcompound.getBoolean("CanBreakDoors"));
    }

    @Override
	public void b(EntityLiving entityliving) {
        super.b(entityliving);
        if ((this.world.getDifficulty() == EnumDifficulty.NORMAL || this.world.getDifficulty() == EnumDifficulty.HARD) && entityliving instanceof EntityVillager) {
            if (this.world.getDifficulty() != EnumDifficulty.HARD && this.random.nextBoolean()) {
                return;
            }

            EntityVillager entityvillager = (EntityVillager) entityliving;
            EntityZombieVillager entityzombievillager = new EntityZombieVillager(this.world);

            entityzombievillager.u(entityvillager);
            this.world.kill(entityvillager);
            entityzombievillager.prepare(this.world.D(new BlockPosition(entityzombievillager)), new EntityZombie.GroupDataZombie(false, null));
            entityzombievillager.setProfession(entityvillager.getProfession());
            entityzombievillager.setBaby(entityvillager.isBaby());
            entityzombievillager.setAI(entityvillager.hasAI());
            if (entityvillager.hasCustomName()) {
                entityzombievillager.setCustomName(entityvillager.getCustomName());
                entityzombievillager.setCustomNameVisible(entityvillager.getCustomNameVisible());
            }

            this.world.addEntity(entityzombievillager, CreatureSpawnEvent.SpawnReason.INFECTION); // CraftBukkit - add SpawnReason
            this.world.a((EntityHuman) null, 1026, new BlockPosition(this), 0);
        }

    }

    @Override
	public float getHeadHeight() {
        float f = 1.74F;

        if (this.isBaby()) {
            f = (float) (f - 0.81D);
        }

        return f;
    }

    @Override
	protected boolean c(ItemStack itemstack) {
        return itemstack.getItem() == Items.EGG && this.isBaby() && this.isPassenger() ? false : super.c(itemstack);
    }

    @Override
	@Nullable
    public GroupDataEntity prepare(DifficultyDamageScaler difficultydamagescaler, @Nullable GroupDataEntity groupdataentity) {
        Object object = super.prepare(difficultydamagescaler, groupdataentity);
        float f = difficultydamagescaler.d();

        this.m(this.random.nextFloat() < 0.55F * f);
        if (object == null) {
            object = new EntityZombie.GroupDataZombie(this.world.random.nextFloat() < 0.05F, null);
        }

        if (object instanceof EntityZombie.GroupDataZombie) {
            EntityZombie.GroupDataZombie entityzombie_groupdatazombie = (EntityZombie.GroupDataZombie) object;

            if (entityzombie_groupdatazombie.a) {
                this.setBaby(true);
                if (this.world.random.nextFloat() < 0.05D) {
                    List list = this.world.a(EntityChicken.class, this.getBoundingBox().grow(5.0D, 3.0D, 5.0D), IEntitySelector.b);

                    if (!list.isEmpty()) {
                        EntityChicken entitychicken = (EntityChicken) list.get(0);

                        entitychicken.p(true);
                        this.startRiding(entitychicken);
                    }
                } else if (this.world.random.nextFloat() < 0.05D) {
                    EntityChicken entitychicken1 = new EntityChicken(this.world);

                    entitychicken1.setPositionRotation(this.locX, this.locY, this.locZ, this.yaw, 0.0F);
                    entitychicken1.prepare(difficultydamagescaler, (GroupDataEntity) null);
                    entitychicken1.p(true);
                    this.world.addEntity(entitychicken1, CreatureSpawnEvent.SpawnReason.MOUNT); // CraftBukkit
                    this.startRiding(entitychicken1);
                }
            }
        }

        this.p(this.random.nextFloat() < f * 0.1F);
        this.a(difficultydamagescaler);
        this.b(difficultydamagescaler);
        if (this.getEquipment(EnumItemSlot.HEAD).isEmpty()) {
            Calendar calendar = this.world.ac();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.random.nextFloat() < 0.25F) {
                this.setSlot(EnumItemSlot.HEAD, new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
                this.dropChanceArmor[EnumItemSlot.HEAD.b()] = 0.0F;
            }
        }

        this.getAttributeInstance(GenericAttributes.c).b(new AttributeModifier("Random spawn bonus", this.random.nextDouble() * 0.05000000074505806D, 0));
        double d0 = this.random.nextDouble() * 1.5D * f;

        if (d0 > 1.0D) {
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).b(new AttributeModifier("Random zombie-spawn bonus", d0, 2));
        }

        if (this.random.nextFloat() < f * 0.05F) {
            this.getAttributeInstance(EntityZombie.a).b(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 0.25D + 0.5D, 0));
            this.getAttributeInstance(GenericAttributes.maxHealth).b(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 3.0D + 1.0D, 2));
            this.p(true);
        }

        return (GroupDataEntity) object;
    }

    public void r(boolean flag) {
        this.a(flag ? 0.5F : 1.0F);
    }

    @Override
	public final void setSize(float f, float f1) {
        boolean flag = this.bB > 0.0F && this.bC > 0.0F;

        this.bB = f;
        this.bC = f1;
        if (!flag) {
            this.a(1.0F);
        }

    }

    protected final void a(float f) {
        super.setSize(this.bB * f, this.bC * f);
    }

    @Override
	public double ax() {
        return this.isBaby() ? 0.0D : -0.45D;
    }

    @Override
	public void die(DamageSource damagesource) {
        // super.die(damagesource); // CraftBukkit
        if (damagesource.getEntity() instanceof EntityCreeper) {
            EntityCreeper entitycreeper = (EntityCreeper) damagesource.getEntity();

            if (entitycreeper.isPowered() && entitycreeper.canCauseHeadDrop()) {
                entitycreeper.setCausedHeadDrop();
                ItemStack itemstack = this.dj();

                if (!itemstack.isEmpty()) {
                    this.a(itemstack, 0.0F);
                }
            }
        }
        super.die(damagesource); // CraftBukkit - moved from above

    }

    protected ItemStack dj() {
        return new ItemStack(Items.SKULL, 1, 2);
    }

    class GroupDataZombie implements GroupDataEntity {

        public boolean a;

        private GroupDataZombie(boolean flag) {
            this.a = flag;
        }

        GroupDataZombie(boolean flag, Object object) {
            this(flag);
        }
    }
}
