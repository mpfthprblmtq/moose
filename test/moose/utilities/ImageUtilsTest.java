package moose.utilities;

import org.junit.Assert;
import org.junit.Test;

import javax.swing.ImageIcon;

public class ImageUtilsTest {

    @Test
    public void testGetBytesFromImageIconWithNullIcon() {
        ImageIcon input = new ImageIcon();

        byte[] expected = new byte[]{};
        byte[] actual = ImageUtils.getBytesFromImageIcon(input);

        Assert.assertArrayEquals(expected, actual);
    }
}