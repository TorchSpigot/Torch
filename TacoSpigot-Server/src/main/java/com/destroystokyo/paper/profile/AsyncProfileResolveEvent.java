package com.destroystokyo.paper.profile.event;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.destroystokyo.paper.profile.LookupCause;
import com.destroystokyo.paper.profile.AccountProfile;

/**
 * Called once a profile is resolved.
 * <p>May come from a plugin or from mojang.</p>
 */
public class AsyncProfileResolveEvent extends Event {
    private final LookupCause cause;
    private AccountProfile result;
    private boolean mojang;

    public AsyncProfileResolveEvent(LookupCause cause, AccountProfile result, boolean mojang) {
        super(!Bukkit.isPrimaryThread());
        this.cause = Preconditions.checkNotNull(cause, "Null cause");;
        setResult(result);
        this.mojang = mojang;
    }

    /**
     * Return the result of this lookup
     *
     * @return the result of the lookup
     */
    public AccountProfile getResult() {
        return result;
    }

    /**
     * Set the result of this lookup
     * <p>Can't be null. If the lookup is a properties lookup, the properties must be set.</p>
     *
     * @param result the result of the lookup
     */
    public void setResult(AccountProfile result) {
        Preconditions.checkNotNull(result, "Null result");
        if (this.getCause() == LookupCause.PROPERTIES_LOOKUP) {
            Preconditions.checkArgument(result.hasProperties(), "Result doesn't have properties in properties lookup: %s", result);
        }
        this.result = result;
        this.mojang = false;
    }

    /**
     * Get what caused this lookup
     *
     * @return what caused this lookup
     */
    public LookupCause getCause() {
        return cause;
    }

    /**
     * Return if the profile is known to come from mojang
     * <p>If not, it is probably from a plugin.</p>
     *
     * @return if the profile is known to come from mojang
     */
    public boolean isFromMojang() {
        return mojang;
    }

    private static final HandlerList handlerList = new HandlerList();

    public HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}