
package moose;

public class Main {
    
    static Frame frame;
    static SettingsFrame settings;
    
    public static void main(String args[]) {
        launchFrame();
    }
    
    /**
     * MainUI
     * Controls the Frame opening
     */
    public static void launchFrame() {
        frame = new Frame();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    /**
     * SettingsFrame
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
    
    
}
