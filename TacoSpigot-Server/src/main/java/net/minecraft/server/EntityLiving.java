package net.minecraft.server;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;

import java.util.Queue;
import java.util.concurrent.RecursiveAction;

// CraftBukkit start
import java.util.ArrayList;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.craftbukkit.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
// CraftBukkit end

import co.aikar.timings.MinecraftTimings; // Paper

public abstract class EntityLiving extends Entity {

    private static final UUID a = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final AttributeModifier b = (new AttributeModifier(EntityLiving.a, "Sprinting speed boost", 0.30000001192092896D, 2)).a(false);
    protected static final DataWatcherObject<Byte> at = DataWatcher.a(EntityLiving.class, DataWatcherRegistry.a);
    public static final DataWatcherObject<Float> HEALTH = DataWatcher.a(EntityLiving.class, DataWatcherRegistry.c);
    private static final DataWatcherObject<Integer> f = DataWatcher.a(EntityLiving.class, DataWatcherRegistry.b);
    private static final DataWatcherObject<Boolean> g = DataWatcher.a(EntityLiving.class, DataWatcherRegistry.h);
    private static final DataWatcherObject<Integer> h = DataWatcher.a(EntityLiving.class, DataWatcherRegistry.b);
    private AttributeMapBase bq;
    public CombatTracker combatTracker = new CombatTracker(this);
    public final Map<MobEffectList, MobEffect> effects = Maps.newHashMap();
    private final ItemStack[] bt = new ItemStack[2];
    private final ItemStack[] bu = new ItemStack[4];
    public boolean au;
    public EnumHand av;
    public int aw;
    public int ax;
    public int hurtTicks;
    public int az;
    public float aA;
    public int deathTicks;
    public float aC;
    public float aD;
    protected int aE;
    public float aF;
    public float aG;
    public float aH;
    public int maxNoDamageTicks = 20;
    public float aJ;
    public float aK;
    public float aL;
    public float aM;
    public float aN;
    public float aO;
    public float aP;
    public float aQ;
    public float aR = 0.02F;
    public EntityHuman killer;
    protected int lastDamageByPlayerTime;
    protected boolean aU;
    protected int ticksFarFromPlayer;
    protected float aW;
    protected float aX;
    protected float aY;
    protected float aZ;
    protected float ba;
    protected int bb;
    public float lastDamage;
    protected boolean bd;
    public float be;
    public float bf;
    public float bg;
    protected int bh;
    protected double bi;
    protected double bj;
    protected double bk;
    protected double bl;
    protected double bm;
    public boolean updateEffects = true;
    public EntityLiving lastDamager;
    public int hurtTimestamp;
    private EntityLiving by;
    private int bz;
    private float bA;
    private int bB;
    private float bC;
    protected ItemStack bn;
    protected int bo;
    protected int bp;
    private BlockPosition bD;
    // CraftBukkit start
    public int expToDrop;
    public int maxAirTicks = 300;
    boolean forceDrops;
    ArrayList<org.bukkit.inventory.ItemStack> drops = new ArrayList<org.bukkit.inventory.ItemStack>();
    public org.bukkit.craftbukkit.attribute.CraftAttributeMap craftAttributes;
    public boolean collides = true;
    // CraftBukkit end
    // Spigot start
    public void inactiveTick()
    {
        super.inactiveTick();
        ++this.ticksFarFromPlayer; // Above all the floats
    }
    // Spigot end

