package net.minecraft.server;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.RecursiveAction;
import org.spigotmc.SpigotWorldConfig;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Queues;
import java.util.Queue;
import java.util.concurrent.RecursiveAction;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start
import java.util.logging.Level;

import org.bukkit.WeatherType;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.util.HashTreeSet;

import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
// CraftBukkit end

public class WorldServer extends World implements IAsyncTaskHandler {

    private static final Logger a = LogManager.getLogger();
    boolean stopPhysicsEvent = false; // Paper
    private final MinecraftServer server;
    public EntityTracker tracker;
    private final PlayerChunkMap manager;
    // private final Set<NextTickListEntry> nextTickListHash = Sets.newHashSet();
    private final HashTreeSet<NextTickListEntry> nextTickList = new HashTreeSet<NextTickListEntry>(); // CraftBukkit - HashTreeSet
    private final Map<UUID, Entity> entitiesByUUID = Maps.newHashMap();
    public boolean savingDisabled;
    private boolean O;
    private int emptyTime;
    private final PortalTravelAgent portalTravelAgent;
    private final SpawnerCreature spawnerCreature = new SpawnerCreature();
    protected final VillageSiege siegeManager = new VillageSiege(this);
    private WorldServer.BlockActionDataList[] S = new WorldServer.BlockActionDataList[] { new WorldServer.BlockActionDataList(null), new WorldServer.BlockActionDataList(null)};
    private int T;
	private Queue<NextTickListEntry> U = new ConcurrentLinkedQueue();
	
	protected final Map<Integer, Entity> entitiesById = Maps.newConcurrentMap(); // Torch - async fix

    // CraftBukkit start
    public final int dimension;

    // Add env and gen to constructor
    public WorldServer(MinecraftServer minecraftserver, IDataManager idatamanager, WorldData worlddata, int i, MethodProfiler methodprofiler, org.bukkit.World.Environment env, org.bukkit.generator.ChunkGenerator gen) {
        super(idatamanager, worlddata, DimensionManager.a(env.getId()).d(), methodprofiler, false, gen, env);
        this.dimension = i;
        this.pvpMode = minecraftserver.getPVP();
        worlddata.world = this;
        // CraftBukkit end
        this.server = minecraftserver;
        this.tracker = new EntityTracker(this);
        this.manager = new PlayerChunkMap(this, spigotConfig.viewDistance); // Spigot
        this.worldProvider.a((World) this);
        this.chunkProvider = this.n();
        this.portalTravelAgent = new org.bukkit.craftbukkit.CraftTravelAgent(this); // CraftBukkit
        this.H();
        this.I();
        this.getWorldBorder().a(minecraftserver.aD());
    }

    public World b() {
        this.worldMaps = new PersistentCollection(this.dataManager);
        String s = PersistentVillage.a(this.worldProvider);
        PersistentVillage persistentvillage = (PersistentVillage) this.worldMaps.get(PersistentVillage.class, s);

        if (persistentvillage == null) {
            this.villages = new PersistentVillage(this);
            this.worldMaps.a(s, this.villages);
        } else {
            this.villages = persistentvillage;
            this.villages.a((World) this);
        }

        if (getServer().getScoreboardManager() == null) { // CraftBukkit
        this.scoreboard = new ScoreboardServer(this.server);
        PersistentScoreboard persistentscoreboard = (PersistentScoreboard) this.worldMaps.get(PersistentScoreboard.class, "scoreboard");

        if (persistentscoreboard == null) {
            persistentscoreboard = new PersistentScoreboard();
            this.worldMaps.a("scoreboard", persistentscoreboard);
        }

        persistentscoreboard.a(this.scoreboard);
        ((ScoreboardServer) this.scoreboard).a((Runnable) (new RunnableSaveScoreboard(persistentscoreboard)));
        // CraftBukkit start
        } else {
            this.scoreboard = getServer().getScoreboardManager().getMainScoreboard().getHandle();
        }
        // CraftBukkit end
        this.B = new LootTableRegistry(new File(new File(this.dataManager.getDirectory(), "data"), "loot_tables"));
        this.getWorldBorder().setCenter(this.worldData.B(), this.worldData.C());
        this.getWorldBorder().setDamageAmount(this.worldData.H());
        this.getWorldBorder().setDamageBuffer(this.worldData.G());
        this.getWorldBorder().setWarningDistance(this.worldData.I());
        this.getWorldBorder().setWarningTime(this.worldData.J());
        if (this.worldData.E() > 0L) {
            this.getWorldBorder().transitionSizeBetween(this.worldData.D(), this.worldData.F(), this.worldData.E());
        } else {
            this.getWorldBorder().setSize(this.worldData.D());
        }

        // CraftBukkit start
        if (generator != null) {
            getWorld().getPopulators().addAll(generator.getDefaultPopulators(getWorld()));
        }
        // CraftBukkit end

        return this;
    }

    // CraftBukkit start
    @Override
    public TileEntity getTileEntity(BlockPosition pos) {
        return getTileEntity(pos, false);
    }
	
	@Override
    public TileEntity getTileEntity(BlockPosition pos, boolean isRemoving) {
        TileEntity result = super.getTileEntity(pos, isRemoving);
        if (isRemoving) return result;
        // Paper end
        Block type = getType(pos).getBlock();

        if (type == Blocks.CHEST || type == Blocks.TRAPPED_CHEST) { // Spigot
            if (!(result instanceof TileEntityChest)) {
                result = fixTileEntity(pos, type, result);
            }
        } else if (type == Blocks.FURNACE) {
            if (!(result instanceof TileEntityFurnace)) {
                result = fixTileEntity(pos, type, result);
            }
        } else if (type == Blocks.DROPPER) {
            if (!(result instanceof TileEntityDropper)) {
                result = fixTileEntity(pos, type, result);
            }
        } else if (type == Blocks.DISPENSER) {
            if (!(result instanceof TileEntityDispenser)) {
                result = fixTileEntity(pos, type, result);
            }
        } else if (type == Blocks.JUKEBOX) {
            if (!(result instanceof BlockJukeBox.TileEntityRecordPlayer)) {
                result = fixTileEntity(pos, type, result);
            }
        } else if (type == Blocks.NOTEBLOCK) {
            if (!(result instanceof TileEntityNote)) {
                result = fixTileEntity(pos, type, result);
            }
        } else if (type == Blocks.MOB_SPAWNER) {
            if (!(result instanceof TileEntityMobSpawner)) {
                result = fixTileEntity(pos, type, result);
            }
        } else if ((type == Blocks.STANDING_SIGN) || (type == Blocks.WALL_SIGN)) {
            if (!(result instanceof TileEntitySign)) {
                result = fixTileEntity(pos, type, result);
            }
        } else if (type == Blocks.ENDER_CHEST) {
            if (!(result instanceof TileEntityEnderChest)) {
                result = fixTileEntity(pos, type, result);
            }
        } else if (type == Blocks.BREWING_STAND) {
            if (!(result instanceof TileEntityBrewingStand)) {
                result = fixTileEntity(pos, type, result);
            }
        } else if (type == Blocks.BEACON) {
            if (!(result instanceof TileEntityBeacon)) {
                result = fixTileEntity(pos, type, result);
            }
        } else if (type == Blocks.HOPPER) {
            if (!(result instanceof TileEntityHopper)) {
                result = fixTileEntity(pos, type, result);
            }
        }

        return result;
    }

