package net.minecraft.server;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;

// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;
import org.torch.server.Caches;
// CraftBukkit end

public abstract class EntityHuman extends EntityLiving {

    private static final DataWatcherObject<Float> a = DataWatcher.a(EntityHuman.class, DataWatcherRegistry.c);
    private static final DataWatcherObject<Integer> b = DataWatcher.a(EntityHuman.class, DataWatcherRegistry.b);
    protected static final DataWatcherObject<Byte> bq = DataWatcher.a(EntityHuman.class, DataWatcherRegistry.a);
    protected static final DataWatcherObject<Byte> br = DataWatcher.a(EntityHuman.class, DataWatcherRegistry.a);
    public PlayerInventory inventory = new PlayerInventory(this);
    private InventoryEnderChest enderChest = new InventoryEnderChest(this); // CraftBukkit - add "this" to constructor
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
    private ItemStack bU;
    private final ItemCooldown bV;
    @Nullable
    public EntityFishingHook hookedFish;
    public boolean affectsSpawning = true;

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
        this.bU = ItemStack.a;
        this.bV = this.l();
        this.a(a(gameprofile));
        this.bS = gameprofile;
        this.defaultContainer = new ContainerPlayer(this.inventory, !world.isClientSide, this);
        this.activeContainer = this.defaultContainer;
        BlockPosition blockposition = world.getSpawn();

