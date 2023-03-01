package com.mpfthprblmtq.moose.services;

import com.mpfthprblmtq.commons.utils.CollectionUtils;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.RegexUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.controllers.SongController;
import com.mpfthprblmtq.moose.objects.Settings;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.objects.api.imageSearch.ImageSearchQuery;
import com.mpfthprblmtq.moose.utilities.ImageUtils;
import com.mpfthprblmtq.moose.utilities.MP3FileUtils;
import com.mpfthprblmtq.moose.utilities.viewUtils.DialogUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.mpfthprblmtq.moose.utilities.Constants.*;

@Data
@NoArgsConstructor
public class AutoTaggingService {

    // controllers
    SongController songController;

    // services
    SpotifyApiService spotifyApiService = new SpotifyApiService();

    public AutoTaggingService(SongController songController) {
        this.songController = songController;
    }

    public void autoTag(List<Song> songs) {
        for (Song song : songs) {

            String title = getTitleFromFile(song.getFile());
            String artist = getArtistFromFile(song.getFile());
            String album = getAlbumFromFile(song.getFile());
            String albumArtist = getAlbumArtistFromFile(song.getFile());
            String year = getYearFromFile(song.getFile());
            String genre = getGenreFromFile(song);

            // get row and index for the setters
            int index = song.getIndex();
            int row = Moose.getSongController().getRow(index);

            // title
            Moose.getSongController().setTitle(index, title);
            Moose.getFrame().getTable().setValueAt(title, row, TABLE_COLUMN_TITLE);

            // artist
            Moose.getSongController().setArtist(index, artist);
            Moose.getFrame().getTable().setValueAt(artist, row, TABLE_COLUMN_ARTIST);

            // album
            Moose.getSongController().setAlbum(index, album);
            Moose.getFrame().getTable().setValueAt(album, row, TABLE_COLUMN_ALBUM);

            // album artist
            Moose.getSongController().setAlbumArtist(index, albumArtist);
            Moose.getFrame().getTable().setValueAt(albumArtist, row, TABLE_COLUMN_ALBUM_ARTIST);

            // year
            Moose.getSongController().setYear(index, year);
            Moose.getFrame().getTable().setValueAt(year, row, TABLE_COLUMN_YEAR);

            // genre
            Moose.getSongController().setGenre(index, genre);
            Moose.getFrame().getTable().setValueAt(genre, row, TABLE_COLUMN_GENRE);
        }

        // tracks and disk numbers
        autoAddTrackAndDiskNumbers(songs);

        // album art
        autoAddCoverArt(songs);
    }

    /**
     * Gets the title from the filename after doing some processing
     * @param file the file to check
     * @return the title
     */
    public String getTitleFromFile(File file) {
        if (file.getName().matches(FILENAME_TRACK_NUMBER_ARTIST_TITLE)) {
            return FileUtils.cleanFilenameForOSX(
                            RegexUtils.getMatchedGroup(file.getName(), FILENAME_TRACK_NUMBER_ARTIST_TITLE, "title"))
                    .replace(".mp3", StringUtils.EMPTY);
        } else if (file.getName().matches(FILENAME_TRACK_NUMBER_TITLE)) {
            return FileUtils.cleanFilenameForOSX(
                            RegexUtils.getMatchedGroup(file.getName(), FILENAME_TRACK_NUMBER_TITLE, "title"))
                    .replace(".mp3", StringUtils.EMPTY);
        } else {
            return file.getName().replace(".mp3", StringUtils.EMPTY);
        }
    }

