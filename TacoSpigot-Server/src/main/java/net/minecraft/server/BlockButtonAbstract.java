package net.minecraft.server;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

// CraftBukkit start
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityInteractEvent;
// CraftBukkit end

public abstract class BlockButtonAbstract extends BlockDirectional {

    public static final BlockStateBoolean POWERED = BlockStateBoolean.of("powered");
    protected static final AxisAlignedBB b = new AxisAlignedBB(0.3125D, 0.875D, 0.375D, 0.6875D, 1.0D, 0.625D);
    protected static final AxisAlignedBB c = new AxisAlignedBB(0.3125D, 0.0D, 0.375D, 0.6875D, 0.125D, 0.625D);
    protected static final AxisAlignedBB d = new AxisAlignedBB(0.3125D, 0.375D, 0.875D, 0.6875D, 0.625D, 1.0D);
    protected static final AxisAlignedBB e = new AxisAlignedBB(0.3125D, 0.375D, 0.0D, 0.6875D, 0.625D, 0.125D);
    protected static final AxisAlignedBB f = new AxisAlignedBB(0.875D, 0.375D, 0.3125D, 1.0D, 0.625D, 0.6875D);
    protected static final AxisAlignedBB g = new AxisAlignedBB(0.0D, 0.375D, 0.3125D, 0.125D, 0.625D, 0.6875D);
    protected static final AxisAlignedBB B = new AxisAlignedBB(0.3125D, 0.9375D, 0.375D, 0.6875D, 1.0D, 0.625D);
    protected static final AxisAlignedBB C = new AxisAlignedBB(0.3125D, 0.0D, 0.375D, 0.6875D, 0.0625D, 0.625D);
    protected static final AxisAlignedBB D = new AxisAlignedBB(0.3125D, 0.375D, 0.9375D, 0.6875D, 0.625D, 1.0D);
    protected static final AxisAlignedBB E = new AxisAlignedBB(0.3125D, 0.375D, 0.0D, 0.6875D, 0.625D, 0.0625D);
    protected static final AxisAlignedBB F = new AxisAlignedBB(0.9375D, 0.375D, 0.3125D, 1.0D, 0.625D, 0.6875D);
    protected static final AxisAlignedBB G = new AxisAlignedBB(0.0D, 0.375D, 0.3125D, 0.0625D, 0.625D, 0.6875D);
    private final boolean I;

    protected BlockButtonAbstract(boolean flag) {
        super(Material.ORIENTABLE);
        this.w(this.blockStateList.getBlockData().set(BlockButtonAbstract.FACING, EnumDirection.NORTH).set(BlockButtonAbstract.POWERED, Boolean.valueOf(false)));
        this.a(true);
        this.a(CreativeModeTab.d);
        this.I = flag;
    }

    @Nullable
    public AxisAlignedBB a(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return BlockButtonAbstract.k;
    }

    public int a(World world) {
        return this.I ? 30 : 20;
    }

    public boolean b(IBlockData iblockdata) {
        return false;
    }

    public boolean c(IBlockData iblockdata) {
        return false;
    }

    public boolean canPlace(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        return a(world, blockposition, enumdirection.opposite());
    }

    public boolean canPlace(World world, BlockPosition blockposition) {
        EnumDirection[] aenumdirection = EnumDirection.values();
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection[j];

            if (a(world, blockposition, enumdirection)) {
                return true;
            }
        }

