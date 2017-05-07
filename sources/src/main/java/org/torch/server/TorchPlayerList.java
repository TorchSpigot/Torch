package org.torch.server;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.koloboke.collect.map.hash.HashObjObjMaps;
import com.koloboke.collect.set.hash.HashObjSets;
import com.mojang.authlib.GameProfile;

import co.aikar.timings.MinecraftTimings;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.chunkio.ChunkIOExecutor;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import org.torch.api.Async;
import org.torch.api.TorchReactor;

import net.minecraft.server.*;

import static org.torch.server.TorchServer.logger;

@Getter
public final class TorchPlayerList implements TorchReactor {
    /**
     * Legacy player list instance
     */
    private final PlayerList servant;
    /**
     * Reference to the MinecraftServer object
     */
    private final MinecraftServer minecraftServer;
    /**
     * Reference to the TorchServer object
     */
    private final TorchServer server;

    /**
     * The banned players json file
     */
    public static final File PLAYER_BANS_FILE = new File("banned-players.json");
    /**
     * The banned ips json file
     */
    public static final File IP_BANS_FILE = new File("banned-ips.json");
    /**
     * The ops json file
     */
    public static final File OPS_FILE = new File("ops.json");
    /**
     * The whitelist json file
     */
    public static final File WHITELIST_FILE = new File("whitelist.json");
    /**
     * The date format
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd \'at\' HH:mm:ss z");

    /**
     * A list of player entities that exist on this server
     */
    public final List<EntityPlayer> players = Lists.newCopyOnWriteArrayList();
    /**
     * A set containing the OPs
     */
    private final OpList operators;
    /**
     * The Set of all whitelisted players
     */
    private final WhiteList whitelist;
    /**
     * A map containing the player file datas
     */
    public IPlayerFileData playerFileData;
    /**
     * If the server setting to white-list mode
     */
    private boolean isWhitelistMode;
    /**
     * The max number of players that can be connected at a time
     */
    @Setter protected int maxPlayers;
    /**
     * A map containing the key-value pairs for player UUID and its EntityPlayer object
     */
    private final Map<UUID, EntityPlayer> uuidToPlayerMap = HashObjObjMaps.newMutableMap();
    /**
     * The banned players list
     */
    private final GameProfileBanList bannedPlayers;
    /**
     * The banned IPs list
     */
    private final IpBanList bannedIPs;
    /**
     * A map containing the key-value pairs for player UUID and its stats
     */
    private final Map<UUID, ServerStatisticManager> playerStatFiles;
    /**
     * The server view distance
     */
    private int viewDistance;
    /**
     * The server game mode
     */
    private EnumGamemode gameMode;
    /**
     * True if all players are allowed to use commands (cheats in single player occasion)
     */
    private boolean allowedCommands;
    /**
     * Index into playerEntities of player to ping, updated every tick; currently hardcoded to max at 200 players
     */
    private int playerPingIndex;
    /**
     * Craft server instance
     */
    private CraftServer craftServer;
    /**
     * A map containing the key-value pairs for player name and its EntityPlayer object, the name is case insensitive
     */
    private final Map<String, EntityPlayer> playersByName = new org.spigotmc.CaseInsensitiveMap<>();

    public TorchPlayerList(MinecraftServer server, PlayerList legacy) {
        // Setup instance for org.torch.api.TorchReactor
        servant = legacy;
        this.server = TorchServer.getServer();

        TorchServer.getServer().craftServer = server.server = craftServer = new CraftServer(this.server, this);
        TorchServer.getServer().console = server.console = org.bukkit.craftbukkit.command.ColouredConsoleSender.getInstance();
        TorchServer.getServer().reader.addCompleter(new org.bukkit.craftbukkit.command.ConsoleCommandCompleter(server.server));
        // Port the reader for compatibility
        server.reader = TorchServer.getServer().reader;
        minecraftServer = server;

        bannedPlayers = new GameProfileBanList(PLAYER_BANS_FILE);
        bannedIPs = new IpBanList(IP_BANS_FILE);
        operators = new OpList(OPS_FILE);
        whitelist = new WhiteList(WHITELIST_FILE);
        playerStatFiles = HashObjObjMaps.newMutableMap();

        // Sets whether we are a LAN server
        bannedPlayers.a(false);
        bannedIPs.a(false);
        // maxPlayers = 9; // baka

        /**
         * Dedicated PlayerList
         */
        this.setViewDistance(this.server.getIntProperty("view-distance", 10));
        this.setMaxPlayers(this.server.getIntProperty("max-players", 20));
        this.setWhitelistMode(this.server.getBooleanProperty("white-list", false));

        this.refreshJsonLists();
    }

    /**
     * Load banned players, whitelist and Ops
     */
    @SuppressWarnings("deprecation")
    public void refreshJsonLists() {
        if (!this.server.isSinglePlayer()) {
            this.bannedPlayers.a(true);
            this.bannedIPs.a(true);
        }

        this.loadPlayerBanList();
        this.savePlayerBanList();
        this.loadIPBanList();
        this.saveIPBanList();
        this.loadIPBanList();
        this.readWhiteList();
        this.loadOpsList();
        this.saveOpsList();

        // Save the whitelist file if doesn exist
        if (!this.getWhitelist().c().exists()) {
            this.saveWhiteList();
        }
    }

    public void sendScoreboard(ScoreboardServer scoreboard, EntityPlayer player) {
        Set<ScoreboardObjective> set = HashObjSets.newUpdatableSet(19);

        for (ScoreboardTeam team : scoreboard.getTeams()) {
            player.playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
        }

        for (int i = 0; i < 19; ++i) {
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(i);

            if (objective != null && !set.contains(objective)) {
                List<Packet<?>> list = scoreboard.getScoreboardScorePacketsForObjective(objective);

                for (Packet<?> packet : list) {
                    player.playerConnection.sendPacket(packet);
                }

                set.add(objective);
            }
        }
    }

    public void preparePlayer(EntityPlayer player, WorldServer world) {
        if (world != null) {
            world.getPlayerChunkMap().removePlayer(player);
        } else {
            world = player.x(); // PAIL: x() -> Get the player world
            if (world != null) world.getPlayerChunkMap().removePlayer(player);
        }

        world.getPlayerChunkMap().addPlayer(player);
        world.getChunkProviderServer().getChunkAt((int) player.locX >> 4, (int) player.locZ >> 4);
    }

    /**
     * Returns the furthest viewable block in current view distance
     */
    public int getEntityViewDistance() {
        return PlayerChunkMap.getFurthestViewableBlock(getViewDistance());
    }

    /**
     * called during player login. reads the player info from disk
     */
    @Nullable
    public NBTTagCompound readPlayerDataFromFile(EntityPlayer player) {
        // Get raw player NBT from the primary world data
        NBTTagCompound nbttagcompoundRaw = this.server.worlds.get(0).getWorldData().h();
        NBTTagCompound nbttagcompound;

        if (player.getName().equals(this.server.getServerOwner()) && nbttagcompoundRaw != null) {
            // Process the raw NBT data
            nbttagcompound = this.server.getDataConverterManager().a(DataConverterTypes.PLAYER, nbttagcompoundRaw);
            player.f(nbttagcompound); // PAIL: f() -> Read player from NBT
            logger.debug("loading single player");
        } else {
            nbttagcompound = this.playerFileData.load(player);
        }

        return nbttagcompound;
    }

