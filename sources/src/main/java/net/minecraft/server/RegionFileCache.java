package net.minecraft.server;

import com.destroystokyo.paper.exception.ServerInternalException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import com.destroystokyo.paper.PaperConfig; // Paper

public class RegionFileCache {

    // public static final Map<File, RegionFile> a = new LinkedHashMap<File, RegionFile>(PaperConfig.regionFileCacheSize, 0.75f, true); // Spigot - private -> public, Paper - HashMap -> LinkedHashMap
    public static final Cache<File, RegionFile> regions = Caffeine.newBuilder().initialCapacity(PaperConfig.regionFileCacheSize).build();

    /** PAIL: createOrLoadRegionFile */
    public static /*synchronized*/ RegionFile a(File worldDir, int chunkX, int chunkZ) {
        File regionDir = new File(worldDir, "region");
        File regionFile = new File(regionDir, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca");
        RegionFile region = regions.getIfPresent(regionFile);

        if (region != null) {
            return region;
        } else {
            if (!regionDir.exists()) {
                regionDir.mkdirs();
            }
            
            if (regions.asMap().size() >= PaperConfig.regionFileCacheSize) {
                trimCache(); // Paper
            }
            
            region = new RegionFile(regionFile);
            
            regions.put(regionFile, region);
            return region;
        }
    }

    /** PAIL: getRegionFileIfExists */
    public static /*synchronized*/ RegionFile b(File worldDir, int i, int j) {
        File regionDir = new File(worldDir, "region");
        File regionFile = new File(regionDir, "r." + (i >> 5) + "." + (j >> 5) + ".mca");
        RegionFile region = regions.getIfPresent(regionFile);

        if (region != null) {
            return region;
        } else if (regionDir.exists() && regionFile.exists()) {
            if (regions.asMap().size() >= PaperConfig.regionFileCacheSize) {
                trimCache();
            }
            
            region = new RegionFile(regionFile);
            
            regions.put(regionFile, region);
            return region;
        } else {
            return null;
        }
    }
    
    public static void a() { trimCache(); } // OBFHELPER
    /**
     * <b>PAIL: clearRegionFileReferences</b>
     * <p>clears region file references
     */
    public static /*synchronized*/ void trimCache() {
        try {
            for (RegionFile region : regions.asMap().values()) {
                if (region != null) region.close();
            }
        } catch (IOException io) {
            io.printStackTrace();
            ServerInternalException.reportInternalException(io); // Paper
        }
        
        regions.invalidateAll();
    }

    // CraftBukkit start - call sites hoisted for synchronization
    public static synchronized NBTTagCompound d(File file, int i, int j) throws IOException {
        RegionFile regionfile = a(file, i, j); // PAIL: createOrLoadRegionFile

        DataInputStream input = regionfile.a(i & 31, j & 31);
        if (input == null) return null;
        
        return NBTCompressedStreamTools.a(input);
    }

    public static synchronized void e(File file, int i, int j, NBTTagCompound compound) throws IOException {
        RegionFile regionfile = a(file, i, j); // PAIL: createOrLoadRegionFile

        DataOutputStream output = regionfile.b(i & 31, j & 31);
        NBTCompressedStreamTools.a(compound, (java.io.DataOutput) output);
        output.close();
    }
    // CraftBukkit end

    public static boolean chunkExists(File file, int x, int z) { return f(file, x, z); } // OBFHELPER
    public static synchronized boolean f(File worldDir, int x, int z) { // CraftBukkit
        RegionFile regionfile = b(worldDir, x, z); // PAIL: getRegionFileIfExists
        
        return regionfile != null ? regionfile.c(x & 31, z & 31) : false; // PAIL: c -> isChunkSaved
    }
}
