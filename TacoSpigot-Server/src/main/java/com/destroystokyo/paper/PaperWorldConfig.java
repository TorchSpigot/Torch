package com.destroystokyo.paper;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spigotmc.SpigotWorldConfig;
import java.util.Arrays;
import com.destroystokyo.paper.antixray.AntiXray;

public class PaperWorldConfig {

    private final String worldName;
    private final SpigotWorldConfig spigotConfig;
    private final YamlConfiguration config;
    private boolean verbose;

    public PaperWorldConfig(String worldName, SpigotWorldConfig spigotConfig) {
        this.worldName = worldName;
        this.spigotConfig = spigotConfig;
        this.config = PaperConfig.config;
        init();
    }

    public void init() {
        this.verbose = getBoolean("verbose", false);

        log("-------- World Settings For [" + worldName + "] --------");
        PaperConfig.readConfig(PaperWorldConfig.class, this);
    }

    private void log(String s) {
        if (verbose) {
            Bukkit.getLogger().info(s);
        }
    }

    private void set(String path, Object val) {
        config.set("world-settings.default." + path, val);
        if (config.get("world-settings." + worldName + "." + path) != null) {
            config.set("world-settings." + worldName + "." + path, val);
        }
    }

    private boolean getBoolean(String path, boolean def) {
        config.addDefault("world-settings.default." + path, def);
        return config.getBoolean("world-settings." + worldName + "." + path, config.getBoolean("world-settings.default." + path));
    }

    private double getDouble(String path, double def) {
        config.addDefault("world-settings.default." + path, def);
        return config.getDouble("world-settings." + worldName + "." + path, config.getDouble("world-settings.default." + path));
    }

    private int getInt(String path, int def) {
        config.addDefault("world-settings.default." + path, def);
        return config.getInt("world-settings." + worldName + "." + path, config.getInt("world-settings.default." + path));
    }

    private float getFloat(String path, float def) {
        // TODO: Figure out why getFloat() always returns the default value.
        return (float) getDouble(path, (double) def);
    }

    private <T> List getList(String path, T def) {
        config.addDefault("world-settings.default." + path, def);
        return (List<T>) config.getList("world-settings." + worldName + "." + path, config.getList("world-settings.default." + path));
    }

    private String getString(String path, String def) {
        config.addDefault("world-settings.default." + path, def);
        return config.getString("world-settings." + worldName + "." + path, config.getString("world-settings.default." + path));
    }

    public double squidMinSpawnHeight;
    public double squidMaxSpawnHeight;
    private void squidSpawnHeights() {
        squidMinSpawnHeight = getDouble("squid-spawn-height.minimum", 45.0D);
        squidMaxSpawnHeight = getDouble("squid-spawn-height.maximum", 63.0D);
        log("Squids will spawn between Y: " + squidMinSpawnHeight + " and Y: " + squidMaxSpawnHeight);
    }

    public int cactusMaxHeight;
    public int reedMaxHeight;
    private void blowGrowthHeight() {
        cactusMaxHeight = getInt("max-growth-height.cactus", 3);
        reedMaxHeight = getInt("max-growth-height.reeds", 3);
        log("Max height for cactus growth " + cactusMaxHeight + ". Max height for reed growth " + reedMaxHeight);

    }

    public double babyZombieMovementSpeed;
    private void babyZombieMovementSpeed() {
        babyZombieMovementSpeed = getDouble("baby-zombie-movement-speed", 0.5D); // Player moves at 0.1F, for reference
        log("Baby zombies will move at the speed of " + babyZombieMovementSpeed);
    }

    public int fishingMinTicks;
    public int fishingMaxTicks;
    private void fishingTickRange() {
        fishingMinTicks = getInt("fishing-time-range.MinimumTicks", 100);
        fishingMaxTicks = getInt("fishing-time-range.MaximumTicks", 900);
        log("Fishing time ranges are between " + fishingMinTicks +" and " + fishingMaxTicks + " ticks");
    }

    public boolean nerfedMobsShouldJump;
    private void nerfedMobsShouldJump() {
        nerfedMobsShouldJump = getBoolean("spawner-nerfed-mobs-should-jump", false);
    }

    public float blockBreakExhaustion;
    public float playerSwimmingExhaustion;
    public void exhaustionValues() {
        blockBreakExhaustion = getFloat("player-exhaustion.block-break", 0.025F);
        playerSwimmingExhaustion = getFloat("player-exhaustion.swimming", 0.015F);
        log("Player exhaustion penalty for breaking blocks is " + blockBreakExhaustion);
        log("Player exhaustion penalty for swimming is " + playerSwimmingExhaustion);
    }

