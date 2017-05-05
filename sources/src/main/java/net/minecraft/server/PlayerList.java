package net.minecraft.server;

import com.mojang.authlib.GameProfile;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.torch.api.Anaphase;
import org.torch.server.TorchPlayerList;
import org.torch.server.TorchServer;
// CraftBukkit end
import lombok.Getter;
import lombok.Setter;

public abstract class PlayerList implements org.torch.api.TorchServant {
    /**
     * Torch PlayerList reactor
     */
    @Getter private TorchPlayerList reactor;

    /**
     * STATIC FIELDS
     */
    private static final Logger f = TorchServer.logger;
    public static final File a = TorchPlayerList.PLAYER_BANS_FILE;
    public static final File b = TorchPlayerList.IP_BANS_FILE;
    public static final File c = TorchPlayerList.OPS_FILE;
    public static final File d = TorchPlayerList.WHITELIST_FILE;
    private static final SimpleDateFormat g = TorchPlayerList.DATE_FORMAT;

    /**
     * ANAPHASE FIELDS
     */
    /**
     * Team name used for Paper collide rule
     */
    @Nullable @Getter @Setter
    @Anaphase String collideRuleTeamName;

    /**
     * NORMAL FIELDS
     */
    private final MinecraftServer server;
    public final List<EntityPlayer> players;
    private final OpList operators;
    private final WhiteList whitelist;
    public IPlayerFileData playerFileData;
    private boolean hasWhitelist;
    protected int maxPlayers;

    private CraftServer cserver;
    private final Map<String,EntityPlayer> playersByName;

    /**
     * OBFUSCATED FIELDS
     */
    /** uuidToPlayerMap */
    private final Map<UUID, EntityPlayer> j;
    /** bannedPlayers */
    private final GameProfileBanList k;
    /** bannedIPs */
    private final IpBanList l;
    /** playerStatFiles */
    private final Map<UUID, ServerStatisticManager> o;
    /** viewDistance */
    private int r;
    /** gameMode */
    private EnumGamemode s;
    /** allowedCommands */
    private boolean t;
    /** playerPingIndex */
    private int u; public void setPlayerPingIndex(int index) { u = index; } // Setter for port

    public PlayerList(MinecraftServer minecraftserver) {
        // Setup instance for org.torch.api.TorchServant
        reactor = new TorchPlayerList(minecraftserver, this);
        server = minecraftserver;

        /**
         * NORMAL FIELDS
         */
        players = reactor.getPlayers();
        operators = reactor.getOperators();
        whitelist = reactor.getWhitelist();
        // playerFileData = reactor.getPlayerFileData(); // Moved to TorchPlayerList
        hasWhitelist = reactor.isWhitelistMode();
        maxPlayers = reactor.getMaxPlayers();
        cserver = reactor.getCraftServer();
        playersByName = reactor.getPlayersByName();

        /**
         * OBFUSCATED FIELDS
         */
        j = reactor.getUuidToPlayerMap();
        k = reactor.getBannedPlayers();
        l = reactor.getBannedIPs();
        o = reactor.getPlayerStatFiles();
        r = reactor.getViewDistance();
        s = reactor.getGameMode();
        t = reactor.isAllowedCommands();
        u = reactor.getPlayerPingIndex();
    }

    public void a(NetworkManager networkmanager, EntityPlayer entityplayer) {
        reactor.initializeConnectionToPlayer(networkmanager, entityplayer);
    }

    public void sendScoreboard(ScoreboardServer scoreboardserver, EntityPlayer entityplayer) {
        reactor.sendScoreboard(scoreboardserver, entityplayer);
    }

    public void setPlayerFileData(WorldServer[] aworldserver) {
        reactor.setPlayerFileData(aworldserver);
    }

    public void a(EntityPlayer entityplayer, WorldServer worldserver) {
        reactor.preparePlayer(entityplayer, worldserver);
    }

    public int d() {
        return reactor.getEntityViewDistance();
    }

    @Nullable
    public NBTTagCompound a(EntityPlayer entityplayer) {
        return reactor.readPlayerDataFromFile(entityplayer);
    }

    protected void savePlayerFile(EntityPlayer entityplayer) {
        reactor.savePlayerFile(entityplayer);
    }

    public void onPlayerJoin(EntityPlayer entityplayer, String joinMessage) { // CraftBukkit added param
        reactor.onPlayerJoin(entityplayer, joinMessage);
    }

