package com.mpfthprblmtq.moose.services;

import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.utilities.AuditCleanupUtils;
import com.mpfthprblmtq.moose.utilities.Constants;
import com.mpfthprblmtq.moose.utilities.viewUtils.DialogUtils;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CleanupService {

    // logger
    Logger logger = Moose.getLogger();

    /**
     * Analysis function for cleanup
     * @param folder, the parent folder of all the artist folders to look at
     * @param filePathList, the list of lists that we'll fill with the analysis results
     * @return a formatted string with the results of the analysis
     */
    public String analyze(File folder, List<List<String>> filePathList) {
        int mp3Count = 0;
        int coverCount = 0;
        List<File> cleanupFiles = new ArrayList<>();
        FileUtils.listFiles(folder, cleanupFiles);
        Moose.getAuditFrame().setCleanupCurrentlyScanningLabelHorizontalAlignment(SwingConstants.TRAILING);
        int total = cleanupFiles.size();
        double index = 0;

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
            Moose.getAuditFrame().updateCleanupProgressBar(AuditCleanupUtils.formatPercentage(index, total));
            index++;
        }
        Moose.getAuditFrame().setCleanupCurrentlyScanningLabelHorizontalAlignment(SwingConstants.LEADING);
        Moose.getAuditFrame().updateCleanupCurrentlyScanningLabel(cleanupFiles.size() + " files successfully scanned!");
        return "MP3 Files:     " + mp3Count + "\n"
                + "Cover Files:   " + coverCount + "\n"
                + "ZIP Files:     " + filePathList.get(3).size() + "\n"
                + "ASD Files:     " + filePathList.get(0).size() + "\n"
                + "WAV Files:     " + filePathList.get(2).size() + "\n"
                + "FLAC Files:    " + filePathList.get(1).size() + "\n"
                + "Image Files:   " + filePathList.get(4).size() + "\n"
                + "Windows Files: " + filePathList.get(5).size() + "\n"
                + "Other Files:   " + filePathList.get(6).size();
    }

    /**
     * Function used to export the results of cleanup to a heavly formatted string
     * @param filePathList, the list of lists for all the cleanup files
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
     */
    public void deleteSelected(List<List<String>> cleanupFilePathList, boolean mp3asd, boolean flac, boolean wav, boolean zip, boolean images, boolean windows, boolean everythingElse) {
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

        // show success message with total files deleted
        DialogUtils.showMessageDialog(null, filesDeleted + " file(s) successfully deleted!", "Delete Success", JOptionPane.WARNING_MESSAGE);

        // clear the list of lists
        AuditCleanupUtils.clearLists(cleanupFilePathList);
    }

    /**
     * Helper function used by deleteSelected to prevent some code duplication
     */
    private int deleteFiles(List<String> list) {
        // check to see if the list is empty, return false if it is
        if (list.isEmpty()) {
            return 0;
        }

        return deleteAllFilesInList(list);
    }

    /**
     * Helper function used to delete files in a specific list
     *
     * @param list, the list of files to delete
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
