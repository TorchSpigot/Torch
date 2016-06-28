package com.destroystokyo.paper.profile;

import java.util.Collection;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class MojangLookup implements ProfileLookup {

    @Override
    public AccountProfile lookup(String name) {
        AccountProfile[] profileHolder = new AccountProfile[1];
        lookupNames(ImmutableList.of(name), (profile, original) -> profileHolder[0] = profile);
        return profileHolder[0];
    }

    @Override
    public AccountProfile lookup(UUID id) {
        return ProfileUtils.requestProfile(id).orElse(null);
    }

    @Override
    public void lookupNames(Collection<String> names, final ProfileLookupCallback<String> callback) {
        Preconditions.checkNotNull(callback, "Null callback");

    }

    @Override
    public ProfileProperties lookupProperties(AccountProfile profile) {
        AccountProfile newProfile = lookup(profile.getId());
        Preconditions.checkArgument(newProfile != null, "%s doesn't exist", profile);
        return newProfile.getProperties();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MojangLookup;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}