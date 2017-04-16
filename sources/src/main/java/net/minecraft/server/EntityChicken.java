package net.minecraft.server;

import com.google.common.collect.Sets;
import com.koloboke.collect.set.hash.HashObjSets;

import java.util.Set;
import javax.annotation.Nullable;

public class EntityChicken extends EntityAnimal {

    private static final Set<Item> bE = HashObjSets.newMutableSet(new Item[] { Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS});
    public float bw;
    public float bx;
    public float by;
    public float bA;
    public float bB = 1.0F;
    public int bC;
    public boolean bD;

    public EntityChicken(World world) {
        super(world);
        this.setSize(0.4F, 0.7F);
        this.bC = this.random.nextInt(6000) + 6000;
        this.a(PathType.WATER, 0.0F);
    }

    @Override
	protected void r() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalPanic(this, 1.4D));
        this.goalSelector.a(2, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.a(3, new PathfinderGoalTempt(this, 1.0D, false, EntityChicken.bE));
        this.goalSelector.a(4, new PathfinderGoalFollowParent(this, 1.1D));
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
    }

    @Override
	public float getHeadHeight() {
        return this.length;
    }

    @Override
	protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(4.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.25D);
    }

    @Override
	public void n() {
        // CraftBukkit start
        if (this.isChickenJockey()) {
            this.persistent = !this.isTypeNotPersistent();
        }
        // CraftBukkit end
        super.n();
        this.bA = this.bw;
        this.by = this.bx;
        this.bx = (float) (this.bx + (this.onGround ? -1 : 4) * 0.3D);
        this.bx = MathHelper.a(this.bx, 0.0F, 1.0F);
        if (!this.onGround && this.bB < 1.0F) {
            this.bB = 1.0F;
        }

        this.bB = (float) (this.bB * 0.9D);
        if (!this.onGround && this.motY < 0.0D) {
            this.motY *= 0.6D;
        }

        this.bw += this.bB * 2.0F;
        if (!this.world.isClientSide && !this.isBaby() && !this.isChickenJockey() && --this.bC <= 0) {
            this.a(SoundEffects.ac, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            this.forceDrops = true; // CraftBukkit
            this.a(Items.EGG, 1);
            this.forceDrops = false; // CraftBukkit
            this.bC = this.random.nextInt(6000) + 6000;
        }

    }

    @Override
	public void e(float f, float f1) {}

    @Override
	protected SoundEffect G() {
        return SoundEffects.aa;
    }

    @Override
	protected SoundEffect bW() {
        return SoundEffects.ad;
    }

    @Override
	protected SoundEffect bX() {
        return SoundEffects.ab;
    }

    @Override
	protected void a(BlockPosition blockposition, Block block) {
        this.a(SoundEffects.ae, 0.15F, 1.0F);
    }

    @Override
	@Nullable
    protected MinecraftKey J() {
        return LootTables.D;
    }

    public EntityChicken b(EntityAgeable entityageable) {
        return new EntityChicken(this.world);
    }

    @Override
	public boolean e(ItemStack itemstack) {
        return EntityChicken.bE.contains(itemstack.getItem());
    }

    @Override
	protected int getExpValue(EntityHuman entityhuman) {
        return this.isChickenJockey() ? 10 : super.getExpValue(entityhuman);
    }

    public static void a(DataConverterManager dataconvertermanager) {
        EntityInsentient.a(dataconvertermanager, EntityChicken.class);
    }

    @Override
	public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.bD = nbttagcompound.getBoolean("IsChickenJockey");
        if (nbttagcompound.hasKey("EggLayTime")) {
            this.bC = nbttagcompound.getInt("EggLayTime");
        }

    }

    @Override
	public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setBoolean("IsChickenJockey", this.bD);
        nbttagcompound.setInt("EggLayTime", this.bC);
    }

    @Override
	protected boolean isTypeNotPersistent() {
        return this.isChickenJockey() && !this.isVehicle();
    }

    @Override
	public void k(Entity entity) {
        super.k(entity);
        float f = MathHelper.sin(this.aN * 0.017453292F);
        float f1 = MathHelper.cos(this.aN * 0.017453292F);
        float f2 = 0.1F;
        float f3 = 0.0F;

        entity.setPosition(this.locX + 0.1F * f, this.locY + this.length * 0.5F + entity.ax() + 0.0D, this.locZ - 0.1F * f1);
        if (entity instanceof EntityLiving) {
            ((EntityLiving) entity).aN = this.aN;
        }

    }

    public boolean isChickenJockey() {
        return this.bD;
    }

    public void p(boolean flag) {
        this.bD = flag;
    }

    @Override
	public EntityAgeable createChild(EntityAgeable entityageable) {
        return this.b(entityageable);
    }
}
