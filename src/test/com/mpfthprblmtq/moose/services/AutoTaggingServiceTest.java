package com.mpfthprblmtq.moose.services;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class AutoTaggingServiceTest {

    AutoTaggingService underTest = new AutoTaggingService();

    @Test
    public void testGetTitleFromFile_processesTrackNumArtistTitleFilename() {
        File file = new File("01 Artist - Title.mp3");
        assertEquals("Title", underTest.getTitleFromFile(file));
    }

    @Test
    public void testGetTitleFromFile_processesTrackNumTitleFilename() {
        File file = new File("01 Title.mp3");
        assertEquals("Title", underTest.getTitleFromFile(file));
    }

    @Test
    public void testGetTitleFromFile_processesTitleFilename() {
        File file = new File("01 Title.mp3");
        assertEquals("Title", underTest.getTitleFromFile(file));
    }

}