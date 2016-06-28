package net.minecraft.server;

import javax.annotation.Nullable;
// CraftBukkit start
import java.util.List;
import org.bukkit.Location;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
// CraftBukkit end

public class InventoryCrafting implements IInventory {

    private final ItemStack[] items;
    private final int b;
    private final int c;
    private final Container d;

    // CraftBukkit start - add fields
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    public IRecipe currentRecipe;
    public IInventory resultInventory;
    private EntityHuman owner;
    private int maxStack = MAX_STACK;

    public ItemStack[] getContents() {
        return this.items;
    }

    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    public InventoryType getInvType() {
        return items.length == 4 ? InventoryType.CRAFTING : InventoryType.WORKBENCH;
    }

    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return transaction;
    }

    public org.bukkit.inventory.InventoryHolder getOwner() {
        return (owner == null) ? null : owner.getBukkitEntity();
    }

    public void setMaxStackSize(int size) {
        maxStack = size;
        resultInventory.setMaxStackSize(size);
    }

    @Override
    public Location getLocation() {
        return owner.getBukkitEntity().getLocation();
    }

    public InventoryCrafting(Container container, int i, int j, EntityHuman player) {
        this(container, i, j);
        this.owner = player;
    }
    // CraftBukkit end

    public InventoryCrafting(Container container, int i, int j) {
        int k = i * j;

        this.items = new ItemStack[k];
        this.d = container;
        this.b = i;
        this.c = j;
    }

    public int getSize() {
        return this.items.length;
    }

    @Nullable
    public ItemStack getItem(int i) {
        return i >= this.getSize() ? null : this.items[i];
    }

    @Nullable
    public ItemStack c(int i, int j) {
        return i >= 0 && i < this.b && j >= 0 && j <= this.c ? this.getItem(i + j * this.b) : null;
    }

    public String getName() {
        return "container.crafting";
    }

    public boolean hasCustomName() {
        return false;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return (IChatBaseComponent) (this.hasCustomName() ? new ChatComponentText(this.getName()) : new ChatMessage(this.getName(), new Object[0]));
    }

    @Nullable
    public ItemStack splitWithoutUpdate(int i) {
        return ContainerUtil.a(this.items, i);
    }

    @Nullable
    public ItemStack splitStack(int i, int j) {
        ItemStack itemstack = ContainerUtil.a(this.items, i, j);

        if (itemstack != null) {
            this.d.a((IInventory) this);
        }

        return itemstack;
    }

    public void setItem(int i, @Nullable ItemStack itemstack) {
        this.items[i] = itemstack;
        this.d.a((IInventory) this);
    }

    public int getMaxStackSize() {
        return 64;
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

    public int h() {
        return this.c;
    }

    public int i() {
        return this.b;
    }
}
