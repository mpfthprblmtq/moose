/**
 *  Proj:   Moose
 *  File:   AuditController.java
 *  Desc:   Controller class for AuditFrame, works directly with the data based on input from AuditFrame UI
 *
 *  Copyright Pat Ripley 2018
 */

// package
package moose.controllers;

//imports
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import moose.views.Frame;
import moose.utilities.Logger;
import moose.Main;
import moose.utilities.Utils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

// class AuditController
public class AuditController {
    
    // some ivars
    int auditCount;
    ArrayList<File> albums = new ArrayList<>();
    File folder;
    File currentDir;
    
    // Arraylists for results
    ArrayList<ArrayList<String>> auditFilePathList = new ArrayList<>(Arrays.asList(
            new ArrayList<>(),      // id3
            new ArrayList<>(),      // filenames
            new ArrayList<>()));    // cover art

    ArrayList<ArrayList<String>> cleanupFilePathList = new ArrayList<>(Arrays.asList(
            new ArrayList<>(),      // mp3.asd
            new ArrayList<>(),      // flac
            new ArrayList<>(),      // wav
            new ArrayList<>(),      // zip
            new ArrayList<>(),      // image files
            new ArrayList<>(),      // windows files
            new ArrayList<>()));    // other files
    
    // logger object
    Logger logger = Main.logger;
    
    // some constants
    public static final int AUDIT = 0;
    public static final int CLEANUP = 1;

    final int ID3 = 0;
    final int FILENAMES = 1;
    final int COVER = 2;

    final int MP3ASD = 0;
    final int FLAC = 1;
    final int WAV = 2;
    final int ZIP = 3;
    final int IMG = 4;
    final int WINDOWS = 5;
    final int OTHER = 6;
    
    public AuditController() {
        
    }
    
    public void setFolder(File folder) {
        this.folder = folder;
    }

    public File getFolder() {
        return folder;
    }

    /**
     * Larger function used to check all the files in the auditFolder for any
     * files that don't really belong
     *
     * @param type the type of analysis to run
     */
    public String analyze(int type) {

        // string to return
        String results = "";

        switch (type) {
            case CLEANUP:

                // reset everything
                clearLists(CLEANUP);

                // let's count the mp3s and covers, cause why not
                int mp3Count = 0;
                int coverCount = 0;

                // create a list of all files from that directory
                ArrayList<File> cleanupFiles = new ArrayList<>();
                cleanupFiles = Utils.listFiles(folder, cleanupFiles);

                // traverse that list
                for (File file : cleanupFiles) {

                    // get the filename to check
                    String filename = file.getName();

                    // check the ending/extension
                    // add it to the specified sublist
                    if (filename.endsWith(".mp3")) {
                        mp3Count++;
                    } else if (filename.startsWith("cover.")) {
                        coverCount++;
                    } else if (filename.endsWith(".mp3.asd")) {
                        cleanupFilePathList.get(0).add(file.getPath());
                    } else if (filename.endsWith(".flac")) {
                        cleanupFilePathList.get(1).add(file.getPath());
                    } else if (filename.endsWith(".wav")) {
                        cleanupFilePathList.get(2).add(file.getPath());
                    } else if (filename.endsWith(".zip")) {
                        cleanupFilePathList.get(3).add(file.getPath());
                    } else if ((filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".JPG"))
                            && !filename.startsWith("cover.")
                            && !file.getParentFile().getName().equals("artwork")) {
                        cleanupFilePathList.get(4).add(file.getPath());
                    } else if (filename.equals("Thumbs.db") || filename.startsWith("folder.")) {
                        cleanupFilePathList.get(5).add(file.getPath());
                    } else {
                        if (!(filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".JPG"))
                                && !filename.equals("done")
                                && !filename.equals(".DS_Store")) {
                            cleanupFilePathList.get(6).add(file.getPath());
                        }
                    }
                }

