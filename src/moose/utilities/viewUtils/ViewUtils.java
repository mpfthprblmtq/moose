package moose.utilities.viewUtils;

import moose.Moose;
import moose.controllers.SongController;
import moose.objects.Settings;
import moose.utilities.FileUtils;
import moose.utilities.StringUtils;
import moose.utilities.logger.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EventObject;

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
                }
                return super.isCellEditable(e);
            }
        };
    }

    /**
     * Sets the specified column width
     *
     * @param column, the column to set
     * @param width,  width in pixels
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
                            File newFile = FileUtils.getNewMP3FileFromOld(oldFile, tcl.getNewValue().toString());
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
                        if (!Moose.getSettings().getGenres().contains(genre) && !StringUtils.isEmpty(genre)) {
                            int res = JOptionPane.showConfirmDialog(Moose.frame, "\"" + genre + "\" isn't in your built-in genre list, would you like to add it?");
                            if (res == JOptionPane.YES_OPTION) {// add the genre to the settings
                                Settings settings = Moose.getSettings();
                                settings.addGenre(genre);
                                Moose.updateSettings(settings);
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

                    case 10:     // disks was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            String disk = tcl.getNewValue().toString();

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
                        logger.logError("Unaccounted case in TCL at col " + tcl.getColumn() + ", row " + tcl.getRow() + ": oldvalue=" + tcl.getOldValue() + ", newvalue=" + tcl.getNewValue());
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
     * @param rows the number of rows currently selected
     * @param base a boolean to determine if we're showing the base popup options
     * @param file a boolean to determine if we're showing the file popup options
     * @param artwork a boolean to determine if we're showing the artwork popup options
     * @param artworkMultPanel a boolean to determine if we're showing the artwork on the mult panel popup options
     */
    public static void showPopUpContextMenu(
            MouseEvent evt, ActionListener menuListener, int rows, boolean base, boolean file, boolean artwork, boolean artworkMultPanel) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        if (base) {
            popup.add(item = new JMenuItem("More info..."));
            item.addActionListener(menuListener);
            popup.addSeparator();
            popup.add(item = new JMenuItem("Remove from list"));
            item.addActionListener(menuListener);
            popup.add(item = new JMenuItem("Play"));
            item.addActionListener(menuListener);
            popup.add(item = new JMenuItem("Save"));
            item.addActionListener(menuListener);
            popup.add(item = new JMenuItem("Show in Finder..."));
            item.addActionListener(menuListener);
            popup.addSeparator();
            popup.add(item = new JMenuItem("Autotag"));
            item.addActionListener(menuListener);
            popup.add(item = new JMenuItem("Format filenames"));
            item.addActionListener(menuListener);
            popup.add(item = new JMenuItem("Auto-add track/disk numbers"));
            item.addActionListener(menuListener);
            popup.add(item = new JMenuItem("Auto-add artwork"));
            item.addActionListener(menuListener);
            popup.addSeparator();
        }
        if (file) {
            popup.add(item = new JMenuItem(rows > 1 ? "Move file..." : "Move files..."));
            item.addActionListener(menuListener);
        }
        if (artwork) {
            popup.add(item = new JMenuItem("Add artwork..."));
            item.addActionListener(menuListener);
            popup.add(item = new JMenuItem("Remove artwork"));
            item.addActionListener(menuListener);
        } else if (artworkMultPanel) {
            popup.add(item = new JMenuItem("Add artwork for selected..."));
            item.addActionListener(menuListener);
            popup.add(item = new JMenuItem("Remove artwork for selected"));
            item.addActionListener(menuListener);
        }

        // show the popup
        popup.show(evt.getComponent(), evt.getX(), evt.getY());
    }
}
