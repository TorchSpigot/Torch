package net.minecraft.server;

import com.google.common.collect.Lists;
import com.koloboke.collect.map.hash.HashObjObjMaps;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;
import org.torch.server.Caches;

import static org.torch.server.TorchServer.logger;

public class NBTTagCompound extends NBTBase {

    private static final Logger b = logger;
    public final Map<String, NBTBase> map = HashObjObjMaps.newMutableMap(); // Paper

    public NBTTagCompound() {}

    @Override
    void write(DataOutput output) throws IOException {
        for (Entry<String, NBTBase> entry : this.map.entrySet()) {
            a(entry.getKey(), entry.getValue(), output); // PAIL: writeEntry
        }

        output.writeByte(0);
    }

    @Override
    void load(DataInput input, int depth, NBTReadLimiter sizeLimiter) throws IOException {
        if (depth > 512) throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        
        sizeLimiter.a(384L); // PAIL: read
        this.map.clear();
        
        byte type;
        while ((type = a(input)) != 0) {
            String key = b(input); // PAIL: readKey
            
            sizeLimiter.a(224 + 16 * key.length()); // PAIL: read
            NBTBase nbt = a(type, key, input, depth + 1, sizeLimiter); // PAIL: readNBT
            
            if (this.map.put(key, nbt) != null) sizeLimiter.a(288L); // PAIL: read
        }
    }

    public Set<String> c() {
        return this.map.keySet();
    }

    @Override
    public byte getTypeId() {
        return (byte) 10;
    }

    public int d() {
        return this.map.size();
    }

    public void set(String s, NBTBase nbtbase) {
        this.map.put(s, nbtbase);
    }

    public void setByte(String s, byte b0) {
        this.map.put(s, new NBTTagByte(b0));
    }

    public void setShort(String s, short short0) {
        this.map.put(s, new NBTTagShort(short0));
    }

    public void setInt(String s, int i) {
        this.map.put(s, new NBTTagInt(i));
    }

    public void setLong(String s, long i) {
        this.map.put(s, new NBTTagLong(i));
    }

    public void setUUID(String prefix, UUID uuid) { a(prefix, uuid); } // Paper - OBFHELPER
    public void a(String s, UUID uuid) {
        this.setLong(s + "Most", uuid.getMostSignificantBits());
        this.setLong(s + "Least", uuid.getLeastSignificantBits());
    }

    public UUID getUUID(String prefix) { return a(prefix); } // Paper - OBFHELPER
    @Nullable
    public UUID a(String s) {
        return new UUID(this.getLong(s + "Most"), this.getLong(s + "Least"));
    }

    public boolean hasUUID(String s) { return b(s); } public boolean b(String s) { // Paper - OBFHELPER
        return this.hasKeyOfType(s + "Most", 99) && this.hasKeyOfType(s + "Least", 99);
    }

    public void setFloat(String s, float f) {
        this.map.put(s, new NBTTagFloat(f));
    }

    public void setDouble(String s, double d0) {
        this.map.put(s, new NBTTagDouble(d0));
    }

    public void setString(String s, String s1) {
        this.map.put(s, new NBTTagString(s1));
    }

    public void setByteArray(String s, byte[] abyte) {
        this.map.put(s, new NBTTagByteArray(abyte));
    }

    public void setIntArray(String s, int[] aint) {
        this.map.put(s, new NBTTagIntArray(aint));
    }

    public void setBoolean(String s, boolean flag) {
        this.setByte(s, (byte) (flag ? 1 : 0));
    }

    public NBTBase get(String s) {
        return this.map.get(s);
    }

    public byte d(String key) {
        NBTBase nbt = this.map.get(key);

        return nbt == null ? 0 : nbt.getTypeId();
    }

    public boolean hasKey(String s) {
        return this.map.containsKey(s);
    }

    /**
     * Returns whether the given string has been previously stored as a key in this tag compound as a particular type,
     * denoted by a parameter in the form of an ordinal.
     * If the provided ordinal is 99, this method will match tag types representing numbers.
     */
    public boolean hasKeyOfType(String key, int type) {
        byte id = this.d(key); // PAIL: d -> getTagId

        return id == type ? true : (type != 99 ? false : id == 1 || id == 2 || id == 3 || id == 4 || id == 5 || id == 6);
    }

    public byte getByte(String s) {
        if (this.hasKeyOfType(s, 99)) {
            return ((NBTNumber) this.map.get(s)).g();
        }

        return (byte) 0;
    }

