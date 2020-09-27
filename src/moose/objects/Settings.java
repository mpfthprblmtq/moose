/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moose.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import moose.utilities.Constants;
import moose.utilities.Utils;

public class Settings {

    private String version;
    private boolean inDebugMode;
    private boolean inDeveloperMode;
    private List<String> genres;
    private String libraryLocation;
    private int preferredCoverArtSize;
    private String albumArtFinderApiKey;
    private String albumArtFinderCseId;
    private int albumArtFinderSearchCount;
    private String albumArtFinderSearchCountDate;
    
    /**
     * Creates a default settings object
     */
    public Settings() {
        this.inDebugMode = false;
        this.inDeveloperMode = false;
        this.genres = new ArrayList<>();
        this.libraryLocation = Constants.EMPTY_STRING;
        this.preferredCoverArtSize = 640;
        this.albumArtFinderApiKey = Constants.EMPTY_STRING;
        this.albumArtFinderCseId = Constants.EMPTY_STRING;
        this.albumArtFinderSearchCount = 0;
        this.albumArtFinderSearchCountDate = Utils.formatDate(new Date());
    }
    
    /**
     * Adds a genre to the genre list
     * @param genre
     */
    public void addGenre(String genre) {
        if(!getGenres().contains(genre)) {
            getGenres().add(genre);
        }
    }
    
    /**
     * Removes a genre from the genre list
     * @param genre
     */
    public void removeGenre(String genre) {
        getGenres().remove(genre);
    }

    public boolean isInDebugMode() {
        return inDebugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.inDebugMode = debugMode;
    }

    public boolean isInDeveloperMode() {
        return inDeveloperMode;
    }

    public void setDeveloperMode(boolean developermode) {
        this.inDeveloperMode = developermode;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public int getPreferredCoverArtSize() {
        return preferredCoverArtSize;
    }

    public void setPreferredCoverArtSize(int preferredCoverArtSize) {
        this.preferredCoverArtSize = preferredCoverArtSize;
    }

    public String getLibraryLocation() {
        return libraryLocation;
    }

    public void setLibraryLocation(String libraryLocation) {
        this.libraryLocation = libraryLocation;
    }

    public String getAlbumArtFinderApiKey() {
        return albumArtFinderApiKey;
    }

    public void setAlbumArtFinderApiKey(String albumArtFinderApiKey) {
        this.albumArtFinderApiKey = albumArtFinderApiKey;
    }

    public String getAlbumArtFinderCseId() {
        return albumArtFinderCseId;
    }

    public void setAlbumArtFinderCseId(String albumArtFinderCseId) {
        this.albumArtFinderCseId = albumArtFinderCseId;
    }

    public int getAlbumArtFinderSearchCount() {
        return albumArtFinderSearchCount;
    }

    public void setAlbumArtFinderSearchCount(int albumArtFinderSearchCount) {
        this.albumArtFinderSearchCount = albumArtFinderSearchCount;
    }

    public String getAlbumArtFinderSearchCountDate() {
        return albumArtFinderSearchCountDate;
    }

    public void setAlbumArtFinderSearchCountDate(String albumArtFinderSearchCountDate) {
        this.albumArtFinderSearchCountDate = albumArtFinderSearchCountDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
