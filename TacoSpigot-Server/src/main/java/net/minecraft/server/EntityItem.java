package net.minecraft.server;

import com.google.common.base.Optional;
import java.util.Iterator;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.player.PlayerPickupItemEvent; // CraftBukkit
import com.destroystokyo.paper.HopperPusher; // Paper

// Paper start - implement HopperPusher
public class EntityItem extends Entity implements HopperPusher {
    @Override
    public boolean acceptItem(TileEntityHopper hopper) {
        return TileEntityHopper.a(hopper, this);
    }
// Paper end

    private static final Logger b = LogManager.getLogger();
    private static final DataWatcherObject<Optional<ItemStack>> c = DataWatcher.a(EntityItem.class, DataWatcherRegistry.f);
    private int age;
    public int pickupDelay;
    private int f;
    private String g;
    private String h;
    public float a;
    private int lastTick = MinecraftServer.currentTick; // CraftBukkit

    public EntityItem(World world, double d0, double d1, double d2) {
        super(world);
        this.f = 5;
        this.a = (float) (Math.random() * 3.141592653589793D * 2.0D);
        this.setSize(0.25F, 0.25F);
        this.setPosition(d0, d1, d2);
        this.yaw = (float) (Math.random() * 360.0D);
        this.motX = (double) ((float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D));
        this.motY = 0.20000000298023224D;
        this.motZ = (double) ((float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D));
    }

    public EntityItem(World world, double d0, double d1, double d2, ItemStack itemstack) {
        this(world, d0, d1, d2);
        // CraftBukkit start - Can't set null items in the datawatcher
        if (itemstack == null || itemstack.getItem() == null) {
            return;
        }
        // CraftBukkit end
        this.setItemStack(itemstack);
    }

    static int uglyHack = 1; // Paper - MC-99914 - ensure EntityItem loads before EntityPotion
    protected boolean playStepSound() {
        return false;
    }

    public EntityItem(World world) {
        super(world);
        this.f = 5;
        this.a = (float) (Math.random() * 3.141592653589793D * 2.0D);
        this.setSize(0.25F, 0.25F);
        this.setItemStack(new ItemStack(Blocks.AIR, 0));
    }

    protected void i() {
        this.getDataWatcher().register(EntityItem.c, Optional.absent());
    }

