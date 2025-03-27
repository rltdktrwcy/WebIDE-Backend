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

    @InjectMocks
    private KeyManagerImpl keyManager;

    @Mock
    private ConfigService configService;

    @Mock
    private Workspace workspace;

    private File baseDir;
    private File keyDir;
    private File privateKeyFile;
    private File publicKeyFile;
    private File knownHostsFile;

    @Before
    public void setUp() {
        baseDir = new File("test-workspace");
        keyDir = new File(baseDir, "ssh-key");
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
        when(configService.getValue("privateKey")).thenReturn("private");
        when(configService.getValue("publicKey")).thenReturn("public");
        when(configService.getValue("fingerPrint")).thenReturn("fingerprint");

        Key key = keyManager.getKey();
        assertEquals("private", key.getPrivateKey());
        assertEquals("public", key.getPublicKey());
        assertEquals("fingerprint", key.getFingerprint());
    }

    @Test
    public void testGetKeyFiles() {
        File privateKeyFile = keyManager.getPrivateKeyFile(workspace);
        File publicKeyFile = keyManager.getPublicKeyFile(workspace);
        File knownHostsFile = keyManager.getKnownHostsFile(workspace);

        assertEquals(new File(keyDir, "id_rsa"), privateKeyFile);
        assertEquals(new File(keyDir, "id_rsa.pub"), publicKeyFile);
        assertEquals(new File(keyDir, "known_hosts"), knownHostsFile);
    }

    @Test
    public void testCopyToWorkspace() throws IOException {
        when(configService.getByKey("publicKey")).thenReturn(null);

        Key key = new Key("private", "public", "fingerprint");
        when(configService.getValue("privateKey")).thenReturn(key.getPrivateKey());
        when(configService.getValue("publicKey")).thenReturn(key.getPublicKey());
        when(configService.getValue("fingerPrint")).thenReturn(key.getFingerprint());

        keyManager.copyToWorkspace(workspace);

        verify(configService).getByKey("publicKey");
    }
}
