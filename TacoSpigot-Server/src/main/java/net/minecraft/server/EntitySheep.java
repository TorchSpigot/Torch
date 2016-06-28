package net.minecraft.server;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;

// CraftBukkit start
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.InventoryView;
// CraftBukkit end

public class EntitySheep extends EntityAnimal {

    private static final DataWatcherObject<Byte> bw = DataWatcher.a(EntitySheep.class, DataWatcherRegistry.a);
    private final InventoryCrafting container = new InventoryCrafting(new Container() {
        public boolean a(EntityHuman entityhuman) {
            return false;
        }

        // CraftBukkit start
        @Override
        public InventoryView getBukkitView() {
            return null; // TODO: O.O
        }
        // CraftBukkit end
    }, 2, 1);
    private static final Map<EnumColor, float[]> by = Maps.newEnumMap(EnumColor.class);
    private int bA;
    private PathfinderGoalEatTile bB;

    public static float[] a(EnumColor enumcolor) {
        return (float[]) EntitySheep.by.get(enumcolor);
    }

    public EntitySheep(World world) {
        super(world);
        this.setSize(0.9F, 1.3F);
        this.container.setItem(0, new ItemStack(Items.DYE));
        this.container.setItem(1, new ItemStack(Items.DYE));
        this.container.resultInventory = new InventoryCraftResult(); // CraftBukkit - add result slot for event
    }

