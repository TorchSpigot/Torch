package net.minecraft.server;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;

import java.util.concurrent.RecursiveAction;
import org.spigotmc.SpigotWorldConfig;
import org.hose.Tick_Pool;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
// CraftBukkit end

public abstract class EntityInsentient extends EntityLiving {

    private static final DataWatcherObject<Byte> a = DataWatcher.a(EntityInsentient.class, DataWatcherRegistry.a);
    public int a_;
    protected int b_;
    private ControllerLook lookController;
    public ControllerMove moveController;
    protected ControllerJump g;
    private EntityAIBodyControl c;
    protected NavigationAbstract navigation;
    public PathfinderGoalSelector goalSelector;
    public PathfinderGoalSelector targetSelector;
    private EntityLiving goalTarget;
    private EntitySenses bv;
    private ItemStack[] bw = new ItemStack[2];
    public float[] dropChanceHand = new float[2];
    private ItemStack[] bx = new ItemStack[4];
    public float[] dropChanceArmor = new float[4];
    public boolean canPickUpLoot;
    public boolean persistent;
    private Map<PathType, Float> bA = Maps.newEnumMap(PathType.class);
    private MinecraftKey bB;
    private long bC;
    private boolean bD;
    private Entity leashHolder;
    private NBTTagCompound bF;
    public PathfinderGoalFloat goalFloat; // Paper

    public EntityInsentient(World world) {
        super(world);
        this.goalSelector = new PathfinderGoalSelector(world != null && world.methodProfiler != null ? world.methodProfiler : null);
        this.targetSelector = new PathfinderGoalSelector(world != null && world.methodProfiler != null ? world.methodProfiler : null);
        this.lookController = new ControllerLook(this);
        this.moveController = new ControllerMove(this);
        this.g = new ControllerJump(this);
        this.c = this.s();
        this.navigation = this.b(world);
        this.bv = new EntitySenses(this);

        int i;

        for (i = 0; i < this.dropChanceArmor.length; ++i) {
            this.dropChanceArmor[i] = 0.085F;
        }

        for (i = 0; i < this.dropChanceHand.length; ++i) {
            this.dropChanceHand[i] = 0.085F;
        }

        if (world != null && !world.isClientSide) {
            this.r();
        }

        // CraftBukkit start - default persistance to type's persistance value
        this.persistent = !isTypeNotPersistent();
        // CraftBukkit end
    }