    public void m() {
        if (this.getItemStack() == null) {
            this.die();
        } else {
            super.m();
            if (tryPutInHopper()) return; // Paper
            // CraftBukkit start - Use wall time for pickup and despawn timers
            int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
            if (this.pickupDelay != 32767) this.pickupDelay -= elapsedTicks;
            if (this.age != -32768) this.age += elapsedTicks;
            this.lastTick = MinecraftServer.currentTick;
            // CraftBukkit end

            this.lastX = this.locX;
            this.lastY = this.locY;
            this.lastZ = this.locZ;
            this.motY -= 0.03999999910593033D;
            this.noclip = this.j(this.locX, (this.getBoundingBox().b + this.getBoundingBox().e) / 2.0D, this.locZ);
            this.move(this.motX, this.motY, this.motZ);
            boolean flag = (int) this.lastX != (int) this.locX || (int) this.lastY != (int) this.locY || (int) this.lastZ != (int) this.locZ;

            if (flag || this.ticksLived % 25 == 0) {
                if (this.world.getType(new BlockPosition(this)).getMaterial() == Material.LAVA) {
                    this.motY = 0.20000000298023224D;
                    this.motX = (double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                    this.motZ = (double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                    this.a(SoundEffects.bA, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
                }

                if (!this.world.isClientSide) {
                    this.x();
                }
            }

            float f = 0.98F;

            if (this.onGround) {
                f = this.world.getType(new BlockPosition(MathHelper.floor(this.locX), MathHelper.floor(this.getBoundingBox().b) - 1, MathHelper.floor(this.locZ))).getBlock().frictionFactor * 0.98F;
            }

            this.motX *= (double) f;
            this.motY *= 0.9800000190734863D;
            this.motZ *= (double) f;
            if (this.onGround) {
                this.motY *= -0.5D;
            }

            /* Craftbukkit start - moved up
            if (this.age != -32768) {
                ++this.age;
            }
            // Craftbukkit end */

            this.aj();
            if (!this.world.isClientSide && this.age >= world.spigotConfig.itemDespawnRate) { // Spigot
                // CraftBukkit start - fire ItemDespawnEvent
                if (org.bukkit.craftbukkit.event.CraftEventFactory.callItemDespawnEvent(this).isCancelled()) {
                    this.age = 0;
                    return;
                }
                // CraftBukkit end
                this.die();
            }

        }
    }

    // Spigot start - copied from above
    @Override
    public void inactiveTick() {
        if (tryPutInHopper()) return; // TacoSpigot
        // CraftBukkit start - Use wall time for pickup and despawn timers
        int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
        if (this.pickupDelay != 32767) this.pickupDelay -= elapsedTicks;
        if (this.age != -32768) this.age += elapsedTicks;
        this.lastTick = MinecraftServer.currentTick;
        // CraftBukkit end

        if (!this.world.isClientSide && this.age >= world.spigotConfig.itemDespawnRate) { // Spigot
            // CraftBukkit start - fire ItemDespawnEvent
            if (org.bukkit.craftbukkit.event.CraftEventFactory.callItemDespawnEvent(this).isCancelled()) {
                this.age = 0;
                return;
            }
            // CraftBukkit end
            this.die();
        }
    }
    // Spigot end

    private void x() {
        // Spigot start
        double radius = world.spigotConfig.itemMerge;
        Iterator iterator = this.world.a(EntityItem.class, this.getBoundingBox().grow(radius, radius, radius)).iterator();
        // Spigot end

        while (iterator.hasNext()) {
            EntityItem entityitem = (EntityItem) iterator.next();

            this.a(entityitem);
        }

    }

    private boolean a(EntityItem entityitem) {
        if (entityitem == this) {
            return false;
        } else if (entityitem.isAlive() && this.isAlive()) {
            ItemStack itemstack = this.getItemStack();
            ItemStack itemstack1 = entityitem.getItemStack();

            if (this.pickupDelay != 32767 && entityitem.pickupDelay != 32767) {
                if (this.age != -32768 && entityitem.age != -32768) {
                    if (itemstack1.getItem() != itemstack.getItem()) {
                        return false;
                    } else if (itemstack1.hasTag() ^ itemstack.hasTag()) {
                        return false;
                    } else if (itemstack1.hasTag() && !itemstack1.getTag().equals(itemstack.getTag())) {
                        return false;
                    } else if (itemstack1.getItem() == null) {
                        return false;
                    } else if (itemstack1.getItem().k() && itemstack1.getData() != itemstack.getData()) {
                        return false;
                    } else if (itemstack1.count < itemstack.count) {
                        return entityitem.a(this);
                    } else if (itemstack1.count + itemstack.count > itemstack1.getMaxStackSize()) {
                        return false;
                    } else {
                        // Spigot start
                        if (org.bukkit.craftbukkit.event.CraftEventFactory.callItemMergeEvent(entityitem, this).isCancelled()) return false; // CraftBukkit
                        itemstack.count += itemstack1.count;
                        this.pickupDelay = Math.max(entityitem.pickupDelay, this.pickupDelay);
                        this.age = Math.min(entityitem.age, this.age);
                        this.setItemStack(itemstack);
                        entityitem.die();
                        // Spigot end
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void j() {
        this.age = 4800;
    }

    public boolean aj() {
        if (this.world.a(this.getBoundingBox(), Material.WATER, (Entity) this)) {
            if (!this.inWater && !this.justCreated) {
                this.ak();
            }

            this.inWater = true;
        } else {
            this.inWater = false;
        }

        return this.inWater;
    }

    protected void burn(int i) {
        this.damageEntity(DamageSource.FIRE, (float) i);
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else if (this.getItemStack() != null && this.getItemStack().getItem() == Items.NETHER_STAR && damagesource.isExplosion()) {
            return false;
        } else {
            // CraftBukkit start
            if (org.bukkit.craftbukkit.event.CraftEventFactory.handleNonLivingEntityDamageEvent(this, damagesource, f)) {
                return false;
            }
            // CraftBukkit end
            this.ao();
            this.f = (int) ((float) this.f - f);
            if (this.f <= 0) {
                this.die();
            }

            return false;
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setShort("Health", (short) this.f);
        nbttagcompound.setShort("Age", (short) this.age);
        nbttagcompound.setShort("PickupDelay", (short) this.pickupDelay);
        if (this.n() != null) {
            nbttagcompound.setString("Thrower", this.g);
        }

        if (this.l() != null) {
            nbttagcompound.setString("Owner", this.h);
        }

        if (this.getItemStack() != null) {
            nbttagcompound.set("Item", this.getItemStack().save(new NBTTagCompound()));
        }

    }

    public void a(NBTTagCompound nbttagcompound) {
        this.f = nbttagcompound.getShort("Health");
        this.age = nbttagcompound.getShort("Age");
        if (nbttagcompound.hasKey("PickupDelay")) {
            this.pickupDelay = nbttagcompound.getShort("PickupDelay");
        }

        if (nbttagcompound.hasKey("Owner")) {
            this.h = nbttagcompound.getString("Owner");
        }

        if (nbttagcompound.hasKey("Thrower")) {
            this.g = nbttagcompound.getString("Thrower");
        }

        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Item");

        // CraftBukkit start - Handle missing "Item" compounds
        if (nbttagcompound1 != null) {
            ItemStack itemstack = ItemStack.createStack(nbttagcompound1);
            if (itemstack != null) {
                this.setItemStack(itemstack);
            } else {
                this.die();
            }
        } else {
            this.die();
        }
        // CraftBukkit end
        if (this.getItemStack() == null) {
            this.die();
        }

    }

    public void d(EntityHuman entityhuman) {
        if (!this.world.isClientSide) {
            ItemStack itemstack = this.getItemStack();
            int i = itemstack.count;

            // CraftBukkit start - fire PlayerPickupItemEvent
            int canHold = entityhuman.inventory.canHold(itemstack);
            int remaining = itemstack.count - canHold;

            if (this.pickupDelay <= 0 && canHold > 0) {
                itemstack.count = canHold;
                PlayerPickupItemEvent event = new PlayerPickupItemEvent((org.bukkit.entity.Player) entityhuman.getBukkitEntity(), (org.bukkit.entity.Item) this.getBukkitEntity(), remaining);
                // event.setCancelled(!entityhuman.canPickUpLoot); TODO
                this.world.getServer().getPluginManager().callEvent(event);
                itemstack.count = canHold + remaining;

                if (event.isCancelled()) {
                    return;
                }

                // Possibly < 0; fix here so we do not have to modify code below
                this.pickupDelay = 0;
            }
            // CraftBukkit end

            if (this.pickupDelay == 0 && (this.h == null || 6000 - this.age <= 200 || this.h.equals(entityhuman.getName())) && entityhuman.inventory.pickup(itemstack)) {
                if (itemstack.getItem() == Item.getItemOf(Blocks.LOG)) {
                    entityhuman.b((Statistic) AchievementList.g);
                }

                if (itemstack.getItem() == Item.getItemOf(Blocks.LOG2)) {
                    entityhuman.b((Statistic) AchievementList.g);
                }

                if (itemstack.getItem() == Items.LEATHER) {
                    entityhuman.b((Statistic) AchievementList.t);
                }

                if (itemstack.getItem() == Items.DIAMOND) {
                    entityhuman.b((Statistic) AchievementList.w);
                }

                if (itemstack.getItem() == Items.BLAZE_ROD) {
                    entityhuman.b((Statistic) AchievementList.A);
                }

                if (itemstack.getItem() == Items.DIAMOND && this.n() != null) {
                    EntityHuman entityhuman1 = this.world.a(this.n());

                    if (entityhuman1 != null && entityhuman1 != entityhuman) {
                        entityhuman1.b((Statistic) AchievementList.x);
                    }
                }

                if (!this.ad()) {
                    this.world.a((EntityHuman) null, entityhuman.locX, entityhuman.locY, entityhuman.locZ, SoundEffects.cV, SoundCategory.PLAYERS, 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }

                entityhuman.receive(this, i);
                if (itemstack.count <= 0) {
                    this.die();
                }

                entityhuman.a(StatisticList.d(itemstack.getItem()), i);
            }

        }
    }

    public String getName() {
        return this.hasCustomName() ? this.getCustomName() : LocaleI18n.get("item." + this.getItemStack().a());
    }

    public boolean aT() {
        return false;
    }

    @Nullable
    public Entity c(int i) {
        Entity entity = super.c(i);

        if (!this.world.isClientSide && entity instanceof EntityItem) {
            ((EntityItem) entity).x();
        }

        return entity;
    }

    public ItemStack getItemStack() {
        ItemStack itemstack = (ItemStack) ((Optional) this.getDataWatcher().get(EntityItem.c)).orNull();

        if (itemstack == null) {
            if (this.world != null) {
                EntityItem.b.error("Item entity " + this.getId() + " has no item?!");
            }

            return new ItemStack(Blocks.STONE);
        } else {
            return itemstack;
        }
    }

    public void setItemStack(@Nullable ItemStack itemstack) {
        this.getDataWatcher().set(EntityItem.c, Optional.fromNullable(itemstack));
        this.getDataWatcher().markDirty(EntityItem.c);
    }

    public String l() {
        return this.h;
    }

    public void d(String s) {
        this.h = s;
    }

    public String n() {
        return this.g;
    }

    public void e(String s) {
        this.g = s;
    }

    public void q() {
        this.pickupDelay = 10;
    }

    public void r() {
        this.pickupDelay = 0;
    }

    public void s() {
        this.pickupDelay = 32767;
    }

    public void a(int i) {
        this.pickupDelay = i;
    }

    public boolean t() {
        return this.pickupDelay > 0;
    }

    public void v() {
        this.age = -6000;
    }

    public void w() {
        this.s();
        this.age = 5999;
    }
}
