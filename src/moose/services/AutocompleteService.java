/*
   Proj:   Moose
   File:   AutoCompleteService.java
   Desc:   Pojo for a bundle of search information to provide to the Album Art Finder service

   Copyright Pat Ripley 2018
 */

// package
package moose.services;

// imports
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import moose.Main;
import moose.utilities.viewUtils.NameService;

import static moose.utilities.Constants.*;

public class AutocompleteService {

    /**
     * Gets the name service based on the column
     *
     * @param isGenreField, a boolean to pre-check if it's a genre
     * @param table, the table to search on
     * @return the NameService to use
     */
    public static NameService getNameService(boolean isGenreField, JTable table) {
        if(isGenreField) {
            List<String> list = new ArrayList<>(Main.getSettings().getGenres());
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

    /**
     * Gets all of the fields in the column we're typing in so we can create the NameService based on them
     *
     * @param table, the table to search on
     * @return a list of all strings in that column
     */
    private static List<String> getAllColumnFields(JTable table) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < table.getModel().getRowCount(); i++) {
            String data = table.getModel().getValueAt(i, table.convertColumnIndexToModel(table.getEditingColumn())).toString();
            if(!list.contains(data)) list.add(data);
        }
        return list;
    }
    
    
}
