/**
 *  File:   Logger.java
 *  Desc:   Custom logger class used to... log...
 *
 *  Copyright Pat Ripley 2018
 */

// package
package moose;

// imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

// class Logger
public class Logger {

    // streams
    PrintStream errorStream;
    PrintStream eventStream;
    PrintStream console = System.out;   // store current System.out before assigning a new value

    // current date
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    Date date;

    public Logger() {

        // create the logging directory if it doesn't already exist
        String logsDir_path = System.getProperty("user.home") + "/Library/Application Support/Moose/";
        File logsDir = new File(logsDir_path);
        if (!logsDir.exists()) {
            System.out.println(logsDir.mkdirs());
        }

        // create the error log
        String errorLog_path = logsDir_path + "errorLog.log";
        File errorLog = new File(errorLog_path);
        if (!errorLog.exists()) {
            try {
                errorLog.createNewFile();
            } catch (IOException ex) {
                logError("Couldn't create error log!  Well, this is redundant...", ex);
            }
        }

        // create the event log
        String eventLog_path = logsDir_path + "eventLog.log";
        File eventLog = new File(eventLog_path);
        if (!eventLog.exists()) {
            try {
                eventLog.createNewFile();
            } catch (IOException ex) {
                logError("Couldn't create eventLog!", ex);
            }
        }

        // set the streams for each file
        try {
            errorStream = new PrintStream(new FileOutputStream(errorLog, true));
            eventStream = new PrintStream(new FileOutputStream(eventLog, true));
        } catch (FileNotFoundException ex) {
            System.err.println(ex);
            //log(ex.getLocalizedMessage(), ERROR);
        }

        // by default, setting the System.out to the errorStream just in case I missed catches
        System.setOut(errorStream);
        System.setErr(errorStream);
    }

    /**
     * Method that actually does the logging for errors
     * @param str the localized string
     * @param ex the exception text
     */
    public void logError(String str, Exception ex) {

        // get the date to format it
        date = new Date();
        String dateStr = "[" + sdf.format(date) + "]";
        
        // set the System.out stream to error
        System.setOut(errorStream);
        
        // output the statement
        System.out.printf("%-21s %s", dateStr, str + "\n");
        System.out.printf("%-21s %s", "", "Exception details:  " + ex.toString() + ", " + ex.getStackTrace()[0] + "\n");
    }
    
    /**
     * Method that actually does the logging for events
     * @param str the localized string
     */
    public void logEvent(String str) {

        // get the date to format it
        date = new Date();
        String dateStr = "[" + sdf.format(date) + "]";
        
        // set the System.out stream to event
        System.setOut(eventStream);
        
        // output the statement
        System.out.printf("%-22s", dateStr);
        System.out.printf(str + "\n");
    }

}
