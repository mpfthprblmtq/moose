/*
 *  Proj:   Moose
 *  File:   CleanupController.java
 *  Desc:   Driver class for the cleanup functionality.  Takes input from the AuditFrame and uses the CleanupService
 *          to perform cleanup functions.
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

// package
package com.mpfthprblmtq.moose.controllers;

// imports
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.services.CleanupService;
import com.mpfthprblmtq.moose.utilities.AuditCleanupUtils;
import com.mpfthprblmtq.moose.views.modals.AuditFrame;
import lombok.Data;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// class CleanupController
@Data
public class CleanupController {

    // service
    CleanupService cleanupService;

    // frame
    AuditFrame auditFrame;

    // some fields
    File folder;

    // lists for results
    List<List<String>> filePathList = new ArrayList<>(Arrays.asList(
            new ArrayList<>(),      // mp3.asd
            new ArrayList<>(),      // flac
            new ArrayList<>(),      // wav
            new ArrayList<>(),      // zip
            new ArrayList<>(),      // image files
            new ArrayList<>(),      // Windows files
            new ArrayList<>(),      // other files
            new ArrayList<>(),      // mp3 files
            new ArrayList<>()));    // cover files

    /**
     * Creates new controller CleanupController
     */
    public CleanupController() {
        this.auditFrame = Moose.getAuditFrame();
        cleanupService = new CleanupService();
        cleanupService.setFolder(folder);
    }

    /**
     * Main driver method for the analysis function.  Scans all files in the folder chosen (either user's library
     * location or the folder chosen), and tabulates how many of each file there is.  All while updating the UI.
     */
    public void analyze() {
        // update graphics to start
        auditFrame.setCleanupLoading(true);

        // set fields to 0 to start fresh
        cleanupService.setMp3Count(0);
        cleanupService.setCoverCount(0);

        // create the list of all files, so we can iterate through them
        List<File> cleanupFiles = new ArrayList<>();
        FileUtils.listFiles(folder, cleanupFiles);

        // do the analysis
        AuditCleanupUtils.clearLists(filePathList);
        for (int i = 0; i < cleanupFiles.size(); i++) {
            cleanupService.analyzeFile(cleanupFiles.get(i), filePathList);
            auditFrame.updateCleanupProgressBar(AuditCleanupUtils.formatPercentage(i, cleanupFiles.size()));
        }

        // update graphics again
        auditFrame.setCleanupLoading(false);
        auditFrame.setCleanupResults(cleanupService.getAnalysisResults(filePathList));
        auditFrame.setCleanupCurrentlyScanningLabelHorizontalAlignment(SwingConstants.LEADING);
        auditFrame.updateCleanupCurrentlyScanningLabel(StringUtils.formatNumber(cleanupFiles.size())
                + " files successfully scanned!");
    }

    /**
     * Returns the detail list for showing on the View Results panel
     * @return the detailed list of file types
     */
    public String getResults() {
        return cleanupService.exportCleanupResultsToString(filePathList);
    }

    /**
     * Deletes all files in the filePathList
     */
    public void deleteAll() {
        cleanupService.deleteAll(filePathList);
        analyze();
    }

    /**
     * Only deletes selected options
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
            boolean mp3asd,
            boolean flac,
            boolean wav,
            boolean zip,
            boolean images,
            boolean windows,
            boolean everythingElse,
            String customExtension) {

        // delete
        cleanupService.deleteSelected(
                filePathList,
                mp3asd,
                flac,
                wav,
                zip,
                images,
                windows,
                everythingElse,
                customExtension);
        analyze();
    }

    /**
     * Sets the folder here as well in the service since we may need it there too
     * @param folder the folder to set
     */
    public void setFolder(File folder) {
        this.folder = folder;
        this.cleanupService.setFolder(folder);
    }
}
