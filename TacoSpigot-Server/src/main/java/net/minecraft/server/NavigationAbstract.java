package net.minecraft.server;

import javax.annotation.Nullable;

public abstract class NavigationAbstract {

    private static int f = 20;
    protected EntityInsentient a;public Entity getEntity() { return a; } // Paper
    protected World b;
    @Nullable
    protected PathEntity c;
    protected double d;
    private final AttributeInstance g;
    private int h;
    private int i;
    private Vec3D j;
    private Vec3D k;
    private long l;
    private long m;
    private double n;
    private float o;
    private boolean p;
    private long q;
    protected PathfinderAbstract e;
    private BlockPosition r;
    private final Pathfinder s;

    public NavigationAbstract(EntityInsentient entityinsentient, World world) {
        this.j = Vec3D.a;
        this.k = Vec3D.a;
        this.l = 0L;
        this.m = 0L;
        this.o = 0.5F;
        this.a = entityinsentient;
        this.b = world;
        this.g = entityinsentient.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        this.s = this.a();
    }

    protected abstract Pathfinder a();

    public void a(double d0) {
        this.d = d0;
    }

    public float h() {
        return (float) this.g.getValue();
    }

    public boolean i() {
        return this.p;
    }

    public void j() {
        if (this.b.getTime() - this.q > (long) NavigationAbstract.f) {
            if (this.r != null) {
                this.c = null;
                this.c = this.a(this.r);
                this.q = this.b.getTime();
                this.p = false;
            }
        } else {
            this.p = true;
        }

    }

    @Nullable
    public final PathEntity a(double d0, double d1, double d2) {
        return this.a(new BlockPosition(MathHelper.floor(d0), (int) d1, MathHelper.floor(d2)));
    }

    @Nullable
    public PathEntity a(BlockPosition blockposition) {
        if (!this.b()) {
            return null;
        } else if (this.c != null && !this.c.b() && blockposition.equals(this.r)) {
            return this.c;
        } else {
            if (!new com.destroystokyo.paper.event.entity.EntityPathfindEvent(getEntity().getBukkitEntity(), MCUtil.toLocation(getEntity().world, blockposition), null).callEvent()) { return null; } // Paper
            this.r = blockposition;
            float f = this.h();

            this.b.methodProfiler.a("pathfind");
            BlockPosition blockposition1 = new BlockPosition(this.a);
            int i = (int) (f + 8.0F);
            ChunkCache chunkcache = new ChunkCache(this.b, blockposition1.a(-i, -i, -i), blockposition1.a(i, i, i), 0);
            PathEntity pathentity = this.s.a(chunkcache, this.a, this.r, f);

            this.b.methodProfiler.b();
            return pathentity;
        }
    }

    @Nullable
    public PathEntity a(Entity entity) {
        if (!this.b()) {
            return null;
        } else {
            BlockPosition blockposition = new BlockPosition(entity);

            if (this.c != null && !this.c.b() && blockposition.equals(this.r)) {
                return this.c;
            } else {
                if (!new com.destroystokyo.paper.event.entity.EntityPathfindEvent(getEntity().getBukkitEntity(), MCUtil.toLocation(entity.world, blockposition), entity.getBukkitEntity()).callEvent()) { return null; } // Paper
                this.r = blockposition;
                float f = this.h();

                this.b.methodProfiler.a("pathfind");
                BlockPosition blockposition1 = (new BlockPosition(this.a)).up();
                int i = (int) (f + 16.0F);
                ChunkCache chunkcache = new ChunkCache(this.b, blockposition1.a(-i, -i, -i), blockposition1.a(i, i, i), 0);
                PathEntity pathentity = this.s.a(chunkcache, this.a, entity, f);

                this.b.methodProfiler.b();
                return pathentity;
            }
        }
    }

    public boolean a(double d0, double d1, double d2, double d3) {
        PathEntity pathentity = this.a((double) MathHelper.floor(d0), (double) ((int) d1), (double) MathHelper.floor(d2));

        return this.a(pathentity, d3);
    }

    public boolean a(Entity entity, double d0) {
        // Paper start - Pathfinding optimizations
        if (this.pathfindFailures > 10 && this.c == null && MinecraftServer.currentTick < this.lastFailure + 40) {
            return false;
        }

        PathEntity pathentity = this.a(entity);

        if (pathentity != null && this.a(pathentity, d0)) {
            this.lastFailure = 0;
            this.pathfindFailures = 0;
            return true;
        } else {
            this.pathfindFailures++;
            this.lastFailure = MinecraftServer.currentTick;
            return false;
        }
    }
    private int lastFailure = 0;
    private int pathfindFailures = 0;
    // Paper end