    protected void r() {}

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeMap().b(GenericAttributes.FOLLOW_RANGE).setValue(16.0D);
    }

    protected NavigationAbstract b(World world) {
        return new Navigation(this, world);
    }

    public float a(PathType pathtype) {
        // CraftBukkit - decompile error
        Float ofloat = (Float) this.bA.get(pathtype);

        return ofloat == null ? pathtype.a() : ofloat.floatValue();
    }

    public void a(PathType pathtype, float f) {
        this.bA.put(pathtype, Float.valueOf(f));
    }

    protected EntityAIBodyControl s() {
        return new EntityAIBodyControl(this);
    }

    public ControllerLook getControllerLook() {
        return this.lookController;
    }

    public ControllerMove getControllerMove() {
        return this.moveController;
    }

    public ControllerJump getControllerJump() {
        return this.g;
    }

    public NavigationAbstract getNavigation() {
        return this.navigation;
    }

    public EntitySenses getEntitySenses() {
        return this.bv;
    }

    @Nullable
    public EntityLiving getGoalTarget() {
        return this.goalTarget;
    }

    public void setGoalTarget(@Nullable EntityLiving entityliving) {
        // CraftBukkit start - fire event
        setGoalTarget(entityliving, EntityTargetEvent.TargetReason.UNKNOWN, true);
    }

    public boolean setGoalTarget(EntityLiving entityliving, EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        if (getGoalTarget() == entityliving) return false;
        if (fireEvent) {
            if (reason == EntityTargetEvent.TargetReason.UNKNOWN && getGoalTarget() != null && entityliving == null) {
                reason = getGoalTarget().isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
            }
            if (reason == EntityTargetEvent.TargetReason.UNKNOWN) {
                world.getServer().getLogger().log(java.util.logging.Level.WARNING, "Unknown target reason, please report on the issue tracker", new Exception());
            }
            CraftLivingEntity ctarget = null;
            if (entityliving != null) {
                ctarget = (CraftLivingEntity) entityliving.getBukkitEntity();
            }
            EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(this.getBukkitEntity(), ctarget, reason);
            world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            if (event.getTarget() != null) {
                entityliving = ((CraftLivingEntity) event.getTarget()).getHandle();
            } else {
                entityliving = null;
            }
        }
        this.goalTarget = entityliving;
        return true;
        // CraftBukkit end
    }

    public boolean d(Class<? extends EntityLiving> oclass) {
        return oclass != EntityGhast.class;
    }

    public void B() {}

    protected void i() {
        super.i();
        this.datawatcher.register(EntityInsentient.a, Byte.valueOf((byte) 0));
    }

    public int C() {
        return 80;
    }

    public void D() {
        SoundEffect soundeffect = this.G();

        if (soundeffect != null) {
            this.a(soundeffect, this.ce(), this.cf());
        }

    }

    public void U() {
        super.U();
        this.world.methodProfiler.a("mobBaseTick");
        if (this.isAlive() && this.random.nextInt(1000) < this.a_++) {
            this.o();
            this.D();
        }

        this.world.methodProfiler.b();
    }

    protected void c(DamageSource damagesource) {
        this.o();
        super.c(damagesource);
    }

    private void o() {
        this.a_ = -this.C();
    }

    protected int getExpValue(EntityHuman entityhuman) {
        if (this.b_ > 0) {
            int i = this.b_;

            int j;

            for (j = 0; j < this.bx.length; ++j) {
                if (this.bx[j] != null && this.dropChanceArmor[j] <= 1.0F) {
                    i += 1 + this.random.nextInt(3);
                }
            }

            for (j = 0; j < this.bw.length; ++j) {
                if (this.bw[j] != null && this.dropChanceHand[j] <= 1.0F) {
                    i += 1 + this.random.nextInt(3);
                }
            }

            return i;
        } else {
            return this.b_;
        }
    }

    public void doSpawnEffect() {
        if (this.world.isClientSide) {
            for (int i = 0; i < 20; ++i) {
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;
                double d3 = 10.0D;

                this.world.addParticle(EnumParticle.EXPLOSION_NORMAL, this.locX + (double) (this.random.nextFloat() * this.width * 2.0F) - (double) this.width - d0 * d3, this.locY + (double) (this.random.nextFloat() * this.length) - d1 * d3, this.locZ + (double) (this.random.nextFloat() * this.width * 2.0F) - (double) this.width - d2 * d3, d0, d1, d2, new int[0]);
            }
        } else {
            this.world.broadcastEntityEffect(this, (byte) 20);
        }

    }

    public void m() {
        super.m();
        if (!this.world.isClientSide) {
            this.cP();
            if (this.ticksLived % 5 == 0) {
                boolean flag = !(this.bu() instanceof EntityInsentient);
                boolean flag1 = !(this.bz() instanceof EntityBoat);

                this.goalSelector.a(5, flag && flag1);
                this.goalSelector.a(2, flag);
            }
        }

    }

    protected float h(float f, float f1) {
        this.c.a();
        return f1;
    }

    @Nullable
    protected SoundEffect G() {
        return null;
    }

    @Nullable
    protected Item getLoot() {
        return null;
    }

    protected void dropDeathLoot(boolean flag, int i) {
        Item item = this.getLoot();

        if (item != null) {
            int j = this.random.nextInt(3);

            if (i > 0) {
                j += this.random.nextInt(i + 1);
            }

            for (int k = 0; k < j; ++k) {
                this.a(item, 1);
            }
        }

    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setBoolean("CanPickUpLoot", this.cN());
        nbttagcompound.setBoolean("PersistenceRequired", this.persistent);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.bx.length; ++i) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            if (this.bx[i] != null) {
                this.bx[i].save(nbttagcompound1);
            }

            nbttaglist.add(nbttagcompound1);
        }

        nbttagcompound.set("ArmorItems", nbttaglist);
        NBTTagList nbttaglist1 = new NBTTagList();

        for (int j = 0; j < this.bw.length; ++j) {
            NBTTagCompound nbttagcompound2 = new NBTTagCompound();

            if (this.bw[j] != null) {
                this.bw[j].save(nbttagcompound2);
            }

            nbttaglist1.add(nbttagcompound2);
        }

        nbttagcompound.set("HandItems", nbttaglist1);
        NBTTagList nbttaglist2 = new NBTTagList();

        for (int k = 0; k < this.dropChanceArmor.length; ++k) {
            nbttaglist2.add(new NBTTagFloat(this.dropChanceArmor[k]));
        }

        nbttagcompound.set("ArmorDropChances", nbttaglist2);
        NBTTagList nbttaglist3 = new NBTTagList();

        for (int l = 0; l < this.dropChanceHand.length; ++l) {
            nbttaglist3.add(new NBTTagFloat(this.dropChanceHand[l]));
        }

        nbttagcompound.set("HandDropChances", nbttaglist3);
        nbttagcompound.setBoolean("Leashed", this.bD);
        if (this.leashHolder != null) {
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();

            if (this.leashHolder instanceof EntityLiving) {
                UUID uuid = this.leashHolder.getUniqueID();

                nbttagcompound3.a("UUID", uuid);
            } else if (this.leashHolder instanceof EntityHanging) {
                BlockPosition blockposition = ((EntityHanging) this.leashHolder).getBlockPosition();

                nbttagcompound3.setInt("X", blockposition.getX());
                nbttagcompound3.setInt("Y", blockposition.getY());
                nbttagcompound3.setInt("Z", blockposition.getZ());
            }

            nbttagcompound.set("Leash", nbttagcompound3);
        }

        nbttagcompound.setBoolean("LeftHanded", this.cT());
        if (this.bB != null) {
            nbttagcompound.setString("DeathLootTable", this.bB.toString());
            if (this.bC != 0L) {
                nbttagcompound.setLong("DeathLootTableSeed", this.bC);
            }
        }

        if (this.hasAI()) {
            nbttagcompound.setBoolean("NoAI", this.hasAI());
        }

    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);

        // CraftBukkit start - If looting or persistence is false only use it if it was set after we started using it
        if (nbttagcompound.hasKeyOfType("CanPickUpLoot", 1)) {
            boolean data = nbttagcompound.getBoolean("CanPickUpLoot");
            if (isLevelAtLeast(nbttagcompound, 1) || data) {
                this.l(data);
            }
        }

        boolean data = nbttagcompound.getBoolean("PersistenceRequired");
        if (isLevelAtLeast(nbttagcompound, 1) || data) {
            this.persistent = data;
        }
        // CraftBukkit end
        NBTTagList nbttaglist;
        int i;

        if (nbttagcompound.hasKeyOfType("ArmorItems", 9)) {
            nbttaglist = nbttagcompound.getList("ArmorItems", 10);

            for (i = 0; i < this.bx.length; ++i) {
                this.bx[i] = ItemStack.createStack(nbttaglist.get(i));
            }
        }

        if (nbttagcompound.hasKeyOfType("HandItems", 9)) {
            nbttaglist = nbttagcompound.getList("HandItems", 10);

            for (i = 0; i < this.bw.length; ++i) {
                this.bw[i] = ItemStack.createStack(nbttaglist.get(i));
            }
        }

        if (nbttagcompound.hasKeyOfType("ArmorDropChances", 9)) {
            nbttaglist = nbttagcompound.getList("ArmorDropChances", 5);

            for (i = 0; i < nbttaglist.size(); ++i) {
                this.dropChanceArmor[i] = nbttaglist.f(i);
            }
        }

        if (nbttagcompound.hasKeyOfType("HandDropChances", 9)) {
            nbttaglist = nbttagcompound.getList("HandDropChances", 5);

            for (i = 0; i < nbttaglist.size(); ++i) {
                this.dropChanceHand[i] = nbttaglist.f(i);
            }
        }

        this.bD = nbttagcompound.getBoolean("Leashed");
        if (this.bD && nbttagcompound.hasKeyOfType("Leash", 10)) {
            this.bF = nbttagcompound.getCompound("Leash");
        }

        this.n(nbttagcompound.getBoolean("LeftHanded"));
        if (nbttagcompound.hasKeyOfType("DeathLootTable", 8)) {
            this.bB = new MinecraftKey(nbttagcompound.getString("DeathLootTable"));
            this.bC = nbttagcompound.getLong("DeathLootTableSeed");
        }

        this.setAI(nbttagcompound.getBoolean("NoAI"));
    }

    @Nullable
    protected MinecraftKey J() {
        return null;
    }

    protected void a(boolean flag, int i, DamageSource damagesource) {
        MinecraftKey minecraftkey = this.bB;

        if (minecraftkey == null) {
            minecraftkey = this.J();
        }

        if (minecraftkey != null) {
            LootTable loottable = this.world.ak().a(minecraftkey);

            this.bB = null;
            LootTableInfo.a loottableinfo_a = (new LootTableInfo.a((WorldServer) this.world)).a((Entity) this).a(damagesource);

            if (flag && this.killer != null) {
                loottableinfo_a = loottableinfo_a.a(this.killer).a(this.killer.dc());
            }

            List list = loottable.a(this.bC == 0L ? this.random : new Random(this.bC), loottableinfo_a.a());
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                ItemStack itemstack = (ItemStack) iterator.next();

                this.a(itemstack, 0.0F);
            }

            this.dropEquipment(flag, i);
        } else {
            super.a(flag, i, damagesource);
        }

    }

    public void o(float f) {
        this.bf = f;
    }

    public void p(float f) {
        this.be = f;
    }

    public void l(float f) {
        super.l(f);
        this.o(f);
    }

    public void n() {
        super.n();
        this.world.methodProfiler.a("looting");
        if (!this.world.isClientSide && this.cN() && !this.aU && this.world.getGameRules().getBoolean("mobGriefing")) {
            List list = this.world.a(EntityItem.class, this.getBoundingBox().grow(1.0D, 0.0D, 1.0D));
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityItem entityitem = (EntityItem) iterator.next();

                if (!entityitem.dead && entityitem.getItemStack() != null && !entityitem.t()) {
                    this.a(entityitem);
                }
            }
        }

        this.world.methodProfiler.b();
    }

    protected void a(EntityItem entityitem) {
        ItemStack itemstack = entityitem.getItemStack();
        EnumItemSlot enumitemslot = d(itemstack);
        boolean flag = true;
        ItemStack itemstack1 = this.getEquipment(enumitemslot);

        if (itemstack1 != null) {
            if (enumitemslot.a() == EnumItemSlot.Function.HAND) {
                if (itemstack.getItem() instanceof ItemSword && !(itemstack1.getItem() instanceof ItemSword)) {
                    flag = true;
                } else if (itemstack.getItem() instanceof ItemSword && itemstack1.getItem() instanceof ItemSword) {
                    ItemSword itemsword = (ItemSword) itemstack.getItem();
                    ItemSword itemsword1 = (ItemSword) itemstack1.getItem();

                    if (itemsword.g() == itemsword1.g()) {
                        flag = itemstack.getData() > itemstack1.getData() || itemstack.hasTag() && !itemstack1.hasTag();
                    } else {
                        flag = itemsword.g() > itemsword1.g();
                    }
                } else if (itemstack.getItem() instanceof ItemBow && itemstack1.getItem() instanceof ItemBow) {
                    flag = itemstack.hasTag() && !itemstack1.hasTag();
                } else {
                    flag = false;
                }
            } else if (itemstack.getItem() instanceof ItemArmor && !(itemstack1.getItem() instanceof ItemArmor)) {
                flag = true;
            } else if (itemstack.getItem() instanceof ItemArmor && itemstack1.getItem() instanceof ItemArmor) {
                ItemArmor itemarmor = (ItemArmor) itemstack.getItem();
                ItemArmor itemarmor1 = (ItemArmor) itemstack1.getItem();

                if (itemarmor.d == itemarmor1.d) {
                    flag = itemstack.getData() > itemstack1.getData() || itemstack.hasTag() && !itemstack1.hasTag();
                } else {
                    flag = itemarmor.d > itemarmor1.d;
                }
            } else {
                flag = false;
            }
        }

        if (flag && this.c(itemstack)) {
            double d0;

            switch (EntityInsentient.SyntheticClass_1.a[enumitemslot.a().ordinal()]) {
            case 1:
                d0 = (double) this.dropChanceHand[enumitemslot.b()];
                break;

            case 2:
                d0 = (double) this.dropChanceArmor[enumitemslot.b()];
                break;

            default:
                d0 = 0.0D;
            }

            if (itemstack1 != null && (double) (this.random.nextFloat() - 0.1F) < d0) {
                this.a(itemstack1, 0.0F);
            }

            if (itemstack.getItem() == Items.DIAMOND && entityitem.n() != null) {
                EntityHuman entityhuman = this.world.a(entityitem.n());

                if (entityhuman != null) {
                    entityhuman.b((Statistic) AchievementList.x);
                }
            }

            this.setSlot(enumitemslot, itemstack);
            switch (EntityInsentient.SyntheticClass_1.a[enumitemslot.a().ordinal()]) {
            case 1:
                this.dropChanceHand[enumitemslot.b()] = 2.0F;
                break;

            case 2:
                this.dropChanceArmor[enumitemslot.b()] = 2.0F;
            }

            this.persistent = true;
            this.receive(entityitem, 1);
            entityitem.die();
        }

    }

    protected boolean c(ItemStack itemstack) {
        return true;
    }

    protected boolean isTypeNotPersistent() {
        return true;
    }

    protected void L() {
        if (this.persistent) {
            this.ticksFarFromPlayer = 0;
        } else {
            EntityHuman entityhuman = this.world.findNearbyPlayer(this, -1.0D, EntityHuman.affectsSpawningFilter()); // Paper - affectsSpawning filter

            if (entityhuman != null) {
                double d0 = entityhuman.locX - this.locX;
                double d1 = entityhuman.locY - this.locY;
                double d2 = entityhuman.locZ - this.locZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 > world.paperConfig.hardDespawnDistance) { // CraftBukkit - remove isTypeNotPersistent() check // Paper - custom despawn distances
                    this.die();
                }

                if (this.ticksFarFromPlayer > 600 && this.random.nextInt(800) == 0 && d3 > world.paperConfig.softDespawnDistance) { // CraftBukkit - remove isTypeNotPersistent() check // Paper - custom despawn distances
                    this.die();
                } else if (d3 < world.paperConfig.softDespawnDistance) { // Paper - custom despawn distances
                    this.ticksFarFromPlayer = 0;
                }
            }

        }
    }

    navigation nav_task;
    targetSelector tar_task;
    protected final void doTick() {//entityliving`s dotick
        ++this.ticksFarFromPlayer;
        this.world.methodProfiler.a("checkDespawn");
        this.L();
        this.world.methodProfiler.b();
        // Spigot Start
        if ( this.fromMobSpawner )
        {
            // Paper start - Allow nerfed mobs to jump and float
            if (goalFloat != null) {
                if (goalFloat.a()) goalFloat.e();
                this.g.b();
            }
            // Paper end
            return;
        }
        // Spigot End
        this.world.methodProfiler.a("sensing");
        this.bv.a();
        this.world.methodProfiler.b();
        this.world.methodProfiler.a("targetSelector");
        //this.targetSelector.a();
        tar_task = new targetSelector();
        tar_task.fork();
        this.world.methodProfiler.b();
        this.world.methodProfiler.a("goalSelector");
        try {
            this.goalSelector.a();
        } catch (Exception ex) {
            world.getServer().getLogger().log(java.util.logging.Level.WARNING, "Find no goal.");
        }
        this.world.methodProfiler.b();
        this.world.methodProfiler.a("navigation");
        //this.navigation.l();//hose
        nav_task = new navigation();
        nav_task.fork();
        this.world.methodProfiler.b();
        this.world.methodProfiler.a("mob tick");
        this.M();
        this.world.methodProfiler.b();
        if (this.isPassenger() && this.bz() instanceof EntityInsentient) {
            EntityInsentient entityinsentient = (EntityInsentient) this.bz();

            entityinsentient.getNavigation().a(this.getNavigation().k(), 1.5D);
            entityinsentient.getControllerMove().a(this.getControllerMove());
        }

        this.world.methodProfiler.a("controls");
        this.world.methodProfiler.a("move");
        this.moveController.c();
        this.world.methodProfiler.c("look");
        this.lookController.a();
        this.world.methodProfiler.c("jump");
        this.g.b();
        this.world.methodProfiler.b();
        this.world.methodProfiler.b();
    }

    protected void M() {}

    public int N() {
        return 40;
    }

    public int cF() {
        return 10;
    }

    public void a(Entity entity, float f, float f1) {
        double d0 = entity.locX - this.locX;
        double d1 = entity.locZ - this.locZ;
        double d2;

        if (entity instanceof EntityLiving) {
            EntityLiving entityliving = (EntityLiving) entity;

            d2 = entityliving.locY + (double) entityliving.getHeadHeight() - (this.locY + (double) this.getHeadHeight());
        } else {
            d2 = (entity.getBoundingBox().b + entity.getBoundingBox().e) / 2.0D - (this.locY + (double) this.getHeadHeight());
        }

        double d3 = (double) MathHelper.sqrt(d0 * d0 + d1 * d1);
        float f2 = (float) (MathHelper.b(d1, d0) * 57.2957763671875D) - 90.0F;
        float f3 = (float) (-(MathHelper.b(d2, d3) * 57.2957763671875D));

        this.pitch = this.b(this.pitch, f3, f1);
        this.yaw = this.b(this.yaw, f2, f);
    }

    private float b(float f, float f1, float f2) {
        float f3 = MathHelper.g(f1 - f);

        if (f3 > f2) {
            f3 = f2;
        }

        if (f3 < -f2) {
            f3 = -f2;
        }

        return f + f3;
    }

    public boolean cG() {
        return true;
    }

    public boolean canSpawn() {
        return !this.world.containsLiquid(this.getBoundingBox()) && this.world.getCubes(this, this.getBoundingBox()).isEmpty() && this.world.a(this.getBoundingBox(), (Entity) this);
    }

    public int cK() {
        return 4;
    }

    public int aW() {
        if (this.getGoalTarget() == null) {
            return 3;
        } else {
            int i = (int) (this.getHealth() - this.getMaxHealth() * 0.33F);

            i -= (3 - this.world.getDifficulty().a()) * 4;
            if (i < 0) {
                i = 0;
            }

            return i + 3;
        }
    }

    public Iterable<ItemStack> aE() {
        return Arrays.asList(this.bw);
    }

    public Iterable<ItemStack> getArmorItems() {
        return Arrays.asList(this.bx);
    }

    @Nullable
    public ItemStack getEquipment(EnumItemSlot enumitemslot) {
        ItemStack itemstack = null;

        switch (EntityInsentient.SyntheticClass_1.a[enumitemslot.a().ordinal()]) {
        case 1:
            itemstack = this.bw[enumitemslot.b()];
            break;

        case 2:
            itemstack = this.bx[enumitemslot.b()];
        }

        return itemstack;
    }

    public void setSlot(EnumItemSlot enumitemslot, @Nullable ItemStack itemstack) {
        switch (EntityInsentient.SyntheticClass_1.a[enumitemslot.a().ordinal()]) {
        case 1:
            this.bw[enumitemslot.b()] = itemstack;
            break;

        case 2:
            this.bx[enumitemslot.b()] = itemstack;
        }

    }

    protected void dropEquipment(boolean flag, int i) {
        EnumItemSlot[] aenumitemslot = EnumItemSlot.values();
        int j = aenumitemslot.length;

        for (int k = 0; k < j; ++k) {
            EnumItemSlot enumitemslot = aenumitemslot[k];
            ItemStack itemstack = this.getEquipment(enumitemslot);
            double d0;

            switch (EntityInsentient.SyntheticClass_1.a[enumitemslot.a().ordinal()]) {
            case 1:
                d0 = (double) this.dropChanceHand[enumitemslot.b()];
                break;

            case 2:
                d0 = (double) this.dropChanceArmor[enumitemslot.b()];
                break;

            default:
                d0 = 0.0D;
            }

            boolean flag1 = d0 > 1.0D;

            if (itemstack != null && (flag || flag1) && (double) (this.random.nextFloat() - (float) i * 0.01F) < d0) {
                if (!flag1 && itemstack.e()) {
                    int l = Math.max(itemstack.j() - 25, 1);
                    int i1 = itemstack.j() - this.random.nextInt(this.random.nextInt(l) + 1);

                    if (i1 > l) {
                        i1 = l;
                    }

                    if (i1 < 1) {
                        i1 = 1;
                    }

                    itemstack.setData(i1);
                }

                this.a(itemstack, 0.0F);
            }
        }

    }

    protected void a(DifficultyDamageScaler difficultydamagescaler) {
        if (this.random.nextFloat() < 0.15F * difficultydamagescaler.c()) {
            int i = this.random.nextInt(2);
            float f = this.world.getDifficulty() == EnumDifficulty.HARD ? 0.1F : 0.25F;

            if (this.random.nextFloat() < 0.095F) {
                ++i;
            }

            if (this.random.nextFloat() < 0.095F) {
                ++i;
            }

            if (this.random.nextFloat() < 0.095F) {
                ++i;
            }

            boolean flag = true;
            EnumItemSlot[] aenumitemslot = EnumItemSlot.values();
            int j = aenumitemslot.length;

            for (int k = 0; k < j; ++k) {
                EnumItemSlot enumitemslot = aenumitemslot[k];

                if (enumitemslot.a() == EnumItemSlot.Function.ARMOR) {
                    ItemStack itemstack = this.getEquipment(enumitemslot);

                    if (!flag && this.random.nextFloat() < f) {
                        break;
                    }

                    flag = false;
                    if (itemstack == null) {
                        Item item = a(enumitemslot, i);

                        if (item != null) {
                            this.setSlot(enumitemslot, new ItemStack(item));
                        }
                    }
                }
            }
        }

    }

    public static EnumItemSlot d(ItemStack itemstack) {
        return itemstack.getItem() != Item.getItemOf(Blocks.PUMPKIN) && itemstack.getItem() != Items.SKULL ? (itemstack.getItem() == Items.cR ? EnumItemSlot.CHEST : (itemstack.getItem() instanceof ItemArmor ? ((ItemArmor) itemstack.getItem()).c : (itemstack.getItem() == Items.cR ? EnumItemSlot.CHEST : EnumItemSlot.MAINHAND))) : EnumItemSlot.HEAD;
    }

    public static Item a(EnumItemSlot enumitemslot, int i) {
        switch (EntityInsentient.SyntheticClass_1.b[enumitemslot.ordinal()]) {
        case 1:
            if (i == 0) {
                return Items.LEATHER_HELMET;
            } else if (i == 1) {
                return Items.GOLDEN_HELMET;
            } else if (i == 2) {
                return Items.CHAINMAIL_HELMET;
            } else if (i == 3) {
                return Items.IRON_HELMET;
            } else if (i == 4) {
                return Items.DIAMOND_HELMET;
            }

        case 2:
            if (i == 0) {
                return Items.LEATHER_CHESTPLATE;
            } else if (i == 1) {
                return Items.GOLDEN_CHESTPLATE;
            } else if (i == 2) {
                return Items.CHAINMAIL_CHESTPLATE;
            } else if (i == 3) {
                return Items.IRON_CHESTPLATE;
            } else if (i == 4) {
                return Items.DIAMOND_CHESTPLATE;
            }

        case 3:
            if (i == 0) {
                return Items.LEATHER_LEGGINGS;
            } else if (i == 1) {
                return Items.GOLDEN_LEGGINGS;
            } else if (i == 2) {
                return Items.CHAINMAIL_LEGGINGS;
            } else if (i == 3) {
                return Items.IRON_LEGGINGS;
            } else if (i == 4) {
                return Items.DIAMOND_LEGGINGS;
            }

        case 4:
            if (i == 0) {
                return Items.LEATHER_BOOTS;
            } else if (i == 1) {
                return Items.GOLDEN_BOOTS;
            } else if (i == 2) {
                return Items.CHAINMAIL_BOOTS;
            } else if (i == 3) {
                return Items.IRON_BOOTS;
            } else if (i == 4) {
                return Items.DIAMOND_BOOTS;
            }

        default:
            return null;
        }
    }

    protected void b(DifficultyDamageScaler difficultydamagescaler) {
        float f = difficultydamagescaler.c();

        if (this.getItemInMainHand() != null && this.random.nextFloat() < 0.25F * f) {
            EnchantmentManager.a(this.random, this.getItemInMainHand(), (int) (5.0F + f * (float) this.random.nextInt(18)), false);
        }

        EnumItemSlot[] aenumitemslot = EnumItemSlot.values();
        int i = aenumitemslot.length;

        for (int j = 0; j < i; ++j) {
            EnumItemSlot enumitemslot = aenumitemslot[j];

            if (enumitemslot.a() == EnumItemSlot.Function.ARMOR) {
                ItemStack itemstack = this.getEquipment(enumitemslot);

                if (itemstack != null && this.random.nextFloat() < 0.5F * f) {
                    EnchantmentManager.a(this.random, itemstack, (int) (5.0F + f * (float) this.random.nextInt(18)), false);
                }
            }
        }

    }

    @Nullable
    public GroupDataEntity prepare(DifficultyDamageScaler difficultydamagescaler, @Nullable GroupDataEntity groupdataentity) {
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).b(new AttributeModifier("Random spawn bonus", this.random.nextGaussian() * 0.05D, 1));
        if (this.random.nextFloat() < 0.05F) {
            this.n(true);
        } else {
            this.n(false);
        }

        return groupdataentity;
    }

    public boolean cL() {
        return false;
    }

    public void cM() {
        this.persistent = true;
    }

    public void a(EnumItemSlot enumitemslot, float f) {
        switch (EntityInsentient.SyntheticClass_1.a[enumitemslot.a().ordinal()]) {
        case 1:
            this.dropChanceHand[enumitemslot.b()] = f;
            break;

        case 2:
            this.dropChanceArmor[enumitemslot.b()] = f;
        }

    }

    public boolean cN() {
        return this.canPickUpLoot;
    }

    public void l(boolean flag) {
        this.canPickUpLoot = flag;
    }

    public boolean isPersistent() {
        return this.persistent;
    }

    public final boolean a(EntityHuman entityhuman, @Nullable ItemStack itemstack, EnumHand enumhand) {
        if (this.isLeashed() && this.getLeashHolder() == entityhuman) {
            // CraftBukkit start - fire PlayerUnleashEntityEvent
            if (CraftEventFactory.callPlayerUnleashEntityEvent(this, entityhuman).isCancelled()) {
                ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutAttachEntity(this, this.getLeashHolder()));
                return false;
            }
            // CraftBukkit end
            this.unleash(true, !entityhuman.abilities.canInstantlyBuild);
            return true;
        } else if (itemstack != null && itemstack.getItem() == Items.LEAD && this.a(entityhuman)) {
            // CraftBukkit start - fire PlayerLeashEntityEvent
            if (CraftEventFactory.callPlayerLeashEntityEvent(this, entityhuman, entityhuman).isCancelled()) {
                ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutAttachEntity(this, this.getLeashHolder()));
                return false;
            }
            // CraftBukkit end
            this.setLeashHolder(entityhuman, true);
            --itemstack.count;
            return true;
        } else {
            return this.a(entityhuman, enumhand, itemstack) ? true : super.a(entityhuman, itemstack, enumhand);
        }
    }

    protected boolean a(EntityHuman entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
        return false;
    }

    protected void cP() {
        if (this.bF != null) {
            this.cU();
        }

        if (this.bD) {
            if (!this.isAlive()) {
                this.world.getServer().getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), UnleashReason.PLAYER_UNLEASH)); // CraftBukkit
                this.unleash(true, true);
            }

            if (this.leashHolder == null || this.leashHolder.dead) {
                this.world.getServer().getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), UnleashReason.HOLDER_GONE)); // CraftBukkit
                this.unleash(true, true);
            }
        }
    }

    public void unleash(boolean flag, boolean flag1) {
        if (this.bD) {
            this.bD = false;
            this.leashHolder = null;
            if (!this.world.isClientSide && flag1) {
                this.forceDrops = true; // CraftBukkit
                this.a(Items.LEAD, 1);
                this.forceDrops = false; // CraftBukkit
            }

            if (!this.world.isClientSide && flag && this.world instanceof WorldServer) {
                ((WorldServer) this.world).getTracker().a((Entity) this, (Packet) (new PacketPlayOutAttachEntity(this, (Entity) null)));
            }
        }

    }

    public boolean a(EntityHuman entityhuman) {
        return !this.isLeashed() && !(this instanceof IMonster);
    }

    public boolean isLeashed() {
        return this.bD;
    }

    public Entity getLeashHolder() {
        return this.leashHolder;
    }

    public void setLeashHolder(Entity entity, boolean flag) {
        this.bD = true;
        this.leashHolder = entity;
        if (!this.world.isClientSide && flag && this.world instanceof WorldServer) {
            ((WorldServer) this.world).getTracker().a((Entity) this, (Packet) (new PacketPlayOutAttachEntity(this, this.leashHolder)));
        }

        if (this.isPassenger()) {
            this.stopRiding();
        }

    }

    public boolean a(Entity entity, boolean flag) {
        boolean flag1 = super.a(entity, flag);

        if (flag1 && this.isLeashed()) {
            this.unleash(true, true);
        }

        return flag1;
    }

    private void cU() {
        if (this.bD && this.bF != null) {
            if (this.bF.b("UUID")) {
                UUID uuid = this.bF.a("UUID");
                List list = this.world.a(EntityLiving.class, this.getBoundingBox().g(10.0D));
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    EntityLiving entityliving = (EntityLiving) iterator.next();

                    if (entityliving.getUniqueID().equals(uuid)) {
                        this.leashHolder = entityliving;
                        break;
                    }
                }
            } else if (this.bF.hasKeyOfType("X", 99) && this.bF.hasKeyOfType("Y", 99) && this.bF.hasKeyOfType("Z", 99)) {
                BlockPosition blockposition = new BlockPosition(this.bF.getInt("X"), this.bF.getInt("Y"), this.bF.getInt("Z"));
                EntityLeash entityleash = EntityLeash.b(this.world, blockposition);

                if (entityleash == null) {
                    entityleash = EntityLeash.a(this.world, blockposition);
                }

                this.leashHolder = entityleash;
            } else {
                this.world.getServer().getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), UnleashReason.UNKNOWN)); // CraftBukkit
                this.unleash(false, true);
            }
        }

        this.bF = null;
    }

    public boolean c(int i, @Nullable ItemStack itemstack) {
        EnumItemSlot enumitemslot;

        if (i == 98) {
            enumitemslot = EnumItemSlot.MAINHAND;
        } else if (i == 99) {
            enumitemslot = EnumItemSlot.OFFHAND;
        } else if (i == 100 + EnumItemSlot.HEAD.b()) {
            enumitemslot = EnumItemSlot.HEAD;
        } else if (i == 100 + EnumItemSlot.CHEST.b()) {
            enumitemslot = EnumItemSlot.CHEST;
        } else if (i == 100 + EnumItemSlot.LEGS.b()) {
            enumitemslot = EnumItemSlot.LEGS;
        } else {
            if (i != 100 + EnumItemSlot.FEET.b()) {
                return false;
            }

            enumitemslot = EnumItemSlot.FEET;
        }

        if (itemstack != null && !b(enumitemslot, itemstack) && enumitemslot != EnumItemSlot.HEAD) {
            return false;
        } else {
            this.setSlot(enumitemslot, itemstack);
            return true;
        }
    }

    public static boolean b(EnumItemSlot enumitemslot, ItemStack itemstack) {
        EnumItemSlot enumitemslot1 = d(itemstack);

        return enumitemslot1 == enumitemslot || enumitemslot1 == EnumItemSlot.MAINHAND && enumitemslot == EnumItemSlot.OFFHAND;
    }

    public boolean cp() {
        return super.cp() && !this.hasAI();
    }

    public void setAI(boolean flag) {
        byte b0 = ((Byte) this.datawatcher.get(EntityInsentient.a)).byteValue();

        this.datawatcher.set(EntityInsentient.a, Byte.valueOf(flag ? (byte) (b0 | 1) : (byte) (b0 & -2)));
    }

    public void n(boolean flag) {
        byte b0 = ((Byte) this.datawatcher.get(EntityInsentient.a)).byteValue();

        this.datawatcher.set(EntityInsentient.a, Byte.valueOf(flag ? (byte) (b0 | 2) : (byte) (b0 & -3)));
    }

    public boolean hasAI() {
        return (((Byte) this.datawatcher.get(EntityInsentient.a)).byteValue() & 1) != 0;
    }

    public boolean cT() {
        return (((Byte) this.datawatcher.get(EntityInsentient.a)).byteValue() & 2) != 0;
    }

    public EnumMainHand getMainHand() {
        return this.cT() ? EnumMainHand.LEFT : EnumMainHand.RIGHT;
    }

    static class SyntheticClass_1 {

        static final int[] a;
        static final int[] b = new int[EnumItemSlot.values().length];

        static {
            try {
                EntityInsentient.SyntheticClass_1.b[EnumItemSlot.HEAD.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                EntityInsentient.SyntheticClass_1.b[EnumItemSlot.CHEST.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                EntityInsentient.SyntheticClass_1.b[EnumItemSlot.LEGS.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                EntityInsentient.SyntheticClass_1.b[EnumItemSlot.FEET.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            a = new int[EnumItemSlot.Function.values().length];

            try {
                EntityInsentient.SyntheticClass_1.a[EnumItemSlot.Function.HAND.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                EntityInsentient.SyntheticClass_1.a[EnumItemSlot.Function.ARMOR.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

        }
    }

    public static enum EnumEntityPositionType {

        ON_GROUND, IN_AIR, IN_WATER;

        private EnumEntityPositionType() {}
    }
	
	public class navigation extends RecursiveAction {

     public navigation(){
     }

     @Override
     protected void compute() {
         navigation.l();
     }
 }
 
 public class targetSelector extends RecursiveAction {

     public targetSelector(){
     }

     @Override
     protected void compute() {
         try {
             targetSelector.a();
         } catch (Exception ex) {
             world.getServer().getLogger().log(java.util.logging.Level.WARNING, "Find no target.");
         }
     }
 }
}
