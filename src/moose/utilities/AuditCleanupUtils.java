package moose.utilities;

import java.util.List;

public class AuditCleanupUtils {

    /**
     * Helper function to get the percentage for the progressBar
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
     * @return the result of the check
     */
    public static boolean isListOfListsEmpty(List<List<String>> list) {
        for (List<String> listInList : list) {
            if (!listInList.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper function to clear out all lists in a list
     */
    public static void clearLists(List<List<String>> list) {
        for (List<String> listInList : list) {
            listInList.clear();
        }
    }
}
