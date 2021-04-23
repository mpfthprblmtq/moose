package moose.utilities;

import moose.Moose;
import moose.utilities.logger.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageUtils {

    static Logger logger = Moose.getLogger();

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

    /**
     * Resizes a BufferedImage
     * @param img the buffered image to resize
     * @param dim the x/y of the image
     * @return a resized image
     */
    private static BufferedImage resize(BufferedImage img, int dim) {
        Image tmp = img.getScaledInstance(dim, dim, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    /**
     * Combines BufferedImages (from byte arrays) by cascading them for use in the mult panel
     * @param images the images to combine
     * @param dim the x/y dimension of the image
     * @return a combined BufferedImage
     */
    public static BufferedImage combineImages(List<byte[]> images, int dim) {

        // let's throw the byte arrays into a buffered image list
        List<BufferedImage> bufferedImages = new ArrayList<>();
        for (byte[] bytes : images) {
            if (bytes.length != 0) {
                try {
                    InputStream is = new ByteArrayInputStream(bytes);
                    bufferedImages.add(resize(ImageIO.read(is), 150));  // resize them while we're at it
                } catch (IOException e) {
                    logger.logError("Exception when adding buffered images to a list from a list of bytes arrays!", e);
                }
            }
        }

        // now let's combine those images
        BufferedImage combinedImage = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = combinedImage.createGraphics();
        int offsetInterval = dim / (bufferedImages.size() + 2);
        int offset = 0;
        g2.drawImage(bufferedImages.get(0), null, offset, offset);
        for (int i = 1; i < bufferedImages.size(); i++) {
            offset += offsetInterval;
            g2.drawImage(bufferedImages.get(i), null, offset, offset);
        }
        g2.dispose();
        return combinedImage;
    }

    /**
     * Utility function to get a unique list of byte arrays
     * @param bytesList the list to check for unique values
     * @return a unique list of byte arrays
     */
    public static List<byte[]> getUniqueByteArrays(List<byte[]> bytesList) {
        List<byte[]> uniqueList = new ArrayList<>();
        for (byte[] bytes : bytesList) {
            if (listDoesntContainsByteArray(uniqueList, bytes)) {
                uniqueList.add(bytes);
            }
        }
        return uniqueList;
    }

    /**
     * Utility function to check if a byte array exists in a list of byte arrays since we have to use Arrays.equals()
     * @param bytesList the list to check
     * @param bytes the bytes to compare the list values against
     * @return the result of the check
     */
    private static boolean listDoesntContainsByteArray(List<byte[]> bytesList, byte[] bytes) {
        for (byte[] bytesInList : bytesList) {
            if (Arrays.equals(bytesInList, bytes)) {
                return false;
            }
        }
        return true;
    }



    /**
     * Checks if a byte array is the same throughout an array
     * @param bytes, the byte array to check
     * @param arr,   the array of byte arrays
     * @return the result of the check
     */
    public static boolean checkIfSame(byte[] bytes, byte[][] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (!Arrays.equals(arr[i], bytes)) {
                return false;
            }
        }
        return true;
    }
}
