package net.minecraft.server;

import java.util.Random;

public class BlockDispenser extends BlockTileEntity {

    public static final BlockStateDirection FACING = BlockDirectional.FACING;
    public static final BlockStateBoolean TRIGGERED = BlockStateBoolean.of("triggered");
    public static final RegistryDefault<Item, IDispenseBehavior> REGISTRY = new RegistryDefault(new DispenseBehaviorItem());
    protected Random d = new Random();
    public static boolean eventFired = false; // CraftBukkit

    protected BlockDispenser() {
        super(Material.STONE);
        this.y(this.blockStateList.getBlockData().set(BlockDispenser.FACING, EnumDirection.NORTH).set(BlockDispenser.TRIGGERED, Boolean.valueOf(false)));
        this.a(CreativeModeTab.d);
    }

    @Override
    public int a(World world) {
        return 4;
    }

    // Paper start - Removed override of onPlace that was reversing placement direction when
    // adjacent to another block, which was not consistent with single player block placement
    /*
    public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata) {
        super.onPlace(world, blockposition, iblockdata);
        this.e(world, blockposition, iblockdata);
    }

    private void e(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (!world.isClientSide) {
            EnumDirection enumdirection = (EnumDirection) iblockdata.get(BlockDispenser.FACING);
            boolean flag = world.getType(blockposition.north()).b();
            boolean flag1 = world.getType(blockposition.south()).b();

            if (enumdirection == EnumDirection.NORTH && flag && !flag1) {
                enumdirection = EnumDirection.SOUTH;
            } else if (enumdirection == EnumDirection.SOUTH && flag1 && !flag) {
                enumdirection = EnumDirection.NORTH;
            } else {
                boolean flag2 = world.getType(blockposition.west()).b();
                boolean flag3 = world.getType(blockposition.east()).b();

                if (enumdirection == EnumDirection.WEST && flag2 && !flag3) {
                    enumdirection = EnumDirection.EAST;
                } else if (enumdirection == EnumDirection.EAST && flag3 && !flag2) {
                    enumdirection = EnumDirection.WEST;
                }
            }

            world.setTypeAndData(blockposition, iblockdata.set(BlockDispenser.FACING, enumdirection).set(BlockDispenser.TRIGGERED, Boolean.valueOf(false)), 2);
        }
    }
    */
    // Paper end

    @Override
    public boolean interact(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof TileEntityDispenser) {
            entityhuman.openContainer((TileEntityDispenser) tileentity);
            if (tileentity instanceof TileEntityDropper) {
                entityhuman.b(StatisticList.O);
            } else {
                entityhuman.b(StatisticList.Q);
            }
        }

        return true;
    }

    public void dispense(World world, BlockPosition blockposition) {
        SourceBlock sourceblock = new SourceBlock(world, blockposition);
        TileEntityDispenser tileentitydispenser = (TileEntityDispenser) sourceblock.getTileEntity();

        if (tileentitydispenser != null) {
            int i = tileentitydispenser.o();

            if (i < 0) {
                world.triggerEffect(1001, blockposition, 0);
            } else {
                ItemStack itemstack = tileentitydispenser.getItem(i);
                IDispenseBehavior idispensebehavior = this.a(itemstack);

                if (idispensebehavior != IDispenseBehavior.NONE) {
                    eventFired = false; // CraftBukkit - reset event status
                    tileentitydispenser.setItem(i, idispensebehavior.a(sourceblock, itemstack));
                }

            }
        }
    }

    protected IDispenseBehavior a(ItemStack itemstack) {
        return BlockDispenser.REGISTRY.get(itemstack.getItem());
    }

    @Override
    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1) {
        boolean flag = world.isBlockIndirectlyPowered(blockposition) || world.isBlockIndirectlyPowered(blockposition.up());
        boolean flag1 = iblockdata.get(BlockDispenser.TRIGGERED).booleanValue();

        if (flag && !flag1) {
            world.a(blockposition, this, this.a(world));
            world.setTypeAndData(blockposition, iblockdata.set(BlockDispenser.TRIGGERED, Boolean.valueOf(true)), 4);
        } else if (!flag && flag1) {
            world.setTypeAndData(blockposition, iblockdata.set(BlockDispenser.TRIGGERED, Boolean.valueOf(false)), 4);
        }

    }

    @Override
    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        this.dispense(world, blockposition);

    }

    @Override
    public TileEntity a(World world, int i) {
        return new TileEntityDispenser();
    }

    @Override
    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        return this.getBlockData().set(BlockDispenser.FACING, EnumDirection.a(blockposition, entityliving)).set(BlockDispenser.TRIGGERED, Boolean.valueOf(false));
    }

    @Override
    public void postPlace(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        world.setTypeAndData(blockposition, iblockdata.set(BlockDispenser.FACING, EnumDirection.a(blockposition, entityliving)), 2);
        if (itemstack.hasName()) {
            TileEntity tileentity = world.getTileEntity(blockposition);

            if (tileentity instanceof TileEntityDispenser) {
                ((TileEntityDispenser) tileentity).a(itemstack.getName());
            }
        }

    }

    @Override
    public void remove(World world, BlockPosition blockposition, IBlockData iblockdata) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof TileEntityDispenser) {
            InventoryUtils.dropInventory(world, blockposition, (TileEntityDispenser) tileentity);
            world.updateAdjacentComparators(blockposition, this);
        }

        super.remove(world, blockposition, iblockdata);
    }

    public static IPosition a(ISourceBlock isourceblock) {
        EnumDirection enumdirection = isourceblock.e().get(BlockDispenser.FACING);
        double d0 = isourceblock.getX() + 0.7D * enumdirection.getAdjacentX();
        double d1 = isourceblock.getY() + 0.7D * enumdirection.getAdjacentY();
        double d2 = isourceblock.getZ() + 0.7D * enumdirection.getAdjacentZ();

        return new Position(d0, d1, d2);
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
    public EnumRenderType a(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockDispenser.FACING, EnumDirection.fromType1(i & 7)).set(BlockDispenser.TRIGGERED, Boolean.valueOf((i & 8) > 0));
    }

    @Override
    public int toLegacyData(IBlockData iblockdata) {
        byte b0 = 0;
        int i = b0 | iblockdata.get(BlockDispenser.FACING).a();

        if (iblockdata.get(BlockDispenser.TRIGGERED).booleanValue()) {
            i |= 8;
        }

        return i;
    }

    @Override
    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return iblockdata.set(BlockDispenser.FACING, enumblockrotation.a(iblockdata.get(BlockDispenser.FACING)));
    }

    @Override
    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.a(enumblockmirror.a(iblockdata.get(BlockDispenser.FACING)));
    }

    @Override
    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockDispenser.FACING, BlockDispenser.TRIGGERED});
    }
}
