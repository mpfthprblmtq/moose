/*
   Proj:   Moose
   File:   AlbumArtFinderService.java
   Desc:   Service class for the album art finder

   Copyright Pat Ripley 2018-2023
 */

// package
package com.mpfthprblmtq.moose.services;

// imports
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;

import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.objects.RequestProperties;
import com.mpfthprblmtq.commons.objects.RequestURL;
import com.mpfthprblmtq.commons.utils.DateUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.commons.utils.WebUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Settings;
import com.mpfthprblmtq.moose.objects.api.imageSearch.ImageSearchResponse;
import com.mpfthprblmtq.moose.objects.api.imageSearch.ImageSearchResult;
import com.mpfthprblmtq.moose.utilities.Constants;

// class GoogleSearchService
public class GoogleSearchService {

    static Logger logger = Moose.logger;
    
    int img_size = Moose.getSettings().getPreferredCoverArtSize();
    
    public GoogleSearchService() {}

    /**
     * Checks to see if we're below the image limit threshold
     * @return the result of the check
     */
    public static boolean checkIfBelowLimit() {
        return Moose.getSettings().getAlbumArtFinderSearchCount() < Constants.IMAGE_LIMIT;
    }

    public ImageSearchResult processImage(ImageSearchResult isr) {
        if ((isr.getImageDimensions().getHeight() != isr.getImageDimensions().getWidth())   // if the image isn't square
                || (isr.getImageDimensions().getHeight() < img_size && isr.getImageDimensions().getWidth() < img_size)) {   // if the image is smaller than the desired size
            return isr;
        } else {
            try {
                isr.setBufferedImage(ImageIO.read(new URL(isr.getUrl())));
                if(isr.getBufferedImage() == null) {
                    return isr;
                }
            } catch (IOException ex) {
                logger.logError("IOException when trying to read an image from the url: " + isr.getUrl() + ", cause: " + ex.getCause().getMessage(), ex);
                return isr;
            }
        }
        return null;
    }

    /**
     * Updates the album art settings
     */
    public void updateAlbumArtSettings() {
        // get the old settings to update
        Settings settings = Moose.getSettings();

        // check if date matches today
        String settingsDate = settings.getAlbumArtFinderSearchCountDate();
        String todaysDate = DateUtils.formatSimpleDate(new Date());
        if (settingsDate.equals(todaysDate)) {
            // date is today, just increment call count
            settings.setAlbumArtFinderSearchCount(Moose.getSettings().getAlbumArtFinderSearchCount() + 2);
        } else {
            // date is not today, set the new date and set the new count to 2
            settings.setAlbumArtFinderSearchCountDate(todaysDate);
            settings.setAlbumArtFinderSearchCount(2);
        }
        Moose.updateSettings(settings);
    }

    /**
     * Actually makes the JSON call
     *
     * @param query, the query to search
     * @param width, the preferred image size
     * @param start, the index to start on in the result list
     * @return a list of ImageSearchResults
     */
    public List<ImageSearchResult> doGoogleSearch(String query, String width, int start) {

        try {
            // build the url from the query
            URL url = buildUrl(query, width, start);

            // set up the request
            RequestProperties requestProperties = new RequestProperties()
                    .withProperty("User-Agent", "Mozilla/5.0")
                    .build();

            // make the request
            String response = WebUtils.get(url, requestProperties.getProperties());

            // map response to object
            ObjectMapper mapper = new ObjectMapper();
            ImageSearchResponse imageSearchResponse = mapper.readValue(response, ImageSearchResponse.class);
            return imageSearchResponse.getItems();

        } catch (MalformedURLException ex) {
            logger.logError("MalformedURLException when trying to create url to search!", ex);
        } catch (IOException ex) {
            logger.logError("IOException when trying to search for album art!", ex);
        }
        return new ArrayList<>();
    }

    /**
     * Builds the url to send to googleapi
     * @param query, the query to search on
     * @param width, the preferred image size
     * @return a url to search on
     */
    private static URL buildUrl(String query, String width, int start) throws MalformedURLException, UnsupportedEncodingException {
        return new RequestURL()
                .withBaseUrl(Constants.CUSTOM_SEARCH_URL)
                .withQueryParam("q", encodeForUrl(query))
                .withQueryParam("imgSize", width)
                .withQueryParam("cx", Moose.getSettings().getAlbumArtFinderCseId())
                .withQueryParam("key", Moose.getSettings().getAlbumArtFinderApiKey())
                .withQueryParam("fields", "items(image(height,width),link,mime)")
                .withQueryParam("searchType", "image")
                .withQueryParam("start", String.valueOf(start))
                .buildUrl();
    }

    private static String encodeForUrl(String toEncode) {
        try {
            return URLEncoder.encode(toEncode, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            logger.logError("Couldn't encode url: " + toEncode);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Builds the google images search query
     *
     * @param query, the query to search on
     * @return a image search query url
     */
    public static String buildImageSearchQuery(String query) {
        String IMG_QRY_START = "https://www.google.com/search?q=";
        String IMG_QRY_END = "&biw=1680&bih=953&tbm=isch&source=lnt&tbs=isz:l";

        return IMG_QRY_START + encodeForUrl(query) + IMG_QRY_END;
    }
}
