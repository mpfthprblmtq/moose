/*
 * Proj:   Moose
 * File:   AuditService.java
 * Desc:   Service class for AuditFrame and AuditController, works directly with AuditController and has the bulk of the
 *         logic for auditing functionality.
 *
 * Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

// package
package com.mpfthprblmtq.moose.services;

//imports
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.utilities.AuditCleanupUtils;
import com.mpfthprblmtq.moose.utilities.Constants;
import com.mpfthprblmtq.moose.utilities.MP3FileUtils;
import com.mpfthprblmtq.moose.utilities.SongUtils;
import com.mpfthprblmtq.moose.views.modals.AuditFrame;

import javax.swing.SwingConstants;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mpfthprblmtq.moose.utilities.Constants.*;

// class AuditController
public class AuditService {

    // some fields
    AuditFrame auditFrame;

    // logger object
    Logger logger = Moose.getLogger();

    /**
     * Creates new service AuditService
     */
    public AuditService() {
        this.auditFrame = Moose.getAuditFrame();
    }

    /**
     * Creates a master list of files from the audit folder, then imports all the albums into an albums list
     * @param folder, the folder to import from
     */
    public List<File> importAlbums(File folder) {

        // create the main albums list
        List<File> albums = new ArrayList<>();

        // list to store all the files
        List<File> allFiles = new ArrayList<>();
        FileUtils.listFiles(folder, allFiles);

        // traverse the list of files and add the albums to the albums list
        for (File file : allFiles) {
            if (file.getName().endsWith(".mp3")) {
                File a;
                // folder is a multi-disk album
                if (file.getPath().matches(CD_FILEPATH_REGEX)) {
                    a = file.getParentFile().getParentFile();
                } else {
                    a = file.getParentFile();
                }
                if (!albums.contains(a)) {
                    albums.add(a);
                }
            }
        }

        // sort them alphabetically
        albums.sort((File o1, File o2) -> {
            String filename1 = o1.getPath().replace(folder.getPath(), StringUtils.EMPTY);
            String filename2 = o2.getPath().replace(folder.getPath(), StringUtils.EMPTY);
            return filename1.compareToIgnoreCase(filename2);
        });

        return albums;
    }

    /**
     * Helper method to get a "pretty" version of the count of each type of issue in the audit file path list to show
     * in the text area on the AuditFrame
     * @param auditFilePathList the list of lists to read
     * @return a formatted string
     */
    public String getResultsFromFilePathList(List<List<String>> auditFilePathList) {
        return "ID3Tags missing:    " + auditFilePathList.get(ID3).size() + "\n"
                + "File path issues:   " + auditFilePathList.get(FILENAMES).size() + "\n"
                + "Cover art missing:  " + auditFilePathList.get(COVER).size();
    }

    /**
     * Helper method to get a "pretty" version of the file paths with issues in the file path list to show in the
     * View Results dialog
     * @param auditFilePathList the list of lists for all the audit files
     * @return a string representation of the results
     */
    public String exportAuditResultsToString(List<List<String>> auditFilePathList) {

        // lets make a string
        String str = "\n";

        str = str.concat(" Some ID3Tags missing:\n");
        for (String path : auditFilePathList.get(Constants.ID3)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        str = str.concat(" Some file paths don't match standard:\n");
        for (String path : auditFilePathList.get(Constants.FILENAMES)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        str = str.concat(" Cover art not found in folder:\n");
        for (String path : auditFilePathList.get(Constants.COVER)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        return str;
    }

    /**
     * Checks to see if in the audit folder there exists a done file in any of its sub folders.
     * Called from the startAudit() method in controller.
     * @param albums the list of directories (albums) to check
     * @return the result of the check, true for existing audit, false for no existing audit
     */
    public boolean checkForExistingAudit(List<File> albums) {
        return albums.stream().anyMatch(this::containsFile);
    }

    /**
     * Gets all the albums from the filePathList
     * @param filePathList the list of all paths with issues
     */
    public List<File> getAllMarkedAlbums(List<List<String>> filePathList) {
        List<File> albums = new ArrayList<>();
        for (List<String> list : filePathList) {
            for (String path : list) {
                albums.add(new File(path));
            }
        }
        return albums;
    }

    /**
     * Opens a new audit window and a new frame with a pre-loaded album
     */
    public void openAuditWindow(List<File> albums, int index) {
        // update graphics
        updateLabelAndProgressBarForAudit(index, albums.size());

        // open up a new AuditFrame and a Frame with the album preloaded
        Moose.frame.dispose();
        Moose.launchFrame(albums.get(index));
        auditFrame = Moose.getAuditFrame();
        auditFrame.refreshAuditFrameFromAudit(getCheckResults(albums.get(index)), albums.get(index).getPath());
    }

    /**
     * Returns the check results on each aspect of the album (ID3 tags, filenames, and album cover)
     * @param album the directory to check
     * @return the result of the checks
     */
    public List<Boolean> getCheckResults(File album) {
        List<Boolean> results = new ArrayList<>();
        results.add(id3TagsHaveErrors(album));
        results.add(filesHaveErrors(album));
        results.add(coverHasErrors(album));
        return results;
    }

    /**
     * Helper function to update some graphics for auditing
     * @param index the index of the current album
     * @param total the total number of albums we're scanning
     */
    private void updateLabelAndProgressBarForAudit(int index, int total) {
        // update the currentlyScanningLabel to show where we are numerically
        auditFrame.setAuditCurrentlyScanningLabelHorizontalAlignment(SwingConstants.LEADING);
        auditFrame.updateAuditCurrentlyScanningLabel("Album " + (index + 1) + " of " + total);

        // update the progress bar
        auditFrame.updateAuditProgressBar(AuditCleanupUtils.formatPercentage(index, total));
    }

    /**
     * Gets the next album index in the albums arraylist
     * @param albums the file list of albums
     * @return the int index of that album
     */
    public int getNextAlbum(List<File> albums) {
        for (File album : albums) {
            if (isNotDone(album)) {
                return albums.indexOf(album);
            }
        }
        return -1;
    }

    /**
     * Function used to create a .done file in the specified folder
     * @param dir the directory to create the file in
     */
    public void setDone(File dir) {
        String path = dir.getPath() + "/.done";
        File done = new File(path);
        try {
            if (!done.createNewFile()) {
                throw new IOException("Couldn't create .done file in " + dir.getPath());
            }
        } catch (IOException ex) {
            logger.logError("Error creating .done file for auditing in folder: " + path, ex);
        }
    }

    /**
     * Checks if that specified directory/album is done
     * @param dir the directory to check
     * @return the result of the check, true for done, false for not done
     */
    public boolean isNotDone(File dir) {
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.getName().equals(".done")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Function used to clear all the .done files from the albums list
     * @param albums the file list of all albums
     */
    public void clearDoneFiles(List<File> albums) {
        albums.stream()
                .filter(this::containsFile)
                .map(File::listFiles)
                .filter(Objects::nonNull).forEachOrdered((files) -> {
                    for (File file : files) {
                        if (file.getName().equals(".done")) {
                            if (!file.delete()) {
                                logger.logError("Could not delete .done file in " + file.getParentFile().getPath());
                            }
                        }
                    }
                });
    }

    /**
     * Helper function used to see if there's a .done file in the specified folder
     * @param dir the directory to check
     * @return the result of the search, true for file found, false for file not found
     */
    public boolean containsFile(File dir) {
        File[] filesInAlbum = dir.listFiles();
        assert filesInAlbum != null;
        for (File file : filesInAlbum) {
            if (file.getName().equals(".done")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks all the mp3s in a folder for all the necessary id3 tags
     * @param dir the folder/album to check
     * @return the result of the check, true if all mp3s are good, false if one or more doesn't have all information needed
     */
    public boolean id3TagsHaveErrors(File dir) {

        // create a list of all files from that directory
        List<File> files = new ArrayList<>();
        FileUtils.listFiles(dir, files);

        // traverse the file list
        for (File file : files) {

            // check if it's a mp3
            if (file.getName().endsWith(".mp3")) {

                Song song = SongUtils.getSongFromFile(file);
                if (song == null) {
                    return true;
                }

                if (StringUtils.isEmpty(song.getTitle())
                        || StringUtils.isEmpty(song.getArtist())
                        || StringUtils.isEmpty(song.getAlbum())
                        || StringUtils.isEmpty(song.getAlbumArtist())
                        || StringUtils.isEmpty(song.getGenre())
                        || StringUtils.isEmpty(song.getYear())
                        || StringUtils.isEmpty(song.getTrack())
                        || StringUtils.isEmpty(song.getDisk())
                        || song.getArtwork_bytes() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Attempts to auto fix the id3 errors by just autotagging like normal
     */
    public void autoFixID3() {
        Moose.getSongController().autoTagFiles(IntStream.range(0, Moose.getFrame().table.getRowCount()).toArray());
        Moose.getSongController().saveTracks(IntStream.range(0, Moose.getFrame().table.getRowCount()).toArray());
    }

    /**
     * Checks the album/folder for a cover.* file (image file)
     * @param dir the folder/album to check
     * @return the result of the check, true for a cover file exists, false for a cover file doesn't exist
     */
    public boolean coverHasErrors(File dir) {

        if (dir.getPath().matches(CD_FILEPATH_REGEX)) {
            dir = dir.getParentFile();
        }

        // check the current directory ivar
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.getName().startsWith("cover.")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Attempts to auto fix the covers by automatically finding/adding the cover
     */
    public void autoFixCovers() {
        Moose.getSongController().getAutoTaggingService()
                .autoAddCoverArt(IntStream.range(0, Moose.getFrame().table.getRowCount()).toArray());
    }

    /**
     * Checks all the files in a folder for the standardized file name
     * @param dir the folder/album to check
     * @return the result of the check, true if all files are good, false if one or more doesn't match
     */
    public boolean filesHaveErrors(File dir) {

        // create a list of all files from that directory
        List<File> files = new ArrayList<>();
        FileUtils.listFiles(dir, files);

        // check all mp3 files in that list
        // also check to see if we're in a label so that we can handle the file path differently
        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                if (MP3FileUtils.isPartOfALabel(file)) {
                    if (MP3FileUtils.isPartOfALabel(file, SINGLES)) {
                        if (!file.getPath().matches(SINGLES_FILEPATH_REGEX) || file.getName().matches(TRACKNUM_ARTIST_TITLE_REGEX)) {
                            return true;
                        }
                    } else if (MP3FileUtils.isPartOfALabel(file, COMPILATIONS)) {
                        if (!file.getPath().matches(COMPILATIONS_FILEPATH_REGEX)) {
                            return true;
                        }
                    } else if (MP3FileUtils.isPartOfALabel(file, LPS)) {
                        if (!file.getPath().matches(LPS_FILEPATH_REGEX)) {
                            return true;
                        }
                    } else if (MP3FileUtils.isPartOfALabel(file, EPS)) {
                        if (!file.getPath().matches(EPS_FILEPATH_REGEX)) {
                            return true;
                        }
                    }
                } else {
                    if (!file.getPath().matches(GENERAL_FILEPATH_REGEX)) {
                        return true;
                    }
                }
            }
        }
        // if we're here, the file is good
        return false;
    }

    /**
     * This used to be one hideous method with lots of repeated code, but I broke it into a million pieces and split
     * all the functionality into helper functions, this method is really the controller for all of it.
     * This method goes through all the files in the directory given and makes sure all the files and folders are
     * named correctly according to where they are.  Also accounts for labels and multi-CD albums.
     * @param dir, the dir to do dir stuff
     * @return a new File if the directory/file changed
     */
    public File autoFixFilePaths(File dir) {

        // go through all the files in the directory and use the mp3 files
        List<File> mp3Files = new ArrayList<>();
        FileUtils.listFiles(dir, mp3Files);
        mp3Files.removeIf(file -> !file.getName().endsWith(".mp3"));

        // file object to return, it's really just an updated version of the file dir passed in
        File updatedDir = dir;

        // lets do some iteration
        for (File file : mp3Files) {

            // check if we're in a label
            if (MP3FileUtils.isPartOfALabel(updatedDir)) {
                // check what type of label directory we're in
                if (MP3FileUtils.isPartOfALabel(updatedDir, SINGLES)) {
                    updatedDir = renameAlbum(file, updatedDir, true);
                    renameTrack(file, updatedDir, false);
                } else if (MP3FileUtils.isPartOfALabel(dir, COMPILATIONS)) {
                    updatedDir = renameAlbum(file, updatedDir, false);
                    renameTrack(file, updatedDir, true);
                } else if (MP3FileUtils.isPartOfALabel(dir, LPS) || MP3FileUtils.isPartOfALabel(dir, EPS)) {
                    updatedDir = renameAlbum(file, updatedDir, true);
                    renameTrack(file, updatedDir, false);
                }
            } else {
                updatedDir = renameAlbum(file, updatedDir, false);
                renameTrack(file, updatedDir, false);
            }
        }
        return updatedDir;
    }

    /**
     * Updates the file while iterating in the parent method in the case of a parent directory changing
     * @param file the file that we're updating
     * @param updatedDir the parent directory that has the newest information
     * @return the updated file
     */
    private File updateFile(File file, File updatedDir) {
        String path;
        // check to see if we're in a multiple CD album, we need to build the path differently then
        if (file.getPath().matches(CD_FILEPATH_REGEX)) {
            Pattern pattern = Pattern.compile(CD_FILEPATH_REGEX);
            Matcher matcher = pattern.matcher(file.getPath());
            if (matcher.find()) {
                path = updatedDir.getPath() + "/" + matcher.group("CDNumber");
            } else {
                path = updatedDir.getPath();
            }
        } else {
            path = updatedDir.getPath();
        }
        String newFileName = file.getName();
        return new File(path + "/" + newFileName);
    }

    /**
     * Validates the directory name based on what we send in
     * @param file the file with the song information
     * @param updatedDir the updated directory from the calling function, where the file lives
     * @param includeArtist a boolean to see if we need to include the artist in the directory name
     * @return updatedDir, just so we can keep track of it in the parent method
     */
    private File renameAlbum(File file, File updatedDir, boolean includeArtist) {
        // grab the song data
        Song song = SongUtils.getSongFromFile(file);

        if (includeArtist) {
            // library/label/lps/[year] artist - album/01 title.mp3
            // check if parent folder matches [year] artist - album standard
            if (!file.getParentFile().getName().matches(YEAR_ARTIST_ALBUM_REGEX)
                    && song != null
                    && StringUtils.isNotEmpty(song.getYear())
                    && StringUtils.isNotEmpty(song.getArtist())
                    && StringUtils.isNotEmpty(song.getAlbum())) {
                // get the common artist in the case of an EP/LP with collaborating artists on the first file we come across
                String commonArtist = getCommonArtist(updatedDir);
                if (StringUtils.isEmpty(commonArtist)) {
                    commonArtist = song.getArtist();
                }
                updatedDir = renameDirectory(updatedDir, "[" + song.getYear() + "] " + commonArtist + " - " + song.getAlbum());
            }
        } else {
            // library/album artist/[year] album/01 title.mp3
            // check to see if parent folder matches [year] album standard
            if (!file.getParentFile().getName().matches(YEAR_ALBUM_REGEX)
                    && song != null
                    && StringUtils.isNotEmpty(song.getYear())
                    && StringUtils.isNotEmpty(song.getAlbum())) {
                updatedDir = renameDirectory(updatedDir, "[" + song.getYear() + "] " + song.getAlbum());
            }
        }
        return updatedDir;
    }

    /**
     * Validates the filename based on what we send in
     * @param file the file with the song information
     * @param updatedDir the updated directory from the calling function, where the file lives
     * @param includeArtist a boolean to see if we need to include the artist in the file name
     */
    private void renameTrack(File file, File updatedDir, boolean includeArtist) {

        // for change tracking
        File oldFile = null;

        // this if check is for if the directory changes, we need to update the file object with the new path
        if (!file.exists()) {
            oldFile = file;
            file = updateFile(file, updatedDir);
        }

        // grab the song data
        Song song = SongUtils.getSongFromFile(file);
        int index = Moose.getSongController().getIndex(song);

        // if the index wasn't found from the song, that means that the table data doesn't match the file data
        // so grab the index from the actual file(s)
        if (index == -1) {
            index = Moose.getSongController().getIndex(oldFile, file);
        }

        if (includeArtist) {
            // check if file matches 01 artist - title.mp3 standard
            if (!file.getName().matches(TRACKNUM_ARTIST_TITLE_REGEX)
                    && song != null
                    && StringUtils.isNotEmpty(song.getTrack())
                    && StringUtils.isNotEmpty(song.getArtist())
                    && StringUtils.isNotEmpty(song.getTitle())) {
                String track = song.getTrack().length() == 1 ? "0".concat(song.getTrack()) : song.getTrack();
                String newFileName = track + " " + song.getArtist() + " - " + song.getTitle() + ".mp3";
                newFileName = newFileName.replace("/", ":");
                renameFile(file, newFileName, index);
            }
        } else {
            // check if file matches 01 title.mp3 standard
            if ((!file.getName().matches(TRACKNUM_TITLE_REGEX) || file.getName().matches(TRACKNUM_ARTIST_TITLE_REGEX))
                    && song != null
                    && StringUtils.isNotEmpty(song.getTitle())
                    && StringUtils.isNotEmpty(song.getTrack())) {
                String track = song.getTrack().length() == 1 ? "0".concat(song.getTrack()) : song.getTrack();
                String newFileName = track + " " + song.getTitle() + ".mp3";
                newFileName = newFileName.replace("/", ":");
                renameFile(file, newFileName, index);
            }
        }
        // update the table and the songController
        Moose.getFrame().table.getModel().setValueAt(file, Moose.getSongController().getRow(index), TABLE_COLUMN_FILENAME);
        Moose.getFrame().songController.setNewFile(index, file);
    }

    /**
     * Helper function that renames a directory, also checks if we're in a multiple CD album
     * @param dir the directory to rename
     * @param newDirName the name to change it to
     * @return a new file if rename was successful
     */
    private File renameDirectory(File dir, String newDirName) {
        // check if we're in a multiple CD album
        File album = dir;
        if (dir.getPath().matches(CD_FILEPATH_REGEX)) {
            album = album.getParentFile();
        }
        String path = album.getParentFile().getPath();
        File newDir = new File(path + "/" + newDirName.replace("/", ":"));
        try {
            FileUtils.rename(album, newDir);
            return newDir;
        } catch (Exception e) {
            logger.logError("Couldn't rename file!", e);
            return null;
        }
    }

    /**
     * Helper function that renames a file
     * @param file the file to rename
     * @param newFileName the name to change it to
     * @param index the index of the song/file we're changing in relation to the table
     */
    private void renameFile(File file, String newFileName, int index) {
        String path = file.getParentFile().getPath();
        File newFile = new File(path + "/" + newFileName);
        // update the table
        try {
            FileUtils.rename(file, newFile);
            Moose.getFrame().table.setValueAt(
                    newFileName.replace(".mp3", StringUtils.EMPTY),
                    Moose.getSongController().getRow(index),
                    TABLE_COLUMN_FILENAME
            );
        } catch (Exception e) {
            logger.logError("Couldn't rename file!", e);
        }

    }

    /**
     * Helper function that gets the common artist of multiple files, so we know what to set the directory name to
     * @param dir the directory to base the search on
     * @return the common artist
     */
    private String getCommonArtist(File dir) {
        // create a list of songs
        List<Song> songs = MP3FileUtils.getAllSongsInDirectory(dir);

        // grab all the artists and throw them in a string list
        List<String> artists = songs.stream()
                .map(Song::getArtist)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());

        // grab the common string (artist) from each string
        String commonArtist = StringUtils.findCommonString(artists);

        // replace any non-word character at the beginning and end of the string, so we don't have any " & " or things like that anywhere
        return commonArtist.replaceAll("(^[\\W_]*)|([\\W_]*$)", StringUtils.EMPTY);
    }
}
