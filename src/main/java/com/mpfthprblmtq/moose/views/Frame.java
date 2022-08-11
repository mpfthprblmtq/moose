/*
   Proj:   Moose
   File:   Frame.java
   Desc:   Main UI class for the JFrame containing the everything.
           Works with the SongController to edit albums, this class just handles all the UI.

   Copyright Pat Ripley 2018
 */

// package
package com.mpfthprblmtq.moose.views;

// imports

import java.awt.*;

import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.commons.utils.WebUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.controllers.SongController;
import com.mpfthprblmtq.moose.objects.Settings;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.services.AutocompleteService;
import com.mpfthprblmtq.moose.utilities.Constants;
import com.mpfthprblmtq.moose.utilities.IconUtils;
import com.mpfthprblmtq.moose.utilities.ImageUtils;
import com.mpfthprblmtq.moose.utilities.viewUtils.*;
import com.mpfthprblmtq.moose.views.modals.InfoFrame;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;

import static com.mpfthprblmtq.moose.utilities.Constants.*;

// class Frame
public class Frame extends javax.swing.JFrame {

    // logger object
    Logger logger = Moose.getLogger();

    // controller
    public SongController songController = Moose.getSongController();

    // some graphics ivars
    ActionListener menuListener;        // listener for the popup menu objects

    int currentRow;     // keeps track of the current row
    int currentColumn;  // keeps track of the current column

    // table model used, with some customizations and overrides
    DefaultTableModel model = ViewUtils.getTableModel();

    // ivars for the multPanel to check if the artwork has changed in the multPanel
    byte[] originalMultPanelArtwork;
    byte[] newMultPanelArtwork;
    boolean multipleArtworks = false;

    /**
     * Creates new form Frame
     */
    public Frame() {

        // init the components
        // checks if we're in the EDT to prevent NoSuchElementExceptions and ArrayIndexOutOfBoundsExceptions
        if (SwingUtilities.isEventDispatchThread()) {
            initComponents();
            init();
        } else {
            SwingUtilities.invokeLater(() -> {
                initComponents();
                init();
            });
        }

        // set up the song controller
        songController.setTable(table);
    }

    /**
     * Creates new form Frame with a folder preloaded
     *
     * @param folder, the folder we want to start with
     */
    public Frame(File folder) {

        // init the components
        // checks if we're in the EDT to prevent NoSuchElementExceptions and ArrayIndexOutOfBoundsExceptions
        if (SwingUtilities.isEventDispatchThread()) {
            initComponents();
            init();

            // set up the song controller
            songController.setTable(table);

            // add the songs in the folder param to start
            List<File> files = new ArrayList<>();
            FileUtils.listFiles(folder, files);

            if (!importFiles(files).isEmpty()) {
                setActionsEnabled(true);
                enableMultPanel(true);
                updateMultiplePanelFields();
                if (Moose.getSettings().getFeatures().get(Settings.CHECK_FOR_NEW_GENRES)) {
                    checkForNewGenres(files);
                }
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                initComponents();
                init();

                // set up the song controller
                songController.setTable(table);

                // add the songs in the folder param to start
                List<File> files = new ArrayList<>();
                FileUtils.listFiles(folder, files);

                if (!importFiles(files).isEmpty()) {
                    setActionsEnabled(true);
                    enableMultPanel(true);
                    updateMultiplePanelFields();
                    if (Moose.getSettings().getFeatures().get(Settings.CHECK_FOR_NEW_GENRES)) {
                        checkForNewGenres(files);
                    }
                }
            });
        }
    }

    /**
     * More init stuff
     */
    public void init() {

        // set the table's model to the custom model
        table.setModel(model);

        // set the songController's table
        songController.setTable(table);

        // mouse event listener to listen for clicking on the table outside the available rows
        // deselects the current row selection
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event.getID() == MouseEvent.MOUSE_CLICKED) {
                MouseEvent mouseEvent = (MouseEvent) event;
                if (table.rowAtPoint(mouseEvent.getPoint()) == -1) {
                    table.clearSelection();
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);

        // listener for window closing, prevents window from closing with unsaved changes
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeWindow();
            }
        });

        // listener for the context menu when you right-click on a row
        // basically tells the program where to go based on the user's choice
        this.menuListener = (ActionEvent event) -> {

            // get all the rows selected
            int[] selectedRows = table.getSelectedRows();

            // switch based on the option selected
            switch (event.getActionCommand()) {
                case MORE_INFO:
                    openMoreInfo(false, null, selectedRows);
                    break;
                case SHOW_IN_FINDER:
                    showInFolder(selectedRows);
                    break;
                case REMOVE_FROM_LIST:
                    removeRows(selectedRows);
                    break;
                case PLAY:
                    songController.playFiles(selectedRows);
                    break;
                case SAVE:
                    songController.saveTracks(selectedRows);
                    break;
                case AUTO_TAG:
                    autoTag(selectedRows);
                    break;
                case AUTO_TRACK_DISK_NUMBERS:
                    songController.autoTaggingService.addTrackAndDiskNumbers(selectedRows);
                    updateMultiplePanelFields();
                    break;
                case AUTO_ARTWORK:
                    autoAddCoverArt(selectedRows);
                    break;
                case MOVE_FILE:
                    songController.moveFiles(selectedRows);
                    break;
                case FORMAT_FILENAME:
                    formatFilenames(selectedRows);
                    break;
                case ADD_ARTWORK:
                    songController.autoTaggingService.addAlbumArt(selectedRows);
                    updateMultiplePanelFields();
                    break;
                case ADD_ARTWORK_SELECTED:
                    getCoverArtForMultPanel(selectedRows);
                    break;
                case REMOVE_ARTWORK:
                    songController.removeAlbumArt(selectedRows);
                    break;
                case REMOVE_ARTWORK_SELECTED:
                    newMultPanelArtwork = null;
                    multImage.setIcon(null);
                    break;
                default:
                    break;
            }
        }; // end menuListener

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
        model.addColumn("I");

        // remove the File and Index columns
        table.removeColumn(table.getColumnModel().getColumn(1));
        table.removeColumn(table.getColumnModel().getColumn(11));

        // set the widths of the columns
        // file name and title are left out, so they can take the remainder of the space dynamically
        ViewUtils.setColumnWidth(table, 0, 12);     // row icon
        ViewUtils.setColumnWidth(table, 3, 150);    // artist
        ViewUtils.setColumnWidth(table, 4, 150);    // album
        ViewUtils.setColumnWidth(table, 5, 150);    // album artist
        ViewUtils.setColumnWidth(table, 6, 80);     // year
        ViewUtils.setColumnWidth(table, 7, 150);    // genre
        ViewUtils.setColumnWidth(table, 8, 50);     // track
        ViewUtils.setColumnWidth(table, 9, 50);     // disk
        ViewUtils.setColumnWidth(table, 10, 100);   // album art
