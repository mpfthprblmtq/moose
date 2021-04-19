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
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.*;

import moose.services.AutocompleteService;
import moose.utilities.logger.Logger;
import moose.utilities.viewUtils.AutoCompleteDocument;
import moose.utilities.viewUtils.FileDrop;
import moose.utilities.viewUtils.TableCellListener;

import static moose.utilities.Constants.*;
import static org.junit.Assert.assertNotNull;

// class Frame
public class Frame extends javax.swing.JFrame {

    // logger object
    Logger logger = Moose.getLogger();

    // controller, instantiated in constructor
    public SongController songController;

    // services
    public IconService iconService;

    // some graphics ivars
    ActionListener menuListener;        // listener for the popup menu objects

    int curr_row;   // keeps track of the current row
    int curr_col;   // keeps track of the current column
    int nav_status = -1;  // keeps track of the navigation status

    // table model used, with some customizations and overrides
    DefaultTableModel model = ViewUtils.getTableModel();

    // cell editor used, with customizations and overrides
    DefaultCellEditor editor = ViewUtils.getCellEditor();

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
    }

    /**
     * Creates new form Frame with a
     *
     * @param folder, the folder we want to start with
     */
    public Frame(File folder) {

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

        // add the songs in the folder param to start
        ArrayList<File> files = new ArrayList<>();
        FileUtils.listFiles(folder, files);

        if (!importFiles(files).isEmpty()) {
            setActionsEnabled(true);
            enableMultPanel(true);
            setMultiplePanelFields();
            checkForNewGenres(files);
        }

    }

    public void init() {

        // set the table's model to the custom model
        table.setModel(model);

        // set up the song controller
        songController = new SongController();
        songController.setTable(table);

        // set up services
        iconService = new IconService();

        // listener for the context menu when you right click on a row
        // basically tells the program where to go based on the user's choice
        this.menuListener = (ActionEvent event) -> {

            // get all the rows selected
            int[] selectedRows = table.getSelectedRows();

            // switch based on the option selected
            switch (event.getActionCommand()) {
                case "More info...":
                    openMoreInfo(false, null);
                    break;
                case "Remove from list":
                    removeRows(selectedRows);
                    break;
                case "Play":
                    songController.playFiles(selectedRows);
                    break;
                case "Save":
                    songController.saveTracks(selectedRows);
                    break;
                case "Autotag":
                    songController.autoTagFiles(selectedRows);
                    setMultiplePanelFields();
                    break;
                case "Auto-add track numbers":
                    songController.autoTaggingService.addTrackAndDiskNumbers(selectedRows);
                    setMultiplePanelFields();
                    break;
                case "Auto-add artwork":
                    songController.autoTaggingService.autoAddCoverArt(selectedRows);
                    break;
                case "Move file...":
                case "Move files...":
                    songController.moveFiles(selectedRows);
                    break;
                case "Add artwork":
                    songController.autoTaggingService.addAlbumArt(selectedRows);
                    break;
                case "Remove artwork":
                    songController.removeAlbumArt(selectedRows);
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
//        setColumnWidth(11, 20);

        table.setCellEditor(editor);

        // taken from the FileDrop example
        new FileDrop(System.out, tableSP, (File[] files) -> {

            // create an arraylist of files and traverse it
            ArrayList<File> fileList = new ArrayList<>();
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

        // create a table cell listener
        TableCellListener tcl = ViewUtils.createTCLAction(table, songController);
        assertNotNull(tcl);     // this line is really just to get rid of the "unused var" warning
    }

    /**
     * Returns the song controller
     * @return the song controller
     */
    public SongController getSongController() {
        return this.songController;
    }

    /**
     * Scans all the files and the mp3tags with them and checks to make sure we
     * know the genre
     *
     * @param files, the files to check
     */
    public void checkForNewGenres(List<File> files) {

        // get all the songs, then the genres from the list of files
        List<String> genres = new ArrayList<>();
        files.stream().map((file) -> songController.getSongFromFile(file)).forEachOrdered((s) -> {
            genres.add(s.getGenre());
        });

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
     * Scans all the songs to check to make sure we know the genre
     *
     * @param songs, the songs to check
     */
    public void checkForNewGenresFromSongs(List<Song> songs) {

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


        // sorts the table on the filename, then the album by default
        @SuppressWarnings("rawtypes") DefaultRowSorter sorter = ((DefaultRowSorter) table.getRowSorter());
        ArrayList<RowSorter.SortKey> list = new ArrayList<>();

        list.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sorter.setSortKeys(list);
        sorter.sort();

        list.add(new RowSorter.SortKey(5, SortOrder.ASCENDING));
        sorter.setSortKeys(list);
        sorter.sort();

        // all is well in the world
        return true;
    }

    /**
     * Helper function to update the UI's console, just appends a string
     * @param s, the string to append
     */
    public void updateConsole(String s) {
        console.append(s + "\n");
    }

    /**
     * Function that selects the cell being edited. Used mainly when pressing tab or enter to navigate.
     * This is one of those methods that just works, and it's best not to mess with it.
     * @param row,      the row of the cell
     * @param column,   the column of the cell
     * @param nav_type, the type of navigation
     */
    public void changeSelection(final int row, final int column, int nav_type) {

        // set the globals to the params
        this.curr_row = row;
        this.curr_col = column;

        // check nav_status and see if we should move
        if (nav_status == FROM_DIALOG) {
            return;
        }

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

        // set the view to the row we're editing
        table.scrollRectToVisible(table.getCellRect(table.getEditingRow(), table.getEditingColumn(), false));
    }

    /**
     * Function used to import files to the table
     *
     * @param files, the files to import
     */
    public List<File> importFiles(ArrayList<File> files) {

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
        saveButton = new javax.swing.JButton();
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
        refreshMenuItem = new javax.swing.JMenuItem();
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
        jLabel1.setText("Moose");

        saveButton.setText("Save All");
        saveButton.setFocusable(false);
        saveButton.setMaximumSize(new java.awt.Dimension(100, 68));
        saveButton.setMinimumSize(new java.awt.Dimension(100, 68));
        saveButton.setPreferredSize(new java.awt.Dimension(100, 68));
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
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clearAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                        .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        fileMenu.add(saveAllMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_DOWN_MASK));
        exitMenuItem.setText("Exit");
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        viewMenu.setText("View");

        refreshMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.META_DOWN_MASK));
        refreshMenuItem.setText("Refresh");
        refreshMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(refreshMenuItem);

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

    /*
     * ActionPerformed methods
     */
    // <editor-fold defaultstate="collapsed" desc="ActionPerformed Methods">   

    /**
     * Action for the save button press
     *
     * @param evt
     */
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        songController.saveTracks(IntStream.range(0, getRowCount()).toArray());
    }//GEN-LAST:event_saveButtonActionPerformed

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
            ArrayList<File> files = new ArrayList<>();
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
     * What happens when the multPanel update button is clicked
     *
     * @param evt
     */
    private void multUpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multUpdateButtonActionPerformed
        updateTableFromMultPanelFields();
    }//GEN-LAST:event_multUpdateButtonActionPerformed

    private void tableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMousePressed

        table.setCellEditor(editor);

        int row = table.rowAtPoint(evt.getPoint());
        int col = table.columnAtPoint(evt.getPoint());
        int rows = table.getSelectedRowCount();
        int[] selectedRows = table.getSelectedRows();

        // check what type of click
        switch (evt.getButton()) {

            // if it's a right click
            case java.awt.event.MouseEvent.BUTTON3:

                if (!MiscUtils.intArrayContains(selectedRows, row)) {
                    table.setRowSelectionInterval(row, row);
                }
                if (row >= 0 && col >= 0) {
                    switch (col) {
                        case 10:
                            showArtworkPopup(evt, rows);
                            break;
                        case 1:
                            showFilePopup(evt, rows);
                            break;
                        default:
                            showRegularPopup(evt, rows);
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
                        showArtworkPopup(evt, rows);
                    } else {
                        changeSelection(row, col, -1);
                        table.getEditorComponent().requestFocusInWindow();
                    }
                }
                break;

            // if it's a scroll click
            case java.awt.event.MouseEvent.BUTTON2:
                File file = null;
                for (int i = 0; i < selectedRows.length; i++) {
                    file = songController.autoTaggingService.getFile(selectedRows[i]);
                    FileUtils.openFile(file);
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
            setMultiplePanelFields();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER && !evt.isShiftDown()) {
            curr_row++;
            changeSelection(curr_row, curr_col, ENTER);
            table.setRowSelectionInterval(curr_row, curr_row);
            setMultiplePanelFields();
        } else if (evt.getKeyCode() == KeyEvent.VK_TAB && evt.isShiftDown()) {
            curr_col--;
            changeSelection(curr_row, curr_col, SHIFT_TAB);
            table.setRowSelectionInterval(curr_row, curr_row);
            setMultiplePanelFields();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER && evt.isShiftDown()) {
            curr_row--;
            changeSelection(curr_row, curr_col, SHIFT_ENTER);
            table.setRowSelectionInterval(curr_row, curr_row);
            setMultiplePanelFields();
        } else {
            // do nothing
        }

    }//GEN-LAST:event_tableKeyReleased

    private void addTrackNumbersMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTrackNumbersMenuItemActionPerformed
        if (table.getSelectedRows().length > 0) {
            songController.autoTaggingService.addTrackAndDiskNumbers(table.getSelectedRows());
            setMultiplePanelFields();
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_addTrackNumbersMenuItemActionPerformed

    /**
     * Opens the context menu for album art
     *
     * @param evt
     */
    private void multImageMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_multImageMousePressed
        showArtworkPopup(evt, table.getSelectedRowCount());
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
        if (table.getSelectedRows().length > 0) {
            songController.autoTaggingService.autoAddCoverArt(table.getSelectedRows());
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_addCoversMenuItemActionPerformed

    private void saveTrackMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTrackMenuItemActionPerformed
        if (table.getSelectedRows().length > 0) {
            int[] selectedRows = table.getSelectedRows();
            songController.saveTracks(selectedRows);
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_saveTrackMenuItemActionPerformed

    private void tableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_A && evt.isMetaDown()) {
            table.selectAll();
            setMultiplePanelFields();
        }
    }//GEN-LAST:event_tableKeyPressed

    private void findAndReplaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findAndReplaceMenuItemActionPerformed
        showFindAndReplaceDialog();
    }//GEN-LAST:event_findAndReplaceMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        showAboutDialog();
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void settingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsMenuItemActionPerformed
        Moose.launchSettingsFrame();
    }//GEN-LAST:event_settingsMenuItemActionPerformed

    private void auditMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auditMenuItemActionPerformed
        Moose.launchAuditFrame();
    }//GEN-LAST:event_auditMenuItemActionPerformed

    private void commandMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commandMenuItemActionPerformed
        // prompt the user to enter a command
        String command = JOptionPane.showInputDialog(this, "Enter a command:");
        if (command != null) {
            doCommand(command);
        }
    }//GEN-LAST:event_commandMenuItemActionPerformed

    private void autoTagMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoTagMenuItemActionPerformed
        if (table.getSelectedRows().length > 0) {
            songController.autoTagFiles(table.getSelectedRows());
            setMultiplePanelFields();
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }

    }//GEN-LAST:event_autoTagMenuItemActionPerformed

    private void clearAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAllButtonActionPerformed
        clearAll();
    }//GEN-LAST:event_clearAllButtonActionPerformed

    /**
     * If enter is pressed while field is in focus
     */
    private void multArtistKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multArtistKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            multUpdateButton.doClick();
        }
    }//GEN-LAST:event_multArtistKeyPressed

    private void multTitleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multTitleFocusGained