    public int softDespawnDistance;
    public int hardDespawnDistance;
    private void despawnDistances() {
        softDespawnDistance = getInt("despawn-ranges.soft", 32); // 32^2 = 1024, Minecraft Default
        hardDespawnDistance = getInt("despawn-ranges.hard", 128); // 128^2 = 16384, Minecraft Default

        if (softDespawnDistance > hardDespawnDistance) {
            softDespawnDistance = hardDespawnDistance;
        }

        log("Living Entity Despawn Ranges:  Soft: " + softDespawnDistance + " Hard: " + hardDespawnDistance);

        softDespawnDistance = softDespawnDistance*softDespawnDistance;
        hardDespawnDistance = hardDespawnDistance*hardDespawnDistance;
    }

    public boolean keepSpawnInMemory;
    private void keepSpawnInMemory() {
        keepSpawnInMemory = getBoolean("keep-spawn-loaded", true);
        log("Keep spawn chunk loaded: " + keepSpawnInMemory);
    }

    public int fallingBlockHeightNerf;
    public int entityTNTHeightNerf;
    private void heightNerfs() {
        fallingBlockHeightNerf = getInt("falling-block-height-nerf", 0);
        entityTNTHeightNerf = getInt("tnt-entity-height-nerf", 0);

        if (fallingBlockHeightNerf != 0) log("Falling Block Height Limit set to Y: " + fallingBlockHeightNerf);
        if (entityTNTHeightNerf != 0) log("TNT Entity Height Limit set to Y: " + entityTNTHeightNerf);
    }

    public int waterOverLavaFlowSpeed;
    private void waterOverLawFlowSpeed() {
        waterOverLavaFlowSpeed = getInt("water-over-lava-flow-speed", 5);
        log("Water over lava flow speed: " + waterOverLavaFlowSpeed);
    }

    public boolean netherVoidTopDamage;
    private void netherVoidTopDamage() {
        netherVoidTopDamage = getBoolean( "nether-ceiling-void-damage", false );
        log("Top of the nether void damage: " + netherVoidTopDamage);
    }

    public boolean queueLightUpdates;
    private void queueLightUpdates() {
        queueLightUpdates = getBoolean("queue-light-updates", false);
        log("Lighting Queue enabled: " + queueLightUpdates);
    }

    public boolean disableEndCredits;
    private void disableEndCredits() {
        disableEndCredits = getBoolean("game-mechanics.disable-end-credits", false);
        log("End credits disabled: " + disableEndCredits);
    }

    public boolean generateCanyon;
    public boolean generateCaves;
    public boolean generateDungeon;
    public boolean generateFortress;
    public boolean generateMineshaft;
    public boolean generateMonument;
    public boolean generateStronghold;
    public boolean generateTemple;
    public boolean generateVillage;
    public boolean generateFlatBedrock;

    private void generatorSettings() {
        generateCanyon = getBoolean("generator-settings.canyon", true);
        generateCaves = getBoolean("generator-settings.caves", true);
        generateDungeon = getBoolean("generator-settings.dungeon", true);
        generateFortress = getBoolean("generator-settings.fortress", true);
        generateMineshaft = getBoolean("generator-settings.mineshaft", true);
        generateMonument = getBoolean("generator-settings.monument", true);
        generateStronghold = getBoolean("generator-settings.stronghold", true);
        generateTemple = getBoolean("generator-settings.temple", true);
        generateVillage = getBoolean("generator-settings.village", true);
        generateFlatBedrock = getBoolean("generator-settings.flat-bedrock", false);
    }

    public boolean optimizeExplosions;
    private void optimizeExplosions() {
        optimizeExplosions = getBoolean("optimize-explosions", false);
        log("Optimize explosions: " + optimizeExplosions);
    }

    public boolean fastDrainLava;
    public boolean fastDrainWater;
    private void fastDrain() {
        fastDrainLava = getBoolean("fast-drain.lava", false);
        fastDrainWater = getBoolean("fast-drain.water", false);
    }

    public int lavaFlowSpeedNormal;
    public int lavaFlowSpeedNether;
    private void lavaFlowSpeeds() {
        lavaFlowSpeedNormal = getInt("lava-flow-speed.normal", 30);
        lavaFlowSpeedNether = getInt("lava-flow-speed.nether", 10);
    }

    public boolean disableExplosionKnockback;
    private void disableExplosionKnockback(){
        disableExplosionKnockback = getBoolean("disable-explosion-knockback", false);
    }

    public boolean disableThunder;
    private void disableThunder() {
        disableThunder = getBoolean("disable-thunder", false);
    }

