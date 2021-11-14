package moose.utilities;

import moose.Moose;
import moose.utilities.logger.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    static Logger logger = Moose.getLogger();

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

    /**
     * Checks to see if a date is the same as another
     * @param date, the date to compare to today
     */
    public static boolean isDateSameAsToday(Date date) {

        Date dateToday = new Date();
        Calendar today = Calendar.getInstance();
        today.setTime(dateToday);

        Calendar compare = Calendar.getInstance();
        compare.setTime(date);

        return today.get(Calendar.DAY_OF_MONTH) == compare.get(Calendar.DAY_OF_MONTH) &&
                today.get(Calendar.MONTH) == compare.get(Calendar.MONTH) &&
                today.get(Calendar.YEAR) == compare.get(Calendar.YEAR);
    }
}
