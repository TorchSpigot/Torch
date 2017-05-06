package net.minecraft.server;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
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
import com.koloboke.collect.map.hash.HashObjObjMaps;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
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
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.torch.api.Async;

public class UserCache {

    public static final SimpleDateFormat a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    /** onlineMode */
    private static boolean c;
    /** Username -> UserCacheEntry */
    private final Map<String, UserCache.UserCacheEntry> d = HashObjObjMaps.newMutableMap();
    /** UUID -> UserCacheEntry */
    private final Map<UUID, UserCache.UserCacheEntry> e = HashObjObjMaps.newMutableMap();
    /** All cached GameProfiles */
    private final Deque<GameProfile> f = Queues.newLinkedBlockingDeque();
    private final GameProfileRepository g;
    protected final Gson b;
    /** userCacheFile */
    private final File h;
    private static final ParameterizedType i = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] { UserCache.UserCacheEntry.class};
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
    
    // Torch start
    private final static Cache<String, GameProfile> offlineCache = isOnlineMode() ? null : Caffeine.newBuilder().maximumSize(512).build(); // TODO: configurable size
    // Torch end

    public UserCache(GameProfileRepository gameprofilerepository, File file) {
        this.g = gameprofilerepository;
        this.h = file;
        GsonBuilder gsonbuilder = new GsonBuilder();

        gsonbuilder.registerTypeHierarchyAdapter(UserCache.UserCacheEntry.class, new UserCache.BanEntrySerializer(null));
        this.b = gsonbuilder.create();
        this.b();
    }

    private static GameProfile a(GameProfileRepository profileRepo, String name) {
        if (!isOnlineMode() && !StringUtils.isBlank(name)) return getProfileOffline(name);
        
        final GameProfile[] profile = new GameProfile[1];
        ProfileLookupCallback lookup = new ProfileLookupCallback() {
            @Override
            public void onProfileLookupSucceeded(GameProfile gameprofile) {
                profile[0] = gameprofile;
            }

            @Override
            public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                profile[0] = null;
            }
        };
        
        profileRepo.findProfilesByNames(new String[] { name }, Agent.MINECRAFT, lookup);
        
        return profile[0];
    }

    public static void a(boolean flag) {
        UserCache.c = flag;
    }

    public static boolean isOnlineMode() { return d(); } // OBFHELPER
    private static boolean d() {
        return UserCache.c;
    }

    public void addEntry(GameProfile profile) { this.a(profile); } // OBFHELPER
    public void a(GameProfile gameprofile) {
        if (!isOnlineMode()) return;
        
        this.addEntry(gameprofile, null);
    }

    public void addEntry(GameProfile profile, Date date) { this.a(profile, date); } // OBFHELPER
    private synchronized void a(GameProfile gameprofile, Date date) { // Paper - synchronize
        if (!isOnlineMode()) return;
        
        UUID uuid = gameprofile.getId();

        if (date == null) {
            Calendar calendar = Calendar.getInstance();

            calendar.setTime(new Date());
            calendar.add(2, 1);
            date = calendar.getTime();
        }

        UserCache.UserCacheEntry entry = new UserCache.UserCacheEntry(gameprofile, date, null);

        UserCache.UserCacheEntry cachedEntry = this.e.get(uuid);
        if (cachedEntry != null) { // Paper

            this.d.remove(cachedEntry.getKey().getName().toLowerCase(Locale.ROOT));
            this.f.remove(gameprofile);
        }

        this.d.put(gameprofile.getName().toLowerCase(Locale.ROOT), entry);
        this.e.put(uuid, entry);
        this.f.addFirst(gameprofile);
        if( !org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly ) this.saveUserCache(); // Spigot - skip saving if disabled
    }
    
    @Nullable
    public GameProfile getProfile(String name) {
        if (!isOnlineMode() && !StringUtils.isBlank(name)) {
            return getProfileOffline(name);
        } else {
            return this.getProfileOnline(name);
        }
    }
    
    private static GameProfile getProfileOffline(String name) {
        GameProfile offline = offlineCache.getIfPresent(name);
        
        if (offline == null) {
            offline = new GameProfile(EntityHuman.offlinePlayerUUID(name), name);
            offlineCache.put(name, offline);
        }
        
        return offline;
    }

    @Nullable
    public synchronized GameProfile getProfileOnline(String name) { // Paper - synchronize
        String standardUsername = name.toLowerCase(Locale.ROOT);
        UserCache.UserCacheEntry entry = this.d.get(standardUsername);

        if (entry != null && (new Date()).getTime() >= entry.c.getTime()) {
            this.e.remove(entry.getKey().getId());
            this.d.remove(entry.getKey().getName().toLowerCase(Locale.ROOT));
            this.f.remove(entry.getKey());
            entry = null;
        }
        
        GameProfile profile;
        
        if (entry != null) {
            profile = entry.getKey();
            this.f.remove(profile);
            this.f.addFirst(profile);
        } else {
            profile = a(this.g, name); // Spigot - use correct case for offline players
            if (profile != null) {
                this.addEntry(profile);
                entry = this.d.get(standardUsername);
            }
        }
        
        if( !org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly ) this.saveUserCache(); // Spigot - skip saving if disabled
        return entry == null ? null : entry.getKey();
    }

    public synchronized String[] a() { // Paper - synchronize
        if (!isOnlineMode()) offlineCache.asMap().keySet().toArray();
        
        return this.d.keySet().toArray(new String[this.d.size()]);
    }

    @Nullable public GameProfile getProfileByUUID(UUID uuid) { return this.a(uuid); } // OBFHELPER
    @Nullable public GameProfile a(UUID uuid) {
        UserCache.UserCacheEntry usercache_usercacheentry = this.e.get(uuid);

        return usercache_usercacheentry == null ? null : usercache_usercacheentry.getKey();
    }

    public UserCacheEntry getEntryByUUID(UUID uuid) { return this.b(uuid); } // OBFHELPER
    private UserCache.UserCacheEntry b(UUID uuid) {
        UserCache.UserCacheEntry usercache_usercacheentry = this.e.get(uuid);

        if (usercache_usercacheentry != null) {
            GameProfile gameprofile = usercache_usercacheentry.getKey();

            this.f.remove(gameprofile);
            this.f.addFirst(gameprofile);
        }

        return usercache_usercacheentry;
    }

    public void b() {
        BufferedReader bufferedreader = null;

        try {
            bufferedreader = Files.newReader(this.h, Charsets.UTF_8);
            List list = (List) this.b.fromJson(bufferedreader, UserCache.i);

            this.d.clear();
            this.e.clear();
            this.f.clear();
            if (list != null) {
                Iterator iterator = Lists.reverse(list).iterator();

                while (iterator.hasNext()) {
                    UserCache.UserCacheEntry usercache_usercacheentry = (UserCache.UserCacheEntry) iterator.next();

                    if (usercache_usercacheentry != null) {
                        this.addEntry(usercache_usercacheentry.getKey(), usercache_usercacheentry.b());
                    }
                }
            }
        } catch (FileNotFoundException filenotfoundexception) {
            ;
            // Spigot Start
        } catch (com.google.gson.JsonSyntaxException ex) {
            JsonList.a.warn( "Usercache.json is corrupted or has bad formatting. Deleting it to prevent further issues." );
            this.h.delete();
            // Spigot End
        } catch (JsonParseException jsonparseexception) {
            ;
        } finally {
            IOUtils.closeQuietly(bufferedreader);
        }

    }

    // Paper start
    @Async public void saveUserCache() { this.c(); } // OBFHELPER
    @Async public void c() {
        c(true);
    }
    public void c(boolean asyncSave) {
        // Paper end
        String s = this.b.toJson(this.matchEntriesWithLimitedSize(org.spigotmc.SpigotConfig.userCacheCap));
        Runnable save = () -> {

            BufferedWriter bufferedwriter = null;

            try {
                bufferedwriter = Files.newWriter(this.h, Charsets.UTF_8);
                bufferedwriter.write(s);
                return;
            } catch (FileNotFoundException filenotfoundexception) {
                return;
            } catch (IOException ioexception) {
                ;
            } finally {
                IOUtils.closeQuietly(bufferedwriter);
            }
            // Paper start
        };
        if (asyncSave) {
            MCUtil.scheduleAsyncTask(save);
        } else {
            save.run();
        }
        // Paper end

    }

    public List<UserCacheEntry> matchEntriesWithLimitedSize(int size) { return this.a(size); } // OBFHELPER
    private List<UserCache.UserCacheEntry> a(int size) {
        ArrayList<UserCacheEntry> list = Lists.newArrayList();
        
        Iterator<GameProfile> itr = Iterators.limit(this.f.iterator(), size);
        while (itr.hasNext()) list.add(this.getEntryByUUID(itr.next().getId()));
        
        return list;
    }

    public class UserCacheEntry {

        private final GameProfile b;
        private final Date c;

        private UserCacheEntry(GameProfile gameprofile, Date date) {
            this.b = gameprofile;
            this.c = date;
        }

        public GameProfile getKey() { return this.a(); } // OBFHELPER
        public GameProfile a() {
            return this.b;
        }

        public Date b() {
            return this.c;
        }

        UserCacheEntry(GameProfile gameprofile, Date date, Object object) {
            this(gameprofile, date);
        }
    }

    class BanEntrySerializer implements JsonDeserializer<UserCache.UserCacheEntry>, JsonSerializer<UserCache.UserCacheEntry> {

        private BanEntrySerializer() {}

        public JsonElement a(UserCache.UserCacheEntry usercache_usercacheentry, Type type, JsonSerializationContext jsonserializationcontext) {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("name", usercache_usercacheentry.getKey().getName());
            UUID uuid = usercache_usercacheentry.getKey().getId();

            jsonobject.addProperty("uuid", uuid == null ? "" : uuid.toString());
            jsonobject.addProperty("expiresOn", UserCache.a.format(usercache_usercacheentry.b()));
            return jsonobject;
        }

        public UserCache.UserCacheEntry a(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            if (jsonelement.isJsonObject()) {
                JsonObject jsonobject = jsonelement.getAsJsonObject();
                JsonElement jsonelement1 = jsonobject.get("name");
                JsonElement jsonelement2 = jsonobject.get("uuid");
                JsonElement jsonelement3 = jsonobject.get("expiresOn");

                if (jsonelement1 != null && jsonelement2 != null) {
                    String s = jsonelement2.getAsString();
                    String s1 = jsonelement1.getAsString();
                    Date date = null;

                    if (jsonelement3 != null) {
                        try {
                            date = UserCache.a.parse(jsonelement3.getAsString());
                        } catch (ParseException parseexception) {
                            date = null;
                        }
                    }

                    if (s1 != null && s != null) {
                        UUID uuid;

                        try {
                            uuid = UUID.fromString(s);
                        } catch (Throwable throwable) {
                            return null;
                        }

                        return UserCache.this.new UserCacheEntry(new GameProfile(uuid, s1), date, null);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        public JsonElement serialize(UserCacheEntry object, Type type, JsonSerializationContext jsonserializationcontext) { // CraftBukkit - decompile error
            return this.a(object, type, jsonserializationcontext);
        }

        @Override
        public UserCacheEntry deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException { // CraftBukkit - decompile error
            return this.a(jsonelement, type, jsondeserializationcontext);
        }

        BanEntrySerializer(Object object) {
            this();
        }
    }
}
