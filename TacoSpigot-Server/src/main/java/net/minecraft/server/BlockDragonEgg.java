package net.minecraft.server;

import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.event.block.BlockFromToEvent; // CraftBukkit

public class BlockDragonEgg extends Block {

    protected static final AxisAlignedBB a = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D);

    public BlockDragonEgg() {
        super(Material.DRAGON_EGG, MaterialMapColor.E);
    }

    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return BlockDragonEgg.a;
    }

    public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata) {
        world.a(blockposition, (Block) this, this.a(world));
    }

    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {
        world.a(blockposition, (Block) this, this.a(world));
    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        this.b(world, blockposition);
    }

    private void b(World world, BlockPosition blockposition) {
        if (BlockFalling.i(world.getType(blockposition.down())) && blockposition.getY() >= 0) {
            byte b0 = 32;

            if (!BlockFalling.instaFall && world.areChunksLoadedBetween(blockposition.a(-b0, -b0, -b0), blockposition.a(b0, b0, b0))) {
                world.addEntity(new EntityFallingBlock(world, (double) ((float) blockposition.getX() + 0.5F), (double) blockposition.getY(), (double) ((float) blockposition.getZ() + 0.5F), this.getBlockData()));
            } else {
                world.setAir(blockposition);

                BlockPosition blockposition1;

                for (blockposition1 = blockposition; BlockFalling.i(world.getType(blockposition1)) && blockposition1.getY() > 0; blockposition1 = blockposition1.down()) {
                    ;
                }

                if (blockposition1.getY() > 0) {
                    world.setTypeAndData(blockposition1, this.getBlockData(), 2);
                }
            }

        }
    }

    public boolean interact(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack, EnumDirection enumdirection, float f, float f1, float f2) {
        this.c(world, blockposition);
        return true;
    }

    public void attack(World world, BlockPosition blockposition, EntityHuman entityhuman) {
        this.c(world, blockposition);
    }

    private void c(World world, BlockPosition blockposition) {
        IBlockData iblockdata = world.getType(blockposition);

        if (iblockdata.getBlock() == this) {
            for (int i = 0; i < 1000; ++i) {
                BlockPosition blockposition1 = blockposition.a(world.random.nextInt(16) - world.random.nextInt(16), world.random.nextInt(8) - world.random.nextInt(8), world.random.nextInt(16) - world.random.nextInt(16));

                if (world.getType(blockposition1).getBlock().material == Material.AIR) {
                    // CraftBukkit start
                    org.bukkit.block.Block from = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                    org.bukkit.block.Block to = world.getWorld().getBlockAt(blockposition1.getX(), blockposition1.getY(), blockposition1.getZ());
                    BlockFromToEvent event = new BlockFromToEvent(from, to);
                    org.bukkit.Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        return;
                    }

                    blockposition1 = new BlockPosition(event.getToBlock().getX(), event.getToBlock().getY(), event.getToBlock().getZ());
                    // CraftBukkit end
                    if (world.isClientSide) {
                        for (int j = 0; j < 128; ++j) {
                            double d0 = world.random.nextDouble();
                            float f = (world.random.nextFloat() - 0.5F) * 0.2F;
                            float f1 = (world.random.nextFloat() - 0.5F) * 0.2F;
                            float f2 = (world.random.nextFloat() - 0.5F) * 0.2F;
                            double d1 = (double) blockposition1.getX() + (double) (blockposition.getX() - blockposition1.getX()) * d0 + (world.random.nextDouble() - 0.5D) + 0.5D;
                            double d2 = (double) blockposition1.getY() + (double) (blockposition.getY() - blockposition1.getY()) * d0 + world.random.nextDouble() - 0.5D;
                            double d3 = (double) blockposition1.getZ() + (double) (blockposition.getZ() - blockposition1.getZ()) * d0 + (world.random.nextDouble() - 0.5D) + 0.5D;

                            world.addParticle(EnumParticle.PORTAL, d1, d2, d3, (double) f, (double) f1, (double) f2, new int[0]);
                        }
                    } else {
                        world.setTypeAndData(blockposition1, iblockdata, 2);
                        world.setAir(blockposition);
                    }

                    return;
                }
            }

        }
    }

    public int a(World world) {
        return 5;
    }

    public boolean b(IBlockData iblockdata) {
        return false;
    }

    public boolean c(IBlockData iblockdata) {
        return false;
    }
}
