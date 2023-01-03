/*
 *  Proj:   Moose
 *  File:   Main.java
 *  Desc:   Entry point for the application, controls all the frames throughout the app and serves as the host
 *          for most of the application support objects (settings, logger, etc.).
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

// package
package com.mpfthprblmtq.moose;

// imports
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.moose.controllers.SettingsController;
import com.mpfthprblmtq.moose.controllers.SongController;
import com.mpfthprblmtq.moose.objects.Settings;
import com.mpfthprblmtq.moose.views.modals.AuditFrame;
import com.mpfthprblmtq.moose.views.Frame;
import com.mpfthprblmtq.moose.views.modals.SettingsFrame;

import java.io.File;

// class Main
public class Moose {
    
    // create and instantiate the frames
    public static Frame frame;
    public static SettingsFrame settingsFrame;
    public static AuditFrame auditFrame;

    // logger object
    public static Logger logger;

    // controllers
    public static SongController songController;
    public static SettingsController settingsController;

    /**
     * Entry point for the app, launches the main Frame
     * @param args, the entry arguments
     */
    public static void main(String[] args) {

        // initialize settings
        initSettings();

        // instantiate the main song controller object
        songController = new SongController();

        // go
        launchFrame();
    }

    /**
     * Helper method that initializes settings for the application. Sets up the logger, sets up the support directory,
     * and loads up the application settings.
     */
    private static void initSettings() {
        // instantiate the logger object so we can have some logging
        boolean developerMode = System.getProperty("java.class.path").contains("idea_rt.jar");
        logger = new Logger(System.getProperty("user.home") + "/Library/Application Support/Moose/", developerMode);

        // instantiate the settings object, so we can have some log/settings files
        settingsController = new SettingsController();

        // set up support directory if it's not set up already
        settingsController.setUpSupportDirectory();

        // initially load the settings
        settingsController.readSettingsFile();

        // create the frame now that we have our settings
        settingsFrame = new SettingsFrame();
    }

    /**
     * Returns the songController
     * @return the songController
     */
    public static SongController getSongController() {
        return songController;
    }

    /**
     * Returns the logger object
     * @return the logger object
     */
    public static Logger getLogger() {
        return logger;
    }
    
    /**
     * Returns the settings object
     * @return the settings object
     */
    public static Settings getSettings() {
        return settingsController.getSettings();
    }

    /**
     * Returns the settingsController
     * @return the settingsController
     */
    public static SettingsController getSettingsController() {
        return settingsController;
    }

    /**
     * Sends a command to update the settings file with the settings object we pass in
     * @param settings the new settings to write
     */
    public static boolean updateSettings(Settings settings) {
        return settingsFrame.settingsController.writeSettingsFile(settings);
    }

    /**
     * Sends a command to update the settings file with the settings object in the SettingsController
     */
    public static void updateSettings() {
        settingsController.writeSettingsFile(getSettings());
    }

    /**
     * Launches a new blank Frame
     */
    public static void launchFrame() {
        songController = new SongController();
        frame = new Frame();
        frame.setLocation(20, 20);
        frame.setVisible(true);
    }
    
    /**
     * Launches a new Frame with an album preloaded
     * @param dir the directory to launch with pre-populated in the table
     */
    public static void launchFrame(File dir) {
        songController = new SongController();
        frame = new Frame(dir);
        frame.setLocation(20, 20);
        frame.setVisible(true);
    }

    /**
     * Returns the main Frame
     * @return the main Frame object
     */
    public static Frame getFrame() {
        return frame;
    }

    /**
     * Launches a new SettingsFrame
     */
    public static void launchSettingsFrame() {
        settingsFrame.setLocationRelativeTo(null);
        settingsFrame.setVisible(true);
    }
    
    /**
     * Launches a new AuditFrame
     */
    public static void launchAuditFrame() {
        auditFrame = new AuditFrame();
        auditFrame.setLocation(frame.getX() + frame.getWidth() + 20, frame.getY());
        auditFrame.setVisible(true);
    }

    /**
     * Returns the AuditFrame
     * @return the AuditFrame object
     */
    public static AuditFrame getAuditFrame() {
        return auditFrame;
    }
}