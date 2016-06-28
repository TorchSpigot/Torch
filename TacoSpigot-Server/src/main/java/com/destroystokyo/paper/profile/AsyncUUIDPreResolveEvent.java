package com.destroystokyo.paper.profile.event;

import java.util.UUID;

import com.destroystokyo.paper.profile.LookupCause;
import com.destroystokyo.paper.profile.AccountProfile;
import com.google.common.base.Preconditions;

import org.bukkit.event.HandlerList;

/**
 * Called before a profile is requested from mojang.
 * Plugins can set a profile, which will effectively 'cancel' the lookup from mojang.
 * <p>The event may be called synchronously if the uuid is resolved on the main thread</p>
 */
public class AsyncUUIDPreResolveEvent extends AsyncProfilePreResolveEvent {
    private final UUID id;

    public AsyncUUIDPreResolveEvent(UUID id) {
        super(LookupCause.UUID_LOOKUP);
        this.id = Preconditions.checkNotNull(id, "Null id");;
    }

    /**
     * Return the id whose profile was requested
     *
     * @return the id that was requested
     */
    public UUID getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the profile's id doesn't match the looked up id
     */
    @Override
    public void setResult(AccountProfile result) {
        if (result != null) {
            Preconditions.checkArgument(result.getId().equals(this.getId()), "Id %s doesn't match looked up id: %s", result.getId(), this.getId());
        }
        super.setResult(result);
    }
    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}