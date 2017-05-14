package net.minecraft.server;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import lombok.Getter;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;
import org.torch.api.Anaphase;
import org.torch.api.Async;
import org.torch.server.cache.TorchUserCache;

public class UserCache implements org.torch.api.TorchServant {
    @Getter private final TorchUserCache reactor;

    public static final SimpleDateFormat a = TorchUserCache.DATE_FORMAT;
    /** onlineMode */
    @Anaphase private static boolean c;
    /** Username -> UserCacheEntry */
    // private final Map<String, UserCache.UserCacheEntry> d = HashObjObjMaps.newMutableMap();
    /** UUID -> UserCacheEntry */
    // private final Map<UUID, UserCache.UserCacheEntry> e = HashObjObjMaps.newMutableMap();
    /** All cached GameProfiles */
    // private final Deque<GameProfile> f = Queues.newLinkedBlockingDeque();
    private final GameProfileRepository g;
    protected final Gson b;
    /** userCacheFile */
    private final File h;
    private static final ParameterizedType i = TorchUserCache.type;

    public UserCache(GameProfileRepository repo, File file) {
        reactor = new TorchUserCache(repo, file, this);
        
        g = repo;
        b = reactor.getGson();
        h = file;
    }

    private static GameProfile a(GameProfileRepository profileRepo, String name) {
        return TorchUserCache.matchProfile(profileRepo, name);
    }

    public static void setOnlineMode(boolean flag) { a(flag); } // OBFHELPER
    public static void a(boolean flag) {
        UserCache.c = flag;
    }

    public static boolean isOnlineMode() { return d(); } // OBFHELPER
    private static boolean d() {
        return UserCache.c;
    }

    public void a(GameProfile profile) {
        reactor.offerCache(profile);
    }

    private void a(GameProfile profile, Date date) {
        reactor.offerCache(profile, date);
    }
    
    @Nullable
    public GameProfile getProfile(String name) {
        return reactor.requestProfile(name);
    }

    public String[] a() {
        return reactor.getCachedUsernames();
    }
    
    @Deprecated
    public GameProfile peekCachedProfile(String username) {
        return reactor.peekCachedProfile(username);
    }

    /*
    @Nullable
    public GameProfile a(UUID uuid) {
        return reactor.peekCachedProfile(uuid);
    }

    private UserCache.UserCacheEntry b(UUID uuid) {
        return reactor.peekCachedEntry(uuid).toLegacy();
    } */

    public void b() {
        reactor.load();
    }

    // Paper start
    @Async
    public void c() {
        reactor.save();
    }
    public void c(boolean asyncSave) {
        reactor.save(asyncSave);
    }

    /* private List<UserCache.UserCacheEntry> a(int size) {
        return reactor.matchEntries(size);
    } */

    @Deprecated
    public class UserCacheEntry {

        private final GameProfile b;
        private final Date c;

        public UserCacheEntry(GameProfile gameprofile, Date date) {
            this.b = gameprofile;
            this.c = date;
        }

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

    /* class BanEntrySerializer implements JsonDeserializer<UserCache.UserCacheEntry>, JsonSerializer<UserCache.UserCacheEntry> {

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
        public JsonElement serialize(UserCacheEntry object, java.reflect.Type type, JsonSerializationContext jsonserializationcontext) { // CraftBukkit - decompile error
            return this.a(object, type, jsonserializationcontext);
        }

        @Override
        public UserCacheEntry deserialize(JsonElement jsonelement, java.reflect.Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException { // CraftBukkit - decompile error
            return this.a(jsonelement, type, jsondeserializationcontext);
        }

        BanEntrySerializer(Object object) {
            this();
        }
    } */
}