    protected void r() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalPanic(this, 1.25D));
        this.goalSelector.a(2, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.a(3, new PathfinderGoalTempt(this, 1.1D, Items.WHEAT, false));
        this.goalSelector.a(4, new PathfinderGoalFollowParent(this, 1.1D));
        this.goalSelector.a(5, this.bB = new PathfinderGoalEatTile(this));
        this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
    }

    protected void M() {
        this.bA = this.bB.f();
        super.M();
    }

    public void n() {
        if (this.world.isClientSide) {
            this.bA = Math.max(0, this.bA - 1);
        }

        super.n();
    }

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(8.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.23000000417232513D);
    }

    protected void i() {
        super.i();
        this.datawatcher.register(EntitySheep.bw, Byte.valueOf((byte) 0));
    }

    @Nullable
    protected MinecraftKey J() {
        if (this.isSheared()) {
            return LootTables.L;
        } else {
            switch (EntitySheep.SyntheticClass_1.a[this.getColor().ordinal()]) {
            case 1:
            default:
                return LootTables.M;

            case 2:
                return LootTables.N;

            case 3:
                return LootTables.O;

            case 4:
                return LootTables.P;

            case 5:
                return LootTables.Q;

            case 6:
                return LootTables.R;

            case 7:
                return LootTables.S;

            case 8:
                return LootTables.T;

            case 9:
                return LootTables.U;

            case 10:
                return LootTables.V;

            case 11:
                return LootTables.W;

            case 12:
                return LootTables.X;

            case 13:
                return LootTables.Y;

            case 14:
                return LootTables.Z;

            case 15:
                return LootTables.aa;

            case 16:
                return LootTables.ab;
            }
        }
    }

    public boolean a(EntityHuman entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
        if (itemstack != null && itemstack.getItem() == Items.SHEARS && !this.isSheared() && !this.isBaby()) {
            if (!this.world.isClientSide) {
                // CraftBukkit start
                PlayerShearEntityEvent event = new PlayerShearEntityEvent((org.bukkit.entity.Player) entityhuman.getBukkitEntity(), this.getBukkitEntity());
                this.world.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return false;
                }
                // CraftBukkit end

                this.setSheared(true);
                int i = 1 + this.random.nextInt(3);

                for (int j = 0; j < i; ++j) {
                    this.forceDrops = true; // CraftBukkit
                    EntityItem entityitem = this.a(new ItemStack(Item.getItemOf(Blocks.WOOL), 1, this.getColor().getColorIndex()), 1.0F);
                    this.forceDrops = false; // CraftBukkit

                    entityitem.motY += (double) (this.random.nextFloat() * 0.05F);
                    entityitem.motX += (double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.1F);
                    entityitem.motZ += (double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.1F);
                }
            }

            itemstack.damage(1, entityhuman);
            this.a(SoundEffects.eP, 1.0F, 1.0F);
        }

        return super.a(entityhuman, enumhand, itemstack);
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setBoolean("Sheared", this.isSheared());
        nbttagcompound.setByte("Color", (byte) this.getColor().getColorIndex());
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.setSheared(nbttagcompound.getBoolean("Sheared"));
        this.setColor(EnumColor.fromColorIndex(nbttagcompound.getByte("Color")));
    }

    protected SoundEffect G() {
        return SoundEffects.eM;
    }

    protected SoundEffect bS() {
        return SoundEffects.eO;
    }

    protected SoundEffect bT() {
        return SoundEffects.eN;
    }

    protected void a(BlockPosition blockposition, Block block) {
        this.a(SoundEffects.eQ, 0.15F, 1.0F);
    }

    public EnumColor getColor() {
        return EnumColor.fromColorIndex(((Byte) this.datawatcher.get(EntitySheep.bw)).byteValue() & 15);
    }

    public void setColor(EnumColor enumcolor) {
        byte b0 = ((Byte) this.datawatcher.get(EntitySheep.bw)).byteValue();

        this.datawatcher.set(EntitySheep.bw, Byte.valueOf((byte) (b0 & 240 | enumcolor.getColorIndex() & 15)));
    }

    public boolean isSheared() {
        return (((Byte) this.datawatcher.get(EntitySheep.bw)).byteValue() & 16) != 0;
    }

    public void setSheared(boolean flag) {
        byte b0 = ((Byte) this.datawatcher.get(EntitySheep.bw)).byteValue();

        if (flag) {
            this.datawatcher.set(EntitySheep.bw, Byte.valueOf((byte) (b0 | 16)));
        } else {
            this.datawatcher.set(EntitySheep.bw, Byte.valueOf((byte) (b0 & -17)));
        }

    }

    public static EnumColor a(Random random) {
        int i = random.nextInt(100);

        return i < 5 ? EnumColor.BLACK : (i < 10 ? EnumColor.GRAY : (i < 15 ? EnumColor.SILVER : (i < 18 ? EnumColor.BROWN : (random.nextInt(500) == 0 ? EnumColor.PINK : EnumColor.WHITE))));
    }

    public EntitySheep b(EntityAgeable entityageable) {
        EntitySheep entitysheep = (EntitySheep) entityageable;
        EntitySheep entitysheep1 = new EntitySheep(this.world);

        entitysheep1.setColor(this.a((EntityAnimal) this, (EntityAnimal) entitysheep));
        return entitysheep1;
    }

    public void B() {
        // CraftBukkit start
        SheepRegrowWoolEvent event = new SheepRegrowWoolEvent((org.bukkit.entity.Sheep) this.getBukkitEntity());
        this.world.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;
        // CraftBukkit end
        this.setSheared(false);
        if (this.isBaby()) {
            this.setAge(60);
        }

    }

    @Nullable
    public GroupDataEntity prepare(DifficultyDamageScaler difficultydamagescaler, @Nullable GroupDataEntity groupdataentity) {
        groupdataentity = super.prepare(difficultydamagescaler, groupdataentity);
        this.setColor(a(this.world.random));
        return groupdataentity;
    }

    private EnumColor a(EntityAnimal entityanimal, EntityAnimal entityanimal1) {
        int i = ((EntitySheep) entityanimal).getColor().getInvColorIndex();
        int j = ((EntitySheep) entityanimal1).getColor().getInvColorIndex();

        this.container.getItem(0).setData(i);
        this.container.getItem(1).setData(j);
        ItemStack itemstack = CraftingManager.getInstance().craft(this.container, ((EntitySheep) entityanimal).world);
        int k;

        if (itemstack != null && itemstack.getItem() == Items.DYE) {
            k = itemstack.getData();
        } else {
            k = this.world.random.nextBoolean() ? i : j;
        }

        return EnumColor.fromInvColorIndex(k);
    }

    public float getHeadHeight() {
        return 0.95F * this.length;
    }

    public EntityAgeable createChild(EntityAgeable entityageable) {
        return this.b(entityageable);
    }

    static {
        EntitySheep.by.put(EnumColor.WHITE, new float[] { 1.0F, 1.0F, 1.0F});
        EntitySheep.by.put(EnumColor.ORANGE, new float[] { 0.85F, 0.5F, 0.2F});
        EntitySheep.by.put(EnumColor.MAGENTA, new float[] { 0.7F, 0.3F, 0.85F});
        EntitySheep.by.put(EnumColor.LIGHT_BLUE, new float[] { 0.4F, 0.6F, 0.85F});
        EntitySheep.by.put(EnumColor.YELLOW, new float[] { 0.9F, 0.9F, 0.2F});
        EntitySheep.by.put(EnumColor.LIME, new float[] { 0.5F, 0.8F, 0.1F});
        EntitySheep.by.put(EnumColor.PINK, new float[] { 0.95F, 0.5F, 0.65F});
        EntitySheep.by.put(EnumColor.GRAY, new float[] { 0.3F, 0.3F, 0.3F});
        EntitySheep.by.put(EnumColor.SILVER, new float[] { 0.6F, 0.6F, 0.6F});
        EntitySheep.by.put(EnumColor.CYAN, new float[] { 0.3F, 0.5F, 0.6F});
        EntitySheep.by.put(EnumColor.PURPLE, new float[] { 0.5F, 0.25F, 0.7F});
        EntitySheep.by.put(EnumColor.BLUE, new float[] { 0.2F, 0.3F, 0.7F});
        EntitySheep.by.put(EnumColor.BROWN, new float[] { 0.4F, 0.3F, 0.2F});
        EntitySheep.by.put(EnumColor.GREEN, new float[] { 0.4F, 0.5F, 0.2F});
        EntitySheep.by.put(EnumColor.RED, new float[] { 0.6F, 0.2F, 0.2F});
        EntitySheep.by.put(EnumColor.BLACK, new float[] { 0.1F, 0.1F, 0.1F});
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumColor.values().length];

        static {
            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.WHITE.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.ORANGE.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.MAGENTA.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.LIGHT_BLUE.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.YELLOW.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.LIME.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.PINK.ordinal()] = 7;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.GRAY.ordinal()] = 8;
            } catch (NoSuchFieldError nosuchfielderror7) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.SILVER.ordinal()] = 9;
            } catch (NoSuchFieldError nosuchfielderror8) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.CYAN.ordinal()] = 10;
            } catch (NoSuchFieldError nosuchfielderror9) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.PURPLE.ordinal()] = 11;
            } catch (NoSuchFieldError nosuchfielderror10) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.BLUE.ordinal()] = 12;
            } catch (NoSuchFieldError nosuchfielderror11) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.BROWN.ordinal()] = 13;
            } catch (NoSuchFieldError nosuchfielderror12) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.GREEN.ordinal()] = 14;
            } catch (NoSuchFieldError nosuchfielderror13) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.RED.ordinal()] = 15;
            } catch (NoSuchFieldError nosuchfielderror14) {
                ;
            }

            try {
                EntitySheep.SyntheticClass_1.a[EnumColor.BLACK.ordinal()] = 16;
            } catch (NoSuchFieldError nosuchfielderror15) {
                ;
            }

        }
    }
}
