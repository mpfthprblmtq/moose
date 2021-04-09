/*
   Proj:   Moose
   File:   Utils.java
   Desc:   Helper class with a smattering of methods to do some neat stuff

   Copyright Pat Ripley 2018
 */

// package
package moose.utilities;

// imports
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

import moose.Main;
import moose.utilities.logger.Logger;

public class MiscUtils {

    // logger
    static Logger logger = Main.logger;
    
    /**
     * Checks if int[] contains a certain int
     * 
     * @param arr, the array of ints to check
     * @param key, the key to check for
     * @return the result of the check
     */
    public static boolean intArrayContains(int[] arr, int key) {
        return Arrays.stream(arr).anyMatch(i -> i == key);
    }
}
