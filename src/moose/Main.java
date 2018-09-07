
package moose;

public class Main {
    
    // create and instantiate the frames and controllers
    static Frame frame = new Frame();
    static Logger logger = new Logger();
    static SettingsFrame settings = new SettingsFrame();
    
    
    public static void main(String args[]) {
        launchFrame();
    }
    
    /**
     * MainUI
     * Controls the Frame opening
     */
    public static void launchFrame() {
        frame.setLocationRelativeTo(null);
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
    
//    public ArrayList<String> getGenreList() {
//        //ArrayList<String> list = new ArrayList<>();
//        return settings.getGenreList();
//    }
}
