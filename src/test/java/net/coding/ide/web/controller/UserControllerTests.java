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
import org.mockito.runners.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTests {

    @Mock
    private KeyManager keyManager;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserController userController;

    private Key testKey;
    private KeyDTO testKeyDTO;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(userController, "username", "testUser");
        ReflectionTestUtils.setField(userController, "avatar", "testAvatar");

        testKey = new Key("private", "public", "fingerprint");
        testKeyDTO = new KeyDTO();
        testKeyDTO.setPublicKey("public");
        testKeyDTO.setFingerprint("fingerprint");
    }

    @Test
    public void testPublicKeyWhenKeyExists() throws Exception {
        when(keyManager.isKeyExist()).thenReturn(true);
        when(keyManager.getKey()).thenReturn(testKey);
        when(modelMapper.map(testKey, KeyDTO.class)).thenReturn(testKeyDTO);

        KeyDTO result = userController.publicKey();

        verify(keyManager).isKeyExist();
        verify(keyManager).getKey();
        verify(keyManager, never()).generateKey();
        verify(modelMapper).map(testKey, KeyDTO.class);

        assertEquals(testKeyDTO, result);
    }

    @Test
    public void testPublicKeyWhenKeyDoesNotExist() throws Exception {
        when(keyManager.isKeyExist()).thenReturn(false);
        when(keyManager.generateKey()).thenReturn(testKey);
        when(modelMapper.map(testKey, KeyDTO.class)).thenReturn(testKeyDTO);

        KeyDTO result = userController.publicKey();

        verify(keyManager).isKeyExist();
        verify(keyManager, never()).getKey();
        verify(keyManager).generateKey();
        verify(modelMapper).map(testKey, KeyDTO.class);

        assertEquals(testKeyDTO, result);
    }

    @Test
    public void testCurrentUser() {
        UserDTO result = userController.currentUser();

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals("testAvatar", result.getAvatar());
    }
}