//        ViewUtils.setColumnWidth(table, 11, 20);    // index

        // taken from the FileDrop example
        new FileDrop(System.out, tableSP, (File[] files) -> {

            // create an arraylist of files and traverse it
            List<File> fileList = new ArrayList<>();
            for (File file : files) {
                if (file.isDirectory()) {
                    FileUtils.listFiles(file, fileList);
                } else {
                    fileList.add(file);
                }
            }

            // sort the file list
            fileList.sort(Comparator.comparing(File::getName));

            // import them all
            List<File> successfullyAddedFiles = importFiles(fileList);

            // check to see if the actions can be enabled
            setActionsEnabled(!successfullyAddedFiles.isEmpty());

            // check for new genres
            if (Moose.getSettings().getFeatures().get(Settings.CHECK_FOR_NEW_GENRES)) {
                checkForNewGenres(successfullyAddedFiles);
            }
        });

        // create a customized cell editor
        DefaultCellEditor editor = ViewUtils.getCellEditor();
        table.setCellEditor(editor);

        // create a table cell listener
        TableCellListener tcl = ViewUtils.createTCL(table, songController);
        if (tcl.getTable() == null) {
            // this line is really just to get rid of the "unused var" warning
            logger.logError("TCL table is null!");
        }
    }

    /**
     * Returns the song controller
     *
     * @return the song controller
     */
    public SongController getSongController() {
        return this.songController;
    }

    /**
     * Scans all the files and the mp3tags with them and checks to make sure we
     * know the genre
     *
     * @param list, the list of either Songs or Files to check
     */
    public void checkForNewGenres(List<?> list) {

        List<Song> songs = new ArrayList<>();

        // if the list given is a list of files, get the songs from those files first
        if (!list.isEmpty() && list.get(0) instanceof File) {
            for (Object file : list) {
                songs.add(songController.getSongFromFile((File) file));
            }
        } else {
            for (Object song : list) {
                songs.add((Song) song);
            }
        }

        // get all the songs, then the genres from the list of files
        List<String> genres = songs.stream().map(Song::getGenre).collect(Collectors.toList());

        // create a list of all the genres that don't exist already
        List<String> newGenres = new ArrayList<>();
        genres.stream().filter((genre) -> (!Moose.getSettings().getGenres().contains(genre) && StringUtils.isNotEmpty(genre))).forEachOrdered((genre) -> {
            if (!newGenres.contains(genre)) {
                newGenres.add(genre);
            }
        });

        // for each new genre, ask if we want to add that one
        for (String newGenre : newGenres) {
            int res = JOptionPane.showConfirmDialog(Moose.frame, "\"" + newGenre + "\" isn't in your built-in genre list, would you like to add it?");
            if (res == JOptionPane.YES_OPTION) {// add the genre to the settings and update
                Moose.getSettings().getGenres().add(newGenre);
                Moose.updateSettings();
            }
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

        setActionsEnabled(table.getRowCount() > 0);
    }

    /**
     * Helper function to set the row icon based on the action of the row.
     *
     * @param icon, the icon to set
     * @param row,  the row to set
     */
    public void setRowIcon(int icon, int row) {
        switch (icon) {
            case DEFAULT:
                table.setValueAt(IconUtils.get(IconUtils.DEFAULT), row, TABLE_COLUMN_ICON);
                break;
            case EDITED:
                table.setValueAt(IconUtils.get(IconUtils.EDITED), row, TABLE_COLUMN_ICON);
                break;
            case SAVED:
                table.setValueAt(IconUtils.get(IconUtils.SAVED), row, TABLE_COLUMN_ICON);
                break;
        }
    }

    /**
     * Adds the file and all of its pertinent information to the table as a row
     * Works with the fileDrop functionality
     *
     * @param file, the file to add
     * @return the result of the file add
     */
    public boolean addFileToTable(File file) {

        // check to make sure we're not adding duplicate files
        List<File> filesInTable = songController.getAllFilesInTable();
        if (filesInTable.contains(file)) {
            return false;
        }

        // check if the file is an mp3
        if (!file.getAbsolutePath().endsWith(".mp3")) {
            return false;
        }

        String cleanedFileName = file.getName()
                .replace(".mp3", StringUtils.EMPTY)
                .replace(":", "/");

        int index = songController.getSongs().size();
        Song s = songController.getSongFromFile(file);

        // getting the image to put on the table
        Icon thumbnail_icon = ImageUtils.getScaledImage(s.getArtwork_bytes(), 100);

        // add the row to the table
        model.addRow(new Object[]{
                IconUtils.get(IconUtils.DEFAULT), // adds the default status icon
                s.getFile(), // hidden file object
                cleanedFileName, // actual editable file name
                s.getTitle(),
                s.getArtist(),
                s.getAlbum(),
                s.getAlbumArtist(),
                s.getYear(),
                s.getGenre(),
                s.getFullTrackString(),
                s.getFullDiskString(),
                thumbnail_icon,
                index // hidden index for the song object
        });

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
     * Sets the menu items to enabled/disabled depending on the features enabled in settings
     */
    public void updateMenuItems() {
        autoTagMenuItem.setEnabled(Moose.getSettings().getFeatures().get(Settings.AUTOTAGGING));
        formatFilenamesMenuItem.setEnabled(Moose.getSettings().getFeatures().get(Settings.FORMAT_FILENAMES));
    }

    /**
     * Function used to import files to the table
     *
     * @param files, the files to import
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<File> importFiles(List<File> files) {

        List<File> filesToRemove = new ArrayList<>();
        List<File> hiddenFilesToIgnore = new ArrayList<>();
        AtomicInteger duplicateFiles = new AtomicInteger();

        // iterate through the files and try to add them
        files.forEach((file) -> {

            if (file.getName().endsWith(".mp3")) {

                // check to make sure it's a valid mp3 file (not blank file name)
                if (StringUtils.isEmpty(file.getName().replace(".mp3", StringUtils.EMPTY))) {
                    hiddenFilesToIgnore.add(file);
                } else {
                    // try to add it to the table
                    // if no luck, remove it from the file list and increment the duplicate file count
                    // since the only chance we'd get a false from that method is the case of a duplicate file
                    if (!addFileToTable(file)) {
                        duplicateFiles.getAndIncrement();
                        filesToRemove.add(file);
                    }
                }

            } else if (file.getName().startsWith(".")) {
                // just straight up skip it
                hiddenFilesToIgnore.add(file);
            } else {
                filesToRemove.add(file);
            }
        });

        filesToRemove.forEach(files::remove);
        hiddenFilesToIgnore.forEach(files::remove);
        int duplicates = duplicateFiles.get();

        // sorts the table on the filename, then the album by default
        DefaultRowSorter sorter = ((DefaultRowSorter) table.getRowSorter());
        ArrayList<RowSorter.SortKey> list = new ArrayList<>();

        list.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sorter.setSortKeys(list);
        sorter.sort();

        list.add(new RowSorter.SortKey(5, SortOrder.ASCENDING));
        sorter.setSortKeys(list);
        sorter.sort();

        // update the log table when you're done with the file iteration
        // including all possible iterations of file combinations, because I hate myself
        if (!files.isEmpty() && filesToRemove.isEmpty() && duplicates == 0) {
            // all files were mp3s
            updateConsole(files.size() + " mp3 file(s) loaded!");
        } else if (!files.isEmpty() && !filesToRemove.isEmpty() && duplicates == 0) {
            // some mp3s, some bad files
            updateConsole(files.size() + " mp3 file(s) loaded, " + filesToRemove.size() +
                    (filesToRemove.size() > 1 ? " files weren't mp3s!" : " file wasn't an mp3!"));
        } else if (!files.isEmpty() && !filesToRemove.isEmpty() && duplicates > 0) {
            // some mp3s, some bad files, some duplicates
            updateConsole(files.size() + " mp3 file(s) loaded, " + filesToRemove.size() +
                    (filesToRemove.size() > 1 ? " files weren't mp3s, " : " file wasn't an mp3, ") +
                    duplicates + " duplicate file(s) skipped.");
        } else if (!files.isEmpty() && filesToRemove.isEmpty() && duplicates > 0) {
            // some mp3s, some duplicates
            updateConsole(files.size() + " mp3 file(s) loaded, " + duplicates + " duplicate file(s) skipped.");
        } else if (files.isEmpty() && !filesToRemove.isEmpty() && duplicates == 0) {
            // just non mp3s
            updateConsole("No files provided were mp3s!");
        } else if (files.isEmpty() && filesToRemove.isEmpty() && duplicates > 0) {
            // just duplicates
            updateConsole("No mp3 files loaded, " + duplicates + " duplicate files skipped.");
        } else if (files.isEmpty() && !filesToRemove.isEmpty() && duplicates > 0) {
            // just bad files and duplicates
            updateConsole("No mp3s loaded, " + filesToRemove.size() + " invalid file(s) provided and " +
                    duplicates + " duplicate files skipped.");
        } else {
            updateConsole("No files loaded!");
        }

        // return the list of successfully added files
        return files;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        container = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        saveAllButton = new javax.swing.JButton();
        tableSP = new javax.swing.JScrollPane();
        table = new JTable() {
            public Component prepareEditor(TableCellEditor editor, int row, int col)
            {
                Component result = super.prepareEditor(editor, row, col);
                if (result instanceof JTextField) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            String originalText = ((JTextField)result).getText();
                            ((JTextField)result).setDocument(
                                new AutoCompleteDocument(
                                    AutocompleteService.getNameService(table.getEditingColumn() == TABLE_COLUMN_GENRE, table),
                                    ((JTextField)result)
                                )
                            );
                            ((JTextField)result).setText(originalText);
                            ((JTextField)result).requestFocus();
                            ((JTextField)result).selectAll();
                        }
                    });
                }
                return result;
            }
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
        jLabel3 = new javax.swing.JLabel();
        clearAllButton = new javax.swing.JButton();
        openAllButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveTrackMenuItem = new javax.swing.JMenuItem();
        saveAllMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        selectAllMenuItem = new javax.swing.JMenuItem();
        macroMenu = new javax.swing.JMenu();
        autoTagMenuItem = new javax.swing.JMenuItem();
        addCoversMenuItem = new javax.swing.JMenuItem();
        findAndReplaceMenuItem = new javax.swing.JMenuItem();
        addTrackNumbersMenuItem = new javax.swing.JMenuItem();
        formatFilenamesMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        auditMenuItem = new javax.swing.JMenuItem();
        analyticsMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        wikiMenuItem = new javax.swing.JMenuItem();
        commandMenuItem = new javax.swing.JMenuItem();
        settingsMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Moose");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel1.setText("Moose");

        saveAllButton.setText("Save All");
        saveAllButton.setFocusable(false);
        saveAllButton.setMaximumSize(new java.awt.Dimension(100, 68));
        saveAllButton.setMinimumSize(new java.awt.Dimension(100, 68));
        saveAllButton.setPreferredSize(new java.awt.Dimension(100, 68));
        saveAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAllButtonActionPerformed(evt);
            }
        });

        table.setAutoCreateRowSorter(true);
        table.setModel(model);
        table.setRequestFocusEnabled(false);
        table.setRowHeight(20);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setShowGrid(true);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
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
        console.setMaximumSize(new java.awt.Dimension(611, 219));
        console.setMinimumSize(new java.awt.Dimension(611, 219));
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

        multTitle.setEnabled(false);
        multTitle.setMaximumSize(new java.awt.Dimension(250, 26));
        multTitle.setMinimumSize(new java.awt.Dimension(250, 26));
        multTitle.setNextFocusableComponent(multArtist);
        multTitle.setPreferredSize(new java.awt.Dimension(250, 26));
        multTitle.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                multTitleFocusGained(evt);
            }
        });
        multTitle.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multTitleKeyPressed(evt);
            }
        });

        multArtist.setEnabled(false);
        multArtist.setMaximumSize(new java.awt.Dimension(250, 26));
        multArtist.setMinimumSize(new java.awt.Dimension(250, 26));
        multArtist.setNextFocusableComponent(multAlbum);
        multArtist.setPreferredSize(new java.awt.Dimension(250, 26));
        multArtist.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                multArtistFocusGained(evt);
            }
        });
        multArtist.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multArtistKeyPressed(evt);
            }
        });

        multAlbum.setEnabled(false);
        multAlbum.setMaximumSize(new java.awt.Dimension(250, 26));
        multAlbum.setMinimumSize(new java.awt.Dimension(250, 26));
        multAlbum.setNextFocusableComponent(multAlbumArtist);
        multAlbum.setPreferredSize(new java.awt.Dimension(250, 26));
        multAlbum.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                multAlbumFocusGained(evt);
            }
        });
        multAlbum.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multAlbumKeyPressed(evt);
            }
        });

        multAlbumArtist.setEnabled(false);
        multAlbumArtist.setMaximumSize(new java.awt.Dimension(250, 26));
        multAlbumArtist.setMinimumSize(new java.awt.Dimension(250, 26));
        multAlbumArtist.setNextFocusableComponent(multGenre);
        multAlbumArtist.setPreferredSize(new java.awt.Dimension(250, 26));
        multAlbumArtist.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                multAlbumArtistFocusGained(evt);
            }
        });
        multAlbumArtist.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multAlbumArtistKeyPressed(evt);
            }
        });

        multGenre.setEnabled(false);
        multGenre.setMaximumSize(new java.awt.Dimension(150, 26));
        multGenre.setMinimumSize(new java.awt.Dimension(150, 26));
        multGenre.setNextFocusableComponent(multYear);
        multGenre.setPreferredSize(new java.awt.Dimension(150, 26));
        multGenre.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                multGenreFocusGained(evt);
            }
        });
        multGenre.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multGenreKeyPressed(evt);
            }
        });

        multYear.setEnabled(false);
        multYear.setMaximumSize(new java.awt.Dimension(100, 26));
        multYear.setMinimumSize(new java.awt.Dimension(100, 26));
        multYear.setNextFocusableComponent(multTrack);
        multYear.setPreferredSize(new java.awt.Dimension(100, 26));
        multYear.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                multYearFocusGained(evt);
            }
        });
        multYear.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multYearKeyPressed(evt);
            }
        });

        multTrack.setEnabled(false);
        multTrack.setMaximumSize(new java.awt.Dimension(50, 26));
        multTrack.setMinimumSize(new java.awt.Dimension(50, 26));
        multTrack.setNextFocusableComponent(multDisk);
        multTrack.setPreferredSize(new java.awt.Dimension(50, 26));
        multTrack.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                multTrackFocusGained(evt);
            }
        });
        multTrack.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multTrackKeyPressed(evt);
            }
        });

        multDisk.setEnabled(false);
        multDisk.setMaximumSize(new java.awt.Dimension(50, 26));
        multDisk.setMinimumSize(new java.awt.Dimension(50, 26));
        multDisk.setNextFocusableComponent(multTitle);
        multDisk.setPreferredSize(new java.awt.Dimension(50, 26));
        multDisk.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                multDiskFocusGained(evt);
            }
        });
        multDisk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                multDiskKeyPressed(evt);
            }
        });

        multImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        multImage.setText(" ");
        multImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        multImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        multImage.setIconTextGap(0);
        multImage.setMaximumSize(new java.awt.Dimension(156, 156));
        multImage.setMinimumSize(new java.awt.Dimension(156, 156));
        multImage.setRequestFocusEnabled(false);
        multImage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                multImageMousePressed(evt);
            }
        });

        multUpdateButton.setText("Update Fields");
        multUpdateButton.setEnabled(false);
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
                            .addComponent(multArtist, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(multAlbum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(multAlbumArtist, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(multTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                                .addComponent(multTrack, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(multDisk, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(multYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(29, 29, 29))
                    .addGroup(multPanelLayout.createSequentialGroup()
                        .addComponent(L1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(multUpdateButton, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addComponent(multImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(L2)
                                    .addComponent(multTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jLabel3.setIcon(IconUtils.get(IconUtils.MOOSE_64));

        clearAllButton.setText("Clear All");
        clearAllButton.setMaximumSize(new java.awt.Dimension(100, 68));
        clearAllButton.setMinimumSize(new java.awt.Dimension(100, 68));
        clearAllButton.setPreferredSize(new java.awt.Dimension(100, 68));
        clearAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAllButtonActionPerformed(evt);
            }
        });

        openAllButton.setText("Open All");
        openAllButton.setMaximumSize(new java.awt.Dimension(100, 68));
        openAllButton.setMinimumSize(new java.awt.Dimension(100, 68));
        openAllButton.setPreferredSize(new java.awt.Dimension(100, 68));
        openAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openAllButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout containerLayout = new javax.swing.GroupLayout(container);
        container.setLayout(containerLayout);
        containerLayout.setHorizontalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerLayout.createSequentialGroup()
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tableSP)
                    .addGroup(containerLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clearAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, containerLayout.createSequentialGroup()
                        .addComponent(consoleSP, javax.swing.GroupLayout.PREFERRED_SIZE, 611, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(multPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        containerLayout.setVerticalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(openAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clearAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableSP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(multPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(consoleSP))
                .addContainerGap())
        );

        fileMenu.setText("File");

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_DOWN_MASK));
        openMenuItem.setText("Open...");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveTrackMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.META_DOWN_MASK));
        saveTrackMenuItem.setText("Save Track");
        saveTrackMenuItem.setEnabled(false);
        saveTrackMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveTrackMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveTrackMenuItem);

        saveAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.META_DOWN_MASK));
        saveAllMenuItem.setText("Save All");
        saveAllMenuItem.setEnabled(false);
        saveAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAllMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAllMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_DOWN_MASK));
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        viewMenu.setText("View");

        selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.META_DOWN_MASK));
        selectAllMenuItem.setText("Select All");
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(selectAllMenuItem);

        jMenuBar1.add(viewMenu);

        macroMenu.setText("Macros");

        autoTagMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.META_DOWN_MASK));
        autoTagMenuItem.setText("AutoTag");
        autoTagMenuItem.setEnabled(false);
        autoTagMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoTagMenuItemActionPerformed(evt);
            }
        });
        macroMenu.add(autoTagMenuItem);

        addCoversMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.META_DOWN_MASK));
        addCoversMenuItem.setText("Add Covers");
        addCoversMenuItem.setEnabled(false);
        addCoversMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCoversMenuItemActionPerformed(evt);
            }
        });
        macroMenu.add(addCoversMenuItem);

        findAndReplaceMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.META_DOWN_MASK));
        findAndReplaceMenuItem.setText("Find and Replace");
        findAndReplaceMenuItem.setEnabled(false);
        findAndReplaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findAndReplaceMenuItemActionPerformed(evt);
            }
        });
        macroMenu.add(findAndReplaceMenuItem);

        addTrackNumbersMenuItem.setText("Add Track Numbers");
        addTrackNumbersMenuItem.setEnabled(false);
        addTrackNumbersMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTrackNumbersMenuItemActionPerformed(evt);
            }
        });
        macroMenu.add(addTrackNumbersMenuItem);

        formatFilenamesMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.META_DOWN_MASK));
        formatFilenamesMenuItem.setText("Format Filenames");
        formatFilenamesMenuItem.setEnabled(false);
        formatFilenamesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatFilenamesMenuItemActionPerformed(evt);
            }
        });
        macroMenu.add(formatFilenamesMenuItem);

        jMenuBar1.add(macroMenu);

        toolsMenu.setText("Tools");

        auditMenuItem.setText("Audit/Cleanup...");
        auditMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auditMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(auditMenuItem);

        analyticsMenuItem.setText("Analytics...");
        toolsMenu.add(analyticsMenuItem);

        jMenuBar1.add(toolsMenu);

        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        wikiMenuItem.setText("Open Wiki...");
        wikiMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wikiMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(wikiMenuItem);

        commandMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.META_DOWN_MASK));
        commandMenuItem.setText("Command Prompt");
        commandMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commandMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(commandMenuItem);

        settingsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_COMMA, java.awt.event.InputEvent.META_DOWN_MASK));
        settingsMenuItem.setText("Settings");
        settingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(settingsMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  MENU BAR ACTIONS
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // <editor-fold defaultstate="collapsed" desc="MENU BAR ACTIONS">

    /**
     * Action for the open menu item, opens a JFileChooser for the user to select a directory to open
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed

        File[] dirs = FileUtils.launchJFileChooser(
                "Select a folder to open...",
                "Open",
                JFileChooser.DIRECTORIES_ONLY,
                true,
                null,
                null);

        if (dirs != null) {
            // create an arraylist of files
            List<File> files = new ArrayList<>();
            for (File dir : dirs) {
                FileUtils.listFiles(dir, files);
            }

            // import the files
            setActionsEnabled(!importFiles(files).isEmpty());

        } else {
            // no files chose, update console
            updateConsole("No file(s) chosen!");
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    /**
     * Action for the save track menu item, saves all selected rows
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void saveTrackMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTrackMenuItemActionPerformed
        if (table.getSelectedRows().length > 0) {
            int[] selectedRows = table.getSelectedRows();
            songController.saveTracks(selectedRows);
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_saveTrackMenuItemActionPerformed

    /**
     * Action for the save all menu item, saves all rows in the table
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void saveAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAllMenuItemActionPerformed
        songController.saveTracks(IntStream.range(0, table.getRowCount()).toArray());
    }//GEN-LAST:event_saveAllMenuItemActionPerformed

    /**
     * Action for the exit menu item
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        closeWindow();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /**
     * Actual closing window method, checks to see if we have any unsaved changes before closing
     */
    public void closeWindow() {
        if (songController.hasUnsavedChanges()) {
            if (DialogUtils.showUnsavedChangesDialog(this) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    /**
     * Action for the select all menu item, selects all the rows on the table
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:selectAllMenuItemActionPerformed
        selectAll();
    }//GEN-LAST:selectAllMenuItemActionPerformed

    /**
     * Selects all rows in the table
     */
    private void selectAll() {
        if (table.getRowCount() > 0) {
            table.selectAll();
        }
        updateMultiplePanelFields();
    }

    /**
     * Action for the Audit menu item, launches the audit frame
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void auditMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auditMenuItemActionPerformed
        Moose.launchAuditFrame();
    }//GEN-LAST:event_auditMenuItemActionPerformed

    /**
     * Action for the Autotag menu item, auto tags the selected rows
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void autoTagMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoTagMenuItemActionPerformed
        if (table.getSelectedRows().length > 0) {
            autoTag(table.getSelectedRows());
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_autoTagMenuItemActionPerformed

    /**
     * Actually does the auto tag call
     *
     * @param selectedRows, the rows to auto tag
     */
    public void autoTag(int[] selectedRows) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        songController.autoTagFiles(selectedRows);
        updateMultiplePanelFields();
    }

    /**
     * Action for the Add Covers menu item, auto adds the covers
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void addCoversMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCoversMenuItemActionPerformed
        if (table.getSelectedRows().length > 0) {
            autoAddCoverArt(table.getSelectedRows());
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_addCoversMenuItemActionPerformed

    /**
     * Actually does the auto add cover art call
     * @param selectedRows, the rows to add cover art to
     */
    public void autoAddCoverArt(int[] selectedRows) {
        songController.autoTaggingService.autoAddCoverArt(selectedRows);
        updateMultiplePanelFields();
    }

    /**
     * Action for the Find and Replace menu item, opens the Find and Replace dialog
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void findAndReplaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findAndReplaceMenuItemActionPerformed
        showFindAndReplaceDialog();
    }//GEN-LAST:event_findAndReplaceMenuItemActionPerformed

    /**
     * Shows a dialog with a find input and replace input
     */
    public void showFindAndReplaceDialog() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        JTextField find = new JTextField();
        JTextField replace = new JTextField();
        JCheckBox includeFilesBox = new JCheckBox();
        includeFilesBox.setText("Include file names");
        includeFilesBox.setSelected(true);
        Object[] message = {"Find:", find, "Replace:", replace, includeFilesBox};

        ViewUtils.focusOnField(find, "Find and Replace");

        int option = JOptionPane.showConfirmDialog(this, message, "Find and Replace", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String findStr = find.getText();
            String replStr = replace.getText();
            boolean includeFiles = includeFilesBox.isSelected();
            int result = songController.findAndReplace(findStr, replStr, includeFiles);
            if (result == 0) {   // nothing to replace
                JOptionPane.showMessageDialog(null, "Nothing to replace!", "Find and Replace", JOptionPane.PLAIN_MESSAGE);
            } else if (result > 0) {
                JOptionPane.showMessageDialog(null, "Successfully made " + result + " replacements!", "Find and Replace", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    /**
     * Action for the Add Track/Disk numbers menu item, auto adds the track numbers and disk numbers
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void addTrackNumbersMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTrackNumbersMenuItemActionPerformed
        if (table.getSelectedRows().length > 0) {
            songController.autoTaggingService.addTrackAndDiskNumbers(table.getSelectedRows());
            updateMultiplePanelFields();
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_addTrackNumbersMenuItemActionPerformed

    /**
     * Action for the Format Filenames menu item, formats the filenames
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void formatFilenamesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatFilenamesMenuItemActionPerformed
        if (table.getSelectedRows().length > 0) {
            formatFilenames(table.getSelectedRows());
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_formatFilenamesMenuItemActionPerformed

    /**
     * Actually does the formatting of filenames
     * @param selectedRows, the selected rows on the table
     */
    public void formatFilenames(int[] selectedRows) {
        songController.formatFilenames(selectedRows);
    }

    /**
     * Action for the About menu item, opens the about dialog
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        DialogUtils.showAboutDialog();
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    /**
     * Action for the Wiki menu item, opens the wiki page
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void wikiMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wikiMenuItemActionPerformed
        try {
            WebUtils.openPage(MOOSE_WIKI);
        } catch (Exception e) {
            logger.logError("Couldn't open web page: " + MOOSE_WIKI, e);
        }
    }//GEN-LAST:event_wikiMenuItemActionPerformed

    /**
     * Action for the Command Prompt menu item, opens a command prompt dialog
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void commandMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commandMenuItemActionPerformed
        // prompt the user to enter a command
        String command = JOptionPane.showInputDialog(this, "Enter a command:");
        if (command != null) {
            doCommand(command);
        }
    }//GEN-LAST:event_commandMenuItemActionPerformed

    /**
     * Performs a command based on the user input
     *
     * @param command, the command to execute
     */
    public void doCommand(String command) {
        command = command.toLowerCase();
        switch (command) {
            case "clear error log":
                Moose.settingsFrame.settingsController.clearErrorLog();
                break;
            case "clear event log":
                Moose.settingsFrame.settingsController.clearEventLog();
                break;
            case "open error log":
                Moose.settingsFrame.settingsController.openErrorLog();
                break;
            case "open event log":
                Moose.settingsFrame.settingsController.openEventLog();
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown Command!");
                break;
        }
    }

    private void settingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsMenuItemActionPerformed
        Moose.launchSettingsFrame();
    }//GEN-LAST:event_settingsMenuItemActionPerformed

    // </editor-fold>

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  MAIN ALL ACTION BUTTONS
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // <editor-fold defaultstate="collapsed" desc="MAIN ALL ACTION BUTTONS">

    /**
     * Action for the Save All button press, saves all tracks currently on the table
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void saveAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAllButtonActionPerformed
        songController.saveTracks(IntStream.range(0, table.getRowCount()).toArray());
    }//GEN-LAST:event_saveAllButtonActionPerformed

    /**
     * Clear All button, shows a dialog if the user has the isAskBeforeClearAll setting enabled
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void clearAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAllButtonActionPerformed
        if (Moose.getSettings().isAskBeforeClearAll()) {
            Boolean result = DialogUtils.showClearAllDialog(this);
            if (result != null) {
                boolean dontAskAgain = result;
                if (dontAskAgain) {
                    Moose.getSettings().setAskBeforeClearAll(false);
                    Moose.settingsFrame.settingsController.writeSettingsFile(Moose.getSettings());
                }
                clearAll();
            }
        } else {
            clearAll();
        }
        Moose.songController = new SongController();
    }//GEN-LAST:event_clearAllButtonActionPerformed

    /**
     * Clears the table and all songs, basically resets everything
     */
    public void clearAll() {
        model.setRowCount(0);
        table.removeAll();
        songController.getSongs().clear();
        enableMultPanel(false);
        setActionsEnabled(false);
    }

    /**
     * Action for the Open All button press, opens all the tracks currently on the table
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void openAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openAllButtonActionPerformed
        for (int i = 0; i < table.getRowCount(); i++) {
            File file = songController.autoTaggingService.getFile(i);
            try {
                FileUtils.openFile(file);
            } catch (Exception e) {
                logger.logError("Couldn't open file!", e);
            }
        }

    }//GEN-LAST:event_openAllButtonActionPerformed

    // </editor-fold>

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  TABLE EDITING/NAVIGATION
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // <editor-fold defaultstate="collapsed" desc="TABLE EDITING/NAVIGATION">

    /**
     * What happens if we click on the table
     *
     * @param evt, the MouseEvent we use to determine what to do (left, right, scroll/middle click)
     */
    private void tableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMousePressed

        int row = table.rowAtPoint(evt.getPoint());
        int col = table.columnAtPoint(evt.getPoint());
        int rows = table.getSelectedRowCount();
        int[] selectedRows = table.getSelectedRows();

        // check what type of click
        switch (evt.getButton()) {

            // if it's a right click, show context menu
            case java.awt.event.MouseEvent.BUTTON3:
                if (row >= 0 && col >= 0) {
                    switch (col) {
                        case 10:
                            ViewUtils.showPopUpContextMenu(
                                    evt, menuListener, rows, true, false, true, false, null);
                            break;
                        case 1:
                            ViewUtils.showPopUpContextMenu(
                                    evt, menuListener, rows, true, true, false, false, null);
                            break;
                        default:
                            ViewUtils.showPopUpContextMenu(
                                    evt, menuListener, rows, true, false, false, false, null);
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
                    if (col == 10) {
                        ViewUtils.showPopUpContextMenu(evt, menuListener, table.getSelectedRowCount(), true, false, true, false, null);
                    } else {
                        currentRow = row;
                        currentColumn = col;
                        table.getEditorComponent().requestFocusInWindow();
                    }
                }
                break;

            // if it's a scroll click, open files
            case java.awt.event.MouseEvent.BUTTON2:
                for (int selectedRow : selectedRows) {
                    try {
                        FileUtils.openFile(songController.autoTaggingService.getFile(selectedRow));
                    } catch (Exception e) {
                        logger.logError("Couldn't open file!", e);
                    }
                }
                break;

            default:
                break;
        }

        // find out what to do when row(s) are selected
        if (table.getSelectedRowCount() >= 1) {
            enableMultPanel(true);
            updateMultiplePanelFields();
        }

    }//GEN-LAST:event_tableMousePressed

    /**
     * What happens when we're editing a cell and press a key, mainly used for Enter/Tab navigation
     *
     * @param evt, the KeyEvent we use to determine where to go
     */
    private void tableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_META) {
            evt.consume();
        } else {
            changeSelection(evt);
        }
    }//GEN-LAST:event_tableKeyReleased

    private void tableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_META || evt.isMetaDown()) {
            evt.consume();
        }
    }//GEN-LAST:event_tableKeyPressed

    /**
     * Function that selects the cell being edited. Used mainly when pressing tab or enter to navigate.
     * This is one of those methods that just works, and it's best not to mess with it.
     *
     * @param evt, the KeyEvent that we use to determine the type of navigation (Enter, Tab, Shift+Enter, Shift+Tab)
     */
    public void changeSelection(KeyEvent evt) {

        // check if it's an automatic action
        if (evt.getKeyCode() == KeyEvent.VK_A && evt.isMetaDown()) {
            selectAll();
        }

        // changes to the row/column
        int rowDelta;
        int columnDelta;

        // figure out what to do based on how we got here
        if (evt.getKeyCode() == KeyEvent.VK_ENTER && !evt.isShiftDown()) {
            // if on last row, go to the first row
            rowDelta = currentRow + 1;
            currentRow = rowDelta > table.getRowCount() - 1 ? 0 : currentRow + 1;
            editCell();

        } else if (evt.getKeyCode() == KeyEvent.VK_TAB && !evt.isShiftDown()) {
            // if on last column, go to the first column and the next row
            // also check if we're on the last row, go to the first row too if we are
            columnDelta = currentColumn + 1;
            if (columnDelta > table.getColumnCount() - 2) {
                currentColumn = 1;
                currentRow = currentRow >= table.getRowCount() - 1 ? 0 : currentRow + 1;
            } else {
                currentColumn++;
            }
            editCell();

        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER && evt.isShiftDown()) {
            // if on first row, go to the last row
            rowDelta = currentRow - 1;
            currentRow = rowDelta < 0 ? table.getRowCount() - 1 : currentRow - 1;
            editCell();

        } else if (evt.getKeyCode() == KeyEvent.VK_TAB && evt.isShiftDown()) {
            // if on first column, go to the last column and the previous row
            // also check if we're on the first row, go to last row too if we are
            columnDelta = currentColumn - 1;
            if (columnDelta < 1) {
                currentColumn = table.getColumnCount() - 2;
                currentRow = currentRow <= 0 ? table.getRowCount() - 1 : currentRow - 1;
            } else {
                currentColumn--;
            }
            editCell();

        } else {
            evt.consume();
        }
    }

    /**
     * Actually edits the cell using the currentRow and currentColumn ivars
     */
    private void editCell() {
        // edit that cell
        table.editCellAt(currentRow, currentColumn);

        // make sure we're selecting the row
        table.setRowSelectionInterval(currentRow, currentRow);

        // set the view to the row we're editing
        table.scrollRectToVisible(table.getCellRect(table.getEditingRow(), table.getEditingColumn(), false));

        // TODO make this better
        updateMultiplePanelFields();
    }

    // </editor-fold>

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  MULT PANEL EVENTS/METHODS
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // <editor-fold defaultstate="collapsed" desc="MULT PANEL EVENTS/METHODS">

    /**
     * What happens when the multPanel update button is clicked
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void multUpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multUpdateButtonActionPerformed
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows != null && selectedRows.length == 0) {
            selectedRows = IntStream.range(0, table.getRowCount() - 1).toArray();
        }
        updateTableFromMultPanelFields(selectedRows);
    }//GEN-LAST:event_multUpdateButtonActionPerformed

    /**
     * Opens the context menu for album art on clicking on the multImage box
     *
     * @param evt, the mouse event, used to get the x,y of the click
     */
    private void multImageMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_multImageMousePressed
        ViewUtils.showPopUpContextMenu(evt, menuListener, table.getSelectedRowCount(), false, false, false, true, null);
    }//GEN-LAST:event_multImageMousePressed

    /**
     * If enter is pressed while field is in focus, simulate update button click
     *
     * @param evt, the KeyEvent to check against
     */
    private void multTitleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multTitleKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multTitleKeyPressed

    /**
     * If enter is pressed while field is in focus, simulate update button click
     *
     * @param evt, the KeyEvent to check against
     */
    private void multArtistKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multArtistKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multArtistKeyPressed

    /**
     * If enter is pressed while field is in focus, simulate update button click
     *
     * @param evt, the KeyEvent to check against
     */
    private void multAlbumKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multAlbumKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multAlbumKeyPressed

    /**
     * If enter is pressed while field is in focus, simulate update button click
     *
     * @param evt, the KeyEvent to check against
     */
    private void multAlbumArtistKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multAlbumArtistKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multAlbumArtistKeyPressed

    /**
     * If enter is pressed while field is in focus, simulate update button click
     *
     * @param evt, the KeyEvent to check against
     */
    private void multGenreKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multGenreKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multGenreKeyPressed

    /**
     * If enter is pressed while field is in focus, simulate update button click
     *
     * @param evt, the KeyEvent to check against
     */
    private void multYearKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multYearKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multYearKeyPressed

    /**
     * If enter is pressed while field is in focus, simulate update button click
     *
     * @param evt, the KeyEvent to check against
     */
    private void multTrackKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multTrackKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multTrackKeyPressed

    /**
     * If enter is pressed while field is in focus, simulate update button click
     *
     * @param evt, the KeyEvent to check against
     */
    private void multDiskKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multDiskKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multDiskKeyPressed

    /**
     * On focus gain, select all text
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multTitleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multTitleFocusGained
        multTitle.selectAll();
    }//GEN-LAST:event_multTitleFocusGained

    /**
     * On focus gain, update auto complete fields and select all
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multArtistFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multArtistFocusGained
        updateAutocompleteFields(multArtist, false);
        multArtist.selectAll();
    }//GEN-LAST:event_multArtistFocusGained

    /**
     * On focus gain, update auto complete fields and select all
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multAlbumFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multAlbumFocusGained
        updateAutocompleteFields(multAlbum, false);
        multAlbum.selectAll();
    }//GEN-LAST:event_multAlbumFocusGained

    /**
     * On focus gain, update auto complete fields and select all
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multAlbumArtistFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multAlbumArtistFocusGained
        updateAutocompleteFields(multAlbumArtist, false);
        multAlbumArtist.selectAll();
    }//GEN-LAST:event_multAlbumArtistFocusGained

    /**
     * On focus gain, update auto complete fields and select all
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multGenreFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multGenreFocusGained
        updateAutocompleteFields(multGenre, true);
        multGenre.selectAll();
    }//GEN-LAST:event_multGenreFocusGained

    /**
     * On focus gain, update auto complete fields and select all
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multYearFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multYearFocusGained
        updateAutocompleteFields(multYear, false);
        multYear.selectAll();
    }//GEN-LAST:event_multYearFocusGained

    /**
     * On focus gain, select all text
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multTrackFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multTrackFocusGained
        multTrack.selectAll();
    }//GEN-LAST:event_multTrackFocusGained

    /**
     * On focus gain, select all text
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multDiskFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multDiskFocusGained
        multDisk.selectAll();
    }//GEN-LAST:event_multDiskFocusGained

    /**
     * Sets the multiple fields panel based on the data selected
     */
    public void updateMultiplePanelFields() {

        // get the selected rows
        int[] selectedRows;
        int rows;

        if (table.getSelectedRows().length == 0) {
            selectedRows = new int[table.getRowCount()];
            for (int i = 0; i < selectedRows.length; i++) {
                selectedRows[i] = i;
            }
            rows = selectedRows.length;
        } else {
            selectedRows = table.getSelectedRows();
            rows = table.getSelectedRowCount();
        }

        // make the arrays of values
        String[] titles = new String[rows];
        String[] artists = new String[rows];
        String[] albums = new String[rows];
        String[] albumArtists = new String[rows];
        String[] genres = new String[rows];
        String[] years = new String[rows];
        String[] tracks = new String[rows];
        String[] disks = new String[rows];
        byte[][] images = new byte[rows][];

        // fill the arrays
        for (int i = 0; i < selectedRows.length; i++) {

            int row = selectedRows[i];

            titles[i] = table.getValueAt(selectedRows[i], 2).toString();
            artists[i] = table.getValueAt(selectedRows[i], 3).toString();
            albums[i] = table.getValueAt(selectedRows[i], 4).toString();
            albumArtists[i] = table.getValueAt(selectedRows[i], 5).toString();
            years[i] = table.getValueAt(selectedRows[i], 6).toString();
            genres[i] = table.getValueAt(selectedRows[i], 7).toString();
            tracks[i] = table.getValueAt(selectedRows[i], 8).toString();
            disks[i] = table.getValueAt(selectedRows[i], 9).toString();
            images[i] = songController.getSongs().get(songController.getIndex(row)).getArtwork_bytes();
        }

        // fill the fields
        multTitle.setText(StringUtils.checkIfSame(titles[0], Arrays.asList(titles)) ? titles[0] : Constants.DASH);
        multArtist.setText(StringUtils.checkIfSame(artists[0], Arrays.asList(artists)) ? artists[0] : Constants.DASH);
        multAlbum.setText(StringUtils.checkIfSame(albums[0], Arrays.asList(albums)) ? albums[0] : Constants.DASH);
        multAlbumArtist.setText(StringUtils.checkIfSame(albumArtists[0], Arrays.asList(albumArtists)) ? albumArtists[0] : Constants.DASH);
        multGenre.setText(StringUtils.checkIfSame(genres[0], Arrays.asList(genres)) ? genres[0] : Constants.DASH);
        multYear.setText(StringUtils.checkIfSame(years[0], Arrays.asList(years)) ? years[0] : Constants.DASH);
        multTrack.setText(StringUtils.checkIfSame(tracks[0], Arrays.asList(tracks)) ? tracks[0] : Constants.DASH);
        multDisk.setText(StringUtils.checkIfSame(disks[0], Arrays.asList(disks)) ? disks[0] : Constants.DASH);

        if (ImageUtils.checkIfSame(images[0], images) && images[0] != null) {
            multImage.setIcon(ImageUtils.getScaledImage(images[0], 150));
            originalMultPanelArtwork = newMultPanelArtwork = images[0];
            multipleArtworks = false;
        } else {
            List<byte[]> bytesList = ImageUtils.getUniqueByteArrays(Arrays.asList(images));
            multImage.setIcon(new ImageIcon(ImageUtils.combineImages(bytesList, 150)));
            multipleArtworks = true;
        }
    }

    /**
     * Updates the table from the mult panel fields
     *
     * @param selectedRows, the selected rows on the table
     */
    public void updateTableFromMultPanelFields(int[] selectedRows) {

        // create a song from the values in the mult panel to update on the rows
        Song song = new Song();
        song.setTitle(multTitle.getText().equals(DASH) ? null : multTitle.getText());
        song.setArtist(multArtist.getText().equals(DASH) ? null : multArtist.getText());
        song.setAlbum(multAlbum.getText().equals(DASH) ? null : multAlbum.getText());
        song.setAlbumArtist(multAlbumArtist.getText().equals(DASH) ? null : multAlbumArtist.getText());
        song.setYear(multYear.getText().equals(DASH) ? null : multYear.getText());
        song.setGenre(multGenre.getText().equals(DASH) ? null : multGenre.getText());
        if (StringUtils.isNotEmpty(multTrack.getText()) && !multTrack.getText().equals(DASH) && !multTrack.getText().matches(TRACK_DISK_REGEX)) {
            DialogUtils.showMessageDialog(this, "Invalid track input: " + multTrack.getText(), "Warning", JOptionPane.WARNING_MESSAGE);
        } else if (StringUtils.isNotEmpty(multTrack.getText()) && !multTrack.getText().equals(DASH)) {
            String[] arr = multTrack.getText().split("/");
            song.setTrack(arr[0]);
            song.setTotalTracks(arr[1]);
        } else if (StringUtils.isEmpty(multTrack.getText())) {
            song.setTrack("");
            song.setTotalTracks("");
        }
        if (StringUtils.isNotEmpty(multDisk.getText()) && !multDisk.getText().equals(DASH) && !multDisk.getText().matches(TRACK_DISK_REGEX)) {
            DialogUtils.showMessageDialog(this, "Invalid disk input: " + multDisk.getText(), "Warning", JOptionPane.WARNING_MESSAGE);
        } else if (StringUtils.isNotEmpty(multDisk.getText()) && !multDisk.getText().equals(DASH)) {
            String[] arr = multDisk.getText().split("/");
            song.setDisk(arr[0]);
            song.setTotalDisks(arr[1]);
        } else if (StringUtils.isEmpty(multDisk.getText())) {
            song.setDisk("");
            song.setTotalDisks("");
        }

        if (!multipleArtworks) {
            if (!Arrays.equals(originalMultPanelArtwork, newMultPanelArtwork)) {
                song.setArtwork_bytes(newMultPanelArtwork);
            } else {
                song.setArtwork_bytes(originalMultPanelArtwork);
            }
        }

        // update the rows iteratively
        for (Integer row : selectedRows) {
            submitRowChanges(row, song);
        }
    }

    /**
     * Gets the album art for the mult panel
     *
     * @param selectedRows, the selected rows on the table
     */
    public void getCoverArtForMultPanel(int[] selectedRows) {
        int index = songController.getIndex(selectedRows[0]);
        File startingPoint = songController.getSongs().get(index).getFile();
        File image = ImageUtils.selectAlbumArt(startingPoint);
        if (image != null) {
            try {
                BufferedImage bi = ImageIO.read(image);
                newMultPanelArtwork = ImageUtils.getBytesFromBufferedImage(bi);
                multImage.setIcon(ImageUtils.getScaledImage(newMultPanelArtwork, 150));
            } catch (IOException e) {
                logger.logError("IOException when trying to get album art from mult panel!", e);
            }
        }
    }

    // </editor-fold>

    /**
     * Updates the autocomplete selection for the field
     *
     * @param component,    the text field that we're updating
     * @param isGenreField, a boolean to see if it's a genre field
     */
    public void updateAutocompleteFields(JTextField component, boolean isGenreField) {
        String text = component.getText();
        component.setDocument(new AutoCompleteDocument(AutocompleteService.getNameService(isGenreField, table), component));
        component.setText(text);
    }

    /**
     * Gets the info for multiple songs
     *
     * @param editModeEnabled, a boolean to check if we should be in edit mode or not
     * @param focusedField,    the field we want to focus on first
     * @param selectedRows,    the rows currentlyl selected
     */
    public void openMoreInfo(boolean editModeEnabled, Component focusedField, int[] selectedRows) {
        // get the map of songs
        Map<Integer, Song> songs = new HashMap<>();
        for (int row : selectedRows) {
            songs.put(row, songController.getSongs().get(songController.getIndex(row)));
        }

        InfoFrame infoFrame = new InfoFrame(songs, selectedRows, editModeEnabled, focusedField);
        infoFrame.setLocationRelativeTo(this);
        infoFrame.setVisible(true);
        this.setEnabled(false);
    }

    /**
     * Opens the folder where this track lives
     *
     * @param selectedRows, the rows currently selected
     */
    public void showInFolder(int[] selectedRows) {
        List<File> folders = new ArrayList<>();
        for (int row : selectedRows) {
            File file = (File) model.getValueAt(table.convertRowIndexToModel(row), 1);
            if (!folders.contains(file)) {
                folders.add(file);
            }
        }
        for (File folder : folders) {
            try {
                FileUtils.showInFolder(folder);
            } catch (Exception e) {
                logger.logError("Couldn't open file!", e);
            }

        }
    }

    /**
     * Moves to the next song in the table
     *
     * @param editModeEnabled, a boolean to see if we should open the frame in edit mode
     * @param focusedField,    the field to start focus on
     */
    public void next(boolean editModeEnabled, Component focusedField) {
        int row = table.getSelectedRow();
        table.setRowSelectionInterval(row + 1, row + 1);
        openMoreInfo(editModeEnabled, focusedField, new int[]{row + 1});
        this.setEnabled(false);
    }

    /**
     * Moves to the previous song in the table
     *
     * @param editModeEnabled, a boolean to see if we should open the frame in edit mode
     * @param focusedField,    the field to start focus on
     */
    public void previous(boolean editModeEnabled, Component focusedField) {
        int row = table.getSelectedRow();
        table.setRowSelectionInterval(row - 1, row - 1);
        openMoreInfo(editModeEnabled, focusedField, new int[]{row - 1});
        this.setEnabled(false);
    }

    /**
     * Submits changes to the table with a row to change
     *
     * @param row,  the row to update the table with
     * @param song, the song with updated information
     */
    public void submitRowChanges(int row, Song song) {

        // check if song has a new file, which means we need to change the file name
        if (song.getNewFile() != null) {
            songController.setNewFile(songController.getIndex(row), song.getNewFile());
            table.setValueAt(song.getNewFile().getName().replace(".mp3", StringUtils.EMPTY), row, TABLE_COLUMN_FILENAME);
        }

        // title
        if (song.getTitle() != null && !table.getValueAt(row, TABLE_COLUMN_TITLE).equals(song.getTitle())) {
            songController.setTitle(songController.getIndex(row), song.getTitle());
            table.setValueAt(song.getTitle(), row, TABLE_COLUMN_TITLE);
        }

        // artist
        if (song.getArtist() != null && !table.getValueAt(row, TABLE_COLUMN_ARTIST).equals(song.getArtist())) {
            songController.setArtist(songController.getIndex(row), song.getArtist());
            table.setValueAt(song.getArtist(), row, TABLE_COLUMN_ARTIST);
        }

        // album
        if (song.getAlbum() != null && !table.getValueAt(row, TABLE_COLUMN_ALBUM).equals(song.getAlbum())) {
            songController.setAlbum(songController.getIndex(row), song.getAlbum());
            table.setValueAt(song.getAlbum(), row, TABLE_COLUMN_ALBUM);
        }

        // album artist
        if (song.getAlbumArtist() != null && !table.getValueAt(row, TABLE_COLUMN_ALBUM_ARTIST).equals(song.getAlbumArtist())) {
            songController.setAlbumArtist(songController.getIndex(row), song.getAlbumArtist());
            table.setValueAt(song.getAlbumArtist(), row, TABLE_COLUMN_ALBUM_ARTIST);
        }

        // year
        if (song.getYear() != null && !table.getValueAt(row, TABLE_COLUMN_YEAR).equals(song.getYear())) {
            songController.setYear(songController.getIndex(row), song.getYear());
            table.setValueAt(song.getYear(), row, TABLE_COLUMN_YEAR);
        }

        // genre
        if (song.getGenre() != null && !table.getValueAt(row, TABLE_COLUMN_GENRE).equals(song.getGenre())) {
            songController.setGenre(songController.getIndex(row), song.getGenre());
            table.setValueAt(song.getGenre(), row, TABLE_COLUMN_GENRE);
        }

        // tracks
        if ((song.getTrack() != null && song.getTotalTracks() != null)
                && !table.getValueAt(row, TABLE_COLUMN_TRACK).equals(song.getFullTrackString())) {
            songController.setTrack(songController.getIndex(row), song.getTrack());
            songController.setTotalTracks(songController.getIndex(row), song.getTotalTracks());
            table.setValueAt(song.getFullTrackString(), row, TABLE_COLUMN_TRACK);
        }

        // disks
        if ((song.getDisk() != null && song.getTotalDisks() != null)
                && !table.getValueAt(row, TABLE_COLUMN_DISK).equals(song.getFullDiskString())) {
            songController.setDisk(songController.getIndex(row), song.getDisk());
            songController.setTotalDisks(songController.getIndex(row), song.getTotalDisks());
            table.setValueAt(song.getFullDiskString(), row, TABLE_COLUMN_DISK);
        }

        // comment
        if (song.getComment() != null) {
            songController.setComment(songController.getIndex(row), song.getComment());
        }

        // album art
        if (!Arrays.equals(song.getArtwork_bytes(),
                ImageUtils.getBytesFromImageIcon((ImageIcon) table.getValueAt(row, TABLE_COLUMN_ALBUM_ART)))) {
            if (!multipleArtworks) {
                songController.setAlbumImage(songController.getIndex(row), song.getArtwork_bytes());
                table.setValueAt(ImageUtils.getScaledImage(song.getArtwork_bytes(), 100), row, TABLE_COLUMN_ALBUM_ART);
            }
        }
    }

    /**
     * Enables/Disables the mult panel
     *
     * @param enabled, the boolean to set the fields enabled
     */
    public void enableMultPanel(boolean enabled) {

        multTitle.setText("");
        multArtist.setText("");
        multAlbum.setText("");
        multAlbumArtist.setText("");
        multGenre.setText("");
        multYear.setText("");
        multTrack.setText("");
        multDisk.setText("");

        multTitle.setEnabled(enabled);
        multArtist.setEnabled(enabled);
        multAlbum.setEnabled(enabled);
        multAlbumArtist.setEnabled(enabled);
        multGenre.setEnabled(enabled);
        multYear.setEnabled(enabled);
        multTrack.setEnabled(enabled);
        multDisk.setEnabled(enabled);
        multImage.setIcon(null);
        multUpdateButton.setEnabled(enabled);
    }

    /**
     * Enables/Disables the actions based on the row selection
     *
     * @param enabled, the boolean to set the fields enabled
     */
    private void setActionsEnabled(boolean enabled) {
        autoTagMenuItem.setEnabled(Moose.getSettings().getFeatures().get(Settings.AUTOTAGGING) && enabled);
        addCoversMenuItem.setEnabled(enabled);
        findAndReplaceMenuItem.setEnabled(enabled);
        addTrackNumbersMenuItem.setEnabled(enabled);
        formatFilenamesMenuItem.setEnabled(Moose.getSettings().getFeatures().get(Settings.FORMAT_FILENAMES) && enabled);
        saveTrackMenuItem.setEnabled(enabled);
        saveAllMenuItem.setEnabled(enabled);
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
    private javax.swing.JMenuItem analyticsMenuItem;
    private javax.swing.JMenuItem auditMenuItem;
    private javax.swing.JMenuItem autoTagMenuItem;
    private javax.swing.JButton clearAllButton;
    private javax.swing.JMenuItem commandMenuItem;
    private javax.swing.JTextArea console;
    private javax.swing.JScrollPane consoleSP;
    private javax.swing.JPanel container;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem findAndReplaceMenuItem;
    private javax.swing.JMenuItem formatFilenamesMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu macroMenu;
    private javax.swing.JTextField multAlbum;
    private javax.swing.JTextField multAlbumArtist;
    private javax.swing.JTextField multArtist;
    private javax.swing.JTextField multDisk;
    private javax.swing.JTextField multGenre;
    public javax.swing.JLabel multImage;
    private javax.swing.JPanel multPanel;
    private javax.swing.JTextField multTitle;
    private javax.swing.JTextField multTrack;
    private javax.swing.JButton multUpdateButton;
    private javax.swing.JTextField multYear;
    private javax.swing.JButton openAllButton;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JButton saveAllButton;
    private javax.swing.JMenuItem saveAllMenuItem;
    private javax.swing.JMenuItem saveTrackMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem settingsMenuItem;
    public javax.swing.JTable table;
    private javax.swing.JScrollPane tableSP;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem wikiMenuItem;
    // End of variables declaration//GEN-END:variables

}
