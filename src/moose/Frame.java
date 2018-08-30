/**
 *  File:   Frame.java
 *  Desc:   Main UI class for the JFrame containing the everything.
 *
 *  Copyright Pat Ripley 2018
 */
// package
package moose;

// imports
import com.apple.eawt.Application;
import com.mpatric.mp3agic.*;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;

// class Frame
public class Frame extends javax.swing.JFrame {

    // ArrayLists
    HashMap<Integer, Song> songs = new HashMap<>();     // hashmap to contain Song objects
    ArrayList edited_songs = new ArrayList();           // arraylist to contain indices of edited songs to save

    // some graphics ivars
    ActionListener menuListener;        // listener for the popup menu objects

    int curr_row;   // keeps track of the current row
    int curr_col;   // keeps track of the current column

    // table model used, with some customizations and overrides
    DefaultTableModel model = new DefaultTableModel() {
        @Override   // returns a certain type of class based on the column index
        public Class getColumnClass(int column) {
            if (column == 11 || column == 0) {
                return ImageIcon.class;
            } else {
                return Object.class;
            }
        }

        @Override   // returns if the cell is editable based on the column index
        public boolean isCellEditable(int row, int column) {
            if (column == 11 || column == 0) {
                return false;
            } else {
                return true;
            }
        }
    };

    // some constants to make life easier
    private static final int DEFAULT = 0;
    private static final int EDITED = 1;
    private static final int SAVED = 2;

    private static final int SHIFT_TAB = 0;
    private static final int SHIFT_ENTER = 1;
    private static final int TAB = 2;
    private static final int ENTER = 3;

