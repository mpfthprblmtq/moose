/*
   Proj:   Moose
   File:   SongController.java
   Desc:   Controller class for Frame, works directly with the data based on input from Frame UI

   Copyright Pat Ripley 2018
 */

// package
package moose.controllers;

// imports

import com.mpatric.mp3agic.*;

import moose.services.AutoTaggingService;
import moose.utilities.Constants;
import moose.utilities.Logger;
import moose.Main;
import moose.objects.Song;
import moose.utilities.Utils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

// class SongController
public class SongController {

    // table object that references the JTable on the form
    JTable table;

    // AutoTaggingService
    public AutoTaggingService autoTaggingService = new AutoTaggingService();

    // logger object
    Logger logger = Main.getLogger();

    // ArrayLists
    HashMap<Integer, Song> songs = new HashMap<>();     // hashmap to contain Song objects
    ArrayList<Integer> edited_songs = new ArrayList<>();           // arraylist to contain indices of edited songs to save

    /**
     * Default constructor
     */
    public SongController() {
    }

    /**
     * Sets the table ivar
     *
     * @param table, the table to set
     */
    public void setTable(JTable table) {
        this.table = table;
    }

    /**
     * Returns the songs list
     *
     * @return the songs map
     */
    public HashMap<Integer, Song> getSongs() {
        return songs;
    }

    /**
     * Returns a list of all the files in the table currently
     */
    public List<File> getAllFiles() {
        List<File> files = new ArrayList<>();
        for (Song song : getSongs().values()) {
            files.add(song.getFile());
        }
        return files;
    }

    /**
     * Gets a song object from a file
     *
     * @param file, the file to get info from
     * @return a song object
     */
    public Song getSongFromFile(File file) {

        // regex for checking tracks/disks for "/0" or similar
        String regex = "/\\d*";

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

        // get the id3v2 info
        assert mp3file != null;
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
        if (title == null) {
            title = "";
        }
        if (artist == null) {
            artist = "";
        }
        if (album == null) {
            album = "";
        }
        if (albumartist == null) {
            albumartist = "";
        }
        if (genre == null) {
            genre = "";
        }
        if (year == null) {
            year = "";
        }
        if (track == null || track.matches(regex)) {
            track = "";
        }
        if (disk == null || disk.matches(regex)) {
            disk = "";
        }
        if (artwork_bytes == null) {
            artwork_bytes = new byte[0];
        }
        if (comment == null) {
            comment = "";
        }

        // create a song object with the information
        Song s = new Song(file, title, artist, album, albumartist, genre, year, track, disk, artwork_bytes, bitrate, samplerate, len, comment);

        // make an index
        int index = songs.size();

        // add the song to the list
        addSong(index, s);

        return s;
    }

    /**
     * Adds a song to the map
     * @param index, the index key
     * @param s, the song to add
     */
    private void addSong(int index, Song s) {
        Collection<Song> existingSongs = getSongs().values();
        for (Song song : existingSongs) {
            if (song.equals(s)) {
                return;
            }
        }
        songs.put(index, s);
    }

    /**
     * Adds the song index to edited_songs to save, and updates the row icon
     *
     * @param index, the index to add to edited_songs
     */
    public void songEdited(int index) {
        if (!edited_songs.contains(index)) {
            edited_songs.add(index);
            int row = getRow(index);
            Main.frame.setRowIcon(Constants.EDITED, row);
        }
        // else do nothing, index is already added
    }

