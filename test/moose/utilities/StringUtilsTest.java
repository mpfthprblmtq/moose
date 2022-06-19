package moose.utilities;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtilsTest {

    @Test
    public void testSame() {
        List<String> strings = new ArrayList<>(Arrays.asList("graceful", "grace", "gracefully"));
        String expected = "grace";
        String actual = StringUtils.same(strings);
        Assert.assertEquals(expected, actual);
    }
}