/*
 *  Proj:   Moose
 *  File:   ImageSearchQuery.java
 *  Desc:   Pojo for a bundle of search information to provide to the Google CSE
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects.api.imageSearch;

// imports
import com.mpfthprblmtq.commons.utils.RegexUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;

import static com.mpfthprblmtq.moose.utilities.Constants.TITLE_FEATURING_AND_REMIX_ARTIST;
import static com.mpfthprblmtq.moose.utilities.Constants.TITLE_FEATURING_ARTIST;

// class ImageSearchQuery
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageSearchQuery {
    private String artist;
    private String album;
    private File dir;
    private List<Integer> rows;

    public static boolean contains(List<ImageSearchQuery> queries, String album) {
        for(ImageSearchQuery imageSearchQuery : queries) {
            if (imageSearchQuery.getAlbum().equals(album)) {
                return true;
            }
        }
        return false;
    }

    public static int getIndex(List<ImageSearchQuery> queries, String album) {
        for (int i = 0; i < queries.size(); i++) {
            if (queries.get(i).getAlbum().equals(album)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the primary artist from the given artist (really just the first one)
     * @param artist the artist to split up
     */
    public static String getPrimaryArtist(String artist) {
        if (StringUtils.isEmpty(artist)) {
            return artist;
        }
        String[] individualArtists = artist.split("( & )|( x )|( X )|(, & )|(, )|( with )");
        return individualArtists[0];
    }

    /**
     * Gets the main title, leaving out featuring artists
     * @param title the title to parse
     */
    public static String getMainTitle(String title) {
        if (StringUtils.isEmpty(title)) {
            return title;
        }
        if (title.matches(TITLE_FEATURING_AND_REMIX_ARTIST)) {
            String featuredArtist = RegexUtils.getMatchedGroup(title, TITLE_FEATURING_AND_REMIX_ARTIST, "featuredArtist");
            return title.replace(featuredArtist, StringUtils.EMPTY);
        } else if (title.matches(TITLE_FEATURING_ARTIST)) {
            String featuredArtist = RegexUtils.getMatchedGroup(title, TITLE_FEATURING_ARTIST, "featuredArtist");
            return title.replace(featuredArtist, StringUtils.EMPTY);
        }
        return title;
    }
}
