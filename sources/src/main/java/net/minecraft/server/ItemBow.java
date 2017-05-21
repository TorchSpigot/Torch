package net.minecraft.server;

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
        if (this.d(entityhuman.b(EnumHand.OFF_HAND))) {
            return entityhuman.b(EnumHand.OFF_HAND);
        } else if (this.d(entityhuman.b(EnumHand.MAIN_HAND))) {
            return entityhuman.b(EnumHand.MAIN_HAND);
        } else {
            for (int i = 0; i < entityhuman.inventory.getSize(); ++i) {
                ItemStack itemstack = entityhuman.inventory.getItem(i);

                if (this.d(itemstack)) {
                    return itemstack;
                }
            }

            return ItemStack.a;
        }
    }

    protected boolean d(ItemStack itemstack) {
        return itemstack.getItem() instanceof ItemArrow;
    }

    @Override
	public void a(ItemStack itemstack, World world, EntityLiving entityliving, int i) {
        if (entityliving instanceof EntityHuman) {
            EntityHuman entityhuman = (EntityHuman) entityliving;
            boolean flag = entityhuman.abilities.canInstantlyBuild || EnchantmentManager.getEnchantmentLevel(Enchantments.ARROW_INFINITE, itemstack) > 0;
            ItemStack itemstack1 = this.a(entityhuman);

            if (!itemstack1.isEmpty() || flag) {
                if (itemstack1.isEmpty()) {
                    itemstack1 = new ItemStack(Items.ARROW);
                }

                int j = this.e(itemstack) - i;
                float f = b(j);

                if (f >= 0.1D) {
                    boolean flag1 = flag && itemstack1.getItem() == Items.ARROW;

                    ItemArrow itemarrow = ((ItemArrow) (itemstack1.getItem() instanceof ItemArrow ? itemstack1.getItem() : Items.ARROW));
                    EntityArrow entityarrow = itemarrow.a(world, itemstack1, entityhuman);

                    entityarrow.a(entityhuman, entityhuman.pitch, entityhuman.yaw, 0.0F, f * 3.0F, 1.0F);
                    if (f == 1.0F) {
                        entityarrow.setCritical(true);
                    }

                    int k = EnchantmentManager.getEnchantmentLevel(Enchantments.ARROW_DAMAGE, itemstack);

                    if (k > 0) {
                        entityarrow.c(entityarrow.k() + k * 0.5D + 0.5D);
                    }

                    int l = EnchantmentManager.getEnchantmentLevel(Enchantments.ARROW_KNOCKBACK, itemstack);

                    if (l > 0) {
                        entityarrow.setKnockbackStrength(l);
                    }

                    if (EnchantmentManager.getEnchantmentLevel(Enchantments.ARROW_FIRE, itemstack) > 0 && !entityarrow.isInWater()) {
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
                    if (flag1 || entityhuman.abilities.canInstantlyBuild && (itemstack1.getItem() == Items.SPECTRAL_ARROW || itemstack1.getItem() == Items.TIPPED_ARROW)) {
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

                    world.a((EntityHuman) null, entityhuman.locX, entityhuman.locY, entityhuman.locZ, SoundEffects.w, SoundCategory.PLAYERS, 1.0F, 1.0F / (ItemBow.j.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!flag1 && !entityhuman.abilities.canInstantlyBuild) {
                        itemstack1.subtract(1);
                        if (itemstack1.isEmpty()) {
                            entityhuman.inventory.d(itemstack1);
                        }
                    }

                    entityhuman.b(StatisticList.b(this));
                }
            }
        }
    }

    public static float b(int i) {
        float f = i / 20.0F;

        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    @Override
	public int e(ItemStack itemstack) {
        return 72000;
    }

    @Override
	public EnumAnimation f(ItemStack itemstack) {
        return EnumAnimation.BOW;
    }

    @Override
	public InteractionResultWrapper<ItemStack> a(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.b(enumhand);
        boolean flag = !this.a(entityhuman).isEmpty();

        if (!entityhuman.abilities.canInstantlyBuild && !flag) {
            return flag ? new InteractionResultWrapper(EnumInteractionResult.PASS, itemstack) : new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack);
        } else {
            entityhuman.c(enumhand);
            return new InteractionResultWrapper(EnumInteractionResult.SUCCESS, itemstack);
        }
    }

    @Override
	public int c() {
        return 1;
    }
}
