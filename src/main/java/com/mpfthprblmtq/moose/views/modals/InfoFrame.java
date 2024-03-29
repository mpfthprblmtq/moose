/*
 *  Proj:   Moose
 *  File:   InfoFrame.java
 *  Desc:   Main UI class for the JFrame containing the song info.
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.views.modals;

// imports
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;

import com.mpfthprblmtq.commons.utils.FileUtils;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.controllers.SongController;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.utilities.Constants;
import com.mpfthprblmtq.moose.utilities.IconUtils;
import com.mpfthprblmtq.moose.utilities.ImageUtils;
import com.mpfthprblmtq.moose.utilities.MP3FileUtils;
import com.mpfthprblmtq.moose.utilities.viewUtils.ViewUtils;

import static com.mpfthprblmtq.moose.utilities.Constants.*;

// class InfoFrame
@SuppressWarnings("FieldCanBeLocal")    // for NetBeans' field declaration at bottom of class
public class InfoFrame extends javax.swing.JFrame {

    // some graphics fields
    ActionListener menuListener;        // listener for the popup menu objects
    
    // songController object
    SongController songController;

    // the song(s) in the thingy
    // I am
    // a m a z i n g
    // at commenting code
    Map<Integer, Song> songs;

    // edited globals
    boolean edited;
    boolean editModeEnabled;
    Component lastEditedField;
    List<String> originalValues = new ArrayList<>();

    // fields for the artwork label to check if the artwork has changed in this frame
    byte[] originalArtwork;
    byte[] newArtwork;

    int[] selectedRows;

    /**
     * Creates new InfoFrame
     * @param songs the map of songs to base info off of
     * @param selectedRows the selected rows on the main table
     * @param editModeEnabled a boolean to tell us if we want to start in edit mode
     * @param focusedField the focused field to start on
     */
    public InfoFrame(Map<Integer, Song> songs, int[] selectedRows, boolean editModeEnabled, Component focusedField) {
        initComponents();
        this.songs = songs;
        this.selectedRows = selectedRows;
        init();

        this.editModeEnabled = editModeEnabled;
        if (editModeEnabled) {
            editSubmitButton.setText("Submit");
            setFocusedField(focusedField);
        }
    }

    /**
     * It's some code innit
     */
    private void init() {

        // set the title
        if (songs.size() == 1) {
            Song s = songs.get(new ArrayList<>(songs.keySet()).get(0));
            this.setTitle(s.getArtist() + " - " + s.getTitle());
        } else {
            this.setTitle("> Multiple Values <");
        }

        // sets all the fields on the frame
        setFields(new ArrayList<>(songs.values()));

        // set the navigation button based on the row we're given
        setNavigationButtons();

        // set the edit mode
        setFieldsEditable(editModeEnabled);

        // set the song controller
        songController = Moose.getFrame().songController;

        this.menuListener = (ActionEvent event) -> {
            // switch based on the option selected
            switch (event.getActionCommand()) {
                case ADD_ARTWORK:
                    addAlbumArt();
                    break;
                case REMOVE_ARTWORK:
                    removeAlbumArt();
                    break;
                case AUTO_ARTWORK:
                    // TODO
                    break;
                default:
                    break;
            }
        };  // end menuListener
    }

    /**
     * Sets the focused field
     */
    private void setFocusedField(Component focusedField) {
        if(focusedField == null) {
            return;
        }

        switch (focusedField.getName()) {
            case "filename":
                filenameField.requestFocus();
                break;
            case "title":
                titleField.requestFocus();
                break;
            case "artist":
                artistField.requestFocus();
                break;
            case "album":
                albumField.requestFocus();
                break;
            case "albumArtist":
                albumArtistField.requestFocus();
                break;
            case "year":
                yearField.requestFocus();
                break;
            case "genre":
                genreField.requestFocus();
                break;
            case "track1":
                track1Field.requestFocus();
                break;
            case "track2":
                track2Field.requestFocus();
                break;
            case "disk1":
                disk1Field.requestFocus();
                break;
            case "disk2":
                disk2Field.requestFocus();
                break;
            case "comment":
                commentField.requestFocus();
                break;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        coverLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        filenameField = new javax.swing.JTextField();
        titleField = new javax.swing.JTextField();
        artistField = new javax.swing.JTextField();
        albumField = new javax.swing.JTextField();
        albumArtistField = new javax.swing.JTextField();
        yearField = new javax.swing.JTextField();
        genreField = new javax.swing.JTextField();
        track1Field = new javax.swing.JTextField();
        track2Field = new javax.swing.JTextField();
        disk1Field = new javax.swing.JTextField();
        disk2Field = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        commentField = new javax.swing.JTextArea();
        lengthField = new javax.swing.JTextField();
        bitrateField = new javax.swing.JTextField();
        sampleRateField = new javax.swing.JTextField();
        editSubmitButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        editedLabel = new javax.swing.JLabel();

        jLabel1.setText("jLabel1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        jLabel2.setText("More Info:");

        coverLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        coverLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        coverLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                coverLabelMouseClicked(evt);
            }
        });

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel4.setText("Filename:");

        filenameField.setEditable(false);
        filenameField.setMaximumSize(new java.awt.Dimension(200, 26));
        filenameField.setMinimumSize(new java.awt.Dimension(200, 26));
        filenameField.setName("filename"); // NOI18N
        filenameField.setNextFocusableComponent(titleField);
        filenameField.setPreferredSize(new java.awt.Dimension(200, 26));
        filenameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filenameFieldKeyReleased(evt);
            }
        });

        titleField.setEditable(false);
        titleField.setMaximumSize(new java.awt.Dimension(200, 26));
        titleField.setMinimumSize(new java.awt.Dimension(200, 26));
        titleField.setName("title"); // NOI18N
        titleField.setNextFocusableComponent(artistField);
        titleField.setPreferredSize(new java.awt.Dimension(200, 26));
        titleField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                titleFieldKeyReleased(evt);
            }
        });

        artistField.setEditable(false);
        artistField.setMaximumSize(new java.awt.Dimension(200, 26));
        artistField.setMinimumSize(new java.awt.Dimension(200, 26));
        artistField.setName("artist"); // NOI18N
        artistField.setNextFocusableComponent(albumField);
        artistField.setPreferredSize(new java.awt.Dimension(200, 26));
        artistField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                artistFieldKeyReleased(evt);
            }
        });

        albumField.setEditable(false);
        albumField.setMaximumSize(new java.awt.Dimension(200, 26));
        albumField.setMinimumSize(new java.awt.Dimension(200, 26));
        albumField.setName("album"); // NOI18N
        albumField.setNextFocusableComponent(albumArtistField);
        albumField.setPreferredSize(new java.awt.Dimension(200, 26));
        albumField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                albumFieldKeyReleased(evt);
            }
        });

        albumArtistField.setEditable(false);
        albumArtistField.setMaximumSize(new java.awt.Dimension(200, 26));
        albumArtistField.setMinimumSize(new java.awt.Dimension(200, 26));
        albumArtistField.setName("albumArtist"); // NOI18N
        albumArtistField.setNextFocusableComponent(yearField);
        albumArtistField.setPreferredSize(new java.awt.Dimension(200, 26));
        albumArtistField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                albumArtistFieldKeyReleased(evt);
            }
        });

        yearField.setEditable(false);
        yearField.setMaximumSize(new java.awt.Dimension(85, 26));
        yearField.setMinimumSize(new java.awt.Dimension(85, 26));
        yearField.setName("year"); // NOI18N
        yearField.setNextFocusableComponent(genreField);
        yearField.setPreferredSize(new java.awt.Dimension(85, 26));
        yearField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                yearFieldKeyReleased(evt);
            }
        });

        genreField.setEditable(false);
        genreField.setMaximumSize(new java.awt.Dimension(200, 26));
        genreField.setMinimumSize(new java.awt.Dimension(200, 26));
        genreField.setName("genre"); // NOI18N
        genreField.setNextFocusableComponent(track1Field);
        genreField.setPreferredSize(new java.awt.Dimension(200, 26));
        genreField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                genreFieldKeyReleased(evt);
            }
        });

        track1Field.setEditable(false);
        track1Field.setMaximumSize(new java.awt.Dimension(30, 26));
        track1Field.setMinimumSize(new java.awt.Dimension(30, 26));
        track1Field.setName("track1"); // NOI18N
        track1Field.setNextFocusableComponent(track2Field);
        track1Field.setPreferredSize(new java.awt.Dimension(30, 26));
        track1Field.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                track1FieldKeyReleased(evt);
            }
        });

        track2Field.setEditable(false);
        track2Field.setMaximumSize(new java.awt.Dimension(30, 26));
        track2Field.setMinimumSize(new java.awt.Dimension(30, 26));
        track2Field.setName("track2"); // NOI18N
        track2Field.setNextFocusableComponent(disk1Field);
        track2Field.setPreferredSize(new java.awt.Dimension(30, 26));
        track2Field.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                track2FieldKeyReleased(evt);
            }
        });

        disk1Field.setEditable(false);
        disk1Field.setMaximumSize(new java.awt.Dimension(30, 26));
        disk1Field.setMinimumSize(new java.awt.Dimension(30, 26));
        disk1Field.setName("disk1"); // NOI18N
        disk1Field.setNextFocusableComponent(disk2Field);
        disk1Field.setPreferredSize(new java.awt.Dimension(30, 26));
        disk1Field.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                disk1FieldKeyReleased(evt);
            }
        });

        disk2Field.setEditable(false);
        disk2Field.setMaximumSize(new java.awt.Dimension(30, 26));
        disk2Field.setMinimumSize(new java.awt.Dimension(30, 26));
        disk2Field.setName("disk2"); // NOI18N
        disk2Field.setNextFocusableComponent(commentField);
        disk2Field.setPreferredSize(new java.awt.Dimension(30, 26));
        disk2Field.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                disk2FieldKeyReleased(evt);
            }
        });

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel5.setText("Title:");

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel6.setText("Artist:");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel7.setText("Album:");

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel8.setText("Album Artist:");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel9.setText("Year:");

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel10.setText("Track:");

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel11.setText("Disk:");

        jLabel12.setText("of");

        jLabel13.setText("of");

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel14.setText("Length:");

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel15.setText("Bitrate:");

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel16.setText("Sample rate:");

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel17.setText("Comment:");

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        commentField.setEditable(false);
        commentField.setColumns(20);
        commentField.setRows(5);
        commentField.setName("comment"); // NOI18N
        commentField.setNextFocusableComponent(filenameField);
        commentField.setPreferredSize(new java.awt.Dimension(250, 80));
        commentField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                commentFieldKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                commentFieldKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(commentField);

        lengthField.setEditable(false);
        lengthField.setMaximumSize(new java.awt.Dimension(85, 26));
        lengthField.setMinimumSize(new java.awt.Dimension(85, 26));
        lengthField.setPreferredSize(new java.awt.Dimension(85, 26));

        bitrateField.setEditable(false);
        bitrateField.setMaximumSize(new java.awt.Dimension(85, 26));
        bitrateField.setMinimumSize(new java.awt.Dimension(85, 26));
        bitrateField.setPreferredSize(new java.awt.Dimension(85, 26));

        sampleRateField.setEditable(false);
        sampleRateField.setMaximumSize(new java.awt.Dimension(85, 26));
        sampleRateField.setMinimumSize(new java.awt.Dimension(85, 26));
        sampleRateField.setPreferredSize(new java.awt.Dimension(85, 26));

        editSubmitButton.setText("Edit");
        editSubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSubmitButtonActionPerformed(evt);
            }
        });

        previousButton.setText("<");
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        nextButton.setText(">");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel18.setText("Genre:");

        editedLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(albumArtistField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(albumField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(artistField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(titleField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(filenameField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(previousButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(editedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(editSubmitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(genreField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(yearField, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(6, 6, 6)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(track1Field, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(disk1Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel13)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(disk2Field, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(jLabel12)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(track2Field, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lengthField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(bitrateField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(sampleRateField, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(115, 115, 115))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(coverLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(filenameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(artistField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(albumField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(albumArtistField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(yearField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(genreField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(track1Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(track2Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(disk1Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(disk2Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(lengthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(bitrateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(sampleRateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(coverLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(editSubmitButton)
                        .addComponent(previousButton)
                        .addComponent(nextButton))
                    .addComponent(editedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Handles the next button press. Goes to the next song if there is one
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        if (editSubmitButton.getText().equals("Submit")) {
            this.editModeEnabled = true;
        }
        submit();
        this.dispose();
        Moose.getFrame().nextFromInfoFrame(this.editModeEnabled, lastEditedField);
    }//GEN-LAST:event_nextButtonActionPerformed

    /**
     * Handles the previous button press. Goes to the previous song if there is one
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        if (editSubmitButton.getText().equals("Submit")) {
            this.editModeEnabled = true;
        }
        submit();
        this.dispose();
        Moose.getFrame().previousFromInfoFrame(this.editModeEnabled, lastEditedField);
    }//GEN-LAST:event_previousButtonActionPerformed

    /**
     * Handles the edit or submit button press. Checks if we're editing or submitting and does the action based on that.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void editSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSubmitButtonActionPerformed
        if(editSubmitButton.getText().equals("Edit")) {
            editSubmitButton.setText("Submit");
            setFieldsEditable(true);
            this.editModeEnabled = true;
        } else if (editSubmitButton.getText().equals("Submit")) {
            editSubmitButton.setText("Edit");
            setFieldsEditable(false);
            submit();
            this.editModeEnabled = false;
        }
    }//GEN-LAST:event_editSubmitButtonActionPerformed

    /**
     * Handles the window closing action
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        setFieldsEditable(false);
        Moose.getFrame().setEnabled(true);
    }//GEN-LAST:event_formWindowClosed

    private void filenameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filenameFieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        }
        if (!originalValues.get(0).equals(filenameField.getText())) {
            edited = true;
        }
        lastEditedField = filenameField;
    }//GEN-LAST:event_filenameFieldKeyReleased

    private void titleFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_titleFieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        }
        if (!originalValues.get(1).equals(titleField.getText())) {
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
        }
        lastEditedField = titleField;
    }//GEN-LAST:event_titleFieldKeyReleased

    private void artistFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_artistFieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        }
        if (!originalValues.get(2).equals(artistField.getText())) {
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
        }
        lastEditedField = artistField;
    }//GEN-LAST:event_artistFieldKeyReleased

    private void albumFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_albumFieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        }
        if (!originalValues.get(3).equals(albumField.getText())) {
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
        }
        lastEditedField = albumField;
    }//GEN-LAST:event_albumFieldKeyReleased

    private void albumArtistFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_albumArtistFieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        }
        if (!originalValues.get(4).equals(albumArtistField.getText())) {
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
        }
        lastEditedField = albumArtistField;
    }//GEN-LAST:event_albumArtistFieldKeyReleased

    private void yearFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_yearFieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        }
        if (!originalValues.get(5).equals(yearField.getText())) {
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
        }
        lastEditedField = yearField;
    }//GEN-LAST:event_yearFieldKeyReleased

    private void genreFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_genreFieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        }
        if (!originalValues.get(6).equals(genreField.getText())) {
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
        }
        lastEditedField = genreField;
    }//GEN-LAST:event_genreFieldKeyReleased

    private void track1FieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_track1FieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        }
        if (!originalValues.get(7).equals(track1Field.getText())) {
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
        }
        lastEditedField = track1Field;
    }//GEN-LAST:event_track1FieldKeyReleased

    private void track2FieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_track2FieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        }
        if (!originalValues.get(8).equals(track2Field.getText())) {
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
        }
        lastEditedField = track2Field;
    }//GEN-LAST:event_track2FieldKeyReleased

    private void disk1FieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_disk1FieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        }
        if (!originalValues.get(9).equals(disk1Field.getText())) {
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
        }
        lastEditedField = disk1Field;
    }//GEN-LAST:event_disk1FieldKeyReleased

    private void disk2FieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_disk2FieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        }
        if (!originalValues.get(10).equals(disk2Field.getText())) {
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
        }
        lastEditedField = disk2Field;
    }//GEN-LAST:event_disk2FieldKeyReleased

    private void commentFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_commentFieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            editSubmitButton.doClick();
        } else if (!originalValues.get(11).equals(commentField.getText())) {
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
        }
        lastEditedField = commentField;
    }//GEN-LAST:event_commentFieldKeyReleased

    private void commentFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_commentFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_TAB) {
            commentField.transferFocus();
            evt.consume();
        }
    }//GEN-LAST:event_commentFieldKeyPressed

    private void coverLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_coverLabelMouseClicked
        if (this.editModeEnabled) {
            ViewUtils.showPopUpContextMenu(evt, menuListener,  false, false, true, false, new String[] {AUTO_ARTWORK});
        }
    }//GEN-LAST:event_coverLabelMouseClicked

    /**
     * Gets the album art from the table and puts it on the more info frame
     */
    public void addAlbumArt() {
        File startingPoint = FileUtils.getStartingPoint(songs.values().stream().map(Song::getFile).collect(Collectors.toList()));
        File image = ImageUtils.selectAlbumArt(startingPoint);
        if (image != null) {
            byte[] bytes = ImageUtils.getBytesFromFile(image);
            Icon artwork_icon = ImageUtils.getScaledImage(bytes, 290);
            coverLabel.setIcon(artwork_icon);
            edited = true;
            editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
            newArtwork = bytes;
        }
    }
    
    /**
     * Removes the album art from the label on the more info frame
     */
     public void removeAlbumArt() {
         coverLabel.setIcon(null);
         edited = true;
         editedLabel.setIcon(IconUtils.get(IconUtils.EDITED));
     }
    
    /**
     * Goes to the next song
     */
    public void next() {
        if (editSubmitButton.getText().equals("Submit")) {
            this.editModeEnabled = true;
        }
        submit();
        this.dispose();
        Moose.getFrame().nextFromInfoFrame(this.editModeEnabled, lastEditedField);
    }

    /**
     * Sets the navigation buttons based on the row selected in the table
     */
    public void setNavigationButtons() {
        if (this.selectedRows.length == 1) {
            int row = selectedRows[0];
            if (row == Moose.getFrame().table.getRowCount() - 1) {
                previousButton.setEnabled(true);
                nextButton.setEnabled(false);
            } else if (row == 0) {
                previousButton.setEnabled(false);
                nextButton.setEnabled(true);
            }
        } else {
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
        }
    }
    
    /**
     * Sets the fields on the UI
     * @param songs, a list of songs with data to set
     */
    public void setFields(List<Song> songs) {
        if (songs.size() == 1) {
            Song song = songs.get(0);
            filenameField.setText(song.getFile().getName());
            titleField.setText(song.getTitle());
            artistField.setText(song.getArtist());
            albumField.setText(song.getAlbum());
            albumArtistField.setText(song.getAlbumArtist());
            yearField.setText(song.getYear());
            genreField.setText(song.getGenre());
            track1Field.setText(song.getTrack());
            track2Field.setText(song.getTotalTracks());
            disk1Field.setText(song.getDisk());
            disk2Field.setText(song.getTotalDisks());
            lengthField.setText(song.getLength());
            bitrateField.setText(song.getBitrate());
            sampleRateField.setText(song.getSampleRate());
            commentField.setText(song.getComment());
            coverLabel.setIcon(ImageUtils.getScaledImage(song.getArtwork_bytes(), 290));

        } else {

            // make the lists of values
            List<String> titles = new ArrayList<>();
            List<String> artists = new ArrayList<>();
            List<String> albums = new ArrayList<>();
            List<String> albumArtists = new ArrayList<>();
            List<String> genres = new ArrayList<>();
            List<String> years = new ArrayList<>();
            List<String> tracks = new ArrayList<>();
            List<String> trackTotals = new ArrayList<>();
            List<String> disks = new ArrayList<>();
            List<String> diskTotals = new ArrayList<>();
            List<String> lengths = new ArrayList<>();
            List<String> bitRates = new ArrayList<>();
            List<String> sampleRates = new ArrayList<>();
            List<String> comments = new ArrayList<>();
            List<byte[]> images = new ArrayList<>();

            // fill the lists
            for (Song s : songs) {
                titles.add(s.getTitle());
                artists.add(s.getArtist());
                albums.add(s.getAlbum());
                albumArtists.add(s.getAlbumArtist());
                years.add(s.getYear());
                tracks.add(s.getTrack());
                trackTotals.add(s.getTotalTracks());
                disks.add(s.getDisk());
                diskTotals.add(s.getTotalDisks());
                genres.add(s.getGenre());
                lengths.add(s.getLength());
                bitRates.add(s.getBitrate());
                sampleRates.add(s.getSampleRate());
                comments.add(s.getComment());
                images.add(s.getArtwork_bytes());
            }

            // set the fields
            filenameField.setText(Constants.DASH);
            filenameField.setEnabled(false);
            titleField.setText(StringUtils.checkIfSame(titles.get(0), tracks) ? titles.get(0) : Constants.DASH);
            artistField.setText(StringUtils.checkIfSame(artists.get(0), artists) ? artists.get(0) : Constants.DASH);
            albumField.setText(StringUtils.checkIfSame(albums.get(0), albums) ? albums.get(0) : Constants.DASH);
            albumArtistField.setText(StringUtils.checkIfSame(albumArtists.get(0), albumArtists) ? albumArtists.get(0) : Constants.DASH);
            yearField.setText(StringUtils.checkIfSame(years.get(0), years) ? years.get(0) : Constants.DASH);
            genreField.setText(StringUtils.checkIfSame(genres.get(0), genres) ? genres.get(0) : Constants.DASH);
            track1Field.setText(StringUtils.checkIfSame(tracks.get(0), tracks) ? tracks.get(0) : Constants.DASH);
            track2Field.setText(StringUtils.checkIfSame(trackTotals.get(0), trackTotals) ? trackTotals.get(0) : Constants.DASH);
            disk1Field.setText(StringUtils.checkIfSame(disks.get(0), disks) ? disks.get(0) : Constants.DASH);
            disk2Field.setText(StringUtils.checkIfSame(diskTotals.get(0), diskTotals) ? diskTotals.get(0) : Constants.DASH);
            lengthField.setText(StringUtils.checkIfSame(lengths.get(0), lengths) ? lengths.get(0) : Constants.DASH);
            bitrateField.setText(StringUtils.checkIfSame(bitRates.get(0), bitRates) ? bitRates.get(0) : Constants.DASH);
            sampleRateField.setText(StringUtils.checkIfSame(sampleRates.get(0), sampleRates) ? sampleRates.get(0) : Constants.DASH);
            commentField.setText(StringUtils.checkIfSame(comments.get(0), comments) ? comments.get(0) : Constants.DASH);

            if (ImageUtils.checkIfSame(images.get(0), images) && images.get(0) != null) {
                coverLabel.setIcon(ImageUtils.getScaledImage(images.get(0), 290));
                originalArtwork = newArtwork = images.get(0);
            } else {
                List<byte[]> bytesList = ImageUtils.getUniqueByteArrays(images);
                coverLabel.setIcon(new ImageIcon(ImageUtils.combineImages(bytesList, 290)));
            }
        }

        // store the fields' original values to determine if the form was edited
        originalValues.addAll(Arrays.asList(
                filenameField.getText(),
                titleField.getText(),
                artistField.getText(),
                albumField.getText(),
                albumArtistField.getText(),
                yearField.getText(),
                genreField.getText(),
                track1Field.getText(),
                track2Field.getText(),
                disk1Field.getText(),
                disk2Field.getText(),
                commentField.getText()
        ));
    }
    
    /**
     * Sets all the fields editable
     * @param bool, the boolean to set the fields editable to
     */
    public void setFieldsEditable(boolean bool) {
        filenameField.setEditable(bool);
        titleField.setEditable(bool);
        artistField.setEditable(bool);
        albumField.setEditable(bool);
        albumArtistField.setEditable(bool);
        yearField.setEditable(bool);
        genreField.setEditable(bool);
        track1Field.setEditable(bool);
        track2Field.setEditable(bool);
        disk1Field.setEditable(bool);
        disk2Field.setEditable(bool);
        lengthField.setEditable(bool);
        bitrateField.setEditable(bool);
        sampleRateField.setEditable(bool);
        commentField.setEditable(bool);
    }
    
    /**
     * Submits the changes of the editing
     */
    public void submit() {
        if (edited) {

            JTable table = Moose.getFrame().table;

            for (Integer row : songs.keySet()) {

                // check if we need to create a new file
                File oldFile = (File) table.getModel().getValueAt(table.convertRowIndexToModel(row), 1);
                File newFile = null;
                if (!filenameField.getText().equals(DASH) &&
                        !originalValues.get(0).equals(table.getValueAt(row, 1))) {
                    newFile = MP3FileUtils.getNewMP3FileFromOld(oldFile, filenameField.getText());
                }

                // create a new song with the file(s)
                Song song = new Song(oldFile, newFile);

                // set the values of the song object
                // note that if the value in the text field matches the original value, we'll send back null for that field
                song.setTitle(titleField.getText().equals(originalValues.get(1)) ? null : titleField.getText());
                song.setArtist(artistField.getText().equals(originalValues.get(2)) ? null : artistField.getText());
                song.setAlbum(albumField.getText().equals(originalValues.get(3)) ? null : albumField.getText());
                song.setAlbumArtist(albumArtistField.getText().equals(originalValues.get(4)) ? null : albumArtistField.getText());
                song.setYear(yearField.getText().equals(originalValues.get(5)) ? null : yearField.getText());
                song.setGenre(genreField.getText().equals(originalValues.get(6)) ? null : genreField.getText());
                if (!track1Field.getText().equals(originalValues.get(7)) || !track2Field.getText().equals(originalValues.get(8))) {
                    String formattedTracks = table.getValueAt(row, TABLE_COLUMN_TRACK).toString();
                    String[] arr = formattedTracks.split("/");
                    String track = arr[0];
                    String totalTracks = arr[1];
                    song.setTrack(track1Field.getText().equals(originalValues.get(7)) ? track : track1Field.getText());
                    song.setTotalTracks(track2Field.getText().equals(originalValues.get(8)) ? totalTracks : track2Field.getText());
                }
                if (!disk1Field.getText().equals(originalValues.get(9)) || !disk2Field.getText().equals(originalValues.get(10))) {
                    String formattedDisks = table.getValueAt(row, TABLE_COLUMN_DISK).toString();
                    String[] arr = formattedDisks.split("/");
                    String disk = arr[0];
                    String totalDisks = arr[1];
                    song.setDisk(disk1Field.getText().equals(originalValues.get(9)) ? disk : disk1Field.getText());
                    song.setTotalDisks(disk2Field.getText().equals(originalValues.get(10)) ? totalDisks : disk2Field.getText());
                }
                song.setComment(commentField.getText().equals(originalValues.get(11)) ? null : commentField.getText());
                if (!Arrays.equals(newArtwork, originalArtwork)) {
                    song.setArtwork_bytes(newArtwork);
                }

                // submit sequentially
                Moose.getFrame().submitRowChanges(row, song);
            }
        }
        this.edited = false;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField albumArtistField;
    private javax.swing.JTextField albumField;
    private javax.swing.JTextField artistField;
    private javax.swing.JTextField bitrateField;
    private javax.swing.JTextArea commentField;
    private javax.swing.JLabel coverLabel;
    private javax.swing.JTextField disk1Field;
    private javax.swing.JTextField disk2Field;
    private javax.swing.JButton editSubmitButton;
    private javax.swing.JLabel editedLabel;
    private javax.swing.JTextField filenameField;
    private javax.swing.JTextField genreField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField lengthField;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    private javax.swing.JTextField sampleRateField;
    private javax.swing.JTextField titleField;
    private javax.swing.JTextField track1Field;
    private javax.swing.JTextField track2Field;
    private javax.swing.JTextField yearField;
    // End of variables declaration//GEN-END:variables
}
