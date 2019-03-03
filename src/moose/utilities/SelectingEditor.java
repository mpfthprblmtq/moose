/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moose.utilities;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 *
 * @author pat
 */
public class SelectingEditor extends DefaultCellEditor {

    public SelectingEditor(JTextField textField) {
        super(textField);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        if (c instanceof JTextComponent) {
            JTextComponent jtc = (JTextComponent) c;
            jtc.requestFocus();
//                    jtc.selectAll();
            SwingUtilities.invokeLater(() -> {
                jtc.selectAll();
            });
        }
        return c;
    }
}
