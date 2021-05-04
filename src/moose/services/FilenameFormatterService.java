/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moose.services;

import java.awt.*;
import java.io.File;
import moose.Moose;
import moose.objects.Song;
import moose.utilities.Constants;
import moose.utilities.SongUtils;
import moose.utilities.StringUtils;


/**
 *
 * @author pat
 */
public class FilenameFormatterService {

    // frame
    Frame frame;

    public FilenameFormatterService() {
        this.frame = Moose.getFrame();
    }

    /**
     * Formats filenames to a more suitable standard if we find a match
     * @param file a file with a name to format
     * @return a new file with a better name
     */
    public String formatFilename(File file) {
        // clean it up first
        String filename = cleanupFilename(file.getName());

        // check for regex matches
        String regexMatched = checkForMatch(filename);
        if (StringUtils.isNotEmpty(regexMatched)) {
            String newFilename = getGoodFilename(file, regexMatched);
            if (!newFilename.equals(filename)) {
                return newFilename.replace("/", ":");
            } else {
                return filename;
            }
        }
        return filename;
    }

    /**
     * Cleans up the file name before doing any intense operations, which for now is just replacing feat with ft
     */
    private String cleanupFilename(String filename) {
        String newFilename = filename
                .replace("feat.", "ft.");
        if (newFilename.contains("feat")) {
            newFilename = newFilename.replace("feat", "ft.");
        }
        if (newFilename.contains("ft") && !newFilename.contains("ft.")) {
            newFilename = newFilename.replace("ft", "ft.");
        }
        return newFilename;
    }

    /**
     * Checks for a match in any of the file regexes declared as "dumb" and/or "bad"
     * @param filename the filename to check
     * @return the regex the filename matched with, null if it didn't match
     */
    private String checkForMatch(String filename) {
        for (String regex : Constants.REGEX_ARRAY) {
            if (filename.matches(regex)) {
                return regex;
            }
        }
        return null;
    }

    /**
     * Checks for a match of known filename regexes, then applies fixes to the filename
     * @param file the file with the name to change
     * @param regex the regex that was matched
     * @return a good filename, or null if the fix couldn't be applied
     */
    private String getGoodFilename(File file, String regex) {

        String filename = file.getName();
        String newFilename = StringUtils.EMPTY;

        // apply regex fix
        if (regex.equals(Constants.REGEX_ARRAY.get(2))) {
            newFilename = applyRegex(filename, ". ");
        } else if (
                regex.equals(Constants.REGEX_ARRAY.get(0)) ||
                        regex.equals(Constants.REGEX_ARRAY.get(1)) ||
                regex.equals(Constants.REGEX_ARRAY.get(3)) ||
                regex.equals(Constants.REGEX_ARRAY.get(4))
        ) {
            newFilename = applyRegex(filename, " - ");
        } else if (regex.equals(Constants.REGEX_ARRAY.get(5))) {
            newFilename = getFilenameWithNoTrack(file);
        }

        // if it's empty for some reason, the regex fix didn't work, so let's just use the old filename
        if (StringUtils.isEmpty(newFilename)) {
            newFilename = filename;
        }

        // return the new parsed filename
        return newFilename;
    }

    /**
     * Actually does the fix
     * @param filename the filename to fix
     * @param initialSplit the split string used to split the track and title
     * @return a good filename, or null if the fix couldn't be applied
     */
    private String applyRegex(String filename, String initialSplit) {
        // split to get a track and title
        String[] arr = filename.split(initialSplit);
        String track = arr[0];
        String title = arr[1];
        if (arr.length > 2) {
            title = arr[arr.length - 1];
        }

        // make sure track is the two digit
        if (!track.matches(Constants.TRACK_NUMBER_REGEX)) {
            track = track.substring(0,2);
        }

        // verify the filename and return it if it's good
        String newFilename = track + " " + title;
        if (newFilename.matches(Constants.TRACK_FILENAME_REGEX)) {
            return newFilename;
        }

        // filename wasn't good, return null
        return null;
    }

    /**
     * Attempts to get the track name/title from the file if there's no track number on the file
     */
    private String getFilenameWithNoTrack(File file) {

        // get the song data if there is any
        Song song = SongUtils.getSongFromFile(file);
        if (song == null) {
            return file.getName();
        }

        // get the title
        String title = song.getTitle();
        if (StringUtils.isEmpty(title)) {
            title = file.getName()
                    .replace(":", "/")
                    .replace(".mp3", StringUtils.EMPTY);
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
        if (track.length() == 1) {
            track = "0".concat(track);
        }

        // yay we have a track and title
        return track + " " + title;
    }
}
