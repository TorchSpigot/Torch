package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.IOException;
import java.net.Proxy;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Logger;

import org.torch.server.TorchServer;

public class DedicatedServer extends MinecraftServer implements IMinecraftServer, org.torch.api.TorchServant {
    /**
     * STATIC FIELDS
     */
    /**
     * Legacy dedicated server instance
     */
    private static DedicatedServer instance;
    /**
     * Common logger
     */
    private static final Logger LOGGER = TorchServer.logger;
    /**
     * SHA-1 pattern
     */
    private static final Pattern l = TorchServer.RESOURCE_PACK_SHA1_PATTERN;

    /**
     * NORMAL FIELDS
     */
    /**
     * Server command queue -> serverCommandQueue
     */
    private final Queue<ServerCommand> serverCommandQueue; // Torch - List -> Queue
    /**
     * Rcon command listener -> remoteControlCommandListener
     */
    public final RemoteControlCommandListener remoteControlCommandListener;
    /**
     * Server properties -> propertyManager
     */
    public PropertyManager propertyManager;
    /**
     * If generate structures -> generateStructures
     */
    private boolean generateStructures;

    /**
     * OBFUSCATED FIELDS
     */
    /**
     * Rcon query listener -> remoteQueryListener
     */
    private RemoteStatusListener n;
    /**
     * Rcon control listener -> remoteControlListener
     */
    private RemoteControlListener p;
    /**
     * EULA -> eula
     */
    private EULA r;
    /**
     * Game mode -> gameMode
     */
    private EnumGamemode t;
    /**
     * GUI -> guiIsEnabled
     */
    private boolean u;

    public DedicatedServer(joptsimple.OptionSet options, DataConverterManager dataconvertermanager, YggdrasilAuthenticationService yggdrasilauthenticationservice, MinecraftSessionService minecraftsessionservice, GameProfileRepository gameprofilerepository, UserCache usercache) {
        super(options, Proxy.NO_PROXY, dataconvertermanager, yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, usercache);

        instance = this;

        /**
         * NORMAL FIELDS
         */
        serverCommandQueue = reactor.getServerCommandQueue();
        remoteControlCommandListener = reactor.getRemoteControlCommandListener();
        propertyManager = reactor.getPropertyManager();
        generateStructures = reactor.isGenerateStructures();

        /**
         * OBFUSCATED FIELDS
         */
        n = reactor.getRemoteQueryListener();
        p = reactor.getRemoteControlListener();
        r = reactor.getEula();
        t = reactor.getGameMode();
        u = reactor.isGuiIsEnabled();
    }

    @Override
    public boolean init() throws IOException {
        return reactor.init();
    }

    public String aL() {
        return reactor.loadResourcePackSHA();
    }

    @Override
    public void setGamemode(EnumGamemode enumgamemode) {
        reactor.setGamemodeGlobal(enumgamemode);
    }

    @Override
    public boolean getGenerateStructures() {
        return reactor.isGenerateStructures();
    }

    @Override
    public EnumGamemode getGamemode() {
        return reactor.getGameMode();
    }

    @Override
    public EnumDifficulty getDifficulty() {
        return reactor.getDifficulty();
    }

    @Override
    public boolean isHardcore() {
        return reactor.isHardcore();
    }

    @Override
    public CrashReport b(CrashReport crashreport) {
        return reactor.addServerInfoToCrashReportDedicated(crashreport);
    }

    @Override
    public void B() {
        reactor.systemExitNow();
    }

    @Override
    public void D() {
        reactor.updateLogicsPhysicsExecuteCommands();
    }

    @Override
    public boolean getAllowNether() {
        return reactor.getAllowNether();
    }

    @Override
    public boolean getSpawnMonsters() {
        return reactor.getSpawnMonsters();
    }

    // Snooper
    @Override
    public void a(MojangStatisticsGenerator mojangstatisticsgenerator) {}

    @Override
    public boolean getSnooperEnabled() {
        return reactor.getSnooperEnabled();
    }

    public void issueCommand(String s, ICommandListener icommandlistener) {
        reactor.issueCommand(s, icommandlistener);
    }

    public void aM() {
        reactor.executePendingCommands();
    }

    @Override
    public boolean aa() {
        return true; // isDedicatedServer
    }

    @Override
    public boolean af() {
        return reactor.shouldUseNativeTransport();
    }

    public DedicatedPlayerList aN() {
        return reactor.getDedicatedPlayerList();
    }

    @Override
    public int a(String s, int i) {
        return reactor.getIntProperty(s, i);
    }

    @Override
    public String a(String s, String s1) {
        return reactor.getStringProperty(s, s1);
    }

    public boolean a(String s, boolean flag) {
        return reactor.getBooleanProperty(s, flag);
    }

    @Override
    public void a(String s, Object object) {
        reactor.setProperty(s, object);
    }

    @Override
    public void a() {
        reactor.saveProperties();
    }

    @Override
    public String b() {
        return reactor.getSettingsPath();
    }

    @Override
    public String d_() {
        return reactor.getServerIp();
    }

    @Override
    public int e_() {
        return reactor.getServerPort();
    }

    @Override
    public String f_() {
        return reactor.getMotd();
    }

    public void aO() {
        reactor.setGuiEnabled();
    }

    @Override
    public boolean ap() {
        return reactor.isGuiIsEnabled();
    }

    @Override
    public String a(EnumGamemode enumgamemode, boolean flag) {
        return ""; // shareToLAN
    }

    @Override
    public boolean getEnableCommandBlock() {
        return reactor.getEnableCommandBlock();
    }

    @Override
    public int getSpawnProtection() {
        return reactor.getSpawnProtectionSize();
    }

    @Override
    public boolean a(World world, BlockPosition blockposition, EntityHuman entityhuman) {
        return reactor.isBlockProtected(world, blockposition, entityhuman);
    }

    @Override
    public int q() {
        return reactor.getOpPermissionLevel();
    }

    @Override
    public void setIdleTimeout(int i) {
        reactor.setIdleTimeout(i);
    }

    @Override
    public boolean r() {
        return reactor.shouldBroadcastRconToOps();
    }

    @Override
    public boolean s() {
        return reactor.shouldBroadcastConsoleToOps();
    }

    @Override
    public boolean ay() {
        return reactor.isAnnouncingPlayerAchievements();
    }

    @Override
    public int aE() {
        return reactor.getMaxWorldSize();
    }

    @Override
    public int aG() {
        return reactor.getNetworkCompressionThreshold();
    }

    protected boolean aP() {
        return reactor.convertFilesUUID();
    }

    private void aS() {
        reactor.sleepFiveSeconds();
    }

    public long aQ() {
        return reactor.getMaxTickTime();
    }

    @Override
    public String getPlugins() {
        return reactor.getPluginsRcon();
    }

    @Override
    public String executeRemoteCommand(final String s) {
        return reactor.executeRemoteCommand(s);
    }

    @Override
    public PlayerList getPlayerList() {
        return reactor.getDedicatedPlayerList();
    }

    @Override
    public PropertyManager getPropertyManager() {
        return reactor.getPropertyManager();
    }

    public static DedicatedServer getServer() {
        return instance;
    }
}
