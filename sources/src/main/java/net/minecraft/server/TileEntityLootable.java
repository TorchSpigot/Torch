package net.minecraft.server;

import com.destroystokyo.paper.loottable.CraftLootableInventoryData; // Paper
import com.destroystokyo.paper.loottable.CraftLootableInventory; // Paper
import com.destroystokyo.paper.loottable.LootableInventory; // Paper

import java.util.Random;
import javax.annotation.Nullable;

public abstract class TileEntityLootable extends TileEntityContainer implements ILootable, CraftLootableInventory { // Paper

    protected MinecraftKey m;
    protected long n; public long getLootTableSeed() { return n; } // Paper - OBFHELPER
    protected String o;

    public TileEntityLootable() {}

    protected boolean c(NBTTagCompound nbttagcompound) {
        lootableData.loadNbt(nbttagcompound); // Paper
        if (nbttagcompound.hasKeyOfType("LootTable", 8)) {
            this.m = new MinecraftKey(nbttagcompound.getString("LootTable"));
            this.n = nbttagcompound.getLong("LootTableSeed");
            return false; // Paper - always load the items, table may still remain
        } else {
            return false;
        }
    }

    protected boolean d(NBTTagCompound nbttagcompound) {
        lootableData.saveNbt(nbttagcompound); // Paper
        if (this.m != null) {
            nbttagcompound.setString("LootTable", this.m.toString());
            if (this.n != 0L) {
                nbttagcompound.setLong("LootTableSeed", this.n);
            }

            return false; // Paper - always save the items, table may still remain
        } else {
            return false;
        }
    }

    public void d(@Nullable EntityHuman entityhuman) {
        if (lootableData.shouldReplenish(entityhuman)) { // Paper
            LootTable loottable = this.world.ak().a(this.m);

            lootableData.processRefill(entityhuman); // Paper
            Random random;

            if (this.n == 0L) {
                random = new Random();
            } else {
                random = new Random(this.n);
            }

            LootTableInfo.a loottableinfo_a = new LootTableInfo.a((WorldServer) this.world);

            if (entityhuman != null) {
                loottableinfo_a.a(entityhuman.dj());
            }

            loottable.a(this, random, loottableinfo_a.a());
        }

    }

    public MinecraftKey getLootTableKey() { return b(); } // Paper - OBFHELPER
    public MinecraftKey b() {
        return this.m;
    }

    public void setLootTable(MinecraftKey key, long seed) { a(key, seed);} // Paper - OBFHELPER
    public void a(MinecraftKey minecraftkey, long i) {
        this.m = minecraftkey;
        this.n = i;
    }

    public boolean hasCustomName() {
        return this.o != null && !this.o.isEmpty();
    }

    public void a(String s) {
        this.o = s;
    }

    public ItemStack getItem(int i) {
        this.d((EntityHuman) null);
        return (ItemStack) this.q().get(i);
    }

    public ItemStack splitStack(int i, int j) {
        this.d((EntityHuman) null);
        ItemStack itemstack = ContainerUtil.a(this.q(), i, j);

        if (!itemstack.isEmpty()) {
            this.update();
        }

        return itemstack;
    }

    public ItemStack splitWithoutUpdate(int i) {
        this.d((EntityHuman) null);
        return ContainerUtil.a(this.q(), i);
    }

    public void setItem(int i, @Nullable ItemStack itemstack) {
        this.d((EntityHuman) null);
        this.q().set(i, itemstack);
        if (itemstack.getCount() > this.getMaxStackSize()) {
            itemstack.setCount(this.getMaxStackSize());
        }

        this.update();
    }

    public boolean a(EntityHuman entityhuman) {
        return this.world.getTileEntity(this.position) != this ? false : entityhuman.d((double) this.position.getX() + 0.5D, (double) this.position.getY() + 0.5D, (double) this.position.getZ() + 0.5D) <= 64.0D;
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

    public int h() {
        return 0;
    }

    public void clear() {
        this.d((EntityHuman) null);
        this.q().clear();
    }

    protected abstract NonNullList<ItemStack> q();

    // Paper start - LootTable API
    private final CraftLootableInventoryData lootableData = new CraftLootableInventoryData(this);

    @Override
    public CraftLootableInventoryData getLootableData() {
        return lootableData;
    }

    @Override
    public LootableInventory getAPILootableInventory() {
        return (LootableInventory) getBukkitWorld().getBlockAt(MCUtil.toLocation(world, getPosition())).getState();
    }

    @Override
    public World getNMSWorld() {
        return world;
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
        this.m = (MinecraftKey) null;
    }
    // Paper end

}