    /**
     * Creates new form Frame
     */
    public Frame() {

        // set the moose icon in the app and in the dock (OS X)
        this.setIconImage(new ImageIcon("img/moose.png").getImage());
        Application.getApplication().setDockIconImage(
                new ImageIcon("img/moose.png").getImage());

        // listener for the context menu when you right click on a row
        // basically tells the program where to go based on the user's choice
        this.menuListener = (ActionEvent event) -> {

            // get all the rows selected
            int[] selectedRows = table.getSelectedRows();

            // switch based on the option selected
            switch (event.getActionCommand()) {
                case "Add":
                    addAlbumArt(selectedRows);
                    break;
                case "Remove":
                    removeAlbumArt(selectedRows);
                    break;
                case "Remove from list":
                    removeRows(selectedRows);
                    break;
                case "Play":
                    playFiles(selectedRows);
                    break;
                case "Move File...":
                    moveFiles(selectedRows);
                    break;
                case "Save":
                    saveTracks(selectedRows);
                default:
                    break;
            }
        }; // end menuListener

        // init the components
        initComponents();

        // manual init'ing of components
        // set the model to the table's model
        model = (DefaultTableModel) table.getModel();

        // create the columns
        model.addColumn("");
        model.addColumn("File");
        model.addColumn("Filename");
        model.addColumn("Title");
        model.addColumn("Artist");
        model.addColumn("Album");
        model.addColumn("Album Artist");
        model.addColumn("Year");
        model.addColumn("Genre");
        model.addColumn("Track");
        model.addColumn("Disk");
        model.addColumn("Artwork");
        model.addColumn("Index");

        // remove the File and Index columns
        table.removeColumn(table.getColumnModel().getColumn(1));
        //table.removeColumn(table.getColumnModel().getColumn(11));

        // set the widths of the columns
        // file name and title are left out so they can take the remainder of the space dynamically
        setColumnWidth(0, 12);      // row icon
        setColumnWidth(3, 150);     // artist
        setColumnWidth(4, 150);     // album
        setColumnWidth(5, 150);     // album artist
        setColumnWidth(6, 80);      // year
        setColumnWidth(7, 150);     // genre
        setColumnWidth(8, 50);      // track
        setColumnWidth(9, 50);      // disk
        setColumnWidth(10, 100);    // album art
        setColumnWidth(11, 20);

        // taken from the FileDrop example
        FileDrop fileDrop = new FileDrop(System.out, tableSP, (java.io.File[] files) -> {

            int succ_mp3Count = 0;   // lets count the number of successful files imported
            int unsucc_mp3Count = 0; // lets count the number of all files attempted to import

            // create an arraylist of files and traverse it
            ArrayList<File> fileList = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    fileList = listFiles(files[i], fileList);
                } else {
                    fileList.add(files[i]);
                }
            }

            // iterate through the files and add them to the table if you can
            for (int i = 0; i < fileList.size(); i++) {
                if (addFileToTable(fileList.get(i))) {
                    succ_mp3Count++;
                } else {
                    unsucc_mp3Count++;
                }
            }

            // update the log table when you're done with the file iteration
            if (succ_mp3Count == 0) {
                updateConsole("No mp3 files found!");
            } else if (succ_mp3Count > 1 && unsucc_mp3Count == 0) {
                updateConsole(succ_mp3Count + " mp3 files loaded!");
            } else if (succ_mp3Count == 1) {
                updateConsole("1 mp3 file imported.");
            } else if (succ_mp3Count > 1 && unsucc_mp3Count == 1) {
                updateConsole(succ_mp3Count + " mp3 files loaded, 1 file wasn't an mp3!");
            } else if (succ_mp3Count > 1 && unsucc_mp3Count > 1) {
                updateConsole(succ_mp3Count + " mp3 files loaded, " + unsucc_mp3Count + " unknown files not loaded!");
            } else {
                // I don't think this should happen
            }
        });

        // listener for editing cells
        // uses custom class TableCellListener to get the row, col, before and after values
        Action action = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TableCellListener tcl = (TableCellListener) e.getSource();

                int r = tcl.getRow();
                int c = tcl.getColumn();
                
                int index = Integer.valueOf(model.getValueAt(r, 12).toString());

                // switch to see what column changed, and do a task based on that
                switch (c) {
                    case 2:     // filename was changed
                        // with the filename changing, this changes automatically without hitting save
                        // this functionality might change
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            File old_file = (File) model.getValueAt(r, 1);
                            String path = old_file.getPath().replace(old_file.getName(), "");
                            String fileName = model.getValueAt(r, c).toString();
                            File new_file = new File(path + "//" + fileName + ".mp3");
                            songs.get(index).setFile(new_file);

                            old_file.renameTo(new_file);
                            model.setValueAt(new_file, r, 1);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 3:     // title was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setTitle(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 4:     // artist was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setArtist(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 5:     // album was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setAlbum(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 6:     // album artist was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setAlbumArtist(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 7:     // year was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setYear(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 8:     // genre was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setGenre(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 9:     // tracks was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setTrack(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 10:     // disks was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setDisk(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 11:    // artwork was changed
                    //setAlbumImage(index, tcl.getNewValue().toString());
                    default:    // not accounted for
                        break;
                }
            }
        };

        // declare the TCL for use
        TableCellListener tcl2 = new TableCellListener(table, action);
    }

    // TODO When editing a single cell, the row icon doesn't update correctly
    
    /**
     * Adds the song index to edited_songs to save, and updates the row icon
     *
     * @param index, the index to add to edited_songs
     */
    public void songEdited(int index) {
        if (!edited_songs.contains(index)) {
            edited_songs.add(index);
            int row = getRow(index);
            setRowIcon(EDITED, row);
        } else {
            // do nothing, index is already added
        }
    }

    // TODO Make sure this works and document it
    public void moveFiles(int[] selectedRows) {

        JFileChooser jfc = new JFileChooser();
        File library = new File("/Users/pat/Music/Library");
        jfc.setCurrentDirectory(library);
        jfc.setDialogTitle("Choose the destination folder...");
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = jfc.showDialog(this, "Select");

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            for (int i = 0; i < selectedRows.length; i++) {
                File file = (File) model.getValueAt(selectedRows[i], 1);

                File directory = jfc.getSelectedFile();
                File new_file = new File(directory.getPath() + "/" + file.getName());
                file.renameTo(new_file);

                int index = getIndex(selectedRows[i]);
                songs.get(index).setFile(new_file);
                model.setValueAt(new_file, selectedRows[i], 1);
            }
        } else {
            return;
        }

    }

    /**
     * Removes the rows from the table
     *
     * @param selectedRows the rows to remove
     */
    public void removeRows(int[] selectedRows) {
        // traverse the array of selectedRows and delete them
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            int row = table.convertRowIndexToModel(selectedRows[i]);    // get the row
            model.removeRow(row);
        }
        // update some graphics
        enableMultPanel(false);
    }

    /**
     * Plays the files using the default mp3 player
     *
     * @param selectedRows the rows of files to play
     */
    public void playFiles(int[] selectedRows) {
        // traverse the array of rows and play each file sequentially
        for (int i = 0; i < selectedRows.length; i++) {
            try {
                int row = table.convertRowIndexToModel(selectedRows[i]);    // get the row
                File file = (File) model.getValueAt(row, 1);
                Desktop desktop = Desktop.getDesktop();
                if (file.exists()) {
                    desktop.open(file);
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }

    /**
     * Adds the album art for each song if there exists a cover.* file in the
     * same dir
     *
     * @param selectedRows the rows of songs to update
     */
    public void autoAddCovers(int[] selectedRows) {
        // traverse the array of rows and add each image
        for (int i = 0; i < selectedRows.length; i++) {
            int row = table.convertRowIndexToModel(selectedRows[i]);    // get the row

            // get the parent directory of the song
            File dir = songs.get(getIndex(row)).getFile().getParentFile();

            // check if the parent directory has a cover.* file
            File cover = folderContainsCover(dir);
            if (cover != null) {
                // if cover doesn't return as null, add the cover
                addIndividualCover(row, cover);
            }
        }
    }

    /**
     * Helper function to check and see if a directory has a cover image file
     *
     * @param folder, the folder to check
     * @return the cover file, or null if it doesn't exist
     */
    public File folderContainsCover(File folder) {
        File[] files = folder.listFiles();      // get all the files
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals("cover.png")
                    || files[i].getName().equals("cover.jpg")
                    || files[i].getName().equals("cover.jpeg")) {
                return files[i];
            }
        }
        // no cover files were found, returning null
        return null;
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
            int index = Integer.valueOf(model.getValueAt(row, 12).toString());

            // convert file to byte array
            RandomAccessFile ra_file = new RandomAccessFile(cover.getAbsolutePath(), "r");
            byte[] bytes = new byte[(int) ra_file.length()];
            ra_file.read(bytes);
            ra_file.close();

            // update the track in the songs array
            songs.get(index).setArtwork_bytes(bytes);

            // update graphics
            Icon thumbnail_icon = null;
            try {
                // getting the image from the byte array
                ImageIcon icon = new ImageIcon(bytes);
                Image img = icon.getImage();

                // scaling down the image to put on the row
                Image thumbnail = img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                thumbnail_icon = new ImageIcon(thumbnail);

            } catch (NullPointerException e) {
                System.err.println(e);
            }

            // set the image on the row
            model.setValueAt(thumbnail_icon, row, 11);

            // song was edited, add it to the list
            songEdited(index);

            // if there's multiple rows selected, also add it to the multiple fields panel
            if (table.getSelectedRowCount() > 1) {
                // getting the image from the byte array
                ImageIcon icon = new ImageIcon(bytes);
                Image img = icon.getImage();

                // scaling down the image to put on the panel
                Image thumbnail = img.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH);
                ImageIcon artwork_icon = new ImageIcon(thumbnail);
                multImage.setIcon(artwork_icon);
            }

        } catch (IOException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method for adding album art for songs manually This method is called when
     * the "Add" selection is pressed in the context menu
     *
     * @param selectedRows
     */
    public void addAlbumArt(int[] selectedRows) {

        // need this for some reason
        File img_file = null;

        for (int i = 0; i < selectedRows.length; i++) {

            try {
                // get the row and index of the track
                int row = table.convertRowIndexToModel(selectedRows[i]);
                int index = Integer.valueOf(model.getValueAt(row, 12).toString());

                // get the file to use as the starting point for choosing an image
                File file = songs.get(index).getFile();

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

                // convert that file to a byte array
                byte[] bytes;
                try (RandomAccessFile ra_file = new RandomAccessFile(img_file.getAbsolutePath(), "r")) {
                    bytes = new byte[(int) ra_file.length()];
                    ra_file.read(bytes);
                }

                // set the artwork in the songs array
                songs.get(index).setArtwork_bytes(bytes);

                // update graphics
                Icon thumbnail_icon = null;
                try {
                    // getting the image from the byte array
                    ImageIcon icon = new ImageIcon(bytes);
                    Image img = icon.getImage();
                    Image thumbnail = img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                    thumbnail_icon = new ImageIcon(thumbnail);
                } catch (NullPointerException e) {
                    System.err.println(e);
                }
                model.setValueAt(thumbnail_icon, row, 11);

                // send the track to the edited_songs array
                songEdited(index);

                // if there's multiple images, update the multPanel
                if (table.getSelectedRowCount() > 1) {
                    // getting the image from the byte array
                    ImageIcon icon = new ImageIcon(bytes);
                    Image img = icon.getImage();

                    // scaling down image to use in multPanel
                    Image thumbnail = img.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH);
                    ImageIcon artwork_icon = new ImageIcon(thumbnail);
                    multImage.setIcon(artwork_icon);
                }

            } catch (IOException ex) {
                Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Method for removing album art for songs manually This method is called
     * when the "Remove" selection is pressed in the context menu
     *
     * @param selectedRows
     */
    public void removeAlbumArt(int[] selectedRows) {

        for (int i = 0; i < selectedRows.length; i++) {

            // get the row and index of the track
            int row = table.convertRowIndexToModel(selectedRows[i]);
            int index = getIndex(row);

            // update the songs array
            songs.get(index).setArtwork_bytes(null);

            // send the track to the edited_songs array
            songEdited(index);

            // update graphics
            model.setValueAt(null, row, 11);
            multImage.setIcon(null);
        }

    }

    /**
     * Helper function to set the title of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param title, the title to set
     */
    public void setTitle(int index, String title) {
        songs.get(index).setTitle(title);
    }

    /**
     * Helper function to set the artist of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param artist, the artist to set
     */
    public void setArtist(int index, String artist) {
        songs.get(index).setArtist(artist);
    }

    /**
     * Helper function to set the album of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param album, the album to set
     */
    public void setAlbum(int index, String album) {
        songs.get(index).setAlbum(album);
    }

    /**
     * Helper function to set the album artist of the song in the songs
     * arraylist.
     *
     * @param index, the index of the song
     * @param albumartist, the albumartist to set
     */
    public void setAlbumArtist(int index, String albumartist) {
        songs.get(index).setAlbumartist(albumartist);
    }

    /**
     * Helper function to set the genre of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param genre, the genre to set
     */
    public void setGenre(int index, String genre) {
        songs.get(index).setGenre(genre);
    }

    /**
     * Helper function to set the year of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param year, the year to set
     */
    public void setYear(int index, String year) {
        songs.get(index).setYear(year);
    }

    /**
     * Helper function to set the track of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param track, the track to set
     */
    public void setTrack(int index, String track) {
        songs.get(index).setTrack(track);
    }

    /**
     * Helper function to set the disk of the song in the songs arraylist.
     *
     * @param index, the index of the song
     * @param disk, the disk to set
     */
    public void setDisk(int index, String disk) {
        songs.get(index).setDisk(disk);
    }

    /**
     * Helper function to set the album image of the song in the songs
     * arraylist.
     *
     * @param index, the index of the song
     * @param bytes, the byte array of the album image to set
     */
    public void setAlbumImage(int index, byte[] bytes) {
        songs.get(index).setArtwork_bytes(bytes);
    }

    /**
     * Helper function to set the row icon based on the action of the row.
     *
     * @param icon, the icon to set
     * @param row, the row to set
     */
    public void setRowIcon(int icon, int row) {

        row = table.convertRowIndexToModel(row);
        switch (icon) {
            case DEFAULT:
                model.setValueAt(new ImageIcon("img//default.png"), row, 0);
                break;
            case EDITED:
                model.setValueAt(new ImageIcon("img//edit.png"), row, 0);
                break;
            case SAVED:
                model.setValueAt(new ImageIcon("img//check.png"), row, 0);
                break;
        }
    }

    /**
     * Adds the file and all of its pertinent information to the table as a row
     * Works with the fileDrop functionality
     *
     * @param file, the file to add
     * @return
     */
    public boolean addFileToTable(File file) {

        // check if the file is an mp3
        if (!file.getAbsolutePath().endsWith(".mp3")) {
            return false;
        } else {

            // mp3agic Mp3File object, used for the id3tags
            Mp3File mp3file;
            try {
                // create the mp3file from the file's path
                mp3file = new Mp3File(file.getAbsolutePath());

                // if the mp3file doesn't have an id3tag, create one
                if (!mp3file.hasId3v2Tag()) {
                    ID3v2 tag = new ID3v24Tag();
                    mp3file.setId3v2Tag(tag);
                }
            } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                // things borked
                mp3file = null;
            }

            // get the id3v2 info
            String title = mp3file.getId3v2Tag().getTitle();
            String artist = mp3file.getId3v2Tag().getArtist();
            String album = mp3file.getId3v2Tag().getAlbum();
            String albumartist = mp3file.getId3v2Tag().getAlbumArtist();
            String genre = mp3file.getId3v2Tag().getGenreDescription();
            String year = mp3file.getId3v2Tag().getYear();
            String track = mp3file.getId3v2Tag().getTrack();
            String disk = mp3file.getId3v2Tag().getPartOfSet();
            byte[] artwork_bytes = mp3file.getId3v2Tag().getAlbumImage();

            // sets the strings to blank to avoid NPE
            if (title == null) {
                title = "";
            }
            if (artist == null) {
                artist = "";
            }
            if (album == null) {
                album = "";
            }
            if (albumartist == null) {
                albumartist = "";
            }
            if (genre == null) {
                genre = "";
            }
            if (year == null) {
                year = "";
            }
            if (track == null) {
                track = "";
            }
            if (disk == null) {
                disk = "";
            }
            if (artwork_bytes == null) {
                artwork_bytes = new byte[0];
            }

            // create a song object with the information
            Song s = new Song(file, title, artist, album, albumartist, genre, year, track, disk, artwork_bytes);

            // make an index
            int index = songs.size();

            // add the song to the list
            songs.put(index, s);

            // getting the image to put on the table
            Icon thumbnail_icon = null;
            try {
                // getting the image from the byte array
                ImageIcon icon = new ImageIcon(artwork_bytes);
                Image img = icon.getImage();

                // scaling the image down to fit on the table
                Image thumbnail = img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                thumbnail_icon = new ImageIcon(thumbnail);
            } catch (NullPointerException ex) {
                System.err.println(ex);
            }

            // add the row to the table
            model.addRow(new Object[]{
                new ImageIcon("img//default.png"), // adds the default status icon
                s.getFile(), // hidden file object
                s.getFile().getName(), // actual editable file name
                s.getTitle(),
                s.getArtist(),
                s.getAlbum(),
                s.getAlbumartist(),
                s.getYear(),
                s.getGenre(),
                s.getFullTrackString(),
                s.getFullDiskString(),
                (thumbnail_icon != null) ? thumbnail_icon : null, // checks for null value first
                index // hidden index for the song object
            });
        }

        // sorts the table on the filename by default
        DefaultRowSorter sorter = ((DefaultRowSorter) table.getRowSorter());
        ArrayList list = new ArrayList();
        list.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sorter.setSortKeys(list);
        sorter.sort();

        // all is well in the world
        return true;
    }

    /**
     * Helper function to update the UI's console, just appends a string
     *
     * @param s, the string to append
     */
    public void updateConsole(String s) {
        console.append(s + "\n");
    }

    /**
     * Function that selects the cell being edited. Used mainly when pressing
     * tab or enter to navigate.
     *
     * @param row, the row of the cell
     * @param column, the column of the cell
     * @param nav_type, the type of navigation
     */
    public void changeSelection(final int row, final int column, int nav_type) {

        // set the globals to the params
        this.curr_row = row;
        this.curr_col = column;

        // check if the cell can be edited
        if (table.editCellAt(row, column)) {
            // if the cell is good to be edited, select it
            table.getEditorComponent().requestFocusInWindow();
        } else {
            // cell couldn't be edited, so determine where to go based on the navigation type
            switch (nav_type) {

                // enter was pressed on the last row, go to the top of the list in the same column
                case ENTER:
                    changeSelection(0, column, -1);
                    break;

                // tab was pressed on the last column, go to the next row
                case TAB:
                    // if the row is the last row, go to the first row
                    if (row == table.getRowCount() - 1) {
                        changeSelection(0, 1, -1);
                    } else {
                        changeSelection(row + 1, 1, -1);
                    }
                    break;

                // shift + enter was pressed on the first row, go to the last row in the same column
                case SHIFT_ENTER:
                    changeSelection(table.getRowCount() - 1, column, -1);
                    break;

                // shift + tab was pressed on the first column, go to the previous row in the last column
                case SHIFT_TAB:

                    // if the row is the first row, go to the last row
                    if (row != 0) {
                        changeSelection(row - 1, 9, -1);
                    } else {
                        changeSelection(table.getRowCount() - 1, 9, -1);
                    }
                    break;

                // none of the above conditions are met, try to edit the cell normally
                default:
                    changeSelection(row, column, -1);
                    break;
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        container = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        tableSP = new javax.swing.JScrollPane();
        table = new javax.swing.JTable() {

        };
        consoleSP = new javax.swing.JScrollPane();
        console = new javax.swing.JTextArea();
        multPanel = new javax.swing.JPanel();
        L1 = new javax.swing.JLabel();
        L2 = new javax.swing.JLabel();
        L3 = new javax.swing.JLabel();
        L4 = new javax.swing.JLabel();
        L5 = new javax.swing.JLabel();
        L6 = new javax.swing.JLabel();
        L7 = new javax.swing.JLabel();
        L8 = new javax.swing.JLabel();
        L9 = new javax.swing.JLabel();
        multTitle = new javax.swing.JTextField();
        multArtist = new javax.swing.JTextField();
        multAlbum = new javax.swing.JTextField();
        multAlbumArtist = new javax.swing.JTextField();
        multGenre = new javax.swing.JTextField();
        multYear = new javax.swing.JTextField();
        multTrack = new javax.swing.JTextField();
        multDisk = new javax.swing.JTextField();
        multImage = new javax.swing.JLabel();
        multUpdateButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveTrackMenuItem = new javax.swing.JMenuItem();
        saveAllMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        refreshMenuItem = new javax.swing.JMenuItem();
        macroMenu = new javax.swing.JMenu();
        addCoversMenuItem = new javax.swing.JMenuItem();
        addTrackNumbersMenuItem = new javax.swing.JMenuItem();
        formatFilenamesMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Moose");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel1.setText("Moose");

        saveButton.setText("Save All");
        saveButton.setFocusable(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        table.setAutoCreateRowSorter(true);
        table.setModel(model);
        table.setRequestFocusEnabled(false);
        table.setRowHeight(20);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setShowGrid(true);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableMousePressed(evt);
            }
        });
        table.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tableKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableKeyReleased(evt);
            }
        });
        tableSP.setViewportView(table);

        console.setEditable(false);
        console.setColumns(20);
        console.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        console.setRows(5);
        consoleSP.setViewportView(console);

        multPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        L1.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        L1.setText("Edit Multiple Items:");

        L2.setText("Title:");

        L3.setText("Artist:");

        L4.setText("Album:");

        L5.setText("Album Artist:");

        L6.setText("Genre:");

        L7.setText("Year:");

        L8.setText("Track:");

        L9.setText("Disk:");

        multTitle.setNextFocusableComponent(multArtist);
        multTitle.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multTitleKeyPressed(evt);
            }
        });

        multArtist.setNextFocusableComponent(multAlbum);
        multArtist.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multArtistKeyPressed(evt);
            }
        });

        multAlbum.setNextFocusableComponent(multAlbumArtist);
        multAlbum.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multAlbumKeyPressed(evt);
            }
        });

        multAlbumArtist.setNextFocusableComponent(multGenre);
        multAlbumArtist.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multAlbumArtistKeyPressed(evt);
            }
        });

        multGenre.setNextFocusableComponent(multYear);
        multGenre.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multGenreKeyPressed(evt);
            }
        });

        multYear.setNextFocusableComponent(multTrack);
        multYear.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multYearKeyPressed(evt);
            }
        });

        multTrack.setNextFocusableComponent(multDisk);
        multTrack.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multTrackKeyPressed(evt);
            }
        });

        multDisk.setNextFocusableComponent(multTitle);
        multDisk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multDiskKeyPressed(evt);
            }
        });

        multImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        multImage.setText(" ");
        multImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        multImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        multImage.setMaximumSize(new java.awt.Dimension(156, 156));
        multImage.setMinimumSize(new java.awt.Dimension(156, 156));
        multImage.setPreferredSize(new java.awt.Dimension(156, 156));
        multImage.setRequestFocusEnabled(false);
        multImage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                multImageMousePressed(evt);
            }
        });

        multUpdateButton.setText("Update Fields");
        multUpdateButton.setFocusable(false);
        multUpdateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multUpdateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout multPanelLayout = new javax.swing.GroupLayout(multPanel);
        multPanel.setLayout(multPanelLayout);
        multPanelLayout.setHorizontalGroup(
            multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, multPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(multPanelLayout.createSequentialGroup()
                        .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(L2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(L3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(L4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(L5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(multTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(multArtist)
                            .addComponent(multAlbum)
                            .addComponent(multAlbumArtist))
                        .addGap(18, 18, 18)
                        .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(L8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(L6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(L7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(L9, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(multGenre, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(multTrack, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(multDisk, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(multYear, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(29, 29, 29))
                    .addGroup(multPanelLayout.createSequentialGroup()
                        .addComponent(L1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(multUpdateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(multImage, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        multPanelLayout.setVerticalGroup(
            multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(multPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(multPanelLayout.createSequentialGroup()
                        .addComponent(L1)
                        .addGap(9, 9, 9)
                        .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(multPanelLayout.createSequentialGroup()
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L2)
                                    .addComponent(multTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L3)
                                    .addComponent(multArtist, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L4)
                                    .addComponent(multAlbum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(multPanelLayout.createSequentialGroup()
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L6)
                                    .addComponent(multGenre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L7)
                                    .addComponent(multYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L8)
                                    .addComponent(multTrack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L9)
                                    .addComponent(multDisk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(multAlbumArtist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(L5)))))
                    .addComponent(multImage, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(multUpdateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout containerLayout = new javax.swing.GroupLayout(container);
        container.setLayout(containerLayout);
        containerLayout.setHorizontalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveButton))
                    .addComponent(tableSP, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 1400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerLayout.createSequentialGroup()
                        .addComponent(consoleSP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(multPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        containerLayout.setVerticalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(tableSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(multPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(consoleSP))
                .addContainerGap())
        );

        fileMenu.setText("File");

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_MASK));
        openMenuItem.setText("Open...");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveTrackMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.META_MASK));
        saveTrackMenuItem.setText("Save Track");
        saveTrackMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveTrackMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveTrackMenuItem);

        saveAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        saveAllMenuItem.setText("Save All");
        fileMenu.add(saveAllMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_MASK));
        exitMenuItem.setText("Exit");
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        viewMenu.setText("View");

        refreshMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.META_MASK));
        refreshMenuItem.setText("Refresh");
        viewMenu.add(refreshMenuItem);

        jMenuBar1.add(viewMenu);

        macroMenu.setText("Macros");

        addCoversMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        addCoversMenuItem.setText("Add Covers");
        addCoversMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCoversMenuItemActionPerformed(evt);
            }
        });
        macroMenu.add(addCoversMenuItem);

        addTrackNumbersMenuItem.setText("Add Track Numbers");
        addTrackNumbersMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTrackNumbersMenuItemActionPerformed(evt);
            }
        });
        macroMenu.add(addTrackNumbersMenuItem);

        formatFilenamesMenuItem.setText("Format Filenames");
        macroMenu.add(formatFilenamesMenuItem);

        jMenuBar1.add(macroMenu);

        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(container, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(container, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Action for the save button press
     *
     * @param evt
     */
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        saveAll();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed

        // TODO create a importFiles method that takes a File[] and does this stuff
        // use a filechooser to open the folder full of stuff
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

        // result of filechoosing
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            ArrayList<File> files = new ArrayList<>();
            files = listFiles(fc.getSelectedFile(), files);

            int succ_mp3Count = 0;   // lets count the number of successful files imported
            int unsucc_mp3Count = 0; // lets count the number of all files attempted to import

            // iterate through the files and try to add them
            for (File file : files) {
                if (file.getName().endsWith(".mp3")) {
                    succ_mp3Count++;
                    addFileToTable(file);
                } else {
                    unsucc_mp3Count++;
                }
            }

            // update the log table when you're done with the file iteration
            if (succ_mp3Count == 0) {
                updateConsole("No mp3 files found!");
            } else if (succ_mp3Count > 1 && unsucc_mp3Count == 0) {
                updateConsole(succ_mp3Count + " mp3 files loaded!");
            } else if (succ_mp3Count == 1) {
                updateConsole("1 mp3 file imported.");
            } else if (succ_mp3Count > 1 && unsucc_mp3Count == 1) {
                updateConsole(succ_mp3Count + " mp3 files loaded, 1 file wasn't an mp3!");
            } else if (succ_mp3Count > 1 && unsucc_mp3Count > 1) {
                updateConsole(succ_mp3Count + " mp3 files loaded, " + unsucc_mp3Count + " unknown files not loaded!");
            } else {
                // I don't think this should happen
            }
        } else {
            // no files chose, update console
            updateConsole("No file(s) chosen!");
        }

    }//GEN-LAST:event_openMenuItemActionPerformed

    /**
     * What happens when the multPanel update button is clicked
     *
     * @param evt
     */
    private void multUpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multUpdateButtonActionPerformed
        updateMultPanelFields();
    }//GEN-LAST:event_multUpdateButtonActionPerformed

    private void tableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMousePressed

        // check what type of click
        switch (evt.getButton()) {

            // if it's a right click
            case java.awt.event.MouseEvent.BUTTON3:
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0) {
                    switch (col) {
                        case 10:
                            showArtworkPopup(evt);
                            break;
                        case 1:
                            showFilePopup(evt);
                            break;
                        default:
                            showRegularPopup(evt);
                            break;
                    }
                } else {
                    table.clearSelection();
                }
                break;

            // if it's a left click
            case java.awt.event.MouseEvent.BUTTON1:

                // check if double click
                if (evt.getClickCount() == 2) {
                    if (table.getSelectedColumn() == 10) {
                        showArtworkPopup(evt);
                    } else {
                        changeSelection(table.getSelectedRow(), table.getSelectedColumn(), -1);
                    }
                }
                break;

            // if it's a scroll click
            case java.awt.event.MouseEvent.BUTTON2:
                row = table.rowAtPoint(evt.getPoint());
                table.setRowSelectionInterval(row, row);

                int index = getIndex(table.convertRowIndexToModel(row));
                File file = songs.get(index).getFile();

                try {
                    Desktop desktop = Desktop.getDesktop();
                    if (file.exists()) {
                        desktop.open(file);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            default:
                break;
        }

        // find out what to do when row(s) are selected
        if (table.getSelectedRowCount() >= 1) {
            enableMultPanel(true);
            setMultiplePanelFields();
        } else if (table.getSelectedRowCount() < 1) {
            // no rows selected
        }

    }//GEN-LAST:event_tableMousePressed

    /**
     * Determines where to go based on the key press Looks at if the shift key
     * is pressed down on tabbing or not Sends information to changeSelection()
     * to determine where to go
     *
     * @param evt
     */
    private void tableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyReleased

        if (evt.getKeyCode() == KeyEvent.VK_TAB && !evt.isShiftDown()) {
            curr_col++;
            changeSelection(curr_row, curr_col, TAB);
            table.setRowSelectionInterval(curr_row, curr_row);
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER && !evt.isShiftDown()) {
            curr_row++;
            changeSelection(curr_row, curr_col, ENTER);
            table.setRowSelectionInterval(curr_row, curr_row);
        } else if (evt.getKeyCode() == KeyEvent.VK_TAB && evt.isShiftDown()) {
            curr_col--;
            changeSelection(curr_row, curr_col, SHIFT_TAB);
            table.setRowSelectionInterval(curr_row, curr_row);
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER && evt.isShiftDown()) {
            curr_row--;
            changeSelection(curr_row, curr_col, SHIFT_ENTER);
            table.setRowSelectionInterval(curr_row, curr_row);
        }

    }//GEN-LAST:event_tableKeyReleased

    private void addTrackNumbersMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTrackNumbersMenuItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addTrackNumbersMenuItemActionPerformed

    /**
     * Opens the context menu for album art
     *
     * @param evt
     */
    private void multImageMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_multImageMousePressed
        showArtworkPopup(evt);
    }//GEN-LAST:event_multImageMousePressed

    /**
     * If enter is pressed while field is in focus
     */
    private void multTitleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multTitleKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multTitleKeyPressed

    /**
     * If enter is pressed while field is in focus
     */
    private void multArtistKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multArtistKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multArtistKeyPressed

    /**
     * If enter is pressed while field is in focus
     */
    private void multAlbumKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multAlbumKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multAlbumKeyPressed

    /**
     * If enter is pressed while field is in focus
     */
    private void multAlbumArtistKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multAlbumArtistKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multAlbumArtistKeyPressed

    /**
     * If enter is pressed while field is in focus
     */
    private void multGenreKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multGenreKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multGenreKeyPressed

    /**
     * If enter is pressed while field is in focus
     */
    private void multYearKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multYearKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multYearKeyPressed

    /**
     * If enter is pressed while field is in focus
     */
    private void multTrackKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multTrackKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multTrackKeyPressed

    /**
     * If enter is pressed while field is in focus
     */
    private void multDiskKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multDiskKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multDiskKeyPressed

    private void addCoversMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCoversMenuItemActionPerformed
        autoAddCovers(table.getSelectedRows());
    }//GEN-LAST:event_addCoversMenuItemActionPerformed

    private void saveTrackMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTrackMenuItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saveTrackMenuItemActionPerformed

    private void tableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_A && evt.isMetaDown()) {
            table.selectAll();
            setMultiplePanelFields();
        }
    }//GEN-LAST:event_tableKeyPressed

    /**
     * Sets the multiple fields panel based on the data selected
     */
    public void setMultiplePanelFields() {

        // get the indices of the selected rows
        int[] selectedRows = table.getSelectedRows();
        int rows = table.getSelectedRowCount();
        
        // make the arrays of values
        String[] titles = new String[rows];
        String[] artists = new String[rows];
        String[] albums = new String[rows];
        String[] albumartists = new String[rows];
        String[] genres = new String[rows];
        String[] years = new String[rows];
        String[] tracks = new String[rows];
        String[] disks = new String[rows];
        byte[][] images = new byte[rows][];

        // fill the arrays
        for (int i = 0; i < selectedRows.length; i++) {

            titles[i] = table.getValueAt(selectedRows[i], 2).toString();
            artists[i] = table.getValueAt(selectedRows[i], 3).toString();
            albums[i] = table.getValueAt(selectedRows[i], 4).toString();
            albumartists[i] = table.getValueAt(selectedRows[i], 5).toString();
            years[i] = table.getValueAt(selectedRows[i], 6).toString();
            genres[i] = table.getValueAt(selectedRows[i], 7).toString();
            tracks[i] = table.getValueAt(selectedRows[i], 8).toString();
            disks[i] = table.getValueAt(selectedRows[i], 9).toString();
            images[i] = songs.get(getIndex(selectedRows[i])).getArtwork_bytes();
        }

        // fill the fields
        if (checkIfSame(titles[0], titles)) {
            multTitle.setText(titles[0]);
        } else {
            multTitle.setText("-");
        }

        if (checkIfSame(artists[0], artists)) {
            multArtist.setText(artists[0]);
        } else {
            multArtist.setText("-");
        }

        if (checkIfSame(albums[0], albums)) {
            multAlbum.setText(albums[0]);
        } else {
            multAlbum.setText("-");
        }

        if (checkIfSame(albumartists[0], albumartists)) {
            multAlbumArtist.setText(albumartists[0]);
        } else {
            multAlbumArtist.setText("-");
        }

        if (checkIfSame(genres[0], genres)) {
            multGenre.setText(genres[0]);
        } else {
            multGenre.setText("-");
        }

        if (checkIfSame(years[0], years)) {
            multYear.setText(years[0]);
        } else {
            multYear.setText("-");
        }

        if (checkIfSame(tracks[0], tracks)) {
            multTrack.setText(tracks[0]);
        } else {
            multTrack.setText("-");
        }

        if (checkIfSame(disks[0], disks)) {
            multDisk.setText(disks[0]);
        } else {
            multDisk.setText("-");
        }

        if (checkIfSame(images[0], images) && images[0] != null) {

            // getting the image from the byte array
            ImageIcon icon = new ImageIcon(images[0]);
            Image img = icon.getImage();
            Image thumbnail = img.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH);
            ImageIcon artwork_icon = new ImageIcon(thumbnail);
            multImage.setIcon(artwork_icon);

        } else {
            multImage.setIcon(null);
        }
    }

    /**
     * Updates the fields in the table with the fields in the mult panel Gets
     * called when the update button is pressed
     */
    public void updateMultPanelFields() {

        // get the selected rows
        int[] selectedRows = table.getSelectedRows();

        // get the fields in the mult panel
        String title = multTitle.getText();
        String artist = multArtist.getText();
        String album = multAlbum.getText();
        String albumArtist = multAlbumArtist.getText();
        String genre = multGenre.getText();
        String year = multYear.getText();
        String track = multTrack.getText();
        String disk = multDisk.getText();

        // check if the title field needs updated
        if (!title.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = table.convertRowIndexToModel(selectedRows[i]);
                int index = getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(title, selectedRows[i], 2);

                // set the value in the songs array
                setTitle(index, title);

                // add the song to edited_songs and update the row icon
                songEdited(index);
            }
        }

        // check if the artist field needs updated
        if (!artist.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = table.convertRowIndexToModel(selectedRows[i]);
                int index = getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(artist, selectedRows[i], 3);

                // set the value in the songs array
                setArtist(index, artist);

                // add the song to edited_songs and update the row icon
                songEdited(index);
            }
        }

        // check if the album field needs updated
        if (!album.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = table.convertRowIndexToModel(selectedRows[i]);
                int index = getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(album, selectedRows[i], 4);

                // set the value in the songs array
                setAlbum(index, album);

                // add the song to edited_songs and update the row icon
                songEdited(index);
            }
        }

        // check if the album artist field needs updated
        if (!albumArtist.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = table.convertRowIndexToModel(selectedRows[i]);
                int index = getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(albumArtist, selectedRows[i], 5);

                // set the value in the songs array
                setAlbumArtist(index, albumArtist);

                // add the song to edited_songs and update the row icon
                songEdited(index);
            }
        }

        // check if the year field needs updated
        if (!year.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = table.convertRowIndexToModel(selectedRows[i]);
                int index = getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(year, selectedRows[i], 6);

                // set the value in the songs array
                setYear(index, year);

                // add the song to edited_songs and update the row icon
                songEdited(index);
            }
        }

        // check if the genre field needs updated
        if (!genre.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = table.convertRowIndexToModel(selectedRows[i]);
                int index = getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(genre, selectedRows[i], 7);

                // set the value in the songs array
                setGenre(index, genre);

                // add the song to edited_songs and update the row icon
                songEdited(index);
            }
        }

        // check if the track field needs updated
        if (!track.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = table.convertRowIndexToModel(selectedRows[i]);
                int index = getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(track, selectedRows[i], 8);

                // set the value in the songs array
                setTrack(index, track);

                // add the song to edited_songs and update the row icon
                songEdited(index);
            }
        }

        // check if the disk field needs updated
        if (!disk.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = table.convertRowIndexToModel(selectedRows[i]);
                int index = getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(disk, selectedRows[i], 9);

                // set the value in the songs array
                setDisk(index, disk);

                // add the song to edited_songs and update the row icon
                songEdited(index);
            }
        }
    }

    /**
     * Helper Function Lists and stores all of the files in a directory and
     * subdirectories
     *
     * @param directory
     * @param files
     * @return
     */
    public ArrayList<File> listFiles(File directory, ArrayList<File> files) {
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listFiles(file, files);     // this file is a directory, recursively call itself
            }
        }
        return files;
    }

    /**
     * Enables/Disables the mult panel
     *
     * @param bool
     */
    public void enableMultPanel(boolean bool) {

        multTitle.setText("");
        multArtist.setText("");
        multAlbum.setText("");
        multAlbumArtist.setText("");
        multGenre.setText("");
        multYear.setText("");
        multTrack.setText("");
        multDisk.setText("");

        multTitle.setEnabled(bool);
        multArtist.setEnabled(bool);
        multAlbum.setEnabled(bool);
        multAlbumArtist.setEnabled(bool);
        multGenre.setEnabled(bool);
        multYear.setEnabled(bool);
        multTrack.setEnabled(bool);
        multDisk.setEnabled(bool);
        multImage.setIcon(null);
        multUpdateButton.setEnabled(bool);
    }

    /**
     * Helper Function Checks if a string is the same throughout an array
     *
     * @param str, the string to check
     * @param arr, the array of strings
     * @return the result of the check
     */
    public boolean checkIfSame(String str, String[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (!arr[i].equals(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper Function Checks if a byte array is the same throughout an array
     *
     * @param bytes, the byte array to check
     * @param arr, the array of byte arrays
     * @return the result of the check
     */
    public boolean checkIfSame(byte[] bytes, byte[][] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (!Arrays.equals(arr[i], bytes)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Goes through the edited_songs array and saves each one
     */
    public void saveAll() {

        // traverse the array of songs
        for (int i = 0; i < songs.size(); i++) {

            // if the song at i is in edited_songs, update it
            if (edited_songs.contains(i)) {
                Song s = songs.get(i);
                File file = s.getFile();
                Mp3File mp3file = null;

                try {
                    mp3file = new Mp3File(file.getAbsolutePath());
                    ID3v2 tag = new ID3v24Tag();
                    mp3file.setId3v2Tag(tag);
                } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                    System.err.println(ex);
                }

                // set all the text based items
                try {
                    mp3file.getId3v2Tag().setTitle(s.getTitle());
                    mp3file.getId3v2Tag().setArtist(s.getArtist());
                    mp3file.getId3v2Tag().setAlbum(s.getAlbum());
                    mp3file.getId3v2Tag().setAlbumArtist(s.getAlbumartist());
                    mp3file.getId3v2Tag().setGenreDescription(s.getGenre());
                    mp3file.getId3v2Tag().setYear(s.getYear());
                    mp3file.getId3v2Tag().setTrack(s.getFullTrackString());
                    mp3file.getId3v2Tag().setPartOfSet(s.getFullDiskString());
                } catch (IllegalArgumentException ex) {
                    // this exception doesn't really matter
                    // this only happens if you save a track with no genre
                }

                // set album art
                String type = "image/jpeg";
                mp3file.getId3v2Tag().clearAlbumImage();
                mp3file.getId3v2Tag().setAlbumImage(s.getArtwork_bytes(), type);

                save(mp3file, file);

                // update the row graphic
                setRowIcon(SAVED, getRow(i));
            } else {
                // skip it, no changes
            }
        }

        // done with the saving, clear the edited_songs list
        edited_songs.clear();
    }

    public void saveTracks(int[] selectedRows) {
        // traverse the array of rows and play each file sequentially
        for (int i = 0; i < selectedRows.length; i++) {
            int row = table.convertRowIndexToModel(selectedRows[i]);    // get the row
            int index = getIndex(row);
            save(index);
        }
    }
    
    public void save(int index) {
        if (edited_songs.contains(index)) {
            Song s = songs.get(index);
            File file = s.getFile();
            Mp3File mp3file = null;

            try {
                mp3file = new Mp3File(file.getAbsolutePath());
                ID3v2 tag = new ID3v24Tag();
                mp3file.setId3v2Tag(tag);
            } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                System.err.println(ex);
            }

            // set all the text based items
            try {
                mp3file.getId3v2Tag().setTitle(s.getTitle());
                mp3file.getId3v2Tag().setArtist(s.getArtist());
                mp3file.getId3v2Tag().setAlbum(s.getAlbum());
                mp3file.getId3v2Tag().setAlbumArtist(s.getAlbumartist());
                mp3file.getId3v2Tag().setGenreDescription(s.getGenre());
                mp3file.getId3v2Tag().setYear(s.getYear());
                mp3file.getId3v2Tag().setTrack(s.getFullTrackString());
                mp3file.getId3v2Tag().setPartOfSet(s.getFullDiskString());
            } catch (IllegalArgumentException ex) {
                // this exception doesn't really matter
                // this only happens if you save a track with no genre
            }

            // set album art
            String type = "image/jpeg";
            mp3file.getId3v2Tag().clearAlbumImage();
            mp3file.getId3v2Tag().setAlbumImage(s.getArtwork_bytes(), type);

            save(mp3file, file);

            // update the row graphic
            setRowIcon(SAVED, getRow(index));
            
            // done saving, remove it
            // gives an IndexOutOfBoundsException when trying to remove() with one element in it
            if(edited_songs.size() == 1) {
                edited_songs.clear();
            } else if (edited_songs.size() > 1) {
                edited_songs.remove(index);
            }
        } else {
            // song doesn't need to be saved
        }
    }

    /**
     * Helper Function Gets row from an index
     *
     * @param index, the index of the song
     * @return the row where the index matches
     */
    public int getRow(int index) {
        String[] indices = getIndices();
        for (int i = 0; i < indices.length; i++) {
            String[] arr = indices[i].split("_");
            if (Integer.valueOf(arr[1]) == index) {
                return Integer.valueOf(arr[0]);
            }
        }
        return -1;
    }

    public String[] getIndices() {
//        int[] indices = new int[table.getRowCount()];
//        for (int i = 0; i < table.getRowCount(); i++) {
//            indices[i] = getIndex(i);
//        }
        String[] indices = new String[table.getRowCount()];
        for (int i = 0; i < indices.length; i++) {
            int row = table.convertRowIndexToModel(i);
            indices[i] = i + "_" + getIndex(row);
        }
        return indices;
    }

    /**
     * Helper Function Gets the index at the specified row
     *
     * @param row
     * @return
     */
    public int getIndex(int row) {
        return Integer.valueOf(model.getValueAt(row, 12).toString());
    }

    /**
     * Saves an individual file
     *
     * @param mp3file
     * @param file
     */
    public void save(Mp3File mp3file, File file) {
        try {
            // save the new mp3file
            mp3file.save(file.getAbsolutePath().replace(".mp3", "_.mp3"));

            // delete the old file
            file.delete();

            // rename the new file to match the old
            File newFile = new File(file.getAbsolutePath().replace(".mp3", "_.mp3"));
            newFile.renameTo(file);

        } catch (IOException | NotSupportedException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Shows the popup when you click on an album image
     *
     * @param e
     */
    void showArtworkPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        popup.add(item = new JMenuItem("Add"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Remove"));
        item.addActionListener(menuListener);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Shows the normal popup
     *
     * @param e
     */
    void showRegularPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        popup.add(item = new JMenuItem("Remove from list"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Play"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Save"));
        item.addActionListener(menuListener);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Shows the normal popup with some file options too
     *
     * @param e
     */
    void showFilePopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        popup.add(item = new JMenuItem("Remove from list"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Play"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Save"));
        item.addActionListener(menuListener);
        popup.addSeparator();
        popup.add(item = new JMenuItem("Move File..."));
        item.addActionListener(menuListener);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Sets the specified column width
     *
     * @param column, the column to set
     * @param width, width in pixels
     */
    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        if (width < 0) {
            // use the preferred width of the header..
            JLabel label = new JLabel((String) tableColumn.getHeaderValue());
            Dimension preferred = label.getPreferredSize();
            // altered 10->14 as per camickr comment.
            width = (int) preferred.getWidth() + 14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
 /*
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
         */
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            //new Frame().setVisible(true);
            Frame frame = new Frame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel L1;
    private javax.swing.JLabel L2;
    private javax.swing.JLabel L3;
    private javax.swing.JLabel L4;
    private javax.swing.JLabel L5;
    private javax.swing.JLabel L6;
    private javax.swing.JLabel L7;
    private javax.swing.JLabel L8;
    private javax.swing.JLabel L9;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem addCoversMenuItem;
    private javax.swing.JMenuItem addTrackNumbersMenuItem;
    private javax.swing.JTextArea console;
    private javax.swing.JScrollPane consoleSP;
    private javax.swing.JPanel container;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem formatFilenamesMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu macroMenu;
    private javax.swing.JTextField multAlbum;
    private javax.swing.JTextField multAlbumArtist;
    private javax.swing.JTextField multArtist;
    private javax.swing.JTextField multDisk;
    private javax.swing.JTextField multGenre;
    private javax.swing.JLabel multImage;
    private javax.swing.JPanel multPanel;
    private javax.swing.JTextField multTitle;
    private javax.swing.JTextField multTrack;
    private javax.swing.JButton multUpdateButton;
    private javax.swing.JTextField multYear;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem refreshMenuItem;
    private javax.swing.JMenuItem saveAllMenuItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveTrackMenuItem;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableSP;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables

}
