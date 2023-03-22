/*
 *  Proj:   Moose
 *  File:   MP3FileUtils.java
 *  Desc:   A utility class to pull out common logic from interacting with mp3 files.
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.utilities;

// imports
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.utilities.viewUtils.ViewUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mpfthprblmtq.moose.utilities.Constants.*;

// class MP3FileUtils
public class MP3FileUtils {

    static Logger logger = Moose.getLogger();

    /**
     * Gets all mp3 files and converts them to Song objects from a directory and its subdirectories
     * @param dir the directory of files to get the mp3s
     * @return a list of all mp3 files
     */
    public static List<Song> getAllSongsInDirectory(File dir) {
        List<Song> songs = new ArrayList<>();
        List<File> filesInDir = new ArrayList<>();
        FileUtils.listFiles(dir, filesInDir);
        filesInDir.removeIf(fileInDir -> !fileInDir.getName().endsWith(".mp3"));
        for (File fileInDir : filesInDir) {
            Song songFromFile = Moose.getSongController().getSongService().getSongFromFile(fileInDir);
            if (songFromFile != null) {
                songs.add(songFromFile);
            }
        }
        return songs;
    }

    /**
     * Check if a directory is from a label
     * @param dir the directory to check
     * @return the result of the check, true if it is a label, false if it isn't a label
     */
    public static boolean isPartOfALabel(File dir) {
        return (dir.getPath().contains("/" + SINGLES) ||
                dir.getPath().contains("/" + COMPILATIONS) ||
                dir.getPath().contains("/" + LPS) ||
                dir.getPath().contains("/" + EPS));
    }

    /**
     * Check if a directory is a single in a label
     * @param file the file to check
     * @param type the type of work to check explicitly for (Singles, LPs, EPs, Compilations)
     * @return the result of the check, true if it's a single from a label, false if it isn't
     */
    public static boolean isPartOfALabel(File file, String type) {
        return file.getPath().contains("/" + type + "/");
    }

    /**
     * Creates a new file with the same path, just a different name
     * @param oldFile the original file
     * @param newFilename the name to change the original file to
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
     * @param folder the directory to check
     * @return the result of the check, true if the folder only has one mp3 file in it, false if not
     */
    public static boolean folderContainsOnlyOneMP3(File folder) {
        List<File> files = new ArrayList<>();
        FileUtils.listFiles(folder, files);

        int mp3Count = 0;
        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                mp3Count++;
                if (mp3Count > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Gets the total number of tracks in a folder
     * @param dir the folder to check
     * @return an int count of mp3 files in a folder
     */
    public static int getNumberOfMP3Files(File dir) {
        File[] files = dir.listFiles();
        int count = 0;
        assert files != null;
        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the total disks from a folder
     * @param dir the folder to check
     * @return an int count of disks
     */
    public static int getTotalDisksFromFolder(File dir) {
        File[] dirs = dir.listFiles(File::isDirectory);
        int count = 0;
        assert dirs != null;
        for (File folder : dirs) {
            if (folder.getName().startsWith("CD")) {
                count++;
            }
        }
        return count;
    }

    /**
     * Helper function to check and see if a directory has a cover image file
     * @param folder the folder to check
     * @return the cover file, or null if it doesn't exist
     */
    public static File getCoverIfExists(File folder) {

        // if the folder is a cd in a multi-cd album
        if (folder.getPath().matches(FILENAME_MULTIPLE_CD_FILEPATH)) {
            folder = folder.getParentFile();
        }

        // if the folder isn't an album
        if (!folder.getName().matches(FILENAME_YEAR_ALBUM)) {
            return null;
        }

        // get list of files in this directory
        List<File> files = FileUtils.listFilesShallow(folder);

        // check to see if we have a "cover.*" file, or if there are any images in general
        List<File> images = new ArrayList<>();
        for (File file : files) {
            if (file.getName().equals("cover.png") || file.getName().equals("cover.jpg") || file.getName().equals("cover.jpeg")) {
                return file;
            }
            if (file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")) {
                images.add(file);
            }
        }

        // if we reach this point, an image file named cover.* wasn't found
        // now we check to see if a single image file exists
        if (images.size() == 1) {
            // try to get the image from the file
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(images.get(0));
            } catch (IOException ex) {
                logger.logError("IOException while trying to reach buffered image: ".concat(images.get(0).getPath()));
                ViewUtils.showErrorDialog("IOException while trying to reach buffered image: "
                        .concat(images.get(0).getPath()), ex, Moose.getFrame());
            }
            // do some processing on the image if we need to
            BufferedImage newBufferedImage = null;
            if (bufferedImage != null) {
                // check to see if it is the same width/height, resize if not
                if (bufferedImage.getWidth() != bufferedImage.getHeight()) {
                    newBufferedImage = ImageUtils.resize(bufferedImage, Math.min(bufferedImage.getHeight(), bufferedImage.getWidth()));
                }
            }
            // now that we have a bufferedImage, let's create the file
            File resultFile;
            if (newBufferedImage != null) {
                resultFile = ImageUtils.createImageFile(newBufferedImage, images.get(0).getParentFile(), newBufferedImage.getHeight());
                if (images.get(0).delete()) {
                    return resultFile;
                }
            } else {
                String parent = images.get(0).getParentFile().getPath();
                String filename = images.get(0).getName();
                String type = filename.substring(filename.lastIndexOf("."));
                resultFile = new File(parent.concat("/cover").concat(type));
                if (!images.get(0).renameTo(resultFile)) {
                    logger.logError("Error while renaming image file: ".concat(resultFile.getPath()));
                }
            }

            return resultFile;
        }

        // if we reach this point, an image file wasn't found at all, let's check all the files to see if they
        // share the same cover art, and grab the image if they do
        List<Song> songs = MP3FileUtils.getAllSongsInDirectory(folder);

        List<byte[]> bytesList = new ArrayList<>();
        for (Song song : songs) {
            bytesList.add(song.getArtwork_bytes());
        }
        if (ImageUtils.checkIfSame(bytesList.get(0), bytesList)) {
            byte[] bytes = bytesList.get(0);
            BufferedImage image = ImageUtils.getBufferedImageFromBytes(bytes);
            // check if it meets the size requirement first (both size wise, and dimension wise, shouldn't take a cover
            // with differing height and width
            if (image != null && image.getHeight() >= Moose.getSettings().getPreferredCoverArtSize() && image.getWidth() >= Moose.getSettings().getPreferredCoverArtSize()) {
                if (image.getWidth() == image.getHeight()) {
                    return ImageUtils.createImageFile(image, folder, image.getHeight());
                }
            }
        }

        // if we reach this point, no valid image files exist in that directory
        // perform one final check and recursively call itself
        if (folder.getParentFile().getName().matches(FILENAME_YEAR_ALBUM)) {
            return getCoverIfExists(folder.getParentFile());
        }

        // no cover files were found, returning null
        return null;
    }
}
