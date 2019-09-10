/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moose.services;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import moose.Main;
import moose.utilities.NameService;

/**
 *
 * @author pat
 */
public class AutocompleteService {

    // column constants
    private static final int TABLE_COLUMN_TITLE = 2;
    private static final int TABLE_COLUMN_ARTIST = 3;
    private static final int TABLE_COLUMN_ALBUM = 4;
    private static final int TABLE_COLUMN_ALBUMARTIST = 5;
    private static final int TABLE_COLUMN_YEAR = 6;
    private static final int TABLE_COLUMN_GENRE = 7;
    private static final int TABLE_COLUMN_TRACK = 8;
    private static final int TABLE_COLUMN_DISK = 9;
    private static final int TABLE_COLUMN_ALBUMART = 10;
    
    public static NameService getNameService(boolean isGenreField, JTable table) {
        List<String> list = new ArrayList<>();
        if(isGenreField) {
            list.addAll(Main.getSettings().getGenres());
            return new NameService(list);
        } else {
            if(table.getEditingColumn() == TABLE_COLUMN_TITLE
                    || table.getEditingColumn() == TABLE_COLUMN_ARTIST
                    || table.getEditingColumn() == TABLE_COLUMN_ALBUM
                    || table.getEditingColumn() == TABLE_COLUMN_ALBUMARTIST
                    || table.getEditingColumn() == TABLE_COLUMN_YEAR) {
                return new NameService(getAllColumnFields(table));
            } else {
                return new NameService(new ArrayList<>());
            }
        }
    }
    
    private static List<String> getAllColumnFields(JTable table) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < table.getModel().getRowCount(); i++) {
            String data = table.getModel().getValueAt(i, table.convertColumnIndexToModel(table.getEditingColumn())).toString();
            if(!list.contains(data)) list.add(data);
        }
        return list;
    }
    
    
}
