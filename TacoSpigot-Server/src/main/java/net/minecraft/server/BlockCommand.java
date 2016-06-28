package net.minecraft.server;

import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.event.block.BlockRedstoneEvent; // CraftBukkit

public class BlockCommand extends BlockTileEntity {

    public static final BlockStateDirection a = BlockDirectional.FACING;
    public static final BlockStateBoolean b = BlockStateBoolean.of("conditional");

    public BlockCommand(MaterialMapColor materialmapcolor) {
        super(Material.ORE, materialmapcolor);
        this.w(this.blockStateList.getBlockData().set(BlockCommand.a, EnumDirection.NORTH).set(BlockCommand.b, Boolean.valueOf(false)));
    }

    public TileEntity a(World world, int i) {
        TileEntityCommand tileentitycommand = new TileEntityCommand();

        tileentitycommand.b(this == Blocks.dd);
        return tileentitycommand;
    }

    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {
        if (!world.isClientSide) {
            TileEntity tileentity = world.getTileEntity(blockposition);

            if (tileentity instanceof TileEntityCommand) {
                TileEntityCommand tileentitycommand = (TileEntityCommand) tileentity;
                boolean flag = world.isBlockIndirectlyPowered(blockposition);
                boolean flag1 = tileentitycommand.e();
                boolean flag2 = tileentitycommand.g();

            // CraftBukkit start
            org.bukkit.block.Block bukkitBlock = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            int old = flag1 ? 15 : 0;
            int current = flag ? 15 : 0;

            BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bukkitBlock, old, current);
            world.getServer().getPluginManager().callEvent(eventRedstone);
            // CraftBukkit end

            if (eventRedstone.getNewCurrent() > 0 && !(eventRedstone.getOldCurrent() > 0)) { // CraftBukkit
                    tileentitycommand.a(true);
                    if (tileentitycommand.j() != TileEntityCommand.Type.SEQUENCE && !flag2) {
                        boolean flag3 = !tileentitycommand.k() || this.e(world, blockposition, iblockdata);

                        tileentitycommand.c(flag3);
                        world.a(blockposition, (Block) this, this.a(world));
                        if (flag3) {
                            this.c(world, blockposition);
                        }
                    }
            } else if (!(eventRedstone.getNewCurrent() > 0) && eventRedstone.getOldCurrent() > 0) { // CraftBukkit
                    tileentitycommand.a(false);
                }

            }
        }
    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (!world.isClientSide) {
            TileEntity tileentity = world.getTileEntity(blockposition);

            if (tileentity instanceof TileEntityCommand) {
                TileEntityCommand tileentitycommand = (TileEntityCommand) tileentity;
                CommandBlockListenerAbstract commandblocklistenerabstract = tileentitycommand.getCommandBlock();
                boolean flag = !UtilColor.b(commandblocklistenerabstract.getCommand());
                TileEntityCommand.Type tileentitycommand_type = tileentitycommand.j();
                boolean flag1 = !tileentitycommand.k() || this.e(world, blockposition, iblockdata);
                boolean flag2 = tileentitycommand.h();
                boolean flag3 = false;

                if (tileentitycommand_type != TileEntityCommand.Type.SEQUENCE && flag2 && flag) {
                    commandblocklistenerabstract.a(world);
                    flag3 = true;
                }

                if (tileentitycommand.e() || tileentitycommand.g()) {
                    if (tileentitycommand_type == TileEntityCommand.Type.SEQUENCE && flag1 && flag) {
                        commandblocklistenerabstract.a(world);
                        flag3 = true;
                    }

                    if (tileentitycommand_type == TileEntityCommand.Type.AUTO) {
                        world.a(blockposition, (Block) this, this.a(world));
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
    }

    public boolean e(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.get(BlockCommand.a);
        TileEntity tileentity = world.getTileEntity(blockposition.shift(enumdirection.opposite()));

        return tileentity instanceof TileEntityCommand && ((TileEntityCommand) tileentity).getCommandBlock().k() > 0;
    }

    public int a(World world) {
        return 1;
    }

    public boolean interact(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack, EnumDirection enumdirection, float f, float f1, float f2) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof TileEntityCommand) {
            if (!entityhuman.abilities.canInstantlyBuild) {
                return false;
            } else {
                entityhuman.a((TileEntityCommand) tileentity);
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean isComplexRedstone(IBlockData iblockdata) {
        return true;
    }

    public int d(IBlockData iblockdata, World world, BlockPosition blockposition) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        return tileentity instanceof TileEntityCommand ? ((TileEntityCommand) tileentity).getCommandBlock().k() : 0;
    }

    public void postPlace(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof TileEntityCommand) {
            TileEntityCommand tileentitycommand = (TileEntityCommand) tileentity;
            CommandBlockListenerAbstract commandblocklistenerabstract = tileentitycommand.getCommandBlock();

            if (itemstack.hasName()) {
                commandblocklistenerabstract.setName(itemstack.getName());
            }

            if (!world.isClientSide) {
                NBTTagCompound nbttagcompound = itemstack.getTag();

                if (nbttagcompound == null || !nbttagcompound.hasKeyOfType("BlockEntityTag", 10)) {
                    commandblocklistenerabstract.a(world.getGameRules().getBoolean("sendCommandFeedback"));
                    tileentitycommand.b(this == Blocks.dd);
                }

                if (tileentitycommand.j() == TileEntityCommand.Type.SEQUENCE) {
                    boolean flag = world.isBlockIndirectlyPowered(blockposition);

                    tileentitycommand.a(flag);
                }
            }

        }
    }

    public int a(Random random) {
        return 0;
    }

    public EnumRenderType a(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockCommand.a, EnumDirection.fromType1(i & 7)).set(BlockCommand.b, Boolean.valueOf((i & 8) != 0));
    }

    public int toLegacyData(IBlockData iblockdata) {
        return ((EnumDirection) iblockdata.get(BlockCommand.a)).a() | (((Boolean) iblockdata.get(BlockCommand.b)).booleanValue() ? 8 : 0);
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return iblockdata.set(BlockCommand.a, enumblockrotation.a((EnumDirection) iblockdata.get(BlockCommand.a)));
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.a(enumblockmirror.a((EnumDirection) iblockdata.get(BlockCommand.a)));
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockCommand.a, BlockCommand.b});
    }

    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        return this.getBlockData().set(BlockCommand.a, BlockPiston.a(blockposition, entityliving)).set(BlockCommand.b, Boolean.valueOf(false));
    }

    public void c(World world, BlockPosition blockposition) {
        IBlockData iblockdata = world.getType(blockposition);

        if (iblockdata.getBlock() == Blocks.COMMAND_BLOCK || iblockdata.getBlock() == Blocks.dc) {
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(blockposition);

            blockposition_mutableblockposition.c((EnumDirection) iblockdata.get(BlockCommand.a));

            for (TileEntity tileentity = world.getTileEntity(blockposition_mutableblockposition); tileentity instanceof TileEntityCommand; tileentity = world.getTileEntity(blockposition_mutableblockposition)) {
                TileEntityCommand tileentitycommand = (TileEntityCommand) tileentity;

                if (tileentitycommand.j() != TileEntityCommand.Type.SEQUENCE) {
                    break;
                }

                IBlockData iblockdata1 = world.getType(blockposition_mutableblockposition);
                Block block = iblockdata1.getBlock();

                if (block != Blocks.dd || world.b((BlockPosition) blockposition_mutableblockposition, block)) {
                    break;
                }

                world.a(new BlockPosition(blockposition_mutableblockposition), block, this.a(world));
                blockposition_mutableblockposition.c((EnumDirection) iblockdata1.get(BlockCommand.a));
            }

        }
    }
}
