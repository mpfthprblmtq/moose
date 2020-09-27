package moose.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;

import moose.utilities.Constants;
import moose.utilities.Logger;
import moose.Main;
import moose.objects.ImageSearchResponse;
import moose.utilities.Utils;

import static moose.utilities.Constants.LARGE;
import static moose.utilities.Constants.XLARGE;

public class AlbumArtFinderService {

    static Logger logger = Main.logger;
    
    // list of responses
    List<ImageSearchResponse> responses = new ArrayList<>();
    int img_size = Main.getSettings().getPreferredCoverArtSize();
    
    public AlbumArtFinderService() {
        
    }

    public List<ImageSearchResponse> getResponses() {
        return this.responses;
    }

    /**
     * Checks to see if we're below the image limit threshold
     * @return the result of the check
     */
    public static boolean checkIfBelowLimit() {
        return Main.getSettings().getAlbumArtFinderSearchCount() < Constants.IMAGE_LIMIT;
    }
    
    public void makeFirstCall(String query) {
        // make the first call for large type images
        responses.addAll(makeJsonCall(query, LARGE, 1));
    }

    public void makeSecondCall(String query) {
        // make the second call for large type images
        responses.addAll(makeJsonCall(query, LARGE, 11));
    }

    public void makeThirdCall(String query) {
        // make the first call for xlarge type images
        responses.addAll(makeJsonCall(query, XLARGE, 1));
    }

    public void makeFourthCall(String query) {
        // make the second call for xlarge type images
        responses.addAll(makeJsonCall(query, XLARGE, 11));
    }

    public ImageSearchResponse processImage(ImageSearchResponse isr) {
        if ((isr.getImage().getHeight() != isr.getImage().getWidth())   // if the image isn't square
                || (isr.getImage().getHeight() < img_size && isr.getImage().getWidth() < img_size)) {   // if the image is smaller than the desired size
//            toRemove.add(isr);
            return isr;
        } else {
            try {
                isr.setBImage(ImageIO.read(new URL(isr.getLink())));
                if(isr.getBImage() == null) {
                    return isr;
                }
            } catch (IOException ex) {
                logger.logError("IOException when trying to read an image from the url: " + isr.getLink() + ", cause: " + ex.getCause().getMessage(), ex);
                return isr;
            }
        }
        return null;
    }

    public void processResponses() {
//        int img_size = Main.getSettings().getPreferredCoverArtSize();

        List<ImageSearchResponse> toRemove = new ArrayList<>();
        responses.forEach((isr) -> {
            if ((isr.getImage().getHeight() != isr.getImage().getWidth())   // if the image isn't square
                    || (isr.getImage().getHeight() < img_size && isr.getImage().getWidth() < img_size)) {   // if the image is smaller than the desired size
                toRemove.add(isr);
            } else {
                try {
                    isr.setBImage(ImageIO.read(new URL(isr.getLink())));
                    if(isr.getBImage() == null) {
                        toRemove.add(isr);
                    }
                } catch (IOException ex) {
                    logger.logError("IOException when trying to read an image from the url: " + isr.getLink() + ", cause: " + ex.getCause().getMessage(), ex);
                    toRemove.add(isr);
                }
            }
        });
        responses.removeAll(toRemove);
    }
    
    /**
     * Updates the album art settings
     */
    public void updateAlbumArtSettings() {
        // check if date matches today
        String settingsDate = Main.getSettings().getAlbumArtFinderSearchCountDate();
        String todaysDate = Utils.formatDate(new Date());
        if (settingsDate.equals(todaysDate)) {
            // date is today, just increment call count
            Main.getSettings().setAlbumArtFinderSearchCount(Main.getSettings().getAlbumArtFinderSearchCount() + 1);
        } else {
            // date is not today, set the new date and set the new count to 1
            Main.getSettings().setAlbumArtFinderSearchCountDate(todaysDate);
            Main.getSettings().setAlbumArtFinderSearchCount(1);
        }
        Main.updateSettings();
    }

