package net.minecraft.server;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
// CraftBukkit end
// TacoSpigot start
import org.bukkit.craftbukkit.entity.CraftItem;
import net.techcable.tacospigot.HopperHelper;
// TacoSpigot end

public class TileEntityHopper extends TileEntityLootable implements IHopper, ITickable {

    private ItemStack[] items = new ItemStack[5];
    private String f;
    private int g = -1;

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

    public TileEntityHopper() {}

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.items = new ItemStack[this.getSize()];
        if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
            this.f = nbttagcompound.getString("CustomName");
        }

        this.g = nbttagcompound.getInt("TransferCooldown");
        if (!this.d(nbttagcompound)) {
            NBTTagList nbttaglist = nbttagcompound.getList("Items", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.get(i);
                byte b0 = nbttagcompound1.getByte("Slot");

                if (b0 >= 0 && b0 < this.items.length) {
                    this.items[b0] = ItemStack.createStack(nbttagcompound1);
                }
            }
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

        nbttagcompound.setInt("TransferCooldown", this.g);
        if (this.hasCustomName()) {
            nbttagcompound.setString("CustomName", this.f);
        }

        return nbttagcompound;
    }

    public int getSize() {
        return this.items.length;
    }

    @Nullable
    public ItemStack getItem(int i) {
        this.d((EntityHuman) null);
        return this.items[i];
    }

    @Nullable
    public ItemStack splitStack(int i, int j) {
        this.d((EntityHuman) null);
        return ContainerUtil.a(this.items, i, j);
    }

    @Nullable
    public ItemStack splitWithoutUpdate(int i) {
        this.d((EntityHuman) null);
        return ContainerUtil.a(this.items, i);
    }

    public void setItem(int i, @Nullable ItemStack itemstack) {
        this.d((EntityHuman) null);
        this.items[i] = itemstack;
        if (itemstack != null && itemstack.count > this.getMaxStackSize()) {
            itemstack.count = this.getMaxStackSize();
        }

    }

    public String getName() {
        return this.hasCustomName() ? this.f : "container.hopper";
    }

    public boolean hasCustomName() {
        return this.f != null && !this.f.isEmpty();
    }

    public void a(String s) {
        this.f = s;
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

    public void c() {
        mayAcceptItems = false; // Paper - at the beginning of a tick, assume we can't accept items
        if (this.world != null && !this.world.isClientSide) {
            --this.g;
            if (!this.o()) {
                this.setCooldown(0);
                this.m();
            }

        }
    }

    public boolean m() {
        if (this.world != null && !this.world.isClientSide) {
            if (!this.o() && BlockHopper.f(this.u())) {
                boolean flag = false;

                if (!this.q()) {
                    flag = this.H();
                }

                if (!this.r()) {
                    mayAcceptItems = true; // Paper - flag this hopper to be able to accept items
                    flag = a((IHopper) this) || flag;
                }

                if (flag) {
                    this.setCooldown(world.spigotConfig.hopperTransfer); // Spigot
                    this.update();
                    return true;
                }

                // Paper start
                if (world.paperConfig.useHopperCheck && !this.o()) {
                    this.setCooldown(4); // Torch re-set correctly
                }
                // Paper end
            }

			if (!isCooledDown() && isFull()) { setCooldown(world.spigotConfig.hopperTransfer); } // Torch - backport
            return false;
        } else {
            return false;
        }
    }

    // Paper start
    private boolean mayAcceptItems = true;

    public boolean canAcceptItems() {
        return mayAcceptItems;
    }
    // Paper end

	boolean isFull() { return q(); } // Paper // OBFHELPER // Torch - backport
    private boolean q() {
        ItemStack[] aitemstack = this.items;
        int i = aitemstack.length;

        for (int j = 0; j < i; ++j) {
            ItemStack itemstack = aitemstack[j];

            if (itemstack != null) {
                return false;
            }
        }

        return true;
    }

    private boolean r() {
        ItemStack[] aitemstack = this.items;
        int i = aitemstack.length;

        for (int j = 0; j < i; ++j) {
            ItemStack itemstack = aitemstack[j];

            if (itemstack == null || itemstack.count != itemstack.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    private boolean H() {
        IInventory iinventory = HopperHelper.getInventory(getWorld(), getPosition().shift(BlockHopper.e(this.u()))); // TacoSpigot - avoid bounding box search

        if (iinventory == null) {
            return false;
        } else {
            EnumDirection enumdirection = BlockHopper.e(this.u()).opposite();

            if (this.a(iinventory, enumdirection)) {
                return false;
            } else {
                for (int i = 0; i < this.getSize(); ++i) {
                    if (this.getItem(i) != null) {
                        ItemStack itemstack = this.getItem(i).cloneItemStack();
                        // ItemStack itemstack1 = addItem(iinventory, this.splitStack(i, 1), enumdirection);

                        // CraftBukkit start - Call event when pushing items into other inventories
                        CraftItemStack oitemstack = CraftItemStack.asCraftMirror(this.splitStack(i, world.spigotConfig.hopperAmount)); // Spigot
                        // TacoSpigot start - option to disable event
                        final org.bukkit.inventory.ItemStack stack;
                        if (HopperHelper.isFireInventoryMoveItemEvent(this)) {
                            // TacoSpigot end
                        Inventory destinationInventory;
                        // Have to special case large chests as they work oddly
                        if (iinventory instanceof InventoryLargeChest) {
                            destinationInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((InventoryLargeChest) iinventory);
                        } else {
                            destinationInventory = iinventory.getOwner().getInventory();
                        }

                        InventoryMoveItemEvent event = new InventoryMoveItemEvent(this.getOwner().getInventory(), oitemstack.clone(), destinationInventory, true);
                        this.getWorld().getServer().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            this.setItem(i, itemstack);
                            this.setCooldown(world.spigotConfig.hopperTransfer); // Spigot
                            return false;
                        }
                            // TacoSpigot start
                            stack = event.getItem();
                        } else {
                            stack = oitemstack;
                        }
                        int origCount = stack.getAmount(); // Spigot
                        ItemStack itemstack1 = addItem(iinventory, CraftItemStack.asNMSCopy(stack), enumdirection);
                        // TacoSpigot end

                        if (itemstack1 == null || itemstack1.count == 0) {
                            if (stack.equals(oitemstack)) { // TacoSpigot - 'event.getItem' -> stack
                                iinventory.update();
                            } else {
                                this.setItem(i, itemstack);
                            }
                            // CraftBukkit end
                            return true;
                        }
                        itemstack.count -= origCount - itemstack1.count; // Spigot
                        this.setItem(i, itemstack);
                    }
                }

                return false;
            }
        }
    }

    private boolean a(IInventory iinventory, EnumDirection enumdirection) {
        if (iinventory instanceof IWorldInventory) {
            IWorldInventory iworldinventory = (IWorldInventory) iinventory;
            int[] aint = iworldinventory.getSlotsForFace(enumdirection);

            for (int i = 0; i < aint.length; ++i) {
                ItemStack itemstack = iworldinventory.getItem(aint[i]);

                if (itemstack == null || itemstack.count != itemstack.getMaxStackSize()) {
                    return false;
                }
            }
        } else {
            int j = iinventory.getSize();

            for (int k = 0; k < j; ++k) {
                ItemStack itemstack1 = iinventory.getItem(k);

                if (itemstack1 == null || itemstack1.count != itemstack1.getMaxStackSize()) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean b(IInventory iinventory, EnumDirection enumdirection) {
        if (iinventory instanceof IWorldInventory) {
            IWorldInventory iworldinventory = (IWorldInventory) iinventory;
            int[] aint = iworldinventory.getSlotsForFace(enumdirection);

            for (int i = 0; i < aint.length; ++i) {
                if (iworldinventory.getItem(aint[i]) != null) {
                    return false;
                }
            }
        } else {
            int j = iinventory.getSize();

            for (int k = 0; k < j; ++k) {
                if (iinventory.getItem(k) != null) {
                    return false;
                }
            }
        }

        return true;
    }

  // Paper start - split methods, and only do entity lookup if in pull mode
    public static boolean a(IHopper ihopper) {
       IInventory iinventory = getInventory(ihopper, !(ihopper instanceof TileEntityHopper) || !ihopper.getWorld().paperConfig.isHopperPushBased);

       return acceptItem(ihopper, iinventory);
   }

   public static boolean acceptItem(IHopper ihopper, IInventory iinventory) {
       // Paper end

        if (iinventory != null) {
            EnumDirection enumdirection = EnumDirection.DOWN;

            if (b(iinventory, enumdirection)) {
                return false;
            }

            if (iinventory instanceof IWorldInventory) {
                IWorldInventory iworldinventory = (IWorldInventory) iinventory;
                int[] aint = iworldinventory.getSlotsForFace(enumdirection);

                for (int i = 0; i < aint.length; ++i) {
                    if (a(ihopper, iinventory, aint[i], enumdirection)) {
                        return true;
                    }
                }
            } else {
                int j = iinventory.getSize();

                for (int k = 0; k < j; ++k) {
                    if (a(ihopper, iinventory, k, enumdirection)) {
                        return true;
                    }
                }
            }
        } else if (!ihopper.getWorld().paperConfig.isHopperPushBased || !(ihopper instanceof TileEntityHopper)) { // Paper - only search for entities in 'pull mode'
            Iterator iterator = a(ihopper.getWorld(), ihopper.E(), ihopper.F(), ihopper.G()).iterator(); // Change getHopperLookupBoundingBox() if this ever changes

            while (iterator.hasNext()) {
                EntityItem entityitem = (EntityItem) iterator.next();

                if (a((IInventory) ihopper, entityitem)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean a(IHopper ihopper, IInventory iinventory, int i, EnumDirection enumdirection) {
        ItemStack itemstack = iinventory.getItem(i);

        if (itemstack != null && b(iinventory, itemstack, i, enumdirection)) {
            ItemStack itemstack1 = itemstack.cloneItemStack();
            // ItemStack itemstack2 = addItem(ihopper, iinventory.splitStack(i, 1), (EnumDirection) null);
            // CraftBukkit start - Call event on collection of items from inventories into the hopper
            CraftItemStack oitemstack = CraftItemStack.asCraftMirror(iinventory.splitStack(i, ihopper.getWorld().spigotConfig.hopperAmount)); // Spigot
            // TacoSpigot start - option to disable event
            final org.bukkit.inventory.ItemStack stack;
            if (HopperHelper.isFireInventoryMoveItemEvent(ihopper)) {
                // TacoSpigot end

            Inventory sourceInventory;
            // Have to special case large chests as they work oddly
            if (iinventory instanceof InventoryLargeChest) {
                sourceInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((InventoryLargeChest) iinventory);
            } else {
                sourceInventory = iinventory.getOwner().getInventory();
            }

            InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, oitemstack.clone(), ihopper.getOwner().getInventory(), false);

            ihopper.getWorld().getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                iinventory.setItem(i, itemstack1);

                if (ihopper instanceof TileEntityHopper) {
                    ((TileEntityHopper) ihopper).setCooldown(ihopper.getWorld().spigotConfig.hopperTransfer); // Spigot
                } else if (ihopper instanceof EntityMinecartHopper) {
                    ((EntityMinecartHopper) ihopper).setCooldown(ihopper.getWorld().spigotConfig.hopperTransfer / 2); // Spigot
                }
                return false;
            }
                // TacoSpigot start
                stack = event.getItem();
            } else {
                stack = oitemstack;
            }
            int origCount = stack.getAmount(); // Spigot
            ItemStack itemstack2 = addItem(ihopper, CraftItemStack.asNMSCopy(stack), null);
            // TacoSpigot end

            if (itemstack2 == null || itemstack2.count == 0) {
                if (stack.equals(oitemstack)) { // TacoSpigot - 'event.getItem()' -> stack
                    iinventory.update();
                } else {
                    iinventory.setItem(i, itemstack1);
                }
                // CraftBukkit end
                return true;
            }
            itemstack1.count -= origCount - itemstack2.count; // Spigot

            iinventory.setItem(i, itemstack1);
        }

        return false;
    }

    public static boolean a(IInventory iinventory, EntityItem entityitem) {
        boolean flag = false;

        if (entityitem == null) {
            return false;
        } else {
            // CraftBukkit start
            InventoryPickupItemEvent event = new InventoryPickupItemEvent(iinventory.getOwner().getInventory(), (org.bukkit.entity.Item) entityitem.getBukkitEntity());
            entityitem.world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            // CraftBukkit end
            ItemStack itemstack = entityitem.getItemStack().cloneItemStack();
            ItemStack itemstack1 = addItem(iinventory, itemstack, (EnumDirection) null);

            if (itemstack1 != null && itemstack1.count != 0) {
                entityitem.setItemStack(itemstack1);
            } else {
                flag = true;
                entityitem.die();
            }

            return flag;
        }
    }

    public static ItemStack addItem(IInventory iinventory, ItemStack itemstack, @Nullable EnumDirection enumdirection) {
        if (iinventory instanceof IWorldInventory && enumdirection != null) {
            IWorldInventory iworldinventory = (IWorldInventory) iinventory;
            int[] aint = iworldinventory.getSlotsForFace(enumdirection);

            for (int i = 0; i < aint.length && itemstack != null && itemstack.count > 0; ++i) {
                itemstack = c(iinventory, itemstack, aint[i], enumdirection);
            }
        } else {
            int j = iinventory.getSize();

            for (int k = 0; k < j && itemstack != null && itemstack.count > 0; ++k) {
                itemstack = c(iinventory, itemstack, k, enumdirection);
            }
        }

        if (itemstack != null && itemstack.count == 0) {
            itemstack = null;
        }

        return itemstack;
    }

    private static boolean a(IInventory iinventory, ItemStack itemstack, int i, EnumDirection enumdirection) {
        return !iinventory.b(i, itemstack) ? false : !(iinventory instanceof IWorldInventory) || ((IWorldInventory) iinventory).canPlaceItemThroughFace(i, itemstack, enumdirection);
    }

    private static boolean b(IInventory iinventory, ItemStack itemstack, int i, EnumDirection enumdirection) {
        return !(iinventory instanceof IWorldInventory) || ((IWorldInventory) iinventory).canTakeItemThroughFace(i, itemstack, enumdirection);
    }

    private static ItemStack c(IInventory iinventory, ItemStack itemstack, int i, EnumDirection enumdirection) {
        ItemStack itemstack1 = iinventory.getItem(i);

        if (a(iinventory, itemstack, i, enumdirection)) {
            boolean flag = false;

            if (itemstack1 == null) {
                IGNORE_TILE_UPDATES = true; // Paper
                iinventory.setItem(i, itemstack);
                IGNORE_TILE_UPDATES = false; // Paper
                itemstack = null;
                flag = true;
            } else if (a(itemstack1, itemstack)) {
                int j = itemstack.getMaxStackSize() - itemstack1.count;
                int k = Math.min(itemstack.count, j);

                itemstack.count -= k;
                itemstack1.count += k;
                flag = k > 0;
            }

            if (flag) {
                if (iinventory instanceof TileEntityHopper) {
                    TileEntityHopper tileentityhopper = (TileEntityHopper) iinventory;

                    if (tileentityhopper.p()) {
                        tileentityhopper.setCooldown(tileentityhopper.world.spigotConfig.hopperTransfer); // Spigot
                    }

                    //iinventory.update(); // Paper
                }

                iinventory.update();
            }
        }

        return itemstack;
    }

    private IInventory I() {
        EnumDirection enumdirection = BlockHopper.e(this.u());

        // Paper start - don't search for entities in push mode
        World world = getWorld();
        return getInventory(world, this.E() + (double) enumdirection.getAdjacentX(), this.F() + (double) enumdirection.getAdjacentY(), this.G() + (double) enumdirection.getAdjacentZ(), !world.paperConfig.isHopperPushBased);
        // Paper endtcon
    }

    // Paper start - add option to search for entities
   public static IInventory b(IHopper hopper) {
       return getInventory(hopper, true);
   }

   public static IInventory getInventory(IHopper ihopper, boolean searchForEntities) {
       return getInventory(ihopper.getWorld(), ihopper.E(), ihopper.F() + 1.0D, ihopper.G(), searchForEntities);
       // Paper end
    }

    public static List<EntityItem> a(World world, double d0, double d1, double d2) {
        return world.a(EntityItem.class, new AxisAlignedBB(d0 - 0.5D, d1, d2 - 0.5D, d0 + 0.5D, d1 + 1.5D, d2 + 0.5D), IEntitySelector.a); // Change getHopperLookupBoundingBox(double, double, double) if the bounding box calculation is ever changed
    }

   // Paper start
   public AxisAlignedBB getHopperLookupBoundingBox() {
       return getHopperLookupBoundingBox(this.getX(), this.getY(), this.getZ());
   }
 
   private static AxisAlignedBB getHopperLookupBoundingBox(double d0, double d1, double d2) {
       // Change this if a(World, double, double, double) above ever changes
       return new AxisAlignedBB(d0 - 0.5D, d1, d2 - 0.5D, d0 + 0.5D, d1 + 1.5D, d2 + 0.5D);
   }
   // Paper end

   // Paper start - add option to searchForEntities
    public static IInventory b(World world, double d0, double d1, double d2) {
		return getInventory(world, d0, d1, d2, true);
    }

    public static IInventory getInventory(World world, double d0, double d1, double d2, boolean searchForEntities) {
        // Paper end
        Object object = null;
        int i = MathHelper.floor(d0);
        int j = MathHelper.floor(d1);
        int k = MathHelper.floor(d2);
        BlockPosition blockposition = new BlockPosition(i, j, k);
        if ( !world.isLoaded( blockposition ) ) return null; // Spigot
        Block block = world.getType(blockposition).getBlock();

        if (block.isTileEntity()) {
            TileEntity tileentity = world.getTileEntity(blockposition);

            if (tileentity instanceof IInventory) {
                object = (IInventory) tileentity;
                if (object instanceof TileEntityChest && block instanceof BlockChest) {
                    object = ((BlockChest) block).a(world, blockposition, true);
                }
            }
        }

        if (object == null && searchForEntities) { // Paper - only if searchForEntities
            List list = world.getEntities((Entity) null, new AxisAlignedBB(d0 - 0.5D, d1 - 0.5D, d2 - 0.5D, d0 + 0.5D, d1 + 0.5D, d2 + 0.5D), IEntitySelector.c);

            if (!list.isEmpty()) {
                object = (IInventory) list.get(world.random.nextInt(list.size()));
            }
        }

        return (IInventory) object;
    }

    private static boolean a(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack.getItem() != itemstack1.getItem() ? false : (itemstack.getData() != itemstack1.getData() ? false : (itemstack.count > itemstack.getMaxStackSize() ? false : ItemStack.equals(itemstack, itemstack1)));
    }

    public double E() {
        return (double) this.position.getX() + 0.5D;
    }

    public double F() {
        return (double) this.position.getY() + 0.5D;
    }

    public double G() {
        return (double) this.position.getZ() + 0.5D;
    }

    public void setCooldown(int i) {
        this.g = i;
    }

    boolean isCooledDown() { return o(); } // Paper // OBFHELPER
    public boolean o() {
        return this.g > 0;
    }

    public boolean p() {
        return this.g <= 1;
    }

    public String getContainerName() {
        return "minecraft:hopper";
    }

    public Container createContainer(PlayerInventory playerinventory, EntityHuman entityhuman) {
        this.d(entityhuman);
        return new ContainerHopper(playerinventory, this, entityhuman);
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
