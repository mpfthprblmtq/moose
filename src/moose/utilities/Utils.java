/*
   Proj:   Moose
   File:   Utils.java
   Desc:   Helper class with a smattering of methods to do some neat stuff

   Copyright Pat Ripley 2018
 */

package moose.utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.imageio.ImageIO;
import moose.Main;

public class Utils {

    // logger
    static Logger logger = Main.logger;

    // date formatter
    static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
    
    /**
     * Checks if int[] contains a certain int
     * 
     * @param arr, the array of ints to check
     * @param key, the key to check for
     * @return the result of the check
     */
    public static boolean intArrayContains(int[] arr, int key) {
        return Arrays.stream(arr).anyMatch(i -> i == key);
    }

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
     * Formats a Date object to a string
     *
     * @param date, the date to format
     * @return the formatted date
     */
    public static String formatDate(Date date) {
        return sdf.format(date);
    }

    /**
     * Gets a Date object from a string
     *
     * @param date the date string to parse
     * @return a date object
     */
    public static Date getDate(String date) {
        try {
            return sdf.parse(date);
        } catch (ParseException ex) {
            logger.logError("ParseException when parsing date \"" + date + "\"");
            return null;
        }
    }

    /**
     * Checks a string if it's empty or not
     *
     * @param str, the string to check
     * @return the result of the check
     */
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }
        return str.equals(Constants.EMPTY_STRING);
    }

    /**
     * Opens a webpage with the specified url
     *
     * @param url, the url to open
     */
    public static void openPage(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            logger.logError("Exception when trying to open the webpage: " + url, e);
        }
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
            logger.logError("NullPointerException while trying to scale an image!", e);
        }
        return thumbnail_icon;
    }

    /**
     * Returns a byte array from a BufferedImage
     *
     * @param image, the image to convert
     * @return the byte array representation of the image
     */
    public static byte[] getBytesFromBufferedImage(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            logger.logError("IOException when trying to read a BufferedImage to a byte array!", ex);
            return null;
        } catch (Exception ex) {
            System.out.println("what");
            return null;
        }
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
     *
     * @return the result of the check
     */
    public static boolean isLibraryLocationSet() {
        return !(Main.getSettings().getLibraryLocation() == null
                || Main.getSettings().getLibraryLocation().equals(""));
    }

    /**
     * Creates a JFileChooser, configures it, and launches it
     * Returns a single index array if there's only one file returned
     *
     * @param title, the title of the window
     * @param approveButtonText, the text to show on the approve button
     * @param selectionMode, the mode for selecting files
     * @param multipleSelection, a boolean for allowing multiple file selection in the window
     * @return a file array of the selected file(s)
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
        if (Utils.isLibraryLocationSet()) {
            library = new File(Main.getSettings().getLibraryLocation());
        } else {
            library = new File(System.getProperty("user.home"));
        }
        jfc.setCurrentDirectory(library);
        jfc.setDialogTitle(title);
        jfc.setMultiSelectionEnabled(multipleSelection);
        jfc.setFileSelectionMode(selectionMode);

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

    /**
     * Opens a file
     *
     * @param file, the file to open
     * @throws java.io.IOException if an exception is thrown while opening the file
     */
    public static void openFile(File file) throws IOException {
        Desktop desktop = Desktop.getDesktop();
        if (file.exists()) {
            desktop.open(file);
        } else {
            logger.logError("Tried to open file, but " + file.getName() + " doesn't exist!");
        }
    }
    
    /**
     * Creates a img file
     * @param img, the img file to create
     * @param dir, the directory of the file to create
     * @param dim, the dimension of the image (width and height)
     * @return the file created from the bufferedImage
     */
    public static File createImageFile(BufferedImage img, File dir, int dim) {
        String filePath = dir.getPath() + "/cover.jpg";
        File outputFile = new File(filePath);
        
        if(img.getWidth() != dim || img.getHeight() != dim) {
            img = resize(img, dim);
        }
        
        try {
            ImageIO.write(img, "jpg", outputFile);
        } catch (IOException ex) {
            logger.logError("IOException when trying to create cover file!  Path: " + filePath, ex);
            return null;
        }
        
        return outputFile;
    }
    
    private static BufferedImage resize(BufferedImage img, int dim) {
        Image tmp = img.getScaledInstance(dim, dim, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
}
