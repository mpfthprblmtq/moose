/**
 *  Proj:   Moose
 *  File:   SongController.java
 *  Desc:   Controller class for Frame, works directly with the data based on input from Frame UI
 *
 *  Copyright Pat Ripley 2018
 */

// package
package moose.controllers;

import com.mpatric.mp3agic.*;

import moose.utilities.Logger;
import moose.Main;
import moose.objects.Song;
import moose.utilities.Utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SongController {

    // table object that references the JTable on the form
    JTable table;

    // logger object
    Logger logger = new Logger();

    // ArrayLists
    HashMap<Integer, Song> songs = new HashMap<>();     // hashmap to contain Song objects
    ArrayList edited_songs = new ArrayList();           // arraylist to contain indices of edited songs to save

    public SongController() {
    }

    public void setTable(JTable table) {
        this.table = table;
    }
    
    /**
     * Returns the songs list
     * @return the songs map
     */
    public HashMap<Integer, Song> getSongs() {
        return songs;
    }

    /**
     * Gets a song object from a file
     * @param file, the file to get info from
     * @return a song object
     */
    public Song getSongFromFile(File file) {
        // mp3agic Mp3File object, used for the id3tags
        Mp3File mp3file;
        try {
            // create the mp3file from the file's path
            mp3file = new Mp3File(file.getAbsolutePath());

            // if the mp3file doesn't have an id3tag, create one
            if (!mp3file.hasId3v2Tag()) {
                ID3v2 tag = new ID3v24Tag();
                mp3file.setId3v2Tag(tag);
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
            // things borked
            mp3file = null;
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
        if (track == null) {
            track = "";
        }
        if (disk == null) {
            disk = "";
        }
        if (artwork_bytes == null) {
            artwork_bytes = new byte[0];
        }

        // create a song object with the information
        Song s = new Song(file, title, artist, album, albumartist, genre, year, track, disk, artwork_bytes);

        // make an index
        int index = songs.size();

        // add the song to the list
        songs.put(index, s);

        return s;
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
            Main.frame.setRowIcon(Main.frame.EDITED, row);
        } else {
            // do nothing, index is already added
        }
    }

    /**
     * Helper Function Gets row from an index
     *
     * @param index, the index of the song
     * @return the row where the index matches
     */
    public int getRow(int index) {
        String[] indices = getIndices();
        for (String i : indices) {
            String[] arr = i.split("_");
            if (Integer.valueOf(arr[1]) == index) {
                return Integer.valueOf(arr[0]);
            }
        }
        return -1;
    }

    public String[] getIndices() {
        String[] indices = new String[table.getRowCount()];
        for (int i = 0; i < indices.length; i++) {
            int row = table.convertRowIndexToModel(i);
            indices[i] = i + "_" + getIndex(row);
        }
        return indices;
    }

    /**
     * Helper Function Gets the index at the specified row
     *
     * @param row
     * @return
     */
    public int getIndex(int row) {
        return Integer.valueOf(table.getModel().getValueAt(row, 12).toString());
    }

    /**
     * Helper function to set the file of the song file in the songs arraylist.
     *
     * @param index, the index of the song
     * @param old_file, the file to replace
     * @param new_file, the new file to set
     */
    public void setFile(int index, File old_file, File new_file) {
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
     * @param index, the index of the song
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
     * @param index, the index of the song
     * @param albumartist, the albumartist to set
     */
    public void setAlbumArtist(int index, String albumartist) {
        songs.get(index).setAlbumartist(albumartist);
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
     * @param year, the year to set
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
     * @param disk, the disk to set
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
     * Goes through the edited_songs array and saves each one
     */
    public void saveAll() {

        // traverse the array of songs
        for (int i = 0; i < songs.size(); i++) {

            // if the song at i is in edited_songs, update it
            if (edited_songs.contains(i)) {
                Song s = songs.get(i);
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
                    mp3file.getId3v2Tag().setTitle(s.getTitle());
                    mp3file.getId3v2Tag().setArtist(s.getArtist());
                    mp3file.getId3v2Tag().setAlbum(s.getAlbum());
                    mp3file.getId3v2Tag().setAlbumArtist(s.getAlbumartist());
                    mp3file.getId3v2Tag().setGenreDescription(s.getGenre());
                    mp3file.getId3v2Tag().setYear(s.getYear());
                    mp3file.getId3v2Tag().setTrack(s.getFullTrackString());
                    mp3file.getId3v2Tag().setPartOfSet(s.getFullDiskString());
                } catch (IllegalArgumentException ex) {
                    // this exception doesn't really matter
                    // this only happens if you save a track with no genre
                }

                // set album art
                String type = "image/jpeg";
                mp3file.getId3v2Tag().clearAlbumImage();
                mp3file.getId3v2Tag().setAlbumImage(s.getArtwork_bytes(), type);

                save(mp3file, file);

                // update the row graphic
                Main.frame.setRowIcon(Main.frame.SAVED, getRow(i));
            } else {
                // skip it, no changes
            }
        }

        // done with the saving, clear the edited_songs list
        edited_songs.clear();
    }

    public void saveTracks(int[] selectedRows) {
        // traverse the array of rows and play each file sequentially
        for (int i = 0; i < selectedRows.length; i++) {
            int row = table.convertRowIndexToModel(selectedRows[i]);    // get the row
            int index = getIndex(row);
            save(index);
        }
    }

    public void save(int index) {
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
                mp3file.getId3v2Tag().setTitle(s.getTitle());
                mp3file.getId3v2Tag().setArtist(s.getArtist());
                mp3file.getId3v2Tag().setAlbum(s.getAlbum());
                mp3file.getId3v2Tag().setAlbumArtist(s.getAlbumartist());
                mp3file.getId3v2Tag().setGenreDescription(s.getGenre());
                mp3file.getId3v2Tag().setYear(s.getYear());
                mp3file.getId3v2Tag().setTrack(s.getFullTrackString());
                mp3file.getId3v2Tag().setPartOfSet(s.getFullDiskString());
            } catch (IllegalArgumentException ex) {
                // this exception doesn't really matter
                // this only happens if you save a track with no genre
            }

            // set album art
            String type = "image/jpeg";
            mp3file.getId3v2Tag().clearAlbumImage();
            mp3file.getId3v2Tag().setAlbumImage(s.getArtwork_bytes(), type);

            save(mp3file, file);

            // update the row graphic
            Main.frame.setRowIcon(Main.frame.SAVED, getRow(index));

            // done saving, remove it
            // gives an IndexOutOfBoundsException when trying to remove() with one element in it
            if (edited_songs.size() == 1) {
                edited_songs.clear();
            } else if (edited_songs.size() > 1) {
                edited_songs.remove(index);
            }
        } else {
            // song doesn't need to be saved
        }
    }

    /**
     * Saves an individual file
     *
     * @param mp3file
     * @param file
     */
    public void save(Mp3File mp3file, File file) {
        try {
            // save the new mp3file
            mp3file.save(file.getAbsolutePath().replace(".mp3", "_.mp3"));

            // delete the old file
            file.delete();

            // rename the new file to match the old
            File newFile = new File(file.getAbsolutePath().replace(".mp3", "_.mp3"));
            newFile.renameTo(file);

        } catch (IOException | NotSupportedException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Method for adding album art for songs manually This method is called when
     * the "Add" selection is pressed in the context menu
     *
     * @param selectedRows
     */
    public void addAlbumArt(int[] selectedRows) {

        // need this for some reason
        File img_file = null;

        for (int i = 0; i < selectedRows.length; i++) {

            try {
                // get the row and index of the track
                int row = table.convertRowIndexToModel(selectedRows[i]);
                int index = Integer.valueOf(table.getModel().getValueAt(row, 12).toString());

                // get the file to use as the starting point for choosing an image
                File file = songs.get(index).getFile();

                // only show the JFileChooser on the first go
                if (i == 0) {
                    JFileChooser fc = new JFileChooser(new File(file.getAbsolutePath()));
                    fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "tif"));

                    // result of the file choosing
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        img_file = fc.getSelectedFile();
                    } else {
                        return;
                    }
                }

                // convert that file to a byte array
                byte[] bytes;
                try (RandomAccessFile ra_file = new RandomAccessFile(img_file.getAbsolutePath(), "r")) {
                    bytes = new byte[(int) ra_file.length()];
                    ra_file.read(bytes);
                }

                // set the artwork in the songs array
                songs.get(index).setArtwork_bytes(bytes);

                // update graphics
                Icon thumbnail_icon = Utils.getScaledImage(bytes, 100);
                table.getModel().setValueAt(thumbnail_icon, row, 11);

                // send the track to the edited_songs array
                songEdited(index);

                // if there's multiple images, update the multPanel
                if (table.getSelectedRowCount() > 1) {
                    Icon artwork_icon = Utils.getScaledImage(bytes, 150);
                    Main.frame.multImage.setIcon(artwork_icon);
                }

            } catch (IOException ex) {
                //Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Method for removing album art for songs manually This method is called
     * when the "Remove" selection is pressed in the context menu
     *
     * @param selectedRows
     */
    public void removeAlbumArt(int[] selectedRows) {

        for (int i = 0; i < selectedRows.length; i++) {

            // get the row and index of the track
            int row = table.convertRowIndexToModel(selectedRows[i]);
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
     * Adds the album art for each song if there exists a cover.* file in the
     * same dir
     *
     * @param selectedRows the rows of songs to update
     */
    public void autoAddCovers(int[] selectedRows) {
        // traverse the array of rows and add each image
        for (int i = 0; i < selectedRows.length; i++) {
            int row = table.convertRowIndexToModel(selectedRows[i]);    // get the row

            // get the parent directory of the song
            File dir = songs.get(getIndex(row)).getFile().getParentFile();

            // check if the parent directory has a cover.* file
            File cover = folderContainsCover(dir);
            if (cover != null) {
                // if cover doesn't return as null, add the cover
                addIndividualCover(row, cover);
            }
        }
    }

    /**
     * Helper function to check and see if a directory has a cover image file
     *
     * @param folder, the folder to check
     * @return the cover file, or null if it doesn't exist
     */
    public File folderContainsCover(File folder) {
        File[] files = folder.listFiles();      // get all the files
        for (File file : files) {
            if (file.getName().equals("cover.png") || file.getName().equals("cover.jpg") || file.getName().equals("cover.jpeg")) {
                return file;
            }
        }
        // no cover files were found, returning null
        return null;
    }

    /**
     * Function to add an album cover for just one row
     *
     * @param row
     * @param cover
     */
    public void addIndividualCover(int row, File cover) {

        try {
            // get the index of the track
            int index = Integer.valueOf(table.getModel().getValueAt(row, 12).toString());

            // convert file to byte array
            byte[] bytes;
            try (RandomAccessFile ra_file = new RandomAccessFile(cover.getAbsolutePath(), "r")) {
                bytes = new byte[(int) ra_file.length()];
                ra_file.read(bytes);
            }

            // update the track in the songs array
            songs.get(index).setArtwork_bytes(bytes);

            // update graphics
            Icon thumbnail_icon = Utils.getScaledImage(bytes, 100);


            // set the image on the row
            table.getModel().setValueAt(thumbnail_icon, row, 11);

            // song was edited, add it to the list
            songEdited(index);

            // if there's multiple rows selected, also add it to the multiple fields panel
            if (table.getSelectedRowCount() > 1) {
                Icon artwork_icon = Utils.getScaledImage(bytes, 150);
                Main.frame.multImage.setIcon(artwork_icon);
            }

        } catch (IOException ex) {
            logger.logError("Exception adding individual cover!", ex);
        }
    }

    /**
     * Function that looks at the file's name and location and auto generates some tags
     * @param selectedRows, the rows selected on the table
     */
    public void autoTagFiles(int[] selectedRows) {
        for (int i = 0; i < selectedRows.length; i++) {
            autoTag(selectedRows[i], (File) table.getValueAt(i, 1));
        }
    }

    /**
     * Function that actually does the autotagging
     * @param row, the row to update
     * @param file, the file to look at
     */
    public void autoTag(int row, File file) {

        String title = getTitleFromFile(file);
        String album = getAlbumFromFile(file);
        String year = getYearFromFile(file);
        String tracks = getTracksFromFolder(file.getParentFile());
    }

    public String getTitleFromFile(File file) {
        String regex = "\\d{2} .*\\.mp3";
        if(file.getName().matches(regex)) {
            return file.getName().substring(3).trim();
        } else {
            return "";
        }
    }

    public String getAlbumFromFile(File file) {
        File dir = file.getParentFile();
        String regex = "\\[\\d{4}\\] .*";
        if(dir.getName().matches(regex)) {
            return dir.getName().substring(6).trim();
        } else if (dir.getName().startsWith("CD")) {
            // album is a multiple CD album
            dir = dir.getParentFile();
            if (dir.getName().equals(regex)) {
                return dir.getName().substring(6).trim();
            }
        }
        return "";
    }

    public String getYearFromFile(File file) {
        File dir = file.getParentFile();
        String regex = "\\[\\d{4}\\] .*";
        if(dir.getName().matches(regex)) {
            return dir.getName().substring(1,5).trim();
        } else if (dir.getName().startsWith("CD")) {
            // album is a multiple CD album
            dir = dir.getParentFile();
            if (dir.getName().equals(regex)) {
                return dir.getName().substring(1, 5).trim();
            }
        }
        return "";
    }

    public String getTracksFromFolder(File file) {
        String regex = "\\d{2} .*\\.mp3";
        if(file.getName().matches(regex)) {
            return file.getName().substring(0,1) + "/" + getTotalTracksFromFolder(file);
        } else {
            return "";
        }
    }

    public String getTotalTracksFromFolder(File file) {
        String regex = "\\[\\d{4}\\] .*";
        int totalTracks = 0;
        if(file.getName().matches(regex)) {
            return String.valueOf(getNumberOfSongs(file));
        } else if (file.getName().startsWith("CD")) {
            // album is a multiple CD album
            // TODO handle this logic
            return "";
        } else {
            return "";
        }
    }

    public int getNumberOfSongs(File file) {
        File[] files = file.listFiles();
        int count = 0;
        for (int i = 0; i< files.length; i++) {
            if(files[i].getName().endsWith(".mp3")) {
                count++;
            }
        }
        return count;
    }

    // TODO Make sure this works and document it
    public void moveFiles(int[] selectedRows) {

        JFileChooser jfc = new JFileChooser();
        File library = new File("/Users/pat/Music/Library");
        jfc.setCurrentDirectory(library);
        jfc.setDialogTitle("Choose the destination folder...");
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = jfc.showDialog(Main.frame, "Select");

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            for (int i = 0; i < selectedRows.length; i++) {
                File file = (File) table.getModel().getValueAt(selectedRows[i], 1);

                File directory = jfc.getSelectedFile();
                File new_file = new File(directory.getPath() + "/" + file.getName());
                file.renameTo(new_file);

                int index = getIndex(selectedRows[i]);
                songs.get(index).setFile(new_file);
                table.getModel().setValueAt(new_file, selectedRows[i], 1);
            }
        } else {
            // do nothing, user exited or pressed cancel
        }
    }

    /**
     * Plays the files using the default mp3 player
     *
     * @param selectedRows the rows of files to play
     */
    public void playFiles(int[] selectedRows) {
        // traverse the array of rows and play each file sequentially
        for (int i = 0; i < selectedRows.length; i++) {
            try {
                int row = table.convertRowIndexToModel(selectedRows[i]);    // get the row
                File file = (File) table.getModel().getValueAt(row, 1);
                Desktop desktop = Desktop.getDesktop();
                if (file.exists()) {
                    desktop.open(file);
                }
            } catch (IOException ex) {
                System.err.println(ex);
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
     * @param arr, the array of byte arrays
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
