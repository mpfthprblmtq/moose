package moose.services;

import moose.controllers.SongController;
import org.junit.Before;
import org.junit.Test;

public class FilenameFormatterServiceTest {

    FilenameFormatterService filenameFormatterService;

    @Before
    public void setUp() {
        filenameFormatterService = new FilenameFormatterService(new SongController());
    }

    @Test
    public void testFormatFilename() {
        // TODO eventually
    }
}