package net.coding.ide.service;

import net.coding.ide.entity.ConfigEntity;
import net.coding.ide.repository.ConfigRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConfigServiceImplTests {

    @Mock
    private ConfigRepository configRepository;

    @InjectMocks
    private ConfigServiceImpl configService;

    @Before
    public void setUp() {
        ConfigEntity entity = new ConfigEntity();
        entity.setKey("test.key");
        entity.setValue("test.value");
        entity.setName("Test Name");

        when(configRepository.getByKey("test.key")).thenReturn(entity);
        when(configRepository.getValueByKey("test.key")).thenReturn("test.value");
        when(configRepository.getNameByKey("test.key")).thenReturn("Test Name");
    }

    @Test
    public void testGetValue() {
        when(configRepository.getValueByKey("simple")).thenReturn("value");
        assertEquals("value", configService.getValue("simple"));

        when(configRepository.getValueByKey("nested")).thenReturn("prefix ${test.key} suffix");
        assertEquals("prefix test.value suffix", configService.getValue("nested"));

        when(configRepository.getValueByKey("missing")).thenReturn("prefix ${missing.key} suffix");
        assertEquals("prefix ${missing.key} suffix", configService.getValue("missing"));

        assertNull(configService.getValue("nonexistent"));
    }

    @Test
    public void testGetValueWithDefault() {
        assertEquals("test.value", configService.getValue("test.key", "default"));
        assertEquals("default", configService.getValue("missing.key", "default"));
    }

    @Test
    public void testGetValueWithSubstitutions() {
        when(configRepository.getValueByKey("template")).thenReturn("Hello {0} {1}!");
        assertEquals("Hello John Doe!", configService.getValue("template", "John", "Doe"));

        assertNull(configService.getValue("missing.key", "sub1", "sub2"));
    }

    @Test
    public void testGetValues() {
        when(configRepository.getValueByKey("list")).thenReturn("a,b,c");
        String[] values = configService.getValues("list", ",");
        assertArrayEquals(new String[]{"a", "b", "c"}, values);
    }

    @Test
    public void testGetName() {
        assertEquals("Test Name", configService.getName("test.key"));
    }

    @Test
    public void testGetByKey() {
        ConfigEntity entity = configService.getByKey("test.key");
        assertNotNull(entity);
        assertEquals("test.key", entity.getKey());
        assertEquals("test.value", entity.getValue());
    }

    @Test
    public void testExist() {
        assertTrue(configService.exist("test.key"));
        assertFalse(configService.exist("missing.key"));
    }
}