    public short getShort(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).f();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return (short) 0;
    }

    public int getInt(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).e();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return 0;
    }

    public long getLong(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).d();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return 0L;
    }

    public float getFloat(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).i();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return 0.0F;
    }

    public double getDouble(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).asDouble();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return 0.0D;
    }

    public String getString(String s) {
        try {
            if (this.hasKeyOfType(s, 8)) {
                return this.map.get(s).c_();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return "";
    }

    public byte[] getByteArray(String s) {
        try {
            if (this.hasKeyOfType(s, 7)) {
                return ((NBTTagByteArray) this.map.get(s)).c();
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, 7, classcastexception));
        }

        return new byte[0];
    }

    public int[] getIntArray(String s) {
        try {
            if (this.hasKeyOfType(s, 11)) {
                return ((NBTTagIntArray) this.map.get(s)).d();
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, 11, classcastexception));
        }

        return new int[0];
    }

    public NBTTagCompound getCompound(String s) {
        try {
            if (this.hasKeyOfType(s, 10)) {
                return (NBTTagCompound) this.map.get(s);
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, 10, classcastexception));
        }

        return new NBTTagCompound();
    }

    public NBTTagList getList(String s, int i) {
        try {
            if (this.d(s) == 9) {
                NBTTagList nbttaglist = (NBTTagList) this.map.get(s);

                if (!nbttaglist.isEmpty() && nbttaglist.g() != i) {
                    return new NBTTagList();
                }

                return nbttaglist;
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, 9, classcastexception));
        }

        return new NBTTagList();
    }

    public boolean getBoolean(String s) {
        return this.getByte(s) != 0;
    }

    public void remove(String s) {
        this.map.remove(s);
    }

    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("{");
        Object object = this.map.keySet();

        if (logger.isDebugEnabled()) {
            ArrayList keys = Lists.newArrayList(object);

            Collections.sort(keys);
            object = Lists.newArrayList(this.map.keySet());
        }
        
        String s;
        for (Iterator iterator = ((Collection) object).iterator(); iterator.hasNext(); stringbuilder.append(s).append(':').append(this.map.get(s))) {
            s = (String) iterator.next();
            if (stringbuilder.length() != 1) {
                stringbuilder.append(',');
            }
        }
        
        return stringbuilder.append('}').toString();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    /**
     * <b>PAIL: createCrashReport</b>
     * <p>
     * Create a crash report which indicates a NBT read error.
     */
    private CrashReport a(final String s, final int i, ClassCastException ex) {
        CrashReport report = CrashReport.a(ex, "Reading NBT data");
        CrashReportSystemDetails details = report.a("Corrupt NBT tag", 1);

        details.a("Tag type found", new CrashReportCallable<String>() {
            @Override
            public String call() throws Exception {
                return NBTBase.a[NBTTagCompound.this.map.get(s).getTypeId()];
            }
        });
        details.a("Tag type expected", new CrashReportCallable<String>() {
            @Override
            public String call() throws Exception {
                return NBTBase.a[i];
            }
        });
        details.a("Tag name", s);
        return report;
    }

    /**
     * <b>PAIL: copy</b>
     * <p>
     * Creates a clone of the tag.
     */
    public NBTTagCompound g() {
        NBTTagCompound copy = new NBTTagCompound();
        for (String each : this.map.keySet()) {
            copy.set(each, this.map.get(each).clone());
        }
        
        return copy;
    }

    @Override
    public boolean equals(Object nbt) {
        if (super.equals(nbt)) {
            NBTTagCompound nbttagcompound = (NBTTagCompound) nbt;

            return this.map.entrySet().equals(nbttagcompound.map.entrySet());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.map.hashCode();
    }

    /**
     * <b>PAIL: writeEntry</b>
     */
    private static void a(String key, NBTBase data, DataOutput output) throws IOException {
        output.writeByte(data.getTypeId());
        if (data.getTypeId() != 0) {
            output.writeUTF(key);
            data.write(output);
        }
    }

    /**
     * <b>PAIL: readType</b>
     */
    private static byte a(DataInput input /*, NBTReadLimiter sizeLimiter*/) throws IOException {
        return input.readByte();
    }

    /**
     * <b>PAIL: readKey</b>
     */
    private static String b(DataInput input /*, NBTReadLimiter sizeLimiter*/) throws IOException {
        return input.readUTF();
    }

    /**
     * <b>PAIL: readNBT</b>
     */
    static NBTBase a(byte id, String key, DataInput input, int depth, NBTReadLimiter sizeLimiter) throws IOException {
        NBTBase newTag = NBTBase.createTag(id);

        try {
            newTag.load(input, depth, sizeLimiter);
            return newTag;
        } catch (IOException io) {
            CrashReport report = CrashReport.a(io, "Loading NBT data");
            CrashReportSystemDetails details = report.a("NBT Tag");

            details.a("Tag name", key);
            details.a("Tag type", Byte.valueOf(id));
            throw new ReportedException(report);
        }
    }

    /**
     * <b>PAIL: merge</b>
     * <p>
     * Merges this NBTTagCompound with the given compound.
     * Any sub-compounds are merged using the same methods, other types of tags are overwritten from the given compound.
     */
    public void a(NBTTagCompound other) {
        String key; NBTBase nbt;
        
        for (Entry<String, NBTBase> entry : other.map.entrySet()) {
            key = entry.getKey();
            nbt = entry.getValue();
            
            if (nbt.getTypeId() == 10) {
                if (this.hasKeyOfType(key, 10)) {
                    NBTTagCompound sub = this.getCompound(key);

                    sub.a((NBTTagCompound) nbt); // PAIL: merge
                } else {
                    this.set(key, nbt.clone());
                }
                
            } else {
                this.set(key, nbt.clone());
            }
        }
    }

    @Override
    public NBTBase clone() {
        return this.g();
    }
}
