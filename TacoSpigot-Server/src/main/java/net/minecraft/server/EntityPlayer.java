package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.MainHand;
// CraftBukkit end

public class EntityPlayer extends EntityHuman implements ICrafting {

    private static final Logger bR = LogManager.getLogger();
    public String locale = null; // Spigot private -> public // Paper - default to null
    public PlayerConnection playerConnection;
    public final MinecraftServer server;
    public final PlayerInteractManager playerInteractManager;
    public double d;
    public double e;
    public final List<Integer> removeQueue = Lists.newLinkedList();
    private final ServerStatisticManager bU;
    private float bV = Float.MIN_VALUE;
    private int bW = Integer.MIN_VALUE;
    private int bX = Integer.MIN_VALUE;
    private int bY = Integer.MIN_VALUE;
    private int bZ = Integer.MIN_VALUE;
    private int ca = Integer.MIN_VALUE;
    private float lastHealthSent = -1.0E8F;
    private int cc = -99999999;
    private boolean cd = true;
    public int lastSentExp = -99999999;
    public int invulnerableTicks = 60;
    private EntityHuman.EnumChatVisibility cg;
    private boolean ch = true;
    private long ci = System.currentTimeMillis();
    private Entity cj = null;
    protected boolean ck; // PAIL: private -> protected, rename worldChangeInvuln
    private int containerCounter;
    public boolean f;
    public int ping;
    public boolean viewingCredits;
    // Paper start - Player view distance API
    private int viewDistance = -1;
    public int getViewDistance() {
        return viewDistance == -1 ? ((WorldServer) world).getPlayerChunkMap().getViewDistance() : viewDistance;
    }
    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }
    // Paper end
    private int containerUpdateDelay; // Paper

    // CraftBukkit start
    public String displayName;
    public IChatBaseComponent listName;
    public org.bukkit.Location compassTarget;
    public int newExp = 0;
    public int newLevel = 0;
    public int newTotalExp = 0;
    public boolean keepLevel = false;
    public double maxHealthCache;
    public boolean joining = true;
    // CraftBukkit end

    public EntityPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(worldserver, gameprofile);
        playerinteractmanager.player = this;
        this.playerInteractManager = playerinteractmanager;
        BlockPosition blockposition = worldserver.getSpawn();

        if (!worldserver.worldProvider.m() && worldserver.getWorldData().getGameType() != WorldSettings.EnumGamemode.ADVENTURE) {
            int i = Math.max(0, minecraftserver.a(worldserver));
            int j = MathHelper.floor(worldserver.getWorldBorder().b((double) blockposition.getX(), (double) blockposition.getZ()));

            if (j < i) {
                i = j;
            }

            if (j <= 1) {
                i = 1;
            }

            blockposition = worldserver.q(blockposition.a(this.random.nextInt(i * 2 + 1) - i, 0, this.random.nextInt(i * 2 + 1) - i));
        }

        this.server = minecraftserver;
        this.bU = minecraftserver.getPlayerList().a((EntityHuman) this);
        this.P = 0.0F;
        this.setPositionRotation(blockposition, 0.0F, 0.0F);

        while (!worldserver.getCubes(this, this.getBoundingBox()).isEmpty() && this.locY < 255.0D) {
            this.setPosition(this.locX, this.locY + 1.0D, this.locZ);
        }

        // CraftBukkit start
        this.displayName = this.getName();
        // this.canPickUpLoot = true; TODO
        this.maxHealthCache = this.getMaxHealth();
        // CraftBukkit end
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.hasKeyOfType("playerGameType", 99)) {
            if (this.h().getForceGamemode()) {
                this.playerInteractManager.setGameMode(this.h().getGamemode());
            } else {
                this.playerInteractManager.setGameMode(WorldSettings.EnumGamemode.getById(nbttagcompound.getInt("playerGameType")));
            }
        }

        this.getBukkitEntity().readExtraData(nbttagcompound); // CraftBukkit
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setInt("playerGameType", this.playerInteractManager.getGameMode().getId());
        Entity entity = this.getVehicle();

        if (this.bz() != null && entity != this & entity.b(EntityPlayer.class).size() == 1) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            NBTTagCompound nbttagcompound2 = new NBTTagCompound();

            entity.d(nbttagcompound2);
            nbttagcompound1.a("Attach", this.bz().getUniqueID());
            nbttagcompound1.set("Entity", nbttagcompound2);
            nbttagcompound.set("RootVehicle", nbttagcompound1);
        }
		this.getBukkitEntity().setExtraData(nbttagcompound); // CraftBukkit
    }

    // CraftBukkit start - World fallback code, either respawn location or global spawn
    public void spawnIn(World world) {
        super.spawnIn(world);
        if (world == null) {
            this.dead = false;
            BlockPosition position = null;
            if (this.spawnWorld != null && !this.spawnWorld.equals("")) {
                CraftWorld cworld = (CraftWorld) Bukkit.getServer().getWorld(this.spawnWorld);
                if (cworld != null && this.getBed() != null) {
                    world = cworld.getHandle();
                    position = EntityHuman.getBed(cworld.getHandle(), this.getBed(), false);
                }
            }
            if (world == null || position == null) {
                world = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();
                position = world.getSpawn();
            }
            this.world = world;
            this.setPosition(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        }
        this.dimension = ((WorldServer) this.world).dimension;
        this.playerInteractManager.a((WorldServer) world);
    }
    // CraftBukkit end

    public void levelDown(int i) {
        super.levelDown(i);
        this.lastSentExp = -1;
    }

    public void enchantDone(int i) {
        super.enchantDone(i);
        this.lastSentExp = -1;
    }

    public void syncInventory() {
        this.activeContainer.addSlotListener(this);
    }

    public void enterCombat() {
        super.enterCombat();
        this.playerConnection.sendPacket(new PacketPlayOutCombatEvent(this.getCombatTracker(), PacketPlayOutCombatEvent.EnumCombatEventType.ENTER_COMBAT));
    }

    public void exitCombat() {
        super.exitCombat();
        this.playerConnection.sendPacket(new PacketPlayOutCombatEvent(this.getCombatTracker(), PacketPlayOutCombatEvent.EnumCombatEventType.END_COMBAT));
    }

    protected ItemCooldown l() {
        return new ItemCooldownPlayer(this);
    }

    public void m() {
        // CraftBukkit start
        if (this.joining) {
            this.joining = false;
        }
        // CraftBukkit end
        this.playerInteractManager.a();
        --this.invulnerableTicks;
        if (this.noDamageTicks > 0) {
            --this.noDamageTicks;
        }

        // Paper start - Configurable container update tick rate
        if (--containerUpdateDelay <= 0) {
            this.activeContainer.b();
            containerUpdateDelay = world.paperConfig.containerUpdateTickRate;
        }
        // Paper end
        if (!this.world.isClientSide && !this.activeContainer.a((EntityHuman) this)) {
            this.closeInventory();
            this.activeContainer = this.defaultContainer;
        }

        while (!this.removeQueue.isEmpty()) {
            int i = Math.min(this.removeQueue.size(), Integer.MAX_VALUE);
            int[] aint = new int[i];
            Iterator iterator = this.removeQueue.iterator();
            int j = 0;

            while (iterator.hasNext() && j < i) {
                aint[j++] = ((Integer) iterator.next()).intValue();
                iterator.remove();
            }

            this.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(aint));
        }

        Entity entity = this.getSpecatorTarget();

        if (entity != this) {
            if (!entity.isAlive()) {
                this.setSpectatorTarget(this);
            } else {
                this.setLocation(entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
                this.server.getPlayerList().d(this);
                if (this.isSneaking()) {
                    this.setSpectatorTarget(this);
                }
            }
        }

    }

    public void k_() {
        try {
            super.m();

            for (int i = 0; i < this.inventory.getSize(); ++i) {
                ItemStack itemstack = this.inventory.getItem(i);

                if (itemstack != null && itemstack.getItem().f()) {
                    Packet packet = ((ItemWorldMapBase) itemstack.getItem()).a(itemstack, this.world, (EntityHuman) this);

                    if (packet != null) {
                        this.playerConnection.sendPacket(packet);
                    }
                }
            }

            if (this.getHealth() != this.lastHealthSent || this.cc != this.foodData.getFoodLevel() || this.foodData.getSaturationLevel() == 0.0F != this.cd) {
                this.playerConnection.sendPacket(new PacketPlayOutUpdateHealth(this.getBukkitEntity().getScaledHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel())); // CraftBukkit
                this.lastHealthSent = this.getHealth();
                this.cc = this.foodData.getFoodLevel();
                this.cd = this.foodData.getSaturationLevel() == 0.0F;
            }

            if (this.getHealth() + this.getAbsorptionHearts() != this.bV) {
                this.bV = this.getHealth() + this.getAbsorptionHearts();
                this.a(IScoreboardCriteria.g, MathHelper.f(this.bV));
            }

            if (this.foodData.getFoodLevel() != this.bW) {
                this.bW = this.foodData.getFoodLevel();
                this.a(IScoreboardCriteria.h, MathHelper.f((float) this.bW));
            }

            if (this.getAirTicks() != this.bX) {
                this.bX = this.getAirTicks();
                this.a(IScoreboardCriteria.i, MathHelper.f((float) this.bX));
            }

            // CraftBukkit start - Force max health updates
            if (this.maxHealthCache != this.getMaxHealth()) {
                this.getBukkitEntity().updateScaledHealth();
            }
            // CraftBukkit end

            if (this.expTotal != this.ca) {
                this.ca = this.expTotal;
                this.a(IScoreboardCriteria.k, MathHelper.f((float) this.ca));
            }

            if (this.expLevel != this.bZ) {
                this.bZ = this.expLevel;
                this.a(IScoreboardCriteria.l, MathHelper.f((float) this.bZ));
            }

            if (this.expTotal != this.lastSentExp) {
                this.lastSentExp = this.expTotal;
                this.playerConnection.sendPacket(new PacketPlayOutExperience(this.exp, this.expTotal, this.expLevel));
            }

            if (this.ticksLived % 20 * 5 == 0 && !this.getStatisticManager().hasAchievement(AchievementList.L)) {
                this.o();
            }

            // CraftBukkit start - initialize oldLevel and fire PlayerLevelChangeEvent
            if (this.oldLevel == -1) {
                this.oldLevel = this.expLevel;
            }

            if (this.oldLevel != this.expLevel) {
                CraftEventFactory.callPlayerLevelChangeEvent(this.world.getServer().getPlayer((EntityPlayer) this), this.oldLevel, this.expLevel);
                this.oldLevel = this.expLevel;
            }
            // CraftBukkit end
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Ticking player");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Player being ticked");

            this.appendEntityCrashDetails(crashreportsystemdetails);
            throw new ReportedException(crashreport);
        }
    }

    private void a(IScoreboardCriteria iscoreboardcriteria, int i) {
        Collection collection = this.world.getServer().getScoreboardManager().getScoreboardScores(iscoreboardcriteria, this.getName(), new java.util.ArrayList<ScoreboardScore>()); // CraftBukkit - Use our scores instead
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            ScoreboardScore scoreboardscore = (ScoreboardScore) iterator.next(); // CraftBukkit - Use our scores instead

            scoreboardscore.setScore(i);
        }

    }

    protected void o() {
        BiomeBase biomebase = this.world.getBiome(new BlockPosition(MathHelper.floor(this.locX), 0, MathHelper.floor(this.locZ)));
        String s = biomebase.l();
        AchievementSet achievementset = (AchievementSet) this.getStatisticManager().b((Statistic) AchievementList.L);

        if (achievementset == null) {
            achievementset = (AchievementSet) this.getStatisticManager().a(AchievementList.L, new AchievementSet());
        }

        achievementset.add(s);
        if (this.getStatisticManager().b(AchievementList.L) && achievementset.size() >= BiomeBase.i.size()) {
            HashSet hashset = Sets.newHashSet(BiomeBase.i);
            Iterator iterator = achievementset.iterator();

            while (iterator.hasNext()) {
                String s1 = (String) iterator.next();
                Iterator iterator1 = hashset.iterator();

                while (iterator1.hasNext()) {
                    BiomeBase biomebase1 = (BiomeBase) iterator1.next();

                    if (biomebase1.l().equals(s1)) {
                        iterator1.remove();
                    }
                }

                if (hashset.isEmpty()) {
                    break;
                }
            }

            if (hashset.isEmpty()) {
                this.b((Statistic) AchievementList.L);
            }
        }

    }

    public void die(DamageSource damagesource) {
        boolean flag = this.world.getGameRules().getBoolean("showDeathMessages");

        this.playerConnection.sendPacket(new PacketPlayOutCombatEvent(this.getCombatTracker(), PacketPlayOutCombatEvent.EnumCombatEventType.ENTITY_DIED, flag));
        // CraftBukkit start - fire PlayerDeathEvent
        if (this.dead) {
            return;
        }
        java.util.List<org.bukkit.inventory.ItemStack> loot = new java.util.ArrayList<org.bukkit.inventory.ItemStack>();
        boolean keepInventory = this.world.getGameRules().getBoolean("keepInventory");

        if (!keepInventory) {
            for (int i = 0; i < this.inventory.items.length; ++i) {
                if (this.inventory.items[i] != null) {
                    loot.add(CraftItemStack.asCraftMirror(this.inventory.items[i]));
                }
            }
            for (int i = 0; i < this.inventory.armor.length; ++i) {
                if (this.inventory.armor[i] != null) {
                    loot.add(CraftItemStack.asCraftMirror(this.inventory.armor[i]));
                }
            }
            for (int i = 0; i < this.inventory.extraSlots.length; ++i) {
                if (this.inventory.extraSlots[i] != null) {
                    loot.add(CraftItemStack.asCraftMirror(this.inventory.extraSlots[i]));
                }
            }
        }

        IChatBaseComponent chatmessage = this.getCombatTracker().getDeathMessage();

        String deathmessage = chatmessage.toPlainText();
        org.bukkit.event.entity.PlayerDeathEvent event = CraftEventFactory.callPlayerDeathEvent(this, loot, deathmessage, keepInventory);

        String deathMessage = event.getDeathMessage();

        if (deathMessage != null && deathMessage.length() > 0 && flag) { // TODO: allow plugins to override?
            if (deathMessage.equals(deathmessage)) {
                ScoreboardTeamBase scoreboardteambase = this.aO();

                if (scoreboardteambase != null && scoreboardteambase.getDeathMessageVisibility() != ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS) {
                    if (scoreboardteambase.getDeathMessageVisibility() == ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OTHER_TEAMS) {
                        this.server.getPlayerList().a((EntityHuman) this, chatmessage);
                    } else if (scoreboardteambase.getDeathMessageVisibility() == ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OWN_TEAM) {
                        this.server.getPlayerList().b((EntityHuman) this, chatmessage);
                    }
                } else {
                    this.server.getPlayerList().sendMessage(chatmessage);
                }
            } else {
                this.server.getPlayerList().sendMessage(org.bukkit.craftbukkit.util.CraftChatMessage.fromString(deathMessage));
            }
        }

        // we clean the player's inventory after the EntityDeathEvent is called so plugins can get the exact state of the inventory.
        if (!event.getKeepInventory()) {
            for (int i = 0; i < this.inventory.items.length; ++i) {
                this.inventory.items[i] = null;
            }
            for (int i = 0; i < this.inventory.armor.length; ++i) {
                this.inventory.armor[i] = null;
            }
            for (int i = 0; i < this.inventory.extraSlots.length; ++i) {
                this.inventory.extraSlots[i] = null;
            }
        }

        this.closeInventory();
        this.setSpectatorTarget(this); // Remove spectated target
        // CraftBukkit end

        // CraftBukkit - Get our scores instead
        Collection collection = this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreboardCriteria.d, this.getName(), new java.util.ArrayList<ScoreboardScore>());
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            ScoreboardScore scoreboardscore = (ScoreboardScore) iterator.next(); // CraftBukkit - Use our scores instead

            scoreboardscore.incrementScore();
        }

        EntityLiving entityliving = this.bW();

        if (entityliving != null) {
            EntityTypes.MonsterEggInfo entitytypes_monsteregginfo = (EntityTypes.MonsterEggInfo) EntityTypes.eggInfo.get(EntityTypes.b((Entity) entityliving));

            if (entitytypes_monsteregginfo != null) {
                this.b(entitytypes_monsteregginfo.killedByEntityStatistic);
            }

            entityliving.b(this, this.bb);
        }

        this.b(StatisticList.A);
        this.a(StatisticList.h);
        this.getCombatTracker().g();
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else {
            boolean flag = this.server.aa() && this.canPvP() && "fall".equals(damagesource.translationIndex);

            if (!flag && this.invulnerableTicks > 0 && damagesource != DamageSource.OUT_OF_WORLD) {
                return false;
            } else {
                if (damagesource instanceof EntityDamageSource) {
                    Entity entity = damagesource.getEntity();

                    if (entity instanceof EntityHuman && !this.a((EntityHuman) entity)) {
                        return false;
                    }

                    if (entity instanceof EntityArrow) {
                        EntityArrow entityarrow = (EntityArrow) entity;

                        if (entityarrow.shooter instanceof EntityHuman && !this.a((EntityHuman) entityarrow.shooter)) {
                            return false;
                        }
                    }
                }

                return super.damageEntity(damagesource, f);
            }
        }
    }

    public boolean a(EntityHuman entityhuman) {
        return !this.canPvP() ? false : super.a(entityhuman);
    }

    private boolean canPvP() {
        // CraftBukkit - this.server.getPvP() -> this.world.pvpMode
        return this.world.pvpMode;
    }


    // Paper start - Give "theEnd2" achievement if the player doesn't already have it
    private void giveTheEnd2() {
        if (!this.a(AchievementList.D)) {
            this.b(AchievementList.D);
        }
    }
    // Paper end

    @Nullable
    public Entity c(int i) {
        //this.ck = true; // CraftBukkit - Moved down and into PlayerList#changeDimension
        if (this.dimension == 1 && i == 1) {
            this.ck = true; // CraftBukkit - Moved down from above
            this.world.kill(this);
            if (!this.viewingCredits) {
                this.viewingCredits = true;
                // Paper start - Allow configurable end portal credits
                if (world.paperConfig.disableEndCredits || this.a(AchievementList.D)) {
                    this.giveTheEnd2();
                    // Paper end
                    this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(4, 0.0F));
                } else {
                    this.b((Statistic) AchievementList.D);
                    this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(4, 1.0F));
                }
            }

            return this;
        } else {
            if (this.dimension == 0 && i == 1) {
                this.b((Statistic) AchievementList.C);
                i = 1;
            } else {
                this.b((Statistic) AchievementList.y);
            }

            // CraftBukkit start
            TeleportCause cause = (this.dimension == 1 || i == 1) ? TeleportCause.END_PORTAL : TeleportCause.NETHER_PORTAL;
            this.server.getPlayerList().changeDimension(this, i, cause); // PAIL: check all this
            // CraftBukkit end
            this.playerConnection.sendPacket(new PacketPlayOutWorldEvent(1032, BlockPosition.ZERO, 0, false));
            this.lastSentExp = -1;
            this.lastHealthSent = -1.0F;
            this.cc = -1;
            return this;
        }
    }

    public boolean a(EntityPlayer entityplayer) {
        return entityplayer.isSpectator() ? this.getSpecatorTarget() == this : (this.isSpectator() ? false : super.a(entityplayer));
    }

    private void a(TileEntity tileentity) {
        if (tileentity != null) {
            PacketPlayOutTileEntityData packetplayouttileentitydata = tileentity.getUpdatePacket();

            if (packetplayouttileentitydata != null) {
                this.playerConnection.sendPacket(packetplayouttileentitydata);
            }
        }

    }

    public void receive(Entity entity, int i) {
        super.receive(entity, i);
        this.activeContainer.b();
    }

    public EntityHuman.EnumBedResult a(BlockPosition blockposition) {
        EntityHuman.EnumBedResult entityhuman_enumbedresult = super.a(blockposition);

        if (entityhuman_enumbedresult == EntityHuman.EnumBedResult.OK) {
            this.b(StatisticList.ad);
            PacketPlayOutBed packetplayoutbed = new PacketPlayOutBed(this, blockposition);

            this.x().getTracker().a((Entity) this, (Packet) packetplayoutbed);
            this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
            this.playerConnection.sendPacket(packetplayoutbed);
        }

        return entityhuman_enumbedresult;
    }

    public void a(boolean flag, boolean flag1, boolean flag2) {
        if (!this.sleeping) return; // CraftBukkit - Can't leave bed if not in one!
        if (this.isSleeping()) {
            this.x().getTracker().sendPacketToEntity(this, new PacketPlayOutAnimation(this, 2));
        }

        super.a(flag, flag1, flag2);
        if (this.playerConnection != null) {
            this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
        }

    }

    public boolean a(Entity entity, boolean flag) {
        Entity entity1 = this.bz();

        if (!super.a(entity, flag)) {
            return false;
        } else {
            Entity entity2 = this.bz();

            if (entity2 != entity1 && this.playerConnection != null) {
                this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
            }

            return true;
        }
    }

    public void stopRiding() {
        Entity entity = this.bz();

        super.stopRiding();
        Entity entity1 = this.bz();

        if (entity1 != entity && this.playerConnection != null) {
            this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
        }

    }

    public boolean isInvulnerable(DamageSource damagesource) {
        return super.isInvulnerable(damagesource) || this.K();
    }

    protected void a(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {}

    protected void b(BlockPosition blockposition) {
        if (!this.isSpectator()) {
            super.b(blockposition);
        }

    }

    public void a(double d0, boolean flag) {
        int i = MathHelper.floor(this.locX);
        int j = MathHelper.floor(this.locY - 0.20000000298023224D);
        int k = MathHelper.floor(this.locZ);
        BlockPosition blockposition = new BlockPosition(i, j, k);
        IBlockData iblockdata = this.world.getType(blockposition);

        if (iblockdata.getMaterial() == Material.AIR) {
            BlockPosition blockposition1 = blockposition.down();
            IBlockData iblockdata1 = this.world.getType(blockposition1);
            Block block = iblockdata1.getBlock();

            if (block instanceof BlockFence || block instanceof BlockCobbleWall || block instanceof BlockFenceGate) {
                blockposition = blockposition1;
                iblockdata = iblockdata1;
            }
        }

        super.a(d0, flag, iblockdata, blockposition);
    }

    public void openSign(TileEntitySign tileentitysign) {
        tileentitysign.a((EntityHuman) this);
        this.playerConnection.sendPacket(new PacketPlayOutOpenSignEditor(tileentitysign.getPosition()));
    }

    public int nextContainerCounter() { // CraftBukkit - void -> int
        this.containerCounter = this.containerCounter % 100 + 1;
        return containerCounter; // CraftBukkit
    }

    public void openTileEntity(ITileEntityContainer itileentitycontainer) {
        // CraftBukkit start - Inventory open hook
        Container container = CraftEventFactory.callInventoryOpenEvent(this, itileentitycontainer.createContainer(this.inventory, this));
        if (container == null) {
            return;
        }

        this.nextContainerCounter();
        this.activeContainer = container;
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, itileentitycontainer.getContainerName(), itileentitycontainer.getScoreboardDisplayName()));
        // CraftBukkit end
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void openContainer(IInventory iinventory) {
        // CraftBukkit start - Inventory open hook
        // Copied from below
        boolean cancelled = false;
        if (iinventory instanceof ITileInventory) {
            ITileInventory itileinventory = (ITileInventory) iinventory;
            cancelled = itileinventory.x_() && !this.a(itileinventory.y_()) && !this.isSpectator(); // PAIL: rename
        }

        Container container;
        if (iinventory instanceof ITileEntityContainer) {
            container = ((ITileEntityContainer) iinventory).createContainer(this.inventory, this);
        } else {
            container = new ContainerChest(this.inventory, iinventory, this);
        }
        container = CraftEventFactory.callInventoryOpenEvent(this, container, cancelled);
        if (container == null && !cancelled) { // Let pre-cancelled events fall through
            iinventory.closeContainer(this);
            return;
        }
        // CraftBukkit end

        if (iinventory instanceof ILootable && ((ILootable) iinventory).b() != null && this.isSpectator()) {
            this.sendMessage((new ChatMessage("container.spectatorCantOpen", new Object[0])).setChatModifier((new ChatModifier()).setColor(EnumChatFormat.RED)));
        } else {
            if (this.activeContainer != this.defaultContainer) {
                this.closeInventory();
            }

            if (iinventory instanceof ITileInventory) {
                ITileInventory itileinventory = (ITileInventory) iinventory;

                if (itileinventory.x_() && !this.a(itileinventory.y_()) && !this.isSpectator()) {
                    this.playerConnection.sendPacket(new PacketPlayOutChat(new ChatMessage("container.isLocked", new Object[] { iinventory.getScoreboardDisplayName()}), (byte) 2));
                    this.playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(SoundEffects.W, SoundCategory.BLOCKS, this.locX, this.locY, this.locZ, 1.0F, 1.0F));
                    iinventory.closeContainer(this); // CraftBukkit
                    return;
                }
            }

            this.nextContainerCounter();
            // CraftBukkit start
            if (iinventory instanceof ITileEntityContainer) {
                this.activeContainer = container;
                this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, ((ITileEntityContainer) iinventory).getContainerName(), iinventory.getScoreboardDisplayName(), iinventory.getSize()));
            } else {
                this.activeContainer = container;
                this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, "minecraft:container", iinventory.getScoreboardDisplayName(), iinventory.getSize()));
            }
            // CraftBukkit end

            this.activeContainer.windowId = this.containerCounter;
            this.activeContainer.addSlotListener(this);
        }
    }

    public void openTrade(IMerchant imerchant) {
        // CraftBukkit start - Inventory open hook
        Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerMerchant(this.inventory, imerchant, this.world));
        if (container == null) {
            return;
        }
        // CraftBukkit end
        this.nextContainerCounter();
        this.activeContainer = container; // CraftBukkit
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
        InventoryMerchant inventorymerchant = ((ContainerMerchant) this.activeContainer).e();
        IChatBaseComponent ichatbasecomponent = imerchant.getScoreboardDisplayName();

        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, "minecraft:villager", ichatbasecomponent, inventorymerchant.getSize()));
        MerchantRecipeList merchantrecipelist = imerchant.getOffers(this);

        if (merchantrecipelist != null) {
            PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());

            packetdataserializer.writeInt(this.containerCounter);
            merchantrecipelist.a(packetdataserializer);
            this.playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|TrList", packetdataserializer));
        }

    }

    public void a(EntityHorse entityhorse, IInventory iinventory) {
        // CraftBukkit start - Inventory open hook
        Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerHorse(this.inventory, iinventory, entityhorse, this));
        if (container == null) {
            iinventory.closeContainer(this);
            return;
        }
        // CraftBukkit end
        if (this.activeContainer != this.defaultContainer) {
            this.closeInventory();
        }

        this.nextContainerCounter();
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(this.containerCounter, "EntityHorse", iinventory.getScoreboardDisplayName(), iinventory.getSize(), entityhorse.getId()));
        this.activeContainer = container;
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener(this);
    }

    public void a(ItemStack itemstack, EnumHand enumhand) {
        Item item = itemstack.getItem();

        if (item == Items.WRITTEN_BOOK) {
            //PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());
			PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer(), this); // TacoSpigot - pass player to PacketDataSerializer

            packetdataserializer.a((Enum) enumhand);
            this.playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|BOpen", packetdataserializer));
        }

    }

    public void a(TileEntityCommand tileentitycommand) {
        if (this.a(2, "")) {
            tileentitycommand.d(true);
            this.a((TileEntity) tileentitycommand);
        }

    }

    public void a(Container container, int i, ItemStack itemstack) {
        if (!(container.getSlot(i) instanceof SlotResult)) {
            if (!this.f) {
                this.playerConnection.sendPacket(new PacketPlayOutSetSlot(container.windowId, i, itemstack));
            }
        }
    }

    public void updateInventory(Container container) {
        this.a(container, container.a());
    }

    public void a(Container container, List<ItemStack> list) {
        this.playerConnection.sendPacket(new PacketPlayOutWindowItems(container.windowId, list));
        this.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.inventory.getCarried()));
        // CraftBukkit start - Send a Set Slot to update the crafting result slot
        if (java.util.EnumSet.of(InventoryType.CRAFTING,InventoryType.WORKBENCH).contains(container.getBukkitView().getType())) {
            this.playerConnection.sendPacket(new PacketPlayOutSetSlot(container.windowId, 0, container.getSlot(0).getItem()));
        }
        // CraftBukkit end
    }

    public void setContainerData(Container container, int i, int j) {
        this.playerConnection.sendPacket(new PacketPlayOutWindowData(container.windowId, i, j));
    }

    public void setContainerData(Container container, IInventory iinventory) {
        for (int i = 0; i < iinventory.g(); ++i) {
            this.playerConnection.sendPacket(new PacketPlayOutWindowData(container.windowId, i, iinventory.getProperty(i)));
        }

    }

    public void closeInventory() {
        CraftEventFactory.handleInventoryCloseEvent(this); // CraftBukkit
        this.playerConnection.sendPacket(new PacketPlayOutCloseWindow(this.activeContainer.windowId));
        this.s();
    }

    public void broadcastCarriedItem() {
        if (!this.f) {
            this.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.inventory.getCarried()));
        }
    }

    public void s() {
        this.activeContainer.b((EntityHuman) this);
        this.activeContainer = this.defaultContainer;
    }

    public void a(float f, float f1, boolean flag, boolean flag1) {
        if (this.isPassenger()) {
            if (f >= -1.0F && f <= 1.0F) {
                this.be = f;
            }

            if (f1 >= -1.0F && f1 <= 1.0F) {
                this.bf = f1;
            }

            this.bd = flag;
            this.setSneaking(flag1);
        }

    }

    public boolean a(Achievement achievement) {
        return this.bU.hasAchievement(achievement);
    }

    public void a(Statistic statistic, int i) {
        if (statistic != null) {
            this.bU.b(this, statistic, i);
            Iterator iterator = this.getScoreboard().getObjectivesForCriteria(statistic.k()).iterator();

            while (iterator.hasNext()) {
                ScoreboardObjective scoreboardobjective = (ScoreboardObjective) iterator.next();

                this.getScoreboard().getPlayerScoreForObjective(this.getName(), scoreboardobjective).addScore(i);
            }

            if (this.bU.e()) {
                this.bU.a(this);
            }

        }
    }

    public void a(Statistic statistic) {
        if (statistic != null) {
            this.bU.setStatistic(this, statistic, 0);
            Iterator iterator = this.getScoreboard().getObjectivesForCriteria(statistic.k()).iterator();

            while (iterator.hasNext()) {
                ScoreboardObjective scoreboardobjective = (ScoreboardObjective) iterator.next();

                this.getScoreboard().getPlayerScoreForObjective(this.getName(), scoreboardobjective).setScore(0);
            }

            if (this.bU.e()) {
                this.bU.a(this);
            }

        }
    }

    public void t() {
        this.az();
        if (this.sleeping) {
            this.a(true, false, false);
        }

    }

    public void triggerHealthUpdate() {
        this.lastHealthSent = -1.0E8F;
        this.lastSentExp = -1; // CraftBukkit - Added to reset
    }

    // CraftBukkit start - Support multi-line messages
    public void sendMessage(IChatBaseComponent[] ichatbasecomponent) {
        for (IChatBaseComponent component : ichatbasecomponent) {
            this.sendMessage(component);
        }
    }
    // CraftBukkit end

    public void b(IChatBaseComponent ichatbasecomponent) {
        this.playerConnection.sendPacket(new PacketPlayOutChat(ichatbasecomponent));
    }

    protected void v() {
        if (this.bn != null && this.ct()) {
            this.playerConnection.sendPacket(new PacketPlayOutEntityStatus(this, (byte) 9));
            super.v();
        }

    }

    public void copyTo(EntityHuman entityhuman, boolean flag) {
        super.copyTo(entityhuman, flag);
        this.lastSentExp = -1;
        this.lastHealthSent = -1.0F;
        this.cc = -1;
        this.removeQueue.addAll(((EntityPlayer) entityhuman).removeQueue);
    }

    protected void a(MobEffect mobeffect) {
        super.a(mobeffect);
        this.playerConnection.sendPacket(new PacketPlayOutEntityEffect(this.getId(), mobeffect));
    }

    protected void a(MobEffect mobeffect, boolean flag) {
        super.a(mobeffect, flag);
        this.playerConnection.sendPacket(new PacketPlayOutEntityEffect(this.getId(), mobeffect));
    }

    protected void b(MobEffect mobeffect) {
        super.b(mobeffect);
        this.playerConnection.sendPacket(new PacketPlayOutRemoveEntityEffect(this.getId(), mobeffect.getMobEffect()));
    }

    public void enderTeleportTo(double d0, double d1, double d2) {
        this.playerConnection.a(d0, d1, d2, this.yaw, this.pitch);
    }

    public void a(Entity entity) {
        this.x().getTracker().sendPacketToEntity(this, new PacketPlayOutAnimation(entity, 4));
    }

    public void b(Entity entity) {
        this.x().getTracker().sendPacketToEntity(this, new PacketPlayOutAnimation(entity, 5));
    }

    public void updateAbilities() {
        if (this.playerConnection != null) {
            this.playerConnection.sendPacket(new PacketPlayOutAbilities(this.abilities));
            this.F();
        }
    }

    public WorldServer x() {
        return (WorldServer) this.world;
    }

    public void a(WorldSettings.EnumGamemode worldsettings_enumgamemode) {
        getBukkitEntity().setGameMode(org.bukkit.GameMode.getByValue(worldsettings_enumgamemode.getId()));
        /* CraftBukkit start - defer to our setGameMode
        this.playerInteractManager.setGameMode(worldsettings_enumgamemode);
        this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, (float) worldsettings_enumgamemode.getId()));
        if (worldsettings_enumgamemode == WorldSettings.EnumGamemode.SPECTATOR) {
            this.stopRiding();
        } else {
            this.setSpectatorTarget(this);
        }

        this.updateAbilities();
        this.cr();
        // CraftBukkit end */
    }

    public boolean isSpectator() {
        return this.playerInteractManager.getGameMode() == WorldSettings.EnumGamemode.SPECTATOR;
    }

    public boolean l_() {
        return this.playerInteractManager.getGameMode() == WorldSettings.EnumGamemode.CREATIVE;
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        this.playerConnection.sendPacket(new PacketPlayOutChat(ichatbasecomponent));
    }

    public boolean a(int i, String s) {
        /* CraftBukkit start
        if ("seed".equals(s) && !this.server.aa()) {
            return true;
        } else if (!"tell".equals(s) && !"help".equals(s) && !"me".equals(s) && !"trigger".equals(s)) {
            if (this.server.getPlayerList().isOp(this.getProfile())) {
                OpListEntry oplistentry = (OpListEntry) this.server.getPlayerList().getOPs().get(this.getProfile());

                return oplistentry != null ? oplistentry.a() >= i : this.server.q() >= i;
            } else {
                return false;
            }
        } else {
            return true;
        }
        */
        if ("@".equals(s)) {
            return getBukkitEntity().hasPermission("minecraft.command.selector");
        }
        if ("".equals(s)) {
            return getBukkitEntity().isOp();
        }
        return getBukkitEntity().hasPermission("minecraft.command." + s);
        // CraftBukkit end
    }

    public String A() {
        String s = this.playerConnection.networkManager.getSocketAddress().toString();

        s = s.substring(s.indexOf("/") + 1);
        s = s.substring(0, s.indexOf(":"));
        return s;
    }

    public void a(PacketPlayInSettings packetplayinsettings) {
        // CraftBukkit start
        if (getMainHand() != packetplayinsettings.f()) { // PAIL: rename
            PlayerChangedMainHandEvent event = new PlayerChangedMainHandEvent(getBukkitEntity(), getMainHand() == EnumMainHand.LEFT ? MainHand.LEFT : MainHand.RIGHT);
            this.server.server.getPluginManager().callEvent(event);
        }
        // CraftBukkit end

        // Paper start - add PlayerLocaleChangeEvent
        // Since the field is initialized to null, this event should always fire the first time the packet is received
        String oldLocale = this.locale;
        this.locale = packetplayinsettings.a();
        if (!this.locale.equals(oldLocale)) {
            new com.destroystokyo.paper.event.player.PlayerLocaleChangeEvent(this.getBukkitEntity(), oldLocale, this.locale).callEvent();
        }
        // Paper end
        this.cg = packetplayinsettings.c();
        this.ch = packetplayinsettings.d();
        this.getDataWatcher().set(EntityPlayer.bq, Byte.valueOf((byte) packetplayinsettings.e()));
        this.getDataWatcher().set(EntityPlayer.br, Byte.valueOf((byte) (packetplayinsettings.f() == EnumMainHand.LEFT ? 0 : 1)));
    }

    public EntityHuman.EnumChatVisibility getChatFlags() {
        return this.cg;
    }

    public void setResourcePack(String s, String s1) {
        this.playerConnection.sendPacket(new PacketPlayOutResourcePackSend(s, s1));
    }

    public BlockPosition getChunkCoordinates() {
        return new BlockPosition(this.locX, this.locY + 0.5D, this.locZ);
    }

    public void resetIdleTimer() {
        this.ci = MinecraftServer.av();
    }

    public ServerStatisticManager getStatisticManager() {
        return this.bU;
    }

    public void c(Entity entity) {
        if (entity instanceof EntityHuman) {
            this.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(new int[] { entity.getId()}));
        } else {
            this.removeQueue.add(Integer.valueOf(entity.getId()));
        }

    }

    public void d(Entity entity) {
        this.removeQueue.remove(Integer.valueOf(entity.getId()));
    }

    protected void F() {
        if (this.isSpectator()) {
            this.bN();
            this.setInvisible(true);
        } else {
            super.F();
        }

        this.x().getTracker().a(this);
    }

    public Entity getSpecatorTarget() {
        return (Entity) (this.cj == null ? this : this.cj);
    }

    public void setSpectatorTarget(Entity entity) {
        Entity entity1 = this.getSpecatorTarget();

        this.cj = (Entity) (entity == null ? this : entity);
        if (entity1 != this.cj) {
            this.playerConnection.sendPacket(new PacketPlayOutCamera(this.cj));
            this.enderTeleportTo(this.cj.locX, this.cj.locY, this.cj.locZ);
        }

    }

    protected void H() {
        if (this.portalCooldown > 0 && !this.ck) {
            --this.portalCooldown;
        }

    }

    public void attack(Entity entity) {
        if (this.playerInteractManager.getGameMode() == WorldSettings.EnumGamemode.SPECTATOR) {
            this.setSpectatorTarget(entity);
        } else {
            super.attack(entity);
        }

    }

    public long I() {
        return this.ci;
    }

    @Nullable
    public IChatBaseComponent getPlayerListName() {
        return listName; // CraftBukkit
    }

    public void a(EnumHand enumhand) {
        super.a(enumhand);
        this.da();
    }

    public boolean K() {
        return this.ck;
    }

    public void L() {
        this.ck = false;
    }

    public void M() {
        if (!CraftEventFactory.callToggleGlideEvent(this, true).isCancelled()) // CraftBukkit
        this.setFlag(7, true);
    }

    public void N() {
        // CraftBukkit start
        if (!CraftEventFactory.callToggleGlideEvent(this, false).isCancelled()) {
            this.setFlag(7, true);
            this.setFlag(7, false);
        }
        // CraftBukkit end
    }

    // CraftBukkit start - Add per-player time and weather.
    public long timeOffset = 0;
    public boolean relativeTime = true;

    public long getPlayerTime() {
        if (this.relativeTime) {
            // Adds timeOffset to the current server time.
            return this.world.getDayTime() + this.timeOffset;
        } else {
            // Adds timeOffset to the beginning of this day.
            return this.world.getDayTime() - (this.world.getDayTime() % 24000) + this.timeOffset;
        }
    }

    public WeatherType weather = null;

    public WeatherType getPlayerWeather() {
        return this.weather;
    }

    public void setPlayerWeather(WeatherType type, boolean plugin) {
        if (!plugin && this.weather != null) {
            return;
        }

        if (plugin) {
            this.weather = type;
        }

        if (type == WeatherType.DOWNFALL) {
            this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(2, 0));
        } else {
            this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(1, 0));
        }
    }

    private float pluginRainPosition;
    private float pluginRainPositionPrevious;

    public void updateWeather(float oldRain, float newRain, float oldThunder, float newThunder) {
        if (this.weather == null) {
            // Vanilla
            if (oldRain != newRain) {
                this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(7, newRain));
            }
        } else {
            // Plugin
            if (pluginRainPositionPrevious != pluginRainPosition) {
                this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(7, pluginRainPosition));
            }
        }

        if (oldThunder != newThunder) {
            if (weather == WeatherType.DOWNFALL || weather == null) {
                this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(8, newThunder));
            } else {
                this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(8, 0));
            }
        }
    }

    public void tickWeather() {
        if (this.weather == null) return;

        pluginRainPositionPrevious = pluginRainPosition;
        if (weather == WeatherType.DOWNFALL) {
            pluginRainPosition += 0.01;
        } else {
            pluginRainPosition -= 0.01;
        }

        pluginRainPosition = MathHelper.a(pluginRainPosition, 0.0F, 1.0F);
    }

    public void resetPlayerWeather() {
        this.weather = null;
        this.setPlayerWeather(this.world.getWorldData().hasStorm() ? WeatherType.DOWNFALL : WeatherType.CLEAR, false);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + this.getName() + " at " + this.locX + "," + this.locY + "," + this.locZ + ")";
    }

    public void reset() {
        float exp = 0;
        boolean keepInventory = this.world.getGameRules().getBoolean("keepInventory");

        if (this.keepLevel || keepInventory) {
            exp = this.exp;
            this.newTotalExp = this.expTotal;
            this.newLevel = this.expLevel;
        }

        this.setHealth(this.getMaxHealth());
        this.fireTicks = 0;
        this.fallDistance = 0;
        this.foodData = new FoodMetaData(this);
        this.expLevel = this.newLevel;
        this.expTotal = this.newTotalExp;
        this.exp = 0;
        this.deathTicks = 0;
        this.removeAllEffects();
        this.updateEffects = true;
        this.activeContainer = this.defaultContainer;
        this.killer = null;
        this.lastDamager = null;
        this.combatTracker = new CombatTracker(this);
        this.lastSentExp = -1;
        if (this.keepLevel || keepInventory) {
            this.exp = exp;
        } else {
            this.giveExp(this.newExp);
        }
        this.keepLevel = false;
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        return (CraftPlayer) super.getBukkitEntity();
    }
    // CraftBukkit end
}
