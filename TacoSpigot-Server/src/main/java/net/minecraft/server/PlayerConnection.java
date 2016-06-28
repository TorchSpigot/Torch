package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.util.AttributeKey;

// CraftBukkit start
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.util.NumberConversions;
import co.aikar.timings.MinecraftTimings; // Paper
// CraftBukkit end

public class PlayerConnection implements PacketListenerPlayIn, ITickable {

    private static final Logger LOGGER = LogManager.getLogger();
    public final NetworkManager networkManager;
    private final MinecraftServer minecraftServer;
    public EntityPlayer player;
    private int e;
    private int f;
    private long g;
    private long h;
    // CraftBukkit start - multithreaded fields
    private volatile int chatThrottle;
    private static final AtomicIntegerFieldUpdater chatSpamField = AtomicIntegerFieldUpdater.newUpdater(PlayerConnection.class, "chatThrottle");
    // CraftBukkit end
    private int j;
    private final IntHashMap<Short> k = new IntHashMap();
    private double l;
    private double m;
    private double n;
    private double o;
    private double p;
    private double q;
    private Entity r;
    private double s;
    private double t;
    private double u;
    private double v;
    private double w;
    private double x;
    private Vec3D teleportPos;
    private int teleportAwait;
    private int A;
    private boolean B;
    private int C;
    private boolean D;
    private int E;
    private int F;
    private int G;
    private boolean processedDisconnect; // CraftBukkit - Added
	public static final AttributeKey<EntityPlayer> PLAYER_ATTR_KEY = AttributeKey.valueOf("player"); // TacoSpigot

    public PlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        this.minecraftServer = minecraftserver;
        this.networkManager = networkmanager;
        networkmanager.setPacketListener(this);
        this.player = entityplayer;
        entityplayer.playerConnection = this;
		this.networkManager.channel.attr(PLAYER_ATTR_KEY).set(this.player); // TacoSpigot - set the player

