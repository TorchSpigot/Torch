package net.minecraft.server;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate; // Paper
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import java.util.Queue;

import com.destroystokyo.paper.profile.AccountProfile;
import com.destroystokyo.paper.profile.ProfileUtils;

// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;
// CraftBukkit end

import static net.techcable.tacospigot.TacoSpigotConfig.betterPvp; // TacoSpigot

public abstract class EntityHuman extends EntityLiving {

    private static final DataWatcherObject<Float> a = DataWatcher.a(EntityHuman.class, DataWatcherRegistry.c);
    private static final DataWatcherObject<Integer> b = DataWatcher.a(EntityHuman.class, DataWatcherRegistry.b);
    protected static final DataWatcherObject<Byte> bq = DataWatcher.a(EntityHuman.class, DataWatcherRegistry.a);
    protected static final DataWatcherObject<Byte> br = DataWatcher.a(EntityHuman.class, DataWatcherRegistry.a);
    public PlayerInventory inventory = new PlayerInventory(this);
    private InventoryEnderChest enderChest = new InventoryEnderChest();
    public Container defaultContainer;
    public Container activeContainer;
    protected FoodMetaData foodData = new FoodMetaData(this); // CraftBukkit - add "this" to constructor
    protected int bw;
    public float bx;
    public float by;
    public int bz;
    public double bA;
    public double bB;
    public double bC;
    public double bD;
    public double bE;
    public double bF;
    public boolean sleeping;
    public BlockPosition bedPosition;
    public int sleepTicks;
    public float bI;
    public float bJ;
    private BlockPosition e;
    private boolean f;
    private BlockPosition g;
    public PlayerAbilities abilities = new PlayerAbilities();
    public int expLevel;
    public int expTotal;
    public float exp;
    private int h;
    protected float bO = 0.1F;
    protected float bP = 0.02F;
    private int bR;
    private final GameProfile bS;
    private boolean bT = false;
    private ItemStack bU = null;
    private final ItemCooldown bV = this.l();
    public EntityFishingHook hookedFish;

    public int clientTicksSinceLastAttack; // TacoSpigot

    // Paper start - affectsSpawning API
    public boolean affectsSpawning = true;

    public static Predicate<EntityHuman> affectsSpawningFilter() {
        return new Predicate<EntityHuman>() {
            @Override
            public boolean apply(EntityHuman entityHuman) {
                return entityHuman.affectsSpawning;
            }
        };
    }
    // Paper end

    // CraftBukkit start
    public boolean fauxSleeping;
    public String spawnWorld = "";
    public int oldLevel = -1;

    @Override
    public CraftHumanEntity getBukkitEntity() {
        return (CraftHumanEntity) super.getBukkitEntity();
    }
    // CraftBukkit end

    protected ItemCooldown l() {
        return new ItemCooldown();
    }

