package net.coding.ide.utils;

import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class HostMatcherTests {

    @Test
    public void testExactHostMatch() {
        HostMatcher matcher = new HostMatcher("example.com");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Host")).thenReturn("example.com");
        assertTrue(matcher.matches(request));
    }

    @Test
    public void testWildcardHostMatch() {
        HostMatcher matcher = new HostMatcher("*.example.com");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Host")).thenReturn("test.example.com");
        assertTrue(matcher.matches(request));
    }

    @Test
    public void testNoMatch() {
        HostMatcher matcher = new HostMatcher("example.com");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Host")).thenReturn("different.com");
        assertFalse(matcher.matches(request));
    }

    @Test
    public void testNullHost() {
        HostMatcher matcher = new HostMatcher("example.com");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Host")).thenReturn(null);
        assertFalse(matcher.matches(request));
    }

    @Test
    public void testMultipleWildcards() {
        HostMatcher matcher = new HostMatcher("*.example.*");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Host")).thenReturn("test.example.org");
        assertTrue(matcher.matches(request));
    }

    @Test
    public void testDotEscaping() {
        HostMatcher matcher = new HostMatcher("example.com");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Host")).thenReturn("examplexcom");
        assertFalse(matcher.matches(request));
    }
}
