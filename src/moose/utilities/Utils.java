package moose.utilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import moose.Main;

public class Utils {

    static Logger logger = Main.logger;

    /**
     * Helper Function that lists and stores all of the files in a directory and
     * subdirectories
     *
     * @param directory
     * @param files
     * @return
     */
    public static ArrayList<File> listFiles(File directory, ArrayList<File> files) {
        
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listFiles(file, files);     // this file is a directory, recursively call itself
            }
        }
        return files;
    }

    /**
     * Gets the scaled instance of album art
     *
     * @param bytes, the album art in a byte array type
     * @param dim, the dimension of the image
     * @return the scaled instance of the image
     */
    public static Icon getScaledImage(byte[] bytes, int dim) {
        Icon thumbnail_icon = null;
        try {
            // getting the image from the byte array
            ImageIcon icon = new ImageIcon(bytes);
            Image img = icon.getImage();

            // scaling down the image to put on the row
            Image thumbnail = img.getScaledInstance(dim, dim, java.awt.Image.SCALE_SMOOTH);
            thumbnail_icon = new ImageIcon(thumbnail);

        } catch (NullPointerException e) {
            System.err.println(e);
        }
        return thumbnail_icon;
    }

    /**
     * Check if a directory is from a label
     *
     * @param dir, the directory to check
     * @return the result of the check, true if it is a label, false if it isn't
     * a label
     */
    public static boolean isPartOfALabel(File dir) {
        String path = dir.getPath();
        return (path.contains("/Genres/") || path.contains("/EPs/") || path.contains("/EP's/"));
    }
    
    public static boolean isAnEPPartOfALabel(File dir) {
        String path = dir.getPath();
        return (path.contains("/EPs/") || path.contains("/EP's/"));
    }
    
    public static boolean isAGenrePartOfALabel(File dir) {
        String path = dir.getPath();
        return path.contains("/Genres/");
    }
    
    /**
     * Checks if the libraryLocation is set
     * @return the result of the check
     */
    public static boolean isLibraryLocationSet() {
        return !(Main.settings.settingsController.getLibraryLocation() == null
                || Main.settings.settingsController.getLibraryLocation().equals(""));
    }
    
    /**
     * Creates a JFileChooser, configures it, and launches it
     * Returns a single index array if there's only one file returned
     * 
     * @param title
     * @param approveButtonText
     * @param selectionMode
     * @param multipleSelection
     * @return 
     */
    public static File[] launchJFileChooser(String title, String approveButtonText, int selectionMode, boolean multipleSelection) {
        
        // create it
        JFileChooser jfc = new JFileChooser() {
            // overriding to prevent a user selecting nothing inside a directory
            @Override
            public void approveSelection() {
                File file = this.getSelectedFile();
                if (file.isDirectory()) {
                    super.approveSelection();
                }
            }
        };
        
        // configure it
        File library;
        if(Utils.isLibraryLocationSet()) {
            library = new File(Main.settings.settingsController.getLibraryLocation());
        } else {
            library = new File(System.getProperty("user.home"));
        }
        jfc.setCurrentDirectory(library);
        jfc.setDialogTitle(title);
        jfc.setMultiSelectionEnabled(multipleSelection);
        jfc.setFileSelectionMode(selectionMode);
        
        // launch it
        int returnVal = jfc.showDialog(null, approveButtonText);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            if(multipleSelection) {
                return jfc.getSelectedFiles();
            } else {
                return new File[] { jfc.getSelectedFile() };
            }
        } else {
            return null;
        }
    }  
    
    /**
     * Opens a file
     * @param file, the file to open
     * @throws java.io.IOException
     */
    public static void openFile(File file) throws IOException {
            Desktop desktop = Desktop.getDesktop();
            if (file.exists()) {
                desktop.open(file);
            } else {
                logger.logError("Tried to open file, but " + file.getName() + " doesn't exist!");
            }
    }
}
