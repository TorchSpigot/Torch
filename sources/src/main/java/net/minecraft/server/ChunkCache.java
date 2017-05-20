package net.minecraft.server;

import javax.annotation.Nullable;

public class ChunkCache implements IBlockAccess {

    protected int a;
    protected int b;
    protected Chunk[][] c;
    protected boolean d;
    protected World e;

    public ChunkCache(World world, BlockPosition blockposition, BlockPosition blockposition1, int i) {
        this.e = world;
        this.a = blockposition.getX() - i >> 4;
        this.b = blockposition.getZ() - i >> 4;
        int j = blockposition1.getX() + i >> 4;
        int k = blockposition1.getZ() + i >> 4;

        this.c = new Chunk[j - this.a + 1][k - this.b + 1];
        this.d = true;

        int l;
        int i1;

        for (l = this.a; l <= j; ++l) {
            for (i1 = this.b; i1 <= k; ++i1) {
                this.c[l - this.a][i1 - this.b] = world.getChunkIfLoaded(l, i1); // Paper
            }
        }

        for (l = blockposition.getX() >> 4; l <= blockposition1.getX() >> 4; ++l) {
            for (i1 = blockposition.getZ() >> 4; i1 <= blockposition1.getZ() >> 4; ++i1) {
                Chunk chunk = this.c[l - this.a][i1 - this.b];

                if (chunk != null && !chunk.c(blockposition.getY(), blockposition1.getY())) {
                    this.d = false;
                }
            }
        }

    }

    @Override @Nullable
    public TileEntity getTileEntity(BlockPosition blockposition) {
        return this.a(blockposition, Chunk.EnumTileEntityState.IMMEDIATE);
    }

    @Nullable
    public TileEntity a(BlockPosition position, Chunk.EnumTileEntityState state) {
        int arrayX = (position.getX() >> 4) - this.a;
        int arrayY = (position.getZ() >> 4) - this.b;
        
        return this.c[arrayX][arrayY].a(position, state);
    }

    @Override
    public IBlockData getType(BlockPosition blockposition) {
        if (blockposition.getY() >= 0 && blockposition.getY() < 256) {
            int arrayX = (blockposition.getX() >> 4) - this.a;
            int arrayY = (blockposition.getZ() >> 4) - this.b;

            if (arrayX >= 0 && arrayX < this.c.length && arrayY >= 0 && arrayY < this.c[arrayX].length) {
                Chunk chunk = this.c[arrayX][arrayY];

                if (chunk != null) {
                    return chunk.getBlockData(blockposition);
                }
            }
        }

        return Blocks.AIR.getBlockData();
    }

    @Override
    public boolean isEmpty(BlockPosition blockposition) {
        return this.getType(blockposition).getMaterial() == Material.AIR;
    }

    @Override
    public int getBlockPower(BlockPosition blockposition, EnumDirection enumdirection) {
        return this.getType(blockposition).b(this, blockposition, enumdirection);
    }
}
