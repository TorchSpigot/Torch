package net.minecraft.server;

import java.util.Iterator;
import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class BlockStem extends BlockPlant implements IBlockFragilePlantElement {

    public static final BlockStateInteger AGE = BlockStateInteger.of("age", 0, 7);
    public static final BlockStateDirection FACING = BlockTorch.FACING;
    private final Block blockFruit;
    protected static final AxisAlignedBB[] d = new AxisAlignedBB[] { new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.125D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.25D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.375D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.5D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.625D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.75D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.875D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D)};

    protected BlockStem(Block block) {
        this.y(this.blockStateList.getBlockData().set(BlockStem.AGE, Integer.valueOf(0)).set(BlockStem.FACING, EnumDirection.UP));
        this.blockFruit = block;
        this.a(true);
        this.a((CreativeModeTab) null);
    }

    @Override
    public AxisAlignedBB b(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return BlockStem.d[iblockdata.get(BlockStem.AGE).intValue()];
    }

    @Override
    public IBlockData updateState(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        int i = iblockdata.get(BlockStem.AGE).intValue();

        iblockdata = iblockdata.set(BlockStem.FACING, EnumDirection.UP);
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection = (EnumDirection) iterator.next();

            if (iblockaccess.getType(blockposition.shift(enumdirection)).getBlock() == this.blockFruit && i == 7) {
                iblockdata = iblockdata.set(BlockStem.FACING, enumdirection);
                break;
            }
        }

        return iblockdata;
    }

    @Override
    protected boolean i(IBlockData iblockdata) {
        return iblockdata.getBlock() == Blocks.FARMLAND;
    }

    @Override
    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        super.b(world, blockposition, iblockdata, random);
        if (world.isLightLevel(blockposition.up(), 9)) { // Paper
            float f = BlockCrops.a(this, world, blockposition);

            if (random.nextInt((int) ((100.0F / (this == Blocks.PUMPKIN_STEM ? world.spigotConfig.pumpkinModifier : world.spigotConfig.melonModifier)) * (25.0F / f)) + 1) == 0) { // Spigot
                int i = iblockdata.get(BlockStem.AGE).intValue();

                if (i < 7) {
                    iblockdata = iblockdata.set(BlockStem.AGE, Integer.valueOf(i + 1));
                    // world.setTypeAndData(blockposition, iblockdata, 2); // CraftBukkit
                    CraftEventFactory.handleBlockGrowEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this, toLegacyData(iblockdata)); // CraftBukkit
                } else {
                    Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

                    while (iterator.hasNext()) {
                        EnumDirection enumdirection = (EnumDirection) iterator.next();

                        if (world.getType(blockposition.shift(enumdirection)).getBlock() == this.blockFruit) {
                            return;
                        }
                    }

                    blockposition = blockposition.shift(EnumDirection.EnumDirectionLimit.HORIZONTAL.a(random));
                    Block block = world.getType(blockposition.down()).getBlock();

                    if (world.getType(blockposition).getBlock().material == Material.AIR && (block == Blocks.FARMLAND || block == Blocks.DIRT || block == Blocks.GRASS)) {
                        // world.setTypeUpdate(blockposition, this.blockFruit.getBlockData()); // CraftBukkit
                        CraftEventFactory.handleBlockGrowEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this.blockFruit, 0); // CraftBukkit
                    }
                }
            }

        }
    }

    public void g(World world, BlockPosition blockposition, IBlockData iblockdata) {
        int i = iblockdata.get(BlockStem.AGE).intValue() + MathHelper.nextInt(world.random, 2, 5);

        // world.setTypeAndData(blockposition, iblockdata.set(BlockStem.AGE, Integer.valueOf(Math.min(7, i))), 2);
        CraftEventFactory.handleBlockGrowEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this, Math.min(7, i)); // CraftBukkit
    }

    @Override
    public void dropNaturally(World world, BlockPosition blockposition, IBlockData iblockdata, float f, int i) {
        super.dropNaturally(world, blockposition, iblockdata, f, i);
        Item item = this.e();

        if (item != null) {
            int j = iblockdata.get(BlockStem.AGE).intValue();

            for (int k = 0; k < 3; ++k) {
                if (world.random.nextInt(15) <= j) {
                    a(world, blockposition, new ItemStack(item));
                }
            }

        }
    }

    @Nullable
    protected Item e() {
        return this.blockFruit == Blocks.PUMPKIN ? Items.PUMPKIN_SEEDS : (this.blockFruit == Blocks.MELON_BLOCK ? Items.MELON_SEEDS : null);
    }

    @Override
    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return Items.a;
    }

    @Override
    public ItemStack a(World world, BlockPosition blockposition, IBlockData iblockdata) {
        Item item = this.e();

        return item == null ? ItemStack.a : new ItemStack(item);
    }

    @Override
    public boolean a(World world, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return iblockdata.get(BlockStem.AGE).intValue() != 7;
    }

    @Override
    public boolean a(World world, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void b(World world, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        this.g(world, blockposition, iblockdata);
    }

    @Override
    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockStem.AGE, Integer.valueOf(i));
    }

    @Override
    public int toLegacyData(IBlockData iblockdata) {
        return iblockdata.get(BlockStem.AGE).intValue();
    }

    @Override
    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockStem.AGE, BlockStem.FACING});
    }
}
