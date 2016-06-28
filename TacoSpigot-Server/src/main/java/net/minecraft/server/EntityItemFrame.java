package net.minecraft.server;

import java.util.UUID;
import org.apache.commons.codec.Charsets;
import com.google.common.base.Optional;
import javax.annotation.Nullable;

public class EntityItemFrame extends EntityHanging {

    private static final DataWatcherObject<Optional<ItemStack>> c = DataWatcher.a(EntityItemFrame.class, DataWatcherRegistry.f);
    private static final DataWatcherObject<Integer> d = DataWatcher.a(EntityItemFrame.class, DataWatcherRegistry.b);
    private float e = 1.0F;

    public EntityItemFrame(World world) {
        super(world);
    }

    public EntityItemFrame(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        super(world, blockposition);
        this.setDirection(enumdirection);
    }

    protected void i() {
        this.getDataWatcher().register(EntityItemFrame.c, Optional.absent());
        this.getDataWatcher().register(EntityItemFrame.d, Integer.valueOf(0));
    }

    public float aA() {
        return 0.0F;
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else if (!damagesource.isExplosion() && this.getItem() != null) {
            if (!this.world.isClientSide) {
                // CraftBukkit start - fire EntityDamageEvent
                if (org.bukkit.craftbukkit.event.CraftEventFactory.handleNonLivingEntityDamageEvent(this, damagesource, f, false) || this.dead) {
                    return true;
                }
                // CraftBukkit end
                this.b(damagesource.getEntity(), false);
                this.a(SoundEffects.cS, 1.0F, 1.0F);
                this.setItem((ItemStack) null);
            }

            return true;
        } else {
            return super.damageEntity(damagesource, f);
        }
    }

    public int getWidth() {
        return 12;
    }

    public int getHeight() {
        return 12;
    }

    public void a(@Nullable Entity entity) {
        this.a(SoundEffects.cQ, 1.0F, 1.0F);
        this.b(entity, true);
    }

    public void o() {
        this.a(SoundEffects.cR, 1.0F, 1.0F);
    }

    public void b(@Nullable Entity entity, boolean flag) {
        if (this.world.getGameRules().getBoolean("doEntityDrops")) {
            ItemStack itemstack = this.getItem();

            if (entity instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entity;

                if (entityhuman.abilities.canInstantlyBuild) {
                    this.b(itemstack);
                    return;
                }
            }

            if (flag) {
                this.a(new ItemStack(Items.ITEM_FRAME), 0.0F);
            }

            if (itemstack != null && this.random.nextFloat() < this.e) {
                itemstack = itemstack.cloneItemStack();
                this.b(itemstack);
                this.a(itemstack, 0.0F);
            }

        }
    }

    private void b(ItemStack itemstack) {
        if (itemstack != null) {
            if (itemstack.getItem() == Items.FILLED_MAP) {
                WorldMap worldmap = ((ItemWorldMap) itemstack.getItem()).getSavedMap(itemstack, this.world);

                worldmap.decorations.remove(UUID.nameUUIDFromBytes(("frame-" + this.getId()).getBytes(Charsets.US_ASCII))); // Spigot
            }

            itemstack.a((EntityItemFrame) null);
        }
    }

    @Nullable
    public ItemStack getItem() {
        return (ItemStack) ((Optional) this.getDataWatcher().get(EntityItemFrame.c)).orNull();
    }

    public void setItem(@Nullable ItemStack itemstack) {
        this.setItem(itemstack, true);
    }

    private void setItem(@Nullable ItemStack itemstack, boolean flag) {
        if (itemstack != null) {
            itemstack = itemstack.cloneItemStack();
            itemstack.count = 1;
            itemstack.a(this);
        }

        this.getDataWatcher().set(EntityItemFrame.c, Optional.fromNullable(itemstack));
        this.getDataWatcher().markDirty(EntityItemFrame.c);
        if (itemstack != null) {
            this.a(SoundEffects.cP, 1.0F, 1.0F);
        }

        if (flag && this.blockPosition != null) {
            this.world.updateAdjacentComparators(this.blockPosition, Blocks.AIR);
        }

    }

    public void a(DataWatcherObject<?> datawatcherobject) {
        if (datawatcherobject.equals(EntityItemFrame.c)) {
            ItemStack itemstack = this.getItem();

            if (itemstack != null && itemstack.z() != this) {
                itemstack.a(this);
            }
        }

    }

    public int getRotation() {
        return ((Integer) this.getDataWatcher().get(EntityItemFrame.d)).intValue();
    }

    public void setRotation(int i) {
        this.setRotation(i, true);
    }

    private void setRotation(int i, boolean flag) {
        this.getDataWatcher().set(EntityItemFrame.d, Integer.valueOf(i % 8));
        if (flag && this.blockPosition != null) {
            this.world.updateAdjacentComparators(this.blockPosition, Blocks.AIR);
        }

    }

    public void b(NBTTagCompound nbttagcompound) {
        if (this.getItem() != null) {
            nbttagcompound.set("Item", this.getItem().save(new NBTTagCompound()));
            nbttagcompound.setByte("ItemRotation", (byte) this.getRotation());
            nbttagcompound.setFloat("ItemDropChance", this.e);
        }

        super.b(nbttagcompound);
    }

    public void a(NBTTagCompound nbttagcompound) {
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Item");

        if (nbttagcompound1 != null && !nbttagcompound1.isEmpty()) {
            this.setItem(ItemStack.createStack(nbttagcompound1), false);
            this.setRotation(nbttagcompound.getByte("ItemRotation"), false);
            if (nbttagcompound.hasKeyOfType("ItemDropChance", 99)) {
                this.e = nbttagcompound.getFloat("ItemDropChance");
            }
        }

        super.a(nbttagcompound);
    }

    public boolean a(EntityHuman entityhuman, @Nullable ItemStack itemstack, EnumHand enumhand) {
        if (this.getItem() == null) {
            if (itemstack != null && !this.world.isClientSide) {
                this.setItem(itemstack);
                if (!entityhuman.abilities.canInstantlyBuild) {
                    --itemstack.count;
                }
            }
        } else if (!this.world.isClientSide) {
            this.a(SoundEffects.cT, 1.0F, 1.0F);
            this.setRotation(this.getRotation() + 1);
        }

        return true;
    }

    public int t() {
        return this.getItem() == null ? 0 : this.getRotation() % 8 + 1;
    }
}
