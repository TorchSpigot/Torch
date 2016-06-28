package net.minecraft.server;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class PacketPlayOutMapChunk implements Packet<PacketListenerPlayOut> {

    private int a;
    private int b;
    private int c;
    private byte[] d;
    private List<NBTTagCompound> e;
    private boolean f;
	private volatile boolean ready = false;

    // Paper start - Async-Anti-Xray - Set ready to true
    public PacketPlayOutMapChunk() {
        this.ready = true;
    }
    // Paper end

    public PacketPlayOutMapChunk(Chunk chunk, int i) {
        this.a = chunk.locX;
        this.b = chunk.locZ;
        this.f = i == '\uffff';
        boolean flag = !chunk.getWorld().worldProvider.m();

        this.d = new byte[this.a(chunk, flag, i)];
        chunk.world.paperConfig.antiXrayInstance.createPacket(this, new PacketDataSerializer(this.g()), chunk, flag, i);
        this.e = Lists.newArrayList();
        Iterator iterator = chunk.getTileEntities().entrySet().iterator();

        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            BlockPosition blockposition = (BlockPosition) entry.getKey();
            TileEntity tileentity = (TileEntity) entry.getValue();
            int j = blockposition.getY() >> 4;

            if (this.e() || (i & 1 << j) != 0) {
                NBTTagCompound nbttagcompound = tileentity.E_();

                this.e.add(nbttagcompound);
            }
        }

    }
	
	// Paper start - Async-Anti-Xray
public boolean isReady() { // Paper - Async-Anti-Xray - Used by the NetworkManager to check if the packet is sendable
    return this.ready;
}

public void setReady(boolean ready) {
    this.ready = ready;
}

public void setWrittenChunkSections(int writtenChunkSections) { // Paper - Async-Anti-Xray - Used to set this.c to the return value of this.a(PacketDataSerializer packetdataserializer, Chunk chunk, boolean flag, int i, Chunk[] nearbyChunks) because it is not directly possible as return value in async mode
    this.c = writtenChunkSections;
}
// Paper end

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        this.a = packetdataserializer.readInt();
        this.b = packetdataserializer.readInt();
        this.f = packetdataserializer.readBoolean();
        this.c = packetdataserializer.g();
        int i = packetdataserializer.g();

        if (i > 2097152) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        } else {
            this.d = new byte[i];
            packetdataserializer.readBytes(this.d);
            int j = packetdataserializer.g();

            this.e = Lists.newArrayList();

            for (int k = 0; k < j; ++k) {
                this.e.add(packetdataserializer.j());
            }

        }
    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.writeInt(this.a);
        packetdataserializer.writeInt(this.b);
        packetdataserializer.writeBoolean(this.f);
        packetdataserializer.d(this.c);
        packetdataserializer.d(this.d.length);
        packetdataserializer.writeBytes(this.d);
        packetdataserializer.d(this.e.size());
        Iterator iterator = this.e.iterator();

        while (iterator.hasNext()) {
            NBTTagCompound nbttagcompound = (NBTTagCompound) iterator.next();

            packetdataserializer.a(nbttagcompound);
        }

    }

    public void a(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.a(this);
    }

    private ByteBuf g() {
        ByteBuf bytebuf = Unpooled.wrappedBuffer(this.d);

        bytebuf.writerIndex(0);
        return bytebuf;
    }

    public int a(PacketDataSerializer packetdataserializer, Chunk chunk, boolean flag, int i) {
		Chunk[] nearbyChunks = {chunk.world.getChunkIfLoaded(chunk.locX - 1, chunk.locZ), chunk.world.getChunkIfLoaded(chunk.locX + 1, chunk.locZ), chunk.world.getChunkIfLoaded(chunk.locX, chunk.locZ - 1), chunk.world.getChunkIfLoaded(chunk.locX, chunk.locZ + 1)};
        return this.a(packetdataserializer, chunk, flag, i, nearbyChunks);
    }
    // Paper end

    public int a(PacketDataSerializer packetdataserializer, Chunk chunk, boolean flag, int i, Chunk[] nearbyChunks) {
        int j = 0;
        ChunkSection[] achunksection = chunk.getSections();
        int k = 0;

        for (int l = achunksection.length; k < l; ++k) {
            ChunkSection chunksection = achunksection[k];

            if (chunksection != Chunk.a && (!this.e() || !chunksection.a()) && (i & 1 << k) != 0) {
                j |= 1 << k;
                chunksection.getBlocks().serializeOrObfuscate(packetdataserializer, chunk, k, nearbyChunks);
                packetdataserializer.writeBytes(chunksection.getEmittedLightArray().asBytes());
                if (flag) {
                    packetdataserializer.writeBytes(chunksection.getSkyLightArray().asBytes());
                }
            }
        }

        if (this.e()) {
            packetdataserializer.writeBytes(chunk.getBiomeIndex());
        }

        return j;
    }

    protected int a(Chunk chunk, boolean flag, int i) {
        int j = 0;
        ChunkSection[] achunksection = chunk.getSections();
        int k = 0;

        for (int l = achunksection.length; k < l; ++k) {
            ChunkSection chunksection = achunksection[k];

            if (chunksection != Chunk.a && (!this.e() || !chunksection.a()) && (i & 1 << k) != 0) {
                j += chunksection.getBlocks().a();
                j += chunksection.getEmittedLightArray().asBytes().length;
                if (flag) {
                    j += chunksection.getSkyLightArray().asBytes().length;
                }
            }
        }

        if (this.e()) {
            j += chunk.getBiomeIndex().length;
        }

        return j;
    }

    public boolean e() {
        return this.f;
    }
}
