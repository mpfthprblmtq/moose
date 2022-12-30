/*
   Proj:   Moose
   File:   Main.java
   Desc:   Class that controls everything

   Copyright Pat Ripley 2018
 */

// package
package com.mpfthprblmtq.moose;

import java.io.File;

import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.moose.controllers.SettingsController;
import com.mpfthprblmtq.moose.controllers.SongController;
import com.mpfthprblmtq.moose.objects.Settings;
import com.mpfthprblmtq.moose.views.modals.AuditFrame;
import com.mpfthprblmtq.moose.views.Frame;
import com.mpfthprblmtq.moose.views.modals.SettingsFrame;

import javax.swing.*;

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
        // instantiate the settings object so we can have some log/settings files
        settingsController = new SettingsController();
        settingsFrame = new SettingsFrame(settingsController);
        
        // instantiate the logger object so we can have some logging
        // TODO make sure this works
        boolean developerMode = System.getProperty("java.class.path").contains("idea_rt.jar");
        logger = new Logger(System.getProperty("user.home") + "/Library/Application Support/Moose/", developerMode);

        // instantiate the main song controller object
        songController = new SongController();
        
        // go
        launchFrame();
//        launchFrame(new File("/Users/mpfthprblmtq/Music/Library - For Testing/bitbirb/Singles/Future Bass"));
    }

    /**
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
     * Returns the settings controller
     * @return the settings controller
     */
    public static SettingsController getSettingsController() {
        return settingsController;
    }

    /**
     * Sends a command to update the settings file with the settings object
     * @param settings the new settings to write
     */
    public static boolean updateSettings(Settings settings) {
        return settingsFrame.settingsController.writeSettingsFile(settings);
    }

    /**
     * Sends a command to update the seetings file with the settings we already have
     */
    public static void updateSettings() {
        settingsController.writeSettingsFile(getSettings());
    }

    /**
     * MainUI
     * Controls the Frame opening
     */
    public static void launchFrame() {
        songController = new SongController();
        frame = new Frame();
        frame.setLocation(20, 20);
        frame.setVisible(true);
    }
    
    /**
     * Controls the Frame opening
     * @param dir, the directory to launch with pre-populated in the table
     */
    public static void launchFrame(File dir) {
        songController = new SongController();
        frame = new Frame(dir);
        frame.setLocation(20, 20);
        frame.setVisible(true);
    }

    /**
     * Returns the main frame
     */
    public static Frame getFrame() {
        return frame;
    }

    /**
     * Returns the main frame table
     * @return the JTable on the Frame
     */
    public static JTable getTable() {
        return frame.table;
    }

    /**
     * Controls the SettingsFrame opening and closing
     */
    public static void launchSettingsFrame() {
        settingsFrame.setLocationRelativeTo(null);
        settingsFrame.setVisible(true);
    }
    
    /**
     * Controls the AuditFrame opening and closing
     */
    public static void launchAuditFrame() {
        auditFrame = new AuditFrame(frame, frame.songController);
        auditFrame.setLocation(frame.getX() + frame.getWidth() + 20, frame.getY());
        auditFrame.setVisible(true);
    }

    /**
     * Returns the auditFrame
     */
    public static AuditFrame getAuditFrame() {
        return auditFrame;
    }

}
