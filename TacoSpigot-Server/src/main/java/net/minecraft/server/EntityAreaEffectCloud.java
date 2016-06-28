package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.craftbukkit.entity.CraftLivingEntity; // CraftBukkit
import org.bukkit.entity.LivingEntity; // CraftBukkit

import java.util.Map.Entry;
import javax.annotation.Nullable;

public class EntityAreaEffectCloud extends Entity {

    private static final DataWatcherObject<Float> a = DataWatcher.a(EntityAreaEffectCloud.class, DataWatcherRegistry.c);
    private static final DataWatcherObject<Integer> b = DataWatcher.a(EntityAreaEffectCloud.class, DataWatcherRegistry.b);
    private static final DataWatcherObject<Boolean> c = DataWatcher.a(EntityAreaEffectCloud.class, DataWatcherRegistry.h);
    private static final DataWatcherObject<Integer> d = DataWatcher.a(EntityAreaEffectCloud.class, DataWatcherRegistry.b);
    private PotionRegistry e;
    public List<MobEffect> effects;
    private final Map<Entity, Integer> g;
    private int h;
    public int waitTime;
    public int reapplicationDelay;
    private boolean hasColor;
    public int durationOnUse;
    public float radiusOnUse;
    public float radiusPerTick;
    private EntityLiving az;
    private UUID aA;

    public EntityAreaEffectCloud(World world) {
        super(world);
        this.e = Potions.a;
        this.effects = Lists.newArrayList();
        this.g = Maps.newHashMap();
        this.h = 600;
        this.waitTime = 20;
        this.reapplicationDelay = 20;
        this.noclip = true;
        this.fireProof = true;
        this.setRadius(3.0F);
    }

    public EntityAreaEffectCloud(World world, double d0, double d1, double d2) {
        this(world);
        this.setPosition(d0, d1, d2);
    }

    protected void i() {
        this.getDataWatcher().register(EntityAreaEffectCloud.b, Integer.valueOf(0));
        this.getDataWatcher().register(EntityAreaEffectCloud.a, Float.valueOf(0.5F));
        this.getDataWatcher().register(EntityAreaEffectCloud.c, Boolean.valueOf(false));
        this.getDataWatcher().register(EntityAreaEffectCloud.d, Integer.valueOf(EnumParticle.SPELL_MOB.c()));
    }

    public void setRadius(float f) {
        double d0 = this.locX;
        double d1 = this.locY;
        double d2 = this.locZ;

        this.setSize(f * 2.0F, 0.5F);
        this.setPosition(d0, d1, d2);
        if (!this.world.isClientSide) {
            this.getDataWatcher().set(EntityAreaEffectCloud.a, Float.valueOf(f));
        }

    }

    public float getRadius() {
        return ((Float) this.getDataWatcher().get(EntityAreaEffectCloud.a)).floatValue();
    }

    public void a(PotionRegistry potionregistry) {
        this.e = potionregistry;
        if (!this.hasColor) {
            if (potionregistry == Potions.a && this.effects.isEmpty()) {
                this.getDataWatcher().set(EntityAreaEffectCloud.b, Integer.valueOf(0));
            } else {
                this.getDataWatcher().set(EntityAreaEffectCloud.b, Integer.valueOf(PotionUtil.a((Collection) PotionUtil.a(potionregistry, (Collection) this.effects))));
            }
        }

    }

    public void a(MobEffect mobeffect) {
        this.effects.add(mobeffect);
        if (!this.hasColor) {
            this.getDataWatcher().set(EntityAreaEffectCloud.b, Integer.valueOf(PotionUtil.a((Collection) PotionUtil.a(this.e, (Collection) this.effects))));
        }

    }

    // CraftBukkit start accessor methods
    public void refreshEffects() {
        if (!this.hasColor) {
            this.getDataWatcher().set(EntityAreaEffectCloud.b, Integer.valueOf(PotionUtil.a((Collection) PotionUtil.a(this.e, (Collection) this.effects)))); // PAIL: rename
        }
    }

