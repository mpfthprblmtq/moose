package moose.services;

import moose.controllers.SongController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FilenameFormatterServiceTest {

    FilenameFormatterService filenameFormatterService;

    @BeforeAll
    public void setUp() {
        filenameFormatterService = new FilenameFormatterService(new SongController());
    }

    @Test
    public void testFormatFilename() {
        // TODO eventually
    }
}