    /**
     * Write player data and stat, also stores the NBTTags if this is an intergratedPlayerList
     */
    public void savePlayerFile(EntityPlayer player) {
        player.lastSave = MinecraftServer.currentTick;
        this.playerFileData.save(player);
        ServerStatisticManager statisticManager = this.playerStatFiles.get(player.getUniqueID());

        if (statisticManager != null) {
            statisticManager.b(); // PAIL: b() -> Save the stat file
        }
    }

    /**
     * Using player's dimension, update the chunks around them
     */
    public void updateMovingPlayer(EntityPlayer player) {
        // getWorldServer -> getPlayerChunkMap -> movePlayer
        player.x().getPlayerChunkMap().movePlayer(player);
    }

    /**
     * Also checks for multiple logins across servers, **does nothing cause Bukkit moved up this
     */
    @Deprecated
    public EntityPlayer processLogin(GameProfile gameprofile, EntityPlayer player) {
        return player;
    }

    @SuppressWarnings("deprecation")
    public void updatePermissionLevel(EntityPlayer player) {
        GameProfile gameprofile = player.getProfile();
        // PAIL: Return the permission level if the player is Opped
        int permLevel = this.isOp(gameprofile) ? this.operators.getPermissionLevel(gameprofile) : 0;

        // PAIL: If the world is commands allowed, return 4
        permLevel = this.server.isSinglePlayer() && this.minecraftServer.worldServer[0].getWorldData().u() ? 4 : permLevel;
        // PAIL: If the server is commands allowed for all (chest mode), return 4
        permLevel = this.allowedCommands ? 4 : permLevel;
        this.sendPlayerPermissionLevel(player, permLevel);
    }

    public void sendPlayerPermissionLevel(EntityPlayer player, int permLevel) {
        if (player != null && player.playerConnection != null) {
            byte data = permLevel <= 0 ? 24 : permLevel >= 4 ? 28 : (byte) (24 + permLevel);
            player.playerConnection.sendPacket(new PacketPlayOutEntityStatus(player, data));
        }
    }

    public boolean isWhitelisted(GameProfile profile) {
        // whitelistIsDisabled || isOP || isWhitelisted
        return !this.isWhitelistMode || this.operators.contains(profile) || this.whitelist.isWhitelisted(profile);
    }

    public boolean isOp(GameProfile gameprofile) {
        // isOP || (isSinglePlayer && isCommandAllowed && isServerOwner) || isChestMode 
        return this.operators.contains(gameprofile) /*|| this.server.isSinglePlayer() && this.server.worlds.get(0).getWorldData().u() && this.server.getServerOwner().equalsIgnoreCase(gameprofile.getName())*/ || this.allowedCommands;
    }

    public void addOp(GameProfile gameprofile) {
        int permLevel = this.server.getOpPermissionLevel();
        // factor3 = bypassesPlayerLimit?
        this.operators.add(new OpListEntry(gameprofile, this.server.getOpPermissionLevel(), this.operators.b(gameprofile)));
        this.sendPlayerPermissionLevel(this.getPlayerByUUID(gameprofile.getId()), permLevel);
        
        // Handle Bukkit permissions
        Player player = server.craftServer.getPlayer(gameprofile.getId());
        if (player != null) player.recalculatePermissions();
        
        // this.saveOpsList(); - Already saved in operators.add()
    }

    public void removeOp(GameProfile gameprofile) {
        this.operators.remove(gameprofile);
        this.sendPlayerPermissionLevel(this.getPlayerByUUID(gameprofile.getId()), 0);

        // Handle Bukkit permissions
        Player player = server.craftServer.getPlayer(gameprofile.getId());
        if (player != null) player.recalculatePermissions();

        // this.saveOpsList(); - Already saved in operators.remove()
    }

    public void initializeConnectionToPlayer(NetworkManager networkmanager, EntityPlayer entityplayer) {
        GameProfile profile = entityplayer.getProfile();
        TorchUserCache usercache = this.server.getUserCache();
        GameProfile cachedProfile = usercache.peekCachedProfile(profile.getName());
        String username = cachedProfile == null ? profile.getName() : cachedProfile.getName();

        usercache.offerCache(profile);
        
        NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(entityplayer);
        // Better rename detection
        if (nbttagcompound != null && nbttagcompound.hasKey("bukkit")) {
            NBTTagCompound bukkit = nbttagcompound.getCompound("bukkit");
            username = bukkit.hasKeyOfType("lastKnownName", 8) ? bukkit.getString("lastKnownName") : username;
        }
        
        // Support PlayerInitialSpawnEvent
        Location originalLoc = new Location(entityplayer.world.getWorld(), entityplayer.locX, entityplayer.locY, entityplayer.locZ, entityplayer.yaw, entityplayer.pitch);
        com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent event = new com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent(entityplayer.getBukkitEntity(), originalLoc);
        this.server.craftServer.getPluginManager().callEvent(event);

        Location newLoc = event.getSpawnLocation();
        entityplayer.world = ((CraftWorld) newLoc.getWorld()).getHandle();
        entityplayer.locX = newLoc.getX();
        entityplayer.locY = newLoc.getY();
        entityplayer.locZ = newLoc.getZ();
        entityplayer.yaw = newLoc.getYaw();
        entityplayer.pitch = newLoc.getPitch();
        entityplayer.dimension = ((CraftWorld) newLoc.getWorld()).getHandle().dimension;

        entityplayer.spawnIn(this.server.getWorldServer(entityplayer.dimension));
        entityplayer.playerInteractManager.a((WorldServer) entityplayer.world);

        String socketAddress = "local";
        if (networkmanager.getSocketAddress() != null) {
            socketAddress = networkmanager.getSocketAddress().toString();
        }

        // Spawn location event
        Player bukkitPlayer = entityplayer.getBukkitEntity();
        PlayerSpawnLocationEvent ev = new PlayerSpawnLocationEvent(bukkitPlayer, bukkitPlayer.getLocation());
        Bukkit.getPluginManager().callEvent(ev);

        Location loc = ev.getSpawnLocation();
        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();

        entityplayer.spawnIn(world);
        entityplayer.setPosition(loc.getX(), loc.getY(), loc.getZ());
        entityplayer.setYawPitch(loc.getYaw(), loc.getPitch()); 

        WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);
        WorldData worlddata = worldserver.getWorldData();

        this.setPlayerGamemodeBasedOnOther(entityplayer, (EntityPlayer) null, worldserver);
        PlayerConnection playerconnection = new PlayerConnection(this.minecraftServer, networkmanager, entityplayer);

