package moose.utilities;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtilsTest {

    @Test
    public void testGetStartingPoint() {
        File file1 = new File("/users/pat/Music/Library/TestArtist1/TestAlbum1/file1.mp3");
        File file2 = new File("/users/pat/Music/Library/TestArtist1/TestAlbum2/file1.mp3");
        File file3 = new File("/users/pat/Music/Library/TestArtist1/TestAlbum3/file1.mp3");

        List<File> files = new ArrayList<>(Arrays.asList(file1, file2, file3));
        File expected = new File("/users/pat/Music/Library/TestArtist1");

        File actual = FileUtils.getStartingPoint(files);
        Assert.assertEquals(expected.getPath(), actual.getPath());
    }
}