    public boolean disableIceAndSnow;
    private void disableIceAndSnow(){
        disableIceAndSnow = getBoolean("disable-ice-and-snow", false);
    }

    public int mobSpawnerTickRate;
    private void mobSpawnerTickRate() {
        mobSpawnerTickRate = getInt("mob-spawner-tick-rate", 1);
    }

    public int containerUpdateTickRate;
    private void containerUpdateTickRate() {
        containerUpdateTickRate = getInt("container-update-tick-rate", 1);
    }

    public boolean disableChestCatDetection;
    private void disableChestCatDetection() {
        disableChestCatDetection = getBoolean("game-mechanics.disable-chest-cat-detection", false);
    }

    public boolean allChunksAreSlimeChunks;
    private void allChunksAreSlimeChunks() {
        allChunksAreSlimeChunks = getBoolean("all-chunks-are-slime-chunks", false);
    }

    public boolean allowBlockLocationTabCompletion;
    private void allowBlockLocationTabCompletion() {
        allowBlockLocationTabCompletion = getBoolean("allow-block-location-tab-completion", true);
    }

    public int portalSearchRadius;
    private void portalSearchRadius() {
        portalSearchRadius = getInt("portal-search-radius", 128);
    }

    public boolean disableTeleportationSuffocationCheck;
    private void disableTeleportationSuffocationCheck() {
        disableTeleportationSuffocationCheck = getBoolean("disable-teleportation-suffocation-check", false);
    }

    public boolean nonPlayerEntitiesOnScoreboards = false;
    private void nonPlayerEntitiesOnScoreboards() {
        nonPlayerEntitiesOnScoreboards = getBoolean("allow-non-player-entities-on-scoreboards", false);
    }

    public boolean useHopperCheck;
    private void useHopperCheck() {
        useHopperCheck = getBoolean("use-hopper-check", false);
    }

    public boolean allowLeashingUndeadHorse = false;
    private void allowLeashingUndeadHorse() {
        allowLeashingUndeadHorse = getBoolean("allow-leashing-undead-horse", false);
    }

    public int nonPlayerArrowDespawnRate = -1;
    private void nonPlayerArrowDespawnRate() {
        nonPlayerArrowDespawnRate = getInt("non-player-arrow-despawn-rate", -1);
        if (nonPlayerArrowDespawnRate == -1) {
            nonPlayerArrowDespawnRate = spigotConfig.arrowDespawnRate;
        }
        log("Non Player Arrow Despawn Rate: " + nonPlayerArrowDespawnRate);
    }

    public double skeleHorseSpawnChance;
    private void skeleHorseSpawnChance() {
        skeleHorseSpawnChance = getDouble("skeleton-horse-thunder-spawn-chance", -1.0D); // -1.0D represents a "vanilla" state
        if (skeleHorseSpawnChance < 0) {
            skeleHorseSpawnChance = 0.05D; // Vanilla
        }
    }

    public boolean firePhysicsEventForRedstone = false;
    private void firePhysicsEventForRedstone() {
        firePhysicsEventForRedstone = getBoolean("fire-physics-event-for-redstone", firePhysicsEventForRedstone);
    }

    public boolean useInhabitedTime = true;
    private void useInhabitedTime() {
        useInhabitedTime = getBoolean("use-chunk-inhabited-timer", true);
    }

    public int grassUpdateRate = 1;
    private void grassUpdateRate() {
        grassUpdateRate = Math.max(0, getInt("grass-spread-tick-rate", grassUpdateRate));
        log("Grass Spread Tick Rate: " + grassUpdateRate);
    }

    public short keepLoadedRange;
    private void keepLoadedRange() {
        keepLoadedRange = (short) (getInt("keep-spawn-loaded-range", Math.min(spigotConfig.viewDistance, 8)) * 16);
        log( "Keep Spawn Loaded Range: " + (keepLoadedRange/16));
    }

    public boolean useVanillaScoreboardColoring;
    private void useVanillaScoreboardColoring() {
        useVanillaScoreboardColoring = getBoolean("use-vanilla-world-scoreboard-name-coloring", false);
    }

    public boolean frostedIceEnabled = true;
    public int frostedIceDelayMin = 20;
    public int frostedIceDelayMax = 40;
    private void frostedIce() {
        this.frostedIceEnabled = this.getBoolean("frosted-ice.enabled", this.frostedIceEnabled);
        this.frostedIceDelayMin = this.getInt("frosted-ice.delay.min", this.frostedIceDelayMin);
        this.frostedIceDelayMax = this.getInt("frosted-ice.delay.max", this.frostedIceDelayMax);
        this.log("Frosted Ice: " + (this.frostedIceEnabled ? "enabled" : "disabled") + " / delay: min=" + this.frostedIceDelayMin + ", max=" + this.frostedIceDelayMax);
    }