        this.setPositionRotation(blockposition.getX() + 0.5D, blockposition.getY() + 1, blockposition.getZ() + 0.5D, 0.0F, 0.0F);
        this.ba = 180.0F;
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE).setValue(1.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.10000000149011612D);
        this.getAttributeMap().b(GenericAttributes.f);
        this.getAttributeMap().b(GenericAttributes.i);
    }

    @Override
    protected void i() {
        super.i();
        this.datawatcher.register(EntityHuman.a, Float.valueOf(0.0F));
        this.datawatcher.register(EntityHuman.b, Integer.valueOf(0));
        this.datawatcher.register(EntityHuman.bq, Byte.valueOf((byte) 0));
        this.datawatcher.register(EntityHuman.br, Byte.valueOf((byte) 1));
    }

    @Override
    public void A_() {
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

        super.A_();
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
                this.dh();
            }

            this.bU = itemstack.isEmpty() ? ItemStack.a : itemstack.cloneItemStack();
        }

        this.bV.a();
        this.cL();
    }

    private void o() {
        this.bA = this.bD;
        this.bB = this.bE;
        this.bC = this.bF;
        double d0 = this.locX - this.bD;
        double d1 = this.locY - this.bE;
        double d2 = this.locZ - this.bF;
        double d3 = 10.0D;

        if (d0 > 10.0D) {
            this.bD = this.locX;
            this.bA = this.bD;
        }

        if (d2 > 10.0D) {
            this.bF = this.locZ;
            this.bC = this.bF;
        }

        if (d1 > 10.0D) {
            this.bE = this.locY;
            this.bB = this.bE;
        }

        if (d0 < -10.0D) {
            this.bD = this.locX;
            this.bA = this.bD;
        }

        if (d2 < -10.0D) {
            this.bF = this.locZ;
            this.bC = this.bF;
        }

        if (d1 < -10.0D) {
            this.bE = this.locY;
            this.bB = this.bE;
        }

        this.bD += d0 * 0.25D;
        this.bF += d2 * 0.25D;
        this.bE += d1 * 0.25D;
    }

    protected void cL() {
        float f;
        float f1;

        if (this.cH()) {
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

            axisalignedbb = new AxisAlignedBB(axisalignedbb.a, axisalignedbb.b, axisalignedbb.c, axisalignedbb.a + f, axisalignedbb.b + f1, axisalignedbb.c + f);
            if (!this.world.a(axisalignedbb)) {
                this.setSize(f, f1);
            }
        }

    }

    @Override
    public int V() {
        return this.abilities.isInvulnerable ? 1 : 80;
    }

    @Override
    protected SoundEffect aa() {
        return SoundEffects.eK;
    }

    @Override
    protected SoundEffect ab() {
        return SoundEffects.eJ;
    }

    @Override
    public int aE() {
        return 10;
    }

    @Override
    public void a(SoundEffect soundeffect, float f, float f1) {
        this.world.a(this, this.locX, this.locY, this.locZ, soundeffect, this.bC(), f, f1);
    }

    @Override
    public SoundCategory bC() {
        return SoundCategory.PLAYERS;
    }

    @Override
    public int getMaxFireTicks() {
        return 20;
    }

    @Override
    protected boolean isFrozen() {
        return this.getHealth() <= 0.0F || this.isSleeping();
    }

    public void closeInventory() {
        this.activeContainer = this.defaultContainer;
    }

    @Override
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
            this.l(this.locX - d0, this.locY - d1, this.locZ - d2);
            if (this.bB() instanceof EntityPig) {
                this.pitch = f1;
                this.yaw = f;
                this.aN = ((EntityPig) this.bB()).aN;
            }

        }
    }

    @Override
    protected void doTick() {
        super.doTick();
        this.cd();
        this.aP = this.yaw;
    }

    @Override
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

        this.inventory.n();
        this.bx = this.by;
        super.n();
        AttributeInstance attributeinstance = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);

        if (!this.world.isClientSide) {
            attributeinstance.setValue(this.abilities.b());
        }

        this.aR = this.bP;
        if (this.isSprinting()) {
            this.aR = (float) (this.aR + this.bP * 0.3D);
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
            AxisAlignedBB axisalignedbb;

            if (this.isPassenger() && !this.bB().dead) {
                axisalignedbb = this.getBoundingBox().b(this.bB().getBoundingBox()).grow(1.0D, 0.0D, 1.0D);
            } else {
                axisalignedbb = this.getBoundingBox().grow(1.0D, 0.5D, 1.0D);
            }

            List list = this.world.getEntities(this, axisalignedbb);

            for (int i = 0; i < list.size(); ++i) {
                Entity entity = (Entity) list.get(i);

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
        return this.datawatcher.get(EntityHuman.b).intValue();
    }

    public void setScore(int i) {
        this.datawatcher.set(EntityHuman.b, Integer.valueOf(i));
    }

    public void addScore(int i) {
        int j = this.getScore();

        this.datawatcher.set(EntityHuman.b, Integer.valueOf(j + i));
    }

    @Override
    public void die(DamageSource damagesource) {
        super.die(damagesource);
        this.setSize(0.2F, 0.2F);
        this.setPosition(this.locX, this.locY, this.locZ);
        this.motY = 0.10000000149011612D;
        if ("Notch".equals(this.getName())) {
            this.a(new ItemStack(Items.APPLE, 1), true, false);
        }

        if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
            this.cN();
            this.inventory.o();
        }

        if (damagesource != null) {
            this.motX = -MathHelper.cos((this.aA + this.yaw) * 0.017453292F) * 0.1F;
            this.motZ = -MathHelper.sin((this.aA + this.yaw) * 0.017453292F) * 0.1F;
        } else {
            this.motX = 0.0D;
            this.motZ = 0.0D;
        }

        this.b(StatisticList.A);
        this.a(StatisticList.h);
        this.extinguish();
        this.setFlag(0, false);
    }

    protected void cN() {
        for (int i = 0; i < this.inventory.getSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);

            if (!itemstack.isEmpty() && EnchantmentManager.e(itemstack)) {
                this.inventory.splitWithoutUpdate(i);
            }
        }

    }

    @Override
    protected SoundEffect bW() {
        return SoundEffects.ENTITY_PLAYER_HURT;
    }

    @Override
    protected SoundEffect bX() {
        return SoundEffects.eF;
    }

    @Override
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

                iterator.next().incrementScore();
                // CraftBukkit end
            }

        }
    }

    private Collection<ScoreboardScore> d(Entity entity) { // CraftBukkit
        String s = entity instanceof EntityHuman ? entity.getName() : entity.bf();
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
        return this.a(this.inventory.splitStack(this.inventory.itemInHandIndex, flag && !this.inventory.getItemInHand().isEmpty() ? this.inventory.getItemInHand().getCount() : 1), false, true);
    }

    @Nullable
    public EntityItem drop(ItemStack itemstack, boolean flag) {
        return this.a(itemstack, false, flag);
    }

    @Nullable
    public EntityItem a(ItemStack itemstack, boolean flag, boolean flag1) {
        if (itemstack.isEmpty()) {
            return null;
        } else {
            double d0 = this.locY - 0.30000001192092896D + this.getHeadHeight();
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
                entityitem.motX = -MathHelper.sin(f1) * f;
                entityitem.motZ = MathHelper.cos(f1) * f;
                entityitem.motY = 0.20000000298023224D;
            } else {
                f = 0.3F;
                entityitem.motX = -MathHelper.sin(this.yaw * 0.017453292F) * MathHelper.cos(this.pitch * 0.017453292F) * f;
                entityitem.motZ = MathHelper.cos(this.yaw * 0.017453292F) * MathHelper.cos(this.pitch * 0.017453292F) * f;
                entityitem.motY = -MathHelper.sin(this.pitch * 0.017453292F) * f + 0.1F;
                f1 = this.random.nextFloat() * 6.2831855F;
                f = 0.02F * this.random.nextFloat();
                entityitem.motX += Math.cos(f1) * f;
                entityitem.motY += (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;
                entityitem.motZ += Math.sin(f1) * f;
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
                if (!itemstack1.isEmpty()) {
                    this.a(StatisticList.e(itemstack1.getItem()), itemstack.getCount());
                }

                this.b(StatisticList.x);
            }

            return entityitem;
        }
    }

    protected ItemStack a(EntityItem entityitem) {
        this.world.addEntity(entityitem);
        return entityitem.getItemStack();
    }

    public float a(IBlockData iblockdata) {
        float f = this.inventory.a(iblockdata);

        if (f > 1.0F) {
            int i = EnchantmentManager.getDigSpeedEnchantmentLevel(this);
            ItemStack itemstack = this.getItemInMainHand();

            if (i > 0 && !itemstack.isEmpty()) {
                f += i * i + 1;
            }
        }

        if (this.hasEffect(MobEffects.FASTER_DIG)) {
            f *= 1.0F + (this.getEffect(MobEffects.FASTER_DIG).getAmplifier() + 1) * 0.2F;
        }

        if (this.hasEffect(MobEffects.SLOWER_DIG)) {
            float f1;

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

        if (this.a(Material.WATER) && !EnchantmentManager.h(this)) {
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

    public static void c(DataConverterManager dataconvertermanager) {
        dataconvertermanager.a(DataConverterTypes.PLAYER, new DataInspector() {
            @Override
            public NBTTagCompound a(DataConverter dataconverter, NBTTagCompound nbttagcompound, int i) {
                DataConverterRegistry.b(dataconverter, nbttagcompound, i, "Inventory");
                DataConverterRegistry.b(dataconverter, nbttagcompound, i, "EnderItems");
                return nbttagcompound;
            }
        });
    }

    @Override
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

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setInt("DataVersion", 922);
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
        nbttagcompound.set("EnderItems", this.enderChest.i());
        nbttagcompound.setString("SpawnWorld", spawnWorld); // CraftBukkit - fixes bed spawns for multiworld worlds
    }

    @Override
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
                        f = Math.min(f / 2.0F + 1.0F, f);
                    }

                    if (this.world.getDifficulty() == EnumDifficulty.HARD) {
                        f = f * 3.0F / 2.0F;
                    }
                }

                return super.damageEntity(damagesource, f); // CraftBukkit - Don't filter out 0 damage
            }
        }
    }

    @Override
    protected void c(EntityLiving entityliving) {
        super.c(entityliving);
        if (entityliving.getItemInMainHand().getItem() instanceof ItemAxe) {
            this.m(true);
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

    @Override
    protected void damageArmor(float f) {
        this.inventory.a(f);
    }

    @Override
    protected void damageShield(float f) {
        if (f >= 3.0F && this.activeItem.getItem() == Items.SHIELD) {
            int i = 1 + MathHelper.d(f);

            this.activeItem.damage(i, this);
            if (this.activeItem.isEmpty()) {
                EnumHand enumhand = this.cz();

                if (enumhand == EnumHand.MAIN_HAND) {
                    this.setSlot(EnumItemSlot.MAINHAND, ItemStack.a);
                } else {
                    this.setSlot(EnumItemSlot.OFFHAND, ItemStack.a);
                }

                this.activeItem = ItemStack.a;
                this.a(SoundEffects.fx, 0.8F, 0.8F + this.world.random.nextFloat() * 0.4F);
            }
        }

    }

    public float cO() {
        int i = 0;
        Iterator iterator = this.inventory.armor.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();

            if (!itemstack.isEmpty()) {
                ++i;
            }
        }

        return (float) i / (float) this.inventory.armor.size();
    }

    // CraftBukkit start
    @Override
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

    public void a(TileEntityStructure tileentitystructure) {}

    public void openTrade(IMerchant imerchant) {}

    public void openContainer(IInventory iinventory) {}

    public void openHorseInventory(EntityHorseAbstract entityhorseabstract, IInventory iinventory) {}

    public void openTileEntity(ITileEntityContainer itileentitycontainer) {}

    public void a(ItemStack itemstack, EnumHand enumhand) {}

    public EnumInteractionResult a(Entity entity, EnumHand enumhand) {
        if (this.isSpectator()) {
            if (entity instanceof IInventory) {
                this.openContainer((IInventory) entity);
            }

            return EnumInteractionResult.PASS;
        } else {
            ItemStack itemstack = this.b(enumhand);
            ItemStack itemstack1 = itemstack.isEmpty() ? ItemStack.a : itemstack.cloneItemStack();

            if (entity.b(this, enumhand)) {
                if (this.abilities.canInstantlyBuild && itemstack == this.b(enumhand) && itemstack.getCount() < itemstack1.getCount()) {
                    itemstack.setCount(itemstack1.getCount());
                }

                return EnumInteractionResult.SUCCESS;
            } else {
                if (!itemstack.isEmpty() && entity instanceof EntityLiving) {
                    if (this.abilities.canInstantlyBuild) {
                        itemstack = itemstack1;
                    }

                    if (itemstack.a(this, (EntityLiving) entity, enumhand)) {
                        if (itemstack.isEmpty() && !this.abilities.canInstantlyBuild) {
                            this.a(enumhand, ItemStack.a);
                        }

                        return EnumInteractionResult.SUCCESS;
                    }
                }

                return EnumInteractionResult.PASS;
            }
        }
    }

    @Override
    public double ax() {
        return -0.35D;
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        this.j = 0;
    }

    public void attack(Entity entity) {
        if (entity.aV()) {
            if (!entity.t(this)) {
                float f = (float) this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
                float f1;

                if (entity instanceof EntityLiving) {
                    f1 = EnchantmentManager.a(this.getItemInMainHand(), ((EntityLiving) entity).getMonsterType());
                } else {
                    f1 = EnchantmentManager.a(this.getItemInMainHand(), EnumMonsterType.UNDEFINED);
                }

                float f2 = this.o(0.5F);

                f *= 0.2F + f2 * f2 * 0.8F;
                f1 *= f2;
                this.dh();
                if (f > 0.0F || f1 > 0.0F) {
                    boolean flag = f2 > 0.9F;
                    boolean flag1 = false;
                    byte b0 = 0;
                    int i = b0 + EnchantmentManager.b(this);

                    if (this.isSprinting() && flag) {
                        this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.ex, this.bC(), 1.0F, 1.0F);
                        ++i;
                        flag1 = true;
                    }

                    boolean flag2 = flag && this.fallDistance > 0.0F && !this.onGround && !this.m_() && !this.isInWater() && !this.hasEffect(MobEffects.BLINDNESS) && !this.isPassenger() && entity instanceof EntityLiving;

                    flag2 = flag2 && !this.isSprinting();
                    if (flag2) {
                        f *= 1.5F;
                    }

                    f += f1;
                    boolean flag3 = false;
                    double d0 = this.J - this.I;

                    if (flag && !flag2 && !flag1 && this.onGround && d0 < this.cq()) {
                        ItemStack itemstack = this.b(EnumHand.MAIN_HAND);

                        if (itemstack.getItem() instanceof ItemSword) {
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
                                ((EntityLiving) entity).a(this, i * 0.5F, MathHelper.sin(this.yaw * 0.017453292F), (-MathHelper.cos(this.yaw * 0.017453292F)));
                            } else {
                                entity.f(-MathHelper.sin(this.yaw * 0.017453292F) * i * 0.5F, 0.1D, MathHelper.cos(this.yaw * 0.017453292F) * i * 0.5F);
                            }

                            this.motX *= 0.6D;
                            this.motZ *= 0.6D;
                            this.setSprinting(false);
                        }

                        if (flag3) {
                            float f4 = 1.0F + EnchantmentManager.a(this) * f;
                            List list = this.world.a(EntityLiving.class, entity.getBoundingBox().grow(1.0D, 0.25D, 1.0D));
                            Iterator iterator = list.iterator();

                            while (iterator.hasNext()) {
                                EntityLiving entityliving = (EntityLiving) iterator.next();

                                if (entityliving != this && entityliving != entity && !this.r(entityliving) && this.h(entityliving) < 9.0D) {
                                    // CraftBukkit start - Only apply knockback if the damage hits
                                    if (entityliving.damageEntity(DamageSource.playerAttack(this).sweep(), f4)) {
                                        entityliving.a(this, 0.4F, MathHelper.sin(this.yaw * 0.017453292F), (-MathHelper.cos(this.yaw * 0.017453292F)));
                                    }
                                    // CraftBukkit end
                                }
                            }

                            this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.eA, this.bC(), 1.0F, 1.0F);
                            this.cP();
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
                            this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.ENTITY_PLAYER_ATTACK_CRIT, this.bC(), 1.0F, 1.0F);
                            this.a(entity);
                        }

                        if (!flag2 && !flag3) {
                            if (flag) {
                                this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.ez, this.bC(), 1.0F, 1.0F);
                            } else {
                                this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.eB, this.bC(), 1.0F, 1.0F);
                            }
                        }

                        if (f1 > 0.0F) {
                            this.b(entity);
                        }

                        if (f >= 18.0F) {
                            this.b(AchievementList.F);
                        }

                        this.z(entity);
                        if (entity instanceof EntityLiving) {
                            EnchantmentManager.a((EntityLiving) entity, (Entity) this);
                        }

                        EnchantmentManager.b(this, entity);
                        ItemStack itemstack1 = this.getItemInMainHand();
                        Object object = entity;

                        if (entity instanceof EntityComplexPart) {
                            IComplex icomplex = ((EntityComplexPart) entity).owner;

                            if (icomplex instanceof EntityLiving) {
                                object = icomplex;
                            }
                        }

                        if (!itemstack1.isEmpty() && object instanceof EntityLiving) {
                            itemstack1.a((EntityLiving) object, this);
                            if (itemstack1.isEmpty()) {
                                this.a(EnumHand.MAIN_HAND, ItemStack.a);
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
                                int k = (int) (f5 * 0.5D);

                                ((WorldServer) this.world).a(EnumParticle.DAMAGE_INDICATOR, entity.locX, entity.locY + entity.length * 0.5F, entity.locZ, k, 0.1D, 0.0D, 0.1D, 0.2D, new int[0]);
                            }
                        }

                        this.applyExhaustion(world.spigotConfig.combatExhaustion); // Spigot - Change to use configurable value
                    } else {
                        this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.ey, this.bC(), 1.0F, 1.0F);
                        if (flag4) {
                            entity.extinguish();
                        }
                        // CraftBukkit start - resync on cancelled event
                        if (this instanceof EntityPlayer) {
                            ((EntityPlayer) this).getBukkitEntity().updateInventory();
                        }
                        // CraftBukkit end
                    }
                }

            }
        }
    }

    public void m(boolean flag) {
        float f = 0.25F + EnchantmentManager.getDigSpeedEnchantmentLevel(this) * 0.05F;

        if (flag) {
            f += 0.75F;
        }

        if (this.random.nextFloat() < f) {
            this.di().a(Items.SHIELD, 100);
            this.cF();
            this.world.broadcastEntityEffect(this, (byte) 30);
        }

    }

    public void a(Entity entity) {}

    public void b(Entity entity) {}

    public void cP() {
        double d0 = (-MathHelper.sin(this.yaw * 0.017453292F));
        double d1 = MathHelper.cos(this.yaw * 0.017453292F);

        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).a(EnumParticle.SWEEP_ATTACK, this.locX + d0, this.locY + this.length * 0.5D, this.locZ + d1, 0, d0, 0.0D, d1, 0.0D, new int[0]);
        }

    }

    @Override
    public void die() {
        super.die();
        this.defaultContainer.b(this);
        if (this.activeContainer != null) {
            this.activeContainer.b(this);
        }

    }

    @Override
    public boolean inBlock() {
        return !this.sleeping && super.inBlock();
    }

    public boolean cR() {
        return false;
    }

    public GameProfile getProfile() {
        return this.bS;
    }

    public EntityHuman.EnumBedResult a(BlockPosition blockposition) {
        EnumDirection enumdirection = this.world.getType(blockposition).get(BlockFacingHorizontal.FACING);

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

            if (!this.a(blockposition, enumdirection)) {
                return EntityHuman.EnumBedResult.TOO_FAR_AWAY;
            }

            double d0 = 8.0D;
            double d1 = 5.0D;
            List list = this.world.a(EntityMonster.class, new AxisAlignedBB(blockposition.getX() - 8.0D, blockposition.getY() - 5.0D, blockposition.getZ() - 8.0D, blockposition.getX() + 8.0D, blockposition.getY() + 5.0D, blockposition.getZ() + 8.0D));

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
            float f = 0.5F + enumdirection.getAdjacentX() * 0.4F;
            float f1 = 0.5F + enumdirection.getAdjacentZ() * 0.4F;

            this.a(enumdirection);
            this.setPosition(blockposition.getX() + f, blockposition.getY() + 0.6875F, blockposition.getZ() + f1);
        } else {
            this.setPosition(blockposition.getX() + 0.5F, blockposition.getY() + 0.6875F, blockposition.getZ() + 0.5F);
        }

        this.sleeping = true;
        this.sleepTicks = 0;
        this.bedPosition = blockposition;
        this.motX = 0.0D;
        this.motY = 0.0D;
        this.motZ = 0.0D;
        if (!this.world.isClientSide) {
            this.world.everyoneSleeping();
        }

        return EntityHuman.EnumBedResult.OK;
    }

    private boolean a(BlockPosition blockposition, EnumDirection enumdirection) {
        if (Math.abs(this.locX - blockposition.getX()) <= 3.0D && Math.abs(this.locY - blockposition.getY()) <= 2.0D && Math.abs(this.locZ - blockposition.getZ()) <= 3.0D) {
            return true;
        } else {
            BlockPosition blockposition1 = blockposition.shift(enumdirection.opposite());

            return Math.abs(this.locX - blockposition1.getX()) <= 3.0D && Math.abs(this.locY - blockposition1.getY()) <= 2.0D && Math.abs(this.locZ - blockposition1.getZ()) <= 3.0D;
        }
    }

    private void a(EnumDirection enumdirection) {
        this.bI = -1.8F * enumdirection.getAdjacentX();
        this.bJ = -1.8F * enumdirection.getAdjacentZ();
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

            this.setPosition(blockposition.getX() + 0.5F, blockposition.getY() + 0.1F, blockposition.getZ() + 0.5F);
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

    @Override
    public boolean isSleeping() {
        return this.sleeping;
    }

    public boolean isDeeplySleeping() {
        return this.sleeping && this.sleepTicks >= 100;
    }

    public void a(IChatBaseComponent ichatbasecomponent, boolean flag) {}

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

    @Override
    public void cm() {
        super.cm();
        this.b(StatisticList.w);
        if (this.isSprinting()) {
            this.applyExhaustion(world.spigotConfig.jumpSprintExhaustion); // Spigot - Change to use configurable value
        } else {
            this.applyExhaustion(world.spigotConfig.jumpWalkExhaustion); // Spigot - Change to use configurable value
        }

    }

    @Override
    public void g(float f, float f1) {
        double d0 = this.locX;
        double d1 = this.locY;
        double d2 = this.locZ;

        if (this.abilities.isFlying && !this.isPassenger()) {
            double d3 = this.motY;
            float f2 = this.aR;

            this.aR = this.abilities.a() * (this.isSprinting() ? 2 : 1);
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

    @Override
    public float cq() {
        return (float) this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
    }

    public void checkMovement(double d0, double d1, double d2) {
        if (!this.isPassenger()) {
            int i;

            if (this.a(Material.WATER)) {
                i = Math.round(MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
                if (i > 0) {
                    this.a(StatisticList.q, i);
                    this.applyExhaustion(world.spigotConfig.swimMultiplier * i * 0.01F); // Spigot
                }
            } else if (this.isInWater()) {
                i = Math.round(MathHelper.sqrt(d0 * d0 + d2 * d2) * 100.0F);
                if (i > 0) {
                    this.a(StatisticList.m, i);
                    this.applyExhaustion(world.spigotConfig.swimMultiplier * i * 0.01F); // Spigot
                }
            } else if (this.m_()) {
                if (d1 > 0.0D) {
                    this.a(StatisticList.o, (int) Math.round(d1 * 100.0D));
                }
            } else if (this.onGround) {
                i = Math.round(MathHelper.sqrt(d0 * d0 + d2 * d2) * 100.0F);
                if (i > 0) {
                    if (this.isSprinting()) {
                        this.a(StatisticList.l, i);
                        this.applyExhaustion(world.spigotConfig.sprintMultiplier * i * 0.01F); // Spigot
                    } else if (this.isSneaking()) {
                        this.a(StatisticList.k, i);
                        this.applyExhaustion(world.spigotConfig.otherMultiplier * i * 0.01F); // Spigot
                    } else {
                        this.a(StatisticList.j, i);
                        this.applyExhaustion(world.spigotConfig.otherMultiplier * i * 0.01F); // Spigot
                    }
                }
            } else if (this.cH()) {
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

    private void l(double d0, double d1, double d2) {
        if (this.isPassenger()) {
            int i = Math.round(MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);

            if (i > 0) {
                if (this.bB() instanceof EntityMinecartAbstract) {
                    this.a(StatisticList.r, i);
                    if (this.g == null) {
                        this.g = new BlockPosition(this);
                    } else if (this.g.distanceSquared(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ)) >= 1000000.0D) {
                        this.b(AchievementList.q);
                    }
                } else if (this.bB() instanceof EntityBoat) {
                    this.a(StatisticList.s, i);
                } else if (this.bB() instanceof EntityPig) {
                    this.a(StatisticList.t, i);
                } else if (this.bB() instanceof EntityHorseAbstract) {
                    this.a(StatisticList.u, i);
                }
            }
        }

    }

    @Override
    public void e(float f, float f1) {
        if (!this.abilities.canFly) {
            if (f >= 2.0F) {
                this.a(StatisticList.n, (int) Math.round(f * 100.0D));
            }

            super.e(f, f1);
        }
    }

    @Override
    protected void al() {
        if (!this.isSpectator()) {
            super.al();
        }

    }

    @Override
    protected SoundEffect e(int i) {
        return i > 4 ? SoundEffects.eC : SoundEffects.eI;
    }

    @Override
    public void b(EntityLiving entityliving) {
        if (entityliving instanceof IMonster) {
            this.b(AchievementList.s);
        }

        EntityTypes.MonsterEggInfo entitytypes_monsteregginfo = EntityTypes.eggInfo.get(EntityTypes.a(entityliving));

        if (entitytypes_monsteregginfo != null) {
            this.b(entitytypes_monsteregginfo.killEntityStatistic);
        }

    }

    @Override
    public void aS() {
        if (!this.abilities.isFlying) {
            super.aS();
        }

    }

    public void giveExp(int i) {
        this.addScore(i);
        int j = Integer.MAX_VALUE - this.expTotal;

        if (i > j) {
            i = j;
        }

        this.exp += (float) i / (float) this.getExpToLevel();

        for (this.expTotal += i; this.exp >= 1.0F; this.exp /= this.getExpToLevel()) {
            this.exp = (this.exp - 1.0F) * this.getExpToLevel();
            this.levelDown(1);
        }

    }

    public int cY() {
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

        if (i > 0 && this.expLevel % 5 == 0 && this.bR < this.ticksLived - 100.0F) {
            float f = this.expLevel > 30 ? 1.0F : this.expLevel / 30.0F;

            this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.eH, this.bC(), f * 0.75F, 1.0F);
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

    public boolean n(boolean flag) {
        return (flag || this.foodData.c()) && !this.abilities.isInvulnerable;
    }

    public boolean db() {
        return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
    }

    public boolean dc() {
        return this.abilities.mayBuild;
    }

    public boolean a(BlockPosition blockposition, EnumDirection enumdirection, ItemStack itemstack) {
        if (this.abilities.mayBuild) {
            return true;
        } else if (itemstack.isEmpty()) {
            return false;
        } else {
            BlockPosition blockposition1 = blockposition.shift(enumdirection.opposite());
            Block block = this.world.getType(blockposition1).getBlock();

            return itemstack.b(block) || itemstack.y();
        }
    }

    @Override
    protected int getExpValue(EntityHuman entityhuman) {
        if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
            int i = this.expLevel * 7;

            return i > 100 ? 100 : i;
        } else {
            return 0;
        }
    }

    @Override
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

    @Override
    protected boolean playStepSound() {
        return !this.abilities.isFlying;
    }

    public void updateAbilities() {}

    public void a(EnumGamemode enumgamemode) {}

    @Override
    public String getName() {
        return this.bS.getName();
    }

    public InventoryEnderChest getEnderChest() {
        return this.enderChest;
    }

    @Override
    public ItemStack getEquipment(EnumItemSlot enumitemslot) {
        return enumitemslot == EnumItemSlot.MAINHAND ? this.inventory.getItemInHand() : (enumitemslot == EnumItemSlot.OFFHAND ? (ItemStack) this.inventory.extraSlots.get(0) : (enumitemslot.a() == EnumItemSlot.Function.ARMOR ? (ItemStack) this.inventory.armor.get(enumitemslot.b()) : ItemStack.a));
    }

    @Override
    public void setSlot(EnumItemSlot enumitemslot, ItemStack itemstack) {
        if (enumitemslot == EnumItemSlot.MAINHAND) {
            this.a_(itemstack);
            this.inventory.items.set(this.inventory.itemInHandIndex, itemstack);
        } else if (enumitemslot == EnumItemSlot.OFFHAND) {
            this.a_(itemstack);
            this.inventory.extraSlots.set(0, itemstack);
        } else if (enumitemslot.a() == EnumItemSlot.Function.ARMOR) {
            this.a_(itemstack);
            this.inventory.armor.set(enumitemslot.b(), itemstack);
        }

    }

    public boolean c(ItemStack itemstack) {
        this.a_(itemstack);
        return this.inventory.pickup(itemstack);
    }

    @Override
    public Iterable<ItemStack> aG() {
        return Lists.newArrayList(new ItemStack[] { this.getItemInMainHand(), this.getItemInOffHand()});
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.inventory.armor;
    }

    public abstract boolean isSpectator();

    public abstract boolean z();

    @Override
    public boolean bg() {
        return !this.abilities.isFlying;
    }

    public Scoreboard getScoreboard() {
        return this.world.getScoreboard();
    }

    @Override
    public ScoreboardTeamBase aQ() {
        return this.getScoreboard().getPlayerTeam(this.getName());
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        ChatComponentText chatcomponenttext = new ChatComponentText(ScoreboardTeam.getPlayerDisplayName(this.aQ(), this.getName()));

        chatcomponenttext.getChatModifier().setChatClickable(new ChatClickable(ChatClickable.EnumClickAction.SUGGEST_COMMAND, "/msg " + this.getName() + " "));
        chatcomponenttext.getChatModifier().setChatHoverable(this.bn());
        chatcomponenttext.getChatModifier().setInsertion(this.getName());
        return chatcomponenttext;
    }

    @Override
    public float getHeadHeight() {
        float f = 1.62F;

        if (this.isSleeping()) {
            f = 0.2F;
        } else if (!this.isSneaking() && this.length != 1.65F) {
            if (this.cH() || this.length == 0.6F) {
                f = 0.4F;
            }
        } else {
            f -= 0.08F;
        }

        return f;
    }

    @Override
    public void setAbsorptionHearts(float f) {
        if (f < 0.0F) {
            f = 0.0F;
        }

        this.getDataWatcher().set(EntityHuman.a, Float.valueOf(f));
    }

    @Override
    public float getAbsorptionHearts() {
        return this.getDataWatcher().get(EntityHuman.a).floatValue();
    }

    public static UUID matchUUID(GameProfile profile) { return a(profile); } // OBFHELPER
    public static UUID a(GameProfile gameprofile) {
        UUID uuid = gameprofile.getId();

        if (uuid == null) {
            uuid = offlinePlayerUUID(gameprofile.getName());
        }

        return uuid;
    }

    // Torch start - OBFHELPER
    public static UUID offlinePlayerUUID(String name) {
        return offlinePlayerUUID(name, true);
    }

    public static UUID offlinePlayerUUID(String name, boolean toLowerCase) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + (toLowerCase ? Caches.toLowerCase(name) : name)).getBytes(Charsets.UTF_8));
    }

    public static UUID d(String name) {
        return offlinePlayerUUID(name, true);
    }
    // Torch end

    public boolean a(ChestLock chestlock) {
        if (chestlock.a()) {
            return true;
        } else {
            ItemStack itemstack = this.getItemInMainHand();

            return !itemstack.isEmpty() && itemstack.hasName() ? itemstack.getName().equals(chestlock.b()) : false;
        }
    }

    @Override
    public boolean getSendCommandFeedback() {
        return this.B_().worldServer[0].getGameRules().getBoolean("sendCommandFeedback");
    }

    @Override
    public boolean c(int i, ItemStack itemstack) {
        if (i >= 0 && i < this.inventory.items.size()) {
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
                if (!itemstack.isEmpty()) {
                    if (!(itemstack.getItem() instanceof ItemArmor) && !(itemstack.getItem() instanceof ItemElytra)) {
                        if (enumitemslot != EnumItemSlot.HEAD) {
                            return false;
                        }
                    } else if (EntityInsentient.d(itemstack) != enumitemslot) {
                        return false;
                    }
                }

                this.inventory.setItem(enumitemslot.b() + this.inventory.items.size(), itemstack);
                return true;
            }
        }
    }

    @Override
    public EnumMainHand getMainHand() {
        return this.datawatcher.get(EntityHuman.br).byteValue() == 0 ? EnumMainHand.LEFT : EnumMainHand.RIGHT;
    }

    public void a(EnumMainHand enummainhand) {
        this.datawatcher.set(EntityHuman.br, Byte.valueOf((byte) (enummainhand == EnumMainHand.LEFT ? 0 : 1)));
    }

    public float dg() {
        return (float) (1.0D / this.getAttributeInstance(GenericAttributes.f).getValue() * 20.0D);
    }

    public float o(float f) {
        return MathHelper.a((this.aE + f) / this.dg(), 0.0F, 1.0F);
    }

    public void dh() {
        this.aE = 0;
    }

    public ItemCooldown di() {
        return this.bV;
    }

    @Override
    public void collide(Entity entity) {
        if (!this.isSleeping()) {
            super.collide(entity);
        }

    }

    public float dj() {
        return (float) this.getAttributeInstance(GenericAttributes.i).getValue();
    }

    public boolean dk() {
        return this.abilities.canInstantlyBuild && this.a(2, "");
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
