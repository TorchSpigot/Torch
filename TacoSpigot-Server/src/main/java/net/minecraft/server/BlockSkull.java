package net.minecraft.server;

import com.google.common.base.Predicate;
import java.util.Iterator;
import java.util.Random;
import javax.annotation.Nullable;

// CraftBukkit start
import org.bukkit.craftbukkit.util.BlockStateListPopulator;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
// CraftBukkit end

public class BlockSkull extends BlockTileEntity {

    public static final BlockStateDirection FACING = BlockDirectional.FACING;
    public static final BlockStateBoolean NODROP = BlockStateBoolean.of("nodrop");
    private static final Predicate<ShapeDetectorBlock> B = new Predicate() {
        public boolean a(@Nullable ShapeDetectorBlock shapedetectorblock) {
            return shapedetectorblock.a() != null && shapedetectorblock.a().getBlock() == Blocks.SKULL && shapedetectorblock.b() instanceof TileEntitySkull && ((TileEntitySkull) shapedetectorblock.b()).getSkullType() == 1;
        }

        public boolean apply(Object object) {
            return this.a((ShapeDetectorBlock) object);
        }
    };
    protected static final AxisAlignedBB c = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.5D, 0.75D);
    protected static final AxisAlignedBB d = new AxisAlignedBB(0.25D, 0.25D, 0.5D, 0.75D, 0.75D, 1.0D);
    protected static final AxisAlignedBB e = new AxisAlignedBB(0.25D, 0.25D, 0.0D, 0.75D, 0.75D, 0.5D);
    protected static final AxisAlignedBB f = new AxisAlignedBB(0.5D, 0.25D, 0.25D, 1.0D, 0.75D, 0.75D);
    protected static final AxisAlignedBB g = new AxisAlignedBB(0.0D, 0.25D, 0.25D, 0.5D, 0.75D, 0.75D);
    private ShapeDetector C;
    private ShapeDetector D;

    protected BlockSkull() {
        super(Material.ORIENTABLE);
        this.w(this.blockStateList.getBlockData().set(BlockSkull.FACING, EnumDirection.NORTH).set(BlockSkull.NODROP, Boolean.valueOf(false)));
    }

    public String getName() {
        return LocaleI18n.get("tile.skull.skeleton.name");
    }

    public boolean b(IBlockData iblockdata) {
        return false;
    }

    public boolean c(IBlockData iblockdata) {
        return false;
    }

    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        switch (BlockSkull.SyntheticClass_1.a[((EnumDirection) iblockdata.get(BlockSkull.FACING)).ordinal()]) {
        case 1:
        default:
            return BlockSkull.c;

        case 2:
            return BlockSkull.d;

        case 3:
            return BlockSkull.e;

        case 4:
            return BlockSkull.f;

        case 5:
            return BlockSkull.g;
        }
    }

    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        return this.getBlockData().set(BlockSkull.FACING, entityliving.getDirection()).set(BlockSkull.NODROP, Boolean.valueOf(false));
    }

    public TileEntity a(World world, int i) {
        return new TileEntitySkull();
    }

    public ItemStack a(World world, BlockPosition blockposition, IBlockData iblockdata) {
        int i = 0;
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity instanceof TileEntitySkull) {
            i = ((TileEntitySkull) tileentity).getSkullType();
        }

        return new ItemStack(Items.SKULL, 1, i);
    }

    // CraftBukkit start - Special case dropping so we can get info from the tile entity
    @Override
    public void dropNaturally(World world, BlockPosition blockposition, IBlockData iblockdata, float f, int i) {
        if (world.random.nextFloat() < f) {
            TileEntitySkull tileentityskull = (TileEntitySkull) world.getTileEntity(blockposition);
            ItemStack itemstack = this.a(world, blockposition, iblockdata);

            if (tileentityskull.getSkullType() == 3 && tileentityskull.getGameProfile() != null) {
                itemstack.setTag(new NBTTagCompound());
                NBTTagCompound nbttagcompound = new NBTTagCompound();

                GameProfileSerializer.serialize(nbttagcompound, tileentityskull.getGameProfile());
                itemstack.getTag().set("SkullOwner", nbttagcompound);
            }

            a(world, blockposition, itemstack);
        }
    }
    // CraftBukkit end

    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        if (entityhuman.abilities.canInstantlyBuild) {
            iblockdata = iblockdata.set(BlockSkull.NODROP, Boolean.valueOf(true));
            world.setTypeAndData(blockposition, iblockdata, 4);
        }

        super.a(world, blockposition, iblockdata, entityhuman);
    }

    public void remove(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (!world.isClientSide) {
            // CraftBukkit start - Drop item in code above, not here
            // if (!((Boolean) iblockdata.get(BlockSkull.NODROP)).booleanValue()) {
            if (false) {
                // CraftBukkit end
                TileEntity tileentity = world.getTileEntity(blockposition, true); // Paper - This is being removed, don't fix

                if (tileentity instanceof TileEntitySkull) {
                    TileEntitySkull tileentityskull = (TileEntitySkull) tileentity;
                    ItemStack itemstack = this.a(world, blockposition, iblockdata);

                    if (tileentityskull.getSkullType() == 3 && tileentityskull.getGameProfile() != null) {
                        itemstack.setTag(new NBTTagCompound());
                        NBTTagCompound nbttagcompound = new NBTTagCompound();

                        GameProfileSerializer.serialize(nbttagcompound, tileentityskull.getGameProfile());
                        itemstack.getTag().set("SkullOwner", nbttagcompound);
                    }

                    a(world, blockposition, itemstack);
                }
            }

            super.remove(world, blockposition, iblockdata);
        }
    }

    @Nullable
    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return Items.SKULL;
    }

    public boolean b(World world, BlockPosition blockposition, ItemStack itemstack) {
        return itemstack.getData() == 1 && blockposition.getY() >= 2 && world.getDifficulty() != EnumDifficulty.PEACEFUL && !world.isClientSide ? this.e().a(world, blockposition) != null : false;
    }

    public void a(World world, BlockPosition blockposition, TileEntitySkull tileentityskull) {
        if (world.captureBlockStates) return; // CraftBukkit
        if (tileentityskull.getSkullType() == 1 && blockposition.getY() >= 2 && world.getDifficulty() != EnumDifficulty.PEACEFUL && !world.isClientSide) {
            ShapeDetector shapedetector = this.g();
            ShapeDetector.ShapeDetectorCollection shapedetector_shapedetectorcollection = shapedetector.a(world, blockposition);

            if (shapedetector_shapedetectorcollection != null) {
                // CraftBukkit start - Use BlockStateListPopulator
                BlockStateListPopulator blockList = new BlockStateListPopulator(world.getWorld());
                int i;

                for (i = 0; i < 3; ++i) {
                    ShapeDetectorBlock shapedetectorblock = shapedetector_shapedetectorcollection.a(i, 0, 0);

                    // CraftBukkit start
                    // world.setTypeAndData(shapedetectorblock.getPosition(), shapedetectorblock.a().set(BlockSkull.NODROP, Boolean.valueOf(true)), 2);
                    BlockPosition pos = shapedetectorblock.getPosition();
                    IBlockData data = shapedetectorblock.a().set(BlockSkull.NODROP, Boolean.valueOf(true));
                    blockList.setTypeAndData(pos.getX(), pos.getY(), pos.getZ(), data.getBlock(), data.getBlock().toLegacyData(data), 2);
                    // CraftBukkit end
                }

                for (i = 0; i < shapedetector.c(); ++i) {
                    for (int j = 0; j < shapedetector.b(); ++j) {
                        ShapeDetectorBlock shapedetectorblock1 = shapedetector_shapedetectorcollection.a(i, j, 0);

                        // CraftBukkit start
                        // world.setTypeAndData(shapedetectorblock1.getPosition(), Blocks.AIR.getBlockData(), 2);
                        BlockPosition pos = shapedetectorblock1.getPosition();
                        blockList.setTypeAndData(pos.getX(), pos.getY(), pos.getZ(), Blocks.AIR, 0, 2);
                        // CraftBukkit end
                    }
                }

                BlockPosition blockposition1 = shapedetector_shapedetectorcollection.a(1, 0, 0).getPosition();
                EntityWither entitywither = new EntityWither(world);
                BlockPosition blockposition2 = shapedetector_shapedetectorcollection.a(1, 2, 0).getPosition();

                entitywither.setPositionRotation((double) blockposition2.getX() + 0.5D, (double) blockposition2.getY() + 0.55D, (double) blockposition2.getZ() + 0.5D, shapedetector_shapedetectorcollection.getFacing().k() == EnumDirection.EnumAxis.X ? 0.0F : 90.0F, 0.0F);
                entitywither.aN = shapedetector_shapedetectorcollection.getFacing().k() == EnumDirection.EnumAxis.X ? 0.0F : 90.0F;
                entitywither.o();
                Iterator iterator = world.a(EntityHuman.class, entitywither.getBoundingBox().g(50.0D)).iterator();

                // CraftBukkit start
                if (world.addEntity(entitywither, SpawnReason.BUILD_WITHER)) {
                    blockList.updateList();

                while (iterator.hasNext()) {
                    EntityHuman entityhuman = (EntityHuman) iterator.next();

                    entityhuman.b((Statistic) AchievementList.I);
                }

                int k;

                for (k = 0; k < 120; ++k) {
                    world.addParticle(EnumParticle.SNOWBALL, (double) blockposition1.getX() + world.random.nextDouble(), (double) (blockposition1.getY() - 2) + world.random.nextDouble() * 3.9D, (double) blockposition1.getZ() + world.random.nextDouble(), 0.0D, 0.0D, 0.0D, new int[0]);
                }

                for (k = 0; k < shapedetector.c(); ++k) {
                    for (int l = 0; l < shapedetector.b(); ++l) {
                        ShapeDetectorBlock shapedetectorblock2 = shapedetector_shapedetectorcollection.a(k, l, 0);

                        world.update(shapedetectorblock2.getPosition(), Blocks.AIR);
                    }
                }
                } // CraftBukkit end

            }
        }
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockSkull.FACING, EnumDirection.fromType1(i & 7)).set(BlockSkull.NODROP, Boolean.valueOf((i & 8) > 0));
    }

    public int toLegacyData(IBlockData iblockdata) {
        byte b0 = 0;
        int i = b0 | ((EnumDirection) iblockdata.get(BlockSkull.FACING)).a();

        if (((Boolean) iblockdata.get(BlockSkull.NODROP)).booleanValue()) {
            i |= 8;
        }

        return i;
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return iblockdata.set(BlockSkull.FACING, enumblockrotation.a((EnumDirection) iblockdata.get(BlockSkull.FACING)));
    }

    public IBlockData a(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.a(enumblockmirror.a((EnumDirection) iblockdata.get(BlockSkull.FACING)));
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockSkull.FACING, BlockSkull.NODROP});
    }

    protected ShapeDetector e() {
        if (this.C == null) {
            this.C = ShapeDetectorBuilder.a().a(new String[] { "   ", "###", "~#~"}).a('#', ShapeDetectorBlock.a((Predicate) BlockStatePredicate.a(Blocks.SOUL_SAND))).a('~', ShapeDetectorBlock.a((Predicate) BlockStatePredicate.a(Blocks.AIR))).b();
        }

        return this.C;
    }

    protected ShapeDetector g() {
        if (this.D == null) {
            this.D = ShapeDetectorBuilder.a().a(new String[] { "^^^", "###", "~#~"}).a('#', ShapeDetectorBlock.a((Predicate) BlockStatePredicate.a(Blocks.SOUL_SAND))).a('^', BlockSkull.B).a('~', ShapeDetectorBlock.a((Predicate) BlockStatePredicate.a(Blocks.AIR))).b();
        }

        return this.D;
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumDirection.values().length];

        static {
            try {
                BlockSkull.SyntheticClass_1.a[EnumDirection.UP.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                BlockSkull.SyntheticClass_1.a[EnumDirection.NORTH.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                BlockSkull.SyntheticClass_1.a[EnumDirection.SOUTH.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                BlockSkull.SyntheticClass_1.a[EnumDirection.WEST.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                BlockSkull.SyntheticClass_1.a[EnumDirection.EAST.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

        }
    }
}
