import org.junit.Test;
import static org.junit.Assert.*;

public class UtilTests {

    @Test
    public void testPlusPositiveNumbers() {
        Util util = new Util();
        assertEquals(5, util.plus(2, 3));
    }

    @Test
    public void testPlusNegativeNumbers() {
        Util util = new Util();
        assertEquals(-5, util.plus(-2, -3));
    }

    @Test
    public void testPlusWithZero() {
        Util util = new Util();
        assertEquals(3, util.plus(3, 0));
        assertEquals(3, util.plus(0, 3));
        assertEquals(0, util.plus(0, 0));
    }

    @Test
    public void testPlusPositiveAndNegative() {
        Util util = new Util();
        assertEquals(0, util.plus(3, -3));
        assertEquals(-1, util.plus(2, -3));
    }

}