    private TileEntity fixTileEntity(BlockPosition pos, Block type, TileEntity found) {
        this.getServer().getLogger().log(Level.SEVERE, "Block at {0},{1},{2} is {3} but has {4}" + ". "
                + "Bukkit will attempt to fix this, but there may be additional damage that we cannot recover.", new Object[]{pos.getX(), pos.getY(), pos.getZ(), org.bukkit.Material.getMaterial(Block.getId(type)).toString(), found});

        if (type instanceof ITileEntity) {
            TileEntity replacement = ((ITileEntity) type).a(this, type.toLegacyData(this.getType(pos)));
            replacement.world = this;
            this.setTileEntity(pos, replacement);
            return replacement;
        } else {
            this.getServer().getLogger().severe("Don't know how to fix for this type... Can't do anything! :(");
            return found;
        }
    }

    private boolean canSpawn(int x, int z) {
        if (this.generator != null) {
            return this.generator.canSpawn(this.getWorld(), x, z);
        } else {
            return this.worldProvider.canSpawn(x, z);
        }
    }
    // CraftBukkit end

    public void doTick() {
        super.doTick();
        if (this.getWorldData().isHardcore() && this.getDifficulty() != EnumDifficulty.HARD) {
            this.getWorldData().setDifficulty(EnumDifficulty.HARD);
        }

        this.worldProvider.k().b();
        if (this.everyoneDeeplySleeping()) {
            if (this.getGameRules().getBoolean("doDaylightCycle")) {
                long i = this.worldData.getDayTime() + 24000L;

                this.worldData.setDayTime(i - i % 24000L);
            }

            this.f();
        }

        // CraftBukkit start - Only call spawner if we have players online and the world allows for mobs or animals
        long time = this.worldData.getTime();
        if (this.getGameRules().getBoolean("doMobSpawning") && this.worldData.getType() != WorldType.DEBUG_ALL_BLOCK_STATES && (this.allowMonsters || this.allowAnimals) && (this instanceof WorldServer && this.players.size() > 0)) {
            timings.mobSpawn.startTiming(); // Spigot
            this.spawnerCreature.a(this, this.allowMonsters && (this.ticksPerMonsterSpawns != 0 && time % this.ticksPerMonsterSpawns == 0L), this.allowAnimals && (this.ticksPerAnimalSpawns != 0 && time % this.ticksPerAnimalSpawns == 0L), this.worldData.getTime() % 400L == 0L);
            timings.mobSpawn.stopTiming(); // Spigot
            // CraftBukkit end
        }

        timings.doChunkUnload.startTiming(); // Spigot
        this.methodProfiler.c("chunkSource");
        this.chunkProvider.unloadChunks();
        int j = this.a(1.0F);

        if (j != this.af()) {
            this.c(j);
        }

        this.worldData.setTime(this.worldData.getTime() + 1L);
        if (this.getGameRules().getBoolean("doDaylightCycle")) {
            this.worldData.setDayTime(this.worldData.getDayTime() + 1L);
        }

        timings.doChunkUnload.stopTiming(); // Spigot
        this.methodProfiler.c("tickPending");
        timings.scheduledBlocks.startTiming(); // Paper
        this.a(false);
        timings.scheduledBlocks.stopTiming(); // Paper
        this.methodProfiler.c("tickBlocks");
        timings.chunkTicks.startTiming(); // Paper
        this.j();
        timings.chunkTicks.stopTiming(); // Paper
        this.methodProfiler.c("chunkMap");
        timings.doChunkMap.startTiming(); // Spigot
        this.manager.flush();
        timings.doChunkMap.stopTiming(); // Spigot
        this.methodProfiler.c("village");
        timings.doVillages.startTiming(); // Spigot
        this.villages.tick();
        this.siegeManager.a();
        timings.doVillages.stopTiming(); // Spigot
        this.methodProfiler.c("portalForcer");
        timings.doPortalForcer.startTiming(); // Spigot
        this.portalTravelAgent.a(this.getTime());
        timings.doPortalForcer.stopTiming(); // Spigot
        this.methodProfiler.b();
        timings.doSounds.startTiming(); // Spigot
        this.ao();
        timings.doSounds.stopTiming(); // Spigot

        timings.doChunkGC.startTiming();// Spigot
        this.getWorld().processChunkGC(); // CraftBukkit
        timings.doChunkGC.stopTiming(); // Spigot
    }

    @Nullable
    public BiomeBase.BiomeMeta a(EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
        List list = this.getChunkProviderServer().a(enumcreaturetype, blockposition);

        return list != null && !list.isEmpty() ? (BiomeBase.BiomeMeta) WeightedRandom.a(this.random, list) : null;
    }

    public boolean a(EnumCreatureType enumcreaturetype, BiomeBase.BiomeMeta biomebase_biomemeta, BlockPosition blockposition) {
        List list = this.getChunkProviderServer().a(enumcreaturetype, blockposition);

        return list != null && !list.isEmpty() ? list.contains(biomebase_biomemeta) : false;
    }

    public void everyoneSleeping() {
        this.O = false;
        if (!this.players.isEmpty()) {
            int i = 0;
            int j = 0;
            Iterator iterator = this.players.iterator();

            while (iterator.hasNext()) {
                EntityHuman entityhuman = (EntityHuman) iterator.next();

                if (entityhuman.isSpectator()) {
                    ++i;
                } else if (entityhuman.isSleeping() || entityhuman.fauxSleeping) {
                    ++j;
                }
            }

            this.O = j > 0 && j >= this.players.size() - i;
        }

    }

    protected void f() {
        this.O = false;
        Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            EntityHuman entityhuman = (EntityHuman) iterator.next();

            if (entityhuman.isSleeping()) {
                entityhuman.a(false, false, true);
            }
        }

