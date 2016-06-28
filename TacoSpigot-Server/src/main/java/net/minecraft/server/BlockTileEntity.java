package net.minecraft.server;

public abstract class BlockTileEntity extends Block implements ITileEntity {

    protected BlockTileEntity(Material material) {
        this(material, material.r());
    }

    protected BlockTileEntity(Material material, MaterialMapColor materialmapcolor) {
        super(material, materialmapcolor);
        this.isTileEntity = true;
    }

    protected boolean a(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        return world.getType(blockposition.shift(enumdirection)).getMaterial() == Material.CACTUS;
    }

    protected boolean b(World world, BlockPosition blockposition) {
        return this.a(world, blockposition, EnumDirection.NORTH) || this.a(world, blockposition, EnumDirection.SOUTH) || this.a(world, blockposition, EnumDirection.WEST) || this.a(world, blockposition, EnumDirection.EAST);
    }

    public EnumRenderType a(IBlockData iblockdata) {
        return EnumRenderType.INVISIBLE;
    }

    public void remove(World world, BlockPosition blockposition, IBlockData iblockdata) {
        super.remove(world, blockposition, iblockdata);
        world.s(blockposition);
    }

    public boolean a(IBlockData iblockdata, World world, BlockPosition blockposition, int i, int j) {
        super.a(iblockdata, world, blockposition, i, j);
        TileEntity tileentity = world.getTileEntity(blockposition);

        return tileentity == null ? false : tileentity.c(i, j);
    }
}
