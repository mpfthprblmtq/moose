/*
   Proj:   Moose
   File:   Settings.java
   Desc:   Pojo for the Settings information

   Copyright Pat Ripley 2018
 */

// package
package moose.objects;

// imports
import com.mpfthprblmtq.commons.utils.DateUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import lombok.Data;

import java.util.*;

// class Settings
@Data
public class Settings {

    private String version;
    private boolean inDebugMode;
    private boolean inDeveloperMode;
    private boolean askBeforeClearAll;
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

    public static final String REMOVE_COMMENT_ON_AUTOTAGGING = "removeCommentOnAutoTagging";
    public static final String AUTO_FIND_COVER_ART_WITH_SPOTIFY = "autoFindCoverArtWithSpotify";

    
    /**
     * Creates a default settings object
     */
    public Settings() {
        setInDebugMode(false);
        setInDeveloperMode(false);
        setAskBeforeClearAll(true);
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
        getFeatures().put(REMOVE_COMMENT_ON_AUTOTAGGING, true);
        getFeatures().put(AUTO_FIND_COVER_ART_WITH_SPOTIFY, true);
        setSpotifyArtists(new HashMap<>());

        // set the support location since we always know where that'll be
        setApplicationSupportLocation(System.getProperty("user.home") + "/Library/Application Support/moose/");
    }

    public Settings withVersionNumber(String version) {
        this.setVersion(version);
        return this;
    }
}
