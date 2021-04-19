package moose.utilities;

public class StringUtils {

    public static final String EMPTY = "";
    public static final String NEW_LINE = "\n";

    /**
     * Checks a string if it's empty or not
     *
     * @param str, the string to check
     * @return the result of the check
     */
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }
        return str.equals(EMPTY);
    }

    /**
     * Utility function to set a string to either "" or the string value (really just prevents null values)
     * @param s the string to check
     * @return the valid string
     */
    public static String validateString(String s) {
        return isEmpty(s) ? EMPTY : s;
    }

    /**
     * Checks if a string is the same throughout an array
     * @param str, the string to check
     * @param arr, the array of strings
     * @return the result of the check
     */
    public static boolean checkIfSame(String str, String[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (!arr[i].equals(str)) {
                return false;
            }
        }
        return true;
    }
}
