package moose.controllers;

import moose.Moose;
import moose.services.AuditService;
import moose.utilities.AuditCleanupUtils;
import moose.services.DialogService;
import moose.views.AuditFrame;
import moose.views.Frame;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static moose.utilities.Constants.*;

public class AuditController {

    // service
    AuditService auditService;

    // frames
    Frame frame;
    AuditFrame auditFrame;

    // controller
    SongController songController;

    // some ivars
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

    public AuditController(Frame frame, AuditFrame auditFrame, SongController songController) {
        this.frame = frame;
        this.auditFrame = auditFrame;
        this.auditService = new AuditService(this.auditFrame);
        this.songController = songController;
    }

    public String analyze() {
        AuditCleanupUtils.clearLists(filePathList);
        albums = auditService.importAlbums(folder);
        return auditService.analyzeForAudit(albums, filePathList);
    }

    /**
     * Checks if an audit is already in process first, then user decides to either start a new audit or continue that existing audit
     */
    public void startAudit() {

        // check what type of audit we want to do
        switch(DialogService.showShouldAuditAllDialog()) {
            case 2:     // all albums
                break;
            case 1:     // only marked albums
                albums = auditService.getAllMarkedAlbums(filePathList);
                if (!albums.isEmpty()) {
                    if (albums.size() == 1) {
                        auditFrame.setNextButtonText("Finish Audit");
                    }
                    newAudit();
                } else {
                    finishAudit();
                }
                return;
            default:
                return;
        }

        // check for audit in process
        if (auditService.checkForExistingAudit(albums)) {
            switch (DialogService.showExistingAuditDialog()) {
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
            // existing audit not found, start a new one
            newAudit();
        }

        // bring the audit frame into focus
        auditFrame.toFront();
        auditFrame.requestFocus();
    }

    public void newAudit() {
        this.currentIndex = 0;
        auditService.clearDoneFiles(albums);    // clear any residual files
        auditService.openAuditWindow(albums, currentIndex);    // start at zero
    }

    public void continueAudit() {
        this.currentIndex = auditService.getNextAlbum(albums);  // get where we left off
        auditService.openAuditWindow(albums, currentIndex);    // open the next window
    }

    /**
     * Goes to the next folder
     */
    public void nextFolder() {

        // sets the current album as done
        if (auditService.isNotDone(albums.get(currentIndex))) {
            auditService.setDone(albums.get(currentIndex));
        }

        // get the most up to date frame and song controller since the frame updates on each album
        this.frame = Moose.getFrame();
        this.songController = Moose.getFrame().getSongController();

        // update the table in the songController
        songController.setTable(frame.table);

        // save all of the tracks in the current screen so the user doesn't have to manually do it
        songController.saveTracks(IntStream.range(0, frame.table.getRowCount()).toArray());

        // check if the audit is done
        if (currentIndex + 2 == albums.size()) {
            // audit is close to done
            auditFrame.setNextButtonText("Finish Audit");
        }
        if (currentIndex + 1 == albums.size()) {
            // audit is done
            finishAudit();
        } else {
            // move on with the next album in the list
            currentIndex++;
            auditService.openAuditWindow(albums, currentIndex);
        }
    }

    /**
     * Goes to the previous folder
     */
    public void previousFolder() {
        // sets the current album as done
        if (auditService.isNotDone(albums.get(currentIndex))) {
            auditService.setDone(albums.get(currentIndex));
        }

        // save all of the tracks in the current screen so the user doesn't have to manually do it
        Moose.frame.songController.saveTracks(IntStream.range(0, frame.table.getRowCount()).toArray());

        // prevent a negative index
        if (currentIndex >= 0) {
            // move on with the previous album in the list
            currentIndex--;
            auditService.openAuditWindow(albums, currentIndex);
        }
    }

    /**
     * Stops the audit
     */
    public void stopAudit() {
        albums.clear();
        Moose.frame.dispose();
        Moose.frame = new Frame();
        Moose.launchFrame();
    }

    /**
     * Finishes the audit
     */
    public void finishAudit() {

        // reset the audit frame
        auditFrame.resetAuditFrame();

        // clear done files
        auditService.clearDoneFiles(albums);

        // clear ivars
        albums.clear();
        currentIndex = 0;

        // launch a blank frame
        Moose.frame.dispose();
        Moose.frame = new Frame();
        Moose.launchFrame();

        // show that the audit is done
        DialogService.showMessageDialog(null, "Audit is complete!", "Audit Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Sets the ivars in the controller
     */
    public void setIvars(boolean id3Issues, boolean fileIssues, boolean coverIssues, String currentDirectory) {
        this.id3Issues = id3Issues;
        this.fileIssues = fileIssues;
        this.coverIssues = coverIssues;
        this.currentDirectory = currentDirectory;
    }

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

        // now that the auto fixes are done, reload the frame and set some ivars
        // if the directory changed name, update it in the filePathList so that we can keep track of how many albums we've gone through
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
        setIvars(results.get(ID3), results.get(FILENAMES), results.get(COVER), directory.getPath());
        auditFrame.refreshAuditFrame(results, directory.getPath());

        updateAuditResultCount();
    }

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
        auditFrame.updateConsole(auditService.getResultsFromFilePathList(filePathList));
    }

    public String getCurrentDirString(String currentDir) {
        return auditService.formatStringForCurrentlyScanningPath(currentDir);
    }

    public String getResults() {
        return auditService.exportAuditResultsToString(filePathList);
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
