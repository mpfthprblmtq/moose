/*
 *  Proj:   Moose
 *  File:   CleanupService.java
 *  Desc:   Service class for AuditFrame and CleanupController, works directly with CleanupController and has the bulk
 *          of the logic for cleanup functionality.
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.services;

// imports
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.utilities.AuditCleanupUtils;
import com.mpfthprblmtq.moose.utilities.Constants;
import com.mpfthprblmtq.moose.utilities.viewUtils.DialogUtils;
import lombok.Data;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// class CleanupService
@Data
public class CleanupService {

    // folder where all the magic happens
    File folder;

    // logger
    Logger logger = Moose.getLogger();

    // some count fields
    int mp3Count;
    int coverCount;

    /**
     * Analysis function for cleanup
     * @param file the file we're checking
     * @param filePathList the list of lists that we'll fill with the analysis results
     */
    public void analyzeFile(File file, List<List<String>> filePathList) {
        // get the filename to check
        String filename = file.getName();

        // check the ending/extension
        // add it to the specified sublist
        if (filename.endsWith(".mp3")) {
            mp3Count++;
        } else if (filename.startsWith("cover.")) {
            coverCount++;
        } else if (filename.endsWith(".mp3.asd")) {
            filePathList.get(Constants.MP3ASD).add(file.getPath());
        } else if (filename.endsWith(".flac")) {
            filePathList.get(Constants.FLAC).add(file.getPath());
        } else if (filename.endsWith(".wav")) {
            filePathList.get(Constants.WAV).add(file.getPath());
        } else if (filename.endsWith(".zip")) {
            filePathList.get(Constants.ZIP).add(file.getPath());
        } else {
            final boolean isImageFile = filename.endsWith(".png")
                    || filename.endsWith(".jpg")
                    || filename.endsWith(".jpeg")
                    || filename.endsWith(".JPG");
            if (isImageFile
                    && !filename.startsWith("cover.")
                    && !file.getParentFile().getName().equals("artwork")) {
                filePathList.get(Constants.IMG).add(file.getPath());
            } else if (filename.equals("Thumbs.db") || filename.startsWith("folder.")) {
                filePathList.get(Constants.WINDOWS).add(file.getPath());
            } else {
                if (!isImageFile
                        && !filename.equals("done")
                        && !filename.equals(".DS_Store")) {
                    filePathList.get(Constants.OTHER).add(file.getPath());
                }
            }
        }
    }

    /**
     * Returns a basic version of the cleanup analysis results to show on the AuditFrame cleanup text area
     * @param filePathList the list of file paths for most of the file types
     * @return the string version of the results
     */
    public String getAnalysisResults(List<List<String>> filePathList) {
        return "MP3 Files:     " + StringUtils.formatNumber(mp3Count) + "\n"
                + "Cover Files:   " + StringUtils.formatNumber(coverCount) + "\n"
                + "ZIP Files:     " + StringUtils.formatNumber(filePathList.get(3).size()) + "\n"
                + "ASD Files:     " + StringUtils.formatNumber(filePathList.get(0).size()) + "\n"
                + "WAV Files:     " + StringUtils.formatNumber(filePathList.get(2).size()) + "\n"
                + "FLAC Files:    " + StringUtils.formatNumber(filePathList.get(1).size()) + "\n"
                + "Image Files:   " + StringUtils.formatNumber(filePathList.get(4).size()) + "\n"
                + "Windows Files: " + StringUtils.formatNumber(filePathList.get(5).size()) + "\n"
                + "Other Files:   " + StringUtils.formatNumber(filePathList.get(6).size());
    }

    /**
     * Helper method to get a "pretty" version of the count of each type of file in the cleanup file path list to show
     * in the View Results dialog
     * @param filePathList the list of lists for all the cleanup files
     * @return a string representation of the results
     */
    public String exportCleanupResultsToString(List<List<String>> filePathList) {

        // lets make a string
        String str = "\n";

        str = str.concat(" MP3.ASD FILES:\n");
        for (String path : filePathList.get(Constants.MP3ASD)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        str = str.concat(" FLAC FILES:\n");
        for (String path : filePathList.get(Constants.FLAC)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        str = str.concat(" WAV FILES:\n");
        for (String path : filePathList.get(Constants.WAV)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        str = str.concat(" ZIP FILES:\n");
        for (String path : filePathList.get(Constants.ZIP)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        str = str.concat(" IMAGE FILES:\n");
        for (String path : filePathList.get(Constants.IMG)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        str = str.concat(" WINDOWS FILES:\n");
        for (String path : filePathList.get(Constants.WINDOWS)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        str = str.concat(" OTHER FILES:\n");
        for (String path : filePathList.get(Constants.OTHER)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        return str;
    }

    /**
     * Deletes all files in the cleanupFilePathList arraylist
     * @param cleanupFilePathList the list of all file path lists to delete
     */
    public void deleteAll(List<List<String>> cleanupFilePathList) {
        int filesDeleted = 0;
        if (!AuditCleanupUtils.isListOfListsEmpty(cleanupFilePathList)) {
            for (List<String> list : cleanupFilePathList) {
                filesDeleted += deleteAllFilesInList(list);
            }
        }

        // show success message with total files deleted
        DialogUtils.showMessageDialog(null, filesDeleted + " file(s) successfully deleted!", "Delete Success", JOptionPane.WARNING_MESSAGE);

        // clear all the lists to reset counts
        AuditCleanupUtils.clearLists(cleanupFilePathList);
    }

    /**
     * Deletes only the selected file types
     * @param cleanupFilePathList the list of file path lists to add to
     * @param mp3asd a boolean to determine if we're deleting .mp3asd files
     * @param flac a boolean to determine if we're deleting .flac files
     * @param wav a boolean to determine if we're deleting .wav files
     * @param zip a boolean to determine if we're deleting .zip files
     * @param images a boolean to determine if we're deleting image files (*.jpg, *.png, etc.) (Excludes cover.* files)
     * @param windows a boolean to determine if we're deleting windows-specific files (folder.*, thumbs.db, etc.)
     * @param everythingElse a boolean to determine if we're deleting all other random files
     * @param customExtension a string as a certain file extension to delete
     */
    public void deleteSelected(
            List<List<String>> cleanupFilePathList,
            boolean mp3asd,
            boolean flac,
            boolean wav,
            boolean zip,
            boolean images,
            boolean windows,
            boolean everythingElse,
            String customExtension) {
        int filesDeleted = 0;

        if (mp3asd) {
            filesDeleted += deleteFiles(cleanupFilePathList.get(Constants.MP3ASD));
        }
        if (flac) {
            filesDeleted += deleteFiles(cleanupFilePathList.get(Constants.FLAC));
        }
        if (wav) {
            filesDeleted += deleteFiles(cleanupFilePathList.get(Constants.WAV));
        }
        if (zip) {
            filesDeleted += deleteFiles(cleanupFilePathList.get(Constants.ZIP));
        }
        if (images) {
            filesDeleted += deleteFiles(cleanupFilePathList.get(Constants.IMG));
        }
        if (windows) {
            filesDeleted += deleteFiles(cleanupFilePathList.get(Constants.WINDOWS));
        }
        if (everythingElse) {
            filesDeleted += deleteFiles(cleanupFilePathList.get(Constants.OTHER));
        }
        if (StringUtils.isNotEmpty(customExtension)) {
            filesDeleted += deleteFiles(getAllFilesWithExtension(customExtension));
        }

        // show success message with total files deleted
        DialogUtils.showMessageDialog(null, filesDeleted + " file(s) successfully deleted!", "Delete Success", JOptionPane.WARNING_MESSAGE);

        // clear the list of lists
        AuditCleanupUtils.clearLists(cleanupFilePathList);
    }

    /**
     * Helper function used by deleteSelected to create a list of all custom file extensions
     * @param extension the extension of the files to delete
     * @return a list of the files with that extension
     */
    private List<String> getAllFilesWithExtension(String extension) {
        List<String> customExtensionFilePaths = new ArrayList<>();
        List<File> customExtensionFiles = new ArrayList<>();
        FileUtils.listFiles(folder, customExtensionFiles);

        for (File file : customExtensionFiles) {
            if (file.getName().endsWith("." + extension)) {
                customExtensionFilePaths.add(file.getPath());
            }
        }
        return customExtensionFilePaths;
    }

    /**
     * Helper function used by deleteSelected to prevent some code duplication
     * @param list the list of file paths to delete
     */
    private int deleteFiles(List<String> list) {
        // check to see if the list is empty, return 0 if it is
        if (list.isEmpty()) {
            return 0;
        }
        return deleteAllFilesInList(list);
    }

    /**
     * Helper function used to delete files in a specific list
     * @param list the list of files to delete
     */
    public int deleteAllFilesInList(List<String> list) {
        int filesDeleted = 0;
        for (String path : list) {
            File file = new File(path);
            if (file.exists()) {
                if (!file.delete()) {
                    logger.logError("Tried to delete " + file.getPath() + " in cleanup, but couldn't!");
                } else {
                    filesDeleted++;
                }
            } else {
                logger.logError("Tried to delete " + file.getPath() + " in cleanup, but the file didn't exist!");
            }
        }
        return filesDeleted;
    }
}
