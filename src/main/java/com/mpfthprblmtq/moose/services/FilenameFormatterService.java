package com.mpfthprblmtq.moose.services;

import com.mpfthprblmtq.commons.utils.RegexUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.controllers.SongController;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.utilities.MP3FileUtils;
import com.mpfthprblmtq.moose.utilities.viewUtils.DialogUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.mpfthprblmtq.moose.utilities.Constants.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilenameFormatterService {

    // controllers
    SongController songController;

    /**
     * Formats the filenames of all songs in the list given to us, sets the new file in the songs map in the
     * SongController, then sets the new value in the table
     * @param songs the list of songs to format filenames for
     */
    public void formatFilenames(List<Song> songs) {
        for (Song song : songs) {
            String newFilename = formatFilename(song);
            if (StringUtils.isNotEmpty(newFilename) && !newFilename.equals(song.getFile().getName())) {
                String path = song.getFile().getPath().replace(song.getFile().getName(), StringUtils.EMPTY);
                songController.setNewFile(song.getIndex(), new File(path + newFilename));
                Moose.getFrame().getTable().getModel().setValueAt(new File(path + newFilename), songController.getRow(song.getIndex()), 1);
                Moose.getFrame().getTable().setValueAt((newFilename)
                        .replace(".mp3", StringUtils.EMPTY)
                        .replace(":", "/"), songController.getRow(song.getIndex()), 1);
            }
        }
    }

    /**
     * Method used to do the heavy lifting of the actual filename change
     * @param song the file to change the name of
     * @return a better filename
     */
    protected String formatFilename(Song song) {
        String filename = song.getFile().getName();

        filename = cleanupFeaturingTag(filename);
        filename = cleanupCommonReplaceableStrings(filename);
        filename = cleanupTrackNumber(filename);
        filename = modifyFilename(song, filename);

        return filename;
    }

    /**
     * Helper function to clean up the featuring tag in filenames.  For the most part, it goes and replaces any feat.
     * (case-insensitive) with "ft." and then makes sure there's parentheses around it.  Also accounts for if there's
     * a remixed by artist as well.
     * @param filename the filename to clean up
     * @return the cleaned up filename
     */
    protected String cleanupFeaturingTag(@NonNull String filename) {
        // remove the file extension to make our lives easier
        filename = filename.substring(0, filename.length() - 4);

        // initial cleanup
        filename = filename.replaceAll("(?i)feat\\.", "ft.").replaceAll("Ft.", "ft.");

        // then specific regex cleanup (handles no parentheses, remixed by artists, etc.)
        if (filename.contains("ft.")) {
            if (filename.matches(FEATURED_NO_PARENS_WITH_REMIXED_BY_ARTIST)) {
                String title = RegexUtils.getMatchedGroup(
                        filename, FEATURED_NO_PARENS_WITH_REMIXED_BY_ARTIST, "title")
                        .trim();
                String featured = RegexUtils.getMatchedGroup(
                        filename, FEATURED_NO_PARENS_WITH_REMIXED_BY_ARTIST, "featured")
                        .trim();
                String remixedBy = RegexUtils.getMatchedGroup(
                        filename, FEATURED_NO_PARENS_WITH_REMIXED_BY_ARTIST, "remixedBy")
                        .trim();
                filename = title + " (ft. " + featured + ") " + remixedBy;
            } else if (filename.matches(FEATURED_NO_PARENS)) {
                String title = RegexUtils.getMatchedGroup(
                        filename, FEATURED_NO_PARENS, "title")
                        .trim();
                String featured = RegexUtils.getMatchedGroup(
                        filename, FEATURED_NO_PARENS, "featured")
                        .trim();
                filename = title + " (ft. " + featured + ")";
            }
        }
        return filename.trim() + ".mp3";
    }

    /**
     * Helper function to clean up any commonly found strings in filenames (usually from web/YouTube downloads).
     * Goes through the filename and removes any strings that are found in the commonly replaceable string list.
     * I totally get my music legitimately, don't judge me.
     * @param filename the filename to clean up
     * @return the cleaned up filename
     */
    protected String cleanupCommonReplaceableStrings(@NonNull String filename) {
        // remove the file extension to make our lives easier
        filename = filename.substring(0, filename.length() - 4);

        for (String toReplace : FILENAME_STRINGS_TO_REMOVE) {
            filename = filename.replaceAll("(?i)" + toReplace, StringUtils.EMPTY);
            filename = filename.replaceAll("(?i)" + toReplace, StringUtils.EMPTY);
        }
        filename = filename.replaceAll("\\(\\)", StringUtils.EMPTY);
        filename = filename.replaceAll("\\[]", StringUtils.EMPTY);

        return filename.trim() + ".mp3";
    }

    /**
     * Helper function to clean up the track number.  Basically just removes periods, hyphens, etc. between the track
     * number and the title.
     * @param filename the filename to clean up
     * @return the cleaned up filename
     */
    protected String cleanupTrackNumber(@NonNull String filename) {
        // remove the file extension to make our lives easier
        filename = filename.substring(0, filename.length() - 4);

        if (filename.matches(FILENAME_TRACK_NUMBER_PERIOD)) {
            Map<String, List<String>> matches = RegexUtils.getAllMatchesForGroups(
                    filename, FILENAME_TRACK_NUMBER_PERIOD, Arrays.asList("track", "title"));
            filename = matches.get("track").get(0) + StringUtils.SPACE + matches.get("title").get(0);
        } else if (filename.matches(FILENAME_TRACK_NUMBER_HYPHEN)) {
            Map<String, List<String>> matches = RegexUtils.getAllMatchesForGroups(
                    filename, FILENAME_TRACK_NUMBER_HYPHEN, Arrays.asList("track", "title"));
            filename = matches.get("track").get(0) + StringUtils.SPACE + matches.get("title").get(0);
        }

        // pad the track number if we have to
        if (filename.matches(FILENAME_TRACK_NUMBER_TITLE)) {
            String title = RegexUtils.getMatchedGroup(filename, FILENAME_TRACK_NUMBER_TITLE, "title");
            String track = RegexUtils.getMatchedGroup(filename, FILENAME_TRACK_NUMBER_TITLE, "track");
            track = track.length() == 1 ? "0".concat(track) : track;
            return track + StringUtils.SPACE + title + ".mp3";
        }

        // couldn't do any more modifications
        return filename + ".mp3";
    }

    /**
     * Function that does the majority of the heavy lifting, sets the filename based on the exiting ID3 data, folder
     * structure, and a few other factors.
     * @param song the song with the original file
     * @param filename the newly cleaned up filename
     * @return a modified filename with the new information
     */
    protected String modifyFilename(Song song, String filename) {
        String track;
        String title;

        // remove the file extension to make our lives easier
        filename = filename.substring(0, filename.length() - 4);

        // try and find the track
        if (filename.matches(FILENAME_TRACK_NUMBER_TITLE)) {
            track = RegexUtils.getMatchedGroup(filename, FILENAME_TRACK_NUMBER_TITLE, "track");
        } else {
            // see if it's a single file, which means it can be "01"
            track = MP3FileUtils.folderContainsOnlyOneMP3(song.getFile().getParentFile()) ? "01" : StringUtils.EMPTY;

            // if it's still empty, try and get it from existing ID3 information
            if (StringUtils.isEmpty(track)) {
                track = song.getTrack();
            }
        }

        // try to find the title
        if (!MP3FileUtils.isPartOfALabel(song.getFile(), COMPILATIONS)) {
            // we're not in a compilation, check if we have the track number, artist, and title
            if (filename.matches(FILENAME_TRACK_NUMBER_ARTIST_TITLE)) {
                // we have all three, which means we need to cut it down to just track number and title,
                // but before we do, let's set the artist on the song if it's not already set
                String filenameArtist = RegexUtils.getMatchedGroup(filename, FILENAME_TRACK_NUMBER_ARTIST_TITLE, "artist");
                if (!filenameArtist.equals(song.getArtist())) {
                    songController.setArtist(song.getIndex(), filenameArtist);
                }

                // build the file name with just the track and title
                track = RegexUtils.getMatchedGroup(filename, FILENAME_TRACK_NUMBER_ARTIST_TITLE, "track");
                title = RegexUtils.getMatchedGroup(filename, FILENAME_TRACK_NUMBER_ARTIST_TITLE, "title");
                filename = track + StringUtils.SPACE + title;

            // check to see if we have the artist and title, so we can just grab the title
            } else if (filename.matches(FILENAME_ARTIST_TITLE)) {
                // we have only the artist and title, which means we need to cut it down to just the title,
                // but before we do, let's set the artist on the song if it's not already set
                String filenameArtist = RegexUtils.getMatchedGroup(filename, FILENAME_ARTIST_TITLE, "artist");
                if (!filenameArtist.equals(song.getArtist())) {
                    songController.setArtist(song.getIndex(), filenameArtist);
                }

                // build the file name with just the track and title
                // if we don't already have the track, just set the filename to the title
                title = RegexUtils.getMatchedGroup(filename, FILENAME_ARTIST_TITLE, "title");
                filename = StringUtils.isNotEmpty(track) ? track + StringUtils.SPACE + title : title;

            // check to see if we have the track number and title, so we can just grab the title normally
            // else we should just set the title to the filename
            } else if (filename.matches(FILENAME_TRACK_NUMBER_TITLE)) {
                title = RegexUtils.getMatchedGroup(filename, FILENAME_TRACK_NUMBER_TITLE, "title");
            } else {
                title = filename;
            }
        } else {
            // we're in a compilation, which means we need to have the track number, artist, and title
            if (filename.matches(FILENAME_TRACK_NUMBER_ARTIST_TITLE)) {
                // we have all three, which means we can get all that we need,
                // but before we do, let's set the artist on the song if it's not already set
                String filenameArtist = RegexUtils.getMatchedGroup(filename, FILENAME_TRACK_NUMBER_ARTIST_TITLE, "artist");
                if (!filenameArtist.equals(song.getArtist())) {
                    songController.setArtist(song.getIndex(), filenameArtist);
                }

                // we can just get the title like normal
                title = RegexUtils.getMatchedGroup(filename, FILENAME_TRACK_NUMBER_TITLE, "title");
            } else {
                // we only have the track number and title, so let's try and get the artist to add it to the filename
                if (filename.matches(FILENAME_TRACK_NUMBER_TITLE)) {
                    title = RegexUtils.getMatchedGroup(filename, FILENAME_TRACK_NUMBER_TITLE, "title");
                    title = StringUtils.isNotEmpty(song.getArtist()) ? song.getArtist() + " - " + title : filename;
                } else {
                    title = filename;
                }
            }
        }

        // if either track or title are still empty, get them from the user using the dialog
        if (StringUtils.isEmpty(track) || StringUtils.isEmpty(title)) {
            if (MP3FileUtils.isPartOfALabel(song.getFile(), COMPILATIONS)) {

                // we're in a compilation, so let's show the dialog with track, artist, and title
                String[] arr = DialogUtils.showGetTitleAndTrackNumberAndArtistDialog(
                        Moose.getFrame(), filename, StringUtils.EMPTY);
                if (arr != null) {
                    track = arr[0];
                    String artist = arr[1];
                    title = arr[2];
                    title = artist + " - " + title;
                }
            } else {

                // we're not in a compilation, just show the track and title modal
                String[] arr = DialogUtils.showGetTitleAndTrackNumberDialog(Moose.getFrame(), filename);
                if (arr != null) {
                    track = arr[0];
                    title = arr[1];
                }
            }
        }

        // no track, just return the title
        if (StringUtils.isEmpty(track)) {
            return title + ".mp3";
        }

        // pad the track if we haven't already
        track = track.length() == 1 ? "0".concat(track) : track;

        return (track + StringUtils.SPACE + title + ".mp3").trim();
    }
}
