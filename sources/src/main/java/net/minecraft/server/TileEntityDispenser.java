package net.minecraft.server;

import java.util.Iterator;
import java.util.Random;
// CraftBukkit start
import java.util.List;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
// CraftBukkit end

public class TileEntityDispenser extends TileEntityLootable {

    private static final Random a = new Random();
    private NonNullList<ItemStack> items;

    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    public List<ItemStack> getContents() {
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

    public TileEntityDispenser() {
        this.items = NonNullList.a(9, ItemStack.a);
    }

    public int getSize() {
        return 9;
    }

    public boolean w_() {
        Iterator iterator = this.items.iterator();

        ItemStack itemstack;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            itemstack = (ItemStack) iterator.next();
        } while (itemstack.isEmpty());

        return false;
    }

    public int o() {
        this.d((EntityHuman) null);
        int i = -1;
        int j = 1;

        for (int k = 0; k < this.items.size(); ++k) {
            if (!((ItemStack) this.items.get(k)).isEmpty() && TileEntityDispenser.a.nextInt(j++) == 0) {
                i = k;
            }
        }

        return i;
    }

    public int addItem(ItemStack itemstack) {
        for (int i = 0; i < this.items.size(); ++i) {
            if (((ItemStack) this.items.get(i)).isEmpty()) {
                this.setItem(i, itemstack);
                return i;
            }
        }

        return -1;
    }

    public String getName() {
        return this.hasCustomName() ? this.o : "container.dispenser";
    }

    public static void a(DataConverterManager dataconvertermanager) {
        dataconvertermanager.a(DataConverterTypes.BLOCK_ENTITY, (DataInspector) (new DataInspectorItemList(TileEntityDispenser.class, new String[] { "Items"})));
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.items = NonNullList.a(this.getSize(), ItemStack.a);
        if (!this.c(nbttagcompound)) {
            ContainerUtil.b(nbttagcompound, this.items);
        }

        if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
            this.o = nbttagcompound.getString("CustomName");
        }

    }

    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        super.save(nbttagcompound);
        if (!this.d(nbttagcompound)) {
            ContainerUtil.a(nbttagcompound, this.items);
        }

        if (this.hasCustomName()) {
            nbttagcompound.setString("CustomName", this.o);
        }

        return nbttagcompound;
    }

    public int getMaxStackSize() {
        return maxStack; // CraftBukkit
    }

    public String getContainerName() {
        return "minecraft:dispenser";
    }

    public Container createContainer(PlayerInventory playerinventory, EntityHuman entityhuman) {
        this.d(entityhuman);
        return new ContainerDispenser(playerinventory, this);
    }

    protected NonNullList<ItemStack> q() {
        return this.items;
    }
}
