package net.minecraft.server;

import javax.annotation.Nullable;
// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.event.block.EntityBlockFormEvent;
// CraftBukkit end

public class EntitySnowman extends EntityGolem implements IRangedEntity {

    private static final DataWatcherObject<Byte> a = DataWatcher.a(EntitySnowman.class, DataWatcherRegistry.a);

    public EntitySnowman(World world) {
        super(world);
        this.setSize(0.7F, 1.9F);
    }

    public static void a(DataConverterManager dataconvertermanager) {
        EntityInsentient.a(dataconvertermanager, EntitySnowman.class);
    }

    @Override
    protected void r() {
        this.goalSelector.a(1, new PathfinderGoalArrowAttack(this, 1.25D, 20, 10.0F));
        this.goalSelector.a(2, new PathfinderGoalRandomStrollLand(this, 1.0D, 1.0000001E-5F));
        this.goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(this, EntityInsentient.class, 10, true, false, IMonster.d));
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(4.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.20000000298023224D);
    }

    @Override
    protected void i() {
        super.i();
        this.datawatcher.register(EntitySnowman.a, Byte.valueOf((byte) 16));
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setBoolean("Pumpkin", this.hasPumpkin());
    }

    @Override
    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.hasKey("Pumpkin")) {
            this.setHasPumpkin(nbttagcompound.getBoolean("Pumpkin"));
        }

    }

    @Override
    public void n() {
        super.n();
        int i = MathHelper.floor(this.locX);
        int j = MathHelper.floor(this.locY);
        int k = MathHelper.floor(this.locZ);

        if (this.ai()) {
            this.damageEntity(DamageSource.DROWN, 1.0F);
        }

        if (this.world.getBiome(new BlockPosition(i, 0, k)).a(new BlockPosition(i, j, k)) > 1.0F) {
            this.damageEntity(CraftEventFactory.MELTING, 1.0F); // CraftBukkit - DamageSource.BURN -> CraftEventFactory.MELTING
        }

        if (!this.world.getGameRules().getBoolean("mobGriefing")) {
            return;
        }

        for (int l = 0; l < 4; ++l) {
            i = MathHelper.floor(this.locX + (l % 2 * 2 - 1) * 0.25F);
            j = MathHelper.floor(this.locY);
            k = MathHelper.floor(this.locZ + (l / 2 % 2 * 2 - 1) * 0.25F);
            BlockPosition blockposition = new BlockPosition(i, j, k);

            if (this.world.getType(blockposition).getMaterial() == Material.AIR && this.world.getBiome(blockposition).a(blockposition) < 0.8F && Blocks.SNOW_LAYER.canPlace(this.world, blockposition)) {
                org.bukkit.craftbukkit.event.CraftEventFactory.handleBlockFormEvent(this.world, blockposition, Blocks.SNOW_LAYER, this); // CraftBukkit
            }
        }

    }

    @Override
    @Nullable
    protected MinecraftKey J() {
        return LootTables.B;
    }

    @Override
    public void a(EntityLiving entityliving, float f) {
        EntitySnowball entitysnowball = new EntitySnowball(this.world, this);
        double d0 = entityliving.locY + entityliving.getHeadHeight() - 1.100000023841858D;
        double d1 = entityliving.locX - this.locX;
        double d2 = d0 - entitysnowball.locY;
        double d3 = entityliving.locZ - this.locZ;
        float f1 = MathHelper.sqrt(d1 * d1 + d3 * d3) * 0.2F;

        entitysnowball.shoot(d1, d2 + f1, d3, 1.6F, 12.0F);
        this.a(SoundEffects.gs, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.world.addEntity(entitysnowball);
    }

    @Override
    public float getHeadHeight() {
        return 1.7F;
    }

    @Override
    protected boolean a(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.b(enumhand);

        if (itemstack.getItem() == Items.SHEARS && this.hasPumpkin()) {
            this.setHasPumpkin(false);
            itemstack.damage(1, entityhuman);
        }

        return super.a(entityhuman, enumhand);
    }

    public boolean hasPumpkin() {
        return (this.datawatcher.get(EntitySnowman.a).byteValue() & 16) != 0;
    }

    public void setHasPumpkin(boolean flag) {
        byte b0 = this.datawatcher.get(EntitySnowman.a).byteValue();

        if (flag) {
            this.datawatcher.set(EntitySnowman.a, Byte.valueOf((byte) (b0 | 16)));
        } else {
            this.datawatcher.set(EntitySnowman.a, Byte.valueOf((byte) (b0 & -17)));
        }

    }

    @Override
    @Nullable
    protected SoundEffect G() {
        return SoundEffects.gp;
    }

    @Override
    @Nullable
    protected SoundEffect bW() {
        return SoundEffects.gr;
    }

    @Override
    @Nullable
    protected SoundEffect bX() {
        return SoundEffects.gq;
    }
}
