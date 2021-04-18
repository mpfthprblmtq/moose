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
import moose.Main;
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
    final String version = "1.2.1";
    // THIS HAS TO BE IN THIS FILE, DO NOT EDIT OR REMOVE THE PREVIOUS LINE

    // main settingsFile file
    File settingsFile;

    // main settings object
    Settings settings;

    // settings json mapper
    final ObjectMapper mapper = new ObjectMapper();

    // logger object
    Logger logger = Main.getLogger();

    public SettingsController() {
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
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
        String settingsDir_path = System.getProperty("user.home") + "/Library/Application Support/Moose/";
        File settingsDir = new File(settingsDir_path);
        if (!settingsDir.exists()) {
            if (!settingsDir.mkdirs()) {
                logger.logError("Couldn't create the main Application Support directory in "
                        + settingsDir.getParentFile().getPath());
            }
        }

        // create the settingsFile file if it doesn't already exist
        String settings_path = settingsDir_path + "moose.json";
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
        return Main.getSettings().getGenres().isEmpty();
    }

    public boolean defaultLogging() {
        settings.setDeveloperMode(false);
        settings.setDebugMode(false);
        writeSettingsFile(settings);

        // check if successful
        return !Main.getSettings().isInDebugMode() && !Main.getSettings().isInDeveloperMode();
    }

    public boolean defaultFiles() {
        settings.setLibraryLocation(StringUtils.EMPTY);
        writeSettingsFile(settings);

        // check if successful
        return StringUtils.isEmpty(Main.getSettings().getLibraryLocation());
    }

    public boolean defaultApi() {
        settings.setAlbumArtFinderApiKey(StringUtils.EMPTY);
        settings.setAlbumArtFinderCseId(StringUtils.EMPTY);
        settings.setPreferredCoverArtSize(640);
        writeSettingsFile(settings);

        // check if successful
        return StringUtils.isEmpty(Main.getSettings().getAlbumArtFinderApiKey())
                && StringUtils.isEmpty(Main.getSettings().getAlbumArtFinderCseId());
    }

    public Settings getSettings() {
        return this.settings.withVersionNumber(version);
    }

    public Settings copySettings() {
        Settings tempSettings = new Settings();
        Settings actualSettings = getSettings();
        tempSettings.setGenres(actualSettings.getGenres());
        tempSettings.setDebugMode(actualSettings.isInDebugMode());
        tempSettings.setDeveloperMode(actualSettings.isInDeveloperMode());
        tempSettings.setLibraryLocation(actualSettings.getLibraryLocation());
        tempSettings.setRemoveCommentOnAutoTagging(actualSettings.getRemoveCommentOnAutoTagging());
        tempSettings.setAskBeforeClearAll(actualSettings.isAskBeforeClearAll());
        tempSettings.setAlbumArtFinderCseId(actualSettings.getAlbumArtFinderCseId());
        tempSettings.setAlbumArtFinderApiKey(actualSettings.getAlbumArtFinderApiKey());
        tempSettings.setAlbumArtFinderSearchCount(actualSettings.getAlbumArtFinderSearchCount());
        tempSettings.setAlbumArtFinderSearchCountDate(actualSettings.getAlbumArtFinderSearchCountDate());
        tempSettings.setPreferredCoverArtSize(actualSettings.getPreferredCoverArtSize());
        return tempSettings;
    }

    /**
     * Opens the event log
     */
    public void openEventLog() {
        FileUtils.openFile(Main.logger.getEventLog());
    }

    /**
     * Opens the error log
     */
    public void openErrorLog() {
        FileUtils.openFile(Main.logger.getErrorLog());
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
