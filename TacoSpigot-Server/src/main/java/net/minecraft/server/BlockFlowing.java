package net.minecraft.server;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

// CraftBukkit start
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockFromToEvent;
// CraftBukkit end

public class BlockFlowing extends BlockFluids {

    int a;

    protected BlockFlowing(Material material) {
        super(material);
    }

    private void f(World world, BlockPosition blockposition, IBlockData iblockdata) {
        world.setTypeAndData(blockposition, b(this.material).getBlockData().set(BlockFlowing.LEVEL, iblockdata.get(BlockFlowing.LEVEL)), 2);
    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        // CraftBukkit start
        org.bukkit.World bworld = world.getWorld();
        org.bukkit.Server server = world.getServer();
        org.bukkit.block.Block source = bworld == null ? null : bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
        // CraftBukkit end
        int i = ((Integer) iblockdata.get(BlockFlowing.LEVEL)).intValue();
        byte b0 = 1;

        if (this.material == Material.LAVA && !world.worldProvider.l()) {
            b0 = 2;
        }

        int j = this.getFlowSpeed(world, blockposition); // Paper
        int k;

        if (i > 0) {
            int l = -100;

            this.a = 0;

            EnumDirection enumdirection;

            for (Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator(); iterator.hasNext(); l = this.a(world, blockposition.shift(enumdirection), l)) {
                enumdirection = (EnumDirection) iterator.next();
            }

            int i1 = l + b0;

            if (i1 >= 8 || l < 0) {
                i1 = -1;
            }

            k = this.i(world.getType(blockposition.up()));
            if (k >= 0) {
                if (k >= 8) {
                    i1 = k;
                } else {
                    i1 = k + 8;
                }
            }

            if (this.a >= 2 && this.material == Material.WATER) {
                IBlockData iblockdata1 = world.getType(blockposition.down());

                if (iblockdata1.getMaterial().isBuildable()) {
                    i1 = 0;
                } else if (iblockdata1.getMaterial() == this.material && ((Integer) iblockdata1.get(BlockFlowing.LEVEL)).intValue() == 0) {
                    i1 = 0;
                }
            }

            if (!world.paperConfig.fastDrainLava && this.material == Material.LAVA && i < 8 && i1 < 8 && i1 > i && random.nextInt(4) != 0) { // Paper
                j *= 4;
            }

            if (i1 == i) {
                this.f(world, blockposition, iblockdata);
            } else {
                i = i1;
                if (i1 < 0 || canFastDrain(world, blockposition)) { // Paper - Fast draining
                    world.setAir(blockposition);
                } else {
                    iblockdata = iblockdata.set(BlockFlowing.LEVEL, Integer.valueOf(i1));
                    world.setTypeAndData(blockposition, iblockdata, 2);
                    world.a(blockposition, (Block) this, j);
                    // Paper start - Optimize draining
                    world.e(blockposition.west(), this);
                    world.e(blockposition.east(), this);
                    world.e(blockposition.up(), this);
                    world.e(blockposition.north(), this);
                    world.e(blockposition.south(), this);
                    // Paper end
                }
            }
        } else {
            this.f(world, blockposition, iblockdata);
        }

        if (world.getType(blockposition).getBlock().getBlockData().getMaterial() != material) return; // Paper - Stop updating flowing block if material has changed
        IBlockData iblockdata2 = world.getType(blockposition.down());

        if (this.h(world, blockposition.down(), iblockdata2)) {
            if (!canFlowTo(world, source, BlockFace.DOWN)) { return; } // Paper
            // CraftBukkit start - Send "down" to the server
            BlockFromToEvent event = new BlockFromToEvent(source, BlockFace.DOWN);
            if (server != null) {
                server.getPluginManager().callEvent(event);
            }
            if (!event.isCancelled()) {
            if (this.material == Material.LAVA && world.getType(blockposition.down()).getMaterial() == Material.WATER) {
                world.setTypeUpdate(blockposition.down(), Blocks.STONE.getBlockData());
                this.fizz(world, blockposition.down());
                return;
            }

            if (i >= 8) {
                this.flow(world, blockposition.down(), iblockdata2, i);
            } else {
                this.flow(world, blockposition.down(), iblockdata2, i + 8);
            }
            }
            // CraftBukkit end
        } else if (i >= 0 && (i == 0 || this.g(world, blockposition.down(), iblockdata2))) {
            Set set = this.c(world, blockposition);

            k = i + b0;
            if (i >= 8) {
                k = 1;
            }

            if (k >= 8) {
                return;
            }

            Iterator iterator1 = set.iterator();

            while (iterator1.hasNext()) {
                EnumDirection enumdirection1 = (EnumDirection) iterator1.next();

                // CraftBukkit start
                if (!canFlowTo(world, source, org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(enumdirection1))) { continue; } // Paper
                BlockFromToEvent event = new BlockFromToEvent(source, org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(enumdirection1));
                if (server != null) {
                    server.getPluginManager().callEvent(event);
                }

                if (!event.isCancelled()) {
                    this.flow(world, blockposition.shift(enumdirection1), world.getType(blockposition.shift(enumdirection1)), k);
                }
                // CraftBukkit end
            }
        }

    }

