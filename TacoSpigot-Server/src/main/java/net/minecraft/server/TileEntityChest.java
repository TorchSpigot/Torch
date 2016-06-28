package net.minecraft.server;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
// CraftBukkit end

public class TileEntityChest extends TileEntityLootable implements IInventory { // Paper - Remove ITickable

    private ItemStack[] items = new ItemStack[27];
    public boolean a;
    public TileEntityChest f; // Paper - Adjacent Chest Z Neg
    public TileEntityChest g; // Paper - Adjacent Chest X Pos
    public TileEntityChest h; // Paper - Adjacent Chest X Neg
    public TileEntityChest i; // Paper - Adjacent Chest Z Pos
    public float j; // Paper - lid angle
    public float k;
    public int l; // Paper - Number of viewers
    private int p;
    private BlockChest.Type q;
    private String r;

    public TileEntityChest() {}

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

    public TileEntityChest(BlockChest.Type blockchest_type) {
        this.q = blockchest_type;
    }

    public int getSize() {
        return 27;
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

    public void setItem(int i, @Nullable ItemStack itemstack) {
        this.d((EntityHuman) null);
        this.items[i] = itemstack;
        if (itemstack != null && itemstack.count > this.getMaxStackSize()) {
            itemstack.count = this.getMaxStackSize();
        }

        this.update();
    }

    public String getName() {
        return this.hasCustomName() ? this.r : "container.chest";
    }

    public boolean hasCustomName() {
        return this.r != null && !this.r.isEmpty();
    }

    public void a(String s) {
        this.r = s;
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.items = new ItemStack[this.getSize()];
        if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
            this.r = nbttagcompound.getString("CustomName");
        }

        if (!this.d(nbttagcompound)) {
            NBTTagList nbttaglist = nbttagcompound.getList("Items", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.get(i);
                int j = nbttagcompound1.getByte("Slot") & 255;

                if (j >= 0 && j < this.items.length) {
                    this.items[j] = ItemStack.createStack(nbttagcompound1);
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

        if (this.hasCustomName()) {
            nbttagcompound.setString("CustomName", this.r);
        }

        return nbttagcompound;
    }

    public int getMaxStackSize() {
        return maxStack; // CraftBukkit
    }

    public boolean a(EntityHuman entityhuman) {
        if (this.world == null) return true; // CraftBukkit
        return this.world.getTileEntity(this.position) != this ? false : entityhuman.e((double) this.position.getX() + 0.5D, (double) this.position.getY() + 0.5D, (double) this.position.getZ() + 0.5D) <= 64.0D;
    }

    public void invalidateBlockCache() {
        super.invalidateBlockCache();
        this.a = false;
    }

    private void a(TileEntityChest tileentitychest, EnumDirection enumdirection) {
        if (tileentitychest.x()) {
            this.a = false;
        } else if (this.a) {
            switch (TileEntityChest.SyntheticClass_1.a[enumdirection.ordinal()]) {
            case 1:
                if (this.f != tileentitychest) {
                    this.a = false;
                }
                break;

            case 2:
                if (this.i != tileentitychest) {
                    this.a = false;
                }
                break;

            case 3:
                if (this.g != tileentitychest) {
                    this.a = false;
                }
                break;

            case 4:
                if (this.h != tileentitychest) {
                    this.a = false;
                }
            }
        }

    }

    public void m() {
        if (!this.a) {
            this.a = true;
            this.h = this.a(EnumDirection.WEST);
            this.g = this.a(EnumDirection.EAST);
            this.f = this.a(EnumDirection.NORTH);
            this.i = this.a(EnumDirection.SOUTH);
        }
    }

    @Nullable
    protected TileEntityChest a(EnumDirection enumdirection) {
        BlockPosition blockposition = this.position.shift(enumdirection);

        if (this.b(blockposition)) {
            TileEntity tileentity = this.world.getTileEntity(blockposition);

            if (tileentity instanceof TileEntityChest) {
                TileEntityChest tileentitychest = (TileEntityChest) tileentity;

                tileentitychest.a(this, enumdirection.opposite());
                return tileentitychest;
            }
        }

        return null;
    }

    private boolean b(BlockPosition blockposition) {
        if (this.world == null) {
            return false;
        } else {
            Block block = this.world.getType(blockposition).getBlock();

            return block instanceof BlockChest && ((BlockChest) block).g == this.o();
        }
    }

    public void c() {
        // Paper - Disable all of this, just in case this does get ticked
        /*
        this.m();
        int i = this.position.getX();
        int j = this.position.getY();
        int k = this.position.getZ();

        ++this.p;
        float f;

        if (!this.world.isClientSide && this.l != 0 && (this.p + i + j + k) % 200 == 0) {
            this.l = 0;
            f = 5.0F;
            List list = this.world.a(EntityHuman.class, new AxisAlignedBB((double) ((float) i - f), (double) ((float) j - f), (double) ((float) k - f), (double) ((float) (i + 1) + f), (double) ((float) (j + 1) + f), (double) ((float) (k + 1) + f)));
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityHuman entityhuman = (EntityHuman) iterator.next();

                if (entityhuman.activeContainer instanceof ContainerChest) {
                    IInventory iinventory = ((ContainerChest) entityhuman.activeContainer).e();

                    if (iinventory == this || iinventory instanceof InventoryLargeChest && ((InventoryLargeChest) iinventory).a((IInventory) this)) {
                        ++this.l;
                    }
                }
            }
        }

        this.k = this.j;
        f = 0.1F;
        double d0;

        if (this.l > 0 && this.j == 0.0F && this.f == null && this.h == null) {
            double d1 = (double) i + 0.5D;

            d0 = (double) k + 0.5D;
            if (this.i != null) {
                d0 += 0.5D;
            }

            if (this.g != null) {
                d1 += 0.5D;
            }

            this.world.a((EntityHuman) null, d1, (double) j + 0.5D, d0, SoundEffects.X, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
        }

        if (this.l == 0 && this.j > 0.0F || this.l > 0 && this.j < 1.0F) {
            float f1 = this.j;

            if (this.l > 0) {
                this.j += f;
            } else {
                this.j -= f;
            }

            if (this.j > 1.0F) {
                this.j = 1.0F;
            }

            float f2 = 0.5F;

            if (this.j < f2 && f1 >= f2 && this.f == null && this.h == null) {
                d0 = (double) i + 0.5D;
                double d2 = (double) k + 0.5D;

                if (this.i != null) {
                    d2 += 0.5D;
                }

                if (this.g != null) {
                    d0 += 0.5D;
                }

                this.world.a((EntityHuman) null, d0, (double) j + 0.5D, d2, SoundEffects.V, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
            }

            if (this.j < 0.0F) {
                this.j = 0.0F;
            }
        }
        */
        // Paper end
    }

    public boolean c(int i, int j) {
        if (i == 1) {
            this.l = j;
            return true;
        } else {
            return super.c(i, j);
        }
    }

    public void startOpen(EntityHuman entityhuman) {
        if (!entityhuman.isSpectator()) {
            if (this.l < 0) {
                this.l = 0;
            }
            int oldPower = Math.max(0, Math.min(15, this.l)); // CraftBukkit - Get power before new viewer is added

            ++this.l;

            // Paper start - Move chest open sound out of the tick loop
            this.m();

            if (this.l > 0 && this.j == 0.0F && this.f == null && this.h == null) {
                this.j = 0.7F;

                double d0 = (double) this.position.getZ() + 0.5D;
                double d1 = (double) this.position.getX() + 0.5D;

                if (this.i != null) {
                    d0 += 0.5D;
                }

                if (this.g != null) {
                    d1 += 0.5D;
                }

                this.world.a((EntityHuman) null, d1, (double) this.position.getY() + 0.5D, d0, SoundEffects.X, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
            }
            // Paper end

            if (this.world == null) return; // CraftBukkit
            this.world.playBlockAction(this.position, this.getBlock(), 1, this.l);

            // CraftBukkit start - Call redstone event
            if (this.getBlock() == Blocks.TRAPPED_CHEST) {
                int newPower = Math.max(0, Math.min(15, this.l));

                if (oldPower != newPower) {
                    org.bukkit.craftbukkit.event.CraftEventFactory.callRedstoneChange(world, position.getX(), position.getY(), position.getZ(), oldPower, newPower);
                }
            }
            // CraftBukkit end
            this.world.applyPhysics(this.position, this.getBlock());
            this.world.applyPhysics(this.position.down(), this.getBlock());
        }

    }

    public void closeContainer(EntityHuman entityhuman) {
        if (!entityhuman.isSpectator() && this.getBlock() instanceof BlockChest) {
            int oldPower = Math.max(0, Math.min(15, this.l)); // CraftBukkit - Get power before new viewer is added
            --this.l;
            if (this.world == null) return; // CraftBukkit

            // Paper start - Move chest close sound out of the tick loop
            if (this.l == 0 && this.j > 0.0F || this.l > 0 && this.j < 1.0F) {
                float f = 0.1F;

                if (this.l > 0) {
                    this.j += f;
                } else {
                    this.j -= f;
                }

                double d0 = (double) this.getPosition().getX() + 0.5D;
                double d2 = (double) this.getPosition().getZ() + 0.5D;
                int yLoc = this.position.getY();

                if (this.i != null) {
                    d2 += 0.5D;
                }

                if (this.g != null) {
                    d0 += 0.5D;
                }

                this.world.a((EntityHuman) null, d0, (double) yLoc + 0.5D, d2, SoundEffects.V, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
                this.j = 0.0F;
            }
            // Paper end

            this.world.playBlockAction(this.position, this.getBlock(), 1, this.l);

            // CraftBukkit start - Call redstone event
            if (this.getBlock() == Blocks.TRAPPED_CHEST) {
                int newPower = Math.max(0, Math.min(15, this.l));

                if (oldPower != newPower) {
                    org.bukkit.craftbukkit.event.CraftEventFactory.callRedstoneChange(world, position.getX(), position.getY(), position.getZ(), oldPower, newPower);
                }
            }
            // CraftBukkit end
            this.world.applyPhysics(this.position, this.getBlock());
            this.world.applyPhysics(this.position.down(), this.getBlock());
        }

    }

    public boolean b(int i, ItemStack itemstack) {
        return true;
    }

    public void y() {
        super.y();
        this.invalidateBlockCache();
        this.m();
    }

    public BlockChest.Type o() {
        if (this.q == null) {
            if (this.world == null || !(this.getBlock() instanceof BlockChest)) {
                return BlockChest.Type.BASIC;
            }

            this.q = ((BlockChest) this.getBlock()).g;
        }

        return this.q;
    }

    public String getContainerName() {
        return "minecraft:chest";
    }

    public Container createContainer(PlayerInventory playerinventory, EntityHuman entityhuman) {
        this.d(entityhuman);
        return new ContainerChest(playerinventory, this, entityhuman);
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

    // CraftBukkit start
    @Override
    public boolean isFilteredNBT() {
        return true;
    }
    // CraftBukkit end

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumDirection.values().length];

        static {
            try {
                TileEntityChest.SyntheticClass_1.a[EnumDirection.NORTH.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                TileEntityChest.SyntheticClass_1.a[EnumDirection.SOUTH.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                TileEntityChest.SyntheticClass_1.a[EnumDirection.EAST.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                TileEntityChest.SyntheticClass_1.a[EnumDirection.WEST.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

        }
    }
}
