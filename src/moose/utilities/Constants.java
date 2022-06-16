package moose.utilities;

import java.awt.Color;

public class Constants {

    public static final String DASH = "-";

    public static final String LARGE = "large";
    public static final String XLARGE = "xlarge";
    public static final int IMAGE_LIMIT = 100;
    
    public static final Color RED = new Color(130, 0, 50);
    public static final Color GREEN = new Color(0, 130, 50);
    public static final Color BLACK = new Color(0, 0, 0);

    public static final String MOOSE_WIKI = "https://www.github.com/mpfthprblmtq/moose/wiki";

    public static final String SINGLES = "Singles";
    public static final String COMPILATIONS = "Compilations";
    public static final String LPS = "LPs";
    public static final String EPS = "EPs";
    public static final String SINGLES_FILEPATH_REGEX = ".*/.*/Singles/.*/\\[\\d{4}\\] .* - .*/\\d{1,3} .*\\.mp3";
    public static final String COMPILATIONS_FILEPATH_REGEX = ".*/.*/Compilations/\\[\\d{4}\\] .*/\\d{1,3} .* - .*\\.mp3";
    public static final String LPS_FILEPATH_REGEX = ".*/.*/LPs/\\[\\d{4}\\] .* - .*/\\d{1,3} .*\\.mp3";
    public static final String EPS_FILEPATH_REGEX = ".*/.*/EPs/\\[\\d{4}\\] .* - .*/\\d{1,3} .*\\.mp3";
    public static final String GENERAL_FILEPATH_REGEX = ".*/.*/\\[\\d{4}\\] .*/\\d{1,3} .*\\.mp3";

    public static final String ALBUM_FOLDER_REGEX = "\\[\\d{4}\\] .*";

    public static final String TRACK_DISK_REGEX = "\\d{1,3}\\/\\d{1,3}";
    public static final String FILENAME_PRECHECK_REGEX = "\\d{1,3}.*";
    public static final String FILENAME_TRIM_REGEX = "(?<Title>\\w.*).*";
    public static final String FILENAME_REGEX = "(?<TrackNumber>\\d{1,3})[\\.\\s-]*(?<Title>.*)";
    public static final String TITLE_REGEX = "(?<Title>.*).mp3";
    public static final String TRACKNUM_TITLE_REGEX = "(?<TrackNumber>\\d{1,3}) (?<Title>.*).mp3";
    public static final String ARTIST_TITLE_REGEX = "(?<Artist>.*) - (?<Title>.*).mp3";
    public static final String YEAR_ARTIST_ALBUM_REGEX = "\\[(?<Year>\\d{4})\\] (?<Artist>.*) - (?<Album>.*)";
    public static final String YEAR_ALBUM_REGEX = "\\[(?<Year>\\d{4})\\] (?<Album>.*)";
    public static final String TRACKNUM_ARTIST_TITLE_REGEX = "(?<TrackNumber>\\d{1,3}) (?<Artist>.*) - (?<Title>.*).mp3";
    public static final String TRACKNUM_ARTIST_TITLE_OPT_REGEX = "(\\d{1,2} )*(?<Artist>.*) - (?<Title>.*).mp3";
    public static final String CD_FILEPATH_REGEX = ".*\\/(?<CDNumber>CD\\d+)\\.*";
    public static final String[] FILENAME_STRINGS_TO_REMOVE = new String[] {
            "LYRIC VIDEO",
            "OFFICIAL LYRIC VIDEO",
            "OFFICIAL MUSIC VIDEO",
            "LYRICS",
            "MONSTERCAT RELEASE",
            "MONSTERCAT LYRIC VIDEO",
    };

    // menu options
    public static final String MORE_INFO = "More info...";
    public static final String SHOW_IN_FINDER = "Show in Finder...";
    public static final String REMOVE_FROM_LIST = "Remove from list";
    public static final String PLAY = "Play";
    public static final String SAVE = "Save";
    public static final String AUTO_TAG = "Autotag";
    public static final String AUTO_TRACK_DISK_NUMBERS = "Auto-add track/disk numbers";
    public static final String AUTO_ARTWORK = "Auto-add artwork";
    public static final String MOVE_FILE = "Move file(s)";
    public static final String FORMAT_FILENAME = "Format filename(s)";
    public static final String ADD_ARTWORK = "Add artwork...";
    public static final String ADD_ARTWORK_SELECTED = "Add artwork for selected...";
    public static final String REMOVE_ARTWORK = "Remove artwork";
    public static final String REMOVE_ARTWORK_SELECTED = "Remove artwork for selected";

    public static final int DEFAULT = 0;
    public static final int EDITED = 1;
    public static final int SAVED = 2;

    // column constants
    public static final int TABLE_COLUMN_ICON = 0;
    public static final int TABLE_COLUMN_FILENAME = 1;
    public static final int TABLE_COLUMN_TITLE = 2;
    public static final int TABLE_COLUMN_ARTIST = 3;
    public static final int TABLE_COLUMN_ALBUM = 4;
    public static final int TABLE_COLUMN_ALBUM_ARTIST = 5;
    public static final int TABLE_COLUMN_YEAR = 6;
    public static final int TABLE_COLUMN_GENRE = 7;
    public static final int TABLE_COLUMN_TRACK = 8;
    public static final int TABLE_COLUMN_DISK = 9;
    public static final int TABLE_COLUMN_ALBUM_ART = 10;

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

    // settings tabs
    public static final int GENRE = 0;
    public static final int LOGGING = 1;
    public static final int FILES = 2;
    public static final int API = 3;
    public static final int FEATURES = 4;
}
