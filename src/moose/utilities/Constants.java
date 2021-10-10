package moose.utilities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String DASH = "-";

    public static final String LARGE = "large";
    public static final String XLARGE = "xlarge";
    public static final int IMAGE_LIMIT = 100;
    
    public static final Color RED = new Color(130, 0, 50);
    public static final Color GREEN = new Color(0, 130, 50);
    public static final Color BLACK = new Color(0, 0, 0);

    public static final String MOOSE_WIKI = "https://www.github.com/mpfthprblmtq/moose/wiki";

    public static final String TRACK_FILENAME_REGEX = "\\d{2} ((.)*).mp3";
    public static final String TRACK_NUMBER_REGEX = "\\d{2}";
    public static final String ALBUM_FOLDER_REGEX = "\\[\\d{4}\\] .*";

    public static final List<String> REGEX_ARRAY = new ArrayList<>(Arrays.asList(
            "\\d{2}\\. .* - .*\\.mp3",  // 01. Kasbo - The Making of a Paracosm.mp3
            "\\d{2} - .* - .*\\.mp3",   // 01 - Kasbo - The Making of a Paracosm.mp3
            "\\d{2}\\. .*\\.mp3",       // 01. The Making of a Paracosm.mp3
            "\\d{2} .* - .*\\.mp3",     // 02 Kasbo - Play Pretend (ft. Ourchives).mp3
            "\\d{2} - .*\\.mp3",        // 02 - Play Pretend (ft. Ourchives).mp3
            "[^\\d{2}].* - .*\\.mp3",    // Kasbo - Play Pretend (ft. Ourchives).mp3
            "[^\\d{2}].*\\.mp3"         // Play Pretend (ft. Ourchives).mp3
    ));

    public static final int UNDETERMINED = -1;
    public static final int YES = 0;
    public static final int NO = 1;
    public static final int SAVED = 2;

    // some constants to make life easier
    public static final int DEFAULT = 0;
    public static final int EDITED = 1;

    public static final int SHIFT_TAB = 0;
    public static final int SHIFT_ENTER = 1;
    public static final int TAB = 2;
    public static final int ENTER = 3;

    public static final int FROM_DIALOG = 1;
    public static final int NORMAL_NAV = 2;

    // column constants
    public static final int TABLE_COLUMN_TITLE = 2;
    public static final int TABLE_COLUMN_ARTIST = 3;
    public static final int TABLE_COLUMN_ALBUM = 4;
    public static final int TABLE_COLUMN_ALBUMARTIST = 5;
    public static final int TABLE_COLUMN_YEAR = 6;
    public static final int TABLE_COLUMN_GENRE = 7;
    public static final int TABLE_COLUMN_TRACK = 8;
    public static final int TABLE_COLUMN_DISK = 9;
    public static final int TABLE_COLUMN_ALBUMART = 10;

    // audit constants
    public static final int AUDIT = 0;
    public static final int CLEANUP = 1;

    public static final int ID3 = 0;
    public static final int FILENAMES = 1;
    public static final int COVER = 2;

    public static final int MP3ASD = 0;
    public static final int FLAC = 1;
    public static final int WAV = 2;
    public static final int ZIP = 3;
    public static final int IMG = 4;
    public static final int WINDOWS = 5;
    public static final int OTHER = 6;

    public static final int GENRE = 0;
    public static final int LOGGING = 1;
    public static final int FILES = 2;
    public static final int API = 3;
    public static final int FEATURES = 4;
}
