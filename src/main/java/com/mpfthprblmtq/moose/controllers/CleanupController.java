package com.mpfthprblmtq.moose.controllers;

import com.mpfthprblmtq.moose.services.CleanupService;
import com.mpfthprblmtq.moose.utilities.AuditCleanupUtils;
import com.mpfthprblmtq.moose.views.modals.AuditFrame;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CleanupController {

    // service
    CleanupService cleanupService;

    // frame
    AuditFrame auditFrame;

    // some ivars
    File folder;

    // lists for results
    List<List<String>> filePathList = new ArrayList<>(Arrays.asList(
            new ArrayList<>(),      // mp3.asd
            new ArrayList<>(),      // flac
            new ArrayList<>(),      // wav
            new ArrayList<>(),      // zip
            new ArrayList<>(),      // image files
            new ArrayList<>(),      // windows files
            new ArrayList<>()));    // other files

    public CleanupController(AuditFrame auditFrame) {
        this.auditFrame = auditFrame;
        cleanupService = new CleanupService();
    }

    public String analyze() {
        AuditCleanupUtils.clearLists(filePathList);
        return cleanupService.analyze(folder, filePathList);
    }

    public String getResults() {
        return cleanupService.exportCleanupResultsToString(filePathList);
    }

    public void deleteAll() {
        cleanupService.deleteAll(filePathList);
        // TODO reset statistics
    }

    public void deleteSelected(boolean mp3asd, boolean flac, boolean wav, boolean zip, boolean images, boolean windows, boolean everythingElse) {
        cleanupService.deleteSelected(filePathList, mp3asd, flac, wav, zip, images, windows, everythingElse);
        // TODO reset statistics
    }

    /**
     * Sets the folder
     * @param folder the folder to set
     */
    public void setFolder(File folder) {
        this.folder = folder;
    }

    /**
     * Returns the folder
     * @return the folder
     */
    public File getFolder() {
        return this.folder;
    }
}