    public void Q() {
        this.damageEntity(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
    }

    public EntityLiving(World world) {
        super(world);
        this.initAttributes();
        // CraftBukkit - setHealth(getMaxHealth()) inlined and simplified to skip the instanceof check for EntityPlayer, as getBukkitEntity() is not initialized in constructor
        this.datawatcher.set(EntityLiving.HEALTH, (float) this.getAttributeInstance(GenericAttributes.maxHealth).getValue());
        this.i = true;
        this.aM = (float) ((Math.random() + 1.0D) * 0.009999999776482582D);
        this.setPosition(this.locX, this.locY, this.locZ);
        this.aL = (float) Math.random() * 12398.0F;
        this.yaw = (float) (Math.random() * 6.2831854820251465D);
        this.aP = this.yaw;
        this.P = 0.6F;
    }

    protected void i() {
        this.datawatcher.register(EntityLiving.at, Byte.valueOf((byte) 0));
        this.datawatcher.register(EntityLiving.f, Integer.valueOf(0));
        this.datawatcher.register(EntityLiving.g, Boolean.valueOf(false));
        this.datawatcher.register(EntityLiving.h, Integer.valueOf(0));
        this.datawatcher.register(EntityLiving.HEALTH, Float.valueOf(1.0F));
    }

    protected void initAttributes() {
        this.getAttributeMap().b(GenericAttributes.maxHealth);
        this.getAttributeMap().b(GenericAttributes.c);
        this.getAttributeMap().b(GenericAttributes.MOVEMENT_SPEED);
        this.getAttributeMap().b(GenericAttributes.g);
        this.getAttributeMap().b(GenericAttributes.h);
    }

    protected void a(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {
        if (!this.isInWater()) {
            this.aj();
        }

        if (!this.world.isClientSide && this.fallDistance > 3.0F && flag) {
            float f = (float) MathHelper.f(this.fallDistance - 3.0F);

            if (iblockdata.getMaterial() != Material.AIR) {
                double d1 = Math.min((double) (0.2F + f / 15.0F), 2.5D);
                int i = (int) (150.0D * d1);

                // CraftBukkit start - visiblity api
                if (this instanceof EntityPlayer) {
                    ((WorldServer) this.world).sendParticles((EntityPlayer) this, EnumParticle.BLOCK_DUST, false, this.locX, this.locY, this.locZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, new int[] { Block.getCombinedId(iblockdata)});
                } else {
                    ((WorldServer) this.world).a(EnumParticle.BLOCK_DUST, this.locX, this.locY, this.locZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, new int[] { Block.getCombinedId(iblockdata)});
                }
                // CraftBukkit end
            }
        }

        super.a(d0, flag, iblockdata, blockposition);
    }

    public boolean bC() {
        return false;
    }

    public void U() {
        this.aC = this.aD;
        super.U();
        this.world.methodProfiler.a("livingEntityBaseTick");
        boolean flag = this instanceof EntityHuman;

        if (this.isAlive()) {
            if (this.inBlock()) {
                this.damageEntity(DamageSource.STUCK, 1.0F);
            } else if (flag && !this.world.getWorldBorder().a(this.getBoundingBox())) {
                double d0 = this.world.getWorldBorder().a((Entity) this) + this.world.getWorldBorder().getDamageBuffer();

                if (d0 < 0.0D) {
                    double d1 = this.world.getWorldBorder().getDamageAmount();

                    if (d1 > 0.0D) {
                        this.damageEntity(DamageSource.STUCK, (float) Math.max(1, MathHelper.floor(-d0 * d1)));
                    }
                }
            }
        }

        if (this.isFireProof() || this.world.isClientSide) {
            this.extinguish();
        }

        boolean flag1 = flag && ((EntityHuman) this).abilities.isInvulnerable;

        if (this.isAlive()) {
            if (this.a(Material.WATER)) {
                if (!this.bC() && !this.hasEffect(MobEffects.WATER_BREATHING) && !flag1) {
                    this.setAirTicks(this.d(this.getAirTicks()));
                    if (this.getAirTicks() == -20) {
                        this.setAirTicks(0);

                        for (int i = 0; i < 8; ++i) {
                            float f = this.random.nextFloat() - this.random.nextFloat();
                            float f1 = this.random.nextFloat() - this.random.nextFloat();
                            float f2 = this.random.nextFloat() - this.random.nextFloat();

                            this.world.addParticle(EnumParticle.WATER_BUBBLE, this.locX + (double) f, this.locY + (double) f1, this.locZ + (double) f2, this.motX, this.motY, this.motZ, new int[0]);
                        }

                        this.damageEntity(DamageSource.DROWN, 2.0F);
                    }
                }

                if (!this.world.isClientSide && this.isPassenger() && this.bz() instanceof EntityLiving) {
                    this.stopRiding();
                }
            } else {
                // CraftBukkit start - Only set if needed to work around a DataWatcher inefficiency
                if (this.getAirTicks() != 300) {
                    this.setAirTicks(maxAirTicks);
                }
                // CraftBukkit end
            }

            if (!this.world.isClientSide) {
                BlockPosition blockposition = new BlockPosition(this);

                if (!Objects.equal(this.bD, blockposition)) {
                    this.bD = blockposition;
                    this.b(blockposition);
                }
            }
        }

        if (this.isAlive() && this.ah()) {
            this.extinguish();
        }

        this.aJ = this.aK;
        if (this.hurtTicks > 0) {
            --this.hurtTicks;
        }

        if (this.noDamageTicks > 0 && !(this instanceof EntityPlayer)) {
            --this.noDamageTicks;
        }

        if (this.getHealth() <= 0.0F) {
            this.bD();
        }

        if (this.lastDamageByPlayerTime > 0) {
            --this.lastDamageByPlayerTime;
        } else {
            this.killer = null;
        }

        if (this.by != null && !this.by.isAlive()) {
            this.by = null;
        }

        if (this.lastDamager != null) {
            if (!this.lastDamager.isAlive()) {
                this.a((EntityLiving) null);
            } else if (this.ticksLived - this.hurtTimestamp > 100) {
                this.a((EntityLiving) null);
            }
        }

        this.tickPotionEffects();
        this.aZ = this.aY;
        this.aO = this.aN;
        this.aQ = this.aP;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.world.methodProfiler.b();
    }

    // CraftBukkit start
    public int getExpReward() {
        int exp = this.getExpValue(this.killer);

        if (!this.world.isClientSide && (this.lastDamageByPlayerTime > 0 || this.alwaysGivesExp()) && this.isDropExperience() && this.world.getGameRules().getBoolean("doMobLoot")) {
            return exp;
        } else {
            return 0;
        }
    }
    // CraftBukkit end

    protected void b(BlockPosition blockposition) {
        int i = EnchantmentManager.a(Enchantments.j, this);

        if (i > 0) {
            EnchantmentFrostWalker.a(this, this.world, blockposition, i);
        }

    }

    public boolean isBaby() {
        return false;
    }

    protected void bD() {
        ++this.deathTicks;
        if (this.deathTicks >= 20 && !this.dead) { // CraftBukkit - (this.deathTicks == 20) -> (this.deathTicks >= 20 && !this.dead)
            int i;

            // CraftBukkit start - Update getExpReward() above if the removed if() changes!
            i = this.expToDrop;
            while (i > 0) {
                int j = EntityExperienceOrb.getOrbValue(i);

                i -= j;
                this.world.addEntity(new EntityExperienceOrb(this.world, this.locX, this.locY, this.locZ, j));
            }
            this.expToDrop = 0;
            // CraftBukkit end

            this.die();

            for (i = 0; i < 20; ++i) {
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;

                this.world.addParticle(EnumParticle.EXPLOSION_NORMAL, this.locX + (double) (this.random.nextFloat() * this.width * 2.0F) - (double) this.width, this.locY + (double) (this.random.nextFloat() * this.length), this.locZ + (double) (this.random.nextFloat() * this.width * 2.0F) - (double) this.width, d0, d1, d2, new int[0]);
            }
        }

    }

    protected boolean isDropExperience() {
        return !this.isBaby();
    }

    protected int d(int i) {
        int j = EnchantmentManager.getOxygenEnchantmentLevel(this);

        return j > 0 && this.random.nextInt(j + 1) > 0 ? i : i - 1;
    }

    protected int getExpValue(EntityHuman entityhuman) {
        return 0;
    }

    protected boolean alwaysGivesExp() {
        return false;
    }

    public Random getRandom() {
        return this.random;
    }

    public EntityLiving getLastDamager() {
        return this.lastDamager;
    }

    public int bI() {
        return this.hurtTimestamp;
    }

    public void a(@Nullable EntityLiving entityliving) {
        this.lastDamager = entityliving;
        this.hurtTimestamp = this.ticksLived;
    }

    public EntityLiving bJ() {
        return this.by;
    }

    public int bK() {
        return this.bz;
    }

    public void z(Entity entity) {
        if (entity instanceof EntityLiving) {
            this.by = (EntityLiving) entity;
        } else {
            this.by = null;
        }

        this.bz = this.ticksLived;
    }

    public int bL() {
        return this.ticksFarFromPlayer;
    }

    protected void a_(@Nullable ItemStack itemstack) {
        if (itemstack != null) {
            SoundEffect soundeffect = SoundEffects.p;
            Item item = itemstack.getItem();

            if (item instanceof ItemArmor) {
                soundeffect = ((ItemArmor) item).d().b();
            } else if (item == Items.cR) {
                soundeffect = SoundEffects.s;
            }

            this.a(soundeffect, 1.0F, 1.0F);
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setFloat("Health", this.getHealth());
        nbttagcompound.setShort("HurtTime", (short) this.hurtTicks);
        nbttagcompound.setInt("HurtByTimestamp", this.hurtTimestamp);
        nbttagcompound.setShort("DeathTime", (short) this.deathTicks);
        nbttagcompound.setFloat("AbsorptionAmount", this.getAbsorptionHearts());
        EnumItemSlot[] aenumitemslot = EnumItemSlot.values();
        int i = aenumitemslot.length;

        int j;
        EnumItemSlot enumitemslot;
        ItemStack itemstack;

        for (j = 0; j < i; ++j) {
            enumitemslot = aenumitemslot[j];
            itemstack = this.getEquipment(enumitemslot);
            if (itemstack != null) {
                this.getAttributeMap().a(itemstack.a(enumitemslot));
            }
        }

        nbttagcompound.set("Attributes", GenericAttributes.a(this.getAttributeMap()));
        aenumitemslot = EnumItemSlot.values();
        i = aenumitemslot.length;

        for (j = 0; j < i; ++j) {
            enumitemslot = aenumitemslot[j];
            itemstack = this.getEquipment(enumitemslot);
            if (itemstack != null) {
                this.getAttributeMap().b(itemstack.a(enumitemslot));
            }
        }

        if (!this.effects.isEmpty()) {
            NBTTagList nbttaglist = new NBTTagList();
            Iterator iterator = this.effects.values().iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator.next();

                nbttaglist.add(mobeffect.a(new NBTTagCompound()));
            }

            nbttagcompound.set("ActiveEffects", nbttaglist);
        }

    }

    public void a(NBTTagCompound nbttagcompound) {
        // Paper start - jvm keeps optimizing the setter
        float absorptionAmount = nbttagcompound.getFloat("AbsorptionAmount");
        if (Float.isNaN(absorptionAmount)) {
            absorptionAmount = 0;
        }
        this.setAbsorptionHearts(absorptionAmount);
        // Paper end
        if (nbttagcompound.hasKeyOfType("Attributes", 9) && this.world != null && !this.world.isClientSide) {
            GenericAttributes.a(this.getAttributeMap(), nbttagcompound.getList("Attributes", 10));
        }

        if (nbttagcompound.hasKeyOfType("ActiveEffects", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getList("ActiveEffects", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.get(i);
                MobEffect mobeffect = MobEffect.b(nbttagcompound1);

                if (mobeffect != null) {
                    this.effects.put(mobeffect.getMobEffect(), mobeffect);
                }
            }
        }

        // CraftBukkit start
        if (nbttagcompound.hasKey("Bukkit.MaxHealth")) {
            NBTBase nbtbase = nbttagcompound.get("Bukkit.MaxHealth");
            if (nbtbase.getTypeId() == 5) {
                this.getAttributeInstance(GenericAttributes.maxHealth).setValue((double) ((NBTTagFloat) nbtbase).c());
            } else if (nbtbase.getTypeId() == 3) {
                this.getAttributeInstance(GenericAttributes.maxHealth).setValue((double) ((NBTTagInt) nbtbase).d());
            }
        }
        // CraftBukkit end

        if (nbttagcompound.hasKeyOfType("Health", 99)) {
            this.setHealth(nbttagcompound.getFloat("Health"));
        }

        this.hurtTicks = nbttagcompound.getShort("HurtTime");
        this.deathTicks = nbttagcompound.getShort("DeathTime");
        this.hurtTimestamp = nbttagcompound.getInt("HurtByTimestamp");
        if (nbttagcompound.hasKeyOfType("Team", 8)) {
            String s = nbttagcompound.getString("Team");

            this.world.getScoreboard().addPlayerToTeam(this.bd(), s);
        }

    }

    // CraftBukkit start
    private boolean isTickingEffects = false;
    private List<Object> effectsToProcess = Lists.newArrayList();
    // CraftBukkit end

    protected void tickPotionEffects() {
        Iterator iterator = this.effects.keySet().iterator();

        isTickingEffects = true; // CraftBukkit
        while (iterator.hasNext()) {
            MobEffectList mobeffectlist = (MobEffectList) iterator.next();
            MobEffect mobeffect = (MobEffect) this.effects.get(mobeffectlist);

            if (!mobeffect.tick(this)) {
                if (!this.world.isClientSide) {
                    iterator.remove();
                    this.b(mobeffect);
                }
            } else if (mobeffect.getDuration() % 600 == 0) {
                this.a(mobeffect, false);
            }
        }
        // CraftBukkit start
        isTickingEffects = false;
        for (Object e : effectsToProcess) {
            if (e instanceof MobEffect) {
                addEffect((MobEffect) e);
            } else {
                removeEffect((MobEffectList) e);
            }
        }
        // CraftBukkit end

        if (this.updateEffects) {
            if (!this.world.isClientSide) {
                this.F();
            }

            this.updateEffects = false;
        }

        int i = ((Integer) this.datawatcher.get(EntityLiving.f)).intValue();
        boolean flag = ((Boolean) this.datawatcher.get(EntityLiving.g)).booleanValue();

        if (i > 0) {
            boolean flag1 = false;

            if (!this.isInvisible()) {
                flag1 = this.random.nextBoolean();
            } else {
                flag1 = this.random.nextInt(15) == 0;
            }

            if (flag) {
                flag1 &= this.random.nextInt(5) == 0;
            }

            if (flag1 && i > 0) {
                double d0 = (double) (i >> 16 & 255) / 255.0D;
                double d1 = (double) (i >> 8 & 255) / 255.0D;
                double d2 = (double) (i >> 0 & 255) / 255.0D;

                this.world.addParticle(flag ? EnumParticle.SPELL_MOB_AMBIENT : EnumParticle.SPELL_MOB, this.locX + (this.random.nextDouble() - 0.5D) * (double) this.width, this.locY + this.random.nextDouble() * (double) this.length, this.locZ + (this.random.nextDouble() - 0.5D) * (double) this.width, d0, d1, d2, new int[0]);
            }
        }

    }

    protected void F() {
        if (this.effects.isEmpty()) {
            this.bN();
            this.setInvisible(false);
        } else {
            Collection collection = this.effects.values();

            this.datawatcher.set(EntityLiving.g, Boolean.valueOf(a(collection)));
            this.datawatcher.set(EntityLiving.f, Integer.valueOf(PotionUtil.a(collection)));
            this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY));
        }

    }

    public static boolean a(Collection<MobEffect> collection) {
        Iterator iterator = collection.iterator();

        MobEffect mobeffect;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            mobeffect = (MobEffect) iterator.next();
        } while (mobeffect.isAmbient());

        return false;
    }

