package net.minecraft.server;

import java.util.Arrays;
import javax.annotation.Nullable;

// CraftBukkit start
import java.util.List;
import org.bukkit.Location;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
// CraftBukkit end

public class PlayerInventory implements IInventory {

    public final ItemStack[] items = new ItemStack[36];
    public final ItemStack[] armor = new ItemStack[4];
    public final ItemStack[] extraSlots = new ItemStack[1];
    private final ItemStack[][] g;
    public int itemInHandIndex;
    public EntityHuman player;
    private ItemStack carried;
    public boolean f;

    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    public ItemStack[] getContents() {
        ItemStack[] combined = new ItemStack[items.length + armor.length + extraSlots.length];
        System.arraycopy(items, 0, combined, 0, items.length);
        System.arraycopy(armor, 0, combined, items.length, armor.length);
        System.arraycopy(extraSlots, 0, combined, items.length + armor.length, extraSlots.length);
        return combined;
    }

    public ItemStack[] getArmorContents() {
        return this.armor;
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

    public org.bukkit.inventory.InventoryHolder getOwner() {
        return this.player.getBukkitEntity();
    }

    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public Location getLocation() {
        return player.getBukkitEntity().getLocation();
    }
    // CraftBukkit end

    public PlayerInventory(EntityHuman entityhuman) {
        this.g = new ItemStack[][] { this.items, this.armor, this.extraSlots};
        this.player = entityhuman;
    }

    @Nullable
    public ItemStack getItemInHand() {
        return e(this.itemInHandIndex) ? this.items[this.itemInHandIndex] : null;
    }

    public static int getHotbarSize() {
        return 9;
    }

    private boolean a(@Nullable ItemStack itemstack, ItemStack itemstack1) {
        return itemstack != null && this.b(itemstack, itemstack1) && itemstack.isStackable() && itemstack.count < itemstack.getMaxStackSize() && itemstack.count < this.getMaxStackSize();
    }

    private boolean b(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack.getItem() == itemstack1.getItem() && (!itemstack.usesData() || itemstack.getData() == itemstack1.getData()) && ItemStack.equals(itemstack, itemstack1);
    }

    // CraftBukkit start - Watch method above! :D
    public int canHold(ItemStack itemstack) {
        int remains = itemstack.count;
        for (int i = 0; i < this.items.length; ++i) {
            if (this.items[i] == null) return itemstack.count;

            // Taken from firstPartial(ItemStack)
            if (this.items[i] != null && this.items[i].getItem() == itemstack.getItem() && this.items[i].isStackable() && this.items[i].count < this.items[i].getMaxStackSize() && this.items[i].count < this.getMaxStackSize() && (!this.items[i].usesData() || this.items[i].getData() == itemstack.getData()) && ItemStack.equals(this.items[i], itemstack)) {
                remains -= (this.items[i].getMaxStackSize() < this.getMaxStackSize() ? this.items[i].getMaxStackSize() : this.getMaxStackSize()) - this.items[i].count;
            }
            if (remains <= 0) return itemstack.count;
        }
        return itemstack.count - remains;
    }
    // CraftBukkit end

    public int getFirstEmptySlotIndex() {
        for (int i = 0; i < this.items.length; ++i) {
            if (this.items[i] == null) {
                return i;
            }
        }

        return -1;
    }

    public void d(int i) {
        this.itemInHandIndex = this.k();
        ItemStack itemstack = this.items[this.itemInHandIndex];

        this.items[this.itemInHandIndex] = this.items[i];
        this.items[i] = itemstack;
    }

    public static boolean e(int i) {
        return i >= 0 && i < 9;
    }

    public int k() {
        int i;
        int j;

        for (i = 0; i < 9; ++i) {
            j = (this.itemInHandIndex + i) % 9;
            if (this.items[j] == null) {
                return j;
            }
        }

        for (i = 0; i < 9; ++i) {
            j = (this.itemInHandIndex + i) % 9;
            if (!this.items[j].hasEnchantments()) {
                return j;
            }
        }

        return this.itemInHandIndex;
    }

    public int a(@Nullable Item item, int i, int j, @Nullable NBTTagCompound nbttagcompound) {
        int k = 0;

        int l;

        for (l = 0; l < this.getSize(); ++l) {
            ItemStack itemstack = this.getItem(l);

            if (itemstack != null && (item == null || itemstack.getItem() == item) && (i <= -1 || itemstack.getData() == i) && (nbttagcompound == null || GameProfileSerializer.a(nbttagcompound, itemstack.getTag(), true))) {
                int i1 = j <= 0 ? itemstack.count : Math.min(j - k, itemstack.count);

                k += i1;
                if (j != 0) {
                    itemstack.count -= i1;
                    if (itemstack.count == 0) {
                        this.setItem(l, (ItemStack) null);
                    }

                    if (j > 0 && k >= j) {
                        return k;
                    }
                }
            }
        }

        if (this.carried != null) {
            if (item != null && this.carried.getItem() != item) {
                return k;
            }

            if (i > -1 && this.carried.getData() != i) {
                return k;
            }

            if (nbttagcompound != null && !GameProfileSerializer.a(nbttagcompound, this.carried.getTag(), true)) {
                return k;
            }

            l = j <= 0 ? this.carried.count : Math.min(j - k, this.carried.count);
            k += l;
            if (j != 0) {
                this.carried.count -= l;
                if (this.carried.count == 0) {
                    this.carried = null;
                }

                if (j > 0 && k >= j) {
                    return k;
                }
            }
        }

        return k;
    }

    private int g(ItemStack itemstack) {
        Item item = itemstack.getItem();
        int i = itemstack.count;
        int j = this.firstPartial(itemstack);

        if (j == -1) {
            j = this.getFirstEmptySlotIndex();
        }

        if (j == -1) {
            return i;
        } else {
            ItemStack itemstack1 = this.getItem(j);

            if (itemstack1 == null) {
                itemstack1 = new ItemStack(item, 0, itemstack.getData());
                if (itemstack.hasTag()) {
                    itemstack1.setTag((NBTTagCompound) itemstack.getTag().clone());
                }

                this.setItem(j, itemstack1);
            }

            int k = i;

            if (i > itemstack1.getMaxStackSize() - itemstack1.count) {
                k = itemstack1.getMaxStackSize() - itemstack1.count;
            }

            if (k > this.getMaxStackSize() - itemstack1.count) {
                k = this.getMaxStackSize() - itemstack1.count;
            }

            if (k == 0) {
                return i;
            } else {
                i -= k;
                itemstack1.count += k;
                itemstack1.c = 5;
                return i;
            }
        }
    }

    private int firstPartial(ItemStack itemstack) {
        if (this.a(this.getItem(this.itemInHandIndex), itemstack)) {
            return this.itemInHandIndex;
        } else if (this.a(this.getItem(40), itemstack)) {
            return 40;
        } else {
            for (int i = 0; i < this.items.length; ++i) {
                if (this.a(this.items[i], itemstack)) {
                    return i;
                }
            }

            return -1;
        }
    }

    public void m() {
        for (int i = 0; i < this.g.length; ++i) {
            ItemStack[] aitemstack = this.g[i];

            for (int j = 0; j < aitemstack.length; ++j) {
                if (aitemstack[j] != null) {
                    aitemstack[j].a(this.player.world, this.player, j, this.itemInHandIndex == j);
                }
            }
        }

    }

    public boolean pickup(@Nullable final ItemStack itemstack) {
        if (itemstack != null && itemstack.count != 0 && itemstack.getItem() != null) {
            try {
                int i;

                if (itemstack.g()) {
                    i = this.getFirstEmptySlotIndex();
                    if (i >= 0) {
                        this.items[i] = ItemStack.c(itemstack);
                        this.items[i].c = 5;
                        itemstack.count = 0;
                        return true;
                    } else if (this.player.abilities.canInstantlyBuild) {
                        itemstack.count = 0;
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    do {
                        i = itemstack.count;
                        itemstack.count = this.g(itemstack);
                    } while (itemstack.count > 0 && itemstack.count < i);

                    if (itemstack.count == i && this.player.abilities.canInstantlyBuild) {
                        itemstack.count = 0;
                        return true;
                    } else {
                        return itemstack.count < i;
                    }
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Adding item to inventory");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Item being added");

                crashreportsystemdetails.a("Item ID", (Object) Integer.valueOf(Item.getId(itemstack.getItem())));
                crashreportsystemdetails.a("Item data", (Object) Integer.valueOf(itemstack.getData()));
                crashreportsystemdetails.a("Item name", new CrashReportCallable() {
                    public String a() throws Exception {
                        return itemstack.getName();
                    }

                    public Object call() throws Exception {
                        return this.a();
                    }
                });
                throw new ReportedException(crashreport);
            }
        } else {
            return false;
        }
    }

    @Nullable
    public ItemStack splitStack(int i, int j) {
        ItemStack[] aitemstack = null;
        ItemStack[][] aitemstack1 = this.g;
        int k = aitemstack1.length;

        for (int l = 0; l < k; ++l) {
            ItemStack[] aitemstack2 = aitemstack1[l];

            if (i < aitemstack2.length) {
                aitemstack = aitemstack2;
                break;
            }

            i -= aitemstack2.length;
        }

        return aitemstack != null && aitemstack[i] != null ? ContainerUtil.a(aitemstack, i, j) : null;
    }

    public void d(ItemStack itemstack) {
        ItemStack[][] aitemstack = this.g;
        int i = aitemstack.length;
        int j = 0;

        while (j < i) {
            ItemStack[] aitemstack1 = aitemstack[j];
            int k = 0;

            while (true) {
                if (k < aitemstack1.length) {
                    if (aitemstack1[k] != itemstack) {
                        ++k;
                        continue;
                    }

                    aitemstack1[k] = null;
                }

                ++j;
                break;
            }
        }

    }

    @Nullable
    public ItemStack splitWithoutUpdate(int i) {
        ItemStack[] aitemstack = null;
        ItemStack[][] aitemstack1 = this.g;
        int j = aitemstack1.length;

        for (int k = 0; k < j; ++k) {
            ItemStack[] aitemstack2 = aitemstack1[k];

            if (i < aitemstack2.length) {
                aitemstack = aitemstack2;
                break;
            }

            i -= aitemstack2.length;
        }

        if (aitemstack != null && aitemstack[i] != null) {
            Object object = aitemstack[i];

            aitemstack[i] = null;
            return (ItemStack) object;
        } else {
            return null;
        }
    }

    public void setItem(int i, @Nullable ItemStack itemstack) {
        ItemStack[] aitemstack = null;
        ItemStack[][] aitemstack1 = this.g;
        int j = aitemstack1.length;

        for (int k = 0; k < j; ++k) {
            ItemStack[] aitemstack2 = aitemstack1[k];

            if (i < aitemstack2.length) {
                aitemstack = aitemstack2;
                break;
            }

            i -= aitemstack2.length;
        }

        if (aitemstack != null) {
            aitemstack[i] = itemstack;
        }

    }

    public float a(IBlockData iblockdata) {
        float f = 1.0F;

        if (this.items[this.itemInHandIndex] != null) {
            f *= this.items[this.itemInHandIndex].a(iblockdata);
        }

        return f;
    }

    public NBTTagList a(NBTTagList nbttaglist) {
        int i;
        NBTTagCompound nbttagcompound;

        for (i = 0; i < this.items.length; ++i) {
            if (this.items[i] != null) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                this.items[i].save(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        for (i = 0; i < this.armor.length; ++i) {
            if (this.armor[i] != null) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) (i + 100));
                this.armor[i].save(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        for (i = 0; i < this.extraSlots.length; ++i) {
            if (this.extraSlots[i] != null) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) (i + 150));
                this.extraSlots[i].save(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        return nbttaglist;
    }

    public void b(NBTTagList nbttaglist) {
        Arrays.fill(this.items, (Object) null);
        Arrays.fill(this.armor, (Object) null);
        Arrays.fill(this.extraSlots, (Object) null);

        for (int i = 0; i < nbttaglist.size(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.get(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.createStack(nbttagcompound);

            if (itemstack != null) {
                if (j >= 0 && j < this.items.length) {
                    this.items[j] = itemstack;
                } else if (j >= 100 && j < this.armor.length + 100) {
                    this.armor[j - 100] = itemstack;
                } else if (j >= 150 && j < this.extraSlots.length + 150) {
                    this.extraSlots[j - 150] = itemstack;
                }
            }
        }

    }

    public int getSize() {
        return this.items.length + this.armor.length + this.extraSlots.length;
    }

    @Nullable
    public ItemStack getItem(int i) {
        ItemStack[] aitemstack = null;
        ItemStack[][] aitemstack1 = this.g;
        int j = aitemstack1.length;

        for (int k = 0; k < j; ++k) {
            ItemStack[] aitemstack2 = aitemstack1[k];

            if (i < aitemstack2.length) {
                aitemstack = aitemstack2;
                break;
            }

            i -= aitemstack2.length;
        }

        return aitemstack == null ? null : aitemstack[i];
    }

    public String getName() {
        return "container.inventory";
    }

    public boolean hasCustomName() {
        return false;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return (IChatBaseComponent) (this.hasCustomName() ? new ChatComponentText(this.getName()) : new ChatMessage(this.getName(), new Object[0]));
    }

    public int getMaxStackSize() {
        return maxStack; // CraftBukkit
    }

    public boolean b(IBlockData iblockdata) {
        if (iblockdata.getMaterial().isAlwaysDestroyable()) {
            return true;
        } else {
            ItemStack itemstack = this.getItem(this.itemInHandIndex);

            return itemstack != null ? itemstack.b(iblockdata) : false;
        }
    }

    public void a(float f) {
        f /= 4.0F;
        if (f < 1.0F) {
            f = 1.0F;
        }

        for (int i = 0; i < this.armor.length; ++i) {
            if (this.armor[i] != null && this.armor[i].getItem() instanceof ItemArmor) {
                this.armor[i].damage((int) f, this.player);
                if (this.armor[i].count == 0) {
                    this.armor[i] = null;
                }
            }
        }

    }

    public void n() {
        ItemStack[][] aitemstack = this.g;
        int i = aitemstack.length;

        for (int j = 0; j < i; ++j) {
            ItemStack[] aitemstack1 = aitemstack[j];

            for (int k = 0; k < aitemstack1.length; ++k) {
                if (aitemstack1[k] != null) {
                    this.player.a(aitemstack1[k], true, false);
                    aitemstack1[k] = null;
                }
            }
        }

    }

    public void update() {
        this.f = true;
    }

    public void setCarried(@Nullable ItemStack itemstack) {
        this.carried = itemstack;
    }

    @Nullable
    public ItemStack getCarried() {
        // CraftBukkit start
        if (this.carried != null && this.carried.count == 0) {
            this.setCarried(null);
        }
        // CraftBukkit end
        return this.carried;
    }

    public boolean a(EntityHuman entityhuman) {
        return this.player.dead ? false : entityhuman.h(this.player) <= 64.0D;
    }

    public boolean f(ItemStack itemstack) {
        ItemStack[][] aitemstack = this.g;
        int i = aitemstack.length;

        for (int j = 0; j < i; ++j) {
            ItemStack[] aitemstack1 = aitemstack[j];

            for (int k = 0; k < aitemstack1.length; ++k) {
                if (aitemstack1[k] != null && aitemstack1[k].doMaterialsMatch(itemstack)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void startOpen(EntityHuman entityhuman) {}

    public void closeContainer(EntityHuman entityhuman) {}

    public boolean b(int i, ItemStack itemstack) {
        return true;
    }

    public void a(PlayerInventory playerinventory) {
        for (int i = 0; i < this.getSize(); ++i) {
            this.setItem(i, playerinventory.getItem(i));
        }

        this.itemInHandIndex = playerinventory.itemInHandIndex;
    }

    public int getProperty(int i) {
        return 0;
    }

    public void setProperty(int i, int j) {}

    public int g() {
        return 0;
    }

    public void l() {
        ItemStack[][] aitemstack = this.g;
        int i = aitemstack.length;

        for (int j = 0; j < i; ++j) {
            ItemStack[] aitemstack1 = aitemstack[j];

            for (int k = 0; k < aitemstack1.length; ++k) {
                aitemstack1[k] = null;
            }
        }

    }
}
