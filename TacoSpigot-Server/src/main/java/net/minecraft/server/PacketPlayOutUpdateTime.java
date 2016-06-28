package net.minecraft.server;

import java.io.IOException;

public class PacketPlayOutUpdateTime implements Packet<PacketListenerPlayOut> {

    // World Age in ticks
    // Not changed by server commands
    private long a;
    // Time of Day in ticks
    // If negative the sun will stop moving at the Math.abs of the time
    private long b;

    public PacketPlayOutUpdateTime() {}

    public PacketPlayOutUpdateTime(long i, long j, boolean flag) {
        this.a = i;
        this.b = j;
        if (!flag) {
            this.b = -this.b;
            if (this.b == 0L) {
                this.b = -1L;
            }
        }

        // Paper start
        this.a = this.a % 192000; // World age must not be negative
        this.b = this.b % 192000 - (this.b < 0 ? 192000 : 0); // Keep sign
        // Paper end
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        this.a = packetdataserializer.readLong();
        this.b = packetdataserializer.readLong();
    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.writeLong(this.a);
        packetdataserializer.writeLong(this.b);
    }

    public void a(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.a(this);
    }
}
