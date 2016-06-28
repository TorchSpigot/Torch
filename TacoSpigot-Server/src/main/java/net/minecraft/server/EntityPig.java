package net.minecraft.server;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class EntityPig extends EntityAnimal {

    private static final DataWatcherObject<Boolean> bw = DataWatcher.a(EntityPig.class, DataWatcherRegistry.h);
    private static final Set<Item> bx = Sets.newHashSet(new Item[] { Items.CARROT, Items.POTATO, Items.BEETROOT});
    private boolean by;
    private int bA;
    private int bB;

    public EntityPig(World world) {
        super(world);
        this.setSize(0.9F, 0.9F);
    }

    protected void r() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalPanic(this, 1.25D));
        this.goalSelector.a(3, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.a(4, new PathfinderGoalTempt(this, 1.2D, Items.CARROT_ON_A_STICK, false));
        this.goalSelector.a(4, new PathfinderGoalTempt(this, 1.2D, false, EntityPig.bx));
        this.goalSelector.a(5, new PathfinderGoalFollowParent(this, 1.1D));
        this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
    }

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(10.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.25D);
    }

    @Nullable
    public Entity bu() {
        return this.bv().isEmpty() ? null : (Entity) this.bv().get(0);
    }

    public boolean cL() {
        Entity entity = this.bu();

        if (!(entity instanceof EntityHuman)) {
            return false;
        } else {
            EntityHuman entityhuman = (EntityHuman) entity;
            ItemStack itemstack = entityhuman.getItemInMainHand();

            if (itemstack != null && itemstack.getItem() == Items.CARROT_ON_A_STICK) {
                return true;
            } else {
                itemstack = entityhuman.getItemInOffHand();
                return itemstack != null && itemstack.getItem() == Items.CARROT_ON_A_STICK;
            }
        }
    }

    protected void i() {
        super.i();
        this.datawatcher.register(EntityPig.bw, Boolean.valueOf(false));
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setBoolean("Saddle", this.hasSaddle());
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.setSaddle(nbttagcompound.getBoolean("Saddle"));
    }

    protected SoundEffect G() {
        return SoundEffects.dQ;
    }

    protected SoundEffect bS() {
        return SoundEffects.dS;
    }

    protected SoundEffect bT() {
        return SoundEffects.dR;
    }

    protected void a(BlockPosition blockposition, Block block) {
        this.a(SoundEffects.dU, 0.15F, 1.0F);
    }

    public boolean a(EntityHuman entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
        if (!super.a(entityhuman, enumhand, itemstack)) {
            if (this.hasSaddle() && !this.world.isClientSide && !this.isVehicle()) {
                entityhuman.startRiding(this);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    protected void dropEquipment(boolean flag, int i) {
        super.dropEquipment(flag, i);
        if (this.hasSaddle()) {
            this.a(Items.SADDLE, 1);
        }

    }

    @Nullable
    protected MinecraftKey J() {
        return LootTables.D;
    }

    public boolean hasSaddle() {
        return ((Boolean) this.datawatcher.get(EntityPig.bw)).booleanValue();
    }

    public void setSaddle(boolean flag) {
        if (flag) {
            this.datawatcher.set(EntityPig.bw, Boolean.valueOf(true));
        } else {
            this.datawatcher.set(EntityPig.bw, Boolean.valueOf(false));
        }

    }

    public void onLightningStrike(EntityLightning entitylightning) {
        if (!this.world.isClientSide && !this.dead) {
            EntityPigZombie entitypigzombie = new EntityPigZombie(this.world);

            // CraftBukkit start
            if (CraftEventFactory.callPigZapEvent(this, entitylightning, entitypigzombie).isCancelled()) {
                return;
            }
            // CraftBukkit end

            entitypigzombie.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
            entitypigzombie.setPositionRotation(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
            entitypigzombie.setAI(this.hasAI());
            if (this.hasCustomName()) {
                entitypigzombie.setCustomName(this.getCustomName());
                entitypigzombie.setCustomNameVisible(this.getCustomNameVisible());
            }

            // CraftBukkit - added a reason for spawning this creature
            this.world.addEntity(entitypigzombie, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.LIGHTNING);
            this.die();
        }
    }

    public void e(float f, float f1) {
        super.e(f, f1);
        if (f > 5.0F) {
            Iterator iterator = this.b(EntityHuman.class).iterator();

            while (iterator.hasNext()) {
                EntityHuman entityhuman = (EntityHuman) iterator.next();

                entityhuman.b((Statistic) AchievementList.u);
            }
        }

    }

    public void g(float f, float f1) {
        Entity entity = this.bv().isEmpty() ? null : (Entity) this.bv().get(0);

        if (this.isVehicle() && this.cL()) {
            this.lastYaw = this.yaw = entity.yaw;
            this.pitch = entity.pitch * 0.5F;
            this.setYawPitch(this.yaw, this.pitch);
            this.aP = this.aN = this.yaw;
            this.P = 1.0F;
            this.aR = this.cl() * 0.1F;
            if (this.by()) {
                float f2 = (float) this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * 0.225F;

                if (this.by) {
                    if (this.bA++ > this.bB) {
                        this.by = false;
                    }

                    f2 += f2 * 1.15F * MathHelper.sin((float) this.bA / (float) this.bB * 3.1415927F);
                }

                this.l(f2);
                super.g(0.0F, 1.0F);
            } else {
                this.motX = 0.0D;
                this.motY = 0.0D;
                this.motZ = 0.0D;
            }

            this.aF = this.aG;
            double d0 = this.locX - this.lastX;
            double d1 = this.locZ - this.lastZ;
            float f3 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;

            if (f3 > 1.0F) {
                f3 = 1.0F;
            }

            this.aG += (f3 - this.aG) * 0.4F;
            this.aH += this.aG;
        } else {
            this.P = 0.5F;
            this.aR = 0.02F;
            super.g(f, f1);
        }
    }

    public boolean db() {
        if (this.by) {
            return false;
        } else {
            this.by = true;
            this.bA = 0;
            this.bB = this.getRandom().nextInt(841) + 140;
            return true;
        }
    }

    public EntityPig b(EntityAgeable entityageable) {
        return new EntityPig(this.world);
    }

    public boolean e(@Nullable ItemStack itemstack) {
        return itemstack != null && EntityPig.bx.contains(itemstack.getItem());
    }

    public EntityAgeable createChild(EntityAgeable entityageable) {
        return this.b(entityageable);
    }
}
