package net.minecraft.server;

import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;
// CraftBukkit start
import jline.console.ConsoleReader;
import joptsimple.OptionSet;
// CraftBukkit end
import org.spigotmc.SlackActivityAccountant; // Spigot
import org.torch.api.Anaphase;
import org.torch.api.TorchReactor;
import org.torch.server.TorchServer;

public abstract class MinecraftServer implements Runnable, ICommandListener, IAsyncTaskHandler, IMojangStatistics, org.torch.api.TorchServant {
	/**
	 * STATIC FIELDS
	 */
	/**
	 * Torch server instance
	 */
	private static TorchServer reactor;
	/**
	 * Legacy server instance
	 */
    private static MinecraftServer SERVER;
    /**
     * Common logger
     */
    public static final Logger LOGGER = TorchServer.logger;
    /**
     * User cache file
     */
    public static final File a = TorchServer.USER_CACHE_FILE;
    /**
     * The sample TPS
     */
    private static final int TPS = TorchServer.SAMPLE_TPS;
    /**
     * The sample nano per second
     */
    private static final long SEC_IN_NANO = TorchServer.SAMPLE_SEC_IN_NANO;
    /**
     * The sample full tick time
     */
    public static final long TICK_TIME = TorchServer.SAMPLE_TICK_TIME;
    /**
     * The sample maxi catchup buffer
     */
    private static final long MAX_CATCHUP_BUFFER = TorchServer.SAMPLE_MAX_CATCHUP_BUFFER;
    /**
     * The sample tps interval
     */
    private static final int SAMPLE_INTERVAL = TorchServer.SAMPLE_TICK_INTERVAL;
    
    /**
     * ANAPHASE FIELDS
     */
    /**
     * The default world instances
     */
    @Anaphase public WorldServer[] worldServer;
    /**
     * The current tick number, incremented every tick
     */
    @Anaphase public static int currentTick = 0;
    /**
     * {@see MinecraftServer#currentTick}
     */
    @Anaphase @lombok.Setter private int ticks = currentTick;
    /**
     * Current TPS from 1m
     */
    @Anaphase public final RollingAverage tps1 = new RollingAverage(60);
    /**
     * Current TPS from 5m
     */
    @Anaphase public final RollingAverage tps5 = new RollingAverage(60 * 5);
    /**
     * Current TPS from 15m
     */
    @Anaphase public final RollingAverage tps15 = new RollingAverage(60 * 15);
    /**
     * Current TPS
     */
    @Anaphase public double[] recentTps = new double[3];
    
    /**
     * NORMAL FIELDS
     */
    /** anvilFileConverter */
    public Convertable convertable;
    /** universeAnvilFile */
    public File universe;
    public final MethodProfiler methodProfiler;
    private final DataConverterManager dataConverterManager;
    private String serverIp;
    private boolean isRunning;
    private boolean isStopped;
    private boolean onlineMode;
    private boolean spawnAnimals;
    private boolean spawnNPCs;
    private boolean pvpMode;
    private boolean allowFlight;
    private String motd;
    private boolean demoMode;
    private Thread serverThread;
    /** serverThread */
    public final Thread primaryThread;
    public List<WorldServer> worlds;
    /** craftServer */
    public org.bukkit.craftbukkit.CraftServer server;
    public OptionSet options;
    public org.bukkit.command.ConsoleCommandSender console;
    public org.bukkit.command.RemoteConsoleCommandSender remoteConsole;
    public ConsoleReader reader;
    public java.util.Queue<Runnable> processQueue;
    public int autosavePeriod;
    public boolean serverAutoSave;
    public final SlackActivityAccountant slackActivityAccountant;
    private boolean hasStopped;
    private final Object stopLock;
    