//        updateAutocompleteFields("multTitle");
    }//GEN-LAST:event_multTitleFocusGained

    private void multArtistFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multArtistFocusGained
        updateAutocompleteFields(multArtist, false);
    }//GEN-LAST:event_multArtistFocusGained

    private void multAlbumFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multAlbumFocusGained
        updateAutocompleteFields(multAlbum, false);
    }//GEN-LAST:event_multAlbumFocusGained

    private void multAlbumArtistFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multAlbumArtistFocusGained
        updateAutocompleteFields(multAlbumArtist, false);
    }//GEN-LAST:event_multAlbumArtistFocusGained

    private void multGenreFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multGenreFocusGained
        updateAutocompleteFields(multGenre, true);
    }//GEN-LAST:event_multGenreFocusGained

    private void multYearFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_multYearFocusGained
        updateAutocompleteFields(multYear, false);
    }//GEN-LAST:event_multYearFocusGained

    private void refreshMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshMenuItemActionPerformed
        int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear your current list and reset?");
        switch (res) {
            case JOptionPane.CANCEL_OPTION:
                // do nothing
                break;
            case JOptionPane.OK_OPTION:
                this.dispose();
                Moose.launchFrame();
        }
    }//GEN-LAST:event_refreshMenuItemActionPerformed

    private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshMenuItemActionPerformed
        if (table.getRowCount() > 0) {
            table.selectAll();
        }
    }//GEN-LAST:event_refreshMenuItemActionPerformed

    private void formatFilenamesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatFilenamesMenuItemActionPerformed
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length > 0) {
            JOptionPane.showMessageDialog(this, "Not implemented yet!");
        } else {
            JOptionPane.showMessageDialog(this, "No rows selected!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_formatFilenamesMenuItemActionPerformed

    private void openAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openAllButtonActionPerformed
        for (int i = 0; i < table.getRowCount(); i++) {
            File file = songController.autoTaggingService.getFile(i);
            FileUtils.openFile(file);
        }
    }//GEN-LAST:event_openAllButtonActionPerformed

    private void wikiMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wikiMenuItemActionPerformed
        WebUtils.openPage(Constants.MOOSE_WIKI);
    }//GEN-LAST:event_wikiMenuItemActionPerformed

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

    /**
     * Gets the total number of rows
     * @return the total number of rows in the table
     */
    public int getRowCount() {
        return table.getRowCount();
    }

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
     * Clears the table
     * Shows a dialog first, as long as the user wants it
     */
    public void clearAll() {
        if (Moose.getSettings().isAskBeforeClearAll()) {
            Boolean result = DialogService.showClearAllDialog(this);
            if (result != null) {
                boolean dontAskAgain = result;
                if (dontAskAgain) {
                    Moose.getSettings().setAskBeforeClearAll(false);
                    Moose.settingsFrame.settingsController.writeSettingsFile(Moose.getSettings());
                }
                model.setRowCount(0);
                table.removeAll();
                songController.getSongs().clear();
                enableMultPanel(false);
                setActionsEnabled(false);
            }
        } else {
            model.setRowCount(0);
            table.removeAll();
            songController.getSongs().clear();
            enableMultPanel(false);
            setActionsEnabled(false);
        }
    }

    /**
     * Gets the info for a song
     */
    public void openMoreInfo(boolean editModeEnabled, Component focusedField) {
        Song s = songController.getSongs().get(songController.getIndex(table.getSelectedRow()));
        InfoFrame infoFrame = new InfoFrame(s, table.getSelectedRow(), editModeEnabled, focusedField);
        infoFrame.setLocationRelativeTo(this);
        infoFrame.setVisible(true);
        this.setEnabled(false);
    }

    /**
     * Moves to the next song
     */
    public void next(boolean editModeEnabled, Component focusedField) {
        int row = table.getSelectedRow();
        table.setRowSelectionInterval(row + 1, row + 1);
        openMoreInfo(editModeEnabled, focusedField);
        this.setEnabled(false);
    }

    /**
     * Moves to the previous song
     */
    public void previous(boolean editModeEnabled, Component focusedField) {
        int row = table.getSelectedRow();
        table.setRowSelectionInterval(row - 1, row - 1);
        openMoreInfo(editModeEnabled, focusedField);
        this.setEnabled(false);
    }

    /**
     * Get the changes from the info panel
     *
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

        int row = table.getSelectedRow();

        if (!table.getValueAt(row, 1).equals(filename)) {
            File old_file = (File) model.getValueAt(table.convertRowIndexToModel(row), 1);
            String path = old_file.getPath().replace(old_file.getName(), "");
            File new_file = new File(path + "//" + filename + ".mp3");
            songController.setFile(songController.getIndex(row), new_file);
            if (!old_file.renameTo(new_file)) {
                logger.logError("Error renaming file: " + old_file.getName() + " to: " + new_file.getName());
            }
            model.setValueAt(new_file, row, 1);
            table.setValueAt(filename, row, 1);
        }
        // else do nothing, nothing was changed

        if (!table.getValueAt(row, 2).equals(title)) {
            songController.setTitle(songController.getIndex(row), title);
            table.setValueAt(title, row, 2);
        }
        // else do nothing, nothing was changed

        if (!table.getValueAt(row, 3).equals(artist)) {
            songController.setArtist(songController.getIndex(row), artist);
            table.setValueAt(artist, row, 3);
        }
        // else do nothing, nothing was changed

        if (!table.getValueAt(row, 4).equals(album)) {
            songController.setAlbum(songController.getIndex(row), album);
            table.setValueAt(album, row, 4);
        }
        // else do nothing, nothing was changed

        if (!table.getValueAt(row, 5).equals(albumArtist)) {
            songController.setAlbumArtist(songController.getIndex(row), albumArtist);
            table.setValueAt(albumArtist, row, 5);
        }
        // else do nothing, nothing was changed

        if (!table.getValueAt(row, 6).equals(year)) {
            songController.setYear(songController.getIndex(row), year);
            table.setValueAt(year, row, 6);
        }
        // else do nothing, nothing was changed

        if (!table.getValueAt(row, 7).equals(genre)) {
            songController.setGenre(songController.getIndex(row), genre);
            table.setValueAt(genre, row, 7);
        }
        // else do nothing, nothing was changed

        if (!table.getValueAt(row, 8).equals(tracks)) {
            if (!tracks.equals("/")) {
                songController.setTrack(songController.getIndex(row), tracks);
                table.setValueAt(tracks, row, 8);
            } else {
                songController.setDisk(songController.getIndex(row), "");
                table.setValueAt("", row, 8);
            }
        }
        // else do nothing, nothing was changed

        if (!table.getValueAt(row, 9).equals(disks)) {
            if (!disks.equals("/")) {
                songController.setDisk(songController.getIndex(row), disks);
                table.setValueAt(disks, row, 9);
            } else {
                songController.setDisk(songController.getIndex(row), "");
                table.setValueAt("", row, 9);
            }
        }
        // else do nothing, nothing was changed

        songController.setComment(songController.getIndex(row), comment);

    }

    /**
     * Show the about dialog, includes name, version, and copyright
     */
    public void showAboutDialog() {
        JOptionPane.showMessageDialog(null,
                "<html><b>Moose</b></html>\nVersion: " + Moose.getSettings().getVersion() + "\n" + "© Pat Ripley 2018-2020",
                "About Moose", JOptionPane.PLAIN_MESSAGE, iconService.get(IconService.MOOSE_128));
    }

    /**
     * Sets the multiple fields panel based on the data selected
     */
    public void setMultiplePanelFields() {

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

        if (MiscUtils.checkIfSame(images[0], images) && images[0] != null) {
            multImage.setIcon(ImageUtils.getScaledImage(images[0], 150));
        } else {
            multImage.setIcon(null);
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

        // check if the title field needs updated
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

        // check if the artist field needs updated
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

        // check if the album field needs updated
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

        // check if the album artist field needs updated
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

        // check if the year field needs updated
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

        // check if the genre field needs updated
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
            checkForNewGenresFromSongs(songsToGenreCheck);
        }

        // check if the track field needs updated
        if (!track.equals("-")) {
            for (int row : selectedRows) {

                // get the index of the song in the table
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(track, row, 8);

                // set the value in the songs array
                songController.setTrack(index, track);
            }
        }

        // check if the disk field needs updated
        if (!disk.equals("-")) {
            for (int row : selectedRows) {

                // get the index of the song in the table
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(disk, row, 9);

                // set the value in the songs array
                songController.setDisk(index, disk);
            }
        }

        // TODO check if the album art field needs updated
    }

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
        (new Thread() {
            @Override
            public void run() {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    logger.logError("Exception with threading when opening the find and replace dialog.", e);
                }
                find.requestFocus();
            }
        }).start();

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
        // else do nothing, nothing was changed
        nav_status = FROM_DIALOG;
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
     * Enables/Disables the actions based on the row selection
     */
    private void setActionsEnabled(boolean b) {
        autoTagMenuItem.setEnabled(b);
        addCoversMenuItem.setEnabled(b);
        findAndReplaceMenuItem.setEnabled(b);
        addTrackNumbersMenuItem.setEnabled(b);
        formatFilenamesMenuItem.setEnabled(b);
        saveTrackMenuItem.setEnabled(b);
        saveAllMenuItem.setEnabled(b);
    }

    /**
     * Shows the popup when you click on an album image
     *
     * @param e, the event to base the location of the menu on
     */
    void showArtworkPopup(MouseEvent e, int rows) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        popup.add(item = new JMenuItem("Add cover..."));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Remove cover"));
        item.addActionListener(menuListener);
        popup.addSeparator();
        if (rows == 1) {
            popup.add(item = new JMenuItem("More info..."));
            item.addActionListener(menuListener);
            popup.addSeparator();
        }
        popup.add(item = new JMenuItem("Remove from list"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Play"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Save"));
        item.addActionListener(menuListener);
        popup.addSeparator();
        popup.add(item = new JMenuItem("Autotag"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Auto-add track numbers"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Auto-add artwork"));
        item.addActionListener(menuListener);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Shows the normal popup
     *
     * @param e, the event to base the location of the menu on
     */
    void showRegularPopup(MouseEvent e, int rows) {
        JPopupMenu popup = getBasePopUpMenu(rows);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Shows the normal popup with some file options too
     *
     * @param e, the event to base the location of the menu on
     */
    void showFilePopup(MouseEvent e, int rows) {
        JPopupMenu popup = getBasePopUpMenu(rows);
        JMenuItem item;
        popup.add(item = new JMenuItem(rows > 1 ? "Move file..." : "Move files..."));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Format filenames"));
        item.addActionListener(menuListener);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Returns the base popup menu
     *
     * @param rows, the number of rows selected
     * @return the base popup menu
     */
    private JPopupMenu getBasePopUpMenu(int rows) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        if (rows == 1) {
            popup.add(item = new JMenuItem("More info..."));
            item.addActionListener(menuListener);
            popup.addSeparator();
        }
        popup.add(item = new JMenuItem("Remove from list"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Play"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Save"));
        item.addActionListener(menuListener);
        popup.addSeparator();
        popup.add(item = new JMenuItem("Autotag"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Auto-add track numbers"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Auto-add artwork"));
        item.addActionListener(menuListener);

        return popup;
    }

    /**
     * Sets the specified column width
     *
     * @param column, the column to set
     * @param width,  width in pixels
     */
    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        if (width < 0) {
            JLabel label = new JLabel((String) tableColumn.getHeaderValue());
            Dimension preferred = label.getPreferredSize();
            width = (int) preferred.getWidth() + 14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {

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
    private javax.swing.JMenuItem refreshMenuItem;
    private javax.swing.JMenuItem saveAllMenuItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveTrackMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem settingsMenuItem;
    public javax.swing.JTable table;
    private javax.swing.JScrollPane tableSP;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem wikiMenuItem;
    // End of variables declaration//GEN-END:variables

}
