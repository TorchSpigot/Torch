package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

// CraftBukkit start
import java.util.HashMap;
import java.util.Map;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
// CraftBukkit end

public abstract class Container {

    public List<ItemStack> b = Lists.newArrayList();
    public List<Slot> c = Lists.newArrayList(); // Torch - fix crafting
    public int windowId;
    private int dragType = -1;
    private int g;
    private final Set<Slot> h = Sets.newHashSet();
    protected List<ICrafting> listeners = Lists.newArrayList();
    private Set<EntityHuman> i = Sets.newHashSet();
    private int tickCount; // Spigot

    // CraftBukkit start
    public boolean checkReachable = true;
    public abstract InventoryView getBukkitView();
    public void transferTo(Container other, org.bukkit.craftbukkit.entity.CraftHumanEntity player) {
        InventoryView source = this.getBukkitView(), destination = other.getBukkitView();
        ((CraftInventory) source.getTopInventory()).getInventory().onClose(player);
        ((CraftInventory) source.getBottomInventory()).getInventory().onClose(player);
        ((CraftInventory) destination.getTopInventory()).getInventory().onOpen(player);
        ((CraftInventory) destination.getBottomInventory()).getInventory().onOpen(player);
    }
    // CraftBukkit end

    public Container() {}

    protected Slot a(Slot slot) {
        slot.rawSlotIndex = this.c.size();
        this.c.add(slot);
        this.b.add(null); // CraftBukkit - fix decompile error
        return slot;
    }

    public void addSlotListener(ICrafting icrafting) {
        if (this.listeners.contains(icrafting)) {
            throw new IllegalArgumentException("Listener already listening");
        } else {
            this.listeners.add(icrafting);
            icrafting.a(this, this.a());
            this.b();
        }
    }

    public List<ItemStack> a() {
        ArrayList arraylist = Lists.newArrayList();

        for (int i = 0; i < this.c.size(); ++i) {
            arraylist.add(((Slot) this.c.get(i)).getItem());
        }

        return arraylist;
    }

    public void b() {
        for (int i = 0; i < this.c.size(); ++i) {
            ItemStack itemstack = ((Slot) this.c.get(i)).getItem();
            ItemStack itemstack1 = (ItemStack) this.b.get(i);

            if (!ItemStack.fastMatches(itemstack1, itemstack) || (tickCount % 20 == 0 && !ItemStack.matches(itemstack1, itemstack))) { // Spigot
                itemstack1 = itemstack == null ? null : itemstack.cloneItemStack();
                this.b.set(i, itemstack1);

                for (int j = 0; j < this.listeners.size(); ++j) {
                    ((ICrafting) this.listeners.get(j)).a(this, i, itemstack1);
                }
            }
        }
        tickCount++; // Spigot

    }

    public boolean a(EntityHuman entityhuman, int i) {
        return false;
    }

    @Nullable
    public Slot getSlot(IInventory iinventory, int i) {
        for (int j = 0; j < this.c.size(); ++j) {
            Slot slot = (Slot) this.c.get(j);

            if (slot.a(iinventory, i)) {
                return slot;
            }
        }

        return null;
    }

    public Slot getSlot(int i) {
        return (Slot) this.c.get(i);
    }

    @Nullable
    public ItemStack b(EntityHuman entityhuman, int i) {
        Slot slot = (Slot) this.c.get(i);

        return slot != null ? slot.getItem() : null;
    }

