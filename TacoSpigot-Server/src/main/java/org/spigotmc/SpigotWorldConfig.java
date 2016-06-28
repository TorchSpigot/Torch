package org.spigotmc;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.Queues;
import io.netty.util.internal.ConcurrentSet;
import java.util.Set;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveAction;

public class SpigotWorldConfig
{
	
	//hose start
 static public final int cpu_core = Runtime.getRuntime().availableProcessors();
 static final ForkJoinPool.ForkJoinWorkerThreadFactory fetp = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
     @Override
     public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
         final ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
         thread.setName("EntityTickPool-" + thread.getPoolIndex());
         return thread;
     }
 };
 static final ForkJoinPool.ForkJoinWorkerThreadFactory fttp = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
     @Override
     public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
         final ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
         thread.setName("TileTickPool-" + thread.getPoolIndex());
         return thread;
     }
 };
 static final ForkJoinPool.ForkJoinWorkerThreadFactory fcp = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
     @Override
     public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
         final ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
         thread.setName("ChunkPool-" + thread.getPoolIndex());
         return thread;
     }
 };
 
 static final ForkJoinPool.ForkJoinWorkerThreadFactory fpcp = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
     @Override
     public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
         final ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
         thread.setName("PlayerChunkPool-" + thread.getPoolIndex());
         return thread;
     }
 };
 
 static final ForkJoinPool.ForkJoinWorkerThreadFactory fcfp = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
     @Override
     public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
         final ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
         thread.setName("ChunkFlushPool-" + thread.getPoolIndex());
         return thread;
     }
 };
 
 static final ForkJoinPool.ForkJoinWorkerThreadFactory fcfpt3 = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
     @Override
     public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
         final ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
         thread.setName("ChunkFlushPool_T3-" + thread.getPoolIndex());
         return thread;
     }
 };
 
 static final ForkJoinPool.ForkJoinWorkerThreadFactory fcpp = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
     @Override
     public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
         final ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
         thread.setName("ChunkPopulate_Pool-" + thread.getPoolIndex());
         return thread;
     }
 };
 
 public static ForkJoinPool EntityTickPool;
 public static ForkJoinPool TileTickPool;
 public static ForkJoinPool ChunkPool;
 public static ForkJoinPool PlayerChunkPool;
 public static ForkJoinPool ChunkFlushPool;
 public static ForkJoinPool ChunkFlushPool_T3;
 public static ForkJoinPool ChunkPopulate_Pool;
 
 /*
 static public FutureTask CollideTask;
     
 static final ThreadFactory tfcp = new ThreadFactoryBuilder().setNameFormat("Collide_Pool-%d").build();
 static public final ExecutorService collidepool_executor = Executors.newFixedThreadPool(cpu_core, tfcp);*/
 
 public static int worldtickentitypool_core_multiple;
 
 public static int getthreadnumber(){
     return cpu_core*worldtickentitypool_core_multiple;
 }
 
 private void wetp_core_multiple()
 {
     worldtickentitypool_core_multiple = getInt("wetp-core-multiple", 4);
     EntityTickPool = new ForkJoinPool(getthreadnumber(), fetp, null, false);
     TileTickPool = new ForkJoinPool(getthreadnumber(), fttp, null, false);
     ChunkPool = new ForkJoinPool(getthreadnumber(), fcp, null, false);
     PlayerChunkPool = new ForkJoinPool(getthreadnumber(), fpcp, null, false);
     ChunkFlushPool = new ForkJoinPool(getthreadnumber(), fcfp, null, false);
     ChunkFlushPool_T3 = new ForkJoinPool(getthreadnumber(), fcfpt3, null, false);
     ChunkPopulate_Pool = new ForkJoinPool(getthreadnumber(), fcpp, null, false);
     //executor = new ThreadPoolExecutor(getthreadnumber(), getthreadnumber(), 1, TimeUnit.DAYS, queue);
     log("WETP Core Multiple: " + worldtickentitypool_core_multiple);
 }
 
 
 //hose end

    private final String worldName;
    private final YamlConfiguration config;
    private boolean verbose;

    public SpigotWorldConfig(String worldName)
    {
        this.worldName = worldName;
        this.config = SpigotConfig.config;
        init();
    }

    public void init()
    {
        this.verbose = getBoolean( "verbose", true );

        log( "-------- World Settings For [" + worldName + "] --------" );
        SpigotConfig.readConfig( SpigotWorldConfig.class, this );
    }

    private void log(String s)
    {
        if ( verbose )
        {
            Bukkit.getLogger().info( s );
        }
    }

    private void set(String path, Object val)
    {
        config.set( "world-settings.default." + path, val );
    }

    private boolean getBoolean(String path, boolean def)
    {
        config.addDefault( "world-settings.default." + path, def );
        return config.getBoolean( "world-settings." + worldName + "." + path, config.getBoolean( "world-settings.default." + path ) );
    }

    private double getDouble(String path, double def)
    {
        config.addDefault( "world-settings.default." + path, def );
        return config.getDouble( "world-settings." + worldName + "." + path, config.getDouble( "world-settings.default." + path ) );
    }

    private int getInt(String path, int def)
    {
        config.addDefault( "world-settings.default." + path, def );
        return config.getInt( "world-settings." + worldName + "." + path, config.getInt( "world-settings.default." + path ) );
    }

    private <T> List getList(String path, T def)
    {
        config.addDefault( "world-settings.default." + path, def );
        return (List<T>) config.getList( "world-settings." + worldName + "." + path, config.getList( "world-settings.default." + path ) );
    }

    private String getString(String path, String def)
    {
        config.addDefault( "world-settings.default." + path, def );
        return config.getString( "world-settings." + worldName + "." + path, config.getString( "world-settings.default." + path ) );
    }

    // Crop growth rates
    public int cactusModifier;
    public int caneModifier;
    public int melonModifier;
    public int mushroomModifier;
    public int pumpkinModifier;
    public int saplingModifier;
    public int wheatModifier;
    public int wartModifier;
    private int getAndValidateGrowth(String crop)
    {
        int modifier = getInt( "growth." + crop.toLowerCase() + "-modifier", 100 );
        if ( modifier == 0 )
        {
            log( "Cannot set " + crop + " growth to zero, defaulting to 100" );
            modifier = 100;
        }
        log( crop + " Growth Modifier: " + modifier + "%" );

        return modifier;
    }
    private void growthModifiers()
    {
        cactusModifier = getAndValidateGrowth( "Cactus" );
        caneModifier = getAndValidateGrowth( "Cane" );
        melonModifier = getAndValidateGrowth( "Melon" );
        mushroomModifier = getAndValidateGrowth( "Mushroom" );
        pumpkinModifier = getAndValidateGrowth( "Pumpkin" );
        saplingModifier = getAndValidateGrowth( "Sapling" );
        wheatModifier = getAndValidateGrowth( "Wheat" );
        wartModifier = getAndValidateGrowth( "NetherWart" );
    }

    public double itemMerge;
    private void itemMerge()
    {
        itemMerge = getDouble("merge-radius.item", 2.5 );
        log( "Item Merge Radius: " + itemMerge );
    }

    public double expMerge;
    private void expMerge()
    {
        expMerge = getDouble("merge-radius.exp", 3.0 );
        log( "Experience Merge Radius: " + expMerge );
    }

    public int viewDistance;
    private void viewDistance()
    {
        viewDistance = getInt( "view-distance", Bukkit.getViewDistance() );
        log( "View Distance: " + viewDistance );
    }

    public byte mobSpawnRange;
    private void mobSpawnRange()
    {
        mobSpawnRange = (byte) getInt( "mob-spawn-range", 4 );
        log( "Mob Spawn Range: " + mobSpawnRange );
    }

    public int itemDespawnRate;
    private void itemDespawnRate()
    {
        itemDespawnRate = getInt( "item-despawn-rate", 6000 );
        log( "Item Despawn Rate: " + itemDespawnRate );
    }

    public int animalActivationRange = 32;
    public int monsterActivationRange = 32;
    public int miscActivationRange = 16;
    private void activationRange()
    {
        animalActivationRange = getInt( "entity-activation-range.animals", animalActivationRange );
        monsterActivationRange = getInt( "entity-activation-range.monsters", monsterActivationRange );
        miscActivationRange = getInt( "entity-activation-range.misc", miscActivationRange );
        log( "Entity Activation Range: An " + animalActivationRange + " / Mo " + monsterActivationRange + " / Mi " + miscActivationRange );
    }

    public int playerTrackingRange = 48;
    public int animalTrackingRange = 48;
    public int monsterTrackingRange = 48;
    public int miscTrackingRange = 32;
    public int otherTrackingRange = 64;
    private void trackingRange()
    {
        playerTrackingRange = getInt( "entity-tracking-range.players", playerTrackingRange );
        animalTrackingRange = getInt( "entity-tracking-range.animals", animalTrackingRange );
        monsterTrackingRange = getInt( "entity-tracking-range.monsters", monsterTrackingRange );
        miscTrackingRange = getInt( "entity-tracking-range.misc", miscTrackingRange );
        otherTrackingRange = getInt( "entity-tracking-range.other", otherTrackingRange );
        log( "Entity Tracking Range: Pl " + playerTrackingRange + " / An " + animalTrackingRange + " / Mo " + monsterTrackingRange + " / Mi " + miscTrackingRange + " / Other " + otherTrackingRange );
    }

    public int hopperTransfer;
    public int hopperCheck;
    public int hopperAmount;
    private void hoppers()
    {
        // Set the tick delay between hopper item movements
        hopperTransfer = getInt( "ticks-per.hopper-transfer", 8 );
        // Set the tick delay between checking for items after the associated
        // container is empty. Default to the hopperTransfer value to prevent
        // hopper sorting machines from becoming out of sync.
        hopperCheck = getInt( "ticks-per.hopper-check", hopperTransfer );
        hopperAmount = getInt( "hopper-amount", 1 );
        log( "Hopper Transfer: " + hopperTransfer + " Hopper Check: " + hopperCheck + " Hopper Amount: " + hopperAmount );
    }

    public boolean randomLightUpdates;
    private void lightUpdates()
    {
        randomLightUpdates = getBoolean( "random-light-updates", false );
        log( "Random Lighting Updates: " + randomLightUpdates );
    }

    public boolean saveStructureInfo;
    private void structureInfo()
    {
        saveStructureInfo = getBoolean( "save-structure-info", true );
        log( "Structure Info Saving: " + saveStructureInfo );
        if ( !saveStructureInfo )
        {
            log( "*** WARNING *** You have selected to NOT save structure info. This may cause structures such as fortresses to not spawn mobs!" );
            log( "*** WARNING *** Please use this option with caution, SpigotMC is not responsible for any issues this option may cause in the future!" );
        }
    }

    public int arrowDespawnRate;
    private void arrowDespawnRate()
    {
        arrowDespawnRate = getInt( "arrow-despawn-rate", 1200  );
        log( "Arrow Despawn Rate: " + arrowDespawnRate );
    }

    public boolean zombieAggressiveTowardsVillager;
    private void zombieAggressiveTowardsVillager()
    {
        zombieAggressiveTowardsVillager = getBoolean( "zombie-aggressive-towards-villager", true );
        log( "Zombie Aggressive Towards Villager: " + zombieAggressiveTowardsVillager );
    }

    public boolean nerfSpawnerMobs;
    private void nerfSpawnerMobs()
    {
        nerfSpawnerMobs = getBoolean( "nerf-spawner-mobs", false );
        log( "Nerfing mobs spawned from spawners: " + nerfSpawnerMobs );
    }

    public boolean enableZombiePigmenPortalSpawns;
    private void enableZombiePigmenPortalSpawns()
    {
        enableZombiePigmenPortalSpawns = getBoolean( "enable-zombie-pigmen-portal-spawns", true );
        log( "Allow Zombie Pigmen to spawn from portal blocks: " + enableZombiePigmenPortalSpawns );
    }

    public int maxCollisionsPerEntity;
    private void maxEntityCollision()
    {
        maxCollisionsPerEntity = getInt( "max-entity-collisions", 8 );
        log( "Max Entity Collisions: " + maxCollisionsPerEntity );
    }

    public int dragonDeathSoundRadius;
    private void keepDragonDeathPerWorld()
    {
        dragonDeathSoundRadius = getInt( "dragon-death-sound-radius", 0 );
    }

    public int witherSpawnSoundRadius;
    private void witherSpawnSoundRadius()
    {
        witherSpawnSoundRadius = getInt( "wither-spawn-sound-radius", 0 );
    }

    public int villageSeed;
    public int largeFeatureSeed;
    private void initWorldGenSeeds()
    {
        villageSeed = getInt( "seed-village", 10387312 );
        largeFeatureSeed = getInt( "seed-feature", 14357617 );
        log( "Custom Map Seeds:  Village: " + villageSeed + " Feature: " + largeFeatureSeed );
    }

    public float walkExhaustion;
    public float sprintExhaustion;
    public float combatExhaustion;
    public float regenExhaustion;
    private void initHunger()
    {
        walkExhaustion = (float) getDouble( "hunger.walk-exhaustion", 0.2 );
        sprintExhaustion = (float) getDouble( "hunger.sprint-exhaustion", 0.8 );
        combatExhaustion = (float) getDouble( "hunger.combat-exhaustion", 0.3 );
        regenExhaustion = (float) getDouble( "hunger.regen-exhaustion", 3 );
    }

    public int currentPrimedTnt = 0;
    public int maxTntTicksPerTick;
    private void maxTntPerTick() {
        if ( SpigotConfig.version < 7 )
        {
            set( "max-tnt-per-tick", 100 );
        }
        maxTntTicksPerTick = getInt( "max-tnt-per-tick", 100 );
        log( "Max TNT Explosions: " + maxTntTicksPerTick );
    }

    public int hangingTickFrequency;
    private void hangingTickFrequency()
    {
        hangingTickFrequency = getInt( "hanging-tick-frequency", 100 );
    }

    public int tileMaxTickTime;
    public int entityMaxTickTime;
    private void maxTickTimes()
    {
        tileMaxTickTime = getInt("max-tick-time.tile", 50);
        entityMaxTickTime = getInt("max-tick-time.entity", 50);
        log("Tile Max Tick Time: " + tileMaxTickTime + "ms Entity max Tick Time: " + entityMaxTickTime + "ms");
    }
}
