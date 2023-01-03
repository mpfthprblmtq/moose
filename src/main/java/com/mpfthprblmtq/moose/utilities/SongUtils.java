/*
 *  Proj:   Moose
 *  File:   SongUtils.java
 *  Desc:   A utility class to pull out common logic from interacting with Song data.  Includes a function used to
 *          retrieve existing ID3 information from a file and throws it in a Song object.
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.utilities;

// imports
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.ID3v2TagFactory;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NoSuchTagException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Song;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// class SongUtils
public class SongUtils {

    static Logger logger = Moose.getLogger();

    /**
     * Gets a song object from a file
     * @param file, the file to get info from
     * @return a song object
     */
    public static Song getSongFromFile(File file) {

        // check if file is mp3
        if (!file.getName().endsWith(".mp3")) {
            return null;
        }

        // mp3agic Mp3File and the ID3v24 tag objects, used for the id3tags
        Mp3File mp3file;
        ID3v24Tag id3v24tag;
        try {
            // create the mp3file from the file's path
            mp3file = new Mp3File(file.getPath());

            // create the id3v24 tag (for the year)
            try {
                byte[] buffer = Files.readAllBytes(Paths.get(file.getPath()));
                id3v24tag = (ID3v24Tag) ID3v2TagFactory.createTag(buffer);
            } catch (ClassCastException | NoSuchTagException e) {
                // class cast exception will happen if there is no ID3v24 tag on the file, so let's make one
                id3v24tag = new ID3v24Tag();
            }

            // if the mp3file doesn't have an id3v2tag, create one
            if (!mp3file.hasId3v2Tag()) {
                ID3v2 tag = new ID3v24Tag();
                mp3file.setId3v2Tag(tag);
            }
            // same thing for the id3v1tag
            if (!mp3file.hasId3v1Tag()) {
                ID3v1 tag = new ID3v1Tag();
                mp3file.setId3v1Tag(tag);
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            // things borked
            logger.logError("Exception when trying to read data from file: " + file.getName(), e);
            return null;
        }

        // get the id3v2 info
        String title = mp3file.getId3v2Tag().getTitle();
        String artist = mp3file.getId3v2Tag().getArtist();
        String album = mp3file.getId3v2Tag().getAlbum();
        String albumArtist = mp3file.getId3v2Tag().getAlbumArtist();
        String genre = mp3file.getId3v2Tag().getGenreDescription();
        String track = mp3file.getId3v2Tag().getTrack();
        String disk = mp3file.getId3v2Tag().getPartOfSet();
        byte[] artwork_bytes = mp3file.getId3v2Tag().getAlbumImage();

        // get the year using both because year can be in two places for some reason?
        String year = getYear(mp3file, id3v24tag);

        int bitrate = mp3file.getBitrate();
        int sampleRate = mp3file.getSampleRate();
        long len = mp3file.getLengthInSeconds();
        String comment = mp3file.getId3v2Tag().getComment();

        // sets the strings to blank to avoid NPE
        title = StringUtils.validateString(title);
        artist = StringUtils.validateString(artist);
        album = StringUtils.validateString(album);
        albumArtist = StringUtils.validateString(albumArtist);
        genre = StringUtils.validateString(genre);
        year = StringUtils.validateString(year);
        track = StringUtils.validateString(track);
        disk = StringUtils.validateString(disk);
        comment = StringUtils.validateString(comment);
        artwork_bytes = artwork_bytes != null ? artwork_bytes : new byte[0];

        // create a song object with the information
        return new Song(file, title, artist, album, albumArtist, genre, year, track, disk, artwork_bytes,
                bitrate, sampleRate, len, comment);
    }

    /**
     * Utility function to get the year from the mp3 file.  Since the
     * @param mp3file, the mp3 file object to read from
     */
    private static String getYear(Mp3File mp3file, ID3v24Tag id3v24Tag) {
        String v2Year = mp3file.getId3v2Tag().getYear();
        String v24Year = id3v24Tag.getRecordingTime();

        // if both are valid and different
        if (StringUtils.isNotEmpty(v2Year) && StringUtils.isNotEmpty(v24Year) && !v2Year.equals(v24Year)) {
            logger.logError("Years don't match in id3Tag for file: " + mp3file.getFilename() +
                    ", v2 Year: " + v2Year + ", v2.4 Year: " + v24Year);
            return v2Year; // return the regular year since that's probably the most accurate
        } else if (StringUtils.isNotEmpty(v2Year) && StringUtils.isNotEmpty(v24Year)) {
            return v2Year;
        } else if (StringUtils.isNotEmpty(v2Year) && StringUtils.isEmpty(v24Year)) {
            return v2Year;
        } else if (StringUtils.isEmpty(v2Year) && StringUtils.isNotEmpty(v24Year)) {
            return v24Year;
        }
        return StringUtils.EMPTY;
    }
}
