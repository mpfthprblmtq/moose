package main.java.com.mpfthprblmtq.moose.utilities;

import com.mpfthprblmtq.moose.utilities.ImageUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import javax.swing.ImageIcon;

public class ImageUtilsTest {

    @Test
    public void testGetBytesFromImageIconWithNullIcon() {
        ImageIcon input = new ImageIcon();

        byte[] expected = new byte[]{};
        byte[] actual = ImageUtils.getBytesFromImageIcon(input);

        assertArrayEquals(expected, actual);
    }
}