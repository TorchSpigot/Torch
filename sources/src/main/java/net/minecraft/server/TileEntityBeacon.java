package net.minecraft.server;

import com.google.common.collect.Lists;
import com.koloboke.collect.set.hash.HashObjSets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.entity.HumanEntity;
import org.bukkit.potion.PotionEffect;
// CraftBukkit end

// Paper start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Player;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
// Paper end

public class TileEntityBeacon extends TileEntityContainer implements ITickable, IWorldInventory {

    public static final MobEffectList[][] a = new MobEffectList[][] { { MobEffects.FASTER_MOVEMENT, MobEffects.FASTER_DIG}, { MobEffects.RESISTANCE, MobEffects.JUMP}, { MobEffects.INCREASE_DAMAGE}, { MobEffects.REGENERATION}};
    private static final Set<MobEffectList> f = HashObjSets.newMutableSet();
    private final List<TileEntityBeacon.BeaconColorTracker> g = Lists.newArrayList();
    private boolean j;
    public int levels = -1;
    @Nullable
    public MobEffectList primaryEffect;
    @Nullable
    public MobEffectList secondaryEffect;
    private ItemStack inventorySlot;
    private String o;
    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;

    @Override
    public List<ItemStack> getContents() {
        return Arrays.asList(this.inventorySlot);
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    public PotionEffect getPrimaryEffect() {
        return (this.primaryEffect != null) ? CraftPotionUtil.toBukkit(new MobEffect(this.primaryEffect, getLevel(), getAmplification(), true, true)) : null;
    }

    public PotionEffect getSecondaryEffect() {
        return (hasSecondaryEffect()) ? CraftPotionUtil.toBukkit(new MobEffect(this.secondaryEffect, getLevel(), getAmplification(), true, true)) : null;
    }
    // CraftBukkit end

    public TileEntityBeacon() {
        this.inventorySlot = ItemStack.a;
    }

    @Override
    public void F_() {
        if (this.world.getTime() % 80L == 0L) {
            this.n();
        }

    }

    public void n() {
        if (this.world != null) {
            this.checkStructure();
            this.applyEffects();
        }
    }

    // CraftBukkit start - split into components
    private byte getAmplification() {
        byte b0 = 0;

        if (this.levels >= 4 && this.primaryEffect == this.secondaryEffect) {
            b0 = 1;
        }

        return b0;
    }

    private int getLevel() {
        int i = (9 + this.levels * 2) * 20;
        return i;
    }

    public List<EntityHuman> getHumansInRange() {
        double d0 = this.levels * 10 + 10;

        int j = this.position.getX();
        int k = this.position.getY();
        int l = this.position.getZ();
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(j, k, l, j + 1, k + 1, l + 1)).g(d0).b(0.0D, this.world.getHeight(), 0.0D);
        List<EntityHuman> list = this.world.a(EntityHuman.class, axisalignedbb);

        return list;
    }

    // Paper - BeaconEffectEvent
    private void applyEffect(List<EntityHuman> list, MobEffectList effects, int i, int b0) {
        applyEffect(list, effects, i, b0, true);
    }

    private void applyEffect(List<EntityHuman> players, MobEffectList effects, int duration, int amplifier, boolean isPrimary) {
        EntityHuman eachPlayer;
        org.bukkit.block.Block block = world.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());
        PotionEffect effect = CraftPotionUtil.toBukkit(new MobEffect(effects, duration, amplifier, true, true));

