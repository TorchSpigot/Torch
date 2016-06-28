package net.minecraft.server;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.event.entity.EntityInteractEvent; // CraftBukkit

public class BlockTripwire extends Block {

    public static final BlockStateBoolean POWERED = BlockStateBoolean.of("powered");
    public static final BlockStateBoolean ATTACHED = BlockStateBoolean.of("attached");
    public static final BlockStateBoolean DISARMED = BlockStateBoolean.of("disarmed");
    public static final BlockStateBoolean NORTH = BlockStateBoolean.of("north");
    public static final BlockStateBoolean EAST = BlockStateBoolean.of("east");
    public static final BlockStateBoolean SOUTH = BlockStateBoolean.of("south");
    public static final BlockStateBoolean WEST = BlockStateBoolean.of("west");
    protected static final AxisAlignedBB B = new AxisAlignedBB(0.0D, 0.0625D, 0.0D, 1.0D, 0.15625D, 1.0D);
    protected static final AxisAlignedBB C = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);

    public BlockTripwire() {
        super(Material.ORIENTABLE);
        this.w(this.blockStateList.getBlockData().set(BlockTripwire.POWERED, Boolean.valueOf(false)).set(BlockTripwire.ATTACHED, Boolean.valueOf(false)).set(BlockTripwire.DISARMED, Boolean.valueOf(false)).set(BlockTripwire.NORTH, Boolean.valueOf(false)).set(BlockTripwire.EAST, Boolean.valueOf(false)).set(BlockTripwire.SOUTH, Boolean.valueOf(false)).set(BlockTripwire.WEST, Boolean.valueOf(false)));
        this.a(true);
    }

    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return !((Boolean) iblockdata.get(BlockTripwire.ATTACHED)).booleanValue() ? BlockTripwire.C : BlockTripwire.B;
    }

    public IBlockData updateState(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.set(BlockTripwire.NORTH, Boolean.valueOf(a(iblockaccess, blockposition, iblockdata, EnumDirection.NORTH))).set(BlockTripwire.EAST, Boolean.valueOf(a(iblockaccess, blockposition, iblockdata, EnumDirection.EAST))).set(BlockTripwire.SOUTH, Boolean.valueOf(a(iblockaccess, blockposition, iblockdata, EnumDirection.SOUTH))).set(BlockTripwire.WEST, Boolean.valueOf(a(iblockaccess, blockposition, iblockdata, EnumDirection.WEST)));
    }

    @Nullable
    public AxisAlignedBB a(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return BlockTripwire.k;
    }

    public boolean b(IBlockData iblockdata) {
        return false;
    }

    public boolean c(IBlockData iblockdata) {
        return false;
    }

    @Nullable
    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return Items.STRING;
    }

    public ItemStack a(World world, BlockPosition blockposition, IBlockData iblockdata) {
        return new ItemStack(Items.STRING);
    }

    public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata) {
        world.setTypeAndData(blockposition, iblockdata, 3);
        this.e(world, blockposition, iblockdata);
    }

    public void remove(World world, BlockPosition blockposition, IBlockData iblockdata) {
        this.e(world, blockposition, iblockdata.set(BlockTripwire.POWERED, Boolean.valueOf(true)));
    }

    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        if (!world.isClientSide) {
            if (entityhuman.getItemInMainHand() != null && entityhuman.getItemInMainHand().getItem() == Items.SHEARS) {
                world.setTypeAndData(blockposition, iblockdata.set(BlockTripwire.DISARMED, Boolean.valueOf(true)), 4);
            }

        }
    }

    private void e(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection[] aenumdirection = new EnumDirection[] { EnumDirection.SOUTH, EnumDirection.WEST};
        int i = aenumdirection.length;
        int j = 0;

        while (j < i) {
            EnumDirection enumdirection = aenumdirection[j];
            int k = 1;

            while (true) {
                if (k < 42) {
                    BlockPosition blockposition1 = blockposition.shift(enumdirection, k);
                    IBlockData iblockdata1 = world.getType(blockposition1);

                    if (iblockdata1.getBlock() == Blocks.TRIPWIRE_HOOK) {
                        if (iblockdata1.get(BlockTripwireHook.FACING) == enumdirection.opposite()) {
                            Blocks.TRIPWIRE_HOOK.a(world, blockposition1, iblockdata1, false, true, k, iblockdata);
                        }
                    } else if (iblockdata1.getBlock() == Blocks.TRIPWIRE) {
                        ++k;
                        continue;
                    }
                }

                ++j;
                break;
            }
        }

    }

    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, Entity entity) {
        if (!world.isClientSide) {
            if (!((Boolean) iblockdata.get(BlockTripwire.POWERED)).booleanValue()) {
                this.b(world, blockposition);
            }
        }
    }

    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {}

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (!world.isClientSide) {
            if (((Boolean) world.getType(blockposition).get(BlockTripwire.POWERED)).booleanValue()) {
                this.b(world, blockposition);
            }
        }
    }

    private void b(World world, BlockPosition blockposition) {
        IBlockData iblockdata = world.getType(blockposition);
        boolean flag = ((Boolean) iblockdata.get(BlockTripwire.POWERED)).booleanValue();
        boolean flag1 = false;
        List list = world.getEntities((Entity) null, iblockdata.c(world, blockposition).a(blockposition));

        if (!list.isEmpty()) {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                if (!entity.isIgnoreBlockTrigger()) {
                    flag1 = true;
                    break;
                }
            }
        }

        // CraftBukkit start - Call interact even when triggering connected tripwire
        if (flag != flag1 && flag1 && (Boolean)iblockdata.get(ATTACHED)) {
            org.bukkit.World bworld = world.getWorld();
            org.bukkit.plugin.PluginManager manager = world.getServer().getPluginManager();
            org.bukkit.block.Block block = bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            boolean allowed = false;

            // If all of the events are cancelled block the tripwire trigger, else allow
            for (Object object : list) {
                if (object != null) {
                    org.bukkit.event.Cancellable cancellable;

                    if (object instanceof EntityHuman) {
                        cancellable = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent((EntityHuman) object, org.bukkit.event.block.Action.PHYSICAL, blockposition, null, null, null);
                    } else if (object instanceof Entity) {
                        cancellable = new EntityInteractEvent(((Entity) object).getBukkitEntity(), block);
                        manager.callEvent((EntityInteractEvent) cancellable);
                    } else {
                        continue;
                    }

                    if (!cancellable.isCancelled()) {
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

        if (flag1 != flag) {
            iblockdata = iblockdata.set(BlockTripwire.POWERED, Boolean.valueOf(flag1));
            world.setTypeAndData(blockposition, iblockdata, 3);
            this.e(world, blockposition, iblockdata);
        }

        if (flag1) {
            world.a(new BlockPosition(blockposition), (Block) this, this.a(world));
        }

    }

    public static boolean a(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, EnumDirection enumdirection) {
        BlockPosition blockposition1 = blockposition.shift(enumdirection);
        IBlockData iblockdata1 = iblockaccess.getType(blockposition1);
        Block block = iblockdata1.getBlock();

        if (block == Blocks.TRIPWIRE_HOOK) {
            EnumDirection enumdirection1 = enumdirection.opposite();

            return iblockdata1.get(BlockTripwireHook.FACING) == enumdirection1;
        } else {
            return block == Blocks.TRIPWIRE;
        }
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockTripwire.POWERED, Boolean.valueOf((i & 1) > 0)).set(BlockTripwire.ATTACHED, Boolean.valueOf((i & 4) > 0)).set(BlockTripwire.DISARMED, Boolean.valueOf((i & 8) > 0));
    }

    public int toLegacyData(IBlockData iblockdata) {
        int i = 0;

        if (((Boolean) iblockdata.get(BlockTripwire.POWERED)).booleanValue()) {
            i |= 1;
        }

        if (((Boolean) iblockdata.get(BlockTripwire.ATTACHED)).booleanValue()) {
            i |= 4;
        }

        if (((Boolean) iblockdata.get(BlockTripwire.DISARMED)).booleanValue()) {
            i |= 8;
        }

        return i;
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        switch (BlockTripwire.SyntheticClass_1.a[enumblockrotation.ordinal()]) {
        case 1:
            return iblockdata.set(BlockTripwire.NORTH, iblockdata.get(BlockTripwire.SOUTH)).set(BlockTripwire.EAST, iblockdata.get(BlockTripwire.WEST)).set(BlockTripwire.SOUTH, iblockdata.get(BlockTripwire.NORTH)).set(BlockTripwire.WEST, iblockdata.get(BlockTripwire.EAST));

        case 2:
            return iblockdata.set(BlockTripwire.NORTH, iblockdata.get(BlockTripwire.EAST)).set(BlockTripwire.EAST, iblockdata.get(BlockTripwire.SOUTH)).set(BlockTripwire.SOUTH, iblockdata.get(BlockTripwire.WEST)).set(BlockTripwire.WEST, iblockdata.get(BlockTripwire.NORTH));

        case 3:
            return iblockdata.set(BlockTripwire.NORTH, iblockdata.get(BlockTripwire.WEST)).set(BlockTripwire.EAST, iblockdata.get(BlockTripwire.NORTH)).set(BlockTripwire.SOUTH, iblockdata.get(BlockTripwire.EAST)).set(BlockTripwire.WEST, iblockdata.get(BlockTripwire.SOUTH));

        default:
            return iblockdata;
        }
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        switch (BlockTripwire.SyntheticClass_1.b[enumblockmirror.ordinal()]) {
        case 1:
            return iblockdata.set(BlockTripwire.NORTH, iblockdata.get(BlockTripwire.SOUTH)).set(BlockTripwire.SOUTH, iblockdata.get(BlockTripwire.NORTH));

        case 2:
            return iblockdata.set(BlockTripwire.EAST, iblockdata.get(BlockTripwire.WEST)).set(BlockTripwire.WEST, iblockdata.get(BlockTripwire.EAST));

        default:
            return super.a(iblockdata, enumblockmirror);
        }
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockTripwire.POWERED, BlockTripwire.ATTACHED, BlockTripwire.DISARMED, BlockTripwire.NORTH, BlockTripwire.EAST, BlockTripwire.WEST, BlockTripwire.SOUTH});
    }

    static class SyntheticClass_1 {

        static final int[] a;
        static final int[] b = new int[EnumBlockMirror.values().length];

        static {
            try {
                BlockTripwire.SyntheticClass_1.b[EnumBlockMirror.LEFT_RIGHT.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                BlockTripwire.SyntheticClass_1.b[EnumBlockMirror.FRONT_BACK.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            a = new int[EnumBlockRotation.values().length];

            try {
                BlockTripwire.SyntheticClass_1.a[EnumBlockRotation.CLOCKWISE_180.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                BlockTripwire.SyntheticClass_1.a[EnumBlockRotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                BlockTripwire.SyntheticClass_1.a[EnumBlockRotation.CLOCKWISE_90.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

        }
    }
}
