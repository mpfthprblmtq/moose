/**
 *  Proj:   Moose
 *  File:   SettingsFrame.java
 *  Desc:   Main UI class for the JFrame containing the settings and options.
 *          Works with the SettingsController to load and update settings, this class just handles all the UI.
 *
 *  Copyright Pat Ripley 2018
 */

// package
package moose.views;

// imports
import moose.Main;
import moose.controllers.*;
import moose.utilities.*;

import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

// class SettingsFrame
public class SettingsFrame extends javax.swing.JFrame {

    // controller
    public SettingsController settingsController = new SettingsController();

    // logger object
    Logger logger = Main.getLogger();

    // JList model
    DefaultListModel<String> genreListModel = new DefaultListModel<>();

    /**
     * Creates new form SettingsFrame
     */
    public SettingsFrame() {

        settingsController.setUpSupportDirectory();

        // initially load the settings
        settingsController.readSettingsFile();

        // init the components
        initComponents();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        genreList = new javax.swing.JList<>();
        genreTextField = new javax.swing.JTextField();
        addGenreButton = new javax.swing.JButton();
        deleteGenreButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        openErrorLogButton = new javax.swing.JButton();
        openEventLogButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        debugCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        clearEventLogButton = new javax.swing.JButton();
        clearErrorLogButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Settings");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        jLabel1.setText("Genres:");

        genreList.setModel(getGenreListModel());
        genreList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        genreList.setMaximumSize(new java.awt.Dimension(200, 300));
        genreList.setMinimumSize(new java.awt.Dimension(200, 300));
        genreList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                genreListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(genreList);

        genreTextField.setMaximumSize(new java.awt.Dimension(167, 26));
        genreTextField.setMinimumSize(new java.awt.Dimension(167, 26));
        genreTextField.setPreferredSize(new java.awt.Dimension(167, 26));

        addGenreButton.setText("Add");
        addGenreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGenreButtonActionPerformed(evt);
            }
        });

        deleteGenreButton.setText("Delete");
        deleteGenreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteGenreButtonActionPerformed(evt);
            }
        });

        jLabel8.setText("Genre to add:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addGenreButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteGenreButton, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1))
                            .addComponent(genreTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(34, 34, 34)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(genreTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addGenreButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteGenreButton)
                        .addGap(0, 132, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Genres", jPanel1);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        jLabel2.setText("Debugging");

        openErrorLogButton.setText("Open Error Log");
        openErrorLogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openErrorLogButtonActionPerformed(evt);
            }
        });

        openEventLogButton.setText("Open Event Log");
        openEventLogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openEventLogButtonActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        jLabel3.setText("Logging");

        debugCheckBox.setSelected(settingsController.getDebugMode());
        debugCheckBox.setText("Enable Enhanced Debugging");
        debugCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debugCheckBoxActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel4.setText(System.getProperty("user.home") + "/Library/Application Support/Moose/Logs/errorLog.log");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabel5.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel5.setText(System.getProperty("user.home") + "/Library/Application Support/Moose/Logs/eventLog.log");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        clearEventLogButton.setText("Clear Event Log");
        clearEventLogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearEventLogButtonActionPerformed(evt);
            }
        });

        clearErrorLogButton.setText("Clear Error Log");
        clearErrorLogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearErrorLogButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(openEventLogButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 84, Short.MAX_VALUE)
                        .addComponent(clearEventLogButton))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(debugCheckBox, javax.swing.GroupLayout.Alignment.LEADING))
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(openErrorLogButton, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clearErrorLogButton)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(debugCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openEventLogButton)
                    .addComponent(clearEventLogButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openErrorLogButton)
                    .addComponent(clearErrorLogButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addContainerGap(97, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Logging", jPanel2);

        jLabel6.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        jLabel6.setText("Music Library Location:");

        jLabel7.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jLabel7.setText(settingsController.getLibraryLocation());
        jLabel7.setToolTipText("");
        jLabel7.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        browseButton.setText("Browse...");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(0, 185, Short.MAX_VALUE))
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(browseButton))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(browseButton)
                .addContainerGap(238, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Files", jPanel3);

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jButton4.setText("Set Defaults");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton)
                    .addComponent(jButton4))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event for addButton 
     * Checks if you're adding a new value or editing an existing value
     * @param evt
     */
    private void addGenreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGenreButtonActionPerformed
        if (addGenreButton.getText().equals("Add")) {
            addGenreToList(genreTextField.getText());
        } else if (addGenreButton.getText().equals("Submit")) {
            submitGenreChange(genreList.getSelectedValue(), genreTextField.getText());
        }
    }//GEN-LAST:event_addGenreButtonActionPerformed

    /**
     * Event for deleteButton
     * @param evt 
     */
    private void deleteGenreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteGenreButtonActionPerformed
        removeGenreFromList(genreTextField.getText());
    }//GEN-LAST:event_deleteGenreButtonActionPerformed

    /**
     * Event for the debuggingmode checkbox
     * @param evt 
     */
    private void debugCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debugCheckBoxActionPerformed
        settingsController.setDebugMode(debugCheckBox.isSelected());
    }//GEN-LAST:event_debugCheckBoxActionPerformed

    /**
     * Event for the default button
     * @param evt 
     */
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        settingsController.setDefaults();
    }//GEN-LAST:event_jButton4ActionPerformed

    /**
     * Event for the save button
     * @param evt 
     */
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        settingsController.writeSettingsFile();
    }//GEN-LAST:event_saveButtonActionPerformed

    /**
     * Event for the JList click
     * Sets the value of the text field and changes the button text to reflect the current action
     * @param evt 
     */
    private void genreListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_genreListMouseClicked
        addGenreButton.setText("Submit");
        genreTextField.setText(genreList.getSelectedValue());
    }//GEN-LAST:event_genreListMouseClicked

    /**
     * Event for the open eventlog button
     * @param evt 
     */
    private void openEventLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openEventLogButtonActionPerformed
        settingsController.openEventLog();
    }//GEN-LAST:event_openEventLogButtonActionPerformed

    /**
     * Event for the open errorlog button
     * @param evt 
     */
    private void openErrorLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openErrorLogButtonActionPerformed
        settingsController.openErrorLog();
    }//GEN-LAST:event_openErrorLogButtonActionPerformed

    /**
     * Event for the clear eventlog button
     * @param evt 
     */
    private void clearEventLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearEventLogButtonActionPerformed
        int returnVal = JOptionPane.showConfirmDialog(
                null, 
                "Are you sure you want to clear the event log?", 
                "Confirm Clear", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
        if(returnVal == 0) {
            settingsController.clearEventLog();
        }
    }//GEN-LAST:event_clearEventLogButtonActionPerformed

    /**
     * Event for the clear errorlog button
     * @param evt 
     */
    private void clearErrorLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearErrorLogButtonActionPerformed
        int returnVal = JOptionPane.showConfirmDialog(
                null, 
                "Are you sure you want to clear the error log?", 
                "Confirm Clear", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
        if(returnVal == 0) {
            settingsController.clearErrorLog();
        }
    }//GEN-LAST:event_clearErrorLogButtonActionPerformed

    /**
     * Event for the browse button on the files tab
     * @param evt 
     */
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        // get the folder through a JFileChooser
        File dir = Utils.launchJFileChooser("Choose the directory you want to store music in...", "Select", JFileChooser.DIRECTORIES_ONLY, false)[0];
        if(dir != null) {
            settingsController.setLibraryLocation(dir.getAbsolutePath() + "/");
            jLabel7.setText(settingsController.getLibraryLocation());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    /**
     * Gets the list of genres and fill a DefaultListModel for the view
     * @return the listModel for the JList
     */
    public DefaultListModel getGenreListModel() {
        if (settingsController.getGenres() != null) {
            for (int i = 0; i < settingsController.getGenres().size(); i++) {
                genreListModel.add(i, settingsController.getGenres().get(i));
            }
            return genreListModel;
        } else {
            return new DefaultListModel();
        }
    }

    /**
     * Adds the genre to the ivar list and the JList model
     * @param genre the genre to add
     */
    public void addGenreToList(String genre) {
        settingsController.addGenre(genre);
        genreListModel.add(genreListModel.size(), genre);
    }

    /**
     * Removes the genre from the ivar list and the JList model
     * @param genre
     */
    public void removeGenreFromList(String genre) {

        // check if that element is actually in the list
        boolean result = genreListModel.removeElement(genre);
        if (!result) {
            JOptionPane.showMessageDialog(null, "Element was not in list!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            settingsController.removeGenre(genre);
            genreTextField.setText("");
        }
    }

    /**
     * Submits the change in the list and in the arraylist
     * @param oldGenre
     * @param newGenre
     */
    public void submitGenreChange(String oldGenre, String newGenre) {
        settingsController.getGenres().set(settingsController.getGenres().indexOf(oldGenre), newGenre);
        genreListModel.set(genreListModel.indexOf(oldGenre), newGenre);
        genreTextField.setText("");
        addGenreButton.setText("Add");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SettingsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SettingsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SettingsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SettingsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            //new SettingsFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addGenreButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton clearErrorLogButton;
    private javax.swing.JButton clearEventLogButton;
    private javax.swing.JCheckBox debugCheckBox;
    private javax.swing.JButton deleteGenreButton;
    private javax.swing.JList<String> genreList;
    private javax.swing.JTextField genreTextField;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton openErrorLogButton;
    private javax.swing.JButton openEventLogButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}
