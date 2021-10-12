/*
   Proj:   Moose
   File:   Frame.java
   Desc:   Main UI class for the JFrame containing the everything.
           Works with the SongController to edit albums, this class just handles all the UI.

   Copyright Pat Ripley 2018
 */

// package
package moose.views;

// imports

import java.awt.*;

import moose.*;
import moose.controllers.SongController;
import moose.objects.Settings;
import moose.objects.Song;
import moose.services.DialogService;
import moose.services.IconService;
import moose.utilities.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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

import moose.services.AutocompleteService;
import moose.utilities.logger.Logger;
import moose.utilities.viewUtils.AutoCompleteDocument;
import moose.utilities.viewUtils.FileDrop;
import moose.utilities.viewUtils.TableCellListener;
import moose.utilities.viewUtils.ViewUtils;

import static moose.utilities.Constants.*;

// class Frame
public class Frame extends javax.swing.JFrame {

    // logger object
    Logger logger = Moose.getLogger();

    // controller, instantiated in constructor
    public SongController songController = new SongController();

    // services
    public IconService iconService = new IconService();

    // some graphics ivars
    ActionListener menuListener;        // listener for the popup menu objects

    int currentRow;     // keeps track of the current row
    int currentColumn;  // keeps track of the current column

    // table model used, with some customizations and overrides
    DefaultTableModel model = ViewUtils.getTableModel();

    // ivars for the multPanel to check if the artwork has changed in the multPanel
    byte[] originalMultPanelArtwork;
    byte[] newMultPanelArtwork;

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
        songController = new SongController();
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
                checkForNewGenres(files);
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
                    checkForNewGenres(files);
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
                    songController.autoTagFiles(selectedRows);
                    updateMultiplePanelFields();
                    break;
                case AUTO_TRACK_DISK_NUMBERS:
                    songController.autoTaggingService.addTrackAndDiskNumbers(selectedRows);
                    updateMultiplePanelFields();
                    break;
                case AUTO_ARTWORK:
                    songController.autoTaggingService.autoAddCoverArt(selectedRows);
                    break;
                case MOVE_FILE:
                    songController.moveFiles(selectedRows);
                    break;
                case FORMAT_FILENAME:
                    songController.formatFilenames(selectedRows);
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
            checkForNewGenres(successfullyAddedFiles);
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
        genres.stream().filter((genre) -> (!Moose.getSettings().getGenres().contains(genre) && !StringUtils.isEmpty(genre))).forEachOrdered((genre) -> {
            if (!newGenres.contains(genre)) {
                newGenres.add(genre);
            }
        });

