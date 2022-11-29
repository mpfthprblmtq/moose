/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mpfthprblmtq.moose.views.modals;

import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.services.AutoMoveService;
import com.mpfthprblmtq.moose.services.FilenameFormatterService;
import com.mpfthprblmtq.moose.utilities.Constants;
import com.mpfthprblmtq.moose.utilities.SongUtils;
import com.mpfthprblmtq.moose.utilities.viewUtils.DialogUtils;
import com.sun.tools.internal.jxc.ap.Const;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mpfthprblmtq.moose.utilities.Constants.TRACKNUM_ARTIST_TITLE_OPT_REGEX;

/**
 * @author mpfthprblmtq
 */
public class AutoMoveFrame extends javax.swing.JFrame {

    // values that are changed and reflected in the path
    String filename;
    String title;
    String artist;
    String album;
    String albumArtist;
    String year;
    String genre;

    // boolean values to see if there's only one file or if all files come from the same parent file
    boolean singleFile;
    boolean commonParentFile;

    // boolean value to see if we're in a label
    boolean isLabel;

    // list of songs and rows to update
    List<Song> songs;
    int[] selectedRows;

    // service
    AutoMoveService autoMoveService = new AutoMoveService();
    FilenameFormatterService filenameFormatterService = new FilenameFormatterService(Moose.getSongController());

    /**
     * Creates new form AutoMoveFrame
     */
    public AutoMoveFrame(List<Song> songs, int[] selectedRows) {
        initComponents();

        // set fields
        this.songs = songs;
        this.selectedRows = selectedRows;

        pathTextArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        pathTextArea.setFont(new java.awt.Font("Lucida Grande", Font.PLAIN, 12));
        setFields();

        // if we're messing with not a single file, then disable the track title and filename fields
        if (!singleFile) {
            filenameLabel.setEnabled(false);
            filenameField.setEnabled(false);
            titleLabel.setEnabled(false);
            titleField.setEnabled(false);
        }
    }

    /**
     * Sets the text fields to either "-" or the value
     */
    private void setFields() {
        singleFile = songs.size() == 1;

        genre = StringUtils.checkIfSame(songs.get(0).getGenre(),
                songs.stream().map(Song::getGenre).collect(Collectors.toList())) ?
                songs.get(0).getGenre() : Constants.DASH;

        // set the common file field
        setCommonParentFile();

        // set the fields' values
        filenameField.setText(getFilename());
        titleField.setText(getTrackTitle());
        artistField.setText(getArtist());
        albumField.setText(getAlbum());
        albumArtistField.setText(getAlbumArtist());
        yearField.setText(getYear());
        genreField.setText(genre);

        updatePath(null);
    }

    /**
     * Sets the common parent file field by checking each parent file of all songs given
     */
    private void setCommonParentFile() {
        // create a list of files to check
        List<File> files = songs
                .stream()
                .map(song -> song.getNewFile() == null ? song.getFile() : song.getNewFile())
                .collect(Collectors.toList());

        // check and see if the parent file is the same for all files
        String compareTo = files.get(0).getParentFile().getName();
        for (File file : files) {
            if (!file.getParentFile().getName().equals(compareTo)) {
                // if we're in this if block, then the parent file name is not the same for all files, return album
                commonParentFile = false;
                return;
            }
        }

        // if we're here, then the parent file for all songs is the same
        commonParentFile = true;
    }

    private String getFilename() {
        // format the filename
        if (singleFile) {
            return filename = filenameFormatterService
                    .formatFilename(songs.get(0).getFile(), true)
                    .replace(".mp3", StringUtils.EMPTY);
        } else {
            return filename = StringUtils.checkIfSame(songs.get(0).getFile().getName(),
                    songs.stream().map(song -> song.getFile().getName()).collect(Collectors.toList())) ?
                    songs.get(0).getFile().getName().replace(".mp3", StringUtils.EMPTY) : Constants.DASH;
        }
    }