        return false;
    }

    protected static boolean a(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        BlockPosition blockposition1 = blockposition.shift(enumdirection);

        return enumdirection == EnumDirection.DOWN ? world.getType(blockposition1).q() : world.getType(blockposition1).l();
    }

    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        return a(world, blockposition, enumdirection.opposite()) ? this.getBlockData().set(BlockButtonAbstract.FACING, enumdirection).set(BlockButtonAbstract.POWERED, Boolean.valueOf(false)) : this.getBlockData().set(BlockButtonAbstract.FACING, EnumDirection.DOWN).set(BlockButtonAbstract.POWERED, Boolean.valueOf(false));
    }

    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {
        if (this.e(world, blockposition, iblockdata) && !a(world, blockposition, ((EnumDirection) iblockdata.get(BlockButtonAbstract.FACING)).opposite())) {
            this.b(world, blockposition, iblockdata, 0);
            world.setAir(blockposition);
        }

    }

    private boolean e(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (this.canPlace(world, blockposition)) {
            return true;
        } else {
            this.b(world, blockposition, iblockdata, 0);
            world.setAir(blockposition);
            return false;
        }
    }

    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.get(BlockButtonAbstract.FACING);
        boolean flag = ((Boolean) iblockdata.get(BlockButtonAbstract.POWERED)).booleanValue();

        switch (BlockButtonAbstract.SyntheticClass_1.a[enumdirection.ordinal()]) {
        case 1:
            return flag ? BlockButtonAbstract.G : BlockButtonAbstract.g;

        case 2:
            return flag ? BlockButtonAbstract.F : BlockButtonAbstract.f;

        case 3:
            return flag ? BlockButtonAbstract.E : BlockButtonAbstract.e;

        case 4:
        default:
            return flag ? BlockButtonAbstract.D : BlockButtonAbstract.d;

        case 5:
            return flag ? BlockButtonAbstract.C : BlockButtonAbstract.c;

        case 6:
            return flag ? BlockButtonAbstract.B : BlockButtonAbstract.b;
        }
    }

    public boolean interact(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack, EnumDirection enumdirection, float f, float f1, float f2) {
        if (((Boolean) iblockdata.get(BlockButtonAbstract.POWERED)).booleanValue()) {
            return true;
        } else {
            // CraftBukkit start
            boolean powered = ((Boolean) iblockdata.get(POWERED));
            org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            int old = (powered) ? 15 : 0;
            int current = (!powered) ? 15 : 0;

            BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, old, current);
            world.getServer().getPluginManager().callEvent(eventRedstone);

            if ((eventRedstone.getNewCurrent() > 0) != (!powered)) {
                return true;
            }
            // CraftBukkit end
            world.setTypeAndData(blockposition, iblockdata.set(BlockButtonAbstract.POWERED, Boolean.valueOf(true)), 3);
            world.b(blockposition, blockposition);
            this.a(entityhuman, world, blockposition);
            this.c(world, blockposition, (EnumDirection) iblockdata.get(BlockButtonAbstract.FACING));
            world.a(blockposition, (Block) this, this.a(world));
            return true;
        }
    }

    protected abstract void a(@Nullable EntityHuman entityhuman, World world, BlockPosition blockposition);

    protected abstract void b(World world, BlockPosition blockposition);

    public void remove(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (((Boolean) iblockdata.get(BlockButtonAbstract.POWERED)).booleanValue()) {
            this.c(world, blockposition, (EnumDirection) iblockdata.get(BlockButtonAbstract.FACING));
        }

        super.remove(world, blockposition, iblockdata);
    }

    public int b(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return ((Boolean) iblockdata.get(BlockButtonAbstract.POWERED)).booleanValue() ? 15 : 0;
    }

    public int c(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return !((Boolean) iblockdata.get(BlockButtonAbstract.POWERED)).booleanValue() ? 0 : (iblockdata.get(BlockButtonAbstract.FACING) == enumdirection ? 15 : 0);
    }

    public boolean isPowerSource(IBlockData iblockdata) {
        return true;
    }

    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {}

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (!world.isClientSide) {
            if (((Boolean) iblockdata.get(BlockButtonAbstract.POWERED)).booleanValue()) {
                if (this.I) {
                    this.e(iblockdata, world, blockposition);
                } else {
                    // CraftBukkit start
                    org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());

                    BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, 15, 0);
                    world.getServer().getPluginManager().callEvent(eventRedstone);

                    if (eventRedstone.getNewCurrent() > 0) {
                        return;
                    }
                    // CraftBukkit end
                    world.setTypeUpdate(blockposition, iblockdata.set(BlockButtonAbstract.POWERED, Boolean.valueOf(false)));
                    this.c(world, blockposition, (EnumDirection) iblockdata.get(BlockButtonAbstract.FACING));
                    this.b(world, blockposition);
                    world.b(blockposition, blockposition);
                }

            }
        }
    }

    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, Entity entity) {
        if (!world.isClientSide) {
            if (this.I) {
                if (!((Boolean) iblockdata.get(BlockButtonAbstract.POWERED)).booleanValue()) {
                    this.e(iblockdata, world, blockposition);
                }
            }
        }
    }

    private void e(IBlockData iblockdata, World world, BlockPosition blockposition) {
        List list = world.a(EntityArrow.class, iblockdata.c(world, blockposition).a(blockposition));
        boolean flag = !list.isEmpty();
        boolean flag1 = ((Boolean) iblockdata.get(BlockButtonAbstract.POWERED)).booleanValue();

        // CraftBukkit start - Call interact event when arrows turn on wooden buttons
        if (flag1 != flag && flag) {
            org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            boolean allowed = false;

            // If all of the events are cancelled block the button press, else allow
            for (Object object : list) {
                if (object != null) {
                    EntityInteractEvent event = new EntityInteractEvent(((Entity) object).getBukkitEntity(), block);
                    world.getServer().getPluginManager().callEvent(event);

                    if (!event.isCancelled()) {
                        allowed = true;
                        break;
                    }
                }
            }

            if (!allowed) {
                return;
            }
        }
        // CraftBukkit end

        if (flag && !flag1) {
            // CraftBukkit start
            org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());

            BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, 0, 15);
            world.getServer().getPluginManager().callEvent(eventRedstone);

            if (eventRedstone.getNewCurrent() <= 0) {
                return;
            }
            // CraftBukkit end
            world.setTypeUpdate(blockposition, iblockdata.set(BlockButtonAbstract.POWERED, Boolean.valueOf(true)));
            this.c(world, blockposition, (EnumDirection) iblockdata.get(BlockButtonAbstract.FACING));
            world.b(blockposition, blockposition);
            this.a((EntityHuman) null, world, blockposition);
        }

        if (!flag && flag1) {
            // CraftBukkit start
            org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());

            BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, 15, 0);
            world.getServer().getPluginManager().callEvent(eventRedstone);

            if (eventRedstone.getNewCurrent() > 0) {
                return;
            }
            // CraftBukkit end
            world.setTypeUpdate(blockposition, iblockdata.set(BlockButtonAbstract.POWERED, Boolean.valueOf(false)));
            this.c(world, blockposition, (EnumDirection) iblockdata.get(BlockButtonAbstract.FACING));
            world.b(blockposition, blockposition);
            this.b(world, blockposition);
        }

        if (flag) {
            world.a(new BlockPosition(blockposition), (Block) this, this.a(world));
        }

    }

    private void c(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        world.applyPhysics(blockposition, this);
        world.applyPhysics(blockposition.shift(enumdirection.opposite()), this);
    }

    public IBlockData fromLegacyData(int i) {
        EnumDirection enumdirection;

        switch (i & 7) {
        case 0:
            enumdirection = EnumDirection.DOWN;
            break;

        case 1:
            enumdirection = EnumDirection.EAST;
            break;

        case 2:
            enumdirection = EnumDirection.WEST;
            break;

        case 3:
            enumdirection = EnumDirection.SOUTH;
            break;

        case 4:
            enumdirection = EnumDirection.NORTH;
            break;

        case 5:
        default:
            enumdirection = EnumDirection.UP;
        }

        return this.getBlockData().set(BlockButtonAbstract.FACING, enumdirection).set(BlockButtonAbstract.POWERED, Boolean.valueOf((i & 8) > 0));
    }

    public int toLegacyData(IBlockData iblockdata) {
        int i;

        switch (BlockButtonAbstract.SyntheticClass_1.a[((EnumDirection) iblockdata.get(BlockButtonAbstract.FACING)).ordinal()]) {
        case 1:
            i = 1;
            break;

        case 2:
            i = 2;
            break;

        case 3:
            i = 3;
            break;

        case 4:
            i = 4;
            break;

        case 5:
        default:
            i = 5;
            break;

        case 6:
            i = 0;
        }

        if (((Boolean) iblockdata.get(BlockButtonAbstract.POWERED)).booleanValue()) {
            i |= 8;
        }

        return i;
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return iblockdata.set(BlockButtonAbstract.FACING, enumblockrotation.a((EnumDirection) iblockdata.get(BlockButtonAbstract.FACING)));
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.a(enumblockmirror.a((EnumDirection) iblockdata.get(BlockButtonAbstract.FACING)));
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockButtonAbstract.FACING, BlockButtonAbstract.POWERED});
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumDirection.values().length];

        static {
            try {
                BlockButtonAbstract.SyntheticClass_1.a[EnumDirection.EAST.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                BlockButtonAbstract.SyntheticClass_1.a[EnumDirection.WEST.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                BlockButtonAbstract.SyntheticClass_1.a[EnumDirection.SOUTH.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                BlockButtonAbstract.SyntheticClass_1.a[EnumDirection.NORTH.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                BlockButtonAbstract.SyntheticClass_1.a[EnumDirection.UP.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                BlockButtonAbstract.SyntheticClass_1.a[EnumDirection.DOWN.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

        }
    }
}
