package org.torch.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.LoggerOutputStream;
import org.bukkit.craftbukkit.Main;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.spigotmc.SlackActivityAccountant;
import org.spigotmc.SpigotConfig;
import org.torch.api.Anaphase;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import co.aikar.timings.MinecraftTimings;

import joptsimple.OptionSet;

import lombok.Getter;
import lombok.Setter;

import net.minecraft.server.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.ResourceLeakDetector;

import jline.console.ConsoleReader;

/**
 * The core class of the Torch server
 */
@Getter
public final class TorchServer implements Runnable, org.torch.api.TorchReactor {
	
	/**
	 * The instance of the server
	 */
	private static TorchServer server;
	
	/**
     * The common logger
     */
    public static final Logger logger = LogManager.getLogger("Minecraft");
    
    /**
     * The game version supported by the server
     */
    public static final String GAME_VERSION = "1.11.2";
    
    /**
     * The protocol version supported by the server
     */
    public static final int PROTOCOL_VERSION = 316;
    
    /**
     * The usercache file
     */
    public static final File USER_CACHE_FILE = new File("usercache.json");
    
    /**
     * The pattern for decode resource pack SHA1
     */
    public static final Pattern RESOURCE_PACK_SHA1_PATTERN = Pattern.compile("^[a-fA-F0-9]{40}$");
    
    /**
     * Anvil converter for anvil file
     */
    public Convertable anvilFileConverter;
    /**
     * The mojang usage snooper
     */
    private final MojangStatisticsGenerator usageSnooper = new MojangStatisticsGenerator("server", getMinecraftServer(), System.currentTimeMillis());
    /**
     * Pending command queue
     */
    private final List<ServerCommand> serverCommandQueue = Collections.synchronizedList(Lists.newArrayList());
    /**
     * The rcon console command listener
     */
    public final RemoteControlCommandListener remoteControlCommandListener = new RemoteControlCommandListener(this.getDedicatedServer());
    /**
     * The rcon query listener
     */
    private RemoteStatusListener remoteQueryListener;
    /**
     * The rcon control listener
     */
    private RemoteControlListener remoteControlListener;
    /**
     * The server properties
     */
    public PropertyManager propertyManager;
    /**
     * The eula, this field only for keeping compatibility
     */
    @Deprecated private EULA eula = new EULA(new File("eula.txt"));
    /**
     * If the server can spawn structures
     */
    private boolean generateStructures;
    
    private EnumGamemode gameMode;
    
    private boolean guiIsEnabled;
    /**
     * The anvil file
     */
    public File universeAnvilFile;
    /**
     * List of names of players who are online
     */
    private final List<net.minecraft.server.ITickable> tickables = Lists.newArrayList();
    /**
     * The command manager
     */
    public final net.minecraft.server.ICommandHandler commandManager;
    /**
     * The profiler
     */
    public final MethodProfiler methodProfiler = new MethodProfiler();
    /**
     * The server connection
     */
    private ServerConnection serverConnection;
    /**
     * The server status responser
     */
    private final ServerPing serverPing = new ServerPing();
    /**
     * The random
     */
    private final Random random = new Random();
    /**
     * The data converter manager
     */
    private final DataConverterManager dataConverterManager;
    /**
     * The server Ip
     */
    @Setter private String serverIp;
    /**
     * The server port
     */
    @Setter private int serverPort = -1;
    /**
     * The player list for this server
     */
    @Setter private TorchPlayerList playerList;
    /**
     * Indicates whether the server is running or not. Sets to false to initiate a shutdown
     */
    private boolean isRunning = true;
    /**
     * Indicates to other classes that the server is safely stopped
     */
    private boolean isStopped;
    /**
     * The server proxy
     */
    protected final Proxy serverProxy;
    /**
     * The task the server is currently working on(and will output on outputPercentRemaining)
     */
    public String currentTask;
    /**
     * The percentage of the current task finished so far
     */
    public int percentDone;
    /**
     * The online mode of the server
     */
    @Setter public boolean onlineMode;
    /**
     * If the server prevent proxy connections
     */
    @Setter private boolean preventProxyConnections;
    /**
     * If the server has animals
     */
    @Setter private boolean spawnAnimals;
    /**
     * If the server has villagers
     */
    @Setter private boolean spawnNPCs;
    /**
     * If PvP is active on the server
     */
    @Setter private boolean pvpMode;
    /**
     * If flight is allowed or not
     */
    @Setter private boolean allowFlight;
    /**
     * The server motd string
     */
    @Setter private String motd;
    /**
     * Maximum build height, default 256
     */
    @Setter private int buildLimit;
    /**
     * The maximum player idle minutes
     */
    private int maxPlayerIdleMinutes;
    
    public final long[] tickTimeArray = new long[100];
    
    public long[][] timeOfLastDimensionTick;
    
    @Setter private KeyPair serverKeyPair;
    /**
     * Username of the server owner (for LAN servers)
     */
    private String serverOwner;
    /**
     * The folder name of primary world, default 'world'
     */
    @Setter private String primaryWorldFolderName;
    /**
     * If the server is demo mode
     */
    @Setter private boolean demoMode;
    
    @Setter private boolean enableBonusChest;
    /**
     * The url of custom resource pack
     */
    private String resourcePackUrl = "";
    /**
     * The hash of custom resource pack
     */
    private String resourcePackHash = "";
    /**
     * If the server is running
     */
    private boolean serverIsRunning;
    /**
     * Sets when warned for "Can't keep up", which triggers again after 15 seconds
     */
    private long timeOfLastWarning;
    
    private String userMessage;
    
    @Deprecated private boolean startProfiling;
    /**
     * If the given game mode will be forced apply
     */
    private boolean isGamemodeForced;
    /**
     * The Yggdrasil authentication service
     */
    private final YggdrasilAuthenticationService authService;
    /**
     * The Minecraft session service
     */
    private final MinecraftSessionService sessionService;
    /**
     * The Mojang game profile repository
     */
    private final GameProfileRepository profileRepo;
    /**
     * The user cache
     */
    private final UserCache userCache;
    
    private long nanoTimeSinceStatusRefresh;
    
    protected final Queue<FutureTask<?>> futureTaskQueue = new com.destroystokyo.paper.utils.CachedSizeConcurrentLinkedQueue<>();
    /**
     * The main server thread
     */
    private Thread serverThread;
    /**
     * Simply return current system time millis
     */
    private long currentTime = System.currentTimeMillis();
    
    ///////// CB / S / P Stuffs
    /**
     * The worlds
     */
    public List<WorldServer> worlds = new ArrayList<WorldServer>();
    /**
     * The auto save period
     */
    public int autosavePeriod;
    /**
     * The server instance of CraftBukkit
     */
    @Anaphase @Setter public org.bukkit.craftbukkit.CraftServer craftServer;
    /**
     * The set for store base server settings, port, etc.
     */
    public OptionSet options;
    /**
     * Bukkit command sender for the console
     */
    @Anaphase @Setter public org.bukkit.command.ConsoleCommandSender console;
    /**
     * Bukkit command sender for the remote console (rcon)
     */
    public org.bukkit.command.RemoteConsoleCommandSender remoteConsole;
    /**
     * The jline reader
     */
    @Anaphase @Setter public ConsoleReader reader;
    
    public java.util.Queue<Runnable> processQueue = new com.destroystokyo.paper.utils.CachedSizeConcurrentLinkedQueue<>();
    /**
     * A singal of Paper, which is used to judge if the server should be auto saved
     */
    public boolean serverAutoSave = false;
    /**
     * The time-spent tracker of Spigot 
     */
    public final SlackActivityAccountant slackActivityAccountant = new SlackActivityAccountant();
    /**
     * If the server has already stopped
     */
    private boolean hasStopped = false;
    /**
     * A Bukkit signal, which is used to stopping server
     */
    private final Object stopLock = new Object();
    
    ///////// TPS Stuffs
    /**
     * The sample TPS
     */
    public static final int SAMPLE_TPS = 20;
    /**
     * The sample nano per second
     */
    public static final long SAMPLE_SEC_IN_NANO = 1000000000;
    /**
     * The sample full tick time
     */
    public static final long SAMPLE_TICK_TIME = SAMPLE_SEC_IN_NANO / SAMPLE_TPS;
    /**
     * The sample maximum catchup buffer
     */
    public static final long SAMPLE_MAX_CATCHUP_BUFFER = SAMPLE_TICK_TIME * SAMPLE_TPS * 60L;
    /**
     * The sample tps interval
     */
    public static final int SAMPLE_TICK_INTERVAL = 20;
    
