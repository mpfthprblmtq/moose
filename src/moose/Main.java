/**
 *  Proj:   Moose
 *  File:   Main.java
 *  Desc:   Class that controls everything
 *
 *  Copyright Pat Ripley 2018
 */

// package
package moose;

import java.io.File;
import moose.utilities.Logger;
import moose.views.AuditFrame;
import moose.views.Frame;
import moose.views.SettingsFrame;

// class Main
public class Main {
    
    // version
    public static String version = "1.1.1";
    
    // create and instantiate the frames
    public static Frame frame;
    public static SettingsFrame settings;
    public static AuditFrame auditFrame;

    // logger object
    public static Logger logger = new Logger();

    /**
     * Entry point for the app, launches the main Frame
     * @param args
     */
    public static void main(String args[]) {
        // instantiate the settings object so we can have some log/settings files
        settings = new SettingsFrame();
        
        // go
        launchFrame();
    }

    /**
     * Returns the logger object
     * @return the logger object
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * MainUI
     * Controls the Frame opening
     */
    public static void launchFrame() {
        frame = new Frame();
        // TODO: Find out why this doesn't work anymore
        //frame.setLocationRelativeTo(null); 
        frame.setLocation(100, 100);
        frame.setVisible(true);
    }
    
    /**
     * Controls the Frame opening
     * @param dir
     */
    public static void launchFrame(File dir) {
        frame = new Frame(dir);
        // TODO: Find out why this doesn't work anymore
        //frame.setLocationRelativeTo(null); 
        frame.setLocation(100, 100);
        frame.setVisible(true);
    }
    
    /**
     * Controls the SettingsFrame opening and closing
     */
    public static void launchSettingsFrame() {
        settings = new SettingsFrame();
        settings.setLocationRelativeTo(null);
        settings.setVisible(true);
    }
    
    public static void closeSettingsFrame() {
        settings.dispose();
    }
    
    /**
     * Controls the AuditFrame opening and closing
     */
    public static void launchAuditFrame() {
        auditFrame = new AuditFrame();
        auditFrame.setLocation(frame.getX() + frame.getWidth() + 20, frame.getY());
        auditFrame.setVisible(true);
    }
    
    public static void closeAuditFrame() {
        auditFrame.dispose();
    }
    
}
