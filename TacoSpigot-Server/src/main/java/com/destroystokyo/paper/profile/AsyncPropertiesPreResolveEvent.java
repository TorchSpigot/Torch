package com.destroystokyo.paper.profile.event;

import com.destroystokyo.paper.profile.ProfileProperties;
import com.google.common.base.Preconditions;

import org.bukkit.event.HandlerList;
import com.destroystokyo.paper.profile.LookupCause;
import com.destroystokyo.paper.profile.AccountProfile;

/**
 * Called before profile properties are requested from mojang.
 * Plugins can set a profile, which will effectively 'cancel' the lookup from mojang.
 * <p>The event may be called synchronously if the uuid is resolved on the main thread</p>
 */
public class AsyncPropertiesPreResolveEvent extends AsyncProfilePreResolveEvent {
    private final AccountProfile profile;

    public AsyncPropertiesPreResolveEvent(AccountProfile profile) {
        super(LookupCause.PROPERTIES_LOOKUP);
        Preconditions.checkNotNull(profile, "Null profile");
        this.profile = profile.withProperties(null);
    }

    /**
     * Set the profile whose properties are being looked up
     */
    public AccountProfile getProfile() {
        return profile;
    }

    /**
     * Set the properties that will be returned by the lookup
     * <p>Overrides any existing profile, and prevents a lookup from mojang.
     * Setting to null re-allows a mojang lookup.</p>
     *
     * @param properties the properties that will be returned by the lookup
     */
    public void setProperties(ProfileProperties properties) {
        setResult(properties == null ? null : getResult().withProperties(properties));
    }

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if the profile's id doesn't match the looked up id
     * @throws IllegalArgumentException if the profile's id doesn't match the looked up name
     * @throws IllegalArgumentException if the profile has 'unset' properties
     */
    @Override
    public void setResult(AccountProfile result) {
        if (result != null) {
            Preconditions.checkArgument(result.hasProperties(), "Profile has unset properties");
            Preconditions.checkArgument(result.getId().equals(this.getProfile().getId()), "Profile id %s doesn't match looked up %s", result.getId(), getProfile().getId());
            Preconditions.checkArgument(result.getName().equals(this.getProfile().getName()), "Profile name %s doesn't match looked up %s", result.getName(), getProfile().getName());
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