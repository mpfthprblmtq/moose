/*
 *  Proj:   Moose
 *  File:   SpotifyApiService.java
 *  Desc:   Service class for Spotify search functionality on the Album Art Finder window.
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.services;

// imports
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.objects.RequestProperties;
import com.mpfthprblmtq.commons.objects.RequestURL;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.commons.utils.WebUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.api.spotify.*;
import com.mpfthprblmtq.moose.utilities.Constants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// class SpotifyApiService
public class SpotifyApiService {

    // logger
    Logger logger = Moose.getLogger();

    // global token for use throughout the service
    private SpotifyToken token;

    // limits of results for each API
    @SuppressWarnings("FieldCanBeLocal")
    private final int ARTIST_LIMIT = 5;
    @SuppressWarnings("FieldCanBeLocal")
    private final int ALBUM_LIMIT = 20;

    /**
     * Automatic function that tries and find the album magically. Basically if it finds a 1 to 1 match on the artist
     * and a 1 to 1 match on the album, just return that url, else return null
     * @param artistQuery the artist to search for
     * @param albumQuery the album to search for
     * @return the url of the image of the found album or null if the album or artist isn't found
     */
    public String getImage(String artistQuery, String albumQuery) {
        List<Artist> artists = new ArrayList<>();
        Artist artist = null;
        Album album = null;

        if (StringUtils.isEmpty(Moose.getSettings().getSpotifyClientSecret())
                || StringUtils.isEmpty(Moose.getSettings().getSpotifyClientId())) {
            return null;
        }

        // authenticate first
        authenticate();

        // check if the artist is in the list of known spotify artists
        if (Moose.getSettings().getSpotifyArtists().containsKey(artistQuery)) {
            try {
                artist = getArtist(Moose.getSettings().getSpotifyArtists().get(artistQuery));
            } catch (IOException e) {
                if (e.getClass() == MalformedURLException.class) {
                    logger.logError("Exception while forming URL with artist query: " + artistQuery, e);
                } else {
                    logger.logError("Exception while getting artist info from Spotify API!", e);
                }
            }
        } else {
            // search for the artist
            try {
                artists = getArtistsFromSearch(artistQuery);
            } catch (IOException e) {
                if (e.getClass() == MalformedURLException.class) {
                    logger.logError("Exception while forming URL with artist query: " + artistQuery, e);
                } else {
                    logger.logError("Exception while getting artist info from Spotify API!", e);
                }
            }

            // if there's multiple artists found, we need to do some manual intervention
            if (artists.size() != 1) {
                return null;
            }

            // grab that artist from the list
            artist = artists.get(0);
        }

        // if we don't have an artist, we can't search for the album
        if (artist == null) {
            return null;
        }

        // add that artist to the list of known artists if it isn't already
        if (!Moose.getSettings().getSpotifyArtists().containsKey(artist.getName())) {
            Moose.getSettingsController().addSpotifyArtist(artist.getName(), artist.getId());
        }

        // search for the album with the found artist
        try {
            String nextUrl = null;
            while (true) {
                AlbumSearchResponse albums = getAlbumFromArtist(artist.getId(), nextUrl);
                for (Album albumInList : albums.getAlbums()) {
                    if (albumInList.getName().contains(albumQuery)) {
                        album = albumInList;
                        break;
                    }
                }

                // check if we found a match at this point
                if (album != null) {
                    break;
                }

                // we got here, so we need to use the next url to make more calls (if we have it)
                if (StringUtils.isNotEmpty(albums.getNext())) {
                    nextUrl = albums.getNext();
                } else {
                    // if the next url is null, we can't search anymore, album wasn't found
                    return null;
                }
            }

        } catch (IOException e) {
            if (e.getClass() == MalformedURLException.class) {
                logger.logError("Exception while forming URL with album query: " + albumQuery, e);
            } else {
                logger.logError("Exception while getting album info from Spotify API!", e);
            }
        }

        // if we don't have an album, then it wasn't found
        if (album == null) {
            return null;
        }

        return album.getImage().getUrl();
    }

    /**
     * Authentication method that calls out to Spotify's auth url with the client id and client secret stored in the
     * settings. Sets the global token for use later and checks if token is still valid (to prevent subsequent auth
     * calls)
     */
    public void authenticate() {

        // check to see if we need to authenticate first
        if (token != null
                && System.currentTimeMillis() > (token.getTimestamp() * 1000) + ((long) token.getExpiration() * 1000)) {
            return;
        }

        try {
            // set up request
            URL url = new RequestURL(Constants.SPOTIFY_AUTH_URL).buildUrl();
            RequestProperties requestProperties = new RequestProperties()
                    .withProperty("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            String body = "grant_type=client_credentials" +
                    "&client_id=" + Moose.getSettings().getSpotifyClientId() +
                    "&client_secret=" + Moose.getSettings().getSpotifyClientSecret();

            // make the request
            String response = WebUtils.post(url, true, requestProperties.getProperties(), body);

            // map response to object
            ObjectMapper mapper = new ObjectMapper();
            token = mapper.readValue(response, SpotifyToken.class);
            token.setTimestamp(new Date().getTime());

        } catch (IOException e) {
            // bad things
            logger.logError("Error while calling Spotify API auth!", e);
        }
    }

    /**
     * Gets an artist based on a string query from Spotify's API. Assumes we're already authenticated.
     * @param query the artist to search for
     * @return an Artist object or null if no artist was found
     * @throws IOException if url could not be created from the query or if there's an issue calling the API
     */
    public List<Artist> getArtistsFromSearch(String query) throws IOException {
        // create the url
        URL url = new RequestURL()
                .withBaseUrl(Constants.SPOTIFY_SEARCH_URL)
                .withQueryParam("q", URLEncoder.encode(query, StandardCharsets.UTF_8))
                .withQueryParam("type", "artist")
                .withQueryParam("limit", String.valueOf(ARTIST_LIMIT))
                .buildUrl();

        // set up the request
        RequestProperties requestProperties = new RequestProperties()
                .withProperty("Accept", "application/json")
                .withProperty("Content-Type", "application/json")
                .withProperty("Authorization", "Bearer " + token.getToken())
                .build();

        // make the request
        String response = WebUtils.get(url, requestProperties.getProperties());

        // map the response to an object
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        ObjectReader reader = mapper.readerFor(ArtistSearchResponse.class).withRootName("artists");
        ArtistSearchResponse artists = reader.readValue(response);

        // get rid of any non-exact matches (case-insensitive, just in case)
        artists.getArtists().removeIf(x -> !StringUtils.equalsIgnoreCase(x.getName(), query));

        // return the artist list
        return artists.getArtists();
    }

    /**
     * Gets an album based on an artist ID and a name of an album from Spotify's API.
     * Assumes we're already authenticated.
     * @param artistId the id of the artist to search on
     * @param nextUrl the "next" url to search on if we need to make subsequent calls
     * @return an album or null if the album wasn't found
     * @throws IOException if url could not be created from the query or if there's an issue calling the API
     */
    public AlbumSearchResponse getAlbumFromArtist(String artistId, String nextUrl) throws IOException {

        // if we have a nextUrl parameter, that means we should use that one
        URL url;
        if (StringUtils.isEmpty(nextUrl)) {
            // create the url
            url = new RequestURL()
                    .withBaseUrl(Constants.SPOTIFY_ALBUMS_URL)
                    .withUrlParam("id", artistId)
                    .withQueryParam("limit", String.valueOf(ALBUM_LIMIT))
                    .buildUrl();
        } else {
            // use the one given
            url = new URL(nextUrl);
        }

        // set up the request
        RequestProperties requestProperties = new RequestProperties()
                .withProperty("Accept", "application/json")
                .withProperty("Content-Type", "application/json")
                .withProperty("Authorization", "Bearer " + token.getToken())
                .build();

        // make the request
        String response = WebUtils.get(url, requestProperties.getProperties());

        // map the response to an object
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, AlbumSearchResponse.class);
    }

    /**
     * Gets an artist based on the artist ID given. Assumes we're already authenticated.
     * @param artistId the id of the artist to get
     * @return an Artist object
     * @throws IOException if url could not be created from the query or if there's an issue calling the API
     */
    public Artist getArtist(String artistId) throws IOException {
        // create the url
        URL url = new RequestURL()
                .withBaseUrl(Constants.SPOTIFY_ARTIST_URL)
                .withUrlParam("id", artistId)
                .buildUrl();

        // set up the request
        RequestProperties requestProperties = new RequestProperties()
                .withProperty("Content_Type", "application/json")
                .withProperty("Authorization", "Bearer " + token.getToken())
                .build();

        // make the request
        String response = WebUtils.get(url, requestProperties.getProperties());

        // map the response to an object
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, Artist.class);
    }
}
