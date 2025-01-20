package net.coding.ide.service;

import net.coding.ide.entity.ConfigEntity;
import net.coding.ide.model.Key;
import net.coding.ide.model.Workspace;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KeyManagerImplTests {

    @Mock
    private ConfigService configService;

    @Mock
    private Workspace workspace;

    @InjectMocks
    private KeyManagerImpl keyManager;

    private File mockKeyDir;

    @Before
    public void setUp() throws IOException {
        ReflectionTestUtils.setField(keyManager, "username", "testuser");
        mockKeyDir = new File(System.getProperty("java.io.tmpdir"), "test-key-dir");
        mockKeyDir.mkdirs();
        when(workspace.getKeyDir()).thenReturn(mockKeyDir);
    }

    @Test
    public void testIsKeyExist() {
        when(configService.getByKey("publicKey")).thenReturn(new ConfigEntity());
        assertTrue(keyManager.isKeyExist());

        when(configService.getByKey("publicKey")).thenReturn(null);
        assertFalse(keyManager.isKeyExist());
    }

    @Test
    public void testIsKeyExistForWorkspace() throws IOException {
        File privateKeyFile = new File(mockKeyDir, "id_rsa");
        File publicKeyFile = new File(mockKeyDir, "id_rsa.pub");

        try {
            privateKeyFile.createNewFile();
            publicKeyFile.createNewFile();
            assertTrue(keyManager.isKeyExist(workspace));
        } finally {
            privateKeyFile.delete();
            publicKeyFile.delete();
        }

        assertFalse(keyManager.isKeyExist(workspace));
    }

    @Test
    public void testGenerateKey() throws IOException {
        Key key = keyManager.generateKey();

        assertNotNull(key);
        assertNotNull(key.getPrivateKey());
        assertNotNull(key.getPublicKey());
        assertNotNull(key.getFingerprint());

        verify(configService).setCfg("publicKey", "publicKey", key.getPublicKey());
        verify(configService).setCfg("privateKey", "privateKey", key.getPrivateKey());
        verify(configService).setCfg("fingerPrint", "fingerPrint", key.getFingerprint());
    }

    @Test
    public void testGetKey() {
        when(configService.getValue("privateKey")).thenReturn("private-key-value");
        when(configService.getValue("publicKey")).thenReturn("public-key-value");
        when(configService.getValue("fingerPrint")).thenReturn("fingerprint-value");

        Key key = keyManager.getKey();

        assertNotNull(key);
        assertEquals("private-key-value", key.getPrivateKey());
        assertEquals("public-key-value", key.getPublicKey());
        assertEquals("fingerprint-value", key.getFingerprint());
    }

    @Test
    public void testGetPrivateKeyFile() {
        File privateKeyFile = keyManager.getPrivateKeyFile(workspace);
        assertEquals(new File(mockKeyDir, "id_rsa"), privateKeyFile);
    }

    @Test
    public void testGetPublicKeyFile() {
        File publicKeyFile = keyManager.getPublicKeyFile(workspace);
        assertEquals(new File(mockKeyDir, "id_rsa.pub"), publicKeyFile);
    }

    @Test
    public void testGetKnownHostsFile() {
        File knownHostsFile = keyManager.getKnownHostsFile(workspace);
        assertEquals(new File(mockKeyDir, "known_hosts"), knownHostsFile);
    }

    @Test
    public void testCopyToWorkspace() throws IOException {
        when(configService.getByKey("publicKey")).thenReturn(new ConfigEntity());

        Key mockKey = new Key("private-key", "public-key", "fingerprint");
        when(configService.getValue("privateKey")).thenReturn(mockKey.getPrivateKey());
        when(configService.getValue("publicKey")).thenReturn(mockKey.getPublicKey());
        when(configService.getValue("fingerPrint")).thenReturn(mockKey.getFingerprint());

        keyManager.copyToWorkspace(workspace);

        File privateKeyFile = new File(mockKeyDir, "id_rsa");
        File publicKeyFile = new File(mockKeyDir, "id_rsa.pub");
        File knownHostsFile = new File(mockKeyDir, "known_hosts");

        assertTrue(privateKeyFile.exists());
        assertTrue(publicKeyFile.exists());
        assertTrue(knownHostsFile.exists());

        privateKeyFile.delete();
        publicKeyFile.delete();
        knownHostsFile.delete();
        mockKeyDir.delete();
    }
}