    /**
     * Gets the artist from the filename or the file location
     * @param file the file to check
     * @return the artist
     */
    public String getArtistFromFile(File file) {
        // check if we already have an artist from the filename processing
        Song song = Moose.getSongController().getSongs().get(Moose.getSongController().getIndex(file));
        if (StringUtils.isNotEmpty(song.getArtist())) {
            return song.getArtist();
        }

        // check the parent file for the artist
        if (file.getParentFile().getName().matches(FILENAME_YEAR_ARTIST_ALBUM)) {
            return FileUtils.cleanFilenameForOSX(
                    RegexUtils.getMatchedGroup(
                            file.getParentFile().getName(), FILENAME_YEAR_ARTIST_ALBUM, "artist"));
        }

        // hail mary attempt at getting any filename that matches the year, artist, and album regex
        String[] arr = file.getPath().split("/");
        for (String folder : arr) {
            if (folder.matches(FILENAME_YEAR_ARTIST_ALBUM)) {
                return FileUtils.cleanFilenameForOSX(
                        RegexUtils.getMatchedGroup(folder, FILENAME_YEAR_ARTIST_ALBUM, "artist"));
            }
        }

        // we're at this point, so it's probably an album artist that we can use
        return getAlbumArtistFromFile(file);
    }

    /**
     * Gets the album from the filename or the file location
     * @param file the file to check
     * @return the album
     */
    public String getAlbumFromFile(File file) {

        // check to see if album is part of a multiple CD collection (CD1, CD2, etc.)
        if (file.getPath().matches(FILENAME_MULTIPLE_CD_FILEPATH)) {
            file = file.getParentFile();
        }

        // check to see if we're in a label
        if (MP3FileUtils.isPartOfALabel(file)) {
            // for singles, the album should be the genre
            if (MP3FileUtils.isPartOfALabel(file, SINGLES)) {
                return file.getParentFile().getParentFile().getName();
            }

            // not a single, so get the album from the EP/LP/Compilation title
            if (file.getName().matches(FILENAME_YEAR_ARTIST_ALBUM)) {
                return FileUtils.cleanFilenameForOSX(
                        RegexUtils.getMatchedGroup(file.getName(), FILENAME_YEAR_ARTIST_ALBUM, "album"));
            } else if (file.getName().matches(FILENAME_YEAR_ALBUM)) {
                return FileUtils.cleanFilenameForOSX(
                        RegexUtils.getMatchedGroup(file.getName(), FILENAME_YEAR_ALBUM, "album"));
            }
        } else {
            // not in a label, get the album normally
            if (file.getParentFile().getName().matches(FILENAME_YEAR_ALBUM)) {
                return FileUtils.cleanFilenameForOSX(
                        RegexUtils.getMatchedGroup(file.getParentFile().getName(), FILENAME_YEAR_ALBUM, "title"));
            }
        }

        // album wasn't found using our method, just return blank
        return StringUtils.EMPTY;
    }

    /**
     * Gets the album artist from the filename or the file location.  Performs some additional checks for label files,
     * since that file structure is different from normal.
     * @param file the file to check
     * @return the album artist
     */
    public String getAlbumArtistFromFile(File file) {

        // Library/Label/Singles/Genre/[2021] Artist - Album/01 Title.mp3
        if (MP3FileUtils.isPartOfALabel(file, SINGLES)) {
            return FileUtils.cleanFilenameForOSX(file.getParentFile().getParentFile().getParentFile().getParentFile().getName());

        } else if (MP3FileUtils.isPartOfALabel(file)) {
            // Library/Label/Compilations/Compilation/01 Artist - Title.mp3
            // Library/Label/EPs/Album/01 Title.mp3
            // Library/Label/LPs/Album/CD2/01 Title.mp3
            if (file.getPath().matches(FILENAME_MULTIPLE_CD_FILEPATH)) {
                return FileUtils.cleanFilenameForOSX(file.getParentFile().getParentFile().getParentFile().getParentFile().getName());
            }
            return FileUtils.cleanFilenameForOSX(file.getParentFile().getParentFile().getParentFile().getName());

        } else {
            // Library/AlbumArtist/[2021] Album/01 Title.mp3
            if (file.getPath().matches(FILENAME_MULTIPLE_CD_FILEPATH)) {
                return FileUtils.cleanFilenameForOSX(file.getParentFile().getParentFile().getParentFile().getName());
            }
            return FileUtils.cleanFilenameForOSX(file.getParentFile().getParentFile().getName());
        }
    }

