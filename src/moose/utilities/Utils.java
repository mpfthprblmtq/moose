package moose.utilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class Utils {

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
            Image thumbnail = img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
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
        return path.contains("/Genres/");
    }
}
