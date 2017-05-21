package net.minecraft.server;

import java.util.Random;

public class BlockCake extends Block {

    public static final BlockStateInteger BITES = BlockStateInteger.of("bites", 0, 6);
    protected static final AxisAlignedBB[] b = new AxisAlignedBB[] { new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.1875D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.3125D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.4375D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.5625D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.6875D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.8125D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D)};

    protected BlockCake() {
        super(Material.CAKE);
        this.y(this.blockStateList.getBlockData().set(BlockCake.BITES, Integer.valueOf(0)));
        this.a(true);
    }

    @Override
    public AxisAlignedBB b(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return BlockCake.b[iblockdata.get(BlockCake.BITES).intValue()];
    }

    @Override
    public boolean c(IBlockData iblockdata) {
        return false;
    }

    @Override
    public boolean b(IBlockData iblockdata) {
        return false;
    }

    @Override
    public boolean interact(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        return this.b(world, blockposition, iblockdata, entityhuman);
    }

    private boolean b(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        if (!entityhuman.n(false)) {
            return false;
        } else {
            entityhuman.b(StatisticList.H);
            // CraftBukkit start
            // entityhuman.getFoodData().eat(2, 0.1F);
            int oldFoodLevel = entityhuman.getFoodData().foodLevel;

            org.bukkit.event.entity.FoodLevelChangeEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callFoodLevelChangeEvent(entityhuman, 2 + oldFoodLevel);

            if (!event.isCancelled()) {
                entityhuman.getFoodData().eat(event.getFoodLevel() - oldFoodLevel, 0.1F);
            }

            ((EntityPlayer) entityhuman).getBukkitEntity().sendHealthUpdate();
            // CraftBukkit end
            int i = iblockdata.get(BlockCake.BITES).intValue();

            if (i < 6) {
                world.setTypeAndData(blockposition, iblockdata.set(BlockCake.BITES, Integer.valueOf(i + 1)), 3);
            } else {
                world.setAir(blockposition);
            }

            return true;
        }
    }

    @Override
    public boolean canPlace(World world, BlockPosition blockposition) {
        return super.canPlace(world, blockposition) ? this.b(world, blockposition) : false;
    }

    @Override
    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1) {
        if (!this.b(world, blockposition)) {
            world.setAir(blockposition);
        }

    }

    private boolean b(World world, BlockPosition blockposition) {
        return world.getType(blockposition.down()).getMaterial().isBuildable();
    }

    @Override
    public int a(Random random) {
        return 0;
    }

    @Override
    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return Items.a;
    }

    @Override
    public ItemStack a(World world, BlockPosition blockposition, IBlockData iblockdata) {
        return new ItemStack(Items.CAKE);
    }

    @Override
    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockCake.BITES, Integer.valueOf(i));
    }

    @Override
    public int toLegacyData(IBlockData iblockdata) {
        return iblockdata.get(BlockCake.BITES).intValue();
    }

    @Override
    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockCake.BITES});
    }

    @Override
    public int c(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return (7 - iblockdata.get(BlockCake.BITES).intValue()) * 2;
    }

    @Override
    public boolean isComplexRedstone(IBlockData iblockdata) {
        return true;
    }
}
