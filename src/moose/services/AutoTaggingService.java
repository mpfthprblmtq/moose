package moose.services;

import moose.Main;
import moose.objects.ImageSearchQuery;
import moose.utilities.Constants;
import moose.utilities.Logger;
import moose.utilities.Utils;
import moose.views.modals.AlbumArtFinderFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AutoTaggingService {

    AlbumArtFinderService albumArtFinderService;
    JTable table = Main.frame.table;
    Logger logger = Main.getLogger();

    // column constants
    private static final int TABLE_COLUMN_TITLE = 2;
    private static final int TABLE_COLUMN_ARTIST = 3;
    private static final int TABLE_COLUMN_ALBUM = 4;
    private static final int TABLE_COLUMN_ALBUMARTIST = 5;
    private static final int TABLE_COLUMN_YEAR = 6;
    private static final int TABLE_COLUMN_GENRE = 7;
    private static final int TABLE_COLUMN_TRACK = 8;
    private static final int TABLE_COLUMN_DISK = 9;
    private static final int TABLE_COLUMN_ALBUMART = 10;

    /**
     * Function that actually does the autotagging
     *
     * @param rows, the row to update
     */
    public void autoTag(int[] rows) {

        for (int row : rows) {
            // get the file we'll use to determine track information
            File file = getFile(row);

            String title = getTitleFromFile(file);
            String artist = getArtistFromFile(file);
            String album = getAlbumFromFile(file);
            String albumArtist = getAlbumArtistFromFile(file);
            String year = getYearFromFile(file);
            String tracks = getTracksFromFolder(file);
            String disks = getDisksFromFile(file);
            String genre = getGenreFromFile(file);

            // get the index for the setters
            int index = getIndex(row);

            // title
            Main.frame.songController.setTitle(index, title);
            table.setValueAt(title, row, TABLE_COLUMN_TITLE);

            // artist
            if (!Utils.isPartOfALabel(file)) {
                Main.frame.songController.setArtist(index, artist);
                table.setValueAt(artist, row, TABLE_COLUMN_ARTIST);
            }

            // album
            Main.frame.songController.setAlbum(index, album);
            table.setValueAt(album, row, TABLE_COLUMN_ALBUM);

            // album artist
            Main.frame.songController.setAlbumArtist(index, albumArtist);
            table.setValueAt(albumArtist, row, TABLE_COLUMN_ALBUMARTIST);

            // year
            Main.frame.songController.setYear(index, year);
            table.setValueAt(year, row, TABLE_COLUMN_YEAR);

            // genre
            if (Utils.isPartOfALabel(file)) {
                Main.frame.songController.setGenre(index, genre);
                table.setValueAt(genre, row, TABLE_COLUMN_GENRE);
            }

            // tracks
            Main.frame.songController.setTrack(index, tracks);
            table.setValueAt(tracks, row, TABLE_COLUMN_TRACK);

            // disks
            Main.frame.songController.setDisk(index, disks);
            table.setValueAt(disks, row, TABLE_COLUMN_DISK);
        }

        // album art
        autoAddCoverArt(rows);
    }

    public void autoAddCoverArt(int[] selectedRows) {

        List<Integer> rowsToReprocess = new ArrayList<>();

        // go through the list and add the covers that exist
        for (int i = 0; i < selectedRows.length; i++) {
            File dir = getFile(selectedRows[i]).getParentFile();
            File cover = folderContainsCover(dir);
            if (cover != null) {
                addIndividualCover(selectedRows[i], cover);
            } else {
                rowsToReprocess.add(selectedRows[i]);
            }
        }

        // now we should determine if we need to use the album art finder
        if (!rowsToReprocess.isEmpty()) {
            int useService = confirmUserWantsAlbumArtFinder();
            if (useService == Constants.YES) {
                List<ImageSearchQuery> queries = new ArrayList<>();
                for (int i = 0; i < rowsToReprocess.size(); i++) {
                    File dir = getFile(rowsToReprocess.get(i)).getParentFile();
                    String query = getArtistAndAlbumFromDirectory(dir);
                    if (!ImageSearchQuery.contains(queries, query)) {
                        ImageSearchQuery imageSearchQuery = new ImageSearchQuery(query, dir, new ArrayList<>());
                        imageSearchQuery.getRows().add(rowsToReprocess.get(i));
                        queries.add(imageSearchQuery);
                    } else {
                        int index = ImageSearchQuery.getIndex(queries, query);
                        queries.get(index).getRows().add(rowsToReprocess.get(i));
                    }
                }

                // get the queries and dirs to open the frames with
                for (int i = 0; i < queries.size(); i++) {
                    showAlbumArtWindow(queries.get(i));
                }
            }
        }
    }

    /**
     * Function to add an album cover for just one row
     *
     * @param row
     * @param cover
     */
    public void addIndividualCover(int row, File cover) {
        try {
            // get the index of the track
            int index = getIndex(row);

            // convert file to byte array
            byte[] bytes;
            try (RandomAccessFile ra_file = new RandomAccessFile(cover.getAbsolutePath(), "r")) {
                bytes = new byte[(int) ra_file.length()];
                ra_file.read(bytes);
            }

            // update the track in the songs array
            Main.frame.songController.getSongs().get(index).setArtwork_bytes(bytes);

            // update graphics
            Icon thumbnail_icon = Utils.getScaledImage(bytes, 100);

            // set the image on the row
            table.setValueAt(thumbnail_icon, row, TABLE_COLUMN_ALBUMART);

            // song was edited, add it to the list
            Main.frame.songController.songEdited(index);

            // if there's multiple rows selected, also add it to the multiple fields panel
            if (table.getSelectedRowCount() > 1) {
                Icon artwork_icon = Utils.getScaledImage(bytes, 150);
                Main.frame.multImage.setIcon(artwork_icon);
            }

        } catch (IOException ex) {
            logger.logError("Exception adding individual cover!", ex);
        }
    }

    /**
     * Method for adding album art for songs manually
     * This method is called when the "Add" selection is pressed in the context menu
     *
     * @param selectedRows
     */
    public void addAlbumArt(int[] selectedRows) {

        // need this for some reason
        File img_file = null;

        for (int i = 0; i < selectedRows.length; i++) {

            // get the row and index of the track
            int row = table.convertRowIndexToModel(selectedRows[i]);
            int index = Integer.valueOf(table.getModel().getValueAt(row, 12).toString());

            // get the file to use as the starting point for choosing an image
            File file = Main.frame.songController.getSongs().get(index).getFile();

            // only show the JFileChooser on the first go
            if (i == 0) {
                JFileChooser fc = new JFileChooser(new File(file.getAbsolutePath()));
                fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "tif"));

                // result of the file choosing
                int returnVal = fc.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    img_file = fc.getSelectedFile();
                } else {
                    return;
                }
            }
            addIndividualCover(row, img_file);
        }
    }

    /**
     * Checks to see if user wants to use the Album Art Finder Service
     *
     * @return
     */
    public int confirmUserWantsAlbumArtFinder() {
        return JOptionPane.showConfirmDialog(
                Main.frame,
                "Cover art wasn't automatically found, would you like\n"
                    + "to use the Album Art Finder service in Moose?",
                "Album Art Finder Service",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a window for the album art
     *
     * @param query
     */
    public void showAlbumArtWindow(ImageSearchQuery query) {
        if (SwingUtilities.isEventDispatchThread()) {
            AlbumArtFinderFrame albumArtFinderFrame = new AlbumArtFinderFrame(query);
            albumArtFinderFrame.setLocationRelativeTo(Main.frame);
            albumArtFinderFrame.setVisible(true);
        } else {
            SwingUtilities.invokeLater(() -> {
                AlbumArtFinderFrame albumArtFinderFrame = new AlbumArtFinderFrame(query);
                albumArtFinderFrame.setLocationRelativeTo(Main.frame);
                albumArtFinderFrame.setVisible(true);
            });
        }
    }

    /**
     * Gets the artist and album from a directory
     *
     * @param dir
     * @return
     */
    public String getArtistAndAlbumFromDirectory(File dir) {
        String artist = dir.getParentFile().getName();
        String album = dir.getName().substring(7);  // substring to get rid of the year prefix
        return artist + " " + album;
    }

    /**
     * Helper function to check and see if a directory has a cover image file
     *
     * @param folder, the folder to check
     * @return the cover file, or null if it doesn't exist
     */
    public File folderContainsCover(File folder) {
        String regex = "\\[\\d{4}\\] .*";
        if (folder.getName().matches(regex)) {
            // all is well, just get the cover normally
        } else if (folder.getName().startsWith("CD")) {
            folder = folder.getParentFile();
        }

        File[] files = folder.listFiles();      // get all the files
        for (File file : files) {
            if (file.getName().equals("cover.png") || file.getName().equals("cover.jpg") || file.getName().equals("cover.jpeg")) {
                return file;
            }
        }
        // if we reach this point, then the cover wasn't found
        // perform one final check and recursively call itself
        if (folder.getParentFile().getName().matches(regex)) {
            return folderContainsCover(folder.getParentFile());
        }

        // no cover files were found, returning null
        return null;
    }

    /**
     * Helper Function Gets the index at the specified row
     *
     * @param row
     * @return
     */
    public int getIndex(int row) {
        row = table.convertRowIndexToModel(row);
        return Integer.valueOf(table.getModel().getValueAt(row, 12).toString());
    }

    /**
     * Gets the File object from the row
     *
     * @param row
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
        if (Utils.isAGenrePartOfALabel(file)) {
            return file.getName().replace(".mp3", "");
        } else {
            String regex = "\\d{2} .*\\.mp3";
            if (file.getName().matches(regex)) {
                return file.getName().substring(3).replace(".mp3", "").trim();
            } else {
                return "";
            }
        }
    }

    /**
     * Gets the artist based on the file location
     *
     * @param file, the file to check
     * @return a string artist
     */
    public String getArtistFromFile(File file) {
        if (Utils.isAnEPPartOfALabel(file)) {
            return "";
        }

        return getArtist(file);
    }

    /**
     * Gets the album based on the file location
     *
     * @param file, the file to check
     * @return a string album
     */
    public String getAlbumFromFile(File file) {
        if (Utils.isAGenrePartOfALabel(file)) {
            return getGenreFromFile(file);
        } else {
            File dir = file.getParentFile();
            String regex = "\\[\\d{4}\\] .*";
            if (dir.getName().matches(regex)) {
                return dir.getName().substring(6).trim();
            } else if (dir.getName().startsWith("CD")) {
                // album is a multiple CD album
                dir = dir.getParentFile();
                if (dir.getName().matches(regex)) {
                    return dir.getName().substring(6).trim();
                }
            }
            return "";
        }
    }

    /**
     * Gets the album artist based on the file location
     *
     * @param file, the file to check
     * @return a string album artist
     */
    public String getAlbumArtistFromFile(File file) {
        if (Utils.isPartOfALabel(file)) {
            File dir = file.getParentFile().getParentFile().getParentFile();
            return dir.getName();
        }

        // get the normal artist
        return getArtist(file);
    }

    /**
     * Gets the artist based on the file location
     *
     * @param file, the file to check
     * @return a string artist
     */
    public String getArtist(File file) {
        File dir = file.getParentFile();
        String regex = "\\[\\d{4}\\] .*";
        if (dir.getName().matches(regex)) {
            dir = dir.getParentFile();
            return dir.getName();
        } else if (dir.getName().startsWith("CD")) {
            dir = dir.getParentFile().getParentFile();
            return dir.getName();
        } else {
            return "";
        }
    }

    /**
     * Gets the year based on the file location
     *
     * @param file, the file to check
     * @return a string year
     */
    public String getYearFromFile(File file) {
        if (file == null) {
            return "";
        }

        File dir = file.getParentFile();
        String regex = "\\[\\d{4}\\] .*";
        if (dir.getName().matches(regex)) {
            return dir.getName().substring(1, 5).trim();
        } else if (dir.getName().startsWith("CD")) {
            // album is a multiple CD album
            dir = dir.getParentFile();
            if (dir.getName().matches(regex)) {
                return dir.getName().substring(1, 5).trim();
            }
        }
        return "";
    }

    /**
     * Gets the tracks based on the filename and other files in the directory
     *
     * @param file, the file to check
     * @return a tracks in the form of a string
     */
    public String getTracksFromFolder(File file) {
        if (file == null) {
            return "";
        }

        File dir = file.getParentFile();
        String totalTracks = getTotalTracksFromFolder(dir);
        String regex = "\\d{2} .*\\.mp3";
        if (file.getName().matches(regex)) {
            if (Integer.valueOf(file.getName().substring(0, 2)) < 10) {
                return file.getName().substring(1, 2) + "/" + totalTracks;
            } else {
                return file.getName().substring(0, 2) + "/" + totalTracks;
            }
        } else {
            return "";
        }
    }

    /**
     * Gets the total number of tracks in a folder
     *
     * @param file, the folder to check
     * @return a String representation of the songs in the folder
     */
    public String getTotalTracksFromFolder(File file) {
        return String.valueOf(getNumberOfSongs(file));
    }

    /**
     * Gets the total number of tracks in a folder
     *
     * @param file, the folder to check
     * @return an int count of mp3 files in a folder
     */
    public int getNumberOfSongs(File file) {
        File[] files = file.listFiles();
        int count = 0;
        for (File file1 : files) {
            if (file1.getName().endsWith(".mp3")) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the disks from the file
     *
     * @param file, the file to check
     * @return a string representation of disks
     */
    public String getDisksFromFile(File file) {
        File dir = file.getParentFile();
        String regex = "\\[\\d{4}\\] .*";
        if (dir.getName().matches(regex)) {
            // there's no CD1, CD2 folders, single disk album
            return "1/1";
        } else if (dir.getName().startsWith("CD") && dir.getParentFile().getName().matches(regex)) {
            // multiple disk album, get the current disk based on the folder it's in
            int totalDisks = getTotalDisksFromFolder(dir);
            return dir.getName().substring(2) + "/" + totalDisks;
        } else {
            return "";
        }
    }

    /**
     * Gets the total disks from a folder
     *
     * @param dir, the folder to check
     * @return an int count of disks
     */
    public int getTotalDisksFromFolder(File dir) {
        dir = dir.getParentFile();
        File[] dirs = dir.listFiles(File::isDirectory);
        int count = 0;
        for (File folder : dirs) {
            if (folder.getName().startsWith("CD")) {
                // most (if not all) times, this should be 2
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a genre String if the file is a part of a label
     *
     * @param file, the file to check
     * @return a genre string
     */
    public String getGenreFromFile(File file) {
        if (!Utils.isAGenrePartOfALabel(file)) {
            return "";
        }
        return file.getParentFile().getName();
    }

    /**
     * Function for adding the track numbers
     *
     * @param selectedRows, the rows selected on the table
     */
    public void addTrackAndDiskNumbers(int[] selectedRows) {
        for (int i = 0; i < selectedRows.length; i++) {
            int row = selectedRows[i];
            File file = getFile(row);
            autoAddTrackAndDiskNumbers(row, file);
        }
    }

    /**
     * Function that actually sets the row info
     *
     * @param row
     * @param file
     */
    public void autoAddTrackAndDiskNumbers(int row, File file) {
        int index = getIndex(row);

        String tracks = getTracksFromFolder(file);
        String disks = getDisksFromFile(file);

        // tracks
        Main.frame.songController.setTrack(index, tracks);
        table.setValueAt(tracks, row, TABLE_COLUMN_TRACK);

        // disks
        Main.frame.songController.setDisk(index, disks);
        table.setValueAt(disks, row, TABLE_COLUMN_DISK);
    }

}
