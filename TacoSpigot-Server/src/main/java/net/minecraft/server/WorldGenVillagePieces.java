package net.minecraft.server;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class WorldGenVillagePieces {

    public static void a() {
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageLibrary.class, "ViBH");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageFarm2.class, "ViDF");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageFarm.class, "ViF");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageLight.class, "ViL");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageButcher.class, "ViPH");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageHouse.class, "ViSH");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageHut.class, "ViSmH");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageTemple.class, "ViST");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageBlacksmith.class, "ViS");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageStartPiece.class, "ViStart");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageRoad.class, "ViSR");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageHouse2.class, "ViTRH");
        WorldGenFactory.a(WorldGenVillagePieces.WorldGenVillageWell.class, "ViW");
    }

    public static List<WorldGenVillagePieces.WorldGenVillagePieceWeight> a(Random random, int i) {
        ArrayList arraylist = Lists.newArrayList();

        arraylist.add(new WorldGenVillagePieces.WorldGenVillagePieceWeight(WorldGenVillagePieces.WorldGenVillageHouse.class, 4, MathHelper.nextInt(random, 2 + i, 4 + i * 2)));
        arraylist.add(new WorldGenVillagePieces.WorldGenVillagePieceWeight(WorldGenVillagePieces.WorldGenVillageTemple.class, 20, MathHelper.nextInt(random, 0 + i, 1 + i)));
        arraylist.add(new WorldGenVillagePieces.WorldGenVillagePieceWeight(WorldGenVillagePieces.WorldGenVillageLibrary.class, 20, MathHelper.nextInt(random, 0 + i, 2 + i)));
        arraylist.add(new WorldGenVillagePieces.WorldGenVillagePieceWeight(WorldGenVillagePieces.WorldGenVillageHut.class, 3, MathHelper.nextInt(random, 2 + i, 5 + i * 3)));
        arraylist.add(new WorldGenVillagePieces.WorldGenVillagePieceWeight(WorldGenVillagePieces.WorldGenVillageButcher.class, 15, MathHelper.nextInt(random, 0 + i, 2 + i)));
        arraylist.add(new WorldGenVillagePieces.WorldGenVillagePieceWeight(WorldGenVillagePieces.WorldGenVillageFarm2.class, 3, MathHelper.nextInt(random, 1 + i, 4 + i)));
        arraylist.add(new WorldGenVillagePieces.WorldGenVillagePieceWeight(WorldGenVillagePieces.WorldGenVillageFarm.class, 3, MathHelper.nextInt(random, 2 + i, 4 + i * 2)));
        arraylist.add(new WorldGenVillagePieces.WorldGenVillagePieceWeight(WorldGenVillagePieces.WorldGenVillageBlacksmith.class, 15, MathHelper.nextInt(random, 0, 1 + i)));
        arraylist.add(new WorldGenVillagePieces.WorldGenVillagePieceWeight(WorldGenVillagePieces.WorldGenVillageHouse2.class, 8, MathHelper.nextInt(random, 0 + i, 3 + i * 2)));
        Iterator iterator = arraylist.iterator();

        while (iterator.hasNext()) {
            if (((WorldGenVillagePieces.WorldGenVillagePieceWeight) iterator.next()).d == 0) {
                iterator.remove();
            }
        }

        return arraylist;
    }

    private static int a(List<WorldGenVillagePieces.WorldGenVillagePieceWeight> list) {
        boolean flag = false;
        int i = 0;

        WorldGenVillagePieces.WorldGenVillagePieceWeight worldgenvillagepieces_worldgenvillagepieceweight;

        for (Iterator iterator = list.iterator(); iterator.hasNext(); i += worldgenvillagepieces_worldgenvillagepieceweight.b) {
            worldgenvillagepieces_worldgenvillagepieceweight = (WorldGenVillagePieces.WorldGenVillagePieceWeight) iterator.next();
            if (worldgenvillagepieces_worldgenvillagepieceweight.d > 0 && worldgenvillagepieces_worldgenvillagepieceweight.c < worldgenvillagepieces_worldgenvillagepieceweight.d) {
                flag = true;
            }
        }

        return flag ? i : -1;
    }

    private static WorldGenVillagePieces.WorldGenVillagePiece a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, WorldGenVillagePieces.WorldGenVillagePieceWeight worldgenvillagepieces_worldgenvillagepieceweight, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
        Class oclass = worldgenvillagepieces_worldgenvillagepieceweight.a;
        Object object = null;

        if (oclass == WorldGenVillagePieces.WorldGenVillageHouse.class) {
            object = WorldGenVillagePieces.WorldGenVillageHouse.a(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection, l);
        } else if (oclass == WorldGenVillagePieces.WorldGenVillageTemple.class) {
            object = WorldGenVillagePieces.WorldGenVillageTemple.a(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection, l);
        } else if (oclass == WorldGenVillagePieces.WorldGenVillageLibrary.class) {
            object = WorldGenVillagePieces.WorldGenVillageLibrary.a(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection, l);
        } else if (oclass == WorldGenVillagePieces.WorldGenVillageHut.class) {
            object = WorldGenVillagePieces.WorldGenVillageHut.a(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection, l);
        } else if (oclass == WorldGenVillagePieces.WorldGenVillageButcher.class) {
            object = WorldGenVillagePieces.WorldGenVillageButcher.a(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection, l);
        } else if (oclass == WorldGenVillagePieces.WorldGenVillageFarm2.class) {
            object = WorldGenVillagePieces.WorldGenVillageFarm2.a(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection, l);
        } else if (oclass == WorldGenVillagePieces.WorldGenVillageFarm.class) {
            object = WorldGenVillagePieces.WorldGenVillageFarm.a(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection, l);
        } else if (oclass == WorldGenVillagePieces.WorldGenVillageBlacksmith.class) {
            object = WorldGenVillagePieces.WorldGenVillageBlacksmith.a(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection, l);
        } else if (oclass == WorldGenVillagePieces.WorldGenVillageHouse2.class) {
            object = WorldGenVillagePieces.WorldGenVillageHouse2.a(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection, l);
        }

        return (WorldGenVillagePieces.WorldGenVillagePiece) object;
    }

    private static WorldGenVillagePieces.WorldGenVillagePiece c(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
        int i1 = a(worldgenvillagepieces_worldgenvillagestartpiece.e);

        if (i1 <= 0) {
            return null;
        } else {
            int j1 = 0;

            while (j1 < 5) {
                ++j1;
                int k1 = random.nextInt(i1);
                Iterator iterator = worldgenvillagepieces_worldgenvillagestartpiece.e.iterator();

                while (iterator.hasNext()) {
                    WorldGenVillagePieces.WorldGenVillagePieceWeight worldgenvillagepieces_worldgenvillagepieceweight = (WorldGenVillagePieces.WorldGenVillagePieceWeight) iterator.next();

                    k1 -= worldgenvillagepieces_worldgenvillagepieceweight.b;
                    if (k1 < 0) {
                        if (!worldgenvillagepieces_worldgenvillagepieceweight.a(l) || worldgenvillagepieces_worldgenvillagepieceweight == worldgenvillagepieces_worldgenvillagestartpiece.d && worldgenvillagepieces_worldgenvillagestartpiece.e.size() > 1) {
                            break;
                        }

                        WorldGenVillagePieces.WorldGenVillagePiece worldgenvillagepieces_worldgenvillagepiece = a(worldgenvillagepieces_worldgenvillagestartpiece, worldgenvillagepieces_worldgenvillagepieceweight, list, random, i, j, k, enumdirection, l);

                        if (worldgenvillagepieces_worldgenvillagepiece != null) {
                            ++worldgenvillagepieces_worldgenvillagepieceweight.c;
                            worldgenvillagepieces_worldgenvillagestartpiece.d = worldgenvillagepieces_worldgenvillagepieceweight;
                            if (!worldgenvillagepieces_worldgenvillagepieceweight.a()) {
                                worldgenvillagepieces_worldgenvillagestartpiece.e.remove(worldgenvillagepieces_worldgenvillagepieceweight);
                            }

                            return worldgenvillagepieces_worldgenvillagepiece;
                        }
                    }
                }
            }

            StructureBoundingBox structureboundingbox = WorldGenVillagePieces.WorldGenVillageLight.a(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection);

            if (structureboundingbox != null) {
                return new WorldGenVillagePieces.WorldGenVillageLight(worldgenvillagepieces_worldgenvillagestartpiece, l, random, structureboundingbox, enumdirection);
            } else {
                return null;
            }
        }
    }

    private static StructurePiece d(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
        if (l > 50) {
            return null;
        } else if (Math.abs(i - worldgenvillagepieces_worldgenvillagestartpiece.c().a) <= 112 && Math.abs(k - worldgenvillagepieces_worldgenvillagestartpiece.c().c) <= 112) {
            WorldGenVillagePieces.WorldGenVillagePiece worldgenvillagepieces_worldgenvillagepiece = c(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection, l + 1);

            if (worldgenvillagepieces_worldgenvillagepiece != null) {
                int i1 = (worldgenvillagepieces_worldgenvillagepiece.l.a + worldgenvillagepieces_worldgenvillagepiece.l.d) / 2;
                int j1 = (worldgenvillagepieces_worldgenvillagepiece.l.c + worldgenvillagepieces_worldgenvillagepiece.l.f) / 2;
                int k1 = worldgenvillagepieces_worldgenvillagepiece.l.d - worldgenvillagepieces_worldgenvillagepiece.l.a;
                int l1 = worldgenvillagepieces_worldgenvillagepiece.l.f - worldgenvillagepieces_worldgenvillagepiece.l.c;
                int i2 = k1 > l1 ? k1 : l1;

                if (worldgenvillagepieces_worldgenvillagestartpiece.h().a(i1, j1, i2 / 2 + 4, WorldGenVillage.a)) {
                    list.add(worldgenvillagepieces_worldgenvillagepiece);
                    worldgenvillagepieces_worldgenvillagestartpiece.f.add(worldgenvillagepieces_worldgenvillagepiece);
                    return worldgenvillagepieces_worldgenvillagepiece;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    private static StructurePiece e(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
        if (l > 3 + worldgenvillagepieces_worldgenvillagestartpiece.c) {
            return null;
        } else if (Math.abs(i - worldgenvillagepieces_worldgenvillagestartpiece.c().a) <= 112 && Math.abs(k - worldgenvillagepieces_worldgenvillagestartpiece.c().c) <= 112) {
            StructureBoundingBox structureboundingbox = WorldGenVillagePieces.WorldGenVillageRoad.a(worldgenvillagepieces_worldgenvillagestartpiece, list, random, i, j, k, enumdirection);

            if (structureboundingbox != null && structureboundingbox.b > 10) {
                WorldGenVillagePieces.WorldGenVillageRoad worldgenvillagepieces_worldgenvillageroad = new WorldGenVillagePieces.WorldGenVillageRoad(worldgenvillagepieces_worldgenvillagestartpiece, l, random, structureboundingbox, enumdirection);
                int i1 = (worldgenvillagepieces_worldgenvillageroad.l.a + worldgenvillagepieces_worldgenvillageroad.l.d) / 2;
                int j1 = (worldgenvillagepieces_worldgenvillageroad.l.c + worldgenvillagepieces_worldgenvillageroad.l.f) / 2;
                int k1 = worldgenvillagepieces_worldgenvillageroad.l.d - worldgenvillagepieces_worldgenvillageroad.l.a;
                int l1 = worldgenvillagepieces_worldgenvillageroad.l.f - worldgenvillagepieces_worldgenvillageroad.l.c;
                int i2 = k1 > l1 ? k1 : l1;

                if (worldgenvillagepieces_worldgenvillagestartpiece.h().a(i1, j1, i2 / 2 + 4, WorldGenVillage.a)) {
                    list.add(worldgenvillagepieces_worldgenvillageroad);
                    worldgenvillagepieces_worldgenvillagestartpiece.g.add(worldgenvillagepieces_worldgenvillageroad);
                    return worldgenvillagepieces_worldgenvillageroad;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumDirection.values().length];

        static {
            try {
                WorldGenVillagePieces.SyntheticClass_1.a[EnumDirection.NORTH.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                WorldGenVillagePieces.SyntheticClass_1.a[EnumDirection.SOUTH.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                WorldGenVillagePieces.SyntheticClass_1.a[EnumDirection.WEST.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                WorldGenVillagePieces.SyntheticClass_1.a[EnumDirection.EAST.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

        }
    }

    public static class WorldGenVillageLight extends WorldGenVillagePieces.WorldGenVillagePiece {

        public WorldGenVillageLight() {}

        public WorldGenVillageLight(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, StructureBoundingBox structureboundingbox, EnumDirection enumdirection) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(enumdirection);
            this.l = structureboundingbox;
        }

        public static StructureBoundingBox a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.a(i, j, k, 0, 0, 0, 3, 4, 2, enumdirection);

            return StructurePiece.a(list, structureboundingbox) != null ? null : structureboundingbox;
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            if (this.h < 0) {
                this.h = this.b(world, structureboundingbox);
                if (this.h < 0) {
                    return true;
                }

                this.l.a(0, this.h - this.l.e + 4 - 1, 0);
            }

            this.a(world, structureboundingbox, 0, 0, 0, 2, 3, 1, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, Blocks.FENCE.getBlockData(), 1, 0, 0, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 1, 1, 0, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 1, 2, 0, structureboundingbox);
            this.a(world, Blocks.WOOL.fromLegacyData(EnumColor.WHITE.getInvColorIndex()), 1, 3, 0, structureboundingbox);
            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.EAST), 2, 3, 0, structureboundingbox);
            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.NORTH), 1, 3, 1, structureboundingbox);
            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.WEST), 0, 3, 0, structureboundingbox);
            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.SOUTH), 1, 3, -1, structureboundingbox);
            return true;
        }
    }

    public static class WorldGenVillageFarm2 extends WorldGenVillagePieces.WorldGenVillagePiece {

        private Block a;
        private Block b;
        private Block c;
        private Block d;

        public WorldGenVillageFarm2() {}

        public WorldGenVillageFarm2(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, StructureBoundingBox structureboundingbox, EnumDirection enumdirection) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(enumdirection);
            this.l = structureboundingbox;
            this.a = this.a(random);
            this.b = this.a(random);
            this.c = this.a(random);
            this.d = this.a(random);
        }

        protected void a(NBTTagCompound nbttagcompound) {
            super.a(nbttagcompound);
            nbttagcompound.setInt("CA", Block.REGISTRY.a(this.a));
            nbttagcompound.setInt("CB", Block.REGISTRY.a(this.b));
            nbttagcompound.setInt("CC", Block.REGISTRY.a(this.c));
            nbttagcompound.setInt("CD", Block.REGISTRY.a(this.d));
        }

        protected void b(NBTTagCompound nbttagcompound) {
            super.b(nbttagcompound);
            this.a = Block.getById(nbttagcompound.getInt("CA"));
            this.b = Block.getById(nbttagcompound.getInt("CB"));
            this.c = Block.getById(nbttagcompound.getInt("CC"));
            this.d = Block.getById(nbttagcompound.getInt("CD"));
            if (!(this.a instanceof BlockCrops)) {
                this.a = Blocks.WHEAT;
            }

            if (!(this.b instanceof BlockCrops)) {
                this.b = Blocks.CARROTS;
            }

            if (!(this.c instanceof BlockCrops)) {
                this.c = Blocks.POTATOES;
            }

            if (!(this.d instanceof BlockCrops)) {
                this.d = Blocks.BEETROOT;
            }

        }

        private Block a(Random random) {
            switch (random.nextInt(10)) {
            case 0:
            case 1:
                return Blocks.CARROTS;

            case 2:
            case 3:
                return Blocks.POTATOES;

            case 4:
                return Blocks.BEETROOT;

            default:
                return Blocks.WHEAT;
            }
        }

        public static WorldGenVillagePieces.WorldGenVillageFarm2 a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.a(i, j, k, 0, 0, 0, 13, 4, 9, enumdirection);

            return a(structureboundingbox) && StructurePiece.a(list, structureboundingbox) == null ? new WorldGenVillagePieces.WorldGenVillageFarm2(worldgenvillagepieces_worldgenvillagestartpiece, l, random, structureboundingbox, enumdirection) : null;
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            if (this.h < 0) {
                this.h = this.b(world, structureboundingbox);
                if (this.h < 0) {
                    return true;
                }

                this.l.a(0, this.h - this.l.e + 4 - 1, 0);
            }

            this.a(world, structureboundingbox, 0, 1, 0, 12, 4, 8, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 1, 2, 0, 7, Blocks.FARMLAND.getBlockData(), Blocks.FARMLAND.getBlockData(), false);
            this.a(world, structureboundingbox, 4, 0, 1, 5, 0, 7, Blocks.FARMLAND.getBlockData(), Blocks.FARMLAND.getBlockData(), false);
            this.a(world, structureboundingbox, 7, 0, 1, 8, 0, 7, Blocks.FARMLAND.getBlockData(), Blocks.FARMLAND.getBlockData(), false);
            this.a(world, structureboundingbox, 10, 0, 1, 11, 0, 7, Blocks.FARMLAND.getBlockData(), Blocks.FARMLAND.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 0, 0, 0, 0, 8, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 6, 0, 0, 6, 0, 8, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 12, 0, 0, 12, 0, 8, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 0, 11, 0, 0, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 8, 11, 0, 8, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 3, 0, 1, 3, 0, 7, Blocks.WATER.getBlockData(), Blocks.WATER.getBlockData(), false);
            this.a(world, structureboundingbox, 9, 0, 1, 9, 0, 7, Blocks.WATER.getBlockData(), Blocks.WATER.getBlockData(), false);

            int i;
            int j;

            for (i = 1; i <= 7; ++i) {
                j = ((BlockCrops) this.a).g();
                int k = j / 3;

                this.a(world, this.a.fromLegacyData(MathHelper.nextInt(random, k, j)), 1, 1, i, structureboundingbox);
                this.a(world, this.a.fromLegacyData(MathHelper.nextInt(random, k, j)), 2, 1, i, structureboundingbox);
                int l = ((BlockCrops) this.b).g();
                int i1 = l / 3;

                this.a(world, this.b.fromLegacyData(MathHelper.nextInt(random, i1, l)), 4, 1, i, structureboundingbox);
                this.a(world, this.b.fromLegacyData(MathHelper.nextInt(random, i1, l)), 5, 1, i, structureboundingbox);
                int j1 = ((BlockCrops) this.c).g();
                int k1 = j1 / 3;

                this.a(world, this.c.fromLegacyData(MathHelper.nextInt(random, k1, j1)), 7, 1, i, structureboundingbox);
                this.a(world, this.c.fromLegacyData(MathHelper.nextInt(random, k1, j1)), 8, 1, i, structureboundingbox);
                int l1 = ((BlockCrops) this.d).g();
                int i2 = l1 / 3;

                this.a(world, this.d.fromLegacyData(MathHelper.nextInt(random, i2, l1)), 10, 1, i, structureboundingbox);
                this.a(world, this.d.fromLegacyData(MathHelper.nextInt(random, i2, l1)), 11, 1, i, structureboundingbox);
            }

            for (i = 0; i < 9; ++i) {
                for (j = 0; j < 13; ++j) {
                    this.b(world, j, 4, i, structureboundingbox);
                    this.b(world, Blocks.DIRT.getBlockData(), j, -1, i, structureboundingbox);
                }
            }

            return true;
        }
    }

    public static class WorldGenVillageFarm extends WorldGenVillagePieces.WorldGenVillagePiece {

        private Block a;
        private Block b;

        public WorldGenVillageFarm() {}

        public WorldGenVillageFarm(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, StructureBoundingBox structureboundingbox, EnumDirection enumdirection) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(enumdirection);
            this.l = structureboundingbox;
            this.a = this.a(random);
            this.b = this.a(random);
        }

        protected void a(NBTTagCompound nbttagcompound) {
            super.a(nbttagcompound);
            nbttagcompound.setInt("CA", Block.REGISTRY.a(this.a));
            nbttagcompound.setInt("CB", Block.REGISTRY.a(this.b));
        }

        protected void b(NBTTagCompound nbttagcompound) {
            super.b(nbttagcompound);
            this.a = Block.getById(nbttagcompound.getInt("CA"));
            this.b = Block.getById(nbttagcompound.getInt("CB"));
        }

        private Block a(Random random) {
            switch (random.nextInt(10)) {
            case 0:
            case 1:
                return Blocks.CARROTS;

            case 2:
            case 3:
                return Blocks.POTATOES;

            case 4:
                return Blocks.BEETROOT;

            default:
                return Blocks.WHEAT;
            }
        }

        public static WorldGenVillagePieces.WorldGenVillageFarm a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.a(i, j, k, 0, 0, 0, 7, 4, 9, enumdirection);

            return a(structureboundingbox) && StructurePiece.a(list, structureboundingbox) == null ? new WorldGenVillagePieces.WorldGenVillageFarm(worldgenvillagepieces_worldgenvillagestartpiece, l, random, structureboundingbox, enumdirection) : null;
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            if (this.h < 0) {
                this.h = this.b(world, structureboundingbox);
                if (this.h < 0) {
                    return true;
                }

                this.l.a(0, this.h - this.l.e + 4 - 1, 0);
            }

            this.a(world, structureboundingbox, 0, 1, 0, 6, 4, 8, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 1, 2, 0, 7, Blocks.FARMLAND.getBlockData(), Blocks.FARMLAND.getBlockData(), false);
            this.a(world, structureboundingbox, 4, 0, 1, 5, 0, 7, Blocks.FARMLAND.getBlockData(), Blocks.FARMLAND.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 0, 0, 0, 0, 8, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 6, 0, 0, 6, 0, 8, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 0, 5, 0, 0, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 8, 5, 0, 8, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 3, 0, 1, 3, 0, 7, Blocks.WATER.getBlockData(), Blocks.WATER.getBlockData(), false);

            int i;
            int j;

            for (i = 1; i <= 7; ++i) {
                j = ((BlockCrops) this.a).g();
                int k = j / 3;

                this.a(world, this.a.fromLegacyData(MathHelper.nextInt(random, k, j)), 1, 1, i, structureboundingbox);
                this.a(world, this.a.fromLegacyData(MathHelper.nextInt(random, k, j)), 2, 1, i, structureboundingbox);
                int l = ((BlockCrops) this.b).g();
                int i1 = l / 3;

                this.a(world, this.b.fromLegacyData(MathHelper.nextInt(random, i1, l)), 4, 1, i, structureboundingbox);
                this.a(world, this.b.fromLegacyData(MathHelper.nextInt(random, i1, l)), 5, 1, i, structureboundingbox);
            }

            for (i = 0; i < 9; ++i) {
                for (j = 0; j < 7; ++j) {
                    this.b(world, j, 4, i, structureboundingbox);
                    this.b(world, Blocks.DIRT.getBlockData(), j, -1, i, structureboundingbox);
                }
            }

            return true;
        }
    }

    public static class WorldGenVillageBlacksmith extends WorldGenVillagePieces.WorldGenVillagePiece {

        private boolean a;

        public WorldGenVillageBlacksmith() {}

        public WorldGenVillageBlacksmith(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, StructureBoundingBox structureboundingbox, EnumDirection enumdirection) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(enumdirection);
            this.l = structureboundingbox;
        }

        public static WorldGenVillagePieces.WorldGenVillageBlacksmith a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.a(i, j, k, 0, 0, 0, 10, 6, 7, enumdirection);

            return a(structureboundingbox) && StructurePiece.a(list, structureboundingbox) == null ? new WorldGenVillagePieces.WorldGenVillageBlacksmith(worldgenvillagepieces_worldgenvillagestartpiece, l, random, structureboundingbox, enumdirection) : null;
        }

        protected void a(NBTTagCompound nbttagcompound) {
            super.a(nbttagcompound);
            nbttagcompound.setBoolean("Chest", this.a);
        }

        protected void b(NBTTagCompound nbttagcompound) {
            super.b(nbttagcompound);
            this.a = nbttagcompound.getBoolean("Chest");
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            if (this.h < 0) {
                this.h = this.b(world, structureboundingbox);
                if (this.h < 0) {
                    return true;
                }

                this.l.a(0, this.h - this.l.e + 6 - 1, 0);
            }

            this.a(world, structureboundingbox, 0, 1, 0, 9, 4, 6, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 0, 0, 9, 0, 6, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 4, 0, 9, 4, 6, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 5, 0, 9, 5, 6, Blocks.STONE_SLAB.getBlockData(), Blocks.STONE_SLAB.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 5, 1, 8, 5, 5, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 1, 0, 2, 3, 0, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 1, 0, 0, 4, 0, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 3, 1, 0, 3, 4, 0, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 1, 6, 0, 4, 6, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, Blocks.PLANKS.getBlockData(), 3, 3, 1, structureboundingbox);
            this.a(world, structureboundingbox, 3, 1, 2, 3, 3, 2, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 4, 1, 3, 5, 3, 3, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 1, 1, 0, 3, 5, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 1, 6, 5, 3, 6, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 5, 1, 0, 5, 3, 0, Blocks.FENCE.getBlockData(), Blocks.FENCE.getBlockData(), false);
            this.a(world, structureboundingbox, 9, 1, 0, 9, 3, 0, Blocks.FENCE.getBlockData(), Blocks.FENCE.getBlockData(), false);
            this.a(world, structureboundingbox, 6, 1, 4, 9, 4, 6, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, Blocks.FLOWING_LAVA.getBlockData(), 7, 1, 5, structureboundingbox);
            this.a(world, Blocks.FLOWING_LAVA.getBlockData(), 8, 1, 5, structureboundingbox);
            this.a(world, Blocks.IRON_BARS.getBlockData(), 9, 2, 5, structureboundingbox);
            this.a(world, Blocks.IRON_BARS.getBlockData(), 9, 2, 4, structureboundingbox);
            this.a(world, structureboundingbox, 7, 2, 4, 8, 2, 5, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 6, 1, 3, structureboundingbox);
            this.a(world, Blocks.FURNACE.getBlockData(), 6, 2, 3, structureboundingbox);
            this.a(world, Blocks.FURNACE.getBlockData(), 6, 3, 3, structureboundingbox);
            this.a(world, Blocks.DOUBLE_STONE_SLAB.getBlockData(), 8, 1, 1, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 2, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 2, 4, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 2, 2, 6, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 4, 2, 6, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 2, 1, 4, structureboundingbox);
            this.a(world, Blocks.WOODEN_PRESSURE_PLATE.getBlockData(), 2, 2, 4, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 1, 1, 5, structureboundingbox);
            this.a(world, Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH), 2, 1, 5, structureboundingbox);
            this.a(world, Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.WEST), 1, 1, 4, structureboundingbox);
            if (!this.a && structureboundingbox.b((BaseBlockPosition) (new BlockPosition(this.a(5, 5), this.d(1), this.b(5, 5))))) {
                this.a = true;
                this.a(world, structureboundingbox, random, 5, 1, 5, LootTables.e);
            }

            int i;

            for (i = 6; i <= 8; ++i) {
                if (this.a(world, i, 0, -1, structureboundingbox).getMaterial() == Material.AIR && this.a(world, i, -1, -1, structureboundingbox).getMaterial() != Material.AIR) {
                    this.a(world, Blocks.STONE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH), i, 0, -1, structureboundingbox);
                }
            }

            for (i = 0; i < 7; ++i) {
                for (int j = 0; j < 10; ++j) {
                    this.b(world, j, 6, i, structureboundingbox);
                    this.b(world, Blocks.COBBLESTONE.getBlockData(), j, -1, i, structureboundingbox);
                }
            }

            this.a(world, structureboundingbox, 7, 1, 1, 1);
            return true;
        }

        protected int c(int i, int j) {
            return 3;
        }
    }

    public static class WorldGenVillageHouse2 extends WorldGenVillagePieces.WorldGenVillagePiece {

        public WorldGenVillageHouse2() {}

        public WorldGenVillageHouse2(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, StructureBoundingBox structureboundingbox, EnumDirection enumdirection) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(enumdirection);
            this.l = structureboundingbox;
        }

        public static WorldGenVillagePieces.WorldGenVillageHouse2 a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.a(i, j, k, 0, 0, 0, 9, 7, 12, enumdirection);

            return a(structureboundingbox) && StructurePiece.a(list, structureboundingbox) == null ? new WorldGenVillagePieces.WorldGenVillageHouse2(worldgenvillagepieces_worldgenvillagestartpiece, l, random, structureboundingbox, enumdirection) : null;
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            if (this.h < 0) {
                this.h = this.b(world, structureboundingbox);
                if (this.h < 0) {
                    return true;
                }

                this.l.a(0, this.h - this.l.e + 7 - 1, 0);
            }

            this.a(world, structureboundingbox, 1, 1, 1, 7, 4, 4, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 2, 1, 6, 8, 4, 10, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 2, 0, 5, 8, 0, 10, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 1, 7, 0, 4, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 0, 0, 0, 3, 5, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 8, 0, 0, 8, 3, 10, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 0, 7, 2, 0, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 5, 2, 1, 5, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 2, 0, 6, 2, 3, 10, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 3, 0, 10, 7, 3, 10, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 2, 0, 7, 3, 0, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 2, 5, 2, 3, 5, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 4, 1, 8, 4, 1, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 4, 4, 3, 4, 4, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 5, 2, 8, 5, 3, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, Blocks.PLANKS.getBlockData(), 0, 4, 2, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 0, 4, 3, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 8, 4, 2, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 8, 4, 3, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 8, 4, 4, structureboundingbox);
            IBlockData iblockdata = Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
            IBlockData iblockdata1 = Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.SOUTH);
            IBlockData iblockdata2 = Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.WEST);
            IBlockData iblockdata3 = Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.EAST);

            int i;
            int j;

            for (i = -1; i <= 2; ++i) {
                for (j = 0; j <= 8; ++j) {
                    this.a(world, iblockdata, j, 4 + i, i, structureboundingbox);
                    if ((i > -1 || j <= 1) && (i > 0 || j <= 3) && (i > 1 || j <= 4 || j >= 6)) {
                        this.a(world, iblockdata1, j, 4 + i, 5 - i, structureboundingbox);
                    }
                }
            }

            this.a(world, structureboundingbox, 3, 4, 5, 3, 4, 10, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 7, 4, 2, 7, 4, 10, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 4, 5, 4, 4, 5, 10, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 6, 5, 4, 6, 5, 10, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 5, 6, 3, 5, 6, 10, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);

            for (i = 4; i >= 1; --i) {
                this.a(world, Blocks.PLANKS.getBlockData(), i, 2 + i, 7 - i, structureboundingbox);

                for (j = 8 - i; j <= 10; ++j) {
                    this.a(world, iblockdata3, i, 2 + i, j, structureboundingbox);
                }
            }

            this.a(world, Blocks.PLANKS.getBlockData(), 6, 6, 3, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 7, 5, 4, structureboundingbox);
            this.a(world, iblockdata2, 6, 6, 4, structureboundingbox);

            for (i = 6; i <= 8; ++i) {
                for (j = 5; j <= 10; ++j) {
                    this.a(world, iblockdata2, i, 12 - i, j, structureboundingbox);
                }
            }

            this.a(world, Blocks.LOG.getBlockData(), 0, 2, 1, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 0, 2, 4, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 2, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 2, 3, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 4, 2, 0, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 5, 2, 0, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 6, 2, 0, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 8, 2, 1, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 8, 2, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 8, 2, 3, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 8, 2, 4, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 8, 2, 5, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 8, 2, 6, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 8, 2, 7, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 8, 2, 8, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 8, 2, 9, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 2, 2, 6, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 2, 2, 7, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 2, 2, 8, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 2, 2, 9, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 4, 4, 10, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 5, 4, 10, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 6, 4, 10, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 5, 5, 10, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 2, 1, 0, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 2, 2, 0, structureboundingbox);
            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.NORTH), 2, 3, 1, structureboundingbox);
            this.a(world, structureboundingbox, random, 2, 1, 0, EnumDirection.NORTH);
            this.a(world, structureboundingbox, 1, 0, -1, 3, 2, -1, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            if (this.a(world, 2, 0, -1, structureboundingbox).getMaterial() == Material.AIR && this.a(world, 2, -1, -1, structureboundingbox).getMaterial() != Material.AIR) {
                this.a(world, iblockdata, 2, 0, -1, structureboundingbox);
            }

            for (i = 0; i < 5; ++i) {
                for (j = 0; j < 9; ++j) {
                    this.b(world, j, 7, i, structureboundingbox);
                    this.b(world, Blocks.COBBLESTONE.getBlockData(), j, -1, i, structureboundingbox);
                }
            }

            for (i = 5; i < 11; ++i) {
                for (j = 2; j < 9; ++j) {
                    this.b(world, j, 7, i, structureboundingbox);
                    this.b(world, Blocks.COBBLESTONE.getBlockData(), j, -1, i, structureboundingbox);
                }
            }

            this.a(world, structureboundingbox, 4, 1, 2, 2);
            return true;
        }
    }

    public static class WorldGenVillageButcher extends WorldGenVillagePieces.WorldGenVillagePiece {

        public WorldGenVillageButcher() {}

        public WorldGenVillageButcher(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, StructureBoundingBox structureboundingbox, EnumDirection enumdirection) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(enumdirection);
            this.l = structureboundingbox;
        }

        public static WorldGenVillagePieces.WorldGenVillageButcher a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.a(i, j, k, 0, 0, 0, 9, 7, 11, enumdirection);

            return a(structureboundingbox) && StructurePiece.a(list, structureboundingbox) == null ? new WorldGenVillagePieces.WorldGenVillageButcher(worldgenvillagepieces_worldgenvillagestartpiece, l, random, structureboundingbox, enumdirection) : null;
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            if (this.h < 0) {
                this.h = this.b(world, structureboundingbox);
                if (this.h < 0) {
                    return true;
                }

                this.l.a(0, this.h - this.l.e + 7 - 1, 0);
            }

            this.a(world, structureboundingbox, 1, 1, 1, 7, 4, 4, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 2, 1, 6, 8, 4, 10, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 2, 0, 6, 8, 0, 10, Blocks.DIRT.getBlockData(), Blocks.DIRT.getBlockData(), false);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 6, 0, 6, structureboundingbox);
            this.a(world, structureboundingbox, 2, 1, 6, 2, 1, 10, Blocks.FENCE.getBlockData(), Blocks.FENCE.getBlockData(), false);
            this.a(world, structureboundingbox, 8, 1, 6, 8, 1, 10, Blocks.FENCE.getBlockData(), Blocks.FENCE.getBlockData(), false);
            this.a(world, structureboundingbox, 3, 1, 10, 7, 1, 10, Blocks.FENCE.getBlockData(), Blocks.FENCE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 1, 7, 0, 4, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 0, 0, 0, 3, 5, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 8, 0, 0, 8, 3, 5, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 0, 7, 1, 0, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 5, 7, 1, 5, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 2, 0, 7, 3, 0, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 2, 5, 7, 3, 5, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 4, 1, 8, 4, 1, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 4, 4, 8, 4, 4, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 5, 2, 8, 5, 3, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, Blocks.PLANKS.getBlockData(), 0, 4, 2, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 0, 4, 3, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 8, 4, 2, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 8, 4, 3, structureboundingbox);
            IBlockData iblockdata = Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
            IBlockData iblockdata1 = Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.SOUTH);
            IBlockData iblockdata2 = Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.WEST);

            int i;
            int j;

            for (i = -1; i <= 2; ++i) {
                for (j = 0; j <= 8; ++j) {
                    this.a(world, iblockdata, j, 4 + i, i, structureboundingbox);
                    this.a(world, iblockdata1, j, 4 + i, 5 - i, structureboundingbox);
                }
            }

            this.a(world, Blocks.LOG.getBlockData(), 0, 2, 1, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 0, 2, 4, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 8, 2, 1, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 8, 2, 4, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 2, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 2, 3, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 8, 2, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 8, 2, 3, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 2, 2, 5, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 3, 2, 5, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 5, 2, 0, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 6, 2, 5, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 2, 1, 3, structureboundingbox);
            this.a(world, Blocks.WOODEN_PRESSURE_PLATE.getBlockData(), 2, 2, 3, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 1, 1, 4, structureboundingbox);
            this.a(world, iblockdata, 2, 1, 4, structureboundingbox);
            this.a(world, iblockdata2, 1, 1, 3, structureboundingbox);
            this.a(world, structureboundingbox, 5, 0, 1, 7, 0, 3, Blocks.DOUBLE_STONE_SLAB.getBlockData(), Blocks.DOUBLE_STONE_SLAB.getBlockData(), false);
            this.a(world, Blocks.DOUBLE_STONE_SLAB.getBlockData(), 6, 1, 1, structureboundingbox);
            this.a(world, Blocks.DOUBLE_STONE_SLAB.getBlockData(), 6, 1, 2, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 2, 1, 0, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 2, 2, 0, structureboundingbox);
            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.NORTH), 2, 3, 1, structureboundingbox);
            this.a(world, structureboundingbox, random, 2, 1, 0, EnumDirection.NORTH);
            if (this.a(world, 2, 0, -1, structureboundingbox).getMaterial() == Material.AIR && this.a(world, 2, -1, -1, structureboundingbox).getMaterial() != Material.AIR) {
                this.a(world, iblockdata, 2, 0, -1, structureboundingbox);
            }

            this.a(world, Blocks.AIR.getBlockData(), 6, 1, 5, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 6, 2, 5, structureboundingbox);
            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.SOUTH), 6, 3, 4, structureboundingbox);
            this.a(world, structureboundingbox, random, 6, 1, 5, EnumDirection.SOUTH);

            for (i = 0; i < 5; ++i) {
                for (j = 0; j < 9; ++j) {
                    this.b(world, j, 7, i, structureboundingbox);
                    this.b(world, Blocks.COBBLESTONE.getBlockData(), j, -1, i, structureboundingbox);
                }
            }

            this.a(world, structureboundingbox, 4, 1, 2, 2);
            return true;
        }

        protected int c(int i, int j) {
            return i == 0 ? 4 : super.c(i, j);
        }
    }

    public static class WorldGenVillageHut extends WorldGenVillagePieces.WorldGenVillagePiece {

        private boolean a;
        private int b;

        public WorldGenVillageHut() {}

        public WorldGenVillageHut(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, StructureBoundingBox structureboundingbox, EnumDirection enumdirection) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(enumdirection);
            this.l = structureboundingbox;
            this.a = random.nextBoolean();
            this.b = random.nextInt(3);
        }

        protected void a(NBTTagCompound nbttagcompound) {
            super.a(nbttagcompound);
            nbttagcompound.setInt("T", this.b);
            nbttagcompound.setBoolean("C", this.a);
        }

        protected void b(NBTTagCompound nbttagcompound) {
            super.b(nbttagcompound);
            this.b = nbttagcompound.getInt("T");
            this.a = nbttagcompound.getBoolean("C");
        }

        public static WorldGenVillagePieces.WorldGenVillageHut a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.a(i, j, k, 0, 0, 0, 4, 6, 5, enumdirection);

            return a(structureboundingbox) && StructurePiece.a(list, structureboundingbox) == null ? new WorldGenVillagePieces.WorldGenVillageHut(worldgenvillagepieces_worldgenvillagestartpiece, l, random, structureboundingbox, enumdirection) : null;
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            if (this.h < 0) {
                this.h = this.b(world, structureboundingbox);
                if (this.h < 0) {
                    return true;
                }

                this.l.a(0, this.h - this.l.e + 6 - 1, 0);
            }

            this.a(world, structureboundingbox, 1, 1, 1, 3, 5, 4, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 0, 0, 3, 0, 4, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 1, 2, 0, 3, Blocks.DIRT.getBlockData(), Blocks.DIRT.getBlockData(), false);
            if (this.a) {
                this.a(world, structureboundingbox, 1, 4, 1, 2, 4, 3, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            } else {
                this.a(world, structureboundingbox, 1, 5, 1, 2, 5, 3, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            }

            this.a(world, Blocks.LOG.getBlockData(), 1, 4, 0, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 2, 4, 0, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 1, 4, 4, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 2, 4, 4, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 0, 4, 1, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 0, 4, 2, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 0, 4, 3, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 3, 4, 1, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 3, 4, 2, structureboundingbox);
            this.a(world, Blocks.LOG.getBlockData(), 3, 4, 3, structureboundingbox);
            this.a(world, structureboundingbox, 0, 1, 0, 0, 3, 0, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 3, 1, 0, 3, 3, 0, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 1, 4, 0, 3, 4, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 3, 1, 4, 3, 3, 4, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 1, 1, 0, 3, 3, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 3, 1, 1, 3, 3, 3, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 1, 0, 2, 3, 0, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 1, 4, 2, 3, 4, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 2, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 3, 2, 2, structureboundingbox);
            if (this.b > 0) {
                this.a(world, Blocks.FENCE.getBlockData(), this.b, 1, 3, structureboundingbox);
                this.a(world, Blocks.WOODEN_PRESSURE_PLATE.getBlockData(), this.b, 2, 3, structureboundingbox);
            }

            this.a(world, Blocks.AIR.getBlockData(), 1, 1, 0, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 1, 2, 0, structureboundingbox);
            this.a(world, structureboundingbox, random, 1, 1, 0, EnumDirection.NORTH);
            if (this.a(world, 1, 0, -1, structureboundingbox).getMaterial() == Material.AIR && this.a(world, 1, -1, -1, structureboundingbox).getMaterial() != Material.AIR) {
                this.a(world, Blocks.STONE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH), 1, 0, -1, structureboundingbox);
            }

            for (int i = 0; i < 5; ++i) {
                for (int j = 0; j < 4; ++j) {
                    this.b(world, j, 6, i, structureboundingbox);
                    this.b(world, Blocks.COBBLESTONE.getBlockData(), j, -1, i, structureboundingbox);
                }
            }

            this.a(world, structureboundingbox, 1, 1, 2, 1);
            return true;
        }
    }

    public static class WorldGenVillageLibrary extends WorldGenVillagePieces.WorldGenVillagePiece {

        public WorldGenVillageLibrary() {}

        public WorldGenVillageLibrary(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, StructureBoundingBox structureboundingbox, EnumDirection enumdirection) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(enumdirection);
            this.l = structureboundingbox;
        }

        public static WorldGenVillagePieces.WorldGenVillageLibrary a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.a(i, j, k, 0, 0, 0, 9, 9, 6, enumdirection);

            return a(structureboundingbox) && StructurePiece.a(list, structureboundingbox) == null ? new WorldGenVillagePieces.WorldGenVillageLibrary(worldgenvillagepieces_worldgenvillagestartpiece, l, random, structureboundingbox, enumdirection) : null;
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            if (this.h < 0) {
                this.h = this.b(world, structureboundingbox);
                if (this.h < 0) {
                    return true;
                }

                this.l.a(0, this.h - this.l.e + 9 - 1, 0);
            }

            this.a(world, structureboundingbox, 1, 1, 1, 7, 5, 4, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 0, 0, 8, 0, 5, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 5, 0, 8, 5, 5, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 6, 1, 8, 6, 4, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 7, 2, 8, 7, 3, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);

            int i;

            for (int j = -1; j <= 2; ++j) {
                for (i = 0; i <= 8; ++i) {
                    this.a(world, Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH), i, 6 + j, j, structureboundingbox);
                    this.a(world, Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.SOUTH), i, 6 + j, 5 - j, structureboundingbox);
                }
            }

            this.a(world, structureboundingbox, 0, 1, 0, 0, 1, 5, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 1, 5, 8, 1, 5, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 8, 1, 0, 8, 1, 4, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 2, 1, 0, 7, 1, 0, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 2, 0, 0, 4, 0, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 2, 5, 0, 4, 5, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 8, 2, 5, 8, 4, 5, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 8, 2, 0, 8, 4, 0, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 2, 1, 0, 4, 4, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 2, 5, 7, 4, 5, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 8, 2, 1, 8, 4, 4, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 2, 0, 7, 4, 0, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 4, 2, 0, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 5, 2, 0, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 6, 2, 0, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 4, 3, 0, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 5, 3, 0, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 6, 3, 0, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 2, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 2, 3, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 3, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 3, 3, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 8, 2, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 8, 2, 3, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 8, 3, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 8, 3, 3, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 2, 2, 5, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 3, 2, 5, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 5, 2, 5, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 6, 2, 5, structureboundingbox);
            this.a(world, structureboundingbox, 1, 4, 1, 7, 4, 1, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 4, 4, 7, 4, 4, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 3, 4, 7, 3, 4, Blocks.BOOKSHELF.getBlockData(), Blocks.BOOKSHELF.getBlockData(), false);
            this.a(world, Blocks.PLANKS.getBlockData(), 7, 1, 4, structureboundingbox);
            this.a(world, Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.EAST), 7, 1, 3, structureboundingbox);
            IBlockData iblockdata = Blocks.OAK_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);

            this.a(world, iblockdata, 6, 1, 4, structureboundingbox);
            this.a(world, iblockdata, 5, 1, 4, structureboundingbox);
            this.a(world, iblockdata, 4, 1, 4, structureboundingbox);
            this.a(world, iblockdata, 3, 1, 4, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 6, 1, 3, structureboundingbox);
            this.a(world, Blocks.WOODEN_PRESSURE_PLATE.getBlockData(), 6, 2, 3, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 4, 1, 3, structureboundingbox);
            this.a(world, Blocks.WOODEN_PRESSURE_PLATE.getBlockData(), 4, 2, 3, structureboundingbox);
            this.a(world, Blocks.CRAFTING_TABLE.getBlockData(), 7, 1, 1, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 1, 1, 0, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 1, 2, 0, structureboundingbox);
            this.a(world, structureboundingbox, random, 1, 1, 0, EnumDirection.NORTH);
            if (this.a(world, 1, 0, -1, structureboundingbox).getMaterial() == Material.AIR && this.a(world, 1, -1, -1, structureboundingbox).getMaterial() != Material.AIR) {
                this.a(world, Blocks.STONE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH), 1, 0, -1, structureboundingbox);
            }

            for (i = 0; i < 6; ++i) {
                for (int k = 0; k < 9; ++k) {
                    this.b(world, k, 9, i, structureboundingbox);
                    this.b(world, Blocks.COBBLESTONE.getBlockData(), k, -1, i, structureboundingbox);
                }
            }

            this.a(world, structureboundingbox, 2, 1, 2, 1);
            return true;
        }

        protected int c(int i, int j) {
            return 1;
        }
    }

    public static class WorldGenVillageTemple extends WorldGenVillagePieces.WorldGenVillagePiece {

        public WorldGenVillageTemple() {}

        public WorldGenVillageTemple(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, StructureBoundingBox structureboundingbox, EnumDirection enumdirection) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(enumdirection);
            this.l = structureboundingbox;
        }

        public static WorldGenVillagePieces.WorldGenVillageTemple a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.a(i, j, k, 0, 0, 0, 5, 12, 9, enumdirection);

            return a(structureboundingbox) && StructurePiece.a(list, structureboundingbox) == null ? new WorldGenVillagePieces.WorldGenVillageTemple(worldgenvillagepieces_worldgenvillagestartpiece, l, random, structureboundingbox, enumdirection) : null;
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            if (this.h < 0) {
                this.h = this.b(world, structureboundingbox);
                if (this.h < 0) {
                    return true;
                }

                this.l.a(0, this.h - this.l.e + 12 - 1, 0);
            }

            this.a(world, structureboundingbox, 1, 1, 1, 3, 3, 7, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 5, 1, 3, 9, 3, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 0, 0, 3, 0, 8, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 1, 0, 3, 10, 0, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 1, 1, 0, 10, 3, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 4, 1, 1, 4, 10, 3, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 0, 4, 0, 4, 7, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 4, 0, 4, 4, 4, 7, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 1, 8, 3, 4, 8, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 5, 4, 3, 10, 4, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 5, 5, 3, 5, 7, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 9, 0, 4, 9, 4, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 4, 0, 4, 4, 4, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 0, 11, 2, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 4, 11, 2, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 2, 11, 0, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 2, 11, 4, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 1, 1, 6, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 1, 1, 7, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 2, 1, 7, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 3, 1, 6, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 3, 1, 7, structureboundingbox);
            IBlockData iblockdata = Blocks.STONE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH);
            IBlockData iblockdata1 = Blocks.STONE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.WEST);
            IBlockData iblockdata2 = Blocks.STONE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.EAST);

            this.a(world, iblockdata, 1, 1, 5, structureboundingbox);
            this.a(world, iblockdata, 2, 1, 6, structureboundingbox);
            this.a(world, iblockdata, 3, 1, 5, structureboundingbox);
            this.a(world, iblockdata1, 1, 2, 7, structureboundingbox);
            this.a(world, iblockdata2, 3, 2, 7, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 2, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 3, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 4, 2, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 4, 3, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 6, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 7, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 4, 6, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 4, 7, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 2, 6, 0, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 2, 7, 0, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 2, 6, 4, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 2, 7, 4, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 3, 6, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 4, 3, 6, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 2, 3, 8, structureboundingbox);
            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.SOUTH), 2, 4, 7, structureboundingbox);
            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.EAST), 1, 4, 6, structureboundingbox);
            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.WEST), 3, 4, 6, structureboundingbox);
            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.NORTH), 2, 4, 5, structureboundingbox);
            IBlockData iblockdata3 = Blocks.LADDER.getBlockData().set(BlockLadder.FACING, EnumDirection.WEST);

            int i;

            for (i = 1; i <= 9; ++i) {
                this.a(world, iblockdata3, 3, i, 3, structureboundingbox);
            }

            this.a(world, Blocks.AIR.getBlockData(), 2, 1, 0, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 2, 2, 0, structureboundingbox);
            this.a(world, structureboundingbox, random, 2, 1, 0, EnumDirection.NORTH);
            if (this.a(world, 2, 0, -1, structureboundingbox).getMaterial() == Material.AIR && this.a(world, 2, -1, -1, structureboundingbox).getMaterial() != Material.AIR) {
                this.a(world, iblockdata, 2, 0, -1, structureboundingbox);
            }

            for (i = 0; i < 9; ++i) {
                for (int j = 0; j < 5; ++j) {
                    this.b(world, j, 12, i, structureboundingbox);
                    this.b(world, Blocks.COBBLESTONE.getBlockData(), j, -1, i, structureboundingbox);
                }
            }

            this.a(world, structureboundingbox, 2, 1, 2, 1);
            return true;
        }

        protected int c(int i, int j) {
            return 2;
        }
    }

    public static class WorldGenVillageHouse extends WorldGenVillagePieces.WorldGenVillagePiece {

        private boolean a;

        public WorldGenVillageHouse() {}

        public WorldGenVillageHouse(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, StructureBoundingBox structureboundingbox, EnumDirection enumdirection) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(enumdirection);
            this.l = structureboundingbox;
            this.a = random.nextBoolean();
        }

        protected void a(NBTTagCompound nbttagcompound) {
            super.a(nbttagcompound);
            nbttagcompound.setBoolean("Terrace", this.a);
        }

        protected void b(NBTTagCompound nbttagcompound) {
            super.b(nbttagcompound);
            this.a = nbttagcompound.getBoolean("Terrace");
        }

        public static WorldGenVillagePieces.WorldGenVillageHouse a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection, int l) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.a(i, j, k, 0, 0, 0, 5, 6, 5, enumdirection);

            return StructurePiece.a(list, structureboundingbox) != null ? null : new WorldGenVillagePieces.WorldGenVillageHouse(worldgenvillagepieces_worldgenvillagestartpiece, l, random, structureboundingbox, enumdirection);
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            if (this.h < 0) {
                this.h = this.b(world, structureboundingbox);
                if (this.h < 0) {
                    return true;
                }

                this.l.a(0, this.h - this.l.e + 6 - 1, 0);
            }

            this.a(world, structureboundingbox, 0, 0, 0, 4, 0, 4, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);
            this.a(world, structureboundingbox, 0, 4, 0, 4, 4, 4, Blocks.LOG.getBlockData(), Blocks.LOG.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 4, 1, 3, 4, 3, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 0, 1, 0, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 0, 2, 0, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 0, 3, 0, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 4, 1, 0, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 4, 2, 0, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 4, 3, 0, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 0, 1, 4, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 0, 2, 4, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 0, 3, 4, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 4, 1, 4, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 4, 2, 4, structureboundingbox);
            this.a(world, Blocks.COBBLESTONE.getBlockData(), 4, 3, 4, structureboundingbox);
            this.a(world, structureboundingbox, 0, 1, 1, 0, 3, 3, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 4, 1, 1, 4, 3, 3, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, structureboundingbox, 1, 1, 4, 3, 3, 4, Blocks.PLANKS.getBlockData(), Blocks.PLANKS.getBlockData(), false);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 0, 2, 2, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 2, 2, 4, structureboundingbox);
            this.a(world, Blocks.GLASS_PANE.getBlockData(), 4, 2, 2, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 1, 1, 0, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 1, 2, 0, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 1, 3, 0, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 2, 3, 0, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 3, 3, 0, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 3, 2, 0, structureboundingbox);
            this.a(world, Blocks.PLANKS.getBlockData(), 3, 1, 0, structureboundingbox);
            if (this.a(world, 2, 0, -1, structureboundingbox).getMaterial() == Material.AIR && this.a(world, 2, -1, -1, structureboundingbox).getMaterial() != Material.AIR) {
                this.a(world, Blocks.STONE_STAIRS.getBlockData().set(BlockStairs.FACING, EnumDirection.NORTH), 2, 0, -1, structureboundingbox);
            }

            this.a(world, structureboundingbox, 1, 1, 1, 3, 3, 3, Blocks.AIR.getBlockData(), Blocks.AIR.getBlockData(), false);
            if (this.a) {
                this.a(world, Blocks.FENCE.getBlockData(), 0, 5, 0, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 1, 5, 0, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 2, 5, 0, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 3, 5, 0, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 4, 5, 0, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 0, 5, 4, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 1, 5, 4, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 2, 5, 4, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 3, 5, 4, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 4, 5, 4, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 4, 5, 1, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 4, 5, 2, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 4, 5, 3, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 0, 5, 1, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 0, 5, 2, structureboundingbox);
                this.a(world, Blocks.FENCE.getBlockData(), 0, 5, 3, structureboundingbox);
            }

            if (this.a) {
                IBlockData iblockdata = Blocks.LADDER.getBlockData().set(BlockLadder.FACING, EnumDirection.SOUTH);

                this.a(world, iblockdata, 3, 1, 3, structureboundingbox);
                this.a(world, iblockdata, 3, 2, 3, structureboundingbox);
                this.a(world, iblockdata, 3, 3, 3, structureboundingbox);
                this.a(world, iblockdata, 3, 4, 3, structureboundingbox);
            }

            this.a(world, Blocks.TORCH.getBlockData().set(BlockTorch.FACING, EnumDirection.NORTH), 2, 3, 1, structureboundingbox);

            for (int i = 0; i < 5; ++i) {
                for (int j = 0; j < 5; ++j) {
                    this.b(world, j, 6, i, structureboundingbox);
                    this.b(world, Blocks.COBBLESTONE.getBlockData(), j, -1, i, structureboundingbox);
                }
            }

            this.a(world, structureboundingbox, 1, 1, 2, 1);
            return true;
        }
    }

    public static class WorldGenVillageRoad extends WorldGenVillagePieces.WorldGenVillageRoadPiece {

        private int a;

        public WorldGenVillageRoad() {}

        public WorldGenVillageRoad(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, StructureBoundingBox structureboundingbox, EnumDirection enumdirection) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(enumdirection);
            this.l = structureboundingbox;
            this.a = Math.max(structureboundingbox.c(), structureboundingbox.e());
        }

        protected void a(NBTTagCompound nbttagcompound) {
            super.a(nbttagcompound);
            nbttagcompound.setInt("Length", this.a);
        }

        protected void b(NBTTagCompound nbttagcompound) {
            super.b(nbttagcompound);
            this.a = nbttagcompound.getInt("Length");
        }

        public void a(StructurePiece structurepiece, List<StructurePiece> list, Random random) {
            boolean flag = false;

            int i;
            StructurePiece structurepiece1;

            for (i = random.nextInt(5); i < this.a - 8; i += 2 + random.nextInt(5)) {
                structurepiece1 = this.a((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, 0, i);
                if (structurepiece1 != null) {
                    i += Math.max(structurepiece1.l.c(), structurepiece1.l.e());
                    flag = true;
                }
            }

            for (i = random.nextInt(5); i < this.a - 8; i += 2 + random.nextInt(5)) {
                structurepiece1 = this.b((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, 0, i);
                if (structurepiece1 != null) {
                    i += Math.max(structurepiece1.l.c(), structurepiece1.l.e());
                    flag = true;
                }
            }

            EnumDirection enumdirection = this.e();

            if (flag && random.nextInt(3) > 0 && enumdirection != null) {
                switch (WorldGenVillagePieces.SyntheticClass_1.a[enumdirection.ordinal()]) {
                case 1:
                    WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.a - 1, this.l.b, this.l.c, EnumDirection.WEST, this.d());
                    break;

                case 2:
                    WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.a - 1, this.l.b, this.l.f - 2, EnumDirection.WEST, this.d());
                    break;

                case 3:
                    WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.a, this.l.b, this.l.c - 1, EnumDirection.NORTH, this.d());
                    break;

                case 4:
                    WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.d - 2, this.l.b, this.l.c - 1, EnumDirection.NORTH, this.d());
                }
            }

            if (flag && random.nextInt(3) > 0 && enumdirection != null) {
                switch (WorldGenVillagePieces.SyntheticClass_1.a[enumdirection.ordinal()]) {
                case 1:
                    WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.d + 1, this.l.b, this.l.c, EnumDirection.EAST, this.d());
                    break;

                case 2:
                    WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.d + 1, this.l.b, this.l.f - 2, EnumDirection.EAST, this.d());
                    break;

                case 3:
                    WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.a, this.l.b, this.l.f + 1, EnumDirection.SOUTH, this.d());
                    break;

                case 4:
                    WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.d - 2, this.l.b, this.l.f + 1, EnumDirection.SOUTH, this.d());
                }
            }

        }

        public static StructureBoundingBox a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j, int k, EnumDirection enumdirection) {
            for (int l = 7 * MathHelper.nextInt(random, 3, 5); l >= 7; l -= 7) {
                StructureBoundingBox structureboundingbox = StructureBoundingBox.a(i, j, k, 0, 0, 0, 3, 3, l, enumdirection);

                if (StructurePiece.a(list, structureboundingbox) == null) {
                    return structureboundingbox;
                }
            }

            return null;
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            IBlockData iblockdata = this.a(Blocks.GRAVEL.getBlockData());
            IBlockData iblockdata1 = this.a(Blocks.COBBLESTONE.getBlockData());

            for (int i = this.l.a; i <= this.l.d; ++i) {
                for (int j = this.l.c; j <= this.l.f; ++j) {
                    BlockPosition blockposition = new BlockPosition(i, 64, j);

                    if (structureboundingbox.b((BaseBlockPosition) blockposition)) {
                        blockposition = world.q(blockposition).down();
                        world.setTypeAndData(blockposition, iblockdata, 2);
                        world.setTypeAndData(blockposition.down(), iblockdata1, 2);
                    }
                }
            }

            return true;
        }
    }

    public abstract static class WorldGenVillageRoadPiece extends WorldGenVillagePieces.WorldGenVillagePiece {

        public WorldGenVillageRoadPiece() {}

        protected WorldGenVillageRoadPiece(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
        }
    }

    public static class WorldGenVillageStartPiece extends WorldGenVillagePieces.WorldGenVillageWell {

        public WorldChunkManager a;
        public boolean b;
        public int c;
        public WorldGenVillagePieces.WorldGenVillagePieceWeight d;
        public List<WorldGenVillagePieces.WorldGenVillagePieceWeight> e;
        public List<StructurePiece> f = Lists.newArrayList();
        public List<StructurePiece> g = Lists.newArrayList();

        public WorldGenVillageStartPiece() {}

        public WorldGenVillageStartPiece(WorldChunkManager worldchunkmanager, int i, Random random, int j, int k, List<WorldGenVillagePieces.WorldGenVillagePieceWeight> list, int l) {
            super((WorldGenVillagePieces.WorldGenVillageStartPiece) null, 0, random, j, k);
            this.a = worldchunkmanager;
            this.e = list;
            this.c = l;
            BiomeBase biomebase = worldchunkmanager.getBiome(new BlockPosition(j, 0, k), Biomes.b);

            this.b = biomebase == Biomes.d || biomebase == Biomes.s;
            this.a(this.b);
        }

        public WorldChunkManager h() {
            return this.a;
        }
    }

    public static class WorldGenVillageWell extends WorldGenVillagePieces.WorldGenVillagePiece {

        public WorldGenVillageWell() {}

        public WorldGenVillageWell(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i, Random random, int j, int k) {
            super(worldgenvillagepieces_worldgenvillagestartpiece, i);
            this.a(EnumDirection.EnumDirectionLimit.HORIZONTAL.a(random));
            if (this.e().k() == EnumDirection.EnumAxis.Z) {
                this.l = new StructureBoundingBox(j, 64, k, j + 6 - 1, 78, k + 6 - 1);
            } else {
                this.l = new StructureBoundingBox(j, 64, k, j + 6 - 1, 78, k + 6 - 1);
            }

        }

        public void a(StructurePiece structurepiece, List<StructurePiece> list, Random random) {
            WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.a - 1, this.l.e - 4, this.l.c + 1, EnumDirection.WEST, this.d());
            WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.d + 1, this.l.e - 4, this.l.c + 1, EnumDirection.EAST, this.d());
            WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.a + 1, this.l.e - 4, this.l.c - 1, EnumDirection.NORTH, this.d());
            WorldGenVillagePieces.e((WorldGenVillagePieces.WorldGenVillageStartPiece) structurepiece, list, random, this.l.a + 1, this.l.e - 4, this.l.f + 1, EnumDirection.SOUTH, this.d());
        }

        public boolean a(World world, Random random, StructureBoundingBox structureboundingbox) {
            if (this.h < 0) {
                this.h = this.b(world, structureboundingbox);
                if (this.h < 0) {
                    return true;
                }

                this.l.a(0, this.h - this.l.e + 3, 0);
            }

            this.a(world, structureboundingbox, 1, 0, 1, 4, 12, 4, Blocks.COBBLESTONE.getBlockData(), Blocks.FLOWING_WATER.getBlockData(), false);
            this.a(world, Blocks.AIR.getBlockData(), 2, 12, 2, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 3, 12, 2, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 2, 12, 3, structureboundingbox);
            this.a(world, Blocks.AIR.getBlockData(), 3, 12, 3, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 1, 13, 1, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 1, 14, 1, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 4, 13, 1, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 4, 14, 1, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 1, 13, 4, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 1, 14, 4, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 4, 13, 4, structureboundingbox);
            this.a(world, Blocks.FENCE.getBlockData(), 4, 14, 4, structureboundingbox);
            this.a(world, structureboundingbox, 1, 15, 1, 4, 15, 4, Blocks.COBBLESTONE.getBlockData(), Blocks.COBBLESTONE.getBlockData(), false);

            for (int i = 0; i <= 5; ++i) {
                for (int j = 0; j <= 5; ++j) {
                    if (j == 0 || j == 5 || i == 0 || i == 5) {
                        this.a(world, Blocks.GRAVEL.getBlockData(), j, 11, i, structureboundingbox);
                        this.b(world, j, 12, i, structureboundingbox);
                    }
                }
            }

            return true;
        }
    }

    abstract static class WorldGenVillagePiece extends StructurePiece {

        protected int h = -1;
        private int a;
        private boolean b;

        public WorldGenVillagePiece() {}

        protected WorldGenVillagePiece(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, int i) {
            super(i);
            if (worldgenvillagepieces_worldgenvillagestartpiece != null) {
                this.b = worldgenvillagepieces_worldgenvillagestartpiece.b;
            }

        }

        protected void a(NBTTagCompound nbttagcompound) {
            nbttagcompound.setInt("HPos", this.h);
            nbttagcompound.setInt("VCount", this.a);
            nbttagcompound.setBoolean("Desert", this.b);
        }

        protected void b(NBTTagCompound nbttagcompound) {
            this.h = nbttagcompound.getInt("HPos");
            this.a = nbttagcompound.getInt("VCount");
            this.b = nbttagcompound.getBoolean("Desert");
        }

        protected StructurePiece a(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j) {
            EnumDirection enumdirection = this.e();

            if (enumdirection != null) {
                switch (WorldGenVillagePieces.SyntheticClass_1.a[enumdirection.ordinal()]) {
                case 1:
                    return WorldGenVillagePieces.d(worldgenvillagepieces_worldgenvillagestartpiece, list, random, this.l.a - 1, this.l.b + i, this.l.c + j, EnumDirection.WEST, this.d());

                case 2:
                    return WorldGenVillagePieces.d(worldgenvillagepieces_worldgenvillagestartpiece, list, random, this.l.a - 1, this.l.b + i, this.l.c + j, EnumDirection.WEST, this.d());

                case 3:
                    return WorldGenVillagePieces.d(worldgenvillagepieces_worldgenvillagestartpiece, list, random, this.l.a + j, this.l.b + i, this.l.c - 1, EnumDirection.NORTH, this.d());

                case 4:
                    return WorldGenVillagePieces.d(worldgenvillagepieces_worldgenvillagestartpiece, list, random, this.l.a + j, this.l.b + i, this.l.c - 1, EnumDirection.NORTH, this.d());
                }
            }

            return null;
        }

        protected StructurePiece b(WorldGenVillagePieces.WorldGenVillageStartPiece worldgenvillagepieces_worldgenvillagestartpiece, List<StructurePiece> list, Random random, int i, int j) {
            EnumDirection enumdirection = this.e();

            if (enumdirection != null) {
                switch (WorldGenVillagePieces.SyntheticClass_1.a[enumdirection.ordinal()]) {
                case 1:
                    return WorldGenVillagePieces.d(worldgenvillagepieces_worldgenvillagestartpiece, list, random, this.l.d + 1, this.l.b + i, this.l.c + j, EnumDirection.EAST, this.d());

                case 2:
                    return WorldGenVillagePieces.d(worldgenvillagepieces_worldgenvillagestartpiece, list, random, this.l.d + 1, this.l.b + i, this.l.c + j, EnumDirection.EAST, this.d());

                case 3:
                    return WorldGenVillagePieces.d(worldgenvillagepieces_worldgenvillagestartpiece, list, random, this.l.a + j, this.l.b + i, this.l.f + 1, EnumDirection.SOUTH, this.d());

                case 4:
                    return WorldGenVillagePieces.d(worldgenvillagepieces_worldgenvillagestartpiece, list, random, this.l.a + j, this.l.b + i, this.l.f + 1, EnumDirection.SOUTH, this.d());
                }
            }

            return null;
        }

        protected int b(World world, StructureBoundingBox structureboundingbox) {
            int i = 0;
            int j = 0;
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

            for (int k = this.l.c; k <= this.l.f; ++k) {
                for (int l = this.l.a; l <= this.l.d; ++l) {
                    blockposition_mutableblockposition.c(l, 64, k);
                    if (structureboundingbox.b((BaseBlockPosition) blockposition_mutableblockposition)) {
                        i += Math.max(world.q(blockposition_mutableblockposition).getY(), world.worldProvider.getSeaLevel());
                        ++j;
                    }
                }
            }

            if (j == 0) {
                return -1;
            } else {
                return i / j;
            }
        }

        protected static boolean a(StructureBoundingBox structureboundingbox) {
            return structureboundingbox != null && structureboundingbox.b > 10;
        }

        protected void a(World world, StructureBoundingBox structureboundingbox, int i, int j, int k, int l) {
            if (this.a < l) {
                for (int i1 = this.a; i1 < l; ++i1) {
                    int j1 = this.a(i + i1, k);
                    int k1 = this.d(j);
                    int l1 = this.b(i + i1, k);

                    if (!structureboundingbox.b((BaseBlockPosition) (new BlockPosition(j1, k1, l1)))) {
                        break;
                    }

                    ++this.a;
                    EntityVillager entityvillager = new EntityVillager(world);

                    entityvillager.setPositionRotation((double) j1 + 0.5D, (double) k1, (double) l1 + 0.5D, 0.0F, 0.0F);
                    entityvillager.prepare(world.D(new BlockPosition(entityvillager)), (GroupDataEntity) null);
                    entityvillager.setProfession(this.c(i1, entityvillager.getProfession()));
                    world.addEntity(entityvillager, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CHUNK_GEN); // CraftBukkit - add SpawnReason
                }

            }
        }

        protected int c(int i, int j) {
            return j;
        }

        protected IBlockData a(IBlockData iblockdata) {
            if (this.b) {
                if (iblockdata.getBlock() == Blocks.LOG || iblockdata.getBlock() == Blocks.LOG2) {
                    return Blocks.SANDSTONE.getBlockData();
                }

                if (iblockdata.getBlock() == Blocks.COBBLESTONE) {
                    return Blocks.SANDSTONE.fromLegacyData(BlockSandStone.EnumSandstoneVariant.DEFAULT.a());
                }

                if (iblockdata.getBlock() == Blocks.PLANKS) {
                    return Blocks.SANDSTONE.fromLegacyData(BlockSandStone.EnumSandstoneVariant.SMOOTH.a());
                }

                if (iblockdata.getBlock() == Blocks.OAK_STAIRS) {
                    return Blocks.SANDSTONE_STAIRS.getBlockData().set(BlockStairs.FACING, iblockdata.get(BlockStairs.FACING));
                }

                if (iblockdata.getBlock() == Blocks.STONE_STAIRS) {
                    return Blocks.SANDSTONE_STAIRS.getBlockData().set(BlockStairs.FACING, iblockdata.get(BlockStairs.FACING));
                }

                if (iblockdata.getBlock() == Blocks.GRAVEL) {
                    return Blocks.SANDSTONE.getBlockData();
                }
            }

            return iblockdata;
        }

        protected void a(World world, IBlockData iblockdata, int i, int j, int k, StructureBoundingBox structureboundingbox) {
            IBlockData iblockdata1 = this.a(iblockdata);

            super.a(world, iblockdata1, i, j, k, structureboundingbox);
        }

        protected void a(World world, StructureBoundingBox structureboundingbox, int i, int j, int k, int l, int i1, int j1, IBlockData iblockdata, IBlockData iblockdata1, boolean flag) {
            IBlockData iblockdata2 = this.a(iblockdata);
            IBlockData iblockdata3 = this.a(iblockdata1);

            super.a(world, structureboundingbox, i, j, k, l, i1, j1, iblockdata2, iblockdata3, flag);
        }

        protected void b(World world, IBlockData iblockdata, int i, int j, int k, StructureBoundingBox structureboundingbox) {
            IBlockData iblockdata1 = this.a(iblockdata);

            super.b(world, iblockdata1, i, j, k, structureboundingbox);
        }

        protected void a(boolean flag) {
            this.b = flag;
        }
    }

    public static class WorldGenVillagePieceWeight {

        public Class<? extends WorldGenVillagePieces.WorldGenVillagePiece> a;
        public final int b;
        public int c;
        public int d;

        public WorldGenVillagePieceWeight(Class<? extends WorldGenVillagePieces.WorldGenVillagePiece> oclass, int i, int j) {
            this.a = oclass;
            this.b = i;
            this.d = j;
        }

        public boolean a(int i) {
            return this.d == 0 || this.c < this.d;
        }

        public boolean a() {
            return this.d == 0 || this.c < this.d;
        }
    }
}
