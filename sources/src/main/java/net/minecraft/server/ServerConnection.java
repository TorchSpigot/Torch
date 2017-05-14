package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.Logger;

import static org.torch.server.TorchServer.logger;

public class ServerConnection {

    private static final Logger e = logger;
    public static final LazyInitVar<NioEventLoopGroup> a = new LazyInitVar<NioEventLoopGroup>() {
        @Override
        protected NioEventLoopGroup init() {
            return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Server IO #%d").setDaemon(true).build());
        }
    };
    public static final LazyInitVar<EpollEventLoopGroup> b = new LazyInitVar<EpollEventLoopGroup>() {
        @Override
        protected EpollEventLoopGroup init() {
            return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build());
        }
    };
    public static final LazyInitVar<LocalEventLoopGroup> c = new LazyInitVar<LocalEventLoopGroup>() {
        @Override
        protected LocalEventLoopGroup init() {
            return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Server IO #%d").setDaemon(true).build());
        }
    };
    
    private final MinecraftServer f;
    public volatile boolean d;
    private final List<ChannelFuture> g = Collections.synchronizedList(Lists.<ChannelFuture>newArrayList());
    private final List<NetworkManager> h = Collections.synchronizedList(Lists.<NetworkManager>newArrayList());
    // Paper start - prevent blocking on adding a new network manager while the server is ticking
    private final List<NetworkManager> pending = Collections.synchronizedList(Lists.<NetworkManager>newArrayList());
    private void addPending() {
        synchronized (pending) {
            this.h.addAll(pending); // Paper - OBFHELPER - List of network managers
            pending.clear();
        }
    }
    // Paper end

    public ServerConnection(MinecraftServer minecraftserver) {
        this.f = minecraftserver;
        this.d = true;
    }

    public void a(InetAddress inetaddress, int i) throws IOException {
        List list = this.g;

        synchronized (this.g) {
            Class<? extends ServerChannel> channel;
            LazyInitVar lazyinitvar;

            if (Epoll.isAvailable() && this.f.af()) {
                channel = EpollServerSocketChannel.class;
                lazyinitvar = ServerConnection.b;
                logger.info("Using epoll channel type");
            } else {
                channel = NioServerSocketChannel.class;
                lazyinitvar = ServerConnection.a;
                logger.info("Using default channel type");
            }
            
            this.g.add((new ServerBootstrap()).channel(channel).childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(true));
                    } catch (ChannelException channelexception) {
                        ;
                    }
                    
                    channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("legacy_query", new LegacyPingHandler(ServerConnection.this)).addLast("splitter", new PacketSplitter()).addLast("decoder", new PacketDecoder(EnumProtocolDirection.SERVERBOUND)).addLast("prepender", new PacketPrepender()).addLast("encoder", new PacketEncoder(EnumProtocolDirection.CLIENTBOUND));
                    NetworkManager networkmanager = new NetworkManager(EnumProtocolDirection.SERVERBOUND);

                    pending.add(networkmanager); // Paper
                    channel.pipeline().addLast("packet_handler", networkmanager);
                    networkmanager.setPacketListener(new HandshakeListener(ServerConnection.this.f, networkmanager));
                }
            }).group((EventLoopGroup) lazyinitvar.c()).localAddress(inetaddress, i).bind().syncUninterruptibly());
        }
    }

    public void b() {
        this.d = false;
        Iterator iterator = this.g.iterator();

        while (iterator.hasNext()) {
            ChannelFuture channelfuture = (ChannelFuture) iterator.next();

            try {
                channelfuture.channel().close().sync();
            } catch (InterruptedException interruptedexception) {
                logger.error("Interrupted whilst closing channel");
            }
        }

    }

    public void c() {
        List list = this.h;

        synchronized (this.h) {
            // Spigot Start
            addPending(); // Paper
            // This prevents players from 'gaming' the server, and strategically relogging to increase their position in the tick order
            if ( org.spigotmc.SpigotConfig.playerShuffle > 0 && MinecraftServer.currentTick % org.spigotmc.SpigotConfig.playerShuffle == 0 )
            {
                Collections.shuffle( this.h );
            }
            // Spigot End
            Iterator iterator = this.h.iterator();

            while (iterator.hasNext()) {
                final NetworkManager networkmanager = (NetworkManager) iterator.next();

                if (!networkmanager.h()) {
                    if (networkmanager.isConnected()) {
                        try {
                            networkmanager.a();
                        } catch (Exception exception) {
                            if (networkmanager.isLocal()) {
                                CrashReport crashreport = CrashReport.a(exception, "Ticking memory connection");
                                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Ticking connection");

                                crashreportsystemdetails.a("Connection", new CrashReportCallable<String>() {
                                    @Override
                                    public String call() throws Exception {
                                        return networkmanager.toString();
                                    }
                                });
                                throw new ReportedException(crashreport);
                            }

                            logger.warn("Failed to handle packet for {}", new Object[] { networkmanager.getSocketAddress(), exception});
                            final ChatComponentText chatcomponenttext = new ChatComponentText("Internal server error");

                            networkmanager.sendPacket(new PacketPlayOutKickDisconnect(chatcomponenttext), new GenericFutureListener() {
                                @Override
                                public void operationComplete(Future future) throws Exception {
                                    networkmanager.close(chatcomponenttext);
                                }
                            }, new GenericFutureListener[0]);
                            networkmanager.stopReading();
                        }
                    } else {
                        // Spigot Start
                        // Fix a race condition where a NetworkManager could be unregistered just before connection.
                        if (networkmanager.preparing) continue;
                        // Spigot End
                        iterator.remove();
                        networkmanager.handleDisconnection();
                    }
                }
            }

        }
    }

    public MinecraftServer d() {
        return this.f;
    }
}
