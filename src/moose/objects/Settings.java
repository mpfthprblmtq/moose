/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moose.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import moose.utilities.Utils;

/**
 *
 * @author pat
 */
public class Settings {

    private boolean inDebugMode;
    private List<String> genres;
    private String libraryLocation;
    private String albumArtFinderApiKey;
    private String albumArtFinderCseId;
    private int albumArtFinderSearchCount;
    private String albumArtFinderSearchCountDate;
    
    /**
     * Creates a default settings object
     */
    public Settings() {
        setDebugMode(false);
        setGenres(new ArrayList<>());
        setLibraryLocation("");
        setAlbumArtFinderApiKey("");
        setAlbumArtFinderCseId("");
        setAlbumArtFinderSearchCount(0);
        setAlbumArtFinderSearchCountDate(Utils.formatDate(new Date()));
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
        if(!getGenres().contains(genre)) {
            getGenres().remove(genre);
        }
    }
    
    /**
     * @return the debug
     */
    public boolean isInDebugMode() {
        return inDebugMode;
    }

    /**
     * @param debugMode the debug to set
     */
    public void setDebugMode(boolean debugMode) {
        this.inDebugMode = debugMode;
    }

    /**
     * @return the genres
     */
    public List<String> getGenres() {
        return genres;
    }

    /**
     * @param genres the genres to set
     */
    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    /**
     * @return the libaryLocation
     */
    public String getLibraryLocation() {
        return libraryLocation;
    }

    /**
     * @param libraryLocation the libraryLocation to set
     */
    public void setLibraryLocation(String libraryLocation) {
        this.libraryLocation = libraryLocation;
    }

    /**
     * @return the albumArtFinderApiKey
     */
    public String getAlbumArtFinderApiKey() {
        return albumArtFinderApiKey;
    }

    /**
     * @param albumArtFinderApiKey the albumArtFinderApiKey to set
     */
    public void setAlbumArtFinderApiKey(String albumArtFinderApiKey) {
        this.albumArtFinderApiKey = albumArtFinderApiKey;
    }

    /**
     * @return the albumArtFinderCseId
     */
    public String getAlbumArtFinderCseId() {
        return albumArtFinderCseId;
    }

    /**
     * @param albumArtFinderCseId the albumArtFinderCseId to set
     */
    public void setAlbumArtFinderCseId(String albumArtFinderCseId) {
        this.albumArtFinderCseId = albumArtFinderCseId;
    }

    /**
     * @return the albumArtFinderSearchCount
     */
    public int getAlbumArtFinderSearchCount() {
        return albumArtFinderSearchCount;
    }

    /**
     * @param albumArtFinderSearchCount the albumArtFinderSearchCount to set
     */
    public void setAlbumArtFinderSearchCount(int albumArtFinderSearchCount) {
        this.albumArtFinderSearchCount = albumArtFinderSearchCount;
    }

    /**
     * @return the albumArtFinderSearchCountDate
     */
    public String getAlbumArtFinderSearchCountDate() {
        return albumArtFinderSearchCountDate;
    }

    /**
     * @param albumArtFinderSearchCountDate the albumArtFinderSearchCountDate to set
     */
    public void setAlbumArtFinderSearchCountDate(String albumArtFinderSearchCountDate) {
        this.albumArtFinderSearchCountDate = albumArtFinderSearchCountDate;
    }
}
