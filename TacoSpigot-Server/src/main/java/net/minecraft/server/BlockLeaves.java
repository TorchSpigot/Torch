package net.minecraft.server;

import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.event.block.LeavesDecayEvent; // CraftBukkit

public abstract class BlockLeaves extends Block {

    public static final BlockStateBoolean DECAYABLE = BlockStateBoolean.of("decayable");
    public static final BlockStateBoolean CHECK_DECAY = BlockStateBoolean.of("check_decay");
    protected boolean c;
    int[] d;

    public BlockLeaves() {
        super(Material.LEAVES);
        this.a(true);
        this.a(CreativeModeTab.c);
        this.c(0.2F);
        this.d(1);
        this.a(SoundEffectType.c);
    }

    public void remove(World world, BlockPosition blockposition, IBlockData iblockdata) {
        byte b0 = 1;
        int i = b0 + 1;
        int j = blockposition.getX();
        int k = blockposition.getY();
        int l = blockposition.getZ();

        if (world.areChunksLoadedBetween(new BlockPosition(j - i, k - i, l - i), new BlockPosition(j + i, k + i, l + i))) {
            for (int i1 = -b0; i1 <= b0; ++i1) {
                for (int j1 = -b0; j1 <= b0; ++j1) {
                    for (int k1 = -b0; k1 <= b0; ++k1) {
                        BlockPosition blockposition1 = blockposition.a(i1, j1, k1);
                        IBlockData iblockdata1 = world.getType(blockposition1);

                        if (iblockdata1.getMaterial() == Material.LEAVES && !((Boolean) iblockdata1.get(BlockLeaves.CHECK_DECAY)).booleanValue()) {
                            world.setTypeAndData(blockposition1, iblockdata1.set(BlockLeaves.CHECK_DECAY, Boolean.valueOf(true)), 4);
                        }
                    }
                }
            }
        }

    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (!world.isClientSide) {
            if (((Boolean) iblockdata.get(BlockLeaves.CHECK_DECAY)).booleanValue() && ((Boolean) iblockdata.get(BlockLeaves.DECAYABLE)).booleanValue()) {
                byte b0 = 4;
                int i = b0 + 1;
                int j = blockposition.getX();
                int k = blockposition.getY();
                int l = blockposition.getZ();
                byte b1 = 32;
                int i1 = b1 * b1;
                int j1 = b1 / 2;

                if (this.d == null) {
                    this.d = new int[b1 * b1 * b1];
                }

                if (world.areChunksLoadedBetween(new BlockPosition(j - i, k - i, l - i), new BlockPosition(j + i, k + i, l + i))) {
                    BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

                    int k1;
                    int l1;
                    int i2;

                    for (k1 = -b0; k1 <= b0; ++k1) {
                        for (l1 = -b0; l1 <= b0; ++l1) {
                            for (i2 = -b0; i2 <= b0; ++i2) {
                                IBlockData iblockdata1 = world.getType(blockposition_mutableblockposition.c(j + k1, k + l1, l + i2));
                                Block block = iblockdata1.getBlock();

                                if (block != Blocks.LOG && block != Blocks.LOG2) {
                                    if (iblockdata1.getMaterial() == Material.LEAVES) {
                                        this.d[(k1 + j1) * i1 + (l1 + j1) * b1 + i2 + j1] = -2;
                                    } else {
                                        this.d[(k1 + j1) * i1 + (l1 + j1) * b1 + i2 + j1] = -1;
                                    }
                                } else {
                                    this.d[(k1 + j1) * i1 + (l1 + j1) * b1 + i2 + j1] = 0;
                                }
                            }
                        }
                    }

                    for (k1 = 1; k1 <= 4; ++k1) {
                        for (l1 = -b0; l1 <= b0; ++l1) {
                            for (i2 = -b0; i2 <= b0; ++i2) {
                                for (int j2 = -b0; j2 <= b0; ++j2) {
                                    if (this.d[(l1 + j1) * i1 + (i2 + j1) * b1 + j2 + j1] == k1 - 1) {
                                        if (this.d[(l1 + j1 - 1) * i1 + (i2 + j1) * b1 + j2 + j1] == -2) {
                                            this.d[(l1 + j1 - 1) * i1 + (i2 + j1) * b1 + j2 + j1] = k1;
                                        }

                                        if (this.d[(l1 + j1 + 1) * i1 + (i2 + j1) * b1 + j2 + j1] == -2) {
                                            this.d[(l1 + j1 + 1) * i1 + (i2 + j1) * b1 + j2 + j1] = k1;
                                        }

                                        if (this.d[(l1 + j1) * i1 + (i2 + j1 - 1) * b1 + j2 + j1] == -2) {
                                            this.d[(l1 + j1) * i1 + (i2 + j1 - 1) * b1 + j2 + j1] = k1;
                                        }

                                        if (this.d[(l1 + j1) * i1 + (i2 + j1 + 1) * b1 + j2 + j1] == -2) {
                                            this.d[(l1 + j1) * i1 + (i2 + j1 + 1) * b1 + j2 + j1] = k1;
                                        }

                                        if (this.d[(l1 + j1) * i1 + (i2 + j1) * b1 + (j2 + j1 - 1)] == -2) {
                                            this.d[(l1 + j1) * i1 + (i2 + j1) * b1 + (j2 + j1 - 1)] = k1;
                                        }

                                        if (this.d[(l1 + j1) * i1 + (i2 + j1) * b1 + j2 + j1 + 1] == -2) {
                                            this.d[(l1 + j1) * i1 + (i2 + j1) * b1 + j2 + j1 + 1] = k1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                int k2 = this.d[j1 * i1 + j1 * b1 + j1];

                if (k2 >= 0) {
                    world.setTypeAndData(blockposition, iblockdata.set(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false)), 4);
                } else {
                    this.b(world, blockposition);
                }
            }

        }
    }

    private void b(World world, BlockPosition blockposition) {
        // CraftBukkit start
        LeavesDecayEvent event = new LeavesDecayEvent(world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
        world.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled() || world.getType(blockposition).getBlock() != this) {
            return;
        }
        // CraftBukkit end
        this.b(world, blockposition, world.getType(blockposition), 0);
        world.setAir(blockposition);
    }

    public int a(Random random) {
        return random.nextInt(20) == 0 ? 1 : 0;
    }

    @Nullable
    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return Item.getItemOf(Blocks.SAPLING);
    }

    public void dropNaturally(World world, BlockPosition blockposition, IBlockData iblockdata, float f, int i) {
        if (!world.isClientSide) {
            int j = this.i(iblockdata);

            if (i > 0) {
                j -= 2 << i;
                if (j < 10) {
                    j = 10;
                }
            }

            if (world.random.nextInt(j) == 0) {
                Item item = this.getDropType(iblockdata, world.random, i);

                a(world, blockposition, new ItemStack(item, 1, this.getDropData(iblockdata)));
            }

            j = 200;
            if (i > 0) {
                j -= 10 << i;
                if (j < 40) {
                    j = 40;
                }
            }

            this.a(world, blockposition, iblockdata, j);
        }

    }

    protected void a(World world, BlockPosition blockposition, IBlockData iblockdata, int i) {}

    protected int i(IBlockData iblockdata) {
        return 20;
    }

    public boolean b(IBlockData iblockdata) {
        return !this.c;
    }

    public boolean j() {
        return false;
    }

    public abstract BlockWood.EnumLogVariant e(int i);
}
