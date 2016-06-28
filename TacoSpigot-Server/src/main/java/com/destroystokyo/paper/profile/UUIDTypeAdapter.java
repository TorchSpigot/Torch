package com.destroystokyo.paper.utils.json;

import java.io.IOException;
import java.util.UUID;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import com.destroystokyo.paper.profile.UUIDUtils;

public class UUIDTypeAdapter extends TypeAdapter<UUID> {
    private final boolean mojangStyle;
    private final boolean lenient;

    private UUIDTypeAdapter(boolean mojangStyle, boolean lenient) {
        if (mojangStyle && !lenient) throw new AssertionError("Mojang style should imply lenient");
        this.mojangStyle = mojangStyle;
        this.lenient = lenient;
    }

    public static UUIDTypeAdapter create() {
        return new UUIDTypeAdapter(false, false);
    }

    public static UUIDTypeAdapter createMojang() {
        return new UUIDTypeAdapter(true, true);
    }

    public static UUIDTypeAdapter createLenient() {
        return new UUIDTypeAdapter(false, true);
    }

    @Override
    public void write(JsonWriter out, UUID value) throws IOException {
        if (value != null) {
            out.value(mojangStyle ? UUIDUtils.toMojangString(value) : value.toString());
        } else {
            out.nullValue();
        }
    }

    @Override
    public UUID read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.NULL) {
            String s = in.nextString();
            return lenient ? UUIDUtils.fromString(s) : UUID.fromString(s);
        } else {
            in.nextNull();
            return null;
        }
    }
}