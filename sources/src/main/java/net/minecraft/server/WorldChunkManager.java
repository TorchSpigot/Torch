package net.minecraft.server;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

import org.torch.server.cache.TorchIntCache;

public class WorldChunkManager {

    private CustomWorldSettingsFinal a;
    private GenLayer b;
    private GenLayer c;
    private final BiomeCache d;
    private final List<BiomeBase> e;

    protected WorldChunkManager() {
        this.d = new BiomeCache(this);
        this.e = Lists.newArrayList(new BiomeBase[] { Biomes.f, Biomes.c, Biomes.g, Biomes.u, Biomes.t, Biomes.w, Biomes.x});
    }

    private WorldChunkManager(long i, WorldType worldtype, String s) {
        this();
        if (worldtype == WorldType.CUSTOMIZED && !s.isEmpty()) {
            this.a = CustomWorldSettingsFinal.CustomWorldSettings.a(s).b();
        }

        GenLayer[] agenlayer = GenLayer.a(i, worldtype, this.a);

        this.b = agenlayer[0];
        this.c = agenlayer[1];
    }

    public WorldChunkManager(WorldData worlddata) {
        this(worlddata.getSeed(), worlddata.getType(), worlddata.getGeneratorOptions());
    }

    public List<BiomeBase> a() {
        return this.e;
    }

    public BiomeBase getBiome(BlockPosition blockposition) {
        return this.getBiome(blockposition, (BiomeBase) null);
    }

    public BiomeBase getBiome(BlockPosition blockposition, BiomeBase biomebase) {
        return this.d.a(blockposition.getX(), blockposition.getZ(), biomebase);
    }

    public float a(float f, int i) {
        return f;
    }

    public BiomeBase[] getBiomes(BiomeBase[] abiomebase, int i, int j, int k, int l) {
        TorchIntCache.releaseCaches();
        if (abiomebase == null || abiomebase.length < k * l) {
            abiomebase = new BiomeBase[k * l];
        }

        int[] aint = this.b.a(i, j, k, l);

        try {
            for (int i1 = 0; i1 < k * l; ++i1) {
                abiomebase[i1] = BiomeBase.getBiome(aint[i1], Biomes.b);
            }

            return abiomebase;
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Invalid Biome id");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("RawBiomeBlock");

            crashreportsystemdetails.a("biomes[] size", Integer.valueOf(abiomebase.length));
            crashreportsystemdetails.a("x", Integer.valueOf(i));
            crashreportsystemdetails.a("z", Integer.valueOf(j));
            crashreportsystemdetails.a("w", Integer.valueOf(k));
            crashreportsystemdetails.a("h", Integer.valueOf(l));
            throw new ReportedException(crashreport);
        }
    }

    public BiomeBase[] getBiomeBlock(@Nullable BiomeBase[] abiomebase, int i, int j, int k, int l) {
        return this.a(abiomebase, i, j, k, l, true);
    }

    /** Gets a list of biomes for the specified blocks */
    public BiomeBase[] getBiomes(@Nullable BiomeBase[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) { return this.a(listToReuse, x, z, width, length, cacheFlag); } // OBFHELPER
    public BiomeBase[] a(@Nullable BiomeBase[] abiomebase, int i, int j, int k, int l, boolean flag) {
        TorchIntCache.releaseCaches();
        if (abiomebase == null || abiomebase.length < k * l) {
            abiomebase = new BiomeBase[k * l];
        }

        if (flag && k == 16 && l == 16 && (i & 15) == 0 && (j & 15) == 0) {
            BiomeBase[] abiomebase1 = this.d.b(i, j);

            System.arraycopy(abiomebase1, 0, abiomebase, 0, k * l);
            return abiomebase;
        } else {
            int[] aint = this.c.a(i, j, k, l);

            for (int i1 = 0; i1 < k * l; ++i1) {
                abiomebase[i1] = BiomeBase.getBiome(aint[i1], Biomes.b);
            }

            return abiomebase;
        }
    }

    public boolean a(int i, int j, int k, List<BiomeBase> list) {
        TorchIntCache.releaseCaches();
        int l = i - k >> 2;
        int i1 = j - k >> 2;
        int j1 = i + k >> 2;
        int k1 = j + k >> 2;
        int l1 = j1 - l + 1;
        int i2 = k1 - i1 + 1;
        int[] aint = this.b.a(l, i1, l1, i2);

        try {
            for (int j2 = 0; j2 < l1 * i2; ++j2) {
                BiomeBase biomebase = BiomeBase.getBiome(aint[j2]);

                if (!list.contains(biomebase)) {
                    return false;
                }
            }

            return true;
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Invalid Biome id");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Layer");

            crashreportsystemdetails.a("Layer", this.b.toString());
            crashreportsystemdetails.a("x", Integer.valueOf(i));
            crashreportsystemdetails.a("z", Integer.valueOf(j));
            crashreportsystemdetails.a("radius", Integer.valueOf(k));
            crashreportsystemdetails.a("allowed", list);
            throw new ReportedException(crashreport);
        }
    }

    @Nullable
    public BlockPosition a(int i, int j, int k, List<BiomeBase> list, Random random) {
        TorchIntCache.releaseCaches();
        int l = i - k >> 2;
        int i1 = j - k >> 2;
        int j1 = i + k >> 2;
        int k1 = j + k >> 2;
        int l1 = j1 - l + 1;
        int i2 = k1 - i1 + 1;
        int[] aint = this.b.a(l, i1, l1, i2);
        BlockPosition blockposition = null;
        int j2 = 0;

        for (int k2 = 0; k2 < l1 * i2; ++k2) {
            int l2 = l + k2 % l1 << 2;
            int i3 = i1 + k2 / l1 << 2;
            BiomeBase biomebase = BiomeBase.getBiome(aint[k2]);

            if (list.contains(biomebase) && (blockposition == null || random.nextInt(j2 + 1) == 0)) {
                blockposition = new BlockPosition(l2, 0, i3);
                ++j2;
            }
        }

        return blockposition;
    }

    public void b() {
        this.d.a();
    }

    public boolean c() {
        return this.a != null && this.a.G >= 0;
    }

    public BiomeBase d() {
        return this.a != null && this.a.G >= 0 ? BiomeBase.a(this.a.G) : null;
    }
}