                // update the results
                results = "MP3 Files:     " + mp3Count + "\n"
                                + "Cover Files:   " + coverCount + "\n"
                                + "ZIP Files:     " + cleanupFilePathList.get(3).size() + "\n"
                                + "ASD Files:     " + cleanupFilePathList.get(0).size() + "\n"
                                + "WAV Files:     " + cleanupFilePathList.get(2).size() + "\n"
                                + "FLAC Files:    " + cleanupFilePathList.get(1).size() + "\n"
                                + "Image Files:   " + cleanupFilePathList.get(4).size() + "\n"
                                + "Windows Files: " + cleanupFilePathList.get(5).size() + "\n"
                                + "Other Files:   " + cleanupFilePathList.get(6).size();

                break;

            case AUDIT:

                // reset everything
                clearLists(AUDIT);

                // traverse that list
                for (File dir : albums) {
                    if(!checkID3Tags(dir)) {
                        auditFilePathList.get(0).add(dir.getPath());
                    }
                    if(!checkFilenames(dir)) {
                        auditFilePathList.get(1).add(dir.getPath());
                    }
                    if(!checkFolderCover(dir)) {
                        auditFilePathList.get(2).add(dir.getPath());
                    }
                }

                // update the results
                results = "ID3Tags missing:   " + auditFilePathList.get(0).size() + "\n"
                                + "Filename issues:   " + auditFilePathList.get(1).size() + "\n"
                                + "Cover art missing: " + auditFilePathList.get(2).size();

