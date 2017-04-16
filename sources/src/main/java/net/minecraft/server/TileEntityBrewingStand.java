package net.minecraft.server;

import java.util.Arrays;
import java.util.Iterator;

// CraftBukkit start
import java.util.List;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
// CraftBukkit end

public class TileEntityBrewingStand extends TileEntityContainer implements ITickable, IWorldInventory {

    private static final int[] a = new int[] { 3};
    private static final int[] f = new int[] { 0, 1, 2, 3};
    private static final int[] g = new int[] { 0, 1, 2, 4};
    private NonNullList<ItemStack> items;
    private int brewTime;
    private boolean[] j;
    private Item k;
    private String l;
    private int m;
    // CraftBukkit start - add fields and methods
    private int lastTick = MinecraftServer.currentTick;
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = 64;

    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return transaction;
    }

    public List<ItemStack> getContents() {
        return this.items;
    }

    public void setMaxStackSize(int size) {
        maxStack = size;
    }
    // CraftBukkit end

    public TileEntityBrewingStand() {
        this.items = NonNullList.a(5, ItemStack.a);
    }

    public String getName() {
        return this.hasCustomName() ? this.l : "container.brewing";
    }

    public boolean hasCustomName() {
        return this.l != null && !this.l.isEmpty();
    }

    public void a(String s) {
        this.l = s;
    }

    public int getSize() {
        return this.items.size();
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

    public void F_() {
        ItemStack itemstack = (ItemStack) this.items.get(4);

        if (this.m <= 0 && itemstack.getItem() == Items.BLAZE_POWDER) {
            // CraftBukkit start
            BrewingStandFuelEvent event = new BrewingStandFuelEvent(world.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ()), CraftItemStack.asCraftMirror(itemstack), 20);
            this.world.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            this.m = event.getFuelPower(); // PAIL fuelLevel
            if (this.m > 0 && event.isConsuming()) {
                itemstack.subtract(1);
            }
            // CraftBukkit end
            this.update();
        }

        boolean flag = this.o();
        boolean flag1 = this.brewTime > 0;
        ItemStack itemstack1 = (ItemStack) this.items.get(3);

        // CraftBukkit start - Use wall time instead of ticks for brewing
        int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
        this.lastTick = MinecraftServer.currentTick;

        if (flag1) {
            this.brewTime -= elapsedTicks;
            boolean flag2 = this.brewTime <= 0; // == -> <=
            // CraftBukkit end

            if (flag2 && flag) {
                this.p();
                this.update();
            } else if (!flag) {
                this.brewTime = 0;
                this.update();
            } else if (this.k != itemstack1.getItem()) {
                this.brewTime = 0;
                this.update();
            }
        } else if (flag && this.m > 0) {
            --this.m;
            this.brewTime = 400;
            this.k = itemstack1.getItem();
            this.update();
        }

        if (!this.world.isClientSide) {
            boolean[] aboolean = this.n();

            if (!Arrays.equals(aboolean, this.j)) {
                this.j = aboolean;
                IBlockData iblockdata = this.world.getType(this.getPosition());

                if (!(iblockdata.getBlock() instanceof BlockBrewingStand)) {
                    return;
                }

                for (int i = 0; i < BlockBrewingStand.HAS_BOTTLE.length; ++i) {
                    iblockdata = iblockdata.set(BlockBrewingStand.HAS_BOTTLE[i], Boolean.valueOf(aboolean[i]));
                }

                this.world.setTypeAndData(this.position, iblockdata, 2);
            }
        }

    }

    public boolean[] n() {
        boolean[] aboolean = new boolean[3];

        for (int i = 0; i < 3; ++i) {
            if (!((ItemStack) this.items.get(i)).isEmpty()) {
                aboolean[i] = true;
            }
        }

        return aboolean;
    }

    private boolean o() {
        ItemStack itemstack = (ItemStack) this.items.get(3);

        if (itemstack.isEmpty()) {
            return false;
        } else if (!PotionBrewer.a(itemstack)) {
            return false;
        } else {
            for (int i = 0; i < 3; ++i) {
                ItemStack itemstack1 = (ItemStack) this.items.get(i);

                if (!itemstack1.isEmpty() && PotionBrewer.a(itemstack1, itemstack)) {
                    return true;
                }
            }

            return false;
        }
    }

    private void p() {
        ItemStack itemstack = (ItemStack) this.items.get(3);
        // CraftBukkit start
        if (getOwner() != null) {
            BrewEvent event = new BrewEvent(world.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ()), (org.bukkit.inventory.BrewerInventory) this.getOwner().getInventory(), this.m);
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }
        // CraftBukkit end

        for (int i = 0; i < 3; ++i) {
            this.items.set(i, PotionBrewer.d(itemstack, (ItemStack) this.items.get(i)));
        }

        itemstack.subtract(1);
        BlockPosition blockposition = this.getPosition();

        if (itemstack.getItem().s()) {
            ItemStack itemstack1 = new ItemStack(itemstack.getItem().r());

            if (itemstack.isEmpty()) {
                itemstack = itemstack1;
            } else {
                InventoryUtils.dropItem(this.world, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), itemstack1);
            }
        }

        this.items.set(3, itemstack);
        this.world.triggerEffect(1035, blockposition, 0);
    }

    public static void a(DataConverterManager dataconvertermanager) {
        dataconvertermanager.a(DataConverterTypes.BLOCK_ENTITY, (DataInspector) (new DataInspectorItemList(TileEntityBrewingStand.class, new String[] { "Items"})));
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.items = NonNullList.a(this.getSize(), ItemStack.a);
        ContainerUtil.b(nbttagcompound, this.items);
        this.brewTime = nbttagcompound.getShort("BrewTime");
        if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
            this.l = nbttagcompound.getString("CustomName");
        }

        this.m = nbttagcompound.getByte("Fuel");
    }

    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        super.save(nbttagcompound);
        nbttagcompound.setShort("BrewTime", (short) this.brewTime);
        ContainerUtil.a(nbttagcompound, this.items);
        if (this.hasCustomName()) {
            nbttagcompound.setString("CustomName", this.l);
        }

        nbttagcompound.setByte("Fuel", (byte) this.m);
        return nbttagcompound;
    }

    public ItemStack getItem(int i) {
        return i >= 0 && i < this.items.size() ? (ItemStack) this.items.get(i) : ItemStack.a;
    }

    public ItemStack splitStack(int i, int j) {
        return ContainerUtil.a(this.items, i, j);
    }

    public ItemStack splitWithoutUpdate(int i) {
        return ContainerUtil.a(this.items, i);
    }

    public void setItem(int i, ItemStack itemstack) {
        if (i >= 0 && i < this.items.size()) {
            this.items.set(i, itemstack);
        }

    }

    public int getMaxStackSize() {
        return this.maxStack; // CraftBukkit
    }

    public boolean a(EntityHuman entityhuman) {
        return this.world.getTileEntity(this.position) != this ? false : entityhuman.d((double) this.position.getX() + 0.5D, (double) this.position.getY() + 0.5D, (double) this.position.getZ() + 0.5D) <= 64.0D;
    }

    public void startOpen(EntityHuman entityhuman) {}

    public void closeContainer(EntityHuman entityhuman) {}

    public boolean b(int i, ItemStack itemstack) {
        if (i == 3) {
            return PotionBrewer.a(itemstack);
        } else {
            Item item = itemstack.getItem();

            return i == 4 ? item == Items.BLAZE_POWDER : (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE) && this.getItem(i).isEmpty(); // CraftBukkit - MC-111753
        }
    }

    public int[] getSlotsForFace(EnumDirection enumdirection) {
        return enumdirection == EnumDirection.UP ? TileEntityBrewingStand.a : (enumdirection == EnumDirection.DOWN ? TileEntityBrewingStand.f : TileEntityBrewingStand.g);
    }

    public boolean canPlaceItemThroughFace(int i, ItemStack itemstack, EnumDirection enumdirection) {
        return this.b(i, itemstack);
    }

    public boolean canTakeItemThroughFace(int i, ItemStack itemstack, EnumDirection enumdirection) {
        return i == 3 ? itemstack.getItem() == Items.GLASS_BOTTLE : true;
    }

    public String getContainerName() {
        return "minecraft:brewing_stand";
    }

    public Container createContainer(PlayerInventory playerinventory, EntityHuman entityhuman) {
        return new ContainerBrewingStand(playerinventory, this);
    }

    public int getProperty(int i) {
        switch (i) {
        case 0:
            return this.brewTime;

        case 1:
            return this.m;

        default:
            return 0;
        }
    }

    public void setProperty(int i, int j) {
        switch (i) {
        case 0:
            this.brewTime = j;
            break;

        case 1:
            this.m = j;
        }

    }

    public int h() {
        return 2;
    }

    public void clear() {
        this.items.clear();
    }
}
