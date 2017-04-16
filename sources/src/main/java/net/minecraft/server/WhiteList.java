package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Iterator;
import org.bukkit.Bukkit;

public class WhiteList extends JsonList<GameProfile, WhiteListEntry> {

    public WhiteList(File file) {
        super(file);
    }

    @Override
	protected JsonListEntry<GameProfile> a(JsonObject jsonobject) {
        return new WhiteListEntry(jsonobject);
    }

    public boolean isWhitelisted(GameProfile profile) {
        if (Bukkit.getOnlineMode()) {
        	return this.contains(profile);
        } else {
        	for (WhiteListEntry entry : this.e().values()) {
        		if (entry.getKey().getName().equalsIgnoreCase(profile.getName())) return true;
        	}
        }
		return false;
    }

    @Override
	public String[] getEntries() {
        String[] astring = new String[this.e().size()];
        int i = 0;

        WhiteListEntry whitelistentry;

        for (Iterator iterator = this.e().values().iterator(); iterator.hasNext(); astring[i++] = whitelistentry.getKey().getName()) {
            whitelistentry = (WhiteListEntry) iterator.next();
        }

        return astring;
    }

    protected String b(GameProfile gameprofile) {
        return gameprofile.getId().toString();
    }

    public GameProfile a(String s) {
        Iterator iterator = this.e().values().iterator();

        WhiteListEntry whitelistentry;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            whitelistentry = (WhiteListEntry) iterator.next();
        } while (!s.equalsIgnoreCase(whitelistentry.getKey().getName()));

        return whitelistentry.getKey();
    }

    @Override
	protected String a(GameProfile object) {
        return this.b(object);
    }
}
