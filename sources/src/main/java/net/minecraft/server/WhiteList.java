package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Iterator;

import org.torch.server.TorchServer;

public class WhiteList extends JsonList<GameProfile, WhiteListEntry> {

    public WhiteList(File file) {
        super(file);
    }

    @Override
    protected JsonListEntry<GameProfile> a(JsonObject jsonobject) {
        return new WhiteListEntry(jsonobject);
    }

    public boolean isWhitelisted(GameProfile profile) {
        if (TorchServer.authUUID()) {
            return this.contains(profile);
        } else {
            return this.contains(profile) || this.contains(new GameProfile(profile.getId(), profile.getName().toLowerCase())); // Support for offline servers
        }
    }

    @Override
    public String[] getEntries() {
        String[] values = new String[this.getMap().size()]; int index = 0;
        for (WhiteListEntry entry : this.getMap().values()) values[index++] = entry.getKey().getName();

        return values;
    }

    protected String b(GameProfile gameprofile) {
        return super.a(gameprofile); // Torch - use cache
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
