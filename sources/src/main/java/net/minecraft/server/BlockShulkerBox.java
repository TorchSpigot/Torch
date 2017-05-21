package net.minecraft.server;

public class BlockShulkerBox extends BlockTileEntity {

    public static final BlockStateEnum<EnumDirection> a = BlockStateDirection.of("facing");
    public final EnumColor b; // PAIL: public, rename

    public BlockShulkerBox(EnumColor enumcolor) {
        super(Material.STONE, MaterialMapColor.b);
        this.b = enumcolor;
        this.a(CreativeModeTab.c);
        this.y(this.blockStateList.getBlockData().set(BlockShulkerBox.a, EnumDirection.UP));
    }

    @Override
    public TileEntity a(World world, int i) {
        return new TileEntityShulkerBox(this.b);
    }

    @Override
    public boolean b(IBlockData iblockdata) {
        return false;
    }

    @Override
    public boolean u(IBlockData iblockdata) {
        return true;
    }

    @Override
    public boolean c(IBlockData iblockdata) {
        return false;
    }

    @Override
    public EnumRenderType a(IBlockData iblockdata) {
        return EnumRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean interact(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        if (entityhuman.isSpectator()) {
            return true;
        } else {
            TileEntity tileentity = world.getTileEntity(blockposition);

            if (tileentity instanceof TileEntityShulkerBox) {
                EnumDirection enumdirection1 = iblockdata.get(BlockShulkerBox.a);
                boolean flag;

                if (((TileEntityShulkerBox) tileentity).p() == TileEntityShulkerBox.AnimationPhase.CLOSED) {
                    AxisAlignedBB axisalignedbb = BlockShulkerBox.j.b(0.5F * enumdirection1.getAdjacentX(), 0.5F * enumdirection1.getAdjacentY(), 0.5F * enumdirection1.getAdjacentZ()).a(enumdirection1.getAdjacentX(), enumdirection1.getAdjacentY(), enumdirection1.getAdjacentZ());

                    flag = !world.a(axisalignedbb.a(blockposition.shift(enumdirection1)));
                } else {
                    flag = true;
                }

                if (flag) {
                    entityhuman.b(StatisticList.ac);
                    entityhuman.openContainer((IInventory) tileentity);
                }

                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        return this.getBlockData().set(BlockShulkerBox.a, enumdirection);
    }

    @Override
    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockShulkerBox.a});
    }

    @Override
    public int toLegacyData(IBlockData iblockdata) {
        return iblockdata.get(BlockShulkerBox.a).a();
    }

    @Override
    public IBlockData fromLegacyData(int i) {
        EnumDirection enumdirection = EnumDirection.fromType1(i);

        return this.getBlockData().set(BlockShulkerBox.a, enumdirection);
    }

    @Override
    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        TileEntityShulkerBox tileentityshulkerbox = (TileEntityShulkerBox) world.getTileEntity(blockposition);

