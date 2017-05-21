package net.minecraft.server;

import com.google.common.collect.Lists;

import net.minecraft.server.BlockPistonExtension.EnumPistonType;

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
import org.bukkit.event.block.BlockPistonEvent;
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
        this.y(this.blockStateList.getBlockData().set(BlockPiston.FACING, EnumDirection.NORTH).set(BlockPiston.EXTENDED, Boolean.valueOf(false)));
        this.sticky = flag;
        this.a(SoundEffectType.d);
        this.c(0.5F);
        this.a(CreativeModeTab.d);
    }

    @Override
    public boolean u(IBlockData iblockdata) {
        return !iblockdata.get(BlockPiston.EXTENDED).booleanValue();
    }

    @Override
    public AxisAlignedBB b(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        if (iblockdata.get(BlockPiston.EXTENDED).booleanValue()) {
            switch (iblockdata.get(BlockPiston.FACING)) {
            case DOWN:
                return BlockPiston.g;

            case UP:
            default:
                return BlockPiston.f;

            case NORTH:
                return BlockPiston.e;

            case SOUTH:
                return BlockPiston.d;

            case WEST:
                return BlockPiston.c;

            case EAST:
                return BlockPiston.b;
            }
        } else {
            return BlockPiston.j;
        }
    }

    @Override
    public boolean k(IBlockData iblockdata) {
        return !iblockdata.get(BlockPiston.EXTENDED).booleanValue() || iblockdata.get(BlockPiston.FACING) == EnumDirection.DOWN;
    }

    @Override
    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, AxisAlignedBB axisalignedbb, List<AxisAlignedBB> list, @Nullable Entity entity, boolean flag) {
        a(blockposition, axisalignedbb, list, iblockdata.d(world, blockposition));
    }

    @Override
    public boolean b(IBlockData iblockdata) {
        return false;
    }

    @Override
    public void postPlace(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        world.setTypeAndData(blockposition, iblockdata.set(BlockPiston.FACING, EnumDirection.a(blockposition, entityliving)), 2);
        this.e(world, blockposition, iblockdata);

    }

    @Override
    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1) {
        this.e(world, blockposition, iblockdata);

    }

    @Override
    public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (world.getTileEntity(blockposition) == null) {
            this.e(world, blockposition, iblockdata);
        }

    }

    @Override
    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        return this.getBlockData().set(BlockPiston.FACING, EnumDirection.a(blockposition, entityliving)).set(BlockPiston.EXTENDED, Boolean.valueOf(false));
    }

    private void e(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection enumdirection = iblockdata.get(BlockPiston.FACING);
        boolean flag = this.a(world, blockposition, enumdirection);

        if (flag && !iblockdata.get(BlockPiston.EXTENDED).booleanValue()) {
            if ((new PistonExtendsChecker(world, blockposition, enumdirection, true)).a()) {
                world.playBlockAction(blockposition, this, 0, enumdirection.a());
            }
        } else if (!flag && iblockdata.get(BlockPiston.EXTENDED).booleanValue()) {
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

    @Override
    public boolean a(IBlockData iblockdata, World world, BlockPosition blockposition, int i, int j) {
        EnumDirection enumdirection = iblockdata.get(BlockPiston.FACING);

        boolean flag = this.a(world, blockposition, enumdirection);

        if (flag && i == 1) {
            world.setTypeAndData(blockposition, iblockdata.set(BlockPiston.EXTENDED, Boolean.valueOf(true)), 2);
            return false;
        }

        if (!flag && i == 0) {
            return false;
        }

        if (i == 0) {
            if (!this.a(world, blockposition, enumdirection, true)) {
                return false;
            }

            world.setTypeAndData(blockposition, iblockdata.set(BlockPiston.EXTENDED, Boolean.valueOf(true)), 3);
            world.a((EntityHuman) null, blockposition, SoundEffects.ev, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
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

                        if (tileentitypiston.f() == enumdirection && tileentitypiston.e()) {
                            tileentitypiston.i();
                            flag1 = true;
                        }
                    }
                }

                if (!flag1 && a(iblockdata1, world, blockposition1, enumdirection.opposite(), false) && (iblockdata1.p() == EnumPistonReaction.NORMAL || block == Blocks.PISTON || block == Blocks.STICKY_PISTON)) { // CraftBukkit - remove 'block.getMaterial() != Material.AIR' condition
                    this.a(world, blockposition, enumdirection, false);
                }
            } else {
                world.setAir(blockposition.shift(enumdirection));
            }

            world.a((EntityHuman) null, blockposition, SoundEffects.eu, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.15F + 0.6F);
        }

        return true;
    }

    @Override
    public boolean c(IBlockData iblockdata) {
        return false;
    }

    @Nullable
    public static EnumDirection e(int i) {
        int j = i & 7;

        return j > 5 ? null : EnumDirection.fromType1(j);
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

                    if (iblockdata.p() == EnumPistonReaction.BLOCK) {
                        return false;
                    }

                    if (iblockdata.p() == EnumPistonReaction.DESTROY) {
                        return flag;
                    }
                } else if (iblockdata.get(BlockPiston.EXTENDED).booleanValue()) {
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

    /** PAIL: doMove */
    private boolean a(World world, BlockPosition position, EnumDirection direction, boolean extending) {
        if (!extending) world.setAir(position.shift(direction));
        
        PistonExtendsChecker extendsChecker = new PistonExtendsChecker(world, position, direction, extending);
        if (!extendsChecker.a()) return false; // PAIL: a() -> canMove()
        
        List<BlockPosition> movedBlockPos = extendsChecker.getMovedBlocks();
        ArrayList<IBlockData> movedBlocks = Lists.newArrayList();

        for (int i = 0, size = movedBlockPos.size(); i < size; i++) {
            BlockPosition eachBlock = movedBlockPos.get(i);
            movedBlocks.add(world.getType(eachBlock).b((IBlockAccess) world, eachBlock)); // PAIL: b -> getActualState
        }
        
        List<BlockPosition> brokenBlockPos = extendsChecker.getBrokenBlocks();
        int blockSize = movedBlockPos.size() + brokenBlockPos.size();
        IBlockData[] blocks = new IBlockData[blockSize];
        EnumDirection actualDirection = extending ? direction : direction.opposite();
        
        // CraftBukkit start
        final org.bukkit.block.Block bukkitBlock = world.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());
        final List<BlockPosition> moved = extendsChecker.getMovedBlocks();
        final List<BlockPosition> broken = extendsChecker.getBrokenBlocks();
        
        List<org.bukkit.block.Block> bukkitBlocks = new AbstractList<org.bukkit.block.Block>() {
            @Override
            public int size() {
                return moved.size() + broken.size();
            }

            @Override
            public org.bukkit.block.Block get(int index) {
                if (index >= size() || index < 0) {
                    throw new ArrayIndexOutOfBoundsException(index);
                }
                BlockPosition pos = index < moved.size() ? moved.get(index) : broken.get(index - moved.size());
                return bukkitBlock.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
            }
        };
        
        BlockPistonEvent event;
        if (extending) {
            event = new BlockPistonExtendEvent(bukkitBlock, bukkitBlocks, CraftBlock.notchToBlockFace(actualDirection));
        } else {
            event = new BlockPistonRetractEvent(bukkitBlock, bukkitBlocks, CraftBlock.notchToBlockFace(actualDirection));
        }
        
        world.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            for (BlockPosition pos : broken) {
                world.notify(pos, Blocks.AIR.getBlockData(), world.getType(pos), 3);
            }
            
            for (BlockPosition pos : moved) {
                world.notify(pos, Blocks.AIR.getBlockData(), world.getType(pos), 3);
                pos = pos.shift(actualDirection);
                world.notify(pos, Blocks.AIR.getBlockData(), world.getType(pos), 3);
            }
            return false;
        }
        // CraftBukkit end

        int index;
        BlockPosition eachBlock;
        IBlockData eachType;
        for (index = brokenBlockPos.size() - 1; index >= 0; --index) {
            eachBlock = brokenBlockPos.get(index);
            eachType = world.getType(eachBlock);
            eachType.getBlock().b(world, eachBlock, eachType, 0); // PAIL: b -> dropBlock
            world.setTypeAndData(eachBlock, Blocks.AIR.getBlockData(), 4);
            
            blockSize--;
            blocks[blockSize] = eachType;
        }

        for (index = movedBlockPos.size() - 1; index >= 0; --index) {
            eachBlock = movedBlockPos.get(index);
            eachType = world.getType(eachBlock);
            world.setTypeAndData(eachBlock, Blocks.AIR.getBlockData(), 2);
            
            eachBlock = eachBlock.shift(actualDirection);
            world.setTypeAndData(eachBlock, Blocks.PISTON_EXTENSION.getBlockData().set(BlockPiston.FACING, direction), 4);
            world.setTileEntity(eachBlock, BlockPistonMoving.a(movedBlocks.get(index), direction, extending, false));
            
            blockSize--;
            blocks[blockSize] = eachType;
        }

        BlockPosition offset = position.shift(direction);
        if (extending) {
            EnumPistonType blockpistonextension_enumpistontype = this.sticky ? EnumPistonType.STICKY : EnumPistonType.DEFAULT;

            eachType = Blocks.PISTON_HEAD.getBlockData().set(BlockPistonExtension.FACING, direction).set(BlockPistonExtension.TYPE, blockpistonextension_enumpistontype);
            IBlockData iblockdata1 = Blocks.PISTON_EXTENSION.getBlockData().set(BlockPistonMoving.FACING, direction).set(BlockPistonMoving.TYPE, this.sticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT);

            world.setTypeAndData(offset, iblockdata1, 4);
            world.setTileEntity(offset, BlockPistonMoving.a(eachType, direction, true, true));
        }

        index = 0;
        for (index = brokenBlockPos.size() - 1; index >= 0; --index) {
            world.applyPhysics(brokenBlockPos.get(index), blocks[blockSize].getBlock(), false);
        }

        for (index = movedBlockPos.size() - 1; index >= 0; --index) {
            world.applyPhysics(movedBlockPos.get(index), blocks[blockSize].getBlock(), false);
        }

        if (extending) {
            world.applyPhysics(offset, Blocks.PISTON_HEAD, false);
        }
        
        return true;
    }

    @Override
    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockPiston.FACING, e(i)).set(BlockPiston.EXTENDED, Boolean.valueOf((i & 8) > 0));
    }

    @Override
    public int toLegacyData(IBlockData iblockdata) {
        byte b0 = 0;
        int i = b0 | iblockdata.get(BlockPiston.FACING).a();

        if (iblockdata.get(BlockPiston.EXTENDED).booleanValue()) {
            i |= 8;
        }

        return i;
    }

    @Override
    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return iblockdata.set(BlockPiston.FACING, enumblockrotation.a(iblockdata.get(BlockPiston.FACING)));
    }

    @Override
    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.a(enumblockmirror.a(iblockdata.get(BlockPiston.FACING)));
    }

    @Override
    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockPiston.FACING, BlockPiston.EXTENDED});
    }
}
