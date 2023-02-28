package com.mpfthprblmtq.moose.controllers;

import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Settings;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.services.AutoTaggingService;
import com.mpfthprblmtq.moose.services.AutoTaggingService_V2;
import com.mpfthprblmtq.moose.services.FilenameFormatterService;
import com.mpfthprblmtq.moose.services.FilenameFormatterService_V2;
import com.mpfthprblmtq.moose.services.SongService;
import com.mpfthprblmtq.moose.utilities.Constants;
import com.mpfthprblmtq.moose.utilities.ImageUtils;
import com.mpfthprblmtq.moose.utilities.MP3FileUtils;
import com.mpfthprblmtq.moose.utilities.viewUtils.ViewUtils;
import lombok.Data;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.mpfthprblmtq.commons.utils.FileUtils.launchJFileChooser;

@Data
public class SongController_V2 {

    // services
    public AutoTaggingService autoTaggingService;
    public FilenameFormatterService filenameFormatterService;
    SongService songService;
    FilenameFormatterService_V2 filenameFormatterService_v2;
    AutoTaggingService_V2 autoTaggingService_v2;

    // logger object
    Logger logger = Moose.getLogger();

    // lists/maps
    HashMap<Integer, Song> songs = new HashMap<>(); // hashmap to contain Song objects
    List<Integer> edited_songs = new ArrayList<>(); // arraylist to contain indices of edited songs to save

    // field to check if user has unsaved changes
    boolean hasUnsavedChanges = false;

