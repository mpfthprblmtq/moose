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
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import moose.Main;
import moose.objects.ImageSearchQuery;
import moose.objects.ImageSearchResponse;
import moose.services.AlbumArtFinderService;
import moose.utilities.ImageUtils;
import moose.utilities.StringUtils;
import moose.utilities.WebUtils;
import moose.utilities.logger.Logger;

public class AlbumArtFinderFrame extends javax.swing.JFrame {

    static Logger logger = Main.getLogger();

    List<Icon> icons = new ArrayList<>();
    int currentIconIndex;
    AlbumArtFinderService albumArtFinderService = new AlbumArtFinderService();

    SwingWorker<Void, Void> // make a swing worker do the image search in a separate thread so I can update the GUI
            worker = new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() {

            // get the query
            String query = queryTextField.getText();

            // iteratively make the calls so that the progress bar can be updated
            albumArtFinderService.makeFirstCall(query);
            progressBar.setValue(8);
            albumArtFinderService.updateAlbumArtSettings();
            progressBar.setValue(16);
            albumArtFinderService.makeSecondCall(query);
            progressBar.setValue(24);
            albumArtFinderService.updateAlbumArtSettings();
            progressBar.setValue(32);
            albumArtFinderService.makeThirdCall(query);
            progressBar.setValue(40);
            albumArtFinderService.updateAlbumArtSettings();
            progressBar.setValue(48);
            albumArtFinderService.makeFourthCall(query);
            progressBar.setValue(56);
            albumArtFinderService.updateAlbumArtSettings();
            progressBar.setValue(64);

            responses = albumArtFinderService.getResponses();
            double interval = responses.size() / 36.0;
            List<ImageSearchResponse> toRemoveList = new ArrayList<>();
            responses.forEach((isr) -> {
                ImageSearchResponse toRemove = albumArtFinderService.processImage(isr);
                if (toRemove != null) {
                    toRemoveList.add(toRemove);
                }
                progressBar.setValue((int) (progressBar.getValue() + Math.floor(interval)));
            });
            responses.removeAll(toRemoveList);
            progressBar.setValue(100);
            loadingLabel.setIcon(new ImageIcon(this.getClass().getResource("/resources/check3.png")));


            icons = getIconsFromImages(responses);
            if (icons.size() > 1) {
                albumArtImageLabel.setIcon(icons.get(0));
                currentIconIndex = 0;
                nextButton.setEnabled(true);
                prevButton.setEnabled(false);
                searchButton.setEnabled(true);
            } else if (icons.size() == 1) {
                albumArtImageLabel.setIcon(icons.get(0));
                currentIconIndex = 0;
                nextButton.setEnabled(false);
                prevButton.setEnabled(false);
                searchButton.setEnabled(true);
            } else {
                albumArtImageLabel.setIcon(null);
                currentIconIndex = -1;
                nextButton.setEnabled(false);
                prevButton.setEnabled(false);
                statusLabel.setText("No results found!");
                loadingLabel.setIcon(null);
                searchButton.setEnabled(true);
                return null;
            }

            statusLabel.setText("Result 1 of " + responses.size());
            sourceLabel.setText(responses.get(currentIconIndex).getLink());
            sourceLabel.setToolTipText(responses.get(currentIconIndex).getLink());
            sizeLabel.setText(responses.get(currentIconIndex).getBImage().getWidth() + "x" + responses.get(currentIconIndex).getBImage().getHeight());
            confirmButton.setEnabled(true);
            googleImagesButton.setEnabled(true);

            return null;    // don't return anything since we're just playing with threads
        }
    };

    List<ImageSearchResponse> responses;
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
        headerLabel.setText("<html>Finding album art for:<br>\"" + query.getQuery() + "\"</html>");
        queryTextField.setText(query.getQuery());
        queryTextField.requestFocus();
        queryTextField.selectAll();
        this.dir = query.getDir();
        this.rows = query.getRows();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerLabel = new javax.swing.JLabel();
        L1 = new javax.swing.JLabel();
        queryTextField = new javax.swing.JTextField();
        confirmButton = new javax.swing.JButton();
        albumArtImageLabel = new javax.swing.JLabel();
        nextButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        googleImagesButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        sizeLabel = new javax.swing.JLabel();
        searchButton = new javax.swing.JButton();
        loadingLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Album Art Finder");
        setAlwaysOnTop(true);
        setResizable(false);

        L1.setText("Search Query:");

        queryTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                queryTextFieldKeyPressed(evt);
            }

            public void keyReleased(java.awt.event.KeyEvent evt) {
                queryTextFieldKeyReleased(evt);
            }
        });

        confirmButton.setText("Confirm");
        confirmButton.setEnabled(false);
        confirmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmButtonActionPerformed(evt);
            }
        });

        albumArtImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        albumArtImageLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        albumArtImageLabel.setMaximumSize(new java.awt.Dimension(250, 250));
        albumArtImageLabel.setMinimumSize(new java.awt.Dimension(250, 250));
        albumArtImageLabel.setSize(new java.awt.Dimension(153, 153));

        nextButton.setText(">");
        nextButton.setEnabled(false);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        prevButton.setText("<");
        prevButton.setEnabled(false);
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });

        googleImagesButton.setText("Open Google Images");
        googleImagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googleImagesButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Source:");

        jLabel2.setText("Size:");

        sourceLabel.setMaximumSize(new java.awt.Dimension(310, 16));
        sourceLabel.setMinimumSize(new java.awt.Dimension(310, 16));
        sourceLabel.setSize(new java.awt.Dimension(310, 16));

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        loadingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(headerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jLabel1)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(sourceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                .addComponent(L1, javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(queryTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addComponent(searchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                                .addComponent(googleImagesButton, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                                                                .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                                .addGroup(layout.createSequentialGroup()
                                                                                        .addComponent(prevButton)
                                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                        .addComponent(nextButton))
                                                                                .addComponent(confirmButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(loadingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(albumArtImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(59, 60, Short.MAX_VALUE))
                                                                        .addComponent(sizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                                .addContainerGap())))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(headerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(L1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(queryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(searchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(sourceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel2)
                                        .addComponent(sizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(loadingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(albumArtImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(confirmButton)
                                        .addComponent(googleImagesButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(nextButton)
                                        .addComponent(prevButton)
                                        .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(11, 11, 11))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void confirmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmButtonActionPerformed
        confirmImage();
    }//GEN-LAST:event_confirmButtonActionPerformed

    private void queryTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_queryTextFieldKeyReleased
        searchButton.setEnabled(!queryTextField.getText().equals(""));
    }//GEN-LAST:event_queryTextFieldKeyReleased

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        if (currentIconIndex < icons.size() - 1) {
            currentIconIndex++;
            albumArtImageLabel.setIcon(icons.get(currentIconIndex));
            prevButton.setEnabled(true);

            if (currentIconIndex == icons.size() - 1) {
                nextButton.setEnabled(false);
                prevButton.setEnabled(true);
            }
        }

        sourceLabel.setText(responses.get(currentIconIndex).getLink());
        sourceLabel.setToolTipText(responses.get(currentIconIndex).getLink());
        sizeLabel.setText(responses.get(currentIconIndex).getBImage().getWidth() + "x" + responses.get(currentIconIndex).getBImage().getHeight());
        statusLabel.setText("Result " + (currentIconIndex + 1) + " of " + responses.size());
    }//GEN-LAST:event_nextButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevButtonActionPerformed
        if (currentIconIndex > 0) {
            currentIconIndex--;
            albumArtImageLabel.setIcon(icons.get(currentIconIndex));
            nextButton.setEnabled(true);

            if (currentIconIndex == 0) {
                prevButton.setEnabled(false);
                nextButton.setEnabled(true);
            }
        }

        sourceLabel.setText(responses.get(currentIconIndex).getLink());
        sourceLabel.setToolTipText(responses.get(currentIconIndex).getLink());
        sizeLabel.setText(responses.get(currentIconIndex).getBImage().getWidth() + "x" + responses.get(currentIconIndex).getBImage().getHeight());
        statusLabel.setText("Result " + (currentIconIndex + 1) + " of " + responses.size());
    }//GEN-LAST:event_prevButtonActionPerformed

    private void googleImagesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleImagesButtonActionPerformed
        WebUtils.openPage(AlbumArtFinderService.buildImageSearchQuery(queryTextField.getText()));
        this.dispose();
    }//GEN-LAST:event_googleImagesButtonActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        doSearch();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void queryTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_queryTextFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            searchButton.doClick();
        }
    }//GEN-LAST:event_queryTextFieldKeyPressed

    public void doSearch() {

        // check to see if we have a valid api key and cse id
        if (StringUtils.isEmpty(Main.getSettings().getAlbumArtFinderApiKey())
                || StringUtils.isEmpty(Main.getSettings().getAlbumArtFinderCseId())) {
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
            sourceLabel.setText("");
            sourceLabel.setToolTipText("");
            sizeLabel.setText("");
            statusLabel.setText("Searching...");
            loadingLabel.setIcon(new ImageIcon(this.getClass().getResource("/resources/loading-icon-2.gif")));
            searchButton.setEnabled(false);
            confirmButton.setEnabled(false);
            googleImagesButton.setEnabled(false);

            // do the search
            worker.execute();

        } else {
            // ruh roh, to many calls for today
            JOptionPane.showMessageDialog(null,
                    "API call limit reached for today!",
                    "API Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setProgress(int percentage) {

    }

    private List<Icon> getIconsFromImages(List<ImageSearchResponse> responses) {
        List<Icon> scaledIcons = new ArrayList<>();
        for (ImageSearchResponse isr : responses) {
            byte[] bytes = ImageUtils.getBytesFromBufferedImage(isr.getBImage());
            scaledIcons.add(ImageUtils.getScaledImage(bytes, 250));
        }
        return scaledIcons;
    }

    private void confirmImage() {
        foundCover = responses.get(currentIconIndex).getBImage();
        int dim = Main.getSettings().getPreferredCoverArtSize();
        File outputFile = ImageUtils.createImageFile(foundCover, dir, dim);
        assert outputFile != null;
        if (outputFile.exists()) {
            for (Integer row : this.rows) {
                Main.frame.songController.autoTaggingService.addIndividualCover(row, outputFile);
            }
        }
        Main.frame.setMultiplePanelFields();
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
    private javax.swing.JLabel L1;
    private javax.swing.JLabel albumArtImageLabel;
    private javax.swing.JButton confirmButton;
    private javax.swing.JButton googleImagesButton;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel loadingLabel;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextField queryTextField;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
}
