package net.coding.ide.web.controller;

import net.coding.ide.dto.KeyDTO;
import net.coding.ide.dto.UserDTO;
import net.coding.ide.model.Key;
import net.coding.ide.service.KeyManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserControllerTests {

    @Mock
    private KeyManager keyManager;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserController userController;

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_AVATAR = "testAvatar";
    private static final String TEST_PUBLIC_KEY = "ssh-rsa AAAAB3NzaC1";
    private static final String TEST_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpA";
    private static final String TEST_FINGERPRINT = "aa:bb:cc:dd";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(userController, "username", TEST_USERNAME);
        ReflectionTestUtils.setField(userController, "avatar", TEST_AVATAR);
    }

    @Test
    public void testPublicKey_WhenKeyExists() throws Exception {
        Key key = new Key(TEST_PRIVATE_KEY, TEST_PUBLIC_KEY, TEST_FINGERPRINT);
        KeyDTO keyDTO = new KeyDTO();
        keyDTO.setPublicKey(TEST_PUBLIC_KEY);
        keyDTO.setFingerprint(TEST_FINGERPRINT);

        when(keyManager.isKeyExist()).thenReturn(true);
        when(keyManager.getKey()).thenReturn(key);
        when(modelMapper.map(key, KeyDTO.class)).thenReturn(keyDTO);

        KeyDTO result = userController.publicKey();

        verify(keyManager).isKeyExist();
        verify(keyManager).getKey();
        verify(modelMapper).map(key, KeyDTO.class);

        assertEquals(TEST_PUBLIC_KEY, result.getPublicKey());
        assertEquals(TEST_FINGERPRINT, result.getFingerprint());
    }

    @Test
    public void testPublicKey_WhenKeyDoesNotExist() throws Exception {
        Key key = new Key(TEST_PRIVATE_KEY, TEST_PUBLIC_KEY, TEST_FINGERPRINT);
        KeyDTO keyDTO = new KeyDTO();
        keyDTO.setPublicKey(TEST_PUBLIC_KEY);
        keyDTO.setFingerprint(TEST_FINGERPRINT);

        when(keyManager.isKeyExist()).thenReturn(false);
        when(keyManager.generateKey()).thenReturn(key);
        when(modelMapper.map(key, KeyDTO.class)).thenReturn(keyDTO);

        KeyDTO result = userController.publicKey();

        verify(keyManager).isKeyExist();
        verify(keyManager).generateKey();
        verify(modelMapper).map(key, KeyDTO.class);

        assertEquals(TEST_PUBLIC_KEY, result.getPublicKey());
        assertEquals(TEST_FINGERPRINT, result.getFingerprint());
    }

    @Test
    public void testCurrentUser() {
        UserDTO result = userController.currentUser();

        assertEquals(TEST_USERNAME, result.getUsername());
        assertEquals(TEST_AVATAR, result.getAvatar());
    }
}
