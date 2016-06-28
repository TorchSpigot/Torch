package com.destroystokyo.paper.profile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.destroystokyo.paper.utils.json.ProfilePropertyTypeAdapter;
import com.destroystokyo.paper.utils.json.UUIDTypeAdapter;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import static com.destroystokyo.paper.profile.UUIDUtils.fromString;
import static com.destroystokyo.paper.profile.UUIDUtils.toMojangString;

public class ProfileUtils {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(UUID.class, UUIDTypeAdapter.createMojang())
            .registerTypeAdapter(ProfileProperty.class, new ProfilePropertyTypeAdapter())
            .create();

    public static Optional<AccountProfile> requestProfile(UUID id) {
        Preconditions.checkNotNull(id, "Null id");
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + toMojangString(id) + "?unsigned=false");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                return Optional.empty(); // Profile not found
            }
            if (connection.getResponseCode() == 429) {
                throw new LookupFailedException("Mojang rate limited request for: " + id);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8))) {
                ProfileResponse response = GSON.fromJson(reader, ProfileResponse.class);

                if (response.errorMessage != null) {
                    throw new LookupFailedException("Mojang returned error: " + response.errorMessage);
                } else if (response.id == null || response.name == null || response.profile == null) {
                    throw new LookupFailedException("Unknown error looking up " + id.toString());
                }

                return Optional.of(new AccountProfile(response.id, response.name, ProfileProperties.copyOf(response.profile)));
            }
        } catch (JsonIOException e) {
            throw new LookupFailedException("Error contacting mojang", e.getCause());
        } catch (MalformedURLException e) {
            // This shouldn't happen as UUID.toString() is a perfectly valid url
            throw new AssertionError("Unable to parse url " + e);
        } catch (IOException | JsonParseException e) {
            throw new LookupFailedException("Error contacting mojang", e);
        }
    }

    private static class ProfileResponse {
        private String errorMessage;
        private UUID id;
        private String name;
        private List<ProfileProperty> profile;
    }

    public static Optional<AccountProfile> lookup(String name) {
        Preconditions.checkNotNull(name, "Null name");
        Preconditions.checkArgument(ProfileLookup.isValidName(name), "Invalid name %s", name);
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                return Optional.empty(); // Profile not found
            }
            if (connection.getResponseCode() == 429) {
                throw new LookupFailedException("Mojang rate limited request for: " + name);
            }

            try (JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8)))) {
                reader.beginObject();
                UUID id = null;
                name = null; // Reset to null so we can check for errors
                while (reader.hasNext()) {
                    String key;
                    switch ((key = reader.nextName())) {
                        case "id":
                            String s = reader.nextString();
                            if (s == null) throw new LookupFailedException("Mojang returned null id");
                            id = fromString(s);
                            break;
                        case "name":
                            name = reader.nextString(); // Now reset to case-corrected name
                            if (name == null) throw new LookupFailedException("Mojang returned null name");
                            break;
                        case "legacy":
                        case "demo":
                            break;
                        default:
                            throw new LookupFailedException("Invalid json, unexpected object key: " + key);
                    }
                }

                if (id == null) {
                    throw new LookupFailedException("Mojang didn't return id");
                } else if (name == null) {
                    throw new LookupFailedException("Mojang didn't return name");
                }
                reader.endObject();
                if (reader.hasNext()) {
                    throw new LookupFailedException("Didn't read all data mojang sent. Unexpected " + reader.peek());
                }
                return Optional.of(new AccountProfile(id, name));
            }
        } catch (JsonIOException e) {
            throw new LookupFailedException("Error contacting mojang", e.getCause());
        } catch (MalformedURLException e) {
            // This shouldn't happen as names are perfectly valid urls
            throw new AssertionError("Unable to parse url " + e);
        } catch (IOException | JsonParseException e) {
            throw new LookupFailedException("Error contacting mojang", e);
        }
    }

    public static final URL BULK_NAME_LOOKUP_URL;

    static {
        try {
            BULK_NAME_LOOKUP_URL = new URL("https://api.mojang.com/profiles/minecraft");
        } catch (MalformedURLException e) {
            throw new AssertionError("Couldn't  parse URL", e);
        }
    }

    public static ImmutableList<AccountProfile> lookupNames(ImmutableSet<String> names) {
        Preconditions.checkNotNull(names, "Null names");
        if (names.isEmpty()) return ImmutableList.of();
        if (names.size() > 100) {
            ImmutableList.Builder<AccountProfile> result = ImmutableList.builder();
            // Split up the request to meet mojang's limit of 100 names per request
            UnmodifiableIterator<String> iterator = names.iterator();
            while (iterator.hasNext()) {
                ImmutableSet.Builder<String> split = ImmutableSet.builder();
                for (int i = 0; i < 100 && iterator.hasNext(); i++) {
                    String name = iterator.next();
                    split.add(name);
                }
                result.addAll(lookupNames(split.build()));
            }
            return result.build();
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) BULK_NAME_LOOKUP_URL.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
            connection.connect();
            if (connection.getResponseCode() == 429) {
                throw new LookupFailedException("Mojang rate limited request for " + names.size() + " names");
            }
            try (JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), Charsets.UTF_8)))) {
                writer.beginArray();
                for (String name : names) {
                    writer.value(name);
                }
                writer.endArray();
            }
            ImmutableList.Builder<AccountProfile> profiles = ImmutableList.builder();
            try (JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8)))) {
                reader.beginArray();
                while (reader.hasNext()) {
                    reader.beginObject();
                    UUID id = null;
                    String name = null;
                    while (reader.hasNext()) {
                        String key;
                        switch ((key = reader.nextName())) {
                            case "id":
                                String s = reader.nextString();
                                if (s == null) throw new LookupFailedException("Mojang returned null id");
                                id = fromString(s);
                                break;
                            case "name":
                                name = reader.nextString();
                                if (name == null) throw new LookupFailedException("Mojang returned null name");
                                break;
                            case "legacy":
                            case "demo":
                                break;
                            default:
                                throw new LookupFailedException("Invalid json. Unexpected object key: " + key);
                        }
                    }

                    if (id == null) {
                        throw new LookupFailedException("Mojang didn't return id");
                    } else if (name == null) {
                        throw new LookupFailedException("Mojang didn't return name");
                    }
                    profiles.add(new AccountProfile(id, name));
                    reader.endObject();
                }
                reader.endArray();
                if (reader.hasNext()) {
                    throw new LookupFailedException("Didn't read response fully. Unexpected: " + reader.peek());
                }
                return profiles.build();
            }
        } catch (IOException e) {
            throw new LookupFailedException("Error contacting mojang", e);
        }
    }

    //
    // Converters
    //

    public static GameProfile toMojang(AccountProfile paper) {
        if (paper == null) return null;
        GameProfile mojang = new GameProfile(paper.getId(), paper.getName());
        if (paper.hasProperties()) {
            mojang.getProperties().clear();
            addAllToMojang(paper.getProperties(), mojang.getProperties());
        }
        return mojang;
    }

    public static AccountProfile toPaper(GameProfile mojang) {
        if (mojang == null) return null;
        // If the profile's properties aren't empty, we must know them
        return toPaper0(mojang, !mojang.getProperties().isEmpty());
    }

    public static AccountProfile toPaperWithProperties(GameProfile mojang) {
        if (mojang == null) return null;
        return toPaper0(mojang, true);
    }

    private static AccountProfile toPaper0(GameProfile mojang, boolean propertiesKnown) {
        Preconditions.checkArgument(mojang.isComplete(), "Incomplete profile %s", mojang);
        return new AccountProfile(mojang.getId(), mojang.getName(), propertiesKnown ? toPaper(mojang.getProperties()) : null);
    }

    public static PropertyMap toMojang(ProfileProperties paper) {
        if (paper == null) return null;
        PropertyMap mojang = new PropertyMap();
        addAllToMojang(paper, mojang);
        return mojang;
    }

    private static void addAllToMojang(ProfileProperties paper, PropertyMap mojang) {
        paper.forEach((name, property) -> mojang.put(name, toMojang(property)));
    }

    public static ProfileProperties toPaper(PropertyMap mojang) {
        if (mojang == null) return null;
        ProfileProperties.Builder builder = ProfileProperties.builder();
        for (Map.Entry<String, Property> entry : mojang.entries()) {
            builder.put(entry.getKey(), toPaper(entry.getValue()));
        }
        return builder.build();
    }

    public static Property toMojang(ProfileProperty paper) {
        if (paper == null) return null;
        return new Property(paper.getName(), paper.getValue(), paper.isSigned() ? paper.getSignature() : null);
    }

    public static ProfileProperty toPaper(Property mojang) {
        if (mojang == null) return null;
        return new ProfileProperty(mojang.getName(), mojang.getValue(), mojang.getSignature());
    }
}