package com.mpfthprblmtq.moose.services;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.ID3v2TagFactory;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NoSuchTagException;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.utilities.viewUtils.ViewUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SongService {

    // logger object
    Logger logger = Moose.getLogger();

    /**
     * Gets a song object from a file
     * @param file, the file to get info from
     * @return a song object
     */
    public Song getSongFromFile(File file) {

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
            // things broke
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

        String bitrate = String.valueOf(mp3file.getBitrate());
        String sampleRate = String.valueOf(mp3file.getSampleRate());
        String len = String.valueOf(mp3file.getLengthInSeconds());
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
        return Song.builder()
                .file(file)
                .title(title)
                .artist(artist)
                .album(album)
                .albumArtist(albumArtist)
                .genre(genre)
                .year(year)
                .track(track)
                .disk(disk)
                .artwork_bytes(artwork_bytes)
                .bitrate(bitrate)
                .sampleRate(sampleRate)
                .length(len)
                .comment(comment)
                .build();
    }

    /**
     * Saves an individual track
     * @param song the song to save
     */
    public boolean save(Song song) {

        // check to see if we need to rename the file
        if (song.getNewFile() != null) {
            if (!song.getNewFile().getName().endsWith(".mp3")) {
                song.setNewFile(new File(song.getNewFile().getAbsolutePath().concat(".mp3")));
            }
            if (!song.getFile().renameTo(song.getNewFile())) {
                logger.logError("Problem saving a file on file name change, file: " + song.getFile().getName());
                ViewUtils.showErrorDialog("Couldn't rename file from " + song.getFile().getName() + " to " + song.getNewFile().getName(), Moose.getFrame());
                return false;
            } else {
                song.setFile(song.getNewFile());
                song.setNewFile(null);
            }
        }

        // try to get the mp3file object from the file
        Mp3File mp3file;
        try {
            mp3file = new Mp3File(song.getFile().getAbsolutePath());
            ID3v2 tag = new ID3v24Tag();
            mp3file.setId3v2Tag(tag);
        } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
            logger.logError("Couldn't save file: " + song.getFile().getName(), ex);
            ViewUtils.showErrorDialog("Couldn't save file: " + song.getFile().getName(), ex, Moose.getFrame());
            return false;
        }

        // set all the text based items
        try {
            // id3v2Tag
            mp3file.getId3v2Tag().setTitle(song.getTitle());
            mp3file.getId3v2Tag().setArtist(song.getArtist());
            mp3file.getId3v2Tag().setAlbum(song.getAlbum());
            mp3file.getId3v2Tag().setAlbumArtist(song.getAlbumArtist());
            mp3file.getId3v2Tag().setYear(song.getYear());
            mp3file.getId3v2Tag().setGenreDescription(song.getGenre());
            mp3file.getId3v2Tag().setTrack(song.getFullTrackString());
            mp3file.getId3v2Tag().setPartOfSet(song.getFullDiskString());
            mp3file.getId3v2Tag().setComment(song.getComment());
        } catch (IllegalArgumentException ignored) {
            // this exception doesn't really matter
            // this only happens if you save a track with no genre
        }

        // set album art
        String type = "image/jpeg";
        mp3file.getId3v2Tag().clearAlbumImage();
        mp3file.getId3v2Tag().setAlbumImage(song.getArtwork_bytes(), type);

        // save the id3 info, and return the result
        return saveID3Info(mp3file, song.getFile());
    }

    /**
     * Saves an individual file's id3 information
     * @param mp3file the mp3 file to source the information from
     * @param file the actual file to save as
     * @return the result of the save
     */
    public boolean saveID3Info(Mp3File mp3file, File file) {
        try {
            // save the new mp3file
            mp3file.save(file.getAbsolutePath().replace(".mp3", "_.mp3"));

            // delete the old file
            if (!file.delete()) {
                logger.logError("Couldn't delete the file to save! Path: " + file.getPath());
                ViewUtils.showErrorDialog("Couldn't delete the file to save! Path: " + file.getPath(), Moose.getFrame());
                return false;
            }

            // rename the new file to match the old
            File newFile = new File(file.getAbsolutePath().replace(".mp3", "_.mp3"));
            if (!newFile.renameTo(file)) {
                logger.logError("Couldn't rename the new mp3 back to the original after deleting! Path: " + newFile.getPath());
                ViewUtils.showErrorDialog("Couldn't rename the new mp3 back to the original after deleting! Path: " + newFile.getPath(), Moose.getFrame());
                return false;
            }

        } catch (IOException | NotSupportedException ex) {
            logger.logError("Exception when trying to save a song!", ex);
            ViewUtils.showErrorDialog("Exception occurred while saving file!", ex, Moose.getFrame());
            return false;
        }

        // save was successful
        return true;
    }

    /**
     * Utility function to get the year from the mp3 file.  Since the year can be in both the ID3v2.3 tag AND the
     * ID3v2.4 tag, want to make sure we're getting at least one of those.
     * @param mp3file the mp3 file object to read from
     * @param id3v24Tag the id3v2.4 tag
     */
    private String getYear(Mp3File mp3file, ID3v24Tag id3v24Tag) {
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

    /**
     * Scans all the songs or files' mp3tags with them and checks to make sure we know the genre
     * @param list the list of either Songs or Files to check
     */
    public void checkForNewGenres(List<?> list) {
        List<Song> songs = new ArrayList<>();

        // if the list given is a list of files, get the songs from those files first
        if (list.isEmpty()) {
            return;
        }
        if (list.get(0) instanceof File) {
            for (Object file : list) {
                songs.add(getSongFromFile((File) file));
            }
        } else if (list.get(0) instanceof Song) {
            for (Object song : list) {
                songs.add((Song) song);
            }
        } else {
            return;
        }

        // get all the songs, then the genres from the list of files
        List<String> genres = songs.stream().map(Song::getGenre).collect(Collectors.toList());

        // create a list of all the genres that don't exist already
        List<String> newGenres = new ArrayList<>();
        genres.stream().filter((genre) -> (!Moose.getSettings().getGenres().contains(genre) && StringUtils.isNotEmpty(genre))).forEachOrdered((genre) -> {
            if (!newGenres.contains(genre)) {
                newGenres.add(genre);
            }
        });

        // for each new genre, ask if we want to add that one
        for (String newGenre : newGenres) {
            int res = JOptionPane.showConfirmDialog(Moose.getFrame(), "\"" + newGenre + "\" isn't in your built-in genre list, would you like to add it?");
            if (res == JOptionPane.YES_OPTION) {
                // add the genre to the settings and update
                Moose.getSettings().getGenres().add(newGenre);
                Moose.updateSettings();
            }
        }
    }
}
