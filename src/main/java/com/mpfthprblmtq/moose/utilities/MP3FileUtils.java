package com.mpfthprblmtq.moose.utilities;

import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.objects.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.mpfthprblmtq.moose.utilities.Constants.*;

public class MP3FileUtils {

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
            Song songFromFile = SongUtils.getSongFromFile(fileInDir);
            if (songFromFile != null) {
                songs.add(songFromFile);
            }
        }
        return songs;
    }

    /**
     * Check if a directory is from a label
     * @param dir, the directory to check
     * @return the result of the check, true if it is a label, false if it isn't
     * a label
     */
    public static boolean isPartOfALabel(File dir) {
        return (dir.getPath().contains("/" + SINGLES) ||
                dir.getPath().contains("/" + COMPILATIONS) ||
                dir.getPath().contains("/" + LPS) ||
                dir.getPath().contains("/" + EPS));
    }

    /**
     * Check if a directory is a single in a label
     * @param file, the file to check
     * @return the result of the check, true if it's a single from a label, false if it isn't
     */
    public static boolean isPartOfALabel(File file, String type) {
        return file.getPath().contains("/" + type + "/");
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
