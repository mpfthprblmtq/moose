package moose.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.objects.RequestProperties;
import com.mpfthprblmtq.commons.objects.RequestURL;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.commons.utils.WebUtils;
import moose.Moose;
import moose.objects.api.spotify.*;
import moose.utilities.Constants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class SpotifyApiService {

    // logger
    Logger logger = Moose.getLogger();

    // global token for use throughout the service
    private SpotifyToken token;

    // limits of results for each API
    private final int ARTIST_LIMIT = 5;
    private final int ALBUM_LIMIT = 20;

    public String getImages(String artistQuery, String albumQuery) {
        Artist artist = null;
        Album album = null;

        // authenticate first (also checks if we already have a valid token)
        authenticate();

        // search for the artist
        try {
            artist = getArtistFromSearch(artistQuery);
        } catch (IOException e) {
            if (e.getClass() == MalformedURLException.class) {
                logger.logError("Exception while forming URL with artist query: " + artistQuery, e);
            } else {
                logger.logError("Exception while getting artist info from Spotify API!", e);
            }
        }

        if (artist == null) {
            return null;
        }

        // search for the album
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

        if (album == null) {
            return null;
        }

        return album.getImage().getUrl();
    }

    /**
     * Authentication method that calls out to Spotify's auth url with the client id and client secret stored in the settings.
     * Sets the global token for use later and checks if token is still valid (to prevent subsequent auth calls)
     */
    public void authenticate() {

        // check to see if we need to authenticate first
        if (token != null && System.currentTimeMillis() > (token.getTimestamp() * 1000) + ((long) token.getExpiration() * 1000)) {
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
            System.err.println(e.getMessage());
        }
    }

    /**
     * Gets an artist based on a string query from Spotify's API
     * @param query the artist to search for
     * @return an Artist object or null if no artist was found
     * @throws IOException if a url could not be created from the query or if there's an issue calling the API
     */
    public Artist getArtistFromSearch(String query) throws IOException {
        // create the url
        URL url = new RequestURL()
                .withBaseUrl(Constants.SPOTIFY_SEARCH_URL)
                .withQueryParam("q", URLEncoder.encode(query, StandardCharsets.UTF_8.toString()))
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

        // return the first one (or nothing)
        return artists.getArtists().size() > 0 ? artists.getArtists().get(0) : null;
    }

    /**
     * Gets an album based on an artist ID and a name of an album from Spotify's API
     * @param artistId the id of the artist to search on
     * @param nextUrl the "next" url to search on if we need to make subsequent calls
     * @return an album or null if the album wasn't found
     * @throws IOException if a url could not be created from the query or if there's an issue calling the API
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
}
