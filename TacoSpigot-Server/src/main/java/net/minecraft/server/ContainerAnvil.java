package net.minecraft.server;

import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

 // CraftBukkit start
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.PrepareAnvilEvent;
// CraftBukkit end

public class ContainerAnvil extends Container {

    private static final Logger f = LogManager.getLogger();
    private IInventory g = new InventoryCraftResult();
    private IInventory h = new InventorySubcontainer("Repair", true, 2) {
        public void update() {
            super.update();
            ContainerAnvil.this.a((IInventory) this);
        }
    };
    private World i;
    private BlockPosition j;
    public int a;
    private int k;
    private String l;
    private final EntityHuman m;
    // CraftBukkit start
    private CraftInventoryView bukkitEntity = null;
    private PlayerInventory player;
    // CraftBukkit end

    public ContainerAnvil(PlayerInventory playerinventory, final World world, final BlockPosition blockposition, EntityHuman entityhuman) {
        this.player = playerinventory; // CraftBukkit
        this.j = blockposition;
        this.i = world;
        this.m = entityhuman;
        this.a(new Slot(this.h, 0, 27, 47));
        this.a(new Slot(this.h, 1, 76, 47));
        this.a(new Slot(this.g, 2, 134, 47) {
            public boolean isAllowed(@Nullable ItemStack itemstack) {
                return false;
            }

            public boolean isAllowed(EntityHuman entityhuman) {
                return (entityhuman.abilities.canInstantlyBuild || entityhuman.expLevel >= ContainerAnvil.this.a) && ContainerAnvil.this.a > 0 && this.hasItem();
            }

            public void a(EntityHuman entityhuman, ItemStack itemstack) {
                if (!entityhuman.abilities.canInstantlyBuild) {
                    entityhuman.levelDown(-ContainerAnvil.this.a);
                }

                ContainerAnvil.this.h.setItem(0, (ItemStack) null);
                if (ContainerAnvil.this.k > 0) {
                    ItemStack itemstack1 = ContainerAnvil.this.h.getItem(1);

                    if (itemstack1 != null && itemstack1.count > ContainerAnvil.this.k) {
                        itemstack1.count -= ContainerAnvil.this.k;
                        ContainerAnvil.this.h.setItem(1, itemstack1);
                    } else {
                        ContainerAnvil.this.h.setItem(1, (ItemStack) null);
                    }
                } else {
                    ContainerAnvil.this.h.setItem(1, (ItemStack) null);
                }

                ContainerAnvil.this.a = 0;
                IBlockData iblockdata = world.getType(blockposition);

                if (!entityhuman.abilities.canInstantlyBuild && !world.isClientSide && iblockdata.getBlock() == Blocks.ANVIL && entityhuman.getRandom().nextFloat() < 0.12F) {
                    int i = ((Integer) iblockdata.get(BlockAnvil.DAMAGE)).intValue();

                    ++i;
                    if (i > 2) {
                        world.setAir(blockposition);
                        world.triggerEffect(1029, blockposition, 0);
                    } else {
                        world.setTypeAndData(blockposition, iblockdata.set(BlockAnvil.DAMAGE, Integer.valueOf(i)), 2);
                        world.triggerEffect(1030, blockposition, 0);
                    }
                } else if (!world.isClientSide) {
                    world.triggerEffect(1030, blockposition, 0);
                }

            }
        });

        int i;

        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.a(new Slot(playerinventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.a(new Slot(playerinventory, i, 8 + i * 18, 142));
        }

    }

    public void a(IInventory iinventory) {
        super.a(iinventory);
        if (iinventory == this.h) {
            this.e();
        }

    }

    public void e() {
        ItemStack itemstack = this.h.getItem(0);

        this.a = 1;
        int i = 0;
        byte b0 = 0;
        byte b1 = 0;

        if (itemstack == null) {
            org.bukkit.craftbukkit.event.CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), null); // CraftBukkit
            this.a = 0;
        } else {
            ItemStack itemstack1 = itemstack.cloneItemStack();
            ItemStack itemstack2 = this.h.getItem(1);
            Map map = EnchantmentManager.a(itemstack1);
            int j = b0 + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());

