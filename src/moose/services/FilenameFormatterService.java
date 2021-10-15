/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moose.services;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import moose.Moose;
import moose.controllers.SongController;
import moose.objects.Song;
import moose.utilities.FileUtils;
import moose.utilities.SongUtils;
import moose.utilities.StringUtils;

import static moose.utilities.Constants.*;


/**
 * @author pat
 */
public class FilenameFormatterService {

    // auto tagging service
    AutoTaggingService autoTaggingService;

    public FilenameFormatterService(SongController songController) {
        this.autoTaggingService = songController.getAutoTaggingService();
    }

    /**
     * Formats filenames to a more suitable standard if we find a match
     *
     * @param file, a file with a name to format
     * @param singleFile, a boolean to tell us if the file is a single, so there's only one
     * @return a new and improved file name
     */
    public String formatFilename(File file, boolean singleFile) {
        // clean it up first
        String filename = cleanupFilename(file.getName());
        file = FileUtils.getNewMP3FileFromOld(file, filename);

        // then process it heavily
        filename = getBetterFilename(file, singleFile);
        return filename;
    }

    /**
     * Cleans up the file name before doing any intense operations, which for now is just replacing feat with ft
     */
    private String cleanupFilename(String filename) {
        return filename
                .replace("feat.", "ft.")
                .replace("Feat.", "ft.");
    }

    /**
     * Checks for a match of known filename regexes, then applies fixes to the filename
     *
     * @param file       the file with the name to change
     * @param singleFile a boolean to tell us if the file is single, so there's only one
     * @return a good filename, or null if the fix couldn't be applied
     */
    public String getBetterFilename(File file, boolean singleFile) {
        String filename = file.getName();
        String trackNumber = StringUtils.EMPTY;
        String trackTitle = StringUtils.EMPTY;

        // try and replace the artist before we use the regex
        String artist = autoTaggingService.getArtist(file);
        filename = filename.replaceFirst(artist, StringUtils.EMPTY);

        // perform regex search on filename
        // first check if it matches the ## Title.mp3 format
        if (filename.matches(FILENAME_PRECHECK_REGEX)) {
            Pattern pattern = Pattern.compile(FILENAME_REGEX);
            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                trackNumber = matcher.group("TrackNumber");
                trackTitle = matcher.group("TrackTitle");
            }
        } else {
            // didn't match precheck regex
            // let's try and get the title by trimming nonsense out
            Pattern pattern = Pattern.compile(FILENAME_TRIM_REGEX);
            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                trackTitle = matcher.group("TrackTitle");
            }
            trackTitle = trackTitle.trim();
        }

        // check to see if it's a single file
        if (StringUtils.isEmpty(trackNumber) && singleFile) {
            trackNumber = "01";
        }

        // pad the 0 if the track number is only one digit
        trackNumber = trackNumber.length() == 1 ? "0" + trackNumber : trackNumber;

        // last ditch effort, make the user do it manually
        if (StringUtils.isEmpty(trackNumber) || StringUtils.isEmpty(trackTitle)) {
            return getManualFilename(file);
        }

        // return the new filename
        return trackNumber + " " + trackTitle;
    }

    /**
     * Attempts to get the track name/title from the file if there's no track number on the file
     */
    private String getManualFilename(File file) {

        // get the song data if there is any
        Song song = SongUtils.getSongFromFile(file);
        if (song == null) {
            return file.getName();
        }

        // get the title
        String title = song.getTitle();
        if (StringUtils.isEmpty(title)) {
            title = file.getName().replace(".mp3", StringUtils.EMPTY);
        }

        // get the track number
        // if there isn't a track in the id3 data, show a dialog where the user can manually input the number
        String track = song.getTrack();
        if (StringUtils.isEmpty(track)) {
            // show the dialog
            String[] arr = DialogService.showGetTitleOrTrackNumberDialog(Moose.getFrame(), title);
            if (arr != null) {
                title = arr[0];
                track = arr[1];
            } else {
                return file.getName();
            }
        }

        // if the track number is still empty (either from sheer user arrogance or some other reason)
        if (StringUtils.isEmpty(track)) {
            return file.getName();
        }

        // clean up the track
        track = track.length() == 1 ? "0" + track : track;

        // yay we have a track and title
        return track + " " + title;
    }
}
