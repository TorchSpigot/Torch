package com.destroystokyo.paper.profile.event;

import com.destroystokyo.paper.profile.LookupCause;
import com.destroystokyo.paper.profile.AccountProfile;
import com.google.common.base.Preconditions;

import org.bukkit.event.HandlerList;

/**
* Called before a uuid is requested from mojang.
* <p>
* <p>The event may be called synchronouslys if the uuid is resolved on the main thread</p>
*/
public class AsyncNamePreResolveEvent extends AsyncProfilePreResolveEvent {
   private final String name;

   public AsyncNamePreResolveEvent(String name) {
       super(LookupCause.NAME_LOOKUP);
       Preconditions.checkNotNull(name, "Null name");
       this.name = name;
   }

   /**
    * Return the name that was used to request the profile
    *
    * @return the name that was requested
    */
   public String getName() {
       return name;
   }

   /**
    * {@inheritDoc}
    *
    * @throws IllegalArgumentException if the profile's name doesn't match the looked up names
    */
   @Override
   public void setResult(AccountProfile result) {
       if (result != null) {
           Preconditions.checkArgument(result.getName().equalsIgnoreCase(this.getName()), "Name %s doesn't match looked up name: %s", result.getName(), this.getName());
       }
       super.setResult(result);
   }

   private static final HandlerList handlerList = new HandlerList();

   public static HandlerList getHandlerList() {
       return handlerList;
   }

   @Override
   public HandlerList getHandlers() {
       return handlerList;
   }
}