        // CraftBukkit start - add fields and methods
        this.server = minecraftserver.server;
    }

    private final org.bukkit.craftbukkit.CraftServer server;
    private int lastTick = MinecraftServer.currentTick;
    private int allowedPlayerTicks = 1;
    private int lastDropTick = MinecraftServer.currentTick;
    private int dropCount = 0;
    private static final int SURVIVAL_PLACE_DISTANCE_SQUARED = 6 * 6;
    private static final int CREATIVE_PLACE_DISTANCE_SQUARED = 7 * 7;

    // Get position of last block hit for BlockDamageLevel.STOPPED
    private double lastPosX = Double.MAX_VALUE;
    private double lastPosY = Double.MAX_VALUE;
    private double lastPosZ = Double.MAX_VALUE;
    private float lastPitch = Float.MAX_VALUE;
    private float lastYaw = Float.MAX_VALUE;
    private boolean justTeleported = false;
    private boolean hasMoved; // Spigot

    public CraftPlayer getPlayer() {
        return (this.player == null) ? null : (CraftPlayer) this.player.getBukkitEntity();
    }
    private final static HashSet<Integer> invalidItems = new HashSet<Integer>(java.util.Arrays.asList(8, 9, 10, 11, 26, 34, 36, 43, 51, 52, 55, 59, 60, 62, 63, 64, 68, 71, 74, 75, 83, 90, 92, 93, 94, 104, 105, 115, 117, 118, 119, 125, 127, 132, 140, 141, 142, 144)); // TODO: Check after every update.
    // CraftBukkit end

    public void c() {
        this.d();
        this.player.k_();
        this.player.setLocation(this.l, this.m, this.n, this.player.yaw, this.player.pitch);
        ++this.e;
        this.G = this.F;
        if (this.B) {
            if (++this.C > 80) {
                PlayerConnection.LOGGER.warn(this.player.getName() + " was kicked for floating too long!");
                this.disconnect("Flying is not enabled on this server");
                return;
            }
        } else {
            this.B = false;
            this.C = 0;
        }

        this.r = this.player.getVehicle();
        if (this.r != this.player && this.r.bu() == this.player) {
            this.s = this.r.locX;
            this.t = this.r.locY;
            this.u = this.r.locZ;
            this.v = this.r.locX;
            this.w = this.r.locY;
            this.x = this.r.locZ;
            if (this.D && this.player.getVehicle().bu() == this.player) {
                if (++this.E > 80) {
                    PlayerConnection.LOGGER.warn(this.player.getName() + " was kicked for floating a vehicle too long!");
                    this.disconnect("Flying is not enabled on this server");
                    return;
                }
            } else {
                this.D = false;
                this.E = 0;
            }
        } else {
            this.r = null;
            this.D = false;
            this.E = 0;
        }

        this.minecraftServer.methodProfiler.a("keepAlive");
        if ((long) this.e - this.h > 40L) {
            this.h = (long) this.e;
            this.g = this.e();
            this.f = (int) this.g;
            this.sendPacket(new PacketPlayOutKeepAlive(this.f));
        }

        this.minecraftServer.methodProfiler.b();
        // CraftBukkit start
        for (int spam; (spam = this.chatThrottle) > 0 && !chatSpamField.compareAndSet(this, spam, spam - 1); ) ;
        /* Use thread-safe field access instead
        if (this.chatThrottle > 0) {
            --this.chatThrottle;
        }
        */
        // CraftBukkit end

        if (this.j > 0) {
            --this.j;
        }

        if (this.player.I() > 0L && this.minecraftServer.getIdleTimeout() > 0 && MinecraftServer.av() - this.player.I() > (long) (this.minecraftServer.getIdleTimeout() * 1000 * 60)) {
            this.player.resetIdleTimer(); // CraftBukkit - SPIGOT-854
            this.disconnect("You have been idle for too long!");
        }

    }

    public void captureCurrentPosition() { d(); } private void d() { // Paper // OBFHELPER
        this.l = this.player.locX;
        this.m = this.player.locY;
        this.n = this.player.locZ;
        this.o = this.player.locX;
        this.p = this.player.locY;
        this.q = this.player.locZ;
    }

    public NetworkManager a() {
        return this.networkManager;
    }

    public void disconnect(String s) {
        // CraftBukkit start - fire PlayerKickEvent
        String leaveMessage = EnumChatFormat.YELLOW + this.player.getName() + " left the game.";

        PlayerKickEvent event = new PlayerKickEvent(this.server.getPlayer(this.player), s, leaveMessage);

        if (this.server.getServer().isRunning()) {
            this.server.getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            // Do not kick the player
            return;
        }
        // Send the possibly modified leave message
        s = event.getReason();
        // CraftBukkit end
        final ChatComponentText chatcomponenttext = new ChatComponentText(s);

        this.networkManager.sendPacket(new PacketPlayOutKickDisconnect(chatcomponenttext), new GenericFutureListener() {
            public void operationComplete(Future future) throws Exception { // CraftBukkit - decompile error
                PlayerConnection.this.networkManager.close(chatcomponenttext);
            }
        }, new GenericFutureListener[0]);
        this.a(chatcomponenttext); // CraftBukkit - fire quit instantly
        this.networkManager.stopReading();
        // CraftBukkit - Don't wait
        this.minecraftServer.postToMainThread(new Runnable() {
            public void run() {
                PlayerConnection.this.networkManager.handleDisconnection();
            }
        });
    }

    public void a(PacketPlayInSteerVehicle packetplayinsteervehicle) {
        PlayerConnectionUtils.ensureMainThread(packetplayinsteervehicle, this, this.player.x());
        this.player.a(packetplayinsteervehicle.a(), packetplayinsteervehicle.b(), packetplayinsteervehicle.c(), packetplayinsteervehicle.d());
    }

    private static boolean b(PacketPlayInFlying packetplayinflying) {
        return Doubles.isFinite(packetplayinflying.a(0.0D)) && Doubles.isFinite(packetplayinflying.b(0.0D)) && Doubles.isFinite(packetplayinflying.c(0.0D)) && Floats.isFinite(packetplayinflying.b(0.0F)) && Floats.isFinite(packetplayinflying.a(0.0F)) ? false : Math.abs(packetplayinflying.a(0.0D)) <= 3.0E7D && Math.abs(packetplayinflying.a(0.0D)) <= 3.0E7D;
    }

    private static boolean b(PacketPlayInVehicleMove packetplayinvehiclemove) {
        return !Doubles.isFinite(packetplayinvehiclemove.getX()) || !Doubles.isFinite(packetplayinvehiclemove.getY()) || !Doubles.isFinite(packetplayinvehiclemove.getZ()) || !Floats.isFinite(packetplayinvehiclemove.getPitch()) || !Floats.isFinite(packetplayinvehiclemove.getYaw());
    }

    public void a(PacketPlayInVehicleMove packetplayinvehiclemove) {
        PlayerConnectionUtils.ensureMainThread(packetplayinvehiclemove, this, this.player.x());
        if (b(packetplayinvehiclemove)) {
            this.disconnect("Invalid move vehicle packet received");
        } else {
            Entity entity = this.player.getVehicle();

            if (entity != this.player && entity.bu() == this.player && entity == this.r) {
                WorldServer worldserver = this.player.x();
                double d0 = entity.locX;
                double d1 = entity.locY;
                double d2 = entity.locZ;
                double d3 = packetplayinvehiclemove.getX();
                double d4 = packetplayinvehiclemove.getY();
                double d5 = packetplayinvehiclemove.getZ();
                float f = packetplayinvehiclemove.getYaw();
                float f1 = packetplayinvehiclemove.getPitch();
                double d6 = d3 - this.s;
                double d7 = d4 - this.t;
                double d8 = d5 - this.u;
                double d9 = entity.motX * entity.motX + entity.motY * entity.motY + entity.motZ * entity.motZ;
                double d10 = d6 * d6 + d7 * d7 + d8 * d8;


                // CraftBukkit start - handle custom speeds and skipped ticks
                this.allowedPlayerTicks += (System.currentTimeMillis() / 50) - this.lastTick;
                this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                this.lastTick = (int) (System.currentTimeMillis() / 50);

                ++this.F;
                int i = this.F - this.G; // PAIL: Rename
                if (i > Math.max(this.allowedPlayerTicks, 5)) {
                    PlayerConnection.LOGGER.debug(this.player.getName() + " is sending move packets too frequently (" + i + " packets since last tick)");
                    i = 1;
                }

                if (d10 > 0) {
                    allowedPlayerTicks -= 1;
                } else {
                    allowedPlayerTicks = 20;
                }
                float speed;
                if (player.abilities.isFlying) {
                    speed = player.abilities.flySpeed * 20f;
                } else {
                    speed = player.abilities.walkSpeed * 10f;
                }
                speed *= 2f; // TODO: Get the speed of the vehicle instead of the player

                if (d10 - d9 > Math.max(100, Math.pow((double) (org.spigotmc.SpigotConfig.movedTooQuicklyMultiplier * (float) i * speed), 2)) && (!this.minecraftServer.R() || !this.minecraftServer.Q().equals(entity.getName()))) { // Spigot
                // CraftBukkit end
                    PlayerConnection.LOGGER.warn(entity.getName() + " (vehicle of " + this.player.getName() + ") moved too quickly! " + d6 + "," + d7 + "," + d8);
                    this.networkManager.sendPacket(new PacketPlayOutVehicleMove(entity));
                    return;
                }

                boolean flag = worldserver.getCubes(entity, entity.getBoundingBox().shrink(0.0625D)).isEmpty();

                d6 = d3 - this.v;
                d7 = d4 - this.w - 1.0E-6D;
                d8 = d5 - this.x;
                entity.move(d6, d7, d8);
                double d11 = d7;

                d6 = d3 - entity.locX;
                d7 = d4 - entity.locY;
                if (d7 > -0.5D || d7 < 0.5D) {
                    d7 = 0.0D;
                }

                d8 = d5 - entity.locZ;
                d10 = d6 * d6 + d7 * d7 + d8 * d8;
                boolean flag1 = false;

                if (d10 > org.spigotmc.SpigotConfig.movedWronglyThreshold) { // Spigot
                    flag1 = true;
                    PlayerConnection.LOGGER.warn(entity.getName() + " moved wrongly!");
                }

                entity.setLocation(d3, d4, d5, f, f1);
                boolean flag2 = worldserver.getCubes(entity, entity.getBoundingBox().shrink(0.0625D)).isEmpty();

                if (flag && (flag1 || !flag2)) {
                    entity.setLocation(d0, d1, d2, f, f1);
                    this.networkManager.sendPacket(new PacketPlayOutVehicleMove(entity));
                    return;
                }

                // CraftBukkit start - fire PlayerMoveEvent
                Player player = this.getPlayer();
                // Spigot Start
                if ( !hasMoved )
                {
                    Location curPos = player.getLocation();
                    lastPosX = curPos.getX();
                    lastPosY = curPos.getY();
                    lastPosZ = curPos.getZ();
                    lastYaw = curPos.getYaw();
                    lastPitch = curPos.getPitch();
                    hasMoved = true;
                }
                // Spigot End
                Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch); // Get the Players previous Event location.
                Location to = player.getLocation().clone(); // Start off the To location as the Players current location.

                // If the packet contains movement information then we update the To location with the correct XYZ.
                to.setX(packetplayinvehiclemove.getX());
                to.setY(packetplayinvehiclemove.getY());
                to.setZ(packetplayinvehiclemove.getZ());


                // If the packet contains look information then we update the To location with the correct Yaw & Pitch.
                to.setYaw(packetplayinvehiclemove.getYaw());
                to.setPitch(packetplayinvehiclemove.getPitch());

                // Prevent 40 event-calls for less than a single pixel of movement >.>
                double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
                float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

                if ((delta > 1f / 256 || deltaAngle > 10f) && !this.player.dead) {
                    this.lastPosX = to.getX();
                    this.lastPosY = to.getY();
                    this.lastPosZ = to.getZ();
                    this.lastYaw = to.getYaw();
                    this.lastPitch = to.getPitch();

                    // Skip the first time we do this
                    if (true) { // Spigot - don't skip any move events
                        Location oldTo = to.clone();
                        PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                        this.server.getPluginManager().callEvent(event);

                        // If the event is cancelled we move the player back to their old location.
                        if (event.isCancelled()) {
                            teleport(from);
                            return;
                        }

                        // If a Plugin has changed the To destination then we teleport the Player
                        // there to avoid any 'Moved wrongly' or 'Moved too quickly' errors.
                        // We only do this if the Event was not cancelled.
                        if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
                            this.player.getBukkitEntity().teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.UNKNOWN);
                            return;
                        }

                        // Check to see if the Players Location has some how changed during the call of the event.
                        // This can happen due to a plugin teleporting the player instead of using .setTo()
                        if (!from.equals(this.getPlayer().getLocation()) && this.justTeleported) {
                            this.justTeleported = false;
                            return;
                        }
                    }
                }
                // CraftBukkit end

                this.minecraftServer.getPlayerList().d(this.player);
                this.player.checkMovement(this.player.locX - d0, this.player.locY - d1, this.player.locZ - d2);
                this.D = d11 >= -0.03125D && !this.minecraftServer.getAllowFlight() && !worldserver.d(entity.getBoundingBox().g(0.0625D).a(0.0D, -0.55D, 0.0D));
                this.v = entity.locX;
                this.w = entity.locY;
                this.x = entity.locZ;
            }

        }
    }

    public void a(PacketPlayInTeleportAccept packetplayinteleportaccept) {
        PlayerConnectionUtils.ensureMainThread(packetplayinteleportaccept, this, this.player.x());
        if (packetplayinteleportaccept.a() == this.teleportAwait) {
            this.player.setLocation(this.teleportPos.x, this.teleportPos.y, this.teleportPos.z, this.player.yaw, this.player.pitch);
            if (this.player.K()) {
                this.o = this.teleportPos.x;
                this.p = this.teleportPos.y;
                this.q = this.teleportPos.z;
                this.player.L();
            }

            this.teleportPos = null;
        }

    }

    public void a(PacketPlayInFlying packetplayinflying) {
        PlayerConnectionUtils.ensureMainThread(packetplayinflying, this, this.player.x());
        if (b(packetplayinflying)) {
            this.disconnect("Invalid move player packet received");
        } else {
            WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

            if (!this.player.viewingCredits && !this.player.dead) { // CraftBukkit - Added ' && !this.player.dead'
                if (this.e == 0) {
                    this.d();
                }
				
				this.player.clientTicksSinceLastAttack++; // TacoSpigot

                if (this.teleportPos != null) {
                    if (this.e - this.A > 20) {
                        this.A = this.e;
                        this.a(this.teleportPos.x, this.teleportPos.y, this.teleportPos.z, this.player.yaw, this.player.pitch);
                    }
                    this.allowedPlayerTicks = 20; // CraftBukkit
                } else {
                    this.A = this.e;
                    if (this.player.isPassenger()) {
                        this.player.setLocation(this.player.locX, this.player.locY, this.player.locZ, packetplayinflying.a(this.player.yaw), packetplayinflying.b(this.player.pitch));
                        this.minecraftServer.getPlayerList().d(this.player);
                        this.allowedPlayerTicks = 20; // CraftBukkit
                    } else {
                        // CraftBukkit - Make sure the move is valid but then reset it for plugins to modify
                        double prevX = player.locX;
                        double prevY = player.locY;
                        double prevZ = player.locZ;
                        float prevYaw = player.yaw;
                        float prevPitch = player.pitch;
                        // CraftBukkit end
                        double d0 = this.player.locX;
                        double d1 = this.player.locY;
                        double d2 = this.player.locZ;
                        double d3 = this.player.locY;
                        double d4 = packetplayinflying.a(this.player.locX);
                        double d5 = packetplayinflying.b(this.player.locY);
                        double d6 = packetplayinflying.c(this.player.locZ);
                        float f = packetplayinflying.a(this.player.yaw);
                        float f1 = packetplayinflying.b(this.player.pitch);
                        double d7 = d4 - this.l;
                        double d8 = d5 - this.m;
                        double d9 = d6 - this.n;
                        double d10 = this.player.motX * this.player.motX + this.player.motY * this.player.motY + this.player.motZ * this.player.motZ;
                        double d11 = d7 * d7 + d8 * d8 + d9 * d9;

                        ++this.F;
                        int i = this.F - this.G;

                        // CraftBukkit start - handle custom speeds and skipped ticks
                        this.allowedPlayerTicks += (System.currentTimeMillis() / 50) - this.lastTick;
                        this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                        this.lastTick = (int) (System.currentTimeMillis() / 50);

                        if (i > Math.max(this.allowedPlayerTicks, 5)) {
                            PlayerConnection.LOGGER.debug(this.player.getName() + " is sending move packets too frequently (" + i + " packets since last tick)");
                            i = 1;
                        }

                        if (packetplayinflying.hasLook || d11 > 0) {
                            allowedPlayerTicks -= 1;
                        } else {
                            allowedPlayerTicks = 20;
                        }
                        float speed;
                        if (player.abilities.isFlying) {
                            speed = player.abilities.flySpeed * 20f;
                        } else {
                            speed = player.abilities.walkSpeed * 10f;
                        }

                        if (!this.player.K() && (!this.player.x().getGameRules().getBoolean("disableElytraMovementCheck") || !this.player.cC())) {
                            float f2 = this.player.cC() ? 300.0F : 100.0F;

                            if (d11 - d10 > Math.max(100, Math.pow((double) (org.spigotmc.SpigotConfig.movedTooQuicklyMultiplier * (float) i * speed), 2)) && (!this.minecraftServer.R() || !this.minecraftServer.Q().equals(this.player.getName()))) { // Spigot
                        // CraftBukkit end
                                PlayerConnection.LOGGER.warn(this.player.getName() + " moved too quickly! " + d7 + "," + d8 + "," + d9);
                                this.a(this.player.locX, this.player.locY, this.player.locZ, this.player.yaw, this.player.pitch);
                                return;
                            }
                        }

                        boolean flag = worldserver.getCubes(this.player, this.player.getBoundingBox().shrink(0.0625D)).isEmpty();

                        d7 = d4 - this.o;
                        d8 = d5 - this.p;
                        d9 = d6 - this.q;
                        if (this.player.onGround && !packetplayinflying.a() && d8 > 0.0D) {
                            this.player.ci();
                        }

                        this.player.move(d7, d8, d9);
                        this.player.onGround = packetplayinflying.a();
                        double d12 = d8;

                        d7 = d4 - this.player.locX;
                        d8 = d5 - this.player.locY;
                        if (d8 > -0.5D || d8 < 0.5D) {
                            d8 = 0.0D;
                        }

                        d9 = d6 - this.player.locZ;
                        d11 = d7 * d7 + d8 * d8 + d9 * d9;
                        boolean flag1 = false;

                        if (!this.player.K() && d11 > org.spigotmc.SpigotConfig.movedWronglyThreshold && !this.player.isSleeping() && !this.player.playerInteractManager.isCreative() && this.player.playerInteractManager.getGameMode() != WorldSettings.EnumGamemode.SPECTATOR) { // Spigot
                            flag1 = true;
                            PlayerConnection.LOGGER.warn(this.player.getName() + " moved wrongly!");
                        }

                        this.player.setLocation(d4, d5, d6, f, f1);
                        this.player.checkMovement(this.player.locX - d0, this.player.locY - d1, this.player.locZ - d2);
                        if (!this.player.noclip && !this.player.isSleeping()) {
                            boolean flag2 = worldserver.getCubes(this.player, this.player.getBoundingBox().shrink(0.0625D)).isEmpty();

                            if (flag && (flag1 || !flag2)) {
                                this.a(d0, d1, d2, f, f1);
                                return;
                            }
                        }

                        // CraftBukkit start - fire PlayerMoveEvent
                        // Rest to old location first
                        this.player.setLocation(prevX, prevY, prevZ, prevYaw, prevPitch);

                        Player player = this.getPlayer();
                        Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch); // Get the Players previous Event location.
                        Location to = player.getLocation().clone(); // Start off the To location as the Players current location.

                        // If the packet contains movement information then we update the To location with the correct XYZ.
                        if (packetplayinflying.hasPos) {
                            to.setX(packetplayinflying.x);
                            to.setY(packetplayinflying.y);
                            to.setZ(packetplayinflying.z);
                        }

                        // If the packet contains look information then we update the To location with the correct Yaw & Pitch.
                        if (packetplayinflying.hasLook) {
                            to.setYaw(packetplayinflying.yaw);
                            to.setPitch(packetplayinflying.pitch);
                        }

                        // Prevent 40 event-calls for less than a single pixel of movement >.>
                        double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
                        float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

                        if ((delta > 1f / 256 || deltaAngle > 10f) && !this.player.dead) {
                            this.lastPosX = to.getX();
                            this.lastPosY = to.getY();
                            this.lastPosZ = to.getZ();
                            this.lastYaw = to.getYaw();
                            this.lastPitch = to.getPitch();

                            // Skip the first time we do this
                            if (from.getX() != Double.MAX_VALUE) {
                                Location oldTo = to.clone();
                                PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                                this.server.getPluginManager().callEvent(event);

                                // If the event is cancelled we move the player back to their old location.
                                if (event.isCancelled()) {
                                    teleport(from);
                                    return;
                                }

                                // If a Plugin has changed the To destination then we teleport the Player
                                // there to avoid any 'Moved wrongly' or 'Moved too quickly' errors.
                                // We only do this if the Event was not cancelled.
                                if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
                                    this.player.getBukkitEntity().teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.UNKNOWN);
                                    return;
                                }

                                // Check to see if the Players Location has some how changed during the call of the event.
                                // This can happen due to a plugin teleporting the player instead of using .setTo()
                                if (!from.equals(this.getPlayer().getLocation()) && this.justTeleported) {
                                    this.justTeleported = false;
                                    return;
                                }
                            }
                        }
                        this.player.setLocation(d4, d5, d6, f, f1); // Copied from above
                        // CraftBukkit end

                        this.B = d12 >= -0.03125D;
                        this.B &= !this.minecraftServer.getAllowFlight() && !this.player.abilities.canFly;
                        this.B &= !this.player.hasEffect(MobEffects.LEVITATION) && !this.player.cC() && !worldserver.d(this.player.getBoundingBox().g(0.0625D).a(0.0D, -0.55D, 0.0D));
                        this.player.onGround = packetplayinflying.a();
                        this.minecraftServer.getPlayerList().d(this.player);
                        this.player.a(this.player.locY - d3, packetplayinflying.a());
                        this.o = this.player.locX;
                        this.p = this.player.locY;
                        this.q = this.player.locZ;
                    }
                }
            }
        }
    }

    public void a(double d0, double d1, double d2, float f, float f1) {
        this.a(d0, d1, d2, f, f1, Collections.<PacketPlayOutPosition.EnumPlayerTeleportFlags>emptySet());
    }

    public void a(double d0, double d1, double d2, float f, float f1, Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> set) {
        // CraftBukkit start - Delegate to teleport(Location)
        Player player = this.getPlayer();
        Location from = player.getLocation();

        double x = d0;
        double y = d1;
        double z = d2;
        float yaw = f;
        float pitch = f1;
        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.X)) {
            x += from.getX();
        }
        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Y)) {
            y += from.getY();
        }
        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Z)) {
            z += from.getZ();
        }
        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Y_ROT)) {
            yaw += from.getYaw();
        }
        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.X_ROT)) {
            pitch += from.getPitch();
        }


        Location to = new Location(this.getPlayer().getWorld(), x, y, z, yaw, pitch);
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from.clone(), to.clone(), PlayerTeleportEvent.TeleportCause.UNKNOWN);
        this.server.getPluginManager().callEvent(event);

        if (event.isCancelled() || !to.equals(event.getTo())) {
            set.clear(); // Can't relative teleport
            to = event.isCancelled() ? event.getFrom() : event.getTo();
            d0 = to.getX();
            d1 = to.getY();
            d2 = to.getZ();
            f = to.getYaw();
            f1 = to.getPitch();
        }

        this.internalTeleport(d0, d1, d2, f, f1, set);
    }

    public void teleport(Location dest) {
        internalTeleport(dest.getX(), dest.getY(), dest.getZ(), dest.getYaw(), dest.getPitch(), Collections.emptySet());
    }

    private void internalTeleport(double d0, double d1, double d2, float f, float f1, Set set) {
        if (Float.isNaN(f)) {
            f = 0;
        }

        if (Float.isNaN(f1)) {
            f1 = 0;
        }
        this.justTeleported = true;
        this.teleportPos = new Vec3D(d0, d1, d2);
        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.X)) {
            this.teleportPos = this.teleportPos.add(this.player.locX, 0.0D, 0.0D);
        }

        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Y)) {
            this.teleportPos = this.teleportPos.add(0.0D, this.player.locY, 0.0D);
        }

        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Z)) {
            this.teleportPos = this.teleportPos.add(0.0D, 0.0D, this.player.locZ);
        }
        // CraftBukkit end
        float f2 = f;
        float f3 = f1;

        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Y_ROT)) {
            f2 = f + this.player.yaw;
        }

        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.X_ROT)) {
            f3 = f1 + this.player.pitch;
        }

        // CraftBukkit start - update last location
        this.lastPosX = this.teleportPos.x;
        this.lastPosY = this.teleportPos.y;
        this.lastPosZ = this.teleportPos.z;
        this.lastYaw = f2;
        this.lastPitch = f3;
        // CraftBukkit end

        if (++this.teleportAwait == Integer.MAX_VALUE) {
            this.teleportAwait = 0;
        }

        this.A = this.e;
        this.player.setLocation(this.teleportPos.x, this.teleportPos.y, this.teleportPos.z, f2, f3);
        this.player.playerConnection.sendPacket(new PacketPlayOutPosition(d0, d1, d2, f, f1, set, this.teleportAwait));
    }

    public void a(PacketPlayInBlockDig packetplayinblockdig) {
        PlayerConnectionUtils.ensureMainThread(packetplayinblockdig, this, this.player.x());
        if (this.player.dead) return; // CraftBukkit
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
        BlockPosition blockposition = packetplayinblockdig.a();

        this.player.resetIdleTimer();
        ItemStack itemstack;

        // CraftBukkit start
        switch (PlayerConnection.SyntheticClass_1.a[packetplayinblockdig.c().ordinal()]) {
        case 1: // SWAP_HELD_ITEMS
            if (!this.player.isSpectator()) {
                // CraftBukkit start
                PlayerSwapHandItemsEvent swapItemsEvent = new PlayerSwapHandItemsEvent(getPlayer(), CraftItemStack.asBukkitCopy(this.player.b(EnumHand.OFF_HAND)), CraftItemStack.asBukkitCopy(this.player.b(EnumHand.MAIN_HAND)));
                this.server.getPluginManager().callEvent(swapItemsEvent);
                if (swapItemsEvent.isCancelled()) {
                    return;
                }
                itemstack = CraftItemStack.asNMSCopy(swapItemsEvent.getMainHandItem());
                this.player.a(EnumHand.OFF_HAND, CraftItemStack.asNMSCopy(swapItemsEvent.getOffHandItem()));
                this.player.a(EnumHand.MAIN_HAND, itemstack);
                // CraftBukkit end
            }

            return;

        case 2: // DROP_ITEM
            if (!this.player.isSpectator()) {
                // limit how quickly items can be dropped
                // If the ticks aren't the same then the count starts from 0 and we update the lastDropTick.
                if (this.lastDropTick != MinecraftServer.currentTick) {
                    this.dropCount = 0;
                    this.lastDropTick = MinecraftServer.currentTick;
                } else {
                    // Else we increment the drop count and check the amount.
                    this.dropCount++;
                    if (this.dropCount >= 20) {
                        LOGGER.warn(this.player.getName() + " dropped their items too quickly!");
                        this.disconnect("You dropped your items too quickly (Hacking?)");
                        return;
                    }
                }
                // CraftBukkit end
                this.player.a(false);
            }

            return;

        case 3: // DROP_ALL_ITEMS
            if (!this.player.isSpectator()) {
                this.player.a(true);
            }

            return;

        case 4: // RELEASE_USE_ITEM
            this.player.clearActiveItem();
            itemstack = this.player.getItemInMainHand();
            if (itemstack != null && itemstack.count == 0) {
                this.player.a(EnumHand.MAIN_HAND, (ItemStack) null);
            }

            return;

        case 5: // START_DESTROY_BLOCK
        case 6: // ABORT_DESTROY_BLOCK
        case 7: // STOP_DESTROY_BLOCK
            double d0 = this.player.locX - ((double) blockposition.getX() + 0.5D);
            double d1 = this.player.locY - ((double) blockposition.getY() + 0.5D) + 1.5D;
            double d2 = this.player.locZ - ((double) blockposition.getZ() + 0.5D);
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d3 > 36.0D) {
                return;
            } else if (blockposition.getY() >= this.minecraftServer.getMaxBuildHeight()) {
                return;
            } else {
                if (packetplayinblockdig.c() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                    if (!this.minecraftServer.a(worldserver, blockposition, this.player) && worldserver.getWorldBorder().a(blockposition)) {
                        this.player.playerInteractManager.a(blockposition, packetplayinblockdig.b());
                    } else {
                        // CraftBukkit start - fire PlayerInteractEvent
                        CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockposition, packetplayinblockdig.b(), this.player.inventory.getItemInHand(), EnumHand.MAIN_HAND);
                        this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(worldserver, blockposition));
                        // Update any tile entity data for this block
                        TileEntity tileentity = worldserver.getTileEntity(blockposition);
                        if (tileentity != null) {
                            this.player.playerConnection.sendPacket(tileentity.getUpdatePacket());
                        }
                        // CraftBukkit end
                    }
                } else {
                    if (packetplayinblockdig.c() == PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) {
                        this.player.playerInteractManager.a(blockposition);
                    } else if (packetplayinblockdig.c() == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                        this.player.playerInteractManager.e();
                    }

                    if (worldserver.getType(blockposition).getMaterial() != Material.AIR) {
                        this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(worldserver, blockposition));
                    }
                }

                return;
            }

        default:
            throw new IllegalArgumentException("Invalid player action");
        }
        // CraftBukkit end
    }

    public void a(PacketPlayInUseItem packetplayinuseitem) {
        PlayerConnectionUtils.ensureMainThread(packetplayinuseitem, this, this.player.x());
        if (this.player.dead) return; // CraftBukkit
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
        EnumHand enumhand = packetplayinuseitem.c();
        ItemStack itemstack = this.player.b(enumhand);
        BlockPosition blockposition = packetplayinuseitem.a();
        EnumDirection enumdirection = packetplayinuseitem.b();

        this.player.resetIdleTimer();
        if (blockposition.getY() >= this.minecraftServer.getMaxBuildHeight() - 1 && (enumdirection == EnumDirection.UP || blockposition.getY() >= this.minecraftServer.getMaxBuildHeight())) {
            ChatMessage chatmessage = new ChatMessage("build.tooHigh", new Object[] { Integer.valueOf(this.minecraftServer.getMaxBuildHeight())});

            chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
            this.player.playerConnection.sendPacket(new PacketPlayOutChat(chatmessage));
        } else if (this.teleportPos == null && this.player.e((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D) < 64.0D && !this.minecraftServer.a(worldserver, blockposition, this.player) && worldserver.getWorldBorder().a(blockposition)) {
            // CraftBukkit start - Check if we can actually do something over this large a distance
            Location eyeLoc = this.getPlayer().getEyeLocation();
            double reachDistance = NumberConversions.square(eyeLoc.getX() - blockposition.getX()) + NumberConversions.square(eyeLoc.getY() - blockposition.getY()) + NumberConversions.square(eyeLoc.getZ() - blockposition.getZ());
            if (reachDistance > (this.getPlayer().getGameMode() == org.bukkit.GameMode.CREATIVE ? CREATIVE_PLACE_DISTANCE_SQUARED : SURVIVAL_PLACE_DISTANCE_SQUARED)) {
                return;
            }
            // CraftBukkit end
            this.player.playerInteractManager.a(this.player, worldserver, itemstack, enumhand, blockposition, enumdirection, packetplayinuseitem.d(), packetplayinuseitem.e(), packetplayinuseitem.f());
        }

        this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(worldserver, blockposition));
        this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(worldserver, blockposition.shift(enumdirection)));
        itemstack = this.player.b(enumhand);
        if (itemstack != null && itemstack.count == 0) {
            this.player.a(enumhand, (ItemStack) null);
            itemstack = null;
        }

    }

    // Spigot start - limit place/interactions
    private long lastPlace = -1;
    private int packets = 0;
    // Spigot end
    public void a(PacketPlayInBlockPlace packetplayinblockplace) {
        PlayerConnectionUtils.ensureMainThread(packetplayinblockplace, this, this.player.x());
        if (this.player.dead) return; // CraftBukkit
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
        EnumHand enumhand = packetplayinblockplace.a();
        ItemStack itemstack = this.player.b(enumhand);

        this.player.resetIdleTimer();
        // Spigot start
        boolean throttled = false;
        // Paper - Allow disabling interact limiter
        if (com.destroystokyo.paper.PaperConfig.useInteractLimiter && lastPlace != -1 && packetplayinblockplace.timestamp - lastPlace < 30 && packets++ >= 4) {
            throttled = true;
        } else if ( packetplayinblockplace.timestamp - lastPlace >= 30 || lastPlace == -1 )
        {
            lastPlace = packetplayinblockplace.timestamp;
            packets = 0;
        }
        // Spigot end
        if (!throttled && itemstack != null) { // Spigot - skip the event if throttled
            // CraftBukkit start
            // Raytrace to look for 'rogue armswings'
            float f1 = this.player.pitch;
            float f2 = this.player.yaw;
            double d0 = this.player.locX;
            double d1 = this.player.locY + (double) this.player.getHeadHeight();
            double d2 = this.player.locZ;
            Vec3D vec3d = new Vec3D(d0, d1, d2);

            float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
            float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
            float f5 = -MathHelper.cos(-f1 * 0.017453292F);
            float f6 = MathHelper.sin(-f1 * 0.017453292F);
            float f7 = f4 * f5;
            float f8 = f3 * f5;
            double d3 = player.playerInteractManager.getGameMode() == WorldSettings.EnumGamemode.CREATIVE ? 5.0D : 4.5D;
            Vec3D vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
            MovingObjectPosition movingobjectposition = this.player.world.rayTrace(vec3d, vec3d1, false);

            boolean cancelled = false;
            if (movingobjectposition == null || movingobjectposition.type != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
                org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.RIGHT_CLICK_AIR, itemstack, enumhand);
                cancelled = event.useItemInHand() == Event.Result.DENY;
            } else {
                if (player.playerInteractManager.firedInteract) {
                    player.playerInteractManager.firedInteract = false;
                    cancelled = player.playerInteractManager.interactResult;
                } else {
                    org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, movingobjectposition.a(), movingobjectposition.direction, itemstack, true, enumhand);
                    cancelled = event.useItemInHand() == Event.Result.DENY;
                }
            }

            if (!cancelled) {
                this.player.playerInteractManager.a(this.player, worldserver, itemstack, enumhand);
                itemstack = this.player.b(enumhand);
                if (itemstack != null && itemstack.count == 0) {
                    this.player.a(enumhand, (ItemStack) null);
                    itemstack = null;
                }
            }
            // CraftBukkit end
        }
    }

    public void a(PacketPlayInSpectate packetplayinspectate) {
        PlayerConnectionUtils.ensureMainThread(packetplayinspectate, this, this.player.x());
        if (this.player.isSpectator()) {
            Entity entity = null;
            WorldServer[] aworldserver = this.minecraftServer.worldServer;
            int i = aworldserver.length;

            // CraftBukkit - use the worlds array list
            for (WorldServer worldserver : minecraftServer.worlds) {

                if (worldserver != null) {
                    entity = packetplayinspectate.a(worldserver);
                    if (entity != null) {
                        break;
                    }
                }
            }

            if (entity != null) {
                this.player.setSpectatorTarget(this.player);
                this.player.stopRiding();

                /* CraftBukkit start - replace with bukkit handling for multi-world
                if (entity.world != this.player.world) {
                    WorldServer worldserver1 = this.player.x();
                    WorldServer worldserver2 = (WorldServer) entity.world;

                    this.player.dimension = entity.dimension;
                    this.sendPacket(new PacketPlayOutRespawn(this.player.dimension, worldserver1.getDifficulty(), worldserver1.getWorldData().getType(), this.player.playerInteractManager.getGameMode()));
                    this.minecraftServer.getPlayerList().f(this.player);
                    worldserver1.removeEntity(this.player);
                    this.player.dead = false;
                    this.player.setPositionRotation(entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
                    if (this.player.isAlive()) {
                        worldserver1.entityJoinedWorld(this.player, false);
                        worldserver2.addEntity(this.player);
                        worldserver2.entityJoinedWorld(this.player, false);
                    }

                    this.player.spawnIn(worldserver2);
                    this.minecraftServer.getPlayerList().a(this.player, worldserver1);
                    this.player.enderTeleportTo(entity.locX, entity.locY, entity.locZ);
                    this.player.playerInteractManager.a(worldserver2);
                    this.minecraftServer.getPlayerList().b(this.player, worldserver2);
                    this.minecraftServer.getPlayerList().updateClient(this.player);
                } else {
                    this.player.enderTeleportTo(entity.locX, entity.locY, entity.locZ);
                }
                */
                this.player.getBukkitEntity().teleport(entity.getBukkitEntity(), PlayerTeleportEvent.TeleportCause.SPECTATE);
                // CraftBukkit end
            }
        }

    }

    // CraftBukkit start
    public void a(PacketPlayInResourcePackStatus packetplayinresourcepackstatus) {
        //this.server.getPluginManager().callEvent(new PlayerResourcePackStatusEvent(getPlayer(), PlayerResourcePackStatusEvent.Status.values()[packetplayinresourcepackstatus.status.ordinal()])); // Paper - comment
        // Paper start
        PlayerConnectionUtils.ensureMainThread(packetplayinresourcepackstatus, this, this.player.x());
        final PlayerResourcePackStatusEvent.Status status = PlayerResourcePackStatusEvent.Status.values()[packetplayinresourcepackstatus.status.ordinal()];
        this.getPlayer().setResourcePackStatus(status, packetplayinresourcepackstatus.a);
        this.server.getPluginManager().callEvent(new PlayerResourcePackStatusEvent(getPlayer(), status, packetplayinresourcepackstatus.a));
        // paper end
    }
    // CraftBukkit end

    public void a(PacketPlayInBoatMove packetplayinboatmove) {
        PlayerConnectionUtils.ensureMainThread(packetplayinboatmove, this, this.player.x());
        Entity entity = this.player.bz();

        if (entity instanceof EntityBoat) {
            ((EntityBoat) entity).a(packetplayinboatmove.a(), packetplayinboatmove.b());
        }

    }

    public void a(IChatBaseComponent ichatbasecomponent) {
        // CraftBukkit start - Rarely it would send a disconnect line twice
        if (this.processedDisconnect) {
            return;
        } else {
            this.processedDisconnect = true;
        }
        // CraftBukkit end
        PlayerConnection.LOGGER.info(this.player.getName() + " lost connection: " + ichatbasecomponent.toPlainText()); // CraftBukkit: Don't toString().
        // CraftBukkit start - Replace vanilla quit message handling with our own.
        /*
        this.minecraftServer.aC();
        ChatMessage chatmessage = new ChatMessage("multiplayer.player.left", new Object[] { this.player.getScoreboardDisplayName()});

        chatmessage.getChatModifier().setColor(EnumChatFormat.YELLOW);
        this.minecraftServer.getPlayerList().sendMessage(chatmessage);
        */

        this.player.t();
        String quitMessage = this.minecraftServer.getPlayerList().disconnect(this.player);
        if ((quitMessage != null) && (quitMessage.length() > 0)) {
            this.minecraftServer.getPlayerList().sendMessage(CraftChatMessage.fromString(quitMessage));
        }
        // CraftBukkit end
        if (this.minecraftServer.R() && this.player.getName().equals(this.minecraftServer.Q())) {
            PlayerConnection.LOGGER.info("Stopping singleplayer server as player logged out");
            this.minecraftServer.safeShutdown();
        }

    }

    public void sendPacket(final Packet<?> packet) {
        if (packet instanceof PacketPlayOutChat) {
            PacketPlayOutChat packetplayoutchat = (PacketPlayOutChat) packet;
            EntityHuman.EnumChatVisibility entityhuman_enumchatvisibility = this.player.getChatFlags();

            if (entityhuman_enumchatvisibility == EntityHuman.EnumChatVisibility.HIDDEN) {
                return;
            }

            if (entityhuman_enumchatvisibility == EntityHuman.EnumChatVisibility.SYSTEM && !packetplayoutchat.b()) {
                return;
            }
        }

        // CraftBukkit start
        if (packet == null || this.processedDisconnect) { // Spigot
            return;
        } else if (packet instanceof PacketPlayOutSpawnPosition) {
            PacketPlayOutSpawnPosition packet6 = (PacketPlayOutSpawnPosition) packet;
            this.player.compassTarget = new Location(this.getPlayer().getWorld(), packet6.position.getX(), packet6.position.getY(), packet6.position.getZ());
        }
        // CraftBukkit end

        try {
            this.networkManager.sendPacket(packet);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Sending packet");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Packet being sent");

            crashreportsystemdetails.a("Packet class", new CrashReportCallable() {
                public String a() throws Exception {
                    return packet.getClass().getCanonicalName();
                }

                public Object call() throws Exception {
                    return this.a();
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    public void a(PacketPlayInHeldItemSlot packetplayinhelditemslot) {
        PlayerConnectionUtils.ensureMainThread(packetplayinhelditemslot, this, this.player.x());
        if (this.player.dead) return; // CraftBukkit
        if (packetplayinhelditemslot.a() >= 0 && packetplayinhelditemslot.a() < PlayerInventory.getHotbarSize()) {
            PlayerItemHeldEvent event = new PlayerItemHeldEvent(this.getPlayer(), this.player.inventory.itemInHandIndex, packetplayinhelditemslot.a());
            this.server.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                this.sendPacket(new PacketPlayOutHeldItemSlot(this.player.inventory.itemInHandIndex));
                this.player.resetIdleTimer();
                return;
            }
            // CraftBukkit end
            this.player.inventory.itemInHandIndex = packetplayinhelditemslot.a();
            this.player.resetIdleTimer();
        } else {
            PlayerConnection.LOGGER.warn(this.player.getName() + " tried to set an invalid carried item");
            this.disconnect("Invalid hotbar selection (Hacking?)"); // CraftBukkit //Spigot "Nope" -> Descriptive reason
        }
    }

    public void a(PacketPlayInChat packetplayinchat) {
        // CraftBukkit start - async chat
        boolean isSync = packetplayinchat.a().startsWith("/");
        if (packetplayinchat.a().startsWith("/")) {
            PlayerConnectionUtils.ensureMainThread(packetplayinchat, this, this.player.x());
        }
        // CraftBukkit end
        if (this.player.dead || this.player.getChatFlags() == EntityHuman.EnumChatVisibility.HIDDEN) { // CraftBukkit - dead men tell no tales
            ChatMessage chatmessage = new ChatMessage("chat.cannotSend", new Object[0]);

            chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
            this.sendPacket(new PacketPlayOutChat(chatmessage));
        } else {
            this.player.resetIdleTimer();
            String s = packetplayinchat.a();

            s = StringUtils.normalizeSpace(s);

            for (int i = 0; i < s.length(); ++i) {
                if (!SharedConstants.isAllowedChatCharacter(s.charAt(i))) {
                    // CraftBukkit start - threadsafety
                    if (!isSync) {
                        Waitable waitable = new Waitable() {
                            @Override
                            protected Object evaluate() {
                                PlayerConnection.this.disconnect("Illegal characters in chat");
                                return null;
                            }
                        };

                        this.minecraftServer.processQueue.add(waitable);

                        try {
                            waitable.get();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        this.disconnect("Illegal characters in chat");
                    }
                    // CraftBukkit end
                    return;
                }
            }

            // CraftBukkit start
            if (isSync) {
                try {
                    this.minecraftServer.server.playerCommandState = true;
                    this.handleCommand(s);
                } finally {
                    this.minecraftServer.server.playerCommandState = false;
                }
            } else if (s.isEmpty()) {
                LOGGER.warn(this.player.getName() + " tried to send an empty message");
            } else if (getPlayer().isConversing()) {
                // Spigot start
                final String message = s;
                this.minecraftServer.processQueue.add( new Waitable()
                {
                    @Override
                    protected Object evaluate()
                    {
                        getPlayer().acceptConversationInput( message );
                        return null;
                    }
                } );
                // Spigot end
            } else if (this.player.getChatFlags() == EntityHuman.EnumChatVisibility.SYSTEM) { // Re-add "Command Only" flag check
                ChatMessage chatmessage = new ChatMessage("chat.cannotSend", new Object[0]);

                chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
                this.sendPacket(new PacketPlayOutChat(chatmessage));
            } else if (true) {
                this.chat(s, true);
                // CraftBukkit end - the below is for reference. :)
            } else {
                ChatMessage chatmessage1 = new ChatMessage("chat.type.text", new Object[] { this.player.getScoreboardDisplayName(), s});

                this.minecraftServer.getPlayerList().sendMessage(chatmessage1, false);
            }

            // Spigot start - spam exclusions
            boolean counted = true;
            for ( String exclude : org.spigotmc.SpigotConfig.spamExclusions )
            {
                if ( exclude != null && s.startsWith( exclude ) )
                {
                    counted = false;
                    break;
                }
            }
            // Spigot end
            // CraftBukkit start - replaced with thread safe throttle
            // this.chatThrottle += 20;
            if (counted && chatSpamField.addAndGet(this, 20) > 200 && !this.minecraftServer.getPlayerList().isOp(this.player.getProfile())) { // Spigot
                if (!isSync) {
                    Waitable waitable = new Waitable() {
                        @Override
                        protected Object evaluate() {
                            PlayerConnection.this.disconnect("disconnect.spam");
                            return null;
                        }
                    };

                    this.minecraftServer.processQueue.add(waitable);

                    try {
                        waitable.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    this.disconnect("disconnect.spam");
                }
                // CraftBukkit end
            }

        }
    }

    // CraftBukkit start - add method
    public void chat(String s, boolean async) {
        if (s.isEmpty() || this.player.getChatFlags() == EntityHuman.EnumChatVisibility.HIDDEN) {
            return;
        }

        if (!async && s.startsWith("/")) {
            // Paper Start
            if (!org.bukkit.Bukkit.isPrimaryThread()) {
                final String fCommandLine = s;
                MinecraftServer.LOGGER.log(org.apache.logging.log4j.Level.ERROR, "Command Dispatched Async: " + fCommandLine);
                MinecraftServer.LOGGER.log(org.apache.logging.log4j.Level.ERROR, "Please notify author of plugin causing this execution to fix this bug! see: http://bit.ly/1oSiM6C", new Throwable());
                Waitable wait = new Waitable() {
                    @Override
                    protected Object evaluate() {
                        chat(fCommandLine, false);
                        return null;
                    }
                };
                minecraftServer.processQueue.add(wait);
                try {
                    wait.get();
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it on!
                } catch (Exception e) {
                    throw new RuntimeException("Exception processing chat command", e.getCause());
                }
            }
            // Paper End
            this.handleCommand(s);
        } else if (this.player.getChatFlags() == EntityHuman.EnumChatVisibility.SYSTEM) {
            // Do nothing, this is coming from a plugin
        } else {
            Player player = this.getPlayer();
            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet(minecraftServer));
            this.server.getPluginManager().callEvent(event);

            if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
                // Evil plugins still listening to deprecated event
                final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(), event.getRecipients());
                queueEvent.setCancelled(event.isCancelled());
                Waitable waitable = new Waitable() {
                    @Override
                    protected Object evaluate() {
                        org.bukkit.Bukkit.getPluginManager().callEvent(queueEvent);

                        if (queueEvent.isCancelled()) {
                            return null;
                        }

                        String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                        PlayerConnection.this.minecraftServer.console.sendMessage(message);
                        if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy()) {
                            for (Object player : PlayerConnection.this.minecraftServer.getPlayerList().players) {
                                ((EntityPlayer) player).sendMessage(CraftChatMessage.fromString(message));
                            }
                        } else {
                            for (Player player : queueEvent.getRecipients()) {
                                player.sendMessage(message);
                            }
                        }
                        return null;
                    }};
                if (async) {
                    minecraftServer.processQueue.add(waitable);
                } else {
                    waitable.run();
                }
                try {
                    waitable.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it on!
                } catch (ExecutionException e) {
                    throw new RuntimeException("Exception processing chat event", e.getCause());
                }
            } else {
                if (event.isCancelled()) {
                    return;
                }

                // Paper Start - (Meh) Support for vanilla world scoreboard name coloring
                String displayName = event.getPlayer().getDisplayName();
                if (this.player.getWorld().paperConfig.useVanillaScoreboardColoring) {
                    displayName = ScoreboardTeam.getPlayerDisplayName(this.player.aO(), player.getDisplayName());
                }

                s = String.format(event.getFormat(), displayName, event.getMessage());
                // Paper end
                minecraftServer.console.sendMessage(s);
                if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
                    for (Object recipient : minecraftServer.getPlayerList().players) {
                        ((EntityPlayer) recipient).sendMessage(CraftChatMessage.fromString(s));
                    }
                } else {
                    for (Player recipient : event.getRecipients()) {
                        recipient.sendMessage(s);
                    }
                }
            }
        }
    }
    // CraftBukkit end

   private void handleCommand(String s) {
       MinecraftTimings.playerCommandTimer.startTiming(); // Paper
       // CraftBukkit start - whole method
        if ( org.spigotmc.SpigotConfig.logCommands ) // Spigot
        this.LOGGER.info(this.player.getName() + " issued server command: " + s);

        CraftPlayer player = this.getPlayer();

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, s, new LazyPlayerSet(minecraftServer));
        this.server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            MinecraftTimings.playerCommandTimer.stopTiming(); // Paper
            return;
        }

        try {
            if (this.server.dispatchCommand(event.getPlayer(), event.getMessage().substring(1))) {
                MinecraftTimings.playerCommandTimer.stopTiming(); // Paper
                return;
            }
        } catch (org.bukkit.command.CommandException ex) {
            player.sendMessage(org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
            java.util.logging.Logger.getLogger(PlayerConnection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            MinecraftTimings.playerCommandTimer.stopTiming(); // Paper
            return;
        }
        MinecraftTimings.playerCommandTimer.stopTiming(); // Paper
        // this.minecraftServer.getCommandHandler().a(this.player, s);
        // CraftBukkit end
    }

    public void a(PacketPlayInArmAnimation packetplayinarmanimation) {
        PlayerConnectionUtils.ensureMainThread(packetplayinarmanimation, this, this.player.x());
        if (this.player.dead) return; // CraftBukkit
        this.player.resetIdleTimer();
        // CraftBukkit start - Raytrace to look for 'rogue armswings'
        float f1 = this.player.pitch;
        float f2 = this.player.yaw;
        double d0 = this.player.locX;
        double d1 = this.player.locY + (double) this.player.getHeadHeight();
        double d2 = this.player.locZ;
        Vec3D vec3d = new Vec3D(d0, d1, d2);

        float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = player.playerInteractManager.getGameMode() == WorldSettings.EnumGamemode.CREATIVE ? 5.0D : 4.5D;
        Vec3D vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
        MovingObjectPosition movingobjectposition = this.player.world.rayTrace(vec3d, vec3d1, false);

        if (movingobjectposition == null || movingobjectposition.type != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR, this.player.inventory.getItemInHand(), EnumHand.MAIN_HAND);
        }

        // Arm swing animation
        PlayerAnimationEvent event = new PlayerAnimationEvent(this.getPlayer());
        this.server.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;
        // CraftBukkit end
        this.player.a(packetplayinarmanimation.a());
    }

    public void a(PacketPlayInEntityAction packetplayinentityaction) {
        PlayerConnectionUtils.ensureMainThread(packetplayinentityaction, this, this.player.x());
        // CraftBukkit start
        if (this.player.dead) return;
        switch (packetplayinentityaction.b()) {
            case START_SNEAKING:
            case STOP_SNEAKING:
                PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getPlayer(), packetplayinentityaction.b() == PacketPlayInEntityAction.EnumPlayerAction.START_SNEAKING);
                this.server.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return;
                }
                break;
            case START_SPRINTING:
            case STOP_SPRINTING:
                PlayerToggleSprintEvent e2 = new PlayerToggleSprintEvent(this.getPlayer(), packetplayinentityaction.b() == PacketPlayInEntityAction.EnumPlayerAction.START_SPRINTING);
                this.server.getPluginManager().callEvent(e2);

                if (e2.isCancelled()) {
                    return;
                }
                break;
        }
        // CraftBukkit end
        this.player.resetIdleTimer();
        IJumpable ijumpable;

        switch (PlayerConnection.SyntheticClass_1.b[packetplayinentityaction.b().ordinal()]) {
        case 1:
            this.player.setSneaking(true);
            break;

        case 2:
            this.player.setSneaking(false);
            break;

        case 3:
            this.player.setSprinting(true);
            break;

        case 4:
            this.player.setSprinting(false);
            break;

        case 5:
            this.player.a(false, true, true);
            this.teleportPos = new Vec3D(this.player.locX, this.player.locY, this.player.locZ);
            break;

        case 6:
            if (this.player.bz() instanceof IJumpable) {
                ijumpable = (IJumpable) this.player.bz();
                int i = packetplayinentityaction.c();

                if (ijumpable.b() && i > 0) {
                    ijumpable.b(i);
                }
            }
            break;

        case 7:
            if (this.player.bz() instanceof IJumpable) {
                ijumpable = (IJumpable) this.player.bz();
                ijumpable.r_();
            }
            break;

        case 8:
            if (this.player.bz() instanceof EntityHorse) {
                ((EntityHorse) this.player.bz()).f((EntityHuman) this.player);
            }
            break;

        case 9:
            if (!this.player.onGround && this.player.motY < 0.0D && !this.player.cC() && !this.player.isInWater()) {
                ItemStack itemstack = this.player.getEquipment(EnumItemSlot.CHEST);

                if (itemstack != null && itemstack.getItem() == Items.cR && ItemElytra.d(itemstack)) {
                    this.player.M();
                }
            } else {
                this.player.N();
            }
            break;

        default:
            throw new IllegalArgumentException("Invalid client command!");
        }

    }

    public void a(PacketPlayInUseEntity packetplayinuseentity) {
        PlayerConnectionUtils.ensureMainThread(packetplayinuseentity, this, this.player.x());
        if (this.player.dead) return; // CraftBukkit
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
        Entity entity = packetplayinuseentity.a((World) worldserver);
        // Spigot Start
        if ( entity == player && !player.isSpectator() )
        {
            disconnect( "Cannot interact with self!" );
            return;
        }
        // Spigot End

        this.player.resetIdleTimer();
        if (entity != null) {
            boolean flag = this.player.hasLineOfSight(entity);
            double d0 = 36.0D;

            if (!flag) {
                d0 = 9.0D;
            }

            if (this.player.h(entity) < d0) {
                EnumHand enumhand;
                ItemStack itemstack;

                ItemStack itemInHand = this.player.b(packetplayinuseentity.b() == null ? EnumHand.MAIN_HAND : packetplayinuseentity.b()); // CraftBukkit

                if (packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT
                        || packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
                    // CraftBukkit start
                    boolean triggerLeashUpdate = itemInHand != null && itemInHand.getItem() == Items.LEAD && entity instanceof EntityInsentient;
                    Item origItem = this.player.inventory.getItemInHand() == null ? null : this.player.inventory.getItemInHand().getItem();
                    PlayerInteractEntityEvent event;
                    if (packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT) {
                        event = new PlayerInteractEntityEvent((Player) this.getPlayer(), entity.getBukkitEntity(), (packetplayinuseentity.b() == EnumHand.OFF_HAND) ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
                    } else {
                        Vec3D target = packetplayinuseentity.c();
                        event = new PlayerInteractAtEntityEvent((Player) this.getPlayer(), entity.getBukkitEntity(), new org.bukkit.util.Vector(target.x, target.y, target.z), (packetplayinuseentity.b() == EnumHand.OFF_HAND) ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
                    }
                    this.server.getPluginManager().callEvent(event);

                    if (triggerLeashUpdate && (event.isCancelled() || this.player.inventory.getItemInHand() == null || this.player.inventory.getItemInHand().getItem() != Items.LEAD)) {
                        // Refresh the current leash state
                        this.sendPacket(new PacketPlayOutAttachEntity(entity, ((EntityInsentient) entity).getLeashHolder()));
                    }

                    if (event.isCancelled() || this.player.inventory.getItemInHand() == null || this.player.inventory.getItemInHand().getItem() != origItem) {
                        // Refresh the current entity metadata
                        this.sendPacket(new PacketPlayOutEntityMetadata(entity.getId(), entity.datawatcher, true));
                    }

                    if (event.isCancelled()) {
                        return;
                    }
                    // CraftBukkit end
                }

                if (packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT) {
                    enumhand = packetplayinuseentity.b();
                    itemstack = this.player.b(enumhand);
                    this.player.a(entity, itemstack, enumhand);

                    // CraftBukkit start
                    if (itemInHand != null && itemInHand.count <= -1) {
                        this.player.updateInventory(this.player.activeContainer);
                    }
                    // CraftBukkit end
                } else if (packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
                    enumhand = packetplayinuseentity.b();
                    itemstack = this.player.b(enumhand);
                    entity.a((EntityHuman) this.player, packetplayinuseentity.c(), itemstack, enumhand);

                    // CraftBukkit start
                    if (itemInHand != null && itemInHand.count <= -1) {
                        this.player.updateInventory(this.player.activeContainer);
                    }
                    // CraftBukkit end
                } else if (packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {
                    if (entity instanceof EntityItem || entity instanceof EntityExperienceOrb || entity instanceof EntityArrow || (entity == this.player && !player.isSpectator())) { // CraftBukkit
                        this.disconnect("Attempting to attack an invalid entity");
                        this.minecraftServer.warning("Player " + this.player.getName() + " tried to attack an invalid entity");
                        return;
                    }

                    this.player.attack(entity);

                    // CraftBukkit start
                    if (itemInHand != null && itemInHand.count <= -1) {
                        this.player.updateInventory(this.player.activeContainer);
                    }
                    // CraftBukkit end
                }
            }
        }
        // Paper start - fire event
        else {
            this.server.getPluginManager().callEvent(new com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent(
                this.getPlayer(),
                packetplayinuseentity.getEntityId(),
                packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK,
                packetplayinuseentity.b() == EnumHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND
            ));
        }
        // Paper end

    }

    public void a(PacketPlayInClientCommand packetplayinclientcommand) {
        PlayerConnectionUtils.ensureMainThread(packetplayinclientcommand, this, this.player.x());
        this.player.resetIdleTimer();
        PacketPlayInClientCommand.EnumClientCommand packetplayinclientcommand_enumclientcommand = packetplayinclientcommand.a();

        switch (PlayerConnection.SyntheticClass_1.c[packetplayinclientcommand_enumclientcommand.ordinal()]) {
        case 1:
            if (this.player.viewingCredits) {
                this.player.viewingCredits = false;
                // this.player = this.minecraftServer.getPlayerList().moveToWorld(this.player, 0, true);
                this.minecraftServer.getPlayerList().changeDimension(this.player, 0, PlayerTeleportEvent.TeleportCause.END_PORTAL); // CraftBukkit - reroute logic through custom portal management
            } else {
                if (this.player.getHealth() > 0.0F) {
                    return;
                }

                this.player = this.minecraftServer.getPlayerList().moveToWorld(this.player, 0, false);
                if (this.minecraftServer.isHardcore()) {
                    this.player.a(WorldSettings.EnumGamemode.SPECTATOR);
                    this.player.x().getGameRules().set("spectatorsGenerateChunks", "false");
                }
            }
            break;

        case 2:
            this.player.getStatisticManager().a(this.player);
            break;

        case 3:
            this.player.b((Statistic) AchievementList.f);
        }

    }

    public void a(PacketPlayInCloseWindow packetplayinclosewindow) {
        PlayerConnectionUtils.ensureMainThread(packetplayinclosewindow, this, this.player.x());

        if (this.player.dead) return; // CraftBukkit
        CraftEventFactory.handleInventoryCloseEvent(this.player); // CraftBukkit

        this.player.s();
    }

    public void a(PacketPlayInWindowClick packetplayinwindowclick) {
        PlayerConnectionUtils.ensureMainThread(packetplayinwindowclick, this, this.player.x());
        if (this.player.dead) return; // CraftBukkit
        this.player.resetIdleTimer();
        if (this.player.activeContainer.windowId == packetplayinwindowclick.a() && this.player.activeContainer.c(this.player)) {
            boolean cancelled = this.player.isSpectator(); // CraftBukkit - see below if
            if (false/*this.player.isSpectator()*/) { // CraftBukkit
                ArrayList arraylist = Lists.newArrayList();

                for (int i = 0; i < this.player.activeContainer.c.size(); ++i) {
                    arraylist.add(((Slot) this.player.activeContainer.c.get(i)).getItem());
                }

                this.player.a(this.player.activeContainer, (List) arraylist);
            } else {
                // CraftBukkit start - Call InventoryClickEvent
                if (packetplayinwindowclick.b() < -1 && packetplayinwindowclick.b() != -999) {
                    return;
                }

                InventoryView inventory = this.player.activeContainer.getBukkitView();
                SlotType type = CraftInventoryView.getSlotType(inventory, packetplayinwindowclick.b());

                InventoryClickEvent event;
                ClickType click = ClickType.UNKNOWN;
                InventoryAction action = InventoryAction.UNKNOWN;

                ItemStack itemstack = null;

                switch (packetplayinwindowclick.f()) {
                    case PICKUP:
                        if (packetplayinwindowclick.c() == 0) {
                            click = ClickType.LEFT;
                        } else if (packetplayinwindowclick.c() == 1) {
                            click = ClickType.RIGHT;
                        }
                        if (packetplayinwindowclick.c() == 0 || packetplayinwindowclick.c() == 1) {
                            action = InventoryAction.NOTHING; // Don't want to repeat ourselves
                            if (packetplayinwindowclick.b() == -999) {
                                if (player.inventory.getCarried() != null) {
                                    action = packetplayinwindowclick.c() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
                                }
                            } else if (packetplayinwindowclick.b() < 0)  {
                                action = InventoryAction.NOTHING;
                            } else {
                                Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                                if (slot != null) {
                                    ItemStack clickedItem = slot.getItem();
                                    ItemStack cursor = player.inventory.getCarried();
                                    if (clickedItem == null) {
                                        if (cursor != null) {
                                            action = packetplayinwindowclick.c() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
                                        }
                                    } else if (slot.isAllowed(player)) {
                                        if (cursor == null) {
                                            action = packetplayinwindowclick.c() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                                        } else if (slot.isAllowed(cursor)) {
                                            if (clickedItem.doMaterialsMatch(cursor) && ItemStack.equals(clickedItem, cursor)) {
                                                int toPlace = packetplayinwindowclick.c() == 0 ? cursor.count : 1;
                                                toPlace = Math.min(toPlace, clickedItem.getMaxStackSize() - clickedItem.count);
                                                toPlace = Math.min(toPlace, slot.inventory.getMaxStackSize() - clickedItem.count);
                                                if (toPlace == 1) {
                                                    action = InventoryAction.PLACE_ONE;
                                                } else if (toPlace == cursor.count) {
                                                    action = InventoryAction.PLACE_ALL;
                                                } else if (toPlace < 0) {
                                                    action = toPlace != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE; // this happens with oversized stacks
                                                } else if (toPlace != 0) {
                                                    action = InventoryAction.PLACE_SOME;
                                                }
                                            } else if (cursor.count <= slot.getMaxStackSize()) {
                                                action = InventoryAction.SWAP_WITH_CURSOR;
                                            }
                                        } else if (cursor.getItem() == clickedItem.getItem() && (!cursor.usesData() || cursor.getData() == clickedItem.getData()) && ItemStack.equals(cursor, clickedItem)) {
                                            if (clickedItem.count >= 0) {
                                                if (clickedItem.count + cursor.count <= cursor.getMaxStackSize()) {
                                                    // As of 1.5, this is result slots only
                                                    action = InventoryAction.PICKUP_ALL;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    // TODO check on updates
                    case QUICK_MOVE:
                        if (packetplayinwindowclick.c() == 0) {
                            click = ClickType.SHIFT_LEFT;
                        } else if (packetplayinwindowclick.c() == 1) {
                            click = ClickType.SHIFT_RIGHT;
                        }
                        if (packetplayinwindowclick.c() == 0 || packetplayinwindowclick.c() == 1) {
                            if (packetplayinwindowclick.b() < 0) {
                                action = InventoryAction.NOTHING;
                            } else {
                                Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                                if (slot != null && slot.isAllowed(this.player) && slot.hasItem()) {
                                    action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
                                } else {
                                    action = InventoryAction.NOTHING;
                                }
                            }
                        }
                        break;
                    case SWAP:
                        if (packetplayinwindowclick.c() >= 0 && packetplayinwindowclick.c() < 9) {
                            click = ClickType.NUMBER_KEY;
                            Slot clickedSlot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                            if (clickedSlot.isAllowed(player)) {
                                ItemStack hotbar = this.player.inventory.getItem(packetplayinwindowclick.c());
                                boolean canCleanSwap = hotbar == null || (clickedSlot.inventory == player.inventory && clickedSlot.isAllowed(hotbar)); // the slot will accept the hotbar item
                                if (clickedSlot.hasItem()) {
                                    if (canCleanSwap) {
                                        action = InventoryAction.HOTBAR_SWAP;
                                    } else {
                                        int firstEmptySlot = player.inventory.getFirstEmptySlotIndex();
                                        if (firstEmptySlot > -1) {
                                            action = InventoryAction.HOTBAR_MOVE_AND_READD;
                                        } else {
                                            action = InventoryAction.NOTHING; // This is not sane! Mojang: You should test for other slots of same type
                                        }
                                    }
                                } else if (!clickedSlot.hasItem() && hotbar != null && clickedSlot.isAllowed(hotbar)) {
                                    action = InventoryAction.HOTBAR_SWAP;
                                } else {
                                    action = InventoryAction.NOTHING;
                                }
                            } else {
                                action = InventoryAction.NOTHING;
                            }
                        }
                        break;
                    case CLONE:
                        if (packetplayinwindowclick.c() == 2) {
                            click = ClickType.MIDDLE;
                            if (packetplayinwindowclick.b() == -999) {
                                action = InventoryAction.NOTHING;
                            } else {
                                Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                                if (slot != null && slot.hasItem() && player.abilities.canInstantlyBuild && player.inventory.getCarried() == null) {
                                    action = InventoryAction.CLONE_STACK;
                                } else {
                                    action = InventoryAction.NOTHING;
                                }
                            }
                        } else {
                            click = ClickType.UNKNOWN;
                            action = InventoryAction.UNKNOWN;
                        }
                        break;
                    case THROW:
                        if (packetplayinwindowclick.b() >= 0) {
                            if (packetplayinwindowclick.c() == 0) {
                                click = ClickType.DROP;
                                Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                                if (slot != null && slot.hasItem() && slot.isAllowed(player) && slot.getItem() != null && slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
                                    action = InventoryAction.DROP_ONE_SLOT;
                                } else {
                                    action = InventoryAction.NOTHING;
                                }
                            } else if (packetplayinwindowclick.c() == 1) {
                                click = ClickType.CONTROL_DROP;
                                Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                                if (slot != null && slot.hasItem() && slot.isAllowed(player) && slot.getItem() != null && slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
                                    action = InventoryAction.DROP_ALL_SLOT;
                                } else {
                                    action = InventoryAction.NOTHING;
                                }
                            }
                        } else {
                            // Sane default (because this happens when they are holding nothing. Don't ask why.)
                            click = ClickType.LEFT;
                            if (packetplayinwindowclick.c() == 1) {
                                click = ClickType.RIGHT;
                            }
                            action = InventoryAction.NOTHING;
                        }
                        break;
                    case QUICK_CRAFT:
                        itemstack = this.player.activeContainer.a(packetplayinwindowclick.b(), packetplayinwindowclick.c(), packetplayinwindowclick.f(), this.player);
                        break;
                    case PICKUP_ALL:
                        click = ClickType.DOUBLE_CLICK;
                        action = InventoryAction.NOTHING;
                        if (packetplayinwindowclick.b() >= 0 && this.player.inventory.getCarried() != null) {
                            ItemStack cursor = this.player.inventory.getCarried();
                            action = InventoryAction.NOTHING;
                            // Quick check for if we have any of the item
                            if (inventory.getTopInventory().contains(org.bukkit.Material.getMaterial(Item.getId(cursor.getItem()))) || inventory.getBottomInventory().contains(org.bukkit.Material.getMaterial(Item.getId(cursor.getItem())))) {
                                action = InventoryAction.COLLECT_TO_CURSOR;
                            }
                        }
                        break;
                    default:
                        break;
                }

                if (packetplayinwindowclick.f() != InventoryClickType.QUICK_CRAFT) {
                    if (click == ClickType.NUMBER_KEY) {
                        event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.b(), click, action, packetplayinwindowclick.c());
                    } else {
                        event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.b(), click, action);
                    }

                    org.bukkit.inventory.Inventory top = inventory.getTopInventory();
                    if (packetplayinwindowclick.b() == 0 && top instanceof CraftingInventory) {
                        org.bukkit.inventory.Recipe recipe = ((CraftingInventory) top).getRecipe();
                        if (recipe != null) {
                            if (click == ClickType.NUMBER_KEY) {
                                event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.b(), click, action, packetplayinwindowclick.c());
                            } else {
                                event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.b(), click, action);
                            }
                        }
                    }

                    event.setCancelled(cancelled);
                    server.getPluginManager().callEvent(event);

                    switch (event.getResult()) {
                        case ALLOW:
                        case DEFAULT:
                            itemstack = this.player.activeContainer.a(packetplayinwindowclick.b(), packetplayinwindowclick.c(), packetplayinwindowclick.f(), this.player);
                            break;
                        case DENY:
                            /* Needs enum constructor in InventoryAction
                            if (action.modifiesOtherSlots()) {

                            } else {
                                if (action.modifiesCursor()) {
                                    this.player.playerConnection.sendPacket(new Packet103SetSlot(-1, -1, this.player.inventory.getCarried()));
                                }
                                if (action.modifiesClicked()) {
                                    this.player.playerConnection.sendPacket(new Packet103SetSlot(this.player.activeContainer.windowId, packet102windowclick.slot, this.player.activeContainer.getSlot(packet102windowclick.slot).getItem()));
                                }
                            }*/
                            switch (action) {
                                // Modified other slots
                                case PICKUP_ALL:
                                case MOVE_TO_OTHER_INVENTORY:
                                case HOTBAR_MOVE_AND_READD:
                                case HOTBAR_SWAP:
                                case COLLECT_TO_CURSOR:
                                case UNKNOWN:
                                    this.player.updateInventory(this.player.activeContainer);
                                    break;
                                // Modified cursor and clicked
                                case PICKUP_SOME:
                                case PICKUP_HALF:
                                case PICKUP_ONE:
                                case PLACE_ALL:
                                case PLACE_SOME:
                                case PLACE_ONE:
                                case SWAP_WITH_CURSOR:
                                    this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.player.inventory.getCarried()));
                                    this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(this.player.activeContainer.windowId, packetplayinwindowclick.b(), this.player.activeContainer.getSlot(packetplayinwindowclick.b()).getItem()));
                                    break;
                                // Modified clicked only
                                case DROP_ALL_SLOT:
                                case DROP_ONE_SLOT:
                                    this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(this.player.activeContainer.windowId, packetplayinwindowclick.b(), this.player.activeContainer.getSlot(packetplayinwindowclick.b()).getItem()));
                                    break;
                                // Modified cursor only
                                case DROP_ALL_CURSOR:
                                case DROP_ONE_CURSOR:
                                case CLONE_STACK:
                                    this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.player.inventory.getCarried()));
                                    break;
                                // Nothing
                                case NOTHING:
                                    break;
                            }
                            return;
                    }

                    if (event instanceof CraftItemEvent) {
                        // Need to update the inventory on crafting to
                        // correctly support custom recipes
                        player.updateInventory(player.activeContainer);
                    }
                }
                // CraftBukkit end
                if (ItemStack.matches(packetplayinwindowclick.e(), itemstack)) {
                    this.player.playerConnection.sendPacket(new PacketPlayOutTransaction(packetplayinwindowclick.a(), packetplayinwindowclick.d(), true));
                    this.player.f = true;
                    this.player.activeContainer.b();
                    this.player.broadcastCarriedItem();
                    this.player.f = false;
                } else {
                    this.k.a(this.player.activeContainer.windowId, Short.valueOf(packetplayinwindowclick.d()));
                    this.player.playerConnection.sendPacket(new PacketPlayOutTransaction(packetplayinwindowclick.a(), packetplayinwindowclick.d(), false));
                    this.player.activeContainer.a(this.player, false);
                    ArrayList arraylist1 = Lists.newArrayList();

                    for (int j = 0; j < this.player.activeContainer.c.size(); ++j) {
                        ItemStack itemstack1 = ((Slot) this.player.activeContainer.c.get(j)).getItem();
                        ItemStack itemstack2 = itemstack1 != null && itemstack1.count > 0 ? itemstack1 : null;

                        arraylist1.add(itemstack2);
                    }

                    this.player.a(this.player.activeContainer, (List) arraylist1);
                }
            }
        }

    }

    public void a(PacketPlayInEnchantItem packetplayinenchantitem) {
        PlayerConnectionUtils.ensureMainThread(packetplayinenchantitem, this, this.player.x());
        if (this.player.dead) return; // CraftBukkit
        this.player.resetIdleTimer();
        if (this.player.activeContainer.windowId == packetplayinenchantitem.a() && this.player.activeContainer.c(this.player) && !this.player.isSpectator()) {
            this.player.activeContainer.a(this.player, packetplayinenchantitem.b());
            this.player.activeContainer.b();
        }

    }

    public void a(PacketPlayInSetCreativeSlot packetplayinsetcreativeslot) {
        PlayerConnectionUtils.ensureMainThread(packetplayinsetcreativeslot, this, this.player.x());
        if (this.player.playerInteractManager.isCreative()) {
            boolean flag = packetplayinsetcreativeslot.a() < 0;
            ItemStack itemstack = packetplayinsetcreativeslot.getItemStack();

            if (itemstack != null && itemstack.hasTag() && itemstack.getTag().hasKeyOfType("BlockEntityTag", 10)) {
                NBTTagCompound nbttagcompound = itemstack.getTag().getCompound("BlockEntityTag");

                if (nbttagcompound.hasKey("x") && nbttagcompound.hasKey("y") && nbttagcompound.hasKey("z")) {
                    BlockPosition blockposition = new BlockPosition(nbttagcompound.getInt("x"), nbttagcompound.getInt("y"), nbttagcompound.getInt("z"));
                    TileEntity tileentity = this.player.world.getTileEntity(blockposition);

                    if (tileentity != null) {
                        NBTTagCompound nbttagcompound1 = tileentity.save(new NBTTagCompound());

                        nbttagcompound1.remove("x");
                        nbttagcompound1.remove("y");
                        nbttagcompound1.remove("z");
                        itemstack.a("BlockEntityTag", (NBTBase) nbttagcompound1);
                    }
                }
            }

            boolean flag1 = packetplayinsetcreativeslot.a() >= 1 && packetplayinsetcreativeslot.a() <= 45;
            // CraftBukkit - Add invalidItems check
            boolean flag2 = itemstack == null || itemstack.getItem() != null && (!invalidItems.contains(Item.getId(itemstack.getItem())) || !org.spigotmc.SpigotConfig.filterCreativeItems); // Spigot
            boolean flag3 = itemstack == null || itemstack.getData() >= 0 && itemstack.count <= 64 && itemstack.count > 0;
            // CraftBukkit start - Call click event
            if (flag || (flag1 && !ItemStack.matches(this.player.defaultContainer.getSlot(packetplayinsetcreativeslot.a()).getItem(), packetplayinsetcreativeslot.getItemStack()))) { // Insist on valid slot

                org.bukkit.entity.HumanEntity player = this.player.getBukkitEntity();
                InventoryView inventory = new CraftInventoryView(player, player.getInventory(), this.player.defaultContainer);
                org.bukkit.inventory.ItemStack item = CraftItemStack.asBukkitCopy(packetplayinsetcreativeslot.getItemStack());

                SlotType type = SlotType.QUICKBAR;
                if (flag) {
                    type = SlotType.OUTSIDE;
                } else if (packetplayinsetcreativeslot.a() < 36) {
                    if (packetplayinsetcreativeslot.a() >= 5 && packetplayinsetcreativeslot.a() < 9) {
                        type = SlotType.ARMOR;
                    } else {
                        type = SlotType.CONTAINER;
                    }
                }
                InventoryCreativeEvent event = new InventoryCreativeEvent(inventory, type, flag ? -999 : packetplayinsetcreativeslot.a(), item);
                server.getPluginManager().callEvent(event);

                itemstack = CraftItemStack.asNMSCopy(event.getCursor());

                switch (event.getResult()) {
                case ALLOW:
                    // Plugin cleared the id / stacksize checks
                    flag2 = flag3 = true;
                    break;
                case DEFAULT:
                    break;
                case DENY:
                    // Reset the slot
                    if (packetplayinsetcreativeslot.a() >= 0) {
                        this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(this.player.defaultContainer.windowId, packetplayinsetcreativeslot.a(), this.player.defaultContainer.getSlot(packetplayinsetcreativeslot.a()).getItem()));
                        this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, null));
                    }
                    return;
                }
            }
            // CraftBukkit end

            if (flag1 && flag2 && flag3) {
                if (itemstack == null) {
                    this.player.defaultContainer.setItem(packetplayinsetcreativeslot.a(), (ItemStack) null);
                } else {
                    this.player.defaultContainer.setItem(packetplayinsetcreativeslot.a(), itemstack);
                }

                this.player.defaultContainer.a(this.player, true);
            } else if (flag && flag2 && flag3 && this.j < 200) {
                this.j += 20;
                EntityItem entityitem = this.player.drop(itemstack, true);

                if (entityitem != null) {
                    entityitem.j();
                }
            }
        }

    }

    public void a(PacketPlayInTransaction packetplayintransaction) {
        PlayerConnectionUtils.ensureMainThread(packetplayintransaction, this, this.player.x());
        if (this.player.dead) return; // CraftBukkit
        Short oshort = (Short) this.k.get(this.player.activeContainer.windowId);

        if (oshort != null && packetplayintransaction.b() == oshort.shortValue() && this.player.activeContainer.windowId == packetplayintransaction.a() && !this.player.activeContainer.c(this.player) && !this.player.isSpectator()) {
            this.player.activeContainer.a(this.player, true);
        }

    }

    public void a(PacketPlayInUpdateSign packetplayinupdatesign) {
        PlayerConnectionUtils.ensureMainThread(packetplayinupdatesign, this, this.player.x());
        if (this.player.dead) return; // CraftBukkit
        this.player.resetIdleTimer();
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
        BlockPosition blockposition = packetplayinupdatesign.a();

        if (worldserver.isLoaded(blockposition)) {
            IBlockData iblockdata = worldserver.getType(blockposition);
            TileEntity tileentity = worldserver.getTileEntity(blockposition);

            if (!(tileentity instanceof TileEntitySign)) {
                return;
            }

            TileEntitySign tileentitysign = (TileEntitySign) tileentity;

            if (!tileentitysign.c() || tileentitysign.d() != this.player) {
                this.minecraftServer.warning("Player " + this.player.getName() + " just tried to change non-editable sign");
                this.sendPacket(tileentity.getUpdatePacket()); // CraftBukkit
                return;
            }

            String[] astring = packetplayinupdatesign.b();

            // CraftBukkit start
            Player player = this.server.getPlayer(this.player);
            int x = packetplayinupdatesign.a().getX();
            int y = packetplayinupdatesign.a().getY();
            int z = packetplayinupdatesign.a().getZ();
            String[] lines = new String[4];

            for (int i = 0; i < astring.length; ++i) {
                lines[i] = EnumChatFormat.a(new ChatComponentText(EnumChatFormat.a(astring[i])).toPlainText());
            }
            SignChangeEvent event = new SignChangeEvent((org.bukkit.craftbukkit.block.CraftBlock) player.getWorld().getBlockAt(x, y, z), this.server.getPlayer(this.player), lines);
            this.server.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                System.arraycopy(org.bukkit.craftbukkit.block.CraftSign.sanitizeLines(event.getLines()), 0, tileentitysign.lines, 0, 4);
                tileentitysign.isEditable = false;
             }
            // CraftBukkit end

            tileentitysign.update();
            worldserver.notify(blockposition, iblockdata, iblockdata, 3);
        }

    }

    public void a(PacketPlayInKeepAlive packetplayinkeepalive) {
        if (packetplayinkeepalive.a() == this.f) {
            int i = (int) (this.e() - this.g);

            this.player.ping = (this.player.ping * 3 + i) / 4;
        }

    }

    private long e() {
        return System.nanoTime() / 1000000L;
    }

    public void a(PacketPlayInAbilities packetplayinabilities) {
        PlayerConnectionUtils.ensureMainThread(packetplayinabilities, this, this.player.x());
        // CraftBukkit start
        if (this.player.abilities.canFly && this.player.abilities.isFlying != packetplayinabilities.isFlying()) {
            PlayerToggleFlightEvent event = new PlayerToggleFlightEvent(this.server.getPlayer(this.player), packetplayinabilities.isFlying());
            this.server.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                this.player.abilities.isFlying = packetplayinabilities.isFlying(); // Actually set the player's flying status
            } else {
                this.player.updateAbilities(); // Tell the player their ability was reverted
            }
        }
        // CraftBukkit end
    }

    public void a(PacketPlayInTabComplete packetplayintabcomplete) {
        PlayerConnectionUtils.ensureMainThread(packetplayintabcomplete, this, this.player.x());
        // CraftBukkit start
        if (chatSpamField.addAndGet(this, 10) > 500 && !this.minecraftServer.getPlayerList().isOp(this.player.getProfile())) {
            this.disconnect("disconnect.spam");
            return;
        }
        // CraftBukkit end
        ArrayList arraylist = Lists.newArrayList();
        Iterator iterator = this.minecraftServer.tabCompleteCommand(this.player, packetplayintabcomplete.a(), packetplayintabcomplete.b(), packetplayintabcomplete.c()).iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            arraylist.add(s);
        }

        this.player.playerConnection.sendPacket(new PacketPlayOutTabComplete((String[]) arraylist.toArray(new String[arraylist.size()])));
    }

    public void a(PacketPlayInSettings packetplayinsettings) {
        PlayerConnectionUtils.ensureMainThread(packetplayinsettings, this, this.player.x());
        this.player.a(packetplayinsettings);
    }

    public void a(PacketPlayInCustomPayload packetplayincustompayload) {
        PlayerConnectionUtils.ensureMainThread(packetplayincustompayload, this, this.player.x());
        String s = packetplayincustompayload.a();
        PacketDataSerializer packetdataserializer;
        ItemStack itemstack;
        ItemStack itemstack1;

        try { // Paper - Reimplement BookEditEvent
        if ("MC|BEdit".equals(s)) {
            packetdataserializer = new PacketDataSerializer(Unpooled.wrappedBuffer(packetplayincustompayload.b()));

            try {
                itemstack = packetdataserializer.k();
                if (itemstack == null) {
                    return;
                }

                if (!ItemBookAndQuill.b(itemstack.getTag())) {
                    throw new IOException("Invalid book tag!");
                }

                itemstack1 = this.player.getItemInMainHand();
                if (itemstack1 != null) {
                    if (itemstack.getItem() == Items.WRITABLE_BOOK && itemstack.getItem() == itemstack1.getItem()) {
                        itemstack1.a("pages", (NBTBase) itemstack.getTag().getList("pages", 8));
                        CraftEventFactory.handleEditBookEvent(player, itemstack1); // Paper
                    }

                    return;
                }
            } catch (Exception exception) {
                PlayerConnection.LOGGER.error("Couldn\'t handle book info", exception);
                this.disconnect("Invalid Book Data!"); // Paper
                return;
            } finally {
                packetdataserializer.release();
            }

            return;
        } else {
            String s1;

            if ("MC|BSign".equals(s)) {
                packetdataserializer = new PacketDataSerializer(Unpooled.wrappedBuffer(packetplayincustompayload.b()));

                try {
                    itemstack = packetdataserializer.k();
                    if (itemstack == null) {
                        return;
                    }

                    if (!ItemWrittenBook.b(itemstack.getTag())) {
                        throw new IOException("Invalid book tag!");
                    }

                    itemstack1 = this.player.getItemInMainHand();
                    if (itemstack1 != null) {
                        if (itemstack.getItem() == Items.WRITABLE_BOOK && itemstack1.getItem() == Items.WRITABLE_BOOK) {
                            itemstack1.a("author", (NBTBase) (new NBTTagString(this.player.getName())));
                            itemstack1.a("title", (NBTBase) (new NBTTagString(itemstack.getTag().getString("title"))));
                            NBTTagList nbttaglist = itemstack.getTag().getList("pages", 8);

                            for (int i = 0; i < nbttaglist.size(); ++i) {
                                s1 = nbttaglist.getString(i);
                                ChatComponentText chatcomponenttext = new ChatComponentText(s1);

                                s1 = IChatBaseComponent.ChatSerializer.a((IChatBaseComponent) chatcomponenttext);
                                nbttaglist.a(i, new NBTTagString(s1));
                            }

                            itemstack1.a("pages", (NBTBase) nbttaglist);
                            itemstack1.setItem(Items.WRITTEN_BOOK);
                            CraftEventFactory.handleEditBookEvent(player, itemstack1); // Paper
                        }

                        return;
                    }
                } catch (Exception exception1) {
                    PlayerConnection.LOGGER.error("Couldn\'t sign book", exception1);
                    this.disconnect("Invalid Book Data!"); // Paper
                    return;
                } finally {
                    packetdataserializer.release();
                }

                return;
            } else if ("MC|TrSel".equals(s)) {
                try {
                    int j = packetplayincustompayload.b().readInt();
                    Container container = this.player.activeContainer;

                    if (container instanceof ContainerMerchant) {
                        ((ContainerMerchant) container).d(j);
                    }
                } catch (Exception exception2) {
                    PlayerConnection.LOGGER.error("Couldn\'t select trade", exception2);
                }
            } else {
                TileEntity tileentity;

                if ("MC|AdvCmd".equals(s)) {
                    if (!this.minecraftServer.getEnableCommandBlock()) {
                        this.player.sendMessage(new ChatMessage("advMode.notEnabled", new Object[0]));
                        return;
                    }

                    if (!this.player.a(2, "") || !this.player.abilities.canInstantlyBuild) {
                        this.player.sendMessage(new ChatMessage("advMode.notAllowed", new Object[0]));
                        return;
                    }

                    packetdataserializer = packetplayincustompayload.b();

                    try {
                        byte b0 = packetdataserializer.readByte();
                        CommandBlockListenerAbstract commandblocklistenerabstract = null;

                        if (b0 == 0) {
                            tileentity = this.player.world.getTileEntity(new BlockPosition(packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt()));
                            if (tileentity instanceof TileEntityCommand) {
                                commandblocklistenerabstract = ((TileEntityCommand) tileentity).getCommandBlock();
                            }
                        } else if (b0 == 1) {
                            Entity entity = this.player.world.getEntity(packetdataserializer.readInt());

                            if (entity instanceof EntityMinecartCommandBlock) {
                                commandblocklistenerabstract = ((EntityMinecartCommandBlock) entity).getCommandBlock();
                            }
                        }

                        String s2 = packetdataserializer.e(packetdataserializer.readableBytes());
                        boolean flag = packetdataserializer.readBoolean();

                        if (commandblocklistenerabstract != null) {
                            commandblocklistenerabstract.setCommand(s2);
                            commandblocklistenerabstract.a(flag);
                            if (!flag) {
                                commandblocklistenerabstract.b((IChatBaseComponent) null);
                            }

                            commandblocklistenerabstract.i();
                            this.player.sendMessage(new ChatMessage("advMode.setCommand.success", new Object[] { s2}));
                        }
                    } catch (Exception exception3) {
                        PlayerConnection.LOGGER.error("Couldn\'t set command block", exception3);
                        this.disconnect("Invalid Command Block Data!"); // Paper
                    } finally {
                        packetdataserializer.release();
                    }
                } else if ("MC|AutoCmd".equals(s)) {
                    if (!this.minecraftServer.getEnableCommandBlock()) {
                        this.player.sendMessage(new ChatMessage("advMode.notEnabled", new Object[0]));
                        return;
                    }

                    if (!this.player.a(2, "") || !this.player.abilities.canInstantlyBuild) {
                        this.player.sendMessage(new ChatMessage("advMode.notAllowed", new Object[0]));
                        return;
                    }

                    packetdataserializer = packetplayincustompayload.b();

                    try {
                        CommandBlockListenerAbstract commandblocklistenerabstract1 = null;
                        TileEntityCommand tileentitycommand = null;
                        BlockPosition blockposition = new BlockPosition(packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt());
                        TileEntity tileentity1 = this.player.world.getTileEntity(blockposition);

                        if (tileentity1 instanceof TileEntityCommand) {
                            tileentitycommand = (TileEntityCommand) tileentity1;
                            commandblocklistenerabstract1 = tileentitycommand.getCommandBlock();
                        }

                        s1 = packetdataserializer.e(packetdataserializer.readableBytes());
                        boolean flag1 = packetdataserializer.readBoolean();
                        TileEntityCommand.Type tileentitycommand_type = TileEntityCommand.Type.valueOf(packetdataserializer.e(16));
                        boolean flag2 = packetdataserializer.readBoolean();
                        boolean flag3 = packetdataserializer.readBoolean();

                        if (commandblocklistenerabstract1 != null) {
                            EnumDirection enumdirection = (EnumDirection) this.player.world.getType(blockposition).get(BlockCommand.a);
                            IBlockData iblockdata;

                            switch (PlayerConnection.SyntheticClass_1.d[tileentitycommand_type.ordinal()]) {
                            case 1:
                                iblockdata = Blocks.dd.getBlockData();
                                this.player.world.setTypeAndData(blockposition, iblockdata.set(BlockCommand.a, enumdirection).set(BlockCommand.b, Boolean.valueOf(flag2)), 2);
                                break;

                            case 2:
                                iblockdata = Blocks.dc.getBlockData();
                                this.player.world.setTypeAndData(blockposition, iblockdata.set(BlockCommand.a, enumdirection).set(BlockCommand.b, Boolean.valueOf(flag2)), 2);
                                break;

                            case 3:
                                iblockdata = Blocks.COMMAND_BLOCK.getBlockData();
                                this.player.world.setTypeAndData(blockposition, iblockdata.set(BlockCommand.a, enumdirection).set(BlockCommand.b, Boolean.valueOf(flag2)), 2);
                            }

                            tileentity1.z();
                            this.player.world.setTileEntity(blockposition, tileentity1);
                            commandblocklistenerabstract1.setCommand(s1);
                            commandblocklistenerabstract1.a(flag1);
                            if (!flag1) {
                                commandblocklistenerabstract1.b((IChatBaseComponent) null);
                            }

                            tileentitycommand.b(flag3);
                            commandblocklistenerabstract1.i();
                            if (!UtilColor.b(s1)) {
                                this.player.sendMessage(new ChatMessage("advMode.setCommand.success", new Object[] { s1}));
                            }
                        }
                    } catch (Exception exception4) {
                        PlayerConnection.LOGGER.error("Couldn\'t set command block", exception4);
                        this.disconnect("Invalid Command Block Data!"); // Paper
                    } finally {
                        packetdataserializer.release();
                    }
                } else {
                    int k;

                    if ("MC|Beacon".equals(s)) {
                        if (this.player.activeContainer instanceof ContainerBeacon) {
                            try {
                                packetdataserializer = packetplayincustompayload.b();
                                k = packetdataserializer.readInt();
                                int l = packetdataserializer.readInt();
                                ContainerBeacon containerbeacon = (ContainerBeacon) this.player.activeContainer;
                                Slot slot = containerbeacon.getSlot(0);

                                if (slot.hasItem()) {
                                    slot.a(1);
                                    IInventory iinventory = containerbeacon.e();

                                    iinventory.setProperty(1, k);
                                    iinventory.setProperty(2, l);
                                    iinventory.update();
                                }
                            } catch (Exception exception5) {
                                PlayerConnection.LOGGER.error("Couldn\'t set beacon", exception5);
                                this.disconnect("Invalid Beacon Data!"); // Paper
                            }
                        }
                    } else if ("MC|ItemName".equals(s)) {
                        if (this.player.activeContainer instanceof ContainerAnvil) {
                            ContainerAnvil containeranvil = (ContainerAnvil) this.player.activeContainer;

                            if (packetplayincustompayload.b() != null && packetplayincustompayload.b().readableBytes() >= 1) {
                                String s3 = SharedConstants.a(packetplayincustompayload.b().e(32767));

                                if (s3.length() <= 30) {
                                    containeranvil.a(s3);
                                }
                            } else {
                                containeranvil.a("");
                            }
                        }
                    } else if ("MC|Struct".equals(s)) {
                        packetdataserializer = packetplayincustompayload.b();

                        try {
                            if (this.player.a(4, "") && this.player.abilities.canInstantlyBuild) {
                                BlockPosition blockposition1 = new BlockPosition(packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt());
                                IBlockData iblockdata1 = this.player.world.getType(blockposition1);

                                tileentity = this.player.world.getTileEntity(blockposition1);
                                if (tileentity instanceof TileEntityStructure) {
                                    TileEntityStructure tileentitystructure = (TileEntityStructure) tileentity;
                                    byte b1 = packetdataserializer.readByte();
                                    String s4 = packetdataserializer.e(32);

                                    tileentitystructure.a(TileEntityStructure.UsageMode.valueOf(s4));
                                    tileentitystructure.a(packetdataserializer.e(64));
                                    tileentitystructure.b(new BlockPosition(packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt()));
                                    tileentitystructure.c(new BlockPosition(packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt()));
                                    String s5 = packetdataserializer.e(32);

                                    tileentitystructure.a(EnumBlockMirror.valueOf(s5));
                                    String s6 = packetdataserializer.e(32);

                                    tileentitystructure.a(EnumBlockRotation.valueOf(s6));
                                    tileentitystructure.b(packetdataserializer.e(128));
                                    tileentitystructure.a(packetdataserializer.readBoolean());
                                    if (b1 == 2) {
                                        if (tileentitystructure.n()) {
                                            this.player.b((IChatBaseComponent) (new ChatComponentText("Structure saved")));
                                        } else {
                                            this.player.b((IChatBaseComponent) (new ChatComponentText("Structure NOT saved")));
                                        }
                                    } else if (b1 == 3) {
                                        if (tileentitystructure.o()) {
                                            this.player.b((IChatBaseComponent) (new ChatComponentText("Structure loaded")));
                                        } else {
                                            this.player.b((IChatBaseComponent) (new ChatComponentText("Structure prepared")));
                                        }
                                    } else if (b1 == 4 && tileentitystructure.m()) {
                                        this.player.b((IChatBaseComponent) (new ChatComponentText("Size detected")));
                                    }

                                    tileentitystructure.update();
                                    this.player.world.notify(blockposition1, iblockdata1, iblockdata1, 3);
                                }
                            }
                        } catch (Exception exception6) {
                            PlayerConnection.LOGGER.error("Couldn\'t set structure block", exception6);
                        } finally {
                            packetdataserializer.release();
                        }
                    } else if ("MC|PickItem".equals(s)) {
                        packetdataserializer = packetplayincustompayload.b();

                        try {
                            k = packetdataserializer.g();
                            this.player.inventory.d(k);
                            this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-2, this.player.inventory.itemInHandIndex, this.player.inventory.getItem(this.player.inventory.itemInHandIndex)));
                            this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-2, k, this.player.inventory.getItem(k)));
                            this.player.playerConnection.sendPacket(new PacketPlayOutHeldItemSlot(this.player.inventory.itemInHandIndex));
                        } catch (Exception exception7) {
                            PlayerConnection.LOGGER.error("Couldn\'t pick item", exception7);
                        } finally {
                            packetdataserializer.release();
                        }
                    }
                    // CraftBukkit start
                    else if (packetplayincustompayload.a().equals("REGISTER")) {
                        String channels = packetplayincustompayload.b().toString(com.google.common.base.Charsets.UTF_8);
                        for (String channel : channels.split("\0")) {
                            getPlayer().addChannel(channel);
                        }
                    } else if (packetplayincustompayload.a().equals("UNREGISTER")) {
                        String channels = packetplayincustompayload.b().toString(com.google.common.base.Charsets.UTF_8);
                        for (String channel : channels.split("\0")) {
                            getPlayer().removeChannel(channel);
                        }
                    } else {
                        byte[] data = new byte[packetplayincustompayload.b().readableBytes()];
                        packetplayincustompayload.b().readBytes(data);
                        server.getMessenger().dispatchIncomingMessage(player.getBukkitEntity(), packetplayincustompayload.a(), data);
                    }
                    // CraftBukkit end
                }
            }
        }
        // Paper start
        } finally {
            if (packetplayincustompayload.b().refCnt() > 0) {
                packetplayincustompayload.b().release();
            }
        }
        // Paper end

    }

    // CraftBukkit start - Add "isDisconnected" method
    public final boolean isDisconnected() {
        return !this.player.joining && !this.networkManager.isConnected();
    }

    static class SyntheticClass_1 {

        static final int[] a;
        static final int[] b;
        static final int[] c;
        static final int[] d = new int[TileEntityCommand.Type.values().length];

        static {
            try {
                PlayerConnection.SyntheticClass_1.d[TileEntityCommand.Type.SEQUENCE.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.d[TileEntityCommand.Type.AUTO.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.d[TileEntityCommand.Type.REDSTONE.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            c = new int[PacketPlayInClientCommand.EnumClientCommand.values().length];

            try {
                PlayerConnection.SyntheticClass_1.c[PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.c[PacketPlayInClientCommand.EnumClientCommand.REQUEST_STATS.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.c[PacketPlayInClientCommand.EnumClientCommand.OPEN_INVENTORY_ACHIEVEMENT.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            b = new int[PacketPlayInEntityAction.EnumPlayerAction.values().length];

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.START_SNEAKING.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.STOP_SNEAKING.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror7) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.START_SPRINTING.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror8) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.STOP_SPRINTING.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror9) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.STOP_SLEEPING.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror10) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.START_RIDING_JUMP.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror11) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.STOP_RIDING_JUMP.ordinal()] = 7;
            } catch (NoSuchFieldError nosuchfielderror12) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.OPEN_INVENTORY.ordinal()] = 8;
            } catch (NoSuchFieldError nosuchfielderror13) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.START_FALL_FLYING.ordinal()] = 9;
            } catch (NoSuchFieldError nosuchfielderror14) {
                ;
            }

            a = new int[PacketPlayInBlockDig.EnumPlayerDigType.values().length];

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.SWAP_HELD_ITEMS.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror15) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.DROP_ITEM.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror16) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.DROP_ALL_ITEMS.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror17) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.RELEASE_USE_ITEM.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror18) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror19) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror20) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK.ordinal()] = 7;
            } catch (NoSuchFieldError nosuchfielderror21) {
                ;
            }

        }
    }
}