        playerconnection.sendPacket(new PacketPlayOutLogin(entityplayer.getId(), entityplayer.playerInteractManager.getGameMode(), worlddata.isHardcore(), worldserver.worldProvider.getDimensionManager().getDimensionID(), worldserver.getDifficulty(), this.getMaxPlayers(), worlddata.getType(), worldserver.getGameRules().getBoolean("reducedDebugInfo")));
        entityplayer.getBukkitEntity().sendSupportedChannels();
        playerconnection.sendPacket(new PacketPlayOutCustomPayload("MC|Brand", (new PacketDataSerializer(Unpooled.buffer())).a(this.getServer().getServerModName())));
        playerconnection.sendPacket(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        playerconnection.sendPacket(new PacketPlayOutAbilities(entityplayer.abilities));
        playerconnection.sendPacket(new PacketPlayOutHeldItemSlot(entityplayer.inventory.itemInHandIndex));
        playerconnection.sendPacket(new PacketPlayOutEntityStatus(entityplayer, (byte) (worldserver.getGameRules().getBoolean("reducedDebugInfo") ? 22 : 23))); // Paper - fix this rule not being initialized on the client
        this.updatePermissionLevel(entityplayer);

        // Update player stats
        entityplayer.getStatisticManager().d(); // mark all dirty
        entityplayer.getStatisticManager().updateStatistics(entityplayer);

        this.sendScoreboard((ScoreboardServer) worldserver.getScoreboard(), entityplayer);
        this.server.refreshStatusNextTick();

        // Login message is handled in the event
        String joinMessage;
        if (entityplayer.getName().equalsIgnoreCase(username)) {
            joinMessage = "\u00A7e" + LocaleI18n.a("multiplayer.player.joined", entityplayer.getName());
        } else {
            joinMessage = "\u00A7e" + LocaleI18n.a("multiplayer.player.joined.renamed", entityplayer.getName(), username);
        }

        this.onPlayerJoin(entityplayer, joinMessage);

        // Update in case join event changed it
        worldserver = server.getWorldServer(entityplayer.dimension);
        // Set player location
        playerconnection.a(entityplayer.locX, entityplayer.locY, entityplayer.locZ, entityplayer.yaw, entityplayer.pitch);
        this.updateTimeAndWeatherForPlayer(entityplayer, worldserver);

        if (!this.server.getResourcePackUrl().isEmpty()) {
            entityplayer.setResourcePack(this.server.getResourcePackUrl(), this.server.getResourcePackHash());
        }

        Regulator.post(() -> {
            for (MobEffect effect : entityplayer.getEffects()) {
                if (!playerconnection.isDisconnected()) playerconnection.sendPacket(new PacketPlayOutEntityEffect(entityplayer.getId(), effect));
            }
        });

        if (nbttagcompound != null && nbttagcompound.hasKeyOfType("RootVehicle", 10)) {
            NBTTagCompound rootVehicle = nbttagcompound.getCompound("RootVehicle");
            // Read vehicle entity
            Entity entity = ChunkRegionLoader.a(rootVehicle.getCompound("Entity"), worldserver, true);

            if (entity != null) { // TODO: need go deeper
                // Read UUID from 'Attach' compound
                UUID uuid = rootVehicle.a("Attach");

                if (entity.getUniqueID().equals(uuid)) {
                    // Start riding
                    entityplayer.a(entity, true);
                } else {
                    for (Entity passager : entity.by()) {
                        if (passager.getUniqueID().equals(uuid)) {
                            // Start riding
                            entityplayer.a(passager, true);
                            break;
                        }
                    }
                }

                if (!entityplayer.isPassenger()) {
                    logger.warn("Couldn\'t reattach entity to player");
                    worldserver.removeEntity(entity);
                    // Remove all passagers
                    for (Entity passager : entity.by()) {
                        worldserver.removeEntity(passager);
                    }
                }
            }
        }

        entityplayer.syncInventory();
        // Add to collideRule team if needed
        final Scoreboard scoreboard = this.getServer().getWorld().getScoreboard();
        if (servant.getCollideRuleTeamName() != null && scoreboard.getTeam(servant.getCollideRuleTeamName()) != null && entityplayer.getTeam() == null) {
            scoreboard.addPlayerToTeam(entityplayer.getName(), servant.getCollideRuleTeamName());
        }

        logger.info(entityplayer.getName() + "[" + socketAddress + "] logged in with entity id " + entityplayer.getId() + " at ([" + entityplayer.world.worldData.getName() + "]" + entityplayer.locX + ", " + entityplayer.locY + ", " + entityplayer.locZ + ")");
    }

    public void setPlayerFileData(WorldServer[] worlds) {
        if (playerFileData != null) return;

        servant.playerFileData = this.playerFileData = worlds[0].getDataManager().getPlayerFileData();
        worlds[0].getWorldBorder().a(new IWorldBorderListener() {
            @Override
            public void a(WorldBorder worldborder, double d0) {
                sendAll(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE), worldborder.world);
            }

            @Override
            public void a(WorldBorder worldborder, double d0, double d1, long i) {
                sendAll(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.LERP_SIZE), worldborder.world);
            }

            @Override
            public void a(WorldBorder worldborder, double d0, double d1) {
                sendAll(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER), worldborder.world);
            }

