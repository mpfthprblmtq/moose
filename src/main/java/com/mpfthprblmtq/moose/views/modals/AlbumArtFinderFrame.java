/*
 *  Proj:   Moose
 *  File:   AlbumArtFinderFrame.java
 *  Desc:   UI class responsible for the album art finder
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.views.modals;

// imports
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import com.mpfthprblmtq.commons.logger.Logger;
import com.mpfthprblmtq.commons.utils.StringUtils;
import com.mpfthprblmtq.commons.utils.WebUtils;
import com.mpfthprblmtq.moose.Moose;
import com.mpfthprblmtq.moose.objects.Song;
import com.mpfthprblmtq.moose.objects.api.imageSearch.ImageSearchQuery;
import com.mpfthprblmtq.moose.objects.api.imageSearch.ImageSearchResult;
import com.mpfthprblmtq.moose.objects.api.spotify.Album;
import com.mpfthprblmtq.moose.objects.api.spotify.AlbumSearchResponse;
import com.mpfthprblmtq.moose.objects.api.spotify.Artist;
import com.mpfthprblmtq.moose.services.GoogleSearchService;
import com.mpfthprblmtq.moose.services.SpotifyApiService;
import com.mpfthprblmtq.moose.utilities.Constants;
import com.mpfthprblmtq.moose.utilities.IconUtils;
import com.mpfthprblmtq.moose.utilities.ImageUtils;

// class AlbumArtFinderFrame
@SuppressWarnings("FieldCanBeLocal")    // for NetBeans' field declaration at bottom of class
public class AlbumArtFinderFrame extends javax.swing.JFrame {

    static Logger logger = Moose.getLogger();

    // main query field
    ImageSearchQuery query;

    // graphics fields
    List<Icon> icons = new ArrayList<>();
    int currentIconIndex;

    // spotify fields
    String spotifyArtistId = StringUtils.EMPTY;
    int currentSpotifyArtistIconIndex;
    List<Artist> spotifyArtists = new ArrayList<>();
    List<Album> spotifyAlbums = new ArrayList<>();

    // google fields
    List<ImageSearchResult> googleImageSearchResults = new ArrayList<>();

    // services
    GoogleSearchService googleSearchService = new GoogleSearchService();
    SpotifyApiService spotifyApiService = new SpotifyApiService();

    File dir;
    List<Integer> rows;

    BufferedImage foundCover;

    /**
     * Creates new form AlbumArtFinderFrame
     * @param query the ImageSearchQuery to search by
     */
    public AlbumArtFinderFrame(ImageSearchQuery query) {
        // init the components
        initComponents();

        // set the fields
        this.query = query;
        this.dir = query.getDir();
        this.rows = query.getRows();

        // set starting values for the header and tabs
        headerLabel.setText("<html>Finding album art for" +
                ((StringUtils.isEmpty(query.getArtist()) && StringUtils.isEmpty(query.getAlbum())) ?
                        " " + query.getRows().size() + " row(s)" :
                        ":<br>\"" + query.getArtist() + " - " + query.getAlbum() + "\"</html>"));
        googleSearchQueryTextField.setText(query.getArtist() + StringUtils.SPACE + query.getAlbum());
        spotifyArtistTextField.setText(query.getArtist());
        spotifyAlbumTextField.setText(query.getAlbum());
        manualSearchQueryTextField.setText(query.getArtist() + StringUtils.SPACE + query.getAlbum());
        dropImageLabel.setTransferHandler(createTransferHandler());

        // set spotify artist info if we have it
        if (Moose.getSettings().getSpotifyArtists().containsKey(spotifyArtistTextField.getText())) {
            spotifyArtistTextField.setEnabled(false);
            spotifyAlbumTextField.setEnabled(false);
            spotifySearchButton.setEnabled(false);
            spotifyLoadingLabel.setIcon(IconUtils.get(IconUtils.LOADING));

            // create a thread to wait until the dialog box pops up
            (new Thread(this::populateSpotifyArtistInfo)).start();
        }
    }

    /**
     * Returns the Google search worker, so I can use it multiple times
     * @return a Google search worker
     */
    private SwingWorker<Void, Void> getGoogleSearchWorker() {
        // make a swing worker do the image search in a separate thread, so I can update the GUI
        return new SwingWorker<>() {
            @Override
            protected Void doInBackground() {

                // get the query
                String query = googleSearchQueryTextField.getText();

                // iteratively make the calls so that the progress bar can be updated
                googleImageSearchResults.addAll(googleSearchService.doGoogleSearch(query, Constants.LARGE, 1));
                googleProgressBar.setValue(30);
                googleImageSearchResults.addAll(googleSearchService.doGoogleSearch(query, Constants.LARGE, 11));
                googleProgressBar.setValue(30);
                googleSearchService.updateAlbumArtSettings();

                // get the interval to update the progress by based on how much is left
                double interval = googleImageSearchResults.size() / 40.0;

                // go through each of the results and attempt to grab a buffered image from them
                List<ImageSearchResult> toRemoveList = new ArrayList<>();
                googleImageSearchResults.forEach((isr) -> {
                    ImageSearchResult toRemove = googleSearchService.processImage(isr);
                    if (toRemove != null) {
                        toRemoveList.add(toRemove);
                    }
                    googleProgressBar.setValue((int) (googleProgressBar.getValue() + Math.floor(interval)));
                });
                // remove all the elements where we couldn't grab the image from
                googleImageSearchResults.removeAll(toRemoveList);
                googleProgressBar.setValue(100);

                // show success or failure
                googleLoadingLabel.setIcon(googleImageSearchResults.isEmpty() ?
                        IconUtils.get(IconUtils.ERROR) : IconUtils.get(IconUtils.SUCCESS));

                // show the images
                icons = getIconsFromImages(googleImageSearchResults);
                if (icons.size() > 1) {
                    googleAlbumArtImageLabel.setIcon(icons.get(0));
                    currentIconIndex = 0;
                    googleNextButton.setEnabled(true);
                    googlePreviousButton.setEnabled(false);
                    googleSearchButton.setEnabled(true);
                } else if (icons.size() == 1) {
                    googleAlbumArtImageLabel.setIcon(icons.get(0));
                    currentIconIndex = 0;
                    googleNextButton.setEnabled(false);
                    googlePreviousButton.setEnabled(false);
                    googleSearchButton.setEnabled(true);
                } else {
                    googleAlbumArtImageLabel.setIcon(null);
                    currentIconIndex = -1;
                    googleNextButton.setEnabled(false);
                    googlePreviousButton.setEnabled(false);
                    googleStatusLabel.setText("No results found!");
                    googleLoadingLabel.setIcon(null);
                    googleSearchButton.setEnabled(true);
                    return null;
                }

                googleStatusLabel.setText("Result 1 of " + googleImageSearchResults.size());
                googleSourceLabel.setText(googleImageSearchResults.get(currentIconIndex).getUrl());
                googleSourceLabel.setToolTipText(googleImageSearchResults.get(currentIconIndex).getUrl());
                googleSizeLabel.setText(googleImageSearchResults.get(currentIconIndex).getImageDimensions().getWidth() + "x" + googleImageSearchResults.get(currentIconIndex).getImageDimensions().getHeight());
                googleConfirmButton.setEnabled(true);
                googleImagesButton.setEnabled(true);

                return null;    // don't return anything since we're just playing with threads
            }
        };
    }

    /**
     * Returns the spotify artist search worker, so I can use it multiple times
     * @return a spotify artist search worker
     */
    private SwingWorker<Void, Void> getSpotifyArtistSearchWorker() {
        // make a swing worker do the spotify search in a separate thread, so I can update the GUI
        return new SwingWorker<>() {
            @Override
            protected Void doInBackground() {

                // get any update to the query fields
                query.setArtist(spotifyArtistTextField.getText());
                query.setAlbum(spotifyAlbumTextField.getText());

                // authenticate first
                spotifyApiService.authenticate();
                spotifyProgressBar.setValue(24);

                // get the artist
                try {
                    spotifyArtists = spotifyApiService.getArtistsFromSearch(spotifyArtistTextField.getText());
                    spotifyProgressBar.setValue(48);
                } catch (IOException e) {
                    if (e.getClass() == MalformedURLException.class) {
                        logger.logError("Exception while forming URL with artist query: " + query.getArtist(), e);
                    } else {
                        logger.logError("Exception while getting artist info from Spotify API!", e);
                    }
                    return null;
                }

                // check if we have the artists
                if (spotifyArtists.isEmpty()) {
                    // no artists were found
                    // update graphics
                    spotifyStatusLabel.setText("No artists found!");
                    spotifyApiStatusLabel.setText("No artists found!");
                    spotifyLoadingLabel.setIcon(IconUtils.get(IconUtils.ERROR));
                    spotifyProgressBar.setValue(100);
                    spotifyAlbumArtImageLabel.setIcon(null);
                    spotifyArtistTextField.setEnabled(true);
                    spotifyAlbumTextField.setEnabled(true);
                    spotifySearchButton.setEnabled(true);
                } else if (spotifyArtists.size() > 1) {
                    // more than one artist was found
                    currentSpotifyArtistIconIndex = 0;
                    spotifyAlbumArtImageLabel.setIcon(ImageUtils.getScaledImage(
                            ImageUtils.getBytesFromBufferedImage(ImageUtils.getImageFromUrl(spotifyArtists.get(currentSpotifyArtistIconIndex).getImages().get(0).getUrl())), 300
                    ));
                    spotifyProgressBar.setValue(100);
                    spotifyLoadingLabel.setIcon(IconUtils.get(IconUtils.SUCCESS));
                    spotifyApiStatusLabel.setText(spotifyArtists.size() + " artists found!");
                    spotifyStatusLabel.setHorizontalAlignment(JLabel.CENTER);
                    spotifyStatusLabel.setText("<html><body>(" + (currentSpotifyArtistIconIndex + 1) + " of " + spotifyArtists.size() + ") <a href=''>" + spotifyArtists.get(currentSpotifyArtistIconIndex).getName() + "</a></body></html>");
                    spotifyConfirmButton.setEnabled(true);
                    spotifyNextButton.setEnabled(true);
                    spotifyPreviousButton.setEnabled(false);
                } else {
                    // only one artist was found, that's the one
                    confirmSpotifyArtist();

                    // update graphics
                    spotifyLoadingLabel.setIcon(IconUtils.get(IconUtils.LOADING));
                    spotifyProgressBar.setValue(0);
                    spotifyStatusLabel.setText("Searching for albums from " + spotifyArtists.get(currentSpotifyArtistIconIndex).getName() + "...");

                    // search for albums
                    getSpotifyAlbumSearchWorker().execute();
                }

                return null;    // don't return anything since we're just playing with threads
            }
        };
    }

    /**
     * Returns the spotify album search worker, so I can use it multiple times
     * @return a spotify album search worker
     */
    private SwingWorker<Void, Void> getSpotifyAlbumSearchWorker() {
        // make a swing worker do the spotify search in a separate thread, so I can update the GUI
        return new SwingWorker<>() {
            @Override
            protected Void doInBackground() {

                // get any update to the query fields
                query.setArtist(spotifyArtistTextField.getText());
                query.setAlbum(spotifyAlbumTextField.getText());

                // search for the album
                Album album = null;
                try {
                    String nextUrl = null;
                    while (true) {

                        // authenticate first
                        spotifyApiService.authenticate();
                        if (StringUtils.isEmpty(nextUrl)) {
                            spotifyProgressBar.setValue(24);
                        }

                        // get the albums
                        AlbumSearchResponse albums = spotifyApiService.getAlbumFromArtist(spotifyArtistId, nextUrl);
                        spotifyProgressBar.setValue(48);
                        for (Album albumInList : albums.getAlbums()) {
                            // we have to reformat Spotify's album name, because they use ’ instead of '
                            albumInList.setName(albumInList.getName().replace("’", "'"));
                            if (albumInList.getName().contains(query.getAlbum()) || query.getAlbum().contains(albumInList.getName())) {
                                album = albumInList;
                                break;
                            }
                        }
                        // check if we found a match at this point
                        if (album != null) {
                            break;
                        }
                        // we got here, so we need to use the next url to make more calls (if we have it)
                        if (StringUtils.isNotEmpty(albums.getNext())) {
                            nextUrl = albums.getNext();
                            spotifyProgressBar.setValue((100 - spotifyProgressBar.getValue()) / 2);
                        } else {
                            // if the next url is null, we can't search anymore, album wasn't found
                            spotifyApiStatusLabel.setText("<html><body>Artist confirmed!<br>Album wasn't found!</body></html>");
                            spotifyLoadingLabel.setIcon(IconUtils.get(IconUtils.ERROR));
                            spotifyProgressBar.setValue(100);
                            spotifyConfirmButton.setEnabled(false);
                            spotifyPreviousButton.setEnabled(false);
                            spotifyNextButton.setEnabled(false);
                            spotifySearchButton.setEnabled(true);
                            spotifyStatusLabel.setText(StringUtils.EMPTY);
                            spotifyAlbumTextField.setEnabled(true);

                            return null;
                        }
                    }
                } catch (IOException e) {
                    if (e.getClass() == MalformedURLException.class) {
                        logger.logError("Exception while forming URL with album query: " + query.getAlbum(), e);
                    } else {
                        logger.logError("Exception while getting album info from Spotify API!", e);
                    }
                    return null;
                }

                // cover was found
                spotifyAlbums.add(album);
                String url = album.getImage().getUrl();
                spotifyLoadingLabel.setIcon(IconUtils.get(IconUtils.SUCCESS));
                foundCover = ImageUtils.getImageFromUrl(url);
                spotifyAlbumArtImageLabel.setIcon(ImageUtils.getScaledImage(ImageUtils.getBytesFromBufferedImage(foundCover), 300));
                spotifyProgressBar.setValue(100);
                spotifyApiStatusLabel.setText("<html><body>Artist confirmed!<br>Album found!</body></html>");
                spotifyStatusLabel.setText(StringUtils.EMPTY);
                spotifyConfirmButton.setEnabled(true);
                spotifyArtistTextField.setEnabled(true);
                spotifyAlbumTextField.setEnabled(true);
                spotifySearchButton.setEnabled(true);

                return null;    // don't return anything since we're just playing with threads
            }
        };
    }

    /**
     * Populates spotify artist info if we have it
     */
    private void populateSpotifyArtistInfo() {
        try {
            // get the artist info from spotify
            spotifyApiService.authenticate();
            Artist artist = spotifyApiService.getArtist(Moose.getSettings().getSpotifyArtists().get(spotifyArtistTextField.getText()));

            // set global
            spotifyArtistId = artist.getId();

            // update graphics
            spotifyArtistImageLabel.setIcon(
                    ImageUtils.getCircularScaledImage(
                            ImageUtils.getBytesFromBufferedImage(
                                    ImageUtils.getImageFromUrl(
                                            artist.getImages().get(0).getUrl()
                                    )
                            ), 56
                    )
            );
            spotifyArtistImageLabel.setToolTipText(artist.getName());
            spotifyApiStatusLabel.setText("Artist confirmed!");
            spotifyArtistTextField.setEnabled(false);
            spotifyAlbumTextField.setEnabled(true);
            spotifySearchButton.setEnabled(true);
            spotifyLoadingLabel.setIcon(null);
        } catch (IOException e) {
            if (e.getClass() == MalformedURLException.class) {
                logger.logError("Exception while forming URL with artist query: " + spotifyArtistTextField.getText(), e);
            } else {
                logger.logError("Exception while getting artist info from Spotify API!", e);
            }
        }
    }

    /**
     * Creates a transfer handler for the manual image label, so you can drag an image onto the panel
     * @return a transfer handler capable of processing images dragged in from the world wide web
     */
    private TransferHandler createTransferHandler(){
        return new TransferHandler(){
            @Override
            public boolean importData(JComponent comp, Transferable aTransferable) {
                try {
                    // get the image
                    Object transferData = aTransferable.getTransferData(DataFlavor.imageFlavor);
                    Image image = (Image) transferData;
                    ImageIcon icon = new ImageIcon(image);
                    byte[] bytes = ImageUtils.getBytesFromImageIcon(icon);
                    foundCover = ImageUtils.getBufferedImageFromBytes(bytes);

                    // update graphics
                    manualStatusLabel.setText("Image successfully loaded!");
                    manualConfirmButton.setEnabled(true);
                    manualSizeLabel.setText(image.getWidth(null) + " x " + image.getHeight(null));
                    dropImageLabel.setIcon(ImageUtils.getScaledImage(bytes, 300));
                } catch (UnsupportedFlavorException | IOException e) {
                    logger.logError("Exception occurred when image was dragged into the frame!", e);
                    manualStatusLabel.setText("Unsupported media type!");
                    manualConfirmButton.setEnabled(false);
                    manualSizeLabel.setText(StringUtils.EMPTY);
                    dropImageLabel.setIcon(null);
                }
                return true;
            }

            @Override
            public boolean canImport( JComponent comp, DataFlavor[] transferFlavors ) {
                return true;
            }
        };
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        spotifyPane = new javax.swing.JPanel();
        spotifyLoadingLabel = new javax.swing.JLabel();
        spotifyProgressBar = new javax.swing.JProgressBar();
        spotifyAlbumArtImageLabel = new javax.swing.JLabel();
        spotifyConfirmButton = new javax.swing.JButton();
        spotifyStatusLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        spotifyArtistTextField = new javax.swing.JTextField();
        spotifyAlbumTextField = new javax.swing.JTextField();
        spotifySearchButton = new javax.swing.JButton();
        spotifyApiStatusLabel = new javax.swing.JLabel();
        spotifyPreviousButton = new javax.swing.JButton();
        spotifyNextButton = new javax.swing.JButton();
        spotifyArtistImageLabel = new javax.swing.JLabel();
        googleSearchPane = new javax.swing.JPanel();
        searchQueryLabel = new javax.swing.JLabel();
        googleSearchQueryTextField = new javax.swing.JTextField();
        googleSearchButton = new javax.swing.JButton();
        googleSourceLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        googleSizeLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        googleLoadingLabel = new javax.swing.JLabel();
        googleAlbumArtImageLabel = new javax.swing.JLabel();
        googleConfirmButton = new javax.swing.JButton();
        googlePreviousButton = new javax.swing.JButton();
        googleNextButton = new javax.swing.JButton();
        googleStatusLabel = new javax.swing.JLabel();
        googleProgressBar = new javax.swing.JProgressBar();
        manualImagePane = new javax.swing.JPanel();
        googleImagesButton = new javax.swing.JButton();
        dropImageLabel = new javax.swing.JLabel();
        manualConfirmButton = new javax.swing.JButton();
        manualSizeLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        searchQueryLabel1 = new javax.swing.JLabel();
        manualSearchQueryTextField = new javax.swing.JTextField();
        manualStatusLabel = new javax.swing.JLabel();
        headerLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        resetMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Album Art Finder");
        setAlwaysOnTop(true);
        setResizable(false);

        tabbedPane.setForeground(new java.awt.Color(1, 1, 1));
        tabbedPane.setToolTipText("");

        spotifyLoadingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        spotifyAlbumArtImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        spotifyAlbumArtImageLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        spotifyAlbumArtImageLabel.setMaximumSize(new java.awt.Dimension(250, 250));
        spotifyAlbumArtImageLabel.setMinimumSize(new java.awt.Dimension(250, 250));
        spotifyAlbumArtImageLabel.setSize(new java.awt.Dimension(153, 153));

        spotifyConfirmButton.setText("Confirm");
        spotifyConfirmButton.setEnabled(false);
        spotifyConfirmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spotifyConfirmButtonActionPerformed(evt);
            }
        });

        spotifyStatusLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        spotifyStatusLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                spotifyStatusLabelMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                spotifyStatusLabelMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                spotifyStatusLabelMouseEntered(evt);
            }
        });

        jLabel3.setText("Artist:");

        jLabel4.setText("Album:");

        spotifyArtistTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spotifyArtistTextFieldKeyPressed(evt);
            }
        });

        spotifyAlbumTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                spotifyAlbumTextFieldKeyPressed(evt);
            }
        });

        spotifySearchButton.setText("Search");
        spotifySearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spotifySearchButtonActionPerformed(evt);
            }
        });

        spotifyPreviousButton.setText("<");
        spotifyPreviousButton.setEnabled(false);
        spotifyPreviousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spotifyPreviousButtonActionPerformed(evt);
            }
        });

        spotifyNextButton.setText(">");
        spotifyNextButton.setEnabled(false);
        spotifyNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spotifyNextButtonActionPerformed(evt);
            }
        });

        spotifyArtistImageLabel.setIcon(IconUtils.get(IconUtils.CIRCLE));
        spotifyArtistImageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                spotifyArtistImageLabelMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                spotifyArtistImageLabelMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                spotifyArtistImageLabelMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout spotifyPaneLayout = new javax.swing.GroupLayout(spotifyPane);
        spotifyPane.setLayout(spotifyPaneLayout);
        spotifyPaneLayout.setHorizontalGroup(
            spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, spotifyPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(spotifyAlbumArtImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spotifyStatusLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, spotifyPaneLayout.createSequentialGroup()
                        .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spotifyArtistTextField)
                            .addComponent(spotifyAlbumTextField)))
                    .addGroup(spotifyPaneLayout.createSequentialGroup()
                        .addComponent(spotifyPreviousButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spotifyConfirmButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spotifyNextButton))
                    .addGroup(spotifyPaneLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(spotifyPaneLayout.createSequentialGroup()
                                .addComponent(spotifyArtistImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spotifyApiStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spotifySearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(spotifyPaneLayout.createSequentialGroup()
                                .addComponent(spotifyLoadingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spotifyProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        spotifyPaneLayout.setVerticalGroup(
            spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, spotifyPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(spotifyArtistTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spotifyAlbumTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spotifyApiStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spotifySearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spotifyArtistImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(spotifyProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spotifyLoadingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spotifyAlbumArtImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spotifyConfirmButton)
                    .addComponent(spotifyNextButton)
                    .addComponent(spotifyPreviousButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spotifyStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPane.addTab("Spotify", spotifyPane);

        searchQueryLabel.setText("Search Query:");

        googleSearchQueryTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                googleSearchQueryTextFieldKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                googleSearchQueryTextFieldKeyReleased(evt);
            }
        });

        googleSearchButton.setText("Search");
        googleSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googleSearchButtonActionPerformed(evt);
            }
        });

        googleSourceLabel.setMaximumSize(new java.awt.Dimension(310, 16));
        googleSourceLabel.setMinimumSize(new java.awt.Dimension(310, 16));
        googleSourceLabel.setSize(new java.awt.Dimension(310, 16));

        jLabel1.setText("Source:");

        jLabel2.setText("Size:");

        googleLoadingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        googleAlbumArtImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        googleAlbumArtImageLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        googleAlbumArtImageLabel.setMaximumSize(new java.awt.Dimension(250, 250));
        googleAlbumArtImageLabel.setMinimumSize(new java.awt.Dimension(250, 250));
        googleAlbumArtImageLabel.setSize(new java.awt.Dimension(153, 153));

        googleConfirmButton.setText("Confirm");
        googleConfirmButton.setEnabled(false);
        googleConfirmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googleConfirmButtonActionPerformed(evt);
            }
        });

        googlePreviousButton.setText("<");
        googlePreviousButton.setEnabled(false);
        googlePreviousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googlePreviousButtonActionPerformed(evt);
            }
        });

        googleNextButton.setText(">");
        googleNextButton.setEnabled(false);
        googleNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googleNextButtonActionPerformed(evt);
            }
        });

        googleStatusLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        javax.swing.GroupLayout googleSearchPaneLayout = new javax.swing.GroupLayout(googleSearchPane);
        googleSearchPane.setLayout(googleSearchPaneLayout);
        googleSearchPaneLayout.setHorizontalGroup(
            googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(googleSearchPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(googleSearchPaneLayout.createSequentialGroup()
                        .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(googleAlbumArtImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(googleSearchPaneLayout.createSequentialGroup()
                                .addComponent(googleLoadingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(googleProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(googleSearchPaneLayout.createSequentialGroup()
                        .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(googleStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(googleSearchPaneLayout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(googleSizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(googleSearchPaneLayout.createSequentialGroup()
                                .addComponent(googlePreviousButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(googleConfirmButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(googleNextButton))
                            .addGroup(googleSearchPaneLayout.createSequentialGroup()
                                .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(googleSearchPaneLayout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(googleSourceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(googleSearchPaneLayout.createSequentialGroup()
                                        .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(searchQueryLabel)
                                            .addComponent(googleSearchQueryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(googleSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        googleSearchPaneLayout.setVerticalGroup(
            googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(googleSearchPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(googleSearchPaneLayout.createSequentialGroup()
                        .addComponent(searchQueryLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(googleSearchQueryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(googleSearchButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(googleSourceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(googleSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(googleLoadingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(googleProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(googleAlbumArtImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(googleConfirmButton)
                    .addComponent(googlePreviousButton)
                    .addComponent(googleNextButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(googleStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPane.addTab("Google Image Search", googleSearchPane);

        googleImagesButton.setText("Open Google Images");
        googleImagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googleImagesButtonActionPerformed(evt);
            }
        });

        dropImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dropImageLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        dropImageLabel.setMaximumSize(new java.awt.Dimension(250, 250));
        dropImageLabel.setMinimumSize(new java.awt.Dimension(250, 250));
        dropImageLabel.setSize(new java.awt.Dimension(153, 153));

        manualConfirmButton.setText("Confirm");
        manualConfirmButton.setEnabled(false);
        manualConfirmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualConfirmButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("Size:");

        searchQueryLabel1.setText("Search Query:");

        manualStatusLabel.setText("Drag image from the web into the space below!");

        javax.swing.GroupLayout manualImagePaneLayout = new javax.swing.GroupLayout(manualImagePane);
        manualImagePane.setLayout(manualImagePaneLayout);
        manualImagePaneLayout.setHorizontalGroup(
            manualImagePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manualImagePaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manualImagePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(manualImagePaneLayout.createSequentialGroup()
                        .addComponent(dropImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(manualImagePaneLayout.createSequentialGroup()
                        .addGap(81, 81, 81)
                        .addComponent(manualConfirmButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(87, 87, 87))
                    .addGroup(manualImagePaneLayout.createSequentialGroup()
                        .addGroup(manualImagePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, manualImagePaneLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(googleImagesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(manualImagePaneLayout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(manualSizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(manualImagePaneLayout.createSequentialGroup()
                        .addComponent(manualSearchQueryTextField)
                        .addContainerGap())
                    .addGroup(manualImagePaneLayout.createSequentialGroup()
                        .addGroup(manualImagePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(searchQueryLabel1)
                            .addComponent(manualStatusLabel))
                        .addContainerGap(9, Short.MAX_VALUE))))
        );
        manualImagePaneLayout.setVerticalGroup(
            manualImagePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, manualImagePaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchQueryLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(manualSearchQueryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(googleImagesButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(manualImagePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(manualSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(manualStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dropImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(manualConfirmButton)
                .addGap(36, 36, 36))
        );

        tabbedPane.addTab("Manual", manualImagePane);

        fileMenu.setText("File");

        resetMenuItem.setText("Reset");
        resetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(resetMenuItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(headerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 575, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  SPOTIFY TAB METHODS
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // <editor-fold defaultstate="collapsed" desc="SPOTIFY TAB METHODS">

    /**
     * Handles the search button press. Does the spotify search.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void spotifySearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spotifySearchButtonActionPerformed
        doSpotifySearch();
    }//GEN-LAST:event_spotifySearchButtonActionPerformed

    /**
     * Checks to see if we have the client id/client secret before doing a search.
     * Updates the gui to signify it starting, then starts up the spotify swing worker
     */
    private void doSpotifySearch() {
        // check to see if we have a valid client id and client secret
        if (StringUtils.isEmpty(Moose.getSettings().getSpotifyClientId())
                || StringUtils.isEmpty(Moose.getSettings().getSpotifyClientSecret())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid/Missing Client ID or Client Secret, open Settings to configure.",
                    "Error with Album Art Finder",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // check if we already have an artist
        if (StringUtils.isEmpty(spotifyArtistId)) {
            // we don't have a spotify artist id, which means we still need to search for the artist
            // update the gui to signify it starting
            spotifyApiStatusLabel.setText("Searching for artists...");
            spotifyLoadingLabel.setIcon(IconUtils.get(IconUtils.LOADING));
            spotifySearchButton.setEnabled(false);
            spotifyArtistTextField.setEnabled(false);
            spotifyAlbumTextField.setEnabled(false);

            // do the artist search
            getSpotifyArtistSearchWorker().execute();
        } else {
            // we have a spotify artist id, which means we don't need to search for the artist, search for albums straight away
            // update the gui to signify it starting
            spotifyApiStatusLabel.setText("<html><body>Artist confirmed!<br>Searching for albums...</body></html>");
            spotifyLoadingLabel.setIcon(IconUtils.get(IconUtils.LOADING));
            spotifyProgressBar.setValue(0);
            spotifyStatusLabel.setText("Searching for albums from " + spotifyArtistTextField.getText() + "...");
            spotifyConfirmButton.setEnabled(false);
            spotifyPreviousButton.setEnabled(false);
            spotifyNextButton.setEnabled(false);

            // do the search
            getSpotifyAlbumSearchWorker().execute();
        }
    }

    /**
     * Handles the confirm button press. Confirms both the image and the spotify artist and saves it.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void spotifyConfirmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spotifyConfirmButtonActionPerformed
        if (!spotifyArtists.isEmpty() && spotifyAlbums.isEmpty()) {
            confirmSpotifyArtist();
        } else if (StringUtils.isNotEmpty(spotifyArtistId)) {
            confirmImage();
        }
    }//GEN-LAST:event_spotifyConfirmButtonActionPerformed

    /**
     * Handles the next button press. Goes to the next artist/album if there is one.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void spotifyNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spotifyNextButtonActionPerformed
        currentSpotifyArtistIconIndex++;
        spotifyPreviousButton.setEnabled(currentSpotifyArtistIconIndex != 0);
        spotifyNextButton.setEnabled(currentSpotifyArtistIconIndex < spotifyArtists.size() - 1);
        spotifyAlbumArtImageLabel.setIcon(ImageUtils.getScaledImage(
                ImageUtils.getBytesFromBufferedImage(ImageUtils.getImageFromUrl(spotifyArtists.get(currentSpotifyArtistIconIndex).getImages().get(0).getUrl())), 300
        ));
        spotifyStatusLabel.setText("<html><body>(" + (currentSpotifyArtistIconIndex + 1) + " of " + spotifyArtists.size() + ") <a href=''>" + spotifyArtists.get(currentSpotifyArtistIconIndex).getName() + "</a></body></html>");
    }//GEN-LAST:event_spotifyNextButtonActionPerformed

    /**
     * Handles the previous button press. Goes to the previous artist/album if there is one.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void spotifyPreviousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spotifyPreviousButtonActionPerformed
        currentSpotifyArtistIconIndex--;
        spotifyPreviousButton.setEnabled(currentSpotifyArtistIconIndex != 0);
        spotifyNextButton.setEnabled(currentSpotifyArtistIconIndex < spotifyArtists.size() - 1);
        spotifyAlbumArtImageLabel.setIcon(ImageUtils.getScaledImage(
                ImageUtils.getBytesFromBufferedImage(ImageUtils.getImageFromUrl(spotifyArtists.get(currentSpotifyArtistIconIndex).getImages().get(0).getUrl())), 300
        ));
        spotifyStatusLabel.setText("<html><body>(" + (currentSpotifyArtistIconIndex + 1) + " of " + spotifyArtists.size() + ") <a href=''>" + spotifyArtists.get(currentSpotifyArtistIconIndex).getName() + "</a></body></html>");
    }//GEN-LAST:event_spotifyPreviousButtonActionPerformed

    /**
     * Handles the event for when mouse enters the artist image label, just makes it look clickable.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void spotifyArtistImageLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spotifyArtistImageLabelMouseEntered
        spotifyArtistImageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_spotifyArtistImageLabelMouseEntered

    /**
     * Handles the click event for the artist image label. Opens the artist page of that artist.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void spotifyArtistImageLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spotifyArtistImageLabelMouseClicked
        if (StringUtils.isNotEmpty(spotifyArtistId)) {
            String url = "https://open.spotify.com/artist/" + spotifyArtistId;
            try {
                WebUtils.openPage(url);
            } catch (Exception e) {
                logger.logError("Couldn't open web page: " + url, e);
            }
        }
    }//GEN-LAST:event_spotifyArtistImageLabelMouseClicked

    /**
     * Handles the event for when mouse exits the artist image label, just sets the cursor back to normal.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void spotifyArtistImageLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spotifyArtistImageLabelMouseExited
        spotifyArtistImageLabel.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_spotifyArtistImageLabelMouseExited

    /**
     * Handles the event for when mouse enters the spotify status label, just makes it look clickable.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void spotifyStatusLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spotifyStatusLabelMouseEntered
        spotifyStatusLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_spotifyStatusLabelMouseEntered

    /**
     * Handles the event for when mouse exits the spotify status label, just sets the cursor back to normal.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void spotifyStatusLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spotifyStatusLabelMouseExited
        spotifyStatusLabel.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_spotifyStatusLabelMouseExited

    /**
     * Handles the click event for the spotify status label. Opens the artist page of that artist.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void spotifyStatusLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spotifyStatusLabelMouseClicked
        if (spotifyStatusLabel.getText().matches(".*\\(\\d+ of \\d+\\).*")) {
            String url = "https://open.spotify.com/artist/" + spotifyArtists.get(currentSpotifyArtistIconIndex).getId();
            try {
                WebUtils.openPage(url);
            } catch (Exception e) {
                logger.logError("Couldn't open web page: " + url, e);
            }
        }
    }//GEN-LAST:event_spotifyStatusLabelMouseClicked

    // </editor-fold>

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  GOOGLE TAB METHODS
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // <editor-fold defaultstate="collapsed" desc="GOOGLE TAB METHODS">

    /**
     * Handles the search button press.  Does the Google Search.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void googleSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleSearchButtonActionPerformed
        doGoogleSearch();
    }//GEN-LAST:event_googleSearchButtonActionPerformed

    /**
     * Checks to see if the Google search CSE id and google search api key before running the search
     * Also checks to see if the api call limit has been reached before searching
     */
    public void doGoogleSearch() {

        // check to see if we have a valid api key and cse id
        if (StringUtils.isEmpty(Moose.getSettings().getAlbumArtFinderApiKey())
                || StringUtils.isEmpty(Moose.getSettings().getAlbumArtFinderCseId())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid/Missing API key or CSE ID, open Settings to configure.",
                    "Error with Album Art Finder",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // check to make sure we're below the allowed search threshold
        if (GoogleSearchService.checkIfBelowLimit()) {

            // update the gui to signify it starting
            googleSourceLabel.setText(StringUtils.EMPTY);
            googleSourceLabel.setToolTipText(StringUtils.EMPTY);
            googleSizeLabel.setText(StringUtils.EMPTY);
            googleStatusLabel.setText("Searching...");
            googleLoadingLabel.setIcon(IconUtils.get(IconUtils.LOADING));
            googleSearchButton.setEnabled(false);
            googleConfirmButton.setEnabled(false);
            googleImagesButton.setEnabled(false);

            // do the search
            getGoogleSearchWorker().execute();

        } else {
            // ruh roh, to many calls for today
            JOptionPane.showMessageDialog(null,
                    "API call limit reached for today!",
                    "API Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the confirm button press. Sets the class field and confirms image.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void googleConfirmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleConfirmButtonActionPerformed
        foundCover = googleImageSearchResults.get(currentIconIndex).getBufferedImage();
        confirmImage();
    }//GEN-LAST:event_googleConfirmButtonActionPerformed

    /**
     * Handles the key pressing for the Google search query field.  Sets the search button enabled based on text
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void googleSearchQueryTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_googleSearchQueryTextFieldKeyReleased
        googleSearchButton.setEnabled(!googleSearchQueryTextField.getText().equals(""));
    }//GEN-LAST:event_googleSearchQueryTextFieldKeyReleased

    /**
     * Handles the next button press.  Goes to the next image if there is one.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void googleNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleNextButtonActionPerformed
        if (currentIconIndex < icons.size() - 1) {
            currentIconIndex++;
            googleAlbumArtImageLabel.setIcon(icons.get(currentIconIndex));
            googlePreviousButton.setEnabled(true);

            if (currentIconIndex == icons.size() - 1) {
                googleNextButton.setEnabled(false);
                googlePreviousButton.setEnabled(true);
            }
        }

        googleSourceLabel.setText(googleImageSearchResults.get(currentIconIndex).getUrl());
        googleSourceLabel.setToolTipText(googleImageSearchResults.get(currentIconIndex).getUrl());
        googleSizeLabel.setText(googleImageSearchResults.get(currentIconIndex).getImageDimensions().getWidth() + "x" + googleImageSearchResults.get(currentIconIndex).getImageDimensions().getHeight());
        googleStatusLabel.setText("Result " + (currentIconIndex + 1) + " of " + googleImageSearchResults.size());
    }//GEN-LAST:event_googleNextButtonActionPerformed

    /**
     * Handles the previous button press.  Goes to the previous image if there is one.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void googlePreviousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googlePreviousButtonActionPerformed
        if (currentIconIndex > 0) {
            currentIconIndex--;
            googleAlbumArtImageLabel.setIcon(icons.get(currentIconIndex));
            googleNextButton.setEnabled(true);

            if (currentIconIndex == 0) {
                googlePreviousButton.setEnabled(false);
                googleNextButton.setEnabled(true);
            }
        }

        googleSourceLabel.setText(googleImageSearchResults.get(currentIconIndex).getUrl());
        googleSourceLabel.setToolTipText(googleImageSearchResults.get(currentIconIndex).getUrl());
        googleSizeLabel.setText(googleImageSearchResults.get(currentIconIndex).getImageDimensions().getWidth() + "x" + googleImageSearchResults.get(currentIconIndex).getImageDimensions().getHeight());
        googleStatusLabel.setText("Result " + (currentIconIndex + 1) + " of " + googleImageSearchResults.size());
    }//GEN-LAST:event_googlePreviousButtonActionPerformed

    /**
     * Handles the "Open Google Images" button press.  Opens a web page with the search filled out.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void googleImagesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleImagesButtonActionPerformed
        String url = StringUtils.isEmpty(manualSearchQueryTextField.getText()) ? "https://images.google.com/" :
                GoogleSearchService.buildImageSearchQuery(googleSearchQueryTextField.getText());
        try {
            WebUtils.openPage(url);
        } catch (Exception e) {
            logger.logError("Couldn't open web page: " + url, e);
        }
    }//GEN-LAST:event_googleImagesButtonActionPerformed

    /**
     * Handles the search query key press. Checks if the key code is of type Enter, which means button click.
     * @param evt the ActionEvent
     */
    private void googleSearchQueryTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_googleSearchQueryTextFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            googleSearchButton.doClick();
        }
    }//GEN-LAST:event_googleSearchQueryTextFieldKeyPressed
    // </editor-fold>

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  MANUAL TAB METHODS
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // <editor-fold defaultstate="collapsed" desc="MANUAL TAB METHODS">

    /**
     * Handles the confirm button press. Confirms the image.
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void manualConfirmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualConfirmButtonActionPerformed
        confirmImage();
    }//GEN-LAST:event_manualConfirmButtonActionPerformed

    // </editor-fold>

    /**
     * Handles the event for resetting the screen.
     * TODO don't dispose, just clear the fields
     * @param evt the ActionEvent (not used, but here because Netbeans)
     */
    @SuppressWarnings("unused") // for the evt param
    private void resetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetMenuItemActionPerformed
        int selectedTab = tabbedPane.getSelectedIndex();
        this.dispose();
        AlbumArtFinderFrame newFrame = new AlbumArtFinderFrame(query);
        newFrame.setLocationRelativeTo(Moose.getFrame());
        newFrame.setVisible(true);
        newFrame.tabbedPane.setSelectedIndex(selectedTab);
    }//GEN-LAST:event_resetMenuItemActionPerformed

    /**
     * Handles the event for a key press on the album text field, just listens to Enter pressed, simulates a click on
     * the search button.
     * @param evt the ActionEvent
     */
    private void spotifyAlbumTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_spotifyAlbumTextFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            spotifySearchButton.doClick();
        }
    }//GEN-LAST:event_spotifyAlbumTextFieldKeyPressed

    /**
     * Handles the event for a key press on the artist text field, just listens to Enter pressed, simulates a click on
     * the search button.
     * @param evt the ActionEvent
     */
    private void spotifyArtistTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_spotifyArtistTextFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            spotifySearchButton.doClick();
        }
    }//GEN-LAST:event_spotifyArtistTextFieldKeyPressed

    /**
     * Gets a list of Icons from a list of image search results containing BufferedImages to display on the modal.
     * @param responses the image search results to parse
     * @return a list of Icons to display on the modal
     */
    private List<Icon> getIconsFromImages(List<ImageSearchResult> responses) {
        List<Icon> scaledIcons = new ArrayList<>();
        for (ImageSearchResult isr : responses) {
            byte[] bytes = ImageUtils.getBytesFromBufferedImage(isr.getBufferedImage());
            scaledIcons.add(ImageUtils.getScaledImage(bytes, 300));
        }
        return scaledIcons;
    }

    /**
     * Confirms the image with the foundCover class field. Creates the image file and closes the modal.
     */
    private void confirmImage() {
        int dim = Moose.getSettings().getPreferredCoverArtSize();
        File outputFile = ImageUtils.createImageFile(foundCover, dir, dim);
        assert outputFile != null;
        if (outputFile.exists()) {
            List<Song> songs = Moose.getSongController().getSongsFromRows(
                    this.rows.stream().mapToInt(Integer::intValue).toArray());
            for (Song song : songs) {
                Moose.getSongController().getAutoTaggingService().addCoverForFile(song, outputFile);
            }
        }
        Moose.getFrame().updateMultiplePanelFields();
        this.dispose();
    }

    /**
     * Confirms the spotify artist that was found.
     * Updates some graphics and adds the artist id to the list of known artists in settings.
     */
    private void confirmSpotifyArtist() {
        // set artist local value
        Artist artist = spotifyArtists.get(currentSpotifyArtistIconIndex);

        // set global
        spotifyArtistId = artist.getId();

        // add that artist to the list of known artists if it isn't already
        if (!Moose.getSettings().getSpotifyArtists().containsKey(artist.getName())) {
            Moose.getSettingsController().addSpotifyArtist(artist.getName(), artist.getId());
        }

        // update graphics
        spotifyArtistImageLabel.setIcon(
                ImageUtils.getCircularScaledImage(
                        ImageUtils.getBytesFromBufferedImage(
                                ImageUtils.getImageFromUrl(
                                        artist.getImages().get(0).getUrl()
                                )
                        ), 56
                )
        );
        spotifyArtistImageLabel.setToolTipText(artist.getName());
        spotifyAlbumArtImageLabel.setIcon(null);
        spotifyApiStatusLabel.setText("<html><body>Artist confirmed!<br>Searching for albums...</body></html>");
        spotifyLoadingLabel.setIcon(IconUtils.get(IconUtils.LOADING));
        spotifyProgressBar.setValue(0);
        spotifyStatusLabel.setText("Searching for albums from " + artist.getName() + "...");
        spotifyConfirmButton.setEnabled(false);
        spotifyPreviousButton.setEnabled(false);
        spotifyNextButton.setEnabled(false);

        // do the search
        getSpotifyAlbumSearchWorker().execute();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dropImageLabel;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel googleAlbumArtImageLabel;
    private javax.swing.JButton googleConfirmButton;
    private javax.swing.JButton googleImagesButton;
    private javax.swing.JLabel googleLoadingLabel;
    private javax.swing.JButton googleNextButton;
    private javax.swing.JButton googlePreviousButton;
    private javax.swing.JProgressBar googleProgressBar;
    private javax.swing.JButton googleSearchButton;
    private javax.swing.JPanel googleSearchPane;
    private javax.swing.JTextField googleSearchQueryTextField;
    private javax.swing.JLabel googleSizeLabel;
    private javax.swing.JLabel googleSourceLabel;
    private javax.swing.JLabel googleStatusLabel;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JButton manualConfirmButton;
    private javax.swing.JPanel manualImagePane;
    private javax.swing.JTextField manualSearchQueryTextField;
    private javax.swing.JLabel manualSizeLabel;
    private javax.swing.JLabel manualStatusLabel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem resetMenuItem;
    private javax.swing.JLabel searchQueryLabel;
    private javax.swing.JLabel searchQueryLabel1;
    private javax.swing.JLabel spotifyAlbumArtImageLabel;
    private javax.swing.JTextField spotifyAlbumTextField;
    private javax.swing.JLabel spotifyApiStatusLabel;
    private javax.swing.JLabel spotifyArtistImageLabel;
    private javax.swing.JTextField spotifyArtistTextField;
    private javax.swing.JButton spotifyConfirmButton;
    private javax.swing.JLabel spotifyLoadingLabel;
    private javax.swing.JButton spotifyNextButton;
    private javax.swing.JPanel spotifyPane;
    private javax.swing.JButton spotifyPreviousButton;
    private javax.swing.JProgressBar spotifyProgressBar;
    private javax.swing.JButton spotifySearchButton;
    private javax.swing.JLabel spotifyStatusLabel;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
