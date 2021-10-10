package moose.utilities;

import moose.Moose;
import moose.utilities.logger.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    static Logger logger = Moose.getLogger();

    /**
     * Helper Function that lists and stores all of the files in a directory and subdirectories
     * @param directory, the directory to list files from
     * @param files, the arrayList to store the files in
     */
    public static void listFiles(File directory, List<File> files) {

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
    }

    /**
     * Opens a file
     * @param file, the file to open
     */
    public static void openFile(File file) {
        Desktop desktop = Desktop.getDesktop();
        if (file.exists()) {
            try {
                desktop.open(file);
            } catch (IOException ex) {
                logger.logError("Couldn't open the file: " + file.getName(), ex);
            }
        } else {
            logger.logError("Tried to open file, but " + file.getName() + " doesn't exist!");
        }
    }

    /**
     * Opens the containing folder for the file
     * @param file the file to open
     */
    public static void showInFolder(File file) {
        String path = file.getPath().replace(file.getName(), StringUtils.EMPTY);
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(path));
            }
        } catch (IOException e) {
            logger.logError("IOException when opening containing folder of " + file.getPath());
        }
    }

    /**
     * Creates a new file with the same path, just a different name
     */
    public static File getNewMP3FileFromOld(File oldFile, String newFilename) {
        String path = oldFile.getPath().replace(oldFile.getName(), StringUtils.EMPTY);
        if (!newFilename.endsWith(".mp3")) {
            newFilename = newFilename.concat(".mp3");
        }
        return new File(path + newFilename);
    }

    /**
     * Checks to see if the folder only has one mp3 file in it
     */
    public static boolean folderContainsOnlyOneMP3(File folder) {
        List<File> files = new ArrayList<>();
        listFiles(folder, files);

        int mp3Count = 0;
        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                mp3Count++;
            }
        }

        return mp3Count == 1;
    }

    /**
     * Creates a JFileChooser, configures it, and launches it
     * Returns a single index array if there's only one file returned
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
        JFileChooser jfc = new JFileChooser();

        // configure it

        // if the parameter openAt is null, open the library location by default if its set
        // if it's not set, open the user.home
        if (openAt == null) {
            if (StringUtils.isEmpty(Moose.getSettings().getLibraryLocation())) {
                openAt = new File(Moose.getSettings().getLibraryLocation());
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
