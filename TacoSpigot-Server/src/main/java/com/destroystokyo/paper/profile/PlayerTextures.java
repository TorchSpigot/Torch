package com.destroystokyo.paper.profile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
* A player's texture data, including skin and cape.
*/
public final class PlayerTextures {
   private final AccountProfile profile;
   private final TextureData skinData, capeData;

   public PlayerTextures(AccountProfile profile, TextureData skinData, TextureData capeData) {
       Preconditions.checkNotNull(profile, "Null profile");
       Preconditions.checkArgument(profile.hasProperties(), "No properties for %s", profile);
       Preconditions.checkArgument(getProperty(profile.getProperties()) != null, "No texture data for %s", profile);
       this.profile = profile;
       this.skinData = skinData;
       this.capeData = capeData;
   }

   /**
    * Return the player's skin data, or null if the player has no skin
    *
    * @return the player's skin data, or null if none
    */
   public TextureData getSkinData() {
       return skinData;
   }

   /**
    * Return the player's cape data, or null if the player has no cape
    *
    * @return the player's cape data, or null if none
    */
   public TextureData getCapeData() {
       return skinData;
   }

   /**
    * Return the url of the player's skin, or null if the player has no skin
    *
    * @return the url of the player's skin, or null if none
    */
   public URL getSkin() {
       return getSkinData() == null ? null : getSkinData().getUrl();
   }

   /**
    * Return if the player has a skin
    *
    * @return the player has a skin
    */
   public boolean hasSkin() {
       return getSkinData() != null;
   }

   /**
    * Return if the player has a cape
    *
    * @return the player has a cape
    */
   public boolean hasCape() {
       return getCapeData() != null;
   }

   /**
    * Return the url of the player's cape, or null if the player has no cape
    *
    * @return the url of the player's cape, or null if none
    */
   public URL getCape() {
       return getCapeData() == null ? null : getCapeData().getUrl();
   }

   /**
    * Return if the player's skin has slim arms, or false if the player has no skin.
    * <p>Alex style skins should return true. Steve style skins should return false.
    * If the player has no skin, returns false.</p>
    *
    * @return if the player has slim arms
    */
   public boolean isSlimSkin() {
       String model;
       return hasSkin() && (model = getSkinData().getMetadata().get("model")) != null && model.equals("slim");
   }

   /**
    * Get the profile this texture is associated with
    *
    * @return the profile
    */
   public AccountProfile getProfile() {
       return profile;
   }

   public static PlayerTextures parseTextures(final AccountProfile profile) {
       Preconditions.checkNotNull(profile, "Null profile");
       Preconditions.checkArgument(profile.hasProperties(), "No properties for %s", profile);
       ProfileProperty texture = getProperty(profile.getProperties());
       if (texture == null) return null;
       TextureData skinData = null;
       TextureData capeData = null;
       try {
           JsonObject textureData = new JsonParser().parse(new String(Base64.getDecoder().decode(texture.getValue()), Charsets.UTF_8)).getAsJsonObject();
           Preconditions.checkArgument(UUIDUtils.toMojangString(profile.getId()).equals(textureData.get("id").getAsString()), "Unexpected id: %s", textureData.get("id").getAsString());
           JsonObject textures = textureData.get("textures").getAsJsonObject();
           JsonObject skinJson = textures.getAsJsonObject("SKIN");
           JsonObject capeJson = textures.getAsJsonObject("CAPE");
           if (skinJson != null) {
               String url = skinJson.getAsJsonPrimitive("url").getAsString();
               ImmutableMap<String, String> metadata;
               JsonObject metadataJson = skinJson.getAsJsonObject("metadata");
               if (metadataJson != null) {
                   ImmutableMap.Builder<String, String> metadataBuilder = ImmutableMap.builder();
                   for (Map.Entry<String, JsonElement> entry : metadataJson.entrySet()) {
                       metadataBuilder.put(entry.getKey(), entry.getValue().getAsString());
                   }
                   metadata = metadataBuilder.build();
               } else {
                   metadata = ImmutableMap.of();
               }
               skinData = new TextureData(url, metadata);
           }
           if (capeJson != null) {
               String url = capeJson.getAsJsonPrimitive("url").getAsString();
               ImmutableMap<String, String> metadata;
               JsonObject metadataJson = capeJson.getAsJsonObject("metadata");
               if (metadataJson != null) {
                   ImmutableMap.Builder<String, String> metadataBuilder = ImmutableMap.builder();
                   for (Map.Entry<String, JsonElement> entry : metadataJson.entrySet()) {
                       metadataBuilder.put(entry.getKey(), entry.getValue().getAsString());
                   }
                   metadata = metadataBuilder.build();
               } else {
                   metadata = ImmutableMap.of();
               }
               capeData = new TextureData(url, metadata);
           }
           return new PlayerTextures(profile, skinData, capeData);
       } catch (JsonParseException | ClassCastException | IllegalStateException e) { // IllegalStateException or ClassCastException is thrown by 'getAs()' methods
           throw new IllegalArgumentException("Invalid json in textures", e);
       }
   }

