package net.minecraft.server;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class BlockPoweredRail extends BlockMinecartTrackAbstract {

    public static final BlockStateEnum<BlockMinecartTrackAbstract.EnumTrackPosition> SHAPE = BlockStateEnum.a("shape", BlockMinecartTrackAbstract.EnumTrackPosition.class, new Predicate() {
        public boolean a(@Nullable BlockMinecartTrackAbstract.EnumTrackPosition blockminecarttrackabstract_enumtrackposition) {
            return blockminecarttrackabstract_enumtrackposition != BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_EAST && blockminecarttrackabstract_enumtrackposition != BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_WEST && blockminecarttrackabstract_enumtrackposition != BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_EAST && blockminecarttrackabstract_enumtrackposition != BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_WEST;
        }

        public boolean apply(Object object) {
            return this.a((BlockMinecartTrackAbstract.EnumTrackPosition) object);
        }
    });
    public static final BlockStateBoolean POWERED = BlockStateBoolean.of("powered");

    protected BlockPoweredRail() {
        super(true);
        this.w(this.blockStateList.getBlockData().set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH).set(BlockPoweredRail.POWERED, Boolean.valueOf(false)));
    }

    protected boolean a(World world, BlockPosition blockposition, IBlockData iblockdata, boolean flag, int i) {
        if (i >= 8) {
            return false;
        } else {
            int j = blockposition.getX();
            int k = blockposition.getY();
            int l = blockposition.getZ();
            boolean flag1 = true;
            BlockMinecartTrackAbstract.EnumTrackPosition blockminecarttrackabstract_enumtrackposition = (BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(BlockPoweredRail.SHAPE);

            switch (BlockPoweredRail.SyntheticClass_1.a[blockminecarttrackabstract_enumtrackposition.ordinal()]) {
            case 1:
                if (flag) {
                    ++l;
                } else {
                    --l;
                }
                break;

            case 2:
                if (flag) {
                    --j;
                } else {
                    ++j;
                }
                break;

            case 3:
                if (flag) {
                    --j;
                } else {
                    ++j;
                    ++k;
                    flag1 = false;
                }

                blockminecarttrackabstract_enumtrackposition = BlockMinecartTrackAbstract.EnumTrackPosition.EAST_WEST;
                break;

            case 4:
                if (flag) {
                    --j;
                    ++k;
                    flag1 = false;
                } else {
                    ++j;
                }

                blockminecarttrackabstract_enumtrackposition = BlockMinecartTrackAbstract.EnumTrackPosition.EAST_WEST;
                break;

            case 5:
                if (flag) {
                    ++l;
                } else {
                    --l;
                    ++k;
                    flag1 = false;
                }

                blockminecarttrackabstract_enumtrackposition = BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH;
                break;

            case 6:
                if (flag) {
                    ++l;
                    ++k;
                    flag1 = false;
                } else {
                    --l;
                }

                blockminecarttrackabstract_enumtrackposition = BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH;
            }

            return this.a(world, new BlockPosition(j, k, l), flag, i, blockminecarttrackabstract_enumtrackposition) ? true : flag1 && this.a(world, new BlockPosition(j, k - 1, l), flag, i, blockminecarttrackabstract_enumtrackposition);
        }
    }

    protected boolean a(World world, BlockPosition blockposition, boolean flag, int i, BlockMinecartTrackAbstract.EnumTrackPosition blockminecarttrackabstract_enumtrackposition) {
        IBlockData iblockdata = world.getType(blockposition);

        if (iblockdata.getBlock() != this) {
            return false;
        } else {
            BlockMinecartTrackAbstract.EnumTrackPosition blockminecarttrackabstract_enumtrackposition1 = (BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(BlockPoweredRail.SHAPE);

            return blockminecarttrackabstract_enumtrackposition == BlockMinecartTrackAbstract.EnumTrackPosition.EAST_WEST && (blockminecarttrackabstract_enumtrackposition1 == BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH || blockminecarttrackabstract_enumtrackposition1 == BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_NORTH || blockminecarttrackabstract_enumtrackposition1 == BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_SOUTH) ? false : (blockminecarttrackabstract_enumtrackposition == BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH && (blockminecarttrackabstract_enumtrackposition1 == BlockMinecartTrackAbstract.EnumTrackPosition.EAST_WEST || blockminecarttrackabstract_enumtrackposition1 == BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_EAST || blockminecarttrackabstract_enumtrackposition1 == BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_WEST) ? false : (((Boolean) iblockdata.get(BlockPoweredRail.POWERED)).booleanValue() ? (world.isBlockIndirectlyPowered(blockposition) ? true : this.a(world, blockposition, iblockdata, flag, i + 1)) : false));
        }
    }

    protected void b(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {
        boolean flag = ((Boolean) iblockdata.get(BlockPoweredRail.POWERED)).booleanValue();
        boolean flag1 = world.isBlockIndirectlyPowered(blockposition) || this.a(world, blockposition, iblockdata, true, 0) || this.a(world, blockposition, iblockdata, false, 0);

        if (flag1 != flag) {
            // CraftBukkit start
            int power = (Boolean)iblockdata.get(POWERED) ? 15 : 0;
            int newPower = CraftEventFactory.callRedstoneChange(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), power, 15 - power).getNewCurrent();
            if (newPower == power) {
                return;
            }
            // CraftBukkit end
            world.setTypeAndData(blockposition, iblockdata.set(BlockPoweredRail.POWERED, Boolean.valueOf(flag1)), 3);
            world.applyPhysics(blockposition.down(), this);
            if (((BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(BlockPoweredRail.SHAPE)).c()) {
                world.applyPhysics(blockposition.up(), this);
            }
        }

    }

    public IBlockState<BlockMinecartTrackAbstract.EnumTrackPosition> g() {
        return BlockPoweredRail.SHAPE;
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.a(i & 7)).set(BlockPoweredRail.POWERED, Boolean.valueOf((i & 8) > 0));
    }

    public int toLegacyData(IBlockData iblockdata) {
        byte b0 = 0;
        int i = b0 | ((BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(BlockPoweredRail.SHAPE)).a();

        if (((Boolean) iblockdata.get(BlockPoweredRail.POWERED)).booleanValue()) {
            i |= 8;
        }

        return i;
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        switch (BlockPoweredRail.SyntheticClass_1.b[enumblockrotation.ordinal()]) {
        case 1:
            switch (BlockPoweredRail.SyntheticClass_1.a[((BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(BlockPoweredRail.SHAPE)).ordinal()]) {
            case 3:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_WEST);

            case 4:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_EAST);

            case 5:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_SOUTH);

            case 6:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_NORTH);

            case 7:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_WEST);

            case 8:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_EAST);

            case 9:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_EAST);

            case 10:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_WEST);
            }

        case 2:
            switch (BlockPoweredRail.SyntheticClass_1.a[((BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(BlockPoweredRail.SHAPE)).ordinal()]) {
            case 1:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.EAST_WEST);

            case 2:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH);

            case 3:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_NORTH);

            case 4:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_SOUTH);

            case 5:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_WEST);

            case 6:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_EAST);

            case 7:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_EAST);

            case 8:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_EAST);

            case 9:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_WEST);

            case 10:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_WEST);
            }

        case 3:
            switch (BlockPoweredRail.SyntheticClass_1.a[((BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(BlockPoweredRail.SHAPE)).ordinal()]) {
            case 1:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.EAST_WEST);

            case 2:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH);

            case 3:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_SOUTH);

            case 4:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_NORTH);

            case 5:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_EAST);

            case 6:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_WEST);

            case 7:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_WEST);

            case 8:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_WEST);

            case 9:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_EAST);

            case 10:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_EAST);
            }

        default:
            return iblockdata;
        }
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        BlockMinecartTrackAbstract.EnumTrackPosition blockminecarttrackabstract_enumtrackposition = (BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(BlockPoweredRail.SHAPE);

        switch (BlockPoweredRail.SyntheticClass_1.c[enumblockmirror.ordinal()]) {
        case 1:
            switch (BlockPoweredRail.SyntheticClass_1.a[blockminecarttrackabstract_enumtrackposition.ordinal()]) {
            case 5:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_SOUTH);

            case 6:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_NORTH);

            case 7:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_EAST);

            case 8:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_WEST);

            case 9:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_WEST);

            case 10:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_EAST);

            default:
                return super.a(iblockdata, enumblockmirror);
            }

        case 2:
            switch (BlockPoweredRail.SyntheticClass_1.a[blockminecarttrackabstract_enumtrackposition.ordinal()]) {
            case 3:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_WEST);

            case 4:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_EAST);

            case 5:
            case 6:
            default:
                break;

            case 7:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_WEST);

            case 8:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_EAST);

            case 9:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_EAST);

            case 10:
                return iblockdata.set(BlockPoweredRail.SHAPE, BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_WEST);
            }
        }

        return super.a(iblockdata, enumblockmirror);
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockPoweredRail.SHAPE, BlockPoweredRail.POWERED});
    }

    static class SyntheticClass_1 {

        static final int[] a;
        static final int[] b;
        static final int[] c = new int[EnumBlockMirror.values().length];

        static {
            try {
                BlockPoweredRail.SyntheticClass_1.c[EnumBlockMirror.LEFT_RIGHT.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.c[EnumBlockMirror.FRONT_BACK.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            b = new int[EnumBlockRotation.values().length];

            try {
                BlockPoweredRail.SyntheticClass_1.b[EnumBlockRotation.CLOCKWISE_180.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.b[EnumBlockRotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.b[EnumBlockRotation.CLOCKWISE_90.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            a = new int[BlockMinecartTrackAbstract.EnumTrackPosition.values().length];

            try {
                BlockPoweredRail.SyntheticClass_1.a[BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.a[BlockMinecartTrackAbstract.EnumTrackPosition.EAST_WEST.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.a[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_EAST.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror7) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.a[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_WEST.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror8) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.a[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_NORTH.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror9) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.a[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_SOUTH.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror10) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.a[BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_EAST.ordinal()] = 7;
            } catch (NoSuchFieldError nosuchfielderror11) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.a[BlockMinecartTrackAbstract.EnumTrackPosition.SOUTH_WEST.ordinal()] = 8;
            } catch (NoSuchFieldError nosuchfielderror12) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.a[BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_WEST.ordinal()] = 9;
            } catch (NoSuchFieldError nosuchfielderror13) {
                ;
            }

            try {
                BlockPoweredRail.SyntheticClass_1.a[BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_EAST.ordinal()] = 10;
            } catch (NoSuchFieldError nosuchfielderror14) {
                ;
            }

        }
    }
}
