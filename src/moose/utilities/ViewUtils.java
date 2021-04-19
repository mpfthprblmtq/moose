package moose.utilities;

import moose.Moose;
import moose.controllers.SongController;
import moose.objects.Settings;
import moose.utilities.logger.Logger;
import moose.utilities.viewUtils.TableCellListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
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
     * Creates the TCL for the table, which allows us to get the row, col, before and after values for each edited cell
     * @param table the table we're attaching this TCL to
     * @param songController the songController that these actions act on
     * @return a Table Cell Listener for the table
     */
    public static TableCellListener createTCLAction(JTable table, SongController songController) {
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
                        // with the filename changing, this changes automatically without hitting save
                        // this functionality might change
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            File old_file = (File) model.getValueAt(r, 1);
                            String path = old_file.getPath().replace(old_file.getName(), "");
                            String fileName = model.getValueAt(r, c).toString();
                            File new_file = new File(path + "//" + fileName + ".mp3");

                            songController.setFile(index, new_file);

                            if (!old_file.renameTo(new_file)) {
                                logger.logError("Couldn't rename file! Path: " + old_file.getPath());
                            }
                            model.setValueAt(new_file, r, 1);
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
                            songController.setTrack(index, tcl.getNewValue().toString());
                        }
                        // else do nothing, nothing was changed
                        break;

                    case 10:     // disks was changed
                        if (!tcl.getNewValue().equals(tcl.getOldValue())) {
                            songController.setDisk(index, tcl.getNewValue().toString());
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
}