    // Paper start
    private boolean canFlowTo(World world, org.bukkit.block.Block source, BlockFace face) {
        return source.getWorld().isChunkLoaded((source.getX() + face.getModX()) >> 4, (source.getZ() + face.getModZ()) >> 4);
    }
    // Paper end

    private void flow(World world, BlockPosition blockposition, IBlockData iblockdata, int i) {
        if (/*world.isLoaded(blockposition) &&*/ this.h(world, blockposition, iblockdata)) { // CraftBukkit - add isLoaded check // Paper - Already checked before we get here for isLoaded
            if (iblockdata.getBlock() != Blocks.AIR) {
                if (this.material == Material.LAVA) {
                    this.fizz(world, blockposition);
                } else {
                    iblockdata.getBlock().b(world, blockposition, iblockdata, 0);
                }
            }

            world.setTypeAndData(blockposition, this.getBlockData().set(BlockFlowing.LEVEL, Integer.valueOf(i)), 3);
        }

    }

    private int a(World world, BlockPosition blockposition, int i, EnumDirection enumdirection) {
        int j = 1000;
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection1 = (EnumDirection) iterator.next();

            if (enumdirection1 != enumdirection) {
                BlockPosition blockposition1 = blockposition.shift(enumdirection1);
                IBlockData iblockdata = world.getType(blockposition1);

                if (!this.g(world, blockposition1, iblockdata) && (iblockdata.getMaterial() != this.material || ((Integer) iblockdata.get(BlockFlowing.LEVEL)).intValue() > 0)) {
                    if (!this.g(world, blockposition1.down(), iblockdata)) {
                        return i;
                    }

                    if (i < this.b(world)) {
                        int k = this.a(world, blockposition1, i + 1, enumdirection1.opposite());

                        if (k < j) {
                            j = k;
                        }
                    }
                }
            }
        }