    public String getType() {
        return ((MinecraftKey) PotionRegistry.a.b(this.e)).toString(); // PAIL: rename
    }

    public void setType(String string) {
        a(PotionRegistry.a.get(new MinecraftKey(string))); // PAIL: rename
    }
    // CraftBukkit end

    public int getColor() {
        return ((Integer) this.getDataWatcher().get(EntityAreaEffectCloud.b)).intValue();
    }

    public void setColor(int i) {
        this.hasColor = true;
        this.getDataWatcher().set(EntityAreaEffectCloud.b, Integer.valueOf(i));
    }

    public EnumParticle getParticle() {
        return EnumParticle.a(((Integer) this.getDataWatcher().get(EntityAreaEffectCloud.d)).intValue());
    }

    public void setParticle(EnumParticle enumparticle) {
        this.getDataWatcher().set(EntityAreaEffectCloud.d, Integer.valueOf(enumparticle.c()));
    }

    protected void a(boolean flag) {
        this.getDataWatcher().set(EntityAreaEffectCloud.c, Boolean.valueOf(flag));
    }

    public boolean n() {
        return ((Boolean) this.getDataWatcher().get(EntityAreaEffectCloud.c)).booleanValue();
    }

    public int getDuration() {
        return this.h;
    }

    public void setDuration(int i) {
        this.h = i;
    }

