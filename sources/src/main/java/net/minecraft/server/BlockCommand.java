package net.minecraft.server;

import java.util.Random;

import org.bukkit.event.block.BlockRedstoneEvent; // CraftBukkit

public class BlockCommand extends BlockTileEntity {

    public static final BlockStateDirection a = BlockDirectional.FACING;
    public static final BlockStateBoolean b = BlockStateBoolean.of("conditional");

    public BlockCommand(MaterialMapColor materialmapcolor) {
        super(Material.ORE, materialmapcolor);
        this.y(this.blockStateList.getBlockData().set(BlockCommand.a, EnumDirection.NORTH).set(BlockCommand.b, Boolean.valueOf(false)));
    }

    @Override
    public TileEntity a(World world, int i) {
        TileEntityCommand tileentitycommand = new TileEntityCommand();

        tileentitycommand.b(this == Blocks.dd);
        return tileentitycommand;
    }

    @Override
    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof TileEntityCommand) {
            TileEntityCommand tileentitycommand = (TileEntityCommand) tileentity;
            boolean flag = world.isBlockIndirectlyPowered(blockposition);
            boolean flag1 = tileentitycommand.f();
            boolean flag2 = tileentitycommand.h();

        // CraftBukkit start
        org.bukkit.block.Block bukkitBlock = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
        int old = flag1 ? 15 : 0;
        int current = flag ? 15 : 0;

        BlockRedstoneEvent eventRedstone = BlockRedstoneEvent.requestMutable(bukkitBlock, old, current);
        world.getServer().getPluginManager().callEvent(eventRedstone);
        // CraftBukkit end

        if (eventRedstone.getNewCurrent() > 0 && !(eventRedstone.getOldCurrent() > 0)) { // CraftBukkit
                tileentitycommand.a(true);
                if (tileentitycommand.k() != TileEntityCommand.Type.SEQUENCE && !flag2) {
                    boolean flag3 = !tileentitycommand.l() || this.e(world, blockposition, iblockdata);

                    tileentitycommand.c(flag3);
                    world.a(blockposition, this, this.a(world));
                    if (flag3) {
                        this.c(world, blockposition);
                    }
                }
        } else if (!(eventRedstone.getNewCurrent() > 0) && eventRedstone.getOldCurrent() > 0) { // CraftBukkit
                tileentitycommand.a(false);
            }

        }
    }

    @Override
    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof TileEntityCommand) {
            TileEntityCommand tileentitycommand = (TileEntityCommand) tileentity;
            CommandBlockListenerAbstract commandblocklistenerabstract = tileentitycommand.getCommandBlock();
            boolean flag = !UtilColor.b(commandblocklistenerabstract.getCommand());
            TileEntityCommand.Type tileentitycommand_type = tileentitycommand.k();
            boolean flag1 = !tileentitycommand.l() || this.e(world, blockposition, iblockdata);
            boolean flag2 = tileentitycommand.i();
            boolean flag3 = false;

            if (tileentitycommand_type != TileEntityCommand.Type.SEQUENCE && flag2 && flag) {
                commandblocklistenerabstract.a(world);
                flag3 = true;
            }

            if (tileentitycommand.f() || tileentitycommand.h()) {
                if (tileentitycommand_type == TileEntityCommand.Type.SEQUENCE && flag1 && flag) {
                    commandblocklistenerabstract.a(world);
                    flag3 = true;
                }

                if (tileentitycommand_type == TileEntityCommand.Type.AUTO) {
                    world.a(blockposition, this, this.a(world));
                    if (flag1) {
                        this.c(world, blockposition);
                    }
                }
            }

            if (!flag3) {
                commandblocklistenerabstract.a(0);
            }

            tileentitycommand.c(flag1);
            world.updateAdjacentComparators(blockposition, this);
        }
    }

    public boolean e(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection enumdirection = iblockdata.get(BlockCommand.a);
        TileEntity tileentity = world.getTileEntity(blockposition.shift(enumdirection.opposite()));

        return tileentity instanceof TileEntityCommand && ((TileEntityCommand) tileentity).getCommandBlock().k() > 0;
    }

    @Override
    public int a(World world) {
        return 1;
    }

    @Override
    public boolean interact(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof TileEntityCommand && entityhuman.dk()) {
            entityhuman.a((TileEntityCommand) tileentity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isComplexRedstone(IBlockData iblockdata) {
        return true;
    }

    @Override
    public int c(IBlockData iblockdata, World world, BlockPosition blockposition) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        return tileentity instanceof TileEntityCommand ? ((TileEntityCommand) tileentity).getCommandBlock().k() : 0;
    }

    @Override
    public void postPlace(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof TileEntityCommand) {
            TileEntityCommand tileentitycommand = (TileEntityCommand) tileentity;
            CommandBlockListenerAbstract commandblocklistenerabstract = tileentitycommand.getCommandBlock();

            if (itemstack.hasName()) {
                commandblocklistenerabstract.setName(itemstack.getName());
            }

            NBTTagCompound nbttagcompound = itemstack.getTag();

            if (nbttagcompound == null || !nbttagcompound.hasKeyOfType("BlockEntityTag", 10)) {
                commandblocklistenerabstract.a(world.getGameRules().getBoolean("sendCommandFeedback"));
                tileentitycommand.b(this == Blocks.dd);
            }

            if (tileentitycommand.k() == TileEntityCommand.Type.SEQUENCE) {
                boolean flag = world.isBlockIndirectlyPowered(blockposition);

                tileentitycommand.a(flag);
            }

        }
    }

    @Override
    public int a(Random random) {
        return 0;
    }

    @Override
    public EnumRenderType a(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockCommand.a, EnumDirection.fromType1(i & 7)).set(BlockCommand.b, Boolean.valueOf((i & 8) != 0));
    }

    @Override
    public int toLegacyData(IBlockData iblockdata) {
        return iblockdata.get(BlockCommand.a).a() | (iblockdata.get(BlockCommand.b).booleanValue() ? 8 : 0);
    }

    @Override
    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return iblockdata.set(BlockCommand.a, enumblockrotation.a(iblockdata.get(BlockCommand.a)));
    }

    @Override
    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.a(enumblockmirror.a(iblockdata.get(BlockCommand.a)));
    }

    @Override
    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockCommand.a, BlockCommand.b});
    }

    @Override
    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        return this.getBlockData().set(BlockCommand.a, EnumDirection.a(blockposition, entityliving)).set(BlockCommand.b, Boolean.valueOf(false));
    }

    public void c(World world, BlockPosition blockposition) {
        IBlockData iblockdata = world.getType(blockposition);

        if (iblockdata.getBlock() == Blocks.COMMAND_BLOCK || iblockdata.getBlock() == Blocks.dc) {
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(blockposition);

            blockposition_mutableblockposition.c(iblockdata.get(BlockCommand.a));

            for (TileEntity tileentity = world.getTileEntity(blockposition_mutableblockposition); tileentity instanceof TileEntityCommand; tileentity = world.getTileEntity(blockposition_mutableblockposition)) {
                TileEntityCommand tileentitycommand = (TileEntityCommand) tileentity;

                if (tileentitycommand.k() != TileEntityCommand.Type.SEQUENCE) {
                    break;
                }

                IBlockData iblockdata1 = world.getType(blockposition_mutableblockposition);
                Block block = iblockdata1.getBlock();

                if (block != Blocks.dd || world.b(blockposition_mutableblockposition, block)) {
                    break;
                }

                world.a(new BlockPosition(blockposition_mutableblockposition), block, this.a(world));
                blockposition_mutableblockposition.c(iblockdata1.get(BlockCommand.a));
            }

        }
    }
}
