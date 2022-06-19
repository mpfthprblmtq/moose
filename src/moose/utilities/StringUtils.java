package moose.utilities;

import java.util.List;
import java.util.Objects;

public class StringUtils {

    public static final String EMPTY = "";
    public static final String NEW_LINE = "\n";
    public static final String SPACE = " ";

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

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
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

    /**
     * Cleans a string if it's a file (from mac, might need to test this on Windows)
     * @param s, the string to clean (replace : with /)
     * @return a cleaned string
     */
    public static String cleanFilenameString(String s) {
        return s.replaceAll(":", "/");
    }

    /**
     * Same utility function, finds common longest string from list of strings
     * @param strings, the list of strings
     * @return the longest common string
     */
    public static String same(List<String> strings) {

        // result string
        String result = "";

        // get first string from list as reference
        String s = strings.get(0);

        for (int i = 0; i < s.length(); i++) {
            for (int j = i + 1; j <= s.length(); j++) {

                // generating all possible substrings of our reference string strings.get(0) i.e s
                String stem = s.substring(i, j);
                int k;
                for (k = 1; k < strings.size(); k++) {
                    // check if the generated stem is common to all words
                    if (!strings.get(k).contains(stem))
                        break;
                }

                // if current substring is present in all strings and its length is greater than current result
                if (k == strings.size() && result.length() < stem.length())
                    result = stem;
            }
        }

        return result;
    }
}
