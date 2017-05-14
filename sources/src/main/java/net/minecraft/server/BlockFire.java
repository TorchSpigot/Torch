package net.minecraft.server;

import com.koloboke.collect.map.hash.HashObjIntMaps;
import com.koloboke.collect.Equivalence;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;
// CraftBukkit end

public class BlockFire extends Block {

    public static final BlockStateInteger AGE = BlockStateInteger.of("age", 0, 15);
    public static final BlockStateBoolean NORTH = BlockStateBoolean.of("north");
    public static final BlockStateBoolean EAST = BlockStateBoolean.of("east");
    public static final BlockStateBoolean SOUTH = BlockStateBoolean.of("south");
    public static final BlockStateBoolean WEST = BlockStateBoolean.of("west");
    public static final BlockStateBoolean UPPER = BlockStateBoolean.of("up");
    private final Map<Block, Integer> flameChances = HashObjIntMaps.getDefaultFactory().withKeyEquivalence(Equivalence.identity()).newMutableMap();
    private final Map<Block, Integer> B = HashObjIntMaps.getDefaultFactory().withKeyEquivalence(Equivalence.identity()).newMutableMap();

    @Override
	public IBlockData updateState(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return !iblockaccess.getType(blockposition.down()).r() && !Blocks.FIRE.c(iblockaccess, blockposition.down()) ? iblockdata.set(BlockFire.NORTH, Boolean.valueOf(this.c(iblockaccess, blockposition.north()))).set(BlockFire.EAST, Boolean.valueOf(this.c(iblockaccess, blockposition.east()))).set(BlockFire.SOUTH, Boolean.valueOf(this.c(iblockaccess, blockposition.south()))).set(BlockFire.WEST, Boolean.valueOf(this.c(iblockaccess, blockposition.west()))).set(BlockFire.UPPER, Boolean.valueOf(this.c(iblockaccess, blockposition.up()))) : this.getBlockData();
    }

    protected BlockFire() {
        super(Material.FIRE);
        this.y(this.blockStateList.getBlockData().set(BlockFire.AGE, Integer.valueOf(0)).set(BlockFire.NORTH, Boolean.valueOf(false)).set(BlockFire.EAST, Boolean.valueOf(false)).set(BlockFire.SOUTH, Boolean.valueOf(false)).set(BlockFire.WEST, Boolean.valueOf(false)).set(BlockFire.UPPER, Boolean.valueOf(false)));
        this.a(true);
    }

    public static void e() {
        Blocks.FIRE.a(Blocks.PLANKS, 5, 20);
        Blocks.FIRE.a(Blocks.DOUBLE_WOODEN_SLAB, 5, 20);
        Blocks.FIRE.a(Blocks.WOODEN_SLAB, 5, 20);
        Blocks.FIRE.a(Blocks.FENCE_GATE, 5, 20);
        Blocks.FIRE.a(Blocks.SPRUCE_FENCE_GATE, 5, 20);
        Blocks.FIRE.a(Blocks.BIRCH_FENCE_GATE, 5, 20);
        Blocks.FIRE.a(Blocks.JUNGLE_FENCE_GATE, 5, 20);
        Blocks.FIRE.a(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
        Blocks.FIRE.a(Blocks.ACACIA_FENCE_GATE, 5, 20);
        Blocks.FIRE.a(Blocks.FENCE, 5, 20);
        Blocks.FIRE.a(Blocks.SPRUCE_FENCE, 5, 20);
        Blocks.FIRE.a(Blocks.BIRCH_FENCE, 5, 20);
        Blocks.FIRE.a(Blocks.JUNGLE_FENCE, 5, 20);
        Blocks.FIRE.a(Blocks.DARK_OAK_FENCE, 5, 20);
        Blocks.FIRE.a(Blocks.ACACIA_FENCE, 5, 20);
        Blocks.FIRE.a(Blocks.OAK_STAIRS, 5, 20);
        Blocks.FIRE.a(Blocks.BIRCH_STAIRS, 5, 20);
        Blocks.FIRE.a(Blocks.SPRUCE_STAIRS, 5, 20);
        Blocks.FIRE.a(Blocks.JUNGLE_STAIRS, 5, 20);
        Blocks.FIRE.a(Blocks.ACACIA_STAIRS, 5, 20);
        Blocks.FIRE.a(Blocks.DARK_OAK_STAIRS, 5, 20);
        Blocks.FIRE.a(Blocks.LOG, 5, 5);
        Blocks.FIRE.a(Blocks.LOG2, 5, 5);
        Blocks.FIRE.a(Blocks.LEAVES, 30, 60);
        Blocks.FIRE.a(Blocks.LEAVES2, 30, 60);
        Blocks.FIRE.a(Blocks.BOOKSHELF, 30, 20);
        Blocks.FIRE.a(Blocks.TNT, 15, 100);
        Blocks.FIRE.a(Blocks.TALLGRASS, 60, 100);
        Blocks.FIRE.a(Blocks.DOUBLE_PLANT, 60, 100);
        Blocks.FIRE.a(Blocks.YELLOW_FLOWER, 60, 100);
        Blocks.FIRE.a(Blocks.RED_FLOWER, 60, 100);
        Blocks.FIRE.a(Blocks.DEADBUSH, 60, 100);
        Blocks.FIRE.a(Blocks.WOOL, 30, 60);
        Blocks.FIRE.a(Blocks.VINE, 15, 100);
        Blocks.FIRE.a(Blocks.COAL_BLOCK, 5, 5);
        Blocks.FIRE.a(Blocks.HAY_BLOCK, 60, 20);
        Blocks.FIRE.a(Blocks.CARPET, 60, 20);
    }

    public void a(Block block, int i, int j) {
        this.flameChances.put(block, Integer.valueOf(i));
        this.B.put(block, Integer.valueOf(j));
    }

    @Override
	@Nullable
    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return BlockFire.k;
    }

