package com.mpfthprblmtq.moose.controllers;

import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.services.AutoTaggingService;
import com.mpfthprblmtq.moose.services.FilenameFormatterService;
import com.mpfthprblmtq.moose.utilities.Constants;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SongController_V2 {

    // services
    public AutoTaggingService autoTaggingService;
    public FilenameFormatterService filenameFormatterService;

    // logger object
    Logger logger = Moose.getLogger();

    // lists/maps
    HashMap<Integer, Song> songs = new HashMap<>(); // hashmap to contain Song objects
    List<Integer> edited_songs = new ArrayList<>(); // arraylist to contain indices of edited songs to save

    // ivar to check if user has unsaved changes
    boolean hasUnsavedChanges = false;

    /**
     * Default constructor, sets the autotagging and filename formatter services
     */
    public SongController_V2() {
//        autoTaggingService = new AutoTaggingService(this);
//        filenameFormatterService = new FilenameFormatterService(this);
    }

    /**
     * Returns the hasUnsavedChanges flag
     */
    public boolean hasUnsavedChanges() {
        return this.hasUnsavedChanges;
    }

    // <editor-fold defaultstate="collapsed" desc="SONG SETTERS">
    /**
     * Helper function to set the new file of the song file in the songs list.
     * @param index the index of the song
     * @param newFile the new file to set
     */
    public void setNewFile(int index, File newFile) {
        songs.get(index).setNewFile(newFile);
        songEdited(index);
    }

    /**
     * Helper function to set the title of the song in the songs list.
     * @param index the index of the song
     * @param title the title to set
     */
    public void setTitle(int index, String title) {
        songs.get(index).setTitle(title);
        songEdited(index);
    }

    /**
     * Helper function to set the artist of the song in the songs list.
     * @param index the index of the song
     * @param artist the artist to set
     */
    public void setArtist(int index, String artist) {
        songs.get(index).setArtist(artist);
        songEdited(index);
    }

    /**
     * Helper function to set the album of the song in the songs list.
     * @param index the index of the song
     * @param album the album to set
     */
    public void setAlbum(int index, String album) {
        songs.get(index).setAlbum(album);
        songEdited(index);
    }

    /**
     * Helper function to set the album artist of the song in the songs list.
     * @param index the index of the song
     * @param albumArtist the albumArtist to set
     */
    public void setAlbumArtist(int index, String albumArtist) {
        songs.get(index).setAlbumArtist(albumArtist);
        songEdited(index);
    }

    /**
     * Helper function to set the genre of the song in the songs list.
     * @param index the index of the song
     * @param genre the genre to set
     */
    public void setGenre(int index, String genre) {
        songs.get(index).setGenre(genre);
        songEdited(index);
    }

    /**
     * Helper function to set the year of the song in the songs list.
     * @param index the index of the song
     * @param year the year to set
     */
    public void setYear(int index, String year) {
        songs.get(index).setYear(year);
        songEdited(index);
    }

    /**
     * Helper function to set the track of the song in the songs list.
     * @param index the index of the song
     * @param track the track to set
     */
    public void setTrack(int index, String track) {
        songs.get(index).setTrack(track);
        songEdited(index);
    }

    /**
     * Helper function to set the total tracks of the song in the songs list.
     * @param index the index of the song
     * @param totalTracks the totalTracks to set
     */
    public void setTotalTracks(int index, String totalTracks) {
        songs.get(index).setTotalTracks(totalTracks);
        songEdited(index);
    }

    /**
     * Helper function to set the disk of the song in the songs list.
     * @param index the index of the song
     * @param disk the disk to set
     */
    public void setDisk(int index, String disk) {
        songs.get(index).setDisk(disk);
        songEdited(index);
    }

    /**
     * Helper function to set the total disks of the song in the songs list.
     * @param index the index of the song
     * @param totalDisks the totalDisks to set
     */
    public void setTotalDisks(int index, String totalDisks) {
        songs.get(index).setTotalDisks(totalDisks);
        songEdited(index);
    }

    /**
     * Helper function to set the album image of the song in the songs list.
     * @param index the index of the song
     * @param bytes the byte array of the album image to set
     */
    public void setAlbumImage(int index, byte[] bytes) {
        songs.get(index).setArtwork_bytes(bytes);
        songEdited(index);
    }

    /**
     * Helper function to set the comment of the song in the songs list.
     * @param index the index of the song
     * @param comment the comment to set
     */
    public void setComment(int index, String comment) {
        songs.get(index).setComment(comment);
        songEdited(index);
    }

    /**
     * Adds the song index to edited_songs to save, and updates the row icon
     * @param index the index to add to edited_songs
     */
    public void songEdited(int index) {
        if (!edited_songs.contains(index)) {
            edited_songs.add(index);
            Moose.getFrame().setRowIcon(Constants.EDITED, getRow(index));
            this.hasUnsavedChanges = true;
        }
        // else do nothing, index is already added
    }
    // </editor-fold>

    /**
     * Adds a song to the song map with the next available index
     * @param s the song to add
     */
    public void addSong(Song s) {
        Collection<Song> existingSongs = getSongs().values();
        for (Song song : existingSongs) {
            if (song.equals(s)) {
                return;
            }
        }
        s.setIndex(getSongs().size());
        songs.put(s.getIndex(), s);
    }

    /**
     * Returns all the files currently in the table
     * @return the files currently in the main table
     */
    public List<File> getAllFilesInTable() {
        return songs.values().stream()
                .map(Song::getFile)
                .collect(Collectors.toList());
    }

    /**
     * Gets row from an index
     * @param index the index of the song
     * @return the row where the index matches
     */
    public int getRow(int index) {
        for (int i = 0; i < Moose.getFrame().getTable().getRowCount(); i++) {
            if (getIndex(i) == index) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper method to get the index from whatever we give it
     * @param t the object to find the index with. Can be:
     *          - Integer: the row on the table
     *          - Song: a song object with data to compare with
     *          - List<File>: a collection of two files, first index being the new file, second being the old file
     */
    @SuppressWarnings("unchecked")  // for the cast to List<File>
    public <T> int getIndex(T t) {
        if (t instanceof Integer) {
            int row = (Integer) t;
            row = Moose.getFrame().getTable().convertRowIndexToModel(row);
            return Integer.parseInt(Moose.getFrame().getTable().getModel().getValueAt(row, 12).toString());

        } else if (t instanceof Song) {
            Song song = (Song) t;
            for (Integer index : getSongs().keySet()) {
                Song songInMap = getSongs().get(index);
                if (song.equals(songInMap, false)) {
                    return index;
                }
            }

        } else if (t instanceof List) { // TODO remove this type of index get, there has to be a better way
            List<File> files = (List<File>) t;
            File newFile = files.get(0);
            File oldFile = files.get(1);
            for (Integer index : getSongs().keySet()) {
                Song songInMap = getSongs().get(index);
                if (songInMap.getFile().getPath().equals(oldFile.getPath())
                        || songInMap.getNewFile().getPath().equals(newFile.getPath())) {
                    return index;
                }
            }
        }
        return -1; // index wasn't found
    }


}
