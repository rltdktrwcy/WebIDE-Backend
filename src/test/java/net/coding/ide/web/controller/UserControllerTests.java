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
    private static final String TEST_AVATAR = "test-avatar.png";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(userController, "username", TEST_USERNAME);
        ReflectionTestUtils.setField(userController, "avatar", TEST_AVATAR);
    }

    @Test
    public void testPublicKey_WhenKeyExists() throws Exception {
        Key mockKey = new Key("private", "public", "fingerprint");
        KeyDTO mockKeyDTO = new KeyDTO();
        mockKeyDTO.setPublicKey("public");
        mockKeyDTO.setFingerprint("fingerprint");

        when(keyManager.isKeyExist()).thenReturn(true);
        when(keyManager.getKey()).thenReturn(mockKey);
        when(modelMapper.map(mockKey, KeyDTO.class)).thenReturn(mockKeyDTO);

        KeyDTO result = userController.publicKey();

        verify(keyManager).isKeyExist();
        verify(keyManager).getKey();
        verify(keyManager, never()).generateKey();
        verify(modelMapper).map(mockKey, KeyDTO.class);

        assertEquals("public", result.getPublicKey());
        assertEquals("fingerprint", result.getFingerprint());
    }

    @Test
    public void testPublicKey_WhenKeyDoesNotExist() throws Exception {
        Key mockKey = new Key("private", "public", "fingerprint");
        KeyDTO mockKeyDTO = new KeyDTO();
        mockKeyDTO.setPublicKey("public");
        mockKeyDTO.setFingerprint("fingerprint");

        when(keyManager.isKeyExist()).thenReturn(false);
        when(keyManager.generateKey()).thenReturn(mockKey);
        when(modelMapper.map(mockKey, KeyDTO.class)).thenReturn(mockKeyDTO);

        KeyDTO result = userController.publicKey();

        verify(keyManager).isKeyExist();
        verify(keyManager, never()).getKey();
        verify(keyManager).generateKey();
        verify(modelMapper).map(mockKey, KeyDTO.class);

        assertEquals("public", result.getPublicKey());
        assertEquals("fingerprint", result.getFingerprint());
    }

    @Test
    public void testCurrentUser() {
        UserDTO result = userController.currentUser();

        assertEquals(TEST_USERNAME, result.getUsername());
        assertEquals(TEST_AVATAR, result.getAvatar());
    }
}
