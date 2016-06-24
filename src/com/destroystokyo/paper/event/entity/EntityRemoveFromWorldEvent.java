package com.destroystokyo.paper.event.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired any time an entity is being removed from a world for any reason
 */
public class EntityRemoveFromWorldEvent extends Event {
    private final Entity entity;

    public EntityRemoveFromWorldEvent(Entity entity) {
        this.entity = entity;
    }

    /**
     * Gets the entity being removed from the world
     * @return
     */
    public Entity getEntity() {
        return entity;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
