package net.minecraft.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagString extends NBTBase {

    private String data;

    public NBTTagString() {
        this.data = "";
    }

    public NBTTagString(String s) {
        this.data = s;
        if (s == null) {
            throw new IllegalArgumentException("Empty string not allowed");
        }
    }

    @Override
	void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeUTF(this.data);
    }

    @Override
	void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        nbtreadlimiter.a(288L);
        this.data = datainput.readUTF();
        nbtreadlimiter.a(16 * this.data.length());
    }

    @Override
	public byte getTypeId() {
        return (byte) 8;
    }

    @Override
	public String toString() {
        return "\"" + this.data.replace("\"", "\\\"") + "\"";
    }

    public NBTTagString c() {
        return new NBTTagString(this.data);
    }

    @Override
	public boolean isEmpty() {
        return this.data.isEmpty();
    }

    @Override
	public boolean equals(Object object) {
        if (!super.equals(object)) {
            return false;
        } else {
            NBTTagString nbttagstring = (NBTTagString) object;

            return this.data == null && nbttagstring.data == null || this.data != null && this.data.equals(nbttagstring.data);
        }
    }

    @Override
	public int hashCode() {
        return super.hashCode() ^ this.data.hashCode();
    }

    @Override
	public String c_() {
        return this.data;
    }

    @Override
	public NBTBase clone() {
        return this.c();
    }
}
