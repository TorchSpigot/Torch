package com.destroystokyo.paper.profile;

import java.util.UUID;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.CraftServer;

import net.minecraft.server.MinecraftServer;

/**
* Represents a player's profile
* Contains their uuid and username
* <p>
* This may or may not have properties
*/
public final class AccountProfile {
   private final UUID id;
   private final String name;
   private final ProfileProperties properties;

   public AccountProfile(UUID id, String name, ProfileProperties properties) {
       Preconditions.checkNotNull(id, "Null id");
       Preconditions.checkNotNull(name, "Null name");
       Preconditions.checkArgument(ProfileLookup.isValidName(name), "Invalid name %s", name);
       this.id = id;
       this.name = name;
       this.properties = properties;
   }

   public AccountProfile(UUID id, String name) {
       this(id, name, null);
   }

   /**
    * Get this player's uuid
    *
    * @return this players uuid
    */
   public UUID getId() {
       return id;
   }

   /**
    * Get this player's name
    *
    * @return this player's name
    */
   public String getName() {
       return name;
   }

   /**
    * Get a json array with this players propertes
    *
    * @return a json array with this player's properties or null if not retreived
    * @throws IllegalStateException if the player's profiles haven't been looked up
    */
   public ProfileProperties getProperties() {
       Preconditions.checkState(hasProperties(), "Profile %s has no properties", this);
       return properties;
   }

   /**
    * Return if the player has its profiles
    *
    * @return if the player has its properties
    */
   public boolean hasProperties() {
       return properties != null;
   }

   /**
    * Return the player's textures, or null if none
    *
    * @return the player's textures, or null if none
    * @throws IllegalStateException if the player has no properties
    */
   public PlayerTextures getTextures() {
       Preconditions.checkState(hasProperties(), "Profile %s has no properties", this);
       return PlayerTextures.parseTextures(this);
   }

   /**
    * Return the player if online
    *
    * @return the player, or null if none
    */
   public Player getPlayer() {
       return Bukkit.getPlayer(this.getId());
   }

   /**
    * Return the offline player
    * <p>Should never return null, even if the player doesn't exist.2</p>
    *
    * @return the offline player
    */
   public OfflinePlayer getOfflinePlayer() {
       return Bukkit.getOfflinePlayer(getId());
   }

   /**
    * Return a version of this profile with no properties
    * <p>
    * Returns this object if this object has no properties
    *
    * @return a version of this profile with no properties
    */
   public AccountProfile clearProperties() {
       return withProperties(null);
   }

   /**
    * Lookup the properties if needed
    * <p>
    * Unlike {@link #lookupProperties()}, this only does a lookup if needed
    * Looks up from the default lookup.
    *
    * @return the profile, with properties looked up
    * @throws IllegalArgumentException if there is no player found with this profile
    * @throws LookupFailedException    if unable to lookup properties
    */
   public AccountProfile withProperties() {
       if (hasProperties()) {
           return this;
       } else {
           return lookupProperties();
       }
   }

   /**
    * Return a copy of this profile with properties looked up from the default lookup
    * <p>
    * This is just a utility wrapper for {@link ProfileLookup#lookupProperties(AccountProfile)}
    *
    * @return a copy of this profile with updated properties
    * @throws IllegalArgumentException if there is no player found with this profile
    * @throws LookupFailedException    if unable to lookup properties
    */
   public AccountProfile lookupProperties() {
       return withProperties(MinecraftServer.getProfileLookup().lookupProperties(this));
   }

   /**
    * Return a copy of this profile with the given properties
    * <p>
    * Returns this object if the properties are the same as the current properties
    *
    * @param properties the properties to use for the new profile
    * @return a copy of this profile with the given properties
    */
   public AccountProfile withProperties(ProfileProperties properties) {
       return this.properties == properties ? this : new AccountProfile(getId(), getName(), properties);
   }

   @Override
   public boolean equals(Object obj) {
       if (obj == this) return true;
       if (obj == null) return false;
       if (obj.getClass() == AccountProfile.class) {
           AccountProfile other = (AccountProfile) obj;
           return other.getId().equals(this.getId())
                   && other.getName().equals(this.getName())
                   && Objects.equal(this.properties, other.properties);
       }
       return false;
   }

   @Override
   public int hashCode() {
       return getId().hashCode();
   }

   @Override
   public String toString() {
       return getName() + ": " + getId();
   }
}