    protected void bN() {
        this.datawatcher.set(EntityLiving.g, Boolean.valueOf(false));
        this.datawatcher.set(EntityLiving.f, Integer.valueOf(0));
    }

    public void removeAllEffects() {
        if (!this.world.isClientSide) {
            Iterator iterator = this.effects.values().iterator();

            while (iterator.hasNext()) {
                this.b((MobEffect) iterator.next());
                iterator.remove();
            }

        }
    }

    public Collection<MobEffect> getEffects() {
        return this.effects.values();
    }

    public boolean hasEffect(MobEffectList mobeffectlist) {
        return this.effects.containsKey(mobeffectlist);
    }

    @Nullable
    public MobEffect getEffect(MobEffectList mobeffectlist) {
        return (MobEffect) this.effects.get(mobeffectlist);
    }

    public void addEffect(MobEffect mobeffect) {
        if (org.spigotmc.AsyncCatcher.catchOp_flag()) { // HOSE
            System.out.println("AsyncCatcher: effect add.");
            return;
        }
        // CraftBukkit start
        if (isTickingEffects) {
            effectsToProcess.add(mobeffect);
            return;
        }
        // CraftBukkit end
        if (this.d(mobeffect)) {
            MobEffect mobeffect1 = (MobEffect) this.effects.get(mobeffect.getMobEffect());

            if (mobeffect1 == null) {
                this.effects.put(mobeffect.getMobEffect(), mobeffect);
                this.a(mobeffect);
            } else {
                mobeffect1.a(mobeffect);
                this.a(mobeffect1, true);
            }

        }
    }

    public boolean d(MobEffect mobeffect) {
        if (this.getMonsterType() == EnumMonsterType.UNDEAD) {
            MobEffectList mobeffectlist = mobeffect.getMobEffect();

            if (mobeffectlist == MobEffects.REGENERATION || mobeffectlist == MobEffects.POISON) {
                return false;
            }
        }

        return true;
    }

    public boolean bQ() {
        return this.getMonsterType() == EnumMonsterType.UNDEAD;
    }

    @Nullable
    public MobEffect c(@Nullable MobEffectList mobeffectlist) {
        // CraftBukkit start
        if (isTickingEffects) {
            effectsToProcess.add(mobeffectlist);
            return null;
        }
        // CraftBukkit end
        return (MobEffect) this.effects.remove(mobeffectlist);
    }

    public void removeEffect(MobEffectList mobeffectlist) {
        MobEffect mobeffect = this.c(mobeffectlist);

        if (mobeffect != null) {
            this.b(mobeffect);
        }

    }

    protected void a(MobEffect mobeffect) {
        this.updateEffects = true;
        if (!this.world.isClientSide) {
            mobeffect.getMobEffect().b(this, this.getAttributeMap(), mobeffect.getAmplifier());
        }

    }

    protected void a(MobEffect mobeffect, boolean flag) {
        this.updateEffects = true;
        if (flag && !this.world.isClientSide) {
            MobEffectList mobeffectlist = mobeffect.getMobEffect();

            mobeffectlist.a(this, this.getAttributeMap(), mobeffect.getAmplifier());
            mobeffectlist.b(this, this.getAttributeMap(), mobeffect.getAmplifier());
        }

    }

    protected void b(MobEffect mobeffect) {
        this.updateEffects = true;
        if (!this.world.isClientSide) {
            mobeffect.getMobEffect().a(this, this.getAttributeMap(), mobeffect.getAmplifier());
        }

    }

    // CraftBukkit start - Delegate so we can handle providing a reason for health being regained
    public void heal(float f) {
        heal(f, EntityRegainHealthEvent.RegainReason.CUSTOM);
    }

    public void heal(float f, EntityRegainHealthEvent.RegainReason regainReason) {
        // Paper start - Forward
        heal(f, regainReason, false);
    }

