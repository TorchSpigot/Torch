package net.minecraft.server;

import java.util.UUID;

// CraftBukkit start
import org.bukkit.event.entity.EntityUnleashEvent;
// CraftBukkit end

public abstract class EntityCreature extends EntityInsentient {

    public static final UUID bu = UUID.fromString("E199AD21-BA8A-4C53-8D13-6182D5C69D3A");
    public static final AttributeModifier bv = (new AttributeModifier(EntityCreature.bu, "Fleeing speed bonus", 2.0D, 2)).a(false);
    private BlockPosition a;
    private float b;
    private PathfinderGoal c;
    private boolean bw;
    private float bx;

    public EntityCreature(World world) {
        super(world);
        this.a = BlockPosition.ZERO;
        this.b = -1.0F;
        this.bx = PathType.WATER.a();
        this.c = new PathfinderGoalMoveTowardsRestriction(this, 1.0D);
    }

    public float a(BlockPosition blockposition) {
        return 0.0F;
    }

    public boolean cG() {
        return super.cG() && this.a(new BlockPosition(this.locX, this.getBoundingBox().b, this.locZ)) >= 0.0F;
    }

    public boolean cU() {
        return !this.navigation.n();
    }

    public boolean cV() {
        return this.f(new BlockPosition(this));
    }

    public boolean f(BlockPosition blockposition) {
        return this.b == -1.0F ? true : this.a.n(blockposition) < (double) (this.b * this.b);
    }

    public void a(BlockPosition blockposition, int i) {
        this.a = blockposition;
        this.b = (float) i;
    }

    public BlockPosition cW() {
        return this.a;
    }

    public float cX() {
        return this.b;
    }

    public void cY() {
        this.b = -1.0F;
    }

    public boolean cZ() {
        return this.b != -1.0F;
    }

    protected void cP() {
        super.cP();
        if (this.isLeashed() && this.getLeashHolder() != null && this.getLeashHolder().world == this.world) {
            Entity entity = this.getLeashHolder();

            this.a(new BlockPosition((int) entity.locX, (int) entity.locY, (int) entity.locZ), 5);
            float f = this.g(entity);

            if (this instanceof EntityTameableAnimal && ((EntityTameableAnimal) this).isSitting()) {
                if (f > 10.0F) {
                    this.world.getServer().getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), EntityUnleashEvent.UnleashReason.DISTANCE)); // CraftBukkit
                    this.unleash(true, true);
                }

                return;
            }

            if (!this.bw) {
                this.goalSelector.a(2, this.c);
                if (this.getNavigation() instanceof Navigation) {
                    this.bx = this.a(PathType.WATER);
                    this.a(PathType.WATER, 0.0F);
                }

                this.bw = true;
            }

            this.q(f);
            if (f > 4.0F) {
                this.getNavigation().a(entity, 1.0D);
            }

            if (f > 6.0F) {
                double d0 = (entity.locX - this.locX) / (double) f;
                double d1 = (entity.locY - this.locY) / (double) f;
                double d2 = (entity.locZ - this.locZ) / (double) f;

                this.motX += d0 * Math.abs(d0) * 0.4D;
                this.motY += d1 * Math.abs(d1) * 0.4D;
                this.motZ += d2 * Math.abs(d2) * 0.4D;
            }

            if (f > 10.0F) {
                this.world.getServer().getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), EntityUnleashEvent.UnleashReason.DISTANCE)); // CraftBukkit
                this.unleash(true, true);
            }
        } else if (!this.isLeashed() && this.bw) {
            this.bw = false;
            this.goalSelector.a(this.c);
            if (this.getNavigation() instanceof Navigation) {
                this.a(PathType.WATER, this.bx);
            }

            this.cY();
        }

    }

    protected void q(float f) {}
}