    private String getTrackTitle() {
        title = StringUtils.checkIfSame(songs.get(0).getTitle(),
                songs.stream().map(Song::getTitle).collect(Collectors.toList())) ?
                songs.get(0).getTitle() : Constants.DASH;

        if (StringUtils.isEmpty(title)) {
            Pattern pattern = Pattern.compile("(?<trackNumber>\\d{2,} ).*");
            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                return title = filename.replace(matcher.group("trackNumber"), StringUtils.EMPTY);
            } else {
                return filename;
            }
        }

        return title;
    }

    /**
     * Gets the album by looking at ID3 information and checking the parent file across all given songs
     *
     * @return an album name
     */
    private String getAlbum() {
        // get the main album from existing ID3 tags
        album = StringUtils.checkIfSame(songs.get(0).getAlbum(),
                songs.stream().map(Song::getAlbum).collect(Collectors.toList())) ?
                songs.get(0).getAlbum() : Constants.DASH;

        // if we don't have a common parent file, then just return the album
        if (!commonParentFile) {
            return album;
        }

        // if we're messing with a single file, then set the album to the title
        if (singleFile) {
            return album = title;
        }

        // if we're here, then the parent file is equal across all songs, so let's check the parent file to see if it matches a certain regex
        File file = songs.get(0).getNewFile() == null ? songs.get(0).getFile() : songs.get(0).getNewFile();
        String directoryName = file.getParentFile().getName();

        // check to see if we can get rid of a year
        Pattern pattern = Pattern.compile(".*(?<year>(\\[|\\()\\d{4}(\\]|\\))).*");
        Matcher matcher = pattern.matcher(directoryName);
        if (matcher.find()) {
            directoryName = directoryName.replace(matcher.group("year"), StringUtils.EMPTY).trim();
        }
        // check to see if the directory name matches Artist - Album regex and pull it from there
        pattern = Pattern.compile("(?<artist>.*) - (?<album>.*)");
        matcher = pattern.matcher(directoryName);
        if (matcher.find() && (StringUtils.isEmpty(album) || album.equals(Constants.DASH))) {
            return album = matcher.group("album");
        }

        // if we're here, then the parent file is equal across all songs, and the name of that directory doesn't match the ".* - .*" regex
        // so let's just return the parent file's name
        return StringUtils.isNotEmpty(album) ? album : file.getParentFile().getName();
    }

    private String getAlbumArtist() {
        albumArtist = StringUtils.checkIfSame(songs.get(0).getAlbumArtist(),
                songs.stream().map(Song::getAlbumArtist).collect(Collectors.toList())) ?
                songs.get(0).getAlbumArtist() : Constants.DASH;

        return StringUtils.isNotEmpty(albumArtist) ? albumArtist : artist;
    }

    /**
     * Gets the year by looking at ID3 information and checking the parent file across all given songs
     *
     * @return a year
     */
    private String getYear() {
        year = StringUtils.checkIfSame(songs.get(0).getYear(),
                songs.stream().map(Song::getYear).collect(Collectors.toList())) ?
                songs.get(0).getYear() : Constants.DASH;

        // if we don't have a common parent file, then just return the year
        if (!commonParentFile) {
            return year;
        }

        // if we're here, then the parent file is equal across all songs, so let's check the parent file
        File file = songs.get(0).getNewFile() == null ? songs.get(0).getFile() : songs.get(0).getNewFile();
        String directoryName = file.getParentFile().getName();
        Pattern pattern = Pattern.compile(".*\\((?<year>\\d{4})\\).*");
        Matcher matcher = pattern.matcher(directoryName);
        if (matcher.find() && (StringUtils.isEmpty(year) || year.equals(Constants.DASH))) {
            return year = matcher.group("year");
        }

        pattern = Pattern.compile(".*\\[(?<year>\\d{4})].*");
        matcher = pattern.matcher(directoryName);
        if (matcher.find() && (StringUtils.isEmpty(year) || year.equals(Constants.DASH))) {
            return year = matcher.group("year");
        }

        // if we're here, then we couldn't find it from the parent file, just return the year or the current year
        return StringUtils.isEmpty(year) ? String.valueOf(LocalDate.now().getYear()) : year;

    }

    /**
     * Gets the artist by looking at ID3 information and checking the parent file across all given songs
     *
     * @return an artist
     */
    private String getArtist() {
        artist = StringUtils.checkIfSame(songs.get(0).getArtist(),
                songs.stream().map(Song::getArtist).collect(Collectors.toList())) ?
                songs.get(0).getArtist() : Constants.DASH;

        // if we don't have a common parent file, then just return the year
        if (!commonParentFile) {
            return artist;
        }

        // if we're here, then the parent file is equal across all songs, so let's check the parent file
        File file = songs.get(0).getNewFile() == null ? songs.get(0).getFile() : songs.get(0).getNewFile();
        String directoryName = file.getParentFile().getName();
        Pattern pattern = Pattern.compile("(?<artist>.*) - (?<album>.*)");
        Matcher matcher = pattern.matcher(directoryName);
        if (matcher.find() && (StringUtils.isEmpty(artist) || artist.equals(Constants.DASH))) {
            return artist = matcher.group("artist");
        }

        // if we're here, then we couldn't find it from the parent file, let's try and find it from the file if it's a single
        if (singleFile) {
            pattern = Pattern.compile("(?<trackNumber>\\d{2,} )(?<artist>.*) - (?<album>.*)");
            matcher = pattern.matcher(filename);
            if (matcher.find() && (StringUtils.isEmpty(artist) || artist.equals(Constants.DASH))) {
                return artist = matcher.group("artist");
            }
        }

        // if we're here, then we couldn't find it from the parent file, just return the artist
        return artist;
    }

    /**
     * Updates the path label with the results from the user entering them in the text fields
     */
    private void updatePath(Component c) {

        String path;
        if (isLabel) {
            if (StringUtils.equals(getAlbumType(), "Single")) {
                // single albums
                path = validateAlbumArtist(c)
                        + "/" + validateAlbumType(c)
                        + "/" + validateGenre(c)
                        + "/" + validateYear(c) + validateArtist(c) + " - " + validateAlbum(c)
                        + "/" + validateFilenameTitle(c);
            } else if (StringUtils.equals(getAlbumType(), "Compilation")) {
                // compilation albums
                path = validateAlbumArtist(c)
                        + "/" + validateAlbumType(c)
                        + "/" + validateYear(c) + validateAlbum(c)
                        + "/" + validateFilenameTitle(c);
            } else {
                // eps and lps
                path = validateAlbumArtist(c)
                        + "/" + validateAlbumType(c)
                        + "/" + validateYear(c) + validateArtist(c) + " - " + validateAlbum(c)
                        + "/" + validateFilenameTitle(c);
            }
        } else {
            path = validateAlbumArtist(c)
                    + "/" + validateYear(c) + validateAlbum(c)
                    + "/" + validateFilenameTitle(c);
        }
        pathTextArea.setText(Moose.getSettings().getLibraryLocation() + path);
    }

    private String validateFilenameTitle(Component c) {
        if (singleFile) {
            return c instanceof JTextField &&
                    (c.getName().equals("filename") || c.getName().equals("title")) ?
                    "<b>" + filename + ".mp3</b>" : filename + ".mp3";
        }
        return StringUtils.EMPTY;
    }

    private String validateArtist(Component c) {
        return c instanceof JTextField && c.getName().equals("artist") ? "<b>" + artist + "</b>" : artist;
    }

    private String validateAlbum(Component c) {
        return c instanceof JTextField && c.getName().equals("album") ? "<b>" + album + "</b>" : album;
    }

    private String validateAlbumArtist(Component c) {
        return c instanceof JTextField && c.getName().equals("albumArtist") ? "<b>" + albumArtist + "</b>" : albumArtist;
    }

    private String validateYear(Component c) {
        return c instanceof JTextField && c.getName().equals("year") ? "<b>[" + year + "]</b> " : "[" + year + "] ";
    }

    private String validateGenre(Component c) {
        return c instanceof JTextField && c.getName().equals("genre") ? "<b>" + genre + "</b>" : genre;
    }

    private String validateAlbumType(Component c) {
        return c instanceof JComboBox ?
                "<b>" + mapAlbumType(getAlbumType()) + "</b>" :
                mapAlbumType(getAlbumType());
    }

    private String mapAlbumType(String albumType) {
        switch (albumType) {
            case "Single":
                return "Singles";
            case "Album":
                return "LPs";
            case "EP":
                return "EPs";
            case "Compilation":
                return "Compilations";
            default:
                return StringUtils.EMPTY;
        }
    }

    private void updateFilenameAndTitleFields(Component c) {
        if (c instanceof JTextField && c.getName().equals("title")) {
            // event came from title field, update filename field
            Pattern pattern = Pattern.compile("(?<trackNumber>\\d{2,} ).*");
            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                filenameField.setText(matcher.group("trackNumber") + title);
                filename = filenameField.getText();
            }
        } else {
            // event came from filename field, update title field
            Pattern pattern = Pattern.compile("(?<trackNumber>\\d{2,} ).*");
            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                titleField.setText(filename.replace(matcher.group("trackNumber"), StringUtils.EMPTY));
                title = titleField.getText();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filenameField = new javax.swing.JTextField();
        titleField = new javax.swing.JTextField();
        artistField = new javax.swing.JTextField();
        albumField = new javax.swing.JTextField();
        albumArtistField = new javax.swing.JTextField();
        yearField = new javax.swing.JTextField();
        genreField = new javax.swing.JTextField();
        moveButton = new javax.swing.JButton();
        filenameLabel = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        artistLabel = new javax.swing.JLabel();
        albumLabel = new javax.swing.JLabel();
        albumArtistLabel = new javax.swing.JLabel();
        yearLabel = new javax.swing.JLabel();
        genreLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        autoTagOnMoveCheckBox = new javax.swing.JCheckBox();
        albumTypeLabel = new javax.swing.JLabel();
        albumTypeComboBox = new javax.swing.JComboBox<>();
        jSeparator2 = new javax.swing.JSeparator();
        partOfLabelLabel = new javax.swing.JLabel();
        partOfLabelCheckBox = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        pathTextArea = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        filenameField.setMaximumSize(new java.awt.Dimension(200, 26));
        filenameField.setMinimumSize(new java.awt.Dimension(200, 26));
        filenameField.setName("filename"); // NOI18N
        filenameField.setPreferredSize(new java.awt.Dimension(200, 26));
        filenameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                filenameFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                filenameFieldFocusLost(evt);
            }
        });
        filenameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filenameFieldKeyReleased(evt);
            }
        });

        titleField.setMaximumSize(new java.awt.Dimension(200, 26));
        titleField.setMinimumSize(new java.awt.Dimension(200, 26));
        titleField.setName("title"); // NOI18N
        titleField.setPreferredSize(new java.awt.Dimension(200, 26));
        titleField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                titleFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                titleFieldFocusLost(evt);
            }
        });
        titleField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                titleFieldKeyReleased(evt);
            }
        });

        artistField.setMaximumSize(new java.awt.Dimension(200, 26));
        artistField.setMinimumSize(new java.awt.Dimension(200, 26));
        artistField.setName("artist"); // NOI18N
        artistField.setPreferredSize(new java.awt.Dimension(200, 26));
        artistField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                artistFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                artistFieldFocusLost(evt);
            }
        });
        artistField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                artistFieldKeyReleased(evt);
            }
        });

        albumField.setMaximumSize(new java.awt.Dimension(200, 26));
        albumField.setMinimumSize(new java.awt.Dimension(200, 26));
        albumField.setName("album"); // NOI18N
        albumField.setPreferredSize(new java.awt.Dimension(200, 26));
        albumField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                albumFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                albumFieldFocusLost(evt);
            }
        });
        albumField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                albumFieldKeyReleased(evt);
            }
        });

        albumArtistField.setMaximumSize(new java.awt.Dimension(200, 26));
        albumArtistField.setMinimumSize(new java.awt.Dimension(200, 26));
        albumArtistField.setName("albumArtist"); // NOI18N
        albumArtistField.setPreferredSize(new java.awt.Dimension(200, 26));
        albumArtistField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                albumArtistFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                albumArtistFieldFocusLost(evt);
            }
        });
        albumArtistField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                albumArtistFieldKeyReleased(evt);
            }
        });

        yearField.setMaximumSize(new java.awt.Dimension(85, 26));
        yearField.setMinimumSize(new java.awt.Dimension(85, 26));
        yearField.setName("year"); // NOI18N
        yearField.setPreferredSize(new java.awt.Dimension(85, 26));
        yearField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                yearFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                yearFieldFocusLost(evt);
            }
        });
        yearField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                yearFieldKeyReleased(evt);
            }
        });

        genreField.setMaximumSize(new java.awt.Dimension(200, 26));
        genreField.setMinimumSize(new java.awt.Dimension(200, 26));
        genreField.setName("genre"); // NOI18N
        genreField.setPreferredSize(new java.awt.Dimension(200, 26));
        genreField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                genreFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                genreFieldFocusLost(evt);
            }
        });
        genreField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                genreFieldKeyReleased(evt);
            }
        });

        moveButton.setText("Move");
        moveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveButtonActionPerformed(evt);
            }
        });

        filenameLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        filenameLabel.setText("Filename:");

        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        titleLabel.setText("Title:");

        artistLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        artistLabel.setText("Artist:");

        albumLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        albumLabel.setText("Album:");

        albumArtistLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        albumArtistLabel.setText("Album Artist:");

        yearLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        yearLabel.setText("Year:");

        genreLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        genreLabel.setText("Genre:");

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        jLabel1.setText("Auto Move:");

        jLabel2.setText("File(s) will be moved to:");

        statusLabel.setForeground(Constants.RED);

        autoTagOnMoveCheckBox.setSelected(true);

        albumTypeLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        albumTypeLabel.setText("Album Type:");
        albumTypeLabel.setToolTipText("Only applies for labels");
        albumTypeLabel.setEnabled(false);

        albumTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Single", "Album", "EP", "Compilation" }));
        albumTypeComboBox.setToolTipText("Only applies for labels");
        albumTypeComboBox.setEnabled(false);
        albumTypeComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                albumTypeComboBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                albumTypeComboBoxFocusLost(evt);
            }
        });
        albumTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                albumTypeComboBoxActionPerformed(evt);
            }
        });

        partOfLabelLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        partOfLabelLabel.setText("Part of Label:");
        partOfLabelLabel.setEnabled(false);

        partOfLabelCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                partOfLabelCheckBoxActionPerformed(evt);
            }
        });

        jLabel10.setText("AutoTag on Move:");

        pathTextArea.setEditable(false);
        pathTextArea.setContentType("text/html"); // NOI18N
        pathTextArea.setText("");
        pathTextArea.setToolTipText("");
        jScrollPane2.setViewportView(pathTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2)
                    .addComponent(jSeparator3)
                    .addComponent(jSeparator2)
                    .addComponent(statusLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(filenameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(artistLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(albumLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(albumArtistLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(albumField, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                            .addComponent(albumArtistField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(filenameField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(titleField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(artistField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(yearLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(genreLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(yearField, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(genreField, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(partOfLabelLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                            .addComponent(albumTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(partOfLabelCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(albumTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(autoTagOnMoveCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(moveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filenameLabel)
                    .addComponent(filenameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(artistLabel)
                    .addComponent(artistField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(albumLabel)
                    .addComponent(albumField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(albumArtistLabel)
                    .addComponent(albumArtistField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yearLabel)
                    .addComponent(yearField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(genreLabel)
                    .addComponent(genreField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(partOfLabelCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(partOfLabelLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(albumTypeLabel)
                    .addComponent(albumTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(autoTagOnMoveCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(moveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void filenameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filenameFieldKeyReleased
        filename = filenameField.getText();
        filenameField.setForeground(Constants.BLACK);
        filenameLabel.setForeground(Constants.BLACK);
        updateFilenameAndTitleFields(filenameField);
        updatePath(filenameField);
    }//GEN-LAST:event_filenameFieldKeyReleased

    private void titleFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_titleFieldKeyReleased
        title = titleField.getText();
        titleField.setForeground(Constants.BLACK);
        titleLabel.setForeground(Constants.BLACK);
        updateFilenameAndTitleFields(titleField);
        updatePath(titleField);
    }//GEN-LAST:event_titleFieldKeyReleased

    private void artistFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_artistFieldKeyReleased
        artist = artistField.getText();
        artistField.setForeground(Constants.BLACK);
        artistLabel.setForeground(Constants.BLACK);
        updatePath(artistField);
    }//GEN-LAST:event_artistFieldKeyReleased

    private void albumFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_albumFieldKeyReleased
        album = albumField.getText();
        albumField.setForeground(Constants.BLACK);
        albumLabel.setForeground(Constants.BLACK);
        updatePath(albumField);
    }//GEN-LAST:event_albumFieldKeyReleased

    private void albumArtistFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_albumArtistFieldKeyReleased
        albumArtist = albumArtistField.getText();
        albumArtistField.setForeground(Constants.BLACK);
        albumArtistLabel.setForeground(Constants.BLACK);
        checkIfLabel();
        updatePath(albumArtistField);
    }//GEN-LAST:event_albumArtistFieldKeyReleased

    private void yearFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_yearFieldKeyReleased
        year = yearField.getText();
        yearField.setForeground(Constants.BLACK);
        yearLabel.setForeground(Constants.BLACK);
        updatePath(yearField);
    }//GEN-LAST:event_yearFieldKeyReleased

    private void genreFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_genreFieldKeyReleased
        genre = genreField.getText();
        genreField.setForeground(Constants.BLACK);
        genreLabel.setForeground(Constants.BLACK);
        updatePath(genreField);
    }//GEN-LAST:event_genreFieldKeyReleased

    private void moveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveButtonActionPerformed
        statusLabel.setText(StringUtils.EMPTY);
        move();
    }//GEN-LAST:event_moveButtonActionPerformed

    private void albumTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_albumTypeComboBoxActionPerformed
        updatePath(albumTypeComboBox);
    }//GEN-LAST:event_albumTypeComboBoxActionPerformed

    private void partOfLabelCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_partOfLabelCheckBoxActionPerformed
        isLabel = partOfLabelCheckBox.isSelected();
        handleLabel();
    }//GEN-LAST:event_partOfLabelCheckBoxActionPerformed

    private void filenameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_filenameFieldFocusGained
        updatePath(filenameField);
    }//GEN-LAST:event_filenameFieldFocusGained

    private void filenameFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_filenameFieldFocusLost
        updatePath(null);
    }//GEN-LAST:event_filenameFieldFocusLost

    private void titleFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_titleFieldFocusGained
        updatePath(titleField);
    }//GEN-LAST:event_titleFieldFocusGained

    private void titleFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_titleFieldFocusLost
        updatePath(null);
    }//GEN-LAST:event_titleFieldFocusLost

    private void artistFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_artistFieldFocusGained
        updatePath(artistField);
    }//GEN-LAST:event_artistFieldFocusGained

    private void artistFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_artistFieldFocusLost
        updatePath(null);
    }//GEN-LAST:event_artistFieldFocusLost

    private void albumFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_albumFieldFocusGained
        updatePath(albumField);
    }//GEN-LAST:event_albumFieldFocusGained

    private void albumFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_albumFieldFocusLost
        updatePath(null);
    }//GEN-LAST:event_albumFieldFocusLost

    private void albumArtistFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_albumArtistFieldFocusGained
        updatePath(albumArtistField);
    }//GEN-LAST:event_albumArtistFieldFocusGained

    private void albumArtistFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_albumArtistFieldFocusLost
        updatePath(null);
    }//GEN-LAST:event_albumArtistFieldFocusLost

    private void yearFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_yearFieldFocusGained
        updatePath(yearField);
    }//GEN-LAST:event_yearFieldFocusGained

    private void yearFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_yearFieldFocusLost
        updatePath(null);
    }//GEN-LAST:event_yearFieldFocusLost

    private void genreFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_genreFieldFocusGained
        updatePath(genreField);
    }//GEN-LAST:event_genreFieldFocusGained

    private void genreFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_genreFieldFocusLost
        updatePath(null);
    }//GEN-LAST:event_genreFieldFocusLost

    private void albumTypeComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_albumTypeComboBoxFocusGained
        updatePath(albumTypeComboBox);
    }//GEN-LAST:event_albumTypeComboBoxFocusGained

    private void albumTypeComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_albumTypeComboBoxFocusLost
        updatePath(null);
    }//GEN-LAST:event_albumTypeComboBoxFocusLost

    private void move() {

        // check if we're good to move
        if (!validateFields()) {
            return;
        }

        // get the path
        String path = pathTextArea.getText()
                .replace("<html>", StringUtils.EMPTY)
                .replace("<b>", StringUtils.EMPTY)
                .replace("</b>", StringUtils.EMPTY)
                .replace("</html>", StringUtils.EMPTY)
                .replace("<head>", StringUtils.EMPTY)
                .replace("</head>", StringUtils.EMPTY)
                .replace("<body>", StringUtils.EMPTY)
                .replace("</body>", StringUtils.EMPTY)
                .replace("\n", StringUtils.EMPTY)
                .replace("  ", StringUtils.EMPTY)
                .trim();

        // do the move
        String result = autoMoveService.move(path, songs, autoTagOnMoveCheckBox.isSelected(), genre, selectedRows);

        // do stuff with results
        if (result.startsWith("Couldn't")) {
            statusLabel.setText(result);
            statusLabel.setToolTipText(result);
        } else {
            this.dispose();
            DialogUtils.showMessageDialog(Moose.getFrame(), result, "Auto Move", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String getAlbumType() {
        return String.valueOf(albumTypeComboBox.getSelectedItem());
    }

    private boolean validateFields() {
        // check that all required fields are filled out and meet guidelines
        int invalidFields = 0;
        String message = StringUtils.EMPTY;

        // if we're moving a single file and the filename field is blank
        if (singleFile && StringUtils.isEmpty(filenameField.getText())) {
            invalidFields++;
            message = "Missing required field(s)!";
            filenameLabel.setForeground(Constants.RED);
            filenameField.setForeground(Constants.RED);
        }

        // if we're moving a single file and the title field is blank
        if (singleFile && StringUtils.isEmpty(titleField.getText())) {
            invalidFields++;
            message = "Missing required field(s)!";
            titleLabel.setForeground(Constants.RED);
            titleField.setForeground(Constants.RED);
        }

        // if we're in a label and the album type is EP, LP, or Single and artist is empty
        if (isLabel && (getAlbumType().equals("EP") || getAlbumType().equals("LP") || getAlbumType().equals("Single")) && StringUtils.isEmpty(artistField.getText())) {
            invalidFields++;
            message = "Missing required field(s)!";
            artistLabel.setForeground(Constants.RED);
            artistField.setForeground(Constants.RED);
        }

        // if the album is empty (applies to all scenarios)
        if (StringUtils.isEmpty(albumField.getText())) {
            invalidFields++;
            message = "Missing required field(s)!";
            albumLabel.setForeground(Constants.RED);
            albumField.setForeground(Constants.RED);
        }

        // if the album artist is empty (applies to all scenarios)
        if(StringUtils.isEmpty(albumArtistField.getText())) {
            invalidFields++;
            message = "Missing required field(s)!";
            albumArtistLabel.setForeground(Constants.RED);
            albumArtistField.setForeground(Constants.RED);
        }

        // if the year is empty (applies to all scenarios) or if it isn't 4 digits/characters long
        if (StringUtils.isEmpty(yearField.getText())) {
            invalidFields++;
            message = "Missing required field(s)!";
            yearLabel.setForeground(Constants.RED);
            yearField.setForeground(Constants.RED);
        } else if (!yearField.getText().matches("\\d{4}")) {
            invalidFields++;
            message = "Year is improperly formatted!";
            yearLabel.setForeground(Constants.RED);
            yearField.setForeground(Constants.RED);
        }

        // if the genre field is blank and the album type is single while we're in a label
        if (StringUtils.isEmpty(genre) && isLabel && getAlbumType().equals("Single")) {
            invalidFields++;
            message = "Genre is required for Singles!";
        }
        // if the genre field has a / in it, and the album type is single while we're in a label
        if (StringUtils.isNotEmpty(genre) && isLabel && genre.contains("/") && getAlbumType().equals("Single")) {
            invalidFields++;
            message = "Genre can't have a slash for Singles!";
        }

        // build the message if we have one
        if (invalidFields == 0 && StringUtils.isEmpty(message)) {
            return true;
        } else if (invalidFields == 1) {
            statusLabel.setText(message);
        } else {
            statusLabel.setText(message + " (" + (invalidFields - 1) + " more)");
        }
        return false;
    }

    private void checkIfLabel() {
        if (partOfLabelCheckBox.isSelected()) {
            isLabel = true;
        } else {
            String path = Moose.getSettings().getLibraryLocation() + albumArtist;
            File[] directories = new File(path).listFiles();
            if (directories != null) {
                for (File directory : directories) {
                    if (SongUtils.isPartOfALabel(directory)) {
                        isLabel = true;
                        handleLabel();
                        return;
                    }
                }
            }
            isLabel = false;
        }
        handleLabel();
    }

    private void handleLabel() {
        partOfLabelLabel.setEnabled(isLabel);
        partOfLabelCheckBox.setSelected(isLabel);
        albumTypeLabel.setEnabled(isLabel);
        albumTypeComboBox.setEnabled(isLabel);
        updatePath(null);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField albumArtistField;
    private javax.swing.JLabel albumArtistLabel;
    private javax.swing.JTextField albumField;
    private javax.swing.JLabel albumLabel;
    private javax.swing.JComboBox<String> albumTypeComboBox;
    private javax.swing.JLabel albumTypeLabel;
    private javax.swing.JTextField artistField;
    private javax.swing.JLabel artistLabel;
    private javax.swing.JCheckBox autoTagOnMoveCheckBox;
    private javax.swing.JTextField filenameField;
    private javax.swing.JLabel filenameLabel;
    private javax.swing.JTextField genreField;
    private javax.swing.JLabel genreLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton moveButton;
    private javax.swing.JCheckBox partOfLabelCheckBox;
    private javax.swing.JLabel partOfLabelLabel;
    private javax.swing.JEditorPane pathTextArea;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTextField titleField;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField yearField;
    private javax.swing.JLabel yearLabel;
    // End of variables declaration//GEN-END:variables
}
