package net.minecraft.server;

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
    private static final RegistryMaterials<MinecraftKey, Class<? extends TileEntity>> f = new RegistryMaterials();
    protected World world;
    protected BlockPosition position;
    protected boolean d;
    private int g;
    protected Block e;

    public TileEntity() {
        this.position = BlockPosition.ZERO;
        this.g = -1;
    }

    private static void a(String s, Class<? extends TileEntity> oclass) {
        TileEntity.f.a(new MinecraftKey(s), oclass);
    }

    @Nullable
    public static MinecraftKey a(Class<? extends TileEntity> oclass) {
        return (MinecraftKey) TileEntity.f.b(oclass);
    }

    static boolean IGNORE_TILE_UPDATES = false; // Paper
    public World getWorld() {
        return this.world;
    }

    public void a(World world) {
        this.world = world;
    }

    public boolean u() {
        return this.world != null;
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.position = new BlockPosition(nbttagcompound.getInt("x"), nbttagcompound.getInt("y"), nbttagcompound.getInt("z"));
    }

    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        return this.c(nbttagcompound);
    }

    private NBTTagCompound c(NBTTagCompound nbttagcompound) {
        MinecraftKey minecraftkey = (MinecraftKey) TileEntity.f.b(this.getClass());

        if (minecraftkey == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        } else {
            nbttagcompound.setString("id", minecraftkey.toString());
            nbttagcompound.setInt("x", this.position.getX());
            nbttagcompound.setInt("y", this.position.getY());
            nbttagcompound.setInt("z", this.position.getZ());
            return nbttagcompound;
        }
    }

    @Nullable
    public static TileEntity a(World world, NBTTagCompound nbttagcompound) {
        TileEntity tileentity = null;
        String s = nbttagcompound.getString("id");

        try {
            Class oclass = (Class) TileEntity.f.get(new MinecraftKey(s));

            if (oclass != null) {
                tileentity = (TileEntity) oclass.newInstance();
            }
        } catch (Throwable throwable) {
            TileEntity.a.error("Failed to create block entity {}", new Object[] { s, throwable});
        }

        if (tileentity != null) {
            try {
                tileentity.b(world);
                tileentity.a(nbttagcompound);
            } catch (Throwable throwable1) {
                TileEntity.a.error("Failed to load data for block entity {}", new Object[] { s, throwable1});
                tileentity = null;
            }
        } else {
            TileEntity.a.warn("Skipping BlockEntity with id {}", new Object[] { s});
        }

        return tileentity;
    }

    protected void b(World world) {}

    public int v() {
        if (this.g == -1) {
            IBlockData iblockdata = this.world.getType(this.position);

            this.g = iblockdata.getBlock().toLegacyData(iblockdata);
        }

        return this.g;
    }

    public void update() {
        if (this.world != null) {
            if (IGNORE_TILE_UPDATES) return; // Paper
            IBlockData iblockdata = this.world.getType(this.position);

            this.g = iblockdata.getBlock().toLegacyData(iblockdata);
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

    public NBTTagCompound d() {
        return this.c(new NBTTagCompound());
    }

    public boolean y() {
        return this.d;
    }

    public void z() {
        this.d = true;
    }

    public void A() {
        this.d = false;
    }

    public boolean c(int i, int j) {
        return false;
    }

    public void invalidateBlockCache() {
        this.e = null;
        this.g = -1;
    }

    public void a(CrashReportSystemDetails crashreportsystemdetails) {
        crashreportsystemdetails.a("Name", new CrashReportCallable() {
            public String a() throws Exception {
                return TileEntity.f.b(TileEntity.this.getClass()) + " // " + TileEntity.this.getClass().getCanonicalName();
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        if (this.world != null) {
            // Paper start - Prevent TileEntity and Entity crashes
            Block block = this.getBlock();
            if (block != null) {
                CrashReportSystemDetails.a(crashreportsystemdetails, this.position, this.getBlock(), this.v());
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

    public void setPosition(BlockPosition blockposition) {
        this.position = blockposition.h();
    }

    public boolean isFilteredNBT() {
        return false;
    }

    @Nullable
    public IChatBaseComponent i_() {
        return null;
    }

    public void a(EnumBlockRotation enumblockrotation) {}

    public void a(EnumBlockMirror enumblockmirror) {}

    static {
        a("furnace", TileEntityFurnace.class);
        a("chest", TileEntityChest.class);
        a("ender_chest", TileEntityEnderChest.class);
        a("jukebox", BlockJukeBox.TileEntityRecordPlayer.class);
        a("dispenser", TileEntityDispenser.class);
        a("dropper", TileEntityDropper.class);
        a("sign", TileEntitySign.class);
        a("mob_spawner", TileEntityMobSpawner.class);
        a("noteblock", TileEntityNote.class);
        a("piston", TileEntityPiston.class);
        a("brewing_stand", TileEntityBrewingStand.class);
        a("enchanting_table", TileEntityEnchantTable.class);
        a("end_portal", TileEntityEnderPortal.class);
        a("beacon", TileEntityBeacon.class);
        a("skull", TileEntitySkull.class);
        a("daylight_detector", TileEntityLightDetector.class);
        a("hopper", TileEntityHopper.class);
        a("comparator", TileEntityComparator.class);
        a("flower_pot", TileEntityFlowerPot.class);
        a("banner", TileEntityBanner.class);
        a("structure_block", TileEntityStructure.class);
        a("end_gateway", TileEntityEndGateway.class);
        a("command_block", TileEntityCommand.class);
        a("shulker_box", TileEntityShulkerBox.class);
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