    public TorchServer(OptionSet optionSet, Proxy proxy, DataConverterManager dataFixer, YggdrasilAuthenticationService yggdrasil, MinecraftSessionService session, GameProfileRepository profileRepository, UserCache profileCache) {
        server = this;
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        
        serverProxy = proxy;
        authService = yggdrasil;
        sessionService = session;
        profileRepo = profileRepository;
        userCache = profileCache;
        commandManager = createCommandDispatcher();
        dataConverterManager = dataFixer;
        options = optionSet;
        
        // Initial the server connection
        handleServerConnection();
        getMinecraftServer().setServerConnection(this.serverConnection);
        
        Regulator.getInstance();
        
        // Try to see if we're actually running in a terminal, disable jline if not
        if (System.console() == null && System.getProperty("jline.terminal") == null) {
            System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
            Main.useJline = false;
        }
        
        try {
        	getMinecraftServer().reader = new ConsoleReader(System.in, System.out);
            // Avoid parsing exceptions for uncommonly used event designators
        	getMinecraftServer().reader.setExpandEvents(false);
        } catch (Throwable t) {
            try {
                // Try again with jline disabled for Windows users without C++ 2008 Redistributable
                System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
                System.setProperty("user.language", "en");
                Main.useJline = false;
                getMinecraftServer().reader = new ConsoleReader(System.in, System.out);
                getMinecraftServer().reader.setExpandEvents(false);
            } catch (IOException ex) {
                logger.warn((String) null, ex);
            }
        }
        
        // Initial the console reader
        setReader(getMinecraftServer().reader);
        
        Runtime.getRuntime().addShutdownHook(new org.bukkit.craftbukkit.util.ServerShutdownThread(getMinecraftServer()));
        serverThread = new Thread(this, "Server thread");
        // Bump the main thread priority
        Thread.currentThread().setPriority(9);
    }
    
    /**
     * Constructor for the dedicated server
     */
    public TorchServer(OptionSet optionSet, DataConverterManager dataFixer, YggdrasilAuthenticationService yggdrasil, MinecraftSessionService session, GameProfileRepository profileRepository, UserCache userCache) {
        this(optionSet, Proxy.NO_PROXY, dataFixer, yggdrasil, session, profileRepository, userCache);
        
        new Thread("Server Infinisleeper") {
        	{ setDaemon(true); start(); }
        	
        	@Override
            public void run() {
        		try {
                    while (true) Thread.sleep(2147483647L);
                } catch (InterruptedException interruptedexception) {
                    ;
                }
            }
        };
    }
    
