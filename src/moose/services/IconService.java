package moose.services;

import javax.swing.*;
import java.util.Objects;

public class IconService {

    public static final int DEFAULT = 0;
    public static final int EDITED = 1;
    public static final int SAVED = 2;

    public static final int AUDIT_PASS = 3;
    public static final int AUDIT_FAIL = 4;

    public static final int MOOSE_128 = 5;

    public IconService() {}

    public Icon get(int type) {
        if (this.getClass() != null) {
            switch(type) {
                case MOOSE_128:
                    return new ImageIcon(Objects.requireNonNull(
                            this.getClass().getResource("/resources/moose128.png")));
                case AUDIT_PASS:
                    return new ImageIcon(Objects.requireNonNull(
                            this.getClass().getResource("/resources/check2.png")));
                case AUDIT_FAIL:
                    return new ImageIcon(Objects.requireNonNull(
                            this.getClass().getResource("/resources/error.png")));
            }
        }
        return null;
    }
}
