package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import static org.torch.server.cache.TorchUserCache.authUUID;

import java.io.File;
import java.util.Iterator;

public class WhiteList extends JsonList<GameProfile, WhiteListEntry> {

    public WhiteList(File file) {
        super(file);
    }

    @Override
    protected JsonListEntry<GameProfile> a(JsonObject jsonobject) {
        return new WhiteListEntry(jsonobject);
    }

    public boolean isWhitelisted(GameProfile profile) {
        return this.contains(profile);
    }

    @Override
    public String[] getEntries() {
        String[] values = new String[this.getMap().size()]; int index = 0;
        for (WhiteListEntry entry : this.getMap().values()) values[index++] = entry.getKey().getName();

        return values;
    }

    protected String b(GameProfile profile) {
        return authUUID() ? profile.getId().toString() : profile.getName().toLowerCase();
    }

    public GameProfile a(String s) {
        Iterator<WhiteListEntry> iterator = this.e().values().iterator();

        WhiteListEntry whitelistentry;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            whitelistentry = iterator.next();
        } while (!s.equalsIgnoreCase(whitelistentry.getKey().getName()));

        return whitelistentry.getKey();
    }

    @Override
    protected String a(GameProfile object) {
        return this.b(object);
    }
}
