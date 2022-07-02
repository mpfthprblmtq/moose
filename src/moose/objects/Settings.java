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

    public static final String REMOVE_COMMENT_ON_AUTOTAGGING = "removeCommentOnAutoTagging";
    public static final String AUTO_FIND_COVER_ART_WITH_SPOTIFY = "autoFindCoverArtWithSpotify";

    
    /**
     * Creates a default settings object
     */
    public Settings() {
        this.inDebugMode = false;
        this.inDeveloperMode = false;
        this.askBeforeClearAll = true;
        this.genres = new ArrayList<>();
        this.libraryLocation = StringUtils.EMPTY;
        this.preferredCoverArtSize = 640;
        this.albumArtFinderApiKey = StringUtils.EMPTY;
        this.albumArtFinderCseId = StringUtils.EMPTY;
        this.albumArtFinderSearchCount = 0;
        this.albumArtFinderSearchCountDate = DateUtils.formatSimpleDate(new Date());
        this.spotifyClientId = StringUtils.EMPTY;
        this.spotifyClientSecret = StringUtils.EMPTY;
        this.features = new HashMap<>();
        this.features.put(REMOVE_COMMENT_ON_AUTOTAGGING, true);
        this.features.put(AUTO_FIND_COVER_ART_WITH_SPOTIFY, true);

        // set the support location since we always know where that'll be
        setApplicationSupportLocation(System.getProperty("user.home") + "/Library/Application Support/Moose/");
    }

    public Settings withVersionNumber(String version) {
        this.setVersion(version);
        return this;
    }
    
    /**
     * Adds a genre to the genre list
     * @param genre, the genre to add
     */
    public void addGenre(String genre) {
        if(!getGenres().contains(genre)) {
            getGenres().add(genre);
        }
    }

    /**
     * Removes a genre from the genre list
     * @param genre, the genre to remove
     */
    public void removeGenre(String genre) {
        getGenres().remove(genre);
    }
}
