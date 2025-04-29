import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class UtilTests {

    private Util util;

    @Before
    public void setUp() {
        util = new Util();
    }

    @Test
    public void testPlus() {
        assertEquals(5, util.plus(2, 3));
        assertEquals(0, util.plus(0, 0));
        assertEquals(-5, util.plus(-2, -3));
        assertEquals(1, util.plus(-2, 3));
    }

}
