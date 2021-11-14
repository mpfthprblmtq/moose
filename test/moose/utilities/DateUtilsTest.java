package moose.utilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class DateUtilsTest {

    Date today;
    Date past;
    Date future;

    @Before
    public void setUp() {
        today = new Date();
        past = new Date();
        future = new Date();

        long pastLong = Long.parseLong("1293861600000");
        long futureLong = Long.parseLong("1925013600000");

        past.setTime(pastLong);
        future.setTime(futureLong);
    }

    @Test
    public void testIsDateSameAsToday() {
        Assert.assertTrue(DateUtils.isDateSameAsToday(today));
        Assert.assertFalse(DateUtils.isDateSameAsToday(past));
        Assert.assertFalse(DateUtils.isDateSameAsToday(future));
    }
}