package net.minecraft.server;

import com.destroystokyo.paper.loottable.CraftLootableInventoryData; // Paper
import com.destroystokyo.paper.loottable.CraftLootableInventory; // Paper
import com.destroystokyo.paper.loottable.LootableInventory; // Paper

import java.util.Random;
import javax.annotation.Nullable;

public abstract class TileEntityLootable extends TileEntityContainer implements ILootable, CraftLootableInventory { // Paper

    protected MinecraftKey m;
    protected long n; public long getLootTableSeed() { return n; } // Paper // OBFHELPER

    public TileEntityLootable() {}

    protected boolean d(NBTTagCompound nbttagcompound) {
        lootableData.loadNbt(nbttagcompound); // Paper
        if (nbttagcompound.hasKeyOfType("LootTable", 8)) {
            this.m = new MinecraftKey(nbttagcompound.getString("LootTable"));
            this.n = nbttagcompound.getLong("LootTableSeed");
            return false; // Paper - always load the items, table may still remain
        } else {
            return false;
        }
    }

    protected boolean e(NBTTagCompound nbttagcompound) {
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

    protected void d(@Nullable EntityHuman entityhuman) {
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
                loottableinfo_a.a(entityhuman.dc());
            }

            loottable.a(this, random, loottableinfo_a.a());
        }

    }

    public MinecraftKey getLootTableKey() { return b(); } // Paper // OBFHELPER
    public MinecraftKey b() {
        return this.m;
    }

    public void setLootTable(MinecraftKey key, long seed) { a(key, seed);} // Paper // OBFHELPER
    public void a(MinecraftKey minecraftkey, long i) {
        this.m = minecraftkey;
        this.n = i;
    }

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
