package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.torch.server.TorchServer;

public class WhiteList extends JsonList<GameProfile, WhiteListEntry> {

    public WhiteList(File file) {
        super(file);
    }

    @Override
	protected JsonListEntry<GameProfile> a(JsonObject jsonobject) {
        return new WhiteListEntry(jsonobject);
    }

    public boolean isWhitelisted(GameProfile profile) { // Torch - skip UUID check for offline servers
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
    	if (!modified) return lastEntries; // Returns cached entries directly if un-modified
    	
        String[] values = new String[this.getMap().size()]; int index = 0;
		for (WhiteListEntry entry : this.getMap().values()) values[index++] = entry.getKey().getName();
    	
		modified = false; // Mark as un-modified, skip next time
    	return lastEntries = values;
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
