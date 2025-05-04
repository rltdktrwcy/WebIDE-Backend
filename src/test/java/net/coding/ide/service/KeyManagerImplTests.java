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

    private File keyDir;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(keyManager, "username", "test-user");
        keyDir = new File(System.getProperty("java.io.tmpdir"), "test-key-dir");
        when(workspace.getKeyDir()).thenReturn(keyDir);
    }

    @Test
    public void testIsKeyExist() {
        when(configService.getByKey("publicKey")).thenReturn(null);
        assertFalse(keyManager.isKeyExist());

        ConfigEntity configEntity = new ConfigEntity();
        when(configService.getByKey("publicKey")).thenReturn(configEntity);
        assertTrue(keyManager.isKeyExist());
    }

    @Test
    public void testIsKeyExistInWorkspace() {
        File privateKeyFile = new File(keyDir, "id_rsa");
        File publicKeyFile = new File(keyDir, "id_rsa.pub");

        assertTrue(keyManager.isKeyExist(workspace));
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

        assertEquals("private-key-value", key.getPrivateKey());
        assertEquals("public-key-value", key.getPublicKey());
        assertEquals("fingerprint-value", key.getFingerprint());
    }

    @Test
    public void testCopyToWorkspace() throws IOException {
        keyDir.mkdirs();

        when(configService.getValue("privateKey")).thenReturn("private-key-value");
        when(configService.getValue("publicKey")).thenReturn("public-key-value");
        when(configService.getValue("fingerPrint")).thenReturn("fingerprint-value");

        keyManager.copyToWorkspace(workspace);

        File privateKeyFile = new File(keyDir, "id_rsa");
        File publicKeyFile = new File(keyDir, "id_rsa.pub");
        File knownHostsFile = new File(keyDir, "known_hosts");

        assertTrue(privateKeyFile.exists());
        assertTrue(publicKeyFile.exists());
        assertTrue(knownHostsFile.exists());

        privateKeyFile.delete();
        publicKeyFile.delete();
        knownHostsFile.delete();
        keyDir.delete();
    }

    @Test
    public void testGetPrivateKeyFile() {
        File privateKeyFile = keyManager.getPrivateKeyFile(workspace);
        assertEquals(new File(keyDir, "id_rsa"), privateKeyFile);
    }

    @Test
    public void testGetPublicKeyFile() {
        File publicKeyFile = keyManager.getPublicKeyFile(workspace);
        assertEquals(new File(keyDir, "id_rsa.pub"), publicKeyFile);
    }

    @Test
    public void testGetKnownHostsFile() {
        File knownHostsFile = keyManager.getKnownHostsFile(workspace);
        assertEquals(new File(keyDir, "known_hosts"), knownHostsFile);
    }
}
