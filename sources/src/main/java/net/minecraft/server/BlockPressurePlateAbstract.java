package net.minecraft.server;

import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.event.block.BlockRedstoneEvent; // CraftBukkit

public abstract class BlockPressurePlateAbstract extends Block {

    protected static final AxisAlignedBB a = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.03125D, 0.9375D);
    protected static final AxisAlignedBB b = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.0625D, 0.9375D);
    protected static final AxisAlignedBB c = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.25D, 0.875D);

    protected BlockPressurePlateAbstract(Material material) {
        this(material, material.r());
    }

    protected BlockPressurePlateAbstract(Material material, MaterialMapColor materialmapcolor) {
        super(material, materialmapcolor);
        this.a(CreativeModeTab.d);
        this.a(true);
    }

    @Override
    public AxisAlignedBB b(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        boolean flag = this.getPower(iblockdata) > 0;

        return flag ? BlockPressurePlateAbstract.a : BlockPressurePlateAbstract.b;
    }

    @Override
    public int a(World world) {
        return 20;
    }

    @Override
    @Nullable
    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return BlockPressurePlateAbstract.k;
    }

    @Override
    public boolean b(IBlockData iblockdata) {
        return false;
    }

    @Override
    public boolean c(IBlockData iblockdata) {
        return false;
    }

    @Override
    public boolean b(IBlockAccess iblockaccess, BlockPosition blockposition) {
        return true;
    }

    @Override
    public boolean d() {
        return true;
    }

    @Override
    public boolean canPlace(World world, BlockPosition blockposition) {
        return this.i(world, blockposition.down());
    }

    @Override
    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1) {
        if (!this.i(world, blockposition.down())) {
            this.b(world, blockposition, iblockdata, 0);
            world.setAir(blockposition);
        }

    }

    private boolean i(World world, BlockPosition blockposition) {
        return world.getType(blockposition).r() || world.getType(blockposition).getBlock() instanceof BlockFence;
    }

    @Override
    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {}

    @Override
    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        int i = this.getPower(iblockdata);

        if (i > 0) {
            this.a(world, blockposition, iblockdata, i);
        }
    }

    @Override
    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, Entity entity) {
        int i = this.getPower(iblockdata);

        if (i == 0) {
            this.a(world, blockposition, iblockdata, i);
        }
    }

    protected void a(World world, BlockPosition blockposition, IBlockData iblockdata, int i) {
        int j = this.e(world, blockposition);
        boolean flag = i > 0;
        boolean flag1 = j > 0;

        // CraftBukkit start - Interact Pressure Plate
        org.bukkit.World bworld = world.getWorld();
        org.bukkit.plugin.PluginManager manager = world.getServer().getPluginManager();

        if (flag != flag1) {
            BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), i, j);
            manager.callEvent(eventRedstone);

            flag1 = eventRedstone.getNewCurrent() > 0;
            j = eventRedstone.getNewCurrent();
        }
        // CraftBukkit end

        if (i != j) {
            iblockdata = this.a(iblockdata, j);
            world.setTypeAndData(blockposition, iblockdata, 2);
            this.d(world, blockposition);
            world.b(blockposition, blockposition);
        }

        if (!flag1 && flag) {
            this.c(world, blockposition);
        } else if (flag1 && !flag) {
            this.b(world, blockposition);
        }

        if (flag1) {
            world.a(new BlockPosition(blockposition), this, this.a(world));
        }

    }

    protected abstract void b(World world, BlockPosition blockposition);

    protected abstract void c(World world, BlockPosition blockposition);

    @Override
    public void remove(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (this.getPower(iblockdata) > 0) {
            this.d(world, blockposition);
        }

        super.remove(world, blockposition, iblockdata);
    }

    protected void d(World world, BlockPosition blockposition) {
        world.applyPhysics(blockposition, this, false);
        world.applyPhysics(blockposition.down(), this, false);
    }

    @Override
    public int b(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return this.getPower(iblockdata);
    }

    @Override
    public int c(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return enumdirection == EnumDirection.UP ? this.getPower(iblockdata) : 0;
    }

    @Override
    public boolean isPowerSource(IBlockData iblockdata) {
        return true;
    }

    @Override
    public EnumPistonReaction h(IBlockData iblockdata) {
        return EnumPistonReaction.DESTROY;
    }

    protected abstract int e(World world, BlockPosition blockposition);

    protected abstract int getPower(IBlockData iblockdata);

    protected abstract IBlockData a(IBlockData iblockdata, int i);
}
