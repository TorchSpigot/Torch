package net.minecraft.server;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import java.util.Iterator;
import java.util.Queue;

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
    private static final DataWatcherObject<Boolean> bz = DataWatcher.a(EntityZombie.class, DataWatcherRegistry.h);
    private final PathfinderGoalBreakDoor bA = new PathfinderGoalBreakDoor(this);
    private int bB;
    private boolean bC = false;
    private float bD = -1.0F;
    private float bE;
    private int lastTick = MinecraftServer.currentTick; // CraftBukkit - add field

    public EntityZombie(World world) {
        super(world);
        this.setSize(0.6F, 1.95F);
    }

    protected void r() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalZombieAttack(this, 1.0D, false));
        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.o();
    }

    protected void o() {
        this.goalSelector.a(6, new PathfinderGoalMoveThroughVillage(this, 1.0D, false));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true, new Class[] { EntityPigZombie.class}));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        if ( world.spigotConfig.zombieAggressiveTowardsVillager ) this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityVillager.class, false)); // Spigot
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityIronGolem.class, true));
    }

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(35.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.23000000417232513D);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.0D);
        this.getAttributeInstance(GenericAttributes.g).setValue(2.0D);
        this.getAttributeMap().b(EntityZombie.a).setValue(this.random.nextDouble() * 0.10000000149011612D);
    }

    protected void i() {
        super.i();
        this.getDataWatcher().register(EntityZombie.bw, Boolean.valueOf(false));
        this.getDataWatcher().register(EntityZombie.bx, Integer.valueOf(0));
        this.getDataWatcher().register(EntityZombie.by, Boolean.valueOf(false));
        this.getDataWatcher().register(EntityZombie.bz, Boolean.valueOf(false));
    }

    public void a(boolean flag) {
        this.getDataWatcher().set(EntityZombie.bz, Boolean.valueOf(flag));
    }

    public boolean dd() {
        return this.bC;
    }

    public void o(boolean flag) {
        if (this.bC != flag) {
            this.bC = flag;
            ((Navigation) this.getNavigation()).a(flag);
            if (flag) {
                this.goalSelector.a(1, this.bA);
            } else {
                this.goalSelector.a((PathfinderGoal) this.bA);
            }
        }

    }

    public boolean isBaby() {
        return ((Boolean) this.getDataWatcher().get(EntityZombie.bw)).booleanValue();
    }

    protected int getExpValue(EntityHuman entityhuman) {
        if (this.isBaby()) {
            this.b_ = (int) ((float) this.b_ * 2.5F);
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

        this.q(flag);
    }

    public boolean isVillager() {
        return ((Integer) this.getDataWatcher().get(EntityZombie.bx)).intValue() > 0;
    }

    public int getVillagerType() {
        return ((Integer) this.getDataWatcher().get(EntityZombie.bx)).intValue() - 1;
    }

    public void setVillagerType(int i) {
        this.getDataWatcher().set(EntityZombie.bx, Integer.valueOf(i + 1));
    }

    public void clearVillagerType() {
        this.getDataWatcher().set(EntityZombie.bx, Integer.valueOf(0));
    }

    public void a(DataWatcherObject<?> datawatcherobject) {
        if (EntityZombie.bw.equals(datawatcherobject)) {
            this.q(this.isBaby());
        }

        super.a(datawatcherobject);
    }

    public void n() {
        if (this.world.B() && !this.world.isClientSide && !this.isBaby()) {
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

        super.n();
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (super.damageEntity(damagesource, f)) {
            EntityLiving entityliving = this.getGoalTarget();

            if (entityliving == null && damagesource.getEntity() instanceof EntityLiving) {
                entityliving = (EntityLiving) damagesource.getEntity();
            }

            if (entityliving != null && this.world.getDifficulty() == EnumDifficulty.HARD && (double) this.random.nextFloat() < this.getAttributeInstance(EntityZombie.a).getValue() && this.world.getGameRules().getBoolean("doMobSpawning")) {
                int i = MathHelper.floor(this.locX);
                int j = MathHelper.floor(this.locY);
                int k = MathHelper.floor(this.locZ);
                EntityZombie entityzombie = new EntityZombie(this.world);

                for (int l = 0; l < 50; ++l) {
                    int i1 = i + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                    int j1 = j + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                    int k1 = k + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);

                    if (this.world.getType(new BlockPosition(i1, j1 - 1, k1)).q() && !this.world.isLightLevel(new BlockPosition(i1, j1, k1), 10)) { // Paper
                        entityzombie.setPosition((double) i1, (double) j1, (double) k1);
                        if (!this.world.isPlayerNearby((double) i1, (double) j1, (double) k1, 7.0D, EntityHuman.affectsSpawningFilter()) && this.world.a(entityzombie.getBoundingBox(), (Entity) entityzombie) && this.world.getCubes(entityzombie, entityzombie.getBoundingBox()).isEmpty() && !this.world.containsLiquid(entityzombie.getBoundingBox())) { // Paper - affectsSpawning filter
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

    public void m() {
        if (!this.world.isClientSide && this.isConverting()) {
            int i = this.getConversionTime();
            // CraftBukkit start - Use wall time instead of ticks for villager conversion
            int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
            this.lastTick = MinecraftServer.currentTick;
            i *= elapsedTicks;
            // CraftBukkit end

            this.bB -= i;
            if (this.bB <= 0) {
                this.di();
            }
        }

        super.m();
    }

    public boolean B(Entity entity) {
        boolean flag = super.B(entity);

        if (flag) {
            int i = this.world.getDifficulty().a();

            if (this.getItemInMainHand() == null && this.isBurning() && this.random.nextFloat() < (float) i * 0.3F) {
                // CraftBukkit start
                EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), 2 * i);
                this.world.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    entity.setOnFire(event.getDuration());
                }
                // CraftBukkit end
            }
        }

        return flag;
    }

    protected SoundEffect G() {
        return this.isVillager() ? SoundEffects.hw : SoundEffects.hh;
    }

    protected SoundEffect bS() {
        return this.isVillager() ? SoundEffects.hA : SoundEffects.hp;
    }

    protected SoundEffect bT() {
        return this.isVillager() ? SoundEffects.hz : SoundEffects.hl;
    }

    protected void a(BlockPosition blockposition, Block block) {
        this.a(this.isVillager() ? SoundEffects.hB : SoundEffects.hv, 0.15F, 1.0F);
    }

    public EnumMonsterType getMonsterType() {
        return EnumMonsterType.UNDEAD;
    }

    @Nullable
    protected MinecraftKey J() {
        return LootTables.ai;
    }

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

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        if (this.isBaby()) {
            nbttagcompound.setBoolean("IsBaby", true);
        }

        if (this.isVillager()) {
            nbttagcompound.setBoolean("IsVillager", true);
            nbttagcompound.setInt("VillagerProfession", this.getVillagerType());
        }

        nbttagcompound.setInt("ConversionTime", this.isConverting() ? this.bB : -1);
        nbttagcompound.setBoolean("CanBreakDoors", this.dd());
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.getBoolean("IsBaby")) {
            this.setBaby(true);
        }

        if (nbttagcompound.getBoolean("IsVillager")) {
            if (nbttagcompound.hasKeyOfType("VillagerProfession", 99)) {
                this.setVillagerType(nbttagcompound.getInt("VillagerProfession"));
            } else {
                this.setVillagerType(this.world.random.nextInt(5));
            }
        }

        if (nbttagcompound.hasKeyOfType("ConversionTime", 99) && nbttagcompound.getInt("ConversionTime") > -1) {
            this.b(nbttagcompound.getInt("ConversionTime"));
        }

        this.o(nbttagcompound.getBoolean("CanBreakDoors"));
    }

    public void b(EntityLiving entityliving) {
        super.b(entityliving);
        if ((this.world.getDifficulty() == EnumDifficulty.NORMAL || this.world.getDifficulty() == EnumDifficulty.HARD) && entityliving instanceof EntityVillager) {
            if (this.world.getDifficulty() != EnumDifficulty.HARD && this.random.nextBoolean()) {
                return;
            }

            EntityVillager entityvillager = (EntityVillager) entityliving;
            EntityZombie entityzombie = new EntityZombie(this.world);

            entityzombie.u(entityliving);
            this.world.kill(entityliving);
            entityzombie.prepare(this.world.D(new BlockPosition(entityzombie)), new EntityZombie.GroupDataZombie(false, true, (EntityZombie.SyntheticClass_1) null));
            entityzombie.setVillagerType(entityvillager.getProfession());
            entityzombie.setBaby(entityliving.isBaby());
            entityzombie.setAI(entityvillager.hasAI());
            if (entityvillager.hasCustomName()) {
                entityzombie.setCustomName(entityvillager.getCustomName());
                entityzombie.setCustomNameVisible(entityvillager.getCustomNameVisible());
            }

            this.world.addEntity(entityzombie, CreatureSpawnEvent.SpawnReason.INFECTION); // CraftBukkit - add SpawnReason
            this.world.a((EntityHuman) null, 1026, new BlockPosition((int) this.locX, (int) this.locY, (int) this.locZ), 0);
        }

    }

    public float getHeadHeight() {
        float f = 1.74F;

        if (this.isBaby()) {
            f = (float) ((double) f - 0.81D);
        }

        return f;
    }

    protected boolean c(ItemStack itemstack) {
        return itemstack.getItem() == Items.EGG && this.isBaby() && this.isPassenger() ? false : super.c(itemstack);
    }

    @Nullable
    public GroupDataEntity prepare(DifficultyDamageScaler difficultydamagescaler, @Nullable GroupDataEntity groupdataentity) {
        Object object = super.prepare(difficultydamagescaler, groupdataentity);
        float f = difficultydamagescaler.c();

        this.l(this.random.nextFloat() < 0.55F * f);
        if (object == null) {
            object = new EntityZombie.GroupDataZombie(this.world.random.nextFloat() < 0.05F, this.world.random.nextFloat() < 0.05F, (EntityZombie.SyntheticClass_1) null);
        }

        if (object instanceof EntityZombie.GroupDataZombie) {
            EntityZombie.GroupDataZombie entityzombie_groupdatazombie = (EntityZombie.GroupDataZombie) object;

            if (entityzombie_groupdatazombie.b) {
                this.setVillagerType(this.random.nextInt(5));
            }

            if (entityzombie_groupdatazombie.a) {
                this.setBaby(true);
                if ((double) this.world.random.nextFloat() < 0.05D) {
                    List list = this.world.a(EntityChicken.class, this.getBoundingBox().grow(5.0D, 3.0D, 5.0D), IEntitySelector.b);

					Iterator it = list.iterator();
                    if (!list.isEmpty()) {
                        EntityChicken entitychicken = (EntityChicken) it.next();

                        entitychicken.o(true);
                        this.startRiding(entitychicken);
                    }
                } else if ((double) this.world.random.nextFloat() < 0.05D) {
                    EntityChicken entitychicken1 = new EntityChicken(this.world);

                    entitychicken1.setPositionRotation(this.locX, this.locY, this.locZ, this.yaw, 0.0F);
                    entitychicken1.prepare(difficultydamagescaler, (GroupDataEntity) null);
                    entitychicken1.o(true);
                    this.world.addEntity(entitychicken1, CreatureSpawnEvent.SpawnReason.MOUNT); // CraftBukkit
                    this.startRiding(entitychicken1);
                }
            }
        }

        this.o(this.random.nextFloat() < f * 0.1F);
        this.a(difficultydamagescaler);
        this.b(difficultydamagescaler);
        if (this.getEquipment(EnumItemSlot.HEAD) == null) {
            Calendar calendar = this.world.ac();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.random.nextFloat() < 0.25F) {
                this.setSlot(EnumItemSlot.HEAD, new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
                this.dropChanceArmor[EnumItemSlot.HEAD.b()] = 0.0F;
            }
        }

        this.getAttributeInstance(GenericAttributes.c).b(new AttributeModifier("Random spawn bonus", this.random.nextDouble() * 0.05000000074505806D, 0));
        double d0 = this.random.nextDouble() * 1.5D * (double) f;

        if (d0 > 1.0D) {
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).b(new AttributeModifier("Random zombie-spawn bonus", d0, 2));
        }

        if (this.random.nextFloat() < f * 0.05F) {
            this.getAttributeInstance(EntityZombie.a).b(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 0.25D + 0.5D, 0));
            this.getAttributeInstance(GenericAttributes.maxHealth).b(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 3.0D + 1.0D, 2));
            this.o(true);
        }

        return (GroupDataEntity) object;
    }

    public boolean a(EntityHuman entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
        if (itemstack != null && itemstack.getItem() == Items.GOLDEN_APPLE && itemstack.getData() == 0 && this.isVillager() && this.hasEffect(MobEffects.WEAKNESS)) {
            if (!entityhuman.abilities.canInstantlyBuild) {
                --itemstack.count;
            }

            if (!this.world.isClientSide) {
                this.b(this.random.nextInt(2401) + 3600);
            }

            return true;
        } else {
            return false;
        }
    }

    protected void b(int i) {
        this.bB = i;
        this.getDataWatcher().set(EntityZombie.by, Boolean.valueOf(true));
        this.removeEffect(MobEffects.WEAKNESS);
        this.addEffect(new MobEffect(MobEffects.INCREASE_DAMAGE, i, Math.min(this.world.getDifficulty().a() - 1, 0)));
        this.world.broadcastEntityEffect(this, (byte) 16);
    }

    protected boolean isTypeNotPersistent() {
        return !this.isConverting();
    }

    public boolean isConverting() {
        return ((Boolean) this.getDataWatcher().get(EntityZombie.by)).booleanValue();
    }

    protected void di() {
        EntityVillager entityvillager = new EntityVillager(this.world);

        entityvillager.u(this);
        entityvillager.prepare(this.world.D(new BlockPosition(entityvillager)), (GroupDataEntity) null);
        entityvillager.de();
        if (this.isBaby()) {
            entityvillager.setAgeRaw(-24000);
        }

        this.world.kill(this);
        entityvillager.setAI(this.hasAI());
        entityvillager.setProfession(this.getVillagerType());
        if (this.hasCustomName()) {
            entityvillager.setCustomName(this.getCustomName());
            entityvillager.setCustomNameVisible(this.getCustomNameVisible());
        }

        this.world.addEntity(entityvillager, CreatureSpawnEvent.SpawnReason.CURED); // CraftBukkit - add SpawnReason
        entityvillager.addEffect(new MobEffect(MobEffects.CONFUSION, 200, 0));
        this.world.a((EntityHuman) null, 1027, new BlockPosition((int) this.locX, (int) this.locY, (int) this.locZ), 0);
    }

    protected int getConversionTime() {
        int i = 1;

        if (this.random.nextFloat() < 0.01F) {
            int j = 0;
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

            for (int k = (int) this.locX - 4; k < (int) this.locX + 4 && j < 14; ++k) {
                for (int l = (int) this.locY - 4; l < (int) this.locY + 4 && j < 14; ++l) {
                    for (int i1 = (int) this.locZ - 4; i1 < (int) this.locZ + 4 && j < 14; ++i1) {
                        Block block = this.world.getType(blockposition_mutableblockposition.c(k, l, i1)).getBlock();

                        if (block == Blocks.IRON_BARS || block == Blocks.BED) {
                            if (this.random.nextFloat() < 0.3F) {
                                ++i;
                            }

                            ++j;
                        }
                    }
                }
            }
        }

        return i;
    }

    public void q(boolean flag) {
        this.a(flag ? 0.5F : 1.0F);
    }

    public final void setSize(float f, float f1) {
        boolean flag = this.bD > 0.0F && this.bE > 0.0F;

        this.bD = f;
        this.bE = f1;
        if (!flag) {
            this.a(1.0F);
        }

    }

    protected final void a(float f) {
        super.setSize(this.bD * f, this.bE * f);
    }

    public double ax() {
        return this.isBaby() ? 0.0D : -0.35D;
    }

    public void die(DamageSource damagesource) {
        // super.die(damagesource); // CraftBukkit
        if (damagesource.getEntity() instanceof EntityCreeper && !(this instanceof EntityPigZombie) && ((EntityCreeper) damagesource.getEntity()).isPowered() && ((EntityCreeper) damagesource.getEntity()).canCauseHeadDrop()) {
            ((EntityCreeper) damagesource.getEntity()).setCausedHeadDrop();
            this.a(new ItemStack(Items.SKULL, 1, 2), 0.0F);
        }
        super.die(damagesource); // CraftBukkit - moved from above

    }

    static class SyntheticClass_1 {    }

    class GroupDataZombie implements GroupDataEntity {

        public boolean a;
        public boolean b;

        private GroupDataZombie(boolean flag, boolean flag1) {
            this.a = false;
            this.b = false;
            this.a = flag;
            this.b = flag1;
        }

        GroupDataZombie(boolean flag, boolean flag1, EntityZombie.SyntheticClass_1 entityzombie_syntheticclass_1) {
            this(flag, flag1);
        }
    }
}