        return j;
    }

    private int b(World world) {
        return this.material == Material.LAVA && !world.worldProvider.l() ? 2 : 4;
    }

    private Set<EnumDirection> c(World world, BlockPosition blockposition) {
        int i = 1000;
        EnumSet enumset = EnumSet.noneOf(EnumDirection.class);
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection = (EnumDirection) iterator.next();
            BlockPosition blockposition1 = blockposition.shift(enumdirection);
            IBlockData iblockdata = world.getType(blockposition1);

            if (!this.g(world, blockposition1, iblockdata) && (iblockdata.getMaterial() != this.material || ((Integer) iblockdata.get(BlockFlowing.LEVEL)).intValue() > 0)) {
                int j;

                if (this.g(world, blockposition1.down(), world.getType(blockposition1.down()))) {
                    j = this.a(world, blockposition1, 1, enumdirection.opposite());
                } else {
                    j = 0;
                }

                if (j < i) {
                    enumset.clear();
                }

                if (j <= i) {
                    enumset.add(enumdirection);
                    i = j;
                }
            }
        }

        return enumset;
    }

    private boolean g(World world, BlockPosition blockposition, IBlockData iblockdata) {
        Block block = world.getType(blockposition).getBlock();

        return !(block instanceof BlockDoor) && block != Blocks.STANDING_SIGN && block != Blocks.LADDER && block != Blocks.REEDS ? (block.material == Material.PORTAL ? true : block.material.isSolid()) : true;
    }

    protected int a(World world, BlockPosition blockposition, int i) {
        int j = this.i(world.getType(blockposition));

        if (j < 0) {
            return i;
        } else {
            if (j == 0) {
                ++this.a;
            }

            if (j >= 8) {
                j = 0;
            }

            return i >= 0 && j >= i ? i : j;
        }
    }

    private boolean h(World world, BlockPosition blockposition, IBlockData iblockdata) {
        Material material = iblockdata.getMaterial();

        return material != this.material && material != Material.LAVA && !this.g(world, blockposition, iblockdata);
    }

    public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (!this.e(world, blockposition, iblockdata)) {
            world.a(blockposition, (Block) this, this.getFlowSpeed(world, blockposition)); // Paper
        }

    }

    // Paper start
    /**
     * Paper - Get flow speed. Throttle if its water and flowing adjacent to lava
     */
    public int getFlowSpeed(World world, BlockPosition blockposition) {
        if (this.material == Material.LAVA) {
            return world.worldProvider.m() ? world.paperConfig.lavaFlowSpeedNether : world.paperConfig.lavaFlowSpeedNormal;
        }
        if (this.material == Material.WATER && (
                world.getType(blockposition.north(1)).getBlock().material == Material.LAVA ||
                        world.getType(blockposition.south(1)).getBlock().material == Material.LAVA ||
                        world.getType(blockposition.west(1)).getBlock().material == Material.LAVA ||
                        world.getType(blockposition.east(1)).getBlock().material == Material.LAVA)) {
            return world.paperConfig.waterOverLavaFlowSpeed;
        }
        return super.a(world);
    }

    private int getFluidLevel(IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockaccess.getType(blockposition).getMaterial() == this.material ? iblockaccess.getType(blockposition).get(BlockFluids.LEVEL) : -1;
    }

    /**
     * Paper - Data check method for fast draining
     */
    public int getData(World world, BlockPosition position) {
        int data = this.getFluidLevel((IBlockAccess) world, position);
        return data < 8 ? data : 0;
    }

    /**
     * Paper - Checks surrounding blocks to determine if block can be fast drained
     */
    public boolean canFastDrain(World world, BlockPosition position) {
        boolean result = false;
        int data = getData(world, position);
        if (this.material == Material.WATER) {
            if (world.paperConfig.fastDrainWater) {
                result = true;
                if (getData(world, position.down()) < 0) {
                    result = false;
                } else if (world.getType(position.north()).getBlock().getBlockData().getMaterial() == Material.WATER && getData(world, position.north()) < data) {
                    result = false;
                } else if (world.getType(position.south()).getBlock().getBlockData().getMaterial() == Material.WATER && getData(world, position.south()) < data) {
                    result = false;
                } else if (world.getType(position.west()).getBlock().getBlockData().getMaterial() == Material.WATER && getData(world, position.west()) < data) {
                    result = false;
                } else if (world.getType(position.east()).getBlock().getBlockData().getMaterial() == Material.WATER && getData(world, position.east()) < data) {
                    result = false;
                }
            }
        } else if (this.material == Material.LAVA) {
            if (world.paperConfig.fastDrainLava) {
                result = true;
                if (getData(world, position.down()) < 0 || world.getType(position.up()).getBlock().getBlockData().getMaterial() != Material.AIR) {
                    result = false;
                } else if (world.getType(position.north()).getBlock().getBlockData().getMaterial() == Material.LAVA && getData(world, position.north()) < data) {
                    result = false;
                } else if (world.getType(position.south()).getBlock().getBlockData().getMaterial() == Material.LAVA && getData(world, position.south()) < data) {
                    result = false;
                } else if (world.getType(position.west()).getBlock().getBlockData().getMaterial() == Material.LAVA && getData(world, position.west()) < data) {
                    result = false;
                } else if (world.getType(position.east()).getBlock().getBlockData().getMaterial() == Material.LAVA && getData(world, position.east()) < data) {
                    result = false;
                }
            }
        }
        return result;
    }
    // Paper end
}
