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
import org.spigotmc.SpigotConfig;
import org.torch.utils.collection.WrappedCollections;

import static org.torch.server.TorchServer.logger;

public class ServerConnection {
    
    private static final Logger e = logger;
    /**
     * SERVER_NIO_EVENTLOOP
     */
    public static final LazyInitVar<NioEventLoopGroup> a = new LazyInitVar<NioEventLoopGroup>() {
        @Override
        protected NioEventLoopGroup init() {
            return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Server IO #%d").setDaemon(true).build());
        }
    };
    /**
     * SERVER_EPOLL_EVENTLOOP
     */
    public static final LazyInitVar<EpollEventLoopGroup> b = new LazyInitVar<EpollEventLoopGroup>() {
        @Override
        protected EpollEventLoopGroup init() {
            return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build());
        }
    };
    /**
     * SERVER_LOCAL_EVENTLOOP
     */
    public static final LazyInitVar<LocalEventLoopGroup> c = new LazyInitVar<LocalEventLoopGroup>() {
        @Override
        protected LocalEventLoopGroup init() {
            return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Server IO #%d").setDaemon(true).build());
        }
    };
    /**
     * Reference to the MinecraftServer instance
     */
    private final MinecraftServer f;
    /**
     * True if this NetworkSystem has never had his endpoints terminated
     */
    public volatile boolean d;
    /**
     * Contains all endpoints added to this NetworkSystem
     */
    private final List<ChannelFuture> g = WrappedCollections.wrappedList(Lists.newCopyOnWriteArrayList());
    /**
     * A list containing all NetworkManager instances of all endpoints
     */
    private final List<NetworkManager> h = WrappedCollections.wrappedList(Lists.newCopyOnWriteArrayList());
    
    private final List<NetworkManager> pending = WrappedCollections.wrappedList(Collections.synchronizedList(Lists.newLinkedList()));
    
    private void removePending() {
        synchronized (pending) {
            h.removeAll(pending);
            pending.clear();
        }
    }
    
    public ServerConnection(MinecraftServer minecraftserver) {
        this.f = minecraftserver;
        this.d = true;
    }
    
    public void a(InetAddress address, int port) throws IOException {
        this.addEndpoint(address, port);
    }

    /**
     * Adds channels that listens on publicly accessible network ports
     */
    public void addEndpoint(InetAddress address, int port) throws IOException {
        Class<? extends ServerChannel> channel;
        LazyInitVar<?> initer;
        
        if (Epoll.isAvailable() && this.f.af()) { // PAIL: useNativeTransport
            channel = EpollServerSocketChannel.class;
            initer = ServerConnection.b;
            logger.info("Using epoll channel type");
        } else {
            channel = NioServerSocketChannel.class;
            initer = ServerConnection.a;
            logger.info("Using default channel type");
        }
        
        this.g.add(new ServerBootstrap().channel(channel).childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(true));
                } catch (ChannelException ignored) {
                    ;
                }
                
                channel.pipeline()
                                 .addLast("timeout", new ReadTimeoutHandler(30))
                                 .addLast("legacy_query", new LegacyPingHandler(ServerConnection.this))
                                 .addLast("splitter", new PacketSplitter()).addLast("decoder", new PacketDecoder(EnumProtocolDirection.SERVERBOUND))
                                 .addLast("prepender", new PacketPrepender()).addLast("encoder", new PacketEncoder(EnumProtocolDirection.CLIENTBOUND));
                
                NetworkManager networkManager = new NetworkManager(EnumProtocolDirection.SERVERBOUND);
                
                h.add(networkManager); // Adding network manager
                
                channel.pipeline().addLast("packet_handler", networkManager);
                networkManager.setPacketListener(new HandshakeListener(f, networkManager));
            }
        }).group((EventLoopGroup) initer.c()).localAddress(address, port).bind().syncUninterruptibly());
    }
    
    /**
     * Shuts down all open endpoints
     */
    // terminateEndpoints
    public void b() {
        this.d = false;
        
        for (ChannelFuture channelfuture : this.g) {
            try {
                channelfuture.channel().close().sync();
            } catch (InterruptedException interrupted) {
                logger.error("Interrupted whilst closing channel");
            }
        }
    }
    
    /**
     * Will try to process the packets received by each NetworkManager,
     * gracefully manage processing failures and cleans up dead connections
     */
    // networkTick
    public void c() {
        // Spigot - This prevents players from 'gaming' the server, and strategically relogging to increase their position in the tick order
        if (SpigotConfig.playerShuffle > 0 && MinecraftServer.currentTick % SpigotConfig.playerShuffle == 0) {
            Collections.shuffle(this.h);
        }
        
        boolean needRemoval = false;
        Iterator<NetworkManager> iterator = this.h.iterator();
        
        while (iterator.hasNext()) {
            final NetworkManager networkManager = iterator.next();
            
            if (networkManager.h()) continue; // PAIL: hasNoChannel
            
            if (networkManager.isConnected()) {
                try {
                    networkManager.a(); // PAIL: processReceivedPackets
                } catch (Exception exception) {
                    if (networkManager.isLocal()) {
                        CrashReport crashreport = CrashReport.a(exception, "Ticking memory connection");
                        CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Ticking connection");

                        crashreportsystemdetails.a("Connection", new CrashReportCallable<String>() {
                            @Override
                            public String call() throws Exception {
                                return networkManager.toString();
                            }
                        });
                        throw new ReportedException(crashreport);
                    }
                    
                    logger.warn("Failed to handle packet for {}", new Object[] {networkManager.getSocketAddress(), exception});
                    final ChatComponentText chatcomponenttext = new ChatComponentText("Internal server error");

                    networkManager.sendPacket(new PacketPlayOutKickDisconnect(chatcomponenttext), new GenericFutureListener<Future<? super Void>>() {
                        @Override
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            networkManager.close(chatcomponenttext);
                        }
                    }, new GenericFutureListener[0]);
                    networkManager.stopReading();
                }
            } else {
                // Spigot - Fix a race condition where a NetworkManager could be unregistered just before connection.
                if (networkManager.preparing) continue;
                
                needRemoval = true;
                pending.add(networkManager);
                networkManager.handleDisconnection();
            }
        }
        
        if (needRemoval) removePending();
    }

    public MinecraftServer d() {
        return this.f;
    }
}