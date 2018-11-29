/**
 *  Proj:   Moose
 *  File:   Main.java
 *  Desc:   Class that controls everything
 *
 *  Copyright Pat Ripley 2018
 */

// package
package moose;

// class Main
public class Main {
    
    // version
    public static String version = "1.1.0";
    
    // create and instantiate the frames and controllers
    static Frame frame = new Frame();
    static Logger logger = new Logger();
    static SettingsFrame settings = new SettingsFrame();
    static AuditFrame auditFrame = new AuditFrame();

    /**
     * Entry point for the app, launches the main Frame
     * @param args
     */
    public static void main(String args[]) {
        launchFrame();
    }
    
    /**
     * MainUI
     * Controls the Frame opening
     */
    public static void launchFrame() {
        // TODO: Find out why this doesn't work anymore
        //frame.setLocationRelativeTo(null); 
        frame.setLocation(100, 100);
        frame.setVisible(true);
    }
    
    /**
     * SettingsFrame
     * Controls the SettingsFrame opening and closing
     */
    public static void launchSettingsFrame() {
        settings.setLocationRelativeTo(null);
        settings.setVisible(true);
    }
    
    public static void closeSettingsFrame() {
        settings.dispose();
    }
    
    /**
     * AuditFrame
     * Controls the AuditFrame opening and closing
     */
    public static void launchAuditFrame() {
        auditFrame.setLocation(frame.getX() + frame.getWidth() + 20, frame.getY());
        auditFrame.setVisible(true);
    }
    
    public static void closeAuditFrame() {
        auditFrame.dispose();
    }
    
}
