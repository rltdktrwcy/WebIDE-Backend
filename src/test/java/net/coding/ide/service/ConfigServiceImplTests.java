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

    private ConfigEntity configEntity;

    @Before
    public void setup() {
        configEntity = new ConfigEntity();
        configEntity.setKey("test.key");
        configEntity.setValue("test.value");
        configEntity.setName("Test Name");
    }

    @Test
    public void testGetValue() {
        when(configRepository.getValueByKey("test.key")).thenReturn("test.value");
        assertEquals("test.value", configService.getValue("test.key"));
    }

    @Test
    public void testGetValueWithPlaceholder() {
        when(configRepository.getValueByKey("base.key")).thenReturn("base.value");
        when(configRepository.getValueByKey("test.key")).thenReturn("prefix ${base.key} suffix");
        when(configRepository.getByKey("base.key")).thenReturn(new ConfigEntity());

        assertEquals("prefix base.value suffix", configService.getValue("test.key"));
    }

    @Test
    public void testGetValueWithNonExistentPlaceholder() {
        when(configRepository.getValueByKey("test.key")).thenReturn("prefix ${nonexistent.key} suffix");
        when(configRepository.getByKey("nonexistent.key")).thenReturn(null);

        assertEquals("prefix ${nonexistent.key} suffix", configService.getValue("test.key"));
    }

    @Test
    public void testGetValueWithDefaultValue() {
        when(configRepository.getByKey("test.key")).thenReturn(null);
        assertEquals("default.value", configService.getValue("test.key", "default.value"));
    }

    @Test
    public void testGetValueWithSubstitutions() {
        when(configRepository.getValueByKey("test.key")).thenReturn("Hello {0} and {1}");
        assertEquals("Hello World and User", configService.getValue("test.key", "World", "User"));
    }

    @Test
    public void testGetValuesWithSeparator() {
        when(configRepository.getValueByKey("test.key")).thenReturn("value1,value2,value3");
        String[] values = configService.getValues("test.key", ",");
        assertArrayEquals(new String[]{"value1", "value2", "value3"}, values);
    }

    @Test
    public void testGetName() {
        when(configRepository.getNameByKey("test.key")).thenReturn("Test Name");
        assertEquals("Test Name", configService.getName("test.key"));
    }

    @Test
    public void testGetByKey() {
        when(configRepository.getByKey("test.key")).thenReturn(configEntity);
        assertEquals(configEntity, configService.getByKey("test.key"));
    }

    @Test
    public void testExist() {
        when(configRepository.getByKey("test.key")).thenReturn(configEntity);
        assertTrue(configService.exist("test.key"));

        when(configRepository.getByKey("nonexistent.key")).thenReturn(null);
        assertFalse(configService.exist("nonexistent.key"));
    }

    @Test
    public void testGetValueWithNullKey() {
        when(configRepository.getValueByKey(null)).thenReturn(null);
        assertNull(configService.getValue(null));
    }

    @Test
    public void testGetValueWithNestedPlaceholders() {
        when(configRepository.getValueByKey("first.key")).thenReturn("${second.key}");
        when(configRepository.getValueByKey("second.key")).thenReturn("${third.key}");
        when(configRepository.getValueByKey("third.key")).thenReturn("final.value");

        when(configRepository.getByKey("first.key")).thenReturn(new ConfigEntity());
        when(configRepository.getByKey("second.key")).thenReturn(new ConfigEntity());
        when(configRepository.getByKey("third.key")).thenReturn(new ConfigEntity());

        assertEquals("final.value", configService.getValue("first.key"));
    }
}
