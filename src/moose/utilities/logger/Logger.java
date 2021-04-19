/*
   Proj:   Moose
   File:   Logger.java
   Desc:   Custom logger class used to... log...

   Copyright Pat Ripley 2018
 */

// package
package moose.utilities.logger;

// imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import moose.Moose;

// class Logger
public class Logger {

    Logger logger = Moose.getLogger();

    // streams
    static PrintStream errorStream;
    static PrintStream eventStream;
    static PrintStream console = System.out;   // store current System.out before assigning a new value
    
    File errorLog;
    File eventLog;
    
    public String appSupportPath;

    // current date
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    Date date;

    public Logger() {
        
        this.appSupportPath = System.getProperty("user.home") + "/Library/Application Support/Moose/";

        // create the logging directory if it doesn't already exist
        String logsDir_path = appSupportPath + "Logs/";
        File logsDir = new File(logsDir_path);
        if (!logsDir.exists()) {
            if (!logsDir.mkdirs()) {
                logger.logError("Couldn't create the logs directory!  Path: " + logsDir_path);
            }
        }

        // create the error log
        String errorLog_path = logsDir_path + "errorLog.log";
        errorLog = new File(errorLog_path);
        if (!errorLog.exists()) {
            try {
                if (!errorLog.createNewFile()) {
                    throw new IOException("IOException thrown trying to create the error log!");
                }
            } catch (IOException ex) {
                logError("Couldn't create error log!  Well, this is redundant...", ex);
            }
        }

        // create the event log
        String eventLog_path = logsDir_path + "eventLog.log";
        eventLog = new File(eventLog_path);
        if (!eventLog.exists()) {
            try {
                if (!eventLog.createNewFile()) {
                    throw new IOException("IOException thrown trying to create the event log!");
                }
            } catch (IOException ex) {
                logError("Couldn't create eventLog!", ex);
            }
        }

        // set the streams for each file
        try {
            errorStream = new PrintStream(new FileOutputStream(errorLog, true));
            eventStream = new PrintStream(new FileOutputStream(eventLog, true));
        } catch (FileNotFoundException ex) {
            logger.logError("Couldn't set the print streams to error log and event log!", ex);
        }

        // by default, setting the System.out to the errorStream just in case I missed catches
        if (!Moose.getSettings().isInDeveloperMode()) {
            setSystemOutToConsole();
            setSystemErrToConsole();
        } else {
            setSystemOutToEventLog();
            setSystemErrToErrorLog();
        }
    }
    
    /**
     * Sets the System.err stream back to the console for debugging in the IDE
     */
    public static void setSystemOutToConsole() {
        System.setOut(console);
    }
    
    /**
     * Sets the System.out stream to the eventLog
     */
    public static void setSystemOutToEventLog() {
        System.setOut(eventStream);
    }
    
    /**
     * Sets the System.err stream to the console
     */
    public static void setSystemErrToConsole() {
        System.setOut(console);
    }
    
    /**
     * Sets the System.err stream to the console
     */
    public static void setSystemErrToErrorLog() {
        System.setOut(errorStream);
    }
    
    public static void disableLogging() {
        System.setErr(null);
        System.setOut(null);
    }

    /**
     * Returns the error log
     * @return the error log File
     */
    public File getErrorLog() {
        return errorLog;
    }

    /**
     * Returns the event log
     * @return the event log File
     */
    public File getEventLog() {
        return eventLog;
    }

    /**
     * Method that actually does the logging for errors
     * @param str the localized string
     * @param ex the exception text
     */
    public final void logError(String str, Exception ex) {

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
     * Method that actually does the logging for errors
     * @param str the localized string
     */
    public void logError(String str) {

        // get the date to format it
        date = new Date();
        String dateStr = "[" + sdf.format(date) + "]";
        
        // set the System.out stream to error
        System.setOut(errorStream);
        
        // output the statement
        System.out.printf("%-21s %s", dateStr, str + "\n");
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
        System.out.print(str + "\n");
        
        // put it back to errorStream by default
        System.setOut(errorStream);
    }

}
