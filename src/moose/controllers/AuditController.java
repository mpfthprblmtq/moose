package moose.controllers;

import moose.Main;
import moose.services.AuditService;
import moose.services.DialogService;
import moose.utilities.AuditCleanupUtils;
import moose.utilities.Constants;
import moose.utilities.StringUtils;
import moose.views.AuditFrame;
import moose.views.Frame;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuditController {

    // service
    AuditService auditService;

    // frame
    AuditFrame auditFrame;

    // controller
    SongController songController;

    // some ivars
    File folder;
    int currentIndex;

    // list of albums for the audit
    List<File> albums = new ArrayList<>();

    // lists for results
    List<List<String>> auditFilePathList = new ArrayList<>(Arrays.asList(
            new ArrayList<>(),      // id3
            new ArrayList<>(),      // filenames
            new ArrayList<>()));    // cover art

    public AuditController(AuditFrame auditFrame, SongController songController) {
        this.auditFrame = auditFrame;
        this.auditService = new AuditService(this.auditFrame);
        this.songController = songController;
    }

    public String analyze() {
        AuditCleanupUtils.clearLists(auditFilePathList);
        albums = auditService.importAlbums(folder);
        return auditService.analyzeForAudit(albums, auditFilePathList);
    }

    /**
     * Checks if an audit is already in process first,
     * then user decides to either start a new audit or continue that existing audit
     */
    public void startAudit() {
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

        // save all of the tracks in the current screen so the user doesn't have to manually do it
        songController.saveAll();

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
        Main.frame.songController.saveAll();

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
        Main.frame.dispose();
        Main.frame = new Frame();
        Main.launchFrame();
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
        Main.frame.dispose();
        Main.frame = new Frame();
        Main.launchFrame();

        // show that the audit is done
        DialogService.showMessageDialog(null, "Audit is complete!", "Audit Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    public String getCurrentDirString(String currentDir) {
        return auditService.formatStringForCurrentlyScanningPath(currentDir);
    }

    public String getResults() {
        return auditService.exportAuditResultsToString(auditFilePathList);
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