        // for each new genre, ask if we want to add that one
        for (String newGenre : newGenres) {
            int res = JOptionPane.showConfirmDialog(Moose.frame, "\"" + newGenre + "\" isn't in your built-in genre list, would you like to add it?");
            if (res == JOptionPane.YES_OPTION) {// add the genre to the settings and update
                Settings settings = Moose.getSettings();
                settings.addGenre(newGenre);
                Moose.updateSettings(settings);
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
                table.setValueAt(iconService.get(IconService.DEFAULT), row, 0);
                break;
            case EDITED:
                table.setValueAt(iconService.get(IconService.EDITED), row, 0);
                break;
            case Constants.SAVED:
                table.setValueAt(iconService.get(IconService.SAVED), row, 0);
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
                iconService.get(IconService.DEFAULT), // adds the default status icon
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
     * Function used to import files to the table
     *
     * @param files, the files to import
     */
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
        @SuppressWarnings("rawtypes") DefaultRowSorter sorter = ((DefaultRowSorter) table.getRowSorter());
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        container = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        saveAllButton = new javax.swing.JButton();
        tableSP = new javax.swing.JScrollPane();
        table = new JTable() {
            public Component prepareEditor(TableCellEditor editor, int row, int col) {
                Component result = super.prepareEditor(editor, row, col);
                if (result instanceof JTextField) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            String originalText = ((JTextField) result).getText();
                            ((JTextField) result).setDocument(
                                    new AutoCompleteDocument(
                                            AutocompleteService.getNameService(table.getEditingColumn() == TABLE_COLUMN_GENRE, table),
                                            ((JTextField) result)
                                    )
                            );
                            ((JTextField) result).setText(originalText);
                            ((JTextField) result).requestFocus();
                            ((JTextField) result).selectAll();
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
        auditMenuItem = new javax.swing.JMenuItem();
        autoTagMenuItem = new javax.swing.JMenuItem();
        addCoversMenuItem = new javax.swing.JMenuItem();
        findAndReplaceMenuItem = new javax.swing.JMenuItem();
        addTrackNumbersMenuItem = new javax.swing.JMenuItem();
        formatFilenamesMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        wikiMenuItem = new javax.swing.JMenuItem();
        commandMenuItem = new javax.swing.JMenuItem();
        settingsMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Moose");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel1.setText("Moo0oO0ose");

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

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/moose64.png"))); // NOI18N

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
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(openAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(saveAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(clearAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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

        macroMenu.setText("Actions");

        auditMenuItem.setText("Audit...");
        auditMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auditMenuItemActionPerformed(evt);
            }
        });
        macroMenu.add(auditMenuItem);

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
        // select some file(s)
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
     * Action for the exit menu item, quits the app
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /**
     * Action for the select all menu item, selects all the rows on the table
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:selectAllMenuItemActionPerformed
        if (table.getRowCount() > 0) {
            table.selectAll();
        }
    }//GEN-LAST:selectAllMenuItemActionPerformed

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
            songController.autoTagFiles(table.getSelectedRows());
            updateMultiplePanelFields();
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_autoTagMenuItemActionPerformed

    /**
     * Action for the Add Covers menu item, auto adds the covers
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void addCoversMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCoversMenuItemActionPerformed
        if (table.getSelectedRows().length > 0) {
            songController.autoTaggingService.autoAddCoverArt(table.getSelectedRows());
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_addCoversMenuItemActionPerformed

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
        JTextField find = new JTextField();
        JTextField replace = new JTextField();
        JCheckBox includeFilesBox = new JCheckBox();
        includeFilesBox.setText("Include file names");
        includeFilesBox.setSelected(true);
        Object[] message = {"Find:", find, "Replace:", replace, includeFilesBox};

        // create a thread to wait until the dialog box pops up
        (new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.logError("Exception with threading when opening the find and replace dialog.", e);
            }
            find.requestFocus();
        })).start();

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
        songController.formatFilenames(table.getSelectedRows());
    }//GEN-LAST:event_formatFilenamesMenuItemActionPerformed

    /**
     * Action for the About menu item, opens the about dialog
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        DialogService.showAboutDialog();
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    /**
     * Action for the Wiki menu item, opens the wiki page
     *
     * @param evt, the ActionEvent (not used, but here because Netbeans)
     */
    private void wikiMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wikiMenuItemActionPerformed
        WebUtils.openPage(Constants.MOOSE_WIKI);
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
            Boolean result = DialogService.showClearAllDialog(this);
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
            FileUtils.openFile(file);
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
                                    evt, menuListener, rows, true, false, true, false);
                            break;
                        case 1:
                            ViewUtils.showPopUpContextMenu(
                                    evt, menuListener, rows, true, true, false, false);
                            break;
                        default:
                            ViewUtils.showPopUpContextMenu(
                                    evt, menuListener, rows, true, false, false, false);
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
                        ViewUtils.showPopUpContextMenu(evt, menuListener, table.getSelectedRowCount(), true, false, true, false);
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
                    FileUtils.openFile(songController.autoTaggingService.getFile(selectedRow));
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
        changeSelection(evt);
    }//GEN-LAST:event_tableKeyReleased

    /**
     * Function that selects the cell being edited. Used mainly when pressing tab or enter to navigate.
     * This is one of those methods that just works, and it's best not to mess with it.
     *
     * @param evt, the KeyEvent that we use to determine the type of navigation (Enter, Tab, Shift+Enter, Shift+Tab)
     */
    public void changeSelection(KeyEvent evt) {

        // changes to the row/column
        int rowDelta;
        int columnDelta;

        // figure out what to do based on how we got here
        if (evt.getKeyCode() == KeyEvent.VK_ENTER && !evt.isShiftDown()) {
            // if on last row, go to the first row
            rowDelta = currentRow + 1;
            currentRow = rowDelta > table.getRowCount() - 1 ? 0 : currentRow + 1;

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

        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER && evt.isShiftDown()) {
            // if on first row, go to the last row
            rowDelta = currentRow - 1;
            currentRow = rowDelta < 0 ? table.getRowCount() - 1 : currentRow - 1;

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
        }

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
        updateTableFromMultPanelFields();
    }//GEN-LAST:event_multUpdateButtonActionPerformed

    /**
     * Opens the context menu for album art on clicking on the multImage box
     *
     * @param evt, the mouse event, used to get the x,y of the click
     */
    private void multImageMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_multImageMousePressed
        ViewUtils.showPopUpContextMenu(evt, menuListener, table.getSelectedRowCount(), true, false, false, true);
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
     * On focus gain, update auto complete fields
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multArtistFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multArtistFocusGained
        updateAutocompleteFields(multArtist, false);
    }//GEN-LAST:event_multArtistFocusGained

    /**
     * On focus gain, update auto complete fields
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multAlbumFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multAlbumFocusGained
        updateAutocompleteFields(multAlbum, false);
    }//GEN-LAST:event_multAlbumFocusGained

    /**
     * On focus gain, update auto complete fields
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multAlbumArtistFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multAlbumArtistFocusGained
        updateAutocompleteFields(multAlbumArtist, false);
    }//GEN-LAST:event_multAlbumArtistFocusGained

    /**
     * On focus gain, update auto complete fields
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multGenreFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multGenreFocusGained
        updateAutocompleteFields(multGenre, true);
    }//GEN-LAST:event_multGenreFocusGained

    /**
     * On focus gain, update auto complete fields
     *
     * @param evt, the FocusEvent (not used, but here because Netbeans)
     */
    private void multYearFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multYearFocusGained
        updateAutocompleteFields(multYear, false);
    }//GEN-LAST:event_multYearFocusGained

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
     * @param editModeEnabled, a boolean to check if we should be in edit mode or not
     * @param focusedField, the field we want to focus on first
     * @param selectedRows, the rows currentlyl selected
     */
    public void openMoreInfo(boolean editModeEnabled, Component focusedField, int[] selectedRows) {
        // get the map of songs
        Map<Integer, Song> songs = new HashMap<>();
        for (int row : selectedRows) {
            songs.put(row, songController.getSongs().get(songController.getIndex(row)));
        }

        // send it -1 as the row because there's more than one row, makes sense, right?
        InfoFrame infoFrame = new InfoFrame(songs, editModeEnabled, focusedField);
        infoFrame.setLocationRelativeTo(this);
        infoFrame.setVisible(true);
        this.setEnabled(false);
    }

