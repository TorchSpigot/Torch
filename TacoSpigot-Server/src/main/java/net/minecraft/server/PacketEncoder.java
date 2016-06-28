package net.minecraft.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {

    private static final Logger a = LogManager.getLogger();
    private static final Marker b = MarkerManager.getMarker("PACKET_SENT", NetworkManager.b);
    private final EnumProtocolDirection c;

    public PacketEncoder(EnumProtocolDirection enumprotocoldirection) {
        this.c = enumprotocoldirection;
    }

    protected void encode(ChannelHandlerContext channelhandlercontext, Packet<?> packet, ByteBuf bytebuf) throws Exception { // TacoSpigot - fix decompiler issue
        Integer integer = ((EnumProtocol) channelhandlercontext.channel().attr(NetworkManager.c).get()).a(this.c, packet);

        if (PacketEncoder.a.isDebugEnabled()) {
            PacketEncoder.a.debug(PacketEncoder.b, "OUT: [{}:{}] {}", new Object[] { channelhandlercontext.channel().attr(NetworkManager.c).get(), integer, packet.getClass().getName()});
        }

        if (integer == null) {
            throw new IOException("Can\'t serialize unregistered packet");
        } else {
            //PacketDataSerializer packetdataserializer = new PacketDataSerializer(bytebuf);
			PacketDataSerializer packetdataserializer = new PacketDataSerializer(bytebuf, channelhandlercontext.channel().attr(PlayerConnection.PLAYER_ATTR_KEY).get()); // TacoSpigot - pass player as argument to PacketDataSerializer

            packetdataserializer.d(integer.intValue());

            try {
                packet.b(packetdataserializer);
            } catch (Throwable throwable) {
                PacketEncoder.a.error(throwable);
            }

        }
    }

    //protected void encode(ChannelHandlerContext channelhandlercontext, Object object, ByteBuf bytebuf) throws Exception {
    //    this.a(channelhandlercontext, (Packet) object, bytebuf);
    //}
}
