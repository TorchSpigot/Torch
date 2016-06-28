package com.destroystokyo.paper.utils.json;

import java.io.IOException;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ProfilePropertyTypeAdapter extends TypeAdapter<ProfileProperty> {
    @Override
    public void write(JsonWriter out, ProfileProperty property) throws IOException {
        if (property != null) {
            out.beginObject();
            out.name("name");
            out.value(property.getName());
            out.name("value");
            out.value(property.getName());
            if (property.isSigned()) {
                out.name("signature");
                out.name(property.getSignature());
            }
            out.endObject();
        }
    }

    @Override
    public ProfileProperty read(JsonReader in) throws IOException {
        return null;
    }
}