    public void heal(float f, EntityRegainHealthEvent.RegainReason regainReason, boolean isFastRegen) {
        // Paper end
        float f1 = this.getHealth();

        if (f1 > 0.0F) {
            EntityRegainHealthEvent event = new EntityRegainHealthEvent(this.getBukkitEntity(), f, regainReason, isFastRegen); // Paper - Add isFastRegen
            this.world.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                this.setHealth((float) (this.getHealth() + event.getAmount()));
            }
            // CraftBukkit end
        }

    }

    public final float getHealth() {
        // CraftBukkit start - Use unscaled health
        if (this instanceof EntityPlayer) {
            return (float) ((EntityPlayer) this).getBukkitEntity().getHealth();
        }
        // CraftBukkit end
        return ((Float) this.datawatcher.get(EntityLiving.HEALTH)).floatValue();
    }

    public void setHealth(float f) {
        // Paper start
        if (Float.isNaN(f)) { f = getMaxHealth(); if (this.valid) {
            System.err.println("[NAN-HEALTH] " + getName() + " had NaN health set");
        } } // Paper end
        // CraftBukkit start - Handle scaled health
        if (this instanceof EntityPlayer) {
            org.bukkit.craftbukkit.entity.CraftPlayer player = ((EntityPlayer) this).getBukkitEntity();
            // Squeeze
            if (f < 0.0F) {
                player.setRealHealth(0.0D);
            } else if (f > player.getMaxHealth()) {
                player.setRealHealth(player.getMaxHealth());
            } else {
                player.setRealHealth(f);
            }

            this.datawatcher.set(EntityLiving.HEALTH, Float.valueOf(player.getScaledHealth()));
            return;
        }
        // CraftBukkit end
        this.datawatcher.set(EntityLiving.HEALTH, Float.valueOf(MathHelper.a(f, 0.0F, this.getMaxHealth())));
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else if (this.world.isClientSide) {
            return false;
        } else {
            this.ticksFarFromPlayer = 0;
            if (this.getHealth() <= 0.0F) {
                return false;
            } else if (damagesource.o() && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                return false;
            } else {
                // CraftBukkit - Moved into damageEntity0(DamageSource, float)
                if (false && (damagesource == DamageSource.ANVIL || damagesource == DamageSource.FALLING_BLOCK) && this.getEquipment(EnumItemSlot.HEAD) != null) {
                    this.getEquipment(EnumItemSlot.HEAD).damage((int) (f * 4.0F + this.random.nextFloat() * f * 2.0F), this);
                    f *= 0.75F;
                }

                boolean flag = f > 0.0F && this.d(damagesource); // Copied from below

                // CraftBukkit - Moved into damageEntity0(DamageSource, float)
                if (false && f > 0.0F && this.d(damagesource)) {
                    this.k(f);
                    if (damagesource.a()) {
                        f = 0.0F;
                    } else {
                        f *= 0.33F;
                        if (damagesource.i() instanceof EntityLiving) {
                            ((EntityLiving) damagesource.i()).a(this, 0.5F, this.locX - damagesource.i().locX, this.locZ - damagesource.i().locZ);
                        }
                    }

                    flag = true;
                }

                this.aG = 1.5F;
                boolean flag1 = true;

                if ((float) this.noDamageTicks > (float) this.maxNoDamageTicks / 2.0F) {
                    if (f <= this.lastDamage) {
                        this.forceExplosionKnockback = true; // CraftBukkit - SPIGOT-949 - for vanilla consistency, cooldown does not prevent explosion knockback
                        return false;
                    }

                    // CraftBukkit start
                    if (!this.damageEntity0(damagesource, f - this.lastDamage)) {
                        return false;
                    }
                    // CraftBukkit end
                    this.lastDamage = f;
                    flag1 = false;
                } else {
                    // CraftBukkit start
                    if (!this.damageEntity0(damagesource, f)) {
                        return false;
                    }
                    this.lastDamage = f;
                    this.noDamageTicks = this.maxNoDamageTicks;
                    // this.damageEntity0(damagesource, f);
                    // CraftBukkit end
                    this.hurtTicks = this.az = 10;
                }

                // CraftBukkit start
                if(this instanceof EntityAnimal){
                    ((EntityAnimal)this).resetLove(); 
                    if(this instanceof EntityTameableAnimal){
                        ((EntityTameableAnimal)this).getGoalSit().setSitting(false);
                    }
                }
                // CraftBukkit end

                this.aA = 0.0F;
                Entity entity = damagesource.getEntity();

                if (entity != null) {
                    if (entity instanceof EntityLiving) {
                        this.a((EntityLiving) entity);
                    }

                    if (entity instanceof EntityHuman) {
                        this.lastDamageByPlayerTime = 100;
                        this.killer = (EntityHuman) entity;
                    } else if (entity instanceof EntityWolf) {
                        EntityWolf entitywolf = (EntityWolf) entity;

                        if (entitywolf.isTamed()) {
                            this.lastDamageByPlayerTime = 100;
                            this.killer = null;
                        }
                    }
                }

                boolean knockbackCancelled = world.paperConfig.disableExplosionKnockback && damagesource.isExplosion() && this instanceof EntityHuman; // Paper - Disable explosion knockback
                if (flag1) {
                    if (flag) {
                        this.world.broadcastEntityEffect(this, (byte) 29);
                    } else if (damagesource instanceof EntityDamageSource && ((EntityDamageSource) damagesource).x()) {
                        this.world.broadcastEntityEffect(this, (byte) 33);
                    } else {
                        if (!knockbackCancelled) // Paper - Disable explosion knockback
                        this.world.broadcastEntityEffect(this, (byte) 2);
                    }

                    if (damagesource != DamageSource.DROWN && (!flag || f > 0.0F)) {
                        this.ao();
                    }

                    if (entity != null) {
                        double d0 = entity.locX - this.locX;

                        double d1;

                        for (d1 = entity.locZ - this.locZ; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                            d0 = (Math.random() - Math.random()) * 0.01D;
                        }

                        this.aA = (float) (MathHelper.b(d1, d0) * 57.2957763671875D - (double) this.yaw);
                        this.a(entity, 0.4F, d0, d1);
                    } else {
                        this.aA = (float) ((int) (Math.random() * 2.0D) * 180);
                    }
                }

                if (knockbackCancelled) this.world.broadcastEntityEffect(this, (byte) 2); // Paper - Disable explosion knockback

                if (this.getHealth() <= 0.0F) {
                    SoundEffect soundeffect = this.bT();

                    if (flag1 && soundeffect != null) {
                        this.a(soundeffect, this.ce(), this.cf());
                    }

                    this.die(damagesource);
                } else if (flag1) {
                    this.c(damagesource);
                }

                return !flag || f > 0.0F;
            }
        }
    }

    protected void c(DamageSource damagesource) {
        SoundEffect soundeffect = this.bS();

        if (soundeffect != null) {
            this.a(soundeffect, this.ce(), this.cf());
        }

    }

    private boolean d(DamageSource damagesource) {
        if (!damagesource.ignoresArmor() && this.isBlocking()) {
            Vec3D vec3d = damagesource.v();

            if (vec3d != null) {
                Vec3D vec3d1 = this.f(1.0F);
                Vec3D vec3d2 = vec3d.a(new Vec3D(this.locX, this.locY, this.locZ)).a();

                vec3d2 = new Vec3D(vec3d2.x, 0.0D, vec3d2.z);
                if (vec3d2.b(vec3d1) < 0.0D) {
                    return true;
                }
            }
        }

        return false;
    }

    public void b(ItemStack itemstack) {
        this.a(SoundEffects.cU, 0.8F, 0.8F + this.world.random.nextFloat() * 0.4F);

        for (int i = 0; i < 5; ++i) {
            Vec3D vec3d = new Vec3D(((double) this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);

            vec3d = vec3d.a(-this.pitch * 0.017453292F);
            vec3d = vec3d.b(-this.yaw * 0.017453292F);
            double d0 = (double) (-this.random.nextFloat()) * 0.6D - 0.3D;
            Vec3D vec3d1 = new Vec3D(((double) this.random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);

            vec3d1 = vec3d1.a(-this.pitch * 0.017453292F);
            vec3d1 = vec3d1.b(-this.yaw * 0.017453292F);
            vec3d1 = vec3d1.add(this.locX, this.locY + (double) this.getHeadHeight(), this.locZ);
            this.world.addParticle(EnumParticle.ITEM_CRACK, vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z, new int[] { Item.getId(itemstack.getItem())});
        }

    }

    public void die(DamageSource damagesource) {
        if (!this.aU) {
            Entity entity = damagesource.getEntity();
            EntityLiving entityliving = this.bW();

            if (this.bb >= 0 && entityliving != null) {
                entityliving.b(this, this.bb);
            }

            if (entity != null) {
                entity.b(this);
            }

            this.aU = true;
            this.getCombatTracker().g();
            if (!this.world.isClientSide) {
                int i = 0;

                if (entity instanceof EntityHuman) {
                    i = EnchantmentManager.h((EntityLiving) entity);
                }

                if (this.isDropExperience() && this.world.getGameRules().getBoolean("doMobLoot")) {
                    boolean flag = this.lastDamageByPlayerTime > 0;

                    this.a(flag, i, damagesource);
                    // CraftBukkit start - Call death event
                    CraftEventFactory.callEntityDeathEvent(this, this.drops);
                    this.drops = new ArrayList<org.bukkit.inventory.ItemStack>();
                } else {
                    CraftEventFactory.callEntityDeathEvent(this);
                    // CraftBukkit end
                }
            }

            this.world.broadcastEntityEffect(this, (byte) 3);
        }
    }

    protected void a(boolean flag, int i, DamageSource damagesource) {
        this.dropDeathLoot(flag, i);
        this.dropEquipment(flag, i);
    }

    protected void dropEquipment(boolean flag, int i) {}

    public void a(Entity entity, float f, double d0, double d1) {
        if (this.random.nextDouble() >= this.getAttributeInstance(GenericAttributes.c).getValue()) {
            this.impulse = true;
            float f1 = MathHelper.sqrt(d0 * d0 + d1 * d1);

            this.motX /= 2.0D;
            this.motZ /= 2.0D;
            this.motX -= d0 / (double) f1 * (double) f;
            this.motZ -= d1 / (double) f1 * (double) f;
            if (this.onGround) {
                this.motY /= 2.0D;
                this.motY += (double) f;
                if (this.motY > 0.4000000059604645D) {
                    this.motY = 0.4000000059604645D;
                }
            }

        }
    }

    @Nullable
    protected SoundEffect bS() {
        return SoundEffects.bG;
    }

    @Nullable
    protected SoundEffect bT() {
        return SoundEffects.bB;
    }

    protected SoundEffect e(int i) {
        return i > 4 ? SoundEffects.bz : SoundEffects.bH;
    }

    protected void dropDeathLoot(boolean flag, int i) {}

    public boolean n_() {
        int i = MathHelper.floor(this.locX);
        int j = MathHelper.floor(this.getBoundingBox().b);
        int k = MathHelper.floor(this.locZ);

        if (this instanceof EntityHuman && ((EntityHuman) this).isSpectator()) {
            return false;
        } else {
            BlockPosition blockposition = new BlockPosition(i, j, k);
            IBlockData iblockdata = this.world.getType(blockposition);
            Block block = iblockdata.getBlock();

            return block != Blocks.LADDER && block != Blocks.VINE ? block instanceof BlockTrapdoor && this.a(blockposition, iblockdata) : true;
        }
    }

    private boolean a(BlockPosition blockposition, IBlockData iblockdata) {
        if (((Boolean) iblockdata.get(BlockTrapdoor.OPEN)).booleanValue()) {
            IBlockData iblockdata1 = this.world.getType(blockposition.down());

            if (iblockdata1.getBlock() == Blocks.LADDER && iblockdata1.get(BlockLadder.FACING) == iblockdata.get(BlockTrapdoor.FACING)) {
                return true;
            }
        }

        return false;
    }

    public boolean isAlive() {
        return !this.dead && this.getHealth() > 0.0F;
    }

    public void e(float f, float f1) {
        super.e(f, f1);
        MobEffect mobeffect = this.getEffect(MobEffects.JUMP);
        float f2 = mobeffect == null ? 0.0F : (float) (mobeffect.getAmplifier() + 1);
        int i = MathHelper.f((f - 3.0F - f2) * f1);

        if (i > 0) {
            // CraftBukkit start
            if (!this.damageEntity(DamageSource.FALL, (float) i)) {
                return;
            }
            // CraftBukkit end
            this.a(this.e(i), 1.0F, 1.0F);
            // this.damageEntity(DamageSource.FALL, (float) i); // CraftBukkit - moved up
            int j = MathHelper.floor(this.locX);
            int k = MathHelper.floor(this.locY - 0.20000000298023224D);
            int l = MathHelper.floor(this.locZ);
            IBlockData iblockdata = this.world.getType(new BlockPosition(j, k, l));

            if (iblockdata.getMaterial() != Material.AIR) {
                SoundEffectType soundeffecttype = iblockdata.getBlock().w();

                this.a(soundeffecttype.g(), soundeffecttype.a() * 0.5F, soundeffecttype.b() * 0.75F);
            }
        }

    }

    public int getArmorStrength() {
        AttributeInstance attributeinstance = this.getAttributeInstance(GenericAttributes.g);

        return MathHelper.floor(attributeinstance.getValue());
    }

    protected void damageArmor(float f) {}

    protected void k(float f) {}

    protected float applyArmorModifier(DamageSource damagesource, float f) {
        if (!damagesource.ignoresArmor()) {
            // this.damageArmor(f); // CraftBukkit - Moved into damageEntity0(DamageSource, float)
            f = CombatMath.a(f, (float) this.getArmorStrength());
        }

        return f;
    }

    protected float applyMagicModifier(DamageSource damagesource, float f) {
        if (damagesource.isStarvation()) {
            return f;
        } else {
            int i;

            // CraftBukkit - Moved to damageEntity0(DamageSource, float)
            if (false && this.hasEffect(MobEffects.RESISTANCE) && damagesource != DamageSource.OUT_OF_WORLD) {
                i = (this.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f1 = f * (float) j;

                f = f1 / 25.0F;
            }

            if (f <= 0.0F) {
                return 0.0F;
            } else {
                i = EnchantmentManager.a(this.getArmorItems(), damagesource);
                if (i > 0) {
                    f = CombatMath.a(f, (float) i);
                }

                return f;
            }
        }
    }

    // CraftBukkit start
    protected boolean damageEntity0(final DamageSource damagesource, float f) { // void -> boolean, add final
       if (!this.isInvulnerable(damagesource)) {
            final boolean human = this instanceof EntityHuman;
            float originalDamage = f;
            Function<Double, Double> hardHat = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    if ((damagesource == DamageSource.ANVIL || damagesource == DamageSource.FALLING_BLOCK) && EntityLiving.this.getEquipment(EnumItemSlot.HEAD) != null) {
                        return -(f - (f * 0.75F));

                    }
                    return -0.0;
                }
            };
            float hardHatModifier = hardHat.apply((double) f).floatValue();
            f += hardHatModifier;

            Function<Double, Double> blocking = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    return -((EntityLiving.this.d(damagesource)) ? ((damagesource.a()) ? f : (f - (f * 0.33F))) : 0.0); // PAIL: rename
                }
            };
            float blockingModifier = blocking.apply((double) f).floatValue();
            f += blockingModifier;

            Function<Double, Double> armor = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    return -(f - EntityLiving.this.applyArmorModifier(damagesource, f.floatValue()));
                }
            };
            float armorModifier = armor.apply((double) f).floatValue();
            f += armorModifier;

            Function<Double, Double> resistance = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    if (!damagesource.isStarvation() && EntityLiving.this.hasEffect(MobEffects.RESISTANCE) && damagesource != DamageSource.OUT_OF_WORLD) {
                        int i = (EntityLiving.this.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                        int j = 25 - i;
                        float f1 = f.floatValue() * (float) j;
                        return -(f - (f1 / 25.0F));
                    }
                    return -0.0;
                }
            };
            float resistanceModifier = resistance.apply((double) f).floatValue();
            f += resistanceModifier;

            Function<Double, Double> magic = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    return -(f - EntityLiving.this.applyMagicModifier(damagesource, f.floatValue()));
                }
            };
            float magicModifier = magic.apply((double) f).floatValue();
            f += magicModifier;

            Function<Double, Double> absorption = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    return -(Math.max(f - Math.max(f - EntityLiving.this.getAbsorptionHearts(), 0.0F), 0.0F));
                }
            };
            float absorptionModifier = absorption.apply((double) f).floatValue();

            EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent(this, damagesource, originalDamage, hardHatModifier, blockingModifier, armorModifier, resistanceModifier, magicModifier, absorptionModifier, hardHat, blocking, armor, resistance, magic, absorption);
            if (event.isCancelled()) {
                return false;
            }

            f = (float) event.getFinalDamage();

            // Apply damage to helmet
            if ((damagesource == DamageSource.ANVIL || damagesource == DamageSource.FALLING_BLOCK) && this.getEquipment(EnumItemSlot.HEAD) != null) {
                this.getEquipment(EnumItemSlot.HEAD).damage((int) (event.getDamage() * 4.0F + this.random.nextFloat() * event.getDamage() * 2.0F), this);
            }

            // Apply damage to armor
            if (!damagesource.ignoresArmor()) {
                float armorDamage = (float) (event.getDamage() + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.HARD_HAT));
                this.damageArmor(armorDamage);
            }

            // Apply blocking code
            if (event.getDamage(DamageModifier.BLOCKING) < 0) {
                this.k((float) event.getDamage()); // PAIL: rename
                if (damagesource.i() instanceof EntityLiving) {// PAIL: rename
                    ((EntityLiving) damagesource.i()).a(EntityLiving.this, 0.5F, EntityLiving.this.locX - damagesource.i().locX, EntityLiving.this.locZ - damagesource.i().locZ);
                }
                if (f <= 0) {
                    return false;
                }
            }

            absorptionModifier = (float) -event.getDamage(DamageModifier.ABSORPTION);
            this.setAbsorptionHearts(Math.max(this.getAbsorptionHearts() - absorptionModifier, 0.0F));
            if (f > 0) {
                if (human) {
                    // PAIL: Be sure to drag all this code from the EntityHuman subclass each update.
                    ((EntityHuman) this).applyExhaustion(damagesource.getExhaustionCost());
                    if (f < 3.4028235E37F) {
                        ((EntityHuman) this).a(StatisticList.z, Math.round(f * 10.0F));
                    }
                }
                // CraftBukkit end
                float f2 = this.getHealth();

                this.setHealth(f2 - f);
                this.getCombatTracker().trackDamage(damagesource, f2, f);
                // CraftBukkit start
                if (!human) {
                    this.setAbsorptionHearts(this.getAbsorptionHearts() - f);
                }
                // CraftBukkit end
            }
            return true;
        }
        return false; // CraftBukkit
    }

    public CombatTracker getCombatTracker() {
        return this.combatTracker;
    }

    public EntityLiving bW() {
        return (EntityLiving) (this.combatTracker.c() != null ? this.combatTracker.c() : (this.killer != null ? this.killer : (this.lastDamager != null ? this.lastDamager : null)));
    }

    public final float getMaxHealth() {
        return (float) this.getAttributeInstance(GenericAttributes.maxHealth).getValue();
    }

    public final int getStuckArrows() { return this.bY(); } // Paper // OBFHELPER
    public final int bY() {
        return ((Integer) this.datawatcher.get(EntityLiving.h)).intValue();
    }

    public final void setStuckArrows(int arrows) { this.k(arrows); } // Paper - deobfuscation helper
    public final void k(int i) {
        this.datawatcher.set(EntityLiving.h, Integer.valueOf(i));
    }

    private int o() {
        return this.hasEffect(MobEffects.FASTER_DIG) ? 6 - (1 + this.getEffect(MobEffects.FASTER_DIG).getAmplifier()) : (this.hasEffect(MobEffects.SLOWER_DIG) ? 6 + (1 + this.getEffect(MobEffects.SLOWER_DIG).getAmplifier()) * 2 : 6);
    }

    public void a(EnumHand enumhand) {
        if (!this.au || this.aw >= this.o() / 2 || this.aw < 0) {
            this.aw = -1;
            this.au = true;
            this.av = enumhand;
            if (this.world instanceof WorldServer) {
                ((WorldServer) this.world).getTracker().a((Entity) this, (Packet) (new PacketPlayOutAnimation(this, enumhand == EnumHand.MAIN_HAND ? 0 : 3)));
            }
        }

    }

    protected void Y() {
        this.damageEntity(DamageSource.OUT_OF_WORLD, 4.0F);
    }

    protected void bZ() {
        int i = this.o();

        if (this.au) {
            ++this.aw;
            if (this.aw >= i) {
                this.aw = 0;
                this.au = false;
            }
        } else {
            this.aw = 0;
        }

        this.aD = (float) this.aw / (float) i;
    }

    public AttributeInstance getAttributeInstance(IAttribute iattribute) {
        return this.getAttributeMap().a(iattribute);
    }

    public AttributeMapBase getAttributeMap() {
        if (this.bq == null) {
            this.bq = new AttributeMapServer();
            this.craftAttributes = new CraftAttributeMap(bq); // CraftBukkit
        }

        return this.bq;
    }

    public EnumMonsterType getMonsterType() {
        return EnumMonsterType.UNDEFINED;
    }

    @Nullable
    public ItemStack getItemInMainHand() {
        return this.getEquipment(EnumItemSlot.MAINHAND);
    }

    @Nullable
    public ItemStack getItemInOffHand() {
        return this.getEquipment(EnumItemSlot.OFFHAND);
    }

    @Nullable
    public ItemStack b(EnumHand enumhand) {
        if (enumhand == EnumHand.MAIN_HAND) {
            return this.getEquipment(EnumItemSlot.MAINHAND);
        } else if (enumhand == EnumHand.OFF_HAND) {
            return this.getEquipment(EnumItemSlot.OFFHAND);
        } else {
            throw new IllegalArgumentException("Invalid hand " + enumhand);
        }
    }

    public void a(EnumHand enumhand, @Nullable ItemStack itemstack) {
        if (enumhand == EnumHand.MAIN_HAND) {
            this.setSlot(EnumItemSlot.MAINHAND, itemstack);
        } else {
            if (enumhand != EnumHand.OFF_HAND) {
                throw new IllegalArgumentException("Invalid hand " + enumhand);
            }

            this.setSlot(EnumItemSlot.OFFHAND, itemstack);
        }

    }

    public abstract Iterable<ItemStack> getArmorItems();

    @Nullable
    public abstract ItemStack getEquipment(EnumItemSlot enumitemslot);

    public abstract void setSlot(EnumItemSlot enumitemslot, @Nullable ItemStack itemstack);

    public void setSprinting(boolean flag) {
        super.setSprinting(flag);
        AttributeInstance attributeinstance = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);

        if (attributeinstance.a(EntityLiving.a) != null) {
            attributeinstance.c(EntityLiving.b);
        }

        if (flag) {
            attributeinstance.b(EntityLiving.b);
        }

    }

    protected float ce() {
        return 1.0F;
    }

    protected float cf() {
        return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.5F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
    }

    protected boolean cg() {
        return this.getHealth() <= 0.0F;
    }

    public void A(Entity entity) {
        double d0;

        if (!(entity instanceof EntityBoat) && !(entity instanceof EntityHorse)) {
            double d1 = entity.locX;
            double d2 = entity.getBoundingBox().b + (double) entity.length;

            d0 = entity.locZ;
            EnumDirection enumdirection = entity.bk();
            EnumDirection enumdirection1 = enumdirection.e();
            int[][] aint = new int[][] { { 0, 1}, { 0, -1}, { -1, 1}, { -1, -1}, { 1, 1}, { 1, -1}, { -1, 0}, { 1, 0}, { 0, 1}};
            double d3 = Math.floor(this.locX) + 0.5D;
            double d4 = Math.floor(this.locZ) + 0.5D;
            double d5 = this.getBoundingBox().d - this.getBoundingBox().a;
            double d6 = this.getBoundingBox().f - this.getBoundingBox().c;
            AxisAlignedBB axisalignedbb = new AxisAlignedBB(d3 - d5 / 2.0D, this.getBoundingBox().b, d4 - d6 / 2.0D, d3 + d5 / 2.0D, this.getBoundingBox().e, d4 + d6 / 2.0D);
            int[][] aint1 = aint;
            int i = aint.length;

            for (int j = 0; j < i; ++j) {
                int[] aint2 = aint1[j];
                double d7 = (double) (enumdirection.getAdjacentX() * aint2[0] + enumdirection1.getAdjacentX() * aint2[1]);
                double d8 = (double) (enumdirection.getAdjacentZ() * aint2[0] + enumdirection1.getAdjacentZ() * aint2[1]);
                double d9 = d3 + d7;
                double d10 = d4 + d8;
                AxisAlignedBB axisalignedbb1 = axisalignedbb.c(d7, 1.0D, d8);

                if (!this.world.b(axisalignedbb1)) {
                    if (this.world.getType(new BlockPosition(d9, this.locY, d10)).q()) {
                        this.enderTeleportTo(d9, this.locY + 1.0D, d10);
                        return;
                    }

                    BlockPosition blockposition = new BlockPosition(d9, this.locY - 1.0D, d10);

                    if (this.world.getType(blockposition).q() || this.world.getType(blockposition).getMaterial() == Material.WATER) {
                        d1 = d9;
                        d2 = this.locY + 1.0D;
                        d0 = d10;
                    }
                } else if (!this.world.b(axisalignedbb1.c(0.0D, 1.0D, 0.0D)) && this.world.getType(new BlockPosition(d9, this.locY + 1.0D, d10)).q()) {
                    d1 = d9;
                    d2 = this.locY + 2.0D;
                    d0 = d10;
                }
            }

            this.enderTeleportTo(d1, d2, d0);
        } else {
            double d11 = (double) (this.width / 2.0F + entity.width / 2.0F) + 0.4D;
            float f;

            if (entity instanceof EntityBoat) {
                f = 0.0F;
            } else {
                f = 1.5707964F * (float) (this.getMainHand() == EnumMainHand.RIGHT ? -1 : 1);
            }

            float f1 = -MathHelper.sin(-this.yaw * 0.017453292F - 3.1415927F + f);
            float f2 = -MathHelper.cos(-this.yaw * 0.017453292F - 3.1415927F + f);

            d0 = Math.abs(f1) > Math.abs(f2) ? d11 / (double) Math.abs(f1) : d11 / (double) Math.abs(f2);
            double d12 = this.locX + (double) f1 * d0;
            double d13 = this.locZ + (double) f2 * d0;

            this.setPosition(d12, entity.locY + (double) entity.length + 0.001D, d13);
            if (this.world.b(this.getBoundingBox())) {
                this.setPosition(d12, entity.locY + (double) entity.length + 1.001D, d13);
                if (this.world.b(this.getBoundingBox())) {
                    this.setPosition(entity.locX, entity.locY + (double) this.length + 0.001D, entity.locZ);
                }
            }
        }
    }

    protected float ch() {
        return 0.42F;
    }

    protected void ci() {
        this.motY = (double) this.ch();
        if (this.hasEffect(MobEffects.JUMP)) {
            this.motY += (double) ((float) (this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
        }

        if (this.isSprinting()) {
            float f = this.yaw * 0.017453292F;

            this.motX -= (double) (MathHelper.sin(f) * 0.2F);
            this.motZ += (double) (MathHelper.cos(f) * 0.2F);
        }

        this.impulse = true;
    }

    protected void cj() {
        this.motY += 0.03999999910593033D;
    }

    protected void ck() {
        this.motY += 0.03999999910593033D;
    }

    public void g(float f, float f1) {
        double d0;
        double d1;
        float f2;

        if (this.cp() || this.by()) {
            float f3;
            float f4;

            if (this.isInWater() && (!(this instanceof EntityHuman) || !((EntityHuman) this).abilities.isFlying)) {
                d1 = this.locY;
                f4 = 0.8F;
                f3 = 0.02F;
                f2 = (float) EnchantmentManager.d(this);
                if (f2 > 3.0F) {
                    f2 = 3.0F;
                }

                if (!this.onGround) {
                    f2 *= 0.5F;
                }

                if (f2 > 0.0F) {
                    f4 += (0.54600006F - f4) * f2 / 3.0F;
                    f3 += (this.cl() - f3) * f2 / 3.0F;
                }

                this.a(f, f1, f3);
                this.move(this.motX, this.motY, this.motZ);
                this.motX *= (double) f4;
                this.motY *= 0.800000011920929D;
                this.motZ *= (double) f4;
                this.motY -= 0.02D;
                if (this.positionChanged && this.c(this.motX, this.motY + 0.6000000238418579D - this.locY + d1, this.motZ)) {
                    this.motY = 0.30000001192092896D;
                }
            } else if (this.an() && (!(this instanceof EntityHuman) || !((EntityHuman) this).abilities.isFlying)) {
                d1 = this.locY;
                this.a(f, f1, 0.02F);
                this.move(this.motX, this.motY, this.motZ);
                this.motX *= 0.5D;
                this.motY *= 0.5D;
                this.motZ *= 0.5D;
                this.motY -= 0.02D;
                if (this.positionChanged && this.c(this.motX, this.motY + 0.6000000238418579D - this.locY + d1, this.motZ)) {
                    this.motY = 0.30000001192092896D;
                }
            } else if (this.cC()) {
                if (this.motY > -0.5D) {
                    this.fallDistance = 1.0F;
                }

                Vec3D vec3d = this.aB();
                float f5 = this.pitch * 0.017453292F;

                d0 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
                double d2 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
                double d3 = vec3d.b();
                float f6 = MathHelper.cos(f5);

                f6 = (float) ((double) f6 * (double) f6 * Math.min(1.0D, d3 / 0.4D));
                this.motY += -0.08D + (double) f6 * 0.06D;
                double d4;

                if (this.motY < 0.0D && d0 > 0.0D) {
                    d4 = this.motY * -0.1D * (double) f6;
                    this.motY += d4;
                    this.motX += vec3d.x * d4 / d0;
                    this.motZ += vec3d.z * d4 / d0;
                }

                if (f5 < 0.0F) {
                    d4 = d2 * (double) (-MathHelper.sin(f5)) * 0.04D;
                    this.motY += d4 * 3.2D;
                    this.motX -= vec3d.x * d4 / d0;
                    this.motZ -= vec3d.z * d4 / d0;
                }

                if (d0 > 0.0D) {
                    this.motX += (vec3d.x / d0 * d2 - this.motX) * 0.1D;
                    this.motZ += (vec3d.z / d0 * d2 - this.motZ) * 0.1D;
                }

                this.motX *= 0.9900000095367432D;
                this.motY *= 0.9800000190734863D;
                this.motZ *= 0.9900000095367432D;
                this.move(this.motX, this.motY, this.motZ);
                if (this.positionChanged && !this.world.isClientSide) {
                    d4 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
                    double d5 = d2 - d4;
                    float f7 = (float) (d5 * 10.0D - 3.0D);

                    if (f7 > 0.0F) {
                        this.a(this.e((int) f7), 1.0F, 1.0F);
                        this.damageEntity(DamageSource.FLY_INTO_WALL, f7);
                    }
                }

                if (this.onGround && !this.world.isClientSide) {
                    if (getFlag(7) && !CraftEventFactory.callToggleGlideEvent(this, false).isCancelled()) // CraftBukkit
                    this.setFlag(7, false);
                }
            } else {
                float f8 = 0.91F;
                BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.d(this.locX, this.getBoundingBox().b - 1.0D, this.locZ);

                if (this.onGround) {
                    f8 = this.world.getType(blockposition_pooledblockposition).getBlock().frictionFactor * 0.91F;
                }

                f4 = 0.16277136F / (f8 * f8 * f8);
                if (this.onGround) {
                    f3 = this.cl() * f4;
                } else {
                    f3 = this.aR;
                }

                this.a(f, f1, f3);
                f8 = 0.91F;
                if (this.onGround) {
                    f8 = this.world.getType(blockposition_pooledblockposition.e(this.locX, this.getBoundingBox().b - 1.0D, this.locZ)).getBlock().frictionFactor * 0.91F;
                }

                if (this.n_()) {
                    f2 = 0.15F;
                    this.motX = MathHelper.a(this.motX, (double) (-f2), (double) f2);
                    this.motZ = MathHelper.a(this.motZ, (double) (-f2), (double) f2);
                    this.fallDistance = 0.0F;
                    if (this.motY < -0.15D) {
                        this.motY = -0.15D;
                    }

                    boolean flag = this.isSneaking() && this instanceof EntityHuman;

                    if (flag && this.motY < 0.0D) {
                        this.motY = 0.0D;
                    }
                }

                this.move(this.motX, this.motY, this.motZ);
                if (this.positionChanged && this.n_()) {
                    this.motY = 0.2D;
                }

                if (this.hasEffect(MobEffects.LEVITATION)) {
                    this.motY += (0.05D * (double) (this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - this.motY) * 0.2D;
                } else {
                    blockposition_pooledblockposition.e(this.locX, 0.0D, this.locZ);
                    if (this.world.isClientSide && (!this.world.isLoaded(blockposition_pooledblockposition) || !this.world.getChunkAtWorldCoords(blockposition_pooledblockposition).p())) {
                        if (this.locY > 0.0D) {
                            this.motY = -0.1D;
                        } else {
                            this.motY = 0.0D;
                        }
                    } else {
                        this.motY -= 0.08D;
                    }
                }

                this.motY *= 0.9800000190734863D;
                this.motX *= (double) f8;
                this.motZ *= (double) f8;
                blockposition_pooledblockposition.t();
            }
        }

        this.aF = this.aG;
        d1 = this.locX - this.lastX;
        d0 = this.locZ - this.lastZ;
        f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;
        if (f2 > 1.0F) {
            f2 = 1.0F;
        }

        this.aG += (f2 - this.aG) * 0.4F;
        this.aH += this.aG;
    }

    public float cl() {
        return this.bA;
    }

    public void l(float f) {
        this.bA = f;
    }

    public boolean B(Entity entity) {
        this.z(entity);
        return false;
    }

    public boolean isSleeping() {
        return false;
    }

    public void m() {
        super.m();
        this.cv();
        if (!this.world.isClientSide) {
            int i = this.bY();

            if (i > 0) {
                if (this.ax <= 0) {
                    this.ax = 20 * (30 - i);
                }

                --this.ax;
                if (this.ax <= 0) {
                    this.k(i - 1);
                }
            }

            EnumItemSlot[] aenumitemslot = EnumItemSlot.values();
            int j = aenumitemslot.length;

            for (int k = 0; k < j; ++k) {
                EnumItemSlot enumitemslot = aenumitemslot[k];
                ItemStack itemstack;

                switch (EntityLiving.SyntheticClass_1.a[enumitemslot.a().ordinal()]) {
                case 1:
                    itemstack = this.bt[enumitemslot.b()];
                    break;

                case 2:
                    itemstack = this.bu[enumitemslot.b()];
                    break;

                default:
                    continue;
                }

                ItemStack itemstack1 = this.getEquipment(enumitemslot);

                if (!ItemStack.matches(itemstack1, itemstack)) {
                    ((WorldServer) this.world).getTracker().a((Entity) this, (Packet) (new PacketPlayOutEntityEquipment(this.getId(), enumitemslot, itemstack1)));
                    if (itemstack != null) {
                        this.getAttributeMap().a(itemstack.a(enumitemslot));
                    }

                    if (itemstack1 != null) {
                        this.getAttributeMap().b(itemstack1.a(enumitemslot));
                    }

                    switch (EntityLiving.SyntheticClass_1.a[enumitemslot.a().ordinal()]) {
                    case 1:
                        this.bt[enumitemslot.b()] = itemstack1 == null ? null : itemstack1.cloneItemStack();
                        break;

                    case 2:
                        this.bu[enumitemslot.b()] = itemstack1 == null ? null : itemstack1.cloneItemStack();
                    }
                }
            }

            if (this.ticksLived % 20 == 0) {
                this.getCombatTracker().g();
            }

            if (!this.glowing) {
                boolean flag = this.hasEffect(MobEffects.GLOWING);

                if (this.getFlag(6) != flag) {
                    this.setFlag(6, flag);
                }
            }
        }

        this.n();
        double d0 = this.locX - this.lastX;
        double d1 = this.locZ - this.lastZ;
        float f = (float) (d0 * d0 + d1 * d1);
        float f1 = this.aN;
        float f2 = 0.0F;

        this.aW = this.aX;
        float f3 = 0.0F;

        if (f > 0.0025000002F) {
            f3 = 1.0F;
            f2 = (float) Math.sqrt((double) f) * 3.0F;
            f1 = (float) MathHelper.b(d1, d0) * 57.295776F - 90.0F;
        }

        if (this.aD > 0.0F) {
            f1 = this.yaw;
        }

        if (!this.onGround) {
            f3 = 0.0F;
        }

        this.aX += (f3 - this.aX) * 0.3F;
        this.world.methodProfiler.a("headTurn");
        f2 = this.h(f1, f2);
        this.world.methodProfiler.b();
        this.world.methodProfiler.a("rangeChecks");

        while (this.yaw - this.lastYaw < -180.0F) {
            this.lastYaw -= 360.0F;
        }

        while (this.yaw - this.lastYaw >= 180.0F) {
            this.lastYaw += 360.0F;
        }

        while (this.aN - this.aO < -180.0F) {
            this.aO -= 360.0F;
        }

        while (this.aN - this.aO >= 180.0F) {
            this.aO += 360.0F;
        }

        while (this.pitch - this.lastPitch < -180.0F) {
            this.lastPitch -= 360.0F;
        }

        while (this.pitch - this.lastPitch >= 180.0F) {
            this.lastPitch += 360.0F;
        }

        while (this.aP - this.aQ < -180.0F) {
            this.aQ -= 360.0F;
        }

        while (this.aP - this.aQ >= 180.0F) {
            this.aQ += 360.0F;
        }

        this.world.methodProfiler.b();
        this.aY += f2;
        if (this.cC()) {
            ++this.bp;
        } else {
            this.bp = 0;
        }
    }

    protected float h(float f, float f1) {
        float f2 = MathHelper.g(f - this.aN);

        this.aN += f2 * 0.3F;
        float f3 = MathHelper.g(this.yaw - this.aN);
        boolean flag = f3 < -90.0F || f3 >= 90.0F;

        if (f3 < -75.0F) {
            f3 = -75.0F;
        }

        if (f3 >= 75.0F) {
            f3 = 75.0F;
        }

        this.aN = this.yaw - f3;
        if (f3 * f3 > 2500.0F) {
            this.aN += f3 * 0.2F;
        }

        if (flag) {
            f1 *= -1.0F;
        }

        return f1;
    }

    public void n() {
        if (this.bB > 0) {
            --this.bB;
        }

        if (this.bh > 0 && !this.by()) {
            double d0 = this.locX + (this.bi - this.locX) / (double) this.bh;
            double d1 = this.locY + (this.bj - this.locY) / (double) this.bh;
            double d2 = this.locZ + (this.bk - this.locZ) / (double) this.bh;
            double d3 = MathHelper.g(this.bl - (double) this.yaw);

            this.yaw = (float) ((double) this.yaw + d3 / (double) this.bh);
            this.pitch = (float) ((double) this.pitch + (this.bm - (double) this.pitch) / (double) this.bh);
            --this.bh;
            this.setPosition(d0, d1, d2);
            this.setYawPitch(this.yaw, this.pitch);
        } else if (!this.cp()) {
            this.motX *= 0.98D;
            this.motY *= 0.98D;
            this.motZ *= 0.98D;
        }

        if (Math.abs(this.motX) < 0.003D) {
            this.motX = 0.0D;
        }

        if (Math.abs(this.motY) < 0.003D) {
            this.motY = 0.0D;
        }

        if (Math.abs(this.motZ) < 0.003D) {
            this.motZ = 0.0D;
        }

        this.world.methodProfiler.a("ai");
        if (this.cg()) {
            this.bd = false;
            this.be = 0.0F;
            this.bf = 0.0F;
            this.bg = 0.0F;
        } else if (this.cp()) {
            this.world.methodProfiler.a("newAi");
            this.doTick();
            this.world.methodProfiler.b();
        }

        this.world.methodProfiler.b();
        this.world.methodProfiler.a("jump");
        if (this.bd) {
            if (this.isInWater()) {
                this.cj();
            } else if (this.an()) {
                this.ck();
            } else if (this.onGround && this.bB == 0) {
                this.ci();
                this.bB = 10;
            }
        } else {
            this.bB = 0;
        }

        this.world.methodProfiler.b();
        this.world.methodProfiler.a("travel");
        this.be *= 0.98F;
        this.bf *= 0.98F;
        this.bg *= 0.9F;
        this.r();
        this.g(this.be, this.bf);
        this.world.methodProfiler.b();
        this.world.methodProfiler.a("push");
        co co_task = new co(this);
        co_task.fork();
        this.world.methodProfiler.b();
    }

    private void r() {
        boolean flag = this.getFlag(7);

        if (flag && !this.onGround && !this.isPassenger()) {
            ItemStack itemstack = this.getEquipment(EnumItemSlot.CHEST);

            if (itemstack != null && itemstack.getItem() == Items.cR && ItemElytra.d(itemstack)) {
                flag = true;
                if (!this.world.isClientSide && (this.bp + 1) % 20 == 0) {
                    itemstack.damage(1, this);
                }
            } else {
                flag = false;
            }
        } else {
            flag = false;
        }

        if (!this.world.isClientSide) {
            if (flag != this.getFlag(7) && !CraftEventFactory.callToggleGlideEvent(this, flag).isCancelled()) // CraftBukkit
            this.setFlag(7, flag);
        }

    }

    protected void doTick() {}

    protected void co() {
        List list = this.world.getEntities(this, this.getBoundingBox(), IEntitySelector.a(this));

        if (!list.isEmpty()) {
                numCollisions = Math.max(0, numCollisions - world.spigotConfig.maxCollisionsPerEntity); // Spigot
                Iterator it = list.iterator();
            while (it.hasNext()) {
                Entity entity = (Entity) it.next();
                // TODO better check now?
                // CraftBukkit start - Only handle mob (non-player) collisions every other tick
                if (entity instanceof EntityLiving && !(this instanceof EntityPlayer) && this.ticksLived % 2 == 0) {
                    continue;
                }
                // CraftBukkit end

                entity.numCollisions++; // Spigot
                numCollisions++; // Spigot
                this.C(entity);
            }
        }

    }

    protected void C(Entity entity) {
        entity.collide(this);
    }

    public void stopRiding() {
        Entity entity = this.bz();

        super.stopRiding();
        if (entity != null && entity != this.bz() && !this.world.isClientSide) {
            this.A(entity);
        }

    }

    public void aw() {
        super.aw();
        this.aW = this.aX;
        this.aX = 0.0F;
        this.fallDistance = 0.0F;
    }

    public void k(boolean flag) {
        this.bd = flag;
    }

    public void receive(Entity entity, int i) {
        if (!entity.dead && !this.world.isClientSide) {
            EntityTracker entitytracker = ((WorldServer) this.world).getTracker();

            if (entity instanceof EntityItem) {
                entitytracker.a(entity, (Packet) (new PacketPlayOutCollect(entity.getId(), this.getId())));
            }

            if (entity instanceof EntityArrow) {
                entitytracker.a(entity, (Packet) (new PacketPlayOutCollect(entity.getId(), this.getId())));
            }

            if (entity instanceof EntityExperienceOrb) {
                entitytracker.a(entity, (Packet) (new PacketPlayOutCollect(entity.getId(), this.getId())));
            }
        }

    }

    public boolean hasLineOfSight(Entity entity) {
        return this.world.rayTrace(new Vec3D(this.locX, this.locY + (double) this.getHeadHeight(), this.locZ), new Vec3D(entity.locX, entity.locY + (double) entity.getHeadHeight(), entity.locZ), false, true, false) == null;
    }

    public Vec3D aB() {
        return this.f(1.0F);
    }

    public Vec3D f(float f) {
        if (f == 1.0F) {
            return this.f(this.pitch, this.aP);
        } else {
            float f1 = this.lastPitch + (this.pitch - this.lastPitch) * f;
            float f2 = this.aQ + (this.aP - this.aQ) * f;

            return this.f(f1, f2);
        }
    }

    public boolean cp() {
        return !this.world.isClientSide;
    }

    public boolean isInteractable() {
        return !this.dead && this.collides; // CraftBukkit
    }

    public boolean isCollidable() {
        return !this.dead && this.collides; // CraftBukkit
    }

    protected void ao() {
        this.velocityChanged = this.random.nextDouble() >= this.getAttributeInstance(GenericAttributes.c).getValue();
    }

    public float getHeadRotation() {
        return this.aP;
    }

    public void h(float f) {
        this.aP = f;
    }

    public void i(float f) {
        this.aN = f;
    }

    public float getAbsorptionHearts() {
        return this.bC;
    }

    public void setAbsorptionHearts(float f) {
        if (f < 0.0F || Float.isNaN(f)) { // Paper
            f = 0.0F;
        }

        this.bC = f;
    }

    public void enterCombat() {}

    public void exitCombat() {}

    protected void cr() {
        this.updateEffects = true;
    }

    public abstract EnumMainHand getMainHand();

    public boolean ct() {
        return (((Byte) this.datawatcher.get(EntityLiving.at)).byteValue() & 1) > 0;
    }

    public EnumHand cu() {
        return (((Byte) this.datawatcher.get(EntityLiving.at)).byteValue() & 2) > 0 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
    }

    protected void cv() {
        if (this.ct()) {
            ItemStack itemstack = this.b(this.cu());

            if (itemstack == this.bn) {
                if (this.cx() <= 25 && this.cx() % 4 == 0) {
                    this.a(this.bn, 5);
                }

                if (--this.bo == 0 && !this.world.isClientSide) {
                    this.v();
                }
            } else {
                this.cA();
            }
        }

    }

    public void c(EnumHand enumhand) {
        ItemStack itemstack = this.b(enumhand);

        if (itemstack != null && !this.ct()) {
            this.bn = itemstack;
            this.bo = itemstack.l();
            if (!this.world.isClientSide) {
                int i = 1;

                if (enumhand == EnumHand.OFF_HAND) {
                    i |= 2;
                }

                this.datawatcher.set(EntityLiving.at, Byte.valueOf((byte) i));
            }

        }
    }

    public void a(DataWatcherObject<?> datawatcherobject) {
        super.a(datawatcherobject);
        if (EntityLiving.at.equals(datawatcherobject) && this.world.isClientSide) {
            if (this.ct() && this.bn == null) {
                this.bn = this.b(this.cu());
                if (this.bn != null) {
                    this.bo = this.bn.l();
                }
            } else if (!this.ct() && this.bn != null) {
                this.bn = null;
                this.bo = 0;
            }
        }

    }

    protected void a(@Nullable ItemStack itemstack, int i) {
        if (itemstack != null && this.ct()) {
            if (itemstack.m() == EnumAnimation.DRINK) {
                this.a(SoundEffects.bC, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
            }

            if (itemstack.m() == EnumAnimation.EAT) {
                for (int j = 0; j < i; ++j) {
                    Vec3D vec3d = new Vec3D(((double) this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);

                    vec3d = vec3d.a(-this.pitch * 0.017453292F);
                    vec3d = vec3d.b(-this.yaw * 0.017453292F);
                    double d0 = (double) (-this.random.nextFloat()) * 0.6D - 0.3D;
                    Vec3D vec3d1 = new Vec3D(((double) this.random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);

                    vec3d1 = vec3d1.a(-this.pitch * 0.017453292F);
                    vec3d1 = vec3d1.b(-this.yaw * 0.017453292F);
                    vec3d1 = vec3d1.add(this.locX, this.locY + (double) this.getHeadHeight(), this.locZ);
                    if (itemstack.usesData()) {
                        this.world.addParticle(EnumParticle.ITEM_CRACK, vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z, new int[] { Item.getId(itemstack.getItem()), itemstack.getData()});
                    } else {
                        this.world.addParticle(EnumParticle.ITEM_CRACK, vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z, new int[] { Item.getId(itemstack.getItem())});
                    }
                }

                this.a(SoundEffects.bD, 0.5F + 0.5F * (float) this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }

        }
    }

    protected void v() {
        if (this.bn != null && this.ct()) {
            this.a(this.bn, 16);
            // CraftBukkit start - fire PlayerItemConsumeEvent
            org.bukkit.inventory.ItemStack craftItem = CraftItemStack.asBukkitCopy(this.bn); // PAIL: rename
            PlayerItemConsumeEvent event = new PlayerItemConsumeEvent((Player) this.getBukkitEntity(), craftItem);
            world.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                // Update client
                if (this instanceof EntityPlayer) {
                    ((EntityPlayer) this).getBukkitEntity().updateInventory();
                    ((EntityPlayer) this).getBukkitEntity().updateScaledHealth();
                }
                return;
            }

            ItemStack itemstack = (craftItem.equals(event.getItem())) ? this.bn.a(this.world, this) : CraftItemStack.asNMSCopy(event.getItem()).a(world, this);
            // CraftBukkit end

            // Paper start - save the default replacement item and change it if necessary
            final ItemStack defaultReplacement = itemstack;
            if (event.getReplacement() != null) {
                itemstack = CraftItemStack.asNMSCopy(event.getReplacement());
            }
            // Paper end

            if (itemstack != null && itemstack.count == 0) {
                itemstack = null;
            }

            this.a(this.cu(), itemstack);
            this.cA();

            // Paper start - if the replacement is anything but the default, update the client inventory
            if (this instanceof EntityPlayer && !com.google.common.base.Objects.equal(defaultReplacement, itemstack)) {
                ((EntityPlayer) this).getBukkitEntity().updateInventory();
            }
            // Paper end
        }

    }

    @Nullable
    public ItemStack cw() {
        return this.bn;
    }

    public int cx() {
        return this.bo;
    }

    public int cy() {
        return this.ct() ? this.bn.l() - this.cx() : 0;
    }

    public void clearActiveItem() {
        if (this.bn != null) {
            this.bn.a(this.world, this, this.cx());
        }

        this.cA();
    }

    public void cA() {
        if (!this.world.isClientSide) {
            this.datawatcher.set(EntityLiving.at, Byte.valueOf((byte) 0));
        }

        this.bn = null;
        this.bo = 0;
    }

    public boolean isBlocking() {
        if (this.ct() && this.bn != null) {
            Item item = this.bn.getItem();

            return item.f(this.bn) != EnumAnimation.BLOCK ? false : item.e(this.bn) - this.bo >= 5;
        } else {
            return false;
        }
    }

    public boolean cC() {
        return this.getFlag(7);
    }

    public boolean k(double d0, double d1, double d2) {
        double d3 = this.locX;
        double d4 = this.locY;
        double d5 = this.locZ;

        this.locX = d0;
        this.locY = d1;
        this.locZ = d2;
        boolean flag = false;
        BlockPosition blockposition = new BlockPosition(this);
        World world = this.world;
        Random random = this.getRandom();

        if (world.isLoaded(blockposition)) {
            boolean flag1 = false;

            while (!flag1 && blockposition.getY() > 0) {
                BlockPosition blockposition1 = blockposition.down();
                IBlockData iblockdata = world.getType(blockposition1);

                if (iblockdata.getMaterial().isSolid()) {
                    flag1 = true;
                } else {
                    --this.locY;
                    blockposition = blockposition1;
                }
            }

            if (flag1) {
                // CraftBukkit start - Teleport event
                // this.enderTeleportTo(this.locX, this.locY, this.locZ);
                EntityTeleportEvent teleport = new EntityTeleportEvent(this.getBukkitEntity(), new Location(this.world.getWorld(), d3, d4, d5), new Location(this.world.getWorld(), this.locX, this.locY, this.locZ));
                this.world.getServer().getPluginManager().callEvent(teleport);
                if (!teleport.isCancelled()) {
                    Location to = teleport.getTo();
                    this.enderTeleportTo(to.getX(), to.getY(), to.getZ());
                    if (world.getCubes(this, this.getBoundingBox()).isEmpty() && !world.containsLiquid(this.getBoundingBox())) {
                        flag = true;
                    }
                }
                // CraftBukkit end
            }
        }

        if (!flag) {
            this.enderTeleportTo(d3, d4, d5);
            return false;
        } else {
            short short0 = 128;

            for (int i = 0; i < short0; ++i) {
                double d6 = (double) i / ((double) short0 - 1.0D);
                float f = (random.nextFloat() - 0.5F) * 0.2F;
                float f1 = (random.nextFloat() - 0.5F) * 0.2F;
                float f2 = (random.nextFloat() - 0.5F) * 0.2F;
                double d7 = d3 + (this.locX - d3) * d6 + (random.nextDouble() - 0.5D) * (double) this.width * 2.0D;
                double d8 = d4 + (this.locY - d4) * d6 + random.nextDouble() * (double) this.length;
                double d9 = d5 + (this.locZ - d5) * d6 + (random.nextDouble() - 0.5D) * (double) this.width * 2.0D;

                world.addParticle(EnumParticle.PORTAL, d7, d8, d9, (double) f, (double) f1, (double) f2, new int[0]);
            }

            if (this instanceof EntityCreature) {
                ((EntityCreature) this).getNavigation().o();
            }

            return true;
        }
    }

    public boolean cE() {
        return true;
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumItemSlot.Function.values().length];

        static {
            try {
                EntityLiving.SyntheticClass_1.a[EnumItemSlot.Function.HAND.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                EntityLiving.SyntheticClass_1.a[EnumItemSlot.Function.ARMOR.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

        }
    }
	
	class co extends RecursiveAction {
    EntityLiving el;
            
    co(EntityLiving el) {
        this.el = el;
    }
    
    @Override
    protected void compute() {
        el.co();
    }
             
}

class g extends RecursiveAction {
    EntityLiving el;
    float be, bf;
            
    g(EntityLiving el, float be, float bf) {
        this.el = el;
        this.be = be;
        this.bf = bf;
    }
    
    @Override
    protected void compute() {
        el.g(be, bf);
    }
             
}
}
