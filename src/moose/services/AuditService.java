/*
  Proj:   Moose
  File:   AuditController.java
  Desc:   Controller class for AuditFrame, works directly with the data based on input from AuditFrame UI
  <p>
  Copyright Pat Ripley 2018-2020
 */

// package
package moose.services;

//imports
import moose.Moose;
import moose.objects.Song;
import moose.utilities.*;
import moose.views.AuditFrame;
import moose.views.Frame;
import moose.utilities.logger.Logger;

import javax.swing.SwingConstants;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// class AuditController
public class AuditService {

    // some ivars
    AuditFrame auditFrame;

    // logger object
    Logger logger = Moose.getLogger();

    public AuditService(AuditFrame auditFrame) {
        this.auditFrame = auditFrame;
    }

    /**
     * Analysis function for the audit
     * @param albums, the list of album folders to go through
     * @param auditFilePathList, the list of lists that we'll fill with the analysis results
     * @return a formatted string with the results of the analysis
     */
    public String analyzeForAudit(List<File> albums, List<List<String>> auditFilePathList) {
        Moose.getAuditFrame().setAuditCurrentlyScanningLabelHorizontalAlignment(SwingConstants.TRAILING);
        int total = albums.size();
        double index = 0;

        for (File dir : albums) {
            Moose.getAuditFrame().updateAuditCurrentlyScanningLabel(formatStringForCurrentlyScanningPath(dir.getPath()));
            if (!checkID3Tags(dir)) {
                auditFilePathList.get(Constants.ID3).add(dir.getPath());
            }
            if (!checkFilenames(dir)) {
                auditFilePathList.get(Constants.FILENAMES).add(dir.getPath());
            }
            if (!checkFolderCover(dir)) {
                auditFilePathList.get(Constants.COVER).add(dir.getPath());
            }

            Moose.getAuditFrame().updateAuditProgressBar(AuditCleanupUtils.formatPercentage(index, total));
            index++;
        }
        Moose.getAuditFrame().setAuditCurrentlyScanningLabelHorizontalAlignment(SwingConstants.LEADING);
        Moose.getAuditFrame().updateAuditCurrentlyScanningLabel(albums.size() + " albums successfully scanned!");
        return "ID3Tags missing:   " + auditFilePathList.get(0).size() + "\n"
                + "Filename issues:   " + auditFilePathList.get(1).size() + "\n"
                + "Cover art missing: " + auditFilePathList.get(2).size();
    }

    /**
     * Creates a master list of files from the audit folder, then imports all
     * the albums into an albums list
     * @param folder, the folder t
     */
    public List<File> importAlbums(File folder) {

        // main albums list
        List<File> albums = new ArrayList<>();

        // arraylist to store all the files
        ArrayList<File> allFiles = new ArrayList<>();
        FileUtils.listFiles(folder, allFiles);

        // traverse the list of files and add the albums to the albums list
        for (File file : allFiles) {
            if (file.getName().endsWith(".mp3")) {
                File a = file.getParentFile();

                // folder is a multi-disk album
                if (a.getName().equals("CD1") || a.getName().equals("CD2")) {
                    a = a.getParentFile();
                }
                if (!albums.contains(a)) {
                    albums.add(a);
                }
            }
        }

        // sort them alphabetically
        albums.sort((File o1, File o2) -> {
            String filename1 = o1.getPath().replace(folder.getPath(), "");
            String filename2 = o2.getPath().replace(folder.getPath(), "");
            return filename1.compareToIgnoreCase(filename2);
        });

        // display the result of importing albums
        auditFrame.updateAuditCurrentlyScanningLabel(albums + " albums to scan!");

        return albums;
    }

    /**
     * Gets all of the albums from the filePathList
     */
    public List<File> getAllMarkedAlbums(List<List<String>> filePathList) {
        List<File> albums = new ArrayList<>();
        for (List<String> list : filePathList) {
            for (String path : list) {
                albums.add(new File(path));
            }
        }
        return albums;
    }

    /**
     * Does the audit
     */
    public void openAuditWindow(List<File> albums, int index) {

        // update graphics
        updateLabelAndProgressBarForAudit(index, albums.size());

        // open up a new Frame with the album preloaded
        Moose.frame.dispose();
        Moose.frame = new Frame(albums.get(index));
        Moose.launchFrame(albums.get(index));
        auditFrame = Moose.getAuditFrame();
        auditFrame.refreshAuditFrame(getCheckResults(albums.get(index)), albums.get(index).getPath());
    }

    /**
     * Returns the check results on each aspect of the album (ID3 tags, filenames, and album cover)
     */
    private List<Boolean> getCheckResults(File album) {
        List<Boolean> results = new ArrayList<>();
        results.add(checkID3Tags(album));
        results.add(checkFilenames(album));
        results.add(checkFolderCover(album));
        return results;
    }

    /**
     * Helper function to update some graphics for auditing
     */
    private void updateLabelAndProgressBarForAudit(int index, int total) {
        // update the currentlyScanningLabel to show where we are numerically
        auditFrame.setAuditCurrentlyScanningLabelHorizontalAlignment(SwingConstants.LEADING);
        auditFrame.updateAuditCurrentlyScanningLabel("Album " + (index + 1) + " of " + total);

        // update the progress bar
        auditFrame.updateAuditProgressBar(AuditCleanupUtils.formatPercentage(index, total));
    }