    /**
     * Gets the year from the file location.  Mostly looks at the parent directory to grab the year if it matches the
     * year regex
     * @param file the file to check
     * @return the year
     */
    public String getYearFromFile(File file) {
        // check to see if we're in a multi disk album
        if (file.getPath().matches(FILENAME_MULTIPLE_CD_FILEPATH)) {
            file = file.getParentFile();
        }

        // grab the year from the parent file
        // this regex covers both [XXXX] Artist - Album and [XXXX] Album
        if (file.getParentFile().getName().matches(FILENAME_YEAR_ALBUM)) {
            return RegexUtils.getMatchedGroup(file.getParentFile().getName(), FILENAME_YEAR_ALBUM, "year");
        }
        return StringUtils.EMPTY;
    }

    /**
     * Gets the tracks from the file location.  Looks at the track number on the file first (which we should have got
     * from the FilenameFormatterService call), then looks at the total number of mp3 files in the parent folder
     * @param file the file to check
     * @return the tracks string in X/XX format
     */
    public String getTracksFromFile(File file) {
        // get the total number of tracks from the parent directory
        File dir = file.getParentFile();
        String totalTracks = String.valueOf(MP3FileUtils.getNumberOfMP3Files(dir));

        // get the track number for the file
        String track = StringUtils.EMPTY;
        if (file.getName().matches(FILENAME_TRACK_NUMBER_TITLE)) {
            track = RegexUtils.getMatchedGroup(file.getName(), FILENAME_TRACK_NUMBER_TITLE, "track");
        }

        // if we don't have the track, then the user didn't want to input it earlier, so don't set it
        // but just return empty string to prevent returning something like "/12"
        if (StringUtils.isEmpty(track)) {
            return StringUtils.EMPTY;
        }

        // parse the int, so we get "1" instead of "01"
        int parsedTrack = Integer.parseInt(track);
        return parsedTrack + "/" + totalTracks;
    }

    /**
     * Gets the disks from the file location.  Looks at the file path to determine if it's a multi-disk album
     * @param file the file to check
     * @return the disks string in X/XX format
     */
    public String getDisksFromFile(File file) {
        // check for multi disk album
        if (file.getPath().matches(FILENAME_MULTIPLE_CD_FILEPATH)) {
            int disk = Integer.parseInt(
                    RegexUtils.getMatchedGroup(
                            file.getParentFile().getPath(), FILENAME_MULTIPLE_CD_FILEPATH, "diskNumber"));
            int totalDisks = MP3FileUtils.getTotalDisksFromFolder(file.getParentFile());
            return disk + "/" + totalDisks;
        } else {
            // not a multi disk album, so just return 1/1
            return "1/1";
        }
    }

    /**
     * Gets the genre from the file.  For genres, the only indicator that we have for genre is if it's not blank,
     * and it's a Single in a label, then assign that genre.  Otherwise, just use whatever genre we have.
     * @param song the Song object to check for genre
     * @return the disks string in X/XX format
     */
    public String getGenreFromFile(Song song) {
        // check to see if we're a single in a label
        if (MP3FileUtils.isPartOfALabel(song.getFile(), SINGLES)) {
            return FileUtils.cleanFilenameForOSX(song.getFile().getParentFile().getParentFile().getName());
        }

        // fall back on the genre already set
        if (StringUtils.isNotEmpty(song.getGenre())) {
            return song.getGenre();
        }

        // get the common genre from the artist to assume its genre
        return getCommonGenreForArtist(song.getFile());
    }

