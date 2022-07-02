package moose.services;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.Objects;

public class IconService {

    public static final String DEFAULT = "/resources/default.png";
    public static final String EDITED = "/resources/edit.png";
    public static final String SAVED = "/resources/check.png";
    public static final String SUCCESS = "/resources/check2.png";
    public static final String ERROR = "/resources/error.png";
    public static final String MOOSE_64 = "/resources/moose64.png";
    public static final String MOOSE_128 = "/resources/moose128.png";
    public static final String LOADING = "/resources/loading-icon.gif";

    public static Icon get(String type) {
        return new ImageIcon(Objects.requireNonNull(IconService.class.getResource(type)));
    }
}
