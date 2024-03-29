package main.java.com.mpfthprblmtq.moose.services;

import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.services.AutoTaggingService;
import com.mpfthprblmtq.moose.utilities.MP3FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.mpfthprblmtq.moose.utilities.Constants.SINGLES;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoTaggingServiceTest {

    private AutoTaggingService autoTaggingService;
    private List<File> files;

    @BeforeAll
    @SuppressWarnings("all")
    public void setUp() {
        autoTaggingService = new AutoTaggingService();
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

    @AfterAll
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
        assertFalse(Files.exists(tempPath));
    }

    @Test
    public void testGetTitleFromFile() {
        String expected = "TestTitle";
        for (File file : files) {
            Song song = new Song();
            song.setFile(file);
            String actual = autoTaggingService.getTitleFromFile(song);
            assertEquals(expected, actual);
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
            String actual = autoTaggingService.getArtistFromFile(files.get(i));
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetAlbumFromFile() {
        String expected;
        for (File file : files) {
            if (MP3FileUtils.isPartOfALabel(file, SINGLES)) {
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
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetAlbumArtistFromFile() {
        String expected = "TestAlbumArtist";
        for (File file : files) {
            String actual = autoTaggingService.getAlbumArtistFromFile(file);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testGetYearFromFile() {
        String expected = "2021";
        for (File file : files) {
            String actual = autoTaggingService.getYearFromFile(file);
            assertEquals(expected, actual);
        }
    }

//    @Test
//    public void testGetGenreFromFile() {
//        String expected;
//        for (File file : files) {
//            if (MP3FileUtils.isPartOfALabel(file, SINGLES)) {
//                expected = "TestGenre";
//            } else {
//                expected = StringUtils.EMPTY;
//            }
//            String actual = autoTaggingService.getGenreFromFile(file);
//            assertEquals(expected, actual);
//        }
//    }

    @Test
    public void testGetTrackFromFile() {
        String expected;
        for (File file : files) {
            if (file.getName().startsWith("01")) {
                if (MP3FileUtils.isPartOfALabel(file, SINGLES)) {
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
            Song song = new Song();
            song.setFile(file);

            String actual = autoTaggingService.getTracksFromFile(song);
            assertEquals(expected, actual);
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
            assertEquals(expected, actual);
        }
    }

}