package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.koloboke.collect.set.hash.HashObjSets;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
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
            this.F();
            this.E();
        }

    }

    // CraftBukkit start - split into components
    private byte getAmplification() {
        {
            byte b0 = 0;

            if (this.levels >= 4 && this.primaryEffect == this.secondaryEffect) {
                b0 = 1;
            }

            return b0;
        }
    }

    private int getLevel() {
        {
            int i = (9 + this.levels * 2) * 20;
            return i;
        }
    }

    public List getHumansInRange() {
        {
            double d0 = this.levels * 10 + 10;

            int j = this.position.getX();
            int k = this.position.getY();
            int l = this.position.getZ();
            AxisAlignedBB axisalignedbb = (new AxisAlignedBB(j, k, l, j + 1, k + 1, l + 1)).g(d0).b(0.0D, this.world.getHeight(), 0.0D);
            List list = this.world.a(EntityHuman.class, axisalignedbb);

            return list;
        }
    }

    private void applyEffect(List list, MobEffectList effects, int i, int b0) {
        // Paper - BeaconEffectEvent
        applyEffect(list, effects, i, b0, true);
    }

    private void applyEffect(List list, MobEffectList effects, int i, int b0, boolean isPrimary) {
        // Paper - BeaconEffectEvent
        {
            Iterator iterator = list.iterator();

            EntityHuman entityhuman;

            // Paper start - BeaconEffectEvent
            org.bukkit.block.Block block = world.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());
            PotionEffect effect = CraftPotionUtil.toBukkit(new MobEffect(effects, i, b0, true, true));
            // Paper end

            while (iterator.hasNext()) {
                entityhuman = (EntityHuman) iterator.next();
                // Paper start - BeaconEffectEvent
                BeaconEffectEvent event = new BeaconEffectEvent(block, effect, (Player) entityhuman.getBukkitEntity(), isPrimary);
                if (CraftEventFactory.callEvent(event).isCancelled()) continue;
                PotionEffect eventEffect = event.getEffect();
                entityhuman.getBukkitEntity().addPotionEffect(eventEffect, true);
                // Paper end
            }
        }
    }

    private boolean hasSecondaryEffect() {
        {
            if (this.levels >= 4 && this.primaryEffect != this.secondaryEffect && this.secondaryEffect != null) {
                return true;
            }

            return false;
        }
    }

    private void E() {
        if (this.j && this.levels > 0 && !this.world.isClientSide && this.primaryEffect != null) {
            byte b0 = getAmplification();

            int i = getLevel();
            List list = getHumansInRange();

            applyEffect(list, this.primaryEffect, i, b0, true); // Paper - BeaconEffectEvent

            if (hasSecondaryEffect()) {
                applyEffect(list, this.secondaryEffect, i, 0, false); // Paper - BeaconEffectEvent
            }
        }

    }
    // CraftBukkit end

    private void F() {
        int i = this.levels;
        int j = this.position.getX();
        int k = this.position.getY();
        int l = this.position.getZ();

        this.levels = 0;
        this.g.clear();
        this.j = true;
        TileEntityBeacon.BeaconColorTracker tileentitybeacon_beaconcolortracker = new TileEntityBeacon.BeaconColorTracker(EntitySheep.a(EnumColor.WHITE));

        this.g.add(tileentitybeacon_beaconcolortracker);
        boolean flag = true;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

        int i1;

        for (i1 = k + 1; i1 < 256; ++i1) {
            IBlockData iblockdata = this.world.getType(blockposition_mutableblockposition.c(j, i1, l));
            float[] afloat;

            if (iblockdata.getBlock() == Blocks.STAINED_GLASS) {
                afloat = EntitySheep.a(iblockdata.get(BlockStainedGlass.COLOR));
            } else {
                if (iblockdata.getBlock() != Blocks.STAINED_GLASS_PANE) {
                    if (iblockdata.c() >= 15 && iblockdata.getBlock() != Blocks.BEDROCK) {
                        this.j = false;
                        this.g.clear();
                        break;
                    }

                    tileentitybeacon_beaconcolortracker.a();
                    continue;
                }

                afloat = EntitySheep.a(iblockdata.get(BlockStainedGlassPane.COLOR));
            }

            if (!flag) {
                afloat = new float[] { (tileentitybeacon_beaconcolortracker.b()[0] + afloat[0]) / 2.0F, (tileentitybeacon_beaconcolortracker.b()[1] + afloat[1]) / 2.0F, (tileentitybeacon_beaconcolortracker.b()[2] + afloat[2]) / 2.0F};
            }

            if (Arrays.equals(afloat, tileentitybeacon_beaconcolortracker.b())) {
                tileentitybeacon_beaconcolortracker.a();
            } else {
                tileentitybeacon_beaconcolortracker = new TileEntityBeacon.BeaconColorTracker(afloat);
                this.g.add(tileentitybeacon_beaconcolortracker);
            }

            flag = false;
        }

        if (this.j) {
            for (i1 = 1; i1 <= 4; this.levels = i1++) {
                int j1 = k - i1;

                if (j1 < 0) {
                    break;
                }

                boolean flag1 = true;

                for (int k1 = j - i1; k1 <= j + i1 && flag1; ++k1) {
                    for (int l1 = l - i1; l1 <= l + i1; ++l1) {
                        Block block = this.world.getType(new BlockPosition(k1, j1, l1)).getBlock();

                        if (block != Blocks.EMERALD_BLOCK && block != Blocks.GOLD_BLOCK && block != Blocks.DIAMOND_BLOCK && block != Blocks.IRON_BLOCK) {
                            flag1 = false;
                            break;
                        }
                    }
                }

                if (!flag1) {
                    break;
                }
            }

            if (this.levels == 0) {
                this.j = false;
            }
        }

        if (!this.world.isClientSide && this.levels == 4 && i < this.levels) {
            Iterator iterator = this.world.a(EntityHuman.class, (new AxisAlignedBB(j, k, l, j, k - 4, l)).grow(10.0D, 5.0D, 10.0D)).iterator();

            while (iterator.hasNext()) {
                EntityHuman entityhuman = (EntityHuman) iterator.next();

                entityhuman.b(AchievementList.K);
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
