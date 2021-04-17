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
}