   private volatile Boolean signedByMojang;

   /**
    * Return if the texture has been singed by mojang
    * <p>Clients will not accept textures that have not been signed by mojang.</p>
    *
    * @return if signed by mojang
    */
   public boolean isSignedByMojang() {
       // Cache if we have a signature, to avoid verifying the texture twice
       if (signedByMojang == null) {
               synchronized (this) {
                   if (signedByMojang == null) {
                       signedByMojang = isSigned() && getProperty().isSignedByMojang();
                   }
               }
       }
       return signedByMojang;
   }

   /**
    * Return if the texture has a signature.
    *
    * @return if the texture has a signature
    */
   public boolean isSigned() {
       return getProperty().isSigned();
   }

   public ProfileProperty getProperty() {
       return getProperty(profile.getProperties());
   }

   private static ProfileProperty getProperty(ProfileProperties properties) {
       // Don't worry, getProperties() is immutable and IterableSet has a defined iteration order
       return Iterables.getFirst(properties.getProperties("textures"), null);
   }

   @Override
   public boolean equals(Object o) {
       if (this == o) return true;
       if (o == null || getClass() != o.getClass()) return false;

       PlayerTextures that = (PlayerTextures) o;

       if (!profile.equals(that.profile)) return false;
       if (skinData != null ? !skinData.equals(that.skinData) : that.skinData != null) return false;
       return !(capeData != null ? !capeData.equals(that.capeData) : that.capeData != null);

   }

   @Override
   public int hashCode() {
       return profile.hashCode();
   }

   public static final class TextureData {
       private final URL url;
       private final ImmutableMap<String, String> metadata;

       public URL getUrl() {
           return url;
       }

       public ImmutableMap<String, String> getMetadata() {
           return metadata;
       }

       public TextureData(String url, ImmutableMap<String, String> metadata) {
           Preconditions.checkNotNull(url, "Null url");
           Preconditions.checkNotNull(metadata, "Null metadata");
           try {
               this.url = new URL(url);
               this.metadata = metadata;
           } catch (MalformedURLException e) {
               throw new IllegalArgumentException("Invalid url " + url, e);
           }
       }

       public TextureData(URL url, ImmutableMap<String, String> metadata) {
           Preconditions.checkNotNull(url, "Null url");
           Preconditions.checkNotNull(metadata, "Null metadata");
           this.url = url;
           this.metadata = metadata;
       }

       @Override
       public boolean equals(Object o) {
           if (this == o) return true;
           if (o == null || getClass() != o.getClass()) return false;

           TextureData data = (TextureData) o;

           if (!url.equals(data.url)) return false;
           return metadata.equals(data.metadata);

       }

       @Override
       public int hashCode() {
           return url.hashCode();
       }

       @Override
       public String toString() {
           return url.toString();
       }
   }
}