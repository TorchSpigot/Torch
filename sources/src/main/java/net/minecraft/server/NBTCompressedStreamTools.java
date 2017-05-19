package net.minecraft.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NBTCompressedStreamTools {

    public static NBTTagCompound a(InputStream inputstream) throws IOException {
        DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(inputstream)));

        NBTTagCompound nbttagcompound;

        try {
            nbttagcompound = a(datainputstream, NBTReadLimiter.a);
        } finally {
            datainputstream.close();
        }

        return nbttagcompound;
    }

    /**
     * <b>PAIL: writeCompressed</b>
     * <p>
     * Write the compound, gzipped, to the output-stream
     */
    public static void a(NBTTagCompound compound, OutputStream outputStream) throws IOException {
        DataOutputStream dataOutput = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputStream)));

        try {
            a(compound, (DataOutput) dataOutput); // PAIL: write
        } finally {
            dataOutput.close();
        }
    }

    public static NBTTagCompound a(DataInputStream datainputstream) throws IOException {
        return a(datainputstream, NBTReadLimiter.a);
    }

    public static NBTTagCompound a(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
        // Spigot start
        if ( datainput instanceof io.netty.buffer.ByteBufInputStream )
        {
            datainput = new DataInputStream(new org.spigotmc.LimitStream((InputStream) datainput, nbtreadlimiter));
        }
        // Spigot end
        NBTBase nbtbase = a(datainput, 0, nbtreadlimiter);

        if (nbtbase instanceof NBTTagCompound) {
            return (NBTTagCompound) nbtbase;
        } else {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    /** PAIL: write */
    public static void a(NBTTagCompound compound, DataOutput dataOutput) throws IOException {
        a((NBTBase) compound, dataOutput); // PAIL: writeTag
    }

    /** PAIL: writeTag */
    private static void a(NBTBase tag, DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(tag.getTypeId());
        
        if (tag.getTypeId() != 0) {
            dataOutput.writeUTF("");
            tag.write(dataOutput);
        }
    }

    private static NBTBase a(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        byte b0 = datainput.readByte();

        if (b0 == 0) {
            return new NBTTagEnd();
        } else {
            datainput.readUTF();
            NBTBase nbtbase = NBTBase.createTag(b0);

            try {
                nbtbase.load(datainput, i, nbtreadlimiter);
                return nbtbase;
            } catch (IOException ioexception) {
                CrashReport crashreport = CrashReport.a(ioexception, "Loading NBT data");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("NBT Tag");

                crashreportsystemdetails.a("Tag name", "[UNNAMED TAG]");
                crashreportsystemdetails.a("Tag type", Byte.valueOf(b0));
                throw new ReportedException(crashreport);
            }
        }
    }
}
