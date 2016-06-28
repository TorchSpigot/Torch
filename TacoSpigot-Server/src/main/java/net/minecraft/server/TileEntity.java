package net.minecraft.server;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import co.aikar.timings.MinecraftTimings; // Paper
import co.aikar.timings.Timing; // Paper
import org.bukkit.inventory.InventoryHolder; // CraftBukkit

public abstract class TileEntity {

    public Timing tickTimer = MinecraftTimings.getTileEntityTimings(this); // Paper
	boolean isLoadingStructure = false; // Paper
    private static final Logger a = LogManager.getLogger();
    private static Map<String, Class<? extends TileEntity>> f = Maps.newHashMap();
    private static Map<Class<? extends TileEntity>, String> g = Maps.newHashMap();
    protected World world;
    protected BlockPosition position;
    protected boolean d;
    private int h;
    protected Block e;

    public TileEntity() {
        this.position = BlockPosition.ZERO;
        this.h = -1;
    }

    private static void a(Class<? extends TileEntity> oclass, String s) {
        if (TileEntity.f.containsKey(s)) {
            throw new IllegalArgumentException("Duplicate id: " + s);
        } else {
            TileEntity.f.put(s, oclass);
            TileEntity.g.put(oclass, s);
        }
    }

    static boolean IGNORE_TILE_UPDATES = false; // Paper
    public World getWorld() {
        return this.world;
    }

    public void a(World world) {
        this.world = world;
    }

