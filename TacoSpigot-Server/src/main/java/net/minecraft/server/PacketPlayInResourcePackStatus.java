package net.minecraft.server;

import java.io.IOException;

public class PacketPlayInResourcePackStatus implements Packet<PacketListenerPlayIn> {

    public String a; // Paper - make public
    public PacketPlayInResourcePackStatus.EnumResourcePackStatus status;

    public PacketPlayInResourcePackStatus() {}

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        this.a = packetdataserializer.e(40);
        this.status = (PacketPlayInResourcePackStatus.EnumResourcePackStatus) packetdataserializer.a(PacketPlayInResourcePackStatus.EnumResourcePackStatus.class);
    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.a(this.a);
        packetdataserializer.a((Enum) this.status);
    }

    public void a(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.a(this);
    }

    public static enum EnumResourcePackStatus {

        SUCCESSFULLY_LOADED, DECLINED, FAILED_DOWNLOAD, ACCEPTED;

        private EnumResourcePackStatus() {}
    }
}
