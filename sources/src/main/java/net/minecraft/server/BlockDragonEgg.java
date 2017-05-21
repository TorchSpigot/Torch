package net.minecraft.server;

import java.util.Random;

import org.bukkit.event.block.BlockFromToEvent; // CraftBukkit

public class BlockDragonEgg extends Block {

    protected static final AxisAlignedBB a = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D);

    public BlockDragonEgg() {
        super(Material.DRAGON_EGG, MaterialMapColor.E);
    }

    @Override
    public AxisAlignedBB b(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return BlockDragonEgg.a;
    }

    @Override
    public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata) {
        world.a(blockposition, this, this.a(world));
    }

    @Override
    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1) {
        world.a(blockposition, this, this.a(world));
    }

    @Override
    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        this.b(world, blockposition);
    }

    private void b(World world, BlockPosition blockposition) {
        if (BlockFalling.i(world.getType(blockposition.down())) && blockposition.getY() >= 0) {
            boolean flag = true;

            if (!BlockFalling.instaFall && world.areChunksLoadedBetween(blockposition.a(-32, -32, -32), blockposition.a(32, 32, 32))) {
                world.addEntity(new EntityFallingBlock(world, blockposition.getX() + 0.5F, blockposition.getY(), blockposition.getZ() + 0.5F, this.getBlockData()));
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

    @Override
    public boolean interact(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        this.c(world, blockposition);
        return true;
    }

    @Override
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
                    world.setTypeAndData(blockposition1, iblockdata, 2);
                    world.setAir(blockposition);

                    return;
                }
            }

        }
    }

    @Override
    public int a(World world) {
        return 5;
    }

    @Override
    public boolean b(IBlockData iblockdata) {
        return false;
    }

    @Override
    public boolean c(IBlockData iblockdata) {
        return false;
    }
}
