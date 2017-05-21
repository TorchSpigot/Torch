package net.minecraft.server;

public class BlockJukeBox extends BlockTileEntity {

    public static final BlockStateBoolean HAS_RECORD = BlockStateBoolean.of("has_record");

    public static void a(DataConverterManager dataconvertermanager) {
        dataconvertermanager.a(DataConverterTypes.BLOCK_ENTITY, (new DataInspectorItem(BlockJukeBox.TileEntityRecordPlayer.class, new String[] { "RecordItem"})));
    }

    protected BlockJukeBox() {
        super(Material.WOOD, MaterialMapColor.l);
        this.y(this.blockStateList.getBlockData().set(BlockJukeBox.HAS_RECORD, Boolean.valueOf(false)));
        this.a(CreativeModeTab.c);
    }

    @Override
    public boolean interact(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        if (iblockdata.get(BlockJukeBox.HAS_RECORD).booleanValue()) {
            this.dropRecord(world, blockposition, iblockdata);
            iblockdata = iblockdata.set(BlockJukeBox.HAS_RECORD, Boolean.valueOf(false));
            world.setTypeAndData(blockposition, iblockdata, 2);
            return true;
        } else {
            return false;
        }
    }

    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, ItemStack itemstack) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof BlockJukeBox.TileEntityRecordPlayer) {
            ((BlockJukeBox.TileEntityRecordPlayer) tileentity).setRecord(itemstack.cloneItemStack());
            world.setTypeAndData(blockposition, iblockdata.set(BlockJukeBox.HAS_RECORD, Boolean.valueOf(true)), 2);
        }
    }

    public void dropRecord(World world, BlockPosition blockposition, IBlockData iblockdata) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof BlockJukeBox.TileEntityRecordPlayer) {
            BlockJukeBox.TileEntityRecordPlayer blockjukebox_tileentityrecordplayer = (BlockJukeBox.TileEntityRecordPlayer) tileentity;
            ItemStack itemstack = blockjukebox_tileentityrecordplayer.getRecord();

            if (!itemstack.isEmpty()) {
                world.triggerEffect(1010, blockposition, 0);
                world.a(blockposition, (SoundEffect) null);
                blockjukebox_tileentityrecordplayer.setRecord(ItemStack.a);
                float f = 0.7F;
                double d0 = world.random.nextFloat() * 0.7F + 0.15000000596046448D;
                double d1 = world.random.nextFloat() * 0.7F + 0.06000000238418579D + 0.6D;
                double d2 = world.random.nextFloat() * 0.7F + 0.15000000596046448D;
                ItemStack itemstack1 = itemstack.cloneItemStack();
                EntityItem entityitem = new EntityItem(world, blockposition.getX() + d0, blockposition.getY() + d1, blockposition.getZ() + d2, itemstack1);

                entityitem.q();
                world.addEntity(entityitem);
            }
        }
    }

    @Override
    public void remove(World world, BlockPosition blockposition, IBlockData iblockdata) {
        this.dropRecord(world, blockposition, iblockdata);
        super.remove(world, blockposition, iblockdata);
    }

    @Override
    public void dropNaturally(World world, BlockPosition blockposition, IBlockData iblockdata, float f, int i) {
        super.dropNaturally(world, blockposition, iblockdata, f, 0);
    }

    @Override
    public TileEntity a(World world, int i) {
        return new BlockJukeBox.TileEntityRecordPlayer();
    }

    @Override
    public boolean isComplexRedstone(IBlockData iblockdata) {
        return true;
    }

    @Override
    public int c(IBlockData iblockdata, World world, BlockPosition blockposition) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof BlockJukeBox.TileEntityRecordPlayer) {
            ItemStack itemstack = ((BlockJukeBox.TileEntityRecordPlayer) tileentity).getRecord();

            if (!itemstack.isEmpty()) {
                return Item.getId(itemstack.getItem()) + 1 - Item.getId(Items.RECORD_13);
            }
        }

        return 0;
    }

    @Override
    public EnumRenderType a(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockJukeBox.HAS_RECORD, Boolean.valueOf(i > 0));
    }

    @Override
    public int toLegacyData(IBlockData iblockdata) {
        return iblockdata.get(BlockJukeBox.HAS_RECORD).booleanValue() ? 1 : 0;
    }

    @Override
    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockJukeBox.HAS_RECORD});
    }

    public static class TileEntityRecordPlayer extends TileEntity {

        private ItemStack record;

        public TileEntityRecordPlayer() {
            this.record = ItemStack.a;
        }

        @Override
        public void a(NBTTagCompound nbttagcompound) {
            super.a(nbttagcompound);
            if (nbttagcompound.hasKeyOfType("RecordItem", 10)) {
                this.setRecord(new ItemStack(nbttagcompound.getCompound("RecordItem")));
            } else if (nbttagcompound.getInt("Record") > 0) {
                this.setRecord(new ItemStack(Item.getById(nbttagcompound.getInt("Record"))));
            }

        }

        @Override
        public NBTTagCompound save(NBTTagCompound nbttagcompound) {
            super.save(nbttagcompound);
            if (!this.getRecord().isEmpty()) {
                nbttagcompound.set("RecordItem", this.getRecord().save(new NBTTagCompound()));
            }

            return nbttagcompound;
        }

        public ItemStack getRecord() {
            return this.record;
        }

        public void setRecord(ItemStack itemstack) {
            // CraftBukkit start - There can only be one
            if (!itemstack.isEmpty()) {
                itemstack.setCount(1);
            }
            // CraftBukkit end
            this.record = itemstack;
            this.update();
        }
    }
}
