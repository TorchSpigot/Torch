package net.minecraft.server;

import javax.annotation.Nullable;
// CraftBukkit start
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
// CraftBukkit end

public class InventoryCraftResult implements IInventory {

    private ItemStack[] items = new ItemStack[1];

    // CraftBukkit start
    private int maxStack = MAX_STACK;

    public ItemStack[] getContents() {
        return this.items;
    }

    public org.bukkit.inventory.InventoryHolder getOwner() {
        return null; // Result slots don't get an owner
    }

    // Don't need a transaction; the InventoryCrafting keeps track of it for us
    public void onOpen(CraftHumanEntity who) {}
    public void onClose(CraftHumanEntity who) {}
    public java.util.List<HumanEntity> getViewers() {
        return new java.util.ArrayList<HumanEntity>();
    }

    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public Location getLocation() {
        return null;
    }
    // CraftBukkit end

    public InventoryCraftResult() {}

    public int getSize() {
        return 1;
    }

    @Nullable
    public ItemStack getItem(int i) {
        return this.items[0];
    }

    public String getName() {
        return "Result";
    }

    public boolean hasCustomName() {
        return false;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return (IChatBaseComponent) (this.hasCustomName() ? new ChatComponentText(this.getName()) : new ChatMessage(this.getName(), new Object[0]));
    }

    @Nullable
    public ItemStack splitStack(int i, int j) {
        return ContainerUtil.a(this.items, 0);
    }

    @Nullable
    public ItemStack splitWithoutUpdate(int i) {
        return ContainerUtil.a(this.items, 0);
    }

    public void setItem(int i, @Nullable ItemStack itemstack) {
        this.items[0] = itemstack;
    }

    public int getMaxStackSize() {
        return maxStack; // CraftBukkit
    }

    public void update() {}

    public boolean a(EntityHuman entityhuman) {
        return true;
    }

    public void startOpen(EntityHuman entityhuman) {}

    public void closeContainer(EntityHuman entityhuman) {}

    public boolean b(int i, ItemStack itemstack) {
        return true;
    }

    public int getProperty(int i) {
        return 0;
    }

    public void setProperty(int i, int j) {}

    public int g() {
        return 0;
    }

    public void l() {
        for (int i = 0; i < this.items.length; ++i) {
            this.items[i] = null;
        }

    }
}
