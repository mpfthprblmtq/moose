package moose.utilities;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.mpatric.mp3agic.InvalidDataException;
import moose.Main;
import moose.objects.Song;
import moose.utilities.logger.Logger;

import java.io.File;
import java.io.IOException;

public class SongUtils {

    static Logger logger = Main.getLogger();

    /**
     * Check if a directory is from a label
     *
     * @param dir, the directory to check
     * @return the result of the check, true if it is a label, false if it isn't
     * a label
     */
    public static boolean isPartOfALabel(File dir) {
        String path = dir.getPath();
        return (path.contains("/Genres/") || path.contains("/EPs/") || path.contains("/EP's/"));
    }

    public static boolean isAnEPPartOfALabel(File dir) {
        String path = dir.getPath();
        return (path.contains("/EPs/") || path.contains("/EP's/"));
    }

    public static boolean isAGenrePartOfALabel(File dir) {
        String path = dir.getPath();
        return path.contains("/Genres/");
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

            // if the mp3file doesn't have an id3tag, create one
            if (!mp3file.hasId3v2Tag()) {
                ID3v2 tag = new ID3v24Tag();
                mp3file.setId3v2Tag(tag);
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            // things borked
            mp3file = null;
            logger.logError("Exception when trying to read data from file: " + file.getName(), e);
        }

        // check if that mp3file is null first
        if (mp3file == null) {
            return null;
        }

        // get the id3v2 info
        String title = mp3file.getId3v2Tag().getTitle();
        String artist = mp3file.getId3v2Tag().getArtist();
        String album = mp3file.getId3v2Tag().getAlbum();
        String albumartist = mp3file.getId3v2Tag().getAlbumArtist();
        String genre = mp3file.getId3v2Tag().getGenreDescription();
        String year = mp3file.getId3v2Tag().getYear();
        String track = mp3file.getId3v2Tag().getTrack();
        String disk = mp3file.getId3v2Tag().getPartOfSet();
        byte[] artwork_bytes = mp3file.getId3v2Tag().getAlbumImage();

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
}
