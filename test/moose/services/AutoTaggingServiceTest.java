package moose.services;

import moose.controllers.SongController;
import moose.utilities.SongUtils;
import moose.utilities.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static moose.utilities.Constants.SINGLES;

public class AutoTaggingServiceTest {

    private AutoTaggingService autoTaggingService;
    private List<File> files;

    @Before
    @SuppressWarnings("all")
    public void setUp() {
        autoTaggingService = new AutoTaggingService(new SongController());
        files = new ArrayList<>();
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/[2021] TestAlbum1/TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/[2021] TestAlbum2/01 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/[2021] TestAlbum2/02 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/[2021] TestAlbum2/03 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/Singles/TestGenre/[2021] TestArtist - TestAlbum/01 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/Compilations/[2021] TestAlbum/01 TestArtist - TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/Compilations/[2021] TestAlbum/02 TestArtist - TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/Compilations/[2021] TestAlbum/03 TestArtist - TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/LPs/[2021] TestArtist - TestAlbum1/CD1/01 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/LPs/[2021] TestArtist - TestAlbum1/CD1/02 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/LPs/[2021] TestArtist - TestAlbum1/CD1/03 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/LPs/[2021] TestArtist - TestAlbum1/CD2/01 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/LPs/[2021] TestArtist - TestAlbum1/CD2/02 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/LPs/[2021] TestArtist - TestAlbum1/CD2/03 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/EPs/[2021] TestArtist - TestAlbum2/01 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/EPs/[2021] TestArtist - TestAlbum2/02 TestTitle.mp3"));
        files.add(new File("test/temp/TestLibrary/TestAlbumArtist/EPs/[2021] TestArtist - TestAlbum2/03 TestTitle.mp3"));
        for (File file : files) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("IOException while trying to create files for testing!");
            }
        }
    }

    @After
    @SuppressWarnings("all")
    public void tearDown() {
        File tempFile = new File("./test/temp");
        Path tempPath = tempFile.toPath();
        try {
            Files.walk(tempPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            System.err.println("IOException while trying to delete temp test directory!");
        }
        Assert.assertFalse("Directory still exists",
                Files.exists(tempPath));
    }

    @Test
    public void testGetTitleFromFile() {
        String expected = "TestTitle";
        for (File file : files) {
            String actual = autoTaggingService.getTitleFromFile(file);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetArtistFromFile() {
        String expected;
        for (int i = 0; i < files.size() - 1; i++) {
            if (i < 4) {
                expected = "TestAlbumArtist";
            } else {
                expected = "TestArtist";
            }
            String actual = autoTaggingService.getArtistFromFile(files.get(i), null);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetAlbumFromFile() {
        String expected;
        for (File file : files) {
            if (SongUtils.isPartOfALabel(file, SINGLES)) {
                expected = "TestGenre";
            } else {
                if (file.getPath().contains("TestAlbum1")) {
                    expected = "TestAlbum1";
                } else if (file.getPath().contains("TestAlbum2")) {
                    expected = "TestAlbum2";
                } else {
                    expected = "TestAlbum";
                }
            }
            String actual = autoTaggingService.getAlbumFromFile(file);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetAlbumArtistFromFile() {
        String expected = "TestAlbumArtist";
        for (File file : files) {
            String actual = autoTaggingService.getAlbumArtistFromFile(file);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetYearFromFile() {
        String expected = "2021";
        for (File file : files) {
            String actual = autoTaggingService.getYearFromFile(file);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetGenreFromFile() {
        String expected;
        for (File file : files) {
            if (SongUtils.isPartOfALabel(file, SINGLES)) {
                expected = "TestGenre";
            } else {
                expected = StringUtils.EMPTY;
            }
            String actual = autoTaggingService.getGenreFromFile(file);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetTrackFromFile() {
        String expected;
        for (File file : files) {
            if (file.getName().startsWith("01")) {
                if (SongUtils.isPartOfALabel(file, SINGLES)) {
                    expected = "1/1";
                } else {
                    expected = "1/3";
                }
            } else if (file.getName().startsWith("02")) {
                expected = "2/3";
            } else if (file.getName().startsWith("03")) {
                expected = "3/3";
            } else {
                expected = StringUtils.EMPTY;
            }

            String actual = autoTaggingService.getTracksFromFile(file);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetDiskFromFile() {
        String expected;
        for (File file : files) {
            if (file.getPath().contains("CD1")) {
                expected = "1/2";
            } else if (file.getPath().contains("CD2")) {
                expected = "2/2";
            } else {
                expected = "1/1";
            }
            String actual = autoTaggingService.getDisksFromFile(file);
            Assert.assertEquals(expected, actual);
        }
    }

}