            @Override
            public void a(WorldBorder worldborder, int i) {
                sendAll(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_TIME), worldborder.world);
            }

            @Override
            public void b(WorldBorder worldborder, int i) {
                sendAll(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS), worldborder.world);
            }

            @Override
            public void b(WorldBorder worldborder, double d0) {}

            @Override
            public void c(WorldBorder worldborder, double d0) {}
        });
    }

    public void onPlayerJoin(EntityPlayer entityplayer, String joinMessage) {
        this.players.add(entityplayer);
        this.playersByName.put(entityplayer.getName(), entityplayer);
        this.uuidToPlayerMap.put(entityplayer.getUniqueID(), entityplayer);
        WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);

        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(craftServer.getPlayer(entityplayer), joinMessage);
        craftServer.getPluginManager().callEvent(playerJoinEvent);

        joinMessage = playerJoinEvent.getJoinMessage();
        if (joinMessage != null && joinMessage.length() > 0) {
            for (IChatBaseComponent line : org.bukkit.craftbukkit.util.CraftChatMessage.fromString(joinMessage)) {
                server.getPlayerList().sendAll(new PacketPlayOutChat(line));
            }
        }

        ChunkIOExecutor.adjustPoolSize(getPlayerCount());

        // sendAll above replaced with this loop
        Regulator.post(() -> {
            for (EntityPlayer eachPlayer : this.players) {
                if (eachPlayer.getBukkitEntity().canSee(entityplayer.getBukkitEntity())) {
                    eachPlayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityplayer));
                }

                if (entityplayer.getBukkitEntity().canSee(eachPlayer.getBukkitEntity())) {
                    entityplayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, eachPlayer));
                }
            }
        });
        entityplayer.sentListPacket = true;

        // Only add if the player wasn't moved in the event
        if (entityplayer.world == worldserver && !worldserver.players.contains(entityplayer)) {
            worldserver.addEntity(entityplayer);
            this.preparePlayer(entityplayer, (WorldServer) null);
        }
    }

    /**
     * Returns the quit message
     */
    public String disconnect(EntityPlayer player) {
        WorldServer world = player.x(); // PAIL: player.x() -> Get world instance from player
        player.b(StatisticList.f); // PAIL: Add stat for player : LEAVE_GAME

        // Quitting must be before we do final save of data, in case plugins need to modify it
        org.bukkit.craftbukkit.event.CraftEventFactory.handleInventoryCloseEvent(player);

        PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(craftServer.getPlayer(player), "\u00A7e" + player.getName() + " left the game");
        craftServer.getPluginManager().callEvent(playerQuitEvent);
        player.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());

        player.playerTick(); // SPIGOT-924

        // Remove from collideRule team if needed
        if (servant.getCollideRuleTeamName() != null) {
            final Scoreboard scoreBoard = this.server.getWorld().getScoreboard();
            final ScoreboardTeam team = scoreBoard.getTeam(servant.getCollideRuleTeamName());
            if (player.getTeam() == team && team != null) {
                scoreBoard.removePlayerFromTeam(player.getName(), team);
            }
        }

        this.savePlayerFile(player);
        if (player.isPassenger()) {
            Entity vehicle = player.getVehicle();
            // Get passengers by type
            if (vehicle.b(EntityPlayer.class).size() == 1) {
                logger.debug("Removing player mount");
                player.stopRiding();
                world.removeEntity(vehicle);

                // Remove recursive passengers
                for (Entity passenger : vehicle.by()) world.removeEntity(passenger);

                // Mark the chunk as modified
                world.getChunkAt(player.ab, player.ad).e();
            }
        }

        world.kill(player);
        world.getPlayerChunkMap().removePlayer(player);
        this.players.remove(player);
        this.playersByName.remove(player.getName());

        UUID uuid = player.getUniqueID();
        EntityPlayer uuidPlayer = this.uuidToPlayerMap.get(uuid);
        if (uuidPlayer == player) {
            this.uuidToPlayerMap.remove(uuid);
            this.playerStatFiles.remove(uuid);
        }

        // Remove the disconnected player from others
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, player);
        for (EntityPlayer eachPlayer : this.players) {
            if (eachPlayer.getBukkitEntity().canSee(player.getBukkitEntity())) {
                eachPlayer.playerConnection.sendPacket(packet);
            } else {
                eachPlayer.getBukkitEntity().removeDisconnectingPlayer(player.getBukkitEntity());
            }
        }

        // This removes the scoreboard (and player reference) for the specific player in the manager
        craftServer.getScoreboardManager().removePlayer(player.getBukkitEntity());

        ChunkIOExecutor.adjustPoolSize(getPlayerCount());

        return playerQuitEvent.getQuitMessage();
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    // Whole method, SocketAddress to LoginListener, added hostname to signature, return EntityPlayer
    public EntityPlayer attemptLogin(LoginListener loginlistener, GameProfile gameprofile, String hostname) {
        // Get UUID from the profile
        UUID uuid = EntityHuman.a(gameprofile);

        // Kick player if already logged // Torch - slight optimization, TODO: check
        EntityPlayer loggedPlayer = this.uuidToPlayerMap.get(uuid);
        if (loggedPlayer != null) {
            // Force the player's inventory to be saved
            savePlayerFile(loggedPlayer);
            loggedPlayer.playerConnection.disconnect("You logged in from another location");
        }

        // Instead of kicking then returning, we need to store the kick reason
        // in the event, check with plugins to see if it's ok, and THEN kick depending on the outcome
        SocketAddress socketaddress = loginlistener.networkManager.getSocketAddress();

        EntityPlayer entity = new EntityPlayer(minecraftServer, server.getWorldServer(0), gameprofile, new PlayerInteractManager(server.getWorldServer(0)));
        Player player = entity.getBukkitEntity();
        PlayerLoginEvent event = new PlayerLoginEvent(player, hostname, ((java.net.InetSocketAddress) socketaddress).getAddress(), ((java.net.InetSocketAddress) loginlistener.networkManager.getRawAddress()).getAddress());
        String reason;

        // Switch the kick reason
        if (getBannedPlayers().isBanned(gameprofile) && !getBannedPlayers().get(gameprofile).hasExpired()) {
            GameProfileBanEntry gameprofilebanentry = this.bannedPlayers.get(gameprofile);

            reason = "You are banned from this server!\nReason: " + gameprofilebanentry.getReason();
            if (gameprofilebanentry.getExpires() != null) {
                reason = reason + "\nYour ban will be removed on " + DATE_FORMAT.format(gameprofilebanentry.getExpires());
            }

            if (!gameprofilebanentry.hasExpired()) event.disallow(PlayerLoginEvent.Result.KICK_BANNED, reason);
        } else if (!this.isWhitelisted(gameprofile)) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, org.spigotmc.SpigotConfig.whitelistMessage);
        } else if (this.getBannedIPs().isBanned(socketaddress) && !this.getBannedIPs().get(socketaddress).hasExpired()) {
            IpBanEntry ipbanentry = this.bannedIPs.get(socketaddress);

            reason = "Your IP address is banned from this server!\nReason: " + ipbanentry.getReason();
            if (ipbanentry.getExpires() != null) {
                reason = reason + "\nYour ban will be removed on " + DATE_FORMAT.format(ipbanentry.getExpires());
            }

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, reason);
        } else {
            if (this.players.size() >= this.maxPlayers && !this.bypassesPlayerLimit(gameprofile)) {
                event.disallow(PlayerLoginEvent.Result.KICK_FULL, org.spigotmc.SpigotConfig.serverFullMessage);
            }
        }

        craftServer.getPluginManager().callEvent(event);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            loginlistener.disconnect(event.getKickMessage());
            return null;
        }
        return entity;
    }

    /**
     * Called on respawn
     */
    public EntityPlayer recreatePlayerEntity(EntityPlayer oldPlayerEntity, int dimension, boolean conqueredEnd) {
        return this.recreatePlayerEntity(oldPlayerEntity, dimension, conqueredEnd, null, true);
    }

    @SuppressWarnings("deprecation")
    public EntityPlayer recreatePlayerEntity(EntityPlayer oldPlayerEntity, int dimension, boolean conqueredEnd, Location location, boolean avoidSuffocation) {
        oldPlayerEntity.stopRiding();
        // PAIL: x() = getWorldServer()
        oldPlayerEntity.x().getTracker().untrackPlayer(oldPlayerEntity);
        oldPlayerEntity.x().getPlayerChunkMap().removePlayer(oldPlayerEntity);
        this.players.remove(oldPlayerEntity);
        this.playersByName.remove(oldPlayerEntity.getName());
        this.server.getWorldServer(oldPlayerEntity.dimension).removeEntity(oldPlayerEntity);
        BlockPosition bedPosition = oldPlayerEntity.getBed();

        EntityPlayer newPlayerEntity = oldPlayerEntity;
        org.bukkit.World fromWorld = oldPlayerEntity.getBukkitEntity().getWorld();
        oldPlayerEntity.viewingCredits = false;

        // newPlayerEntity.playerConnection = oldPlayerEntity.playerConnection;
        // Clone the player
        newPlayerEntity.copyTo(oldPlayerEntity, conqueredEnd);
        // Set entity ID
        newPlayerEntity.h(oldPlayerEntity.getId());
        // Set command stats
        newPlayerEntity.v(oldPlayerEntity);
        // Set primary hand
        newPlayerEntity.a(oldPlayerEntity.getMainHand());

        // Transfer player tags
        for (String tag : oldPlayerEntity.P()) newPlayerEntity.a(tag);

        // Fire PlayerRespawnEvent
        if (location == null) {
            boolean isBedSpawn = false;
            CraftWorld craftWorld = (CraftWorld) this.server.craftServer.getWorld(oldPlayerEntity.spawnWorld);
            if (craftWorld != null && bedPosition != null) {
                BlockPosition spawnPosition = EntityHuman.getBed(craftWorld.getHandle(), bedPosition, oldPlayerEntity.isRespawnForced());
                if (spawnPosition != null) {
                    isBedSpawn = true;
                    location = new Location(craftWorld, spawnPosition.getX() + 0.5F, spawnPosition.getY() + 0.1F, spawnPosition.getZ() + 0.5F);
                } else {
                    newPlayerEntity.setRespawnPosition(null, true);
                    newPlayerEntity.playerConnection.sendPacket(new PacketPlayOutGameStateChange(0, 0.0F));
                }
            }

            if (location == null) {
                craftWorld = (CraftWorld) this.server.craftServer.getWorlds().get(0);
                bedPosition = craftWorld.getHandle().getSpawn();
                location = new Location(craftWorld, bedPosition.getX() + 0.5F, bedPosition.getY() + 0.1F, bedPosition.getZ() + 0.5F);
            }

            Player respawnPlayer = craftServer.getPlayer(newPlayerEntity);
            PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn);
            craftServer.getPluginManager().callEvent(respawnEvent);

            if (oldPlayerEntity.playerConnection.isDisconnected()) {
                return oldPlayerEntity;
            }

            location = respawnEvent.getRespawnLocation();
            oldPlayerEntity.reset();
        } else {
            location.setWorld(server.getWorldServer(dimension).getWorld());
        }

        WorldServer worldserver = ((CraftWorld) location.getWorld()).getHandle();
        newPlayerEntity.forceSetPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        worldserver.getChunkProviderServer().getChunkAt((int) newPlayerEntity.locX >> 4, (int) newPlayerEntity.locZ >> 4);

        while (avoidSuffocation && !worldserver.getCubes(newPlayerEntity, newPlayerEntity.getBoundingBox()).isEmpty() && newPlayerEntity.locY < 256.0D) {
            newPlayerEntity.setPosition(newPlayerEntity.locX, newPlayerEntity.locY + 1.0D, newPlayerEntity.locZ);
        }

        byte actualDimension = (byte) (worldserver.getWorld().getEnvironment().getId());
        // Force the client to refresh their chunk cache
        if (fromWorld.getEnvironment() == worldserver.getWorld().getEnvironment()) {
            newPlayerEntity.playerConnection.sendPacket(new PacketPlayOutRespawn((byte) (actualDimension >= 0 ? -1 : 0), worldserver.getDifficulty(), worldserver.getWorldData().getType(), oldPlayerEntity.playerInteractManager.getGameMode()));
        }

        newPlayerEntity.playerConnection.sendPacket(new PacketPlayOutRespawn(actualDimension, worldserver.getDifficulty(), worldserver.getWorldData().getType(), newPlayerEntity.playerInteractManager.getGameMode()));
        newPlayerEntity.spawnIn(worldserver);
        newPlayerEntity.dead = false;
        newPlayerEntity.playerConnection.teleport(new Location(worldserver.getWorld(), newPlayerEntity.locX, newPlayerEntity.locY, newPlayerEntity.locZ, newPlayerEntity.yaw, newPlayerEntity.pitch));
        newPlayerEntity.setSneaking(false);

        newPlayerEntity.playerConnection.sendPacket(new PacketPlayOutSpawnPosition(worldserver.getSpawn()));
        newPlayerEntity.playerConnection.sendPacket(new PacketPlayOutExperience(newPlayerEntity.exp, newPlayerEntity.expTotal, newPlayerEntity.expLevel));

        updateTimeAndWeatherForPlayer(newPlayerEntity, worldserver);
        updatePermissionLevel(newPlayerEntity);

        if (!oldPlayerEntity.playerConnection.isDisconnected()) {
            worldserver.getPlayerChunkMap().addPlayer(newPlayerEntity);
            worldserver.addEntity(newPlayerEntity);
            this.players.add(newPlayerEntity);
            this.playersByName.put(newPlayerEntity.getName(), newPlayerEntity);
            this.uuidToPlayerMap.put(newPlayerEntity.getUniqueID(), newPlayerEntity);
        }

        newPlayerEntity.setHealth(newPlayerEntity.getHealth());
        // Update health, etc...
        syncPlayerInventoryHealth(newPlayerEntity); // TODO: check
        newPlayerEntity.updateAbilities(); // TODO: check

        Regulator.post(() -> {
            for (MobEffect effect : oldPlayerEntity.getEffects()) {
                if (newPlayerEntity.playerConnection != null) {
                    if (!newPlayerEntity.playerConnection.isDisconnected()) newPlayerEntity.playerConnection.sendPacket(new PacketPlayOutEntityEffect(newPlayerEntity.getId(), effect));
                }
            }
        });

        // Don't fire on respawn
        if (fromWorld != location.getWorld()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(oldPlayerEntity.getBukkitEntity(), fromWorld);
            server.craftServer.getPluginManager().callEvent(event);
        }

        // Save player file again if they were disconnected
        if (newPlayerEntity.playerConnection.isDisconnected()) { // TODO: check
            this.savePlayerFile(newPlayerEntity);
        }
        return newPlayerEntity;
    }

    // Replaced the standard handling of portals with a more customised method.
    public void changeDimension(EntityPlayer entityplayer, int dimension, TeleportCause cause) {
        WorldServer exitWorld = null;
        // Plugins must specify exit from custom Bukkit worlds
        if (entityplayer.dimension < CraftWorld.CUSTOM_DIMENSION_OFFSET) {
            // Only target existing worlds (compensate for allow-nether/allow-end as false)
            for (WorldServer world : this.server.worlds) {
                if (world.dimension == dimension) exitWorld = world;
            }
        }

        Location enter = entityplayer.getBukkitEntity().getLocation();
        Location exit = null;
        // Don't use agent for custom worlds or return from THE_END
        boolean useTravelAgent = false;
        if (exitWorld != null) {
            if ((cause == TeleportCause.END_PORTAL) && (dimension == 0)) {
                // THE_END -> NORMAL; use bed if available, otherwise default spawn
                exit = entityplayer.getBukkitEntity().getBedSpawnLocation();
                if (exit == null || ((CraftWorld) exit.getWorld()).getHandle().dimension != 0) {
                    exit = exitWorld.getWorld().getSpawnLocation();
                }
            } else {
                // NORMAL <-> NETHER or NORMAL -> THE_END
                exit = this.calculateTarget(enter, exitWorld);
                useTravelAgent = true;
            }
        }

        // Return arbitrary TA to compensate for implementation dependent plugins
        TravelAgent agent = exit != null ? (TravelAgent) ((CraftWorld) exit.getWorld()).getHandle().getTravelAgent() : org.bukkit.craftbukkit.CraftTravelAgent.DEFAULT;
        PlayerPortalEvent event = new PlayerPortalEvent(entityplayer.getBukkitEntity(), enter, exit, agent, cause);
        event.useTravelAgent(useTravelAgent);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled() || event.getTo() == null) return;

        exit = event.useTravelAgent() ? event.getPortalTravelAgent().findOrCreate(event.getTo()) : event.getTo();
        if (exit == null) return;

        exitWorld = ((CraftWorld) exit.getWorld()).getHandle();

        org.bukkit.event.player.PlayerTeleportEvent tpEvent = new org.bukkit.event.player.PlayerTeleportEvent(entityplayer.getBukkitEntity(), enter, exit, cause);
        Bukkit.getServer().getPluginManager().callEvent(tpEvent);
        if (tpEvent.isCancelled() || tpEvent.getTo() == null) return;

        Vector velocity = entityplayer.getBukkitEntity().getVelocity();
        exitWorld.getTravelAgent().adjustExit(entityplayer, exit, velocity);

        // Set teleport invulnerability only if player changing worlds
        entityplayer.worldChangeInvuln = true;
        // Vanilla doesn't check for suffocation when handling portals, so neither should we
        this.recreatePlayerEntity(entityplayer, exitWorld.dimension, true, exit, false);
        if (entityplayer.motX != velocity.getX() || entityplayer.motY != velocity.getY() || entityplayer.motZ != velocity.getZ()) {
            entityplayer.getBukkitEntity().setVelocity(velocity);
        }
    }

    public void transferPlayerToDimension(EntityPlayer player, int targetDimension) {
        int exitDimension = player.dimension;
        WorldServer exitWorld = this.server.getWorldServer(player.dimension);
        // Apply new dimension
        player.dimension = targetDimension;
        WorldServer targetWorld = this.server.getWorldServer(player.dimension);

        player.playerConnection.sendPacket(new PacketPlayOutRespawn(player.dimension, player.world.getDifficulty(), player.world.getWorldData().getType(), player.playerInteractManager.getGameMode()));

        this.updatePermissionLevel(player);
        exitWorld.removeEntity(player);
        player.dead = false;

        this.changeWorld(player, exitDimension, exitWorld, targetWorld);
        this.preparePlayer(player, exitWorld);

        player.playerConnection.a(player.locX, player.locY, player.locZ, player.yaw, player.pitch); // PAIL: Set player location
        player.playerInteractManager.a(targetWorld); // PAIL: Set world
        player.playerConnection.sendPacket(new PacketPlayOutAbilities(player.abilities));

        this.updateTimeAndWeatherForPlayer(player, targetWorld);
        this.syncPlayerInventoryHealth(player);

        Regulator.post(() -> {
            for (MobEffect mobeffect : player.getEffects()) {
                if (player.playerConnection != null) {
                    if (!player.playerConnection.isDisconnected()) player.playerConnection.sendPacket(new PacketPlayOutEntityEffect(player.getId(), mobeffect));
                }
            }
        });
    }

    /**
     * Transfers an entity from a world to another world
     */
    public void changeWorld(Entity entity, int exitDimension, WorldServer exitWorld, WorldServer targetWorld) {
        // Split into modular functions
        Location exit = calculateTarget(entity.getBukkitEntity().getLocation(), targetWorld);
        repositionEntity(entity, exit, true);
    }

    public Location calculateTarget(Location enter, World target) {
        WorldServer enterWorld = ((CraftWorld) enter.getWorld()).getHandle();
        WorldServer targetWorld = target.getWorld().getHandle();

        double y = enter.getY(); double x = enter.getX(); double z = enter.getZ();
        float yaw = enter.getYaw(); float pitch = enter.getPitch();
        double d2 = 8.0D;

        if (targetWorld.dimension == -1) {
            // b() = minX, c() = minZ
            x = MathHelper.a(x / d2, targetWorld.getWorldBorder().b()+ 16.0D, targetWorld.getWorldBorder().d() - 16.0D);
            z = MathHelper.a(z / d2, targetWorld.getWorldBorder().c() + 16.0D, targetWorld.getWorldBorder().e() - 16.0D);
        } else if (targetWorld.dimension == 0) {
            x = MathHelper.a(x * d2, targetWorld.getWorldBorder().b() + 16.0D, targetWorld.getWorldBorder().d() - 16.0D);
            z = MathHelper.a(z * d2, targetWorld.getWorldBorder().c() + 16.0D, targetWorld.getWorldBorder().e() - 16.0D);
        } else {
            BlockPosition blockposition;
            if (enterWorld.dimension == 1) {
                // Use default NORMAL world spawn instead of target
                targetWorld = this.server.worlds.get(0);
                blockposition = targetWorld.getSpawn();
            } else {
                blockposition = targetWorld.getDimensionSpawn();
            }

            x = blockposition.getX();
            y = blockposition.getY();
            z = blockposition.getZ();
        }

        if (enterWorld.dimension != 1) {
            x = MathHelper.clamp((int) x, -29999872, 29999872);
            z = MathHelper.clamp((int) z, -29999872, 29999872);
        }

        return new Location(targetWorld.getWorld(), x, y, z, yaw, pitch);
    }

    public void repositionEntity(Entity entity, Location exit, boolean portal) {
        WorldServer currentWorld = (WorldServer) entity.world;
        WorldServer exitWorld = ((CraftWorld) exit.getWorld()).getHandle();

        // If the current world is the primary world
        if (currentWorld.dimension == 1) {
            // Reposition entity to the exit location
            entity.setPositionRotation(exit.getX(), exit.getY(), exit.getZ(), exit.getYaw(), exit.getPitch());
            if (entity.isAlive()) currentWorld.entityJoinedWorld(entity, false);
        } else {
            if (entity.isAlive()) {
                if (portal) {
                    Vector velocity = entity.getBukkitEntity().getVelocity();
                    exitWorld.getTravelAgent().adjustExit(entity, exit, velocity);
                    entity.setPositionRotation(exit.getX(), exit.getY(), exit.getZ(), exit.getYaw(), exit.getPitch());

                    if (entity.motX != velocity.getX() || entity.motY != velocity.getY() || entity.motZ != velocity.getZ()) {
                        entity.getBukkitEntity().setVelocity(velocity);
                    }
                }

                exitWorld.entityJoinedWorld(entity, false);
            }
        }

        entity.spawnIn(exitWorld);
    }

    /**
     * self explanitory (Real Boot)
     */
    public void tick() {
        if (++this.playerPingIndex > 600) {
            Regulator.post(() -> {
                for (EntityPlayer target : this.players) {
                    target.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, Iterables.filter(this.players, new Predicate<EntityPlayer>() {
                        @Override
                        public boolean apply(EntityPlayer input) {
                            return target.getBukkitEntity().canSee(input.getBukkitEntity());
                        }
                    })));
                }
            });

            this.playerPingIndex = 0;
        }

        servant.setPlayerPingIndex(this.playerPingIndex); // Port to servant
    }

    /**
     * Send packet to all online players
     */
    @Async public void sendAll(Packet<?> packet) {
        Regulator.post(() -> {
            for (EntityPlayer player : this.players) {
                player.playerConnection.sendPacket(packet);
            }
        });
    }

    /**
     * Only send packet to the players who can see the key player
     */
    @Async public void sendAll(Packet<?> packet, EntityHuman keyPlayer) {
        Regulator.post(() -> {
            for (EntityPlayer target : this.players) {
                if (keyPlayer != null && keyPlayer instanceof EntityPlayer && !target.getBukkitEntity().canSee(((EntityPlayer) keyPlayer).getBukkitEntity())) {
                    continue;
                }
                target.playerConnection.sendPacket(packet);
            }
        });
    }

    /**
     * Only send packet to the players in the world
     */
    @Async public void sendAll(Packet<?> packet, World world) {
        Regulator.post(() -> {
            for (EntityPlayer player : this.players) {
                player.playerConnection.sendPacket(packet);
            }
        });
    }

    /**
     * Only send packet to the players in the world (dimension)
     */
    @Async public void sendAll(Packet<?> packet, int dimension) {
        Regulator.post(() -> {
            for (EntityPlayer player : this.players) {
                if (player.dimension == dimension) player.playerConnection.sendPacket(packet);
            }
        });
    }

    /**
     * The packet is not sent to the source player, but all other players who can see the source player within the search radius
     */
    @Async public void sendPacketNearby(@Nullable EntityHuman sourcePlayer, double x, double y, double z, double radius, int dimension, Packet<?> packet) {
        Regulator.post(() -> {
            for (EntityPlayer eachPlayer : this.players) {
                // Test if player receiving packet can see the source of the packet
                if (sourcePlayer != null && sourcePlayer instanceof EntityPlayer && !eachPlayer.getBukkitEntity().canSee(((EntityPlayer) sourcePlayer).getBukkitEntity())) {
                    continue;
                }

                if (eachPlayer != sourcePlayer && eachPlayer.dimension == dimension) {
                    double shiftX = x - eachPlayer.locX;
                    double shiftY = y - eachPlayer.locY;
                    double shiftZ = z - eachPlayer.locZ;

                    if (shiftX * shiftX + shiftY * shiftY + shiftZ * shiftZ < radius * radius) {
                        eachPlayer.playerConnection.sendPacket(packet);
                    }
                }
            }
        });
    }

    /**
     * Saves all of the players' current states.
     */
    public void savePlayers() {
        savePlayers(null);
    }

    public void savePlayers(Integer interval) {
        // Ensure main
        server.postToMainThreadMaybeAsync(() -> {
            long now = MinecraftServer.currentTick;
            MinecraftTimings.savePlayers.startTiming();
            for (EntityPlayer player : this.players) {
                if (interval == null || now - player.lastSave >= interval) {
                    this.savePlayerFile(player);
                }
            }
            MinecraftTimings.savePlayers.stopTiming();
            return null;
        }, true);
    }

    public void addWhitelist(GameProfile profile) {
        if (TorchServer.authUUID()) {
            this.whitelist.add(new WhiteListEntry(profile));
        } else {
            this.whitelist.add(new WhiteListEntry(new GameProfile(profile.getId(), profile.getName().toLowerCase()))); // Support for offline servers
        }

        this.saveWhiteList();
    }

    public void removeWhitelist(GameProfile profile) {
        if (TorchServer.authUUID()) { // TODO: configurable
            this.whitelist.remove(profile);
        } else {
            // Support for offline servers
            this.whitelist.remove(profile);
            this.whitelist.remove(new GameProfile(profile.getId(), profile.getName().toLowerCase()));
        }

        this.saveWhiteList();
    }

    public String[] getWhitelistedPlayerNames() {
        return this.whitelist.getEntries();
    }

    public String[] getOppedPlayerNames() {
        return this.operators.getEntries();
    }

    /**
     * Send message to all source player's team members **except the source player
     */
    public void sendMessageToAllTeamMembers(EntityHuman sourcePlayer, IChatBaseComponent component) {
        // Get team from the source player
        ScoreboardTeamBase team = sourcePlayer.aQ();

        if (team != null) {
            for (String memberName : team.getPlayerNameSet()) {
                EntityPlayer entityplayer = this.getPlayer(memberName);

                if (entityplayer != null && entityplayer != sourcePlayer) {
                    entityplayer.sendMessage(component);
                }
            }
        }
    }

    public void sendMessageToAllTeamOrAllPlayers(EntityHuman sourcePlayer, IChatBaseComponent component) {
        // Get team from the source player
        ScoreboardTeamBase team = sourcePlayer.aQ();

        if (team != null) {
            for (String memberName : team.getPlayerNameSet()) {
                EntityPlayer entityplayer = this.getPlayer(memberName);

                if (entityplayer != null && entityplayer.aQ() != team) {
                    entityplayer.sendMessage(component);
                }
            }
        } else {
            this.sendMessage(component);
        }
    }

    public void sendMessage(IChatBaseComponent[] iChatBaseComponents) {
        for (IChatBaseComponent component : iChatBaseComponents) {
            sendMessage(component, true);
        }
    }

    public void sendMessage(IChatBaseComponent component, boolean flag) {
        this.server.sendMessage(component);
        int i = flag ? 1 : 0;

        // We run this through our processor first so we can get web links etc
        this.sendAll(new PacketPlayOutChat(CraftChatMessage.fixComponent(component), (byte) i));
    }

    public void sendMessage(IChatBaseComponent component) {
        this.sendMessage(component, true);
    }

    public EntityPlayer getPlayer(String name) {
        return this.playersByName.get(name);
    }

    public EntityPlayer getPlayer(UUID uuid) {
        return this.uuidToPlayerMap.get(uuid);
    }

    /**
     * Get a comma separated list of online players
     */
    public String getFormattedListOfPlayers(boolean includeUUIDs) {
        String result = ""; boolean first = true;
        for (EntityPlayer player : this.players) {
            if (!first) {
                result = result + ", ";
            } else first = false;
            
            result = result + player.getName();
            if (includeUUIDs) result = result + " (" + player.getUUIDString() + ")";
        }
        
        return result;
    }

    /**
     * Returns an array of the usernames of all the connected players
     */
    public String[] getOnlinePlayerNames() {
        String[] names = new String[this.players.size()]; int index = 0;
        for (EntityPlayer player : this.players) {
            names[index++] = player.getName();
        }
        
        return names;
    }

    /**
     * Returns an array of the game profile of all the connected players
     */
    public GameProfile[] getOnlinePlayerProfiles() {
        GameProfile[] profiles = new GameProfile[this.players.size()]; int index = 0;
        for (EntityPlayer player : this.players) {
            profiles[index++] = player.getProfile();
        }
        
        return profiles;
    }

    /**
     * Updates the time and weather for the given player to those of the given world
     */
    public void updateTimeAndWeatherForPlayer(EntityPlayer player, WorldServer world) {
        WorldBorder worldborder = player.world.getWorldBorder();
        player.playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
        player.playerConnection.sendPacket(new PacketPlayOutUpdateTime(world.getTime(), world.getDayTime(), world.getGameRules().getBoolean("doDaylightCycle")));
        BlockPosition blockposition = world.getSpawn();
        player.playerConnection.sendPacket(new PacketPlayOutSpawnPosition(blockposition));

        // If the world is raining
        if (world.W()) {
            // Handle player weather
            player.setPlayerWeather(org.bukkit.WeatherType.DOWNFALL, false);
            player.updateWeather(-world.o, world.o, -world.q, world.q);
            // CraftBukkit end
        }
    }

    /**
     * Sends the players inventory, scaled health and held item slot to himself
     */
    public void syncPlayerInventoryHealth(EntityPlayer player) {
        player.updateInventory(player.defaultContainer);
        // Update scaled health on respawn and worldchange
        player.getBukkitEntity().updateScaledHealth();
        player.playerConnection.sendPacket(new PacketPlayOutHeldItemSlot(player.inventory.itemInHandIndex));
    }

    /**
     * Returns an array of usernames for which player.dat exists for, only containing players in the primary world
     */
    public String[] getSeenPlayers() {
        return this.server.worlds.get(0).getDataManager().getPlayerFileData().getSeenPlayers();
    }

    /**
     * Returns a list containing all players using the given IP adress
     */
    public List<EntityPlayer> matchingPlayersByAddress(String IpAdress) {
        ArrayList<EntityPlayer> list = Lists.newArrayList();
        
        for (EntityPlayer player : this.players) {
            if (player.getIpAdress().equals(IpAdress)) list.add(player);
        }
        
        return list;
    }

    /**
     * On LAN servers, returns the host's player data to be written to level.dat
     */
    public NBTTagCompound getHostPlayerData() {
        return null;
    }

    /**
     * Apply default game mode if sourcePlayer is null
     */
    public void setPlayerGamemodeBasedOnOther(EntityPlayer player, EntityPlayer sourcePlayer, World world) {
        if (sourcePlayer != null) {
            player.playerInteractManager.setGameMode(sourcePlayer.playerInteractManager.getGameMode());
        } else if (this.gameMode != null) {
            player.playerInteractManager.setGameMode(this.gameMode);
        }

        // Initialize(apply) game mode for the player
        player.playerInteractManager.b(world.getWorldData().getGameType());
    }

    /**
     * Kicks everyone with 'Shutdown Message' as reason, also clear Paper collideRule team
     */
    public void disconnectAllPlayers() {
        // Disconnect safely
        for (EntityPlayer player : this.players) {
            player.playerConnection.disconnect(this.server.craftServer.getShutdownMessage());
        }
        // Remove Paper collideRule team if it exists
        if (this.servant.getCollideRuleTeamName() != null) {
            final Scoreboard scoreboard = this.getServer().getWorld().getScoreboard();
            final ScoreboardTeam team = scoreboard.getTeam(this.servant.getCollideRuleTeamName());
            if (team != null) scoreboard.removeTeam(team);
        }
    }

    public ServerStatisticManager getOrCreatePlayerStatsFile(EntityHuman player) {
        UUID uuid = player.getUniqueID();
        ServerStatisticManager statManager = uuid == null ? null : this.playerStatFiles.get(uuid);

        if (statManager == null) {
            File worldStatDirectory = new File(this.server.getWorldServer(0).getDataManager().getDirectory(), "stats");
            File playerStatFile = new File(worldStatDirectory, uuid + ".json");

            if (!playerStatFile.exists()) {
                File playerStatFileLegacy = new File(worldStatDirectory, player.getName() + ".json");

                if (playerStatFileLegacy.exists() && playerStatFileLegacy.isFile()) {
                    playerStatFileLegacy.renameTo(playerStatFile);
                }
            }

            statManager = new ServerStatisticManager(this.minecraftServer, playerStatFile);
            statManager.a(); // PAIL: Read the stat files
            this.playerStatFiles.put(uuid, statManager);
        }

        return statManager;
    }

    /**
     * Set view distance for all worlds
     */
    public void setViewDistance(int distance) {
        this.viewDistance = distance;
        if (this.minecraftServer.worldServer != null) {
            for (int index = 0, size = server.worlds.size(); index < size; ++index) {
                WorldServer worldserver = server.worlds.get(index); // TODO: CraftBukkit only world(0), but all world in original NMS

                if (worldserver != null) {
                    worldserver.getPlayerChunkMap().a(distance); // PAIL: Set world player view radius
                    worldserver.getTracker().a(distance); // PAIL: Set world view distance
                }
            }
        }
    }

    /**
     * Get the EntityPlayer object representing the player with the UUID
     */
    public EntityPlayer getPlayerByUUID(UUID uuid) {
        return this.uuidToPlayerMap.get(uuid);
    }

    public boolean bypassesPlayerLimit(GameProfile gameprofile) {
        return this.getOperators().b(gameprofile); // PAIL: Bypass limit for the player
    }

    public void saveIPBanList() {
        this.bannedIPs.save();
    }

    public void loadIPBanList() {
        try {
            this.bannedIPs.load();
        } catch (IOException io) {
            logger.warn("Failed to load ip banlist: ", io);
        }
    }

    public void savePlayerBanList() {
        this.bannedPlayers.save();
    }

    public void loadPlayerBanList() {
        try {
            this.bannedPlayers.load();
        } catch (IOException io) {
            logger.warn("Failed to load user banlist: ", io);
        }
    }

    public void saveOpsList() {
        this.operators.save();
    }

    public void loadOpsList() {
        try {
            this.operators.load();
        } catch (Throwable t) {
            logger.warn("Failed to load operators list: ", t);
        }
    }

    public void saveWhiteList() {
        this.whitelist.save();
    }

    public void readWhiteList() {
        try {
            this.whitelist.load();
        } catch (Throwable t) {
            logger.warn("Failed to load white-list: ", t);
        }
    }

    public void setWhitelistMode(boolean flag) {
        this.isWhitelistMode = flag;
        this.getServer().setProperty("white-list", Boolean.valueOf(flag));
        this.getServer().saveProperties();
    }
    
    /** Get player's name by uuid */
    @Nullable public String uuidToUsername(UUID uuid) {
        return this.uuidToPlayerMap.get(uuid).getName();
    }
}
