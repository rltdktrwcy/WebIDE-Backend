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

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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
    public void setup() {
        ReflectionTestUtils.setField(userController, "username", "testuser");
        ReflectionTestUtils.setField(userController, "avatar", "test-avatar.png");

        testKey = new Key("private-key", "public-key", "fingerprint");
        testKeyDTO = new KeyDTO();
        testKeyDTO.setPublicKey("public-key");
        testKeyDTO.setFingerprint("fingerprint");
    }

    @Test
    public void testPublicKeyWhenKeyExists() throws IOException {
        when(keyManager.isKeyExist()).thenReturn(true);
        when(keyManager.getKey()).thenReturn(testKey);
        when(modelMapper.map(testKey, KeyDTO.class)).thenReturn(testKeyDTO);

        KeyDTO result = userController.publicKey();

        assertEquals(testKeyDTO.getPublicKey(), result.getPublicKey());
        assertEquals(testKeyDTO.getFingerprint(), result.getFingerprint());
    }

    @Test
    public void testPublicKeyWhenKeyDoesNotExist() throws IOException {
        when(keyManager.isKeyExist()).thenReturn(false);
        when(keyManager.generateKey()).thenReturn(testKey);
        when(modelMapper.map(testKey, KeyDTO.class)).thenReturn(testKeyDTO);

        KeyDTO result = userController.publicKey();

        assertEquals(testKeyDTO.getPublicKey(), result.getPublicKey());
        assertEquals(testKeyDTO.getFingerprint(), result.getFingerprint());
    }

    @Test
    public void testCurrentUser() {
        UserDTO result = userController.currentUser();

        assertEquals("testuser", result.getUsername());
        assertEquals("test-avatar.png", result.getAvatar());
    }
}
