package com.mpfthprblmtq.moose.services;

import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.utilities.SongUtils;

import java.io.File;

public class AutoTaggingService_V2 {

    public AutoTaggingService_V2() {}

    /**
     * Gets the artist from existing ID3 information
     * @param file the file with the ID3 information to check
     * @return the artist if found, else a blank string
     */
    public String getArtistFromExistingID3Info(File file) {
        Song s = Moose.getSongController_v2().getSongService().getSongFromFile(file);
        if (s != null) {
            return s.getArtist();
        }
        return StringUtils.EMPTY;
    }

    public void setArtistOnSong(File file, String artist) {

    }
}
