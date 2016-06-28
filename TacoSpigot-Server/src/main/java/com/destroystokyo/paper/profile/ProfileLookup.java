package com.destroystokyo.paper.profile;

import java.util.Collection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

public interface ProfileLookup {

   /**
    * A regex of valid minecraft usernames
    * <p>
    * We have to accept spaces due to this bug: https://www.reddit.com/r/Minecraft/comments/276wcb/psa_usernames_can_contain_spaces_this_effectively/
    * We also have to accept names less than 3 characters
    */
   public static final Pattern NAME_PATTERN = Pattern.compile("[ \\w]{1,16}+");

   /**
    * Return if the name is valid
    * <p>
    * This does not mean there is a player with a name, but that there *could* be a player with that name
    *
    * @param s the name to check
    * @return true if valid
    */
   public static boolean isValidName(String s) {
       Matcher m = NAME_PATTERN.matcher(s);
       return m.matches();
   }

   /**
    * Lookup a profile with the given name
    * <p>
    * Returns null if there is no player with the given name.
    * The returned player profile may or may not include properties
    * If properties are needed, proceed to use a property lookup
    *
    * @param name look for a profile with this name
    * @return a profile with the given name, or null if there is no player
    * @throws LookupFailedException if unable to lookup
    * @throws NullPointerException if
    */
   public AccountProfile lookup(String name);

   /**
    * Lookup a profile with the given uuid
    * <p>
    * Returns null if there is no player with the given name.
    * The looked up profiles may or may not include properties
    * If properties are needed, proceed to use a property lookup
    *
    * @param id look for a profile with this uuid
    * @return a profile with the given id
    * @throws LookupFailedException if unable to lookup
    */
   public AccountProfile lookup(UUID id);

   /**
    * Lookup a profile with the given name
    * <p>
    * The looked up profiles may or may not include properties
    * If properties are needed, proceed to use a property lookup
    *
    * @param name     look for a profile with this name
    * @param callback the callback to handle the result of the lookups
    */
   public default void lookup(String name, ProfileLookupCallback<String> callback) {
       try {
           AccountProfile profile = lookup(name);
           callback.onLookup(profile, name);
       } catch (LookupFailedException e) {
           callback.onLookupFailed(e.getCause(), name);
       } catch (Throwable t) {
           callback.onLookupFailed(t, name);
       }
   }


   /**
    * Lookup a profile with the given id
    * <p>
    * The returned player profile may or may not include properties
    * If properties are needed, proceed to use a property lookup
    *
    * @param id       look for a profile with this id
    * @param callback the callback to handle the result of the lookups
    */
   public default void lookup(UUID id, ProfileLookupCallback<UUID> callback) {
       try {
           AccountProfile profile = lookup(id);
           callback.onLookup(profile, id);
       } catch (LookupFailedException e) {
           callback.onLookupFailed(e.getCause(), id);
       } catch (Throwable t) {
           callback.onLookupFailed(t, id);
       }
   }

   /**
    * Lookup all profiles with the given ids, earring on non-existent players
    * <p>
    * The returned profiles may or may not include properties
    * If properties are needed, proceed to use a property lookup
    * <p>
    * Use the callback version of the method if you want to handle (or ignore) non-existent players.
    * The ordering of the returned profiles may or may not coincide with the ordering of the passed collection.
    * Therefore, if you need the original id that you used for lookup, you should use the callback-version,
    *
    * @param ids the ids to lookup
    * @return the resulting profiles
    * @throws LookupFailedException    if lookup fails
    * @throws IllegalArgumentException if one of the ids doesn't exist
    */
   public default ImmutableList<AccountProfile> lookupIds(Collection<UUID> ids) {
       ImmutableList.Builder<AccountProfile> profileBuilder = ImmutableList.builder();
       lookupIds(ids, ProfileLookupCallback.assumeFound((profile, original) -> profileBuilder.add(profile)));
       return profileBuilder.build();
   }

   /**
    * Lookup all profiles with the given names, ignoring non-existent profiles
    * <p>
    * The looked up profiles may or may not include properties
    * If properties are needed, proceed to use a property lookup
    * <p>
    * Use the callback version of the method if you want to handle (or ignore) non-existent players.
    * The ordering of the returned profiles may or may not coincide with the ordering of the passed collection.
    * Therefore, if you need the original name that you used for lookup, you should use the callback-version,
    *
    * @param names the names to lookup
    * @return the resulting profiles
    * @throws LookupFailedException    if lookup fails
    * @throws IllegalArgumentException if one of the names doesn't exist
    */
   public default ImmutableList<AccountProfile> lookupNames(Collection<String> names) {
       ImmutableList.Builder<AccountProfile> profileBuilder = ImmutableList.builder();
       lookupNames(names, ProfileLookupCallback.assumeFound((profile, original) -> profileBuilder.add(profile)));
       return profileBuilder.build();
   }

   /**
    * Lookup all profiles with the given ids
    * <p>
    * The looked up profiles may or may not include properties
    * If properties are needed, proceed to use a property lookup
    * <p>
    * Blocks until the lookups complete
    *
    * @param ids      the ids to lookup
    * @param callback the callback to handle the lookups
    */
   public default void lookupIds(Collection<UUID> ids, ProfileLookupCallback<UUID> callback) {
       ids.forEach((id) -> lookup(id, callback));
   }

   /**
    * Lookup all profiles with the given names
    * <p>
    * The looked up profiles may or may not include properties
    * If properties are needed, proceed to use a property lookup
    * <p>
    * Blocks until the lookups complete
    *
    * @param names    the names to lookup
    * @param callback the callback to handle the lookups
    */
   public default void lookupNames(Collection<String> names, ProfileLookupCallback<String> callback) {
       names.forEach((name) -> lookup(name, callback));
   }

   /**
    * Lookup the player's properties
    * <p>
    * Should never return null
    *
    * @param profile the profile to lookup properties for
    * @return the player's properties
    * @throws IllegalArgumentException if there is no player with the given name/uuid
    * @throws LookupFailedException    if unable to lookup properties
    */
   public ProfileProperties lookupProperties(AccountProfile profile);

   /**
    * Return if the lookups have the same underlying source
    * <p>
    * Two lookups are considered equal if they use the same underlying source
    *
    * @param other the object to check equality with
    * @return if equal
    */
   public boolean equals(Object other);
}