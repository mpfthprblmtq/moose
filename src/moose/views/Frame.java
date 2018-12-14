/**
 *  Proj:   Moose
 *  File:   Frame.java
 *  Desc:   Main UI class for the JFrame containing the everything.
 *          Works with the SongController to edit albums, this class just handles all the UI.
 *
 *  Copyright Pat Ripley 2018
 */
// package
package moose.views;

// imports
import moose.*;
import moose.controllers.SongController;
import moose.objects.Song;
import moose.utilities.FileDrop;
import moose.utilities.Logger;
import moose.utilities.TableCellListener;
import moose.utilities.Utils;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.*;

// class Frame
public class Frame extends javax.swing.JFrame {

    // logger object
    Logger logger = Main.getLogger();

    // controller, instantiated in constructor
    public SongController songController = new SongController();

    // some graphics ivars
    ActionListener menuListener;        // listener for the popup menu objects

    int curr_row;   // keeps track of the current row
    int curr_col;   // keeps track of the current column
    int nav_status = -1;  // keeps track of the navigation status

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
            return !(column == 11 || column == 0);
        }
    };

    // some constants to make life easier
    public static final int DEFAULT = 0;
    public static final int EDITED = 1;
    public static final int SAVED = 2;

    private static final int SHIFT_TAB = 0;
    private static final int SHIFT_ENTER = 1;
    private static final int TAB = 2;
    private static final int ENTER = 3;

    private static final int FROM_DIALOG = 1;
    private static final int NORMAL_NAV = 2;

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
        files = Utils.listFiles(folder, files);
        for (File file : files) {
            addFileToTable(file);
        }
    }

    public void init() {

        // set the table's model to the custom model
        table.setModel(model);

        // listener for the context menu when you right click on a row
        // basically tells the program where to go based on the user's choice
        this.menuListener = (ActionEvent event) -> {

            // get all the rows selected
            int[] selectedRows = table.getSelectedRows();

            // switch based on the option selected
            switch (event.getActionCommand()) {
                case "Add":
                    songController.addAlbumArt(selectedRows);
                    break;
                case "Remove":
                    songController.removeAlbumArt(selectedRows);
                    break;
                case "Remove from list":
                    removeRows(selectedRows);
                    break;
                case "Play":
                    songController.playFiles(selectedRows);
                    break;
                case "Move File...":
                    songController.moveFiles(selectedRows);
                    break;
                case "Save":
                    songController.saveTracks(selectedRows);
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

        // taken from the FileDrop example
        FileDrop fileDrop = new FileDrop(System.out, tableSP, (File[] files) -> {

            // create an arraylist of files and traverse it
            ArrayList<File> fileList = new ArrayList<>();
            for (File file : files) {
                if (file.isDirectory()) {
                    fileList = Utils.listFiles(file, fileList);
                } else {
                    fileList.add(file);
                }
            }

            importFiles(fileList);
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
                    case 0:

                        break;
                    case 2:     // filename was changed
                        // with the filename changing, this changes automatically without hitting save
                        // this functionality might change
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            File old_file = (File) model.getValueAt(r, 1);
                            String path = old_file.getPath().replace(old_file.getName(), "");
                            String fileName = model.getValueAt(r, c).toString();
                            File new_file = new File(path + "//" + fileName + ".mp3");

                            songController.setFile(index, old_file, new_file);

                            old_file.renameTo(new_file);
                            model.setValueAt(new_file, r, 1);
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 3:     // title was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setTitle(index, tcl.getNewValue().toString());
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 4:     // artist was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setArtist(index, tcl.getNewValue().toString());
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 5:     // album was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setAlbum(index, tcl.getNewValue().toString());
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 6:     // album artist was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setAlbumArtist(index, tcl.getNewValue().toString());
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 7:     // year was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setYear(index, tcl.getNewValue().toString());
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 8:     // genre was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setGenre(index, tcl.getNewValue().toString());
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 9:     // tracks was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setTrack(index, tcl.getNewValue().toString());
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 10:     // disks was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setDisk(index, tcl.getNewValue().toString());
                        } else {
                            // do nothing, nothing was changed
                        }
                        break;

                    case 11:    // artwork was changed
                    // TODO:  Check to see if we can use this?
                    //setAlbumImage(index, tcl.getNewValue().toString());
                    default:    // not accounted for
                        logger.logError("Unaccounted case in TCL at col " + tcl.getColumn() + ", row " + tcl.getRow() + ": oldvalue=" + tcl.getOldValue() + ", newvalue=" + tcl.getNewValue());
                        break;
                }
            }
        };

        // declare the TCL for use
        TableCellListener tcl = new TableCellListener(table, action);
    }

    /**
     * Removes the rows from the table
     *
     * @param selectedRows the rows to remove
     */
    // TODO:  Check to see what this does to the songs array and the multpanel
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
     * Helper function to set the row icon based on the action of the row.
     *
     * @param icon, the icon to set
     * @param row, the row to set
     */
    public void setRowIcon(int icon, int row) {

        switch (icon) {
            case DEFAULT:
                table.setValueAt(new ImageIcon(this.getClass().getResource("../../resources/default.jpg")), row, 0);
                break;
            case EDITED:
                table.setValueAt(new ImageIcon(this.getClass().getResource("../../resources/edit.png")), row, 0);
                break;
            case SAVED:
                table.setValueAt(new ImageIcon(this.getClass().getResource("../../resources/check.png")), row, 0);
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

            int index = songController.getSongs().size();
            Song s = songController.getSongFromFile(file);

            // getting the image to put on the table
            Icon thumbnail_icon = Utils.getScaledImage(s.getArtwork_bytes(), 100);

            // add the row to the table
            model.addRow(new Object[]{
                new ImageIcon(this.getClass().getResource("../../resources/default.png")), // adds the default status icon
                s.getFile(), // hidden file object
                s.getFile().getName().replace(".mp3", ""), // actual editable file name
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

        // sorts the table on the filename, then the album by default
        DefaultRowSorter sorter = ((DefaultRowSorter) table.getRowSorter());
        ArrayList list = new ArrayList();

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
    }

    /**
     * Function used to import files to the table
     *
     * @param files, the files to import
     */
    public void importFiles(ArrayList<File> files) {

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
        table = new javax.swing.JTable();
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
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveTrackMenuItem = new javax.swing.JMenuItem();
        saveAllMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        refreshMenuItem = new javax.swing.JMenuItem();
        macroMenu = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        addCoversMenuItem = new javax.swing.JMenuItem();
        findAndReplaceMenuItem = new javax.swing.JMenuItem();
        addTrackNumbersMenuItem = new javax.swing.JMenuItem();
        formatFilenamesMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        settingsMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Moose");
        setResizable(false);

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
        songController.setTable(table);
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

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/moose64.png"))); // NOI18N

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
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveButton))
                    .addGroup(containerLayout.createSequentialGroup()
                        .addComponent(consoleSP, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(multPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        containerLayout.setVerticalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableSP)
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

        macroMenu.setText("Actions");

        jMenuItem2.setText("Audit...");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        macroMenu.add(jMenuItem2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        jMenuItem3.setText("AutoTag");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        macroMenu.add(jMenuItem3);

        addCoversMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        addCoversMenuItem.setText("Add Covers");
        addCoversMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCoversMenuItemActionPerformed(evt);
            }
        });
        macroMenu.add(addCoversMenuItem);

        findAndReplaceMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        findAndReplaceMenuItem.setText("Find and Replace");
        findAndReplaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findAndReplaceMenuItemActionPerformed(evt);
            }
        });
        macroMenu.add(findAndReplaceMenuItem);

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
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        jMenuItem1.setText("Command Prompt");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        helpMenu.add(jMenuItem1);

        settingsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_COMMA, java.awt.event.InputEvent.META_MASK));
        settingsMenuItem.setText("Preferences");
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

    /**
     * Action for the save button press
     *
     * @param evt
     */
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        songController.saveAll();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed

        // select some file(s)
        File[] dirs = Utils.launchJFileChooser("Select a folder to open...", "Open", JFileChooser.DIRECTORIES_ONLY, true);

        if(dirs != null) {
            // create an arraylist of files
            ArrayList<File> files = new ArrayList<>();
            for (int i = 0; i < dirs.length; i++) {
                files = Utils.listFiles(dirs[i], files);
            }

            // import the files
            importFiles(files);

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
//                row = table.rowAtPoint(evt.getPoint());
//                table.setRowSelectionInterval(row, row);
//
//                int index = getIndex(table.convertRowIndexToModel(row));
//                File file = songs.get(index).getFile();
//
//                try {
//                    Desktop desktop = Desktop.getDesktop();
//                    if (file.exists()) {
//                        desktop.open(file);
//                    }
//                } catch (IOException ex) {
//                    //Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
//                }
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
        songController.autoAddCovers(table.getSelectedRows());
    }//GEN-LAST:event_addCoversMenuItemActionPerformed

    private void saveTrackMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTrackMenuItemActionPerformed
        int[] selectedRows = table.getSelectedRows();
        songController.saveTracks(selectedRows);
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
        Main.launchSettingsFrame();
    }//GEN-LAST:event_settingsMenuItemActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        Main.launchAuditFrame();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // prompt the user to enter a command
        String command = JOptionPane.showInputDialog(this, "Enter a command:");
        if(command != null) {
            doCommand(command);
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        songController.autoTagFiles(table.getSelectedRows());
        setMultiplePanelFields();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    /**
     * Performs a command based on the user input
     *
     * @param command
     */
    public void doCommand(String command) {
        command = command.toLowerCase();
        switch (command) {
            case "clear error log":
                Main.settings.settingsController.clearErrorLog();
                break;
            case "clear event log":
                Main.settings.settingsController.clearEventLog();
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown Command!");
                break;
        }
    }

    /**
     * Show the about dialog, includes name, version, and copyright
     */
    public void showAboutDialog() {
        Icon icon = new ImageIcon(this.getClass().getResource("/resources/moose128.png"));
        JOptionPane.showMessageDialog(null,
                "Moose\nVersion: " + Main.version + "\n" + "© Pat Ripley 2018",
                "About Moose", JOptionPane.PLAIN_MESSAGE, icon);
    }

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

            int row = table.convertRowIndexToModel(selectedRows[i]);

            titles[i] = table.getValueAt(selectedRows[i], 2).toString();
            artists[i] = table.getValueAt(selectedRows[i], 3).toString();
            albums[i] = table.getValueAt(selectedRows[i], 4).toString();
            albumartists[i] = table.getValueAt(selectedRows[i], 5).toString();
            years[i] = table.getValueAt(selectedRows[i], 6).toString();
            genres[i] = table.getValueAt(selectedRows[i], 7).toString();
            tracks[i] = table.getValueAt(selectedRows[i], 8).toString();
            disks[i] = table.getValueAt(selectedRows[i], 9).toString();
            images[i] = songController.getSongs().get(songController.getIndex(row)).getArtwork_bytes();
        }

        // fill the fields
        if (songController.checkIfSame(titles[0], titles)) {
            multTitle.setText(titles[0]);
        } else {
            multTitle.setText("-");
        }

        if (songController.checkIfSame(artists[0], artists)) {
            multArtist.setText(artists[0]);
        } else {
            multArtist.setText("-");
        }

        if (songController.checkIfSame(albums[0], albums)) {
            multAlbum.setText(albums[0]);
        } else {
            multAlbum.setText("-");
        }

        if (songController.checkIfSame(albumartists[0], albumartists)) {
            multAlbumArtist.setText(albumartists[0]);
        } else {
            multAlbumArtist.setText("-");
        }

        if (songController.checkIfSame(genres[0], genres)) {
            multGenre.setText(genres[0]);
        } else {
            multGenre.setText("-");
        }

        if (songController.checkIfSame(years[0], years)) {
            multYear.setText(years[0]);
        } else {
            multYear.setText("-");
        }

        if (songController.checkIfSame(tracks[0], tracks)) {
            multTrack.setText(tracks[0]);
        } else {
            multTrack.setText("-");
        }

        if (songController.checkIfSame(disks[0], disks)) {
            multDisk.setText(disks[0]);
        } else {
            multDisk.setText("-");
        }

        if (songController.checkIfSame(images[0], images) && images[0] != null) {

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
                int row = selectedRows[i];
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(title, selectedRows[i], 2);

                // set the value in the songs array
                songController.setTitle(index, title);
            }
        }

        // check if the artist field needs updated
        if (!artist.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = selectedRows[i];
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(artist, selectedRows[i], 3);

                // set the value in the songs array
                songController.setArtist(index, artist);
            }
        }

        // check if the album field needs updated
        if (!album.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = selectedRows[i];
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(album, selectedRows[i], 4);

                // set the value in the songs array
                songController.setAlbum(index, album);
            }
        }

        // check if the album artist field needs updated
        if (!albumArtist.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = selectedRows[i];
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(albumArtist, selectedRows[i], 5);

                // set the value in the songs array
                songController.setAlbumArtist(index, albumArtist);
            }
        }

        // check if the year field needs updated
        if (!year.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = selectedRows[i];
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(year, selectedRows[i], 6);

                // set the value in the songs array
                songController.setYear(index, year);
            }
        }

        // check if the genre field needs updated
        if (!genre.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = selectedRows[i];
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(genre, selectedRows[i], 7);

                // set the value in the songs array
                songController.setGenre(index, genre);
            }
        }

        // check if the track field needs updated
        if (!track.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = selectedRows[i];
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(track, selectedRows[i], 8);

                // set the value in the songs array
                songController.setTrack(index, track);
            }
        }

        // check if the disk field needs updated
        if (!disk.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {

                // get the index of the song in the table
                int row = selectedRows[i];
                int index = songController.getIndex(row);

                // set the value in the table to the new value
                table.setValueAt(disk, selectedRows[i], 9);

                // set the value in the songs array
                songController.setDisk(index, disk);
            }
        }
    }

    /**
     * Shows a dialog with a find input and replace input
     */
    public void showFindAndReplaceDialog() {
        JTextField find = new JTextField();
        JTextField replace = new JTextField();
        Object[] message = {"Find:", find, "Replace:", replace};
        int option = JOptionPane.showConfirmDialog(null, message, "Find and Replace", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String findStr = find.getText();
            String replStr = replace.getText();
            int result = songController.findAndReplace(findStr, replStr);
            if (result == 0) {   // nothing to replace
                JOptionPane.showMessageDialog(null, "Nothing to replace!", "Find and Replace", JOptionPane.PLAIN_MESSAGE);
            } else if (result > 0) {
                JOptionPane.showMessageDialog(null, "Successfully made " + result + " replacements!", "Find and Replace", JOptionPane.PLAIN_MESSAGE);
            }
        } else {
            // user changed their mind
        }
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
    private javax.swing.JMenuItem findAndReplaceMenuItem;
    private javax.swing.JMenuItem formatFilenamesMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
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
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem refreshMenuItem;
    private javax.swing.JMenuItem saveAllMenuItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveTrackMenuItem;
    private javax.swing.JMenuItem settingsMenuItem;
    public javax.swing.JTable table;
    private javax.swing.JScrollPane tableSP;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables

}
