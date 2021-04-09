package moose.utilities;

import moose.Main;
import moose.utilities.logger.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    static Logger logger = Main.getLogger();

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

    private static BufferedImage resize(BufferedImage img, int dim) {
        Image tmp = img.getScaledInstance(dim, dim, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
}
