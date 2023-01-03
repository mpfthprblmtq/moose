/*
 *  Proj:   Moose
 *  File:   AuditCleanupUtils.java
 *  Desc:   A utility class to pull out common logic from the audit and cleanup functionality.
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.utilities;

// imports
import java.util.List;

// class AuditCleanupUtils
public class AuditCleanupUtils {

    /**
     * Helper function to get the percentage for the progressBar
     * @param index where we are in relation to the total
     * @param total the total number of indices we're working with
     */
    public static int formatPercentage(double index, double total) {
        if (index + 1 == total) {
            return 100;
        }
        double ratio = (index + 1) / total;
        double percentage = Math.ceil(ratio * 100);
        return (int) percentage;
    }

    /**
     * Helper function to check if any of the lists are empty
     * @param lists the list of lists
     * @return the result of the check
     */
    public static boolean isListOfListsEmpty(List<List<String>> lists) {
        for (List<String> listInList : lists) {
            if (!listInList.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper function to clear out all lists in a list
     * @param lists the list of lists
     */
    public static void clearLists(List<List<String>> lists) {
        for (List<String> listInList : lists) {
            listInList.clear();
        }
    }
}