        for (int i = 0, size = players.size(); i < size; i++) {
            eachPlayer = players.get(i);

            BeaconEffectEvent event = new BeaconEffectEvent(block, effect, (Player) eachPlayer.getBukkitEntity(), isPrimary);
            if (CraftEventFactory.callEvent(event).isCancelled()) continue;

            PotionEffect eventEffect = event.getEffect();
            eachPlayer.getBukkitEntity().addPotionEffect(eventEffect, true);
        }
    }
    // Paper end

    private boolean hasSecondaryEffect() {
        if (this.levels >= 4 && this.primaryEffect != this.secondaryEffect && this.secondaryEffect != null) {
            return true;
        }
        return false;
    }
    // CraftBukkit end

    // Torch start
    private void F() { checkStructure(); }
    private void checkStructure() {
        int prevLevels = this.levels;
        this.levels = 0;
        final int beaconX = this.position.getX();
        final int beaconY = this.position.getY();
        final int beaconZ = this.position.getZ();
        
        if (this.world.getHighestBlockYAt(this.position).getY() > beaconY) {
            return;
        }
        
        for (int level = 1; level <= 4; level++) {
            final int y = beaconY - level;

            if (y < 0) break;

            for (int x = beaconX - level; x <= beaconX + level; ++x) {
                for (int z = beaconZ - level; z <= beaconZ + level; ++z) {
                    if (!isValidBlock(new BlockPosition(x, y, z))) {
                        return;
                    }
                }
            }
            
            this.levels++;
        }
        
        if (this.levels == 4 && prevLevels < this.levels) {
            for (final EntityHuman entityhuman : this.world.a(EntityHuman.class, new AxisAlignedBB(beaconX, beaconY, beaconZ, beaconX, beaconY - 4, beaconZ).grow(10.0, 5.0, 10.0))) {
                entityhuman.b(AchievementList.K);
            }
        }
    }
    
    private boolean isValidBlock(BlockPosition position) {
        if (this.world.isLoaded(position)) {
            Block block = this.world.getType(position).getBlock();
            if (block == Blocks.EMERALD_BLOCK || block == Blocks.GOLD_BLOCK || block == Blocks.DIAMOND_BLOCK || block == Blocks.IRON_BLOCK) {
                return true;
            }
        }
        
        return false;
    }
    
    private void E() { applyEffects(); }
    public void applyEffects() {
        if (this.j && this.levels > 0 && this.primaryEffect != null) {
            byte amplifier = getAmplification();

            final int duration = getLevel();
            final List<EntityHuman> rangePlayers = getHumansInRange();

            applyEffect(rangePlayers, this.primaryEffect, duration, amplifier, true); // Paper - BeaconEffectEvent

            if (hasSecondaryEffect()) {
                applyEffect(rangePlayers, this.secondaryEffect, duration, 0, false); // Paper - BeaconEffectEvent
            }
        }
    }
    
    @Override
    @Nullable
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return new PacketPlayOutTileEntityData(this.position, 3, this.d());
    }

    @Override
    public NBTTagCompound d() {
        return this.save(new NBTTagCompound());
    }

    @Nullable
    private static MobEffectList f(int i) {
        MobEffectList mobeffectlist = MobEffectList.fromId(i);

        return TileEntityBeacon.f.contains(mobeffectlist) ? mobeffectlist : null;
    }

    @Override
    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.primaryEffect = f(nbttagcompound.getInt("Primary"));
        this.secondaryEffect = f(nbttagcompound.getInt("Secondary"));
        this.levels = nbttagcompound.getInt("Levels");
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        super.save(nbttagcompound);
        nbttagcompound.setInt("Primary", MobEffectList.getId(this.primaryEffect));
        nbttagcompound.setInt("Secondary", MobEffectList.getId(this.secondaryEffect));
        nbttagcompound.setInt("Levels", this.levels);
        return nbttagcompound;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean w_() {
        return this.inventorySlot.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return i == 0 ? this.inventorySlot : ItemStack.a;
    }

    @Override
    public ItemStack splitStack(int i, int j) {
        if (i == 0 && !this.inventorySlot.isEmpty()) {
            if (j >= this.inventorySlot.getCount()) {
                ItemStack itemstack = this.inventorySlot;

                this.inventorySlot = ItemStack.a;
                return itemstack;
            } else {
                return this.inventorySlot.cloneAndSubtract(j);
            }
        } else {
            return ItemStack.a;
        }
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        if (i == 0) {
            ItemStack itemstack = this.inventorySlot;

            this.inventorySlot = ItemStack.a;
            return itemstack;
        } else {
            return ItemStack.a;
        }
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        if (i == 0) {
            this.inventorySlot = itemstack;
        }

    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.o : "container.beacon";
    }

    @Override
    public boolean hasCustomName() {
        return this.o != null && !this.o.isEmpty();
    }

    public void a(String s) {
        this.o = s;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        return this.world.getTileEntity(this.position) != this ? false : entityhuman.d(this.position.getX() + 0.5D, this.position.getY() + 0.5D, this.position.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void startOpen(EntityHuman entityhuman) {}

    @Override
    public void closeContainer(EntityHuman entityhuman) {}

    @Override
    public boolean b(int i, ItemStack itemstack) {
        return itemstack.getItem() == Items.EMERALD || itemstack.getItem() == Items.DIAMOND || itemstack.getItem() == Items.GOLD_INGOT || itemstack.getItem() == Items.IRON_INGOT;
    }

    @Override
    public String getContainerName() {
        return "minecraft:beacon";
    }

    @Override
    public Container createContainer(PlayerInventory playerinventory, EntityHuman entityhuman) {
        return new ContainerBeacon(playerinventory, this);
    }

    @Override
    public int getProperty(int i) {
        switch (i) {
            case 0:
                return this.levels;

            case 1:
                return MobEffectList.getId(this.primaryEffect);

            case 2:
                return MobEffectList.getId(this.secondaryEffect);

            default:
                return 0;
        }
    }

    @Override
    public void setProperty(int i, int j) {
        switch (i) {
            case 0:
                this.levels = j;
                break;

            case 1:
                this.primaryEffect = f(j);
                break;

            case 2:
                this.secondaryEffect = f(j);
        }

    }

    @Override
    public int h() {
        return 3;
    }

    @Override
    public void clear() {
        this.inventorySlot = ItemStack.a;
    }

    @Override
    public boolean c(int i, int j) {
        if (i == 1) {
            this.n();
            return true;
        } else {
            return super.c(i, j);
        }
    }

    @Override
    public int[] getSlotsForFace(EnumDirection enumdirection) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemstack, EnumDirection enumdirection) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemstack, EnumDirection enumdirection) {
        return false;
    }

    static {
        MobEffectList[][] amobeffectlist = TileEntityBeacon.a;
        int i = amobeffectlist.length;

        for (int j = 0; j < i; ++j) {
            MobEffectList[] amobeffectlist1 = amobeffectlist[j];

            Collections.addAll(TileEntityBeacon.f, amobeffectlist1);
        }

    }

    public static class BeaconColorTracker {

        private final float[] a;
        private int b;

        public BeaconColorTracker(float[] afloat) {
            this.a = afloat;
            this.b = 1;
        }

        protected void a() {
            ++this.b;
        }

        public float[] b() {
            return this.a;
        }
    }
}
