package net.minecraft.server;

import com.destroystokyo.paper.exception.ServerInternalException;
import com.google.common.collect.Maps;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import com.destroystokyo.paper.PaperConfig; // Paper
import java.util.LinkedHashMap; // Paper

public class RegionFileCache {

    public static final Map<File, RegionFile> a = new LinkedHashMap(PaperConfig.regionFileCacheSize, 0.75f, true); // Spigot - private -> public, Paper - HashMap -> LinkedHashMap

    // Paper start
    public static synchronized RegionFile a(File file, int i, int j) {
        return a(file, i, j, true);
    }

    public static synchronized RegionFile a(File file, int i, int j, boolean create) {
        // Paper end
        File file1 = new File(file, "region");
        File file2 = new File(file1, "r." + (i >> 5) + "." + (j >> 5) + ".mca");
        RegionFile regionfile = (RegionFile) RegionFileCache.a.get(file2);

        if (regionfile != null) {
            return regionfile;
        } else {
            if (!create && !file2.exists()) { return null; } // Paper
            if (!file1.exists()) {
                file1.mkdirs();
            }

            if (RegionFileCache.a.size() >= PaperConfig.regionFileCacheSize) { // Paper
                trimCache(); // Paper
            }

            RegionFile regionfile1 = new RegionFile(file2);

            RegionFileCache.a.put(file2, regionfile1);
            return regionfile1;
        }
    }

    // Paper Start
    private static synchronized void trimCache() {
        Iterator<Map.Entry<File, RegionFile>> itr = RegionFileCache.a.entrySet().iterator();
        int count = RegionFileCache.a.size() - PaperConfig.regionFileCacheSize;
        while (count-- >= 0 && itr.hasNext()) {
            try {
                itr.next().getValue().c();
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
                ServerInternalException.reportInternalException(ioexception);
            }
            itr.remove();
        }
    }
    // Paper End

    public static synchronized void a() {
        Iterator iterator = RegionFileCache.a.values().iterator();

        while (iterator.hasNext()) {
            RegionFile regionfile = (RegionFile) iterator.next();

            try {
                if (regionfile != null) {
                    regionfile.c();
                }
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
                ServerInternalException.reportInternalException(ioexception); // Paper
            }
        }

        RegionFileCache.a.clear();
    }

    public static DataInputStream c(File file, int i, int j) {
        RegionFile regionfile = a(file, i, j);

        return regionfile.a(i & 31, j & 31);
    }

    public static DataOutputStream d(File file, int i, int j) {
        RegionFile regionfile = a(file, i, j);

        return regionfile.b(i & 31, j & 31);
    }
}
