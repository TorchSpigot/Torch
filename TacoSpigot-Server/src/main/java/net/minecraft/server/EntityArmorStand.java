package net.minecraft.server;

import com.google.common.base.Predicate;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import java.util.Iterator;
 import java.util.List;
import java.util.Queue;

// CraftBukkit start
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
// CraftBukkit end

public class EntityArmorStand extends EntityLiving {

    private static final Vector3f bq = new Vector3f(0.0F, 0.0F, 0.0F);
    private static final Vector3f br = new Vector3f(0.0F, 0.0F, 0.0F);
    private static final Vector3f bs = new Vector3f(-10.0F, 0.0F, -10.0F);
    private static final Vector3f bt = new Vector3f(-15.0F, 0.0F, 10.0F);
    private static final Vector3f bu = new Vector3f(-1.0F, 0.0F, -1.0F);
    private static final Vector3f bv = new Vector3f(1.0F, 0.0F, 1.0F);
    public static final DataWatcherObject<Byte> a = DataWatcher.a(EntityArmorStand.class, DataWatcherRegistry.a);
    public static final DataWatcherObject<Vector3f> b = DataWatcher.a(EntityArmorStand.class, DataWatcherRegistry.i);
    public static final DataWatcherObject<Vector3f> c = DataWatcher.a(EntityArmorStand.class, DataWatcherRegistry.i);
    public static final DataWatcherObject<Vector3f> d = DataWatcher.a(EntityArmorStand.class, DataWatcherRegistry.i);
    public static final DataWatcherObject<Vector3f> e = DataWatcher.a(EntityArmorStand.class, DataWatcherRegistry.i);
    public static final DataWatcherObject<Vector3f> f = DataWatcher.a(EntityArmorStand.class, DataWatcherRegistry.i);
    public static final DataWatcherObject<Vector3f> g = DataWatcher.a(EntityArmorStand.class, DataWatcherRegistry.i);
    private static final Predicate<Entity> bw = new Predicate() {
        public boolean a(@Nullable Entity entity) {
            return entity instanceof EntityMinecartAbstract && ((EntityMinecartAbstract) entity).v() == EntityMinecartAbstract.EnumMinecartType.RIDEABLE;
        }

        public boolean apply(Object object) {
            return this.a((Entity) object);
        }
    };
    private final ItemStack[] bx;
    private final ItemStack[] by;
    private boolean bz;
    public long h;
    private int bA;
    private boolean bB;
    public Vector3f headPose;
    public Vector3f bodyPose;
    public Vector3f leftArmPose;
    public Vector3f rightArmPose;
    public Vector3f leftLegPose;
    public Vector3f rightLegPose;

    public EntityArmorStand(World world) {
        super(world);
        this.bx = new ItemStack[2];
        this.by = new ItemStack[4];
        this.headPose = EntityArmorStand.bq;
        this.bodyPose = EntityArmorStand.br;
        this.leftArmPose = EntityArmorStand.bs;
        this.rightArmPose = EntityArmorStand.bt;
        this.leftLegPose = EntityArmorStand.bu;
        this.rightLegPose = EntityArmorStand.bv;
        this.noclip = this.hasGravity();
        this.setSize(0.5F, 1.975F);
    }

    public EntityArmorStand(World world, double d0, double d1, double d2) {
        this(world);
        this.setPosition(d0, d1, d2);
    }

    public boolean cp() {
        return super.cp() && !this.hasGravity();
    }

    protected void i() {
        super.i();
        this.datawatcher.register(EntityArmorStand.a, Byte.valueOf((byte) 0));
        this.datawatcher.register(EntityArmorStand.b, EntityArmorStand.bq);
        this.datawatcher.register(EntityArmorStand.c, EntityArmorStand.br);
        this.datawatcher.register(EntityArmorStand.d, EntityArmorStand.bs);
        this.datawatcher.register(EntityArmorStand.e, EntityArmorStand.bt);
        this.datawatcher.register(EntityArmorStand.f, EntityArmorStand.bu);
        this.datawatcher.register(EntityArmorStand.g, EntityArmorStand.bv);
    }

    public Iterable<ItemStack> aE() {
        return Arrays.asList(this.bx);
    }

    public Iterable<ItemStack> getArmorItems() {
        return Arrays.asList(this.by);
    }

