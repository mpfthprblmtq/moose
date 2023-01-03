/*
 *  Proj:   Moose
 *  File:   FilenameFormatterService.java
 *  Desc:   Service class for filename formatting. Was once part of the AutoTaggingService, but that became a monolithic
 *          garbage can, so split it into its own service.
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.services;

// imports
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.controllers.SongController;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.utilities.MP3FileUtils;
import com.mpfthprblmtq.moose.utilities.SongUtils;
import com.mpfthprblmtq.moose.utilities.viewUtils.DialogUtils;

import static com.mpfthprblmtq.moose.utilities.Constants.*;

// class FilenameFormatterService
public class FilenameFormatterService {

    // auto tagging service
    AutoTaggingService autoTaggingService;

    public FilenameFormatterService(SongController songController) {
        this.autoTaggingService = songController.getAutoTaggingService();
    }

    /**
     * Formats filenames to a more suitable standard if we find a match
     *
     * @param file,       a file with a name to format
     * @param singleFile, a boolean to tell us if the file is a single, so there's only one
     * @return a new and improved file name
     */
    public String formatFilename(File file, boolean singleFile) {
        return getBetterFilename(file, singleFile);
    }

    /**
     * Cleans up the file name before doing any intense operations,
     * which includes replacing "feat." with "ft." and removing common strings
     *
     * @param filename, the filename to clean up
     * @return the cleaned up filename
     */
    private String cleanupFilename(String filename) {
        filename = filename.replaceAll("(?i)feat.", "ft.");
        filename = filename.replaceAll("Ft.", "ft.");

        // I totally get my music from legit sources, don't judge me
        for (String toReplace : FILENAME_STRINGS_TO_REMOVE) {
            filename = filename.replaceAll("\\((?i)" + toReplace + "\\)", StringUtils.EMPTY);
            filename = filename.replaceAll("\\[(?i)" + toReplace + "\\]", StringUtils.EMPTY);
        }

        // remove all the spaces, even if there's some at the end of the file before the .mp3
        filename = filename.replace(".mp3", StringUtils.EMPTY).trim();
        return filename + ".mp3";
    }

    /**
     * Checks for a match of known filename regexes, then applies fixes to the filename
     *
     * @param file       the file with the name to change
     * @param singleFile a boolean to tell us if the file is single, so there's only one
     * @return a good filename, or null if the fix couldn't be applied
     */
    public String getBetterFilename(File file, boolean singleFile) {
        String filename;

        // apply regex fixes
        String[] result = applyFixes(file, singleFile);

        String trackNumber = result[0];
        String trackTitle = result[1];

        // last ditch effort, make the user do it manually
        if (StringUtils.isEmpty(trackNumber) || StringUtils.isEmpty(trackTitle)) {
            filename = trackNumber + " " + trackTitle;

            // if we have a new file name, change the name of the actual file
            if (!filename.equals(file.getName())) {
                file = MP3FileUtils.getNewMP3FileFromOld(file, filename);
            }

            // get the manual title and number
            filename = getManualFilename(file);

            // if we have a new file name, change the name of the actual file
            if (!filename.equals(file.getName())) {
                file = MP3FileUtils.getNewMP3FileFromOld(file, filename);
            }

            // apply regex fixes again since we might actually have it now
            result = applyFixes(file, singleFile);
            trackNumber = result[0];
            trackTitle = result[1];
        }

        // return the new filename
        String newFileName = StringUtils.isNotEmpty(trackNumber) ? trackNumber + StringUtils.SPACE + trackTitle : trackTitle;
        return cleanupFilename(newFileName);
    }

    /**
     * Applies regex fixes to the file
     *
     * @param file,       the file we're editing
     * @param singleFile, a boolean to determine if the file is a single file
     * @return a two element string array with the track number and track title
     */
    private String[] applyFixes(File file, boolean singleFile) {

        String filename = file.getName();
        String trackNumber = StringUtils.EMPTY;
        String trackTitle = StringUtils.EMPTY;

        // try and replace the artist with already known information before we use the regex
        if (!MP3FileUtils.isPartOfALabel(file, COMPILATIONS)) {
            String artist = autoTaggingService.getArtistFromExistingID3Info(file);
            filename = filename.replace(artist, StringUtils.EMPTY);
        }

        // perform regex search on filename
        // first check if it matches the ## Title.mp3 format
        if (filename.matches(FILENAME_PRECHECK_REGEX)) {
            Pattern pattern = Pattern.compile(FILENAME_REGEX);
            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                trackNumber = matcher.group("TrackNumber");
                trackTitle = matcher.group("Title");
            }
        } else if (filename.matches(YOUTUBE_FILENAME_REGEX)) {
            Pattern pattern = Pattern.compile(YOUTUBE_FILENAME_REGEX);
            Matcher matcher = pattern.matcher(filename);
            if (file.getPath().contains(SINGLES)) {
                trackNumber = "01";
            }
            if (matcher.find()) {
                trackTitle = matcher.group("FileName");
            }
        } else {
            // didn't match precheck regex
            // let's try and get the title by trimming nonsense out
            Pattern pattern = Pattern.compile(FILENAME_TRIM_REGEX);
            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                trackTitle = matcher.group("Title");
            }
            // get rid of any spaces around it or between the title and .mp3
            trackTitle = trackTitle.replace(".mp3", "").trim() + ".mp3";
        }

        // check to see if it's a single file
        if (StringUtils.isEmpty(trackNumber) && singleFile) {
            trackNumber = "01";
        }

        // pad the 0 if the track number is only one digit
        trackNumber = trackNumber.length() == 1 ? "0" + trackNumber : trackNumber;

        return new String[]{trackNumber, trackTitle};
    }

    /**
     * Attempts to get the track name/title from the file if there's no track number on the file
     *
     * @param file,         the file we're editing
//     * @param originalFile, the original file that we were editing, used to get the original song info
     * @return the manual filename
     */
    private String getManualFilename(File file) {

        String trackTitle = StringUtils.EMPTY;
        String trackNumber = StringUtils.EMPTY;
        String trackArtist = StringUtils.EMPTY;

        // get the song data if there is any
        Song song = SongUtils.getSongFromFile(file);

        if (song != null) {
            // get the title
            trackTitle = song.getTitle();
            if (StringUtils.isEmpty(trackTitle)) {
                trackTitle = file.getName().replace(".mp3", StringUtils.EMPTY);
            }

            // get the track number
            // if there isn't a track in the id3 data, show a dialog where the user can manually input the number
            trackNumber = song.getTrack();

            // get the artist if we're in a compilation
            if (MP3FileUtils.isPartOfALabel(file, COMPILATIONS)) {
                trackArtist = song.getArtist();
                if (StringUtils.isEmpty(trackArtist)) {
                    if (file.getName().matches(TRACKNUM_ARTIST_TITLE_OPT_REGEX)) {
                        Pattern pattern = Pattern.compile(TRACKNUM_ARTIST_TITLE_OPT_REGEX);
                        Matcher matcher = pattern.matcher(file.getName());
                        if (matcher.find()) {
                            trackArtist = matcher.group("Artist");
                        }
                    }
                }
            }
        }

        // set the track title
        trackTitle = StringUtils.isEmpty(trackTitle) ? file.getName() : trackTitle;

        if (StringUtils.isEmpty(trackNumber)) {
            // show the dialog if we haven't already
            String[] arr = DialogUtils.showGetTitleOrTrackNumberDialog(Moose.getFrame(), trackTitle.trim());
            if (arr != null) {
                trackTitle = arr[0];
                trackNumber = arr[1];
            } else {
                return file.getName();
            }
        }

        String fileName;
        if (StringUtils.isNotEmpty(trackArtist)) {
            fileName = trackArtist.trim() + " - " + trackTitle.trim();
        } else {
            fileName = trackTitle.trim();
        }

        // if the track number is still empty (either from sheer user arrogance or some other reason)
        if (StringUtils.isEmpty(trackNumber)) {
            return fileName;
        }

        // yay we have a track and title (and maybe an artist)
        if (StringUtils.isNotEmpty(trackArtist)) {
            return trackNumber + StringUtils.SPACE + fileName;
        } else {
            return trackNumber + " " + fileName;
        }
    }
}
