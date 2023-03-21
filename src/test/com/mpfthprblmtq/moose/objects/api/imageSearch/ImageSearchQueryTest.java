package com.mpfthprblmtq.moose.objects.api.imageSearch;

import com.mpfthprblmtq.commons.utils.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageSearchQueryTest {

    @Test
    public void testGetMainTitle_whenGivenEmptyString_returnsEmptyString() {
        assertEquals(StringUtils.EMPTY, ImageSearchQuery.getMainTitle(StringUtils.EMPTY));
    }

    @Test
    public void testGetMainTitle_whenGivenNonFeaturedArtistNonRemix_returnsTitle() {
        String title = "Title";
        assertEquals(title, ImageSearchQuery.getMainTitle(title));
    }

    @Test
    public void testGetMainTitle_whenGivenFeaturedArtistNonRemix_returnsTitleWithoutFeaturedArtist() {
        String title = "Title (ft. Artist)";
        assertEquals("Title", ImageSearchQuery.getMainTitle(title));
    }

    @Test
    public void testGetMainTitle_whenGivenNonFeaturedArtistWithRemixArtist_returnsTitleWithRemix() {
        String title = "Title (Artist Remix)";
        assertEquals(title, ImageSearchQuery.getMainTitle(title));
    }

    @Test
    public void testGetMainTitle_whenGivenFeaturedArtistWithRemixArtist_returnsTitleWithRemix() {
        String title = "Title (ft. FeaturedArtist) (Artist Remix)";
        assertEquals("Title (Artist Remix)", ImageSearchQuery.getMainTitle(title));
    }

}