    public void m() {
        super.m();
        boolean flag = this.n();
        float f = this.getRadius();

        if (this.world.isClientSide) {
            EnumParticle enumparticle = this.getParticle();
            float f1;
            float f2;
            float f3;
            int i;
            int j;
            int k;

            if (flag) {
                if (this.random.nextBoolean()) {
                    int[] aint = new int[enumparticle.d()];

                    for (int l = 0; l < 2; ++l) {
                        float f4 = this.random.nextFloat() * 6.2831855F;

                        f1 = MathHelper.c(this.random.nextFloat()) * 0.2F;
                        f2 = MathHelper.cos(f4) * f1;
                        f3 = MathHelper.sin(f4) * f1;
                        if (enumparticle == EnumParticle.SPELL_MOB) {
                            int i1 = this.random.nextBoolean() ? 16777215 : this.getColor();

                            i = i1 >> 16 & 255;
                            j = i1 >> 8 & 255;
                            k = i1 & 255;
                            this.world.addParticle(EnumParticle.SPELL_MOB, this.locX + (double) f2, this.locY, this.locZ + (double) f3, (double) ((float) i / 255.0F), (double) ((float) j / 255.0F), (double) ((float) k / 255.0F), new int[0]);
                        } else {
                            this.world.addParticle(enumparticle, this.locX + (double) f2, this.locY, this.locZ + (double) f3, 0.0D, 0.0D, 0.0D, aint);
                        }
                    }
                }
            } else {
                float f5 = 3.1415927F * f * f;
                int[] aint1 = new int[enumparticle.d()];

                for (int j1 = 0; (float) j1 < f5; ++j1) {
                    f1 = this.random.nextFloat() * 6.2831855F;
                    f2 = MathHelper.c(this.random.nextFloat()) * f;
                    f3 = MathHelper.cos(f1) * f2;
                    float f6 = MathHelper.sin(f1) * f2;

                    if (enumparticle == EnumParticle.SPELL_MOB) {
                        i = this.getColor();
                        j = i >> 16 & 255;
                        k = i >> 8 & 255;
                        int k1 = i & 255;

                        this.world.addParticle(EnumParticle.SPELL_MOB, this.locX + (double) f3, this.locY, this.locZ + (double) f6, (double) ((float) j / 255.0F), (double) ((float) k / 255.0F), (double) ((float) k1 / 255.0F), new int[0]);
                    } else {
                        this.world.addParticle(enumparticle, this.locX + (double) f3, this.locY, this.locZ + (double) f6, (0.5D - this.random.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.random.nextDouble()) * 0.15D, aint1);
                    }
                }
            }
        } else {
            if (this.ticksLived >= this.waitTime + this.h) {
                this.die();
                return;
            }

            boolean flag1 = this.ticksLived < this.waitTime;

            if (flag != flag1) {
                this.a(flag1);
            }

            if (flag1) {
                return;
            }

            if (this.radiusPerTick != 0.0F) {
                f += this.radiusPerTick;
                if (f < 0.5F) {
                    this.die();
                    return;
                }

                this.setRadius(f);
            }

            if (this.ticksLived % 5 == 0) {
                Iterator iterator = this.g.entrySet().iterator();

                while (iterator.hasNext()) {
                    Entry entry = (Entry) iterator.next();

                    if (this.ticksLived >= ((Integer) entry.getValue()).intValue()) {
                        iterator.remove();
                    }
                }

                ArrayList arraylist = Lists.newArrayList();
                Iterator iterator1 = this.e.a().iterator();

                while (iterator1.hasNext()) {
                    MobEffect mobeffect = (MobEffect) iterator1.next();

                    arraylist.add(new MobEffect(mobeffect.getMobEffect(), mobeffect.getDuration() / 4, mobeffect.getAmplifier(), mobeffect.isAmbient(), mobeffect.isShowParticles()));
                }

                arraylist.addAll(this.effects);
                if (arraylist.isEmpty()) {
                    this.g.clear();
                } else {
                    List list = this.world.a(EntityLiving.class, this.getBoundingBox());

                    if (!list.isEmpty()) {
                        Iterator iterator2 = list.iterator();

                        List<LivingEntity> entities = new ArrayList<LivingEntity>(); // CraftBukkit
                        while (iterator2.hasNext()) {
                            EntityLiving entityliving = (EntityLiving) iterator2.next();

                            if (!this.g.containsKey(entityliving) && entityliving.cE()) {
                                double d0 = entityliving.locX - this.locX;
                                double d1 = entityliving.locZ - this.locZ;
                                double d2 = d0 * d0 + d1 * d1;

                                if (d2 <= (double) (f * f)) {
                                    // CraftBukkit start
                                    entities.add((LivingEntity) entityliving.getBukkitEntity());
                                }
                            }
                        }
                        org.bukkit.event.entity.AreaEffectCloudApplyEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callAreaEffectCloudApplyEvent(this, entities);
                        if (true) { // Preserve NMS spacing and bracket count for smallest diff
                            for (LivingEntity entity : event.getAffectedEntities()) {
                                if (entity instanceof CraftLivingEntity) {
                                    EntityLiving entityliving = ((CraftLivingEntity) entity).getHandle();
                                    // CraftBukkit end
                                    this.g.put(entityliving, Integer.valueOf(this.ticksLived + this.reapplicationDelay));
                                    Iterator iterator3 = arraylist.iterator();

                                    while (iterator3.hasNext()) {
                                        MobEffect mobeffect1 = (MobEffect) iterator3.next();

                                        if (mobeffect1.getMobEffect().isInstant()) {
                                            mobeffect1.getMobEffect().applyInstantEffect(this, this.w(), entityliving, mobeffect1.getAmplifier(), 0.5D);
                                        } else {
                                            entityliving.addEffect(new MobEffect(mobeffect1));
                                        }
                                    }

                                    if (this.radiusOnUse != 0.0F) {
                                        f += this.radiusOnUse;
                                        if (f < 0.5F) {
                                            this.die();
                                            return;
                                        }

                                        this.setRadius(f);
                                    }

                                    if (this.durationOnUse != 0) {
                                        this.h += this.durationOnUse;
                                        if (this.h <= 0) {
                                            this.die();
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public void setRadiusOnUse(float f) {
        this.radiusOnUse = f;
    }

    public void setRadiusPerTick(float f) {
        this.radiusPerTick = f;
    }

    public void setWaitTime(int i) {
        this.waitTime = i;
    }

    public void setSource(@Nullable EntityLiving entityliving) {
        this.az = entityliving;
        this.aA = entityliving == null ? null : entityliving.getUniqueID();
    }

    @Nullable
    public EntityLiving w() {
        if (this.az == null && this.aA != null && this.world instanceof WorldServer) {
            Entity entity = ((WorldServer) this.world).getEntity(this.aA);

            if (entity instanceof EntityLiving) {
                this.az = (EntityLiving) entity;
            }
        }

        return this.az;
    }

    protected void a(NBTTagCompound nbttagcompound) {
        this.ticksLived = nbttagcompound.getInt("Age");
        this.h = nbttagcompound.getInt("Duration");
        this.waitTime = nbttagcompound.getInt("WaitTime");
        this.reapplicationDelay = nbttagcompound.getInt("ReapplicationDelay");
        this.durationOnUse = nbttagcompound.getInt("DurationOnUse");
        this.radiusOnUse = nbttagcompound.getFloat("RadiusOnUse");
        this.radiusPerTick = nbttagcompound.getFloat("RadiusPerTick");
        this.setRadius(nbttagcompound.getFloat("Radius"));
        this.aA = nbttagcompound.a("OwnerUUID");
        if (nbttagcompound.hasKeyOfType("Particle", 8)) {
            EnumParticle enumparticle = EnumParticle.a(nbttagcompound.getString("Particle"));

            if (enumparticle != null) {
                this.setParticle(enumparticle);
            }
        }

        if (nbttagcompound.hasKeyOfType("Color", 99)) {
            this.setColor(nbttagcompound.getInt("Color"));
        }

        if (nbttagcompound.hasKeyOfType("Potion", 8)) {
            this.a(PotionUtil.c(nbttagcompound));
        }

        if (nbttagcompound.hasKeyOfType("Effects", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getList("Effects", 10);

            this.effects.clear();

            for (int i = 0; i < nbttaglist.size(); ++i) {
                MobEffect mobeffect = MobEffect.b(nbttaglist.get(i));

                if (mobeffect != null) {
                    this.a(mobeffect);
                }
            }
        }

    }

    protected void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInt("Age", this.ticksLived);
        nbttagcompound.setInt("Duration", this.h);
        nbttagcompound.setInt("WaitTime", this.waitTime);
        nbttagcompound.setInt("ReapplicationDelay", this.reapplicationDelay);
        nbttagcompound.setInt("DurationOnUse", this.durationOnUse);
        nbttagcompound.setFloat("RadiusOnUse", this.radiusOnUse);
        nbttagcompound.setFloat("RadiusPerTick", this.radiusPerTick);
        nbttagcompound.setFloat("Radius", this.getRadius());
        nbttagcompound.setString("Particle", this.getParticle().b());
        if (this.aA != null) {
            nbttagcompound.a("OwnerUUID", this.aA);
        }

        if (this.hasColor) {
            nbttagcompound.setInt("Color", this.getColor());
        }

        if (this.e != Potions.a && this.e != null) {
            nbttagcompound.setString("Potion", ((MinecraftKey) PotionRegistry.a.b(this.e)).toString());
        }

        if (!this.effects.isEmpty()) {
            NBTTagList nbttaglist = new NBTTagList();
            Iterator iterator = this.effects.iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator.next();

                nbttaglist.add(mobeffect.a(new NBTTagCompound()));
            }

            nbttagcompound.set("Effects", nbttaglist);
        }

    }

    public void a(DataWatcherObject<?> datawatcherobject) {
        if (EntityAreaEffectCloud.a.equals(datawatcherobject)) {
            this.setRadius(this.getRadius());
        }

        super.a(datawatcherobject);
    }

    public EnumPistonReaction z() {
        return EnumPistonReaction.IGNORE;
    }
}
