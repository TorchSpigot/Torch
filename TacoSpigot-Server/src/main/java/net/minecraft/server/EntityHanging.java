package net.minecraft.server;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;

// CraftBukkit start
import org.bukkit.entity.Hanging;
import org.bukkit.event.hanging.HangingBreakEvent;
// CraftBukkit end

public abstract class EntityHanging extends Entity {

    private static final Predicate<Entity> c = new Predicate() {
        public boolean a(@Nullable Entity entity) {
            return entity instanceof EntityHanging;
        }

        public boolean apply(Object object) {
            return this.a((Entity) object);
        }
    };
    private int d;
    public BlockPosition blockPosition;
    @Nullable
    public EnumDirection direction;

    public EntityHanging(World world) {
        super(world);
        this.setSize(0.5F, 0.5F);
    }

    public EntityHanging(World world, BlockPosition blockposition) {
        this(world);
        this.blockPosition = blockposition;
    }

    protected void i() {}

    public void setDirection(EnumDirection enumdirection) {
        Validate.notNull(enumdirection);
        Validate.isTrue(enumdirection.k().c());
        this.direction = enumdirection;
        this.lastYaw = this.yaw = (float) (this.direction.get2DRotationValue() * 90);
        this.updateBoundingBox();
    }

    /* CraftBukkit start - bounding box calculation made static (for spawn usage)

        l is from function l()
        m is from function m()

        Placing here as it's more likely to be noticed as something which needs to be updated
        then something in a CraftBukkit file.
     */
    public static AxisAlignedBB calculateBoundingBox(Entity entity, BlockPosition blockPosition, EnumDirection direction, int width, int height) {
            double d0 = (double) blockPosition.getX() + 0.5D;
            double d1 = (double) blockPosition.getY() + 0.5D;
            double d2 = (double) blockPosition.getZ() + 0.5D;
            double d3 = 0.46875D;
            double d4 = a(width);
            double d5 = a(height);

            d0 -= (double) direction.getAdjacentX() * 0.46875D;
            d2 -= (double) direction.getAdjacentZ() * 0.46875D;
            d1 += d5;
            EnumDirection enumdirection = direction.f();

            d0 += d4 * (double) enumdirection.getAdjacentX();
            d2 += d4 * (double) enumdirection.getAdjacentZ();
        if (entity != null) {
            entity.locX = d0;
            entity.locY = d1;
            entity.locZ = d2;
        }
            double d6 = (double) width;
            double d7 = (double) height;
            double d8 = (double) width;

            if (direction.k() == EnumDirection.EnumAxis.Z) {
                d8 = 1.0D;
            } else {
                d6 = 1.0D;
            }

            d6 /= 32.0D;
            d7 /= 32.0D;
            d8 /= 32.0D;
            return new AxisAlignedBB(d0 - d6, d1 - d7, d2 - d8, d0 + d6, d1 + d7, d2 + d8);
    }

    protected void updateBoundingBox() {
        if (this.direction != null) {
            // CraftBukkit start code moved in to calculateBoundingBox
            this.a(calculateBoundingBox(this, this.blockPosition, this.direction, this.getWidth(), this.getHeight()));
            // CraftBukkit end
        }
    }

    private static double a(int i) {
        return i % 32 == 0 ? 0.5D : 0.0D;
    }

    public void m() {
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        if (this.d++ == this.world.spigotConfig.hangingTickFrequency && !this.world.isClientSide) { // Spigot
            this.d = 0;
            if (!this.dead && !this.survives()) {
                // CraftBukkit start - fire break events
                Material material = this.world.getType(new BlockPosition(this)).getMaterial();
                HangingBreakEvent.RemoveCause cause;

                if (!material.equals(Material.AIR)) {
                    // TODO: This feels insufficient to catch 100% of suffocation cases
                    cause = HangingBreakEvent.RemoveCause.OBSTRUCTION;
                } else {
                    cause = HangingBreakEvent.RemoveCause.PHYSICS;
                }

                HangingBreakEvent event = new HangingBreakEvent((Hanging) this.getBukkitEntity(), cause);
                this.world.getServer().getPluginManager().callEvent(event);

                if (dead || event.isCancelled()) {
                    return;
                }
                // CraftBukkit end
                this.die();
                this.a((Entity) null);
            }
        }

    }

    public boolean survives() {
        if (!this.world.getCubes(this, this.getBoundingBox()).isEmpty()) {
            return false;
        } else {
            int i = Math.max(1, this.getWidth() / 16);
            int j = Math.max(1, this.getHeight() / 16);
            BlockPosition blockposition = this.blockPosition.shift(this.direction.opposite());
            EnumDirection enumdirection = this.direction.f();

            for (int k = 0; k < i; ++k) {
                for (int l = 0; l < j; ++l) {
                    int i1 = i > 2 ? -1 : 0;
                    int j1 = j > 2 ? -1 : 0;
                    BlockPosition blockposition1 = blockposition.shift(enumdirection, k + i1).up(l + j1);
                    IBlockData iblockdata = this.world.getType(blockposition1);

                    if (!iblockdata.getMaterial().isBuildable() && !BlockDiodeAbstract.isDiode(iblockdata)) {
                        return false;
                    }
                }
            }

            return this.world.getEntities(this, this.getBoundingBox(), EntityHanging.c).isEmpty();
        }
    }

