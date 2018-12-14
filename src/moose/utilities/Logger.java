/**
 *  Proj:   Moose
 *  File:   Logger.java
 *  Desc:   Custom logger class used to... log...
 *
 *  Copyright Pat Ripley 2018
 */

// package
package moose.utilities;

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
            logsDir.mkdirs();
        }

        // create the error log
        String errorLog_path = logsDir_path + "errorLog.log";
        errorLog = new File(errorLog_path);
        if (!errorLog.exists()) {
            try {
                errorLog.createNewFile();
            } catch (IOException ex) {
                logError("Couldn't create error log!  Well, this is redundant...", ex);
            }
        }

        // create the event log
        String eventLog_path = logsDir_path + "eventLog.log";
        eventLog = new File(eventLog_path);
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
     * Sets the System.out and System.err stream back to the console for debugging in the IDE
     */
    public void setSystemOutToConsole() {
        System.setOut(console);
        System.setErr(console);
    }
    
    /**
     * Sets the System.out and System.err stream to the errorLog
     */
    public void setSystemOutToLog() {
        System.setOut(errorStream);
        System.setErr(errorStream);
    }
    
    /**
     * Sets the System.out stream to the eventLog
     */
    public void setSystemOutToEventLog() {
        System.setOut(eventStream);
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
        System.out.printf(str + "\n");
        
        // put it back to errorstream by default
        System.setOut(errorStream);
    }

}