    public void d(EntityPlayer entityplayer) {
        reactor.updateMovingPlayer(entityplayer);
    }

    public String disconnect(EntityPlayer entityplayer) { // CraftBukkit - return string
        return reactor.disconnect(entityplayer);
    }

    // CraftBukkit start - Whole method, SocketAddress to LoginListener, added hostname to signature, return EntityPlayer
    public EntityPlayer attemptLogin(LoginListener loginlistener, GameProfile gameprofile, String hostname) {
        return reactor.attemptLogin(loginlistener, gameprofile, hostname);
    }

    public EntityPlayer processLogin(GameProfile gameprofile, EntityPlayer player) { // CraftBukkit - added EntityPlayer
        /* CraftBukkit startMoved up
        UUID uuid = EntityHuman.a(gameprofile);
        ArrayList arraylist = Lists.newArrayList();

        for (int i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer) this.players.get(i);

            if (entityplayer.getUniqueID().equals(uuid)) {
                arraylist.add(entityplayer);
            }
        }

        EntityPlayer entityplayer1 = (EntityPlayer) this.j.get(gameprofile.getId());

        if (entityplayer1 != null && !arraylist.contains(entityplayer1)) {
            arraylist.add(entityplayer1);
        }

        Iterator iterator = arraylist.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer2 = (EntityPlayer) iterator.next();

            entityplayer2.playerConnection.disconnect("You logged in from another location");
        }

        Object object;

        if (this.server.V()) {
            object = new DemoPlayerInteractManager(this.server.getWorldServer(0));
        } else {
            object = new PlayerInteractManager(this.server.getWorldServer(0));
        }

        return new EntityPlayer(this.server, this.server.getWorldServer(0), gameprofile, (PlayerInteractManager) object);
         */
        return reactor.processLogin(gameprofile, player);
        // CraftBukkit end
    }

    // CraftBukkit start
    public EntityPlayer moveToWorld(EntityPlayer entityplayer, int i, boolean flag) {
        return reactor.recreatePlayerEntity(entityplayer, i, flag);
    }

    public EntityPlayer moveToWorld(EntityPlayer entityplayer, int i, boolean flag, Location location, boolean avoidSuffocation) {
        return reactor.recreatePlayerEntity(entityplayer, i, flag, location, avoidSuffocation);
    }

    // CraftBukkit start - Replaced the standard handling of portals with a more customised method.
    public void changeDimension(EntityPlayer entityplayer, int i, TeleportCause cause) {
        reactor.changeDimension(entityplayer, i, cause);
    }

    public void f(EntityPlayer entityplayer) {
        reactor.updatePermissionLevel(entityplayer);
    }

    public void a(EntityPlayer entityplayer, int i) {
        reactor.transferPlayerToDimension(entityplayer, i);
    }

    public void changeWorld(Entity entity, int i, WorldServer worldserver, WorldServer worldserver1) {
        reactor.changeWorld(entity, i, worldserver, worldserver1);
    }

    // Copy of original changeWorld(Entity, int, WorldServer, WorldServer) method with only location calculation logic
    public Location calculateTarget(Location enter, World target) {
        return reactor.calculateTarget(enter, target);
    }

    // copy of original a(Entity, int, WorldServer, WorldServer) method with only entity repositioning logic
    public void repositionEntity(Entity entity, Location exit, boolean portal) {
        reactor.repositionEntity(entity, exit, portal);
    }

    public void tick() {
        reactor.tick();
    }

    public void sendAll(Packet<?> packet) {
        reactor.sendAll(packet);
    }

    // CraftBukkit start - add a world/entity limited version
    public void sendAll(Packet packet, EntityHuman entityhuman) {
        reactor.sendAll(packet, entityhuman);
    }

    public void sendAll(Packet packet, World world) {
        reactor.sendAll(packet, world);
    }
    // CraftBukkit end

    public void a(Packet<?> packet, int i) {
        reactor.sendAll(packet, i);
    }

    public void a(EntityHuman entityhuman, IChatBaseComponent ichatbasecomponent) {
        reactor.sendMessageToAllTeamMembers(entityhuman, ichatbasecomponent);
    }

    public void b(EntityHuman entityhuman, IChatBaseComponent ichatbasecomponent) {
        reactor.sendMessageToAllTeamOrAllPlayers(entityhuman, ichatbasecomponent);
    }

    public String b(boolean flag) {
        return reactor.getFormattedListOfPlayers(flag);
    }