    /**
     * OBFUSCATED FIELDS
     */
    /** usageSnooper */
    private final MojangStatisticsGenerator m;
    /** tickables */
    private final List<ITickable> o;
    /** commandManager */
    public final ICommandHandler b;
    /** serverConnection */
    private ServerConnection p;
    /** serverPing */
    private final ServerPing q;
    /** random */
    private final Random r;
    /** serverPort */
    private int u = -1;
    /** playerList */
    private PlayerList v;
    /** serverProxy */
    protected final Proxy e;
    /** currentTask */
    public String f;
    /** percentDone */
    public int g;
    /** preventProxyConnections */
    private boolean A;
    /** buildLimit */
    private int G;
    /** maxPlayerIdleMinutes */
    private int H;
    /** tickTimeArray */
    public final long[] h;
    /** timeOfLastDimensionTick */
    public long[][] i;
    /** serverKeyPair */
    private KeyPair I;
    /** serverOwner */
    private String J;
    /** folderName */
    private String K;
    /** enableBonusChest */
    private boolean N;
    /** resourcePackUrl */
    private String O;
    /** resourcePackHash */
    private String P;
    /** serverIsRunning */
    private boolean Q;
    /** timeOfLastWarning*/
    private long R;
    /** userMessage */
    private String S;
    /** startProfiling */
    private boolean T;
    /** isGamemodeForced */
    private boolean U;
    /** authService */
    private final YggdrasilAuthenticationService V;
    /** sessionService */
    private final MinecraftSessionService W;
    /** profileRepo */
    private final GameProfileRepository X;
    /** userCache */
    private final UserCache Y;
    /** nanoTimeSinceStatusRefresh */
    private long Z;
    /** futureTaskQueue */
    protected final Queue<FutureTask<?>> j;
    /** currentTime */
    private long ab;

    public static class RollingAverage {
        private final int size;
        private long time;
        private double total;
        private int index = 0;
        private final double[] samples;
        private final long[] times;

        public RollingAverage(int size) {
            this.size = size;
            this.time = size * SEC_IN_NANO;
            this.total = TPS * SEC_IN_NANO * size;
            this.samples = new double[size];
            this.times = new long[size];
            for (int i = 0; i < size; i++) {
                this.samples[i] = TPS;
                this.times[i] = SEC_IN_NANO;
            }
        }

        public void add(double x, long t) {
            time -= times[index];
            total -= samples[index] * times[index];
            samples[index] = x;
            times[index] = t;
            time += t;
            total += x * t;
            if (++index == size) {
                index = 0;
            }
        }

        public double getAverage() {
            return total / time;
        }
    }

    public MinecraftServer(OptionSet optionSet, Proxy proxy, DataConverterManager dataconvertermanager, YggdrasilAuthenticationService yggdrasilauthenticationservice, MinecraftSessionService minecraftsessionservice, GameProfileRepository gameprofilerepository, UserCache usercache) {
    	// Setup instance for org.torch.api.TorchReactor
    	SERVER = this;
    	// Setup instance for org.torch.api.TorchServant
    	if (proxy == Proxy.NO_PROXY) {
    		// Request from dedicated server constructor
    		reactor = new TorchServer(optionSet, dataconvertermanager, yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, usercache);
    	} else {
    		reactor = new TorchServer(optionSet, proxy, dataconvertermanager, yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, usercache);
    	}
    	
    	/**
    	 * NORMAL FIELDS
    	 */
    	convertable = reactor.getAnvilFileConverter();
    	universe = reactor.getUniverseAnvilFile();
    	methodProfiler = reactor.getMethodProfiler();
    	dataConverterManager = reactor.getDataConverterManager();
    	serverIp = reactor.getServerIp();
    	isRunning = reactor.isRunning();
    	onlineMode = reactor.onlineMode; // SP
    	spawnAnimals = reactor.isSpawnAnimals();
    	spawnNPCs = reactor.isSpawnNPCs();
    	pvpMode = reactor.isPvpMode();
    	allowFlight = reactor.isAllowFlight();
    	motd = reactor.getMotd();
    	demoMode = reactor.isDemoMode();
    	worlds = reactor.getWorlds();
    	server = reactor.getCraftServer();
    	options = reactor.getOptions();
    	console = reactor.getConsole();
    	remoteConsole = reactor.getRemoteConsole();
    	reader = reactor.getReader();
    	processQueue = reactor.getProcessQueue();
    	autosavePeriod = reactor.getAutosavePeriod();
    	serverAutoSave = reactor.isServerAutoSave();
    	slackActivityAccountant = reactor.getSlackActivityAccountant();
    	hasStopped = reactor.isHasStopped();
    	stopLock = reactor.getStopLock();
    	serverThread = primaryThread = reactor.getServerThread();
    	
    	/**
    	 * OBFUSCATED FIELDS
    	 */
    	m = reactor.getUsageSnooper();
    	o = reactor.getTickables();
    	b = reactor.getCommandManager();
    	p = reactor.getServerConnection();
    	q = reactor.getServerPing();
    	r = reactor.getRandom();
    	u = reactor.getServerPort();
    	v = reactor.getPlayerList();
    	e = reactor.getServerProxy();
    	f = reactor.getCurrentTask();
    	g = reactor.getPercentDone();
    	A = reactor.isPreventProxyConnections();
    	G = reactor.getBuildLimit();
    	H = reactor.getMaxPlayerIdleMinutes();
    	h = reactor.getTickTimeArray();
    	i = reactor.getTimeOfLastDimensionTick();
    	I = reactor.getServerKeyPair();
    	J = reactor.getServerOwner();
    	K = reactor.getPrimaryWorldFolderName();
    	N = reactor.isEnableBonusChest();
    	O = reactor.getResourcePackUrl();
    	P = reactor.getResourcePackHash();
    	Q = reactor.isServerIsRunning();
    	R = reactor.getTimeOfLastWarning();
    	S = reactor.getUserMessage();
    	T = reactor.isStartProfiling();
    	U = reactor.isGamemodeForced();
    	V = reactor.getAuthService();
    	W = reactor.getSessionService();
    	X = reactor.getProfileRepo();
    	Y = reactor.getUserCache();
    	Z = reactor.getNanoTimeSinceStatusRefresh();
    	j = reactor.getFutureTaskQueue();
    	ab = reactor.getCurrentTime();
    }