    /**
     * Helper method that gets the most occurring genre for the artist
     * @param file the file to check
     * @return the most common genre from that song's artist
     */
    private String getCommonGenreForArtist(File file) {
        // don't care about labels for now
        if (MP3FileUtils.isPartOfALabel(file)) {
            return StringUtils.EMPTY;
        }

        // song is not in a label, let's look at the artist
        if (file.getPath().matches(FILENAME_MULTIPLE_CD_FILEPATH)) {
            file = file.getParentFile();
        }

        // grab the album artist directory, so we can take a peek at all the files under it
        File artistDirectory = file.getParentFile().getParentFile();
        List<File> files = FileUtils.listFiles(artistDirectory);

        // create a map where we can count the occurrences of the genre
        Map<String, Integer> genreCounts = new HashMap<>();

        // traverse the list of files
        for (File fileInList : files) {
            // only care about mp3 files
            if (fileInList.getName().endsWith(".mp3")) {
                // grab the song's genre
                Song song = Moose.getSongController().getSongService().getSongFromFile(fileInList);
                if (StringUtils.isNotEmpty(song.getGenre())) {
                    // place the genre count in the map if it doesn't exist, else increment the count
                    if (!genreCounts.containsKey(song.getGenre())) {
                        // we don't have the genre in the map yet, insert it with an initial value of 1
                        genreCounts.put(song.getGenre(), 1);
                    } else {
                        // we have the genre in the map already, increment count by 1
                        genreCounts.put(song.getGenre(), genreCounts.get(song.getGenre()) + 1);
                    }
                }
            }
        }

        // now let's look and see what the most common genre is
        return Objects.requireNonNull(
                genreCounts.entrySet()
                        .stream()
                        .max(Comparator.comparingInt(Map.Entry::getValue))
                        .orElse(null)).getKey();
    }

