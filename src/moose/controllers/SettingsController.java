/**
 *  Proj:   Moose
 *  File:   SettingsController.java
 *  Desc:   Controller class for SettingsFrame, works directly with the data based on input from AuditFrame UI
 *
 *  Copyright Pat Ripley 2018
 */

// package
package moose.controllers;

// imports
import moose.Main;
import moose.utilities.*;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

// class SettingsController
public class SettingsController {

    // main settings file
    File settings;

    // variables
    boolean debugMode;
    ArrayList<String> genres = new ArrayList<>();
    String libraryLocation;

    // logger object
    Logger logger = Main.getLogger();

    public SettingsController() {

    }

    public void setUpSupportDirectory() {
        // create the logging directory if it doesn't already exist
        String settingsDir_path = System.getProperty("user.home") + "/Library/Application Support/Moose/";
        File settingsDir = new File(settingsDir_path);
        if (!settingsDir.exists()) {
            settingsDir.mkdirs();
        }

        // create the settings file if it doesn't already exist
        String settings_path = settingsDir_path + "moose.conf";
        settings = new File(settings_path);
        if (!settings.exists()) {
            try {
                settings.createNewFile();

                // since we've created a brand new file, fill it with some default values
                fillDefaults();
            } catch (IOException ex) {
                logger.logError("Couldn't create settings file!", ex);
            }
        }
    }

    /**
     * Fills the settings file with some default values
     */
    public void fillDefaults() {
        String defDebug = "DEBUGMODE=false";
        String defGenres = "GENRES={Indie Electronic,Rock,Electronic/Rock}";
        String defLibraryLocation = "LIBRARYLOCATION=";
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(settings));

            bufferedWriter.write(defDebug);
            bufferedWriter.write("\n");
            bufferedWriter.write(defGenres);
            bufferedWriter.write("\n");
            bufferedWriter.write(defLibraryLocation);

        } catch (FileNotFoundException ex) {
            logger.logError("Couldn't find settings file!", ex);
        } catch (IOException ex) {
            logger.logError("Error reading settings file!", ex);
        }
    }/**
     * Reads the settings from the file and sets the ivars
     */
    public void readSettingsFile() {
        String line;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(settings));

            while ((line = bufferedReader.readLine()) != null) {

                // get the debugmode field
                if (line.contains("DEBUGMODE=")) {
                    setDebugMode((line.contains("true")));
                }

                // get the genres
                if (line.contains("GENRES=")) {
                    setGenres(line);
                }

                // get the library location
                if(line.contains("LIBRARYLOCATION=")) {
                    setLibraryLocation(line.replace("LIBRARYLOCATION=", ""));
                }
            }

        } catch (FileNotFoundException ex) {
            logger.logError("Couldn't find settings file!", ex);
        } catch (IOException ex) {
            logger.logError("Error reading settings file!", ex);
        }
    }

    /**
     * Sets the debugmode
     * @param bool
     */
    public void setDebugMode(boolean bool) {
        this.debugMode = bool;
    }

    /**
     * Returns the debugmode
     * @return
     */
    public boolean getDebugMode() {
        return debugMode;
    }

    /**
     * Sets the genre arraylist
     * @param arr
     */
    public void setGenres(ArrayList<String> arr) {
        this.genres = arr;
    }

    /**
     * Returns the genre arraylist
     * @return
     */
    public ArrayList<String> getGenres() {
        return genres;
    }

    /**
     * Sets the library location
     * @param libraryLocation
     */
    public void setLibraryLocation(String libraryLocation) {
        this.libraryLocation = libraryLocation;
    }

    /**
     * Returns the library location
     * @return
     */
    public String getLibraryLocation() {
        return libraryLocation;
    }

    /**
     * Sets the Genres arraylist from a String
     * @param line
     */
    public void setGenres(String line) {

        // remove the garbage from the string
        line = line.replace("GENRES=", "");
        line = line.replace("{", "");
        line = line.replace("}", "");

        // split the string based on a comma
        String[] genresArray = line.split(",");

        // clear the genres arraylist
        genres.clear();

        // convert that array to an arraylist
        genres.addAll(Arrays.asList(genresArray));
    }

    /**
     * Sets the settings.conf file to default values
     */
    public void setDefaults() {
        debugMode = false;
        genres = new ArrayList<>();
        genres.add("Indie Electronic");
        genres.add("Rock");
        genres.add("Electronic/Rock");
        libraryLocation = "Library location not set!";

        writeSettingsFile();
    }

    /**
     * Opens the event log
     */
    public void openEventLog() {
        try {
            Desktop desktop = Desktop.getDesktop();
            if (logger.getEventLog().exists()) {
                desktop.open(logger.getEventLog());
            }
        } catch (IOException ex) {
            logger.logError("Couldn't open the event log!", ex);
        }
    }

    /**
     * Opens the error log
     */
    public void openErrorLog() {
        try {
            Desktop desktop = Desktop.getDesktop();
            if (logger.getErrorLog().exists()) {
                desktop.open(logger.getErrorLog());
            }
        } catch (IOException ex) {
            logger.logError("Couldn't open the event log!", ex);
        }
    }

    /**
     * Clears the event log
     */
    public void clearEventLog() {
        if (logger.getEventLog().exists()) {
            logger.getEventLog().delete();
        }
        try {
            logger.getEventLog().createNewFile();
        } catch (IOException e) {
            logger.logError("Couldn't clear the event log!", e);
        }
    }

    /**
     * Clears the error log
     */
    public void clearErrorLog() {
        if (logger.getErrorLog().exists()) {
            logger.getErrorLog().delete();
        }
        try {
            logger.getErrorLog().createNewFile();
        } catch (IOException e) {
            logger.logError("Couldn't clear the error log!", e);
        }
    }

    public void addGenre(String genre) {
        genres.add(genre);
    }

    public void removeGenre(String genre) {
        genres.remove(genre);
    }

    /**
     * Writes the settings file from the ivars that were set in the program
     */
    public void writeSettingsFile() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(settings));

            bufferedWriter.write("DEBUGMODE=" + debugMode);
            bufferedWriter.write("\n");
            bufferedWriter.write("GENRES=" + listGenres(genres));
            bufferedWriter.write("\n");
            bufferedWriter.write("LIBRARYLOCATION=" + libraryLocation);
            bufferedWriter.flush();

        } catch (FileNotFoundException ex) {
            logger.logError("Couldn't find settings file!", ex);
        } catch (IOException ex) {
            logger.logError("Error reading settings file!", ex);
        }
    }

    /**
     * Lists the genres in a string form
     * @param genres
     * @return
     */
    public String listGenres(ArrayList<String> genres) {
        String str = "{";
        for (int i = 0; i < genres.size(); i++) {
            str = str + genres.get(i);
            if (i < genres.size() - 1) {
                str = str + ",";
            } else {
                str = str + "}";
            }
        }
        return str;
    }
}
