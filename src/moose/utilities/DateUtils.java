package moose.utilities;

import moose.Main;
import moose.utilities.logger.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    static Logger logger = Main.getLogger();

    // date formatter
    static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

    /**
     * Formats a Date object to a string
     *
     * @param date, the date to format
     * @return the formatted date
     */
    public static String formatDate(Date date) {
        return sdf.format(date);
    }

    /**
     * Gets a Date object from a string
     *
     * @param date the date string to parse
     * @return a date object
     */
    public static Date getDate(String date) {
        try {
            return sdf.parse(date);
        } catch (ParseException ex) {
            logger.logError("ParseException when parsing date \"" + date + "\"");
            return null;
        }
    }
}
