package com.destroystokyo.paper.profile.event;

import com.destroystokyo.paper.profile.AccountProfile;
import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import com.destroystokyo.paper.profile.LookupCause;

/**
* Called before a profile is looked up from mojang.
* Plugins can set a profile, which will effectively 'cancel' the lookup from mojang.
* <p>May be called from the main thread if the lookup is from the main thread.</p>
*/
public abstract class AsyncProfilePreResolveEvent extends Event {
   private final LookupCause cause;
   private AccountProfile result;

   public AsyncProfilePreResolveEvent(LookupCause cause) {
       super(!Bukkit.isPrimaryThread());
       this.cause = Preconditions.checkNotNull(cause, "Null cause");;
   }

   /**
    * Get the reason this profile is being looked up
    * @return the reason this profile is being looked up
    */
   public LookupCause getCause() {
       return cause;
   }

   /**
    * Set the profile that will be returned by the lookup
    * <p>Overrides any existing profile, and prevents a lookup from mojang.
    * Setting to null re-allows a mojang lookup.</p>
    *
    * @param result the profile that will be returned by the lookup
    */
   public void setResult(AccountProfile result) {
       this.result = result;
   }

   /**
    * Get if a plugin has set the profile to be returned, and prevented a lookup to mojang
    * <p>If not, it must be looked up from mojang</p>
    *
    * @return if a plugin has overriden the profile to be returned
    */
   public boolean isResolved() {
       return result != null;
   }

   /**
    * Get the profile that will be returned by the lookup, if another plugin has set it
    * <p>If this returns null, than no plugin has set the profile and it must be looked up from mojang</p>
    *
    * @return the profile that has been set, or null if not set
    */
   public AccountProfile getResult() {
       return result;
   }
}