package moose.utilities;

import moose.Main;
import moose.utilities.logger.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebUtils {

    static Logger logger = Main.getLogger();

    /**
     * Opens a webpage with the specified url
     *
     * @param url, the url to open
     */
    public static void openPage(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            logger.logError("Exception when trying to open the webpage: " + url, e);
        }
    }
}