                break;
        }
        return results;
    }

    /**
     * Creates a master list of files from the audit folder, then imports all
     * the albums into the albums ivar list
     */
    public void importAlbums() {

        // arraylist to store all the files
        ArrayList<File> allFiles = new ArrayList<>();
        allFiles = Utils.listFiles(folder, allFiles);

        // traverse the list of files and add the albums to the albums ivar list
        for (File file : allFiles) {
            if (file.getName().endsWith(".mp3")) {
                File a = file.getParentFile();

                // folder is a multi-disk album
                if (a.getName().equals("CD1") || a.getName().equals("CD2")) {
                    a = a.getParentFile();
                }
                if (!albums.contains(a)) {
                    albums.add(a);
                }
            }
        }

        // sort them alphabetically
        Collections.sort(albums, (File o1, File o2) -> {
            String filename1 = o1.getPath().replace(folder.getPath(), "");
            String filename2 = o2.getPath().replace(folder.getPath(), "");
            return filename1.compareToIgnoreCase(filename2);
        });
    }

    /**
     * Called from the start audit button press Checks if an audit is already in
     * process first, then user decides to either start a new audit or continue
     * that existing audit
     */
    public void startAudit() {

        // check for an audit in process
        if (checkForExistingAudit()) {
            Object[] options = new Object[]{"Cancel", "Start New", "Continue"};
            int returnVal = JOptionPane.showOptionDialog(
                    Main.auditFrame,
                    "An existing audit is in process, do you want to continue?",
                    "Existing audit found",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    null);
            switch (returnVal) {
                case 2:     // continue
                    continueAudit();
                    break;
                case 1:     // start a new one
                    newAudit();
                    break;
                default:
                    // do nothing
                    break;
            }
        } else {
            // existing audit not found, start a fresh one
            newAudit();
        }
    }

    /**
     * Start a brand new audit
     */
    public void newAudit() {

        // clear any residual files
        clearDoneFiles();

        // set some ivars
        auditCount = 0;
        currentDir = albums.get(auditCount);

        // open up a new Frame with the album preloaded
        Main.frame.dispose();
        Main.frame = new Frame(albums.get(auditCount));
        Main.launchFrame();
    }

    /**
     * Continue an existing audit
     */
    public void continueAudit() {

        // set some ivars
        auditCount = getNextAlbum();
        currentDir = albums.get(auditCount);

        // open up a new Frame with the next album preloaded
        Main.frame.dispose();
        Main.frame = new Frame(currentDir);
        Main.launchFrame();
    }

    /**
     * Goes to the next audit album/folder
     */
    public void nextAuditFolder() {

        // sets the current album as done
        if (!checkIfDone(albums.get(auditCount))) {
            setDone(albums.get(auditCount));
        }

        // update the ivars
        auditCount++;
        currentDir = albums.get(auditCount);

        // save the tracks here instead of forcing the user to save it in the main frame form
        Main.frame.songController.saveAll();

        // check if the audit is done
        if (auditCount < albums.size()) {
            // close the current frame and open a new one with the new album preloaded
            Main.frame.dispose();
            Main.frame = new Frame(currentDir);
            Main.launchFrame();
        } else {
            // audit is done
            finishAudit();
        }
    }

    /**
     * Goes to the previous album/folder
     */
    public void previousAuditFolder() {

        // sets the current album as done
        if (!checkIfDone(albums.get(auditCount))) {
            setDone(albums.get(auditCount));
        }

        // prevent a negative auditCount
        if (auditCount <= 0) {
            // do nothing
        } else {

            // update ivars
            auditCount--;
            currentDir = albums.get(auditCount);

            Main.frame.dispose();
            Main.frame = new Frame(currentDir);
            Main.launchFrame();
        }
    }

    /**
     * Stops the audit
     */
    public void stopAudit() {
        albums.clear();
        Main.frame.dispose();
        Main.frame = new Frame();
        Main.launchFrame();
    }

    /**
     * Finishes the audit
     */
    public void finishAudit() {

        // set some ivars
        albums.clear();
        auditCount = 0;

        // launch a blank mainframe
        Main.frame.dispose();
        Main.frame = new Frame();
        Main.launchFrame();

        // reset the audit frame
        Main.auditFrame.resetAuditFrame();

        // show that audit is done
        JOptionPane.showMessageDialog(null, "Audit is complete!", "Audit Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Checks to see if in the audit folder there exists a done file in any of
     * its subfolders
     *
     * @return the result of the check, true for existing audit, false for no
     * existing audit
     */
    public boolean checkForExistingAudit() {
        return albums.stream().anyMatch((album) -> (containsFile(album)));
    }

    /**
     * Helper function used to see if there's a done file in the specified
     * folder
     *
     * @param dir, the directory to check
     * @return the result of the search, true for file found, false for file not
     * found
     */
    public boolean containsFile(File dir) {
        File[] filesInAlbum = dir.listFiles();
        for (File file : filesInAlbum) {
            if (file.getName().equals("done")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Function used to create a done file in the specified folder
     *
     * @param dir, the directory to create the file in
     */
    public void setDone(File dir) {
        String path = dir.getPath() + "/done";
        File done = new File(path);
        try {
            done.createNewFile();
        } catch (IOException ex) {
            logger.logError("Error creating done file for auditing in folder: " + path, ex);
        }
    }

    /**
     * Checks if that specified directory/album is done
     *
     * @param dir, the directory to check
     * @return the result of the check, true for done, false for not done
     */
    public boolean checkIfDone(File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.getName().equals("done")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the next album index in the albums arraylist
     *
     * @return the int index of that album
     */
    public int getNextAlbum() {
        for (File album : albums) {
            if (!checkIfDone(album)) {
                return albums.indexOf(album);
            }
        }
        return -1;
    }

    /**
     * Function used to clear all of the done files from the albums list
     */
    public void clearDoneFiles() {
        albums.stream().filter((album) -> (containsFile(album))).map((album) -> album.listFiles()).forEachOrdered((files) -> {
            for (File file : files) {
                if (file.getName().equals("done")) {
                    file.delete();
                }
            }
        });
    }

    /**
     * Function used to export the results to a heavily formatted string
     *
     * @param type the type of results needed
     * @return a string representation of the results
     */
    public String exportResultsToString(int type) {

        // lets make a string
        String str = "\n";

        // do something based on the type
        switch (type) {
            case AUDIT:

                // id3tags
                str = str.concat(" Some ID3Tags missing:\n");
                for (String path : auditFilePathList.get(ID3)) {
                    str = str.concat(" \t" + path + " \n");
                }
                str = str.concat("\n");

                // filenames
                str = str.concat(" Some filenames don't match standard:\n");
                for (String path : auditFilePathList.get(FILENAMES)) {
                    str = str.concat(" \t" + path + " \n");
                }
                str = str.concat("\n");

                // cover art
                str = str.concat(" Cover art not found in folder:\n");
                for (String path : auditFilePathList.get(COVER)) {
                    str = str.concat(" \t" + path + " \n");
                }
                str = str.concat("\n");
                break;
            case CLEANUP:

                // mp3.asd files
                str = str.concat(" MP3.ASD FILES:\n");
                for (String path : cleanupFilePathList.get(MP3ASD)) {
                    str = str.concat(" \t" + path + " \n");
                }
                str = str.concat("\n");

                // flac files
                str = str.concat(" FLAC FILES:\n");
                for (String path : cleanupFilePathList.get(FLAC)) {
                    str = str.concat(" \t" + path + " \n");
                }
                str = str.concat("\n");

                // wav files
                str = str.concat(" WAV FILES:\n");
                for (String path : cleanupFilePathList.get(WAV)) {
                    str = str.concat(" \t" + path + " \n");
                }
                str = str.concat("\n");

                // zip files
                str = str.concat(" ZIP FILES:\n");
                for (String path : cleanupFilePathList.get(ZIP)) {
                    str = str.concat(" \t" + path + " \n");
                }
                str = str.concat("\n");

                // image files
                str = str.concat(" IMAGE FILES:\n");
                for (String path : cleanupFilePathList.get(IMG)) {
                    str = str.concat(" \t" + path + " \n");
                }
                str = str.concat("\n");

                // windows files
                str = str.concat(" WINDOWS FILES:\n");
                for (String path : cleanupFilePathList.get(WINDOWS)) {
                    str = str.concat(" \t" + path + " \n");
                }
                str = str.concat("\n");

                // other files
                str = str.concat(" OTHER FILES:\n");
                for (String path : cleanupFilePathList.get(OTHER)) {
                    str = str.concat(" \t" + path + " \n");
                }
                str = str.concat("\n");

                break;
        }

        return str;
    }

    /**
     * Checks the album/folder for a cover.* file (image file)
     *
     * @return the result of the check, true for a cover file exists, false for
     * a cover file doesn't exist
     */
    public boolean checkFolderCover(File dir) {

        // check the current directory ivar
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.getName().startsWith("cover")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks all the files in a folder for the standardized file name
     *
     * @return the result of the check, true if all files are good, false if one
     * or more doesn't match
     */
    public boolean checkFilenames(File dir) {

        // create a list of all files from that directory
        ArrayList<File> files = new ArrayList<>();
        files = Utils.listFiles(dir, files);

        // regex to check
        String regex = "\\d\\d ((.)*)";

        // check all files in that list
        // also checks if the album artist is a label, since those files aren't formatted the same
        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                if (!file.getName().matches(regex) && !isLabel(file)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks all the mp3s in a folder for all the necessary id3 tags
     *
     * @return the result of the check, true if all mp3s are good, false if one
     * or more doesn't have all information needed
     */
    public boolean checkID3Tags(File dir) {

        // create a list of all files from that directory
        ArrayList<File> files = new ArrayList<>();
        files = Utils.listFiles(dir, files);

        // traverse the file list
        for (File file : files) {

            // check if it's an mp3
            if (file.getName().endsWith(".mp3")) {

                // mp3agic Mp3File object, used for the id3tags
                Mp3File mp3file;
                try {
                    // create the mp3file from the file's path
                    mp3file = new Mp3File(file.getAbsolutePath());

                    // if the mp3file doesn't have an id3tag
                    if (!mp3file.hasId3v2Tag()) {
                        return false;
                    }
                } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                    logger.logError("Exception when checking if an mp3 has id3 information!", ex);
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

                // check if it's a label, since those mp3s don't need as much information
                if (isLabel(dir)) {
                    if (title == null
                            || artist == null
                            || album == null
                            || albumartist == null
                            || genre == null
                            || artwork_bytes == null) {
                        return false;
                    }
                } else {
                    if (title == null
                            || artist == null
                            || album == null
                            || albumartist == null
                            || genre == null
                            || year == null
                            || track == null
                            || disk == null
                            || artwork_bytes == null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Check if a directory is from a label
     *
     * @param dir, the directory to check
     * @return the result of the check, true if it is a label, false if it isn't
     * a label
     */
    public boolean isLabel(File dir) {
        String path = dir.getPath();
        return path.contains("/Genres/");
    }

    /**
     * Deletes all files in the cleanupFilePathList arraylist
     */
    public void deleteAll() {
        if (!iscleanupFilePathListEmpty()) {
            cleanupFilePathList.forEach((list) -> {
                deleteAllFilesInList(list);
            });
            // clear all the lists to reset counts
            clearLists(CLEANUP);
            // reset the statistics
            analyze(CLEANUP);
        } else {
            // show that there's nothing to delete
            JOptionPane.showMessageDialog(null, "Nothing to Delete!", "Delete All", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Deletes only the checked file types
     */
    public void deleteSelected(boolean mp3asd, boolean flac, boolean wav, boolean zip, boolean images, boolean windows, boolean everythingElse) {

        boolean nothingToDelete = true;

        if (mp3asd) {
            nothingToDelete = cleanupFilePathList.get(MP3ASD).isEmpty();
            deleteAllFilesInList(cleanupFilePathList.get(MP3ASD));
            cleanupFilePathList.get(MP3ASD).clear();
        }
        if (flac) {
            nothingToDelete = nothingToDelete && cleanupFilePathList.get(FLAC).isEmpty();
            deleteAllFilesInList(cleanupFilePathList.get(FLAC));
            cleanupFilePathList.get(FLAC).clear();
        }
        if (wav) {
            nothingToDelete = nothingToDelete && cleanupFilePathList.get(WAV).isEmpty();
            deleteAllFilesInList(cleanupFilePathList.get(WAV));
            cleanupFilePathList.get(WAV).clear();
        }
        if (zip) {
            nothingToDelete = nothingToDelete && cleanupFilePathList.get(ZIP).isEmpty();
            deleteAllFilesInList(cleanupFilePathList.get(ZIP));
            cleanupFilePathList.get(ZIP).clear();
        }
        if (images) {
            nothingToDelete = nothingToDelete && cleanupFilePathList.get(IMG).isEmpty();
            deleteAllFilesInList(cleanupFilePathList.get(IMG));
            cleanupFilePathList.get(IMG).clear();
        }
        if (windows) {
            nothingToDelete = nothingToDelete && cleanupFilePathList.get(WINDOWS).isEmpty();
            deleteAllFilesInList(cleanupFilePathList.get(WINDOWS));
            cleanupFilePathList.get(WINDOWS).clear();
        }
        if (everythingElse) {
            nothingToDelete = nothingToDelete && cleanupFilePathList.get(OTHER).isEmpty();
            deleteAllFilesInList(cleanupFilePathList.get(OTHER));
            cleanupFilePathList.get(OTHER).clear();
        }

        if (nothingToDelete) {
            // show that there's nothing to delete
            JOptionPane.showMessageDialog(null, "Nothing to Delete!", "Delete Selected", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // clear all the lists to reset counts
            clearLists(CLEANUP);
            // reset the statistics
            analyze(CLEANUP);
        }
    }

    /**
     * Helper function used to delete files in a specific list
     *
     * @param list
     */
    public void deleteAllFilesInList(ArrayList<String> list) {
        list.stream().map((path) -> new File(path)).forEachOrdered((file) -> {
            if (file.exists()) {
                file.delete();
            } else {
                logger.logError("Tried to delete file in cleanup, but couldn't!");
            }
        });
    }

    /**
     * Helper function to clear all sublists in cleanupFilePathList
     *
     * @param type the type of lists to clear
     */
    public void clearLists(int type) {
        switch (type) {
            case CLEANUP:
                cleanupFilePathList.get(0).clear();
                cleanupFilePathList.get(1).clear();
                cleanupFilePathList.get(2).clear();
                cleanupFilePathList.get(3).clear();
                cleanupFilePathList.get(4).clear();
                cleanupFilePathList.get(5).clear();
                cleanupFilePathList.get(6).clear();
                break;
            case AUDIT:
                auditFilePathList.get(0).clear();
                auditFilePathList.get(1).clear();
                auditFilePathList.get(2).clear();
                break;
        }

    }

    /**
     * Helper function to check if any of the lists are empty
     *
     * @return the result of the check
     */
    public boolean iscleanupFilePathListEmpty() {
        return (cleanupFilePathList.get(0).isEmpty()
                && cleanupFilePathList.get(1).isEmpty()
                && cleanupFilePathList.get(2).isEmpty()
                && cleanupFilePathList.get(3).isEmpty()
                && cleanupFilePathList.get(4).isEmpty()
                && cleanupFilePathList.get(5).isEmpty()
                && cleanupFilePathList.get(6).isEmpty());
    }
    
}