    public abstract PropertyManager getPropertyManager();
    // CraftBukkit end

    protected CommandDispatcher i() {
        return reactor.createCommandDispatcher();
    }

    public abstract boolean init() throws IOException;

    protected void a(String s) {
        reactor.convertMapIfNeeded(s);
    }

    protected synchronized void b(String s) {
        reactor.setUserMessage(s);
    }

    public void a(String s, String s1, long i, WorldType worldtype, String s2) {
    	reactor.loadDefaultWorlds(s, s1, i, worldtype, s2);
    }

    protected void l() {
    	reactor.initialAllWorldsChunk();
    }

    protected void a(String s, IDataManager idatamanager) {
    	reactor.setResourcePackFromWorld(s, idatamanager);
    }

    public abstract boolean getGenerateStructures();

    public abstract EnumGamemode getGamemode();

    public abstract EnumDifficulty getDifficulty();

    public abstract boolean isHardcore();

    public abstract int q();

    public abstract boolean r();

    public abstract boolean s();

    protected void a_(String s, int i) {
        reactor.setCurrentTask(s, i);
    }

    protected void t() {
        reactor.clearCurrentTask();
    }

    protected void saveChunks(boolean flag) {
        reactor.saveChunks(flag);
    }

    public void stop() throws ExceptionWorldConflict { // CraftBukkit - added throws
    	reactor.stopServer();
    }

    public String getServerIp() {
        return reactor.getServerIp();
    }

    public void c(String s) {
    	reactor.setServerIp(s);
    }

    public boolean isRunning() {
        return reactor.isRunning();
    }

    public void safeShutdown() {
    	reactor.safeShutdown();
    }

    @Override
	public void run() {
    	reactor.run();
    }

    public void a(ServerPing serverping) {
    	reactor.applyServerIconToPing(serverping);
    }

    public File A() {
        return reactor.getDataDirectory();
    }

    protected void a(CrashReport crashreport) {}

    public void B() {}

    protected void C() throws ExceptionWorldConflict { // CraftBukkit - added throws
    	reactor.tick();
    }

    public void D() {
    	reactor.updateLogicsAndPhysics();
    }

    public boolean getAllowNether() {
        return reactor.getAllowNether();
    }

    public void a(ITickable itickable) {
    	reactor.registerTickable(itickable);
    }

    public static void main(final OptionSet options) { // CraftBukkit - replaces main(String[] astring)
    	TorchServer.main(options);
    }

    public void F() {
    	reactor.startServerThread();
    }

    public File d(String s) {
        return reactor.getFile(s);
    }

    public void info(String s) {
    	reactor.info(s);
    }

    public void warning(String s) {
    	reactor.warning(s);
    }

    public WorldServer getWorldServer(int i) {
    	return reactor.getWorldServer(i);
    }

    public String getVersion() {
        return reactor.getMinecraftVersion();
    }

    public int H() {
        return reactor.getCurrentPlayerCount();
    }

    public int I() {
        return reactor.getMaxPlayers();
    }

    public String[] getPlayers() {
        return reactor.getOnlinePlayerNames();
    }

