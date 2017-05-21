package net.minecraft.server;

import java.util.Random;

public class BlockFurnace extends BlockTileEntity {

    public static final BlockStateDirection FACING = BlockFacingHorizontal.FACING;
    private final boolean b;
    private static boolean c;

    protected BlockFurnace(boolean flag) {
        super(Material.STONE);
        this.y(this.blockStateList.getBlockData().set(BlockFurnace.FACING, EnumDirection.NORTH));
        this.b = flag;
    }

    @Override
    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return Item.getItemOf(Blocks.FURNACE);
    }

    // Paper start - Removed override of onPlace that was reversing placement direction when
    // adjacent to another block, which was not consistent with single player block placement
    /*
    public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata) {
        this.e(world, blockposition, iblockdata);
    }

    private void e(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (!world.isClientSide) {
            IBlockData iblockdata1 = world.getType(blockposition.north());
            IBlockData iblockdata2 = world.getType(blockposition.south());
            IBlockData iblockdata3 = world.getType(blockposition.west());
            IBlockData iblockdata4 = world.getType(blockposition.east());
            EnumDirection enumdirection = (EnumDirection) iblockdata.get(BlockFurnace.FACING);

            if (enumdirection == EnumDirection.NORTH && iblockdata1.b() && !iblockdata2.b()) {
                enumdirection = EnumDirection.SOUTH;
            } else if (enumdirection == EnumDirection.SOUTH && iblockdata2.b() && !iblockdata1.b()) {
                enumdirection = EnumDirection.NORTH;
            } else if (enumdirection == EnumDirection.WEST && iblockdata3.b() && !iblockdata4.b()) {
                enumdirection = EnumDirection.EAST;
            } else if (enumdirection == EnumDirection.EAST && iblockdata4.b() && !iblockdata3.b()) {
                enumdirection = EnumDirection.WEST;
            }

            world.setTypeAndData(blockposition, iblockdata.set(BlockFurnace.FACING, enumdirection), 2);
        }
    }
    */
    // Paper end

    @Override
    public boolean interact(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof TileEntityFurnace) {
            entityhuman.openContainer((TileEntityFurnace) tileentity);
            entityhuman.b(StatisticList.Y);
        }

        return true;
    }

    public static void a(boolean flag, World world, BlockPosition blockposition) {
        IBlockData iblockdata = world.getType(blockposition);
        TileEntity tileentity = world.getTileEntity(blockposition);

        BlockFurnace.c = true;
        if (flag) {
            world.setTypeAndData(blockposition, Blocks.LIT_FURNACE.getBlockData().set(BlockFurnace.FACING, iblockdata.get(BlockFurnace.FACING)), 3);
            world.setTypeAndData(blockposition, Blocks.LIT_FURNACE.getBlockData().set(BlockFurnace.FACING, iblockdata.get(BlockFurnace.FACING)), 3);
        } else {
            world.setTypeAndData(blockposition, Blocks.FURNACE.getBlockData().set(BlockFurnace.FACING, iblockdata.get(BlockFurnace.FACING)), 3);
            world.setTypeAndData(blockposition, Blocks.FURNACE.getBlockData().set(BlockFurnace.FACING, iblockdata.get(BlockFurnace.FACING)), 3);
        }

        BlockFurnace.c = false;
        if (tileentity != null) {
            tileentity.A();
            world.setTileEntity(blockposition, tileentity);
        }

    }

    @Override
    public TileEntity a(World world, int i) {
        return new TileEntityFurnace();
    }

    @Override
    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        return this.getBlockData().set(BlockFurnace.FACING, entityliving.getDirection().opposite());
    }

    @Override
    public void postPlace(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        world.setTypeAndData(blockposition, iblockdata.set(BlockFurnace.FACING, entityliving.getDirection().opposite()), 2);
        if (itemstack.hasName()) {
            TileEntity tileentity = world.getTileEntity(blockposition);

            if (tileentity instanceof TileEntityFurnace) {
                ((TileEntityFurnace) tileentity).a(itemstack.getName());
            }
        }

    }

    @Override
    public void remove(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (!BlockFurnace.c) {
            TileEntity tileentity = world.getTileEntity(blockposition);

            if (tileentity instanceof TileEntityFurnace) {
                InventoryUtils.dropInventory(world, blockposition, (TileEntityFurnace) tileentity);
                world.updateAdjacentComparators(blockposition, this);
            }
        }

        super.remove(world, blockposition, iblockdata);
    }

    @Override
    public boolean isComplexRedstone(IBlockData iblockdata) {
        return true;
    }

    @Override
    public int c(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return Container.a(world.getTileEntity(blockposition));
    }

    @Override
    public ItemStack a(World world, BlockPosition blockposition, IBlockData iblockdata) {
        return new ItemStack(Blocks.FURNACE);
    }

    @Override
    public EnumRenderType a(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    public IBlockData fromLegacyData(int i) {
        EnumDirection enumdirection = EnumDirection.fromType1(i);

        if (enumdirection.k() == EnumDirection.EnumAxis.Y) {
            enumdirection = EnumDirection.NORTH;
        }

        return this.getBlockData().set(BlockFurnace.FACING, enumdirection);
    }

    @Override
    public int toLegacyData(IBlockData iblockdata) {
        return iblockdata.get(BlockFurnace.FACING).a();
    }

    @Override
    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return iblockdata.set(BlockFurnace.FACING, enumblockrotation.a(iblockdata.get(BlockFurnace.FACING)));
    }

    @Override
    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.a(enumblockmirror.a(iblockdata.get(BlockFurnace.FACING)));
    }

    @Override
    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockFurnace.FACING});
    }
}
