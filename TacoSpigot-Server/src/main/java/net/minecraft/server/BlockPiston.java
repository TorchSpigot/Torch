package net.minecraft.server;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

// CraftBukkit start
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import com.google.common.collect.ImmutableList;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
// CraftBukkit end

public class BlockPiston extends BlockDirectional {

    public static final BlockStateBoolean EXTENDED = BlockStateBoolean.of("extended");
    protected static final AxisAlignedBB b = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D);
    protected static final AxisAlignedBB c = new AxisAlignedBB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB d = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D);
    protected static final AxisAlignedBB e = new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB f = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);
    protected static final AxisAlignedBB g = new AxisAlignedBB(0.0D, 0.25D, 0.0D, 1.0D, 1.0D, 1.0D);
    private final boolean sticky;

    public BlockPiston(boolean flag) {
        super(Material.PISTON);
        this.w(this.blockStateList.getBlockData().set(BlockPiston.FACING, EnumDirection.NORTH).set(BlockPiston.EXTENDED, Boolean.valueOf(false)));
        this.sticky = flag;
        this.a(SoundEffectType.d);
        this.c(0.5F);
        this.a(CreativeModeTab.d);
    }

    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        if (((Boolean) iblockdata.get(BlockPiston.EXTENDED)).booleanValue()) {
            switch (BlockPiston.SyntheticClass_1.a[((EnumDirection) iblockdata.get(BlockPiston.FACING)).ordinal()]) {
            case 1:
                return BlockPiston.g;

            case 2:
            default:
                return BlockPiston.f;

            case 3:
                return BlockPiston.e;

            case 4:
                return BlockPiston.d;

            case 5:
                return BlockPiston.c;

            case 6:
                return BlockPiston.b;
            }
        } else {
            return BlockPiston.j;
        }
    }

    public boolean k(IBlockData iblockdata) {
        return !((Boolean) iblockdata.get(BlockPiston.EXTENDED)).booleanValue() || iblockdata.get(BlockPiston.FACING) == EnumDirection.DOWN;
    }

    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, AxisAlignedBB axisalignedbb, List<AxisAlignedBB> list, @Nullable Entity entity) {
        a(blockposition, axisalignedbb, list, iblockdata.c(world, blockposition));
    }

    public boolean b(IBlockData iblockdata) {
        return false;
    }

    public void postPlace(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        world.setTypeAndData(blockposition, iblockdata.set(BlockPiston.FACING, a(blockposition, entityliving)), 2);
        if (!world.isClientSide) {
            this.e(world, blockposition, iblockdata);
        }

    }

    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {
        if (!world.isClientSide) {
            this.e(world, blockposition, iblockdata);
        }

    }

    public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (!world.isClientSide && world.getTileEntity(blockposition) == null) {
            this.e(world, blockposition, iblockdata);
        }

    }

    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        return this.getBlockData().set(BlockPiston.FACING, a(blockposition, entityliving)).set(BlockPiston.EXTENDED, Boolean.valueOf(false));
    }

    private void e(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.get(BlockPiston.FACING);
        boolean flag = this.a(world, blockposition, enumdirection);

        if (flag && !((Boolean) iblockdata.get(BlockPiston.EXTENDED)).booleanValue()) {
            if ((new PistonExtendsChecker(world, blockposition, enumdirection, true)).a()) {
                world.playBlockAction(blockposition, this, 0, enumdirection.a());
            }
        } else if (!flag && ((Boolean) iblockdata.get(BlockPiston.EXTENDED)).booleanValue()) {
            // CraftBukkit start
            if (!this.sticky) {
                org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                BlockPistonRetractEvent event = new BlockPistonRetractEvent(block, ImmutableList.<org.bukkit.block.Block>of(), CraftBlock.notchToBlockFace(enumdirection));
                world.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return;
                }
            }
            // PAIL: checkME - what happened to setTypeAndData?
            // CraftBukkit end
            world.playBlockAction(blockposition, this, 1, enumdirection.a());
        }

    }

    private boolean a(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        EnumDirection[] aenumdirection = EnumDirection.values();
        int i = aenumdirection.length;

        int j;

        for (j = 0; j < i; ++j) {
            EnumDirection enumdirection1 = aenumdirection[j];

            if (enumdirection1 != enumdirection && world.isBlockFacePowered(blockposition.shift(enumdirection1), enumdirection1)) {
                return true;
            }
        }

        if (world.isBlockFacePowered(blockposition, EnumDirection.DOWN)) {
            return true;
        } else {
            BlockPosition blockposition1 = blockposition.up();
            EnumDirection[] aenumdirection1 = EnumDirection.values();

            j = aenumdirection1.length;

            for (int k = 0; k < j; ++k) {
                EnumDirection enumdirection2 = aenumdirection1[k];

                if (enumdirection2 != EnumDirection.DOWN && world.isBlockFacePowered(blockposition1.shift(enumdirection2), enumdirection2)) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean a(IBlockData iblockdata, World world, BlockPosition blockposition, int i, int j) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.get(BlockPiston.FACING);

        if (!world.isClientSide) {
            boolean flag = this.a(world, blockposition, enumdirection);

            if (flag && i == 1) {
                world.setTypeAndData(blockposition, iblockdata.set(BlockPiston.EXTENDED, Boolean.valueOf(true)), 2);
                return false;
            }

            if (!flag && i == 0) {
                return false;
            }
        }

        if (i == 0) {
            if (!this.a(world, blockposition, enumdirection, true)) {
                return false;
            }

            world.setTypeAndData(blockposition, iblockdata.set(BlockPiston.EXTENDED, Boolean.valueOf(true)), 2);
            world.a((EntityHuman) null, blockposition, SoundEffects.dW, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
        } else if (i == 1) {
            TileEntity tileentity = world.getTileEntity(blockposition.shift(enumdirection));

            if (tileentity instanceof TileEntityPiston) {
                ((TileEntityPiston) tileentity).i();
            }

            world.setTypeAndData(blockposition, Blocks.PISTON_EXTENSION.getBlockData().set(BlockPistonMoving.FACING, enumdirection).set(BlockPistonMoving.TYPE, this.sticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT), 3);
            world.setTileEntity(blockposition, BlockPistonMoving.a(this.fromLegacyData(j), enumdirection, false, true));
            if (this.sticky) {
                BlockPosition blockposition1 = blockposition.a(enumdirection.getAdjacentX() * 2, enumdirection.getAdjacentY() * 2, enumdirection.getAdjacentZ() * 2);
                IBlockData iblockdata1 = world.getType(blockposition1);
                Block block = iblockdata1.getBlock();
                boolean flag1 = false;

                if (block == Blocks.PISTON_EXTENSION) {
                    TileEntity tileentity1 = world.getTileEntity(blockposition1);

                    if (tileentity1 instanceof TileEntityPiston) {
                        TileEntityPiston tileentitypiston = (TileEntityPiston) tileentity1;

                        if (tileentitypiston.g() == enumdirection && tileentitypiston.e()) {
                            tileentitypiston.i();
                            flag1 = true;
                        }
                    }
                }

                if (!flag1 && a(iblockdata1, world, blockposition1, enumdirection.opposite(), false) && (iblockdata1.o() == EnumPistonReaction.NORMAL || block == Blocks.PISTON || block == Blocks.STICKY_PISTON)) { // CraftBukkit - remove 'block.getMaterial() != Material.AIR' condition
                    this.a(world, blockposition, enumdirection, false);
                }
            } else {
                world.setAir(blockposition.shift(enumdirection));
            }

            world.a((EntityHuman) null, blockposition, SoundEffects.dV, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.15F + 0.6F);
        }

        return true;
    }

    public boolean c(IBlockData iblockdata) {
        return false;
    }

    @Nullable
    public static EnumDirection e(int i) {
        int j = i & 7;

        return j > 5 ? null : EnumDirection.fromType1(j);
    }

    public static EnumDirection a(BlockPosition blockposition, EntityLiving entityliving) {
        if (MathHelper.e((float) entityliving.locX - (float) blockposition.getX()) < 2.0F && MathHelper.e((float) entityliving.locZ - (float) blockposition.getZ()) < 2.0F) {
            double d0 = entityliving.locY + (double) entityliving.getHeadHeight();

            if (d0 - (double) blockposition.getY() > 2.0D) {
                return EnumDirection.UP;
            }

            if ((double) blockposition.getY() - d0 > 0.0D) {
                return EnumDirection.DOWN;
            }
        }

        return entityliving.getDirection().opposite();
    }

    public static boolean a(IBlockData iblockdata, World world, BlockPosition blockposition, EnumDirection enumdirection, boolean flag) {
        Block block = iblockdata.getBlock();

        if (block == Blocks.OBSIDIAN) {
            return false;
        } else if (!world.getWorldBorder().a(blockposition)) {
            return false;
        } else if (blockposition.getY() >= 0 && (enumdirection != EnumDirection.DOWN || blockposition.getY() != 0)) {
            if (blockposition.getY() <= world.getHeight() - 1 && (enumdirection != EnumDirection.UP || blockposition.getY() != world.getHeight() - 1)) {
                if (block != Blocks.PISTON && block != Blocks.STICKY_PISTON) {
                    if (iblockdata.b(world, blockposition) == -1.0F) {
                        return false;
                    }

                    if (iblockdata.o() == EnumPistonReaction.BLOCK) {
                        return false;
                    }

                    if (iblockdata.o() == EnumPistonReaction.DESTROY) {
                        return flag;
                    }
                } else if (((Boolean) iblockdata.get(BlockPiston.EXTENDED)).booleanValue()) {
                    return false;
                }

                return !block.isTileEntity();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean a(World world, BlockPosition blockposition, EnumDirection enumdirection, boolean flag) {
        if (!flag) {
            world.setAir(blockposition.shift(enumdirection));
        }

        PistonExtendsChecker pistonextendschecker = new PistonExtendsChecker(world, blockposition, enumdirection, flag);

        if (!pistonextendschecker.a()) {
            return false;
        } else {
            List list = pistonextendschecker.getMovedBlocks();
            ArrayList arraylist = Lists.newArrayList();

            for (int i = 0; i < list.size(); ++i) {
                BlockPosition blockposition1 = (BlockPosition) list.get(i);

                arraylist.add(world.getType(blockposition1).b((IBlockAccess) world, blockposition1));
            }

            List list1 = pistonextendschecker.getBrokenBlocks();
            int j = list.size() + list1.size();
            IBlockData[] aiblockdata = new IBlockData[j];
            EnumDirection enumdirection1 = flag ? enumdirection : enumdirection.opposite();
            // CraftBukkit start
            final org.bukkit.block.Block bblock = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());

            final List<BlockPosition> moved = pistonextendschecker.getMovedBlocks();
            final List<BlockPosition> broken = pistonextendschecker.getBrokenBlocks();

            List<org.bukkit.block.Block> blocks = new AbstractList<org.bukkit.block.Block>() {

                @Override
                public int size() {
                    return moved.size() + broken.size();
                }

                @Override
                public org.bukkit.block.Block get(int index) {
                    if (index >= size() || index < 0) {
                        throw new ArrayIndexOutOfBoundsException(index);
                    }
                    BlockPosition pos = (BlockPosition) (index < moved.size() ? moved.get(index) : broken.get(index - moved.size()));
                    return bblock.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
                }
            };
            org.bukkit.event.block.BlockPistonEvent event;
            if (flag) {
                event = new BlockPistonExtendEvent(bblock, blocks, CraftBlock.notchToBlockFace(enumdirection1));
            } else {
                event = new BlockPistonRetractEvent(bblock, blocks, CraftBlock.notchToBlockFace(enumdirection1));
            }
            world.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                for (BlockPosition b : broken) {
                    world.notify(b, Blocks.AIR.getBlockData(), world.getType(b), 3);
                }
                for (BlockPosition b : moved) {
                    world.notify(b, Blocks.AIR.getBlockData(), world.getType(b), 3);
                    b = b.shift(enumdirection1);
                    world.notify(b, Blocks.AIR.getBlockData(), world.getType(b), 3);
                }
                return false;
            }
            // CraftBukkit end

            int k;
            BlockPosition blockposition2;
            IBlockData iblockdata;

            for (k = list1.size() - 1; k >= 0; --k) {
                blockposition2 = (BlockPosition) list1.get(k);
                iblockdata = world.getType(blockposition2);
                iblockdata.getBlock().b(world, blockposition2, iblockdata, 0);
                world.setAir(blockposition2);
                --j;
                aiblockdata[j] = iblockdata;
            }

            for (k = list.size() - 1; k >= 0; --k) {
                blockposition2 = (BlockPosition) list.get(k);
                iblockdata = world.getType(blockposition2);
                world.setTypeAndData(blockposition2, Blocks.AIR.getBlockData(), 2);
                blockposition2 = blockposition2.shift(enumdirection1);
                world.setTypeAndData(blockposition2, Blocks.PISTON_EXTENSION.getBlockData().set(BlockPiston.FACING, enumdirection), 4);
                world.setTileEntity(blockposition2, BlockPistonMoving.a((IBlockData) arraylist.get(k), enumdirection, flag, false));
                --j;
                aiblockdata[j] = iblockdata;
            }

            BlockPosition blockposition3 = blockposition.shift(enumdirection);

            if (flag) {
                BlockPistonExtension.EnumPistonType blockpistonextension_enumpistontype = this.sticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT;

                iblockdata = Blocks.PISTON_HEAD.getBlockData().set(BlockPistonExtension.FACING, enumdirection).set(BlockPistonExtension.TYPE, blockpistonextension_enumpistontype);
                IBlockData iblockdata1 = Blocks.PISTON_EXTENSION.getBlockData().set(BlockPistonMoving.FACING, enumdirection).set(BlockPistonMoving.TYPE, this.sticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT);

                world.setTypeAndData(blockposition3, iblockdata1, 4);
                world.setTileEntity(blockposition3, BlockPistonMoving.a(iblockdata, enumdirection, true, false));
            }

            int l;

            for (l = list1.size() - 1; l >= 0; --l) {
                world.applyPhysics((BlockPosition) list1.get(l), aiblockdata[j++].getBlock());
            }

            for (l = list.size() - 1; l >= 0; --l) {
                world.applyPhysics((BlockPosition) list.get(l), aiblockdata[j++].getBlock());
            }

            if (flag) {
                world.applyPhysics(blockposition3, Blocks.PISTON_HEAD);
                world.applyPhysics(blockposition, this);
            }

            return true;
        }
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockPiston.FACING, e(i)).set(BlockPiston.EXTENDED, Boolean.valueOf((i & 8) > 0));
    }

    public int toLegacyData(IBlockData iblockdata) {
        byte b0 = 0;
        int i = b0 | ((EnumDirection) iblockdata.get(BlockPiston.FACING)).a();

        if (((Boolean) iblockdata.get(BlockPiston.EXTENDED)).booleanValue()) {
            i |= 8;
        }

        return i;
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return iblockdata.set(BlockPiston.FACING, enumblockrotation.a((EnumDirection) iblockdata.get(BlockPiston.FACING)));
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.a(enumblockmirror.a((EnumDirection) iblockdata.get(BlockPiston.FACING)));
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockPiston.FACING, BlockPiston.EXTENDED});
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumDirection.values().length];

        static {
            try {
                BlockPiston.SyntheticClass_1.a[EnumDirection.DOWN.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                BlockPiston.SyntheticClass_1.a[EnumDirection.UP.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                BlockPiston.SyntheticClass_1.a[EnumDirection.NORTH.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                BlockPiston.SyntheticClass_1.a[EnumDirection.SOUTH.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                BlockPiston.SyntheticClass_1.a[EnumDirection.WEST.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                BlockPiston.SyntheticClass_1.a[EnumDirection.EAST.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

        }
    }
}
