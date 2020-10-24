package moose.utilities;

import moose.Main;
import moose.utilities.logger.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtils {

    static Logger logger = Main.getLogger();

    /**
     * Helper Function that lists and stores all of the files in a directory and
     * subdirectories
     *
     * @param directory, the directory to list files from
     * @param files, the arrayList to store the files in
     * @return a list of all the files in the directory
     */
    public static ArrayList<File> listFiles(File directory, ArrayList<File> files) {

        // get all the files from a directory
        File[] fList = directory.listFiles();
        assert fList != null;
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
     * Opens a file
     *
     * @param file, the file to open
     */
    public static void openFile(File file) {
        Desktop desktop = Desktop.getDesktop();
        if (file.exists()) {
            try {
                desktop.open(file);
            } catch (IOException ex) {
                logger.logError("Couldn't open the event log!", ex);
            }
        } else {
            logger.logError("Tried to open file, but " + file.getName() + " doesn't exist!");
        }
    }

    /**
     * Creates a JFileChooser, configures it, and launches it
     * Returns a single index array if there's only one file returned
     *
     * @param title, the title of the window
     * @param approveButtonText, the text to show on the approve button
     * @param selectionMode, the mode for selecting files
     * @param multipleSelection, a boolean for allowing multiple file selection in the window
     * @param openAt, an optional-ish parameter to open the JFileChooser at a certain location
     * @param fileNameExtensionFilter, an optional-ish parameter to set the filter to look at files on
     * @return a file array of the selected file(s)
     */
    public static File[] launchJFileChooser(String title, String approveButtonText, int selectionMode, boolean multipleSelection, File openAt, FileNameExtensionFilter fileNameExtensionFilter) {

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

        // if the parameter openAt is null, open the library location by default if its set
        // if it's not set, open the user.home
        if (openAt == null) {
            if (StringUtils.isEmpty(Main.getSettings().getLibraryLocation())) {
                openAt = new File(Main.getSettings().getLibraryLocation());
            } else {
                openAt = new File(System.getProperty("user.home"));
            }
        }
        jfc.setCurrentDirectory(openAt);

        // some normal fields
        jfc.setDialogTitle(title);
        jfc.setMultiSelectionEnabled(multipleSelection);
        jfc.setFileSelectionMode(selectionMode);

        // filter the files based on the extensions
        if (fileNameExtensionFilter != null) {
            jfc.setFileFilter(fileNameExtensionFilter);
        }

        // launch it
        int returnVal = jfc.showDialog(null, approveButtonText);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (multipleSelection) {
                return jfc.getSelectedFiles();
            } else {
                return new File[]{jfc.getSelectedFile()};
            }
        } else {
            return null;
        }
    }

}
