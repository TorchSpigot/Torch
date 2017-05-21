package net.minecraft.server;

import com.google.common.collect.Lists;
import com.koloboke.collect.map.hash.HashObjIntMaps;

import java.util.ArrayList;
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
    private static final DataWatcherObject<Integer> e = DataWatcher.a(EntityAreaEffectCloud.class, DataWatcherRegistry.b);
    private static final DataWatcherObject<Integer> f = DataWatcher.a(EntityAreaEffectCloud.class, DataWatcherRegistry.b);
    private PotionRegistry potionRegistry;
    public List<MobEffect> effects;
    private final Map<Entity, Integer> at;
    private int au;
    public int waitTime;
    public int reapplicationDelay;
    private boolean hasColor;
    public int durationOnUse;
    public float radiusOnUse;
    public float radiusPerTick;
    private EntityLiving aB;
    private UUID aC;

    public EntityAreaEffectCloud(World world) {
        super(world);
        this.potionRegistry = Potions.EMPTY;
        this.effects = Lists.newArrayList();
        this.at = HashObjIntMaps.newMutableMap();
        this.au = 600;
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

    @Override
	protected void i() {
        this.getDataWatcher().register(EntityAreaEffectCloud.b, Integer.valueOf(0));
        this.getDataWatcher().register(EntityAreaEffectCloud.a, Float.valueOf(0.5F));
        this.getDataWatcher().register(EntityAreaEffectCloud.c, Boolean.valueOf(false));
        this.getDataWatcher().register(EntityAreaEffectCloud.d, Integer.valueOf(EnumParticle.SPELL_MOB.c()));
        this.getDataWatcher().register(EntityAreaEffectCloud.e, Integer.valueOf(0));
        this.getDataWatcher().register(EntityAreaEffectCloud.f, Integer.valueOf(0));
    }

    public void setRadius(float f) {
        double d0 = this.locX;
        double d1 = this.locY;
        double d2 = this.locZ;

        this.setSize(f * 2.0F, 0.5F);
        this.setPosition(d0, d1, d2);
        this.getDataWatcher().set(EntityAreaEffectCloud.a, Float.valueOf(f));

    }

    public float getRadius() {
        return this.getDataWatcher().get(EntityAreaEffectCloud.a).floatValue();
    }

    public void a(PotionRegistry potionregistry) {
        this.potionRegistry = potionregistry;
        if (!this.hasColor) {
            this.C();
        }

    }

    private void C() {
        if (this.potionRegistry == Potions.EMPTY && this.effects.isEmpty()) {
            this.getDataWatcher().set(EntityAreaEffectCloud.b, Integer.valueOf(0));
        } else {
            this.getDataWatcher().set(EntityAreaEffectCloud.b, Integer.valueOf(PotionUtil.a(PotionUtil.a(this.potionRegistry, this.effects))));
        }

    }

    public void a(MobEffect mobeffect) {
        this.effects.add(mobeffect);
        if (!this.hasColor) {
            this.C();
        }

    }

    // CraftBukkit start accessor methods
    public void refreshEffects() {
        if (!this.hasColor) {
            this.getDataWatcher().set(EntityAreaEffectCloud.b, Integer.valueOf(PotionUtil.a(PotionUtil.a(this.potionRegistry, this.effects)))); // PAIL: rename
        }
    }

    public String getType() {
        return PotionRegistry.a.b(this.potionRegistry).toString(); // PAIL: rename
    }

    public void setType(String string) {
        a(PotionRegistry.a.get(new MinecraftKey(string))); // PAIL: rename
    }
    // CraftBukkit end

    public int getColor() {
        return this.getDataWatcher().get(EntityAreaEffectCloud.b).intValue();
    }

    public void setColor(int i) {
        this.hasColor = true;
        this.getDataWatcher().set(EntityAreaEffectCloud.b, Integer.valueOf(i));
    }

    public EnumParticle getParticle() {
        return EnumParticle.a(this.getDataWatcher().get(EntityAreaEffectCloud.d).intValue());
    }

    public void setParticle(EnumParticle enumparticle) {
        this.getDataWatcher().set(EntityAreaEffectCloud.d, Integer.valueOf(enumparticle.c()));
    }

    public int n() {
        return this.getDataWatcher().get(EntityAreaEffectCloud.e).intValue();
    }

    public void b(int i) {
        this.getDataWatcher().set(EntityAreaEffectCloud.e, Integer.valueOf(i));
    }

    public int o() {
        return this.getDataWatcher().get(EntityAreaEffectCloud.f).intValue();
    }

    public void d(int i) {
        this.getDataWatcher().set(EntityAreaEffectCloud.f, Integer.valueOf(i));
    }

    protected void a(boolean flag) {
        this.getDataWatcher().set(EntityAreaEffectCloud.c, Boolean.valueOf(flag));
    }

    public boolean q() {
        return this.getDataWatcher().get(EntityAreaEffectCloud.c).booleanValue();
    }

    public int getDuration() {
        return this.au;
    }

    public void setDuration(int i) {
        this.au = i;
    }

    @Override
	public void A_() {
        super.A_();
        boolean flag = this.q();
        float f = this.getRadius();

        if (this.ticksLived >= this.waitTime + this.au) {
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
            Iterator iterator = this.at.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();

                if (this.ticksLived >= ((Integer) entry.getValue()).intValue()) {
                    iterator.remove();
                }
            }

            ArrayList arraylist = Lists.newArrayList();
            Iterator iterator1 = this.potionRegistry.a().iterator();

            while (iterator1.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator1.next();

                arraylist.add(new MobEffect(mobeffect.getMobEffect(), mobeffect.getDuration() / 4, mobeffect.getAmplifier(), mobeffect.isAmbient(), mobeffect.isShowParticles()));
            }

            arraylist.addAll(this.effects);
            if (arraylist.isEmpty()) {
                this.at.clear();
            } else {
                List list = this.world.a(EntityLiving.class, this.getBoundingBox());

                if (!list.isEmpty()) {
                    Iterator iterator2 = list.iterator();

                    List<LivingEntity> entities = new ArrayList<LivingEntity>(); // CraftBukkit
                    while (iterator2.hasNext()) {
                        EntityLiving entityliving = (EntityLiving) iterator2.next();

                        if (!this.at.containsKey(entityliving) && entityliving.cJ()) {
                            double d0 = entityliving.locX - this.locX;
                            double d1 = entityliving.locZ - this.locZ;
                            double d2 = d0 * d0 + d1 * d1;

                            if (d2 <= f * f) {
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
                                this.at.put(entityliving, Integer.valueOf(this.ticksLived + this.reapplicationDelay));
                                Iterator iterator3 = arraylist.iterator();

                                while (iterator3.hasNext()) {
                                    MobEffect mobeffect1 = (MobEffect) iterator3.next();

                                    if (mobeffect1.getMobEffect().isInstant()) {
                                        mobeffect1.getMobEffect().applyInstantEffect(this, this.y(), entityliving, mobeffect1.getAmplifier(), 0.5D);
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
                                    this.au += this.durationOnUse;
                                    if (this.au <= 0) {
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
        this.aB = entityliving;
        this.aC = entityliving == null ? null : entityliving.getUniqueID();
    }

    @Nullable
    public EntityLiving y() {
        if (this.aB == null && this.aC != null && this.world instanceof WorldServer) {
            Entity entity = ((WorldServer) this.world).getEntity(this.aC);

            if (entity instanceof EntityLiving) {
                this.aB = (EntityLiving) entity;
            }
        }

        return this.aB;
    }

    @Override
	protected void a(NBTTagCompound nbttagcompound) {
        this.ticksLived = nbttagcompound.getInt("Age");
        this.au = nbttagcompound.getInt("Duration");
        this.waitTime = nbttagcompound.getInt("WaitTime");
        this.reapplicationDelay = nbttagcompound.getInt("ReapplicationDelay");
        this.durationOnUse = nbttagcompound.getInt("DurationOnUse");
        this.radiusOnUse = nbttagcompound.getFloat("RadiusOnUse");
        this.radiusPerTick = nbttagcompound.getFloat("RadiusPerTick");
        this.setRadius(nbttagcompound.getFloat("Radius"));
        this.aC = nbttagcompound.a("OwnerUUID");
        if (nbttagcompound.hasKeyOfType("Particle", 8)) {
            EnumParticle enumparticle = EnumParticle.a(nbttagcompound.getString("Particle"));

            if (enumparticle != null) {
                this.setParticle(enumparticle);
                this.b(nbttagcompound.getInt("ParticleParam1"));
                this.d(nbttagcompound.getInt("ParticleParam2"));
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

    @Override
	protected void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInt("Age", this.ticksLived);
        nbttagcompound.setInt("Duration", this.au);
        nbttagcompound.setInt("WaitTime", this.waitTime);
        nbttagcompound.setInt("ReapplicationDelay", this.reapplicationDelay);
        nbttagcompound.setInt("DurationOnUse", this.durationOnUse);
        nbttagcompound.setFloat("RadiusOnUse", this.radiusOnUse);
        nbttagcompound.setFloat("RadiusPerTick", this.radiusPerTick);
        nbttagcompound.setFloat("Radius", this.getRadius());
        nbttagcompound.setString("Particle", this.getParticle().b());
        nbttagcompound.setInt("ParticleParam1", this.n());
        nbttagcompound.setInt("ParticleParam2", this.o());
        if (this.aC != null) {
            nbttagcompound.a("OwnerUUID", this.aC);
        }

        if (this.hasColor) {
            nbttagcompound.setInt("Color", this.getColor());
        }

        if (this.potionRegistry != Potions.EMPTY && this.potionRegistry != null) {
            nbttagcompound.setString("Potion", PotionRegistry.a.b(this.potionRegistry).toString());
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

    @Override
	public void a(DataWatcherObject<?> datawatcherobject) {
        if (EntityAreaEffectCloud.a.equals(datawatcherobject)) {
            this.setRadius(this.getRadius());
        }

        super.a(datawatcherobject);
    }

    @Override
	public EnumPistonReaction o_() {
        return EnumPistonReaction.IGNORE;
    }
}