        this.c();
    }

    private void c() {
        this.worldData.setStorm(false);
        // CraftBukkit start
        // If we stop due to everyone sleeping we should reset the weather duration to some other random value.
        // Not that everyone ever manages to get the whole server to sleep at the same time....
        if (!this.worldData.hasStorm()) {
            this.worldData.setWeatherDuration(0);
        }
        // CraftBukkit end
        this.worldData.setThundering(false);
        // CraftBukkit start
        // If we stop due to everyone sleeping we should reset the weather duration to some other random value.
        // Not that everyone ever manages to get the whole server to sleep at the same time....
        if (!this.worldData.isThundering()) {
            this.worldData.setThunderDuration(0);
        }
        // CraftBukkit end
    }

    public boolean everyoneDeeplySleeping() {
        if (this.O && !this.isClientSide) {
            Iterator iterator = this.players.iterator();

            // CraftBukkit - This allows us to assume that some people are in bed but not really, allowing time to pass in spite of AFKers
            boolean foundActualSleepers = false;

            EntityHuman entityhuman;

            do {
                if (!iterator.hasNext()) {
                    return foundActualSleepers;
                }

                entityhuman = (EntityHuman) iterator.next();

                // CraftBukkit start
                if (entityhuman.isDeeplySleeping()) {
                    foundActualSleepers = true;
                }
            } while (!entityhuman.isSpectator() || entityhuman.isDeeplySleeping() || entityhuman.fauxSleeping);
            // CraftBukkit end

            return false;
        } else {
            return false;
        }
    }

    protected boolean isChunkLoaded(int i, int j, boolean flag) {
        return this.getChunkProviderServer().isLoaded(i, j);
    }

    protected void i() {
        this.methodProfiler.a("playerCheckLight");
        if (spigotConfig.randomLightUpdates && !this.players.isEmpty()) { // Spigot
            int i = this.random.nextInt(this.players.size());
            EntityHuman entityhuman = (EntityHuman) this.players.get(i);
            int j = MathHelper.floor(entityhuman.locX) + this.random.nextInt(11) - 5;
            int k = MathHelper.floor(entityhuman.locY) + this.random.nextInt(11) - 5;
            int l = MathHelper.floor(entityhuman.locZ) + this.random.nextInt(11) - 5;

            this.w(new BlockPosition(j, k, l));
        }

        this.methodProfiler.b();
    }

	//important calculation
    light_tick l_task;
    chunk_tick c_tick;
    protected void j() {
        this.i();
        if (this.worldData.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            Iterator iterator = this.manager.b();

            while (iterator.hasNext()) {
                ((Chunk) iterator.next()).b(false);
            }

        } else {
            int i = this.getGameRules().c("randomTickSpeed");
            boolean flag = this.W();
            boolean flag1 = this.V();

            this.methodProfiler.a("pollingChunks");

            for (Iterator iterator1 = this.manager.b(); iterator1.hasNext(); this.methodProfiler.b()) {
                this.methodProfiler.a("getChunk");
                Chunk chunk = (Chunk) iterator1.next();
                int j = chunk.locX * 16;
                int k = chunk.locZ * 16;

                this.methodProfiler.c("checkNextLight");
                l_task = new light_tick(chunk);
                l_task.fork();
                //SpigotWorldConfig.LightQueue.add(new lighttick(chunk));
                this.methodProfiler.c("tickChunk");
                c_tick = new chunk_tick(chunk);
                c_tick.fork();
                if ( !chunk.areNeighborsLoaded( 1 ) ) continue; // Spigot
                this.methodProfiler.c("thunder");
                int l;
                BlockPosition blockposition;

                // Paper - Disable thunder
                if (!this.paperConfig.disableThunder && flag && flag1 && this.random.nextInt(100000) == 0) {
                    this.l = this.l * 3 + 1013904223;
                    l = this.l >> 2;
                    blockposition = this.a(new BlockPosition(j + (l & 15), 0, k + (l >> 8 & 15)));
                    if (this.isRainingAt(blockposition)) {
                        DifficultyDamageScaler difficultydamagescaler = this.D(blockposition);

                        if (this.random.nextDouble() < difficultydamagescaler.b() * paperConfig.skeleHorseSpawnChance) { // Paper - Configurable skeleton horse spawn chance
                            EntityHorse entityhorse = new EntityHorse(this);

                            entityhorse.setType(EnumHorseType.SKELETON);
                            entityhorse.x(true);
                            entityhorse.setAgeRaw(0);
                            entityhorse.setPosition((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());
                            this.addEntity(entityhorse, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.LIGHTNING); // CraftBukkit
                            this.strikeLightning(new EntityLightning(this, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), true));
                        } else {
                            this.strikeLightning(new EntityLightning(this, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), false));
                        }
                    }
                }

                this.methodProfiler.c("iceandsnow");
                if (!this.paperConfig.disableIceAndSnow && this.random.nextInt(16) == 0) { // Paper - Disable ice and snow
                    this.l = this.l * 3 + 1013904223;
                    l = this.l >> 2;
                    blockposition = this.p(new BlockPosition(j + (l & 15), 0, k + (l >> 8 & 15)));
                    BlockPosition blockposition1 = blockposition.down();

                    if (this.v(blockposition1)) {
                        // CraftBukkit start
                        BlockState blockState = this.getWorld().getBlockAt(blockposition1.getX(), blockposition1.getY(), blockposition1.getZ()).getState();
                        blockState.setTypeId(Block.getId(Blocks.ICE));

                        BlockFormEvent iceBlockForm = new BlockFormEvent(blockState.getBlock(), blockState);
                        this.getServer().getPluginManager().callEvent(iceBlockForm);
                        if (!iceBlockForm.isCancelled()) {
                            blockState.update(true);
                        }
                        // CraftBukkit end
                    }

                    if (flag && this.f(blockposition, true)) {
                        // CraftBukkit start
                        BlockState blockState = this.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()).getState();
                        blockState.setTypeId(Block.getId(Blocks.SNOW_LAYER));

                        BlockFormEvent snow = new BlockFormEvent(blockState.getBlock(), blockState);
                        this.getServer().getPluginManager().callEvent(snow);
                        if (!snow.isCancelled()) {
                            blockState.update(true);
                        }
                        // CraftBukkit end
                    }

                    if (flag && this.getBiome(blockposition1).d()) {
                        this.getType(blockposition1).getBlock().h(this, blockposition1);
                    }
                }

                timings.chunkTicksBlocks.startTiming(); // Paper
                if (i > 0) {
                    ChunkSection[] achunksection = chunk.getSections();
                    int i1 = achunksection.length;

                    for (int j1 = 0; j1 < i1; ++j1) {
                        ChunkSection chunksection = achunksection[j1];

                        if (chunksection != Chunk.a && chunksection.shouldTick()) {
                            for (int k1 = 0; k1 < i; ++k1) {
                                this.l = this.l * 3 + 1013904223;
                                int l1 = this.l >> 2;
                                int i2 = l1 & 15;
                                int j2 = l1 >> 8 & 15;
                                int k2 = l1 >> 16 & 15;
                                IBlockData iblockdata = chunksection.getType(i2, k2, j2);
                                Block block = iblockdata.getBlock();

                                this.methodProfiler.a("randomTick");
                                if (block.isTicking()) {
                                    block.a((World) this, new BlockPosition(i2 + j, k2 + chunksection.getYPosition(), j2 + k), iblockdata, this.random);
                                }

                                this.methodProfiler.b();
                            }
                        }
                    }
                }
                timings.chunkTicksBlocks.stopTiming(); // Paper
            }

            this.methodProfiler.b();
        }
    }

    protected BlockPosition a(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.p(blockposition);
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockposition1, new BlockPosition(blockposition1.getX(), this.getHeight(), blockposition1.getZ()))).g(3.0D);
        List list = this.a(EntityLiving.class, axisalignedbb, new Predicate() {
            public boolean a(@Nullable EntityLiving entityliving) {
                return entityliving != null && entityliving.isAlive() && WorldServer.this.h(entityliving.getChunkCoordinates());
            }

            public boolean apply(Object object) {
                return this.a((EntityLiving) object);
            }
        });

        if (!list.isEmpty()) {
            List llll = new ArrayList(list);
            return ((EntityLiving) llll.get(this.random.nextInt(list.size()))).getChunkCoordinates();
        } else {
            if (blockposition1.getY() == -1) {
                blockposition1 = blockposition1.up(2);
            }

            return blockposition1;
        }
    }

    public boolean a(BlockPosition blockposition, Block block) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(blockposition, block);

        return this.U.contains(nextticklistentry);
    }

    public boolean b(BlockPosition blockposition, Block block) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(blockposition, block);

        return this.nextTickList.contains(nextticklistentry); // CraftBukkit
    }

    public void a(BlockPosition blockposition, Block block, int i) {
        this.a(blockposition, block, i, 0);
    }

    public void a(BlockPosition blockposition, Block block, int i, int j) {
        if (blockposition instanceof BlockPosition.MutableBlockPosition || blockposition instanceof BlockPosition.PooledBlockPosition) {
            blockposition = new BlockPosition(blockposition);
            LogManager.getLogger().warn("Tried to assign a mutable BlockPos to tick data...", new Error(blockposition.getClass().toString()));
        }

        Material material = block.getBlockData().getMaterial();

        if (this.d && material != Material.AIR) {
            if (block.s()) {
                if (this.areChunksLoadedBetween(blockposition.a(-8, -8, -8), blockposition.a(8, 8, 8))) {
                    IBlockData iblockdata = this.getType(blockposition);

                    if (iblockdata.getMaterial() != Material.AIR && iblockdata.getBlock() == block) {
                        iblockdata.getBlock().b((World) this, blockposition, iblockdata, this.random);
                    }
                }

                return;
            }

            i = 1;
        }

        NextTickListEntry nextticklistentry = new NextTickListEntry(blockposition, block);

        if (this.isLoaded(blockposition)) {
            if (material != Material.AIR) {
                nextticklistentry.a((long) i + this.worldData.getTime());
                nextticklistentry.a(j);
            }

            // CraftBukkit - use nextTickList
            if (!this.nextTickList.contains(nextticklistentry)) {
                this.nextTickList.add(nextticklistentry);
            }
        }

    }

    public void b(BlockPosition blockposition, Block block, int i, int j) {
        if (blockposition instanceof BlockPosition.MutableBlockPosition || blockposition instanceof BlockPosition.PooledBlockPosition) {
            blockposition = new BlockPosition(blockposition);
            LogManager.getLogger().warn("Tried to assign a mutable BlockPos to tick data...", new Error(blockposition.getClass().toString()));
        }

        NextTickListEntry nextticklistentry = new NextTickListEntry(blockposition, block);

        nextticklistentry.a(j);
        Material material = block.getBlockData().getMaterial();

        if (material != Material.AIR) {
            nextticklistentry.a((long) i + this.worldData.getTime());
        }

        // CraftBukkit - use nextTickList
        if (!this.nextTickList.contains(nextticklistentry)) {
            this.nextTickList.add(nextticklistentry);
        }

    }

    public void tickEntities() {
        if (false && this.players.isEmpty()) { // CraftBukkit - this prevents entity cleanup, other issues on servers with no players
            if (this.emptyTime++ >= 300) {
                return;
            }
        } else {
            this.m();
        }

        this.worldProvider.r();
        super.tickEntities();
        spigotConfig.currentPrimedTnt = 0; // Spigot
    }

    protected void l() {
        super.l();
        this.methodProfiler.c("players");

        for (int i = 0; i < this.players.size(); ++i) {
            Entity entity = (Entity) this.players.get(i);
            Entity entity1 = entity.bz();

            if (entity1 != null) {
                if (!entity1.dead && entity1.w(entity)) {
                    continue;
                }

                entity.stopRiding();
            }

            this.methodProfiler.a("tick");
            if (!entity.dead) {
                try {
                    this.g(entity);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.a(throwable, "Ticking player");
                    CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Player being ticked");

                    entity.appendEntityCrashDetails(crashreportsystemdetails);
                    throw new ReportedException(crashreport);
                }
            }

            this.methodProfiler.b();
            this.methodProfiler.a("remove");
            if (entity.dead) {
                int j = entity.ab;
                int k = entity.ad;

                if (entity.aa && this.isChunkLoaded(j, k, true)) {
                    this.getChunkAt(j, k).b(entity);
                }

                this.entityList.remove(entity);
                this.c(entity);
            }

            this.methodProfiler.b();
        }

    }

    public void m() {
        this.emptyTime = 0;
    }

    public boolean a(boolean flag) {
        if (this.worldData.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            return false;
        } else {
            int i = this.nextTickList.size();

            if (false) { // CraftBukkit
                throw new IllegalStateException("TickNextTick list out of synch");
            } else {
                if (i > 65536) {
                    // CraftBukkit start - If the server has too much to process over time, try to alleviate that
                    if (i > 20 * 65536) {
                        i = i / 20;
                    } else {
                        i = 65536;
                    }
                    // CraftBukkit end
                }

                this.methodProfiler.a("cleaning");

                timings.scheduledBlocksCleanup.startTiming(); // Paper
                NextTickListEntry nextticklistentry;

                for (int j = 0; j < i; ++j) {
                    try {
                        nextticklistentry = (NextTickListEntry) this.nextTickList.first();
                    } catch (Exception ex) {
                        continue;
                    }
                    if (!flag && nextticklistentry.b > this.worldData.getTime()) {
                        break;
                    }

                    // CraftBukkit - use nextTickList
                    this.nextTickList.remove(nextticklistentry);
                    // this.nextTickListHash.remove(nextticklistentry);
                    this.U.add(nextticklistentry);
                }
                timings.scheduledBlocksCleanup.stopTiming(); // Paper

                this.methodProfiler.b();
                this.methodProfiler.a("ticking");
                timings.scheduledBlocksTicking.startTiming(); // Paper
                Iterator iterator = this.U.iterator();

                while (iterator.hasNext()) {
                    nextticklistentry = (NextTickListEntry) iterator.next();
                    iterator.remove();
                    byte b0 = 0;

                    if (this.areChunksLoadedBetween(nextticklistentry.a.a(-b0, -b0, -b0), nextticklistentry.a.a(b0, b0, b0))) {
                        IBlockData iblockdata = this.getType(nextticklistentry.a);
                        co.aikar.timings.Timing timing = iblockdata.getBlock().getTiming(); // Paper
                        timing.startTiming(); // Paper

                        if (iblockdata.getMaterial() != Material.AIR && Block.a(iblockdata.getBlock(), nextticklistentry.a())) {
                            try {
                                stopPhysicsEvent = !paperConfig.firePhysicsEventForRedstone && (iblockdata.getBlock() instanceof BlockDiodeAbstract || iblockdata.getBlock() instanceof BlockRedstoneTorch); // Paper
                                iblockdata.getBlock().b((World) this, nextticklistentry.a, iblockdata, this.random);
                            } catch (Throwable throwable) {
                                CrashReport crashreport = CrashReport.a(throwable, "Exception while ticking a block");
                                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being ticked");

                                CrashReportSystemDetails.a(crashreportsystemdetails, nextticklistentry.a, iblockdata);
                                throw new ReportedException(crashreport);
                            } finally { stopPhysicsEvent = false; } // Paper
                        }
                        timing.stopTiming(); // Paper
                    } else {
                        this.a(nextticklistentry.a, nextticklistentry.a(), 0);
                    }
                }
                timings.scheduledBlocksTicking.stopTiming(); // Paper

                this.methodProfiler.b();
                this.U.clear();
                return !this.nextTickList.isEmpty();
            }
        }
    }

    @Nullable
    public List<NextTickListEntry> a(Chunk chunk, boolean flag) {
        ChunkCoordIntPair chunkcoordintpair = chunk.k();
        int i = (chunkcoordintpair.x << 4) - 2;
        int j = i + 16 + 2;
        int k = (chunkcoordintpair.z << 4) - 2;
        int l = k + 16 + 2;

        return this.a(new StructureBoundingBox(i, 0, k, j, 256, l), flag);
    }

    @Nullable
    public List<NextTickListEntry> a(StructureBoundingBox structureboundingbox, boolean flag) {
        ArrayList arraylist = null;

        for (int i = 0; i < 2; ++i) {
            Iterator iterator;

            if (i == 0) {
                iterator = this.nextTickList.iterator();
            } else {
                iterator = this.U.iterator();
            }

            while (iterator.hasNext()) {
                NextTickListEntry nextticklistentry = (NextTickListEntry) iterator.next();
                BlockPosition blockposition = nextticklistentry.a;

                if (blockposition.getX() >= structureboundingbox.a && blockposition.getX() < structureboundingbox.d && blockposition.getZ() >= structureboundingbox.c && blockposition.getZ() < structureboundingbox.f) {
                    if (flag) {
                        if (i == 0) {
                            // this.nextTickListHash.remove(nextticklistentry); // CraftBukkit - removed
                        }

                        iterator.remove();
                    }

                    if (arraylist == null) {
                        arraylist = Lists.newArrayList();
                    }

                    arraylist.add(nextticklistentry);
                }
            }
        }

        return arraylist;
    }

    /* CraftBukkit start - We prevent spawning in general, so this butchering is not needed
    public void entityJoinedWorld(Entity entity, boolean flag) {
        if (!this.getSpawnAnimals() && (entity instanceof EntityAnimal || entity instanceof EntityWaterAnimal)) {
            entity.die();
        }

        if (!this.getSpawnNPCs() && entity instanceof NPC) {
            entity.die();
        }

        super.entityJoinedWorld(entity, flag);
    }
    // CraftBukkit end */

    private boolean getSpawnNPCs() {
        return this.server.getSpawnNPCs();
    }

    private boolean getSpawnAnimals() {
        return this.server.getSpawnAnimals();
    }

    protected IChunkProvider n() {
        IChunkLoader ichunkloader = this.dataManager.createChunkLoader(this.worldProvider);

        // CraftBukkit start
        org.bukkit.craftbukkit.generator.InternalChunkGenerator gen;

        if (this.generator != null) {
            gen = new org.bukkit.craftbukkit.generator.CustomChunkGenerator(this, this.getSeed(), this.generator);
        } else if (this.worldProvider instanceof WorldProviderHell) {
            gen = new org.bukkit.craftbukkit.generator.NetherChunkGenerator(this, this.getSeed());
        } else if (this.worldProvider instanceof WorldProviderTheEnd) {
            gen = new org.bukkit.craftbukkit.generator.SkyLandsChunkGenerator(this, this.getSeed());
        } else {
            gen = new org.bukkit.craftbukkit.generator.NormalChunkGenerator(this, this.getSeed());
        }

        return new ChunkProviderServer(this, ichunkloader, gen);
        // CraftBukkit end
    }

    public List<TileEntity> getTileEntities(int i, int j, int k, int l, int i1, int j1) {
        ArrayList arraylist = Lists.newArrayList();

        // CraftBukkit start - Get tile entities from chunks instead of world
        for (int chunkX = (i >> 4); chunkX <= ((l - 1) >> 4); chunkX++) {
            for (int chunkZ = (k >> 4); chunkZ <= ((j1 - 1) >> 4); chunkZ++) {
                Chunk chunk = getChunkAt(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (Object te : chunk.tileEntities.values()) {
                    TileEntity tileentity = (TileEntity) te;
                    if ((tileentity.position.getX() >= i) && (tileentity.position.getY() >= j) && (tileentity.position.getZ() >= k) && (tileentity.position.getX() < l) && (tileentity.position.getY() < i1) && (tileentity.position.getZ() < j1)) {
                        arraylist.add(tileentity);
                    }
                }
            }
        }
        /*
        for (int k1 = 0; k1 < this.tileEntityList.size(); ++k1) {
            TileEntity tileentity = (TileEntity) this.tileEntityList.get(k1);
            BlockPosition blockposition = tileentity.getPosition();

            if (blockposition.getX() >= i && blockposition.getY() >= j && blockposition.getZ() >= k && blockposition.getX() < l && blockposition.getY() < i1 && blockposition.getZ() < j1) {
                arraylist.add(tileentity);
            }
        }
        */
        // CraftBukkit end

        return arraylist;
    }

    public boolean a(EntityHuman entityhuman, BlockPosition blockposition) {
        return !this.server.a(this, blockposition, entityhuman) && this.getWorldBorder().a(blockposition);
    }

    public void a(WorldSettings worldsettings) {
        if (!this.worldData.v()) {
            try {
                this.b(worldsettings);
                if (this.worldData.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
                    this.an();
                }

                super.a(worldsettings);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Exception initializing level");

                try {
                    this.a(crashreport);
                } catch (Throwable throwable1) {
                    ;
                }

                throw new ReportedException(crashreport);
            }

            this.worldData.d(true);
        }

    }

    private void an() {
        this.worldData.f(false);
        this.worldData.c(true);
        this.worldData.setStorm(false);
        this.worldData.setThundering(false);
        this.worldData.i(1000000000);
        this.worldData.setDayTime(6000L);
        this.worldData.setGameType(WorldSettings.EnumGamemode.SPECTATOR);
        this.worldData.g(false);
        this.worldData.setDifficulty(EnumDifficulty.PEACEFUL);
        this.worldData.e(true);
        this.getGameRules().set("doDaylightCycle", "false");
    }

    private void b(WorldSettings worldsettings) {
        if (!this.worldProvider.e()) {
            this.worldData.setSpawn(BlockPosition.ZERO.up(this.worldProvider.getSeaLevel()));
        } else if (this.worldData.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            this.worldData.setSpawn(BlockPosition.ZERO.up());
        } else {
            this.isLoading = true;
            WorldChunkManager worldchunkmanager = this.worldProvider.k();
            List list = worldchunkmanager.a();
            Random random = new Random(this.getSeed());
            BlockPosition blockposition = worldchunkmanager.a(0, 0, 256, list, random);
            int i = 8;
            int j = this.worldProvider.getSeaLevel();
            int k = 8;

            // CraftBukkit start
            if (this.generator != null) {
                Random rand = new Random(this.getSeed());
                org.bukkit.Location spawn = this.generator.getFixedSpawnLocation(((WorldServer) this).getWorld(), rand);

                if (spawn != null) {
                    if (spawn.getWorld() != ((WorldServer) this).getWorld()) {
                        throw new IllegalStateException("Cannot set spawn point for " + this.worldData.getName() + " to be in another world (" + spawn.getWorld().getName() + ")");
                    } else {
                        this.worldData.setSpawn(new BlockPosition(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()));
                        this.isLoading = false;
                        return;
                    }
                }
            }
            // CraftBukkit end

            if (blockposition != null) {
                i = blockposition.getX();
                k = blockposition.getZ();
            } else {
                WorldServer.a.warn("Unable to find spawn biome");
            }

            int l = 0;

            while (!this.canSpawn(i, k)) { // CraftBukkit - use our own canSpawn
                i += random.nextInt(64) - random.nextInt(64);
                k += random.nextInt(64) - random.nextInt(64);
                ++l;
                if (l == 1000) {
                    break;
                }
            }

            this.worldData.setSpawn(new BlockPosition(i, j, k));
            this.isLoading = false;
            if (worldsettings.c()) {
                this.o();
            }

        }
    }

    protected void o() {
        WorldGenBonusChest worldgenbonuschest = new WorldGenBonusChest();

        for (int i = 0; i < 10; ++i) {
            int j = this.worldData.b() + this.random.nextInt(6) - this.random.nextInt(6);
            int k = this.worldData.d() + this.random.nextInt(6) - this.random.nextInt(6);
            BlockPosition blockposition = this.q(new BlockPosition(j, 0, k)).up();

            if (worldgenbonuschest.generate(this, this.random, blockposition)) {
                break;
            }
        }

    }

    public BlockPosition getDimensionSpawn() {
        return this.worldProvider.h();
    }

    public void save(boolean flag, @Nullable IProgressUpdate iprogressupdate) throws ExceptionWorldConflict {
        ChunkProviderServer chunkproviderserver = this.getChunkProviderServer();

        if (chunkproviderserver.e()) {
            org.bukkit.Bukkit.getPluginManager().callEvent(new org.bukkit.event.world.WorldSaveEvent(getWorld())); // CraftBukkit
            if (iprogressupdate != null) {
                iprogressupdate.a("Saving level");
            }

            this.a();
            if (iprogressupdate != null) {
                iprogressupdate.c("Saving chunks");
            }

            chunkproviderserver.a(flag);
            // CraftBukkit - ArrayList -> Collection
            Collection arraylist = chunkproviderserver.a();
            Iterator iterator = arraylist.iterator();

            while (iterator.hasNext()) {
                Chunk chunk = (Chunk) iterator.next();

                if (chunk != null && !this.manager.a(chunk.locX, chunk.locZ)) {
                    chunkproviderserver.unload(chunk);
                }
            }

        }
    }

    public void flushSave() {
        ChunkProviderServer chunkproviderserver = this.getChunkProviderServer();

        if (chunkproviderserver.e()) {
            chunkproviderserver.c();
        }
    }

    protected void a() throws ExceptionWorldConflict {
        this.checkSession();
        WorldServer[] aworldserver = this.server.worldServer;
        int i = aworldserver.length;

        for (int j = 0; j < i; ++j) {
            WorldServer worldserver = aworldserver[j];

            if (worldserver instanceof SecondaryWorldServer) {
                ((SecondaryWorldServer) worldserver).c();
            }
        }

        // CraftBukkit start - Save secondary data for nether/end
        if (this instanceof SecondaryWorldServer) {
            ((SecondaryWorldServer) this).c();
        }
        // CraftBukkit end

        this.worldData.a(this.getWorldBorder().getSize());
        this.worldData.d(this.getWorldBorder().getCenterX());
        this.worldData.c(this.getWorldBorder().getCenterZ());
        this.worldData.e(this.getWorldBorder().getDamageBuffer());
        this.worldData.f(this.getWorldBorder().getDamageAmount());
        this.worldData.j(this.getWorldBorder().getWarningDistance());
        this.worldData.k(this.getWorldBorder().getWarningTime());
        this.worldData.b(this.getWorldBorder().j());
        this.worldData.e(this.getWorldBorder().i());
        // CraftBukkit start - save worldMaps once, rather than once per shared world
        if (!(this instanceof SecondaryWorldServer)) {
            this.worldMaps.a();
        }
        this.dataManager.saveWorldData(this.worldData, this.server.getPlayerList().t());
        // CraftBukkit end
    }

    public boolean addEntity(Entity entity) {
        return this.i(entity) ? super.addEntity(entity) : false;
    }

    public void a(Collection<Entity> collection) {
        ArrayList arraylist = Lists.newArrayList(collection);
        Iterator iterator = arraylist.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            if (this.i(entity)) {
                this.entityList.add(entity);
                this.b(entity);
            }
        }

    }

    private boolean i(Entity entity) {
        if (entity.dead) {
            // WorldServer.a.warn("Tried to add entity " + EntityTypes.b(entity) + " but it was marked as removed already"); // CraftBukkit
            return false;
        } else {
            UUID uuid = entity.getUniqueID();

            if (this.entitiesByUUID.containsKey(uuid)) {
                Entity entity1 = (Entity) this.entitiesByUUID.get(uuid);

                if (this.f.contains(entity1)) {
                    this.f.remove(entity1);
                } else {
                    if (!(entity instanceof EntityHuman)) {
                        // WorldServer.a.warn("Keeping entity " + EntityTypes.b(entity1) + " that already exists with UUID " + uuid.toString()); // CraftBukkit
                        return false;
                    }

                    WorldServer.a.warn("Force-added player with duplicate UUID " + uuid.toString());
                }

                this.removeEntity(entity1);
            }

            return true;
        }
    }

    protected void b(Entity entity) {
        super.b(entity);
        this.entitiesById.put(entity.getId(), entity);
        this.entitiesByUUID.put(entity.getUniqueID(), entity);
        Entity[] aentity = entity.aR();

        if (aentity != null) {
            for (int i = 0; i < aentity.length; ++i) {
				Entity entity1 = aentity[i]; // Torch
                this.entitiesById.put(entity1.getId(), entity1);
            }
        }

    }

    protected void c(Entity entity) {
        super.c(entity);
        this.entitiesById.remove(entity.getId());
        this.entitiesByUUID.remove(entity.getUniqueID());
        Entity[] aentity = entity.aR();

        if (aentity != null) {
            for (int i = 0; i < aentity.length; ++i) {
                Entity entity1 = aentity[i]; // Torch
				this.entitiesById.remove(entity1.getId());
            }
        }

    }

    public boolean strikeLightning(Entity entity) {
        // CraftBukkit start
        LightningStrikeEvent lightning = new LightningStrikeEvent(this.getWorld(), (org.bukkit.entity.LightningStrike) entity.getBukkitEntity());
        this.getServer().getPluginManager().callEvent(lightning);

        if (lightning.isCancelled()) {
            return false;
        }
        // CraftBukkit end
        if (super.strikeLightning(entity)) {
            this.server.getPlayerList().sendPacketNearby((EntityHuman) null, entity.locX, entity.locY, entity.locZ, 512.0D, dimension, new PacketPlayOutSpawnEntityWeather(entity)); // CraftBukkit - Use dimension
            return true;
        } else {
            return false;
        }
    }

    public void broadcastEntityEffect(Entity entity, byte b0) {
        this.getTracker().sendPacketToEntity(entity, new PacketPlayOutEntityStatus(entity, b0));
    }

    public ChunkProviderServer getChunkProviderServer() {
        return (ChunkProviderServer) super.getChunkProvider();
    }

    public Explosion createExplosion(@Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, boolean flag1) {
        // CraftBukkit start
        Explosion explosion = super.createExplosion(entity, d0, d1, d2, f, flag, flag1);

        if (explosion.wasCanceled) {
            return explosion;
        }

        /* Remove
        Explosion explosion = new Explosion(this, entity, d0, d1, d2, f, flag, flag1);

        explosion.a();
        explosion.a(false);
        */
        // CraftBukkit end - TODO: Check if explosions are still properly implemented
        if (!flag1) {
            explosion.clearBlocks();
        }

        Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            EntityHuman entityhuman = (EntityHuman) iterator.next();

            if (entityhuman.e(d0, d1, d2) < 4096.0D) {
                ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutExplosion(d0, d1, d2, f, explosion.getBlocks(), (Vec3D) explosion.b().get(entityhuman)));
            }
        }

        return explosion;
    }

    public void playBlockAction(BlockPosition blockposition, Block block, int i, int j) {
        BlockActionData blockactiondata = new BlockActionData(blockposition, block, i, j);
        Iterator iterator = this.S[this.T].iterator();

        BlockActionData blockactiondata1;

        do {
            if (!iterator.hasNext()) {
                this.S[this.T].add(blockactiondata);
                return;
            }

            blockactiondata1 = (BlockActionData) iterator.next();
        } while (!blockactiondata1.equals(blockactiondata));

    }

    private void ao() {
        while (!this.S[this.T].isEmpty()) {
            int i = this.T;

            this.T ^= 1;
            Iterator iterator = this.S[i].iterator();

            while (iterator.hasNext()) {
                BlockActionData blockactiondata = (BlockActionData) iterator.next();

                if (this.a(blockactiondata)) {
                    // CraftBukkit - this.worldProvider.dimension -> this.dimension
                    this.server.getPlayerList().sendPacketNearby((EntityHuman) null, (double) blockactiondata.a().getX(), (double) blockactiondata.a().getY(), (double) blockactiondata.a().getZ(), 64.0D, dimension, new PacketPlayOutBlockAction(blockactiondata.a(), blockactiondata.d(), blockactiondata.b(), blockactiondata.c()));
                }
            }

            this.S[i].clear();
        }

    }

    private boolean a(BlockActionData blockactiondata) {
        IBlockData iblockdata = this.getType(blockactiondata.a());

        return iblockdata.getBlock() == blockactiondata.d() ? iblockdata.a(this, blockactiondata.a(), blockactiondata.b(), blockactiondata.c()) : false;
    }

    public void saveLevel() {
        this.dataManager.a();
    }

    protected void t() {
        boolean flag = this.W();

        super.t();
        /* CraftBukkit start
        if (this.n != this.o) {
            this.server.getPlayerList().a((Packet) (new PacketPlayOutGameStateChange(7, this.o)), this.worldProvider.getDimensionManager().getDimensionID());
        }

        if (this.p != this.q) {
            this.server.getPlayerList().a((Packet) (new PacketPlayOutGameStateChange(8, this.q)), this.worldProvider.getDimensionManager().getDimensionID());
        }

        if (flag != this.W()) {
            if (flag) {
                this.server.getPlayerList().sendAll(new PacketPlayOutGameStateChange(2, 0.0F));
            } else {
                this.server.getPlayerList().sendAll(new PacketPlayOutGameStateChange(1, 0.0F));
            }

            this.server.getPlayerList().sendAll(new PacketPlayOutGameStateChange(7, this.o));
            this.server.getPlayerList().sendAll(new PacketPlayOutGameStateChange(8, this.q));
        }
        // */
        if (flag != this.W()) {
            // Only send weather packets to those affected
            for (int i = 0; i < this.players.size(); ++i) {
                if (((EntityPlayer) this.players.get(i)).world == this) {
                    ((EntityPlayer) this.players.get(i)).setPlayerWeather((!flag ? WeatherType.DOWNFALL : WeatherType.CLEAR), false);
                }
            }
        }
        for (int i = 0; i < this.players.size(); ++i) {
            if (((EntityPlayer) this.players.get(i)).world == this) {
                ((EntityPlayer) this.players.get(i)).updateWeather(this.n, this.o, this.p, this.q);
            }
        }
        // CraftBukkit end

    }

    @Nullable
    public MinecraftServer getMinecraftServer() {
        return this.server;
    }

    public EntityTracker getTracker() {
        return this.tracker;
    }

    public PlayerChunkMap getPlayerChunkMap() {
        return this.manager;
    }

    public PortalTravelAgent getTravelAgent() {
        return this.portalTravelAgent;
    }

    public DefinedStructureManager y() {
        return this.dataManager.h();
    }

    public void a(EnumParticle enumparticle, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, int... aint) {
        this.a(enumparticle, false, d0, d1, d2, i, d3, d4, d5, d6, aint);
    }

    public void a(EnumParticle enumparticle, boolean flag, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, int... aint) {
        // CraftBukkit - visibility api support
        sendParticles(null, enumparticle, flag, d0, d1, d2, i, d3, d4, d5, d6, aint);
    }

    public void sendParticles(EntityPlayer sender, EnumParticle enumparticle, boolean flag, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, int... aint) {
        // CraftBukkit end
        PacketPlayOutWorldParticles packetplayoutworldparticles = new PacketPlayOutWorldParticles(enumparticle, flag, (float) d0, (float) d1, (float) d2, (float) d3, (float) d4, (float) d5, (float) d6, i, aint);

        for (int j = 0; j < this.players.size(); ++j) {
            EntityPlayer entityplayer = (EntityPlayer) this.players.get(j);
            if (sender != null && !entityplayer.getBukkitEntity().canSee(sender.getBukkitEntity())) continue; // CraftBukkit
            BlockPosition blockposition = entityplayer.getChunkCoordinates();
            double d7 = blockposition.distanceSquared(d0, d1, d2);


            this.a(entityplayer, flag, d0, d1, d2, packetplayoutworldparticles);
        }

    }

    public void a(EntityPlayer entityplayer, EnumParticle enumparticle, boolean flag, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, int... aint) {
        PacketPlayOutWorldParticles packetplayoutworldparticles = new PacketPlayOutWorldParticles(enumparticle, flag, (float) d0, (float) d1, (float) d2, (float) d3, (float) d4, (float) d5, (float) d6, i, aint);

        this.a(entityplayer, flag, d0, d1, d2, packetplayoutworldparticles);
    }

    private void a(EntityPlayer entityplayer, boolean flag, double d0, double d1, double d2, Packet<?> packet) {
        BlockPosition blockposition = entityplayer.getChunkCoordinates();
        double d3 = blockposition.distanceSquared(d0, d1, d2);

        if (d3 <= 1024.0D || flag && d3 <= 262144.0D) {
            entityplayer.playerConnection.sendPacket(packet);
        }

    }

    @Nullable
    public Entity getEntity(UUID uuid) {
        return (Entity) this.entitiesByUUID.get(uuid);
    }

    public ListenableFuture<Object> postToMainThread(Runnable runnable) {
        return this.server.postToMainThread(runnable);
    }

    public boolean isMainThread() {
        return this.server.isMainThread();
    }

    public IChunkProvider getChunkProvider() {
        return this.getChunkProviderServer();
    }

    static class BlockActionDataList extends ArrayList<BlockActionData> {

        private BlockActionDataList() {}

        BlockActionDataList(Object object) {
            this();
        }
    }
	
	class lighttick extends RecursiveAction {
     Chunk chunk;

     public lighttick(Chunk chunk){
         this.chunk = chunk;
     }

     @Override
     protected void compute() {
         chunk.n();
     }

 }
 