    /**
     * Opens the folder where this track lives
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
            FileUtils.showInFolder(folder);
        }
    }

    /**
     * Moves to the next song in the table
     * @param editModeEnabled, a boolean to see if we should open the frame in edit mode
     * @param focusedField, the field to start focus on
     */
    public void next(boolean editModeEnabled, Component focusedField) {
        int row = table.getSelectedRow();
        table.setRowSelectionInterval(row + 1, row + 1);
        openMoreInfo(editModeEnabled, focusedField, new int[]{row + 1});
        this.setEnabled(false);
    }

    /**
     * Moves to the previous song in the table
     * @param editModeEnabled, a boolean to see if we should open the frame in edit mode
     * @param focusedField, the field to start focus on
     */
    public void previous(boolean editModeEnabled, Component focusedField) {
        int row = table.getSelectedRow();
        table.setRowSelectionInterval(row - 1, row - 1);
        openMoreInfo(editModeEnabled, focusedField, new int[]{row - 1});
        this.setEnabled(false);
    }

    /**
     * Get the changes from the info panel
     *
     * @param row          the row to update the table with
     * @param filename,    the filename to change
     * @param title,       the title to change
     * @param artist,      the artist to change
     * @param album,       the album to change
     * @param albumArtist, the albumArtist to change
     * @param year,        the year to change
     * @param genre,       the genre to change
     * @param tracks,      the tracks to change
     * @param disks,       the disks to change
     * @param comment,     the comment to change
     */
    public void submitChangesFromInfoFrame(
            int row,
            String filename,
            String title,
            String artist,
            String album,
            String albumArtist,
            String year,
            String genre,
            String tracks,
            String disks,
            String comment) {

        // filename
        if (filename != null && !table.getValueAt(row, 1).equals(filename)) {
            File oldFile = (File) model.getValueAt(table.convertRowIndexToModel(row), 1);
            File newFile = FileUtils.getNewMP3FileFromOld(oldFile, filename);
            songController.setNewFile(songController.getIndex(row), newFile);
            table.setValueAt(filename, row, 1);
        }
        // else do nothing, nothing was changed

        // title
        if (title != null && !table.getValueAt(row, 2).equals(title)) {
            songController.setTitle(songController.getIndex(row), title);
            table.setValueAt(title, row, 2);
        }
        // else do nothing, nothing was changed

        // artist
        if (artist != null && !table.getValueAt(row, 3).equals(artist)) {
            songController.setArtist(songController.getIndex(row), artist);
            table.setValueAt(artist, row, 3);
        }
        // else do nothing, nothing was changed

        // album
        if (album != null && !table.getValueAt(row, 4).equals(album)) {
            songController.setAlbum(songController.getIndex(row), album);
            table.setValueAt(album, row, 4);
        }
        // else do nothing, nothing was changed

        // album artist
        if (albumArtist != null && !table.getValueAt(row, 5).equals(albumArtist)) {
            songController.setAlbumArtist(songController.getIndex(row), albumArtist);
            table.setValueAt(albumArtist, row, 5);
        }
        // else do nothing, nothing was changed

        // year
        if (year != null && !table.getValueAt(row, 6).equals(year)) {
            songController.setYear(songController.getIndex(row), year);
            table.setValueAt(year, row, 6);
        }
        // else do nothing, nothing was changed

        // genre
        if (genre != null && !table.getValueAt(row, 7).equals(genre)) {
            songController.setGenre(songController.getIndex(row), genre);
            table.setValueAt(genre, row, 7);
        }
        // else do nothing, nothing was changed

        // tracks
        if (tracks != null && !table.getValueAt(row, 8).equals(tracks)) {
            if (!tracks.equals("/")) {
                String[] arr = tracks.split("/");
                String track = arr[0];
                String totalTracks = arr[1];
                songController.setTrack(songController.getIndex(row), track);
                songController.setTotalTracks(songController.getIndex(row), totalTracks);
                table.setValueAt(tracks, row, 8);
            } else {
                songController.setDisk(songController.getIndex(row), "");
                table.setValueAt("", row, 8);
            }
        }
        // else do nothing, nothing was changed

        // disks
        if (disks != null && !table.getValueAt(row, 9).equals(disks)) {
            if (!disks.equals("/")) {
                String[] arr = disks.split("/");
                String disk = arr[0];
                String totalDisks = arr[1];
                songController.setDisk(songController.getIndex(row), disk);
                songController.setTotalDisks(songController.getIndex(row), totalDisks);
                table.setValueAt(disks, row, 9);
            } else {
                songController.setDisk(songController.getIndex(row), "");
                table.setValueAt("", row, 9);
            }
        }
        // else do nothing, nothing was changed

        // comment
        if (comment != null) {
            songController.setComment(songController.getIndex(row), comment);
        }
    }

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
        multTitle.setText(StringUtils.checkIfSame(titles[0], titles) ? titles[0] : Constants.DASH);
        multArtist.setText(StringUtils.checkIfSame(artists[0], artists) ? artists[0] : Constants.DASH);
        multAlbum.setText(StringUtils.checkIfSame(albums[0], albums) ? albums[0] : Constants.DASH);
        multAlbumArtist.setText(StringUtils.checkIfSame(albumArtists[0], albumArtists) ? albumArtists[0] : Constants.DASH);
        multGenre.setText(StringUtils.checkIfSame(genres[0], genres) ? genres[0] : Constants.DASH);
        multYear.setText(StringUtils.checkIfSame(years[0], years) ? years[0] : Constants.DASH);
        multTrack.setText(StringUtils.checkIfSame(tracks[0], tracks) ? tracks[0] : Constants.DASH);
        multDisk.setText(StringUtils.checkIfSame(disks[0], disks) ? disks[0] : Constants.DASH);