    @Nullable
    public ItemStack getEquipment(EnumItemSlot enumitemslot) {
        ItemStack itemstack = null;

        switch (EntityArmorStand.SyntheticClass_1.a[enumitemslot.a().ordinal()]) {
        case 1:
            itemstack = this.bx[enumitemslot.b()];
            break;

        case 2:
            itemstack = this.by[enumitemslot.b()];
        }

        return itemstack;
    }

    public void setSlot(EnumItemSlot enumitemslot, @Nullable ItemStack itemstack) {
        switch (EntityArmorStand.SyntheticClass_1.a[enumitemslot.a().ordinal()]) {
        case 1:
            this.a_(itemstack);
            this.bx[enumitemslot.b()] = itemstack;
            break;

        case 2:
            this.a_(itemstack);
            this.by[enumitemslot.b()] = itemstack;
        }

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

        if (itemstack != null && !EntityInsentient.b(enumitemslot, itemstack) && enumitemslot != EnumItemSlot.HEAD) {
            return false;
        } else {
            this.setSlot(enumitemslot, itemstack);
            return true;
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.by.length; ++i) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            if (this.by[i] != null) {
                this.by[i].save(nbttagcompound1);
            }

            nbttaglist.add(nbttagcompound1);
        }

        nbttagcompound.set("ArmorItems", nbttaglist);
        NBTTagList nbttaglist1 = new NBTTagList();

        for (int j = 0; j < this.bx.length; ++j) {
            NBTTagCompound nbttagcompound2 = new NBTTagCompound();

            if (this.bx[j] != null) {
                this.bx[j].save(nbttagcompound2);
            }

            nbttaglist1.add(nbttagcompound2);
        }

        nbttagcompound.set("HandItems", nbttaglist1);
        if (this.getCustomNameVisible() && (this.getCustomName() == null || this.getCustomName().isEmpty())) {
            nbttagcompound.setBoolean("CustomNameVisible", this.getCustomNameVisible());
        }

        nbttagcompound.setBoolean("Invisible", this.isInvisible());
        nbttagcompound.setBoolean("Small", this.isSmall());
        nbttagcompound.setBoolean("ShowArms", this.hasArms());
        nbttagcompound.setInt("DisabledSlots", this.bA);
        nbttagcompound.setBoolean("NoGravity", this.hasGravity());
        nbttagcompound.setBoolean("NoBasePlate", this.hasBasePlate());
        if (this.isMarker()) {
            nbttagcompound.setBoolean("Marker", this.isMarker());
        }

        nbttagcompound.set("Pose", this.D());
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        NBTTagList nbttaglist;
        int i;

        if (nbttagcompound.hasKeyOfType("ArmorItems", 9)) {
            nbttaglist = nbttagcompound.getList("ArmorItems", 10);

            for (i = 0; i < this.by.length; ++i) {
                this.by[i] = ItemStack.createStack(nbttaglist.get(i));
            }
        }

        if (nbttagcompound.hasKeyOfType("HandItems", 9)) {
            nbttaglist = nbttagcompound.getList("HandItems", 10);

            for (i = 0; i < this.bx.length; ++i) {
                this.bx[i] = ItemStack.createStack(nbttaglist.get(i));
            }
        }

        this.setInvisible(nbttagcompound.getBoolean("Invisible"));
        this.setSmall(nbttagcompound.getBoolean("Small"));
        this.setArms(nbttagcompound.getBoolean("ShowArms"));
        this.bA = nbttagcompound.getInt("DisabledSlots");
        this.setGravity(nbttagcompound.getBoolean("NoGravity"));
        this.setBasePlate(nbttagcompound.getBoolean("NoBasePlate"));
        this.setMarker(nbttagcompound.getBoolean("Marker"));
        this.bB = !this.isMarker();
        this.noclip = this.hasGravity();
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Pose");

