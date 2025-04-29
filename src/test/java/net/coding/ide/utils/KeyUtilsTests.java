package net.coding.ide.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.junit.Assert.*;

public class KeyUtilsTests {
    private static final Logger logger = LoggerFactory.getLogger(KeyUtilsTests.class);

    @Test
    public void testFingerprint() {
        String publicKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDQqB6k/yQF9qj4KJXZhkH5BS5C5TZgBEgEscVHHIX0K4D8yVyH9/wV/Qf4IA7nHG/WOyJZmVHfuHHK5TkgIB/nZK4p7QJI/kK1rVgCF8NJHWHpZk9yGGRwZNX+4yRKP+hDRUZbvmvvZQUwfI4Ufj6jGjyZW3kk5pV/r5R1znnCiNMU pioneers";

        String fingerprint = KeyUtils.fingerprint(publicKey);
        assertNotNull("Fingerprint should not be null", fingerprint);
        assertTrue("Fingerprint should match expected pattern", fingerprint.matches("([a-f0-9]{2}:){15}[a-f0-9]{2}"));
    }

    @Test
    public void testKeyToStringWithPublicKey() throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        String result = KeyUtils.keyToString(publicKey, "testuser");
        assertNotNull("Public key string should not be null", result);
        assertTrue("Public key string should start with ssh-rsa", result.startsWith("ssh-rsa "));
        assertTrue("Public key string should end with expected suffix", result.endsWith("testuser@WebIDE"));
    }

    @Test
    public void testKeyToStringWithPrivateKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        String result = KeyUtils.keyToString(privateKey);
        logger.debug("Generated private key string: {}", result);

        assertNotNull("Private key string should not be null", result);
        assertTrue("Private key string should contain BEGIN marker",
            result.contains("-----BEGIN RSA PRIVATE KEY-----") ||
            result.contains("-----BEGIN PRIVATE KEY-----"));
        assertTrue("Private key string should contain END marker",
            result.contains("-----END RSA PRIVATE KEY-----") ||
            result.contains("-----END PRIVATE KEY-----"));
    }

    @Test
    public void testIsKeyExist() {
        JsonArray keys = new JsonArray();
        JsonObject key1 = new JsonObject();
        key1.addProperty("key", "test-key-1");
        keys.add(key1);

        JsonObject key2 = new JsonObject();
        key2.addProperty("key", "test-key-2");
        keys.add(key2);

        assertTrue("Should find existing key",
            KeyUtils.isKeyExist(keys, "test-key-1", obj -> obj.get("key").getAsString()));
        assertFalse("Should not find non-existent key",
            KeyUtils.isKeyExist(keys, "non-existent-key", obj -> obj.get("key").getAsString()));
        assertFalse("Should handle null key",
            KeyUtils.isKeyExist(keys, null, obj -> obj.get("key").getAsString()));
    }
}
