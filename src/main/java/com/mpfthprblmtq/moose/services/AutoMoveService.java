package com.mpfthprblmtq.moose.services;

import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.utilities.Constants;
import com.mpfthprblmtq.moose.utilities.MP3FileUtils;
import com.mpfthprblmtq.moose.utilities.viewUtils.DialogUtils;
import com.mpfthprblmtq.moose.views.modals.AutoMoveFrame;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.mpfthprblmtq.moose.utilities.Constants.TABLE_COLUMN_GENRE;

public class AutoMoveService {

    // logger
    Logger logger = Moose.getLogger();

    public void autoMove(List<Song> songs, int[] selectedRows) {
        // make sure the user has their library location set
        if (StringUtils.isEmpty(Moose.getSettings().getLibraryLocation())) {
            DialogUtils.showMessageDialog(
                    Moose.getFrame(),
                    "Library Location must be set in order to use the Auto Move feature!  Set that in Settings!",
                    "Library Location not set",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // grab all the albums to make sure we're auto moving effectively
        List<String> albums = songs.stream()
                .map(Song::getAlbum)
                .collect(Collectors.toList());
        if (!StringUtils.checkIfSame(albums.get(0), albums)) {
            int result = DialogUtils.showConfirmationDialog(
                    Moose.getFrame(),
                    "There are different albums for these files, are you sure you want to Auto Move them to the same album?",
                    "Multiple Albums Found",
                    JOptionPane.WARNING_MESSAGE
            );
            // might have changed their name
            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION) {
                return;
            }
        }

        // lets do the auto move
        AutoMoveFrame autoMoveFrame = new AutoMoveFrame(songs, selectedRows);
        autoMoveFrame.setLocationRelativeTo(Moose.getFrame());
        autoMoveFrame.setVisible(true);
    }

    public String move(String path, List<Song> songs, boolean autoTag, String genre, int[] selectedRows) {

        // if the path ends with a / that means we're working with a directory
        if (path.endsWith("/") && songs.size() > 1) {
            File dir = new File(path);
            if (!dir.mkdirs()) {
                return "Couldn't create directory: " + path;
            }

            // actually move the files
            File oldFile = null;
            for (Song song : songs) {
                oldFile = song.getFile();
                File newFile = new File(path + oldFile.getName());
                if (!oldFile.renameTo(newFile)) {
                    return "Couldn't move file: " + oldFile.getPath();
                } else {
                    Moose.getSongController().getSongs().get(song.getIndex()).setFile(newFile);
                }
            }

            // delete parent directory
            assert oldFile != null;
            if (MP3FileUtils.folderContainsNoMP3Files(oldFile.getParentFile())
                    && !oldFile.getParentFile().getName().contains("Downloads")) {
                if (!FileUtils.deleteFolder(oldFile.getParentFile())) {
                    return "Couldn't delete parent directory: " + oldFile.getParentFile().getPath();
                }
            }

        // if the path doesn't end with / and there's only one song, we're working with a single file
        } else if (!path.endsWith("/") && songs.size() == 1) {
            // get the parent dir for the file
            String[] arr = path.split("/");
            String filename = arr[arr.length - 1];

            // create the directory
            File dir = new File(path.replace(filename, StringUtils.EMPTY));
            if (!dir.mkdirs()) {
                return "Couldn't create directory: " + path;
            }

            // actually move the file
            Song song = songs.get(0);
            File oldFile = song.getFile();
            File newFile = new File(path);
            if (!oldFile.renameTo(newFile)) {
                return "Couldn't move file: " + oldFile.getPath();
            } else {
                Moose.getSongController().getSongs().get(song.getIndex()).setFile(newFile);
            }
        }

        // set the genre of all the songs in the song controller and the table
        if (StringUtils.isNotEmpty(genre) && !genre.equals(Constants.DASH)) {
            for (Song song : songs) {
                int row = Moose.getSongController().getRow(song.getIndex());
                Moose.getSongController().setGenre(song.getIndex(), genre);
                Moose.getFrame().table.setValueAt(genre, row, TABLE_COLUMN_GENRE);
            }
        }

        // check if we need to auto tag
        if (autoTag) {
            Moose.getSongController().autoTagFiles(selectedRows);
            return "All files moved and AutoTagged!";
        }

        // return success message
        return "All files moved!";
    }
}