        this.g(nbttagcompound1);
    }

    private void g(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = nbttagcompound.getList("Head", 5);

        this.setHeadPose(nbttaglist.isEmpty() ? EntityArmorStand.bq : new Vector3f(nbttaglist));
        NBTTagList nbttaglist1 = nbttagcompound.getList("Body", 5);

        this.setBodyPose(nbttaglist1.isEmpty() ? EntityArmorStand.br : new Vector3f(nbttaglist1));
        NBTTagList nbttaglist2 = nbttagcompound.getList("LeftArm", 5);

        this.setLeftArmPose(nbttaglist2.isEmpty() ? EntityArmorStand.bs : new Vector3f(nbttaglist2));
        NBTTagList nbttaglist3 = nbttagcompound.getList("RightArm", 5);

        this.setRightArmPose(nbttaglist3.isEmpty() ? EntityArmorStand.bt : new Vector3f(nbttaglist3));
        NBTTagList nbttaglist4 = nbttagcompound.getList("LeftLeg", 5);

        this.setLeftLegPose(nbttaglist4.isEmpty() ? EntityArmorStand.bu : new Vector3f(nbttaglist4));
        NBTTagList nbttaglist5 = nbttagcompound.getList("RightLeg", 5);

        this.setRightLegPose(nbttaglist5.isEmpty() ? EntityArmorStand.bv : new Vector3f(nbttaglist5));
    }

    private NBTTagCompound D() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        if (!EntityArmorStand.bq.equals(this.headPose)) {
            nbttagcompound.set("Head", this.headPose.a());
        }

        if (!EntityArmorStand.br.equals(this.bodyPose)) {
            nbttagcompound.set("Body", this.bodyPose.a());
        }

        if (!EntityArmorStand.bs.equals(this.leftArmPose)) {
            nbttagcompound.set("LeftArm", this.leftArmPose.a());
        }

        if (!EntityArmorStand.bt.equals(this.rightArmPose)) {
            nbttagcompound.set("RightArm", this.rightArmPose.a());
        }

        if (!EntityArmorStand.bu.equals(this.leftLegPose)) {
            nbttagcompound.set("LeftLeg", this.leftLegPose.a());
        }

        if (!EntityArmorStand.bv.equals(this.rightLegPose)) {
            nbttagcompound.set("RightLeg", this.rightLegPose.a());
        }

        return nbttagcompound;
    }

    public boolean isCollidable() {
        return false;
    }

    protected void C(Entity entity) {}

    protected void co() {
        List list = this.world.getEntities(this, this.getBoundingBox(), EntityArmorStand.bw);
		
		Iterator it = list.iterator();
        while (it.hasNext()) {
            Entity entity = (Entity) it.next();
 
            if (this.h(entity) <= 0.2D) {
                entity.collide(this);
            }
        }


    }

    public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, @Nullable ItemStack itemstack, EnumHand enumhand) {
        if (this.isMarker()) {
            return EnumInteractionResult.PASS;
        } else if (!this.world.isClientSide && !entityhuman.isSpectator()) {
            EnumItemSlot enumitemslot = EnumItemSlot.MAINHAND;
            boolean flag = itemstack != null;
            Item item = flag ? itemstack.getItem() : null;

            if (flag && item instanceof ItemArmor) {
                enumitemslot = ((ItemArmor) item).c;
            }

            if (flag && (item == Items.SKULL || item == Item.getItemOf(Blocks.PUMPKIN))) {
                enumitemslot = EnumItemSlot.HEAD;
            }

            double d0 = 0.1D;
            double d1 = 0.9D;
            double d2 = 0.4D;
            double d3 = 1.6D;
            EnumItemSlot enumitemslot1 = EnumItemSlot.MAINHAND;
            boolean flag1 = this.isSmall();
            double d4 = flag1 ? vec3d.y * 2.0D : vec3d.y;

            if (d4 >= 0.1D && d4 < 0.1D + (flag1 ? 0.8D : 0.45D) && this.getEquipment(EnumItemSlot.FEET) != null) {
                enumitemslot1 = EnumItemSlot.FEET;
            } else if (d4 >= 0.9D + (flag1 ? 0.3D : 0.0D) && d4 < 0.9D + (flag1 ? 1.0D : 0.7D) && this.getEquipment(EnumItemSlot.CHEST) != null) {
                enumitemslot1 = EnumItemSlot.CHEST;
            } else if (d4 >= 0.4D && d4 < 0.4D + (flag1 ? 1.0D : 0.8D) && this.getEquipment(EnumItemSlot.LEGS) != null) {
                enumitemslot1 = EnumItemSlot.LEGS;
            } else if (d4 >= 1.6D && this.getEquipment(EnumItemSlot.HEAD) != null) {
                enumitemslot1 = EnumItemSlot.HEAD;
            }

            boolean flag2 = this.getEquipment(enumitemslot1) != null;

            if (this.b(enumitemslot1) || this.b(enumitemslot)) {
                enumitemslot1 = enumitemslot;
                if (this.b(enumitemslot)) {
                    return EnumInteractionResult.FAIL;
                }
            }

            if (flag && enumitemslot == EnumItemSlot.MAINHAND && !this.hasArms()) {
                return EnumInteractionResult.FAIL;
            } else {
                if (flag) {
                    this.a(entityhuman, enumitemslot, itemstack, enumhand);
                } else if (flag2) {
                    this.a(entityhuman, enumitemslot1, itemstack, enumhand);
                }

                return EnumInteractionResult.SUCCESS;
            }
        } else {
            return EnumInteractionResult.SUCCESS;
        }
    }

    private boolean b(EnumItemSlot enumitemslot) {
        return (this.bA & 1 << enumitemslot.c()) != 0;
    }

    private void a(EntityHuman entityhuman, EnumItemSlot enumitemslot, @Nullable ItemStack itemstack, EnumHand enumhand) {
        ItemStack itemstack1 = this.getEquipment(enumitemslot);

        if (itemstack1 == null || (this.bA & 1 << enumitemslot.c() + 8) == 0) {
            if (itemstack1 != null || (this.bA & 1 << enumitemslot.c() + 16) == 0) {
                ItemStack itemstack2;
                // CraftBukkit start
                org.bukkit.inventory.ItemStack armorStandItem = CraftItemStack.asCraftMirror(itemstack1);
                org.bukkit.inventory.ItemStack playerHeldItem = CraftItemStack.asCraftMirror(itemstack);

                Player player = (Player) entityhuman.getBukkitEntity();
                ArmorStand self = (ArmorStand) this.getBukkitEntity();

                EquipmentSlot slot = CraftEquipmentSlot.getSlot(enumitemslot);
                PlayerArmorStandManipulateEvent armorStandManipulateEvent = new PlayerArmorStandManipulateEvent(player,self,playerHeldItem,armorStandItem,slot);
                this.world.getServer().getPluginManager().callEvent(armorStandManipulateEvent);

                if (armorStandManipulateEvent.isCancelled()) {
                    return;
                }
                // CraftBukkit end

                if (entityhuman.abilities.canInstantlyBuild && (itemstack1 == null || itemstack1.getItem() == Item.getItemOf(Blocks.AIR)) && itemstack != null) {
                    itemstack2 = itemstack.cloneItemStack();
                    itemstack2.count = 1;
                    this.setSlot(enumitemslot, itemstack2);
                } else if (itemstack != null && itemstack.count > 1) {
                    if (itemstack1 == null) {
                        itemstack2 = itemstack.cloneItemStack();
                        itemstack2.count = 1;
                        this.setSlot(enumitemslot, itemstack2);
                        --itemstack.count;
                    }
                } else {
                    this.setSlot(enumitemslot, itemstack);
                    entityhuman.a(enumhand, itemstack1);
                }
            }
        }
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        // CraftBukkit start
        if (org.bukkit.craftbukkit.event.CraftEventFactory.handleNonLivingEntityDamageEvent(this, damagesource, f)) {
            return false;
        }
        // CraftBukkit end
        if (!this.world.isClientSide && !this.dead) {
            if (DamageSource.OUT_OF_WORLD.equals(damagesource)) {
                this.die();
                return false;
            } else if (!this.isInvulnerable(damagesource) && !this.bz && !this.isMarker()) {
                if (damagesource.isExplosion()) {
                    this.I();
                    this.die();
                    return false;
                } else if (DamageSource.FIRE.equals(damagesource)) {
                    if (this.isBurning()) {
                        this.a(0.15F);
                    } else {
                        this.setOnFire(5);
                    }

                    return false;
                } else if (DamageSource.BURN.equals(damagesource) && this.getHealth() > 0.5F) {
                    this.a(4.0F);
                    return false;
                } else {
                    boolean flag = "arrow".equals(damagesource.p());
                    boolean flag1 = "player".equals(damagesource.p());

                    if (!flag1 && !flag) {
                        return false;
                    } else {
                        if (damagesource.i() instanceof EntityArrow) {
                            damagesource.i().die();
                        }

                        if (damagesource.getEntity() instanceof EntityHuman && !((EntityHuman) damagesource.getEntity()).abilities.mayBuild) {
                            return false;
                        } else if (damagesource.u()) {
                            this.E();
                            this.die();
                            return false;
                        } else {
                            long i = this.world.getTime();

                            if (i - this.h > 5L && !flag) {
                                this.world.broadcastEntityEffect(this, (byte) 32);
                                this.h = i;
                            } else {
                                this.G();
                                this.E();
                                this.die();
                            }

                            return false;
                        }
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void E() {
        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).a(EnumParticle.BLOCK_DUST, this.locX, this.locY + (double) this.length / 1.5D, this.locZ, 10, (double) (this.width / 4.0F), (double) (this.length / 4.0F), (double) (this.width / 4.0F), 0.05D, new int[] { Block.getCombinedId(Blocks.PLANKS.getBlockData())});
        }

    }

    private void a(float f) {
        float f1 = this.getHealth();

        f1 -= f;
        if (f1 <= 0.5F) {
            this.I();
            this.die();
        } else {
            this.setHealth(f1);
        }

    }

    private void G() {
        Block.a(this.world, new BlockPosition(this), new ItemStack(Items.ARMOR_STAND));
        this.I();
    }

    private void I() {
        this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.j, this.bA(), 1.0F, 1.0F);

        int i;

        for (i = 0; i < this.bx.length; ++i) {
            if (this.bx[i] != null && this.bx[i].count > 0) {
                if (this.bx[i] != null) {
                    Block.a(this.world, (new BlockPosition(this)).up(), this.bx[i]);
                }

                this.bx[i] = null;
            }
        }

        for (i = 0; i < this.by.length; ++i) {
            if (this.by[i] != null && this.by[i].count > 0) {
                if (this.by[i] != null) {
                    Block.a(this.world, (new BlockPosition(this)).up(), this.by[i]);
                }

                this.by[i] = null;
            }
        }

    }

    protected float h(float f, float f1) {
        this.aO = this.lastYaw;
        this.aN = this.yaw;
        return 0.0F;
    }

    public float getHeadHeight() {
        return this.isBaby() ? this.length * 0.5F : this.length * 0.9F;
    }

    public double ax() {
        return this.isMarker() ? 0.0D : 0.10000000149011612D;
    }

    public void g(float f, float f1) {
        if (!this.hasGravity()) {
            super.g(f, f1);
        }
    }

    public void m() {
        super.m();
        Vector3f vector3f = (Vector3f) this.datawatcher.get(EntityArmorStand.b);

        if (!this.headPose.equals(vector3f)) {
            this.setHeadPose(vector3f);
        }

        Vector3f vector3f1 = (Vector3f) this.datawatcher.get(EntityArmorStand.c);

        if (!this.bodyPose.equals(vector3f1)) {
            this.setBodyPose(vector3f1);
        }

        Vector3f vector3f2 = (Vector3f) this.datawatcher.get(EntityArmorStand.d);

        if (!this.leftArmPose.equals(vector3f2)) {
            this.setLeftArmPose(vector3f2);
        }

        Vector3f vector3f3 = (Vector3f) this.datawatcher.get(EntityArmorStand.e);

        if (!this.rightArmPose.equals(vector3f3)) {
            this.setRightArmPose(vector3f3);
        }

        Vector3f vector3f4 = (Vector3f) this.datawatcher.get(EntityArmorStand.f);

        if (!this.leftLegPose.equals(vector3f4)) {
            this.setLeftLegPose(vector3f4);
        }

        Vector3f vector3f5 = (Vector3f) this.datawatcher.get(EntityArmorStand.g);

        if (!this.rightLegPose.equals(vector3f5)) {
            this.setRightLegPose(vector3f5);
        }

        boolean flag = this.isMarker();

        if (!this.bB && flag) {
            this.a(false);
            this.i = false;
        } else {
            if (!this.bB || flag) {
                return;
            }

            this.a(true);
            this.i = true;
        }

        this.bB = flag;
    }

    private void a(boolean flag) {
        double d0 = this.locX;
        double d1 = this.locY;
        double d2 = this.locZ;

        if (flag) {
            this.setSize(0.5F, 1.975F);
        } else {
            this.setSize(0.0F, 0.0F);
        }

        this.setPosition(d0, d1, d2);
    }

    protected void F() {
        this.setInvisible(this.bz);
    }

    public void setInvisible(boolean flag) {
        this.bz = flag;
        super.setInvisible(flag);
    }

    public boolean isBaby() {
        return this.isSmall();
    }

    public void Q() {
        this.die();
    }

    public boolean br() {
        return this.isInvisible();
    }

    public void setSmall(boolean flag) {
        this.datawatcher.set(EntityArmorStand.a, Byte.valueOf(this.a(((Byte) this.datawatcher.get(EntityArmorStand.a)).byteValue(), 1, flag)));
    }

    public boolean isSmall() {
        return (((Byte) this.datawatcher.get(EntityArmorStand.a)).byteValue() & 1) != 0;
    }

    public void setGravity(boolean flag) {
        this.datawatcher.set(EntityArmorStand.a, Byte.valueOf(this.a(((Byte) this.datawatcher.get(EntityArmorStand.a)).byteValue(), 2, flag)));
    }

    public boolean hasGravity() {
        return (((Byte) this.datawatcher.get(EntityArmorStand.a)).byteValue() & 2) != 0;
    }

    public void setArms(boolean flag) {
        this.datawatcher.set(EntityArmorStand.a, Byte.valueOf(this.a(((Byte) this.datawatcher.get(EntityArmorStand.a)).byteValue(), 4, flag)));
    }

    public boolean hasArms() {
        return (((Byte) this.datawatcher.get(EntityArmorStand.a)).byteValue() & 4) != 0;
    }

    public void setBasePlate(boolean flag) {
        this.datawatcher.set(EntityArmorStand.a, Byte.valueOf(this.a(((Byte) this.datawatcher.get(EntityArmorStand.a)).byteValue(), 8, flag)));
    }

    public boolean hasBasePlate() {
        return (((Byte) this.datawatcher.get(EntityArmorStand.a)).byteValue() & 8) != 0;
    }

    public void setMarker(boolean flag) {
        this.datawatcher.set(EntityArmorStand.a, Byte.valueOf(this.a(((Byte) this.datawatcher.get(EntityArmorStand.a)).byteValue(), 16, flag)));
    }

    public boolean isMarker() {
        return (((Byte) this.datawatcher.get(EntityArmorStand.a)).byteValue() & 16) != 0;
    }

    private byte a(byte b0, int i, boolean flag) {
        if (flag) {
            b0 = (byte) (b0 | i);
        } else {
            b0 = (byte) (b0 & ~i);
        }

        return b0;
    }

    public void setHeadPose(Vector3f vector3f) {
        this.headPose = vector3f;
        this.datawatcher.set(EntityArmorStand.b, vector3f);
    }

    public void setBodyPose(Vector3f vector3f) {
        this.bodyPose = vector3f;
        this.datawatcher.set(EntityArmorStand.c, vector3f);
    }

    public void setLeftArmPose(Vector3f vector3f) {
        this.leftArmPose = vector3f;
        this.datawatcher.set(EntityArmorStand.d, vector3f);
    }

    public void setRightArmPose(Vector3f vector3f) {
        this.rightArmPose = vector3f;
        this.datawatcher.set(EntityArmorStand.e, vector3f);
    }

    public void setLeftLegPose(Vector3f vector3f) {
        this.leftLegPose = vector3f;
        this.datawatcher.set(EntityArmorStand.f, vector3f);
    }

    public void setRightLegPose(Vector3f vector3f) {
        this.rightLegPose = vector3f;
        this.datawatcher.set(EntityArmorStand.g, vector3f);
    }

    public Vector3f w() {
        return this.headPose;
    }

    public Vector3f x() {
        return this.bodyPose;
    }

    public boolean isInteractable() {
        return super.isInteractable() && !this.isMarker();
    }

    public EnumMainHand getMainHand() {
        return EnumMainHand.RIGHT;
    }

    protected SoundEffect e(int i) {
        return SoundEffects.k;
    }

    @Nullable
    protected SoundEffect bS() {
        return SoundEffects.l;
    }

    @Nullable
    protected SoundEffect bT() {
        return SoundEffects.j;
    }

    public void onLightningStrike(EntityLightning entitylightning) {}

    public boolean cE() {
        return false;
    }

    // TacoSpigot start - add an option to make armor stands not move
    @Override
    public void move(double motX, double motY, double motZ) {
        if (getWorld().tacoSpigotConfig.optimizeArmorStandMovement) return;
        super.move(motX, motY, motZ);
    }
    // TacoSpigot end

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumItemSlot.Function.values().length];

        static {
            try {
                EntityArmorStand.SyntheticClass_1.a[EnumItemSlot.Function.HAND.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                EntityArmorStand.SyntheticClass_1.a[EnumItemSlot.Function.ARMOR.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

        }
    }
}
