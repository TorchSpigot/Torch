package net.minecraft.server;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.RecursiveAction;

public abstract class StructureGenerator extends WorldGenBase {

    private PersistentStructure a;
    protected Long2ObjectMap<StructureStart> c = new Long2ObjectOpenHashMap(1024);

    public StructureGenerator() {}

    public abstract String a();

    protected final synchronized void a(World world, final int i, final int j, int k, int l, ChunkSnapshot chunksnapshot) {
        this.a(world);
        if (!this.c.containsKey(ChunkCoordIntPair.a(i, j))) {
            this.f.nextInt();

            try {
                if (this.a(i, j)) {
                    StructureStart structurestart = this.b(i, j);

                    this.c.put(ChunkCoordIntPair.a(i, j), structurestart);
                    if (structurestart.a()) {
                        this.a(i, j, structurestart);
                    }
                }

            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Exception preparing structure feature");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Feature being prepared");

                crashreportsystemdetails.a("Is feature chunk", new CrashReportCallable() {
                    public String a() throws Exception {
                        return StructureGenerator.this.a(i, j) ? "True" : "False";
                    }

                    public Object call() throws Exception {
                        return this.a();
                    }
                });
                crashreportsystemdetails.a("Chunk location", (Object) String.format("%d,%d", new Object[] { Integer.valueOf(i), Integer.valueOf(j)}));
                crashreportsystemdetails.a("Chunk pos hash", new CrashReportCallable() {
                    public String a() throws Exception {
                        return String.valueOf(ChunkCoordIntPair.a(i, j));
                    }

                    public Object call() throws Exception {
                        return this.a();
                    }
                });
                crashreportsystemdetails.a("Structure type", new CrashReportCallable() {
                    public String a() throws Exception {
                        return StructureGenerator.this.getClass().getCanonicalName();
                    }

                    public Object call() throws Exception {
                        return this.a();
                    }
                });
                throw new ReportedException(crashreport);
            }
        }
    }

    public synchronized boolean a(World world, Random random, ChunkCoordIntPair chunkcoordintpair) {
        this.a(world);
        int i = (chunkcoordintpair.x << 4) + 8;
        int j = (chunkcoordintpair.z << 4) + 8;
        boolean flag = false;
        Iterator iterator = this.c.values().iterator();

        while (iterator.hasNext()) {
            StructureStart structurestart = (StructureStart) iterator.next();

            if (structurestart.a() && structurestart.a(chunkcoordintpair) && structurestart.b().a(i, j, i + 15, j + 15)) {
                structurestart.a(world, random, new StructureBoundingBox(i, j, i + 15, j + 15));
                structurestart.b(chunkcoordintpair);
                flag = true;
                structure str_task = new structure(this, structurestart.e(), structurestart.f(), structurestart);
                str_task.fork();
            }
        }

        return flag;
    }

    public boolean b(BlockPosition blockposition) {
        if (this.g == null) return false; // Paper
        this.a(this.g);
        return this.c(blockposition) != null;
    }

    protected synchronized StructureStart c(BlockPosition blockposition) { // CraftBukkit - synchronized
        Iterator iterator = this.c.values().iterator();

        while (iterator.hasNext()) {
            StructureStart structurestart = (StructureStart) iterator.next();

            if (structurestart.a() && structurestart.b().b((BaseBlockPosition) blockposition)) {
                Iterator iterator1 = structurestart.c().iterator();

                while (iterator1.hasNext()) {
                    StructurePiece structurepiece = (StructurePiece) iterator1.next();

                    if (structurepiece.c().b((BaseBlockPosition) blockposition)) {
                        return structurestart;
                    }
                }
            }
        }

        return null;
    }

    public synchronized boolean b(World world, BlockPosition blockposition) { // CraftBukkit - synchronized
        if (this.g == null) return false; // Paper
        this.a(world);
        Iterator iterator = this.c.values().iterator();

        StructureStart structurestart;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            structurestart = (StructureStart) iterator.next();
        } while (!structurestart.a() || !structurestart.b().b((BaseBlockPosition) blockposition));

        return true;
    }

    public synchronized BlockPosition getNearestGeneratedFeature(World world, BlockPosition blockposition) { // CraftBukkit - synchronized
        this.g = world;
        this.a(world);
        this.f.setSeed(world.getSeed());
        long i = this.f.nextLong();
        long j = this.f.nextLong();
        long k = (long) (blockposition.getX() >> 4) * i;
        long l = (long) (blockposition.getZ() >> 4) * j;

        this.f.setSeed(k ^ l ^ world.getSeed());
        this.a(world, blockposition.getX() >> 4, blockposition.getZ() >> 4, 0, 0, (ChunkSnapshot) null);
        double d0 = Double.MAX_VALUE;
        BlockPosition blockposition1 = null;
        Iterator iterator = this.c.values().iterator();

        BlockPosition blockposition2;
        double d1;

        while (iterator.hasNext()) {
            StructureStart structurestart = (StructureStart) iterator.next();

            if (structurestart.a()) {
                StructurePiece structurepiece = (StructurePiece) structurestart.c().get(0);

                blockposition2 = structurepiece.a();
                d1 = blockposition2.n(blockposition);
                if (d1 < d0) {
                    d0 = d1;
                    blockposition1 = blockposition2;
                }
            }
        }

        if (blockposition1 != null) {
            return blockposition1;
        } else {
            List list = this.F_();

            if (list != null) {
                BlockPosition blockposition3 = null;
                Iterator iterator1 = list.iterator();

                while (iterator1.hasNext()) {
                    blockposition2 = (BlockPosition) iterator1.next();
                    d1 = blockposition2.n(blockposition);
                    if (d1 < d0) {
                        d0 = d1;
                        blockposition3 = blockposition2;
                    }
                }

                return blockposition3;
            } else {
                return null;
            }
        }
    }

    protected List<BlockPosition> F_() {
        return null;
    }

    protected synchronized void a(World world) { // CraftBukkit - synchronized
        if (this.a == null) {
            // Spigot Start
            if (world.spigotConfig.saveStructureInfo && !this.a().equals( "Mineshaft" )) {
                this.a = (PersistentStructure) world.a(PersistentStructure.class, this.a());
            } else {
                this.a = new PersistentStructure(this.a());
            }
            // Spigot End
            if (this.a == null) {
                this.a = new PersistentStructure(this.a());
                world.a(this.a(), (PersistentBase) this.a);
            } else {
                NBTTagCompound nbttagcompound = this.a.a();
                Iterator iterator = nbttagcompound.c().iterator();

                while (iterator.hasNext()) {
                    String s = (String) iterator.next();
                    NBTBase nbtbase = nbttagcompound.get(s);

                    if (nbtbase.getTypeId() == 10) {
                        NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbtbase;

                        if (nbttagcompound1.hasKey("ChunkX") && nbttagcompound1.hasKey("ChunkZ")) {
                            int i = nbttagcompound1.getInt("ChunkX");
                            int j = nbttagcompound1.getInt("ChunkZ");
                            StructureStart structurestart = WorldGenFactory.a(nbttagcompound1, world);

                            if (structurestart != null) {
                                this.c.put(ChunkCoordIntPair.a(i, j), structurestart);
                            }
                        }
                    }
                }
            }
        }

    }

    private void a(int i, int j, StructureStart structurestart) {
        this.a.a(structurestart.a(i, j), i, j);
        this.a.c();
    }

    protected abstract boolean a(int i, int j);

    protected abstract StructureStart b(int i, int j);
	
	class structure extends RecursiveAction {
     int i, j;
     StructureStart structurestart;
     StructureGenerator sg;
     
     structure(StructureGenerator sg, int i, int j, StructureStart structurestart) {
         this.sg = sg;
         this.i = i;
         this.j = j;
         this.structurestart = structurestart;
     }
     
     @Override
     protected void compute() {
         sg.a(structurestart.e(), structurestart.f(), structurestart);
     }
              
 }
}