        if (ImageUtils.checkIfSame(images[0], images) && images[0] != null) {
            multImage.setIcon(ImageUtils.getScaledImage(images[0], 150));
            originalMultPanelArtwork = newMultPanelArtwork = images[0];
        } else {
            List<byte[]> bytesList = ImageUtils.getUniqueByteArrays(Arrays.asList(images));
            multImage.setIcon(new ImageIcon(ImageUtils.combineImages(bytesList, 150)));
        }
    }

    /**
     * Updates the fields in the table with the fields in the mult panel Gets
     * called when the update button is pressed
     */
    public void updateTableFromMultPanelFields() {

        // get the selected rows
        int[] selectedRows;

        if (table.getSelectedRows().length == 0) {
            selectedRows = new int[table.getRowCount()];
            for (int i = 0; i < selectedRows.length; i++) {
                selectedRows[i] = i;
            }
        } else {
            selectedRows = table.getSelectedRows();
        }

        // get the fields in the mult panel
        String title = multTitle.getText();
        String artist = multArtist.getText();
        String album = multAlbum.getText();
        String albumArtist = multAlbumArtist.getText();
        String genre = multGenre.getText();
        String year = multYear.getText();
        String track = multTrack.getText();
        String disk = multDisk.getText();

        // check if the title field needs to be updated
        if (!title.equals("-")) {
            for (int row : selectedRows) {

                // get the index of the song in the table
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(title, row, 2);

                // set the value in the songs array
                songController.setTitle(index, title);
            }
        }

        // check if the artist field needs to be updated
        if (!artist.equals("-")) {
            for (int row : selectedRows) {

                // get the index of the song in the table
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(artist, row, 3);

                // set the value in the songs array
                songController.setArtist(index, artist);
            }
        }

        // check if the album field needs to be updated
        if (!album.equals("-")) {
            for (int row : selectedRows) {

                // get the index of the song in the table
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(album, row, 4);

                // set the value in the songs array
                songController.setAlbum(index, album);
            }
        }

        // check if the album artist field needs to be updated
        if (!albumArtist.equals("-")) {
            for (int row : selectedRows) {

                // get the index of the song in the table
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(albumArtist, row, 5);

                // set the value in the songs array
                songController.setAlbumArtist(index, albumArtist);
            }
        }

        // check if the year field needs to be updated
        if (!year.equals("-")) {
            for (int row : selectedRows) {

                // get the index of the song in the table
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(year, row, 6);

                // set the value in the songs array
                songController.setYear(index, year);
            }
        }

        // check if the genre field needs to be updated
        if (!genre.equals("-")) {
            List<Song> songsToGenreCheck = new ArrayList<>();
            for (int row : selectedRows) {

                // get the index of the song in the table
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(genre, row, 7);

                // set the value in the songs array
                songController.setGenre(index, genre);

                // get the file from the row to add in the genre checker
                songsToGenreCheck.add(songController.getSongs().get(index));

            }

            // check for any new genres
            checkForNewGenres(songsToGenreCheck);
        }

        // check if the track field needs to be updated
        if (!track.equals("-")) {
            if (!track.matches(TRACK_DISK_REGEX)) {
                DialogService.showMessageDialog(this, "Invalid input: " + track, "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                for (int row : selectedRows) {

                    // get the index of the song in the table
                    int index = songController.getIndex(row);

                    // set the value in the songs array
                    if (StringUtils.isEmpty(track)) {
                        songController.setTrack(index, track);
                        songController.setTotalTracks(index, track);
                    } else if (track.matches("\\d*/\\d*")) {
                        String[] arr = track.split("/");
                        songController.setTrack(index, arr[0]);
                        songController.setTotalTracks(index, arr[1]);
                    } else if (track.matches("/\\d*")) {
                        songController.setTrack(index, StringUtils.EMPTY);
                        songController.setTotalTracks(index, track);
                    } else if (track.matches("\\d*/")) {
                        songController.setTrack(index, track);
                        songController.setTotalTracks(index, StringUtils.EMPTY);
                    }

                    // set the value in the table to the new value
                    table.setValueAt(songController.getSongs().get(index).getFullTrackString(), row, 8);
                }
            }
        }

        // check if the disk field needs to be updated
        if (!disk.equals("-")) {
            if (!track.matches(TRACK_DISK_REGEX)) {
                DialogService.showMessageDialog(this, "Invalid input: " + disk, "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                for (int row : selectedRows) {

                    // get the index of the song in the table
                    int index = songController.getIndex(row);

                    // set the value in the songs array
                    if (StringUtils.isEmpty(disk)) {
                        songController.setDisk(index, StringUtils.EMPTY);
                        songController.setTotalDisks(index, StringUtils.EMPTY);
                    } else if (disk.matches("\\d*/\\d*")) {
                        String[] arr = disk.split("/");
                        songController.setDisk(index, arr[0]);
                        songController.setTotalDisks(index, arr[1]);
                    } else if (disk.matches("/\\d*")) {
                        songController.setDisk(index, StringUtils.EMPTY);
                        songController.setTotalDisks(index, disk);
                    } else if (disk.matches("\\d*/")) {
                        songController.setTrack(index, disk);
                        songController.setTotalDisks(index, StringUtils.EMPTY);
                    }

                    // set the value in the table to the new value
                    table.setValueAt(songController.getSongs().get(index).getFullDiskString(), row, 9);
                }
            }
        }

        // check if the album art field needs to be updated
        if (!Arrays.equals(originalMultPanelArtwork, newMultPanelArtwork)) {
            // original doesn't match new, the artwork was changed
            for (int row : selectedRows) {

                // get the index of the song in the table
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(ImageUtils.getScaledImage(newMultPanelArtwork, 100), row, 10);

                // set the value in the songs array
                songController.setAlbumImage(index, newMultPanelArtwork);
            }
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
        File image = songController.autoTaggingService.selectAlbumArt(startingPoint);
        try {
            BufferedImage bi = ImageIO.read(image);
            newMultPanelArtwork = ImageUtils.getBytesFromBufferedImage(bi);
            multImage.setIcon(ImageUtils.getScaledImage(newMultPanelArtwork, 150));
        } catch (IOException e) {
            logger.logError("IOException when trying to get album art from mult panel!", e);
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
        autoTagMenuItem.setEnabled(enabled);
        addCoversMenuItem.setEnabled(enabled);
        findAndReplaceMenuItem.setEnabled(enabled);
        addTrackNumbersMenuItem.setEnabled(enabled);
        formatFilenamesMenuItem.setEnabled(enabled);
        saveTrackMenuItem.setEnabled(enabled);
        saveAllMenuItem.setEnabled(enabled);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
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
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem wikiMenuItem;
    // End of variables declaration//GEN-END:variables

}
