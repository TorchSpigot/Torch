package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import static org.torch.server.cache.TorchUserCache.authUUID;

import java.io.File;
import java.util.Iterator;

import org.torch.server.cache.Caches;

public class GameProfileBanList extends JsonList<GameProfile, GameProfileBanEntry> {

    public GameProfileBanList(File file) {
        super(file);
    }

    @Override
    protected JsonListEntry<GameProfile> a(JsonObject jsonobject) {
        return new GameProfileBanEntry(jsonobject);
    }

    public boolean isBanned(GameProfile gameprofile) {
        return this.d(gameprofile);
    }

    @Override
    public String[] getEntries() {
        String[] values = new String[this.getMap().size()]; int index = 0;
        for (GameProfileBanEntry entry : this.getMap().values()) values[index++] = entry.getKey().getName();

        return values;
    }

    protected String b(GameProfile profile) {
        return authUUID() ? Caches.objectString(profile) : Caches.toLowerCase(profile.getName()); // Torch - use cache
    }

    public GameProfile a(String s) {
        Iterator iterator = this.e().values().iterator();

        GameProfileBanEntry gameprofilebanentry;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            gameprofilebanentry = (GameProfileBanEntry) iterator.next();
        } while (!s.equalsIgnoreCase(gameprofilebanentry.getKey().getName()));

        return gameprofilebanentry.getKey();
    }
}
