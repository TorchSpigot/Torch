package org.torch.server;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;

import lombok.Getter;
import net.minecraft.server.MCUtil;
import net.minecraft.server.UserCache;
import net.minecraft.server.EntityHuman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.spigotmc.SpigotConfig;
import org.torch.api.Async;
import org.torch.api.TorchReactor;

import static net.minecraft.server.UserCache.isOnlineMode;
import static net.minecraft.server.UserCache.setOnlineMode;
import static org.torch.server.TorchServer.logger;

@Getter @ThreadSafe
public final class TorchUserCache implements TorchReactor {
    /** The legacy */
    private final UserCache servant;
    
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    
    /**
     * All user caches, Username -> Entry(profile and expire date included)
     */
    private final Cache<String, UserCacheEntry> caches = Caffeine.newBuilder().build();
    
    /** GameProfile repository */
    private final GameProfileRepository profileRepo;
    /** Gson */
    protected final Gson gson;
    /**
     * UserCache file
     * */
    private final File usercacheFile;
    
    public static final ParameterizedType type = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] { UserCacheEntry.class};
        }
        
        @Override
        public Type getRawType() {
            return List.class;
        }
        
        @Override
        public Type getOwnerType() {
            return null;
        }
    };

    public TorchUserCache(GameProfileRepository repo, File file, UserCache legacy) {
        servant = legacy;
        profileRepo = repo;
        usercacheFile = file;
        
        this.gson = new GsonBuilder().registerTypeHierarchyAdapter(UserCacheEntry.class, new CacheSerializer()).create();
        
        this.load();
    }

    /**
     * Lookup the profile by the name from Mojang, triggering network operation
     */
    @Nullable
    public static GameProfile matchProfile(GameProfileRepository profileRepo, String keyUsername) {
        // Keep current case for offline servers
        if (!isOnlineMode()) {
            return new GameProfile(EntityHuman.offlinePlayerUUID(keyUsername, false), keyUsername);
        }
        
        final GameProfile[] profile = new GameProfile[1];
        ProfileLookupCallback callback = new ProfileLookupCallback() {
            @Override
            public void onProfileLookupSucceeded(GameProfile gameprofile) {
                profile[0] = gameprofile;
            }
            
            @Override
            public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                profile[0] = null;
            }
        };
        
        profileRepo.findProfilesByNames(new String[] { keyUsername }, Agent.MINECRAFT, callback);
        
        return profile[0];
    }
    
    /** Generate an new expire date for the cache */
    public static Date warpExpireDate() {
        // Generate new expire date if not given
        Calendar calendar = Calendar.getInstance();
        
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MONTH, 1); // TODO: configurable expire date
        return calendar.getTime();
    }

    /**
     * Add an entry to this cache with the default expire date
     */
    public UserCacheEntry putCache(String keyUsername) {
        return putCache(keyUsername, (Date) null);
    }

    /**
     * Add an entry to this cache with an expire date, return the new entry
     */
    public UserCacheEntry putCache(String keyUsername, Date date) {
        if (date == null) date = warpExpireDate();
        
        UserCacheEntry entry = new UserCacheEntry(matchProfile(profileRepo, keyUsername), date);
        caches.put(keyUsername, entry);
        
        if(!org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly) this.save();
        
        return entry;
    }
    
    /**
     * Also create new entry if not present
     */
    @Nullable
    public GameProfile requestProfile(String username) {
        if (StringUtils.isBlank(username)) return null;
        
        String keyUsername = Caches.toLowerCase(username, Locale.ROOT);
        UserCacheEntry cachedEntry = caches.getIfPresent(keyUsername);
        
        // Remove expired entry
        if (cachedEntry != null) {
            if (System.currentTimeMillis() >= cachedEntry.expireDate.getTime()) {
                caches.invalidate(keyUsername);
                cachedEntry = null;
            }
        }
        
        if (cachedEntry == null) {
            cachedEntry = putCache(keyUsername);
        }
        
        return cachedEntry == null ? null : cachedEntry.profile;
    }
    
    @Nullable
    public GameProfile peekCachedProfile(String username) {
        UserCacheEntry entry = caches.getIfPresent(username);
        
        return entry == null ? null : entry.profile;
    }
    
    @Nullable
    public UserCacheEntry peekCachedEntry(String username) {
        return caches.getIfPresent(username);
    }
    
    /*
    @Nullable
    public GameProfile peekCachedProfile(UUID uuid) {
        String username = TorchServer.getServer().getPlayerList().uuidToUsername(uuid);
        UserCacheEntry entry = caches.getIfPresent(username);
        
        return entry == null ? null : entry.profile;
    }
    
    @Nullable
    public UserCacheEntry peekCachedEntry(UUID uuid) {
        String username = TorchServer.getServer().getPlayerList().uuidToUsername(uuid);
        
        return caches.getIfPresent(username);
    } */
    
    /** Offer or replace the old cache if present */
    @Async
    public void offerCache(GameProfile profile) {
        offerCache(profile, (Date) null);
    }
    
    /** Offer or replace the old cache if present, with an expire date */
    @Async
    public void offerCache(GameProfile profile, Date date) {
        if (date == null) date = warpExpireDate();
        
        String keyUsername = Caches.toLowerCase(profile.getName(), Locale.ROOT);
        UserCacheEntry entry = caches.getIfPresent(keyUsername);
        
        if (entry != null) {
            // Remove expired entry
            if (System.currentTimeMillis() >= entry.expireDate.getTime()) {
                caches.invalidate(keyUsername);
                entry = null;
            }
            
            // The posted profile may has an incorrect case, this only happened on offline servers,
            // replace with an lower-case profile.
            if (entry != null && !isOnlineMode() && !entry.profile.getName().equals(profile.getName())) {
                entry = new UserCacheEntry(matchProfile(profileRepo, keyUsername), date);
                caches.put(keyUsername, entry);
            }
            
        } else {
            if (isOnlineMode()) {
                Date date_ = date;
                Regulator.post(() -> {
                    UserCacheEntry newEntry = new UserCacheEntry(matchProfile(profileRepo, keyUsername), date_);
                    caches.put(keyUsername, newEntry);
                    
                    if(!org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly) this.save(false);
                });
                
                return;
            } else {
                entry = new UserCacheEntry(matchProfile(profileRepo, keyUsername), date);
                caches.put(keyUsername, entry);
            }
            
        }
        
        if(!org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly) this.save();
    }
    
    public String[] getCachedUsernames() {
        return caches.asMap().keySet().toArray(new String[caches.asMap().size()]);
    }
    
    public void load() {
        BufferedReader reader = null;

        try {
            reader = Files.newReader(usercacheFile, Charsets.UTF_8);
            List<UserCacheEntry> entries = this.gson.fromJson(reader, type);
            
            caches.invalidateAll();
            
            if (entries != null) {
                for (UserCacheEntry entry : Lists.reverse(entries)) {
                    if (entry != null) this.offerCache(entry.profile, entry.expireDate);
                }
            }
            
        } catch (FileNotFoundException e) {
            ;
        } catch (JsonSyntaxException e) {
            logger.warn("Usercache.json is corrupted or has bad formatting. Deleting it to prevent further issues.");
            this.usercacheFile.delete();
        } catch (JsonParseException e) {
            ;
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
    
    @Async
    public void save() {
        save(true);
    }
    
    public void save(boolean async) {
        Runnable save = () -> {
            String jsonString = this.gson.toJson(matchEntries(SpigotConfig.userCacheCap));
            BufferedWriter writer = null;
            
            try {
                writer = Files.newWriter(this.usercacheFile, Charsets.UTF_8);
                writer.write(jsonString);
                return;
            } catch (FileNotFoundException e) {
                return;
            } catch (IOException io) {
                ;
            } finally {
                IOUtils.closeQuietly(writer);
            }
        };
        
        if (async) {
            MCUtil.scheduleAsyncTask(save);
        } else {
            save.run();
        }
    }
    
    public ArrayList<UserCacheEntry> matchEntries(int limitedSize) {
        ArrayList<UserCacheEntry> list = Lists.newArrayList();
        
        Iterator<UserCacheEntry> itr = Iterators.limit(caches.asMap().values().iterator(), limitedSize);
        while (itr.hasNext()) list.add(itr.next());
        
        return list;
    }
    
    @Getter
    public final class UserCacheEntry {
        /** The player's GameProfile */
        private final GameProfile profile;
        /** The date that this entry will expire */
        private final Date expireDate;
        
        private UserCacheEntry(GameProfile gameProfile, Date date) {
            this.profile = gameProfile;
            this.expireDate = date;
        }
        
        @Deprecated
        public UserCache.UserCacheEntry toLegacy() {
            return servant.new UserCacheEntry(profile, expireDate);
        }
    }
    
    private final class CacheSerializer implements JsonDeserializer<UserCacheEntry>, JsonSerializer<UserCacheEntry> {
        private CacheSerializer() {}

        @Override
        public JsonElement serialize(UserCacheEntry entry, Type type, JsonSerializationContext context) {
            JsonObject jsonData = new JsonObject();
            UUID uuid = entry.profile.getId();

            jsonData.addProperty("name", entry.profile.getName());
            jsonData.addProperty("uuid", uuid == null ? "" : uuid.toString());
            jsonData.addProperty("expiresOn", DATE_FORMAT.format(entry.expireDate));
            
            return jsonData;
        }
        
        @Override
        public UserCacheEntry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (!jsonElement.isJsonObject()) return null;
            
            JsonObject jsonData = jsonElement.getAsJsonObject();
            JsonElement name = jsonData.get("name");
            JsonElement uuid = jsonData.get("uuid");
            JsonElement expireDate = jsonData.get("expiresOn");
            
            if (name == null || uuid == null) return null;
            
            String uuidString = uuid.getAsString();
            String nameString = name.getAsString();
            
            Date date = null;
            if (expireDate != null) {
                try {
                    date = DATE_FORMAT.parse(expireDate.getAsString());
                } catch (ParseException ex) {
                    ;
                }
            }
            
            if (nameString == null || uuidString == null) return null;
            
            UUID standardUUID;
            try {
                standardUUID = UUID.fromString(uuidString);
            } catch (Throwable t) {
                return null;
            }
            
            return new UserCacheEntry(new GameProfile(standardUUID, nameString), date);
        }
    }
}
