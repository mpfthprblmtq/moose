/*
   Proj:   Moose
   File:   SongController.java
   Desc:   Controller class for Frame, works directly with the data based on input from Frame UI

   Copyright Pat Ripley 2018-2022
 */

// package
package moose.controllers;

// imports
import com.mpatric.mp3agic.*;

import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import moose.Moose;
import moose.services.AutoTaggingService;
import moose.services.FilenameFormatterService;
import moose.utilities.Constants;
import moose.utilities.MP3FileUtils;
import moose.utilities.SongUtils;
import moose.objects.Song;
import moose.utilities.viewUtils.ViewUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.mpfthprblmtq.commons.utils.FileUtils.launchJFileChooser;

// class SongController
public class SongController {

    // table object that references the JTable on the form
    JTable table;

    // services
    public AutoTaggingService autoTaggingService;
    public FilenameFormatterService filenameFormatterService;

    // logger object
    Logger logger = Moose.getLogger();

    // lists/maps
    HashMap<Integer, Song> songs = new HashMap<>();     // hashmap to contain Song objects
    List<Integer> edited_songs = new ArrayList<>();           // arraylist to contain indices of edited songs to save

    // ivar to check if user has unsaved changes
    boolean hasUnsavedChanges = false;

    /**
     * Default constructor
     */
    public SongController() {
        autoTaggingService = new AutoTaggingService(this);
        filenameFormatterService = new FilenameFormatterService(this);
    }

    /**
     * Returns the autoTaggingService
     */
    public AutoTaggingService getAutoTaggingService() {
        return this.autoTaggingService;
    }

    /**
     * Sets the table ivar
     * @param table, the table to set
     */
    public void setTable(JTable table) {
        this.table = table;
        autoTaggingService.setTable(this.table);
    }

    /**
     * Returns the songs map
     * @return the songs map
     */
    public HashMap<Integer, Song> getSongs() {
        return songs;
    }

    /**
     * Returns the hasUnsavedChanges flag
     */
    public boolean hasUnsavedChanges() {
        return this.hasUnsavedChanges;
    }

    /**
     * Returns a list of all the files in the table currently
     */
    public List<File> getAllFilesInTable() {
        List<File> files = new ArrayList<>();
        for (Song song : getSongs().values()) {
            files.add(song.getFile());
        }
        return files;
    }

