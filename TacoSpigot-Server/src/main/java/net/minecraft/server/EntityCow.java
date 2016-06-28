package net.minecraft.server;

import javax.annotation.Nullable;
// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
// CraftBukkit end

public class EntityCow extends EntityAnimal {

    public EntityCow(World world) {
        super(world);
        this.setSize(0.9F, 1.4F);
    }

    protected void r() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalPanic(this, 2.0D));
        this.goalSelector.a(2, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.a(3, new PathfinderGoalTempt(this, 1.25D, Items.WHEAT, false));
        this.goalSelector.a(4, new PathfinderGoalFollowParent(this, 1.25D));
        this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
    }

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(10.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.20000000298023224D);
    }

    protected SoundEffect G() {
        return SoundEffects.am;
    }

    protected SoundEffect bS() {
        return SoundEffects.ao;
    }

    protected SoundEffect bT() {
        return SoundEffects.an;
    }

    protected void a(BlockPosition blockposition, Block block) {
        this.a(SoundEffects.aq, 0.15F, 1.0F);
    }

    protected float ce() {
        return 0.4F;
    }

    @Nullable
    protected MinecraftKey J() {
        return LootTables.H;
    }

    public boolean a(EntityHuman entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
        if (itemstack != null && itemstack.getItem() == Items.BUCKET && !entityhuman.abilities.canInstantlyBuild && !this.isBaby()) {
            // CraftBukkit start - Got milk?
            org.bukkit.Location loc = this.getBukkitEntity().getLocation();
            org.bukkit.event.player.PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent(entityhuman, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), null, itemstack, Items.MILK_BUCKET);

            if (event.isCancelled()) {
                return false;
            }

            ItemStack result = CraftItemStack.asNMSCopy(event.getItemStack());
            entityhuman.a(SoundEffects.ap, 1.0F, 1.0F);
            if (--itemstack.count <= 0) {
                entityhuman.a(enumhand, result);
            } else if (!entityhuman.inventory.pickup(result)) {
                entityhuman.drop(result, false);
            }
            // CraftBukkit end

            return true;
        } else {
            return super.a(entityhuman, enumhand, itemstack);
        }
    }

    public EntityCow b(EntityAgeable entityageable) {
        return new EntityCow(this.world);
    }

    public float getHeadHeight() {
        return this.isBaby() ? this.length : 1.3F;
    }

    public EntityAgeable createChild(EntityAgeable entityageable) {
        return this.b(entityageable);
    }
}
