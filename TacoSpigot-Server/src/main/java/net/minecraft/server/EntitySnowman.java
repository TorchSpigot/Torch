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

    protected void r() {
        this.goalSelector.a(1, new PathfinderGoalArrowAttack(this, 1.25D, 20, 10.0F));
        this.goalSelector.a(2, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(this, EntityInsentient.class, 10, true, false, IMonster.d));
    }

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(4.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.20000000298023224D);
    }

    protected void i() {
        super.i();
        this.datawatcher.register(EntitySnowman.a, Byte.valueOf((byte) 0));
    }

    public void n() {
        super.n();
        if (!this.world.isClientSide) {
            int i = MathHelper.floor(this.locX);
            int j = MathHelper.floor(this.locY);
            int k = MathHelper.floor(this.locZ);

            if (this.ah()) {
                this.damageEntity(DamageSource.DROWN, 1.0F);
            }

            if (this.world.getBiome(new BlockPosition(i, 0, k)).a(new BlockPosition(i, j, k)) > 1.0F) {
                this.damageEntity(CraftEventFactory.MELTING, 1.0F); // CraftBukkit - DamageSource.BURN -> CraftEventFactory.MELTING
            }

            if (!this.world.getGameRules().getBoolean("mobGriefing")) {
                return;
            }

            for (int l = 0; l < 4; ++l) {
                i = MathHelper.floor(this.locX + (double) ((float) (l % 2 * 2 - 1) * 0.25F));
                j = MathHelper.floor(this.locY);
                k = MathHelper.floor(this.locZ + (double) ((float) (l / 2 % 2 * 2 - 1) * 0.25F));
                BlockPosition blockposition = new BlockPosition(i, j, k);

                if (this.world.getType(blockposition).getMaterial() == Material.AIR && this.world.getBiome(new BlockPosition(i, 0, k)).a(blockposition) < 0.8F && Blocks.SNOW_LAYER.canPlace(this.world, blockposition)) {
                    // CraftBukkit start
                    org.bukkit.block.BlockState blockState = this.world.getWorld().getBlockAt(i, j, k).getState();
                    blockState.setType(CraftMagicNumbers.getMaterial(Blocks.SNOW_LAYER));

                    EntityBlockFormEvent event = new EntityBlockFormEvent(this.getBukkitEntity(), blockState.getBlock(), blockState);
                    this.world.getServer().getPluginManager().callEvent(event);

                    if(!event.isCancelled()) {
                        blockState.update(true);
                    }
                    // CraftBukkit end
                }
            }
        }

    }

    @Nullable
    protected MinecraftKey J() {
        return LootTables.A;
    }

    public void a(EntityLiving entityliving, float f) {
        EntitySnowball entitysnowball = new EntitySnowball(this.world, this);
        double d0 = entityliving.locY + (double) entityliving.getHeadHeight() - 1.100000023841858D;
        double d1 = entityliving.locX - this.locX;
        double d2 = d0 - entitysnowball.locY;
        double d3 = entityliving.locZ - this.locZ;
        float f1 = MathHelper.sqrt(d1 * d1 + d3 * d3) * 0.2F;

        entitysnowball.shoot(d1, d2 + (double) f1, d3, 1.6F, 12.0F);
        this.a(SoundEffects.fL, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.world.addEntity(entitysnowball);
    }

    public float getHeadHeight() {
        return 1.7F;
    }

    protected boolean a(EntityHuman entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
        if (itemstack != null && itemstack.getItem() == Items.SHEARS && !this.o() && !this.world.isClientSide) {
            this.a(true);
            itemstack.damage(1, entityhuman);
        }

        return super.a(entityhuman, enumhand, itemstack);
    }

    public boolean o() {
        return (((Byte) this.datawatcher.get(EntitySnowman.a)).byteValue() & 16) != 0;
    }

    public void a(boolean flag) {
        byte b0 = ((Byte) this.datawatcher.get(EntitySnowman.a)).byteValue();

        if (flag) {
            this.datawatcher.set(EntitySnowman.a, Byte.valueOf((byte) (b0 | 16)));
        } else {
            this.datawatcher.set(EntitySnowman.a, Byte.valueOf((byte) (b0 & -17)));
        }

    }

    @Nullable
    protected SoundEffect G() {
        return SoundEffects.fI;
    }

    @Nullable
    protected SoundEffect bS() {
        return SoundEffects.fK;
    }

    @Nullable
    protected SoundEffect bT() {
        return SoundEffects.fJ;
    }
}
