package moose.utilities;

import com.mpatric.mp3agic.*;
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.StringUtils;
import moose.Moose;
import moose.objects.Song;

import java.io.File;
import java.io.IOException;

import static moose.utilities.Constants.*;

public class SongUtils {

    static Logger logger = Moose.getLogger();

    /**
     * Check if a directory is from a label
     *
     * @param dir, the directory to check
     * @return the result of the check, true if it is a label, false if it isn't
     * a label
     */
    public static boolean isPartOfALabel(File dir) {
        String path = dir.getPath();
        return (path.contains("/" + SINGLES + "/") ||
                path.contains("/" + COMPILATIONS + "/") ||
                path.contains("/" + LPS + "/") ||
                path.contains("/" + EPS + "/"));
    }

    /**
     * Check if a directory is a single in a label
     *
     * @param file, the file to check
     * @return the result of the check, true if it's a single from a label, false if it isn't
     */
    public static boolean isPartOfALabel(File file, String type) {
        String path = file.getPath();
        return path.contains("/" + type + "/");
    }

    /**
     * Gets a song object from a file
     * @param file, the file to get info from
     * @return a song object
     */
    public static Song getSongFromFile(File file) {

        // mp3agic Mp3File object, used for the id3tags
        Mp3File mp3file;
        try {
            // create the mp3file from the file's path
            mp3file = new Mp3File(file.getPath());

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
            if (logger != null) {
                logger.logError("Exception when trying to read data from file: " + file.getName(), e);
            }
            return null;
        }

        // get the id3v2 info
        String title = mp3file.getId3v2Tag().getTitle();
        String artist = mp3file.getId3v2Tag().getArtist();
        String album = mp3file.getId3v2Tag().getAlbum();
        String albumartist = mp3file.getId3v2Tag().getAlbumArtist();
        String genre = mp3file.getId3v2Tag().getGenreDescription();
        String track = mp3file.getId3v2Tag().getTrack();
        String disk = mp3file.getId3v2Tag().getPartOfSet();
        byte[] artwork_bytes = mp3file.getId3v2Tag().getAlbumImage();

        // get the year using both because year can be in two places for some reason?
        String year = getYear(mp3file);

        int bitrate = mp3file.getBitrate();
        int samplerate = mp3file.getSampleRate();
        long len = mp3file.getLengthInSeconds();
        String comment = mp3file.getId3v2Tag().getComment();

        // sets the strings to blank to avoid NPE
        title = StringUtils.validateString(title);
        artist = StringUtils.validateString(artist);
        album = StringUtils.validateString(album);
        albumartist = StringUtils.validateString(albumartist);
        genre = StringUtils.validateString(genre);
        year = StringUtils.validateString(year);
        track = StringUtils.validateString(track);
        disk = StringUtils.validateString(disk);
        comment = StringUtils.validateString(comment);
        artwork_bytes = artwork_bytes != null ? artwork_bytes : new byte[0];

        // create a song object with the information
        return new Song(file, title, artist, album, albumartist, genre, year, track, disk, artwork_bytes, bitrate, samplerate, len, comment);
    }

    /**
     * Utility function to get the year from an mp3 file
     * @param mp3file, the mp3 file object to read from
     */
    private static String getYear(Mp3File mp3file) {
        String v2Year = mp3file.getId3v2Tag().getYear();
        String v1Year = mp3file.getId3v1Tag().getYear();
        if (StringUtils.isNotEmpty(v2Year) && StringUtils.isNotEmpty(v1Year)) {
            return v2Year;
        } else if (StringUtils.isNotEmpty(v2Year) && StringUtils.isEmpty(v1Year)) {
            return v2Year;
        } else if (StringUtils.isEmpty(v2Year) && StringUtils.isNotEmpty(v1Year)) {
            return v1Year;
        }
        return StringUtils.EMPTY;
    }
}