    public boolean a(@Nullable PathEntity pathentity, double d0) {
        if (pathentity == null) {
            this.c = null;
            return false;
        } else {
            if (!pathentity.a(this.c)) {
                this.c = pathentity;
            }

            this.d();
            if (this.c.d() == 0) {
                return false;
            } else {
                this.d = d0;
                Vec3D vec3d = this.c();

                this.i = this.h;
                this.j = vec3d;
                return true;
            }
        }
    }

    @Nullable
    public PathEntity k() {
        return this.c;
    }

    public void l() {
        ++this.h;
        if (this.p) {
            this.j();
        }

        if (!this.n()) {
            Vec3D vec3d;

            if (this.b()) {
                this.m();
            } else if (this.c != null && this.c.e() < this.c.d()) {
                vec3d = this.c();
                Vec3D vec3d1 = this.c.a(this.a, this.c.e());

                if (vec3d.y > vec3d1.y && !this.a.onGround && MathHelper.floor(vec3d.x) == MathHelper.floor(vec3d1.x) && MathHelper.floor(vec3d.z) == MathHelper.floor(vec3d1.z)) {
                    this.c.c(this.c.e() + 1);
                }
            }

            if (!this.n()) {
                vec3d = this.c.a((Entity) this.a);
                if (vec3d != null) {
                    BlockPosition blockposition = (new BlockPosition(vec3d)).down();
                    AxisAlignedBB axisalignedbb = this.b.getType(blockposition).c(this.b, blockposition);

                    vec3d = vec3d.a(0.0D, 1.0D - axisalignedbb.e, 0.0D);
                    this.a.getControllerMove().a(vec3d.x, vec3d.y, vec3d.z, this.d);
                }
            }
        }
    }

    protected void m() {
        Vec3D vec3d = this.c();
        int i = this.c.d();

        for (int j = this.c.e(); j < this.c.d(); ++j) {
            if ((double) this.c.a(j).b != Math.floor(vec3d.y)) {
                i = j;
                break;
            }
        }

        this.o = this.a.width > 0.75F ? this.a.width / 2.0F : 0.75F - this.a.width / 2.0F;
        Vec3D vec3d1 = this.c.f();

        if (MathHelper.e((float) (this.a.locX - (vec3d1.x + 0.5D))) < this.o && MathHelper.e((float) (this.a.locZ - (vec3d1.z + 0.5D))) < this.o) {
            this.c.c(this.c.e() + 1);
        }

        int k = MathHelper.f(this.a.width);
        int l = MathHelper.f(this.a.length);
        int i1 = k;

        for (int j1 = i - 1; j1 >= this.c.e(); --j1) {
            if (this.a(vec3d, this.c.a(this.a, j1), k, l, i1)) {
                this.c.c(j1);
                break;
            }
        }

        this.a(vec3d);
    }

    protected void a(Vec3D vec3d) {
        if (this.h - this.i > 100) {
            if (vec3d.distanceSquared(this.j) < 2.25D) {
                this.o();
            }

            this.i = this.h;
            this.j = vec3d;
        }

        if (this.c != null && !this.c.b()) {
            Vec3D vec3d1 = this.c.f();

            if (!vec3d1.equals(this.k)) {
                this.k = vec3d1;
                double d0 = vec3d.f(this.k);

                this.n = this.a.cl() > 0.0F ? d0 / (double) this.a.cl() * 1000.0D : 0.0D;
            } else {
                this.l += System.currentTimeMillis() - this.m;
            }

            if (this.n > 0.0D && (double) this.l > this.n * 3.0D) {
                this.k = Vec3D.a;
                this.l = 0L;
                this.n = 0.0D;
                this.o();
            }

            this.m = System.currentTimeMillis();
        }

    }

    public boolean n() {
        return this.c == null || this.c.b();
    }

    public void o() {
        this.pathfindFailures = 0; this.lastFailure = 0; // Paper - Pathfinding optimizations
        this.c = null;
    }

    protected abstract Vec3D c();

    protected abstract boolean b();

    protected boolean p() {
        return this.a.isInWater() || this.a.an();
    }

    protected void d() {}

    protected abstract boolean a(Vec3D vec3d, Vec3D vec3d1, int i, int j, int k);

    public boolean b(BlockPosition blockposition) {
        return this.b.getType(blockposition.down()).b();
    }

    public PathfinderAbstract q() {
        return this.e;
    }
}