    /**
     * Gets a song object from a file
     * @param file, the file to get info from
     * @return a song object
     */
    public Song getSongFromFile(File file) {

        // get the song from the file
        Song s = SongUtils.getSongFromFile(file);
        if (s == null) {
            return null;
        }

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
     * @param index, the index to add to edited_songs
     */
    public void songEdited(int index) {
        if (!edited_songs.contains(index)) {
            edited_songs.add(index);
            int row = getRow(index);
            Moose.frame.setRowIcon(Constants.EDITED, row);
            this.hasUnsavedChanges = true;
        }
        // else do nothing, index is already added
    }

    /**
     * Gets row from an index
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
     * Gets the index at the specified row
     * @param row, the row to search from
     * @return the index of that row
     */
    public int getIndex(int row) {
        row = table.convertRowIndexToModel(row);
        return Integer.parseInt(table.getModel().getValueAt(row, 12).toString());
    }

    /**
     * Gets the index based on the song information given
     * @param song, the song with the data to compare against
     * @return the index of the song
     */
    public int getIndex(Song song) {
        // check for an exact match first
        for (Integer index : getSongs().keySet()) {
            Song songInMap = getSongs().get(index);
            if (song.equals(songInMap, false)) {
                return index;
            }
        }

        // song wasn't found
        return -1;
    }

    public int getIndex(File oldFile, File newFile) {
        for (Integer index : getSongs().keySet()) {
            Song songInMap = getSongs().get(index);
            if (songInMap.getFile().getPath().equals(oldFile.getPath()) || songInMap.getNewFile().getPath().equals(newFile.getPath())) {
                return index;
            }
        }

        // song wasn't found
        return -1;
    }

    /**
     * Helper function to set the new file of the song file in the songs list.
     * @param index, the index of the song
     * @param newFile, the new file to set
     */
    public void setNewFile(int index, File newFile) {
        songs.get(index).setNewFile(newFile);
        songEdited(index);
    }

    /**
     * Helper function to set the title of the song in the songs list.
     * @param index, the index of the song
     * @param title, the title to set
     */
    public void setTitle(int index, String title) {
        songs.get(index).setTitle(title);
        songEdited(index);
    }

    /**
     * Helper function to set the artist of the song in the songs list.
     * @param index, the index of the song
     * @param artist, the artist to set
     */
    public void setArtist(int index, String artist) {
        songs.get(index).setArtist(artist);
        songEdited(index);
    }

    /**
     * Helper function to set the album of the song in the songs list.
     * @param index, the index of the song
     * @param album, the album to set
     */
    public void setAlbum(int index, String album) {
        songs.get(index).setAlbum(album);
        songEdited(index);
    }

    /**
     * Helper function to set the album artist of the song in the songs list.
     * @param index, the index of the song
     * @param albumartist, the albumartist to set
     */
    public void setAlbumArtist(int index, String albumartist) {
        songs.get(index).setAlbumArtist(albumartist);
        songEdited(index);
    }

    /**
     * Helper function to set the genre of the song in the songs list.
     * @param index, the index of the song
     * @param genre, the genre to set
     */
    public void setGenre(int index, String genre) {
        songs.get(index).setGenre(genre);
        songEdited(index);
    }

    /**
     * Helper function to set the year of the song in the songs list.
     * @param index, the index of the song
     * @param year, the year to set
     */
    public void setYear(int index, String year) {
        songs.get(index).setYear(year);
        songEdited(index);
    }

    /**
     * Helper function to set the track of the song in the songs list.
     * @param index, the index of the song
     * @param track, the track to set
     */
    public void setTrack(int index, String track) {
        songs.get(index).setTrack(track);
        songEdited(index);
    }

    /**
     * Helper function to set the total tracks of the song in the songs list.
     * @param index, the index of the song
     * @param totalTracks, the totalTracks to set
     */
    public void setTotalTracks(int index, String totalTracks) {
        songs.get(index).setTotalTracks(totalTracks);
        songEdited(index);
    }

    /**
     * Helper function to set the disk of the song in the songs list.
     * @param index, the index of the song
     * @param disk, the disk to set
     */
    public void setDisk(int index, String disk) {
        songs.get(index).setDisk(disk);
        songEdited(index);
    }

    /**
     * Helper function to set the total disks of the song in the songs list.
     * @param index, the index of the song
     * @param totalDisks, the totalDisks to set
     */
    public void setTotalDisks(int index, String totalDisks) {
        songs.get(index).setTotalDisks(totalDisks);
        songEdited(index);
    }

    /**
     * Helper function to set the album image of the song in the songs list.
     * @param index, the index of the song
     * @param bytes, the byte array of the album image to set
     */
    public void setAlbumImage(int index, byte[] bytes) {
        songs.get(index).setArtwork_bytes(bytes);
        songEdited(index);
    }

    /**
     * Helper function to set the comment of the song in the songs list.
     * @param index, the index of the song
     * @param comment, the comment to set
     */
    public void setComment(int index, String comment) {
        songs.get(index).setComment(comment);
        songEdited(index);
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
            // check to see if the index is even edited before saving
            if (edited_songs.contains(index)) {
                save(index);
                table.getModel().setValueAt(songs.get(index).getFile(), row, 1);
                count++;
            }
        }
        Moose.getFrame().updateConsole(count + " file(s) updated!");
        this.hasUnsavedChanges = false;
    }

    /**
     * Saves an individual track
     * @param index, the index of the song to save in the songs map
     */
    public void save(int index) {

        // get the song
        Song s = songs.get(index);

        // check to see if we need to rename the file
        // TODO why am I doing all this nonsense
        if (s.getNewFile() != null && s.getFile().getName().equals(s.getNewFile().getName())) {
            s.setFile(s.getNewFile());
            s.setNewFile(null);
        } else if (s.getNewFile() != null) {
            if (!s.getNewFile().getName().endsWith(".mp3")) {
                s.setNewFile(new File(s.getNewFile().getAbsolutePath().concat(".mp3")));
            }
            if (!s.getFile().renameTo(s.getNewFile())) {
                logger.logError("Problem saving a file on file name change, file: " + s.getFile().getName());
            } else {
                s.setFile(s.getNewFile());
                s.setNewFile(null);
            }
        }

        // try to get the mp3file object from the file
        Mp3File mp3file = null;
        try {
            mp3file = new Mp3File(s.getFile().getAbsolutePath());
            ID3v2 tag = new ID3v24Tag();
            mp3file.setId3v2Tag(tag);
        } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
            logger.logError("Couldn't save file: " + s.getFile().getName(), ex);
            ViewUtils.showErrorDialog("Couldn't save file: " + s.getFile().getName(), ex, Moose.getFrame());
        }

