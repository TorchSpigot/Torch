package net.minecraft.server;

public class ChunkCoordIntPair {

    public final int x;
    public final int z;

    public ChunkCoordIntPair(int i, int j) {
        this.x = i;
        this.z = j;
    }

    public ChunkCoordIntPair(BlockPosition blockposition) {
        this.x = blockposition.getX() >> 4;
        this.z = blockposition.getZ() >> 4;
    }

    public static long chunkXZ2Int(int blockX, int blockZ) { return a(blockX, blockZ); } // OBFHELPER
    public static long a(int i, int j) {
        return i & 4294967295L | (j & 4294967295L) << 32;
    }

    @Override
	public int hashCode() {
        int i = 1664525 * this.x + 1013904223;
        int j = 1664525 * (this.z ^ -559038737) + 1013904223;

        return i ^ j;
    }

    @Override
	public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof ChunkCoordIntPair)) {
            return false;
        } else {
            ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair) object;

            return this.x == chunkcoordintpair.x && this.z == chunkcoordintpair.z;
        }
    }

    public double a(Entity entity) {
        double d0 = this.x * 16 + 8;
        double d1 = this.z * 16 + 8;
        double d2 = d0 - entity.locX;
        double d3 = d1 - entity.locZ;

        return d2 * d2 + d3 * d3;
    }

    public int c() {
        return this.x << 4;
    }

    public int d() {
        return this.z << 4;
    }

    public int e() {
        return (this.x << 4) + 15;
    }

    public int f() {
        return (this.z << 4) + 15;
    }

    public BlockPosition a(int i, int j, int k) {
        return new BlockPosition((this.x << 4) + i, j, (this.z << 4) + k);
    }

    @Override
	public String toString() {
        return "[" + this.x + ", " + this.z + "]";
    }
}