        tileentityshulkerbox.a(entityhuman.abilities.canInstantlyBuild);
        tileentityshulkerbox.d(entityhuman);
    }

    // CraftBukkit start - override to prevent duplication when dropping
    @Override
    public void dropNaturally(World world, BlockPosition blockposition, IBlockData iblockdata, float f, int i) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof TileEntityShulkerBox) {
            TileEntityShulkerBox tileentityshulkerbox = (TileEntityShulkerBox) tileentity;

            if (!tileentityshulkerbox.r() && tileentityshulkerbox.F()) {
                ItemStack itemstack = new ItemStack(Item.getItemOf(this));
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                nbttagcompound.set("BlockEntityTag", ((TileEntityShulkerBox) tileentity).f(nbttagcompound1));
                itemstack.setTag(nbttagcompound);
                if (tileentityshulkerbox.hasCustomName()) {
                    itemstack.g(tileentityshulkerbox.getName());
                    tileentityshulkerbox.a("");
                }

                a(world, blockposition, itemstack);
                tileentityshulkerbox.clear(); // Paper - This was intended to be called in Vanilla (is checked in the if statement above if has been called) - Fixes dupe issues
            }

            world.updateAdjacentComparators(blockposition, iblockdata.getBlock());
        }
    }
    // CraftBukkit end

    @Override
    public void postPlace(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        if (itemstack.hasName()) {
            TileEntity tileentity = world.getTileEntity(blockposition);

            if (tileentity instanceof TileEntityShulkerBox) {
                ((TileEntityShulkerBox) tileentity).a(itemstack.getName());
            }
        }

    }

    @Override
    public void remove(World world, BlockPosition blockposition, IBlockData iblockdata) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (false && tileentity instanceof TileEntityShulkerBox) { // CraftBukkit - moved up
            TileEntityShulkerBox tileentityshulkerbox = (TileEntityShulkerBox) tileentity;

            if (!tileentityshulkerbox.r() && tileentityshulkerbox.F()) {
                ItemStack itemstack = new ItemStack(Item.getItemOf(this));
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                nbttagcompound.set("BlockEntityTag", ((TileEntityShulkerBox) tileentity).f(nbttagcompound1));
                itemstack.setTag(nbttagcompound);
                if (tileentityshulkerbox.hasCustomName()) {
                    itemstack.g(tileentityshulkerbox.getName());
                    tileentityshulkerbox.a("");
                }

                a(world, blockposition, itemstack);
            }

        }
        world.updateAdjacentComparators(blockposition, iblockdata.getBlock()); // CraftBukkit - moved down

        super.remove(world, blockposition, iblockdata);
    }

    @Override
    public EnumPistonReaction h(IBlockData iblockdata) {
        return EnumPistonReaction.DESTROY;
    }

    @Override
    public AxisAlignedBB b(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        TileEntity tileentity = iblockaccess.getTileEntity(blockposition);

        return tileentity instanceof TileEntityShulkerBox ? ((TileEntityShulkerBox) tileentity).a(iblockdata) : BlockShulkerBox.j;
    }

    @Override
    public boolean isComplexRedstone(IBlockData iblockdata) {
        return true;
    }

    @Override
    public int c(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return Container.b((IInventory) world.getTileEntity(blockposition));
    }

    @Override
    public ItemStack a(World world, BlockPosition blockposition, IBlockData iblockdata) {
        ItemStack itemstack = super.a(world, blockposition, iblockdata);
        TileEntityShulkerBox tileentityshulkerbox = (TileEntityShulkerBox) world.getTileEntity(blockposition);
        NBTTagCompound nbttagcompound = tileentityshulkerbox.f(new NBTTagCompound());

        if (!nbttagcompound.isEmpty()) {
            itemstack.a("BlockEntityTag", nbttagcompound);
        }

        return itemstack;
    }

    public static Block a(EnumColor enumcolor) {
        switch (enumcolor) {
        case WHITE:
            return Blocks.WHITE_SHULKER_BOX;

        case ORANGE:
            return Blocks.dm;

        case MAGENTA:
            return Blocks.dn;

        case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_SHULKER_BOX;

        case YELLOW:
            return Blocks.dp;

        case LIME:
            return Blocks.dq;

        case PINK:
            return Blocks.dr;

        case GRAY:
            return Blocks.ds;

        case SILVER:
            return Blocks.dt;

        case CYAN:
            return Blocks.du;

        case PURPLE:
        default:
            return Blocks.dv;

        case BLUE:
            return Blocks.dw;

        case BROWN:
            return Blocks.dx;

        case GREEN:
            return Blocks.dy;

        case RED:
            return Blocks.dz;

        case BLACK:
            return Blocks.dA;
        }
    }

    public static ItemStack b(EnumColor enumcolor) {
        return new ItemStack(a(enumcolor));
    }

    @Override
    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return iblockdata.set(BlockShulkerBox.a, enumblockrotation.a(iblockdata.get(BlockShulkerBox.a)));
    }

    @Override
    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.a(enumblockmirror.a(iblockdata.get(BlockShulkerBox.a)));
    }
}
