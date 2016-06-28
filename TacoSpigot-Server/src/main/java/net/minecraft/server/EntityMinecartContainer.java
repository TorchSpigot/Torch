package net.minecraft.server;

import java.util.Random;
import javax.annotation.Nullable;
// CraftBukkit start
import java.util.List;
import org.bukkit.Location;

import com.destroystokyo.paper.loottable.CraftLootableInventoryData; // Paper
import com.destroystokyo.paper.loottable.CraftLootableInventory; // Paper
import com.destroystokyo.paper.loottable.LootableInventory; // Paper
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
// CraftBukkit end
import com.destroystokyo.paper.HopperPusher; // Paper

// Paper start - push into hoppers
	public abstract class EntityMinecartContainer extends EntityMinecartAbstract implements ITileInventory, ILootable, CraftLootableInventory, HopperPusher { // Paper - CraftLootableInventory
   @Override
   public boolean acceptItem(TileEntityHopper hopper) {
       return TileEntityHopper.acceptItem(hopper, this);
   }

   @Override
   public void m() {
       super.m();
       tryPutInHopper();
   }

   @Override
   public void inactiveTick() {
       super.inactiveTick();
       tryPutInHopper();
   }
   // Paper end

    private ItemStack[] items = new ItemStack[27]; // CraftBukkit - 36 -> 27
    private boolean b = true;
    private MinecraftKey c;
    private long d;public long getLootTableSeed() { return d; } // Paper // OBFHELPER

    // CraftBukkit start
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

    public InventoryHolder getOwner() {
        org.bukkit.entity.Entity cart = getBukkitEntity();
        if(cart instanceof InventoryHolder) return (InventoryHolder) cart;
        return null;
    }

    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public Location getLocation() {
        return getBukkitEntity().getLocation();
    }
    // CraftBukkit end

    public EntityMinecartContainer(World world) {
        super(world);
    }

    public EntityMinecartContainer(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    public void a(DamageSource damagesource) {
        super.a(damagesource);
        if (this.world.getGameRules().getBoolean("doEntityDrops")) {
            InventoryUtils.dropEntity(this.world, this, this);
        }

    }

    @Nullable
    public ItemStack getItem(int i) {
        this.f((EntityHuman) null);
        return this.items[i];
    }

    @Nullable
    public ItemStack splitStack(int i, int j) {
        this.f((EntityHuman) null);
        return ContainerUtil.a(this.items, i, j);
    }

    @Nullable
    public ItemStack splitWithoutUpdate(int i) {
        this.f((EntityHuman) null);
        if (this.items[i] != null) {
            ItemStack itemstack = this.items[i];

            this.items[i] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setItem(int i, @Nullable ItemStack itemstack) {
        this.f((EntityHuman) null);
        this.items[i] = itemstack;
        if (itemstack != null && itemstack.count > this.getMaxStackSize()) {
            itemstack.count = this.getMaxStackSize();
        }

    }

    public void update() {}

    public boolean a(EntityHuman entityhuman) {
        return this.dead ? false : entityhuman.h(this) <= 64.0D;
    }

    public void startOpen(EntityHuman entityhuman) {}

    public void closeContainer(EntityHuman entityhuman) {}

    public boolean b(int i, ItemStack itemstack) {
        return true;
    }

    public int getMaxStackSize() {
        return maxStack; // CraftBukkit
    }

    @Nullable
    public Entity c(int i) {
        this.b = false;
        return super.c(i);
    }

    public void die() {
        if (this.b) {
            InventoryUtils.dropEntity(this.world, this, this);
        }

        super.die();
    }

    public void b(boolean flag) {
        this.b = flag;
    }

    protected void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        lootableData.saveNbt(nbttagcompound); // Paper
        if (this.c != null) {
            nbttagcompound.setString("LootTable", this.c.toString());
            if (this.d != 0L) {
                nbttagcompound.setLong("LootTableSeed", this.d);
            }
        } if (true) { // Paper - Always save the items, Table may stick around
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

    }

    protected void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.items = new ItemStack[this.getSize()];
        lootableData.loadNbt(nbttagcompound); // Paper
        if (nbttagcompound.hasKeyOfType("LootTable", 8)) {
            this.c = new MinecraftKey(nbttagcompound.getString("LootTable"));
            this.d = nbttagcompound.getLong("LootTableSeed");
        } if (true) { // Paper - always load the items, table may still remain
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

    public boolean a(EntityHuman entityhuman, @Nullable ItemStack itemstack, EnumHand enumhand) {
        if (!this.world.isClientSide) {
            entityhuman.openContainer(this);
        }

        return true;
    }

    protected void r() {
        float f = 0.98F;

        if (this.c == null) {
            int i = 15 - Container.b((IInventory) this);

            f += (float) i * 0.001F;
        }

        this.motX *= (double) f;
        this.motY *= 0.0D;
        this.motZ *= (double) f;
    }

    public int getProperty(int i) {
        return 0;
    }

    public void setProperty(int i, int j) {}

    public int g() {
        return 0;
    }

    public boolean x_() {
        return false;
    }

    public void a(ChestLock chestlock) {}

    public ChestLock y_() {
        return ChestLock.a;
    }

    public void f(@Nullable EntityHuman entityhuman) {
        if (lootableData.shouldReplenish(entityhuman)) { // Paper
            LootTable loottable = this.world.ak().a(this.c);

            lootableData.processRefill(entityhuman); // Paper
            Random random;

            if (this.d == 0L) {
                random = new Random();
            } else {
                random = new Random(this.d);
            }

            LootTableInfo.a loottableinfo_a = new LootTableInfo.a((WorldServer) this.world);

            if (entityhuman != null) {
                loottableinfo_a.a(entityhuman.dc());
            }

            loottable.a(this, random, loottableinfo_a.a());
        }

    }

    public void l() {
        this.f((EntityHuman) null);

        for (int i = 0; i < this.items.length; ++i) {
            this.items[i] = null;
        }

    }

    public void setLootTable(MinecraftKey key, long seed) { a(key, seed);} // Paper // OBFHELPER
    public void a(MinecraftKey minecraftkey, long i) {
        this.c = minecraftkey;
        this.d = i;
    }


    public MinecraftKey getLootTableKey() { return b(); } // Paper // OBFHELPER
    public MinecraftKey b() {
        return this.c;
    }

    // Paper start
    private final CraftLootableInventoryData lootableData = new CraftLootableInventoryData(this);

    @Override
    public CraftLootableInventoryData getLootableData() {
        return lootableData;
    }

    @Override
    public LootableInventory getAPILootableInventory() {
        return (LootableInventory) this.getBukkitEntity();
    }

    @Override
    public World getNMSWorld() {
        return this.world;
    }

    public String getLootTableName() {
        final MinecraftKey key = getLootTableKey();
        return key != null ? key.toString() : null;
    }

    @Override
    public String setLootTable(String name, long seed) {
        String prev = getLootTableName();
        setLootTable(new MinecraftKey(name), seed);
        return prev;
    }

    @Override
    public void clearLootTable() {
        //noinspection RedundantCast
        this.c = (MinecraftKey) null;
    }
    // Paper end
}
