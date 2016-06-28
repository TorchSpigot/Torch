package net.minecraft.server;

import com.google.common.cache.LoadingCache;
import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.event.entity.EntityPortalEnterEvent; // CraftBukkit
import org.bukkit.event.world.PortalCreateEvent; // CraftBukkit

public class BlockPortal extends BlockHalfTransparent {

    public static final BlockStateEnum<EnumDirection.EnumAxis> AXIS = BlockStateEnum.of("axis", EnumDirection.EnumAxis.class, new EnumDirection.EnumAxis[] { EnumDirection.EnumAxis.X, EnumDirection.EnumAxis.Z});
    protected static final AxisAlignedBB b = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D);
    protected static final AxisAlignedBB c = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D);
    protected static final AxisAlignedBB d = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D);

    public BlockPortal() {
        super(Material.PORTAL, false);
        this.w(this.blockStateList.getBlockData().set(BlockPortal.AXIS, EnumDirection.EnumAxis.X));
        this.a(true);
    }

    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        switch (BlockPortal.SyntheticClass_1.a[((EnumDirection.EnumAxis) iblockdata.get(BlockPortal.AXIS)).ordinal()]) {
        case 1:
            return BlockPortal.b;

        case 2:
        default:
            return BlockPortal.d;

        case 3:
            return BlockPortal.c;
        }
    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        super.b(world, blockposition, iblockdata, random);
        if (world.spigotConfig.enableZombiePigmenPortalSpawns && world.worldProvider.d() && world.getGameRules().getBoolean("doMobSpawning") && random.nextInt(2000) < world.getDifficulty().a()) { // Spigot
            int i = blockposition.getY();

            BlockPosition blockposition1;

            for (blockposition1 = blockposition; !world.getType(blockposition1).q() && blockposition1.getY() > 0; blockposition1 = blockposition1.down()) {
                ;
            }

            if (i > 0 && !world.getType(blockposition1.up()).l()) {
                // CraftBukkit - set spawn reason to NETHER_PORTAL
                Entity entity = ItemMonsterEgg.spawnCreature(world, EntityTypes.getName(EntityPigZombie.class), (double) blockposition1.getX() + 0.5D, (double) blockposition1.getY() + 1.1D, (double) blockposition1.getZ() + 0.5D, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NETHER_PORTAL);

                if (entity != null) {
                    entity.portalCooldown = entity.aC();
                }
            }
        }

    }

    @Nullable
    public AxisAlignedBB a(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return BlockPortal.k;
    }

    public static int a(EnumDirection.EnumAxis enumdirection_enumaxis) {
        return enumdirection_enumaxis == EnumDirection.EnumAxis.X ? 1 : (enumdirection_enumaxis == EnumDirection.EnumAxis.Z ? 2 : 0);
    }

    public boolean c(IBlockData iblockdata) {
        return false;
    }

    public boolean b(World world, BlockPosition blockposition) {
        BlockPortal.Shape blockportal_shape = new BlockPortal.Shape(world, blockposition, EnumDirection.EnumAxis.X);

        if (blockportal_shape.d() && blockportal_shape.e == 0) {
            // CraftBukkit start - return portalcreator
            return blockportal_shape.createPortal();
            // return true;
        } else {
            BlockPortal.Shape blockportal_shape1 = new BlockPortal.Shape(world, blockposition, EnumDirection.EnumAxis.Z);

            if (blockportal_shape1.d() && blockportal_shape1.e == 0) {
                return blockportal_shape1.createPortal();
                // return true;
                // CraftBukkit end
            } else {
                return false;
            }
        }
    }

    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {
        EnumDirection.EnumAxis enumdirection_enumaxis = (EnumDirection.EnumAxis) iblockdata.get(BlockPortal.AXIS);
        BlockPortal.Shape blockportal_shape;

        if (enumdirection_enumaxis == EnumDirection.EnumAxis.X) {
            blockportal_shape = new BlockPortal.Shape(world, blockposition, EnumDirection.EnumAxis.X);
            if (!blockportal_shape.d() || blockportal_shape.e < blockportal_shape.width * blockportal_shape.height) {
                world.setTypeUpdate(blockposition, Blocks.AIR.getBlockData());
            }
        } else if (enumdirection_enumaxis == EnumDirection.EnumAxis.Z) {
            blockportal_shape = new BlockPortal.Shape(world, blockposition, EnumDirection.EnumAxis.Z);
            if (!blockportal_shape.d() || blockportal_shape.e < blockportal_shape.width * blockportal_shape.height) {
                world.setTypeUpdate(blockposition, Blocks.AIR.getBlockData());
            }
        }

    }

    public int a(Random random) {
        return 0;
    }

    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, Entity entity) {
        if (!entity.isPassenger() && !entity.isVehicle() && entity.aV()) {
            // CraftBukkit start - Entity in portal
            EntityPortalEnterEvent event = new EntityPortalEnterEvent(entity.getBukkitEntity(), new org.bukkit.Location(world.getWorld(), blockposition.getX(), blockposition.getY(), blockposition.getZ()));
            world.getServer().getPluginManager().callEvent(event);
            // CraftBukkit end
            entity.e(blockposition);
        }

    }

    @Nullable
    public ItemStack a(World world, BlockPosition blockposition, IBlockData iblockdata) {
        return null;
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockPortal.AXIS, (i & 3) == 2 ? EnumDirection.EnumAxis.Z : EnumDirection.EnumAxis.X);
    }

    public int toLegacyData(IBlockData iblockdata) {
        return a((EnumDirection.EnumAxis) iblockdata.get(BlockPortal.AXIS));
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        switch (BlockPortal.SyntheticClass_1.b[enumblockrotation.ordinal()]) {
        case 1:
        case 2:
            switch (BlockPortal.SyntheticClass_1.a[((EnumDirection.EnumAxis) iblockdata.get(BlockPortal.AXIS)).ordinal()]) {
            case 1:
                return iblockdata.set(BlockPortal.AXIS, EnumDirection.EnumAxis.Z);

            case 3:
                return iblockdata.set(BlockPortal.AXIS, EnumDirection.EnumAxis.X);

            default:
                return iblockdata;
            }

        default:
            return iblockdata;
        }
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockPortal.AXIS});
    }

    public ShapeDetector.ShapeDetectorCollection c(World world, BlockPosition blockposition) {
        EnumDirection.EnumAxis enumdirection_enumaxis = EnumDirection.EnumAxis.Z;
        BlockPortal.Shape blockportal_shape = new BlockPortal.Shape(world, blockposition, EnumDirection.EnumAxis.X);
        LoadingCache loadingcache = ShapeDetector.a(world, true);

        if (!blockportal_shape.d()) {
            enumdirection_enumaxis = EnumDirection.EnumAxis.X;
            blockportal_shape = new BlockPortal.Shape(world, blockposition, EnumDirection.EnumAxis.Z);
        }

        if (!blockportal_shape.d()) {
            return new ShapeDetector.ShapeDetectorCollection(blockposition, EnumDirection.NORTH, EnumDirection.UP, loadingcache, 1, 1, 1);
        } else {
            int[] aint = new int[EnumDirection.EnumAxisDirection.values().length];
            EnumDirection enumdirection = blockportal_shape.c.f();
            BlockPosition blockposition1 = blockportal_shape.position.up(blockportal_shape.a() - 1);
            EnumDirection.EnumAxisDirection[] aenumdirection_enumaxisdirection = EnumDirection.EnumAxisDirection.values();
            int i = aenumdirection_enumaxisdirection.length;

            int j;

            for (j = 0; j < i; ++j) {
                EnumDirection.EnumAxisDirection enumdirection_enumaxisdirection = aenumdirection_enumaxisdirection[j];
                ShapeDetector.ShapeDetectorCollection shapedetector_shapedetectorcollection = new ShapeDetector.ShapeDetectorCollection(enumdirection.c() == enumdirection_enumaxisdirection ? blockposition1 : blockposition1.shift(blockportal_shape.c, blockportal_shape.b() - 1), EnumDirection.a(enumdirection_enumaxisdirection, enumdirection_enumaxis), EnumDirection.UP, loadingcache, blockportal_shape.b(), blockportal_shape.a(), 1);

                for (int k = 0; k < blockportal_shape.b(); ++k) {
                    for (int l = 0; l < blockportal_shape.a(); ++l) {
                        ShapeDetectorBlock shapedetectorblock = shapedetector_shapedetectorcollection.a(k, l, 1);

                        if (shapedetectorblock.a() != null && shapedetectorblock.a().getMaterial() != Material.AIR) {
                            ++aint[enumdirection_enumaxisdirection.ordinal()];
                        }
                    }
                }
            }

            EnumDirection.EnumAxisDirection enumdirection_enumaxisdirection1 = EnumDirection.EnumAxisDirection.POSITIVE;
            EnumDirection.EnumAxisDirection[] aenumdirection_enumaxisdirection1 = EnumDirection.EnumAxisDirection.values();

            j = aenumdirection_enumaxisdirection1.length;

            for (int i1 = 0; i1 < j; ++i1) {
                EnumDirection.EnumAxisDirection enumdirection_enumaxisdirection2 = aenumdirection_enumaxisdirection1[i1];

                if (aint[enumdirection_enumaxisdirection2.ordinal()] < aint[enumdirection_enumaxisdirection1.ordinal()]) {
                    enumdirection_enumaxisdirection1 = enumdirection_enumaxisdirection2;
                }
            }

            return new ShapeDetector.ShapeDetectorCollection(enumdirection.c() == enumdirection_enumaxisdirection1 ? blockposition1 : blockposition1.shift(blockportal_shape.c, blockportal_shape.b() - 1), EnumDirection.a(enumdirection_enumaxisdirection1, enumdirection_enumaxis), EnumDirection.UP, loadingcache, blockportal_shape.b(), blockportal_shape.a(), 1);
        }
    }

    static class SyntheticClass_1 {

        static final int[] a;
        static final int[] b = new int[EnumBlockRotation.values().length];

        static {
            try {
                BlockPortal.SyntheticClass_1.b[EnumBlockRotation.COUNTERCLOCKWISE_90.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                BlockPortal.SyntheticClass_1.b[EnumBlockRotation.CLOCKWISE_90.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            a = new int[EnumDirection.EnumAxis.values().length];

            try {
                BlockPortal.SyntheticClass_1.a[EnumDirection.EnumAxis.X.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                BlockPortal.SyntheticClass_1.a[EnumDirection.EnumAxis.Y.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                BlockPortal.SyntheticClass_1.a[EnumDirection.EnumAxis.Z.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

        }
    }

    public static class Shape {

        private final World a;
        private final EnumDirection.EnumAxis b;
        private final EnumDirection c;
        private final EnumDirection d;
        private int e = 0;
        private BlockPosition position;
        private int height;
        private int width;
        java.util.Collection<org.bukkit.block.Block> blocks = new java.util.HashSet<org.bukkit.block.Block>(); // CraftBukkit - add field

        public Shape(World world, BlockPosition blockposition, EnumDirection.EnumAxis enumdirection_enumaxis) {
            this.a = world;
            this.b = enumdirection_enumaxis;
            if (enumdirection_enumaxis == EnumDirection.EnumAxis.X) {
                this.d = EnumDirection.EAST;
                this.c = EnumDirection.WEST;
            } else {
                this.d = EnumDirection.NORTH;
                this.c = EnumDirection.SOUTH;
            }

            for (BlockPosition blockposition1 = blockposition; blockposition.getY() > blockposition1.getY() - 21 && blockposition.getY() > 0 && this.a(world.getType(blockposition.down()).getBlock()); blockposition = blockposition.down()) {
                ;
            }

            int i = this.a(blockposition, this.d) - 1;

            if (i >= 0) {
                this.position = blockposition.shift(this.d, i);
                this.width = this.a(this.position, this.c);
                if (this.width < 2 || this.width > 21) {
                    this.position = null;
                    this.width = 0;
                }
            }

            if (this.position != null) {
                this.height = this.c();
            }

        }

        protected int a(BlockPosition blockposition, EnumDirection enumdirection) {
            int i;

            for (i = 0; i < 22; ++i) {
                BlockPosition blockposition1 = blockposition.shift(enumdirection, i);

                if (!this.a(this.a.getType(blockposition1).getBlock()) || this.a.getType(blockposition1.down()).getBlock() != Blocks.OBSIDIAN) {
                    break;
                }
            }

            Block block = this.a.getType(blockposition.shift(enumdirection, i)).getBlock();

            return block == Blocks.OBSIDIAN ? i : 0;
        }

        public int a() {
            return this.height;
        }

        public int b() {
            return this.width;
        }

        protected int c() {
            // CraftBukkit start
            this.blocks.clear();
            org.bukkit.World bworld = this.a.getWorld();
            // CraftBukkit end
            int i;

            label56:
            for (this.height = 0; this.height < 21; ++this.height) {
                for (i = 0; i < this.width; ++i) {
                    BlockPosition blockposition = this.position.shift(this.c, i).up(this.height);
                    Block block = this.a.getType(blockposition).getBlock();

                    if (!this.a(block)) {
                        break label56;
                    }

                    if (block == Blocks.PORTAL) {
                        ++this.e;
                    }

                    if (i == 0) {
                        block = this.a.getType(blockposition.shift(this.d)).getBlock();
                        if (block != Blocks.OBSIDIAN) {
                            break label56;
                            // CraftBukkit start - add the block to our list
                        } else {
                            BlockPosition pos = blockposition.shift(this.d);
                            blocks.add(bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
                            // CraftBukkit end
                        }
                    } else if (i == this.width - 1) {
                        block = this.a.getType(blockposition.shift(this.c)).getBlock();
                        if (block != Blocks.OBSIDIAN) {
                            break label56;
                            // CraftBukkit start - add the block to our list
                        } else {
                            BlockPosition pos = blockposition.shift(this.c);
                            blocks.add(bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
                            // CraftBukkit end
                        }
                    }
                }
            }

            for (i = 0; i < this.width; ++i) {
                if (this.a.getType(this.position.shift(this.c, i).up(this.height)).getBlock() != Blocks.OBSIDIAN) {
                    this.height = 0;
                    break;
                    // CraftBukkit start - add the block to our list
                } else {
                    BlockPosition pos = this.position.shift(this.c, i).up(this.height);
                    blocks.add(bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
                    // CraftBukkit end
                }
            }

            if (this.height <= 21 && this.height >= 3) {
                return this.height;
            } else {
                this.position = null;
                this.width = 0;
                this.height = 0;
                return 0;
            }
        }

        protected boolean a(Block block) {
            return block.material == Material.AIR || block == Blocks.FIRE || block == Blocks.PORTAL;
        }

        public boolean d() {
            return this.position != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
        }

        // CraftBukkit start - return boolean
        public boolean createPortal() {
            org.bukkit.World bworld = this.a.getWorld();

            // Copy below for loop
            for (int i = 0; i < this.width; ++i) {
                BlockPosition blockposition = this.position.shift(this.c, i);

                for (int j = 0; j < this.height; ++j) {
                    BlockPosition pos = blockposition.up(j);
                    blocks.add(bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
                }
            }

            PortalCreateEvent event = new PortalCreateEvent(blocks, bworld, PortalCreateEvent.CreateReason.FIRE);
            this.a.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }
            // CraftBukkit end
            for (int i = 0; i < this.width; ++i) {
                BlockPosition blockposition = this.position.shift(this.c, i);

                for (int j = 0; j < this.height; ++j) {
                    this.a.setTypeAndData(blockposition.up(j), Blocks.PORTAL.getBlockData().set(BlockPortal.AXIS, this.b), 2);
                }
            }

            return true; // CraftBukkit
        }
    }
}
