package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import java.io.File;
import java.util.Iterator;

public class OpList extends JsonList<GameProfile, OpListEntry> {

    public OpList(File file) {
        super(file);
    }

    @Override
    protected JsonListEntry<GameProfile> a(JsonObject jsonobject) {
        return new OpListEntry(jsonobject);
    }

    @Override
    public String[] getEntries() {
        String[] astring = new String[this.e().size()];
        int i = 0;

        OpListEntry oplistentry;

        for (Iterator iterator = this.e().values().iterator(); iterator.hasNext(); astring[i++] = oplistentry.getKey().getName()) {
            oplistentry = (OpListEntry) iterator.next();
        }

        return astring;
    }

    public int getPermissionLevel(GameProfile gameprofile) { // Torch - rename to avoid decompile error
        OpListEntry oplistentry = this.get(gameprofile);

        return oplistentry != null ? oplistentry.a() : 0;
    }

    public boolean b(GameProfile gameprofile) {
        OpListEntry oplistentry = this.get(gameprofile);

        return oplistentry != null ? oplistentry.b() : false;
    }

    protected String c(GameProfile profile) { // PAIL: toStringKey
        return profile.getName().toLowerCase();
    }

    public GameProfile a(String s) {
        Iterator iterator = this.e().values().iterator();

        OpListEntry oplistentry;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            oplistentry = (OpListEntry) iterator.next();
        } while (!s.equalsIgnoreCase(oplistentry.getKey().getName()));

        return oplistentry.getKey();
    }

    @Override
    protected String a(GameProfile object) {
        return this.c(object);
    }
}
