package net.minecraft.server;

import javax.annotation.Nullable;
// CraftBukkit start
import org.bukkit.craftbukkit.inventory.CraftInventoryBrewer;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
// CraftBukkit end

public class ContainerBrewingStand extends Container {

    private IInventory brewingStand;
    private final Slot f;
    private int g;
    private int h;

    // CraftBukkit start
    private CraftInventoryView bukkitEntity = null;
    private PlayerInventory player;
    // CraftBukkit end

    public ContainerBrewingStand(PlayerInventory playerinventory, IInventory iinventory) {
        player = playerinventory; // CraftBukkit
        this.brewingStand = iinventory;
        this.a((Slot) (new ContainerBrewingStand.SlotPotionBottle(playerinventory.player, iinventory, 0, 56, 51)));
        this.a((Slot) (new ContainerBrewingStand.SlotPotionBottle(playerinventory.player, iinventory, 1, 79, 58)));
        this.a((Slot) (new ContainerBrewingStand.SlotPotionBottle(playerinventory.player, iinventory, 2, 102, 51)));
        this.f = this.a((Slot) (new ContainerBrewingStand.SlotBrewing(iinventory, 3, 79, 17)));
        this.a((Slot) (new ContainerBrewingStand.a(iinventory, 4, 17, 17)));

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

    public void addSlotListener(ICrafting icrafting) {
        super.addSlotListener(icrafting);
        icrafting.setContainerData(this, this.brewingStand);
    }

    public void b() {
        super.b();

        for (int i = 0; i < this.listeners.size(); ++i) {
            ICrafting icrafting = (ICrafting) this.listeners.get(i);

            if (this.g != this.brewingStand.getProperty(0)) {
                icrafting.setContainerData(this, 0, this.brewingStand.getProperty(0));
            }

            if (this.h != this.brewingStand.getProperty(1)) {
                icrafting.setContainerData(this, 1, this.brewingStand.getProperty(1));
            }
        }

        this.g = this.brewingStand.getProperty(0);
        this.h = this.brewingStand.getProperty(1);
    }

    public boolean a(EntityHuman entityhuman) {
        if (!this.checkReachable) return true; // CraftBukkit
        return this.brewingStand.a(entityhuman);
    }

    @Nullable
    public ItemStack b(EntityHuman entityhuman, int i) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.c.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.cloneItemStack();
            if ((i < 0 || i > 2) && i != 3 && i != 4) {
                if (!this.f.hasItem() && this.f.isAllowed(itemstack1)) {
                    if (!this.a(itemstack1, 3, 4, false)) {
                        return null;
                    }
                } else if (ContainerBrewingStand.SlotPotionBottle.c_(itemstack)) {
                    if (!this.a(itemstack1, 0, 3, false)) {
                        return null;
                    }
                } else if (ContainerBrewingStand.a.b_(itemstack)) {
                    if (!this.a(itemstack1, 4, 5, false)) {
                        return null;
                    }
                } else if (i >= 5 && i < 32) {
                    if (!this.a(itemstack1, 32, 41, false)) {
                        return null;
                    }
                } else if (i >= 32 && i < 41) {
                    if (!this.a(itemstack1, 5, 32, false)) {
                        return null;
                    }
                } else if (!this.a(itemstack1, 5, 41, false)) {
                    return null;
                }
            } else {
                if (!this.a(itemstack1, 5, 41, true)) {
                    return null;
                }

                slot.a(itemstack1, itemstack);
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

    static class a extends Slot {

        public a(IInventory iinventory, int i, int j, int k) {
            super(iinventory, i, j, k);
        }

        public boolean isAllowed(@Nullable ItemStack itemstack) {
            return b_(itemstack);
        }

        public static boolean b_(@Nullable ItemStack itemstack) {
            return itemstack != null && itemstack.getItem() == Items.BLAZE_POWDER;
        }

        public int getMaxStackSize() {
            return 64;
        }
    }

    static class SlotBrewing extends Slot {

        public SlotBrewing(IInventory iinventory, int i, int j, int k) {
            super(iinventory, i, j, k);
        }

        public boolean isAllowed(@Nullable ItemStack itemstack) {
            return itemstack != null && PotionBrewer.a(itemstack);
        }

        public int getMaxStackSize() {
            return 64;
        }
    }

    static class SlotPotionBottle extends Slot {

        private EntityHuman a;

        public SlotPotionBottle(EntityHuman entityhuman, IInventory iinventory, int i, int j, int k) {
            super(iinventory, i, j, k);
            this.a = entityhuman;
        }

        public boolean isAllowed(@Nullable ItemStack itemstack) {
            return c_(itemstack);
        }

        public int getMaxStackSize() {
            return 1;
        }

        public void a(EntityHuman entityhuman, ItemStack itemstack) {
            if (PotionUtil.c(itemstack) != Potions.b) {
                this.a.b((Statistic) AchievementList.B);
            }

            super.a(entityhuman, itemstack);
        }

        public static boolean c_(@Nullable ItemStack itemstack) {
            if (itemstack == null) {
                return false;
            } else {
                Item item = itemstack.getItem();

                return item == Items.POTION || item == Items.GLASS_BOTTLE || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION;
            }
        }
    }

    // CraftBukkit start
    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryBrewer inventory = new CraftInventoryBrewer(this.brewingStand);
        bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, this);
        return bukkitEntity;
    }
    // CraftBukkit end
}
