package com.mpfthprblmtq.moose.utilities;

import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MP3FileUtils {

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
     * Checks to see if the folder contains no mp3 files
     */
    public static boolean folderContainsNoMP3Files(File folder) {
        List<File> files = new ArrayList<>();
        FileUtils.listFiles(folder, files);

        int mp3Count = 0;
        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                mp3Count++;
            }
        }

        return mp3Count == 0;
    }

    /**
     * Gets the total number of tracks in a folder
     *
     * @param dir, the folder to check
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
     *
     * @param dir, the folder to check
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
}