    public static void main(final OptionSet options) {
    	// Register bootstrap
    	DispenserRegistry.c();
    	
        try {
            String rootDirectory = ".";
            YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
            MinecraftSessionService sessionService = authService.createMinecraftSessionService();
            GameProfileRepository profileRepo = authService.createProfileRepository();
            UserCache profileCache = new UserCache(profileRepo, new File(rootDirectory, USER_CACHE_FILE.getName()));
            final DedicatedServer dedicatedserver = new DedicatedServer(options, DataConverterRegistry.a(), authService, sessionService, profileRepo, profileCache);
            
            if (options.has("port")) {
                int port = (Integer) options.valueOf("port");
                if (port > 0 && port < 65536) {
                    dedicatedserver.setPort(port);
                }
            }
            
            if (options.has("universe")) {
                dedicatedserver.universe = (File) options.valueOf("universe");
            }
            
            if (options.has("world")) {
                dedicatedserver.setWorld((String) options.valueOf("world"));
            }
            
            dedicatedserver.primaryThread.start();
        } catch (Throwable t) {
            logger.fatal("Failed to start the minecraft server", t);
            t.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try {
            if (this.init()) {
                currentTime = System.currentTimeMillis();
                
                serverPing.setMOTD(new ChatComponentText(motd));
                serverPing.setServerInfo(new ServerPing.ServerData(GAME_VERSION, PROTOCOL_VERSION));
                this.applyServerIconToPing(serverPing);
                
                Arrays.fill(getMinecraftServer().recentTps, 20);
                long start = System.nanoTime(), lastTick = start - SAMPLE_TICK_TIME, catchupTime = 0, curTime, wait, tickSection = start;
                while (this.isRunning) {
                    curTime = System.nanoTime();
                    wait = SAMPLE_TICK_TIME - (curTime - lastTick);
                    
                    if (wait > 0) {
                        if (catchupTime < 2E6) {
                            wait += Math.abs(catchupTime);
                        } else if (wait < catchupTime) {
                            catchupTime -= wait;
                            wait = 0;
                        } else {
                            wait -= catchupTime;
                            catchupTime = 0;
                        }
                    }
                    
                    if (wait > 0) {
                        Thread.sleep(wait / 1000000);
                        curTime = System.nanoTime();
                        wait = SAMPLE_TICK_TIME - (curTime - lastTick);
                    }

                    catchupTime = Math.min(SAMPLE_MAX_CATCHUP_BUFFER, catchupTime - wait);
                    if ( ++MinecraftServer.currentTick % SAMPLE_TICK_INTERVAL == 0 )
                    {
                        final long diff = curTime - tickSection;
                        double currentTps = 1E9 / diff * SAMPLE_TICK_INTERVAL;
                        getMinecraftServer().tps1.add(currentTps, diff);
                        getMinecraftServer().tps5.add(currentTps, diff);
                        getMinecraftServer().tps15.add(currentTps, diff);
                        // Backwards compat with bad plugins
                        getMinecraftServer().recentTps[0] = getMinecraftServer().tps1.getAverage();
                        getMinecraftServer().recentTps[1] = getMinecraftServer().tps5.getAverage();
                        getMinecraftServer().recentTps[2] = getMinecraftServer().tps15.getAverage();
                        tickSection = curTime;
                    }
                    lastTick = curTime;

                    this.tick();
                    this.serverIsRunning = true;
                }
            } else {
                this.finalTick((CrashReport) null);
            }
        } catch (Throwable throwable) {
            logger.error("Encountered an unexpected exception", throwable);
            if ( throwable.getCause() != null ) MinecraftServer.LOGGER.error( "\tCause of unexpected exception was", throwable.getCause() );
            
            CrashReport crashreport;
            if (throwable instanceof ReportedException) {
                crashreport = this.addServerInfoToCrashReport(((ReportedException) throwable).a());
            } else {
                crashreport = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", throwable));
            }
            
            File file = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
            // Check if the report has been saved successfully
            if (crashreport.a(file)) {
                MinecraftServer.LOGGER.error("This crash report has been saved to: {}", new Object[] { file.getAbsolutePath()});
            } else {
                MinecraftServer.LOGGER.error("We were unable to save this crash report to disk.");
            }
            
            this.finalTick(crashreport);
        } finally {
            try {
                org.spigotmc.WatchdogThread.doStop();
                
                this.isStopped = true;
                this.stopServer();
            } catch (Throwable throwable1) {
                logger.error("Exception stopping the server", throwable1);
            } finally {
                try {
                    reader.getTerminal().restore();
                } catch (Exception ignored) {
                }
                
                this.isStopped = true;
                this.systemExitNow();
            }

        }
    }
    
    public static boolean authUUID() { // TODO: configurable
    	return Bukkit.getOnlineMode() || SpigotConfig.bungee;
    }
    
    /**
     * Load resource pack hash from the property
     */
    public String loadResourcePackSHA() {
        if (this.propertyManager.a("resource-pack-hash")) {
            if (this.propertyManager.a("resource-pack-sha1")) {
                logger.warn("resource-pack-hash is deprecated and found along side resource-pack-sha1. resource-pack-hash will be ignored.");
            } else {
            	logger.warn("resource-pack-hash is deprecated. Please use resource-pack-sha1 instead.");
                this.propertyManager.getString("resource-pack-sha1", this.propertyManager.getString("resource-pack-hash", ""));
                this.propertyManager.b("resource-pack-hash");
            }
        }

        String sha1 = this.propertyManager.getString("resource-pack-sha1", "");

        if (!sha1.isEmpty() && !RESOURCE_PACK_SHA1_PATTERN.matcher(sha1).matches()) {
        	logger.warn("Invalid sha1 for resource-pack-sha1");
        }

        if (!this.propertyManager.getString("resource-pack", "").isEmpty() && sha1.isEmpty()) {
        	logger.warn("You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack.");
        }

        return sha1;
    }
    
    /**
     * Called on exit from the main run() loop, does nothing
     */
    @Deprecated
    public void finalTick(CrashReport report) {}
    
    /**
     * Apply server ping icon from icon file
     */
    public void applyServerIconToPing(ServerPing serverping) {
        File file = this.getFile("server-icon.png");
        
        if (!file.exists()) {
            file = this.getAnvilFileConverter().b(this.getPrimaryWorldFolderName(), "icon.png");
        }
        
        if (file.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();
            
            try {
                BufferedImage bufferedimage = ImageIO.read(file);

                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                ByteBuf base64 = Base64.encode(bytebuf);

                serverping.setFavicon("data:image/png;base64," + base64.toString(Charsets.UTF_8));
            } catch (Throwable t) {
                logger.error("Couldn\'t load server icon", t);
            } finally {
                bytebuf.release();
            }
        }

    }
    
    /**
     * Return current tick number (incremented every tick)
     */
    public int getCurrentTick() {
    	return MinecraftServer.currentTick;
    }
    
    /**
     * Directly calls System.exit(0), instantly killing the program.
     */
    @Deprecated
    public void systemExitNow() {
    	System.exit(0);
    }
    
    public void convertMapIfNeeded(String world) {
        if (this.getAnvilFileConverter().isConvertable(world)) {
            logger.info("Converting map!");
            
            this.setUserMessage("menu.convertingLevel");
            this.getAnvilFileConverter().convert(world, new IProgressUpdate() {
                private long startTime = System.currentTimeMillis();
                
                /**
                 * Shows the 'Saving level' string
                 */
                @Override
				public void a(String message) { displaySavingString(message); } // OBFHELPER
                public void displaySavingString(String message) {}

                /**
                 * Updates the progress bar on the loading screen to the specified amount
                 */
                @Override
				public void a(int progress) { setLoadingProgress(progress); } // OBFHELPER
                public void setLoadingProgress(int progress) {
                	if (System.currentTimeMillis() - this.startTime >= 1000L) {
                        this.startTime = System.currentTimeMillis();
                        MinecraftServer.LOGGER.info("Converting... {}%", new Object[] { Integer.valueOf(progress)});
                    }
                }

                /**
                 * Displays a string on the loading screen supposed to indicate what is being done currently
                 */
                @Override
				public void c(String message) { displayLoadingString(message); } // OBFHELPER
                public void displayLoadingString(String message) {}
            });
        }
    }
    
    /**
     * Load all default worlds: world, world_nether and world_the_end
     */
    @SuppressWarnings("deprecation")
	public void loadDefaultWorlds(String saveName, String worldName, long seed, WorldType worldtype, String generatorOptions) {
        this.convertMapIfNeeded(worldName);
        this.setUserMessage("menu.loadingLevel");
        
        getMinecraftServer().worldServer = new WorldServer[3];
        int worldCount = 3;

        for (int j = 0; j < worldCount; ++j) {
            WorldServer world;
            byte dimension = 0;

            if (j == 1) {
                if (getAllowNether()) {
                    dimension = -1;
                } else {
                    continue;
                }
            }

            if (j == 2) {
                if (craftServer.getAllowEnd()) {
                    dimension = 1;
                } else {
                    continue;
                }
            }

            String worldType = org.bukkit.World.Environment.getEnvironment(dimension).toString().toLowerCase();
            String name = (dimension == 0) ? saveName : saveName + "_" + worldType;

            org.bukkit.generator.ChunkGenerator gen = craftServer.getGenerator(name);
            WorldSettings worldsettings = new WorldSettings(seed, this.getGameMode(), this.isGenerateStructures(), this.isHardcore(), worldtype);
            worldsettings.setGeneratorSettings(worldName);

            if (j == 0) {
                IDataManager idatamanager = new ServerNBTManager(craftServer.getWorldContainer(), worldName, true, this.dataConverterManager);
                WorldData worlddata = idatamanager.getWorldData();
                if (worlddata == null) {
                    worlddata = new WorldData(worldsettings, worldName);
                }
                // Migration did not rewrite the level.dat; This forces 1.8 to take the last loaded world as respawn (in this case the end)
                worlddata.checkName(worldName);
                if (this.isDemoMode()) {
                    world = (WorldServer) (new DemoWorldServer(getMinecraftServer(), idatamanager, worlddata, dimension, this.methodProfiler)).b();
                } else {
                    world = (WorldServer) (new WorldServer(getMinecraftServer(), idatamanager, worlddata, dimension, this.methodProfiler, org.bukkit.World.Environment.getEnvironment(dimension), gen)).b();
                }

                world.a(worldsettings);
                this.craftServer.scoreboardManager = new org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager(getMinecraftServer(), world.getScoreboard());
            } else {
                String dim = "DIM" + dimension;

                File newWorld = new File(new File(name), dim);
                File oldWorld = new File(new File(saveName), dim);

                if ((!newWorld.isDirectory()) && (oldWorld.isDirectory())) {
                    MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder required ----");
                    MinecraftServer.LOGGER.info("Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit requires that you move your " + worldType + " folder to a new location in order to operate correctly.");
                    MinecraftServer.LOGGER.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
                    MinecraftServer.LOGGER.info("Attempting to move " + oldWorld + " to " + newWorld + "...");

                    if (newWorld.exists()) {
                        MinecraftServer.LOGGER.warn("A file or folder already exists at " + newWorld + "!");
                        MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    } else if (newWorld.getParentFile().mkdirs()) {
                        if (oldWorld.renameTo(newWorld)) {
                            MinecraftServer.LOGGER.info("Success! To restore " + worldType + " in the future, simply move " + newWorld + " to " + oldWorld);
                            // Migrate world data too.
                            try {
                                com.google.common.io.Files.copy(new File(new File(saveName), "level.dat"), new File(new File(name), "level.dat"));
                                org.apache.commons.io.FileUtils.copyDirectory(new File(new File(saveName), "data"), new File(new File(name), "data"));
                            } catch (IOException exception) {
                                MinecraftServer.LOGGER.warn("Unable to migrate world data.");
                            }
                            MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder complete ----");
                        } else {
                            MinecraftServer.LOGGER.warn("Could not move folder " + oldWorld + " to " + newWorld + "!");
                            MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                        }
                    } else {
                        MinecraftServer.LOGGER.warn("Could not create path for " + newWorld + "!");
                        MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    }
                }

                IDataManager idatamanager = new ServerNBTManager(craftServer.getWorldContainer(), name, true, this.dataConverterManager);
                // world =, saveName to dimension, worldName to name, added Environment and gen
                WorldData worlddata = idatamanager.getWorldData();
                if (worlddata == null) {
                    worlddata = new WorldData(worldsettings, name);
                }
                worlddata.checkName(name); // CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to take the last loaded world as respawn (in this case the end)
                world = (WorldServer) new SecondaryWorldServer(getMinecraftServer(), idatamanager, dimension, this.worlds.get(0), this.methodProfiler, worlddata, org.bukkit.World.Environment.getEnvironment(dimension), gen).b();
            }

            this.craftServer.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(world.getWorld()));

            world.addIWorldAccess(new WorldManager(getMinecraftServer(), world));
            if (!this.isSinglePlayer()) world.getWorldData().setGameType(this.getGameMode());

            worlds.add(world);
            getPlayerList().setPlayerFileData(worlds.toArray(new WorldServer[worlds.size()]));
        }
        
        getPlayerList().setPlayerFileData(getMinecraftServer().worldServer);
        this.setDifficultyForAllWorlds(this.getDifficulty());
        this.initialAllWorldsChunk();
        
        // Handle collideRule team for player collision toggle
        final Scoreboard scoreboard = this.getWorld().getScoreboard();
        final java.util.Collection<String> toRemove = scoreboard.getTeams().stream().filter(team -> team.getName().startsWith("collideRule_")).map(ScoreboardTeam::getName).collect(java.util.stream.Collectors.toList());
        for (String teamName : toRemove) {
            scoreboard.removeTeam(scoreboard.getTeam(teamName)); // Clean up after ourselves
        }

        if (!com.destroystokyo.paper.PaperConfig.enablePlayerCollisions) {
        	// Note: CollideRuleTeamName Marked as @Anaphase
            playerList.getServant().setCollideRuleTeamName(org.apache.commons.lang3.StringUtils.left("collideRule_" + this.getWorld().random.nextInt(), 16));
            ScoreboardTeam collideTeam = scoreboard.createTeam(playerList.getServant().getCollideRuleTeamName());
            
            // Because we want to mimic them not being on a team at all
            collideTeam.setCanSeeFriendlyInvisibles(false);
        }
    }
    