    public String[] f() {
        return reactor.getOnlinePlayerNames();
    }

    public GameProfile[] g() {
        return reactor.getOnlinePlayerProfiles();
    }

    public GameProfileBanList getProfileBans() {
        return reactor.getBannedPlayers();
    }

    public IpBanList getIPBans() {
        return reactor.getBannedIPs();
    }

    public void addOp(GameProfile gameprofile) {
        reactor.addOp(gameprofile);
    }

    public void removeOp(GameProfile gameprofile) {
        reactor.removeOp(gameprofile);
    }

    private void b(EntityPlayer entityplayer, int i) {
        reactor.sendPlayerPermissionLevel(entityplayer, i);
    }

    public boolean isWhitelisted(GameProfile gameprofile) {
        return reactor.isWhitelisted(gameprofile);
    }

    public boolean isOp(GameProfile gameprofile) {
        return reactor.isOp(gameprofile);
    }

    @Nullable
    public EntityPlayer getPlayer(String s) {
        return reactor.getPlayer(s);
    }

    public void sendPacketNearby(@Nullable EntityHuman entityhuman, double d0, double d1, double d2, double d3, int i, Packet<?> packet) {
        reactor.sendPacketNearby(entityhuman, d0, d1, d2, d3, i, packet);
    }

    // Paper start
    public void savePlayers() {
        reactor.savePlayers();
    }

    public void savePlayers(Integer interval) {
        reactor.savePlayers(interval);
    }
    // Paper end

    public void addWhitelist(GameProfile gameprofile) {
        reactor.addWhitelist(gameprofile);
    }

    public void removeWhitelist(GameProfile gameprofile) {
        reactor.removeWhitelist(gameprofile);
    }

    public WhiteList getWhitelist() {
        return reactor.getWhitelist();
    }

    public String[] getWhitelisted() {
        return reactor.getWhitelistedPlayerNames();
    }

    public OpList getOPs() {
        return reactor.getOperators();
    }

    public String[] n() {
        return reactor.getOppedPlayerNames();
    }

    public void reloadWhitelist() {
        reactor.readWhiteList();
    }

    public void b(EntityPlayer entityplayer, WorldServer worldserver) {
        reactor.updateTimeAndWeatherForPlayer(entityplayer, worldserver);
    }

    public void updateClient(EntityPlayer entityplayer) {
        reactor.syncPlayerInventoryHealth(entityplayer);
    }

    public int getPlayerCount() {
        return reactor.getPlayerCount();
    }

    public int getMaxPlayers() {
        return reactor.getMaxPlayers();
    }

    public String[] getSeenPlayers() {
        return reactor.getSeenPlayers();
    }

    public boolean getHasWhitelist() {
        return reactor.isWhitelistMode();
    }

    public void setHasWhitelist(boolean flag) {
        reactor.setWhitelistMode(flag);
    }

    public List<EntityPlayer> b(String s) {
        return reactor.matchingPlayersByAddress(s);
    }

    public int s() {
        return reactor.getViewDistance();
    }

    public MinecraftServer getServer() {
        return reactor.getMinecraftServer();
    }

    public NBTTagCompound t() {
        return reactor.getHostPlayerData();
    }

    private void a(EntityPlayer entityplayer, EntityPlayer entityplayer1, World world) {
        reactor.setPlayerGamemodeBasedOnOther(entityplayer, entityplayer1, world);
    }

    public void u() {
        reactor.disconnectAllPlayers();
    }

    // CraftBukkit start
    public void sendMessage(IChatBaseComponent[] iChatBaseComponents) {
        reactor.sendMessage(iChatBaseComponents);
    }
    // CraftBukkit end

    public void sendMessage(IChatBaseComponent ichatbasecomponent, boolean flag) {
        reactor.sendMessage(ichatbasecomponent, flag);
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        reactor.sendMessage(ichatbasecomponent);
    }

    public ServerStatisticManager a(EntityHuman entityhuman) {
        return reactor.getOrCreatePlayerStatsFile(entityhuman);
    }

    public void a(int i) {
        reactor.setViewDistance(i);
    }

    public List<EntityPlayer> v() {
        return reactor.players;
    }

    public EntityPlayer a(UUID uuid) {
        return reactor.getPlayerByUUID(uuid);
    }

    public boolean f(GameProfile gameprofile) {
        return reactor.bypassesPlayerLimit(gameprofile);
    }
}