    @Override
	public boolean b(IBlockData iblockdata) {
        return false;
    }

    @Override
	public boolean c(IBlockData iblockdata) {
        return false;
    }

    @Override
	public int a(Random random) {
        return 0;
    }

    @Override
	public int a(World world) {
        return 30;
    }

    @Override
	public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (world.getGameRules().getBoolean("doFireTick")) {
            if (!this.canPlace(world, blockposition)) {
                fireExtinguished(world, blockposition); // CraftBukkit - invalid place location
            }

            Block block = world.getType(blockposition.down()).getBlock();
            boolean flag = block == Blocks.NETHERRACK;

            if (world.worldProvider instanceof WorldProviderTheEnd && block == Blocks.BEDROCK) {
                flag = true;
            }

            int i = iblockdata.get(BlockFire.AGE).intValue();

            if (!flag && world.W() && this.b(world, blockposition) && random.nextFloat() < 0.2F + i * 0.03F) {
                fireExtinguished(world, blockposition); // CraftBukkit - extinguished by rain
            } else {
                if (i < 15) {
                    iblockdata = iblockdata.set(BlockFire.AGE, Integer.valueOf(i + random.nextInt(3) / 2));
                    world.setTypeAndData(blockposition, iblockdata, 4);
                }

                world.a(blockposition, this, this.a(world) + random.nextInt(10));
                if (!flag) {
                    if (!this.c(world, blockposition)) {
                        if (!world.getType(blockposition.down()).r() || i > 3) {
                            fireExtinguished(world, blockposition); // CraftBukkit
                        }

                        return;
                    }

                    if (!this.c((IBlockAccess) world, blockposition.down()) && i == 15 && random.nextInt(4) == 0) {
                        fireExtinguished(world, blockposition); // CraftBukkit
                        return;
                    }
                }

                boolean flag1 = world.C(blockposition);
                byte b0 = 0;

                if (flag1) {
                    b0 = -50;
                }

                // CraftBukkit start - add source blockposition to burn calls
                this.a(world, blockposition.east(), 300 + b0, random, i, blockposition);
                this.a(world, blockposition.west(), 300 + b0, random, i, blockposition);
                this.a(world, blockposition.down(), 250 + b0, random, i, blockposition);
                this.a(world, blockposition.up(), 250 + b0, random, i, blockposition);
                this.a(world, blockposition.north(), 300 + b0, random, i, blockposition);
                this.a(world, blockposition.south(), 300 + b0, random, i, blockposition);
                // CraftBukkit end

                for (int j = -1; j <= 1; ++j) {
                    for (int k = -1; k <= 1; ++k) {
                        for (int l = -1; l <= 4; ++l) {
                            if (j != 0 || l != 0 || k != 0) {
                                int i1 = 100;

                                if (l > 1) {
                                    i1 += (l - 1) * 100;
                                }

                                BlockPosition blockposition1 = blockposition.a(j, l, k);
                                if (!world.isLoaded(blockposition1)) continue; // Paper
                                int j1 = this.d(world, blockposition1);

                                if (j1 > 0) {
                                    int k1 = (j1 + 40 + world.getDifficulty().a() * 7) / (i + 30);

                                    if (flag1) {
                                        k1 /= 2;
                                    }

                                    if (k1 > 0 && random.nextInt(i1) <= k1 && (!world.W() || !this.b(world, blockposition1))) {
                                        int l1 = i + random.nextInt(5) / 4;

                                        if (l1 > 15) {
                                            l1 = 15;
                                        }

                                        // CraftBukkit start - Call to stop spread of fire
                                        if (world.getType(blockposition1) != Blocks.FIRE) {
                                            if (CraftEventFactory.callBlockIgniteEvent(world, blockposition1.getX(), blockposition1.getY(), blockposition1.getZ(), blockposition.getX(), blockposition.getY(), blockposition.getZ()).isCancelled()) {
                                                continue;
                                            }

                                            org.bukkit.Server server = world.getServer();
                                            org.bukkit.World bworld = world.getWorld();
                                            org.bukkit.block.BlockState blockState = bworld.getBlockAt(blockposition1.getX(), blockposition1.getY(), blockposition1.getZ()).getState();
                                            blockState.setTypeId(Block.getId(this));
                                            blockState.setData(new org.bukkit.material.MaterialData(Block.getId(this), (byte) l1));

                                            BlockSpreadEvent spreadEvent = new BlockSpreadEvent(blockState.getBlock(), bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), blockState);
                                            server.getPluginManager().callEvent(spreadEvent);

                                            if (!spreadEvent.isCancelled()) {
                                                blockState.update(true);
                                            }
                                        }
                                        // CraftBukkit end
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    protected boolean b(World world, BlockPosition blockposition) {
        return world.isRainingAt(blockposition) || world.isRainingAt(blockposition.west()) || world.isRainingAt(blockposition.east()) || world.isRainingAt(blockposition.north()) || world.isRainingAt(blockposition.south());
    }

    @Override
	public boolean r() {
        return false;
    }

    private int c(Block block) {
        Integer integer = this.B.get(block);

        return integer == null ? 0 : integer.intValue();
    }

    private int d(Block block) {
        Integer integer = this.flameChances.get(block);

        return integer == null ? 0 : integer.intValue();
    }

    private void a(World world, BlockPosition blockposition, int i, Random random, int j, BlockPosition sourceposition) { // CraftBukkit add sourceposition
        // Paper start
        final IBlockData iblockdata = world.getTypeIfLoaded(blockposition);
        if (iblockdata == null) return;
        int k = this.c(iblockdata.getBlock());
        // Paper end

        if (random.nextInt(i) < k) {
            //IBlockData iblockdata = world.getType(blockposition); // Paper

            // CraftBukkit start
            org.bukkit.block.Block theBlock = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            org.bukkit.block.Block sourceBlock = world.getWorld().getBlockAt(sourceposition.getX(), sourceposition.getY(), sourceposition.getZ());

            BlockBurnEvent event = new BlockBurnEvent(theBlock, sourceBlock);
            world.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }
            // CraftBukkit end

            if (random.nextInt(j + 10) < 5 && !world.isRainingAt(blockposition)) {
                int l = j + random.nextInt(5) / 4;

                if (l > 15) {
                    l = 15;
                }

                world.setTypeAndData(blockposition, this.getBlockData().set(BlockFire.AGE, Integer.valueOf(l)), 3);
            } else {
                world.setAir(blockposition);
            }

            if (iblockdata.getBlock() == Blocks.TNT) {
                Blocks.TNT.postBreak(world, blockposition, iblockdata.set(BlockTNT.EXPLODE, Boolean.valueOf(true)));
            }
        }

    }

    private boolean c(World world, BlockPosition blockposition) {
        EnumDirection[] aenumdirection = EnumDirection.values();
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection[j];

            if (this.c((IBlockAccess) world, blockposition.shift(enumdirection))) {
                return true;
            }
        }

        return false;
    }

    private int d(World world, BlockPosition blockposition) {
        if (!world.isEmpty(blockposition)) {
            return 0;
        } else {
            int i = 0;
            EnumDirection[] aenumdirection = EnumDirection.values();
            int j = aenumdirection.length;

            for (int k = 0; k < j; ++k) {
                EnumDirection enumdirection = aenumdirection[k];

                final IBlockData type = world.getTypeIfLoaded(blockposition.shift(enumdirection)); // Paper
                if (type == null) continue; // Paper
                i = Math.max(this.d(type.getBlock()), i); // Paper
            }

            return i;
        }
    }

    @Override
	public boolean m() {
        return false;
    }

    public boolean c(IBlockAccess iblockaccess, BlockPosition blockposition) {
        return this.d(iblockaccess.getType(blockposition).getBlock()) > 0;
    }

    @Override
	public boolean canPlace(World world, BlockPosition blockposition) {
        return world.getType(blockposition.down()).r() || this.c(world, blockposition);
    }

    @Override
	public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1) {
        if (!world.getType(blockposition.down()).r() && !this.c(world, blockposition)) {
            fireExtinguished(world, blockposition); // CraftBukkit - fuel block gone
        }

    }

    @Override
	public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (world.worldProvider.getDimensionManager().getDimensionID() > 0 || !Blocks.PORTAL.b(world, blockposition)) {
            if (!world.getType(blockposition.down()).r() && !this.c(world, blockposition)) {
                fireExtinguished(world, blockposition); // CraftBukkit - fuel block broke
            } else {
                world.a(blockposition, this, this.a(world) + world.random.nextInt(10));
            }
        }
    }

    @Override
	public MaterialMapColor r(IBlockData iblockdata) {
        return MaterialMapColor.f;
    }

    @Override
	public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockFire.AGE, Integer.valueOf(i));
    }

    @Override
	public int toLegacyData(IBlockData iblockdata) {
        return iblockdata.get(BlockFire.AGE).intValue();
    }

    @Override
	protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockFire.AGE, BlockFire.NORTH, BlockFire.EAST, BlockFire.SOUTH, BlockFire.WEST, BlockFire.UPPER});
    }

    // CraftBukkit start
    private void fireExtinguished(World world, BlockPosition position) {
        if (!CraftEventFactory.callBlockFadeEvent(world.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ()), Blocks.AIR).isCancelled()) {
            world.setAir(position);
        }
    }
    // CraftBukkit end
}