        // set all the text based items
        try {
            assert mp3file != null;
            // id3v2Tag
            mp3file.getId3v2Tag().setTitle(s.getTitle());
            mp3file.getId3v2Tag().setArtist(s.getArtist());
            mp3file.getId3v2Tag().setAlbum(s.getAlbum());
            mp3file.getId3v2Tag().setAlbumArtist(s.getAlbumArtist());
            mp3file.getId3v2Tag().setYear(s.getYear());
            mp3file.getId3v2Tag().setGenreDescription(s.getGenre());
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

        // save the id3 info
        saveID3Info(mp3file, s.getFile());

        // update the row graphic
        Moose.getFrame().setRowIcon(Constants.SAVED, getRow(index));

        // done saving, remove it
        // gives an IndexOutOfBoundsException when trying to remove() with one element in it
        if (edited_songs.size() == 1) {
            edited_songs.clear();
        } else if (edited_songs.size() > 1) {
            edited_songs.remove(Integer.valueOf(index));
        }
    }

    /**
     * Saves an individual file
     * @param mp3file, the mp3 file to source the information from
     * @param file, the actual file to save as
     */
    public void saveID3Info(Mp3File mp3file, File file) {
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
     * @param find, the string to find
     * @param replace, the string to replace
     * @param includeFiles, a boolean to check if we're including the file names in the search
     * @return the results of the replace, true if there was something to replace, false if not
     */
    public int findAndReplace(String find, String replace, boolean includeFiles) {
        int count = 0;
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 0; j < table.getColumnCount(); j++) {
                if (table.getValueAt(i, j).toString().contains(find)) {
                    String toReplace = table.getValueAt(i, j).toString().replace(find, replace);
                    int index = getIndex(i);
                    switch (j) {
                        case 1:     // filename
                            if (includeFiles) {
                                File oldFile = (File) table.getModel().getValueAt(table.convertRowIndexToModel(i), 1);
                                File newFile = MP3FileUtils.getNewMP3FileFromOld(oldFile, toReplace);
                                setNewFile(table.convertRowIndexToModel(i), newFile);
                                table.setValueAt(toReplace, i, 1);
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
            int index = getIndex(selectedRow);

            // update the songs array
            songs.get(index).setArtwork_bytes(null);

            // send the track to the edited_songs array
            songEdited(index);

            // update graphics
            table.getModel().setValueAt(null, row, 11);
            Moose.frame.multImage.setIcon(null);
        }
    }

    /**
     * Function that looks at the file's name and location and auto generates
     * some tags
     *
     * @param selectedRows, the rows selected on the table
     */
    public void autoTagFiles(int[] selectedRows) {
        // set the table in case of an uncaught update
        autoTaggingService.setTable(table);

        // clean up the file name first
        formatFilenames(selectedRows);

        // actually do the autotagging
        autoTaggingService.autoTag(selectedRows);
    }

    /**
     * Formats the filenames
     */
    public void formatFilenames(int[] selectedRows) {
        for (int row : selectedRows) {
            Song s = songs.get(getIndex(row));
            File file = s.getFile();
            if (s.getNewFile() != null) {
                file = s.getNewFile();
            }

            String path = file.getPath().replace(file.getName(), StringUtils.EMPTY);
            String newFilename = filenameFormatterService.formatFilename(file, MP3FileUtils.folderContainsOnlyOneMP3(file.getParentFile()));
            if (StringUtils.isNotEmpty(newFilename)) {
                File newFile = new File(path + newFilename);
                setNewFile(getIndex(row), newFile);
                table.setValueAt(newFile.getName()
                        .replace(".mp3", StringUtils.EMPTY)
                        .replace(":", "/"), row, 1);
            }
        }
    }

    /**
     * Moves selected files to a new destination
     *
     * @param selectedRows, the rows to move
     */
    public void moveFiles(int[] selectedRows) {

        File[] files = launchJFileChooser(
                "Choose the destination folder...",
                "Select",
                JFileChooser.DIRECTORIES_ONLY,
                false,
                null,
                null);
        if (files != null) {
            for (int selectedRow : selectedRows) {
                File old_file = (File) table.getModel().getValueAt(table.convertRowIndexToModel(selectedRow), 1);
                moveFile(selectedRow, old_file, files[0]);
            }
        }
        // else do nothing, user exited or pressed cancel
    }

    /**
     * Function that actually moves the file
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
     * @param selectedRows the rows of files to play
     */
    public void playFiles(int[] selectedRows) {
        // traverse the array of rows and play each file sequentially
        for (int selectedRow : selectedRows) {
            int row = table.convertRowIndexToModel(selectedRow);    // get the row
            File file = (File) table.getModel().getValueAt(row, 1);
            try {
                FileUtils.openFile(file);
            } catch (Exception e) {
                logger.logError("Couldn't play file: " + file.getName(), e);
            }
        }
    }
}
