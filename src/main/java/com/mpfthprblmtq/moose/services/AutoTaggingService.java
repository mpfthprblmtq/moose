/*
 *  Proj:   Moose
 *  File:   AutoTaggingService.java
 *  Desc:   Service class for AutoTagging
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.services;

// imports
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.controllers.SongController;
import com.mpfthprblmtq.moose.objects.Settings;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.objects.api.imageSearch.ImageSearchQuery;
import com.mpfthprblmtq.moose.utilities.ImageUtils;
import com.mpfthprblmtq.moose.utilities.MP3FileUtils;
import com.mpfthprblmtq.moose.utilities.SongUtils;
import com.mpfthprblmtq.moose.utilities.viewUtils.DialogUtils;
import com.mpfthprblmtq.moose.views.modals.AlbumArtFinderFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mpfthprblmtq.moose.utilities.Constants.*;

public class AutoTaggingService {

    JTable table;
    Logger logger = Moose.getLogger();

    // controller
    SongController songController;

    // services
    SpotifyApiService spotifyApiService = new SpotifyApiService();

    public AutoTaggingService(SongController songController) {
        this.songController = songController;
    }

    /**
     * Sets the table
     * @param table the table to set
     */
    public void setTable(JTable table) {
        this.table = table;
    }

    /**
     * Function that actually does the autotagging
     *
     * @param rows, the row to update
     */
    public void autoTag(int[] rows) {

        for (int row : rows) {
            // get the file we'll use to determine track information
            Song s = songController.getSongs().get(songController.getIndex(row));
            File oldFile = null;
            File file = s.getFile();
            if (s.getNewFile() != null) {
                file = s.getNewFile();
                oldFile = s.getFile();
            }
            if (!file.getName().endsWith(".mp3")) {
                file = MP3FileUtils.getNewMP3FileFromOld(file, file.getName());
            }

            String title = getTitleFromFile(file);
            String artist = getArtistFromFile(file, oldFile);
            String album = getAlbumFromFile(file);
            String albumArtist = getAlbumArtistFromFile(file);
            String year = getYearFromFile(file);
            String tracks = getTracksFromFile(file);
            String disks = getDisksFromFile(file);
            String genre = StringUtils.isEmpty(s.getGenre()) ? getGenreFromFile(file) : s.getGenre();

            // get the index for the setters
            int index = s.getIndex();

            // title
            Moose.frame.songController.setTitle(index, title);
            table.setValueAt(title, row, TABLE_COLUMN_TITLE);

            // artist
            Moose.frame.songController.setArtist(index, artist);
            table.setValueAt(artist, row, TABLE_COLUMN_ARTIST);

            // album
            Moose.frame.songController.setAlbum(index, album);
            table.setValueAt(album, row, TABLE_COLUMN_ALBUM);

            // album artist
            Moose.frame.songController.setAlbumArtist(index, albumArtist);
            table.setValueAt(albumArtist, row, TABLE_COLUMN_ALBUM_ARTIST);

            // year
            Moose.frame.songController.setYear(index, year);
            table.setValueAt(year, row, TABLE_COLUMN_YEAR);

            // genre
            Moose.frame.songController.setGenre(index, genre);
            table.setValueAt(genre, row, TABLE_COLUMN_GENRE);

            // tracks
            String[] trackArr = tracks.split("/");
            if (trackArr.length == 2) {
                Moose.frame.songController.setTrack(index, trackArr[0]);
                Moose.frame.songController.setTotalTracks(index, trackArr[1]);
            } else {
                Moose.frame.songController.setTrack(index, StringUtils.EMPTY);
                Moose.frame.songController.setTotalTracks(index, StringUtils.EMPTY);
            }
            table.setValueAt(tracks, row, TABLE_COLUMN_TRACK);

            // disks
            String[] diskArr = disks.split("/");
            if (diskArr.length == 2) {
                Moose.frame.songController.setDisk(index, diskArr[0]);
                Moose.frame.songController.setTotalDisks(index, diskArr[1]);
            } else {
                Moose.frame.songController.setDisk(index, StringUtils.EMPTY);
                Moose.frame.songController.setTotalDisks(index, StringUtils.EMPTY);
            }
            table.setValueAt(disks, row, TABLE_COLUMN_DISK);

            // comment
            if (Moose.getSettings().getFeatures().get(Settings.REMOVE_COMMENT_ON_AUTOTAGGING)) {
                Moose.frame.songController.setComment(index, StringUtils.EMPTY);
            }
        }

        // album art
        autoAddCoverArt(rows);
    }

    /**
     * Attempts to add cover art for the rows
     *
     * @param selectedRows, the rows to update
     */
    public void autoAddCoverArt(int[] selectedRows) {

        List<Integer> rowsToReprocess = new ArrayList<>();

        // go through the list and add the covers that exist
        // if we can't find the cover art automatically, add the rows to a list so that we can reprocess them later
        // using the album art finder
        for (int selectedRow : selectedRows) {
            Song s = songController.getSongs().get(getIndex(selectedRow));
            File dir = s.getNewFile() != null ? s.getNewFile().getParentFile() : s.getFile().getParentFile();
            File cover = folderContainsCover(dir);
            // if we have a cover add it, else add the row to the rowsToReprocess list
            if (cover != null) {
                addIndividualCover(selectedRow, cover);
            } else {
                rowsToReprocess.add(selectedRow);
            }
        }

        // now we should determine if we need to use the album art finder for the rows we couldn't do automatically
        if (!rowsToReprocess.isEmpty()) {

            // let's use the album art finder
            List<ImageSearchQuery> queries = new ArrayList<>();
            for (Integer toReprocess : rowsToReprocess) {

                File file = songController.getSongs().get(songController.getIndex(toReprocess)).getNewFile();
                File oldFile = songController.getSongs().get(songController.getIndex(toReprocess)).getFile();

                // get the query to search on
                String artist = getArtistFromFile(file, oldFile);
                String album = getAlbumFromFileForArtwork(file);

                // get the parent directory to put the cover
                File dir = file.getParentFile();

                // add a ImageSearchQuery object to the list of queries
                if (!ImageSearchQuery.contains(queries, album)) {
                    ImageSearchQuery imageSearchQuery = new ImageSearchQuery(artist, album, dir, new ArrayList<>());
                    imageSearchQuery.getRows().add(toReprocess);
                    queries.add(imageSearchQuery);
                } else {
                    // if we already have the query included in the list, add the row to the rows to update
                    int index = ImageSearchQuery.getIndex(queries, album);
                    queries.get(index).getRows().add(toReprocess);
                }
            }

            // if the option is there to auto process with spotify
            if (Moose.getSettings().getFeatures().get(Settings.AUTO_FIND_COVER_ART_WITH_SPOTIFY)) {
                // go through the search queries one by one
                List<ImageSearchQuery> queriesToRemove = new ArrayList<>();
                for (ImageSearchQuery query : queries) {
                    // get the url
                    String url = spotifyApiService.getImage(query.getArtist(), query.getAlbum());
                    // if the url isn't empty, that means we found the cover art automatically
                    if (StringUtils.isNotEmpty(url)) {
                        // grab the image
                        BufferedImage image = ImageUtils.getImageFromUrl(url);
                        // create the physical file
                        int dim = Moose.getSettings().getPreferredCoverArtSize();
                        assert image != null;
                        File outputFile = ImageUtils.createImageFile(image, query.getDir(), dim);
                        assert outputFile != null;
                        // with that new file, auto add the cover art
                        if (outputFile.exists()) {
                            for (Integer row : query.getRows()) {
                                Moose.frame.songController.autoTaggingService.addIndividualCover(row, outputFile);
                            }
                        }
                        // update graphics
                        Moose.frame.updateMultiplePanelFields();
                        // add that query to a list so we can delete it later
                        queriesToRemove.add(query);
                    }
                }
                // remove that query from the list of queries in case we get rid of all of them
                queries.removeAll(queriesToRemove);
            }

            // if we still have some images to look for, use the album art finder frame for the rest of them
            if (!queries.isEmpty()) {
                if (Moose.getSettings().getFeatures().get(Settings.ALBUM_ART_FINDER)) {
                    // ask the user if they want to use the album art finder
                    int useService = confirmUserWantsAlbumArtFinder();
                    if (useService == JOptionPane.YES_OPTION) {

                        // get the queries and dirs to open the frames with
                        for (ImageSearchQuery query : queries) {
                            showAlbumArtWindow(query);
                        }
                    }
                }
            }
        }
    }

    /**
     * Function to add an album cover for just one row
     *
     * @param row, the row to add the cover on
     * @param cover, the cover to add to the file/table
     */
    public void addIndividualCover(int row, File cover) {
            // get the index of the track
            int index = getIndex(row);

            byte[] bytes = ImageUtils.getBytesFromFile(cover);

            // update the track in the songs array
            Moose.frame.songController.getSongs().get(index).setArtwork_bytes(bytes);

            // update graphics
            Icon thumbnail_icon = ImageUtils.getScaledImage(bytes, 100);

            // set the image on the row
            table.setValueAt(thumbnail_icon, row, TABLE_COLUMN_ALBUM_ART);

            // song was edited, add it to the list
            Moose.frame.songController.songEdited(index);

            // if there's multiple rows selected, also add it to the multiple fields panel
            if (table.getSelectedRowCount() > 1) {
                Icon artwork_icon = ImageUtils.getScaledImage(bytes, 150);
                Moose.frame.multImage.setIcon(artwork_icon);
            }
    }

    /**
     * Method for adding album art for songs manually
     * This method is called when the "Add" selection is pressed in the context menu
     *
     * @param selectedRows, the current selected rows on the table
     */
    public void addAlbumArt(int[] selectedRows) {

        // need this for some reason
        File file = null;

        for (int i = 0; i < selectedRows.length; i++) {

            // get the index of the track
            int index = getIndex(selectedRows[i]);

            // get the file to use as the starting point for choosing an image
            File startingPoint = Moose.frame.songController.getSongs().get(index).getFile();

            // only show the JFileChooser on the first go
            if (i == 0) {
                file = ImageUtils.selectAlbumArt(startingPoint);
                if (file == null) {
                    return;
                }
            }
            addIndividualCover(selectedRows[i], file);
        }
    }

    /**
     * Checks to see if user wants to use the Album Art Finder Service
     *
     * @return if the user wants to use the album art finder
     */
    public int confirmUserWantsAlbumArtFinder() {
        return JOptionPane.showConfirmDialog(
                Moose.frame,
                "Cover art wasn't automatically found, would you like\n"
                    + "to use the Album Art Finder service in Moose?",
                "Album Art Finder Service",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a window for the album art
     *
     * @param query, the ImageSearchQuery object to use in the album art finder window
     */
    public void showAlbumArtWindow(ImageSearchQuery query) {
        if (SwingUtilities.isEventDispatchThread()) {
            AlbumArtFinderFrame albumArtFinderFrame = new AlbumArtFinderFrame(query);
            albumArtFinderFrame.setLocationRelativeTo(Moose.frame);
            albumArtFinderFrame.setVisible(true);
        } else {
            SwingUtilities.invokeLater(() -> {
                AlbumArtFinderFrame albumArtFinderFrame = new AlbumArtFinderFrame(query);
                albumArtFinderFrame.setLocationRelativeTo(Moose.frame);
                albumArtFinderFrame.setVisible(true);
            });
        }
    }

    /**
     * Helper function to check and see if a directory has a cover image file
     *
     * @param folder, the folder to check
     * @return the cover file, or null if it doesn't exist
     */
    public File folderContainsCover(File folder) {

        // if the folder is a cd in a multi-cd album
        if (folder.getName().startsWith("CD")) {
            folder = folder.getParentFile();
        }

        // if the folder isn't an album or part of the label
        if (!folder.getName().matches(ALBUM_FOLDER_REGEX) && !MP3FileUtils.isPartOfALabel(folder)) {
            return null;
        }

        File[] files = folder.listFiles();      // get all the files
        List<File> images = new ArrayList<>();
        assert files != null;
        for (File file : files) {
            if (file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")) {
                images.add(file);
            }
            if (file.getName().equals("cover.png") || file.getName().equals("cover.jpg") || file.getName().equals("cover.jpeg")) {
                return file;
            }
        }

        // if we reach this point, an image file named cover.* wasn't found
        // now we check to see if a single image file exists
        if (images.size() == 1) {
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(images.get(0));
            } catch (IOException ex) {
                logger.logError("IOException while trying to reach buffered image: ".concat(images.get(0).getPath()));
            }
            BufferedImage newBufferedImage = null;
            if (bufferedImage != null) {
                // check to see if it is the same width/height, resize if not
                if (bufferedImage.getWidth() != bufferedImage.getHeight()) {
                    newBufferedImage = ImageUtils.resize(bufferedImage, Math.min(bufferedImage.getHeight(), bufferedImage.getWidth()));
                }
            }
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
        if (ImageUtils.checkIfSame(bytesList.get(0), bytesList.toArray(new byte[0][]))) {
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
        if (folder.getParentFile().getName().matches(ALBUM_FOLDER_REGEX)) {
            return folderContainsCover(folder.getParentFile());
        }

        // no cover files were found, returning null
        return null;
    }

    /**
     * Helper Function Gets the index at the specified row
     *
     * @param row, the row to search on
     * @return the index of that row
     */
    public int getIndex(int row) {
        row = table.convertRowIndexToModel(row);
        return Integer.parseInt(table.getModel().getValueAt(row, 12).toString());
    }

    /**
     * Gets the File object from the row
     *
     * @param row, the row to get the file from
     * @return the File from the row
     */
    public File getFile(int row) {
        return (File) table.getModel().getValueAt(table.convertRowIndexToModel(row), 1);
    }

    /**
     * Gets the title based on the filename
     *
     * @param file, the file to check
     * @return a string track title
     */
    public String getTitleFromFile(File file) {

        // regex objects
        Pattern pattern;
        Matcher matcher;

        // 01 Kasbo - Play Pretend (ft. Ourchives).mp3
        if (file.getName().matches(TRACKNUM_ARTIST_TITLE_REGEX)) {
            pattern = Pattern.compile(TRACKNUM_ARTIST_TITLE_REGEX);
            matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                return FileUtils.cleanFilenameForOSX(matcher.group("Title"));
            }
        }

        // 01 Play Pretend (ft. Ourchives).mp3
        if (file.getName().matches(TRACKNUM_TITLE_REGEX)) {
            pattern = Pattern.compile(TRACKNUM_TITLE_REGEX);
            matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                return FileUtils.cleanFilenameForOSX(matcher.group("Title"));
            }
        }

        // Play Pretend (ft. Ourchives)
        if (file.getName().matches(TITLE_REGEX)) {
            pattern = Pattern.compile(TITLE_REGEX);
            matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                return FileUtils.cleanFilenameForOSX(matcher.group("Title"));
            }
        }

        return file.getName().replace(".mp3", StringUtils.EMPTY);
    }

    /**
     * Gets the artist based on the file location
     *
     * @param file, the file to check
     * @return a string artist
     */
    public String getArtistFromFile(File file, File oldFile) {

        Song s = SongUtils.getSongFromFile(oldFile == null ? file : oldFile);
        if (s != null) {
            if (s.getNewFile() != null) {
                file = s.getNewFile();
            }
            if (StringUtils.isNotEmpty(s.getArtist())) {
                return s.getArtist();
            }
        }

        // regex objects
        Pattern pattern;
        Matcher matcher;

        // 01 Kasbo - Play Pretend (ft. Ourchives).mp3
        pattern = Pattern.compile(TRACKNUM_ARTIST_TITLE_REGEX);
        matcher = pattern.matcher(file.getName());
        if (matcher.find()) {
            return FileUtils.cleanFilenameForOSX(matcher.group("Artist"));
        }

        // Kasbo - Play Pretend (ft. Ourchives).mp3
        pattern = Pattern.compile(ARTIST_TITLE_REGEX);
        matcher = pattern.matcher(file.getName());
        if (matcher.find()) {
            return FileUtils.cleanFilenameForOSX(matcher.group("Artist"));
        }

        // [2021] Kasbo - Play Pretend (ft. Ourchives)
        pattern = Pattern.compile(YEAR_ARTIST_ALBUM_REGEX);
        matcher = pattern.matcher(file.getParentFile().getName());
        if (matcher.find()) {
            return FileUtils.cleanFilenameForOSX(matcher.group("Artist"));
        }

        // split the file path by the / character, then try to parse it on whatever it can find
        String[] arr = file.getPath().split("/");
        for (String folder : arr) {
            pattern = Pattern.compile(YEAR_ARTIST_ALBUM_REGEX);
            matcher = pattern.matcher(folder);
            if (matcher.find()) {
                return FileUtils.cleanFilenameForOSX(matcher.group("Artist"));
            }
        }

        return getAlbumArtistFromFile(file);
    }

    /**
     * Gets the artist from existing ID3 information
     */
    public String getArtistFromExistingID3Info(File file) {
        Song s = SongUtils.getSongFromFile(file);
        if (s != null) {
            return s.getArtist();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Gets the album based on the file location
     *
     * @param file, the file to check
     * @return a string album
     */
    public String getAlbumFromFile(File file) {

        // regex objects
        Pattern pattern;
        Matcher matcher;

        // check to see if the parent file is a part of a multiple CD
        if (file.getPath().matches(CD_FILEPATH_REGEX)) {
            file = file.getParentFile();
        }

        if (MP3FileUtils.isPartOfALabel(file)) {
            // for singles, the album should be the genre
            if (MP3FileUtils.isPartOfALabel(file, SINGLES)) {
                return file.getParentFile().getParentFile().getName();
            } else {
                pattern = Pattern.compile(YEAR_ARTIST_ALBUM_REGEX);
                matcher = pattern.matcher(file.getParentFile().getName());
                if (matcher.find()) {
                    return FileUtils.cleanFilenameForOSX(matcher.group("Album"));
                }

                pattern = Pattern.compile(YEAR_ALBUM_REGEX);
                matcher = pattern.matcher(file.getParentFile().getName());
                if (matcher.find()) {
                    return FileUtils.cleanFilenameForOSX(matcher.group("Album"));
                }
            }
        } else {
            pattern = Pattern.compile(YEAR_ALBUM_REGEX);
            matcher = pattern.matcher(file.getParentFile().getName());
            if (matcher.find()) {
                return FileUtils.cleanFilenameForOSX(matcher.group("Album"));
            }
        }
        return StringUtils.EMPTY;
    }

    public String getAlbumFromFileForArtwork(File file) {
        // regex objects
        Pattern pattern;
        Matcher matcher;

        // check to see if the parent file is a part of a multiple CD
        if (file.getPath().matches(CD_FILEPATH_REGEX)) {
            file = file.getParentFile();
        }

        if (MP3FileUtils.isPartOfALabel(file)) {
            if (MP3FileUtils.isPartOfALabel(file, SINGLES) || MP3FileUtils.isPartOfALabel(file, EPS) || MP3FileUtils.isPartOfALabel(file, LPS)) {
                if (file.getParentFile().getName().matches(YEAR_ARTIST_ALBUM_REGEX)) {
                    pattern = Pattern.compile(YEAR_ARTIST_ALBUM_REGEX);
                    matcher = pattern.matcher(file.getParentFile().getName());
                    if (matcher.find()) {
                        return FileUtils.cleanFilenameForOSX(matcher.group("Album"));
                    }
                }
            } else {
                if (file.getParentFile().getName().matches((YEAR_ALBUM_REGEX))) {
                    pattern = Pattern.compile(YEAR_ALBUM_REGEX);
                    matcher = pattern.matcher(file.getParentFile().getName());
                    if (matcher.find()) {
                        return FileUtils.cleanFilenameForOSX(matcher.group("Album"));
                    }
                }
            }
        } else {
            pattern = Pattern.compile(YEAR_ALBUM_REGEX);
            matcher = pattern.matcher(file.getParentFile().getName());
            if (matcher.find()) {
                return FileUtils.cleanFilenameForOSX(matcher.group("Album"));
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Gets the album artist based on the file location
     *
     * @param file, the file to check
     * @return a string album artist
     */
    public String getAlbumArtistFromFile(File file) {

        // Library/Label/Singles/Genre/[2021] Artist - Album/01 Title.mp3
        if (MP3FileUtils.isPartOfALabel(file, SINGLES)) {
            return FileUtils.cleanFilenameForOSX(file.getParentFile().getParentFile().getParentFile().getParentFile().getName());

        // Library/Label/Compilations/Compilation/01 Artist - Title.mp3
        // Library/Label/EPs/Album/01 Title.mp3
        // Library/Label/LPs/Album/CD2/01 Title.mp3
        } else if (MP3FileUtils.isPartOfALabel(file)) {
            if (file.getPath().matches(CD_FILEPATH_REGEX)) {
                return FileUtils.cleanFilenameForOSX(file.getParentFile().getParentFile().getParentFile().getParentFile().getName());
            }
            return FileUtils.cleanFilenameForOSX(file.getParentFile().getParentFile().getParentFile().getName());

        // Library/AlbumArtist/[2021] Album/01 Title.mp3
        } else {
            if (file.getPath().matches(CD_FILEPATH_REGEX)) {
                return FileUtils.cleanFilenameForOSX(file.getParentFile().getParentFile().getParentFile().getName());
            }
            return FileUtils.cleanFilenameForOSX(file.getParentFile().getParentFile().getName());
        }
    }

    /**
     * Gets the year based on the file location
     *
     * @param file, the file to check
     * @return a string year
     */
    public String getYearFromFile(File file) {

        // regex objects
        Pattern pattern;
        Matcher matcher;

        // go through the file's parent files and try to find the year
        String[] arr = file.getPath().split("/");
        for (String folder : arr) {
            pattern = Pattern.compile(YEAR_ARTIST_ALBUM_REGEX);
            matcher = pattern.matcher(folder);
            if (matcher.find()) {
                return matcher.group("Year");
            }

            pattern = Pattern.compile(YEAR_ALBUM_REGEX);
            matcher = pattern.matcher(folder);
            if (matcher.find()) {
                return matcher.group("Year");
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Gets the tracks based on the filename and other files in the directory
     *
     * @param file, the file to check
     * @return a string representation of tracks
     */
    public String getTracksFromFile(File file) {

        // get the total number of tracks from the parent directory
        File dir = file.getParentFile();
        String totalTracks = String.valueOf(MP3FileUtils.getNumberOfMP3Files(dir));

        // get the string representation of the track number
        String trackNumber = StringUtils.EMPTY;
        Pattern pattern = Pattern.compile(TRACKNUM_TITLE_REGEX);
        Matcher matcher = pattern.matcher(file.getName());
        if (matcher.find()) {
            trackNumber = matcher.group("TrackNumber");
        }

        // if we didn't get it, ask the user
        if (StringUtils.isEmpty(trackNumber)) {
            Song s = SongUtils.getSongFromFile(file);
            String title = file.getName().replace(".mp3", StringUtils.EMPTY);
            if (s != null && StringUtils.isNotEmpty(s.getTitle())) {
                title = s.getTitle();
            }
            String[] arr = DialogUtils.showGetTitleAndTrackNumberDialog(Moose.getFrame(), title);
            if (arr != null) {
                trackNumber = arr[1];
            }
        }

        // if we still didn't get it, return empty string to prevent /12 nonsense
        if (StringUtils.isEmpty(trackNumber)) {
            return StringUtils.EMPTY;
        }

        int parsedTrackNumber = Integer.parseInt(trackNumber);
        return parsedTrackNumber + "/" + totalTracks;
    }

    /**
     * Gets the disks from the file
     *
     * @param file, the file to check
     * @return a string representation of disks
     */
    public String getDisksFromFile(File file) {

        // if we have a multiple CD scenario
        if (file.getPath().replace(file.getName(), StringUtils.EMPTY).matches(CD_FILEPATH_REGEX)) {
            File cdDir = file.getParentFile();
            int cdNumber = Integer.parseInt(cdDir.getName().replace("CD", StringUtils.EMPTY));
            int totalDisks = MP3FileUtils.getTotalDisksFromFolder(cdDir.getParentFile());
            return cdNumber + "/" + totalDisks;
        } else {
            return "1/1";
        }
    }

    /**
     * Returns a genre String if the file is a single in a label, else return nothing
     *
     * @param file, the file to check
     * @return a genre string
     */
    public String getGenreFromFile(File file) {
        if (MP3FileUtils.isPartOfALabel(file, SINGLES)) {
            return FileUtils.cleanFilenameForOSX(file.getParentFile().getParentFile().getName());
        }
        return StringUtils.EMPTY;
    }

    /**
     * Function for adding the track numbers
     *
     * @param selectedRows, the rows selected on the table
     */
    public void addTrackAndDiskNumbers(int[] selectedRows) {
        for (int row : selectedRows) {
            File file = getFile(row);
            autoAddTrackAndDiskNumbers(row, file);
        }
    }

    /**
     * Function that actually sets the row info
     *
     * @param row, the row to add the track/disk numbers to
     * @param file, the file to get the information from
     */
    public void autoAddTrackAndDiskNumbers(int row, File file) {
        int index = getIndex(row);

        String tracks = getTracksFromFile(file);
        String disks = getDisksFromFile(file);

        // tracks
        Moose.frame.songController.setTrack(index, tracks);
        table.setValueAt(tracks, row, TABLE_COLUMN_TRACK);

        // disks
        Moose.frame.songController.setDisk(index, disks);
        table.setValueAt(disks, row, TABLE_COLUMN_DISK);
    }

}