    /**
     * Adds the cover art for the given songs
     * @param songs the list of songs to add cover art
     */
    public void autoAddCoverArt(List<Song> songs) {

        // create a new list of songs for reprocessing if we can't find the image automatically
        List<Song> songsToReprocess = new ArrayList<>();

        // go through the songs and try and get the cover automatically
        for (Song song : songs) {
            File cover = MP3FileUtils.getCoverIfExists(song.getFile().getParentFile());
            if (cover != null) {
                addCoverForFile(song, cover);
            } else {
                songsToReprocess.add(song);
            }
        }

        // now we should determine if we want to use the album art finder for the rows that didn't work automatically
        if (CollectionUtils.isNotEmpty(songsToReprocess)) {
            // build the search queries with the songs we need to reprocess
            List<ImageSearchQuery> queries = new ArrayList<>();
            for (Song song : songsToReprocess) {
                if (!ImageSearchQuery.contains(queries, song.getAlbum())) {
                    // we don't have this query in the list, so let's create one and add the row to the query's rows
                    ImageSearchQuery query = new ImageSearchQuery(
                            song.getArtist(), song.getAlbum(), song.getFile().getParentFile(), new ArrayList<>());
                    query.getRows().add(Moose.getSongController().getRow(song.getIndex()));
                    queries.add(query);
                } else {
                    // if we already have the query included in the list, add the row to the rows to update
                    queries.get(ImageSearchQuery.getIndex(queries, song.getAlbum()))
                            .getRows().add(Moose.getSongController().getRow(song.getIndex()));
                }
            }

            // check to see if we can just automatically process with Spotify
            if (Moose.getSettings().getFeatures().get(Settings.AUTO_FIND_COVER_ART_WITH_SPOTIFY)) {
                // go through the search queries one by one
                List<ImageSearchQuery> queriesToRemove = new ArrayList<>();
                for (ImageSearchQuery query : queries) {
                    // get the url of the image
                    String url = spotifyApiService.getImage(query.getArtist(), query.getAlbum());
                    // if the url isn't empty, that means we found the cover art automatically
                    if (StringUtils.isNotEmpty(url)) {
                        // grab the image
                        BufferedImage image = ImageUtils.getImageFromUrl(url);
                        // create the physical file
                        File outputFile = image == null ? null : ImageUtils.createImageFile(
                                image, query.getDir(), Moose.getSettings().getPreferredCoverArtSize());
                        // with that new file, auto add the cover art
                        if (outputFile != null && outputFile.exists()) {
                            for (Integer row : query.getRows()) {
                                Song song = Moose.getSongController().getSongs().get(
                                        Moose.getSongController().getIndex(row));
                                addCoverForFile(song, outputFile);
                            }
                        }

                        // update graphics
                        Moose.getFrame().updateMultiplePanelFields();
                        // add that successfully processed query to a list for deletion later
                        queriesToRemove.add(query);
                    }
                }
                // remove all queries that were processed successfully
                queries.removeAll(queriesToRemove);
            }

            // if we still have some images to look for, use the album art finder frame for the rest of them
            if (CollectionUtils.isNotEmpty(queries)) {
                if (Moose.getSettings().getFeatures().get(Settings.ALBUM_ART_FINDER)) {
                    // ask the user if they want to use the album art finder
                    int useService = DialogUtils.confirmUserWantsAlbumArtFinder();
                    if (useService == JOptionPane.YES_OPTION) {
                        // get the queries and dirs to open the frames with
                        for (ImageSearchQuery query : queries) {
                            DialogUtils.showAlbumArtWindow(query);
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper method that sets the cover image for the given song.  Also update graphics on the Frame and table.
     * @param song the Song to add the cover on
     * @param cover the cover file to add
     */
    public void addCoverForFile(Song song, File cover) {
        // get the byte array format of the cover file
        byte[] bytes = ImageUtils.getBytesFromFile(cover);

        // update the song in the song controller
        Moose.getSongController().setAlbumImage(song.getIndex(), bytes);

        // update graphics
        Moose.getFrame().getTable().setValueAt(
                ImageUtils.getScaledImage(bytes, 100),
                Moose.getSongController().getRow(song.getIndex()), TABLE_COLUMN_ALBUM_ART);
    }

    /**
     * Method that only sets the track and disk number info
     * @param songs the list of songs to set the information on
     */
    public void autoAddTrackAndDiskNumbers(List<Song> songs) {

        // traverse the song list and set each row with the changes
        for (Song song : songs) {
            // get the row so we can update the table
            int row = Moose.getSongController().getRow(song.getIndex());

            // get the tracks and disks
            String tracks = getTracksFromFile(song.getFile());
            String disks = getDisksFromFile(song.getFile());

            // track
            String[] trackArr = tracks.split("/");
            if (trackArr.length == 2) {
                Moose.getSongController().setTrack(song.getIndex(), trackArr[0]);
                Moose.getSongController().setTotalTracks(song.getIndex(), trackArr[1]);
                Moose.getFrame().getTable().setValueAt(tracks, row, TABLE_COLUMN_TRACK);
            } else {
                Moose.getSongController().setTrack(song.getIndex(), StringUtils.EMPTY);
                Moose.getSongController().setTotalTracks(song.getIndex(), StringUtils.EMPTY);
            }

            // disk
            String[] diskArr = disks.split("/");
            if (diskArr.length == 2) {
                Moose.getSongController().setDisk(song.getIndex(), diskArr[0]);
                Moose.getSongController().setTotalDisks(song.getIndex(), diskArr[1]);
                Moose.getFrame().getTable().setValueAt(disks, row, TABLE_COLUMN_DISK);
            } else {
                Moose.getSongController().setDisk(song.getIndex(), StringUtils.EMPTY);
                Moose.getSongController().setTotalDisks(song.getIndex(), StringUtils.EMPTY);
            }
        }
    }

    /**
     * Gets the artist from existing ID3 information
     * @param file the file with the ID3 information to check
     * @return the artist if found, else a blank string
     */
    public String getArtistFromExistingID3Info(File file) {
        Song s = Moose.getSongController().getSongService().getSongFromFile(file);
        if (s != null) {
            return s.getArtist();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Sets the artist on the song object.  Called from the FilenameFormatterService when cleaning up filename.
     * @param file the file to get data from
     * @param artist the artist to set
     */
    public void setArtistOnSong(File file, String artist) {
        int index = Moose.getSongController().getIndex(file);
        if (index != -1) {
            Moose.getSongController().setArtist(index, artist);
        }
    }
}
