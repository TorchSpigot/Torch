package net.minecraft.server;

import javax.annotation.Nullable;
import org.bukkit.event.entity.EntityCombustEvent; // CraftBukkit

public class ItemBow extends Item {

    public ItemBow() {
        this.maxStackSize = 1;
        this.setMaxDurability(384);
        this.a(CreativeModeTab.j);
        this.a(new MinecraftKey("pull"), new IDynamicTexture() {
        });
        this.a(new MinecraftKey("pulling"), new IDynamicTexture() {
        });
    }

    private ItemStack a(EntityHuman entityhuman) {
        if (this.h_(entityhuman.b(EnumHand.OFF_HAND))) {
            return entityhuman.b(EnumHand.OFF_HAND);
        } else if (this.h_(entityhuman.b(EnumHand.MAIN_HAND))) {
            return entityhuman.b(EnumHand.MAIN_HAND);
        } else {
            for (int i = 0; i < entityhuman.inventory.getSize(); ++i) {
                ItemStack itemstack = entityhuman.inventory.getItem(i);

                if (this.h_(itemstack)) {
                    return itemstack;
                }
            }

            return null;
        }
    }

    protected boolean h_(@Nullable ItemStack itemstack) {
        return itemstack != null && itemstack.getItem() instanceof ItemArrow;
    }

    public void a(ItemStack itemstack, World world, EntityLiving entityliving, int i) {
        if (entityliving instanceof EntityHuman) {
            EntityHuman entityhuman = (EntityHuman) entityliving;
            boolean flag = entityhuman.abilities.canInstantlyBuild || EnchantmentManager.getEnchantmentLevel(Enchantments.ARROW_INFINITE, itemstack) > 0;
            ItemStack itemstack1 = this.a(entityhuman);

            if (itemstack1 != null || flag) {
                if (itemstack1 == null) {
                    itemstack1 = new ItemStack(Items.ARROW);
                }

                int j = this.e(itemstack) - i;
                float f = b(j);

                if ((double) f >= 0.1D) {
                    boolean flag1 = flag && itemstack1.getItem() == Items.ARROW;

                    if (!world.isClientSide) {
                        ItemArrow itemarrow = (ItemArrow) ((ItemArrow) (itemstack1.getItem() instanceof ItemArrow ? itemstack1.getItem() : Items.ARROW));
                        EntityArrow entityarrow = itemarrow.a(world, itemstack1, entityhuman);

                        entityarrow.a(entityhuman, entityhuman.pitch, entityhuman.yaw, 0.0F, f * 3.0F, 1.0F);
                        if (f == 1.0F) {
                            entityarrow.setCritical(true);
                        }

                        int k = EnchantmentManager.getEnchantmentLevel(Enchantments.ARROW_DAMAGE, itemstack);

                        if (k > 0) {
                            entityarrow.c(entityarrow.k() + (double) k * 0.5D + 0.5D);
                        }

                        int l = EnchantmentManager.getEnchantmentLevel(Enchantments.ARROW_KNOCKBACK, itemstack);

                        if (l > 0) {
                            entityarrow.setKnockbackStrength(l);
                        }

                        if (EnchantmentManager.getEnchantmentLevel(Enchantments.ARROW_FIRE, itemstack) > 0) {
                        // CraftBukkit start - call EntityCombustEvent
                        EntityCombustEvent event = new EntityCombustEvent(entityarrow.getBukkitEntity(), 100);
                        entityarrow.world.getServer().getPluginManager().callEvent(event);

                        if (!event.isCancelled()) {
                            entityarrow.setOnFire(event.getDuration());
                        }
                        // CraftBukkit end
                        }
                        // CraftBukkit start
                        org.bukkit.event.entity.EntityShootBowEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callEntityShootBowEvent(entityhuman, itemstack, entityarrow, f);
                        if (event.isCancelled()) {
                            event.getProjectile().remove();
                            return;
                        }

                        itemstack.damage(1, entityhuman);
                        if (flag1) {
                            entityarrow.fromPlayer = EntityArrow.PickupStatus.CREATIVE_ONLY;
                        }

                        if (event.getProjectile() == entityarrow.getBukkitEntity()) {
                            if (!world.addEntity(entityarrow)) {
                                if (entityhuman instanceof EntityPlayer) {
                                    ((EntityPlayer) entityhuman).getBukkitEntity().updateInventory();
                                }
                                return;
                            }
                        }
                        // CraftBukkit end
                    }

                    world.a((EntityHuman) null, entityhuman.locX, entityhuman.locY, entityhuman.locZ, SoundEffects.v, SoundCategory.NEUTRAL, 1.0F, 1.0F / (ItemBow.j.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!flag1) {
                        --itemstack1.count;
                        if (itemstack1.count == 0) {
                            entityhuman.inventory.d(itemstack1);
                        }
                    }

                    entityhuman.b(StatisticList.b((Item) this));
                }
            }
        }
    }

    public static float b(int i) {
        float f = (float) i / 20.0F;

        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public int e(ItemStack itemstack) {
        return 72000;
    }

    public EnumAnimation f(ItemStack itemstack) {
        return EnumAnimation.BOW;
    }

    public InteractionResultWrapper<ItemStack> a(ItemStack itemstack, World world, EntityHuman entityhuman, EnumHand enumhand) {
        boolean flag = this.a(entityhuman) != null;

        if (!entityhuman.abilities.canInstantlyBuild && !flag) {
            return !flag ? new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack) : new InteractionResultWrapper(EnumInteractionResult.PASS, itemstack);
        } else {
            entityhuman.c(enumhand);
            return new InteractionResultWrapper(EnumInteractionResult.SUCCESS, itemstack);
        }
    }

    public int c() {
        return 1;
    }
}
