package net.coding.ide.service;

import net.coding.ide.model.Key;
import net.coding.ide.model.Workspace;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    private File privateKeyFile;
    private File publicKeyFile;
    private File knownHostsFile;

    @Before
    public void setup() {
        keyDir = new File("test-key-dir");
        privateKeyFile = new File(keyDir, "id_rsa");
        publicKeyFile = new File(keyDir, "id_rsa.pub");
        knownHostsFile = new File(keyDir, "known_hosts");

        when(workspace.getKeyDir()).thenReturn(keyDir);
    }

    @Test
    public void testIsKeyExist() {
        when(configService.getByKey("publicKey")).thenReturn(null);
        assertFalse(keyManager.isKeyExist());

        when(configService.getByKey("publicKey")).thenReturn(mock(net.coding.ide.entity.ConfigEntity.class));
        assertTrue(keyManager.isKeyExist());
    }

    @Test
    public void testIsKeyExistInWorkspace() {
        when(workspace.getKeyDir()).thenReturn(keyDir);
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
        when(configService.getValue("privateKey")).thenReturn("private-key-data");
        when(configService.getValue("publicKey")).thenReturn("public-key-data");
        when(configService.getValue("fingerPrint")).thenReturn("fingerprint-data");

        Key key = keyManager.getKey();

        assertEquals("private-key-data", key.getPrivateKey());
        assertEquals("public-key-data", key.getPublicKey());
        assertEquals("fingerprint-data", key.getFingerprint());
    }

    @Test
    public void testCopyToWorkspace() throws IOException {
        when(configService.getByKey("publicKey")).thenReturn(null);

        Key key = new Key("private-key", "public-key", "fingerprint");
        when(configService.getValue("privateKey")).thenReturn(key.getPrivateKey());
        when(configService.getValue("publicKey")).thenReturn(key.getPublicKey());
        when(configService.getValue("fingerPrint")).thenReturn(key.getFingerprint());

        keyManager.copyToWorkspace(workspace);

        verify(configService).getByKey("publicKey");
        verify(configService, times(3)).getValue(anyString());
    }

    @Test
    public void testGetPrivateKeyFile() {
        File result = keyManager.getPrivateKeyFile(workspace);
        assertEquals(privateKeyFile, result);
    }

    @Test
    public void testGetPublicKeyFile() {
        File result = keyManager.getPublicKeyFile(workspace);
        assertEquals(publicKeyFile, result);
    }

    @Test
    public void testGetKnownHostsFile() {
        File result = keyManager.getKnownHostsFile(workspace);
        assertEquals(knownHostsFile, result);
    }
}
