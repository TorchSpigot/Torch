package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start
import java.util.UUID;
import org.bukkit.craftbukkit.entity.CraftPlayer;
// CraftBukkit end

public class WorldNBTStorage implements IDataManager, IPlayerFileData {

    private static final Logger b = LogManager.getLogger();
    private final File baseDir;
    private final File playerDir;
    private final File dataDir;
    private final long sessionId = MinecraftServer.aw();
    private final String g;
    private final DefinedStructureManager h;
    protected final DataConverterManager a;
    private UUID uuid = null; // CraftBukkit

    public WorldNBTStorage(File file, String s, boolean flag, DataConverterManager dataconvertermanager) {
        this.a = dataconvertermanager;
        this.baseDir = new File(file, s);
        this.baseDir.mkdirs();
        this.playerDir = new File(this.baseDir, "playerdata");
        this.dataDir = new File(this.baseDir, "data");
        this.dataDir.mkdirs();
        this.g = s;
        if (flag) {
            this.playerDir.mkdirs();
            this.h = new DefinedStructureManager((new File(this.baseDir, "structures")).toString(), dataconvertermanager);
        } else {
            this.h = null;
        }

        this.i();
    }

    private void i() {
        try {
            File file = new File(this.baseDir, "session.lock");
            DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file));

            try {
                dataoutputstream.writeLong(this.sessionId);
            } finally {
                dataoutputstream.close();
            }

        } catch (IOException ioexception) {
            ioexception.printStackTrace();
            throw new RuntimeException("Failed to check session lock for world located at " + this.baseDir + ", aborting. Stop the server and delete the session.lock in this world to prevent further issues."); // Spigot
        }
    }

    @Override
    public File getDirectory() {
        return this.baseDir;
    }

    @Override
    public void checkSession() throws ExceptionWorldConflict {
        try {
            File file = new File(this.baseDir, "session.lock");
            DataInputStream datainputstream = new DataInputStream(new FileInputStream(file));

            try {
                if (datainputstream.readLong() != this.sessionId) {
                    throw new ExceptionWorldConflict("The save for world located at " + this.baseDir + " is being accessed from another location, aborting");  // Spigot
                }
            } finally {
                datainputstream.close();
            }

        } catch (IOException ioexception) {
            throw new ExceptionWorldConflict("Failed to check session lock for world located at " + this.baseDir + ", aborting. Stop the server and delete the session.lock in this world to prevent further issues."); // Spigot
        }
    }

    @Override
    public IChunkLoader createChunkLoader(WorldProvider worldprovider) {
        throw new RuntimeException("Old Chunk Storage is no longer supported.");
    }

    @Override
    @Nullable
    public WorldData getWorldData() {
        File file = new File(this.baseDir, "level.dat");

        if (file.exists()) {
            WorldData worlddata = WorldLoader.a(file, this.a);

            if (worlddata != null) {
                return worlddata;
            }
        }

        file = new File(this.baseDir, "level.dat_old");
        return file.exists() ? WorldLoader.a(file, this.a) : null;
    }

    @Override
    public void saveWorldData(WorldData worlddata, @Nullable NBTTagCompound nbttagcompound) {
        NBTTagCompound nbttagcompound1 = worlddata.a(nbttagcompound);
        NBTTagCompound nbttagcompound2 = new NBTTagCompound();

        nbttagcompound2.set("Data", nbttagcompound1);

        try {
            File file = new File(this.baseDir, "level.dat_new");
            File file1 = new File(this.baseDir, "level.dat_old");
            File file2 = new File(this.baseDir, "level.dat");

            NBTCompressedStreamTools.a(nbttagcompound2, (new FileOutputStream(file)));
            if (file1.exists()) {
                file1.delete();
            }

            file2.renameTo(file1);
            if (file2.exists()) {
                file2.delete();
            }

            file.renameTo(file2);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public void saveWorldData(WorldData worlddata) {
        this.saveWorldData(worlddata, (NBTTagCompound) null);
    }

    @Override
    public void save(EntityHuman entityhuman) {
        try {
            NBTTagCompound nbttagcompound = entityhuman.e(new NBTTagCompound());
            File file = new File(this.playerDir, entityhuman.bf() + ".dat.tmp");
            File file1 = new File(this.playerDir, entityhuman.bf() + ".dat");

            NBTCompressedStreamTools.a(nbttagcompound, (new FileOutputStream(file)));
            if (file1.exists()) {
                file1.delete();
            }

            file.renameTo(file1);
        } catch (Exception exception) {
            WorldNBTStorage.b.warn("Failed to save player data for {}", new Object[] { entityhuman.getName()});
        }

    }

    @Override
    @Nullable
    public NBTTagCompound load(EntityHuman entityhuman) {
        NBTTagCompound nbttagcompound = null;

        try {
            File file = new File(this.playerDir, entityhuman.bf() + ".dat");
            // Spigot Start
            boolean usingWrongFile = false;
            if (org.bukkit.Bukkit.getOnlineMode() && !file.exists()) { // Paper - Check online mode first
                file = new File(this.playerDir, EntityHuman.offlinePlayerUUID(entityhuman.getName(), false) + ".dat");
                if (file.exists()) {
                    usingWrongFile = true;
                    org.bukkit.Bukkit.getServer().getLogger().warning( "Using offline mode UUID file for player " + entityhuman.getName() + " as it is the only copy we can find." );
                } else {
                    file = new File(this.playerDir, EntityHuman.offlinePlayerUUID(entityhuman.getName()) + ".dat");
                    if (file.exists()) {
                        usingWrongFile = true;
                        org.bukkit.Bukkit.getServer().getLogger().warning( "Using offline mode UUID file for player " + entityhuman.getName() + " as it is the only copy we can find." );
                    }
                }
            }
            // Spigot End

            if (file.exists() && file.isFile()) {
                nbttagcompound = NBTCompressedStreamTools.a((new FileInputStream(file)));
            }
            // Spigot Start
            if (usingWrongFile) {
                file.renameTo( new File( file.getPath() + ".offline-read" ) );
            }
            // Spigot End
        } catch (Exception exception) {
            WorldNBTStorage.b.warn("Failed to load player data for {}", new Object[] { entityhuman.getName()});
        }

        if (nbttagcompound != null) {
            // CraftBukkit start
            if (entityhuman instanceof EntityPlayer) {
                CraftPlayer player = (CraftPlayer) entityhuman.getBukkitEntity();
                // Only update first played if it is older than the one we have
                long modified = new File(this.playerDir, entityhuman.getUniqueID().toString() + ".dat").lastModified();
                if (modified < player.getFirstPlayed()) {
                    player.setFirstPlayed(modified);
                }
            }
            // CraftBukkit end
            entityhuman.f(this.a.a(DataConverterTypes.PLAYER, nbttagcompound));
        }

        return nbttagcompound;
    }

    // CraftBukkit start
    public NBTTagCompound getPlayerData(String s) {
        try {
            File file1 = new File(this.playerDir, s + ".dat");

            if (file1.exists()) {
                return NBTCompressedStreamTools.a((new FileInputStream(file1)));
            }
        } catch (Exception exception) {
            b.warn("Failed to load player data for " + s);
        }

        return null;
    }
    // CraftBukkit end

    @Override
    public IPlayerFileData getPlayerFileData() {
        return this;
    }

    @Override
    public String[] getSeenPlayers() {
        String[] astring = this.playerDir.list();

        if (astring == null) {
            astring = new String[0];
        }

        for (int i = 0; i < astring.length; ++i) {
            if (astring[i].endsWith(".dat")) {
                astring[i] = astring[i].substring(0, astring[i].length() - 4);
            }
        }

        return astring;
    }

    @Override
    public void a() {}

    @Override
    public File getDataFile(String s) {
        return new File(this.dataDir, s + ".dat");
    }

    @Override
    public DefinedStructureManager h() {
        return this.h;
    }

    // CraftBukkit start
    @Override
    public UUID getUUID() {
        if (uuid != null) return uuid;
        File file1 = new File(this.baseDir, "uid.dat");
        if (file1.exists()) {
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(new FileInputStream(file1));
                return uuid = new UUID(dis.readLong(), dis.readLong());
            } catch (IOException ex) {
                b.warn("Failed to read " + file1 + ", generating new random UUID", ex);
            } finally {
                if (dis != null) {
                    try {
                        dis.close();
                    } catch (IOException ex) {
                        // NOOP
                    }
                }
            }
        }
        uuid = UUID.randomUUID();
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(file1));
            dos.writeLong(uuid.getMostSignificantBits());
            dos.writeLong(uuid.getLeastSignificantBits());
        } catch (IOException ex) {
            b.warn("Failed to write " + file1, ex);
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException ex) {
                    // NOOP
                }
            }
        }
        return uuid;
    }

    public File getPlayerDir() {
        return playerDir;
    }
    // CraftBukkit end
}
