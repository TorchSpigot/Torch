package com.destroystokyo.paper.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.destroystokyo.paper.profile.event.AsyncPropertiesPreResolveEvent;
import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import com.destroystokyo.paper.profile.event.AsyncNamePreResolveEvent;
import com.destroystokyo.paper.profile.event.AsyncProfileResolveEvent;
import com.destroystokyo.paper.profile.event.AsyncUUIDPreResolveEvent;

public class EventProfileLookup implements ProfileLookup {
   private final ProfileLookup delegate;

   public EventProfileLookup(ProfileLookup delegate) {
       Preconditions.checkNotNull(delegate, "Null delegate");
       this.delegate = Preconditions.checkNotNull(delegate, "Null delegate");;
   }

   @Override
   public AccountProfile lookup(String name) {
       Preconditions.checkNotNull(name, "Null name");
       AsyncNamePreResolveEvent preResolveEvent = new AsyncNamePreResolveEvent(name);
       Bukkit.getPluginManager().callEvent(preResolveEvent);
       AsyncProfileResolveEvent resolveEvent;
       if (preResolveEvent.isResolved()) { // Plugin set result
           resolveEvent = new AsyncProfileResolveEvent(LookupCause.NAME_LOOKUP, preResolveEvent.getResult(), false);
       } else {
           // Lookup result from mojang
           AccountProfile profile = delegate.lookup(name);
           if (profile == null) return null; // Not found
           resolveEvent = new AsyncProfileResolveEvent(LookupCause.NAME_LOOKUP, profile, true);
       }
       Bukkit.getPluginManager().callEvent(resolveEvent);
       return resolveEvent.getResult();
   }

   @Override
   public AccountProfile lookup(UUID id) {
       Preconditions.checkNotNull(id, "Null id");
       AsyncUUIDPreResolveEvent preResolveEvent = new AsyncUUIDPreResolveEvent(id);
       Bukkit.getPluginManager().callEvent(preResolveEvent);
       AsyncProfileResolveEvent resolveEvent;
       if (preResolveEvent.isResolved()) { // Plugin set result
           resolveEvent = new AsyncProfileResolveEvent(LookupCause.UUID_LOOKUP, preResolveEvent.getResult(), false);
       } else {
           // Lookup result from mojang
           AccountProfile profile = delegate.lookup(id);
           if (profile == null) return null; // Not found
           resolveEvent = new AsyncProfileResolveEvent(LookupCause.UUID_LOOKUP, profile, true);
       }
       Bukkit.getPluginManager().callEvent(resolveEvent);
       return resolveEvent.getResult();
   }

   @Override
   public void lookupIds(Collection<UUID> ids, ProfileLookupCallback<UUID> callback) {
       List<UUID> toLookup = new ArrayList<>(ids.size());
       for (UUID id : ids) {
           Preconditions.checkNotNull(id, "Null id");
           AsyncUUIDPreResolveEvent preResolveEvent = new AsyncUUIDPreResolveEvent(id);
           Bukkit.getPluginManager().callEvent(preResolveEvent);
           if (preResolveEvent.isResolved()) { // Plugin set result
               AsyncProfileResolveEvent resolveEvent = new AsyncProfileResolveEvent(LookupCause.UUID_LOOKUP, preResolveEvent.getResult(), false);
               Bukkit.getPluginManager().callEvent(resolveEvent);
               callback.onLookup(resolveEvent.getResult(), id);
           } else {
               toLookup.add(id);
           }
       }
       delegate.lookupIds(toLookup, new ProfileLookupCallback<UUID>() {
           @Override
           public void onLookup(AccountProfile profile, UUID original) {
               if (profile != null) {
                   AsyncProfileResolveEvent resolveEvent = new AsyncProfileResolveEvent(LookupCause.UUID_LOOKUP, profile, true);
                   Bukkit.getPluginManager().callEvent(resolveEvent);
                   profile = resolveEvent.getResult();
               }
               callback.onLookup(profile, original);
           }

           @Override
           public void onLookupFailed(Throwable t, UUID original) {
               callback.onLookupFailed(t, original);
           }
       });
   }

   @Override
   public void lookupNames(Collection<String> names, ProfileLookupCallback<String> callback) {
       List<String> toLookup = new ArrayList<>(names.size());
       for (String name : names) {
           Preconditions.checkNotNull(name, "Null id");
           AsyncNamePreResolveEvent preResolveEvent = new AsyncNamePreResolveEvent(name);
           Bukkit.getPluginManager().callEvent(preResolveEvent);
           if (preResolveEvent.isResolved()) { // Plugin set result
               AsyncProfileResolveEvent resolveEvent = new AsyncProfileResolveEvent(LookupCause.NAME_LOOKUP, preResolveEvent.getResult(), false);
               Bukkit.getPluginManager().callEvent(resolveEvent);
               callback.onLookup(resolveEvent.getResult(), name);
           } else {
               toLookup.add(name);
           }
       }
       delegate.lookupNames(toLookup, new ProfileLookupCallback<String>() {
           @Override
           public void onLookup(AccountProfile profile, String original) {
               if (profile != null) {
                   AsyncProfileResolveEvent resolveEvent = new AsyncProfileResolveEvent(LookupCause.NAME_LOOKUP, profile, true);
                   Bukkit.getPluginManager().callEvent(resolveEvent);
                   profile = resolveEvent.getResult();
               }
               callback.onLookup(profile, original);
           }

           @Override
           public void onLookupFailed(Throwable t, String original) {
               callback.onLookupFailed(t, original);
           }
       });
   }

   @Override
   public ProfileProperties lookupProperties(AccountProfile profile) {
       AsyncPropertiesPreResolveEvent preResolveEvent = new AsyncPropertiesPreResolveEvent(profile);
       Bukkit.getPluginManager().callEvent(preResolveEvent);
       AsyncProfileResolveEvent resolveEvent;
       if (preResolveEvent.isResolved()) {
           resolveEvent = new AsyncProfileResolveEvent(LookupCause.PROPERTIES_LOOKUP, preResolveEvent.getResult(), false);
       } else {
           ProfileProperties properties = delegate.lookupProperties(profile);
           if (properties == null) return null;
           resolveEvent = new AsyncProfileResolveEvent(LookupCause.PROPERTIES_LOOKUP, profile.withProperties(properties), true);
       }
       Bukkit.getPluginManager().callEvent(resolveEvent);
       return resolveEvent.getResult().getProperties();
   }
}