    public GameProfile[] K() {
        return reactor.getOnlinePlayerProfiles();
    }

    public boolean isDebugging() {
        return reactor.isDebugging();
    }

    public void g(String s) {
    	reactor.error(s);
    }

    public void h(String s) {
    	reactor.debug(s);
    }

    public String getServerModName() {
        return reactor.getServerModName();
    }

    public CrashReport b(CrashReport crashreport) {
    	return reactor.addServerInfoToCrashReport(crashreport);
    }

    public List<String> tabCompleteCommand(ICommandListener icommandlistener, String s, @Nullable BlockPosition blockposition, boolean flag) {
    	return reactor.tabCompleteCommand(icommandlistener, s, blockposition, flag);
    }

    public boolean M() {
        return reactor.isAnvilFileSet();
    }

    @Override
	public String getName() {
        return reactor.getName();
    }

    @Override
	public void sendMessage(IChatBaseComponent ichatbasecomponent) {
    	reactor.sendMessage(ichatbasecomponent);
    }

    @Override
	public boolean a(int i, String s) {
        return reactor.canUseCommand(i, s);
    }

    public ICommandHandler getCommandHandler() {
        return reactor.getCommandManager();
    }

    public KeyPair O() {
        return reactor.getServerKeyPair();
    }

    public int P() {
        return reactor.getServerPort();
    }

    public void setPort(int i) {
    	reactor.setServerPort(i);
    }

    public String Q() {
        return reactor.getServerOwner();
    }

    public void i(String s) {
    	reactor.setServerOwner(s);
    }

    public boolean R() {
        return reactor.isSinglePlayer();
    }

    public String S() {
        return reactor.getPrimaryWorldFolderName();
    }

    public void setWorld(String s) {
    	reactor.setPrimaryWorldFolderName(s);
    }

    public void a(KeyPair keypair) {
    	reactor.setServerKeyPair(keypair);
    }

    public void a(EnumDifficulty enumdifficulty) {
        reactor.setDifficultyForAllWorlds(enumdifficulty);
    }

    public boolean getSpawnMonsters() {
        return reactor.getSpawnMonsters();
    }

    public boolean V() {
        return reactor.isDemoMode();
    }

    public void b(boolean flag) {
    	reactor.setDemoMode(flag);
    }

    public void c(boolean flag) {
    	reactor.setEnableBonusChest(flag);
    }

    public Convertable getConvertable() {
        return reactor.getAnvilFileConverter();
    }

    public String getResourcePack() {
        return reactor.getResourcePackUrl();
    }

    public String getResourcePackHash() {
        return reactor.getResourcePackHash();
    }

    public void setResourcePack(String s, String s1) {
    	reactor.setResourcePack(s, s1);
    }

    // Snooper
    @Override
	public void a(MojangStatisticsGenerator mojangstatisticsgenerator) {}

    // Snooper
    @Override
	public void b(MojangStatisticsGenerator mojangstatisticsgenerator) {}

    @Override
	public boolean getSnooperEnabled() {
        return reactor.getSnooperEnabled();
    }

    public abstract boolean aa();

    public boolean getOnlineMode() {
        return reactor.getCraftServer().getOnlineMode();
    }

    public void setOnlineMode(boolean flag) {
    	reactor.setOnlineMode(flag);
    }

    public boolean ac() {
        return reactor.isPreventProxyConnections();
    }

    public void e(boolean flag) {
    	reactor.setPreventProxyConnections(flag);
    }

    public boolean getSpawnAnimals() {
        return reactor.isSpawnAnimals();
    }

    public void setSpawnAnimals(boolean flag) {
    	reactor.setSpawnAnimals(flag);
    }

    public boolean getSpawnNPCs() {
        return reactor.isSpawnNPCs();
    }

    public abstract boolean af();

    public void setSpawnNPCs(boolean flag) {
    	reactor.setSpawnNPCs(flag);
    }

    public boolean getPVP() {
        return reactor.isPvpMode();
    }

    public void setPVP(boolean flag) {
    	reactor.setPvpMode(flag);
    }

    public boolean getAllowFlight() {
        return reactor.isAllowFlight();
    }

    public void setAllowFlight(boolean flag) {
    	reactor.setAllowFlight(flag);
    }

    public abstract boolean getEnableCommandBlock();

    public String getMotd() {
        return reactor.getMotd();
    }

