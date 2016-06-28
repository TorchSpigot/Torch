package net.minecraft.server;

import java.util.Random;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public abstract class BlockDiodeAbstract extends BlockFacingHorizontal {

    protected static final AxisAlignedBB c = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
    protected final boolean d;

    protected BlockDiodeAbstract(boolean flag) {
        super(Material.ORIENTABLE);
        this.d = flag;
    }

    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return BlockDiodeAbstract.c;
    }

    public boolean c(IBlockData iblockdata) {
        return false;
    }

    public boolean canPlace(World world, BlockPosition blockposition) {
        return world.getType(blockposition.down()).q() ? super.canPlace(world, blockposition) : false;
    }

    public boolean b(World world, BlockPosition blockposition) {
        return world.getType(blockposition.down()).q();
    }

    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {}

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (!this.b((IBlockAccess) world, blockposition, iblockdata)) {
            boolean flag = this.e(world, blockposition, iblockdata);

            if (this.d && !flag) {
                // CraftBukkit start
                if (CraftEventFactory.callRedstoneChange(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), 15, 0).getNewCurrent() != 0) {
                    return;
                }
                // CraftBukkit end
                world.setTypeAndData(blockposition, this.y(iblockdata), 2);
            } else if (!this.d) {
                // CraftBukkit start
                if (CraftEventFactory.callRedstoneChange(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), 0, 15).getNewCurrent() != 15) {
                    return;
                }
                // CraftBukkit end
                world.setTypeAndData(blockposition, this.x(iblockdata), 2);
                if (!flag) {
                    world.a(blockposition, this.x(iblockdata).getBlock(), this.D(iblockdata), -1);
                }
            }

        }
    }

    protected boolean z(IBlockData iblockdata) {
        return this.d;
    }

    public int c(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return iblockdata.a(iblockaccess, blockposition, enumdirection);
    }

    public int b(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return !this.z(iblockdata) ? 0 : (iblockdata.get(BlockDiodeAbstract.FACING) == enumdirection ? this.a(iblockaccess, blockposition, iblockdata) : 0);
    }

    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {
        if (this.b(world, blockposition)) {
            this.g(world, blockposition, iblockdata);
        } else {
            this.b(world, blockposition, iblockdata, 0);
            world.setAir(blockposition);
            // Paper start - Old TNT cannon behaviors
            if (world.paperConfig.oldCannonBehaviors) {
                world.applyPhysics(blockposition.shift(EnumDirection.EAST), this);
                world.applyPhysics(blockposition.shift(EnumDirection.WEST), this);
                world.applyPhysics(blockposition.shift(EnumDirection.SOUTH), this);
                world.applyPhysics(blockposition.shift(EnumDirection.NORTH), this);
                world.applyPhysics(blockposition.shift(EnumDirection.DOWN), this);
                world.applyPhysics(blockposition.shift(EnumDirection.UP), this);
                return;
            }
            // Paper end
            EnumDirection[] aenumdirection = EnumDirection.values();
            int i = aenumdirection.length;

            for (int j = 0; j < i; ++j) {
                EnumDirection enumdirection = aenumdirection[j];

                world.applyPhysics(blockposition.shift(enumdirection), this);
            }

        }
    }

    protected void g(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (!this.b((IBlockAccess) world, blockposition, iblockdata)) {
            boolean flag = this.e(world, blockposition, iblockdata);

            if ((this.d && !flag || !this.d && flag) && !world.a(blockposition, (Block) this)) {
                byte b0 = -1;

                if (this.i(world, blockposition, iblockdata)) {
                    b0 = -3;
                } else if (this.d) {
                    b0 = -2;
                }

                world.a(blockposition, this, this.i(iblockdata), b0);
            }

        }
    }

    public boolean b(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata) {
        return false;
    }

    protected boolean e(World world, BlockPosition blockposition, IBlockData iblockdata) {
        return this.f(world, blockposition, iblockdata) > 0;
    }

    protected int f(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.get(BlockDiodeAbstract.FACING);
        BlockPosition blockposition1 = blockposition.shift(enumdirection);
        int i = world.getBlockFacePower(blockposition1, enumdirection);

        if (i >= 15) {
            return i;
        } else {
            IBlockData iblockdata1 = world.getType(blockposition1);

            return Math.max(i, iblockdata1.getBlock() == Blocks.REDSTONE_WIRE ? ((Integer) iblockdata1.get(BlockRedstoneWire.POWER)).intValue() : 0);
        }
    }

    protected int c(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.get(BlockDiodeAbstract.FACING);
        EnumDirection enumdirection1 = enumdirection.e();
        EnumDirection enumdirection2 = enumdirection.f();

        return Math.max(this.b(iblockaccess, blockposition.shift(enumdirection1), enumdirection1), this.b(iblockaccess, blockposition.shift(enumdirection2), enumdirection2));
    }

    protected int b(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        IBlockData iblockdata = iblockaccess.getType(blockposition);
        Block block = iblockdata.getBlock();

        return this.A(iblockdata) ? (block == Blocks.REDSTONE_BLOCK ? 15 : (block == Blocks.REDSTONE_WIRE ? ((Integer) iblockdata.get(BlockRedstoneWire.POWER)).intValue() : iblockaccess.getBlockPower(blockposition, enumdirection))) : 0;
    }

    public boolean isPowerSource(IBlockData iblockdata) {
        return true;
    }

    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        return this.getBlockData().set(BlockDiodeAbstract.FACING, entityliving.getDirection().opposite());
    }

    public void postPlace(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        if (this.e(world, blockposition, iblockdata)) {
            world.a(blockposition, (Block) this, 1);
        }

    }

    public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata) {
        this.h(world, blockposition, iblockdata);
    }

    protected void h(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.get(BlockDiodeAbstract.FACING);
        BlockPosition blockposition1 = blockposition.shift(enumdirection.opposite());

        world.e(blockposition1, this);
        world.a(blockposition1, (Block) this, enumdirection);
    }

    public void postBreak(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (this.d) {
            // Paper start - Old TNT cannon behaviors
            if (world.paperConfig.oldCannonBehaviors) {
                world.applyPhysics(blockposition.shift(EnumDirection.EAST), this);
                world.applyPhysics(blockposition.shift(EnumDirection.WEST), this);
                world.applyPhysics(blockposition.shift(EnumDirection.NORTH), this);
                world.applyPhysics(blockposition.shift(EnumDirection.SOUTH), this);
                world.applyPhysics(blockposition.shift(EnumDirection.DOWN), this);
                world.applyPhysics(blockposition.shift(EnumDirection.UP), this);
                return;
            }
            // Paper end
            EnumDirection[] aenumdirection = EnumDirection.values();
            int i = aenumdirection.length;

            for (int j = 0; j < i; ++j) {
                EnumDirection enumdirection = aenumdirection[j];

                world.applyPhysics(blockposition.shift(enumdirection), this);
            }
        }

        super.postBreak(world, blockposition, iblockdata);
    }

    public boolean b(IBlockData iblockdata) {
        return false;
    }

    protected boolean A(IBlockData iblockdata) {
        return iblockdata.m();
    }

    protected int a(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata) {
        return 15;
    }

    public static boolean isDiode(IBlockData iblockdata) {
        return Blocks.UNPOWERED_REPEATER.C(iblockdata) || Blocks.UNPOWERED_COMPARATOR.C(iblockdata);
    }

    public boolean C(IBlockData iblockdata) {
        Block block = iblockdata.getBlock();

        return block == this.x(this.getBlockData()).getBlock() || block == this.y(this.getBlockData()).getBlock();
    }

    public boolean i(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection enumdirection = ((EnumDirection) iblockdata.get(BlockDiodeAbstract.FACING)).opposite();
        BlockPosition blockposition1 = blockposition.shift(enumdirection);

        return isDiode(world.getType(blockposition1)) ? world.getType(blockposition1).get(BlockDiodeAbstract.FACING) != enumdirection : false;
    }

    protected int D(IBlockData iblockdata) {
        return this.i(iblockdata);
    }

    protected abstract int i(IBlockData iblockdata);

    protected abstract IBlockData x(IBlockData iblockdata);

    protected abstract IBlockData y(IBlockData iblockdata);

    public boolean b(Block block) {
        return this.C(block.getBlockData());
    }
}