    /**
     * Main function called by run() every loop
     */
    public void tick() throws ExceptionWorldConflict {
        co.aikar.timings.TimingsManager.FULL_SERVER_TICK.startTiming();
        this.slackActivityAccountant.tickStarted();
        long startTime = System.nanoTime();
        
        // Sync ticks for compatability, currentTick increased before this
        getMinecraftServer().setTicks(MinecraftServer.currentTick);
        
        this.updateLogicsPhysicsExecuteCommands();
        
        if (startTime - this.nanoTimeSinceStatusRefresh >= 5000000000L) {
            this.nanoTimeSinceStatusRefresh = startTime;
            this.serverPing.setPlayerSample(new ServerPing.ServerPingPlayerSample(this.getMaxPlayers(), this.getCurrentPlayerCount()));
            
            GameProfile[] samplePlayers = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
            int random = MathHelper.nextInt(this.random, 0, this.getCurrentPlayerCount() - samplePlayers.length);

            for (int k = 0; k < samplePlayers.length; ++k) {
            	samplePlayers[k] = this.playerList.players.get(random + k).getProfile();
            }
            
            Collections.shuffle(Arrays.asList(samplePlayers));
            this.serverPing.b().a(samplePlayers); // PAIL: Apply sample players
        }

        serverAutoSave = (autosavePeriod > 0 && MinecraftServer.currentTick % autosavePeriod == 0);
        int playerSaveInterval = com.destroystokyo.paper.PaperConfig.playerAutoSaveRate;
        if (playerSaveInterval < 0) {
            playerSaveInterval = autosavePeriod;
        }
        if (playerSaveInterval > 0) {
            this.playerList.savePlayers(playerSaveInterval);
        }

        // We replace this with saving each individual world as this.saveChunks(...) is broken,
        // and causes the main thread to sleep for random amounts of time depending on chunk activity
        // Also pass flag to only save modified chunks
        craftServer.playerCommandState = true;
        for (World world : worlds) {
            if (world.paperConfig.autoSavePeriod > 0) world.getWorld().save(false);
        }
        craftServer.playerCommandState = false;
        
        long tickNanos;
        this.tickTimeArray[MinecraftServer.currentTick % 100] = tickNanos = System.nanoTime() - startTime;

        org.spigotmc.WatchdogThread.tick();
        PaperLightingQueue.processQueue(startTime);
        this.slackActivityAccountant.tickEnded(tickNanos);
        co.aikar.timings.TimingsManager.FULL_SERVER_TICK.stopTiming();
    }
    
    /**
     * Main tick stuff, called from updateLogicsPhysicsExecuteCommands()
     */
    public void updateLogicsAndPhysics() {
        MinecraftTimings.bukkitSchedulerTimer.startTiming();
        this.craftServer.getScheduler().mainThreadHeartbeat(MinecraftServer.currentTick);
        MinecraftTimings.bukkitSchedulerTimer.stopTiming();
        
        MinecraftTimings.minecraftSchedulerTimer.startTiming();
        while (!futureTaskQueue.isEmpty()) {
        	SystemUtils.a(futureTaskQueue.remove(), logger);
        }
        MinecraftTimings.minecraftSchedulerTimer.stopTiming();

        MinecraftTimings.processQueueTimer.startTiming();
        // Run tasks that are waiting on processing
        while (!processQueue.isEmpty()) {
            processQueue.remove().run();
        }
        MinecraftTimings.processQueueTimer.stopTiming();
        
        MinecraftTimings.chunkIOTickTimer.startTiming();
        org.bukkit.craftbukkit.chunkio.ChunkIOExecutor.tick();
        MinecraftTimings.chunkIOTickTimer.stopTiming();
        
        MinecraftTimings.timeUpdateTimer.startTiming();
        // Send time updates to everyone, it will get the right time from the world the player is in.
        if (MinecraftServer.currentTick % 20 == 0) {
            for (int index = 0, size = getPlayerList().players.size(); index < size; index++) {
                EntityPlayer entityplayer = this.getPlayerList().players.get(index);
                // Add support for per player time
                entityplayer.playerConnection.sendPacket(new PacketPlayOutUpdateTime(entityplayer.world.getTime(), entityplayer.getPlayerTime(), entityplayer.world.getGameRules().getBoolean("doDaylightCycle")));
            }
        }
        MinecraftTimings.timeUpdateTimer.stopTiming();
        
        int index, size; CrashReport crashreport;
        for (index = 0, size = worlds.size(); index < size; index++) {
            WorldServer worldserver = this.worlds.get(index);
            
            try {
                worldserver.timings.doTick.startTiming();
                worldserver.doTick();
                worldserver.timings.doTick.stopTiming();
            } catch (Throwable t) {
                try {
                	crashreport = CrashReport.a(t, "Exception ticking world");
                } catch (Throwable throwable){
                    throw new RuntimeException("Error generating crash report", throwable);
                }
                
                worldserver.a(crashreport);
                throw new ReportedException(crashreport);
            }

            try {
                worldserver.timings.tickEntities.startTiming();
                worldserver.tickEntities();
                worldserver.timings.tickEntities.stopTiming();
            } catch (Throwable t) {
                try {
                	crashreport = CrashReport.a(t, "Exception ticking world entities");
                } catch (Throwable throwable){
                    throw new RuntimeException("Error generating crash report", throwable);
                }
                
                worldserver.a(crashreport);
                throw new ReportedException(crashreport);
            }

            worldserver.getTracker().updatePlayers();
            worldserver.explosionDensityCache.clear(); // Paper - Optimize explosions
        }
        
        MinecraftTimings.connectionTimer.startTiming();
        this.serverConnection.c();
        MinecraftTimings.connectionTimer.stopTiming();
        
        MinecraftTimings.playerListTimer.startTiming();
        this.playerList.tick();
        MinecraftTimings.playerListTimer.stopTiming();
        
        MinecraftTimings.tickablesTimer.startTiming();
        for (index = 0, size = tickables.size(); index < this.tickables.size(); index++) {
            tickables.get(index).F_();
        }
        MinecraftTimings.tickablesTimer.stopTiming();
    }
    
    /**
     * Initial chunks for all worlds
     */
    public void initialAllWorldsChunk() {
        this.setUserMessage("menu.generatingTerrain");

        // Fire WorldLoadEvent and handle whether or not to keep the spawn in memory
        for (WorldServer world : this.worlds) {
            logger.info("Preparing start region for level " + world.dimension + " (Seed: " + world.getSeed() + ")");

            if (!world.getWorld().getKeepSpawnInMemory()) {
                continue;
            }

            BlockPosition spawnPosition = world.getSpawn();
            long startTime = System.currentTimeMillis();
            int times = 0;
            
            short radius = world.paperConfig.keepLoadedRange;
            for (int startPosX = -radius; startPosX <= radius && isRunning(); startPosX += 16) {
                for (int startPosZ = -radius; startPosZ <= radius && isRunning(); startPosZ += 16) {
                    long eachTime = System.currentTimeMillis();

                    if (eachTime - startTime > 1000L) {
                        this.setCurrentTask("Preparing spawn area", times * 100 / 625);
                        startTime = eachTime;
                    }
                    
                    times++;
                    world.getChunkProviderServer().getChunkAt(spawnPosition.getX() + startPosX >> 4, spawnPosition.getZ() + startPosZ >> 4);
                }
            }
        }
        
        for (WorldServer world : this.worlds) {
            this.craftServer.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(world.getWorld()));
        }
        
