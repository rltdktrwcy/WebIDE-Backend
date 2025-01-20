package net.coding.ide.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class ShortKeyGeneratorTests {

    private ShortKeyGenerator generator;

    @Before
    public void setUp() {
        generator = new ShortKeyGenerator();
    }

    @Test
    public void testGenerateWithSingleNumber() {
        String key = generator.generate(123);
        assertNotNull(key);
        assertEquals(6, key.length());
        assertTrue(key.matches("[a-z]{6}"));
    }

    @Test
    public void testGenerateWithMultipleNumbers() {
        String key = generator.generate(123, 456, 789);
        assertNotNull(key);
        assertEquals(6, key.length());
        assertTrue(key.matches("[a-z]{6}"));
    }

    @Test
    public void testGenerateWithString() {
        String key = generator.generate("test");
        assertNotNull(key);
        assertEquals(6, key.length());
        assertTrue(key.matches("[a-z]{6}"));
    }

    @Test
    public void testGenerateWithMixedTypes() {
        String key = generator.generate("test", 123, true);
        assertNotNull(key);
        assertEquals(6, key.length());
        assertTrue(key.matches("[a-z]{6}"));
    }

    @Test
    public void testGenerateWithEmptyArgs() {
        String key = generator.generate();
        assertNotNull(key);
        assertEquals(6, key.length());
        assertTrue(key.matches("[a-z]{6}"));
    }

    @Test
    public void testGenerateUniqueness() {
        String key1 = generator.generate("test");
        String key2 = generator.generate("test");
        // Due to random salt, keys should be different even with same input
        assertNotEquals(key1, key2);
    }

    @Test(expected = NullPointerException.class)
    public void testGenerateWithNull() {
        generator.generate((Object)null);
    }

    @Test
    public void testGenerateWithLongValues() {
        String key = generator.generate(Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(key);
        assertEquals(6, key.length());
        assertTrue(key.matches("[a-z]{6}"));
    }

    @Test
    public void testGenerateWithSpecialCharacters() {
        String key = generator.generate("!@#$%^&*()");
        assertNotNull(key);
        assertEquals(6, key.length());
        assertTrue(key.matches("[a-z]{6}"));
    }

    @Test
    public void testGenerateWithEmptyString() {
        String key = generator.generate("");
        assertNotNull(key);
        assertEquals(6, key.length());
        assertTrue(key.matches("[a-z]{6}"));
    }
}