    public boolean t() {
        return this.world != null;
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.position = new BlockPosition(nbttagcompound.getInt("x"), nbttagcompound.getInt("y"), nbttagcompound.getInt("z"));
    }

    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        return this.d(nbttagcompound);
    }

    private NBTTagCompound d(NBTTagCompound nbttagcompound) {
        String s = (String) TileEntity.g.get(this.getClass());

        if (s == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        } else {
            nbttagcompound.setString("id", s);
            nbttagcompound.setInt("x", this.position.getX());
            nbttagcompound.setInt("y", this.position.getY());
            nbttagcompound.setInt("z", this.position.getZ());
            return nbttagcompound;
        }
    }

    public static TileEntity c(NBTTagCompound nbttagcompound) {
        TileEntity tileentity = null;
        String s = nbttagcompound.getString("id");

        try {
            Class oclass = (Class) TileEntity.f.get(s);

            if (oclass != null) {
                tileentity = (TileEntity) oclass.newInstance();
            }
        } catch (Throwable throwable) {
            TileEntity.a.error("Failed to create block entity " + s, throwable);
        }

        if (tileentity != null) {
            try {
                tileentity.a(nbttagcompound);
            } catch (Throwable throwable1) {
                TileEntity.a.error("Failed to load data for block entity " + s, throwable1);
                tileentity = null;
            }
        } else {
            TileEntity.a.warn("Skipping BlockEntity with id " + s);
        }

        return tileentity;
    }

    public int u() {
        if (this.h == -1) {
            IBlockData iblockdata = this.world.getType(this.position);

            this.h = iblockdata.getBlock().toLegacyData(iblockdata);
        }

        return this.h;
    }

    public void update() {
        if (this.world != null) {
            if (IGNORE_TILE_UPDATES) return; // Paper
            IBlockData iblockdata = this.world.getType(this.position);

            this.h = iblockdata.getBlock().toLegacyData(iblockdata);
            this.world.b(this.position, this);
            if (this.getBlock() != Blocks.AIR) {
                this.world.updateAdjacentComparators(this.position, this.getBlock());
            }
        }

    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public Block getBlock() {
        if (this.e == null && this.world != null) {
            this.e = this.world.getType(this.position).getBlock();
        }

        return this.e;
    }

    @Nullable
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return null;
    }

    public NBTTagCompound E_() {
        return this.d(new NBTTagCompound());
    }

    public boolean x() {
        return this.d;
    }

    public void y() {
        this.d = true;
    }

    public void z() {
        this.d = false;
    }

    public boolean c(int i, int j) {
        return false;
    }

    public void invalidateBlockCache() {
        this.e = null;
        this.h = -1;
    }

    public void a(CrashReportSystemDetails crashreportsystemdetails) {
        crashreportsystemdetails.a("Name", new CrashReportCallable() {
            public String a() throws Exception {
                return (String) TileEntity.g.get(TileEntity.this.getClass()) + " // " + TileEntity.this.getClass().getCanonicalName();
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        if (this.world != null) {
            // Paper start - Prevent TileEntity and Entity crashes
            Block block = this.getBlock();
            if (block != null) {
                CrashReportSystemDetails.a(crashreportsystemdetails, this.position, block, this.u());
            }
            // Paper end
            crashreportsystemdetails.a("Actual block type", new CrashReportCallable() {
                public String a() throws Exception {
                    int i = Block.getId(TileEntity.this.world.getType(TileEntity.this.position).getBlock());

                    try {
                        return String.format("ID #%d (%s // %s)", new Object[] { Integer.valueOf(i), Block.getById(i).a(), Block.getById(i).getClass().getCanonicalName()});
                    } catch (Throwable throwable) {
                        return "ID #" + i;
                    }
                }

                public Object call() throws Exception {
                    return this.a();
                }
            });
            crashreportsystemdetails.a("Actual block data value", new CrashReportCallable() {
                public String a() throws Exception {
                    IBlockData iblockdata = TileEntity.this.world.getType(TileEntity.this.position);
                    int i = iblockdata.getBlock().toLegacyData(iblockdata);

                    if (i < 0) {
                        return "Unknown? (Got " + i + ")";
                    } else {
                        String s = String.format("%4s", new Object[] { Integer.toBinaryString(i)}).replace(" ", "0");

                        return String.format("%1$d / 0x%1$X / 0b%2$s", new Object[] { Integer.valueOf(i), s});
                    }
                }

                public Object call() throws Exception {
                    return this.a();
                }
            });
        }
    }

    public void a(BlockPosition blockposition) {
        if (blockposition instanceof BlockPosition.MutableBlockPosition || blockposition instanceof BlockPosition.PooledBlockPosition) {
            TileEntity.a.warn("Tried to assign a mutable BlockPos to a block entity...", new Error(blockposition.getClass().toString()));
            blockposition = new BlockPosition(blockposition);
        }

        this.position = blockposition;
    }

    public boolean isFilteredNBT() {
        return false;
    }

    static {
        a(TileEntityFurnace.class, "Furnace");
        a(TileEntityChest.class, "Chest");
        a(TileEntityEnderChest.class, "EnderChest");
        a(BlockJukeBox.TileEntityRecordPlayer.class, "RecordPlayer");
        a(TileEntityDispenser.class, "Trap");
        a(TileEntityDropper.class, "Dropper");
        a(TileEntitySign.class, "Sign");
        a(TileEntityMobSpawner.class, "MobSpawner");
        a(TileEntityNote.class, "Music");
        a(TileEntityPiston.class, "Piston");
        a(TileEntityBrewingStand.class, "Cauldron");
        a(TileEntityEnchantTable.class, "EnchantTable");
        a(TileEntityEnderPortal.class, "Airportal");
        a(TileEntityBeacon.class, "Beacon");
        a(TileEntitySkull.class, "Skull");
        a(TileEntityLightDetector.class, "DLDetector");
        a(TileEntityHopper.class, "Hopper");
        a(TileEntityComparator.class, "Comparator");
        a(TileEntityFlowerPot.class, "FlowerPot");
        a(TileEntityBanner.class, "Banner");
        a(TileEntityStructure.class, "Structure");
        a(TileEntityEndGateway.class, "EndGateway");
        a(TileEntityCommand.class, "Control");
    }

    // CraftBukkit start - add method
    public InventoryHolder getOwner() {
        if (world == null) return null;
        // Spigot start
        org.bukkit.block.Block block = world.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());
        if (block == null) {
            org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "No block for owner at %s %d %d %d", new Object[]{world.getWorld(), position.getX(), position.getY(), position.getZ()});
            return null;
        }
        // Spigot end
        org.bukkit.block.BlockState state = block.getState();
        if (state instanceof InventoryHolder) return (InventoryHolder) state;
        return null;
    }
    // CraftBukkit end
}