    public boolean isInteractable() {
        return true;
    }

    public boolean t(Entity entity) {
        return entity instanceof EntityHuman ? this.damageEntity(DamageSource.playerAttack((EntityHuman) entity), 0.0F) : false;
    }

    public EnumDirection getDirection() {
        return this.direction;
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else {
            if (!this.dead && !this.world.isClientSide) {
                // CraftBukkit start - fire break events
                HangingBreakEvent event = new HangingBreakEvent((Hanging) this.getBukkitEntity(), HangingBreakEvent.RemoveCause.DEFAULT);
                if (damagesource.getEntity() != null) {
                    event = new org.bukkit.event.hanging.HangingBreakByEntityEvent((Hanging) this.getBukkitEntity(), damagesource.getEntity() == null ? null : damagesource.getEntity().getBukkitEntity());
                } else if (damagesource.isExplosion()) {
                    event = new HangingBreakEvent((Hanging) this.getBukkitEntity(), HangingBreakEvent.RemoveCause.EXPLOSION);
                }

                this.world.getServer().getPluginManager().callEvent(event);

                if (this.dead || event.isCancelled()) {
                    return true;
                }
                // CraftBukkit end

                this.die();
                this.ao();
                this.a(damagesource.getEntity());
            }

            return true;
        }
    }

    public void move(double d0, double d1, double d2) {
        if (!this.world.isClientSide && !this.dead && d0 * d0 + d1 * d1 + d2 * d2 > 0.0D) {
            if (this.dead) return; // CraftBukkit

            // CraftBukkit start - fire break events
            // TODO - Does this need its own cause? Seems to only be triggered by pistons
            HangingBreakEvent event = new HangingBreakEvent((Hanging) this.getBukkitEntity(), HangingBreakEvent.RemoveCause.PHYSICS);
            this.world.getServer().getPluginManager().callEvent(event);

            if (this.dead || event.isCancelled()) {
                return;
            }
            // CraftBukkit end

            this.die();
            this.a((Entity) null);
        }

    }

    public void g(double d0, double d1, double d2) {
        if (false && !this.world.isClientSide && !this.dead && d0 * d0 + d1 * d1 + d2 * d2 > 0.0D) { // CraftBukkit - not needed
            this.die();
            this.a((Entity) null);
        }

    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setByte("Facing", (byte) this.direction.get2DRotationValue());
        BlockPosition blockposition = this.getBlockPosition();

        nbttagcompound.setInt("TileX", blockposition.getX());
        nbttagcompound.setInt("TileY", blockposition.getY());
        nbttagcompound.setInt("TileZ", blockposition.getZ());
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.blockPosition = new BlockPosition(nbttagcompound.getInt("TileX"), nbttagcompound.getInt("TileY"), nbttagcompound.getInt("TileZ"));
        this.setDirection(EnumDirection.fromType2(nbttagcompound.getByte("Facing")));
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void a(@Nullable Entity entity);

    public abstract void o();

    public EntityItem a(ItemStack itemstack, float f) {
        EntityItem entityitem = new EntityItem(this.world, this.locX + (double) ((float) this.direction.getAdjacentX() * 0.15F), this.locY + (double) f, this.locZ + (double) ((float) this.direction.getAdjacentZ() * 0.15F), itemstack);

        entityitem.q();
        this.world.addEntity(entityitem);
        return entityitem;
    }

    protected boolean ar() {
        return false;
    }

    public void setPosition(double d0, double d1, double d2) {
        this.blockPosition = new BlockPosition(d0, d1, d2);
        this.updateBoundingBox();
        this.impulse = true;
    }

    public BlockPosition getBlockPosition() {
        return this.blockPosition;
    }

    public float a(EnumBlockRotation enumblockrotation) {
        if (this.direction != null && this.direction.k() != EnumDirection.EnumAxis.Y) {
            switch (EntityHanging.SyntheticClass_1.a[enumblockrotation.ordinal()]) {
            case 1:
                this.direction = this.direction.opposite();
                break;

            case 2:
                this.direction = this.direction.f();
                break;

            case 3:
                this.direction = this.direction.e();
            }
        }

        return super.a(enumblockrotation);
    }

    public float a(EnumBlockMirror enumblockmirror) {
        return this.a(enumblockmirror.a(this.direction));
    }

    public void onLightningStrike(EntityLightning entitylightning) {}

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumBlockRotation.values().length];

        static {
            try {
                EntityHanging.SyntheticClass_1.a[EnumBlockRotation.CLOCKWISE_180.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                EntityHanging.SyntheticClass_1.a[EnumBlockRotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                EntityHanging.SyntheticClass_1.a[EnumBlockRotation.CLOCKWISE_90.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

        }
    }
}
