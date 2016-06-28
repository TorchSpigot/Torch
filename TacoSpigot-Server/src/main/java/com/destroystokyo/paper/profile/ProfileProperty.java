package com.destroystokyo.paper.profile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;

public final class ProfileProperty {
    private final String name, value, signature;

    public ProfileProperty(String name, String value, String signature) {
        this.name = Preconditions.checkNotNull(name, "Name of the property can't be null");
        this.value = Preconditions.checkNotNull(value, "Value of the property can't be null");
        this.signature = signature;
    }

    public ProfileProperty(String name, String value) {
        this(name, value, null);
    }

    /**
     * Return the name of this property
     *
     * @return the name of this property
     */
    public String getName() {
        return name;
    }

    /**
     * Return the value of this property
     *
     * @return the value of this property
     */
    public String getValue() {
        return value;
    }

    /**
     * Return the signature of this property
     * <p>
     * This performs no verification of the returned signature.
     *
     * @return the signature of this property
     * @throws IllegalStateException if the property is not singed
     */
    public String getSignature() {
        Preconditions.checkState(signature != null, "Property is not signed");
        return signature;
    }

    /**
     * Return if the property is signed
     *
     * @return if the property is signed
     */
    public boolean isSigned() {
        return signature != null;
    }

    /**
     * Return if the signature is valid with the specified public key
     *
     * @param key the public key
     * @return if valid
     * @throws IllegalArgumentException if the key is invalid
     * @throws IllegalStateException    if the property is not signed
     * @throws RuntimeException         if unable to verify for some other reason
     */
    public boolean isSignatureValid(PublicKey key) {
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(key);
            signature.update(this.value.getBytes());
            return signature.verify(Base64.getDecoder().decode(getSignature()));
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid key", e);
        } catch (SignatureException e) {
            throw new RuntimeException("Unable to verify", e);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Couldn't find required algorithm", e);
        }
    }

    /**
     * Return if the signature has been signed by mojang
     *
     * @return if valid with mojang
     * @throws IllegalStateException if the property is not signed
     * @throws RuntimeException      if unable to verify for some other reason
     */
    public boolean isSignedByMojang() {
        try {
            return isSignatureValid(YGGDRASIL_PUBLIC_KEY);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid mojang key", e);
        }
    }

    public static final PublicKey YGGDRASIL_PUBLIC_KEY;

    static {
        try (
                // NOTE: Update this if yggdrasil public key location changes
                BufferedInputStream in = new BufferedInputStream(ProfileProperty.class.getResourceAsStream("/yggdrasil_session_pubkey.der"))
        ) {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(ByteStreams.toByteArray(in));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            YGGDRASIL_PUBLIC_KEY = factory.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            throw new AssertionError("Missing/invalid yggdrasil public key!", e);
        } catch (IOException e) {
            throw new AssertionError("Couldn't load key", e);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Missing RSA", e);
        }
    }
}