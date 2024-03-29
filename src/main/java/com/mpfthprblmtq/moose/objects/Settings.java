/*
 *  Proj:   Moose
 *  File:   Settings.java
 *  Desc:   Pojo for the Settings information
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects;

// imports
import com.mpfthprblmtq.commons.utils.DateUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// class Settings
@Data
public class Settings {

    private String version;
    private boolean inDebugMode;
    private boolean inDeveloperMode;
    private boolean askBeforeClearAll;
    private boolean warnBeforeStartingAudit;
    private List<String> genres;
    private String libraryLocation;
    private String applicationSupportLocation;
    private int preferredCoverArtSize;
    private String albumArtFinderApiKey;
    private String albumArtFinderCseId;
    private int albumArtFinderSearchCount;
    private String albumArtFinderSearchCountDate;
    private String spotifyClientId;
    private String spotifyClientSecret;
    private Map<String, Boolean> features;
    private Map<String, String> spotifyArtists;

    public static final String AUTOTAGGING = "autotagging";
    public static final String REMOVE_COMMENT_ON_AUTOTAGGING = "removeCommentOnAutoTagging";
    public static final String ALBUM_ART_FINDER = "albumArtFinder";
    public static final String AUTO_FIND_COVER_ART_WITH_SPOTIFY = "autoFindCoverArtWithSpotify";
    public static final String CHECK_FOR_NEW_GENRES = "checkForNewGenres";
    public static final String FORMAT_FILENAMES = "formatFilenames";

    /**
     * Creates a default settings object
     */
    public Settings() {
        setInDebugMode(false);
        setInDeveloperMode(false);
        setAskBeforeClearAll(true);
        setWarnBeforeStartingAudit(true);
        setGenres(new ArrayList<>());
        setLibraryLocation(StringUtils.EMPTY);
        setPreferredCoverArtSize(640);
        setAlbumArtFinderApiKey(StringUtils.EMPTY);
        setAlbumArtFinderCseId(StringUtils.EMPTY);
        setAlbumArtFinderSearchCount(0);
        setAlbumArtFinderSearchCountDate(DateUtils.formatSimpleDate(new Date()));
        setSpotifyClientId(StringUtils.EMPTY);
        setSpotifyClientSecret(StringUtils.EMPTY);
        setFeatures(new HashMap<>());
        getFeatures().put(AUTOTAGGING, true);
        getFeatures().put(REMOVE_COMMENT_ON_AUTOTAGGING, true);
        getFeatures().put(ALBUM_ART_FINDER, true);
        getFeatures().put(AUTO_FIND_COVER_ART_WITH_SPOTIFY, true);
        getFeatures().put(CHECK_FOR_NEW_GENRES, true);
        getFeatures().put(FORMAT_FILENAMES, true);
        setSpotifyArtists(new HashMap<>());

        // set the support location since we always know where that'll be
        setApplicationSupportLocation(System.getProperty("user.home") + "/Library/Application Support/moose/");
    }

    /**
     * Builder pattern method to append a version to a settings object while getting it
     * @param version the version to set on the Settings object
     * @return the Settings object with the version
     */
    public Settings withVersionNumber(String version) {
        this.setVersion(version);
        return this;
    }
}
