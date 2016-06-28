package net.minecraft.server;

import java.util.List;

public class TileEntityPiston extends TileEntity implements ITickable {

    private IBlockData a;
    private EnumDirection f;
    private boolean g;
    private boolean h;
    private float i;
    private float j;

    public TileEntityPiston() {}

    public TileEntityPiston(IBlockData iblockdata, EnumDirection enumdirection, boolean flag, boolean flag1) {
        this.a = iblockdata;
        this.f = enumdirection;
        this.g = flag;
        this.h = flag1;
    }

    public IBlockData d() {
        return this.a;
    }

    public int u() {
        return 0;
    }

    public boolean e() {
        return this.g;
    }

    public EnumDirection g() {
        return this.f;
    }

    private float e(float f) {
        return this.g ? f - 1.0F : 1.0F - f;
    }

    public AxisAlignedBB a(IBlockAccess iblockaccess, BlockPosition blockposition) {
        return this.a(iblockaccess, blockposition, this.i).a(this.a(iblockaccess, blockposition, this.j));
    }

    public AxisAlignedBB a(IBlockAccess iblockaccess, BlockPosition blockposition, float f) {
        f = this.e(f);
        return this.a.c(iblockaccess, blockposition).c((double) (f * (float) this.f.getAdjacentX()), (double) (f * (float) this.f.getAdjacentY()), (double) (f * (float) this.f.getAdjacentZ()));
    }

    private void j() {
        AxisAlignedBB axisalignedbb = this.a((IBlockAccess) this.world, this.position).a(this.position);
        List list = this.world.getEntities((Entity) null, axisalignedbb);

        if (!list.isEmpty()) {
            EnumDirection enumdirection = this.g ? this.f : this.f.opposite();

            for (int i = 0; i < list.size(); ++i) {
                Entity entity = (Entity) list.get(i);

                if (entity.z() != EnumPistonReaction.IGNORE) {
                    if (this.a.getBlock() == Blocks.SLIME) {
                        switch (TileEntityPiston.SyntheticClass_1.a[enumdirection.k().ordinal()]) {
                        case 1:
                            entity.motX = (double) enumdirection.getAdjacentX();
                            break;

                        case 2:
                            entity.motY = (double) enumdirection.getAdjacentY();
                            break;

                        case 3:
                            entity.motZ = (double) enumdirection.getAdjacentZ();
                        }
                    }

                    double d0 = 0.0D;
                    double d1 = 0.0D;
                    double d2 = 0.0D;
                    AxisAlignedBB axisalignedbb1 = entity.getBoundingBox();

                    switch (TileEntityPiston.SyntheticClass_1.a[enumdirection.k().ordinal()]) {
                    case 1:
                        if (enumdirection.c() == EnumDirection.EnumAxisDirection.POSITIVE) {
                            d0 = axisalignedbb.d - axisalignedbb1.a;
                        } else {
                            d0 = axisalignedbb1.d - axisalignedbb.a;
                        }

                        d0 += 0.01D;
                        break;

                    case 2:
                        if (enumdirection.c() == EnumDirection.EnumAxisDirection.POSITIVE) {
                            d1 = axisalignedbb.e - axisalignedbb1.b;
                        } else {
                            d1 = axisalignedbb1.e - axisalignedbb.b;
                        }

                        d1 += 0.01D;
                        break;

                    case 3:
                        if (enumdirection.c() == EnumDirection.EnumAxisDirection.POSITIVE) {
                            d2 = axisalignedbb.f - axisalignedbb1.c;
                        } else {
                            d2 = axisalignedbb1.f - axisalignedbb.c;
                        }

                        d2 += 0.01D;
                    }

                    entity.move(d0 * (double) enumdirection.getAdjacentX(), d1 * (double) enumdirection.getAdjacentY(), d2 * (double) enumdirection.getAdjacentZ());
                }
            }

        }
    }

    public void i() {
        if (this.j < 1.0F && this.world != null) {
            this.j = this.i = 1.0F;
            this.world.s(this.position);
            this.y();
            if (this.world.getType(this.position).getBlock() == Blocks.PISTON_EXTENSION) {
                this.world.setTypeAndData(this.position, this.a, 3);
                this.world.e(this.position, this.a.getBlock());
            }
        }

    }

    public void c() {
        if (this.world == null) return; // CraftBukkit
        this.j = this.i;
        if (this.j >= 1.0F) {
            this.j();
            this.world.s(this.position);
            this.y();
            if (this.world.getType(this.position).getBlock() == Blocks.PISTON_EXTENSION) {
                this.world.setTypeAndData(this.position, this.a, 3);
                this.world.e(this.position, this.a.getBlock());
            }

        } else {
            this.i += 0.5F;
            if (this.i >= 1.0F) {
                this.i = 1.0F;
            }

            this.j();
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.a = Block.getById(nbttagcompound.getInt("blockId")).fromLegacyData(nbttagcompound.getInt("blockData"));
        this.f = EnumDirection.fromType1(nbttagcompound.getInt("facing"));
        this.j = this.i = nbttagcompound.getFloat("progress");
        this.g = nbttagcompound.getBoolean("extending");
    }

    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        super.save(nbttagcompound);
        nbttagcompound.setInt("blockId", Block.getId(this.a.getBlock()));
        nbttagcompound.setInt("blockData", this.a.getBlock().toLegacyData(this.a));
        nbttagcompound.setInt("facing", this.f.a());
        nbttagcompound.setFloat("progress", this.j);
        nbttagcompound.setBoolean("extending", this.g);
        return nbttagcompound;
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumDirection.EnumAxis.values().length];

        static {
            try {
                TileEntityPiston.SyntheticClass_1.a[EnumDirection.EnumAxis.X.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                TileEntityPiston.SyntheticClass_1.a[EnumDirection.EnumAxis.Y.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                TileEntityPiston.SyntheticClass_1.a[EnumDirection.EnumAxis.Z.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

        }
    }
}
