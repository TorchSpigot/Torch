package net.minecraft.server;

import java.util.Random;
import javax.annotation.Nullable;

// CraftBukkit start
import java.util.List;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
// CraftBukkit end

public class TileEntityDispenser extends TileEntityLootable implements IInventory {

    private static final Random f = new Random();
    private ItemStack[] items = new ItemStack[9];
    protected String a;

    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    public ItemStack[] getContents() {
        return this.items;
    }

    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return transaction;
    }

    public void setMaxStackSize(int size) {
        maxStack = size;
    }
    // CraftBukkit end

    public TileEntityDispenser() {}

    public int getSize() {
        return 9;
    }

    @Nullable
    public ItemStack getItem(int i) {
        this.d((EntityHuman) null);
        return this.items[i];
    }

    @Nullable
    public ItemStack splitStack(int i, int j) {
        this.d((EntityHuman) null);
        ItemStack itemstack = ContainerUtil.a(this.items, i, j);

        if (itemstack != null) {
            this.update();
        }

        return itemstack;
    }

    @Nullable
    public ItemStack splitWithoutUpdate(int i) {
        this.d((EntityHuman) null);
        return ContainerUtil.a(this.items, i);
    }

    public int m() {
        this.d((EntityHuman) null);
        int i = -1;
        int j = 1;

        for (int k = 0; k < this.items.length; ++k) {
            if (this.items[k] != null && TileEntityDispenser.f.nextInt(j++) == 0) {
                if (this.items[k].count == 0) continue; // CraftBukkit
                i = k;
            }
        }

        return i;
    }

    public void setItem(int i, @Nullable ItemStack itemstack) {
        this.d((EntityHuman) null);
        this.items[i] = itemstack;
        if (itemstack != null && itemstack.count > this.getMaxStackSize()) {
            itemstack.count = this.getMaxStackSize();
        }

        this.update();
    }

    public int addItem(ItemStack itemstack) {
        for (int i = 0; i < this.items.length; ++i) {
            if (this.items[i] == null || this.items[i].getItem() == null) {
                this.setItem(i, itemstack);
                return i;
            }
        }

        return -1;
    }

    public String getName() {
        return this.hasCustomName() ? this.a : "container.dispenser";
    }

    public void a(String s) {
        this.a = s;
    }

    public boolean hasCustomName() {
        return this.a != null;
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (!this.d(nbttagcompound)) {
            NBTTagList nbttaglist = nbttagcompound.getList("Items", 10);

            this.items = new ItemStack[this.getSize()];

            for (int i = 0; i < nbttaglist.size(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.get(i);
                int j = nbttagcompound1.getByte("Slot") & 255;

                if (j >= 0 && j < this.items.length) {
                    this.items[j] = ItemStack.createStack(nbttagcompound1);
                }
            }
        }

        if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
            this.a = nbttagcompound.getString("CustomName");
        }

    }

    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        super.save(nbttagcompound);
        if (!this.e(nbttagcompound)) {
            NBTTagList nbttaglist = new NBTTagList();

            for (int i = 0; i < this.items.length; ++i) {
                if (this.items[i] != null) {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                    nbttagcompound1.setByte("Slot", (byte) i);
                    this.items[i].save(nbttagcompound1);
                    nbttaglist.add(nbttagcompound1);
                }
            }

            nbttagcompound.set("Items", nbttaglist);
        }

        if (this.hasCustomName()) {
            nbttagcompound.setString("CustomName", this.a);
        }

        return nbttagcompound;
    }

    public int getMaxStackSize() {
        return maxStack; // CraftBukkit
    }

    public boolean a(EntityHuman entityhuman) {
        return this.world.getTileEntity(this.position) != this ? false : entityhuman.e((double) this.position.getX() + 0.5D, (double) this.position.getY() + 0.5D, (double) this.position.getZ() + 0.5D) <= 64.0D;
    }

    public void startOpen(EntityHuman entityhuman) {}

    public void closeContainer(EntityHuman entityhuman) {}

    public boolean b(int i, ItemStack itemstack) {
        return true;
    }

    public String getContainerName() {
        return "minecraft:dispenser";
    }

    public Container createContainer(PlayerInventory playerinventory, EntityHuman entityhuman) {
        this.d(entityhuman);
        return new ContainerDispenser(playerinventory, this);
    }

    public int getProperty(int i) {
        return 0;
    }

    public void setProperty(int i, int j) {}

    public int g() {
        return 0;
    }

    public void l() {
        this.d((EntityHuman) null);

        for (int i = 0; i < this.items.length; ++i) {
            this.items[i] = null;
        }

    }
}
