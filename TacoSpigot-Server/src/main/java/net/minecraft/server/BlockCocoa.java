package net.minecraft.server;

import java.util.Random;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class BlockCocoa extends BlockFacingHorizontal implements IBlockFragilePlantElement {

    public static final BlockStateInteger AGE = BlockStateInteger.of("age", 0, 2);
    protected static final AxisAlignedBB[] b = new AxisAlignedBB[] { new AxisAlignedBB(0.6875D, 0.4375D, 0.375D, 0.9375D, 0.75D, 0.625D), new AxisAlignedBB(0.5625D, 0.3125D, 0.3125D, 0.9375D, 0.75D, 0.6875D), new AxisAlignedBB(0.5625D, 0.3125D, 0.3125D, 0.9375D, 0.75D, 0.6875D)};
    protected static final AxisAlignedBB[] c = new AxisAlignedBB[] { new AxisAlignedBB(0.0625D, 0.4375D, 0.375D, 0.3125D, 0.75D, 0.625D), new AxisAlignedBB(0.0625D, 0.3125D, 0.3125D, 0.4375D, 0.75D, 0.6875D), new AxisAlignedBB(0.0625D, 0.3125D, 0.3125D, 0.4375D, 0.75D, 0.6875D)};
    protected static final AxisAlignedBB[] d = new AxisAlignedBB[] { new AxisAlignedBB(0.375D, 0.4375D, 0.0625D, 0.625D, 0.75D, 0.3125D), new AxisAlignedBB(0.3125D, 0.3125D, 0.0625D, 0.6875D, 0.75D, 0.4375D), new AxisAlignedBB(0.3125D, 0.3125D, 0.0625D, 0.6875D, 0.75D, 0.4375D)};
    protected static final AxisAlignedBB[] e = new AxisAlignedBB[] { new AxisAlignedBB(0.375D, 0.4375D, 0.6875D, 0.625D, 0.75D, 0.9375D), new AxisAlignedBB(0.3125D, 0.3125D, 0.5625D, 0.6875D, 0.75D, 0.9375D), new AxisAlignedBB(0.3125D, 0.3125D, 0.5625D, 0.6875D, 0.75D, 0.9375D)};

    public BlockCocoa() {
        super(Material.PLANT);
        this.w(this.blockStateList.getBlockData().set(BlockCocoa.FACING, EnumDirection.NORTH).set(BlockCocoa.AGE, Integer.valueOf(0)));
        this.a(true);
    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (!this.e(world, blockposition, iblockdata)) {
            this.f(world, blockposition, iblockdata);
        } else if (world.random.nextInt(5) == 0) {
            int i = ((Integer) iblockdata.get(BlockCocoa.AGE)).intValue();

            if (i < 2) {
                // CraftBukkit start
                IBlockData data = iblockdata.set(AGE, Integer.valueOf(i + 1));
                CraftEventFactory.handleBlockGrowEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this, toLegacyData(data));
                // CraftBukkit end
            }
        }

    }

    public boolean e(World world, BlockPosition blockposition, IBlockData iblockdata) {
        blockposition = blockposition.shift((EnumDirection) iblockdata.get(BlockCocoa.FACING));
        IBlockData iblockdata1 = world.getType(blockposition);

        return iblockdata1.getBlock() == Blocks.LOG && iblockdata1.get(BlockLog1.VARIANT) == BlockWood.EnumLogVariant.JUNGLE;
    }

    public boolean c(IBlockData iblockdata) {
        return false;
    }

    public boolean b(IBlockData iblockdata) {
        return false;
    }

    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        int i = ((Integer) iblockdata.get(BlockCocoa.AGE)).intValue();

        switch (BlockCocoa.SyntheticClass_1.a[((EnumDirection) iblockdata.get(BlockCocoa.FACING)).ordinal()]) {
        case 1:
            return BlockCocoa.e[i];

        case 2:
        default:
            return BlockCocoa.d[i];

        case 3:
            return BlockCocoa.c[i];

        case 4:
            return BlockCocoa.b[i];
        }
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return iblockdata.set(BlockCocoa.FACING, enumblockrotation.a((EnumDirection) iblockdata.get(BlockCocoa.FACING)));
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.a(enumblockmirror.a((EnumDirection) iblockdata.get(BlockCocoa.FACING)));
    }

    public void postPlace(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        EnumDirection enumdirection = EnumDirection.fromAngle((double) entityliving.yaw);

        world.setTypeAndData(blockposition, iblockdata.set(BlockCocoa.FACING, enumdirection), 2);
    }

    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        if (!enumdirection.k().c()) {
            enumdirection = EnumDirection.NORTH;
        }

        return this.getBlockData().set(BlockCocoa.FACING, enumdirection.opposite()).set(BlockCocoa.AGE, Integer.valueOf(0));
    }

    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {
        if (!this.e(world, blockposition, iblockdata)) {
            this.f(world, blockposition, iblockdata);
        }

    }

    private void f(World world, BlockPosition blockposition, IBlockData iblockdata) {
        world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
        this.b(world, blockposition, iblockdata, 0);
    }

    public void dropNaturally(World world, BlockPosition blockposition, IBlockData iblockdata, float f, int i) {
        int j = ((Integer) iblockdata.get(BlockCocoa.AGE)).intValue();
        byte b0 = 1;

        if (j >= 2) {
            b0 = 3;
        }

        for (int k = 0; k < b0; ++k) {
            a(world, blockposition, new ItemStack(Items.DYE, 1, EnumColor.BROWN.getInvColorIndex()));
        }

    }

    public ItemStack a(World world, BlockPosition blockposition, IBlockData iblockdata) {
        return new ItemStack(Items.DYE, 1, EnumColor.BROWN.getInvColorIndex());
    }

    public boolean a(World world, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return ((Integer) iblockdata.get(BlockCocoa.AGE)).intValue() < 2;
    }

    public boolean a(World world, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    public void b(World world, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        // CraftBukkit start
        IBlockData data = iblockdata.set(AGE, Integer.valueOf(((Integer) iblockdata.get(AGE)).intValue() + 1));
        CraftEventFactory.handleBlockGrowEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this, toLegacyData(data));
        // CraftBukkit end
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockCocoa.FACING, EnumDirection.fromType2(i)).set(BlockCocoa.AGE, Integer.valueOf((i & 15) >> 2));
    }

    public int toLegacyData(IBlockData iblockdata) {
        byte b0 = 0;
        int i = b0 | ((EnumDirection) iblockdata.get(BlockCocoa.FACING)).get2DRotationValue();

        i |= ((Integer) iblockdata.get(BlockCocoa.AGE)).intValue() << 2;
        return i;
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockCocoa.FACING, BlockCocoa.AGE});
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumDirection.values().length];

        static {
            try {
                BlockCocoa.SyntheticClass_1.a[EnumDirection.SOUTH.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                BlockCocoa.SyntheticClass_1.a[EnumDirection.NORTH.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                BlockCocoa.SyntheticClass_1.a[EnumDirection.WEST.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                BlockCocoa.SyntheticClass_1.a[EnumDirection.EAST.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

        }
    }
}
