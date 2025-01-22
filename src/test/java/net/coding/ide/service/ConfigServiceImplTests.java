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

    private ConfigEntity testConfig;

    @Before
    public void setUp() {
        testConfig = new ConfigEntity();
        testConfig.setKey("test.key");
        testConfig.setName("Test Name");
        testConfig.setValue("test value");
    }

    @Test
    public void testGetValue() {
        when(configRepository.getValueByKey("test.key")).thenReturn("test value");
        when(configRepository.getValueByKey("nested.key")).thenReturn("${test.key}");
        when(configRepository.getValueByKey("double.nested")).thenReturn("prefix ${nested.key} suffix");
        when(configRepository.getByKey("test.key")).thenReturn(testConfig);
        when(configRepository.getByKey("nested.key")).thenReturn(new ConfigEntity("nested.key", "${test.key}"));
        when(configRepository.getByKey("double.nested")).thenReturn(new ConfigEntity("double.nested", "prefix ${nested.key} suffix"));

        assertEquals("test value", configService.getValue("test.key"));
        assertEquals("test value", configService.getValue("nested.key"));
        assertEquals("prefix test value suffix", configService.getValue("double.nested"));
    }

    @Test
    public void testGetValueWithNonExistentPlaceholder() {
        when(configRepository.getValueByKey("test.key")).thenReturn("Value with ${non.existent}");
        when(configRepository.getByKey("test.key")).thenReturn(testConfig);
        when(configRepository.getByKey("non.existent")).thenReturn(null);

        assertEquals("Value with ${non.existent}", configService.getValue("test.key"));
    }

    @Test
    public void testGetValueWithDefaultValue() {
        when(configRepository.getByKey("non.existent")).thenReturn(null);
        when(configRepository.getByKey("test.key")).thenReturn(testConfig);
        when(configRepository.getValueByKey("test.key")).thenReturn("test value");

        assertEquals("default", configService.getValue("non.existent", "default"));
        assertEquals("test value", configService.getValue("test.key", "default"));
    }

    @Test
    public void testGetValueWithSubstitutions() {
        when(configRepository.getValueByKey("template")).thenReturn("Hello {0}, your score is {1}");
        when(configRepository.getByKey("template")).thenReturn(new ConfigEntity("template", "Hello {0}, your score is {1}"));

        assertEquals("Hello John, your score is 100", configService.getValue("template", "John", "100"));
    }

    @Test
    public void testGetValues() {
        when(configRepository.getValueByKey("list")).thenReturn("a,b,c");
        when(configRepository.getByKey("list")).thenReturn(new ConfigEntity("list", "a,b,c"));

        String[] values = configService.getValues("list", ",");
        assertArrayEquals(new String[]{"a", "b", "c"}, values);
    }

    @Test
    public void testGetName() {
        when(configRepository.getNameByKey("test.key")).thenReturn("Test Name");
        assertEquals("Test Name", configService.getName("test.key"));
    }

    @Test
    public void testGetByKey() {
        when(configRepository.getByKey("test.key")).thenReturn(testConfig);
        ConfigEntity result = configService.getByKey("test.key");
        assertEquals(testConfig, result);
    }

    @Test
    public void testExist() {
        when(configRepository.getByKey("test.key")).thenReturn(testConfig);
        when(configRepository.getByKey("non.existent")).thenReturn(null);

        assertTrue(configService.exist("test.key"));
        assertFalse(configService.exist("non.existent"));
    }

    @Test
    public void testGetValueWithNullInput() {
        when(configRepository.getValueByKey(null)).thenReturn(null);
        assertNull(configService.getValue(null));
    }

    @Test
    public void testGetValueWithEmptyPlaceholders() {
        when(configRepository.getValueByKey("empty.placeholder")).thenReturn("Value with ${}");
        when(configRepository.getByKey("empty.placeholder")).thenReturn(new ConfigEntity("empty.placeholder", "Value with ${}"));

        assertEquals("Value with ${}", configService.getValue("empty.placeholder"));
    }
}