    public void setMotd(String s) {
    	reactor.setMotd(s);
    }

    public int getMaxBuildHeight() {
        return reactor.getBuildLimit();
    }

    public void c(int i) {
    	reactor.setBuildLimit(i);
    }

    public boolean isStopped() {
        return reactor.isStopped();
    }

    public PlayerList getPlayerList() {
        return reactor.getPlayerList();
    }

    public void a(PlayerList playerlist) {
    	reactor.setPlayerList(playerlist);
    }

    public void setGamemode(EnumGamemode enumgamemode) {
    	reactor.setGamemodeForWorlds(enumgamemode);
    }

    public ServerConnection getServerConnection() {
        return reactor.getServerConnection();
    }
    
    public void setServerConnection(ServerConnection connection) {
        this.p = connection;
    }
    
    public ServerConnection an() {
        return reactor.handleServerConnection();
    }

    public boolean ap() {
        return reactor.isGuiIsEnabled();
    }

    public abstract String a(EnumGamemode enumgamemode, boolean flag);

    public int aq() {
        return reactor.getCurrentTick();
    }

    public void ar() {
    	reactor.startProfiling();
    }

    @Override
	public BlockPosition getChunkCoordinates() {
        return reactor.getChunkCoordinates();
    }

    @Override
	public Vec3D d() {
        return reactor.getEntityVec3D();
    }

    @Override
	public World getWorld() {
        return reactor.getWorld();
    }

    @Override
	public Entity f() {
        return reactor.getEntity();
    }

    public int getSpawnProtection() {
        return reactor.getSpawnProtectionSize();
    }

    public boolean a(World world, BlockPosition blockposition, EntityHuman entityhuman) {
        return reactor.isBlockProtected(world, blockposition, entityhuman);
    }

    public void setForceGamemode(boolean flag) {
    	reactor.setForceGamemode(flag);
    }

    public boolean getForceGamemode() {
        return reactor.isGamemodeForced();
    }

    public Proxy av() {
        return reactor.getServerProxy();
    }

    public static long aw() {
        return System.currentTimeMillis();
    }

    public int getIdleTimeout() {
        return reactor.getMaxPlayerIdleMinutes();
    }

    public void setIdleTimeout(int i) {
    	reactor.setIdleTimeout(i);
    }

    @Override
	public IChatBaseComponent getScoreboardDisplayName() {
        return reactor.getScoreboardDisplayName();
    }

    public boolean ay() {
        return reactor.isAnnouncingPlayerAchievements();
    }

    public MinecraftSessionService az() {
        return reactor.getSessionService();
    }

    public GameProfileRepository getGameProfileRepository() {
        return reactor.getProfileRepo();
    }

    public UserCache getUserCache() {
        return reactor.getUserCache();
    }

    public ServerPing getServerPing() {
        return reactor.getServerPing();
    }

    public void aD() {
    	reactor.refreshStatusNextTick();
    }

    @Nullable
    public Entity a(UUID uuid) {
    	return reactor.getEntityFromUUID(uuid);
    }

    @Override
	public boolean getSendCommandFeedback() {
        return reactor.getSendCommandFeedback();
    }

    @Override
	public void a(CommandObjectiveExecutor.EnumCommandResult commandobjectiveexecutor_enumcommandresult, int i) {}

    @Override
	public MinecraftServer B_() {
        return reactor.getMinecraftServer();
    }

    public int aE() {
        return reactor.getMaxWorldSize();
    }

    public <V> ListenableFuture<V> a(Callable<V> callable) {
    	return reactor.postToMainThreadMaybeAsync(callable, true);
    }

    @Override
	public ListenableFuture<Object> postToMainThread(Runnable runnable) {
        return reactor.postToMainThread(runnable);
    }

    @Override
	public boolean isMainThread() {
        return reactor.isMainThread();
    }

    public int aG() {
        return reactor.getNetworkCompressionThreshold();
    }

    public long aH() {
        return reactor.getCurrentTime();
    }

    public Thread aI() {
        return reactor.getServerThread();
    }

    public DataConverterManager getDataConverterManager() {
        return reactor.getDataConverterManager();
    }

    public int a(@Nullable WorldServer worldserver) {
        return reactor.getSpawnRadius(worldserver);
    }

    public static MinecraftServer getServer() {
        return SERVER;
    }
    
    @Override
	public TorchServer getReactor() {
    	return reactor;
    }
}