    /**
     * Default constructor, sets the autotagging and filename formatter services
     */
    public SongController_V2() {
//        autoTaggingService = new AutoTaggingService(this);
//        filenameFormatterService = new FilenameFormatterService(this);
        songService = new SongService();
        filenameFormatterService_v2 = new FilenameFormatterService_V2();
        autoTaggingService_v2 = new AutoTaggingService_V2();
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

        } else if (t instanceof File) {
            for (Integer index : getSongs().keySet()) {
                Song songInMap = getSongs().get(index);
                if (songInMap.getFile().getPath().equals(songInMap.getFile().getPath())) {
                    return index;
                }
            }
        }
        return -1; // index wasn't found
    }

    /**
     * Method for removing album art for songs manually. This method is called when the "Remove" selection is pressed
     * in the context menu
     * @param selectedRows the rows selected on the table
     */
    public void removeAlbumArt(int[] selectedRows) {
        for (int selectedRow : selectedRows) {
            // get the row and index of the track
            int row = Moose.getFrame().getTable().convertRowIndexToModel(selectedRow);
            int index = getIndex(selectedRow);

            // set that index's artwork to null
            setAlbumImage(index, null);

            // update graphics
            Moose.getFrame().getTable().getModel().setValueAt(null, row, 11);
            Moose.getFrame().multImage.setIcon(null);
        }
    }

    /**
     * Function that looks at the file's name and location and auto generates
     * some tags
     *
     * @param selectedRows, the rows selected on the table
     */
    public void autoTagFiles(int[] selectedRows) {

        // create a list of songs with the rows, so we can use them for processing
        List<Song> songs = Arrays.stream(selectedRows)
                .boxed()
                .map((row) -> getSongs().get(getIndex(row)))
                .collect(Collectors.toList());

        // format the filenames first
        filenameFormatterService_v2.formatFilenames(songs);

        // actually do the autotagging
        autoTaggingService_v2.autoTag(songs);
    }

    /**
     * Saves each file sequentially
     * @param selectedRows the rows to save
     */
    public void saveTracks(int[] selectedRows) {
        // count to show how many files were saved
        int count = 0;

        // traverse the array of rows and save each file sequentially
        for (int selectedRow : selectedRows) {
            int row = Moose.getFrame().getTable().convertRowIndexToModel(selectedRow);    // get the row
            int index = getIndex(row);
            // check to see if the index is even edited before saving
            if (edited_songs.contains(index)) {
                // do the save
                if (songService.save(getSongs().get(index))) {
                    // set the value of the File on the table's row to the new file
                    Moose.getFrame().getTable().getModel().setValueAt(songs.get(index).getFile(), row, 1);

                    // update the row graphic
                    Moose.getFrame().setRowIcon(Constants.SAVED, getRow(index));

                    // done saving, remove it
                    // gives an IndexOutOfBoundsException when trying to remove() with one element in it
                    if (edited_songs.size() == 1) {
                        edited_songs.clear();
                    } else if (edited_songs.size() > 1) {
                        edited_songs.remove(index);
                    }

                    // increment the number of successful saves
                    count++;
                }
            }
        }

        Moose.getFrame().updateConsole(count + " file(s) updated!");
        this.hasUnsavedChanges = !edited_songs.isEmpty();
    }

    /**
     * Method for adding album art for songs manually, using a JFileChooser.  This method is called when the "Add"
     * selection is pressed in the context menu.
     * @param selectedRows the current selected rows on the table
     */
    public void addAlbumArtFromFileChooser(int[] selectedRows) {
        File cover = null;
        boolean dialogShown = false;

        for (int row : selectedRows) {
            // get the song
            Song song = getSongs().get(getIndex(row));

            // only want to show the dialog once, so check to see if we've seen the dialog already
            if (!dialogShown) {
                // show the image file choose dialog with the song's file as the starting point
                cover = ImageUtils.selectAlbumArt(song.getFile());
                if (cover == null) {
                    return;
                } else {
                    dialogShown = true;
                }
            }

            // we should have the cover at this point
            autoTaggingService_v2.addCoverForFile(song, cover);

        }
    }

    /**
     * Plays the files using the default mp3 player
     * @param selectedRows the rows of files to play
     */
    public void playFiles(int[] selectedRows) {
        // traverse the array of rows and play each file sequentially
        for (int selectedRow : selectedRows) {
            int row = Moose.getFrame().getTable().convertRowIndexToModel(selectedRow);    // get the row
            File file = (File) Moose.getFrame().getTable().getModel().getValueAt(row, 1);
            try {
                FileUtils.openFile(file);
            } catch (Exception e) {
                logger.logError("Couldn't play file: " + file.getName(), e);
                ViewUtils.showErrorDialog("Couldn't play file: " + file.getName(), e, Moose.getFrame());
            }
        }
    }

    /**
     * Moves selected files to a new destination
     * @param selectedRows the rows of files to move
     */
    public void moveFiles(int[] selectedRows) {
        // show the JFileChooser for the user to select destination folder
        File[] files = launchJFileChooser(
                "Choose the destination folder...",
                "Select",
                JFileChooser.DIRECTORIES_ONLY,
                false,
                null,
                null);

        // if files is null, user exited or cancelled
        if (files != null) {
            // go through the selected rows and move the files
            for (int selectedRow : selectedRows) {
                // get the new location, which is the first element of the files array
                File newLocation = files[0];

                // get the old file from the table
                File oldFile = (File) Moose.getFrame().getTable().getModel().getValueAt(
                        Moose.getFrame().getTable().convertRowIndexToModel(selectedRow), 1);

                // create the new file
                File newFile = new File(newLocation.getPath() + "/" + oldFile.getName());

                // actually move the file
                if (!oldFile.renameTo(newFile)) {
                    logger.logError("Couldn't move file " + oldFile.getPath() + " to " + newLocation.getPath() + "!");
                    ViewUtils.showErrorDialog("Couldn't move file " + oldFile.getPath() + " to " + newLocation.getPath() + "!", Moose.getFrame());
                } else {
                    // update the song in the songs map
                    setNewFile(getIndex(selectedRow), newFile);
                    // update graphics
                    Moose.getFrame().getTable().getModel().setValueAt(newFile, selectedRow, 1);
                }
            }
        }
    }

    /**
     * Method for checking to see if any of the new additions to the table have new genres to add.  Checks to see
     * if that feature is enabled in settings beforehand
     * @param list the list of either Files or Songs to check
     */
    public void checkForNewGenres(List<?> list) {
        if (Moose.getSettings().getFeatures().get(Settings.CHECK_FOR_NEW_GENRES)) {
            songService.checkForNewGenres(list);
        }
    }

    /**
     * Does the finding and replacing from showFindAndReplaceDialog()
     * @param find the string to find
     * @param replace the string to replace
     * @param includeFiles a boolean to check if we're including the file names in the search
     * @return a count of successful replacements
     */
    public int findAndReplace(String find, String replace, boolean includeFiles) {
        int count = 0;
        JTable table = Moose.getFrame().getTable();

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
                                count++;
                            }
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
}
