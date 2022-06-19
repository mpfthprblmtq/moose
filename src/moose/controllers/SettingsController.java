/*
   Proj:   Moose
   File:   SettingsController.java
   Desc:   Controller class for SettingsFrame, works directly with the data based on input from AuditFrame UI

   Copyright Pat Ripley 2018
 */

// package
package moose.controllers;

// imports
import com.fasterxml.jackson.databind.SerializationFeature;
import moose.Moose;
import moose.utilities.*;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import moose.objects.Settings;
import moose.utilities.logger.Logger;

// class SettingsController
public class SettingsController {

    // hardened version
    // THIS HAS TO BE IN THIS FILE, DO NOT EDIT OR REMOVE THE FOLLOWING LINE
    final String version = "1.3.1";
    // THIS HAS TO BE IN THIS FILE, DO NOT EDIT OR REMOVE THE PREVIOUS LINE

    // main settingsFile file
    File settingsFile;

    // main settings object
    Settings settings;

    // settings json mapper
    final ObjectMapper mapper = new ObjectMapper();

    // logger object
    Logger logger = Moose.getLogger();

    public SettingsController() {
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        // create the settings object
        this.settings = new Settings();
    }

    public void readSettingsFile() {
        try {
            String jsonString = new String(Files.readAllBytes(settingsFile.toPath()));
            settings = mapper.readValue(jsonString, Settings.class);
        } catch (IOException e) {
            logger.logError("Exception while reading the settings json!", e);
        }
    }

    public void setUpSupportDirectory() {
        // create the logging directory if it doesn't already exist
        File settingsDir = new File(settings.getApplicationSupportLocation());
        if (!settingsDir.exists()) {
            if (!settingsDir.mkdirs()) {
                logger.logError("Couldn't create the main Application Support directory in "
                        + settingsDir.getParentFile().getPath());
            }
        }

        // create the settingsFile file if it doesn't already exist
        String settings_path = settings.getApplicationSupportLocation() + "moose.json";
        settingsFile = new File(settings_path);
        if (!settingsFile.exists()) {
            try {
                if (!settingsFile.createNewFile()) {
                    throw new IOException("IOException when trying to create the settings file " + settings_path);
                }

                // since we've created a brand new file, fill it with some default values
                fillDefaults();
            } catch (IOException ex) {
                logger.logError("Couldn't create settings file!", ex);
            }
        }
    }

    /**
     * Fills the settingsFile file with some default values
     * @return the result of writing the settings file
     */
    public boolean fillDefaults() {
        settings = new Settings();
        return writeSettingsFile(settings);
    }

    public boolean defaultGenres() {
        settings.setGenres(new ArrayList<>());
        writeSettingsFile(settings);

        // check if successful
        return Moose.getSettings().getGenres().isEmpty();
    }

    public boolean defaultLogging() {
        settings.setDeveloperMode(false);
        settings.setDebugMode(false);
        writeSettingsFile(settings);

        // check if successful
        return !Moose.getSettings().isInDebugMode() && !Moose.getSettings().isInDeveloperMode();
    }

    public boolean defaultFiles() {
        settings.setLibraryLocation(StringUtils.EMPTY);
        writeSettingsFile(settings);

        // check if successful
        return StringUtils.isEmpty(Moose.getSettings().getLibraryLocation());
    }

    public boolean defaultApi() {
        settings.setAlbumArtFinderApiKey(StringUtils.EMPTY);
        settings.setAlbumArtFinderCseId(StringUtils.EMPTY);
        settings.setPreferredCoverArtSize(640);
        writeSettingsFile(settings);

        // check if successful
        return StringUtils.isEmpty(Moose.getSettings().getAlbumArtFinderApiKey())
                && StringUtils.isEmpty(Moose.getSettings().getAlbumArtFinderCseId());
    }

    public Settings getSettings() {
        return this.settings.withVersionNumber(version);
    }

    /**
     * Opens the event log
     */
    public void openEventLog() {
        FileUtils.openFile(Moose.logger.getEventLog());
    }

    /**
     * Opens the error log
     */
    public void openErrorLog() {
        FileUtils.openFile(Moose.logger.getErrorLog());
    }

    /**
     * Clears the event log
     */
    public void clearEventLog() {
        try {
            Files.newBufferedWriter(Paths.get(logger.getEventLog().getPath()));
        } catch (IOException e) {
            logger.logError("Couldn't clear the event log!", e);
        }
    }

    /**
     * Clears the error log
     */
    public void clearErrorLog() {
        try {
            Files.newBufferedWriter(Paths.get(logger.getErrorLog().getPath()));
        } catch (IOException e) {
            logger.logError("Couldn't clear the error log!", e);
        }
    }

    /**
     * Writes the settingsFile file from the ivars that were set in the program
     */
    public boolean writeSettingsFile(Settings settings) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(settingsFile));
            bufferedWriter.write(mapper.writeValueAsString(settings));
            bufferedWriter.flush();
            this.settings = settings;
            return true;
        } catch (FileNotFoundException ex) {
            logger.logError("Couldn't find settings file!", ex);
        } catch (IOException ex) {
            logger.logError("Error reading settings file!", ex);
        }
        return false;
    }
}
