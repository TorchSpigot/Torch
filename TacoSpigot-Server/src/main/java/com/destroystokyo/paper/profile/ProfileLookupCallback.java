package com.destroystokyo.paper.profile;

import com.google.common.base.Preconditions;

/**
* A callback for profile lookup
* <p/>
* Methods may be called multiple times in a bulk lookup.
* <b>Just because a success method is called, doesn't mean the lookup wont fail!</b>
* Callbacks should take this into account, and wait till completion to handle
*
* @param <T> the key that is being looked up
*/
@FunctionalInterface
public interface ProfileLookupCallback<T> {

   /**
    * Calls when a lookup succeeds
    * <p>
    * Profile may not be null if the player doesn't exist
    *
    * @param profile  the profile that was found, or null if the player
    * @param original the key that was being looked up
    */
   public void onLookup(AccountProfile profile, T original);

   /**
    * Called when a lookup fails
    *
    * @param t        the exception that was caught, may be null
    * @param original the key that was being looked up
    */
   public default void onLookupFailed(Throwable t, T original) {
       throw new LookupFailedException("Unable to lookup " + original.toString(), t);
   }

   public static <T> ProfileLookupCallback<T> assumeFound(ProfileLookupCallback<T> delegate) {
       return new ProfileLookupCallback<T>() {
           @Override
           public void onLookup(AccountProfile profile, T original) {
               Preconditions.checkArgument(profile != null, "%s doesn't exist", original);
               delegate.onLookup(profile, original);
           }

           @Override
           public void onLookupFailed(Throwable t, T original) {
               delegate.onLookupFailed(t, original);
           }
       };
   }
}