            this.k = 0;
            if (itemstack2 != null) {
                boolean flag = itemstack2.getItem() == Items.ENCHANTED_BOOK && !Items.ENCHANTED_BOOK.h(itemstack2).isEmpty();
                int k;
                int l;
                int i1;

                if (itemstack1.e() && itemstack1.getItem().a(itemstack, itemstack2)) {
                    k = Math.min(itemstack1.h(), itemstack1.j() / 4);
                    if (k <= 0) {
                        org.bukkit.craftbukkit.event.CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), null); // CraftBukkit
                        this.a = 0;
                        return;
                    }

                    for (l = 0; k > 0 && l < itemstack2.count; ++l) {
                        i1 = itemstack1.h() - k;
                        itemstack1.setData(i1);
                        ++i;
                        k = Math.min(itemstack1.h(), itemstack1.j() / 4);
                    }

                    this.k = l;
                } else {
                    if (!flag && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.e())) {
                        org.bukkit.craftbukkit.event.CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), null); // CraftBukkit
                        this.a = 0;
                        return;
                    }

                    int j1;
                    int k1;

                    if (itemstack1.e() && !flag) {
                        k = itemstack.j() - itemstack.h();
                        l = itemstack2.j() - itemstack2.h();
                        i1 = l + itemstack1.j() * 12 / 100;
                        j1 = k + i1;
                        k1 = itemstack1.j() - j1;
                        if (k1 < 0) {
                            k1 = 0;
                        }

                        if (k1 < itemstack1.getData()) {
                            itemstack1.setData(k1);
                            i += 2;
                        }
                    }

                    Map map1 = EnchantmentManager.a(itemstack2);
                    Iterator iterator = map1.keySet().iterator();

                    while (iterator.hasNext()) {
                        Enchantment enchantment = (Enchantment) iterator.next();

                        if (enchantment != null) {
                            j1 = map.containsKey(enchantment) ? ((Integer) map.get(enchantment)).intValue() : 0;
                            k1 = ((Integer) map1.get(enchantment)).intValue();
                            k1 = j1 == k1 ? k1 + 1 : Math.max(k1, j1);
                            boolean flag1 = enchantment.canEnchant(itemstack);

                            if (this.m.abilities.canInstantlyBuild || itemstack.getItem() == Items.ENCHANTED_BOOK) {
                                flag1 = true;
                            }

                            Iterator iterator1 = map.keySet().iterator();

                            while (iterator1.hasNext()) {
                                Enchantment enchantment1 = (Enchantment) iterator1.next();

                                if (enchantment1 != enchantment && !enchantment.a(enchantment1)) {
                                    flag1 = false;
                                    ++i;
                                }
                            }

                            if (flag1) {
                                if (k1 > enchantment.getMaxLevel()) {
                                    k1 = enchantment.getMaxLevel();
                                }

                                map.put(enchantment, Integer.valueOf(k1));
                                int l1 = 0;

                                switch (ContainerAnvil.SyntheticClass_1.a[enchantment.c().ordinal()]) {
                                case 1:
                                    l1 = 1;
                                    break;

                                case 2:
                                    l1 = 2;
                                    break;

                                case 3:
                                    l1 = 4;
                                    break;

                                case 4:
                                    l1 = 8;
                                }

                                if (flag) {
                                    l1 = Math.max(1, l1 / 2);
                                }

                                i += l1 * k1;
                            }
                        }
                    }
                }
            }

            if (StringUtils.isBlank(this.l)) {
                if (itemstack.hasName()) {
                    b1 = 1;
                    i += b1;
                    itemstack1.r();
                }
            } else if (!this.l.equals(itemstack.getName())) {
                b1 = 1;
                i += b1;
                itemstack1.c(this.l);
            }

            this.a = j + i;
            if (i <= 0) {
                itemstack1 = null;
            }

            if (b1 == i && b1 > 0 && this.a >= 40) {
                this.a = 39;
            }

            if (this.a >= 40 && !this.m.abilities.canInstantlyBuild) {
                itemstack1 = null;
            }

            if (itemstack1 != null) {
                int i2 = itemstack1.getRepairCost();

                if (itemstack2 != null && i2 < itemstack2.getRepairCost()) {
                    i2 = itemstack2.getRepairCost();
                }

                if (b1 != i || b1 == 0) {
                    i2 = i2 * 2 + 1;
                }

                itemstack1.setRepairCost(i2);
                EnchantmentManager.a(map, itemstack1);
            }

            org.bukkit.craftbukkit.event.CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), itemstack1); // CraftBukkit
            this.b();
        }
    }

    public void addSlotListener(ICrafting icrafting) {
        super.addSlotListener(icrafting);
        icrafting.setContainerData(this, 0, this.a);
    }

    public void b(EntityHuman entityhuman) {
        super.b(entityhuman);
        if (!this.i.isClientSide) {
            for (int i = 0; i < this.h.getSize(); ++i) {
                ItemStack itemstack = this.h.splitWithoutUpdate(i);

                if (itemstack != null) {
                    entityhuman.drop(itemstack, false);
                }
            }

        }
    }

    public boolean a(EntityHuman entityhuman) {
        if (!this.checkReachable) return true; // CraftBukkit
        return this.i.getType(this.j).getBlock() != Blocks.ANVIL ? false : entityhuman.e((double) this.j.getX() + 0.5D, (double) this.j.getY() + 0.5D, (double) this.j.getZ() + 0.5D) <= 64.0D;
    }

    @Nullable
    public ItemStack b(EntityHuman entityhuman, int i) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.c.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.cloneItemStack();
            if (i == 2) {
                if (!this.a(itemstack1, 3, 39, true)) {
                    return null;
                }

                slot.a(itemstack1, itemstack);
            } else if (i != 0 && i != 1) {
                if (i >= 3 && i < 39 && !this.a(itemstack1, 0, 2, false)) {
                    return null;
                }
            } else if (!this.a(itemstack1, 3, 39, false)) {
                return null;
            }

            if (itemstack1.count == 0) {
                slot.set((ItemStack) null);
            } else {
                slot.f();
            }

            if (itemstack1.count == itemstack.count) {
                return null;
            }

            slot.a(entityhuman, itemstack1);
        }

        return itemstack;
    }

    public void a(String s) {
        this.l = s;
        if (this.getSlot(2).hasItem()) {
            ItemStack itemstack = this.getSlot(2).getItem();

            if (StringUtils.isBlank(s)) {
                itemstack.r();
            } else {
                itemstack.c(this.l);
            }
        }

        this.e();
    }

    // CraftBukkit start
    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        org.bukkit.craftbukkit.inventory.CraftInventory inventory = new org.bukkit.craftbukkit.inventory.CraftInventoryAnvil(
                new org.bukkit.Location(i.getWorld(), j.getX(), j.getY(), j.getZ()), this.h, this.g);
        bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, this);
        return bukkitEntity;
    }
    // CraftBukkit end

    static class SyntheticClass_1 {

        static final int[] a = new int[Enchantment.Rarity.values().length];

        static {
            try {
                ContainerAnvil.SyntheticClass_1.a[Enchantment.Rarity.COMMON.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                ContainerAnvil.SyntheticClass_1.a[Enchantment.Rarity.UNCOMMON.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                ContainerAnvil.SyntheticClass_1.a[Enchantment.Rarity.RARE.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                ContainerAnvil.SyntheticClass_1.a[Enchantment.Rarity.VERY_RARE.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

        }
    }
}