        this.clearCurrentTask();
        this.craftServer.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
    }
    
    /**
     * Typically "menu.convertingLevel", "menu.loadingLevel" or others
     */
    public synchronized void setUserMessage(String newUserMessage) {
        this.userMessage = newUserMessage;
    }
    
    /**
     * Saves all necessary data as preparation for stopping the server
     */
    public void stopServer() throws ExceptionWorldConflict {
        // Prevent double stopping on multiple threads
        synchronized(stopLock) {
            if (hasStopped) return;
            hasStopped = true;
        }
        
        logger.info("Stopping server");
        // Stop server timings
        MinecraftTimings.stopServer();
        // Disable bukkit plugins
        if (this.craftServer != null) this.craftServer.disablePlugins();
        // Terminate end points
        if (this.getServerConnection() != null) this.getServerConnection().b();

        if (this.playerList != null) {
            logger.info("Saving players");
            this.playerList.savePlayers(); // Save all players data
            this.playerList.disconnectAllPlayers();;
            // SPIGOT-625 - give server at least a chance to send packets
            try { Thread.sleep(100); } catch (InterruptedException ex) {} // TODO: Packet sending
        }
        
        if (getMinecraftServer().worldServer != null) {
            logger.info("Saving worlds");

            for (WorldServer world : getMinecraftServer().worldServer) {
            	if (world != null) world.savingDisabled = false;
            }

            this.saveChunks(false);
        }

        // Stop snooper if running
        if (this.usageSnooper.d()) this.usageSnooper.e();
        // In non-online mode, the usercahce is useless
        if (org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly && org.bukkit.Bukkit.getOnlineMode()) {
            logger.info("Saving usercache.json");
            this.userCache.c(false);
        }
    }
    
    public void setResourcePackFromWorld(String world, IDataManager dataManager) {
        File file = new File(dataManager.getDirectory(), "resources.zip");
        
        if (file.isFile()) {
            try {
                this.setResourcePack("level://" + URLEncoder.encode(world, Charsets.UTF_8.toString()) + "/" + "resources.zip", "");
            } catch (UnsupportedEncodingException unsupported) {
                logger.warn("Something went wrong url encoding {}", new Object[] { world });
            }
        }
    }
    
    /**
     * Returns the world instances
     */
    public WorldServer[] getWorldServers() {
    	return getMinecraftServer().worldServer;
    }
    
    /**
     * Sets custom resource pack for the server
     */
    public void setResourcePack(String url, String hash) {
        this.resourcePackUrl = url;
        this.resourcePackHash = hash;
    }
    
    /**
     * Sets current task and its percent, be used to display a percent remaining given text and the percentage
     */
    public void setCurrentTask(String message, int percent) {
        this.currentTask = message;
        this.percentDone = percent;
        logger.info("{}: {}%", new Object[] { message, Integer.valueOf(percent) });
    }
    
    /**
     * Sets current task to null and set its percentage to 0
     */
    public void clearCurrentTask() {
        this.currentTask = null;
        this.percentDone = 0;
    }
    
    /**
     * Sets game mode for all worlds
     */
    public void setGamemodeForWorlds(EnumGamemode gamemode) {
        for (int index = 0, size = this.worlds.size(); index < size; index++) {
        	worlds.get(index).getWorldData().setGameType(gamemode);
        }
    }
    
    /**
     * Save all worlds and their level
     */
    public void saveChunks(boolean isSilent) {
        for (WorldServer world : this.worlds) {
        	if (world == null) continue;
        	if (!isSilent) logger.info("Saving chunks for level \'{}\'/{}", new Object[] { world.getWorldData().getName(), world.worldProvider.getDimensionManager().b()});
    		
    		try {
                world.save(true, (IProgressUpdate) null);
                world.saveLevel();
            } catch (ExceptionWorldConflict conflict) {
                logger.warn(conflict.getMessage());
            }
        }
    }
    
    /**
     * Returns the root directory of the server
     */
    public File getDataDirectory() {
        return new File(".");
    }
    
    /**
     * If the nether is allowed in this server, default true
     */
    public boolean getAllowNether() {
    	return this.propertyManager.getBoolean("allow-nether", true);
    }
    
    /**
     * Start a pure server thread, may unsafe
     */
    @Deprecated
    public void startServerThread() {
        this.serverThread = new Thread(this, "Server thread");
        this.serverThread.start();
    }
    
    /**
     * Returns a specified file in the server root directory
     */
    public File getFile(String fileName) {
        return new File(this.getDataDirectory(), fileName);
    }

    public void registerTickable(ITickable itickable) {
        this.tickables.add(itickable);
    }
    
    /**
     * Returns the number of players currently on the server
     */
    public int getCurrentPlayerCount() {
        return this.playerList.getPlayerCount();
    }

    /**
     * Returns the maximum number of players allowed on the server
     */
    public int getMaxPlayers() {
        return this.playerList.getMaxPlayers();
    }
    
    /**
     * Starting the method profiler at the beginning of next tick, deprecated because the vanilla method profiler has been removed
     */
    @Deprecated
    public void startProfiling() {
    	this.startProfiling = true;
    }
    
    /**
     * Returns the server connection, initial if doesn't exist
     */
    public ServerConnection handleServerConnection() {
        return this.serverConnection == null ? this.serverConnection = new ServerConnection(getMinecraftServer()) : this.serverConnection;
    }
    
    /**
     * Returns an array of the usernames of all the connected players
     */
    public String[] getOnlinePlayerNames() {
        return this.playerList.getOnlinePlayerNames();
    }

    /**
     * Returns an array of the GameProfiles of all the connected players
     */
    public GameProfile[] getOnlinePlayerProfiles() {
        return this.playerList.getOnlinePlayerProfiles();
    }
    
    /**
     * Send a info via default logger 
     */
    public void info(String info) {
        logger.info(info);
    }

    /**
     * Send a warning via default logger 
     */
    public void warning(String warn) {
        logger.warn(warn);
    }
    
    /**
     * Send a error via default logger 
     */
    public void error(String error) {
        logger.error(error);
    }
    
    /**
     * Send a debug info via default logger 
     */
    public void debug(String info) {
        if (isDebugging()) logger.info(info);
    }
    
    /**
     * Sets the maximum idle minutes and then save the properties
     */
    public void setIdleTimeout(int minutes) {
        this.maxPlayerIdleMinutes = minutes;
        this.propertyManager.setProperty("player-idle-timeout", Integer.valueOf(minutes));
        this.saveProperties();
    }
    
    /**
     * Get moded server name
     */
    public String getServerModName() {
        return "Torch"; // Torch - Torch > // Paper - Paper > // Spigot - Spigot > // CraftBukkit - cb > vanilla!
    }
    
    /**
     * Adds the server info, including from theWorldServer, to the crash report.
     */
    public CrashReport addServerInfoToCrashReport(CrashReport crashreport) {
        crashreport.g().a("Profiler Position", new CrashReportCallable<String>() {
        	
            @Override
            public String call() throws Exception {
            	return methodProfiler.a ? methodProfiler.c() : "N/A (disabled)";
            }
        });
        
        if (this.playerList != null) {
            crashreport.g().a("Player Count", new CrashReportCallable<String>() {
            	
                @Override
                public String call() throws Exception {
                	return playerList.getPlayerCount() + " / " + playerList.getMaxPlayers() + "; " + playerList.getPlayers();
                }
            });
        }

        return crashreport;
    }
    
    /**
     * Tab complete via Bukkit
     */
    public List<String> tabCompleteCommand(ICommandListener icommandlistener, String s, @Nullable BlockPosition blockposition, boolean flag) {
        return craftServer.tabComplete(icommandlistener, s, blockposition, true);
    }
    
    /**
     * Sets owner for the server
     */
    public void setServerOwner(String owner) {
        this.serverOwner = owner;
    }
    
    /**
     * Check if anvil file is set, this will always returns true
     */
    public boolean isAnvilFileSet() {
        return true;
    }
    
    /**
     * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
     */
    public boolean canUseCommand(int permLevel, String commandName) {
        return true;
    }
    
    /**
     * If the server has a owner, representing it's a single player game
     */
    @Deprecated
    public boolean isSinglePlayer() {
        return this.serverOwner != null;
    }
    
    /**
     * Sets game difficulty for all worlds, useless if hardcore mode is enabled
     */
    public void setDifficultyForAllWorlds(EnumDifficulty difficulty) {
        for (WorldServer world : this.worlds) {
        	if (world == null) continue;
        	
        	if (world.getWorldData().isHardcore()) {
                world.getWorldData().setDifficulty(EnumDifficulty.HARD);
                world.setSpawnFlags(true, true);
            } else if (this.isSinglePlayer()) {
                world.getWorldData().setDifficulty(difficulty);
                world.setSpawnFlags(world.getDifficulty() != EnumDifficulty.PEACEFUL, true);
            } else {
                world.getWorldData().setDifficulty(difficulty);
                world.setSpawnFlags(this.getSpawnMonsters(), this.spawnAnimals);
            }
        }
    }
    
    /**
     * Search entity via uuid from all worlds, null if can't find
     */
    @Nullable
    public Entity getEntityFromUUID(UUID uuid) {
        for (WorldServer world : this.worlds) {
        	if (world == null) continue;
        	
        	Entity entity = world.getEntity(uuid);
            if (entity != null) return entity;
        }

        return null;
    }
    
    /**
     * Returns the spawn protection area's size, default 16
     */
    public int getSpawnProtectionSize() {
    	return this.propertyManager.getInt("spawn-protection", 16);
    }
    
    /**
     * Sets the forceGamemode field (whether joining players will be put in their old gamemode or the default one)
     */
    public void setForceGamemode(boolean flag) {
        this.isGamemodeForced = flag;
    }
    
    /**
     * If the server will announcing player achievements, default true
     */
    public boolean isAnnouncingPlayerAchievements() {
    	return this.propertyManager.getBoolean("announce-player-achievements", true);
    }
    
    public void refreshStatusNextTick() {
        this.nanoTimeSinceStatusRefresh = 0L;
    }
    
    /**
     * Get the spawn radius of the given server, returns 10 if the server is null
     */
    public int getSpawnRadius(@Nullable WorldServer worldserver) {
        return worldserver != null ? worldserver.getGameRules().c("spawnRadius") : 10;
    }
    
    /**
     * The compression treshold. If the packet is larger than the specified amount of bytes, it will be compressed, default 256
     */
    public int getNetworkCompressionThreshold() {
    	return this.propertyManager.getInt("network-compression-threshold", 256);
    }
    
    /**
     * If the server can spawn monsters, default true
     */
    public boolean getSpawnMonsters() {
    	return this.propertyManager.getBoolean("spawn-monsters", true);
    }
    
    /**
     * Get world instance in given dimension, returns the primary world instance if the given dimension can't find
     */
    public WorldServer getWorldServer(int dimension) {
        for (WorldServer world : worlds) {
            if (world.dimension == dimension) {
                return world;
            }
        }
        return worlds.get(0);
    }
    
    /**
     * Returns standard minecraft version of the server, 1.11.2, etc.
     */
    public String getMinecraftVersion() {
        return GAME_VERSION;
    }
    
    /**
     * Returns the maximum supported world size, the vaild value from 1 to 29999984, default to maxium
     */
    public int getMaxWorldSize() {
        int size = this.propertyManager.getInt("max-world-size", 29999984);
        return (size < 1) ? 1 : 29999984;
    }

    /**
     * Mark the server as non-running, then it will stop safety by itself
     */
    public void safeShutdown() {
        this.isRunning = false;
    }
    
    /**
     * Create a new command dispatcher
     */
    public CommandDispatcher createCommandDispatcher() {
        return new CommandDispatcher(getMinecraftServer());
    }
    
    /**
     * Check if the server running in online-mode
     */
    public boolean getOnlineMode() {
        return craftServer.getOnlineMode();
    }
    
    /**
     * Returns the instance of the server.
     */
    public static final TorchServer getServer() {
    	return server;
    }
    
    /**
     * Used by RCon's Query in the form of "MajorServerMod 1.2.3: MyPlugin 1.3; AnotherPlugin 2.1; AndSoForth 1.0".
     */
    public String getPluginsRcon() {
        StringBuilder result = new StringBuilder();
        org.bukkit.plugin.Plugin[] plugins = craftServer.getPluginManager().getPlugins();
        
        result.append(craftServer.getName());
        result.append(" on Bukkit ");
        result.append(craftServer.getBukkitVersion());

        if (plugins.length > 0 && craftServer.getQueryPlugins()) {
            result.append(": ");

            for (int i = 0; i < plugins.length; i++) {
                if (i > 0) {
                    result.append("; ");
                }

                result.append(plugins[i].getDescription().getName());
                result.append(" ");
                result.append(plugins[i].getDescription().getVersion().replaceAll(";", ","));
            }
        }
        
        return result.toString();
    }
    
    /**
     * Check if the block is in the server protected area
     */
    public boolean isBlockProtected(World world, BlockPosition blockposition, EntityHuman entityhuman) {
    	if (world.worldProvider.getDimensionManager().getDimensionID() != 0) {
        } else if (this.getDedicatedPlayerList().getOPs().isEmpty()) {
        } else if (this.getDedicatedPlayerList().isOp(entityhuman.getProfile())) {
        } else if (this.getSpawnProtectionSize() <= 0) {
        } else {
            BlockPosition spawnPosition = world.getSpawn();
            int distanceX = MathHelper.a(blockposition.getX() - spawnPosition.getX());
            int distanceZ = MathHelper.a(blockposition.getZ() - spawnPosition.getZ());
            int maxDistance = Math.max(distanceX, distanceZ);

            return maxDistance <= this.getSpawnProtectionSize();
        }
    	
    	return false;
    }
    
    /**
     * Try to converts the old version UUID files
     */
    public boolean convertFilesUUID() {
    	logger.info("Beginning UUID conversion, this may take A LONG time");
    	
        boolean convertedAny = false, convertedUserBanlist = false; int index;
        for (index = 0; !convertedUserBanlist && index <= 2; ++index) {
            if (index > 0) {
                logger.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
                this.sleepFiveSeconds();
            }

            convertedUserBanlist = NameReferencingFileConverter.a(getMinecraftServer());
            if (convertedUserBanlist) convertedAny = true;
        }
        
        boolean convertedIPBanlist = false;
        for (index = 0; !convertedIPBanlist && index <= 2; ++index) {
            if (index > 0) {
            	logger.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
                this.sleepFiveSeconds();
            }
            
            convertedIPBanlist = NameReferencingFileConverter.b(getMinecraftServer());
            if (convertedIPBanlist) convertedAny = true;
        }
        
        boolean convertedOPlist = false;
        for (index = 0; !convertedOPlist && index <= 2; ++index) {
            if (index > 0) {
            	logger.warn("Encountered a problem while converting the op list, retrying in a few seconds");
                this.sleepFiveSeconds();
            }

            convertedOPlist = NameReferencingFileConverter.c(getMinecraftServer());
            if (convertedOPlist) convertedAny = true;
        }
        
        boolean convertedWhitelist = false;
        for (index = 0; !convertedWhitelist && index <= 2; ++index) {
            if (index > 0) {
            	logger.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
                this.sleepFiveSeconds();
            }

            convertedWhitelist = NameReferencingFileConverter.d(getMinecraftServer());
            if (convertedWhitelist) convertedAny = true;
        }
        
        boolean convertedPlayerFiles = false;
        for (index = 0; !convertedPlayerFiles && index <= 2; ++index) {
            if (index > 0) {
            	logger.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
                this.sleepFiveSeconds();
            }

            convertedPlayerFiles = NameReferencingFileConverter.a(this.getDedicatedServer(), this.propertyManager);
            if (convertedPlayerFiles) convertedAny = true;
        }
        
        return convertedAny;
    }
    
    /**
	 * Returns the internal dedicated server instance
	 */
	public DedicatedServer getDedicatedServer() {
		return DedicatedServer.getServer();
	}
    
    /**
     * Returns the max-tick-time setting from property, 1L by default
     */
    public long getMaxTickTime() {
        return this.propertyManager.getLong("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
    }
    
    /**
     * Execute a command from remote(rcon)
     */
    public String executeRemoteCommand(final String command) {
        Waitable<String> waitable = new Waitable<String>() {
            @Override
            protected String evaluate() {
                remoteControlCommandListener.clearMessages();
                
                RemoteServerCommandEvent event = new RemoteServerCommandEvent(remoteConsole, command);
                craftServer.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return "";
                }
                
                ServerCommand serverCommand = new ServerCommand(event.getCommand(), remoteControlCommandListener);
                craftServer.dispatchServerCommand(remoteConsole, serverCommand);
                return remoteControlCommandListener.getMessages();
            }
        };
        if (command.toLowerCase().startsWith("timings") && command.toLowerCase().matches("timings (report|paste|get|merged|seperate)")) {
            org.bukkit.command.BufferedCommandSender sender = new org.bukkit.command.BufferedCommandSender();
            waitable = new Waitable<String>() {
                @Override
                protected String evaluate() {
                    return sender.getBuffer();
                }
            };
            co.aikar.timings.Timings.generateReport(new co.aikar.timings.TimingsReportListener(sender, waitable));
        } else {
            processQueue.add(waitable);
        }
        try {
            return waitable.get();
        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException("Exception processing rcon command " + command, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted processing rcon command " + command, e);
        }
    }
    
    /**
     * Simply sleep current thread for 5 seconds
     */
    public void sleepFiveSeconds() {
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException interruptedexception) {
            ;
        }
    }

	/**
	 * Check if snooper is enabled, default true but never enable actually(we disabled)
	 * For further info, see http://wiki.vg/Snoop
	 */
	public boolean getSnooperEnabled() {
		return this.propertyManager.getBoolean("snooper-enabled", true);
    }
	
    /**
     * Main tick function and then execute pending commands
     */
    public void updateLogicsPhysicsExecuteCommands() {
        this.updateLogicsAndPhysics();
        this.executePendingCommands();
    }
    
    /**
     * Dispatches the commands issued from console
     */
    public void executePendingCommands() {
    	MinecraftTimings.serverCommandTimer.startTiming();
        while (!this.serverCommandQueue.isEmpty()) {
            ServerCommand servercommand = this.serverCommandQueue.remove(0);
            // Fire the server command event
            ServerCommandEvent event = new ServerCommandEvent(console, servercommand.command);
            craftServer.getPluginManager().callEvent(event);
            if (event.isCancelled()) continue;
            servercommand = new ServerCommand(event.getCommand(), servercommand.source);
            // Dispatch the final command
            craftServer.dispatchServerCommand(console, servercommand);
        }

        MinecraftTimings.serverCommandTimer.stopTiming();
    }
	
	/**
	 * Issue a server command to execute
	 */
	public void issueCommand(String command, ICommandListener listener) {
        this.serverCommandQueue.add(new ServerCommand(command, listener));
    }
	
	/**
	 * Check if current thread is the main thread
	 */
	public boolean isMainThread() {
        return Thread.currentThread() == this.serverThread;
    }
	
    /**
     * Post a callable task to main thread(next tick), also return a listenable future task
     */
    public <V> ListenableFuture<V> postToMainThreadMaybeAsync(Callable<V> callable, boolean onlyPostOnAsync) {
    	if (onlyPostOnAsync && !this.isMainThread()) {
            ListenableFutureTask<V> future = ListenableFutureTask.create(callable);

            this.futureTaskQueue.add(future);
            return future;
        } else {
            try {
                return Futures.immediateFuture(callable.call());
            } catch (Exception exception) {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        }
    }
    
    /**
     * Try to post a runnable task to main thread(next tick), or call it if in main thread, also return a listenable future task, the runnable can't be null
     */
	public ListenableFuture<Object> postToMainThread(Runnable runnable) {
		Validate.notNull(runnable);
		return this.postToMainThreadMaybeAsync(Executors.callable(runnable), true);
	}
	
	/**
	 * Get the Vec3D of the entity called command, returns a pure Vec3D (0, 0, 0) here
	 */
	@Deprecated
	public Vec3D getEntityVec3D() {
		return Vec3D.a;
	}

	/**
	 * Returns a zero blockposition (0, 0, 0)
	 */
	@Deprecated
	public BlockPosition getChunkCoordinates() {
        return BlockPosition.ZERO;
    }

	/**
	 * Returns a green color "Server" string
	 */
	public String getName() {
        return org.bukkit.ChatColor.GREEN + "Server";
    }

	/**
	 * Simply package server name to a ChatComponentText
	 */
	@Deprecated
	public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(this.getName());
    }

	/**
	 * Returns the sendCommandFeedback game rule of the primary world
	 */
	public boolean getSendCommandFeedback() {
        return worlds.get(0).getGameRules().getBoolean("sendCommandFeedback");
    }

	/**
	 * Returns the primary world
	 */
    public World getWorld() {
        return this.worlds.get(0);
    }

	/**
	 * Simply send a message
	 */
    public void sendMessage(IChatBaseComponent component) {
        logger.info(component.toPlainText());
    }

	/**
	 * Get the entity called command, returns null here
	 */
	@Deprecated
	public Entity getEntity() {
		return null;
	}

	/**
	 * Returns the internal minecraft server instance
	 */
	public static MinecraftServer getMinecraftServer() {
		return MinecraftServer.getServer();
	}
	
	/**
     * Sets the game type for the server and all worlds
     */
    public void setGamemodeGlobal(EnumGamemode gamemode) {
        this.setGamemodeForWorlds(gamemode);
        this.gameMode = gamemode;
    }
    
    /**
     * Adds the server info, including from theWorldServer, to the crash report.
     */
    public CrashReport addServerInfoToCrashReportDedicated(CrashReport crashreport) {
        crashreport = this.addServerInfoToCrashReport(crashreport);
        crashreport.g().a("Is Modded", new CrashReportCallable<String>() {
            @Override
			public String call() throws Exception {
            	String moddedName = getServerModName();

                return !"vanilla".equals(moddedName) ? "Definitely; Server brand changed to \'" + moddedName + "\'" : "Unknown (can\'t tell)";
            }
        });
        
        crashreport.g().a("Type", new CrashReportCallable<String>() {
            @Override
			public String call() throws Exception {
            	return "Dedicated Server (map_server.txt)";
            }
        });
        return crashreport;
    }
	
	/**
     * Initialises the server and starts it
     */
	public boolean init() throws IOException {
		Thread consoleHandlerThread = new Thread("Server console handler") {
            @Override
			public void run() {
                if (!org.bukkit.craftbukkit.Main.useConsole) return;

                jline.console.ConsoleReader bufferedreader = getMinecraftServer().reader;
                String sourceCommand;
                
                try {
                    // JLine disabling compatibility
                    while (!isStopped() && isRunning()) {
                        if (org.bukkit.craftbukkit.Main.useJline) {
                        	sourceCommand = bufferedreader.readLine(">", null);
                        } else {
                        	sourceCommand = bufferedreader.readLine();
                        }
                        // Trim to filter lines which are just spaces
                        if (sourceCommand != null && sourceCommand.trim().length() > 0) {
                            issueCommand(sourceCommand, getDedicatedServer());
                        }
                    }
                } catch (IOException ioexception) {
                    logger.error("Exception handling console input", ioexception);
                }

            }
        };
        
        java.util.logging.Logger global = java.util.logging.Logger.getLogger("");
        global.setUseParentHandlers(false);
        for (java.util.logging.Handler handler : global.getHandlers()) {
            global.removeHandler(handler);
        }
        global.addHandler(new org.bukkit.craftbukkit.util.ForwardLogHandler());
        
        final org.apache.logging.log4j.core.Logger logger = ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger());
        for (org.apache.logging.log4j.core.Appender appender : logger.getAppenders().values()) {
            if (appender instanceof org.apache.logging.log4j.core.appender.ConsoleAppender) {
                logger.removeAppender(appender);
            }
        }
        
        new Thread(new org.bukkit.craftbukkit.util.TerminalConsoleWriterThread(System.out, this.reader)).start();

        System.setOut(new PrintStream(new LoggerOutputStream(logger, Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(logger, Level.WARN), true));

        consoleHandlerThread.setDaemon(true);
        consoleHandlerThread.start();
        
        logger.info("Starting minecraft server version " + GAME_VERSION);
        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
            logger.warn("Better to start the server with more ram, launch it as \"java -d64 -server -Xmx1024M -Xms1024M -jar torchpowered.jar\"");
        }
        
        //logger.info("Loading properties");
        // CLI argument support
        this.getDedicatedServer().propertyManager = this.propertyManager = new PropertyManager(this.options);
        
        if (this.isSinglePlayer()) {
            this.setServerIp("127.0.0.1");
        } else {
            this.setOnlineMode(this.propertyManager.getBoolean("online-mode", true));
            this.setPreventProxyConnections(this.propertyManager.getBoolean("prevent-proxy-connections", false));
            this.setServerIp(this.propertyManager.getString("server-ip", ""));
        }
        
        this.setSpawnAnimals(this.propertyManager.getBoolean("spawn-animals", true));
        this.setSpawnNPCs(this.propertyManager.getBoolean("spawn-npcs", true));
        this.setPvpMode(this.propertyManager.getBoolean("pvp", true));
        this.setAllowFlight(this.propertyManager.getBoolean("allow-flight", false));
        this.setResourcePack(this.propertyManager.getString("resource-pack", ""), this.loadResourcePackSHA());
        this.setMotd(this.propertyManager.getString("motd", "A Minecraft Server"));
        this.setForceGamemode(this.propertyManager.getBoolean("force-gamemode", false));
        this.setIdleTimeout(this.propertyManager.getInt("player-idle-timeout", 0));
        if (this.propertyManager.getInt("difficulty", 1) < 0) {
            this.propertyManager.setProperty("difficulty", Integer.valueOf(0));
        } else if (this.propertyManager.getInt("difficulty", 1) > 3) {
            this.propertyManager.setProperty("difficulty", Integer.valueOf(3));
        }

        this.generateStructures = this.propertyManager.getBoolean("generate-structures", true);
        int i = this.propertyManager.getInt("gamemode", EnumGamemode.SURVIVAL.getId());
        
        this.gameMode = WorldSettings.a(i);
        logger.info("Default game type: {}", new Object[] { this.gameMode });
        
        InetAddress inetaddress = null;
        if (!this.getServerIp().isEmpty()) inetaddress = InetAddress.getByName(this.getServerIp());
        
        if (this.getServerPort() < 0) {
            this.setServerPort(this.propertyManager.getInt("server-port", 25565));
        }
        
        // Initial the craft server in player list
        this.setPlayerList(new DedicatedPlayerList(this.getDedicatedServer()).getReactor());
        this.getServant().setPlayerList(this.playerList);
        
        org.spigotmc.SpigotConfig.init((File) options.valueOf("spigot-settings"));
        org.spigotmc.SpigotConfig.registerCommands();
        
        com.destroystokyo.paper.PaperConfig.init((File) options.valueOf("paper-settings"));
        com.destroystokyo.paper.PaperConfig.registerCommands();

        //logger.info("Generating keypair");
        this.setServerKeyPair(MinecraftEncryption.b());
        logger.info("Binding server to {}:{}", new Object[] { this.getServerIp().isEmpty() ? "*" : this.getServerIp(), Integer.valueOf(this.getServerPort()) });
        
        if (!org.spigotmc.SpigotConfig.lateBind) {
            try {
                this.getServerConnection().a(inetaddress, this.getServerPort());
            } catch (IOException ioexception) {
                logger.warn("**** FAILED TO BIND TO PORT!");
                logger.warn("The exception was: {}", new Object[] { ioexception.toString()});
                logger.warn("Perhaps a server is already running on that port?");
                return false;
            }
        }
        
        craftServer.loadPlugins();
        craftServer.enablePlugins(org.bukkit.plugin.PluginLoadOrder.STARTUP);
        
        if (!this.getOnlineMode()) {
            logger.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            logger.warn("The server will make no attempt to authenticate usernames. Beware.");
            // Spigot start
            if (org.spigotmc.SpigotConfig.bungee) {
                logger.warn("Whilst this makes it possible to use BungeeCord, unless access to your server is properly restricted, it also opens up the ability for hackers to connect with any username they choose.");
                logger.warn("Please see http://www.spigotmc.org/wiki/firewall-guide/ for further information.");
            } else {
                logger.warn("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
            }
            // Spigot end
            logger.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
        }
        
        if (this.convertFilesUUID()) {
        	// Save the user cache after convert them
            this.getUserCache().c();
        }
        
        if (!NameReferencingFileConverter.a(this.propertyManager)) return false;
        
        this.anvilFileConverter = new WorldLoaderServer(craftServer.getWorldContainer(), this.getDataConverterManager());
        long j = System.nanoTime();
        
        if (this.getPrimaryWorldFolderName() == null) {
            this.setPrimaryWorldFolderName(this.propertyManager.getString("level-name", "world"));
        }
        
        String levelSeed = this.propertyManager.getString("level-seed", "");
        String levelType = this.propertyManager.getString("level-type", "DEFAULT");
        String generatorSettings = this.propertyManager.getString("generator-settings", "");
        // The generator seed we final used
        long generatorSeed = (new Random()).nextLong();
        if (!levelSeed.isEmpty()) {
            try {
                long seed = Long.parseLong(levelSeed);
                // If the given seed is invaild, use random seed
                if (seed != 0L) generatorSeed = seed;
            } catch (NumberFormatException numberformatexception) {
            	generatorSeed = levelSeed.hashCode();
            }
        }
        
        WorldType worldtype = WorldType.getType(levelType);
        if (worldtype == null) worldtype = WorldType.NORMAL;
        
        this.isAnnouncingPlayerAchievements();
        this.getEnableCommandBlock();
        this.getOpPermissionLevel();
        this.getSnooperEnabled();
        this.getNetworkCompressionThreshold();
        
        this.setBuildLimit(this.propertyManager.getInt("max-build-height", 256));
        this.setBuildLimit((this.getBuildLimit() + 8) / 16 * 16);
        this.setBuildLimit(MathHelper.clamp(this.getBuildLimit(), 64, 256));
        this.propertyManager.setProperty("max-build-height", Integer.valueOf(this.getBuildLimit()));
        
        // Set user cache and session service for skulls
        TileEntitySkull.a(this.getUserCache());
        TileEntitySkull.a(this.getSessionService());
        // Set online mode for the user cache
        UserCache.a(this.getOnlineMode());
        
        logger.info("Preparing level \"{}\"", new Object[] { this.getPrimaryWorldFolderName() });
        this.loadDefaultWorlds(this.getPrimaryWorldFolderName(), this.getPrimaryWorldFolderName(), generatorSeed, worldtype, generatorSettings);
        
        long i1 = System.nanoTime() - j;
        String s3 = String.format("%.3fs", new Object[] { Double.valueOf(i1 / 1.0E9D)});
        
        logger.info("Ready for connections! ({})", new Object[] { s3});
        
        if (this.propertyManager.getBoolean("enable-query", false)) {
            logger.info("Starting GS4 status listener");
            this.remoteQueryListener = new RemoteStatusListener(this.getDedicatedServer());
            // Start the rcon query thread
            this.remoteQueryListener.a();
        }
        
        if (this.propertyManager.getBoolean("enable-rcon", false)) {
            logger.info("Starting remote control listener");
            this.remoteControlListener = new RemoteControlListener(this.getDedicatedServer());
            // Start the rcon listener thread
            this.remoteControlListener.a();
            this.remoteConsole = new org.bukkit.craftbukkit.command.CraftRemoteConsoleCommandSender(this.remoteControlCommandListener);
        }
        
        if (this.craftServer.getBukkitSpawnRadius() > -1) {
            logger.info("'settings.spawn-radius' in bukkit.yml has been moved to 'spawn-protection' in server.properties. I will move your config for you.");
            this.propertyManager.properties.remove("spawn-protection");
            this.propertyManager.getInt("spawn-protection", this.craftServer.getBukkitSpawnRadius());
            this.craftServer.removeBukkitSpawnRadius();
            this.propertyManager.savePropertiesFile();
        }
        
        if (org.spigotmc.SpigotConfig.lateBind) {
            try {
                this.getServerConnection().a(inetaddress, this.getServerPort());
            } catch (IOException ioexception) {
                logger.warn("**** FAILED TO BIND TO PORT!");
                logger.warn("The exception was: {}", new Object[] { ioexception.toString()});
                logger.warn("Perhaps a server is already running on that port?");
                return false;
            }
        }
        
        return true;
	}

    /**
     * Get the server's difficulty, returns default(normal) if not found
     */
    public EnumDifficulty getDifficulty() {
    	return EnumDifficulty.getById(this.propertyManager.getInt("difficulty", EnumDifficulty.NORMAL.a()));
    }

    /**
     * Defaults to false
     */
    public boolean isHardcore() {
    	return this.propertyManager.getBoolean("hardcore", false);
    }
    
    /**
     * Defaults to 4
     */
    public int getOpPermissionLevel() {
    	return this.propertyManager.getInt("op-permission-level", 4);
    }

    /**
     * Get if RCON command events should be broadcast to ops, default true
     */
    public boolean shouldBroadcastRconToOps() {
    	return this.propertyManager.getBoolean("broadcast-rcon-to-ops", true);
    }
    
    /**
     * If the server running in debug mode(the mode with debug info output), default false
     */
    public boolean isDebugging() {
        return this.getPropertyManager().getBoolean("debug", false);
    }

    /**
     * Get if console command events should be broadcast to ops, default true
     */
    public boolean shouldBroadcastConsoleToOps() {
    	return this.propertyManager.getBoolean("broadcast-console-to-ops", true);
    }
    
    /**
     * This method will only called from a dedicated server, so it will always return true
     */
    public static boolean isDedicatedServer() {
    	return true;
    }
    
    /**
     * Get if native transport should be used. Native transport means linux server performance improvements and
     * optimized packet sending/receiving on linux
     */
    public boolean shouldUseNativeTransport() {
    	return this.propertyManager.getBoolean("use-native-transport", true);
    }
    
    @Deprecated
    public net.minecraft.server.DedicatedPlayerList getDedicatedPlayerList() {
        return (net.minecraft.server.DedicatedPlayerList) this.playerList.getServant();
    }
    
    /**
     * Gets an integer property. If it does not exist, set it to the specified value
     */
    public int getIntProperty(String key, int defaultValue) {
    	return this.propertyManager.getInt(key, defaultValue);
    }

    /**
     * Gets a string property. If it does not exist, set it to the specified value
     */
    public String getStringProperty(String key, String defaultValue) {
    	return this.propertyManager.getString(key, defaultValue);
    }

    /**
     * Gets a boolean property. If it does not exist, set it to the specified value
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
    	return this.propertyManager.getBoolean(key, defaultValue);
    }

    /**
     * Saves an Object with the given property name
     */
    public void setProperty(String key, Object value) {
    	this.propertyManager.setProperty(key, value);
    }

    /**
     * Saves all of the server properties to the properties file
     */
    public void saveProperties() {
    	this.propertyManager.savePropertiesFile();
    }

    /**
     * Returns the absolute path where server properties are stored
     */
    public String getSettingsPath() {
        // Get the properties file
        File file = this.propertyManager.c();
        return file != null ? file.getAbsolutePath() : "No settings file";
    }
    
    public void setGuiEnabled() {
    	ServerGUI.a(this.getDedicatedServer());
        this.guiIsEnabled = true;
    }
    
    /**
     * Return whether command blocks are enabled
     */
    public boolean getEnableCommandBlock() {
    	return this.propertyManager.getBoolean("enable-command-block", false);
    }
    
    /**
     * On dedicated server does nothing. On LAN servers, sets commandsAllowedForAll, gameType and allows external connections
     */
    @Deprecated
    public String shareToLAN(EnumGamemode gamemode, boolean flag) {
    	return "";
    }
    
	@Override
	public MinecraftServer getServant() {
		return MinecraftServer.getServer();
	}
}