    /**
     * Function used to create a .done file in the specified folder
     * @param dir, the directory to create the file in
     */
    public void setDone(File dir) {
        String path = dir.getPath() + "/.done";
        File done = new File(path);
        try {
            if (!done.createNewFile()) {
                throw new IOException("Couldn't create .done file in " + dir.getPath());
            }
        } catch (IOException ex) {
            logger.logError("Error creating .done file for auditing in folder: " + path, ex);
        }
    }

    /**
     * Checks if that specified directory/album is done
     * @param dir, the directory to check
     * @return the result of the check, true for done, false for not done
     */
    public boolean isNotDone(File dir) {
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.getName().equals(".done")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the next album index in the albums arraylist
     * @param albums, the file list of albums
     * @return the int index of that album
     */
    public int getNextAlbum(List<File> albums) {
        for (File album : albums) {
            if (isNotDone(album)) {
                return albums.indexOf(album);
            }
        }
        return -1;
    }

    /**
     * Function used to clear all of the .done files from the albums list
     * @param albums, the file list of all albums
     */
    public void clearDoneFiles(List<File> albums) {
        albums.stream().filter(this::containsFile).map(File::listFiles).filter(Objects::nonNull).forEachOrdered((files) -> {
            for (File file : files) {
                if (file.getName().equals(".done")) {
                    if (!file.delete()) {
                        logger.logError("Could not delete .done file in " + file.getParentFile().getPath());
                    }
                }
            }
        });
    }

    /**
     * Checks to see if in the audit folder there exists a done file in any of its subfolders
     * @return the result of the check, true for existing audit, false for no existing audit
     */
    public boolean checkForExistingAudit(List<File> albums) {
        return albums.stream().anyMatch(this::containsFile);
    }

    /**
     * Helper function used to see if there's a .done file in the specified folder
     * @param dir, the directory to check
     * @return the result of the search, true for file found, false for file not found
     */
    public boolean containsFile(File dir) {
        File[] filesInAlbum = dir.listFiles();
        assert filesInAlbum != null;
        for (File file : filesInAlbum) {
            if (file.getName().equals(".done")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Function used to export the results of audit to a heavily formatted string
     * @param auditFilePathList, the list of lists for all the audit files
     * @return a string representation of the results
     */
    public String exportAuditResultsToString(List<List<String>> auditFilePathList) {

        // lets make a string
        String str = "\n";

        str = str.concat(" Some ID3Tags missing:\n");
        for (String path : auditFilePathList.get(Constants.ID3)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        str = str.concat(" Some filenames don't match standard:\n");
        for (String path : auditFilePathList.get(Constants.FILENAMES)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        str = str.concat(" Cover art not found in folder:\n");
        for (String path : auditFilePathList.get(Constants.COVER)) {
            str = str.concat(" \t" + path + " \n");
        }
        str = str.concat("\n");
        return str;
    }

    /**
     * Checks the album/folder for a cover.* file (image file)
     * @return the result of the check, true for a cover file exists, false for a cover file doesn't exist
     */
    public boolean checkFolderCover(File dir) {

        // check the current directory ivar
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.getName().startsWith("cover.")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks all the files in a folder for the standardized file name
     * @return the result of the check, true if all files are good, false if one or more doesn't match
     */
    public boolean checkFilenames(File dir) {

        // create a list of all files from that directory
        ArrayList<File> files = new ArrayList<>();
        FileUtils.listFiles(dir, files);

        // check all files in that list
        // also checks if the album artist is a label, since those files aren't formatted the same
        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                if (!file.getName().matches(Constants.TRACK_FILENAME_REGEX) && !SongUtils.isPartOfALabel(file)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks all the mp3s in a folder for all the necessary id3 tags
     * @return the result of the check, true if all mp3s are good, false if one or more doesn't have all information needed
     */
    public boolean checkID3Tags(File dir) {

        // create a list of all files from that directory
        ArrayList<File> files = new ArrayList<>();
        FileUtils.listFiles(dir, files);

        // traverse the file list
        for (File file : files) {

            // check if it's an mp3
            if (file.getName().endsWith(".mp3")) {

                Song song = SongUtils.getSongFromFile(file);
                if (song == null) {
                    return false;
                }

                // check if it's a label, since those mp3s don't need as much information
                if (SongUtils.isPartOfALabel(dir)) {
                    if (StringUtils.isEmpty(song.getTitle())
                            || StringUtils.isEmpty(song.getArtist())
                            || StringUtils.isEmpty(song.getAlbum())
                            || StringUtils.isEmpty(song.getAlbumArtist())
                            || StringUtils.isEmpty(song.getGenre())
                            || song.getArtwork_bytes() == null) {
                        return false;
                    }
                } else {
                    if (StringUtils.isEmpty(song.getTitle())
                            || StringUtils.isEmpty(song.getArtist())
                            || StringUtils.isEmpty(song.getAlbum())
                            || StringUtils.isEmpty(song.getAlbumArtist())
                            || StringUtils.isEmpty(song.getGenre())
                            || StringUtils.isEmpty(song.getYear())
                            || StringUtils.isEmpty(song.getTrack())
                            || StringUtils.isEmpty(song.getDisk())
                            || song.getArtwork_bytes() == null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Helper function to format as string for the currentlyScanningLabel while scanning
     */
    public String formatStringForCurrentlyScanningPath(String path) {
        if (path.length() < 57) {
            return path;
        } else {
            return "..." + path.substring(path.length() - 52);
        }
    }
}
