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
        table.removeColumn(table.getColumnModel().getColumn(11));
        
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
                        if(!tcl.getNewValue().equals(tcl.getOldValue())) {
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
                        if(!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setTitle(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;
                        
                    case 4:     // artist was changed
                        if(!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setArtist(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;
                        
                    case 5:     // album was changed
                        if(!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setAlbum(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;
                        
                    case 6:     // album artist was changed
                        if(!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setAlbumArtist(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;
                        
                    case 7:     // year was changed
                        if(!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setYear(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;
                        
                    case 8:     // genre was changed
                        if(!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setGenre(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;
                        
                    case 9:     // tracks was changed
                        if(!tcl.getNewValue().equals(tcl.getOldValue())) {
                            setTrack(index, tcl.getNewValue().toString());
                            songEdited(index);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;
                        
                    case 10:     // disks was changed
                        if(!tcl.getNewValue().equals(tcl.getOldValue())) {
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

    /**
     * Adds the song index to edited_songs to save, and updates the row icon
     * @param index, the index to add to edited_songs
     */
    public void songEdited(int index) {
        if(!edited_songs.contains(index)) {
            edited_songs.add(index);
            setRowIcon(EDITED, getRow(index));
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
     * Adds the album art for each song if there exists a cover.* file in the same dir
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
     * @param row
     * @param cover
     */
    public void addIndividualCover(int row, File cover) {

        try {
            int index = Integer.valueOf(model.getValueAt(row, 12).toString());
            File file = songs.get(index).getFile();
            Mp3File mp3file = new Mp3File(file.getAbsolutePath());
            ID3v2 id3v2Tag = null;

            if (mp3file.hasId3v2Tag()) {
                id3v2Tag = mp3file.getId3v2Tag();
            } else {
                // do nothing
            }

            RandomAccessFile ra_file = new RandomAccessFile(cover.getAbsolutePath(), "r");
            byte[] bytes = new byte[(int) ra_file.length()];
            ra_file.read(bytes);
            ra_file.close();

            songs.get(index).setArtwork_bytes(bytes);

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
                Image thumbnail = img.getScaledInstance(166, 166, java.awt.Image.SCALE_SMOOTH);
                ImageIcon artwork_icon = new ImageIcon(thumbnail);
                multImage.setIcon(artwork_icon);
            }

        } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Helper function to set the title of the song in the songs arraylist.
     * @param index, the index of the song
     * @param title, the title to set
     */
    public void setTitle(int index, String title) {
        songs.get(index).setTitle(title);
    }

    /**
     * Helper function to set the artist of the song in the songs arraylist.
     * @param index, the index of the song
     * @param artist, the artist to set
     */
    public void setArtist(int index, String artist) {
        songs.get(index).setArtist(artist);
    }

    /**
     * Helper function to set the album of the song in the songs arraylist.
     * @param index, the index of the song
     * @param album, the album to set
     */
    public void setAlbum(int index, String album) {
        songs.get(index).setAlbum(album);
    }

    /**
     * Helper function to set the album artist of the song in the songs
     * arraylist.
     * @param index, the index of the song
     * @param albumartist, the albumartist to set
     */
    public void setAlbumArtist(int index, String albumartist) {
        songs.get(index).setAlbumartist(albumartist);
    }

    /**
     * Helper function to set the genre of the song in the songs arraylist.
     * @param index, the index of the song
     * @param genre, the genre to set
     */
    public void setGenre(int index, String genre) {
        songs.get(index).setGenre(genre);
    }

    /**
     * Helper function to set the year of the song in the songs arraylist.
     * @param index, the index of the song
     * @param year, the year to set
     */
    public void setYear(int index, String year) {
        songs.get(index).setYear(year);
    }

    /**
     * Helper function to set the track of the song in the songs arraylist.
     * @param index, the index of the song
     * @param track, the track to set
     */
    public void setTrack(int index, String track) {
        songs.get(index).setTrack(track);
    }

    /**
     * Helper function to set the disk of the song in the songs arraylist.
     * @param index, the index of the song
     * @param disk, the disk to set
     */
    public void setDisk(int index, String disk) {
        songs.get(index).setDisk(disk);
    }

    /**
     * Helper function to set the album image of the song in the songs
     * arraylist.
     * @param index, the index of the song
     * @param bytes, the byte array of the album image to set
     */
    public void setAlbumImage(int index, byte[] bytes) {
        songs.get(index).setArtwork_bytes(bytes);
    }

    /**
     * Helper function to set the row icon based on the action of the row.
     * @param icon, the icon to set
     * @param row, the row to set
     */
    public void setRowIcon(int icon, int row) {
        
        //int model_row = table.convertRowIndexToModel(row);
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
     * Adds the file and all of its pertinent information to the table as a row.
     * Works with the fileDrop functionality.
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
                // things borked
                // there was no album image or something
                System.err.println(ex + "at line " + ex.getStackTrace()[0].getLineNumber());
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
        jMenu1 = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Moose");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel1.setText("Moose");

        saveButton.setText("Save");
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
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tableKeyTyped(evt);
            }
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

        jMenu1.setText("File");

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_MASK));
        openMenuItem.setText("Open...");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(openMenuItem);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_MASK));
        jMenuItem5.setText("Exit");
        jMenu1.add(jMenuItem5);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("View");

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.META_MASK));
        jMenuItem6.setText("Refresh");
        jMenu2.add(jMenuItem6);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Macros");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        jMenuItem1.setText("Add Covers");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem1);

        jMenuItem2.setText("Add Track Numbers");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem2);

        jMenuItem3.setText("Format Filenames");
        jMenu3.add(jMenuItem3);

        jMenuBar1.add(jMenu3);

        jMenu4.setText("Help");

        jMenuItem4.setText("About");
        jMenu4.add(jMenuItem4);

        jMenuBar1.add(jMenu4);

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

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        saveAll();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            ArrayList<File> files = new ArrayList<>();
            files = listFiles(fc.getSelectedFile(), files);

            for (File file : files) {
                if (file.getName().endsWith(".mp3")) {
                    addFileToTable(file);
                }

            }
        } else {
            updateConsole("No file(s) chosen!");
        }


    }//GEN-LAST:event_openMenuItemActionPerformed

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
                System.out.println("Scroll click");
                //File f = (File) model.getValueAt(table.getSelectedRow(), 1);
                //updateConsole(f.getPath());
                break;

            default:
                break;
        }

        // find out what to do when row(s) are selected
        if (table.getSelectedRowCount() > 1) {
            //System.out.println("multiple rows selected");
            enableMultPanel(true);
            setMultiplePanelFields();
            //table.editCellAt();
//            if (table.isEditing()) {
//                table.getCellEditor().stopCellEditing();
//            }
            //multTitle.requestFocus();
        } else if (table.getSelectedRowCount() == 1) {
            //System.out.println("only one row selected");
            //enableMultPanel(false);
            enableMultPanel(true);
            setMultiplePanelFields();
        } else if (table.getSelectedRowCount() < 1) {
            // no rows selected
        }

    }//GEN-LAST:event_tableMousePressed

    private void tableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyPressed
        //System.out.println("in pressed");
    }//GEN-LAST:event_tableKeyPressed

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

    private void tableKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyTyped
        //System.out.println("in typed");
    }//GEN-LAST:event_tableKeyTyped

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void multImageMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_multImageMousePressed
        showArtworkPopup(evt);
    }//GEN-LAST:event_multImageMousePressed

    private void multTitleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multTitleKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multTitleKeyPressed

    private void multArtistKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multArtistKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multArtistKeyPressed

    private void multAlbumKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multAlbumKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multAlbumKeyPressed

    private void multAlbumArtistKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multAlbumArtistKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multAlbumArtistKeyPressed

    private void multGenreKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multGenreKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multGenreKeyPressed

    private void multYearKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multYearKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multYearKeyPressed

    private void multTrackKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multTrackKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multTrackKeyPressed

    private void multDiskKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multDiskKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multDiskKeyPressed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        autoAddCovers(table.getSelectedRows());
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    public void setMultiplePanelFields() {

        // get the indices of the selected rows
        int[] selectedrows = table.getSelectedRows();
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
        for (int i = 0; i < selectedrows.length; i++) {
            titles[i] = table.getValueAt(selectedrows[i], 2).toString();
            artists[i] = table.getValueAt(selectedrows[i], 3).toString();
            albums[i] = table.getValueAt(selectedrows[i], 4).toString();
            albumartists[i] = table.getValueAt(selectedrows[i], 5).toString();
            years[i] = table.getValueAt(selectedrows[i], 6).toString();
            genres[i] = table.getValueAt(selectedrows[i], 7).toString();
            tracks[i] = table.getValueAt(selectedrows[i], 8).toString();
            disks[i] = table.getValueAt(selectedrows[i], 9).toString();
            images[i] = songs.get(getIndex(selectedrows[i])).getArtwork_bytes();
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

    public void updateMultPanelFields() {
        int[] selectedRows = table.getSelectedRows();

        String title = multTitle.getText();
        String artist = multArtist.getText();
        String album = multAlbum.getText();
        String albumArtist = multAlbumArtist.getText();
        String genre = multGenre.getText();
        String year = multYear.getText();
        String track = multTrack.getText();
        String disk = multDisk.getText();

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
                //edited_songs.add(index);
                songEdited(index);
            }
        }

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
                //edited_songs.add(index);
                songEdited(index);
            }
        }

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
                //edited_songs.add(index);
                songEdited(index);
            }
        }

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
                //edited_songs.add(index);
                songEdited(index);
            }
        }

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
                //edited_songs.add(index);
                songEdited(index);
            }
        }

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
                //edited_songs.add(index);
                songEdited(index);
            }
        }

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
                //edited_songs.add(index);
                songEdited(index);
            }
        }

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
                //edited_songs.add(index);
                songEdited(index);
            }
        }
    }

    public ArrayList<File> listFiles(File directory, ArrayList<File> files) {

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listFiles(file, files);
            }
        }
        return files;
    }

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

    public boolean checkIfSame(String str, String[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (!arr[i].equals(str)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkIfSame(byte[] bytes, byte[][] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (!Arrays.equals(arr[i], bytes)) {
                return false;
            }
        }
        return true;
    }

    public int getHighestIndex() {
        int index = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            int index_to_compare = getIndex(i);
            if (index_to_compare > index) {
                index = index_to_compare;
            }
        }
        return index;
    }

    public void saveAll() {

        for (int i = 0; i < songs.size(); i++) {

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

                String type = "image/jpeg";
                mp3file.getId3v2Tag().clearAlbumImage();
                mp3file.getId3v2Tag().setAlbumImage(s.getArtwork_bytes(), type);

                save(mp3file, file);

                setRowIcon(SAVED, getRow(i));
            } else {
                // skip it, no changes
            }
        }

    }

    public int getRow(int index) {
        int highest = getHighestIndex();
        for (int i = 0; i <= highest; i++) {
            if (i == index) {
                return i;
            }
        }
        return -1;
    }
    
    public int getIndex(int row) {
        return Integer.valueOf(model.getValueAt(row, 12).toString());
    }

    public void removeAlbumArt(int[] selectedRows) {

        for (int i = 0; i < selectedRows.length; i++) {

            int row = table.convertRowIndexToModel(selectedRows[i]);
            int index = getIndex(row);
            File file = songs.get(index).getFile();
            Mp3File mp3file = null;
            try {
                mp3file = new Mp3File(file.getAbsolutePath());
            } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                System.err.println(ex);
            }

            ID3v2 id3v2Tag;
            if (mp3file.hasId3v2Tag()) {
                id3v2Tag = mp3file.getId3v2Tag();
            } else {
                id3v2Tag = null;
            }

            id3v2Tag.clearAlbumImage();

            songs.get(index).setArtwork_bytes(null);
            //edited_songs.add(index);
            songEdited(index);

            model.setValueAt(null, row, 11);

            multImage.setIcon(null);
        }

    }

    public void addAlbumArt(int[] selectedRows) {

        File img_file = null;

        for (int i = 0; i < selectedRows.length; i++) {

            try {
                int index = Integer.valueOf(model.getValueAt(selectedRows[i], 12).toString());
                File file = songs.get(index).getFile();
                Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                ID3v2 id3v2Tag = null;

                if (mp3file.hasId3v2Tag()) {
                    id3v2Tag = mp3file.getId3v2Tag();
                } else {
                    // do nothing
                }

                // only show the JFileChooser on the first go
                if (i == 0) {
                    JFileChooser fc = new JFileChooser(new File(file.getAbsolutePath()));
                    fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "tif"));
                    int returnVal = fc.showOpenDialog(null);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        img_file = fc.getSelectedFile();
                    } else {
                        return;
                    }
                }

                RandomAccessFile ra_file = new RandomAccessFile(img_file.getAbsolutePath(), "r");
                byte[] bytes = new byte[(int) ra_file.length()];
                ra_file.read(bytes);
                ra_file.close();

                songs.get(index).setArtwork_bytes(bytes);

                Icon thumbnail_icon = null;
                try {
                    // getting the image from the byte array
                    ImageIcon icon = new ImageIcon(bytes);
                    Image img = icon.getImage();
                    Image thumbnail = img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                    thumbnail_icon = new ImageIcon(thumbnail);
                    //this.artwork = new ImageIcon(img_scaled);

                    //iipreview.setIcon(thumbnail_icon);
                } catch (NullPointerException e) {
                    System.err.println(e);
                }

                model.setValueAt(thumbnail_icon, selectedRows[i], 11);

                //edited_songs.add(index);
                songEdited(index);

                if (table.getSelectedRowCount() > 1) {
                    // getting the image from the byte array
                    ImageIcon icon = new ImageIcon(bytes);
                    Image img = icon.getImage();
                    Image thumbnail = img.getScaledInstance(166, 166, java.awt.Image.SCALE_SMOOTH);
                    ImageIcon artwork_icon = new ImageIcon(thumbnail);
                    multImage.setIcon(artwork_icon);
                }

            } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

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

    void showArtworkPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        popup.add(item = new JMenuItem("Add"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Remove"));
        item.addActionListener(menuListener);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    void showRegularPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        popup.add(item = new JMenuItem("Remove from list"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Play"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Show Index"));
        item.addActionListener(menuListener);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    void showFilePopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        popup.add(item = new JMenuItem("Remove from list"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Play"));
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
    private javax.swing.JTextArea console;
    private javax.swing.JScrollPane consoleSP;
    private javax.swing.JPanel container;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
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
    private javax.swing.JButton saveButton;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableSP;
    // End of variables declaration//GEN-END:variables

}
