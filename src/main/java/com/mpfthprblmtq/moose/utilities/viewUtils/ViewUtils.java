/*
 *  Proj:   Moose
 *  File:   ViewUtils.java
 *  Desc:   Common functions/methods that deal with Views, Frames, and JTables. (oh, my!)
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.utilities.viewUtils;

// imports
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.CollectionUtils;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.controllers.SongController;
import com.mpfthprblmtq.moose.objects.Settings;
import com.mpfthprblmtq.moose.utilities.MP3FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;

import static com.mpfthprblmtq.moose.utilities.Constants.*;

// class ViewUtils
public class ViewUtils {

    // logger
    static Logger logger = Moose.getLogger();

    /**
     * Builds the specific table model we use
     * @return a configured table model
     */
    public static DefaultTableModel getTableModel() {
        return new DefaultTableModel() {
            @SuppressWarnings("rawtypes")
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
    }

    /**
     * Builds the specific cell editor we use
     * @return a configured cell editor
     */
    public static DefaultCellEditor getCellEditor() {
        return new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean isCellEditable(EventObject e) {
                if (e instanceof MouseEvent) {
                    if (((MouseEvent) e).getClickCount() == 2) {
                        return true;
                    }
                } else if (e instanceof KeyEvent) {
                    if (((KeyEvent) e).getKeyCode() == KeyEvent.VK_META) {
                        return false;
                    }
                }
                return super.isCellEditable(e);
            }
        };
    }

    /**
     * Sets the specified column width
     * @param column the column to set
     * @param width width in pixels
     */
    public static void setColumnWidth(JTable table, int column, int width) {
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
     * Creates the TCL for the table, which allows us to get the row, col, before and after values for each edited cell
     * @param table the table we're attaching this TCL to
     * @param songController the songController that these actions act on
     * @return a Table Cell Listener for the table
     */
    public static TableCellListener createTCL(JTable table, SongController songController) {
        TableModel model = table.getModel();
        Action action = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TableCellListener tcl = (TableCellListener) e.getSource();

                int r = tcl.getRow();
                int c = tcl.getColumn();

                int index = Integer.parseInt(model.getValueAt(r, 12).toString());

                // switch to see what column changed, and do a task based on that
                switch (c) {
                    case 0:

                        break;
                    case 2:     // filename was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            // we have to build the new file and set it in the songController
                            File oldFile = (File) model.getValueAt(r, 1);
                            File newFile = MP3FileUtils.getNewMP3FileFromOld(oldFile, tcl.getNewValue().toString());
                            songController.setNewFile(index, newFile);
                        }
                        // else do nothing, nothing was changed
                        break;

                    case 3:     // title was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setTitle(index, tcl.getNewValue().toString());
                        }
                        // else do nothing, nothing was changed
                        break;

                    case 4:     // artist was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setArtist(index, tcl.getNewValue().toString());
                        }
                        // else do nothing, nothing was changed
                        break;

                    case 5:     // album was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setAlbum(index, tcl.getNewValue().toString());
                        }
                        // else do nothing, nothing was changed
                        break;

                    case 6:     // album artist was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setAlbumArtist(index, tcl.getNewValue().toString());
                        }
                        // else do nothing, nothing was changed
                        break;

                    case 7:     // year was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setYear(index, tcl.getNewValue().toString());
                        }
                        // else do nothing, nothing was changed
                        break;

                    case 8:     // genre was changed
                        String genre = tcl.getNewValue().toString();
                        // check and see if the genre exists already
                        if (Moose.getSettings().getFeatures().get(Settings.CHECK_FOR_NEW_GENRES)) {
                            if (!Moose.getSettings().getGenres().contains(genre) && StringUtils.isNotEmpty(genre)) {
                                int res = JOptionPane.showConfirmDialog(Moose.getFrame(), "\"" + genre + "\" isn't in your built-in genre list, would you like to add it?");
                                if (res == JOptionPane.YES_OPTION) {// add the genre to the settings
                                    Moose.getSettings().getGenres().add(genre);
                                    Moose.updateSettings();
                                }
                            }
                        }
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setGenre(index, genre);

                        }
                        // else do nothing, nothing was changed
                        break;

                    case 9:     // tracks was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            String track = tcl.getNewValue().toString();

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
                            } else {
                                int row = songController.getRow(index);
                                table.setValueAt(songController.getSongs().get(index).getFullTrackString(), row, 8);
                            }
                        }
                        // else do nothing, nothing was changed
                        break;

                    case 10:     // disk was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            String disk = tcl.getNewValue().toString();

                            // set the value in the songs array
                            if (StringUtils.isEmpty(disk)) {
                                songController.setDisk(index, StringUtils.EMPTY);
                                songController.setTotalDisks(index, StringUtils.EMPTY);
                            } else if (disk.matches("\\d+/\\d+")) {
                                String[] arr = disk.split("/");
                                songController.setDisk(index, arr[0]);
                                songController.setTotalDisks(index, arr[1]);
                            } else if (disk.matches("/\\d+")) {
                                songController.setDisk(index, StringUtils.EMPTY);
                                songController.setTotalDisks(index, disk);
                            } else if (disk.matches("\\d+/")) {
                                songController.setTrack(index, disk);
                                songController.setTotalDisks(index, StringUtils.EMPTY);
                            } else {
                                int row = songController.getRow(index);
                                table.setValueAt(songController.getSongs().get(index).getFullDiskString(), row, 9);
                            }
                        }
                        // else do nothing, nothing was changed
                        break;

                    case 11:    // artwork was changed
                        // TODO:  Check to see if we can use this?
                        //setAlbumImage(index, tcl.getNewValue().toString());
                    default:    // not accounted for
                        logger.logError("Unaccounted case in TCL at col " + tcl.getColumn() + ", row " + tcl.getRow() + ": oldValue=" + tcl.getOldValue() + ", newValue=" + tcl.getNewValue());
                        break;
                }
            }
        };

        // return the TCL with the above action
        return new TableCellListener(table, action);
    }

    /**
     * Creates a popup context menu based on the booleans given
     * @param evt the MouseEvent we're using to show the popup
     * @param menuListener the ActionListener that handles all the events from this context menu
     * @param base a boolean to determine if we're showing the base popup options
     * @param file a boolean to determine if we're showing the file popup options
     * @param artwork a boolean to determine if we're showing the artwork popup options
     * @param artworkMultPanel a boolean to determine if we're showing the artwork on the mult panel popup options
     * @param customItems a list of custom item strings to add to the bottom
     */
    public static void showPopUpContextMenu(
            MouseEvent evt, ActionListener menuListener, boolean base, boolean file, boolean artwork, boolean artworkMultPanel, String[] customItems) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        if (base) {
            popup.add(item = new JMenuItem(MORE_INFO));
            item.addActionListener(menuListener);

            popup.addSeparator();

            popup.add(item = new JMenuItem(REMOVE_FROM_LIST));
            item.addActionListener(menuListener);

            popup.add(item = new JMenuItem(PLAY));
            item.addActionListener(menuListener);

            popup.add(item = new JMenuItem(SAVE));
            item.addActionListener(menuListener);

            popup.add(item = new JMenuItem(SHOW_IN_FINDER));
            item.addActionListener(menuListener);

            popup.addSeparator();

            popup.add(item = new JMenuItem(AUTO_TAG));
            item.addActionListener(menuListener);
            // only enable it if the feature is enabled
            item.setEnabled(Moose.getSettings().getFeatures().get(Settings.AUTOTAGGING));

            popup.add(item = new JMenuItem(FORMAT_FILENAME));
            item.addActionListener(menuListener);
            // only enable it if the feature is enabled
            item.setEnabled(Moose.getSettings().getFeatures().get(Settings.FORMAT_FILENAMES));

            popup.add(item = new JMenuItem(AUTO_TRACK_DISK_NUMBERS));
            item.addActionListener(menuListener);

            popup.add(item = new JMenuItem(AUTO_ARTWORK));
            item.addActionListener(menuListener);
        }
        if (file) {
            popup.addSeparator();
            popup.add(item = new JMenuItem(MOVE_FILE));
            item.addActionListener(menuListener);
        }
        if (artwork) {
            popup.addSeparator();

            popup.add(item = new JMenuItem(ADD_ARTWORK));
            item.addActionListener(menuListener);

            popup.add(item = new JMenuItem(REMOVE_ARTWORK));
            item.addActionListener(menuListener);

            popup.add(item = new JMenuItem(USE_ALBUM_ART_FINDER));
            item.addActionListener(menuListener);
            // only enable it if the feature is enabled
            item.setEnabled(Moose.getSettings().getFeatures().get(Settings.ALBUM_ART_FINDER));
        } else if (artworkMultPanel) {
            popup.add(item = new JMenuItem(ADD_ARTWORK_SELECTED));
            item.addActionListener(menuListener);

            popup.add(item = new JMenuItem(REMOVE_ARTWORK_SELECTED));
            item.addActionListener(menuListener);

            popup.add(item = new JMenuItem(USE_ALBUM_ART_FINDER));
            item.addActionListener(menuListener);
            // only enable it if the feature is enabled
            item.setEnabled(Moose.getSettings().getFeatures().get(Settings.ALBUM_ART_FINDER));
        }
        if (CollectionUtils.isNotEmpty(customItems)) {
            for (String str : customItems) {
                popup.addSeparator();
                popup.add(item = new JMenuItem(str));
                item.addActionListener(menuListener);
            }
        }

        // show the popup
        popup.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    /**
     * Returns the import albums swing worker, so I can use it multiple times
     * @param files the array of files to import
     * @return an import albums swing worker
     */
    public static SwingWorker<Void, Void> getImportFilesSwingWorker(File[] files) {
        Moose.getFrame().setLoading(true);
        // make a swing worker do the file import in a separate thread, so I can update the GUI
        return new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                // create an arraylist of files and traverse it
                java.util.List<File> fileList = new ArrayList<>();
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
                List<File> successfullyAddedFiles = Moose.getFrame().importFiles(fileList);

                // check to see if the actions can be enabled
                Moose.getFrame().setActionsEnabled(!successfullyAddedFiles.isEmpty());

                // check for new genres
                if (Moose.getSettings().getFeatures().get(Settings.CHECK_FOR_NEW_GENRES)) {
                    Moose.getSongController().checkForNewGenres(successfullyAddedFiles);
                }

                // update graphics
                Moose.getFrame().setLoading(false);

                return null;    // don't return anything since we're just playing with threads
            }
        };
    }

    /**
     * Returns the autotag swing worker, so I can use it multiple times
     * @param selectedRows the rows currently selected on the table
     * @return an autotag swing worker
     */
    public static SwingWorker<Void, Void> getAutotagSwingWorker(int[] selectedRows) {
        Moose.getFrame().setLoading(true);
        // make a swing worker do the file import in a separate thread, so I can update the GUI
        return new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                // do the autotagging
                if (Moose.getFrame().getTable().isEditing()) {
                    Moose.getFrame().getTable().getCellEditor().stopCellEditing();
                }
                Moose.getSongController().autoTagFiles(selectedRows);
                Moose.getFrame().updateMultiplePanelFields();

                // update graphics
                Moose.getFrame().setLoading(false);

                return null;    // don't return anything since we're just playing with threads
            }
        };
    }

    /**
     * Returns the save tracks swing worker, so I can use it multiple times
     * @param selectedRows the rows currently selected on the table
     * @return a save tracks swing worker
     */
    public static SwingWorker<Void, Void> getSaveTracksSwingWorker(int[] selectedRows) {
        Moose.getFrame().setLoading(true);
        // make a swing worker do the file import in a separate thread, so I can update the GUI
        return new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                // do the save
                Moose.getSongController().saveTracks(selectedRows);

                // update graphics
                Moose.getFrame().setLoading(false);

                return null;    // don't return anything since we're just playing with threads
            }
        };
    }

    /**
     * Returns the open tracks swing worker, so I can use it multiple times
     * @param selectedRows the rows currently selected on the table
     * @return an open tracks swing worker
     */
    public static SwingWorker<Void, Void> getOpenTracksSwingWorker(int[] selectedRows) {
        Moose.getFrame().setLoading(true);
        // make a swing worker do the file import in a separate thread, so I can update the GUI
        return new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                // do the open/play
                Moose.getSongController().playFiles(selectedRows);

                // update graphics
                Moose.getFrame().setLoading(false);

                return null;    // don't return anything since we're just playing with threads
            }
        };
    }

    /**
     * Returns the track/disk number swing worker, so I can use it multiple times
     * @param selectedRows the rows currently selected on the table
     * @return a track/disk number swing worker
     */
    public static SwingWorker<Void, Void> getTrackDiskNumberSwingWorker(int[] selectedRows) {
        Moose.getFrame().setLoading(true);
        // make a swing worker do the file import in a separate thread, so I can update the GUI
        return new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                // add track numbers and disk numbers
                Moose.getSongController().autoAddTrackAndDiskNumbers(selectedRows);
                Moose.getFrame().updateMultiplePanelFields();

                // update graphics
                Moose.getFrame().setLoading(false);

                return null;    // don't return anything since we're just playing with threads
            }
        };
    }

    /**
     * Shows an error dialog
     * @param message the message to show
     * @param ex the exception that occurred
     * @param component the component context
     */
    public static void showErrorDialog(String message, Exception ex, Component component) {
        JOptionPane.showMessageDialog(component, message + "\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows an error dialog
     * @param message the message to show
     * @param component the component context
     */
    public static void showErrorDialog(String message, Component component) {
        JOptionPane.showMessageDialog(component, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Autofocuses on a textField or component in a dialog using threads
     * @param component the component to focus on
     * @param context where the dialog is
     */
    public static void focusOnField(Component component, String context) {
        // create a thread to wait until the dialog box pops up
        (new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                logger.logError("Exception with threading when opening the " + context + " dialog.", e);
            }
            component.requestFocus();
        })).start();
    }

    /**
     * Utility method that returns a custom sorter for the track and disk number columns
     * @return a custom sorter
     */
    public static Comparator<String> getTrackDiskNumberSorter() {
        return (o1, o2) -> {
            if (StringUtils.isNotEmpty(o1) && StringUtils.isNotEmpty(o2)
                    && o1.matches(TRACK_DISK_REGEX) && o2.matches(TRACK_DISK_REGEX)) {
                String o1Number = o1.split("/")[0];
                String o2Number = o2.split("/")[0];
                int o1int = Integer.parseInt(o1Number);
                int o2int = Integer.parseInt(o2Number);
                return Integer.compare(o1int, o2int);
            }
            return 0;
        };
    }
}
