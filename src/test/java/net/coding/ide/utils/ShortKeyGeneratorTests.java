package net.coding.ide.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShortKeyGeneratorTests {

    @InjectMocks
    private ShortKeyGenerator generator;

    @Mock
    private Random random;

    @Before
    public void setup() {
        generator = new ShortKeyGenerator();
    }

    @Test
    public void testGenerateWithNumberArguments() {
        String key1 = generator.generate(123, 456);
        assertNotNull(key1);
        assertEquals(6, key1.length());
        assertTrue(key1.matches("[a-z]{6}"));

        String key2 = generator.generate(123, 456);
        assertNotEquals(key1, key2); // Due to random UUID and prime
    }

    @Test
    public void testGenerateWithStringArguments() {
        String key1 = generator.generate("test", "string");
        assertNotNull(key1);
        assertEquals(6, key1.length());
        assertTrue(key1.matches("[a-z]{6}"));
    }

    @Test
    public void testGenerateWithMixedArguments() {
        String key = generator.generate(123, "test", 456.789);
        assertNotNull(key);
        assertEquals(6, key.length());
        assertTrue(key.matches("[a-z]{6}"));
    }

    @Test
    public void testGenerateWithSingleArgument() {
        String key = generator.generate(12345);
        assertNotNull(key);
        assertEquals(6, key.length());
        assertTrue(key.matches("[a-z]{6}"));
    }

    @Test(expected = NullPointerException.class)
    public void testGenerateWithNullArgument() {
        generator.generate((Object)null);
    }

    @Test
    public void testGenerateWithEmptyArguments() {
        String key = generator.generate();
        assertNotNull(key);
        assertEquals(6, key.length());
        assertTrue(key.matches("[a-z]{6}"));
    }

    @Test
    public void testPrimeMethodReturnsValidPrime() {
        // Use reflection to access private method
        java.lang.reflect.Method primeMethod;
        try {
            primeMethod = ShortKeyGenerator.class.getDeclaredMethod("prime");
            primeMethod.setAccessible(true);

            int prime = (Integer) primeMethod.invoke(generator);

            // Verify prime is in the valid range
            assertTrue(prime >= 2);
            assertTrue(prime <= 997);

            // Verify it's actually a prime number
            assertTrue(isPrime(prime));

        } catch (Exception e) {
            fail("Failed to test prime method: " + e.getMessage());
        }
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;

        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
}
