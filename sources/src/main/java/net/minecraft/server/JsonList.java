package net.minecraft.server;

import com.google.common.base.Charsets;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import static org.torch.server.TorchServer.logger;

public class JsonList<K, V extends JsonListEntry<K>> {

    protected static final Logger a = logger;
    protected final Gson b;
    private final File c;
    private final Map<String, V> d = HashObjObjMaps.newMutableMap();
    private boolean e = true;
    private static final ParameterizedType f = new ParameterizedType() {
        @Override
		public Type[] getActualTypeArguments() {
            return new Type[] { JsonListEntry.class};
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
    /** Only used to reduce entries overread */
    protected boolean modified = false;
    protected String[] lastEntries; // TODO: always overread twice
    // Torch end

    public JsonList(File file) {
        this.c = file;
        GsonBuilder gsonbuilder = (new GsonBuilder()).setPrettyPrinting();

        gsonbuilder.registerTypeHierarchyAdapter(JsonListEntry.class, new JsonList.JsonListEntrySerializer(null));
        this.b = gsonbuilder.create();
    }

    public boolean isEnabled() {
        return this.e;
    }

    public void a(boolean flag) {
        this.e = flag;
    }

    public File c() {
        return this.c;
    }

    public void add(V v0) {
    	modified = true; // Torch
        this.d.put(this.a(v0.getKey()), v0);

        this.save();
    }

    public V get(K k0) {
        this.h();
        return this.d.get(this.a(k0)); // CraftBukkit - fix decompile error
    }

    public void remove(K k0) {
    	modified = true; // Torch
        this.d.remove(this.a(k0));

        this.save();
    }

    public String[] getEntries() {
        return this.d.keySet().toArray(new String[this.d.size()]);
    }

    // CraftBukkit start
    public Collection<V> getValues() {
        return this.d.values();
    }
    // CraftBukkit end

    public boolean isEmpty() {
        return this.d.size() < 1;
    }

    protected String a(K k0) {
        return k0.toString();
    }

    public boolean contains(K k0) { return this.d(k0); } // OBFHELPER
    protected boolean d(K k0) {
        return this.d.containsKey(this.a(k0));
    }

    private void h() { // Torch - slight optimization
        Iterator<V> iterator = this.d.values().iterator();
        while (iterator.hasNext()) if (iterator.next().hasExpired()) iterator.remove();
    }

    protected JsonListEntry<K> a(JsonObject jsonobject) {
        return new JsonListEntry((Object) null, jsonobject);
    }

    public Map<String, V> getMap() { return this.e(); } // OBFHELPER
    protected Map<String, V> e() {
        return this.d;
    }

    public void save() {
        Collection<V> values = this.d.values();
        
        MCUtil.scheduleAsyncTask(() -> {
        	String jsonString = this.b.toJson(values);
        	BufferedWriter writer = null;
            
            try {
                writer = Files.newWriter(this.c, Charsets.UTF_8);
                writer.write(jsonString);
            } catch (IOException io) {
            	logger.warn("Failed to save {}", this.c.getName());
            	io.printStackTrace();
			} finally {
                IOUtils.closeQuietly(writer);
            }
        });
    }

    public void load() throws FileNotFoundException {
    	modified = true; // Torch
        Collection collection = null;
        BufferedReader bufferedreader = null;

        try {
            bufferedreader = Files.newReader(this.c, Charsets.UTF_8);
            collection = (Collection) this.b.fromJson(bufferedreader, JsonList.f);
        // Spigot Start
        } catch ( java.io.FileNotFoundException ex )
        {
            org.bukkit.Bukkit.getLogger().log( java.util.logging.Level.INFO, "Unable to find file {0}, creating it.", this.c );
        } catch ( com.google.gson.JsonSyntaxException ex )
        {
            org.bukkit.Bukkit.getLogger().log( java.util.logging.Level.WARNING, "Unable to read file {0}, backing it up to {0}.backup and creating new copy.", this.c );
            File backup = new File( this.c + ".backup" );
            this.c.renameTo( backup );
            this.c.delete();
        // Spigot End
        } finally {
            IOUtils.closeQuietly(bufferedreader);
        }

        if (collection != null) {
            this.d.clear();
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                JsonListEntry jsonlistentry = (JsonListEntry) iterator.next();

                if (jsonlistentry.getKey() != null) {
                    this.d.put(this.a((K) jsonlistentry.getKey()), (V) jsonlistentry); // CraftBukkit - fix decompile error
                }
            }
        }

    }

    class JsonListEntrySerializer implements JsonDeserializer<JsonListEntry<K>>, JsonSerializer<JsonListEntry<K>> {

        private JsonListEntrySerializer() {}

        public JsonElement a(JsonListEntry<K> jsonlistentry, Type type, JsonSerializationContext jsonserializationcontext) {
            JsonObject jsonobject = new JsonObject();

            jsonlistentry.a(jsonobject);
            return jsonobject;
        }

        public JsonListEntry<K> a(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            if (jsonelement.isJsonObject()) {
                JsonObject jsonobject = jsonelement.getAsJsonObject();

                return JsonList.this.a(jsonobject);
            } else {
                return null;
            }
        }

        @Override
		public JsonElement serialize(JsonListEntry<K> object, Type type, JsonSerializationContext jsonserializationcontext) { // CraftBukkit - fix decompile error
            return this.a(object, type, jsonserializationcontext);
        }

        @Override
		public JsonListEntry<K> deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException { // CraftBukkit - fix decompile error
            return this.a(jsonelement, type, jsondeserializationcontext);
        }

        JsonListEntrySerializer(Object object) {
            this();
        }
    }
}
