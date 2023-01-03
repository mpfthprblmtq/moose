/*
 * Proj:   Moose
 * File:   SettingsController.java
 * Desc:   Controller class for SettingsFrame, works directly with the data based on input from AuditFrame UI
 *
 * Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

// package
package com.mpfthprblmtq.moose.controllers;

// imports
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;

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
import java.util.HashSet;
import java.util.Set;

import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Settings;

// class SettingsController
public class SettingsController {

    // hardened version
    // THIS HAS TO BE IN THIS FILE, DO NOT EDIT OR REMOVE THE FOLLOWING LINE
    final String version = "1.4.0";
    // THIS HAS TO BE IN THIS FILE, DO NOT EDIT OR REMOVE THE PREVIOUS LINE

    // main settingsFile file
    File settingsFile;

    // main settings object
    Settings settings;

    // settings json mapper
    final ObjectMapper mapper = new ObjectMapper();

    // logger object
    Logger logger = Moose.getLogger();

    /**
     * Creates new SettingsController and sets up the JsonMapper and Settings object
     */
    public SettingsController() {
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        // create the settings object
        this.settings = new Settings();
    }

    /**
     * Reads the settings file into the settings field object
     */
    public void readSettingsFile() {
        try {
            String jsonString = new String(Files.readAllBytes(settingsFile.toPath()));
            if (StringUtils.isEmpty(jsonString)) {
                fillDefaults();
                jsonString = new String(Files.readAllBytes(settingsFile.toPath()));
            }
            settings = mapper.readValue(jsonString, Settings.class);
        } catch (IOException e) {
            logger.logError("Exception while reading the settings json!", e);
        }
    }

    /**
     * Creates the application support directory and the main settings file if it doesn't exist
     */
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

                // since we've created a new file, fill it with some default values
                fillDefaults();
            } catch (IOException ex) {
                logger.logError("Couldn't create settings file!", ex);
            }
        }
    }

    /**
     * Fills the settingsFile file with some default values
     */
    public void fillDefaults() {
        settings = new Settings();
        writeSettingsFile(settings);
    }

    /**
     * Blanks out the genre list in settings
     * @return if the list of genres is empty
     */
    public boolean defaultGenres() {
        settings.setGenres(new ArrayList<>());
        writeSettingsFile(settings);

        // check if successful
        return Moose.getSettings().getGenres().isEmpty();
    }

    /**
     * Defaults the logging level
     * @return the result of setting the values to default
     */
    public boolean defaultLogging() {
        settings.setInDeveloperMode(false);
        settings.setInDebugMode(false);
        writeSettingsFile(settings);

        // check if successful
        return !Moose.getSettings().isInDebugMode() && !Moose.getSettings().isInDeveloperMode();
    }

    /**
     * Defaults the files configuration
     * @return the result of setting the values to default
     */
    public boolean defaultFiles() {
        settings.setLibraryLocation(StringUtils.EMPTY);
        writeSettingsFile(settings);

        // check if successful
        return StringUtils.isEmpty(Moose.getSettings().getLibraryLocation());
    }

    /**
     * Clears out and defaults any Spotify API, Google Search API keys and ids
     * @return the result of defaulting those values
     */
    public boolean defaultApi() {
        // search api
        settings.setAlbumArtFinderApiKey(StringUtils.EMPTY);
        settings.setAlbumArtFinderCseId(StringUtils.EMPTY);
        settings.setPreferredCoverArtSize(640);
        // spotify api
        settings.setSpotifyClientId(StringUtils.EMPTY);
        settings.setSpotifyClientSecret(StringUtils.EMPTY);

        writeSettingsFile(settings);

        // check if successful
        return StringUtils.isEmpty(Moose.getSettings().getAlbumArtFinderApiKey())
                && StringUtils.isEmpty(Moose.getSettings().getAlbumArtFinderCseId());
    }

    /**
     * Defaults all features to true
     * @return the result of setting those features to true
     */
    public boolean defaultFeatures() {
        settings.getFeatures().replaceAll((k,v) -> v = true);
        writeSettingsFile(settings);

        // check if successful (throws values of settings into set to check if only one value type exists)
        Set<Boolean> values = new HashSet<>(settings.getFeatures().values());
        return values.size() == 1 && Moose.getSettings().getFeatures().get(Settings.AUTOTAGGING);
    }

    /**
     * Returns the settings object with a version number
     * @return the settings object with a version number
     */
    public Settings getSettings() {
        return this.settings.withVersionNumber(version);
    }

    /**
     * Opens the event log. Logs exception if it can't open the file.
     */
    public void openEventLog() {
        try {
            FileUtils.openFile(logger.getEventLog());
        } catch (Exception e) {
            logger.logError("Couldn't open event log file: " + logger.getEventLog().getPath(), e);
        }
    }

    /**
     * Opens the error log. Logs exception if it can't open the file.
     */
    public void openErrorLog() {
        try {
            FileUtils.openFile(logger.getErrorLog());
        } catch (Exception e) {
            logger.logError("Couldn't open error log file: " + logger.getErrorLog().getPath(), e);
        }
    }

    /**
     * Clears the event log. Logs exception if it can't clear out the file.
     */
    public void clearEventLog() {
        try {
            Files.newBufferedWriter(Paths.get(logger.getEventLog().getPath()));
        } catch (IOException e) {
            logger.logError("Couldn't clear the event log!", e);
        }
    }

    /**
     * Clears the error log. Logs exception if it can't clear out the file.
     */
    public void clearErrorLog() {
        try {
            Files.newBufferedWriter(Paths.get(logger.getErrorLog().getPath()));
        } catch (IOException e) {
            logger.logError("Couldn't clear the error log!", e);
        }
    }

    /**
     * Writes the settingsFile file from the fields that were set at any point in the application.
     * @param settings the Settings object to write
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

    /**
     * Adds a spotify artist to the list of known spotify artists
     * @param name the name of the artist (key)
     * @param id the id of the artist (value)
     */
    public void addSpotifyArtist(String name, String id) {
        settings.getSpotifyArtists().put(name, id);
        writeSettingsFile(settings);
    }
}
