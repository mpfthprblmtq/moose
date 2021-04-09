package moose.utilities;

import java.io.File;

public class SongUtils {

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
}
