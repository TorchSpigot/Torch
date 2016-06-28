package net.minecraft.server;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EntityTippedArrow extends EntityArrow {

    private static final DataWatcherObject<Integer> f = DataWatcher.a(EntityTippedArrow.class, DataWatcherRegistry.b);
    private PotionRegistry g;
    public final Set<MobEffect> h; // CraftBukkit private -> public

    public EntityTippedArrow(World world) {
        super(world);
        this.g = Potions.a;
        this.h = Sets.newHashSet();
    }

    public EntityTippedArrow(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
        this.g = Potions.a;
        this.h = Sets.newHashSet();
    }

    public EntityTippedArrow(World world, EntityLiving entityliving) {
        super(world, entityliving);
        this.g = Potions.a;
        this.h = Sets.newHashSet();
    }

    public void a(ItemStack itemstack) {
        if (itemstack.getItem() == Items.TIPPED_ARROW) {
            this.g = PotionUtil.c(itemstack.getTag());
            List list = PotionUtil.b(itemstack);

            if (!list.isEmpty()) {
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    MobEffect mobeffect = (MobEffect) iterator.next();

                    this.h.add(new MobEffect(mobeffect));
                }
            }

            this.datawatcher.set(EntityTippedArrow.f, Integer.valueOf(PotionUtil.a((Collection) PotionUtil.a(this.g, (Collection) list))));
        } else if (itemstack.getItem() == Items.ARROW) {
            this.g = Potions.a;
            this.h.clear();
            this.datawatcher.set(EntityTippedArrow.f, Integer.valueOf(0));
        }

    }

    public void a(MobEffect mobeffect) {
        this.h.add(mobeffect);
        this.getDataWatcher().set(EntityTippedArrow.f, Integer.valueOf(PotionUtil.a((Collection) PotionUtil.a(this.g, (Collection) this.h))));
    }

    protected void i() {
        super.i();
        this.datawatcher.register(EntityTippedArrow.f, Integer.valueOf(0));
    }

    public void m() {
        super.m();
        if (this.world.isClientSide) {
            if (this.inGround) {
                if (this.b % 5 == 0) {
                    this.b(1);
                }
            } else {
                this.b(2);
            }
        } else if (this.inGround && this.b != 0 && !this.h.isEmpty() && this.b >= 600) {
            this.world.broadcastEntityEffect(this, (byte) 0);
            this.g = Potions.a;
            this.h.clear();
            this.datawatcher.set(EntityTippedArrow.f, Integer.valueOf(0));
        }

    }

    private void b(int i) {
        int j = this.n();

        if (j != 0 && i > 0) {
            double d0 = (double) (j >> 16 & 255) / 255.0D;
            double d1 = (double) (j >> 8 & 255) / 255.0D;
            double d2 = (double) (j >> 0 & 255) / 255.0D;

            for (int k = 0; k < i; ++k) {
                this.world.addParticle(EnumParticle.SPELL_MOB, this.locX + (this.random.nextDouble() - 0.5D) * (double) this.width, this.locY + this.random.nextDouble() * (double) this.length, this.locZ + (this.random.nextDouble() - 0.5D) * (double) this.width, d0, d1, d2, new int[0]);
            }

        }
    }

    // CraftBukkit start accessor methods
    public void refreshEffects() {
        this.getDataWatcher().set(EntityTippedArrow.f, Integer.valueOf(PotionUtil.a((Collection) PotionUtil.a(this.g, (Collection) this.h)))); // PAIL: rename
    }

    public String getType() {
        return ((MinecraftKey) PotionRegistry.a.b(this.g)).toString();
    }

    public void setType(String string) {
        this.g = PotionRegistry.a.get(new MinecraftKey(string));
        this.datawatcher.set(EntityTippedArrow.f, Integer.valueOf(PotionUtil.a((Collection) PotionUtil.a(this.g, (Collection) this.h)))); // PAIL: rename
    }

    public boolean isTipped() {
        return !(this.h.isEmpty() && this.g == Potions.a); // PAIL: rename
    }
    // CraftBukkit end

    public int n() {
        return ((Integer) this.datawatcher.get(EntityTippedArrow.f)).intValue();
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        if (this.g != Potions.a && this.g != null) {
            nbttagcompound.setString("Potion", ((MinecraftKey) PotionRegistry.a.b(this.g)).toString());
        }

        if (!this.h.isEmpty()) {
            NBTTagList nbttaglist = new NBTTagList();
            Iterator iterator = this.h.iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator.next();

                nbttaglist.add(mobeffect.a(new NBTTagCompound()));
            }

            nbttagcompound.set("CustomPotionEffects", nbttaglist);
        }

    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.hasKeyOfType("Potion", 8)) {
            this.g = PotionUtil.c(nbttagcompound);
        }

        Iterator iterator = PotionUtil.b(nbttagcompound).iterator();

        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            this.a(mobeffect);
        }

        if (this.g != Potions.a || !this.h.isEmpty()) {
            this.datawatcher.set(EntityTippedArrow.f, Integer.valueOf(PotionUtil.a((Collection) PotionUtil.a(this.g, (Collection) this.h))));
        }

    }

    protected void a(EntityLiving entityliving) {
        super.a(entityliving);
        Iterator iterator = this.g.a().iterator();

        MobEffect mobeffect;

        while (iterator.hasNext()) {
            mobeffect = (MobEffect) iterator.next();
            entityliving.addEffect(new MobEffect(mobeffect.getMobEffect(), mobeffect.getDuration() / 8, mobeffect.getAmplifier(), mobeffect.isAmbient(), mobeffect.isShowParticles()));
        }

        if (!this.h.isEmpty()) {
            iterator = this.h.iterator();

            while (iterator.hasNext()) {
                mobeffect = (MobEffect) iterator.next();
                entityliving.addEffect(mobeffect);
            }
        }

    }

    protected ItemStack j() {
        if (this.h.isEmpty() && this.g == Potions.a) {
            return new ItemStack(Items.ARROW);
        } else {
            ItemStack itemstack = new ItemStack(Items.TIPPED_ARROW);

            PotionUtil.a(itemstack, this.g);
            PotionUtil.a(itemstack, (Collection) this.h);
            return itemstack;
        }
    }
}
