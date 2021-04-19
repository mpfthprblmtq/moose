package moose.services;

import moose.Moose;
import moose.utilities.Constants;
import moose.utilities.StringUtils;

import javax.swing.*;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

public class DialogService {

    static IconService iconService = new IconService();

    /**
     * Show the about dialog, includes name, version, and copyright
     */
    public static void showAboutDialog() {
        JOptionPane.showMessageDialog(null,
                "<html><b>Moose</b></html>\nVersion: " + Moose.getSettings().getVersion() + "\n" + "© Pat Ripley 2018-2021",
                "About Moose", JOptionPane.PLAIN_MESSAGE, iconService.get(IconService.MOOSE_128));
    }

    /**
     * Utility function that shows a dialog when clearing the main song list
     * Has a checkbox for "Don't show this again"
     *
     * @param context, the component to place this over
     * @return the result of the checkbox, returns null if user hits no or cancels dialog
     */
    public static Boolean showClearAllDialog(Component context) {

        // build the checkbox component
        JCheckBox dontAskAgainBox = new JCheckBox("Don't ask again");

        // build the message array thing to show on the box
        Object[] message = {"Are you sure you want to clear all songs from the table?", StringUtils.NEW_LINE, dontAskAgainBox};

        // show the dialog
        int option = JOptionPane.showConfirmDialog(context, message, "Clearing All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        // check response
        if (option == JOptionPane.YES_OPTION) {
            return dontAskAgainBox.isSelected();
        } else {
            return null;
        }
    }

    public static Map<Integer, Boolean> showDefaultSettingsDialog(Component context, int selectedTab) {
        JCheckBox genreBox = new JCheckBox("Genre List");
        JCheckBox loggingBox = new JCheckBox("Logging");
        JCheckBox filesBox = new JCheckBox("Files");
        JCheckBox apiBox = new JCheckBox("API Configuration");
        switch (selectedTab) {
            case Constants.GENRE:
                genreBox.setSelected(true);
                break;
            case Constants.LOGGING:
                loggingBox.setSelected(true);
                break;
            case Constants.FILES:
                filesBox.setSelected(true);
                break;
            case Constants.API:
                apiBox.setSelected(true);
                break;
        }
        Object[] message = {"Select which Settings you want to clear:", genreBox, loggingBox, filesBox, apiBox};

        int option = JOptionPane.showConfirmDialog(
                context,
                message,
                "Select which Settings to erase",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            Map<Integer, Boolean> boxes = new HashMap<>();
            boxes.put(Constants.GENRE, genreBox.isSelected());
            boxes.put(Constants.LOGGING, loggingBox.isSelected());
            boxes.put(Constants.FILES, filesBox.isSelected());
            boxes.put(Constants.API, apiBox.isSelected());
            return boxes;
        }
        return null;
    }

    public static void showMessageDialog(Component component, String message, String title, int messageType) {
        JOptionPane.showMessageDialog(component, message, title, messageType);
    }

    public static int showExistingAuditDialog() {
        Object[] options = new Object[]{"Cancel", "Start New", "Continue"};
        return JOptionPane.showOptionDialog(
                Moose.auditFrame,
                "An existing audit is in process, do you want to continue?",
                "Existing audit found",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                null);
    }

    public static int showShouldAuditAllDialog() {
        Object[] options = new Object[]{"Cancel", "Only Marked Albums", "All Albums"};
        return JOptionPane.showOptionDialog(
                Moose.auditFrame,
                "Would you like to audit all of the albums, or just the ones marked in the audit?",
                "Audit",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                "Only Marked Albums"
        );
    }
}