    /**
     * Helper Function Gets row from an index
     *
     * @param index, the index of the song
     * @return the row where the index matches
     */
    public int getRow(int index) {
        for (int i = 0; i < table.getRowCount(); i++) {
            if (getIndex(i) == index) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Helper Function Gets the index at the specified row
     *
     * @param row, the row to search from
     * @return the index of that row
     */
    public int getIndex(int row) {
        row = table.convertRowIndexToModel(row);
        return Integer.parseInt(table.getModel().getValueAt(row, 12).toString());
    }

    /**
     * Helper function to set the file of the song file in the songs arraylist.
     *
     * @param index,    the index of the song
     * @param new_file, the new file to set
     */
    public void setFile(int index, File new_file) {
        songs.get(index).setFile(new_file);
        songEdited(index);
    }

    /**
     * Helper function to set the title of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param title, the title to set
     */
    public void setTitle(int index, String title) {
        songs.get(index).setTitle(title);
        songEdited(index);
    }

    /**
     * Helper function to set the artist of the song in the songs arraylist.
     *
     * @param index,  the index of the song
     * @param artist, the artist to set
     */
    public void setArtist(int index, String artist) {
        songs.get(index).setArtist(artist);
        songEdited(index);
    }

    /**
     * Helper function to set the album of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param album, the album to set
     */
    public void setAlbum(int index, String album) {
        songs.get(index).setAlbum(album);
        songEdited(index);
    }

    /**
     * Helper function to set the album artist of the song in the songs
     * arraylist.
     *
     * @param index,       the index of the song
     * @param albumartist, the albumartist to set
     */
    public void setAlbumArtist(int index, String albumartist) {
        songs.get(index).setAlbumArtist(albumartist);
        songEdited(index);
    }

    /**
     * Helper function to set the genre of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param genre, the genre to set
     */
    public void setGenre(int index, String genre) {
        songs.get(index).setGenre(genre);
        songEdited(index);
    }

    /**
     * Helper function to set the year of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param year,  the year to set
     */
    public void setYear(int index, String year) {
        songs.get(index).setYear(year);
        songEdited(index);
    }

    /**
     * Helper function to set the track of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param track, the track to set
     */
    public void setTrack(int index, String track) {
        songs.get(index).setTrack(track);
        songEdited(index);
    }

    /**
     * Helper function to set the disk of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param disk,  the disk to set
     */
    public void setDisk(int index, String disk) {
        songs.get(index).setDisk(disk);
        songEdited(index);
    }

    /**
     * Helper function to set the album image of the song in the songs
     * arraylist.
     *
     * @param index, the index of the song
     * @param bytes, the byte array of the album image to set
     */
    public void setAlbumImage(int index, byte[] bytes) {
        songs.get(index).setArtwork_bytes(bytes);
        songEdited(index);
    }

    /**
     * Helper function to set the comment of the song in the songs arraylist.
     *
     * @param index,   the index of the song
     * @param comment, the comment to set
     */
    public void setComment(int index, String comment) {
        songs.get(index).setComment(comment);
        songEdited(index);
    }

    /**
     * Goes through the edited_songs array and saves each one
     * this works
     */
    public void saveAll() {

        int count = 0;

        // traverse the array of songs and save them all
        for (int i = 0; i < songs.size(); i++) {
            if (save(i)) {
                count++;
            }
        }
        Main.frame.updateConsole(count + " file(s) updated!");
    }

    /**
     * Grabs the index from the selected row, then calls save() with that index
     *
     * @param selectedRows, the row indices to save
     */
    public void saveTracks(int[] selectedRows) {

        int count = 0;

        // traverse the array of rows and play each file sequentially
        for (int selectedRow : selectedRows) {
            int row = table.convertRowIndexToModel(selectedRow);    // get the row
            int index = getIndex(row);
            save(index);
            count++;
        }
        Main.frame.updateConsole(count + " file(s) updated!");
    }

    /**
     * Saves an individual track
     *
     * @param index, the index of the song to save in the songs map
     * @return the result of the save, true for success, false for not success
     */
    public boolean save(int index) {
        if (edited_songs.contains(index)) {
            Song s = songs.get(index);
            File file = s.getFile();
            Mp3File mp3file = null;

            try {
                mp3file = new Mp3File(file.getAbsolutePath());
                ID3v2 tag = new ID3v24Tag();
                mp3file.setId3v2Tag(tag);
            } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                System.err.println(ex);
            }

            // set all the text based items
            try {
                assert mp3file != null;
                mp3file.getId3v2Tag().setTitle(s.getTitle());
                mp3file.getId3v2Tag().setArtist(s.getArtist());
                mp3file.getId3v2Tag().setAlbum(s.getAlbum());
                mp3file.getId3v2Tag().setAlbumArtist(s.getAlbumArtist());
                mp3file.getId3v2Tag().setGenreDescription(s.getGenre());
                mp3file.getId3v2Tag().setYear(s.getYear());
                mp3file.getId3v2Tag().setTrack(s.getFullTrackString());
                mp3file.getId3v2Tag().setPartOfSet(s.getFullDiskString());
                mp3file.getId3v2Tag().setComment(s.getComment());
            } catch (IllegalArgumentException ex) {
                // this exception doesn't really matter
                // this only happens if you save a track with no genre
            }

            // set album art
            String type = "image/jpeg";
            mp3file.getId3v2Tag().clearAlbumImage();
            mp3file.getId3v2Tag().setAlbumImage(s.getArtwork_bytes(), type);

            // not a recursive call, this function does the actual file saving
            save(mp3file, file);

            // update the row graphic
            Main.frame.setRowIcon(Constants.SAVED, getRow(index));

            // done saving, remove it
            // gives an IndexOutOfBoundsException when trying to remove() with one element in it
            if (edited_songs.size() == 1) {
                edited_songs.clear();
            } else if (edited_songs.size() > 1) {
                // edited_songs.remove(index);
                edited_songs.remove(Integer.valueOf(index));
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Saves an individual file
     *
     * @param mp3file, the mp3 file to source the information from
     * @param file,    the actual file to save as
     */
    public void save(Mp3File mp3file, File file) {
        try {
            // save the new mp3file
            mp3file.save(file.getAbsolutePath().replace(".mp3", "_.mp3"));

            // delete the old file
            if (!file.delete()) {
                logger.logError("Couldn't delete the file to save! Path: " + file.getPath());
                return;
            }

            // rename the new file to match the old
            File newFile = new File(file.getAbsolutePath().replace(".mp3", "_.mp3"));
            if (!newFile.renameTo(file)) {
                logger.logError("Couldn't rename the new mp3 back to the original after deleting! Path: " + newFile.getPath());
            }

        } catch (IOException | NotSupportedException ex) {
            logger.logError("Exception when trying to save a song!", ex);
        }
    }

    /**
     * Does the finding and replacing from showFindAndReplaceDialog()
     *
     * @param find,         the string to find
     * @param replace,      the string to replace
     * @param includeFiles, a boolean to check if we're including the file names
     *                      in the search
     * @return the results of the replace, true if there was something to
     * replace, false if not
     */
    public int findAndReplace(String find, String replace, boolean includeFiles) {
        int count = 0;
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 0; j < table.getColumnCount(); j++) {
                if (table.getValueAt(i, j).toString().contains(find)) {
                    String toReplace = table.getValueAt(i, j).toString().replace(find, replace);
                    int index = getIndex(i);
                    switch (j) {
                        case 1:
                            if (includeFiles) {
                                table.setValueAt(toReplace, i, j);
                                File old_file = (File) table.getModel().getValueAt(i, 1);
                                String path = old_file.getPath().replace(old_file.getName(), "");
                                String fileName = table.getValueAt(i, j).toString();
                                File new_file = new File(path + "//" + fileName + ".mp3");

                                setFile(index, new_file);
                                if (!old_file.renameTo(new_file)) {
                                    logger.logError("Couldn't rename the file from Find and Replace! File: " + old_file.getPath());
                                }
                                table.getModel().setValueAt(new_file, i, 1);
                            }
                            count++;
                            break;
                        case 2:     // title
                            table.setValueAt(toReplace, i, j);
                            setTitle(index, toReplace);
                            count++;
                            break;
                        case 3:     // artist
                            table.setValueAt(toReplace, i, j);
                            setArtist(index, toReplace);
                            count++;
                            break;
                        case 4:     // album
                            table.setValueAt(toReplace, i, j);
                            setAlbum(index, toReplace);
                            count++;
                            break;
                        case 5:     // album artist
                            table.setValueAt(toReplace, i, j);
                            setAlbumArtist(index, toReplace);
                            count++;
                            break;
                        case 6:     // year
                            table.setValueAt(toReplace, i, j);
                            setYear(index, toReplace);
                            count++;
                            break;
                        case 7:     // genre
                            table.setValueAt(toReplace, i, j);
                            setGenre(index, toReplace);
                            count++;
                            break;
                        case 8:     // tracks
                            table.setValueAt(toReplace, i, j);
                            setTrack(index, toReplace);
                            count++;
                            break;
                        case 9:     // disks
                            table.setValueAt(toReplace, i, j);
                            setDisk(index, toReplace);
                            count++;
                            break;
                        default:
                            break;
                    }

                }
            }
        }
        return count;
    }

    /**
     * Method for removing album art for songs manually This method is called
     * when the "Remove" selection is pressed in the context menu
     *
     * @param selectedRows, the rows selected on the table
     */
    public void removeAlbumArt(int[] selectedRows) {

        for (int selectedRow : selectedRows) {

            // get the row and index of the track
            int row = table.convertRowIndexToModel(selectedRow);
            int index = getIndex(row);

            // update the songs array
            songs.get(index).setArtwork_bytes(null);

            // send the track to the edited_songs array
            songEdited(index);

            // update graphics
            table.getModel().setValueAt(null, row, 11);
            Main.frame.multImage.setIcon(null);
        }
    }

    /**
     * Function that looks at the file's name and location and auto generates
     * some tags
     *
     * @param selectedRows, the rows selected on the table
     */
    public void autoTagFiles(int[] selectedRows) {
        autoTaggingService.autoTag(selectedRows);
    }

    /**
     * Moves selected files to a new destination
     *
     * @param selectedRows, the rows to move
     */
    public void moveFiles(int[] selectedRows) {

        File directory = Objects.requireNonNull(Utils.launchJFileChooser(
                "Choose the destination folder...",
                "Select",
                JFileChooser.DIRECTORIES_ONLY,
                false,
                null,
                null))[0];
        if (directory != null) {
            for (int selectedRow : selectedRows) {
                File old_file = (File) table.getModel().getValueAt(selectedRow, 1);
                moveFile(selectedRow, old_file, directory);
            }
        }
        // else do nothing, user exited or pressed cancel
    }

    /**
     * Function that actually moves the file
     *
     * @param row,          the row on the table
     * @param old_file,     the file to move
     * @param new_location, the destination file to move it to
     */
    public void moveFile(int row, File old_file, File new_location) {
        File new_file = new File(new_location.getPath() + "/" + old_file.getName());
        if (!old_file.renameTo(new_file)) {
            logger.logError("Couldn't move file " + old_file.getPath() + " to " + new_location.getPath() + "!");
            return;
        }

        int index = getIndex(row);
        songs.get(index).setFile(new_file);
        table.getModel().setValueAt(new_file, row, 1);
    }

    /**
     * Plays the files using the default mp3 player
     *
     * @param selectedRows the rows of files to play
     */
    public void playFiles(int[] selectedRows) {
        // traverse the array of rows and play each file sequentially
        for (int selectedRow : selectedRows) {
            int row = table.convertRowIndexToModel(selectedRow);    // get the row
            File file = (File) table.getModel().getValueAt(row, 1);
            try {
                Utils.openFile(file);
            } catch (IOException ex) {
                logger.logError("Couldn't play file " + file.getName(), ex);
            }
        }
    }

    /**
     * Helper Function Checks if a string is the same throughout an array
     *
     * @param str, the string to check
     * @param arr, the array of strings
     * @return the result of the check
     */
    public boolean checkIfSame(String str, String[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (!arr[i].equals(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper Function Checks if a byte array is the same throughout an array
     *
     * @param bytes, the byte array to check
     * @param arr,   the array of byte arrays
     * @return the result of the check
     */
    public boolean checkIfSame(byte[] bytes, byte[][] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (!Arrays.equals(arr[i], bytes)) {
                return false;
            }
        }
        return true;
    }
}