class block_tick extends RecursiveAction {
     WorldServer worldserver;
     int i2, j, k2, j2, k;
     ChunkSection chunksection;
     IBlockData iblockdata;
     
     block_tick(WorldServer worldserver, int i2, int j, int k2, int j2, int k, ChunkSection chunksection, IBlockData iblockdata) {
         this.worldserver = worldserver;
         this.i2 = i2;
         this.j = j;
         this.k2 = k2;
         this.j2 = j2;
         this.k = k;
         this.chunksection = chunksection;
         this.iblockdata = iblockdata;
     }
     
     @Override
     protected void compute() {
         iblockdata.getBlock().a((World) worldserver, new BlockPosition(i2 + j, k2 + chunksection.getYPosition(), j2 + k), iblockdata, worldserver.random);
     }
              
 }
 
 class light_tick extends RecursiveAction {
     Chunk chunk;
     
     light_tick(Chunk chunk) {
         this.chunk = chunk;
     }
     
     @Override
     protected void compute() {
         chunk.n();
     }
              
 }
 
 class chunk_tick extends RecursiveAction {
     Chunk chunk;
     
     chunk_tick(Chunk chunk) {
         this.chunk = chunk;
     }
     
     @Override
     protected void compute() {
         chunk.b(false);
     }
              
 }
}
