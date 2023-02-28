/*
 *  Proj:   Moose
 *  File:   DialogUtils.java
 *  Desc:   Common functions/methods that deal with Dialogs and JOptionPanes.
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.utilities.viewUtils;

// imports
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.api.imageSearch.ImageSearchQuery;
import com.mpfthprblmtq.moose.utilities.Constants;
import com.mpfthprblmtq.moose.utilities.IconUtils;
import com.mpfthprblmtq.moose.views.modals.AlbumArtFinderFrame;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// class DialogUtils
public class DialogUtils {

    // constants used in custom dialogs
    public static final int ONLY_MARKED_ALBUMS = 1;
    public static final int ALL_ALBUMS = 2;
    public static final int START_NEW_AUDIT = 1;
    public static final int CONTINUE_AUDIT = 2;

    /**
     * Shows the about dialog, includes name, version, and copyright
     */
    public static void showAboutDialog() {
        int year = LocalDate.now().getYear();
        JOptionPane.showMessageDialog(null,
                "<html><b>Moose</b></html>\nVersion: " + Moose.getSettings().getVersion() + "\n" +
                        Constants.MOOSE_COPYRIGHT + year,
                "About Moose", JOptionPane.PLAIN_MESSAGE, IconUtils.get(IconUtils.MOOSE_128));
    }

    /**
     * Utility function that shows a dialog when clearing the main song list.
     * Has a checkbox for "Don't show this again"
     * @param context the component to place this over
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

    /**
     * Utility function that shows a dialog when starting an audit.
     * Has a checkbox for "Don't show this again"
     * @param context the component to place this over
     * @return the result of the checkbox, returns null if user hits no or cancels dialog
     */
    public static Boolean showAuditWarningDialog(Component context) {

        // build the checkbox component
        JCheckBox dontShowAgainBox = new JCheckBox("Don't show this again");

        // build the message array thing to show on the box
        Object[] message = {"Warning: This feature was fleshed out for my own purposes and\n" +
                "might not work for your own system, user results may vary.", StringUtils.NEW_LINE, dontShowAgainBox};

        // show the dialog
        JOptionPane.showMessageDialog(context, message, "Warning - Experimental Feature", JOptionPane.WARNING_MESSAGE);

        // return if the "don't show again" box is selected
        return dontShowAgainBox.isSelected();
    }

    /**
     * Show the default settings dialog window, which allows the user to select which settings they want to default
     * @param context the component we want to show this dialog over
     * @param selectedTab currently selected tab
     * @return a map that tells us what feature(s) we want defaulted
     */
    public static Map<Integer, Boolean> showDefaultSettingsDialog(Component context, int selectedTab) {
        JCheckBox genreBox = new JCheckBox("Genre List");
        JCheckBox loggingBox = new JCheckBox("Logging");
        JCheckBox filesBox = new JCheckBox("Files");
        JCheckBox apiBox = new JCheckBox("API Configuration");
        JCheckBox featuresBox = new JCheckBox("Features");
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
            case Constants.FEATURES:
                featuresBox.setSelected(true);
                break;
        }
        Object[] message = {"Select which Settings you want to clear:", genreBox, loggingBox, filesBox, apiBox, featuresBox};

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
            boxes.put(Constants.FEATURES, featuresBox.isSelected());
            return boxes;
        }
        return null;
    }

    /**
     * A dialog that allows the user to enter a track number if there isn't one
     * @param component the component we want to show this dialog over
     * @param title the track title to show the track number context
     * @return the title and track (in that order)
     */
    public static String[] showGetTitleAndTrackNumberDialog(Component component, String title) {
        // create and configure the title field
        JTextField titleField = new JTextField(20);
        titleField.setText(StringUtils.isNotEmpty(title) ? title.replace(":", "/") : StringUtils.EMPTY);

        // create and configure the track number field
        JTextField trackField = new JTextField();
        ((AbstractDocument)trackField.getDocument()).setDocumentFilter(new DocumentFilter(){
            final Pattern regex = Pattern.compile("\\d*");

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                Matcher matcher = regex.matcher(text);
                if(!matcher.matches()){
                    return;
                }
                super.replace(fb, offset, length, text, attrs);
            }
        });
        Dimension d = trackField.getPreferredSize();
        d.setSize(75, d.getHeight());
        trackField.setMinimumSize(d);
        trackField.setMaximumSize(d);
        trackField.setPreferredSize(d);

        // create labels
        JLabel titleLabel = new JLabel("Title:");
        JLabel trackLabel = new JLabel("Track #:");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(titleLabel);
        panel.add(titleField);
        panel.add(new JLabel(StringUtils.SPACE));
        panel.add(trackLabel);
        panel.add(trackField);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        trackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        trackField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // create the list of items
        Object[] message = {panel};

        ViewUtils.focusOnField(trackField, "Manual Set Track Number");

        // show the dialog
        int option = JOptionPane.showConfirmDialog(component, message, "Manual Set Track Number", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            return new String[]{trackField.getText(), titleField.getText().replace("/", ":")};
        } else {
            return null;
        }
    }

    /**
     * A dialog that allows the user to enter a track number, artist, and title if there isn't one
     * @param component the component we want to show this dialog over
     * @param title the track title to show the track number context
     * @param artist the artist to show the track number context
     * @return a string array with the track, artist, and title (in that order)
     */
    public static String[] showGetTitleAndTrackNumberAndArtistDialog(Component component, String title, String artist) {
        // create and configure the title field
        JTextField titleField = new JTextField(20);
        titleField.setText(StringUtils.isNotEmpty(title) ? title.replace(":", "/") : StringUtils.EMPTY);

        // create and configure the artist field
        JTextField artistField = new JTextField(20);
        artistField.setText(StringUtils.isNotEmpty(artist) ? artist.replace(":", "/") : StringUtils.EMPTY);

        // create and configure the track number field
        JTextField trackField = new JTextField();
        ((AbstractDocument)trackField.getDocument()).setDocumentFilter(new DocumentFilter(){
            final Pattern regex = Pattern.compile("\\d*");

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                Matcher matcher = regex.matcher(text);
                if(!matcher.matches()){
                    return;
                }
                super.replace(fb, offset, length, text, attrs);
            }
        });
        Dimension d = trackField.getPreferredSize();
        d.setSize(75, d.getHeight());
        trackField.setMinimumSize(d);
        trackField.setMaximumSize(d);
        trackField.setPreferredSize(d);

        // create labels
        JLabel titleLabel = new JLabel("Title:");
        JLabel artistLabel = new JLabel("Artist:");
        JLabel trackLabel = new JLabel("Track #:");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(artistLabel);
        panel.add(artistField);
        panel.add(new JLabel(StringUtils.SPACE));
        panel.add(titleLabel);
        panel.add(titleField);
        panel.add(new JLabel(StringUtils.SPACE));
        panel.add(trackLabel);
        panel.add(trackField);
        artistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        artistField.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        trackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        trackField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // create the list of items
        Object[] message = {panel};

        ViewUtils.focusOnField(trackField, "Manual Set Track Number");

        // show the dialog
        int option = JOptionPane.showConfirmDialog(component, message, "Manual Set Track Number", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            return new String[]{
                    trackField.getText(),
                    artistField.getText().replace("/", ":"),
                    titleField.getText().replace("/", ":")};
        } else {
            return null;
        }
    }

    /**
     * Shows a simple message dialog
     * @param component the context of the dialog
     * @param message the message to display
     * @param title the title to display
     * @param messageType the message type (JOptionPane)
     */
    public static void showMessageDialog(Component component, String message, String title, int messageType) {
        JOptionPane.showMessageDialog(component, message, title, messageType);
    }

    /**
     * Shows a dialog with options to create a new audit or continue an existing audit
     * @return the option chosen (0 = cancel, 1 = start new, 2 = continue)
     */
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

    /**
     * Shows a dialog with options to edit all albums or marked albums as part of an audit
     * @return the option chosen (0 = cancel, 1 = only marked albums, 2 = all albums)
     */
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

    /**
     * Shows a dialog to confirm if the user wants to exit with unsaved changes
     * @param component the context of the dialog to show
     * @return a JOptionPane Yes/No option
     */
    public static int showUnsavedChangesDialog(Component component) {
        return JOptionPane.showConfirmDialog(
                component,
                "You have unsaved changes, are you sure you want to exit?",
                "Unsaved Changes",
                JOptionPane.YES_NO_OPTION);
    }

    /**
     * Checks to see if user wants to use the Album Art Finder Service
     * @return if the user wants to use the album art finder
     */
    public static int confirmUserWantsAlbumArtFinder() {
        return JOptionPane.showConfirmDialog(
                Moose.frame,
                "Cover art wasn't automatically found, would you like\n"
                        + "to use the Album Art Finder service in Moose?",
                "Album Art Finder Service",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a window for the album art
     * @param query the ImageSearchQuery object to use with the album art finder window
     */
    public static void showAlbumArtWindow(ImageSearchQuery query) {
        if (SwingUtilities.isEventDispatchThread()) {
            AlbumArtFinderFrame albumArtFinderFrame = new AlbumArtFinderFrame(query);
            albumArtFinderFrame.setLocationRelativeTo(Moose.frame);
            albumArtFinderFrame.setVisible(true);
        } else {
            SwingUtilities.invokeLater(() -> {
                AlbumArtFinderFrame albumArtFinderFrame = new AlbumArtFinderFrame(query);
                albumArtFinderFrame.setLocationRelativeTo(Moose.frame);
                albumArtFinderFrame.setVisible(true);
            });
        }
    }

}
