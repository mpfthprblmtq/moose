/*
   Proj:   Moose
   File:   AlbumArtFinderFrame.java
   Desc:   The UI class for the album art finder service

   Copyright Pat Ripley 2018
 */

// package
package moose.views.modals;

// imports
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
import moose.Moose;
import moose.objects.api.imageSearch.ImageSearchQuery;
import moose.objects.api.imageSearch.ImageSearchResponse;
import moose.objects.api.spotify.Album;
import moose.objects.api.spotify.AlbumSearchResponse;
import moose.objects.api.spotify.Artist;
import moose.services.AlbumArtFinderService;
import moose.services.IconService;
import moose.services.SpotifyApiService;
import moose.utilities.ImageUtils;

public class AlbumArtFinderFrame extends javax.swing.JFrame {

    static Logger logger = Moose.getLogger();

    List<Icon> icons = new ArrayList<>();
    int currentIconIndex;

    AlbumArtFinderService albumArtFinderService = new AlbumArtFinderService();
    SpotifyApiService spotifyApiService = new SpotifyApiService();

    // <editor-fold defaultstate="collapsed" desc="Google Search Swing Worker">
    SwingWorker<Void, Void> // make a swing worker do the image search in a separate thread so I can update the GUI
            googleSearchWorker = new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() {

            // get the query
            String query = googleSearchQueryTextField.getText();

            // iteratively make the calls so that the progress bar can be updated
            albumArtFinderService.makeFirstCall(query);
            googleProgressBar.setValue(8);
            albumArtFinderService.updateAlbumArtSettings();
            googleProgressBar.setValue(16);
            albumArtFinderService.makeSecondCall(query);
            googleProgressBar.setValue(24);
            albumArtFinderService.updateAlbumArtSettings();
            googleProgressBar.setValue(32);
            albumArtFinderService.makeThirdCall(query);
            googleProgressBar.setValue(40);
            albumArtFinderService.updateAlbumArtSettings();
            googleProgressBar.setValue(48);
            albumArtFinderService.makeFourthCall(query);
            googleProgressBar.setValue(56);
            albumArtFinderService.updateAlbumArtSettings();
            googleProgressBar.setValue(64);

            googleImageSearchResponses = albumArtFinderService.getResponses();
            double interval = googleImageSearchResponses.size() / 36.0;
            List<ImageSearchResponse> toRemoveList = new ArrayList<>();
            googleImageSearchResponses.forEach((isr) -> {
                ImageSearchResponse toRemove = albumArtFinderService.processImage(isr);
                if (toRemove != null) {
                    toRemoveList.add(toRemove);
                }
                googleProgressBar.setValue((int) (googleProgressBar.getValue() + Math.floor(interval)));
            });
            googleImageSearchResponses.removeAll(toRemoveList);
            googleProgressBar.setValue(100);
            googleLoadingLabel.setIcon(IconService.get(IconService.SUCCESS));

            icons = getIconsFromImages(googleImageSearchResponses);
            if (icons.size() > 1) {
                googleAlbumArtImageLabel.setIcon(icons.get(0));
                currentIconIndex = 0;
                googleNextButton.setEnabled(true);
                googlePrevButton.setEnabled(false);
                googleSearchButton.setEnabled(true);
            } else if (icons.size() == 1) {
                googleAlbumArtImageLabel.setIcon(icons.get(0));
                currentIconIndex = 0;
                googleNextButton.setEnabled(false);
                googlePrevButton.setEnabled(false);
                googleSearchButton.setEnabled(true);
            } else {
                googleAlbumArtImageLabel.setIcon(null);
                currentIconIndex = -1;
                googleNextButton.setEnabled(false);
                googlePrevButton.setEnabled(false);
                googleStatusLabel.setText("No results found!");
                googleLoadingLabel.setIcon(null);
                googleSearchButton.setEnabled(true);
                return null;
            }

            googleStatusLabel.setText("Result 1 of " + googleImageSearchResponses.size());
            googleSourceLabel.setText(googleImageSearchResponses.get(currentIconIndex).getLink());
            googleSourceLabel.setToolTipText(googleImageSearchResponses.get(currentIconIndex).getLink());
            sizeLabel.setText(googleImageSearchResponses.get(currentIconIndex).getBImage().getWidth() + "x" + googleImageSearchResponses.get(currentIconIndex).getBImage().getHeight());
            googleConfirmButton.setEnabled(true);
            googleImagesButton.setEnabled(true);

            return null;    // don't return anything since we're just playing with threads
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Spotify Search Swing Worker">
    SwingWorker<Void, Void> // make a swing worker do the spotify search in a separate thread so I can update the GUI
            spotifySearchWorker = new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() {

            // authenticate first
            spotifyApiService.authenticate();
            spotifyProgressBar.setValue(24);

            // get the artist
            Artist artist;
            try {
                artist = spotifyApiService.getArtistFromSearch(query.getArtist());
                spotifyProgressBar.setValue(48);
            } catch (IOException e) {
                if (e.getClass() == MalformedURLException.class) {
                    logger.logError("Exception while forming URL with artist query: " + query.getArtist(), e);
                } else {
                    logger.logError("Exception while getting artist info from Spotify API!", e);
                }
                return null;
            }

            // search for the album
            Album album = null;
            try {
                String nextUrl = null;
                while (true) {
                    AlbumSearchResponse albums = spotifyApiService.getAlbumFromArtist(artist.getId(), nextUrl);
                    for (Album albumInList : albums.getAlbums()) {
                        if (StringUtils.equalsIgnoreCase(albumInList.getName(), query.getAlbum())) {
                            album = albumInList;
                            spotifyProgressBar.setValue(72);
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
                        spotifyProgressBar.setValue(84);
                    } else {
                        // if the next url is null, we can't search anymore, album wasn't found
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
            String url = album.getImage().getUrl();
            spotifyLoadingLabel.setIcon(IconService.get(IconService.SUCCESS));
            foundCover = ImageUtils.getImageFromUrl(url);
            spotifyAlbumArtImageLabel.setIcon(getIconFromImage(foundCover));
            spotifyProgressBar.setValue(100);
            spotifyStatusLabel.setText("Cover found!");
            spotifyConfirmButton.setEnabled(true);
            spotifyArtistTextField.setEnabled(true);
            spotifyAlbumTextField.setEnabled(true);

            return null;    // don't return anything since we're just playing with threads
        }
    };
    // </editor-fold>

    ImageSearchQuery query;
    List<ImageSearchResponse> googleImageSearchResponses;

    BufferedImage foundCover;
    File dir;
    List<Integer> rows;

    /**
     * Creates new form AlbumArtFinderFrame
     *
     * @param query, the ImageSearchQuery to search by
     */
    public AlbumArtFinderFrame(ImageSearchQuery query) {
        initComponents();
        this.query = query;
        headerLabel.setText("<html>Finding album art for:<br>\"" + query.getArtist() + " - " + query.getAlbum() + "\"</html>");
        googleSearchQueryTextField.setText(query.getArtist() + StringUtils.SPACE + query.getAlbum());
        spotifyArtistTextField.setText(getPrimaryArtist(query.getArtist()));
        spotifyAlbumTextField.setText(query.getAlbum());
        this.dir = query.getDir();
        this.rows = query.getRows();
    }

    /**
     * Gets the primary artist from the given artist (really just the first one)
     * @param artist the artist to split up
     */
    public String getPrimaryArtist(String artist) {
        String[] individualArtists = artist.split("( & )|( x )|( X )|(, & )|(, )");
        return individualArtists[0];
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
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
        googleSearchPane = new javax.swing.JPanel();
        searchQueryLabel = new javax.swing.JLabel();
        googleSearchQueryTextField = new javax.swing.JTextField();
        googleSearchButton = new javax.swing.JButton();
        googleSourceLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        sizeLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        googleLoadingLabel = new javax.swing.JLabel();
        googleAlbumArtImageLabel = new javax.swing.JLabel();
        googleConfirmButton = new javax.swing.JButton();
        googlePrevButton = new javax.swing.JButton();
        googleNextButton = new javax.swing.JButton();
        googleStatusLabel = new javax.swing.JLabel();
        googleProgressBar = new javax.swing.JProgressBar();
        manualImagePane = new javax.swing.JPanel();
        googleImagesButton = new javax.swing.JButton();
        headerLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Album Art Finder");
        setAlwaysOnTop(true);
        setResizable(false);

        jTabbedPane1.setForeground(new java.awt.Color(1, 1, 1));
        jTabbedPane1.setToolTipText("");

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

        jLabel3.setText("Artist:");

        jLabel4.setText("Album:");

        spotifySearchButton.setText("Search");
        spotifySearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spotifySearchButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout spotifyPaneLayout = new javax.swing.GroupLayout(spotifyPane);
        spotifyPane.setLayout(spotifyPaneLayout);
        spotifyPaneLayout.setHorizontalGroup(
            spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spotifyPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, spotifyPaneLayout.createSequentialGroup()
                        .addGap(81, 81, 81)
                        .addComponent(spotifyConfirmButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(81, 81, 81))
                    .addComponent(spotifyStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(spotifyPaneLayout.createSequentialGroup()
                        .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spotifyArtistTextField)
                            .addComponent(spotifyAlbumTextField)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, spotifyPaneLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(spotifyPaneLayout.createSequentialGroup()
                                .addComponent(spotifyLoadingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spotifyProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(spotifyAlbumArtImageLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, spotifyPaneLayout.createSequentialGroup()
                        .addComponent(spotifyApiStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spotifySearchButton)))
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
                    .addComponent(spotifySearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                    .addComponent(spotifyApiStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(spotifyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(spotifyProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spotifyLoadingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spotifyAlbumArtImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spotifyConfirmButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spotifyStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Spotify", spotifyPane);

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

        googlePrevButton.setText("<");
        googlePrevButton.setEnabled(false);
        googlePrevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googlePrevButtonActionPerformed(evt);
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
                                .addComponent(sizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(googleSearchPaneLayout.createSequentialGroup()
                                .addComponent(googlePrevButton)
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
                    .addComponent(sizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(googleLoadingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(googleProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(googleAlbumArtImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(googleSearchPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(googleConfirmButton)
                    .addComponent(googlePrevButton)
                    .addComponent(googleNextButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(googleStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Google Image Search", googleSearchPane);

        googleImagesButton.setText("Open Google Images");
        googleImagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googleImagesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout manualImagePaneLayout = new javax.swing.GroupLayout(manualImagePane);
        manualImagePane.setLayout(manualImagePaneLayout);
        manualImagePaneLayout.setHorizontalGroup(
            manualImagePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, manualImagePaneLayout.createSequentialGroup()
                .addGap(0, 112, Short.MAX_VALUE)
                .addComponent(googleImagesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        manualImagePaneLayout.setVerticalGroup(
            manualImagePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, manualImagePaneLayout.createSequentialGroup()
                .addContainerGap(494, Short.MAX_VALUE)
                .addComponent(googleImagesButton)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Manual", manualImagePane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(headerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 575, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void googleConfirmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleConfirmButtonActionPerformed
        foundCover = googleImageSearchResponses.get(currentIconIndex).getBImage();
        confirmImage();
    }//GEN-LAST:event_googleConfirmButtonActionPerformed

    private void googleSearchQueryTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_googleSearchQueryTextFieldKeyReleased
        googleSearchButton.setEnabled(!googleSearchQueryTextField.getText().equals(""));
    }//GEN-LAST:event_googleSearchQueryTextFieldKeyReleased

    private void googleNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleNextButtonActionPerformed
        if (currentIconIndex < icons.size() - 1) {
            currentIconIndex++;
            googleAlbumArtImageLabel.setIcon(icons.get(currentIconIndex));
            googlePrevButton.setEnabled(true);

            if (currentIconIndex == icons.size() - 1) {
                googleNextButton.setEnabled(false);
                googlePrevButton.setEnabled(true);
            }
        }

        googleSourceLabel.setText(googleImageSearchResponses.get(currentIconIndex).getLink());
        googleSourceLabel.setToolTipText(googleImageSearchResponses.get(currentIconIndex).getLink());
        sizeLabel.setText(googleImageSearchResponses.get(currentIconIndex).getBImage().getWidth() + "x" + googleImageSearchResponses.get(currentIconIndex).getBImage().getHeight());
        googleStatusLabel.setText("Result " + (currentIconIndex + 1) + " of " + googleImageSearchResponses.size());
    }//GEN-LAST:event_googleNextButtonActionPerformed

    private void googlePrevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googlePrevButtonActionPerformed
        if (currentIconIndex > 0) {
            currentIconIndex--;
            googleAlbumArtImageLabel.setIcon(icons.get(currentIconIndex));
            googleNextButton.setEnabled(true);

            if (currentIconIndex == 0) {
                googlePrevButton.setEnabled(false);
                googleNextButton.setEnabled(true);
            }
        }

        googleSourceLabel.setText(googleImageSearchResponses.get(currentIconIndex).getLink());
        googleSourceLabel.setToolTipText(googleImageSearchResponses.get(currentIconIndex).getLink());
        sizeLabel.setText(googleImageSearchResponses.get(currentIconIndex).getBImage().getWidth() + "x" + googleImageSearchResponses.get(currentIconIndex).getBImage().getHeight());
        googleStatusLabel.setText("Result " + (currentIconIndex + 1) + " of " + googleImageSearchResponses.size());
    }//GEN-LAST:event_googlePrevButtonActionPerformed

    private void googleImagesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleImagesButtonActionPerformed
        String url = AlbumArtFinderService.buildImageSearchQuery(googleSearchQueryTextField.getText());
        try {
            WebUtils.openPage(url);
        } catch (Exception e) {
            logger.logError("Couldn't open web page: " + url, e);
        }

        this.dispose();
    }//GEN-LAST:event_googleImagesButtonActionPerformed

    private void googleSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleSearchButtonActionPerformed
        doGoogleSearch();
    }//GEN-LAST:event_googleSearchButtonActionPerformed

    private void googleSearchQueryTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_googleSearchQueryTextFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            googleSearchButton.doClick();
        }
    }//GEN-LAST:event_googleSearchQueryTextFieldKeyPressed

    private void spotifyConfirmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spotifyConfirmButtonActionPerformed
        confirmImage();
    }//GEN-LAST:event_spotifyConfirmButtonActionPerformed

    private void spotifySearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spotifySearchButtonActionPerformed
        doSpotifySearch();
    }//GEN-LAST:event_spotifySearchButtonActionPerformed

    /**
     * Checks to see if the google search CSE id and google search api key before running the search
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
        if (AlbumArtFinderService.checkIfBelowLimit()) {

            // update the gui to signify it starting
            googleSourceLabel.setText(StringUtils.EMPTY);
            googleSourceLabel.setToolTipText(StringUtils.EMPTY);
            sizeLabel.setText(StringUtils.EMPTY);
            googleStatusLabel.setText("Searching...");
            googleLoadingLabel.setIcon(IconService.get(IconService.LOADING));
            googleSearchButton.setEnabled(false);
            googleConfirmButton.setEnabled(false);
            googleImagesButton.setEnabled(false);

            // do the search
            googleSearchWorker.execute();

        } else {
            // ruh roh, to many calls for today
            JOptionPane.showMessageDialog(null,
                    "API call limit reached for today!",
                    "API Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Checks to see if we have the client id/client secret before doing a search
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

        // update the gui to signify it starting
        spotifyStatusLabel.setText("Searching...");
        spotifyLoadingLabel.setIcon(IconService.get(IconService.LOADING));
        spotifySearchButton.setEnabled(false);
        spotifyArtistTextField.setEnabled(false);
        spotifyAlbumTextField.setEnabled(false);

        // do the search
        spotifySearchWorker.execute();
    }

    private List<Icon> getIconsFromImages(List<ImageSearchResponse> responses) {
        List<Icon> scaledIcons = new ArrayList<>();
        for (ImageSearchResponse isr : responses) {
            byte[] bytes = ImageUtils.getBytesFromBufferedImage(isr.getBImage());
            scaledIcons.add(ImageUtils.getScaledImage(bytes, 300));
        }
        return scaledIcons;
    }

    private Icon getIconFromImage(BufferedImage image) {
        byte[] bytes = ImageUtils.getBytesFromBufferedImage(image);
        return ImageUtils.getScaledImage(bytes, 300);
    }

    private void confirmImage() {
        int dim = Moose.getSettings().getPreferredCoverArtSize();
        File outputFile = ImageUtils.createImageFile(foundCover, dir, dim);
        assert outputFile != null;
        if (outputFile.exists()) {
            for (Integer row : this.rows) {
                Moose.frame.songController.autoTaggingService.addIndividualCover(row, outputFile);
            }
        }
        Moose.frame.updateMultiplePanelFields();
        this.dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            logger.logError("Exception thrown while adding theme to Swing GUI!", ex);
        }
        //</editor-fold>
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel googleAlbumArtImageLabel;
    private javax.swing.JButton googleConfirmButton;
    private javax.swing.JButton googleImagesButton;
    private javax.swing.JLabel googleLoadingLabel;
    private javax.swing.JButton googleNextButton;
    private javax.swing.JButton googlePrevButton;
    private javax.swing.JProgressBar googleProgressBar;
    private javax.swing.JButton googleSearchButton;
    private javax.swing.JPanel googleSearchPane;
    private javax.swing.JTextField googleSearchQueryTextField;
    private javax.swing.JLabel googleSourceLabel;
    private javax.swing.JLabel googleStatusLabel;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel manualImagePane;
    private javax.swing.JLabel searchQueryLabel;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JLabel spotifyAlbumArtImageLabel;
    private javax.swing.JTextField spotifyAlbumTextField;
    private javax.swing.JLabel spotifyApiStatusLabel;
    private javax.swing.JTextField spotifyArtistTextField;
    private javax.swing.JButton spotifyConfirmButton;
    private javax.swing.JLabel spotifyLoadingLabel;
    private javax.swing.JPanel spotifyPane;
    private javax.swing.JProgressBar spotifyProgressBar;
    private javax.swing.JButton spotifySearchButton;
    private javax.swing.JLabel spotifyStatusLabel;
    // End of variables declaration//GEN-END:variables
}
