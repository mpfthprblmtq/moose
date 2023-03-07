/*
 *  Proj:   Moose
 *  File:   IconUtils.java
 *  Desc:   A utility class to return an Icon based on constant values.
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.utilities;

// imports
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.Objects;

// class IconUtils
public class IconUtils {

    // constants that determine which file to use
    public static final String DEFAULT = "default.png";
    public static final String EDITED = "edit.png";
    public static final String SAVED = "check.png";
    public static final String SUCCESS = "check2.png";
    public static final String ERROR = "error.png";
    public static final String MOOSE_64 = "moose64.png";
    public static final String MOOSE_128 = "moose128.png";
    public static final String LOADING = "loading-icon.gif";
    public static final String LOADING_BIG = "loading-icon-big.gif";
    public static final String CIRCLE = "circle-outline.png";

    /**
     * Returns an icon based on the icon passed in
     * @param type a string constant that determines which resource to get
     * @return an Icon of the image resource
     */
    public static Icon get(String type) {
        return new ImageIcon(Objects.requireNonNull(IconUtils.class.getResource("/" + type)));
    }
}
