package net.minecraft.server;

import java.util.Iterator;
import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class BlockVine extends Block {

    public static final BlockStateBoolean UP = BlockStateBoolean.of("up");
    public static final BlockStateBoolean NORTH = BlockStateBoolean.of("north");
    public static final BlockStateBoolean EAST = BlockStateBoolean.of("east");
    public static final BlockStateBoolean SOUTH = BlockStateBoolean.of("south");
    public static final BlockStateBoolean WEST = BlockStateBoolean.of("west");
    public static final BlockStateBoolean[] f = new BlockStateBoolean[] { BlockVine.UP, BlockVine.NORTH, BlockVine.SOUTH, BlockVine.WEST, BlockVine.EAST};
    protected static final AxisAlignedBB g = new AxisAlignedBB(0.0D, 0.9375D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB B = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0625D, 1.0D, 1.0D);
    protected static final AxisAlignedBB C = new AxisAlignedBB(0.9375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB D = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.0625D);
    protected static final AxisAlignedBB E = new AxisAlignedBB(0.0D, 0.0D, 0.9375D, 1.0D, 1.0D, 1.0D);

    public BlockVine() {
        super(Material.REPLACEABLE_PLANT);
        this.w(this.blockStateList.getBlockData().set(BlockVine.UP, Boolean.valueOf(false)).set(BlockVine.NORTH, Boolean.valueOf(false)).set(BlockVine.EAST, Boolean.valueOf(false)).set(BlockVine.SOUTH, Boolean.valueOf(false)).set(BlockVine.WEST, Boolean.valueOf(false)));
        this.a(true);
        this.a(CreativeModeTab.c);
    }

    @Nullable
    public AxisAlignedBB a(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return BlockVine.k;
    }

    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        iblockdata = iblockdata.b(iblockaccess, blockposition);
        int i = 0;
        AxisAlignedBB axisalignedbb = BlockVine.j;

        if (((Boolean) iblockdata.get(BlockVine.UP)).booleanValue()) {
            axisalignedbb = BlockVine.g;
            ++i;
        }

        if (((Boolean) iblockdata.get(BlockVine.NORTH)).booleanValue()) {
            axisalignedbb = BlockVine.D;
            ++i;
        }

        if (((Boolean) iblockdata.get(BlockVine.EAST)).booleanValue()) {
            axisalignedbb = BlockVine.C;
            ++i;
        }

        if (((Boolean) iblockdata.get(BlockVine.SOUTH)).booleanValue()) {
            axisalignedbb = BlockVine.E;
            ++i;
        }

        if (((Boolean) iblockdata.get(BlockVine.WEST)).booleanValue()) {
            axisalignedbb = BlockVine.B;
            ++i;
        }

        return i == 1 ? axisalignedbb : BlockVine.j;
    }

    public IBlockData updateState(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.set(BlockVine.UP, Boolean.valueOf(iblockaccess.getType(blockposition.up()).k()));
    }

    public boolean b(IBlockData iblockdata) {
        return false;
    }

    public boolean c(IBlockData iblockdata) {
        return false;
    }

    public boolean a(IBlockAccess iblockaccess, BlockPosition blockposition) {
        return true;
    }

    public boolean canPlace(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        switch (BlockVine.SyntheticClass_1.a[enumdirection.ordinal()]) {
        case 1:
            return this.x(world.getType(blockposition.up()));

        case 2:
        case 3:
        case 4:
        case 5:
            return this.x(world.getType(blockposition.shift(enumdirection.opposite())));

        default:
            return false;
        }
    }

    private boolean x(IBlockData iblockdata) {
        return iblockdata.h() && iblockdata.getMaterial().isSolid();
    }

    private boolean e(World world, BlockPosition blockposition, IBlockData iblockdata) {
        IBlockData iblockdata1 = iblockdata;
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection = (EnumDirection) iterator.next();
            BlockStateBoolean blockstateboolean = getDirection(enumdirection);

            if (((Boolean) iblockdata.get(blockstateboolean)).booleanValue() && !this.x(world.getType(blockposition.shift(enumdirection)))) {
                IBlockData iblockdata2 = world.getType(blockposition.up());

                if (iblockdata2.getBlock() != this || !((Boolean) iblockdata2.get(blockstateboolean)).booleanValue()) {
                    iblockdata = iblockdata.set(blockstateboolean, Boolean.valueOf(false));
                }
            }
        }

        if (i(iblockdata) == 0) {
            return false;
        } else {
            if (iblockdata1 != iblockdata) {
                world.setTypeAndData(blockposition, iblockdata, 2);
            }

            return true;
        }
    }

    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {
        if (!world.isClientSide && !this.e(world, blockposition, iblockdata)) {
            this.b(world, blockposition, iblockdata, 0);
            world.setAir(blockposition);
        }

    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (!world.isClientSide) {
            if (world.random.nextInt(4) == 0) {
                byte b0 = 4;
                int i = 5;
                boolean flag = false;

                label188:
                for (int j = -b0; j <= b0; ++j) {
                    for (int k = -b0; k <= b0; ++k) {
                        for (int l = -1; l <= 1; ++l) {
                            if (world.getType(blockposition.a(j, l, k)).getBlock() == this) {
                                --i;
                                if (i <= 0) {
                                    flag = true;
                                    break label188;
                                }
                            }
                        }
                    }
                }

                EnumDirection enumdirection = EnumDirection.a(random);
                BlockPosition blockposition1 = blockposition.up();

                if (enumdirection == EnumDirection.UP && blockposition.getY() < 255 && world.isEmpty(blockposition1)) {
                    if (!flag) {
                        IBlockData iblockdata1 = iblockdata;
                        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

                        while (iterator.hasNext()) {
                            EnumDirection enumdirection1 = (EnumDirection) iterator.next();

                            if (random.nextBoolean() || !this.x(world.getType(blockposition1.shift(enumdirection1)))) {
                                iblockdata1 = iblockdata1.set(getDirection(enumdirection1), Boolean.valueOf(false));
                            }
                        }

                        if (((Boolean) iblockdata1.get(BlockVine.NORTH)).booleanValue() || ((Boolean) iblockdata1.get(BlockVine.EAST)).booleanValue() || ((Boolean) iblockdata1.get(BlockVine.SOUTH)).booleanValue() || ((Boolean) iblockdata1.get(BlockVine.WEST)).booleanValue()) {
                            // CraftBukkit start - Call BlockSpreadEvent
                            // world.setTypeAndData(blockposition1, iblockdata1, 2);
                            BlockPosition target = blockposition1;
                            org.bukkit.block.Block source = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                            org.bukkit.block.Block block = world.getWorld().getBlockAt(target.getX(), target.getY(), target.getZ());
                            CraftEventFactory.handleBlockSpreadEvent(block, source, this, toLegacyData(iblockdata1));
                            // CraftBukkit end
                        }

                    }
                } else {
                    IBlockData iblockdata2;
                    Block block;
                    BlockPosition blockposition2;

                    if (enumdirection.k().c() && !((Boolean) iblockdata.get(getDirection(enumdirection))).booleanValue()) {
                        if (!flag) {
                            blockposition2 = blockposition.shift(enumdirection);
                            iblockdata2 = world.getType(blockposition2);
                            block = iblockdata2.getBlock();
                            if (block.material == Material.AIR) {
                                EnumDirection enumdirection2 = enumdirection.e();
                                EnumDirection enumdirection3 = enumdirection.f();
                                boolean flag1 = ((Boolean) iblockdata.get(getDirection(enumdirection2))).booleanValue();
                                boolean flag2 = ((Boolean) iblockdata.get(getDirection(enumdirection3))).booleanValue();
                                BlockPosition blockposition3 = blockposition2.shift(enumdirection2);
                                BlockPosition blockposition4 = blockposition2.shift(enumdirection3);

                                // CraftBukkit start - Call BlockSpreadEvent
                                org.bukkit.block.Block source = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                                org.bukkit.block.Block bukkitBlock = world.getWorld().getBlockAt(blockposition2.getX(), blockposition2.getY(), blockposition2.getZ());

                                if (flag1 && this.x(world.getType(blockposition3))) {
                                    // world.setTypeAndData(blockposition2, this.getBlockData().set(getDirection(enumdirection2), Boolean.valueOf(true)), 2);
                                    CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, toLegacyData(this.getBlockData().set(getDirection(enumdirection2), Boolean.valueOf(true))));
                                } else if (flag2 && this.x(world.getType(blockposition4))) {
                                    // world.setTypeAndData(blockposition2, this.getBlockData().set(getDirection(enumdirection3), Boolean.valueOf(true)), 2);
                                    CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, toLegacyData(this.getBlockData().set(getDirection(enumdirection3), Boolean.valueOf(true))));
                                } else if (flag1 && world.isEmpty(blockposition3) && this.x(world.getType(blockposition.shift(enumdirection2)))) {
                                    // world.setTypeAndData(blockposition3, this.getBlockData().set(getDirection(enumdirection.opposite()), Boolean.valueOf(true)), 2);
                                    bukkitBlock = world.getWorld().getBlockAt(blockposition3.getX(), blockposition3.getY(), blockposition3.getZ());
                                    CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, toLegacyData(this.getBlockData().set(getDirection(enumdirection.opposite()), Boolean.valueOf(true))));
                                } else if (flag2 && world.isEmpty(blockposition4) && this.x(world.getType(blockposition.shift(enumdirection3)))) {
                                    // world.setTypeAndData(blockposition4, this.getBlockData().set(getDirection(enumdirection.opposite()), Boolean.valueOf(true)), 2);
                                    bukkitBlock = world.getWorld().getBlockAt(blockposition4.getX(), blockposition4.getY(), blockposition4.getZ());
                                    CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, toLegacyData(this.getBlockData().set(getDirection(enumdirection.opposite()), Boolean.valueOf(true))));
                                } else if (this.x(world.getType(blockposition2.up()))) {
                                    // world.setTypeAndData(blockposition2, this.getBlockData(), 2);
                                    CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, toLegacyData(this.getBlockData()));
                                }
                                // CraftBukkit end
                            } else if (block.material.k() && iblockdata2.h()) {
                                world.setTypeAndData(blockposition, iblockdata.set(getDirection(enumdirection), Boolean.valueOf(true)), 2);
                            }

                        }
                    } else {
                        if (blockposition.getY() > 1) {
                            blockposition2 = blockposition.down();
                            iblockdata2 = world.getType(blockposition2);
                            block = iblockdata2.getBlock();
                            IBlockData iblockdata3;
                            Iterator iterator1;
                            EnumDirection enumdirection4;

                            if (block.material == Material.AIR) {
                                iblockdata3 = iblockdata;
                                iterator1 = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

                                while (iterator1.hasNext()) {
                                    enumdirection4 = (EnumDirection) iterator1.next();
                                    if (random.nextBoolean()) {
                                        iblockdata3 = iblockdata3.set(getDirection(enumdirection4), Boolean.valueOf(false));
                                    }
                                }

                                if (((Boolean) iblockdata3.get(BlockVine.NORTH)).booleanValue() || ((Boolean) iblockdata3.get(BlockVine.EAST)).booleanValue() || ((Boolean) iblockdata3.get(BlockVine.SOUTH)).booleanValue() || ((Boolean) iblockdata3.get(BlockVine.WEST)).booleanValue()) {
                                    // CraftBukkit start - Call BlockSpreadEvent
                                    // world.setTypeAndData(blockposition2, iblockdata3, 2);
                                    org.bukkit.block.Block source = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                                    org.bukkit.block.Block bukkitBlock = world.getWorld().getBlockAt(blockposition2.getX(), blockposition2.getY(), blockposition2.getZ());
                                    CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, toLegacyData(iblockdata3));
                                    // CraftBukkit end
                                }
                            } else if (block == this) {
                                iblockdata3 = iblockdata2;
                                iterator1 = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

                                while (iterator1.hasNext()) {
                                    enumdirection4 = (EnumDirection) iterator1.next();
                                    BlockStateBoolean blockstateboolean = getDirection(enumdirection4);

                                    if (random.nextBoolean() && ((Boolean) iblockdata.get(blockstateboolean)).booleanValue()) {
                                        iblockdata3 = iblockdata3.set(blockstateboolean, Boolean.valueOf(true));
                                    }
                                }

                                if (((Boolean) iblockdata3.get(BlockVine.NORTH)).booleanValue() || ((Boolean) iblockdata3.get(BlockVine.EAST)).booleanValue() || ((Boolean) iblockdata3.get(BlockVine.SOUTH)).booleanValue() || ((Boolean) iblockdata3.get(BlockVine.WEST)).booleanValue()) {
                                    world.setTypeAndData(blockposition2, iblockdata3, 2);
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        IBlockData iblockdata = this.getBlockData().set(BlockVine.UP, Boolean.valueOf(false)).set(BlockVine.NORTH, Boolean.valueOf(false)).set(BlockVine.EAST, Boolean.valueOf(false)).set(BlockVine.SOUTH, Boolean.valueOf(false)).set(BlockVine.WEST, Boolean.valueOf(false));

        return enumdirection.k().c() ? iblockdata.set(getDirection(enumdirection.opposite()), Boolean.valueOf(true)) : iblockdata;
    }

    @Nullable
    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return null;
    }

    public int a(Random random) {
        return 0;
    }

    public void a(World world, EntityHuman entityhuman, BlockPosition blockposition, IBlockData iblockdata, @Nullable TileEntity tileentity, @Nullable ItemStack itemstack) {
        if (!world.isClientSide && itemstack != null && itemstack.getItem() == Items.SHEARS) {
            entityhuman.b(StatisticList.a((Block) this));
            a(world, blockposition, new ItemStack(Blocks.VINE, 1, 0));
        } else {
            super.a(world, entityhuman, blockposition, iblockdata, tileentity, itemstack);
        }

    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockVine.SOUTH, Boolean.valueOf((i & 1) > 0)).set(BlockVine.WEST, Boolean.valueOf((i & 2) > 0)).set(BlockVine.NORTH, Boolean.valueOf((i & 4) > 0)).set(BlockVine.EAST, Boolean.valueOf((i & 8) > 0));
    }

    public int toLegacyData(IBlockData iblockdata) {
        int i = 0;

        if (((Boolean) iblockdata.get(BlockVine.SOUTH)).booleanValue()) {
            i |= 1;
        }

        if (((Boolean) iblockdata.get(BlockVine.WEST)).booleanValue()) {
            i |= 2;
        }

        if (((Boolean) iblockdata.get(BlockVine.NORTH)).booleanValue()) {
            i |= 4;
        }

        if (((Boolean) iblockdata.get(BlockVine.EAST)).booleanValue()) {
            i |= 8;
        }

        return i;
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockVine.UP, BlockVine.NORTH, BlockVine.EAST, BlockVine.SOUTH, BlockVine.WEST});
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        switch (BlockVine.SyntheticClass_1.b[enumblockrotation.ordinal()]) {
        case 1:
            return iblockdata.set(BlockVine.NORTH, iblockdata.get(BlockVine.SOUTH)).set(BlockVine.EAST, iblockdata.get(BlockVine.WEST)).set(BlockVine.SOUTH, iblockdata.get(BlockVine.NORTH)).set(BlockVine.WEST, iblockdata.get(BlockVine.EAST));

        case 2:
            return iblockdata.set(BlockVine.NORTH, iblockdata.get(BlockVine.EAST)).set(BlockVine.EAST, iblockdata.get(BlockVine.SOUTH)).set(BlockVine.SOUTH, iblockdata.get(BlockVine.WEST)).set(BlockVine.WEST, iblockdata.get(BlockVine.NORTH));

        case 3:
            return iblockdata.set(BlockVine.NORTH, iblockdata.get(BlockVine.WEST)).set(BlockVine.EAST, iblockdata.get(BlockVine.NORTH)).set(BlockVine.SOUTH, iblockdata.get(BlockVine.EAST)).set(BlockVine.WEST, iblockdata.get(BlockVine.SOUTH));

        default:
            return iblockdata;
        }
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        switch (BlockVine.SyntheticClass_1.c[enumblockmirror.ordinal()]) {
        case 1:
            return iblockdata.set(BlockVine.NORTH, iblockdata.get(BlockVine.SOUTH)).set(BlockVine.SOUTH, iblockdata.get(BlockVine.NORTH));

        case 2:
            return iblockdata.set(BlockVine.EAST, iblockdata.get(BlockVine.WEST)).set(BlockVine.WEST, iblockdata.get(BlockVine.EAST));

        default:
            return super.a(iblockdata, enumblockmirror);
        }
    }

    public static BlockStateBoolean getDirection(EnumDirection enumdirection) {
        switch (BlockVine.SyntheticClass_1.a[enumdirection.ordinal()]) {
        case 1:
            return BlockVine.UP;

        case 2:
            return BlockVine.NORTH;

        case 3:
            return BlockVine.SOUTH;

        case 4:
            return BlockVine.EAST;

        case 5:
            return BlockVine.WEST;

        default:
            throw new IllegalArgumentException(enumdirection + " is an invalid choice");
        }
    }

    public static int i(IBlockData iblockdata) {
        int i = 0;
        BlockStateBoolean[] ablockstateboolean = BlockVine.f;
        int j = ablockstateboolean.length;

        for (int k = 0; k < j; ++k) {
            BlockStateBoolean blockstateboolean = ablockstateboolean[k];

            if (((Boolean) iblockdata.get(blockstateboolean)).booleanValue()) {
                ++i;
            }
        }

        return i;
    }

    static class SyntheticClass_1 {

        static final int[] a;
        static final int[] b;
        static final int[] c = new int[EnumBlockMirror.values().length];

        static {
            try {
                BlockVine.SyntheticClass_1.c[EnumBlockMirror.LEFT_RIGHT.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                BlockVine.SyntheticClass_1.c[EnumBlockMirror.FRONT_BACK.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            b = new int[EnumBlockRotation.values().length];

            try {
                BlockVine.SyntheticClass_1.b[EnumBlockRotation.CLOCKWISE_180.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                BlockVine.SyntheticClass_1.b[EnumBlockRotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                BlockVine.SyntheticClass_1.b[EnumBlockRotation.CLOCKWISE_90.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            a = new int[EnumDirection.values().length];

            try {
                BlockVine.SyntheticClass_1.a[EnumDirection.UP.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            try {
                BlockVine.SyntheticClass_1.a[EnumDirection.NORTH.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            try {
                BlockVine.SyntheticClass_1.a[EnumDirection.SOUTH.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror7) {
                ;
            }

            try {
                BlockVine.SyntheticClass_1.a[EnumDirection.EAST.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror8) {
                ;
            }

            try {
                BlockVine.SyntheticClass_1.a[EnumDirection.WEST.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror9) {
                ;
            }

        }
    }
}
