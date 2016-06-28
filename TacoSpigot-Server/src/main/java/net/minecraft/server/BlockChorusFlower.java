package net.minecraft.server;

import java.util.Iterator;
import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class BlockChorusFlower extends Block {

    public static final BlockStateInteger AGE = BlockStateInteger.of("age", 0, 5);

    protected BlockChorusFlower() {
        super(Material.PLANT);
        this.w(this.blockStateList.getBlockData().set(BlockChorusFlower.AGE, Integer.valueOf(0)));
        this.a(CreativeModeTab.c);
        this.a(true);
    }

    @Nullable
    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return null;
    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (!this.b(world, blockposition)) {
            world.setAir(blockposition, true);
        } else {
            BlockPosition blockposition1 = blockposition.up();

            if (world.isEmpty(blockposition1) && blockposition1.getY() < 256) {
                int i = ((Integer) iblockdata.get(BlockChorusFlower.AGE)).intValue();

                if (i < 5 && random.nextInt(1) == 0) {
                    boolean flag = false;
                    boolean flag1 = false;
                    Block block = world.getType(blockposition.down()).getBlock();
                    int j;

                    if (block == Blocks.END_STONE) {
                        flag = true;
                    } else if (block == Blocks.CHORUS_PLANT) {
                        j = 1;

                        int k;

                        for (k = 0; k < 4; ++k) {
                            Block block1 = world.getType(blockposition.down(j + 1)).getBlock();

                            if (block1 != Blocks.CHORUS_PLANT) {
                                if (block1 == Blocks.END_STONE) {
                                    flag1 = true;
                                }
                                break;
                            }

                            ++j;
                        }

                        k = 4;
                        if (flag1) {
                            ++k;
                        }

                        if (j < 2 || random.nextInt(k) >= j) {
                            flag = true;
                        }
                    } else if (block == Blocks.AIR) {
                        flag = true;
                    }

                    if (flag && a(world, blockposition1, (EnumDirection) null) && world.isEmpty(blockposition.up(2))) {
                        // world.setTypeAndData(blockposition, Blocks.CHORUS_PLANT.getBlockData(), 2);
                        // this.a(world, blockposition1, i);
                        // CraftBukkit start - add event
                        BlockPosition target = blockposition1;
                        if (CraftEventFactory.handleBlockSpreadEvent(
                                world.getWorld().getBlockAt(target.getX(), target.getY(), target.getZ()),
                                world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()),
                                this,
                                toLegacyData(this.getBlockData().set(BlockChorusFlower.AGE, Integer.valueOf(i)))
                        )) {
                            world.setTypeAndData(blockposition, Blocks.CHORUS_PLANT.getBlockData(), 2);
                            world.triggerEffect(1033, blockposition, 0);
                        }
                        // CraftBukkit end
                    } else if (i < 4) {
                        j = random.nextInt(4);
                        boolean flag2 = false;

                        if (flag1) {
                            ++j;
                        }

                        for (int l = 0; l < j; ++l) {
                            EnumDirection enumdirection = EnumDirection.EnumDirectionLimit.HORIZONTAL.a(random);
                            BlockPosition blockposition2 = blockposition.shift(enumdirection);

                            if (world.isEmpty(blockposition2) && world.isEmpty(blockposition2.down()) && a(world, blockposition2, enumdirection.opposite())) {
                                // CraftBukkit start - add event
                                // this.a(world, blockposition2, i + 1);
                                BlockPosition target = blockposition2;
                                if (CraftEventFactory.handleBlockSpreadEvent(
                                        world.getWorld().getBlockAt(target.getX(), target.getY(), target.getZ()),
                                        world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()),
                                        this,
                                        toLegacyData(this.getBlockData().set(BlockChorusFlower.AGE, Integer.valueOf(i + 1)))
                                )) {
                                    world.triggerEffect(1033, blockposition, 0);
                                    flag2 = true;
                                }
                                // CraftBukkit end
                            }
                        }

                        if (flag2) {
                            world.setTypeAndData(blockposition, Blocks.CHORUS_PLANT.getBlockData(), 2);
                        } else {
                            // CraftBukkit - add event
                            if (CraftEventFactory.handleBlockGrowEvent(
                                    world,
                                    blockposition.getX(),
                                    blockposition.getY(),
                                    blockposition.getZ(),
                                    this,
                                    toLegacyData(iblockdata.set(BlockChorusFlower.AGE, Integer.valueOf(5)))
                            )) {
                                world.triggerEffect(1034, blockposition, 0);
                            }
                            // this.c(world, blockposition);
                            // CraftBukkit end
                        }
                    } else if (i == 4) {
                        // CraftBukkit - add event
                        if (CraftEventFactory.handleBlockGrowEvent(
                                world,
                                blockposition.getX(),
                                blockposition.getY(),
                                blockposition.getZ(),
                                this,
                                toLegacyData(iblockdata.set(BlockChorusFlower.AGE, Integer.valueOf(5)))
                        )) {
                            world.triggerEffect(1034, blockposition, 0);
                        }
                        // this.c(world, blockposition);
                        // CraftBukkit end
                    }

                }
            }
        }
    }

    private void a(World world, BlockPosition blockposition, int i) {
        world.setTypeAndData(blockposition, this.getBlockData().set(BlockChorusFlower.AGE, Integer.valueOf(i)), 2);
        world.triggerEffect(1033, blockposition, 0);
    }

    private void c(World world, BlockPosition blockposition) {
        world.setTypeAndData(blockposition, this.getBlockData().set(BlockChorusFlower.AGE, Integer.valueOf(5)), 2);
        world.triggerEffect(1034, blockposition, 0);
    }

    private static boolean a(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        EnumDirection enumdirection1;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            enumdirection1 = (EnumDirection) iterator.next();
        } while (enumdirection1 == enumdirection || world.isEmpty(blockposition.shift(enumdirection1)));

        return false;
    }

    public boolean c(IBlockData iblockdata) {
        return false;
    }

    public boolean b(IBlockData iblockdata) {
        return false;
    }

    public boolean canPlace(World world, BlockPosition blockposition) {
        return super.canPlace(world, blockposition) && this.b(world, blockposition);
    }

    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {
        if (!this.b(world, blockposition)) {
            world.a(blockposition, (Block) this, 1);
        }

    }

    public boolean b(World world, BlockPosition blockposition) {
        Block block = world.getType(blockposition.down()).getBlock();

        if (block != Blocks.CHORUS_PLANT && block != Blocks.END_STONE) {
            if (block == Blocks.AIR) {
                int i = 0;
                Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

                while (iterator.hasNext()) {
                    EnumDirection enumdirection = (EnumDirection) iterator.next();
                    Block block1 = world.getType(blockposition.shift(enumdirection)).getBlock();

                    if (block1 == Blocks.CHORUS_PLANT) {
                        ++i;
                    } else if (block1 != Blocks.AIR) {
                        return false;
                    }
                }

                return i == 1;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void a(World world, EntityHuman entityhuman, BlockPosition blockposition, IBlockData iblockdata, @Nullable TileEntity tileentity, @Nullable ItemStack itemstack) {
        super.a(world, entityhuman, blockposition, iblockdata, tileentity, itemstack);
        a(world, blockposition, new ItemStack(Item.getItemOf(this)));
    }

    protected ItemStack u(IBlockData iblockdata) {
        return null;
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockChorusFlower.AGE, Integer.valueOf(i));
    }

    public int toLegacyData(IBlockData iblockdata) {
        return ((Integer) iblockdata.get(BlockChorusFlower.AGE)).intValue();
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockChorusFlower.AGE});
    }

    public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata) {
        super.onPlace(world, blockposition, iblockdata);
    }

    public static void a(World world, BlockPosition blockposition, Random random, int i) {
        world.setTypeAndData(blockposition, Blocks.CHORUS_PLANT.getBlockData(), 2);
        a(world, blockposition, random, blockposition, i, 0);
    }

    private static void a(World world, BlockPosition blockposition, Random random, BlockPosition blockposition1, int i, int j) {
        int k = random.nextInt(4) + 1;

        if (j == 0) {
            ++k;
        }

        for (int l = 0; l < k; ++l) {
            BlockPosition blockposition2 = blockposition.up(l + 1);

            if (!a(world, blockposition2, (EnumDirection) null)) {
                return;
            }

            world.setTypeAndData(blockposition2, Blocks.CHORUS_PLANT.getBlockData(), 2);
        }

        boolean flag = false;

        if (j < 4) {
            int i1 = random.nextInt(4);

            if (j == 0) {
                ++i1;
            }

            for (int j1 = 0; j1 < i1; ++j1) {
                EnumDirection enumdirection = EnumDirection.EnumDirectionLimit.HORIZONTAL.a(random);
                BlockPosition blockposition3 = blockposition.up(k).shift(enumdirection);

                if (Math.abs(blockposition3.getX() - blockposition1.getX()) < i && Math.abs(blockposition3.getZ() - blockposition1.getZ()) < i && world.isEmpty(blockposition3) && world.isEmpty(blockposition3.down()) && a(world, blockposition3, enumdirection.opposite())) {
                    flag = true;
                    world.setTypeAndData(blockposition3, Blocks.CHORUS_PLANT.getBlockData(), 2);
                    a(world, blockposition3, random, blockposition1, i, j + 1);
                }
            }
        }

        if (!flag) {
            world.setTypeAndData(blockposition.up(k), Blocks.CHORUS_FLOWER.getBlockData().set(BlockChorusFlower.AGE, Integer.valueOf(5)), 2);
        }

    }
}
