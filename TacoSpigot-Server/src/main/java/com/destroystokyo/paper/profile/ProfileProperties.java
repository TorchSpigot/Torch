package com.destroystokyo.paper.profile;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

public final class ProfileProperties {
    private final ImmutableSetMultimap<String, ProfileProperty> properties;

    private ProfileProperties(ImmutableSetMultimap<String, ProfileProperty> properties) {
        this.properties = Preconditions.checkNotNull(properties);;
    }

    public static final ProfileProperties EMPTY = new ProfileProperties(ImmutableSetMultimap.of());

    public ImmutableSet<ProfileProperty> getProperties(String name) {
        return properties.get(name);
    }

    /**
     * Get a single property with the given name
     * <p>
     * Throws an exception if there is more than one, or none at all
     *
     * @param name the name of the property to get
     * @return the only property with the given name
     * @throws IllegalStateException if there are no properties
     * @throws IllegalStateException if there are more than one property with the name
     */
    public ProfileProperty getOnlyProperty(String name) {
        ImmutableSet<ProfileProperty> properties = getProperties(name);
        Preconditions.checkState(!properties.isEmpty(), "No properties named %s", name);
        Preconditions.checkState(properties.size() == 1, "%s properties named %s", properties.size(), name);
        return properties.iterator().next();
    }

    public boolean hasProperty(String name) {
        return !getProperties(name).isEmpty();
    }

    public static ProfileProperties copyOf(Collection<ProfileProperty> properties) {
        Builder builder = new Builder();
        properties.forEach(builder::put);
        return builder.build();
    }

    public static ProfileProperties copyOf(SetMultimap<String, ProfileProperty> originalMultimap) {
        Preconditions.checkNotNull(originalMultimap, "Null multimap");
        ImmutableSetMultimap<String, ProfileProperty> multimap = ImmutableSetMultimap.copyOf(originalMultimap);
        if (multimap.isEmpty()) return EMPTY;
        for (Map.Entry<String, ProfileProperty> entry : multimap.entries()) {
            String name = entry.getKey();
            ProfileProperty property = entry.getValue();
            Preconditions.checkArgument(property.getName().equals(name), "Property %s with key %s", property, name);
        }
        return new ProfileProperties(multimap);
    }

    public int size() {
        return properties.size();
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProfileProperties && ((ProfileProperties) obj).properties.equals(this.properties);
    }

    public void forEach(BiConsumer<String, ProfileProperty> consumer) {
        for (Map.Entry<String, ProfileProperty> entry : properties.entries()) {
            consumer.accept(entry.getKey(), entry.getValue());
        }
    }

    public static ProfileProperties.Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private final ImmutableSetMultimap.Builder<String, ProfileProperty> builder = ImmutableSetMultimap.builder();
        private int size;

        public void put(ProfileProperty property) {
            Preconditions.checkNotNull(property, "Null property");
            put0(property.getName(), property);
        }

        public void put(String name, ProfileProperty value) {
            Preconditions.checkNotNull(name, "Null name");
            Preconditions.checkNotNull(value, "Null property");
            Preconditions.checkArgument(name.equals(value.getName()), "Name %s doesn't match property %s", name, value);
            put0(name, value);
        }

        private void put0(String name, ProfileProperty property) {
            builder.put(name, property);
            size++;
        }

        public ProfileProperties build() {
            return size == 0 ? EMPTY : new ProfileProperties(builder.build());
        }
    }
}