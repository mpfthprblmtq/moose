package moose.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilenameFormatterServiceTest {

    FilenameFormatterService filenameFormatterService;

    @Before
    public void setUp() {
        filenameFormatterService = new FilenameFormatterService();
    }

    @Test
    public void testNewFormatFilename() {

        // TODO
//        mockAutoTaggingService = mock(AutoTaggingService.class);
//        when(mockAutoTaggingService.getArtist(any())).thenReturn("Kasbo");

        String[] names = new String[] {
            "/Users/pat/Music/Library/Kasbo/[2020] The Making of a Paracom/02. Kasbo - Play Pretend (feat. Ourchives).mp3",
                    "/Users/pat/Music/Library/Kasbo/[2020] The Making of a Paracom/02 - Kasbo - Play Pretend (feat. Ourchives).mp3",
                    "/Users/pat/Music/Library/Kasbo/[2020] The Making of a Paracom/02 Kasbo - Play Pretend (feat. Ourchives).mp3",
                    "/Users/pat/Music/Library/Kasbo/[2020] The Making of a Paracom/02 - Play Pretend (feat. Ourchives).mp3",
                    "/Users/pat/Music/Library/Kasbo/[2020] The Making of a Paracom/02. Play Pretend (feat. Ourchives).mp3",
                    "/Users/pat/Music/Library/Kasbo/[2020] The Making of a Paracom/2 Play Pretend (feat. Ourchives).mp3",
        };

        String expected = "02 Play Pretend (ft. Ourchives).mp3";
        List<String> actualResults = new ArrayList<>();

        for (String filename : names) {
            actualResults.add(filenameFormatterService.getBetterFilename(new File(filename), false));
        }

        for (String newFilename : actualResults) {
            Assert.assertEquals(expected, newFilename);
        }
    }
}