package org.bukkit.event.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Called when an Entity targets a {@link LivingEntity} and can only target
 * LivingEntity's.
 */
public class EntityTargetLivingEntityEvent extends EntityTargetEvent {
    public EntityTargetLivingEntityEvent(final Entity entity, final LivingEntity target, final TargetReason reason) {
        super(entity, target, reason);
    }
    
    // Torch start
    private static EntityTargetLivingEntityEvent instance;
    
    public static EntityTargetLivingEntityEvent requestMutable(final Entity entity, final LivingEntity target, final TargetReason reason) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Async request mutable event!");
        
        if (instance == null) {
            instance = new EntityTargetLivingEntityEvent(entity, target, reason);
            return instance;
        }
        
        instance.entity = entity;
        instance.target = target;
        instance.reason = reason;
        
        return instance;
    }
    // Torch end

    @Override
    public LivingEntity getTarget() {
        return (LivingEntity) super.getTarget();
    }

    /**
     * Set the Entity that you want the mob to target.
     * <p>
     * It is possible to be null, null will cause the entity to be
     * target-less.
     * <p>
     * Must be a LivingEntity, or null.
     *
     * @param target The entity to target
     */
    @Override
    public void setTarget(Entity target) {
        if (target == null || target instanceof LivingEntity) {
            super.setTarget(target);
        }
    }
}
