package com.mpfthprblmtq.moose.utilities;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.Objects;

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
