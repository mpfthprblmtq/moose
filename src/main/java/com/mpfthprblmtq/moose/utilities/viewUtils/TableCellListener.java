/*
 *  Proj:   Moose
 *  File:   TableCellListener.java
 *  Desc:   Helper class that controls changing of cell values
 *          NOTE:  Not mine, but edited to fit my use case
 *
 *  Copyright ???
 *  Copyright Pat Ripley 2018-2023
 */

package com.mpfthprblmtq.moose.utilities.viewUtils;

// imports
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/*
 *  This class listens for changes made to the data in the table via the
 *  TableCellEditor. When editing is started, the value of the cell is saved
 *  When editing is stopped the new value is saved. When the old and new
 *  values are different, then the provided Action is invoked.
 *  The source of the Action is a TableCellListener instance.
 */
// class TableCellListener
@Data
@AllArgsConstructor
public class TableCellListener implements PropertyChangeListener, Runnable {
    private final JTable table;
    private Action action;

    private int row;
    private int column;
    private Object oldValue;
    private Object newValue;

    /**
     * Creates a TableCellListener.
     * @param table  the table to be monitored for data changes
     * @param action the Action to invoke when cell data is changed
     */
    public TableCellListener(JTable table, Action action) {
        this.table = table;
        this.action = action;
        this.table.addPropertyChangeListener(this);
    }

    @Override   // Implement the PropertyChangeListener interface
    public void propertyChange(PropertyChangeEvent e) {
        //  A cell has started/stopped editing
        if ("tableCellEditor".equals(e.getPropertyName())) {
            if (table.isEditing()) {
                processEditingStarted();
            } else {
                processEditingStopped();
            }
        }
    }

    /**
     * Saves information of the cell about to be edited
     */
    private void processEditingStarted() {
        //  The invokeLater is necessary because the editing row and editing column of the table have
        //  not been set when the "tableCellEditor" PropertyChangeEvent is fired.
        //  This results in the "run" method below being invoked
        SwingUtilities.invokeLater(this);
    }

    @Override
    public void run() {
        if (table.getEditingRow() >= 0) {
            row = table.convertRowIndexToModel(table.getEditingRow());
            column = table.convertColumnIndexToModel(table.getEditingColumn());
            oldValue = table.getModel().getValueAt(row, column);
            newValue = null;
        }
    }

    /**
     * Updates the Cell history when necessary
     */
    private void processEditingStopped() {
        // if the table model is empty, we don't need to worry about this
        if (table.getModel().getRowCount() == 0 && table.getModel().getColumnCount() == 0) {
            return;
        }

        // get the new value
        newValue = table.getModel().getValueAt(row, column);

        //  The data has changed, invoke the supplied Action
        if (!newValue.equals(oldValue)) {
            //  Make a copy of the data in case another cell starts editing while processing this change

            TableCellListener tcl = new TableCellListener(getTable(), getAction(), getRow(), getColumn(), getOldValue(), getNewValue());

            ActionEvent event = new ActionEvent(tcl, ActionEvent.ACTION_PERFORMED, "");
            action.actionPerformed(event);
        }
    }
}