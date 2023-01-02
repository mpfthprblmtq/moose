/*
 *  Proj:   Moose
 *  File:   AuditController.java
 *  Desc:   Driver class for the audit functionality.  Takes input from the AuditFrame and uses the AuditService
 *          to perform audit functions.
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

// package
package com.mpfthprblmtq.moose.controllers;

// imports
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.services.AuditService;
import com.mpfthprblmtq.moose.utilities.AuditCleanupUtils;
import com.mpfthprblmtq.moose.utilities.viewUtils.DialogUtils;
import com.mpfthprblmtq.moose.views.modals.AuditFrame;
import com.mpfthprblmtq.moose.views.Frame;
import lombok.Data;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static com.mpfthprblmtq.moose.utilities.Constants.*;

// class AuditController
@Data
public class AuditController {

    // service
    AuditService auditService;

    // frames
    Frame frame;
    AuditFrame auditFrame;

    // controller
    SongController songController;

    // some fields
    File folder;
    int currentIndex;

    // boolean things
    private boolean coverIssues;
    private boolean fileIssues;
    private boolean id3Issues;

    // current directory
    private String currentDirectory;

    // list of albums for the audit
    List<File> albums = new ArrayList<>();

    // lists for results
    List<List<String>> filePathList = new ArrayList<>(Arrays.asList(
            new ArrayList<>(),      // id3
            new ArrayList<>(),      // filenames
            new ArrayList<>()));    // cover art

    /**
     * Creates new controller AuditController
     */
    public AuditController() {
        this.frame = Moose.getFrame();
        this.auditFrame = Moose.getAuditFrame();
        this.songController = Moose.getSongController();
        this.auditService = new AuditService();
    }

    /**
     * Main driver method for the analysis function.  Warns the user of experimental functionality, imports all the
     * albums from the user's library directory, and analyzes each one.  All while updating the UI.
     */
    public void analyze() {

        // warn the user that this is experimental
        // TODO remove this once it's not experimental anymore
        if (Moose.getSettings().isWarnBeforeStartingAudit()) {
            boolean dontAskAgain = DialogUtils.showAuditWarningDialog(Moose.getAuditFrame());
            if (dontAskAgain) {
                Moose.getSettings().setWarnBeforeStartingAudit(false);
                Moose.settingsFrame.settingsController.writeSettingsFile(Moose.getSettings());
            }
        }

        // clean up to start and update graphics
        AuditCleanupUtils.clearLists(filePathList);
        auditFrame.setAuditLoading(true);

        // import all albums into a list of directories
        albums = auditService.importAlbums(folder);

        // iterate through each album (directory) and check all issues while updating graphics
        auditFrame.setAuditCurrentlyScanningLabelHorizontalAlignment(SwingConstants.TRAILING);
        for (int i = 0; i < albums.size(); i++) {
            File album = albums.get(i);
            auditFrame.updateAuditCurrentlyScanningLabel(   // update currently scanning label with file path
                    StringUtils.truncateWithEllipsesTrailing(album.getPath(), 56));
            // check for each type of issue
            if (auditService.id3TagsHaveErrors(album)) {
                filePathList.get(ID3).add(album.getPath());
            }
            if (auditService.filesHaveErrors(album)) {
                filePathList.get(FILENAMES).add(album.getPath());
            }
            if (auditService.coverHasErrors(album)) {
                filePathList.get(COVER).add(album.getPath());
            }

            // update progress bar
            auditFrame.updateAuditProgressBar(AuditCleanupUtils.formatPercentage(i, albums.size()));
        }

        // update graphics with results
        auditFrame.setAuditLoading(false);
        auditFrame.setAuditCurrentlyScanningLabelHorizontalAlignment(SwingConstants.LEADING);
        auditFrame.updateAuditCurrentlyScanningLabel(StringUtils.formatNumber(albums.size())
                + " albums successfully scanned!");
        auditFrame.setAuditResults(auditService.getResultsFromFilePathList(filePathList));
    }

    /**
     * Returns the detail list for showing on the View Results panel
     * @return the detailed list of issues from audit
     */
    public String getResults() {
        return auditService.exportAuditResultsToString(filePathList);
    }

    /**
     * Main driver for when user wants to start an audit.  Checks if an audit is already in process first, then user
     * decides to either start a new audit or continue that existing audit.
     */
    public void startAudit() {
        // check what type of audit we want to do
        switch(DialogUtils.showShouldAuditAllDialog()) {
            case DialogUtils.ALL_ALBUMS:
                break;
            case DialogUtils.ONLY_MARKED_ALBUMS:
                // if user selects to only do marked albums, call the utility method to get only albums with issues
                albums = auditService.getAllMarkedAlbums(filePathList);
                if (!albums.isEmpty()) {
                    if (albums.size() == 1) {
                        auditFrame.setNextButtonText("Finish Audit");
                    }
                    newAudit();
                } else {
                    stopAudit(true);
                }
                return;
            default:
                return;
        }

        // check for audit in process
        if (auditService.checkForExistingAudit(albums)) {
            switch (DialogUtils.showExistingAuditDialog()) {
                case DialogUtils.CONTINUE_AUDIT:
                    continueAudit();
                    break;
                case DialogUtils.START_NEW_AUDIT:
                    newAudit();
                    break;
                default:
                    // do nothing
                    break;
            }
        } else {
            // existing audit not found, start a new one
            newAudit();
        }

        // bring the audit frame into focus
        auditFrame.toFront();
        auditFrame.requestFocus();
    }

    /**
     * Start method for when user selects to start a new audit.  Clears any residual done files left over from previous
     * audits, and opens a new audit window at 0.
     */
    public void newAudit() {
        this.currentIndex = 0;
        auditService.clearDoneFiles(albums);    // clear any residual files
        auditService.openAuditWindow(albums, currentIndex);    // start at zero
    }

    /**
     * Start method for when user selects to continue an existing audit.  Gets the next album to audit and opens a new
     * audit window at that index.
     */
    public void continueAudit() {
        this.currentIndex = auditService.getNextAlbum(albums);  // get where we left off
        auditService.openAuditWindow(albums, currentIndex);    // open the next window
    }

    /**
     * Goes to the next folder in audit. Called from the next button action method in the audit frame.
     */
    public void nextFolder() {
        // sets the current album as done
        if (auditService.isNotDone(albums.get(currentIndex))) {
            auditService.setDone(albums.get(currentIndex));
        }

        // get the most up-to-date frame and song controller since the frame updates on each album
        this.frame = Moose.getFrame();
        this.songController = Moose.getSongController();

        // update the table in the songController
        songController.setTable(frame.table);

        // save all the tracks in the current screen so the user doesn't have to manually do it
        songController.saveTracks(IntStream.range(0, frame.table.getRowCount()).toArray());

        // check if the audit is done
        if (currentIndex + 2 == albums.size()) {
            // audit is close to done
            auditFrame.setNextButtonText("Finish Audit");
        } else if (currentIndex + 1 == albums.size()) {
            // audit is done
            stopAudit(true);
        } else {
            // move on with the next album in the list
            currentIndex++;
            auditService.openAuditWindow(albums, currentIndex);
        }
    }

    /**
     * Goes to the previous folder in audit. Called from the previous button action method in the audit frame.
     */
    public void previousFolder() {
        // sets the current album as done
        if (auditService.isNotDone(albums.get(currentIndex))) {
            auditService.setDone(albums.get(currentIndex));
        }

        // save all the tracks in the current screen so the user doesn't have to manually do it
        Moose.frame.songController.saveTracks(IntStream.range(0, frame.table.getRowCount()).toArray());

        // prevent a negative index
        if (currentIndex >= 0) {
            // move on with the previous album in the list
            currentIndex--;
            auditService.openAuditWindow(albums, currentIndex);
        }
    }

    /**
     * Stops the audit.
     * @param finished a boolean to determine if we "finished" an audit or if we just stopped it
     */
    public void stopAudit(boolean finished) {
        // reset the audit frame
        auditFrame.resetAuditFrame();

        // clear fields
        albums.clear();
        currentIndex = 0;

        // launch a blank frame
        Moose.frame.dispose();
        Moose.frame = new Frame();
        Moose.launchFrame();

        if (finished) {
            // clear done files
            auditService.clearDoneFiles(albums);
            // show that the audit is done
            DialogUtils.showMessageDialog(null, "Audit is complete!", "Audit Completed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Sets the fields in the controller
     */
    public void setFields(boolean id3Issues, boolean fileIssues, boolean coverIssues, String currentDirectory) {
        this.id3Issues = id3Issues;
        this.fileIssues = fileIssues;
        this.coverIssues = coverIssues;
        this.currentDirectory = currentDirectory;
    }

    /**
     * Attempts to auto fix whatever album we're on, whether it be autotagging, adding a cover, or modifying file names
     */
    public void autoFix() {

        // file object to see if there was an update to the current directory
        File directory = new File(this.currentDirectory);

        if (this.id3Issues && this.fileIssues && this.coverIssues) {
            auditService.autoFixID3();
            directory = auditService.autoFixFilePaths(new File(this.currentDirectory));
            // covers are handled by auto tagging
        } else if (!this.id3Issues && this.fileIssues && this.coverIssues) {
            directory = auditService.autoFixFilePaths(new File(this.currentDirectory));
            auditService.autoFixCovers();
        } else if (this.id3Issues && !this.fileIssues && this.coverIssues) {
            auditService.autoFixID3();
            // covers are handled by auto tagging
        } else if (!this.id3Issues && !this.fileIssues && this.coverIssues) {
            auditService.autoFixCovers();
        } else if (this.id3Issues && this.fileIssues) {
            auditService.autoFixID3();
            directory = auditService.autoFixFilePaths(new File(this.currentDirectory));
        } else if (this.id3Issues) {
            auditService.autoFixID3();
        } else if (this.fileIssues) {
            directory = auditService.autoFixFilePaths(new File(this.currentDirectory));
        }

        // now that the auto fixes are done, reload the frame and set some fields
        // if the directory changed names, update it in the filePathList so that we can keep track of how many albums
        // we've gone through
        if (!this.currentDirectory.equals(directory.getPath())) {
            if (filePathList.get(ID3).contains(this.currentDirectory)) {
                filePathList.get(ID3).set(filePathList.get(ID3).indexOf(this.currentDirectory), directory.getPath());
            }
            if (filePathList.get(FILENAMES).contains(this.currentDirectory)) {
                filePathList.get(FILENAMES).set(filePathList.get(FILENAMES).indexOf(this.currentDirectory), directory.getPath());
            }
            if (filePathList.get(COVER).contains(this.currentDirectory)) {
                filePathList.get(COVER).set(filePathList.get(COVER).indexOf(this.currentDirectory), directory.getPath());
            }
        }
        albums.set(currentIndex, directory);
        List<Boolean> results = auditService.getCheckResults(directory);
        setFields(results.get(ID3), results.get(FILENAMES), results.get(COVER), directory.getPath());
        auditFrame.refreshAuditFrameFromAudit(results, directory.getPath());

        updateAuditResultCount();
        if (!this.id3Issues && !this.fileIssues && !this.coverIssues && auditFrame.shouldAdvanceOnAutofix()) {
            auditFrame.next();
        }
    }

    /**
     * A helper refresh function called from the autofix method to update the checks
     */
    public void updateAuditResultCount() {
        if (!this.id3Issues) {
            filePathList.get(ID3).remove(this.currentDirectory);
        }
        if (!this.fileIssues) {
            filePathList.get(FILENAMES).remove(this.currentDirectory);
        }
        if (!this.coverIssues) {
            filePathList.get(COVER).remove(this.currentDirectory);
        }
        auditFrame.setAuditResults(auditService.getResultsFromFilePathList(filePathList));
    }
}
