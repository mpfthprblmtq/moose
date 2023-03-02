package com.mpfthprblmtq.moose.services;

import com.mpfthprblmtq.moose.controllers.SongController;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.utilities.MP3FileUtils;
import com.mpfthprblmtq.moose.utilities.viewUtils.DialogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static com.mpfthprblmtq.moose.utilities.Constants.COMPILATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilenameFormatterServiceTest {

    // mocks
    @Mock
    private SongController songController;

    @InjectMocks
    private static FilenameFormatterService underTest;

    @BeforeAll
    static void setup() {
        underTest = new FilenameFormatterService();
        mockStatic(MP3FileUtils.class);
        mockStatic(DialogUtils.class);
    }

    @Test
    public void testCleanupFeaturingTag_nothingToReplace() {
        String filename = "01 Track Title (ft. featured artist).mp3";
        assertEquals(filename, underTest.cleanupFeaturingTag(filename));
    }

    @Test
    public void testCleanupFeaturingTag_nothingToReplace_withRemixedByArtist() {
        String filename = "01 Track Title (ft. featured artist) (RemixArtist Remix).mp3";
        assertEquals(filename, underTest.cleanupFeaturingTag(filename));
    }

    @Test
    public void testCleanupFeaturingTag_replacesFeat() {
        String filename = "01 Track Title (feat. featured artist).mp3";
        String expectedFilename = "01 Track Title (ft. featured artist).mp3";
        assertEquals(expectedFilename, underTest.cleanupFeaturingTag(filename));

        filename = "01 Track Title (Feat. featured artist).mp3";
        assertEquals(expectedFilename, underTest.cleanupFeaturingTag(filename));
    }

    @Test
    public void testCleanupFeaturingTag_replacesFeat_withoutParentheses() {
        String filename = "01 Track Title feat. featured artist.mp3";
        String expectedFilename = "01 Track Title (ft. featured artist).mp3";
        assertEquals(expectedFilename, underTest.cleanupFeaturingTag(filename));
    }

    @Test
    public void testCleanupFeaturingTag_replacesFeat_withoutParentheses_withRemixedByArtist() {
        String filename = "01 Track Title feat. featured artist (RemixArtist Remix).mp3";
        String expectedFilename = "01 Track Title (ft. featured artist) (RemixArtist Remix).mp3";
        assertEquals(expectedFilename, underTest.cleanupFeaturingTag(filename));
    }

    @Test
    public void testCleanupCommonReplaceableStrings_nothingToReplace() {
        String filename = "01 Track Title.mp3";
        assertEquals(filename, underTest.cleanupCommonReplaceableStrings(filename));
    }

    @Test
    public void testCleanupCommonReplaceableStrings_replacesString() {
        String filename = "Track Title (Official Music Video).mp3";
        String expectedFilename = "Track Title.mp3";
        assertEquals(expectedFilename, underTest.cleanupCommonReplaceableStrings(filename));
    }

    @Test
    public void testCleanupCommonReplaceableStrings_replacesMultipleStrings() {
        String filename = "Track Title (Official Music Video) [Monstercat Release].mp3";
        String expectedFilename = "Track Title.mp3";
        assertEquals(expectedFilename, underTest.cleanupCommonReplaceableStrings(filename));
    }

    @Test
    public void testCleanupTrackNumber_padsOneDigitTrackNumber() {
        String filename = "1 Track Title.mp3";
        String expectedFilename = "01 Track Title.mp3";
        assertEquals(expectedFilename, underTest.cleanupTrackNumber(filename));
    }

    @Test
    public void testCleanupTrackNumber_removesPeriod() {
        String filename = "01. Track Title.mp3";
        String expectedFilename = "01 Track Title.mp3";
        assertEquals(expectedFilename, underTest.cleanupTrackNumber(filename));
    }

    @Test
    public void testCleanupTrackNumber_removesHyphenWithSpaces() {
        String filename = "01 - Track Title.mp3";
        String expectedFilename = "01 Track Title.mp3";
        assertEquals(expectedFilename, underTest.cleanupTrackNumber(filename));
    }

    @Test
    public void testCleanupTrackNumber_removesHyphenWithoutSpaces() {
        String filename = "01-Track Title.mp3";
        String expectedFilename = "01 Track Title.mp3";
        assertEquals(expectedFilename, underTest.cleanupTrackNumber(filename));
    }

    @ParameterizedTest
    @ValueSource(strings = {"01 Track Title.mp3", "01-Track Title.mp3", "01. Track Title.mp3", "1 Track Title.mp3"})
    public void testFormatFilename_givenRegularTrackWithTrackNumber_formatsFilename(String filename) {
        File file = new File("./test/" + filename);
        Song song = new Song();
        song.setFile(file);
        when(MP3FileUtils.isPartOfALabel(file, COMPILATIONS)).thenReturn(false);

        String expectedFilename = "01 Track Title.mp3";
        String actualFilename = underTest.formatFilename(song);

        assertEquals(expectedFilename, actualFilename);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Track Title.mp3"})
    public void testFormatFilename_givenRegularTrackWithNoTrackNumberAndExistingID3Information_formatsFilename(String filename) {
        File file = new File("./test/" + filename);
        Song song = new Song();
        song.setFile(file);
        song.setTrack("3");
        when(MP3FileUtils.isPartOfALabel(file, COMPILATIONS)).thenReturn(false);

        String expectedFilename = "03 Track Title.mp3";
        String actualFilename = underTest.formatFilename(song);

        assertEquals(expectedFilename, actualFilename);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Track Title.mp3"})
    public void testFormatFilename_givenRegularTrackWithNoTrackNumberAndNoExistingID3Information_formatsFilenameWithUserInput(String filename) {
        File file = new File("./test/" + filename);
        Song song = new Song();
        song.setFile(file);
        when(MP3FileUtils.isPartOfALabel(file, COMPILATIONS)).thenReturn(false);
        when(DialogUtils.showGetTitleAndTrackNumberDialog(any(), anyString())).thenReturn(new String[]{"2", "Track Title"});

        String expectedFilename = "02 Track Title.mp3";
        String actualFilename = underTest.formatFilename(song);

        assertEquals(expectedFilename, actualFilename);
    }

    @ParameterizedTest
    @ValueSource(strings = "01. Artist - Title.mp3")
    public void testFormatFilename_givenTrackInCompilation_formatsFilenameWithArtistAndSetsArtistOnSong(String filename) {
        File file = new File("./test/" + filename);
        Song song = new Song();
        song.setFile(file);
        song.setIndex(1);
        when(MP3FileUtils.isPartOfALabel(file, COMPILATIONS)).thenReturn(true);

        String expectedFilename = "01 Artist - Title.mp3";
        String actualFilename = underTest.formatFilename(song);

        assertEquals(expectedFilename, actualFilename);
        verify(songController, times(1)).setArtist(1, "Artist");
    }

    @ParameterizedTest
    @ValueSource(strings = {"01. Artist - Title.mp3", "01 - Artist - Title.mp3"})
    public void testFormatFilename_givenTrackWithSpecificArtistAndArtistID3TagExists_formatsFilenameWithoutArtist(String filename) {
        File file = new File("./test/" + filename);
        Song song = new Song();
        song.setFile(file);
        when(MP3FileUtils.isPartOfALabel(file, COMPILATIONS)).thenReturn(false);

        String expectedFilename = "01 Title.mp3";
        String actualFilename = underTest.formatFilename(song);

        assertEquals(expectedFilename, actualFilename);
    }

    @ParameterizedTest
    @ValueSource(strings = {"01. Artist - Title.mp3", "01 - Artist - Title.mp3"})
    public void testFormatFilename_givenTrackWithSpecificArtistAndArtistID3TagDoesNotExist_formatsFilenameWithoutArtistAndSetsArtistOnSong(String filename) {
        File file = new File("./test/" + filename);
        Song song = new Song();
        song.setFile(file);
        when(MP3FileUtils.isPartOfALabel(file, COMPILATIONS)).thenReturn(false);

        String expectedFilename = "01 Title.mp3";
        String actualFilename = underTest.formatFilename(song);

        assertEquals(expectedFilename, actualFilename);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Title.mp3"})
    public void testFormatFilename_givenTrackWithNoInformationInCompilation_whenPromptUserForInput_formatsFilename(String filename) {
        File file = new File("./test/" + filename);
        Song song = new Song();
        song.setFile(file);
        when(MP3FileUtils.isPartOfALabel(file, COMPILATIONS)).thenReturn(true);
        when(DialogUtils.showGetTitleAndTrackNumberAndArtistDialog(any(), anyString(), anyString())).thenReturn(
                new String[]{"1", "Artist", filename.substring(0, filename.length() - 4)});

        String expectedFilename = "01 Artist - Title.mp3";
        String actualFilename = underTest.formatFilename(song);

        assertEquals(expectedFilename, actualFilename);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Flume - Counting Sheep (V2) [2018 Export Wav] feat. Injury Reserve.mp3"})
    public void testFormatFilename_givenTrackWithNoInformation_whenPromptUserForInput_formatsGrossFilename(String filename) {
        File file = new File("./test/" + filename);
        Song song = new Song();
        song.setFile(file);
        when(MP3FileUtils.isPartOfALabel(file, COMPILATIONS)).thenReturn(false);
        String expectedIntermediaryFilename = "Counting Sheep (V2) [2018 Export Wav] (ft. Injury Reserve)";
        when(DialogUtils.showGetTitleAndTrackNumberDialog(any(), anyString())).thenReturn(
                new String[]{"1", expectedIntermediaryFilename});

        String expectedFilename = "01 Counting Sheep (V2) [2018 Export Wav] (ft. Injury Reserve).mp3";
        String actualFilename = underTest.formatFilename(song);

        assertEquals(expectedFilename, actualFilename);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Title.mp3"})
    public void testFormatFilename_givenTrackWithNoInformation_whenPromptUserForInputReturnsNull_thenReturnFilename(String filename) {
        File file = new File("./test/" + filename);
        Song song = new Song();
        song.setFile(file);
        when(MP3FileUtils.isPartOfALabel(file, COMPILATIONS)).thenReturn(false);
        when(DialogUtils.showGetTitleAndTrackNumberDialog(any(), anyString())).thenReturn(null);

        assertEquals(filename, underTest.formatFilename(song));
    }
}