    public EntityHuman(World world, GameProfile gameprofile) {
        super(world);
        this.a(a(gameprofile));
        this.bS = gameprofile;
        this.defaultContainer = new ContainerPlayer(this.inventory, !world.isClientSide, this);
        this.activeContainer = this.defaultContainer;
        BlockPosition blockposition = world.getSpawn();

        this.setPositionRotation((double) blockposition.getX() + 0.5D, (double) (blockposition.getY() + 1), (double) blockposition.getZ() + 0.5D, 0.0F, 0.0F);
        this.ba = 180.0F;
        this.maxFireTicks = 20;
    }

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE).setValue(1.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.10000000149011612D);
        this.getAttributeMap().b(GenericAttributes.f);
        this.getAttributeMap().b(GenericAttributes.i);
    }

    protected void i() {
        super.i();
        this.datawatcher.register(EntityHuman.a, Float.valueOf(0.0F));
        this.datawatcher.register(EntityHuman.b, Integer.valueOf(0));
        this.datawatcher.register(EntityHuman.bq, Byte.valueOf((byte) 0));
        this.datawatcher.register(EntityHuman.br, Byte.valueOf((byte) 1));
    }

    public void m() {
        this.noclip = this.isSpectator();
        if (this.isSpectator()) {
            this.onGround = false;
        }

        if (this.bz > 0) {
            --this.bz;
        }

        if (this.isSleeping()) {
            ++this.sleepTicks;
            if (this.sleepTicks > 100) {
                this.sleepTicks = 100;
            }

            if (!this.world.isClientSide) {
                if (!this.r()) {
                    this.a(true, true, false);
                } else if (this.world.B()) {
                    this.a(false, true, true);
                }
            }
        } else if (this.sleepTicks > 0) {
            ++this.sleepTicks;
            if (this.sleepTicks >= 110) {
                this.sleepTicks = 0;
            }
        }

        super.m();
        if (!this.world.isClientSide && this.activeContainer != null && !this.activeContainer.a(this)) {
            this.closeInventory();
            this.activeContainer = this.defaultContainer;
        }

        if (this.isBurning() && this.abilities.isInvulnerable) {
            this.extinguish();
        }

        this.o();
        if (!this.isPassenger()) {
            this.g = null;
        }

        if (!this.world.isClientSide) {
            this.foodData.a(this);
            this.b(StatisticList.g);
            if (this.isAlive()) {
                this.b(StatisticList.h);
            }

            if (this.isSneaking()) {
                this.b(StatisticList.i);
            }
        }

        int i = 29999999;
        double d0 = MathHelper.a(this.locX, -2.9999999E7D, 2.9999999E7D);
        double d1 = MathHelper.a(this.locZ, -2.9999999E7D, 2.9999999E7D);

        if (d0 != this.locX || d1 != this.locZ) {
            this.setPosition(d0, this.locY, d1);
        }

        ++this.aE;
        ItemStack itemstack = this.getItemInMainHand();

        if (!ItemStack.matches(this.bU, itemstack)) {
            if (!ItemStack.d(this.bU, itemstack)) {
                this.da();
            }

            this.bU = itemstack == null ? null : itemstack.cloneItemStack();
        }

        this.bV.a();
        this.cF();
    }

    private void o() {
        this.bA = this.bD;
        this.bB = this.bE;
        this.bC = this.bF;
        double d0 = this.locX - this.bD;
        double d1 = this.locY - this.bE;
        double d2 = this.locZ - this.bF;
        double d3 = 10.0D;

        if (d0 > d3) {
            this.bA = this.bD = this.locX;
        }

        if (d2 > d3) {
            this.bC = this.bF = this.locZ;
        }

        if (d1 > d3) {
            this.bB = this.bE = this.locY;
        }

        if (d0 < -d3) {
            this.bA = this.bD = this.locX;
        }

        if (d2 < -d3) {
            this.bC = this.bF = this.locZ;
        }

        if (d1 < -d3) {
            this.bB = this.bE = this.locY;
        }

        this.bD += d0 * 0.25D;
        this.bF += d2 * 0.25D;
        this.bE += d1 * 0.25D;
    }

    protected void cF() {
        float f = this.width;
        float f1 = this.length;

        if (this.cC()) {
            f = 0.6F;
            f1 = 0.6F;
        } else if (this.isSleeping()) {
            f = 0.2F;
            f1 = 0.2F;
        } else if (this.isSneaking()) {
            f = 0.6F;
            f1 = 1.65F;
        } else {
            f = 0.6F;
            f1 = 1.8F;
        }

        if (f != this.width || f1 != this.length) {
            AxisAlignedBB axisalignedbb = this.getBoundingBox();

            axisalignedbb = new AxisAlignedBB(axisalignedbb.a, axisalignedbb.b, axisalignedbb.c, axisalignedbb.a + (double) f, axisalignedbb.b + (double) f1, axisalignedbb.c + (double) f);
            if (!this.world.b(axisalignedbb)) {
                this.setSize(f, f1);
            }
        }

    }

    public int V() {
        return this.abilities.isInvulnerable ? 1 : 80;
    }

    protected SoundEffect aa() {
        return SoundEffects.el;
    }

    protected SoundEffect ab() {
        return SoundEffects.ek;
    }

    public int aC() {
        return 10;
    }

    public void a(SoundEffect soundeffect, float f, float f1) {
        this.world.a(this, this.locX, this.locY, this.locZ, soundeffect, this.bA(), f, f1);
    }

    public SoundCategory bA() {
        return SoundCategory.PLAYERS;
    }

    protected boolean cg() {
        return this.getHealth() <= 0.0F || this.isSleeping();
    }

    public void closeInventory() {
        this.activeContainer = this.defaultContainer;
    }

    public void aw() {
        if (!this.world.isClientSide && this.isSneaking() && this.isPassenger()) {
            this.stopRiding();
            this.setSneaking(false);
        } else {
            double d0 = this.locX;
            double d1 = this.locY;
            double d2 = this.locZ;
            float f = this.yaw;
            float f1 = this.pitch;

            super.aw();
            this.bx = this.by;
            this.by = 0.0F;
            this.m(this.locX - d0, this.locY - d1, this.locZ - d2);
            if (this.bz() instanceof EntityPig) {
                this.pitch = f1;
                this.yaw = f;
                this.aN = ((EntityPig) this.bz()).aN;
            }

        }
    }

    protected void doTick() {
        super.doTick();
        this.bZ();
        this.aP = this.yaw;
    }

    public void n() {
        if (this.bw > 0) {
            --this.bw;
        }

        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.world.getGameRules().getBoolean("naturalRegeneration")) {
            if (this.getHealth() < this.getMaxHealth() && this.ticksLived % 20 == 0) {
                // CraftBukkit - added regain reason of "REGEN" for filtering purposes.
                this.heal(1.0F, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.REGEN);
            }

            if (this.foodData.c() && this.ticksLived % 10 == 0) {
                this.foodData.a(this.foodData.getFoodLevel() + 1);
            }
        }

        this.inventory.m();
        this.bx = this.by;
        super.n();
        AttributeInstance attributeinstance = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);

        if (!this.world.isClientSide) {
            attributeinstance.setValue((double) this.abilities.b());
        }

        this.aR = this.bP;
        if (this.isSprinting()) {
            this.aR = (float) ((double) this.aR + (double) this.bP * 0.3D);
        }

        this.l((float) attributeinstance.getValue());
        float f = MathHelper.sqrt(this.motX * this.motX + this.motZ * this.motZ);
        float f1 = (float) ( org.bukkit.craftbukkit.TrigMath.atan(-this.motY * 0.20000000298023224D) * 15.0D); // CraftBukkit

        if (f > 0.1F) {
            f = 0.1F;
        }

        if (!this.onGround || this.getHealth() <= 0.0F) {
            f = 0.0F;
        }

        if (this.onGround || this.getHealth() <= 0.0F) {
            f1 = 0.0F;
        }

        this.by += (f - this.by) * 0.4F;
        this.aK += (f1 - this.aK) * 0.8F;
        if (this.getHealth() > 0.0F && !this.isSpectator()) {
            AxisAlignedBB axisalignedbb = null;

            if (this.isPassenger() && !this.bz().dead) {
                axisalignedbb = this.getBoundingBox().a(this.bz().getBoundingBox()).grow(1.0D, 0.0D, 1.0D);
            } else {
                axisalignedbb = this.getBoundingBox().grow(1.0D, 0.5D, 1.0D);
            }

            List list = this.world.getEntities(this, axisalignedbb);

			Iterator it = list.iterator();
            while (it.hasNext()) {
                Entity entity = (Entity) it.next();
 
                if (!entity.dead) {
                    this.c(entity);
                }
            }
        }

    }

    private void c(Entity entity) {
        entity.d(this);
    }

    public int getScore() {
        return ((Integer) this.datawatcher.get(EntityHuman.b)).intValue();
    }

    public void setScore(int i) {
        this.datawatcher.set(EntityHuman.b, Integer.valueOf(i));
    }

    public void addScore(int i) {
        int j = this.getScore();

        this.datawatcher.set(EntityHuman.b, Integer.valueOf(j + i));
    }

    public void die(DamageSource damagesource) {
        super.die(damagesource);
        this.setSize(0.2F, 0.2F);
        this.setPosition(this.locX, this.locY, this.locZ);
        this.motY = 0.10000000149011612D;
        if (this.getName().equals("Notch")) {
            this.a(new ItemStack(Items.APPLE, 1), true, false);
        }

        if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
            this.inventory.n();
        }

        if (damagesource != null) {
            this.motX = (double) (-MathHelper.cos((this.aA + this.yaw) * 0.017453292F) * 0.1F);
            this.motZ = (double) (-MathHelper.sin((this.aA + this.yaw) * 0.017453292F) * 0.1F);
        } else {
            this.motX = this.motZ = 0.0D;
        }

        this.b(StatisticList.A);
        this.a(StatisticList.h);
    }

    protected SoundEffect bS() {
        return SoundEffects.ENTITY_PLAYER_HURT;
    }

    protected SoundEffect bT() {
        return SoundEffects.eg;
    }

    public void b(Entity entity, int i) {
        if (entity != this) {
            this.addScore(i);
            // CraftBukkit - Get our scores instead
            Collection<ScoreboardScore> collection = this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreboardCriteria.f, this.getName(), new java.util.ArrayList<ScoreboardScore>());

            if (entity instanceof EntityHuman) {
                this.b(StatisticList.D);
                // CraftBukkit - Get our scores instead
                this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreboardCriteria.e, this.getName(), collection);
                // collection.addAll(this.getScoreboard().getObjectivesForCriteria(IScoreboardCriteria.e));
                // CraftBukkit end
            } else {
                this.b(StatisticList.B);
            }

            collection.addAll(this.d(entity));
            Iterator<ScoreboardScore> iterator = collection.iterator();

            while (iterator.hasNext()) {
                // CraftBukkit start
                // ScoreboardObjective scoreboardobjective = (ScoreboardObjective) iterator.next();
                // ScoreboardScore scoreboardscore = this.getScoreboard().getPlayerScoreForObjective(this.getName(), scoreboardobjective);

                iterator.next().incrementScore();
                // CraftBukkit end
            }

        }
    }

    private Collection<ScoreboardScore> d(Entity entity) { // CraftBukkit
        String s = entity instanceof EntityHuman ? entity.getName() : entity.getUniqueID().toString();
        ScoreboardTeam scoreboardteam = this.getScoreboard().getPlayerTeam(this.getName());

        if (scoreboardteam != null) {
            int i = scoreboardteam.m().b();

            if (i >= 0 && i < IScoreboardCriteria.n.length) {
                Iterator iterator = this.getScoreboard().getObjectivesForCriteria(IScoreboardCriteria.n[i]).iterator();

                while (iterator.hasNext()) {
                    ScoreboardObjective scoreboardobjective = (ScoreboardObjective) iterator.next();
                    ScoreboardScore scoreboardscore = this.getScoreboard().getPlayerScoreForObjective(s, scoreboardobjective);

                    scoreboardscore.incrementScore();
                }
            }
        }

        ScoreboardTeam scoreboardteam1 = this.getScoreboard().getPlayerTeam(s);

        if (scoreboardteam1 != null) {
            int j = scoreboardteam1.m().b();

            if (j >= 0 && j < IScoreboardCriteria.m.length) {
                // CraftBukkit - Get our scores instead
                return this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreboardCriteria.m[j], this.getName(), new java.util.ArrayList<ScoreboardScore>());
                // return this.getScoreboard().getObjectivesForCriteria(IScoreboardCriteria.m[j]);
                // CraftBukkit end
            }
        }

        return Lists.newArrayList();
    }

    @Nullable
    public EntityItem a(boolean flag) {
        // Called only when dropped by Q or CTRL-Q
        return this.a(this.inventory.splitStack(this.inventory.itemInHandIndex, flag && this.inventory.getItemInHand() != null ? this.inventory.getItemInHand().count : 1), false, true);
    }

    @Nullable
    public EntityItem drop(@Nullable ItemStack itemstack, boolean flag) {
        return this.a(itemstack, false, false);
    }

    @Nullable
    public EntityItem a(@Nullable ItemStack itemstack, boolean flag, boolean flag1) {
        if (itemstack == null) {
            return null;
        } else if (itemstack.count == 0) {
            return null;
        } else {
            double d0 = this.locY - 0.30000001192092896D + (double) this.getHeadHeight();
            EntityItem entityitem = new EntityItem(this.world, this.locX, d0, this.locZ, itemstack);

            entityitem.a(40);
            if (flag1) {
                entityitem.e(this.getName());
            }

            float f;
            float f1;

            if (flag) {
                f = this.random.nextFloat() * 0.5F;
                f1 = this.random.nextFloat() * 6.2831855F;
                entityitem.motX = (double) (-MathHelper.sin(f1) * f);
                entityitem.motZ = (double) (MathHelper.cos(f1) * f);
                entityitem.motY = 0.20000000298023224D;
            } else {
                f = 0.3F;
                entityitem.motX = (double) (-MathHelper.sin(this.yaw * 0.017453292F) * MathHelper.cos(this.pitch * 0.017453292F) * f);
                entityitem.motZ = (double) (MathHelper.cos(this.yaw * 0.017453292F) * MathHelper.cos(this.pitch * 0.017453292F) * f);
                entityitem.motY = (double) (-MathHelper.sin(this.pitch * 0.017453292F) * f + 0.1F);
                f1 = this.random.nextFloat() * 6.2831855F;
                f = 0.02F * this.random.nextFloat();
                entityitem.motX += Math.cos((double) f1) * (double) f;
                entityitem.motY += (double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.1F);
                entityitem.motZ += Math.sin((double) f1) * (double) f;
            }

            // CraftBukkit start - fire PlayerDropItemEvent
            Player player = (Player) this.getBukkitEntity();
            CraftItem drop = new CraftItem(this.world.getServer(), entityitem);

            PlayerDropItemEvent event = new PlayerDropItemEvent(player, drop);
            this.world.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                org.bukkit.inventory.ItemStack cur = player.getInventory().getItemInHand();
                if (flag1 && (cur == null || cur.getAmount() == 0)) {
                    // The complete stack was dropped
                    player.getInventory().setItemInHand(drop.getItemStack());
                } else if (flag1 && cur.isSimilar(drop.getItemStack()) && drop.getItemStack().getAmount() == 1) {
                    // Only one item is dropped
                    cur.setAmount(cur.getAmount() + 1);
                    player.getInventory().setItemInHand(cur);
                } else {
                    // Fallback
                    player.getInventory().addItem(drop.getItemStack());
                }
                return null;
            }
            // CraftBukkit end
            // Paper start - remove player from map on drop
            if (itemstack.getItem() == Items.FILLED_MAP) {
                WorldMap worldmap = Items.FILLED_MAP.getSavedMap(itemstack, this.world);
                worldmap.updateSeenPlayers(this, itemstack);
            }
            // Paper stop

            ItemStack itemstack1 = this.a(entityitem);

            if (flag1) {
                if (itemstack1 != null) {
                    this.a(StatisticList.e(itemstack1.getItem()), itemstack.count);
                }

                this.b(StatisticList.x);
            }

            return entityitem;
        }
    }

    @Nullable
    protected ItemStack a(EntityItem entityitem) {
        this.world.addEntity(entityitem);
        return entityitem.getItemStack();
    }

    public float a(IBlockData iblockdata) {
        float f = this.inventory.a(iblockdata);

        if (f > 1.0F) {
            int i = EnchantmentManager.getDigSpeedEnchantmentLevel(this);
            ItemStack itemstack = this.getItemInMainHand();

            if (i > 0 && itemstack != null) {
                f += (float) (i * i + 1);
            }
        }

        if (this.hasEffect(MobEffects.FASTER_DIG)) {
            f *= 1.0F + (float) (this.getEffect(MobEffects.FASTER_DIG).getAmplifier() + 1) * 0.2F;
        }

        if (this.hasEffect(MobEffects.SLOWER_DIG)) {
            float f1 = 1.0F;

            switch (this.getEffect(MobEffects.SLOWER_DIG).getAmplifier()) {
            case 0:
                f1 = 0.3F;
                break;

            case 1:
                f1 = 0.09F;
                break;

            case 2:
                f1 = 0.0027F;
                break;

            case 3:
            default:
                f1 = 8.1E-4F;
            }

            f *= f1;
        }

        if (this.a(Material.WATER) && !EnchantmentManager.i(this)) {
            f /= 5.0F;
        }

        if (!this.onGround) {
            f /= 5.0F;
        }

        return f;
    }

    public boolean hasBlock(IBlockData iblockdata) {
        return this.inventory.b(iblockdata);
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.a(a(this.bS));
        NBTTagList nbttaglist = nbttagcompound.getList("Inventory", 10);

        this.inventory.b(nbttaglist);
        this.inventory.itemInHandIndex = nbttagcompound.getInt("SelectedItemSlot");
        this.sleeping = nbttagcompound.getBoolean("Sleeping");
        this.sleepTicks = nbttagcompound.getShort("SleepTimer");
        this.exp = nbttagcompound.getFloat("XpP");
        this.expLevel = nbttagcompound.getInt("XpLevel");
        this.expTotal = nbttagcompound.getInt("XpTotal");
        this.h = nbttagcompound.getInt("XpSeed");
        if (this.h == 0) {
            this.h = this.random.nextInt();
        }

        this.setScore(nbttagcompound.getInt("Score"));
        if (this.sleeping) {
            this.bedPosition = new BlockPosition(this);
            this.a(true, true, false);
        }

        // CraftBukkit start
        this.spawnWorld = nbttagcompound.getString("SpawnWorld");
        if ("".equals(spawnWorld)) {
            this.spawnWorld = this.world.getServer().getWorlds().get(0).getName();
        }
        // CraftBukkit end

        if (nbttagcompound.hasKeyOfType("SpawnX", 99) && nbttagcompound.hasKeyOfType("SpawnY", 99) && nbttagcompound.hasKeyOfType("SpawnZ", 99)) {
            this.e = new BlockPosition(nbttagcompound.getInt("SpawnX"), nbttagcompound.getInt("SpawnY"), nbttagcompound.getInt("SpawnZ"));
            this.f = nbttagcompound.getBoolean("SpawnForced");
        }

        this.foodData.a(nbttagcompound);
        this.abilities.b(nbttagcompound);
        if (nbttagcompound.hasKeyOfType("EnderItems", 9)) {
            NBTTagList nbttaglist1 = nbttagcompound.getList("EnderItems", 10);

            this.enderChest.a(nbttaglist1);
        }

    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setInt("DataVersion", 184);
        nbttagcompound.set("Inventory", this.inventory.a(new NBTTagList()));
        nbttagcompound.setInt("SelectedItemSlot", this.inventory.itemInHandIndex);
        nbttagcompound.setBoolean("Sleeping", this.sleeping);
        nbttagcompound.setShort("SleepTimer", (short) this.sleepTicks);
        nbttagcompound.setFloat("XpP", this.exp);
        nbttagcompound.setInt("XpLevel", this.expLevel);
        nbttagcompound.setInt("XpTotal", this.expTotal);
        nbttagcompound.setInt("XpSeed", this.h);
        nbttagcompound.setInt("Score", this.getScore());
        if (this.e != null) {
            nbttagcompound.setInt("SpawnX", this.e.getX());
            nbttagcompound.setInt("SpawnY", this.e.getY());
            nbttagcompound.setInt("SpawnZ", this.e.getZ());
            nbttagcompound.setBoolean("SpawnForced", this.f);
        }

        this.foodData.b(nbttagcompound);
        this.abilities.a(nbttagcompound);
        nbttagcompound.set("EnderItems", this.enderChest.h());
        ItemStack itemstack = this.inventory.getItemInHand();

        if (itemstack != null && itemstack.getItem() != null) {
            nbttagcompound.set("SelectedItem", itemstack.save(new NBTTagCompound()));
        }
        nbttagcompound.setString("SpawnWorld", spawnWorld); // CraftBukkit - fixes bed spawns for multiworld worlds
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else if (this.abilities.isInvulnerable && !damagesource.ignoresInvulnerability()) {
            return false;
        } else {
            this.ticksFarFromPlayer = 0;
            if (this.getHealth() <= 0.0F) {
                return false;
            } else {
                if (this.isSleeping() && !this.world.isClientSide) {
                    this.a(true, true, false);
                }

                if (damagesource.r()) {
                    if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
                        return false; // CraftBukkit - f = 0.0f -> return false
                    }

                    if (this.world.getDifficulty() == EnumDifficulty.EASY) {
                        f = f / 2.0F + 1.0F;
                    }

                    if (this.world.getDifficulty() == EnumDifficulty.HARD) {
                        f = f * 3.0F / 2.0F;
                    }
                }

                if (false && f == 0.0F) { // CraftBukkit - Don't filter out 0 damage
                    return false;
                } else {
                    Entity entity = damagesource.getEntity();

                    if (entity instanceof EntityArrow && ((EntityArrow) entity).shooter != null) {
                        entity = ((EntityArrow) entity).shooter;
                    }

                    return super.damageEntity(damagesource, f);
                }
            }
        }
    }

    public boolean a(EntityHuman entityhuman) {
        // CraftBukkit start - Change to check OTHER player's scoreboard team according to API
        // To summarize this method's logic, it's "Can parameter hurt this"
        org.bukkit.scoreboard.Team team;
        if (entityhuman instanceof EntityPlayer) {
            EntityPlayer thatPlayer = (EntityPlayer) entityhuman;
            team = thatPlayer.getBukkitEntity().getScoreboard().getPlayerTeam(thatPlayer.getBukkitEntity());
            if (team == null || team.allowFriendlyFire()) {
                return true;
            }
        } else {
            // This should never be called, but is implemented anyway
            org.bukkit.OfflinePlayer thisPlayer = entityhuman.world.getServer().getOfflinePlayer(entityhuman.getName());
            team = entityhuman.world.getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(thisPlayer);
            if (team == null || team.allowFriendlyFire()) {
                return true;
            }
        }

        if (this instanceof EntityPlayer) {
            return !team.hasPlayer(((EntityPlayer) this).getBukkitEntity());
        }
        return !team.hasPlayer(this.world.getServer().getOfflinePlayer(this.getName()));
        // CraftBukkit end
    }

    protected void damageArmor(float f) {
        this.inventory.a(f);
    }

    protected void k(float f) {
        if (f >= 3.0F && this.bn != null && this.bn.getItem() == Items.SHIELD) {
            int i = 1 + MathHelper.d(f);

            this.bn.damage(i, this);
            if (this.bn.count <= 0) {
                EnumHand enumhand = this.cu();

                if (enumhand == EnumHand.MAIN_HAND) {
                    this.setSlot(EnumItemSlot.MAINHAND, (ItemStack) null);
                } else {
                    this.setSlot(EnumItemSlot.OFFHAND, (ItemStack) null);
                }

                this.bn = null;
                this.a(SoundEffects.eS, 0.8F, 0.8F + this.world.random.nextFloat() * 0.4F);
            }
        }

    }

    public float cH() {
        int i = 0;
        ItemStack[] aitemstack = this.inventory.armor;
        int j = aitemstack.length;

        for (int k = 0; k < j; ++k) {
            ItemStack itemstack = aitemstack[k];

            if (itemstack != null) {
                ++i;
            }
        }

        return (float) i / (float) this.inventory.armor.length;
    }

    // CraftBukkit start
    protected boolean damageEntity0(DamageSource damagesource, float f) { // void -> boolean
        if (true) {
            return super.damageEntity0(damagesource, f);
        }
        // CraftBukkit end
        if (!this.isInvulnerable(damagesource)) {
            f = this.applyArmorModifier(damagesource, f);
            f = this.applyMagicModifier(damagesource, f);
            float f1 = f;

            f = Math.max(f - this.getAbsorptionHearts(), 0.0F);
            this.setAbsorptionHearts(this.getAbsorptionHearts() - (f1 - f));
            if (f != 0.0F) {
                this.applyExhaustion(damagesource.getExhaustionCost());
                float f2 = this.getHealth();

                this.setHealth(this.getHealth() - f);
                this.getCombatTracker().trackDamage(damagesource, f2, f);
                if (f < 3.4028235E37F) {
                    this.a(StatisticList.z, Math.round(f * 10.0F));
                }

            }
        }
        return false; // CraftBukkit
    }

    public void openSign(TileEntitySign tileentitysign) {}

    public void a(CommandBlockListenerAbstract commandblocklistenerabstract) {}

    public void a(TileEntityCommand tileentitycommand) {}

    public void openTrade(IMerchant imerchant) {}

    public void openContainer(IInventory iinventory) {}

    public void a(EntityHorse entityhorse, IInventory iinventory) {}

    public void openTileEntity(ITileEntityContainer itileentitycontainer) {}

    public void a(ItemStack itemstack, EnumHand enumhand) {}

    public EnumInteractionResult a(Entity entity, @Nullable ItemStack itemstack, EnumHand enumhand) {
        if (this.isSpectator()) {
            if (entity instanceof IInventory) {
                this.openContainer((IInventory) entity);
            }

            return EnumInteractionResult.PASS;
        } else {
            ItemStack itemstack1 = itemstack != null ? itemstack.cloneItemStack() : null;

            if (!entity.a(this, itemstack, enumhand)) {
                if (itemstack != null && entity instanceof EntityLiving) {
                    if (this.abilities.canInstantlyBuild) {
                        itemstack = itemstack1;
                    }

                    if (itemstack.a(this, (EntityLiving) entity, enumhand)) {
                        if (itemstack.count <= 0 && !this.abilities.canInstantlyBuild) {
                            this.a(enumhand, (ItemStack) null);
                        }

                        return EnumInteractionResult.SUCCESS;
                    }
                }

                return EnumInteractionResult.PASS;
            } else {
                if (itemstack != null && itemstack == this.b(enumhand)) {
                    if (itemstack.count <= 0 && !this.abilities.canInstantlyBuild) {
                        this.a(enumhand, (ItemStack) null);
                    } else if (itemstack.count < itemstack1.count && this.abilities.canInstantlyBuild) {
                        itemstack.count = itemstack1.count;
                    }
                }

                return EnumInteractionResult.SUCCESS;
            }
        }
    }

    public double ax() {
        return -0.35D;
    }

    public void stopRiding() {
        super.stopRiding();
        this.j = 0;
    }

    public void attack(Entity entity) {
        if (entity.aT()) {
            if (!entity.t(this)) {
                float f = (float) this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
                float f1 = 0.0F;

                if (entity instanceof EntityLiving) {
                    f1 = EnchantmentManager.a(this.getItemInMainHand(), ((EntityLiving) entity).getMonsterType());
                } else {
                    f1 = EnchantmentManager.a(this.getItemInMainHand(), EnumMonsterType.UNDEFINED);
                }

                float f2 = this.o(0.5F);

                f *= 0.2F + f2 * f2 * 0.8F;
                f1 *= f2;
                this.da();
                if (f > 0.0F || f1 > 0.0F) {
                    boolean flag = f2 > 0.9F;
                    boolean flag1 = false;
                    boolean flag2 = false;
                    boolean flag3 = false;
                    byte b0 = 0;
                    int i = b0 + EnchantmentManager.a((EntityLiving) this);

                    if (this.isSprinting() && flag) {
                        this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.dY, this.bA(), 1.0F, 1.0F);
                        ++i;
                        flag1 = true;
                    }

                    flag2 = flag && this.fallDistance > 0.0F && !this.onGround && !this.n_() && !this.isInWater() && !this.hasEffect(MobEffects.BLINDNESS) && !this.isPassenger() && entity instanceof EntityLiving;
                    flag2 = flag2 && !this.isSprinting();
                    if (flag2) {
                        f *= 1.5F;
                    }

                    f += f1;
                    double d0 = (double) (this.J - this.I);

                    if (flag && !flag2 && !flag1 && this.onGround && d0 < (double) this.cl()) {
                        ItemStack itemstack = this.b(EnumHand.MAIN_HAND);

                        if (itemstack != null && itemstack.getItem() instanceof ItemSword) {
                            flag3 = true;
                        }
                    }

                    float f3 = 0.0F;
                    boolean flag4 = false;
                    int j = EnchantmentManager.getFireAspectEnchantmentLevel(this);

                    if (entity instanceof EntityLiving) {
                        f3 = ((EntityLiving) entity).getHealth();
                        if (j > 0 && !entity.isBurning()) {
                            // CraftBukkit start - Call a combust event when somebody hits with a fire enchanted item
                            EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), 1);
                            org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

                            if (!combustEvent.isCancelled()) {
                                flag4 = true;
                                entity.setOnFire(combustEvent.getDuration());
                            }
                            // CraftBukkit end
                        }
                    }

                    double d1 = entity.motX;
                    double d2 = entity.motY;
                    double d3 = entity.motZ;
                    boolean flag5 = entity.damageEntity(DamageSource.playerAttack(this), f);

                    if (flag5) {
                        if (i > 0) {
                            if (entity instanceof EntityLiving) {
                                ((EntityLiving) entity).a(this, (float) i * 0.5F, (double) MathHelper.sin(this.yaw * 0.017453292F), (double) (-MathHelper.cos(this.yaw * 0.017453292F)));
                            } else {
                                entity.g((double) (-MathHelper.sin(this.yaw * 0.017453292F) * (float) i * 0.5F), 0.1D, (double) (MathHelper.cos(this.yaw * 0.017453292F) * (float) i * 0.5F));
                            }

                            this.motX *= 0.6D;
                            this.motZ *= 0.6D;
                            this.setSprinting(false);
                        }

                        if (flag3) {
                            List list = this.world.a(EntityLiving.class, entity.getBoundingBox().grow(1.0D, 0.25D, 1.0D));
                            Iterator iterator = list.iterator();

                            while (iterator.hasNext()) {
                                EntityLiving entityliving = (EntityLiving) iterator.next();

                                if (entityliving != this && entityliving != entity && !this.r(entityliving) && this.h(entityliving) < 9.0D) {
                                    // CraftBukkit start - Only apply knockback if the damage hits
                                    if (entityliving.damageEntity(DamageSource.playerAttack(this), 1.0F)) {
                                    entityliving.a(this, 0.4F, (double) MathHelper.sin(this.yaw * 0.017453292F), (double) (-MathHelper.cos(this.yaw * 0.017453292F)));
                                    }
                                    // CraftBukkit end
                                }
                            }

                            this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.eb, this.bA(), 1.0F, 1.0F);
                            this.cI();
                        }

                        if (entity instanceof EntityPlayer && entity.velocityChanged) {
                            // CraftBukkit start - Add Velocity Event
                            boolean cancelled = false;
                            Player player = (Player) entity.getBukkitEntity();
                            org.bukkit.util.Vector velocity = new Vector( d1, d2, d3 );

                            PlayerVelocityEvent event = new PlayerVelocityEvent(player, velocity.clone());
                            world.getServer().getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                cancelled = true;
                            } else if (!velocity.equals(event.getVelocity())) {
                                player.setVelocity(event.getVelocity());
                            }

                            if (!cancelled) {
                            ((EntityPlayer) entity).playerConnection.sendPacket(new PacketPlayOutEntityVelocity(entity));
                            entity.velocityChanged = false;
                            entity.motX = d1;
                            entity.motY = d2;
                            entity.motZ = d3;
                            }
                            // CraftBukkit end
                        }

                        if (flag2) {
                            this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.ENTITY_PLAYER_ATTACK_CRIT, this.bA(), 1.0F, 1.0F);
                            this.a(entity);
                        }

                        if (!flag2 && !flag3) {
                            if (flag) {
                                this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.ea, this.bA(), 1.0F, 1.0F);
                            } else {
                                this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.ec, this.bA(), 1.0F, 1.0F);
                            }
                        }

                        if (f1 > 0.0F) {
                            this.b(entity);
                        }

                        if (!this.world.isClientSide && entity instanceof EntityHuman) {
                            EntityHuman entityhuman = (EntityHuman) entity;
                            ItemStack itemstack1 = this.getItemInMainHand();
                            ItemStack itemstack2 = entityhuman.ct() ? entityhuman.cw() : null;

                            if (itemstack1 != null && itemstack2 != null && itemstack1.getItem() instanceof ItemAxe && itemstack2.getItem() == Items.SHIELD) {
                                float f4 = 0.25F + (float) EnchantmentManager.getDigSpeedEnchantmentLevel(this) * 0.05F;

                                if (flag1) {
                                    f4 += 0.75F;
                                }

                                if (this.random.nextFloat() < f4) {
                                    entityhuman.db().a(Items.SHIELD, 100);
                                    this.world.broadcastEntityEffect(entityhuman, (byte) 30);
                                }
                            }
                        }

                        if (f >= 18.0F) {
                            this.b((Statistic) AchievementList.F);
                        }

                        this.z(entity);
                        if (entity instanceof EntityLiving) {
                            EnchantmentManager.a((EntityLiving) entity, (Entity) this);
                        }

                        EnchantmentManager.b((EntityLiving) this, entity);
                        ItemStack itemstack3 = this.getItemInMainHand();
                        Object object = entity;

                        if (entity instanceof EntityComplexPart) {
                            IComplex icomplex = ((EntityComplexPart) entity).owner;

                            if (icomplex instanceof EntityLiving) {
                                object = (EntityLiving) icomplex;
                            }
                        }

                        if (itemstack3 != null && object instanceof EntityLiving) {
                            itemstack3.a((EntityLiving) object, this);
                            // CraftBukkit - bypass infinite items; <= 0 -> == 0
                            if (itemstack3.count == 0) {
                                this.a(EnumHand.MAIN_HAND, (ItemStack) null);
                            }
                        }

                        if (entity instanceof EntityLiving) {
                            float f5 = f3 - ((EntityLiving) entity).getHealth();

                            this.a(StatisticList.y, Math.round(f5 * 10.0F));
                            if (j > 0) {
                                // CraftBukkit start - Call a combust event when somebody hits with a fire enchanted item
                                EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), j * 4);
                                org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

                                if (!combustEvent.isCancelled()) {
                                    entity.setOnFire(combustEvent.getDuration());
                                }
                                // CraftBukkit end
                            }

                            if (this.world instanceof WorldServer && f5 > 2.0F) {
                                int k = (int) ((double) f5 * 0.5D);

                                ((WorldServer) this.world).a(EnumParticle.DAMAGE_INDICATOR, entity.locX, entity.locY + (double) (entity.length * 0.5F), entity.locZ, k, 0.1D, 0.0D, 0.1D, 0.2D, new int[0]);
                            }
                        }

                        this.applyExhaustion(world.spigotConfig.combatExhaustion); // Spigot - Change to use configurable value
                    } else {
                        this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.dZ, this.bA(), 1.0F, 1.0F);
                        if (flag4) {
                            entity.extinguish();
                        }
                    }
                }

            }
        }
    }

    public void a(Entity entity) {}

    public void b(Entity entity) {}

    public void cI() {
        double d0 = (double) (-MathHelper.sin(this.yaw * 0.017453292F));
        double d1 = (double) MathHelper.cos(this.yaw * 0.017453292F);

        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).a(EnumParticle.SWEEP_ATTACK, this.locX + d0, this.locY + (double) this.length * 0.5D, this.locZ + d1, 0, d0, 0.0D, d1, 0.0D, new int[0]);
        }

    }

    public void die() {
        super.die();
        this.defaultContainer.b(this);
        if (this.activeContainer != null) {
            this.activeContainer.b(this);
        }

    }

    public boolean inBlock() {
        return !this.sleeping && super.inBlock();
    }

    public boolean cK() {
        return false;
    }

    public GameProfile getProfile() {
        return this.bS;
    }

	// Paper start - bukkit profile method
    private final AccountProfile bukkitProfile = ProfileUtils.toPaperWithProperties(getProfile());

    public AccountProfile getBukkitProfile() {
        return bukkitProfile;
    }
    // Paper end
	
    public EntityHuman.EnumBedResult a(BlockPosition blockposition) {
        if (!this.world.isClientSide) {
            if (this.isSleeping() || !this.isAlive()) {
                return EntityHuman.EnumBedResult.OTHER_PROBLEM;
            }

            if (!this.world.worldProvider.d()) {
                return EntityHuman.EnumBedResult.NOT_POSSIBLE_HERE;
            }

            if (this.world.B()) {
                return EntityHuman.EnumBedResult.NOT_POSSIBLE_NOW;
            }

            if (Math.abs(this.locX - (double) blockposition.getX()) > 3.0D || Math.abs(this.locY - (double) blockposition.getY()) > 2.0D || Math.abs(this.locZ - (double) blockposition.getZ()) > 3.0D) {
                return EntityHuman.EnumBedResult.TOO_FAR_AWAY;
            }

            double d0 = 8.0D;
            double d1 = 5.0D;
            List list = this.world.a(EntityMonster.class, new AxisAlignedBB((double) blockposition.getX() - d0, (double) blockposition.getY() - d1, (double) blockposition.getZ() - d0, (double) blockposition.getX() + d0, (double) blockposition.getY() + d1, (double) blockposition.getZ() + d0));

            if (!list.isEmpty()) {
                return EntityHuman.EnumBedResult.NOT_SAFE;
            }
        }

        if (this.isPassenger()) {
            this.stopRiding();
        }

        // CraftBukkit start - fire PlayerBedEnterEvent
        if (this.getBukkitEntity() instanceof Player) {
            Player player = (Player) this.getBukkitEntity();
            org.bukkit.block.Block bed = this.world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());

            PlayerBedEnterEvent event = new PlayerBedEnterEvent(player, bed);
            this.world.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return EnumBedResult.OTHER_PROBLEM;
            }
        }
        // CraftBukkit end

        this.setSize(0.2F, 0.2F);
        if (this.world.isLoaded(blockposition)) {
            EnumDirection enumdirection = (EnumDirection) this.world.getType(blockposition).get(BlockFacingHorizontal.FACING);
            float f = 0.5F;
            float f1 = 0.5F;

            switch (EntityHuman.SyntheticClass_1.a[enumdirection.ordinal()]) {
            case 1:
                f1 = 0.9F;
                break;

            case 2:
                f1 = 0.1F;
                break;

            case 3:
                f = 0.1F;
                break;

            case 4:
                f = 0.9F;
            }

            this.a(enumdirection);
            this.setPosition((double) ((float) blockposition.getX() + f), (double) ((float) blockposition.getY() + 0.6875F), (double) ((float) blockposition.getZ() + f1));
        } else {
            this.setPosition((double) ((float) blockposition.getX() + 0.5F), (double) ((float) blockposition.getY() + 0.6875F), (double) ((float) blockposition.getZ() + 0.5F));
        }

        this.sleeping = true;
        this.sleepTicks = 0;
        this.bedPosition = blockposition;
        this.motX = this.motZ = this.motY = 0.0D;
        if (!this.world.isClientSide) {
            this.world.everyoneSleeping();
        }

        return EntityHuman.EnumBedResult.OK;
    }

    private void a(EnumDirection enumdirection) {
        this.bI = 0.0F;
        this.bJ = 0.0F;
        switch (EntityHuman.SyntheticClass_1.a[enumdirection.ordinal()]) {
        case 1:
            this.bJ = -1.8F;
            break;

        case 2:
            this.bJ = 1.8F;
            break;

        case 3:
            this.bI = 1.8F;
            break;

        case 4:
            this.bI = -1.8F;
        }

    }

    public void a(boolean flag, boolean flag1, boolean flag2) {
        this.setSize(0.6F, 1.8F);
        IBlockData iblockdata = this.world.getType(this.bedPosition);

        if (this.bedPosition != null && iblockdata.getBlock() == Blocks.BED) {
            this.world.setTypeAndData(this.bedPosition, iblockdata.set(BlockBed.OCCUPIED, Boolean.valueOf(false)), 4);
            BlockPosition blockposition = BlockBed.a(this.world, this.bedPosition, 0);

            if (blockposition == null) {
                blockposition = this.bedPosition.up();
            }

            this.setPosition((double) ((float) blockposition.getX() + 0.5F), (double) ((float) blockposition.getY() + 0.1F), (double) ((float) blockposition.getZ() + 0.5F));
        }

        this.sleeping = false;
        if (!this.world.isClientSide && flag1) {
            this.world.everyoneSleeping();
        }

        // CraftBukkit start - fire PlayerBedLeaveEvent
        if (this.getBukkitEntity() instanceof Player) {
            Player player = (Player) this.getBukkitEntity();

            org.bukkit.block.Block bed;
            BlockPosition blockposition = this.bedPosition;
            if (blockposition != null) {
                bed = this.world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            } else {
                bed = this.world.getWorld().getBlockAt(player.getLocation());
            }

            PlayerBedLeaveEvent event = new PlayerBedLeaveEvent(player, bed);
            this.world.getServer().getPluginManager().callEvent(event);
        }
        // CraftBukkit end

        this.sleepTicks = flag ? 0 : 100;
        if (flag2) {
            this.setRespawnPosition(this.bedPosition, false);
        }

    }

    private boolean r() {
        return this.world.getType(this.bedPosition).getBlock() == Blocks.BED;
    }

    @Nullable
    public static BlockPosition getBed(World world, BlockPosition blockposition, boolean flag) {
        Block block = world.getType(blockposition).getBlock();

        if (block != Blocks.BED) {
            if (!flag) {
                return null;
            } else {
                boolean flag1 = block.d();
                boolean flag2 = world.getType(blockposition.up()).getBlock().d();

                return flag1 && flag2 ? blockposition : null;
            }
        } else {
            return BlockBed.a(world, blockposition, 0);
        }
    }

    public boolean isSleeping() {
        return this.sleeping;
    }

    public boolean isDeeplySleeping() {
        return this.sleeping && this.sleepTicks >= 100;
    }

    public void b(IChatBaseComponent ichatbasecomponent) {}

    public BlockPosition getBed() {
        return this.e;
    }

    public boolean isRespawnForced() {
        return this.f;
    }

    public void setRespawnPosition(BlockPosition blockposition, boolean flag) {
        if (blockposition != null) {
            this.e = blockposition;
            this.f = flag;
            this.spawnWorld = this.world.worldData.getName(); // CraftBukkit
        } else {
            this.e = null;
            this.f = false;
            this.spawnWorld = ""; // CraftBukkit
        }

    }

    public boolean a(Achievement achievement) {
        return false;
    }

    public void b(Statistic statistic) {
        this.a(statistic, 1);
    }

    public void a(Statistic statistic, int i) {}

    public void a(Statistic statistic) {}

    public void ci() {
        super.ci();
        this.b(StatisticList.w);
        if (this.isSprinting()) {
            this.applyExhaustion(world.spigotConfig.sprintExhaustion); // Spigot - Change to use configurable value
        } else {
            this.applyExhaustion(world.spigotConfig.walkExhaustion); // Spigot - Change to use configurable value
        }

    }

    public void g(float f, float f1) {
        double d0 = this.locX;
        double d1 = this.locY;
        double d2 = this.locZ;

        if (this.abilities.isFlying && !this.isPassenger()) {
            double d3 = this.motY;
            float f2 = this.aR;

            this.aR = this.abilities.a() * (float) (this.isSprinting() ? 2 : 1);
            super.g(f, f1);
            this.motY = d3 * 0.6D;
            this.aR = f2;
            this.fallDistance = 0.0F;
            this.setFlag(7, false);
        } else {
            super.g(f, f1);
        }

        this.checkMovement(this.locX - d0, this.locY - d1, this.locZ - d2);
    }

    public float cl() {
        return (float) this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
    }

    public void checkMovement(double d0, double d1, double d2) {
        if (!this.isPassenger()) {
            int i;

            if (this.a(Material.WATER)) {
                i = Math.round(MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
                if (i > 0) {
                    this.a(StatisticList.q, i);
                    this.applyExhaustion(world.paperConfig.playerSwimmingExhaustion); // Paper - Configurable swimming exhaustion
                }
            } else if (this.isInWater()) {
                i = Math.round(MathHelper.sqrt(d0 * d0 + d2 * d2) * 100.0F);
                if (i > 0) {
                    this.a(StatisticList.m, i);
                    this.applyExhaustion(world.paperConfig.playerSwimmingExhaustion); // Paper - Configurable swimming exhaustion
                }
            } else if (this.n_()) {
                if (d1 > 0.0D) {
                    this.a(StatisticList.o, (int) Math.round(d1 * 100.0D));
                }
            } else if (this.onGround) {
                i = Math.round(MathHelper.sqrt(d0 * d0 + d2 * d2) * 100.0F);
                if (i > 0) {
                    if (this.isSprinting()) {
                        this.a(StatisticList.l, i);
                        this.applyExhaustion(0.099999994F * (float) i * 0.01F);
                    } else if (this.isSneaking()) {
                        this.a(StatisticList.k, i);
                        this.applyExhaustion(0.005F * (float) i * 0.01F);
                    } else {
                        this.a(StatisticList.j, i);
                        this.applyExhaustion(0.01F * (float) i * 0.01F);
                    }
                }
            } else if (this.cC()) {
                i = Math.round(MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
                this.a(StatisticList.v, i);
            } else {
                i = Math.round(MathHelper.sqrt(d0 * d0 + d2 * d2) * 100.0F);
                if (i > 25) {
                    this.a(StatisticList.p, i);
                }
            }

        }
    }

    private void m(double d0, double d1, double d2) {
        if (this.isPassenger()) {
            int i = Math.round(MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);

            if (i > 0) {
                if (this.bz() instanceof EntityMinecartAbstract) {
                    this.a(StatisticList.r, i);
                    if (this.g == null) {
                        this.g = new BlockPosition(this);
                    } else if (this.g.distanceSquared((double) MathHelper.floor(this.locX), (double) MathHelper.floor(this.locY), (double) MathHelper.floor(this.locZ)) >= 1000000.0D) {
                        this.b((Statistic) AchievementList.q);
                    }
                } else if (this.bz() instanceof EntityBoat) {
                    this.a(StatisticList.s, i);
                } else if (this.bz() instanceof EntityPig) {
                    this.a(StatisticList.t, i);
                } else if (this.bz() instanceof EntityHorse) {
                    this.a(StatisticList.u, i);
                }
            }
        }

    }

    public void e(float f, float f1) {
        if (!this.abilities.canFly) {
            if (f >= 2.0F) {
                this.a(StatisticList.n, (int) Math.round((double) f * 100.0D));
            }

            super.e(f, f1);
        }
    }

    protected void ak() {
        if (!this.isSpectator()) {
            super.ak();
        }

    }

    protected SoundEffect e(int i) {
        return i > 4 ? SoundEffects.ed : SoundEffects.ej;
    }

    public void b(EntityLiving entityliving) {
        if (entityliving instanceof IMonster) {
            this.b((Statistic) AchievementList.s);
        }

        EntityTypes.MonsterEggInfo entitytypes_monsteregginfo = (EntityTypes.MonsterEggInfo) EntityTypes.eggInfo.get(EntityTypes.b((Entity) entityliving));

        if (entitytypes_monsteregginfo != null) {
            this.b(entitytypes_monsteregginfo.killEntityStatistic);
        }

    }

    public void aQ() {
        if (!this.abilities.isFlying) {
            super.aQ();
        }

    }

    public void giveExp(int i) {
        this.addScore(i);
        int j = Integer.MAX_VALUE - this.expTotal;

        if (i > j) {
            i = j;
        }

        this.exp += (float) i / (float) this.getExpToLevel();

        for (this.expTotal += i; this.exp >= 1.0F; this.exp /= (float) this.getExpToLevel()) {
            this.exp = (this.exp - 1.0F) * (float) this.getExpToLevel();
            this.levelDown(1);
        }

    }

    public int cR() {
        return this.h;
    }

    public void enchantDone(int i) {
        this.expLevel -= i;
        if (this.expLevel < 0) {
            this.expLevel = 0;
            this.exp = 0.0F;
            this.expTotal = 0;
        }

        this.h = this.random.nextInt();
    }

    public void levelDown(int i) {
        this.expLevel += i;
        if (this.expLevel < 0) {
            this.expLevel = 0;
            this.exp = 0.0F;
            this.expTotal = 0;
        }

        if (i > 0 && this.expLevel % 5 == 0 && (float) this.bR < (float) this.ticksLived - 100.0F) {
            float f = this.expLevel > 30 ? 1.0F : (float) this.expLevel / 30.0F;

            this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.ei, this.bA(), f * 0.75F, 1.0F);
            this.bR = this.ticksLived;
        }

    }

    public int getExpToLevel() {
        return this.expLevel >= 30 ? 112 + (this.expLevel - 30) * 9 : (this.expLevel >= 15 ? 37 + (this.expLevel - 15) * 5 : 7 + this.expLevel * 2);
    }

    public void applyExhaustion(float f) {
        if (!this.abilities.isInvulnerable) {
            if (!this.world.isClientSide) {
                this.foodData.a(f);
            }

        }
    }

    public FoodMetaData getFoodData() {
        return this.foodData;
    }

    public boolean l(boolean flag) {
        return (flag || this.foodData.c()) && !this.abilities.isInvulnerable;
    }

    public boolean cU() {
        return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
    }

    public boolean cV() {
        return this.abilities.mayBuild;
    }

    public boolean a(BlockPosition blockposition, EnumDirection enumdirection, @Nullable ItemStack itemstack) {
        if (this.abilities.mayBuild) {
            return true;
        } else if (itemstack == null) {
            return false;
        } else {
            BlockPosition blockposition1 = blockposition.shift(enumdirection.opposite());
            Block block = this.world.getType(blockposition1).getBlock();

            return itemstack.b(block) || itemstack.x();
        }
    }

    protected int getExpValue(EntityHuman entityhuman) {
        if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
            int i = this.expLevel * 7;

            return i > 100 ? 100 : i;
        } else {
            return 0;
        }
    }

    protected boolean alwaysGivesExp() {
        return true;
    }

    public void copyTo(EntityHuman entityhuman, boolean flag) {
        if (flag) {
            this.inventory.a(entityhuman.inventory);
            this.setHealth(entityhuman.getHealth());
            this.foodData = entityhuman.foodData;
            this.expLevel = entityhuman.expLevel;
            this.expTotal = entityhuman.expTotal;
            this.exp = entityhuman.exp;
            this.setScore(entityhuman.getScore());
            this.an = entityhuman.an;
            this.ao = entityhuman.ao;
            this.ap = entityhuman.ap;
        } else if (this.world.getGameRules().getBoolean("keepInventory") || entityhuman.isSpectator()) {
            this.inventory.a(entityhuman.inventory);
            this.expLevel = entityhuman.expLevel;
            this.expTotal = entityhuman.expTotal;
            this.exp = entityhuman.exp;
            this.setScore(entityhuman.getScore());
        }

        this.h = entityhuman.h;
        this.enderChest = entityhuman.enderChest;
        this.getDataWatcher().set(EntityHuman.bq, entityhuman.getDataWatcher().get(EntityHuman.bq));
    }

    protected boolean playStepSound() {
        return !this.abilities.isFlying;
    }

    public void updateAbilities() {}

    public void a(WorldSettings.EnumGamemode worldsettings_enumgamemode) {}

    public String getName() {
        return this.bS.getName();
    }

    public InventoryEnderChest getEnderChest() {
        return this.enderChest;
    }

    @Nullable
    public ItemStack getEquipment(EnumItemSlot enumitemslot) {
        return enumitemslot == EnumItemSlot.MAINHAND ? this.inventory.getItemInHand() : (enumitemslot == EnumItemSlot.OFFHAND ? this.inventory.extraSlots[0] : (enumitemslot.a() == EnumItemSlot.Function.ARMOR ? this.inventory.armor[enumitemslot.b()] : null));
    }

    public void setSlot(EnumItemSlot enumitemslot, @Nullable ItemStack itemstack) {
        if (enumitemslot == EnumItemSlot.MAINHAND) {
            this.a_(itemstack);
            this.inventory.items[this.inventory.itemInHandIndex] = itemstack;
        } else if (enumitemslot == EnumItemSlot.OFFHAND) {
            this.a_(itemstack);
            this.inventory.extraSlots[0] = itemstack;
        } else if (enumitemslot.a() == EnumItemSlot.Function.ARMOR) {
            this.a_(itemstack);
            this.inventory.armor[enumitemslot.b()] = itemstack;
        }

    }

    public Iterable<ItemStack> aE() {
        return Lists.newArrayList(new ItemStack[] { this.getItemInMainHand(), this.getItemInOffHand()});
    }

    public Iterable<ItemStack> getArmorItems() {
        return Arrays.asList(this.inventory.armor);
    }

    public abstract boolean isSpectator();

    public abstract boolean l_();

    public boolean be() {
        return !this.abilities.isFlying;
    }

    public Scoreboard getScoreboard() {
        return this.world.getScoreboard();
    }

    public ScoreboardTeamBase aO() {
        return this.getScoreboard().getPlayerTeam(this.getName());
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        ChatComponentText chatcomponenttext = new ChatComponentText(ScoreboardTeam.getPlayerDisplayName(this.aO(), this.getName()));

        chatcomponenttext.getChatModifier().setChatClickable(new ChatClickable(ChatClickable.EnumClickAction.SUGGEST_COMMAND, "/msg " + this.getName() + " "));
        chatcomponenttext.getChatModifier().setChatHoverable(this.bl());
        chatcomponenttext.getChatModifier().setInsertion(this.getName());
        return chatcomponenttext;
    }

    public float getHeadHeight() {
        float f = 1.62F;

        if (this.isSleeping()) {
            f = 0.2F;
        } else if (!this.isSneaking() && this.length != 1.65F) {
            if (this.cC() || this.length == 0.6F) {
                f = 0.4F;
            }
        } else {
            f -= 0.08F;
        }

        return f;
    }

    public void setAbsorptionHearts(float f) {
        if (f < 0.0F) {
            f = 0.0F;
        }

        this.getDataWatcher().set(EntityHuman.a, Float.valueOf(f));
    }

    public float getAbsorptionHearts() {
        return ((Float) this.getDataWatcher().get(EntityHuman.a)).floatValue();
    }

    public static UUID a(GameProfile gameprofile) {
        UUID uuid = gameprofile.getId();

        if (uuid == null) {
            uuid = d(gameprofile.getName());
        }

        return uuid;
    }

    public static UUID d(String s) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + s).getBytes(Charsets.UTF_8));
    }

    public boolean a(ChestLock chestlock) {
        if (chestlock.a()) {
            return true;
        } else {
            ItemStack itemstack = this.getItemInMainHand();

            return itemstack != null && itemstack.hasName() ? itemstack.getName().equals(chestlock.b()) : false;
        }
    }

    public boolean getSendCommandFeedback() {
        return this.h().worldServer[0].getGameRules().getBoolean("sendCommandFeedback");
    }

    public boolean c(int i, ItemStack itemstack) {
        if (i >= 0 && i < this.inventory.items.length) {
            this.inventory.setItem(i, itemstack);
            return true;
        } else {
            EnumItemSlot enumitemslot;

            if (i == 100 + EnumItemSlot.HEAD.b()) {
                enumitemslot = EnumItemSlot.HEAD;
            } else if (i == 100 + EnumItemSlot.CHEST.b()) {
                enumitemslot = EnumItemSlot.CHEST;
            } else if (i == 100 + EnumItemSlot.LEGS.b()) {
                enumitemslot = EnumItemSlot.LEGS;
            } else if (i == 100 + EnumItemSlot.FEET.b()) {
                enumitemslot = EnumItemSlot.FEET;
            } else {
                enumitemslot = null;
            }

            if (i == 98) {
                this.setSlot(EnumItemSlot.MAINHAND, itemstack);
                return true;
            } else if (i == 99) {
                this.setSlot(EnumItemSlot.OFFHAND, itemstack);
                return true;
            } else if (enumitemslot == null) {
                int j = i - 200;

                if (j >= 0 && j < this.enderChest.getSize()) {
                    this.enderChest.setItem(j, itemstack);
                    return true;
                } else {
                    return false;
                }
            } else {
                if (itemstack != null && itemstack.getItem() != null) {
                    if (!(itemstack.getItem() instanceof ItemArmor) && !(itemstack.getItem() instanceof ItemElytra)) {
                        if (enumitemslot != EnumItemSlot.HEAD) {
                            return false;
                        }
                    } else if (EntityInsentient.d(itemstack) != enumitemslot) {
                        return false;
                    }
                }

                this.inventory.setItem(enumitemslot.b() + this.inventory.items.length, itemstack);
                return true;
            }
        }
    }

    public EnumMainHand getMainHand() {
        return ((Byte) this.datawatcher.get(EntityHuman.br)).byteValue() == 0 ? EnumMainHand.LEFT : EnumMainHand.RIGHT;
    }

    public void a(EnumMainHand enummainhand) {
        this.datawatcher.set(EntityHuman.br, Byte.valueOf((byte) (enummainhand == EnumMainHand.LEFT ? 0 : 1)));
    }

    public float cZ() {
        return (float) (1.0D / this.getAttributeInstance(GenericAttributes.f).getValue() * 20.0D);
    }

    public float o(float f) {
        return MathHelper.a(((float) (betterPvp ? Math.max(this.aE, this.clientTicksSinceLastAttack) : this.aE) + f) / this.cZ(), 0.0F, 1.0F); // TacoSpigot - check client and server ticks
    }

    public void da() {
        this.aE = 0;
        this.clientTicksSinceLastAttack = 0; // TacoSpigot
    }

    public ItemCooldown db() {
        return this.bV;
    }

    public void collide(Entity entity) {
        if (!this.isSleeping()) {
            super.collide(entity);
        }

    }

    public float dc() {
        return (float) this.getAttributeInstance(GenericAttributes.i).getValue();
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumDirection.values().length];

        static {
            try {
                EntityHuman.SyntheticClass_1.a[EnumDirection.SOUTH.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                EntityHuman.SyntheticClass_1.a[EnumDirection.NORTH.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                EntityHuman.SyntheticClass_1.a[EnumDirection.WEST.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                EntityHuman.SyntheticClass_1.a[EnumDirection.EAST.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

        }
    }

    public static enum EnumBedResult {

        OK, NOT_POSSIBLE_HERE, NOT_POSSIBLE_NOW, TOO_FAR_AWAY, OTHER_PROBLEM, NOT_SAFE;

        private EnumBedResult() {}
    }

    public static enum EnumChatVisibility {

        FULL(0, "options.chat.visibility.full"), SYSTEM(1, "options.chat.visibility.system"), HIDDEN(2, "options.chat.visibility.hidden");

        private static final EntityHuman.EnumChatVisibility[] d = new EntityHuman.EnumChatVisibility[values().length];
        private final int e;
        private final String f;

        private EnumChatVisibility(int i, String s) {
            this.e = i;
            this.f = s;
        }

        static {
            EntityHuman.EnumChatVisibility[] aentityhuman_enumchatvisibility = values();
            int i = aentityhuman_enumchatvisibility.length;

            for (int j = 0; j < i; ++j) {
                EntityHuman.EnumChatVisibility entityhuman_enumchatvisibility = aentityhuman_enumchatvisibility[j];

                EntityHuman.EnumChatVisibility.d[entityhuman_enumchatvisibility.e] = entityhuman_enumchatvisibility;
            }

        }
    }
}