    public boolean autoReplenishLootables;
    public boolean restrictPlayerReloot;
    public boolean changeLootTableSeedOnFill;
    public int maxLootableRefills;
    public int lootableRegenMin;
    public int lootableRegenMax;
    private void enhancedLootables() {
        autoReplenishLootables = getBoolean("lootables.auto-replenish", false);
        restrictPlayerReloot = getBoolean("lootables.restrict-player-reloot", true);
        changeLootTableSeedOnFill = getBoolean("lootables.reset-seed-on-fill", true);
        maxLootableRefills = getInt("lootables.max-refills", -1);
        lootableRegenMin = PaperConfig.getSeconds(getString("lootables.refresh-min", "12h"));
        lootableRegenMax = PaperConfig.getSeconds(getString("lootables.refresh-max", "2d"));
        if (autoReplenishLootables) {
            log("Lootables: Replenishing every " +
                PaperConfig.timeSummary(lootableRegenMin) + " to " +
                PaperConfig.timeSummary(lootableRegenMax) +
                (restrictPlayerReloot ? " (restricting reloot)" : "")
            );
        }
    }

    public boolean oldCannonBehaviors;
    private void oldCannonBehaviors() {
        oldCannonBehaviors = getBoolean("enable-old-tnt-cannon-behaviors", false);
        if (oldCannonBehaviors) {
            log("Old Cannon Behaviors: This feature may not be working entirely properly at the moment");
        }
    }
	
	public boolean elytraHitWallDamage = true;
     private void elytraHitWallDamage() {
        elytraHitWallDamage = getBoolean("elytra-hit-wall-damage", true);
     }
	 
	 public boolean antiXray;
    public int engineMode;
    public List<Object> hiddenBlocks;
    public List<Object> replaceBlocks;
    public int maxChunkY;
    public boolean asynchronous;
    public int neighborsMode;
    public AntiXray antiXrayInstance;
    private void antiXray() {
  antiXray = getBoolean("anti-xray.enabled", false);
  log("Anti X-Ray: " + antiXray);
  engineMode = getInt("anti-xray.engine-mode", 3);
  log("\tEngine Mode: " + engineMode + " (" + (engineMode == 1 ? "hidden ores" : engineMode == 2 ? "fake ores" : engineMode == 3 ? "fake ores every third block" : "unknown") + ")");
  hiddenBlocks = getList("anti-xray.hide-blocks", Arrays.asList(new Object[] {
      "gold_ore", "iron_ore", "coal_ore", "lapis_ore", "mossy_cobblestone", "obsidian", "chest", "diamond_ore", "redstone_ore", "lit_redstone_ore", "clay", "emerald_ore", "ender_chest"
  }));
  log("\tHidden Blocks: " + hiddenBlocks);
  replaceBlocks = getList("anti-xray.replace-blocks", Arrays.asList(new Object[] {
      "stone", "planks"
  }));
  log("\tReplace Blocks: " + replaceBlocks + " (for engine mode 2 and 3)");
  maxChunkY = getInt("anti-xray.max-chunk-y", 3);
  log("\tMax Chunk Y: " + maxChunkY + " (obfuscate up to " + ((maxChunkY + 1) * 16) + " blocks)");
  asynchronous = getBoolean("anti-xray.asynchronous", true);
  log("\tAsynchronous: " + asynchronous);
  neighborsMode = getInt("anti-xray.neighbors-mode", 2);
  log("\tNeighbors Mode: " + neighborsMode + " (" + (neighborsMode == 1 ? "MC default, sometimes the edges of chunks can't be obfuscated" : neighborsMode == 2 ? "wait until neighbor chunks are loaded" : neighborsMode == 3 ? "load neighbor chunks" : "unknown") + ")");
  antiXrayInstance = new AntiXray(this);
    }
	 
	public long delayChunkUnloadsBy;
    private void delayChunkUnloadsBy() {
        delayChunkUnloadsBy = PaperConfig.getSeconds(getString("delay-chunk-unloads-by", "30s"));
        if (delayChunkUnloadsBy > 0) {
            log("Delaying chunk unloads by " + delayChunkUnloadsBy + " seconds");
            delayChunkUnloadsBy *= 1000;
        }
    }
	
	public boolean isHopperPushBased;
    private void isHopperPushBased() {
        isHopperPushBased = getBoolean("hopper.push-based", true);
    }
}
