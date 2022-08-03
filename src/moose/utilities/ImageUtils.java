package moose.utilities;

import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.FileUtils;
import moose.Moose;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageUtils {

    static Logger logger = Moose.getLogger();

    /**
     * Method for getting the artwork you want to use
     */
    public static File selectAlbumArt(File startingPoint) {
        File[] files = FileUtils.launchJFileChooser(
                "Select an image to use",
                "Select",
                JFileChooser.FILES_ONLY,
                false,
                startingPoint,
                new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "tif"));
        if (files != null) {
            return files[0];
        }
        return null;
    }

    /**
     * Returns a byte array from a File
     * @param file, the file to read from
     * @return bytes, the byte array
     */
    public static byte[] getBytesFromFile(File file) {
        byte[] bytes = null;
        try (RandomAccessFile ra_file = new RandomAccessFile(file.getAbsolutePath(), "r")) {
            bytes = new byte[(int) ra_file.length()];
            ra_file.read(bytes);
        } catch (IOException ex) {
            logger.logError("Exception getting bytes from a file!", ex);
        }
        return bytes;
    }

    /**
     * Returns a BufferedImage from a url
     * @param imageUrl the url to get the image from
     * @return a buffered image
     */
    public static BufferedImage getImageFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            return ImageIO.read(url);
        } catch (IOException e) {
            logger.logError("Exception while getting an image from the url: " + imageUrl, e);
            return null;
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
        byte[] scaledBytes;

        if (bytes == null) {
            return null;
        }

        // create a bufferedImage to validate size against
        BufferedImage bi = getBufferedImageFromBytes(bytes);
        if (bi == null) {
            return null;
        }

        // if the dimensions aren't the same, get the non-stretched square version
        if (bi.getHeight() > bi.getWidth()) {
            // image is taller
            int diff = bi.getHeight() - bi.getWidth();
            bi = bi.getSubimage(0, diff / 2, bi.getWidth(), bi.getWidth());
            scaledBytes = getBytesFromBufferedImage(bi);
        } else if (bi.getHeight() < bi.getWidth()) {
            // image is wider
            int diff = bi.getWidth() - bi.getHeight();
            bi = bi.getSubimage(diff / 2, 0, bi.getHeight(), bi.getHeight());
            scaledBytes = getBytesFromBufferedImage(bi);
        } else {
            // image has equal width and height
            scaledBytes = bytes;
        }

        try {
            // getting the image from the byte array
            ImageIcon icon = new ImageIcon(scaledBytes);
            Image img = icon.getImage();

            // scaling down the image
            Image thumbnail = img.getScaledInstance(dim, dim, java.awt.Image.SCALE_SMOOTH);
            thumbnail_icon = new ImageIcon(thumbnail);

        } catch (NullPointerException e) {
            logger.logError("NullPointerException while trying to scale an image!", e);
        }
        return thumbnail_icon;
    }

    /**
     * Gets a circular image
     */
    public static Icon getCircularScaledImage(byte[] bytes, int dim) {
        Icon icon = getScaledImage(bytes, dim);
        if (icon == null) {
            return null;
        }

        BufferedImage bi = getBufferedImageFromBytes(getBytesFromImageIcon((ImageIcon) icon));
        if (bi == null) {
            return null;
        }

        BufferedImage out = new BufferedImage(bi.getWidth(), bi.getWidth(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillOval(0, 0, bi.getWidth(), bi.getWidth());
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(bi, 0, 0, null);
        g2.dispose();

        return new ImageIcon(out);
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
     * Returns a byte array from an ImageIcon
     * @param icon, the imageIcon to scan
     */
    public static byte[] getBytesFromImageIcon(ImageIcon icon) {
        // check if icon is blank first
        if (icon == null || (icon.getIconWidth() < 0 && icon.getIconHeight() < 0)) {
            return new byte[]{};
        }

        // icon isn't blank, let's create a buffered image and then some bytes
        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        icon.paintIcon(null, g, 0,0);
        g.dispose();
        return getBytesFromBufferedImage(bi);
    }

    /**
     * Returns a buffered image from a byte array
     * @param bytes, the bytes to read from
     */
    public static BufferedImage getBufferedImageFromBytes(byte[] bytes) {
        try {
            InputStream is = new ByteArrayInputStream(bytes);
            return ImageIO.read(is);
        } catch (IOException e) {
            logger.logError("Couldn't convert bytes to bufferedImage!");
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
    public static BufferedImage resize(BufferedImage img, int dim) {
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

        // return a blank image if any of the images are blank
        if (hasEmptyByteArray(images)) {
            BufferedImage blankImage = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = blankImage.createGraphics();
            g2.drawImage(blankImage, null, 0, 0);
            g2.dispose();
            return blankImage;
        }

        // let's throw the byte arrays into a buffered image list
        List<BufferedImage> bufferedImages = new ArrayList<>();
        for (byte[] bytes : images) {
            if (bytes.length != 0) {
                try {
                    InputStream is = new ByteArrayInputStream(bytes);
                    bufferedImages.add(resize(ImageIO.read(is), dim));  // resize them while we're at it
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
     * Checks a list of byte arrays to see if there's any empty byte arrays
     */
    public static boolean hasEmptyByteArray(List<byte[]> byteList) {
        for (byte[] bytes : byteList) {
            if (bytes.length == 0) {
                return true;
            }
        }
        return false;
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
