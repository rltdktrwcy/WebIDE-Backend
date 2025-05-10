package net.coding.ide.utils;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class CookieStoreHolderTests {

    @After
    public void tearDown() {
        CookieStoreHolder.clearCookieStore();
    }

    @Test
    public void testClearCookieStore() {
        CookieStore cookieStore = new BasicCookieStore();
        CookieStoreHolder.setCookieStore(cookieStore);

        CookieStoreHolder.clearCookieStore();

        assertNull(CookieStoreHolder.getCookieStore(false));
    }

    @Test
    public void testGetCookieStoreWhenEmpty() {
        CookieStore cookieStore = CookieStoreHolder.getCookieStore(false);
        assertNull(cookieStore);
    }

    @Test
    public void testGetCookieStoreWithCreate() {
        CookieStore cookieStore = CookieStoreHolder.getCookieStore(true);
        assertNotNull(cookieStore);
        assertTrue(cookieStore instanceof BasicCookieStore);
    }

    @Test
    public void testGetCookieStoreWhenAlreadySet() {
        CookieStore originalStore = new BasicCookieStore();
        CookieStoreHolder.setCookieStore(originalStore);

        CookieStore retrievedStore = CookieStoreHolder.getCookieStore(true);

        assertSame(originalStore, retrievedStore);
    }

    @Test
    public void testSetCookieStore() {
        CookieStore cookieStore = new BasicCookieStore();
        CookieStoreHolder.setCookieStore(cookieStore);

        assertSame(cookieStore, CookieStoreHolder.getCookieStore(false));
    }

    @Test
    public void testThreadLocalIsolation() throws InterruptedException {
        CookieStore mainThreadStore = new BasicCookieStore();
        CookieStoreHolder.setCookieStore(mainThreadStore);

        Thread thread = new Thread(() -> {
            assertNull(CookieStoreHolder.getCookieStore(false));

            CookieStore threadStore = new BasicCookieStore();
            CookieStoreHolder.setCookieStore(threadStore);

            assertSame(threadStore, CookieStoreHolder.getCookieStore(false));
            assertNotSame(mainThreadStore, CookieStoreHolder.getCookieStore(false));
        });

        thread.start();
        thread.join();

        assertSame(mainThreadStore, CookieStoreHolder.getCookieStore(false));
    }
}