    /**
     * Actually makes the JSON call
     *
     * @param query
     * @param width
     * @param start
     * @return
     */
    public static List<ImageSearchResponse> makeJsonCall(String query, String width, int start) {

        List<ImageSearchResponse> responseList = new ArrayList<>();
        try {
            // build the url from the query
            URL url = new URL(buildUrl(query, width, start));

            // create the connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // set some default parameters for the rest call
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            // response variables
            int responseCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();
            
            // if it's good
            if (responseCode == 200) {
                StringBuilder response;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                
                // parse through the response
                ObjectMapper mapper = new ObjectMapper();
                String responseString = response.toString();
                responseString = responseString.replace("{  \"items\": ", "");   // get rid of the "items" field name in json
                responseString = responseString.substring(0, responseString.length() - 1);  // get rid of the trailing curly brace
                
                // disables the "An illegal reflective access operation has occurred" warning
                if (Main.getSettings().isInDeveloperMode()) {
                    Logger.disableLogging();
                } else {
                    Logger.setSystemErrToConsole();
                }
                responseList = mapper.readValue(responseString, new TypeReference<List<ImageSearchResponse>>(){});  // parse that bad boy
                // TODO:  Get rid of "An illegal reflective access operation has occured" ^^^ happens on this line
                if (Main.getSettings().isInDeveloperMode()) {
                    
                }
                
            // if it's not so good
            } else {
                ImageSearchResponse errorSearchResponse = new ImageSearchResponse();
                errorSearchResponse.setMime("error");
                errorSearchResponse.setLink("Response code: " + responseCode + ", Response message: " + responseMessage);
                return Arrays.asList(new ImageSearchResponse[]{errorSearchResponse});
            }
        } catch (MalformedURLException ex) {
            logger.logError("MalformedURLException when trying to create url to search!", ex);
        } catch (IOException ex) {
            logger.logError("IOException when trying to search for album art!", ex);
        }
        return responseList;
    }

    /**
     * Builds the url to send to googleapi
     *
     * @param query
     * @param width
     * @return
     */
    private static String buildUrl(String query, String width, int start) {
        String api_key = Main.getSettings().getAlbumArtFinderApiKey();
        String cse_id = Main.getSettings().getAlbumArtFinderCseId();
        String fields = "items(image(height%2Cwidth)%2Clink%2Cmime)";
        String search_type = "image";

        String URL_BASE = "https://www.googleapis.com/customsearch/v1";
        String URL_Q_ID = "?q=";
        String URL_IMG_SIZE_ID = "&imgSize=";
        String URL_CSE_ID = "&cx=";
        String URL_KEY_ID = "&key=";
        String URL_FIELDS_ID = "&fields=";
        String URL_SEARCH_TYPE_ID = "&searchType=";
        String URL_START_INDEX = "&start=";

        StringBuilder builder = new StringBuilder();
        builder.append(URL_BASE);
        builder.append(URL_Q_ID);
        builder.append(query);
        builder.append(URL_KEY_ID);
        builder.append(api_key);
        builder.append(URL_CSE_ID);
        builder.append(cse_id);
        builder.append(URL_SEARCH_TYPE_ID);
        builder.append(search_type);
        builder.append(URL_START_INDEX);
        builder.append(start);
        builder.append(URL_IMG_SIZE_ID);
        builder.append(width);
        builder.append(URL_FIELDS_ID);
        builder.append(fields);
        return encodeForUrl(builder.toString());
    }

    private static String encodeForUrl(String toEncode) {
        return toEncode.replace(" ", "+");
    }

    /**
     * Builds the google images search query
     *
     * @param query
     * @return
     */
    public static String buildImageSearchQuery(String query) {
        String IMG_QRY_START = "https://www.google.com/search?q=";
        String IMG_QRY_END = "&biw=1680&bih=953&tbm=isch&source=lnt&tbs=isz:l";
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(IMG_QRY_START);
        stringBuilder.append(encodeForUrl(query));
        stringBuilder.append(IMG_QRY_END);

        return stringBuilder.toString();
    }
}
