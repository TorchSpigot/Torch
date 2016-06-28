package net.minecraft.server;

public class PathfinderGoalPanic extends PathfinderGoal {

    private EntityCreature b;
    protected double a;
    private double c;
    private double d;
    private double e;

    public PathfinderGoalPanic(EntityCreature entitycreature, double d0) {
        this.b = entitycreature;
        this.a = d0;
        this.a(1);
    }

    public boolean a() {
        if (this.b.getLastDamager() == null && !this.b.isBurning()) {
            return false;
        } else {
            Vec3D vec3d = RandomPositionGenerator.a(this.b, 5, 4);

            if (vec3d == null) {
                return false;
            } else {
                this.c = vec3d.x;
                this.d = vec3d.y;
                this.e = vec3d.z;
                if (this.b.isBurning()) {
                    BlockPosition blockposition = this.a(this.b.world, this.b, 5, 4);

                    if (blockposition != null) {
                        this.c = (double) blockposition.getX();
                        this.d = (double) blockposition.getY();
                        this.e = (double) blockposition.getZ();
                    }
                }

                return true;
            }
        }
    }

    public void c() {
        this.b.getNavigation().a(this.c, this.d, this.e, this.a);
    }

    public boolean b() {
        // CraftBukkit start - introduce a temporary timeout hack until this is fixed properly
        if ((this.b.ticksLived - this.b.hurtTimestamp) > 100) {
            this.b.b((EntityLiving) null);
            return false;
        }
        // CraftBukkit end
        return !this.b.getNavigation().n();
    }

    private BlockPosition a(World world, Entity entity, int i, int j) {
        BlockPosition blockposition = new BlockPosition(entity);
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        int k = blockposition.getX();
        int l = blockposition.getY();
        int i1 = blockposition.getZ();
        float f = (float) (i * i * j * 2);
        BlockPosition blockposition1 = null;

        for (int j1 = k - i; j1 <= k + i; ++j1) {
            for (int k1 = l - j; k1 <= l + j; ++k1) {
                for (int l1 = i1 - i; l1 <= i1 + i; ++l1) {
                    blockposition_mutableblockposition.c(j1, k1, l1);
                    IBlockData iblockdata = world.getType(blockposition_mutableblockposition);
                    Block block = iblockdata.getBlock();

                    if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                        float f1 = (float) ((j1 - k) * (j1 - k) + (k1 - l) * (k1 - l) + (l1 - i1) * (l1 - i1));

                        if (f1 < f) {
                            f = f1;
                            blockposition1 = new BlockPosition(blockposition_mutableblockposition);
                        }
                    }
                }
            }
        }

        return blockposition1;
    }
}
