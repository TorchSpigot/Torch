package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap; // Paper
import org.apache.commons.lang3.ObjectUtils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.techcable.tacospigot.ArrayMap;
import net.techcable.tacospigot.NoOpReadWriteLock;
import net.techcable.tacospigot.TacoSpigotConfig;

import com.google.common.base.Optional;

import net.techcable.tacospigot.TacoSpigotConfig;

public class DataWatcher {

    private static final Map<Class<? extends Entity>, Integer> a = Maps.newConcurrentMap();
    private final Entity b;
    private final Map<Integer, DataWatcher.Item<?>> c = new Int2ObjectOpenHashMap<>(); // Paper
    private final ReadWriteLock d = new ReentrantReadWriteLock(); // Torch - do not backport this cause some inconsistencies
    private boolean e = true;
    private boolean f;

    public DataWatcher(Entity entity) {
        this.b = entity;
    }

    public static <T> DataWatcherObject<T> a(Class<? extends Entity> oclass, DataWatcherSerializer<T> datawatcherserializer) {
        int i;

        if (DataWatcher.a.containsKey(oclass)) {
            i = ((Integer) DataWatcher.a.get(oclass)).intValue() + 1;
        } else {
            int j = 0;
            Class oclass1 = oclass;

            while (oclass1 != Entity.class) {
                oclass1 = oclass1.getSuperclass();
                if (DataWatcher.a.containsKey(oclass1)) {
                    j = ((Integer) DataWatcher.a.get(oclass1)).intValue() + 1;
                    break;
                }
            }

            i = j;
        }

        if (i > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + 254 + ")");
        } else {
            DataWatcher.a.put(oclass, Integer.valueOf(i));
            return datawatcherserializer.a(i);
        }
    }

    public <T> void register(DataWatcherObject<T> datawatcherobject, Object t0) { // CraftBukkit T -> Object
        int i = datawatcherobject.a();

        if (i > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + 254 + ")");
        } else if (this.c.containsKey(Integer.valueOf(i))) {
            throw new IllegalArgumentException("Duplicate id value for " + i + "!");
        } else if (DataWatcherRegistry.b(datawatcherobject.b()) < 0) {
            throw new IllegalArgumentException("Unregistered serializer " + datawatcherobject.b() + " for " + i + "!");
        } else {
            this.registerObject(datawatcherobject, t0);
        }
    }

    private <T> void registerObject(DataWatcherObject<T> datawatcherobject, Object t0) { // CraftBukkit Object
        DataWatcher.Item datawatcher_item = new DataWatcher.Item(datawatcherobject, t0);

        this.d.writeLock().lock();
        this.c.put(Integer.valueOf(datawatcherobject.a()), datawatcher_item);
        this.e = false;
        this.d.writeLock().unlock();
    }

    private <T> DataWatcher.Item<T> c(DataWatcherObject<T> datawatcherobject) {
        this.d.readLock().lock();

        DataWatcher.Item datawatcher_item;

        try {
            datawatcher_item = (DataWatcher.Item) this.c.get(Integer.valueOf(datawatcherobject.a()));
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Getting synched entity data");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Synched entity data");

            crashreportsystemdetails.a("Data ID", (Object) datawatcherobject);
            throw new ReportedException(crashreport);
        }

        this.d.readLock().unlock();
        return datawatcher_item;
    }

    public <T> T get(DataWatcherObject<T> datawatcherobject) {
        return this.c(datawatcherobject).b();
    }

    public <T> void set(DataWatcherObject<T> datawatcherobject, T t0) {
        DataWatcher.Item datawatcher_item = this.c(datawatcherobject);

        if (ObjectUtils.notEqual(t0, datawatcher_item.b())) {
            datawatcher_item.a(t0);
            this.b.a(datawatcherobject);
            datawatcher_item.a(true);
            this.f = true;
        }

    }

    public <T> void markDirty(DataWatcherObject<T> datawatcherobject) {
        this.c(datawatcherobject).c = true;
        this.f = true;
    }

    public boolean a() {
        return this.f;
    }

    public static void a(List<DataWatcher.Item<?>> list, PacketDataSerializer packetdataserializer) throws IOException {
        if (list != null) {
            int i = 0;

            for (int j = list.size(); i < j; ++i) {
                DataWatcher.Item datawatcher_item = (DataWatcher.Item) list.get(i);

                a(packetdataserializer, datawatcher_item);
            }
        }

        packetdataserializer.writeByte(255);
    }

    @Nullable
    public List<DataWatcher.Item<?>> b() {
        ArrayList arraylist = null;

        if (this.f) {
            this.d.readLock().lock();
            Iterator iterator = this.c.values().iterator();

            while (iterator.hasNext()) {
                DataWatcher.Item datawatcher_item = (DataWatcher.Item) iterator.next();

                if (datawatcher_item.c()) {
                    datawatcher_item.a(false);
                    if (arraylist == null) {
                        arraylist = Lists.newArrayList();
                    }

                    arraylist.add(datawatcher_item);
                }
            }

            this.d.readLock().unlock();
        }

        this.f = false;
        return arraylist;
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        this.d.readLock().lock();
        Iterator iterator = this.c.values().iterator();

        while (iterator.hasNext()) {
            DataWatcher.Item datawatcher_item = (DataWatcher.Item) iterator.next();

            a(packetdataserializer, datawatcher_item);
        }

        this.d.readLock().unlock();
        packetdataserializer.writeByte(255);
    }

    @Nullable
    public List<DataWatcher.Item<?>> c() {
        ArrayList arraylist = null;

        this.d.readLock().lock();

        DataWatcher.Item datawatcher_item;

        for (Iterator iterator = this.c.values().iterator(); iterator.hasNext(); arraylist.add(datawatcher_item)) {
            datawatcher_item = (DataWatcher.Item) iterator.next();
            if (arraylist == null) {
                arraylist = Lists.newArrayList();
            }
        }

        this.d.readLock().unlock();
        return arraylist;
    }

    private static <T> void a(PacketDataSerializer packetdataserializer, DataWatcher.Item<T> datawatcher_item) throws IOException {
        DataWatcherObject datawatcherobject = datawatcher_item.a();
        int i = DataWatcherRegistry.b(datawatcherobject.b());

        if (i < 0) {
            throw new EncoderException("Unknown serializer type " + datawatcherobject.b());
        } else {
            packetdataserializer.writeByte(datawatcherobject.a());
            packetdataserializer.d(i);
            datawatcherobject.b().a(packetdataserializer, datawatcher_item.b());
        }
    }

    @Nullable
    public static List<DataWatcher.Item<?>> b(PacketDataSerializer packetdataserializer) throws IOException {
        ArrayList arraylist = null;

        short short0;

        while ((short0 = packetdataserializer.readUnsignedByte()) != 255) {
            if (arraylist == null) {
                arraylist = Lists.newArrayList();
            }

            int i = packetdataserializer.g();
            DataWatcherSerializer datawatcherserializer = DataWatcherRegistry.a(i);

            if (datawatcherserializer == null) {
                throw new DecoderException("Unknown serializer type " + i);
            }

            arraylist.add(new DataWatcher.Item(datawatcherserializer.a(short0), datawatcherserializer.a(packetdataserializer)));
        }

        return arraylist;
    }

    public boolean d() {
        return this.e;
    }

    public void e() {
        this.f = false;
    }

    public static class Item<T> {

        private final DataWatcherObject<T> a;
        private T b;
        private boolean c;

        public Item(DataWatcherObject<T> datawatcherobject, T t0) {
            this.a = datawatcherobject;
            this.b = t0;
            this.c = true;
        }

        public DataWatcherObject<T> a() {
            return this.a;
        }

        public void a(T t0) {
            this.b = t0;
        }

        public T b() {
            return this.b;
        }

        public boolean c() {
            return this.c;
        }

        public void a(boolean flag) {
            this.c = flag;
        }
    }
}