    @Nullable
    public ItemStack a(int i, int j, InventoryClickType inventoryclicktype, EntityHuman entityhuman) {
        ItemStack itemstack = null;
        PlayerInventory playerinventory = entityhuman.inventory;
        int k;
        ItemStack itemstack1;

        if (inventoryclicktype == InventoryClickType.QUICK_CRAFT) {
            int l = this.g;

            this.g = c(j);
            if ((l != 1 || this.g != 2) && l != this.g) {
                this.d();
            } else if (playerinventory.getCarried() == null) {
                this.d();
            } else if (this.g == 0) {
                this.dragType = b(j);
                if (a(this.dragType, entityhuman)) {
                    this.g = 1;
                    this.h.clear();
                } else {
                    this.d();
                }
            } else if (this.g == 1) {
                Slot slot = i < this.c.size() ? this.c.get(i) : null; // Paper - Ensure drag in bounds

                if (slot != null && a(slot, playerinventory.getCarried(), true) && slot.isAllowed(playerinventory.getCarried()) && playerinventory.getCarried().count > this.h.size() && this.b(slot)) {
                    this.h.add(slot);
                }
            } else if (this.g == 2) {
                if (!this.h.isEmpty()) {
                    itemstack1 = playerinventory.getCarried().cloneItemStack();
                    k = playerinventory.getCarried().count;
                    Iterator iterator = this.h.iterator();

                    Map<Integer, ItemStack> draggedSlots = new HashMap<Integer, ItemStack>(); // CraftBukkit - Store slots from drag in map (raw slot id -> new stack)
                    while (iterator.hasNext()) {
                        Slot slot1 = (Slot) iterator.next();

                        if (slot1 != null && a(slot1, playerinventory.getCarried(), true) && slot1.isAllowed(playerinventory.getCarried()) && playerinventory.getCarried().count >= this.h.size() && this.b(slot1)) {
                            ItemStack itemstack2 = itemstack1.cloneItemStack();
                            int i1 = slot1.hasItem() ? slot1.getItem().count : 0;

                            a(this.h, this.dragType, itemstack2, i1);
                            if (itemstack2.count > itemstack2.getMaxStackSize()) {
                                itemstack2.count = itemstack2.getMaxStackSize();
                            }

                            if (itemstack2.count > slot1.getMaxStackSize(itemstack2)) {
                                itemstack2.count = slot1.getMaxStackSize(itemstack2);
                            }

                            k -= itemstack2.count - i1;
                            // slot1.set(itemstack2);
                            draggedSlots.put(slot1.rawSlotIndex, itemstack2); // CraftBukkit - Put in map instead of setting
                        }
                    }

                    // CraftBukkit start - InventoryDragEvent
                    InventoryView view = getBukkitView();
                    org.bukkit.inventory.ItemStack newcursor = CraftItemStack.asCraftMirror(itemstack1);
                    newcursor.setAmount(k);
                    Map<Integer, org.bukkit.inventory.ItemStack> eventmap = new HashMap<Integer, org.bukkit.inventory.ItemStack>();
                    for (Map.Entry<Integer, ItemStack> ditem : draggedSlots.entrySet()) {
                        eventmap.put(ditem.getKey(), CraftItemStack.asBukkitCopy(ditem.getValue()));
                    }

                    // It's essential that we set the cursor to the new value here to prevent item duplication if a plugin closes the inventory.
                    ItemStack oldCursor = playerinventory.getCarried();
                    playerinventory.setCarried(CraftItemStack.asNMSCopy(newcursor));

                    InventoryDragEvent event = new InventoryDragEvent(view, (newcursor.getType() != org.bukkit.Material.AIR ? newcursor : null), CraftItemStack.asBukkitCopy(oldCursor), this.dragType == 1, eventmap);
                    entityhuman.world.getServer().getPluginManager().callEvent(event);

                    // Whether or not a change was made to the inventory that requires an update.
                    boolean needsUpdate = event.getResult() != Result.DEFAULT;

                    if (event.getResult() != Result.DENY) {
                        for (Map.Entry<Integer, ItemStack> dslot : draggedSlots.entrySet()) {
                            view.setItem(dslot.getKey(), CraftItemStack.asBukkitCopy(dslot.getValue()));
                        }
                        // The only time the carried item will be set to null is if the inventory is closed by the server.
                        // If the inventory is closed by the server, then the cursor items are dropped.  This is why we change the cursor early.
                        if (playerinventory.getCarried() != null) {
                            playerinventory.setCarried(CraftItemStack.asNMSCopy(event.getCursor()));
                            needsUpdate = true;
                        }
                    } else {
                        playerinventory.setCarried(oldCursor);
                    }

                    if (needsUpdate && entityhuman instanceof EntityPlayer) {
                        ((EntityPlayer) entityhuman).updateInventory(this);
                    }
                    // CraftBukkit end
                }

                this.d();
            } else {
                this.d();
            }
        } else if (this.g != 0) {
            this.d();
        } else {
            Slot slot2;
            ItemStack itemstack3;
            int j1;

            if ((inventoryclicktype == InventoryClickType.PICKUP || inventoryclicktype == InventoryClickType.QUICK_MOVE) && (j == 0 || j == 1)) {
                if (i == -999) {
                    if (playerinventory.getCarried() != null) {
                        if (j == 0) {
                            entityhuman.drop(playerinventory.getCarried(), true);
                            playerinventory.setCarried((ItemStack) null);
                        }

                        if (j == 1) {
                            // CraftBukkit start - Store a reference, don't drop unless > 0
                            ItemStack carried = playerinventory.getCarried();
                            if (carried.count > 0) {
                                entityhuman.drop(carried.cloneAndSubtract(1), true);
                            }

                            if (carried.count == 0) {
                                // CraftBukkit end
                                playerinventory.setCarried((ItemStack) null);
                            }
                        }
                    }
                } else if (inventoryclicktype == InventoryClickType.QUICK_MOVE) {
                    if (i < 0) {
                        return null;
                    }

                    slot2 = (Slot) this.c.get(i);
                    if (slot2 != null && slot2.isAllowed(entityhuman)) {
                        itemstack1 = slot2.getItem();
                        if (itemstack1 != null && itemstack1.count <= 0) {
                            itemstack = itemstack1.cloneItemStack();
                            slot2.set((ItemStack) null);
                        }

                        itemstack3 = this.b(entityhuman, i);
                        if (itemstack3 != null) {
                            Item item = itemstack3.getItem();

                            itemstack = itemstack3.cloneItemStack();
                            if (slot2.getItem() != null && slot2.getItem().getItem() == item) {
                                this.a(i, j, true, entityhuman);
                            }
                        }
                    }
                } else {
                    if (i < 0) {
                        return null;
                    }

                    slot2 = (Slot) this.c.get(i);
                    if (slot2 != null) {
                        itemstack1 = slot2.getItem();
                        itemstack3 = playerinventory.getCarried();
                        if (itemstack1 != null) {
                            itemstack = itemstack1.cloneItemStack();
                        }

                        if (itemstack1 == null) {
                            if (itemstack3 != null && slot2.isAllowed(itemstack3)) {
                                j1 = j == 0 ? itemstack3.count : 1;
                                if (j1 > slot2.getMaxStackSize(itemstack3)) {
                                    j1 = slot2.getMaxStackSize(itemstack3);
                                }

                                slot2.set(itemstack3.cloneAndSubtract(j1));
                                if (itemstack3.count == 0) {
                                    playerinventory.setCarried((ItemStack) null);
                                // CraftBukkit start - Update client cursor if we didn't empty it
                                } else if (entityhuman instanceof EntityPlayer) {
                                    ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, entityhuman.inventory.getCarried()));
                                }
                                // CraftBukkit end
                            }
                        } else if (slot2.isAllowed(entityhuman)) {
                            if (itemstack3 == null) {
                                if (itemstack1.count > 0) {
                                    j1 = j == 0 ? itemstack1.count : (itemstack1.count + 1) / 2;
                                    playerinventory.setCarried(slot2.a(j1));
                                    if (itemstack1.count <= 0) {
                                        slot2.set((ItemStack) null);
                                    }

                                    slot2.a(entityhuman, playerinventory.getCarried());
                                } else {
                                    slot2.set((ItemStack) null);
                                    playerinventory.setCarried((ItemStack) null);
                                }
                            } else if (slot2.isAllowed(itemstack3)) {
                                if (itemstack1.getItem() == itemstack3.getItem() && itemstack1.getData() == itemstack3.getData() && ItemStack.equals(itemstack1, itemstack3)) {
                                    j1 = j == 0 ? itemstack3.count : 1;
                                    if (j1 > slot2.getMaxStackSize(itemstack3) - itemstack1.count) {
                                        j1 = slot2.getMaxStackSize(itemstack3) - itemstack1.count;
                                    }

                                    if (j1 > itemstack3.getMaxStackSize() - itemstack1.count) {
                                        j1 = itemstack3.getMaxStackSize() - itemstack1.count;
                                    }

                                    itemstack3.cloneAndSubtract(j1);
                                    if (itemstack3.count == 0) {
                                        playerinventory.setCarried((ItemStack) null);
                                    // CraftBukkit start - Update client cursor if we didn't empty it
                                    } else if (entityhuman instanceof EntityPlayer) {
                                        ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, entityhuman.inventory.getCarried()));
                                    }
                                    // CraftBukkit end

                                    itemstack1.count += j1;
                                } else if (itemstack3.count <= slot2.getMaxStackSize(itemstack3)) {
                                    slot2.set(itemstack3);
                                    playerinventory.setCarried(itemstack1);
                                }
                            } else if (itemstack1.getItem() == itemstack3.getItem() && itemstack3.getMaxStackSize() > 1 && (!itemstack1.usesData() || itemstack1.getData() == itemstack3.getData()) && ItemStack.equals(itemstack1, itemstack3)) {
                                j1 = itemstack1.count;
                                // CraftBukkit start - itemstack3.getMaxStackSize() -> maxStack
                                int maxStack = Math.min(itemstack3.getMaxStackSize(), slot2.getMaxStackSize());
                                if (j1 > 0 && j1 + itemstack3.count <= maxStack) {
                                    // CraftBukkit end
                                    itemstack3.count += j1;
                                    itemstack1 = slot2.a(j1);
                                    if (itemstack1.count == 0) {
                                        slot2.set((ItemStack) null);
                                    }

                                    slot2.a(entityhuman, playerinventory.getCarried());
                                // CraftBukkit start - Update client cursor if we didn't empty it
                                } else if (entityhuman instanceof EntityPlayer) {
                                    ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, entityhuman.inventory.getCarried()));
                                }
                                // CraftBukkit end
                            }
                        }

                        slot2.f();
                        // CraftBukkit start - Make sure the client has the right slot contents
                        if (entityhuman instanceof EntityPlayer && slot2.getMaxStackSize() != 64) {
                            ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(this.windowId, slot2.rawSlotIndex, slot2.getItem()));
                            // Updating a crafting inventory makes the client reset the result slot, have to send it again
                            if (this.getBukkitView().getType() == InventoryType.WORKBENCH || this.getBukkitView().getType() == InventoryType.CRAFTING) {
                                ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(this.windowId, 0, this.getSlot(0).getItem()));
                            }
                        }
                        // CraftBukkit end
                    }
                }
            } else if (inventoryclicktype == InventoryClickType.SWAP && j >= 0 && j < 9) {
                slot2 = (Slot) this.c.get(i);
                itemstack1 = playerinventory.getItem(j);
                if (itemstack1 != null && itemstack1.count <= 0) {
                    itemstack1 = null;
                    playerinventory.setItem(j, (ItemStack) null);
                }

                itemstack3 = slot2.getItem();
                if (itemstack1 != null || itemstack3 != null) {
                    if (itemstack1 == null) {
                        if (slot2.isAllowed(entityhuman)) {
                            playerinventory.setItem(j, itemstack3);
                            slot2.set((ItemStack) null);
                            slot2.a(entityhuman, itemstack3);
                        }
                    } else if (itemstack3 == null) {
                        if (slot2.isAllowed(itemstack1)) {
                            j1 = slot2.getMaxStackSize(itemstack1);
                            if (itemstack1.count > j1) {
                                slot2.set(itemstack1.cloneAndSubtract(j1));
                            } else {
                                slot2.set(itemstack1);
                                playerinventory.setItem(j, (ItemStack) null);
                            }
                        }
                    } else if (slot2.isAllowed(entityhuman) && slot2.isAllowed(itemstack1)) {
                        j1 = slot2.getMaxStackSize(itemstack1);
                        if (itemstack1.count > j1) {
                            slot2.set(itemstack1.cloneAndSubtract(j1));
                            slot2.a(entityhuman, itemstack3);
                            if (!playerinventory.pickup(itemstack3)) {
                                entityhuman.drop(itemstack3, true);
                            }
                        } else {
                            slot2.set(itemstack1);
                            playerinventory.setItem(j, itemstack3);
                            slot2.a(entityhuman, itemstack3);
                        }
                    }
                }
            } else if (inventoryclicktype == InventoryClickType.CLONE && entityhuman.abilities.canInstantlyBuild && playerinventory.getCarried() == null && i >= 0) {
                slot2 = (Slot) this.c.get(i);
                if (slot2 != null && slot2.hasItem()) {
                    if (slot2.getItem().count > 0) {
                        itemstack1 = slot2.getItem().cloneItemStack();
                        itemstack1.count = itemstack1.getMaxStackSize();
                        playerinventory.setCarried(itemstack1);
                    } else {
                        slot2.set((ItemStack) null);
                    }
                }
            } else if (inventoryclicktype == InventoryClickType.THROW && playerinventory.getCarried() == null && i >= 0) {
                slot2 = (Slot) this.c.get(i);
                if (slot2 != null && slot2.hasItem() && slot2.isAllowed(entityhuman)) {
                    itemstack1 = slot2.a(j == 0 ? 1 : slot2.getItem().count);
                    slot2.a(entityhuman, itemstack1);
                    entityhuman.drop(itemstack1, true);
                }
            } else if (inventoryclicktype == InventoryClickType.PICKUP_ALL && i >= 0) {
                slot2 = (Slot) this.c.get(i);
                itemstack1 = playerinventory.getCarried();
                if (itemstack1 != null && (slot2 == null || !slot2.hasItem() || !slot2.isAllowed(entityhuman))) {
                    k = j == 0 ? 0 : this.c.size() - 1;
                    j1 = j == 0 ? 1 : -1;

                    for (int k1 = 0; k1 < 2; ++k1) {
                        for (int l1 = k; l1 >= 0 && l1 < this.c.size() && itemstack1.count < itemstack1.getMaxStackSize(); l1 += j1) {
                            Slot slot3 = (Slot) this.c.get(l1);

                            if (slot3.hasItem() && a(slot3, itemstack1, true) && slot3.isAllowed(entityhuman) && this.a(itemstack1, slot3) && (k1 != 0 || slot3.getItem().count != slot3.getItem().getMaxStackSize())) {
                                int i2 = Math.min(itemstack1.getMaxStackSize() - itemstack1.count, slot3.getItem().count);
                                ItemStack itemstack4 = slot3.a(i2);

                                itemstack1.count += i2;
                                if (itemstack4.count <= 0) {
                                    slot3.set((ItemStack) null);
                                }

                                slot3.a(entityhuman, itemstack4);
                            }
                        }
                    }
                }

                this.b();
            }
        }

        return itemstack;
    }

    public boolean a(ItemStack itemstack, Slot slot) {
        return true;
    }

    protected void a(int i, int j, boolean flag, EntityHuman entityhuman) {
        this.a(i, j, InventoryClickType.QUICK_MOVE, entityhuman);
    }

    public void b(EntityHuman entityhuman) {
        PlayerInventory playerinventory = entityhuman.inventory;

        if (playerinventory.getCarried() != null) {
            entityhuman.drop(playerinventory.getCarried(), false);
            playerinventory.setCarried((ItemStack) null);
        }

    }

    public void a(IInventory iinventory) {
        this.b();
    }

    public void setItem(int i, ItemStack itemstack) {
        this.getSlot(i).set(itemstack);
    }

    public boolean c(EntityHuman entityhuman) {
        return !this.i.contains(entityhuman);
    }

    public void a(EntityHuman entityhuman, boolean flag) {
        if (flag) {
            this.i.remove(entityhuman);
        } else {
            this.i.add(entityhuman);
        }

    }

    public abstract boolean a(EntityHuman entityhuman);

    protected boolean a(ItemStack itemstack, int i, int j, boolean flag) {
        boolean flag1 = false;
        int k = i;

        if (flag) {
            k = j - 1;
        }

        Slot slot;
        ItemStack itemstack1;

        if (itemstack.isStackable()) {
            while (itemstack.count > 0 && (!flag && k < j || flag && k >= i)) {
                slot = (Slot) this.c.get(k);
                itemstack1 = slot.getItem();
                if (itemstack1 != null && a(itemstack, itemstack1)) {
                    int l = itemstack1.count + itemstack.count;

                    // CraftBukkit start - itemstack.getMaxStackSize() -> maxStack
                    int maxStack = Math.min(itemstack.getMaxStackSize(), slot.getMaxStackSize());
                    if (l <= maxStack) {
                        itemstack.count = 0;
                        itemstack1.count = l;
                        slot.f();
                        flag1 = true;
                    } else if (itemstack1.count < maxStack) {
                        itemstack.count -= maxStack - itemstack1.count;
                        itemstack1.count = maxStack;
                        // CraftBukkit end
                        slot.f();
                        flag1 = true;
                    }
                }

                if (flag) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        if (itemstack.count > 0) {
            if (flag) {
                k = j - 1;
            } else {
                k = i;
            }

            while (!flag && k < j || flag && k >= i) {
                slot = (Slot) this.c.get(k);
                itemstack1 = slot.getItem();
                if (itemstack1 == null) {
                    slot.set(itemstack.cloneItemStack());
                    slot.f();
                    itemstack.count = 0;
                    flag1 = true;
                    break;
                }

                if (flag) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        return flag1;
    }

    private static boolean a(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack1.getItem() == itemstack.getItem() && (!itemstack.usesData() || itemstack.getData() == itemstack1.getData()) && ItemStack.equals(itemstack, itemstack1);
    }

    public static int b(int i) {
        return i >> 2 & 3;
    }

    public static int c(int i) {
        return i & 3;
    }

    public static boolean a(int i, EntityHuman entityhuman) {
        return i == 0 ? true : (i == 1 ? true : i == 2 && entityhuman.abilities.canInstantlyBuild);
    }

    protected void d() {
        this.g = 0;
        this.h.clear();
    }

    public static boolean a(Slot slot, ItemStack itemstack, boolean flag) {
        boolean flag1 = slot == null || !slot.hasItem();

        if (slot != null && slot.hasItem() && itemstack != null && itemstack.doMaterialsMatch(slot.getItem()) && ItemStack.equals(slot.getItem(), itemstack)) {
            flag1 |= slot.getItem().count + (flag ? 0 : itemstack.count) <= itemstack.getMaxStackSize();
        }

        return flag1;
    }

    public static void a(Set<Slot> set, int i, ItemStack itemstack, int j) {
        switch (i) {
        case 0:
            itemstack.count = MathHelper.d((float) itemstack.count / (float) set.size());
            break;

        case 1:
            itemstack.count = 1;
            break;

        case 2:
            itemstack.count = itemstack.getItem().getMaxStackSize();
        }

        itemstack.count += j;
    }

    public boolean b(Slot slot) {
        return true;
    }

    public static int a(@Nullable TileEntity tileentity) {
        return tileentity instanceof IInventory ? b((IInventory) tileentity) : 0;
    }

    public static int b(@Nullable IInventory iinventory) {
        if (iinventory == null) {
            return 0;
        } else {
            int i = 0;
            float f = 0.0F;

            for (int j = 0; j < iinventory.getSize(); ++j) {
                ItemStack itemstack = iinventory.getItem(j);

                if (itemstack != null) {
                    f += (float) itemstack.count / (float) Math.min(iinventory.getMaxStackSize(), itemstack.getMaxStackSize());
                    ++i;
                }
            }

            f /= (float) iinventory.getSize();
            return MathHelper.d(f * 14.0F) + (i > 0 ? 1 : 0);
        }
    }
}
