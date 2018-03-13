package moose;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class Frame extends javax.swing.JFrame {

    //ArrayList<Song> songs = new ArrayList<>();
    //ArrayList<Song> edited_songs = new ArrayList<>();
    HashMap<Integer, Song> songs = new HashMap<>();
    HashMap<Integer, Song> edited_songs = new HashMap<>();

    ArrayList<File> files = new ArrayList<>();
    ArrayList<Icon> covers = new ArrayList<>();

    DefaultTableModel model;
    ActionListener menuListener;

    /**
     * Creates new form Frame
     */
    public Frame() {
        this.menuListener = (ActionEvent event) -> {
            if (event.getActionCommand().equals("Add")) {

            } else if (event.getActionCommand().equals("Remove")) {

            } else if (event.getActionCommand().equals("Remove from list")) {
                model.removeRow(table.getSelectedRow());
            }
        };
        initComponents();

        // taken from the FileDrop example
        FileDrop fileDrop = new FileDrop(System.out, tableSP, (java.io.File[] files) -> {
            for (File file : files) {
                addFileToTable(file);
            }
        });

        model = (DefaultTableModel) table.getModel();

//        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
//
//            @Override
//            protected void setValue(Object value) {
//                Object result = value;
//                if ((value != null) && (value instanceof File)) {
//                    File file = (File) value;
//                    result = file.getName();
//                    super.setValue(result);
//                    super.setToolTipText(file.toString());
//                }
//            }
//        });

        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableCellListener tcl = (TableCellListener) e.getSource();
                updateConsole("Row   : " + tcl.getRow());
                updateConsole("Column: " + tcl.getColumn());
                updateConsole("Old   : " + tcl.getOldValue());
                updateConsole("New   : " + tcl.getNewValue());
            }
        };

        TableCellListener tcl = new TableCellListener(table, action);

    }

    public void addFileToTable(File file) {

        if (!file.getAbsolutePath().endsWith(".mp3")) {
            updateConsole(file.getName() + " is not an mp3!");
        } else {

            Mp3File mp3file = null;
            try {
                mp3file = new Mp3File(file.getAbsolutePath());
                //System.out.println("Length of this mp3 is: " + mp3file.getLengthInSeconds() + " seconds");
                //System.out.println("Bitrate: " + mp3file.getBitrate() + " kbps " + (mp3file.isVbr() ? "(VBR)" : "(CBR)"));
                //System.out.println("Sample rate: " + mp3file.getSampleRate() + " Hz");
                //System.out.println("Has ID3v1 tag?: " + (mp3file.hasId3v1Tag() ? "YES" : "NO"));
                //System.out.println("Has ID3v2 tag?: " + (mp3file.hasId3v2Tag() ? "YES" : "NO"));
                //System.out.println("Has custom tag?: " + (mp3file.hasCustomTag() ? "YES" : "NO"));
                //System.out.println(mp3file.getId3v2Tag().getAlbumArtist());
                if (!mp3file.hasId3v2Tag()) {
                    updateConsole(file.getName() + " does not have an id3v2 tag!");
                }
            } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
            }

            // get the id3v2 info
            String title = mp3file.getId3v2Tag().getTitle();
            String artist = mp3file.getId3v2Tag().getArtist();
            String album = mp3file.getId3v2Tag().getAlbum();
            String albumartist = mp3file.getId3v2Tag().getAlbumArtist();
            String genre = mp3file.getId3v2Tag().getGenreDescription();
            String track = mp3file.getId3v2Tag().getTrack();
            String disk = mp3file.getId3v2Tag().getPartOfSet();
            byte[] artwork_bytes = mp3file.getId3v2Tag().getAlbumImage();

            // create a Phile object
            Phile phile = new Phile(file);
            
            // create a song object
            Song s = new Song(phile, title, artist, album, albumartist, genre, track, disk, artwork_bytes);

            // add the song to the list
            songs.put(songs.size()-1, s);

            // add the file to the files list and the artwork to the artwork list
            files.add(file);
            covers.add(s.getArtwork());

            // get the table model from the table
            //model = (DefaultTableModel) table.getModel();
            //add the song to the table
            model.addRow(new Object[]{
                s.getFile(),
                s.getTitle(),
                s.getArtist(),
                s.getAlbum(),
                s.getAlbumartist(),
                s.getGenre(),
                s.getFullTrackString(),
                s.getFullDiskString(),
                s.getArtwork()
            });
            //File f = (File) model.getValueAt(model.getRowCount() - 1, 0);
            //model.setValueAt(f.getName(), model.getRowCount() - 1, 0);

        }
    }

    public void updateConsole(String s) {
        console.append(s + "\n");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        container = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        tableSP = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        consoleSP = new javax.swing.JScrollPane();
        console = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Moose");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Moose");

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        DefaultTableModel tableModel = new DefaultTableModel()
        {
            @Override
            public Class getColumnClass(int column)
            {
                if (column == 8) { return ImageIcon.class; }
                else if (column == 0) { return Phile.class; }
                else { return Object.class; }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                if(column == 8) {
                    return false;
                } else if (column == 0) {
                    System.out.println("column 0 is in fact editable");
                    return true;
                }
                return true;
            }
        };
        table.setModel(tableModel);
        table.setRowHeight(20);
        tableModel.addColumn("Filename");
        tableModel.addColumn("Title");
        tableModel.addColumn("Artist");
        tableModel.addColumn("Album");
        tableModel.addColumn("Album Artist");
        tableModel.addColumn("Genre");
        tableModel.addColumn("Track");
        tableModel.addColumn("Disk");
        tableModel.addColumn("Artwork");
        table.setAutoCreateRowSorter(true);
        setColumnWidth(6, 50);
        setColumnWidth(7, 50);
        setColumnWidth(8, 110);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        tableSP.setViewportView(table);

        console.setEditable(false);
        console.setColumns(20);
        console.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        console.setRows(5);
        consoleSP.setViewportView(console);

        javax.swing.GroupLayout containerLayout = new javax.swing.GroupLayout(container);
        container.setLayout(containerLayout);
        containerLayout.setHorizontalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerLayout.createSequentialGroup()
                        .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, containerLayout.createSequentialGroup()
                                .addComponent(consoleSP, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveButton)))
                        .addGap(1004, 1004, 1004))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerLayout.createSequentialGroup()
                        .addComponent(tableSP)
                        .addContainerGap())))
        );
        containerLayout.setVerticalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(consoleSP, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .addGroup(containerLayout.createSequentialGroup()
                        .addComponent(saveButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(container, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(container, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked

        // check what type of click
        switch (evt.getButton()) {

            // if it's a right click
            case java.awt.event.MouseEvent.BUTTON3:
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0) {
                    table.setRowSelectionInterval(row, row);
                    if (col == 8) {
                        showArtworkPopup(evt);
                    } else {
                        showRegularPopup(evt);
                    }
                } else {
                    table.clearSelection();
                }
                break;

            // if it's a left click
            case java.awt.event.MouseEvent.BUTTON1:

                // check if double click
                if (evt.getClickCount() == 2) {
                    if (table.getSelectedColumn() == 8) {
                        showArtworkPopup(evt);
                    } 
                }
                

                break;

            // if it's a scroll click
            case java.awt.event.MouseEvent.BUTTON2:
                System.out.println("Scroll click");
                break;

            default:
                break;
        }


    }//GEN-LAST:event_tableMouseClicked

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        for (int i = 0; i < model.getRowCount(); i++) {

            System.out.println("Column 0 class: " + model.getColumnClass(0));
            
            //File f = (File) model.getValueAt(i, 0);
            //File f = (File) model.getValueAt(model.getRowCount() - 1, 0);
            Phile phile = (Phile) model.getValueAt(i, 0);
            String title = model.getValueAt(i, 1).toString();
            String artist = model.getValueAt(i, 2).toString();
            String album = model.getValueAt(i, 3).toString();
            String albumartist = model.getValueAt(i, 4).toString();
            String genre = model.getValueAt(i, 5).toString();
            String track = model.getValueAt(i, 6).toString();
            String disk = model.getValueAt(i, 7).toString();
            ImageIcon artwork = (ImageIcon) model.getValueAt(i, 8);

//            byte[] byteArray = null;
//            try {
//                ImageIcon artwork = (ImageIcon) model.getValueAt(i, 8);
//                BufferedImage bi = getBufferedImage(artwork.getImage());
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                ImageIO.write(bi, "png", baos);
//                byteArray = baos.toByteArray();
//
//            //preparedStatement.setBytes(1, byteArray);
//            } catch (IOException ex) {
//                System.err.println(ex);
//            }
            //File f = new File(songs.get(i).getFile().getAbsolutePath().replace(songs.get(i).getFile().getName(), "") + filename);
            edited_songs.put(i, new Song(phile, title, artist, album, albumartist, genre, track, disk, artwork));

        }
        removeUnchanged();
        
        submitChanges();


    }//GEN-LAST:event_saveButtonActionPerformed
//
//    public static BufferedImage getBufferedImage(Image img) {
//        if (img instanceof BufferedImage) {
//            return (BufferedImage) img;
//        }
//
//        BufferedImage bimage = new BufferedImage(img.getWidth(null),
//                img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//
//        Graphics2D bGr = bimage.createGraphics();
//        bGr.drawImage(img, 0, 0, null);
//        bGr.dispose();
//
//        // Return the buffered image
//        return bimage;
//    }

    void showArtworkPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        popup.add(item = new JMenuItem("Add"));
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem("Remove"));
        item.addActionListener(menuListener);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    void showRegularPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        popup.add(item = new JMenuItem("Remove from list"));
        item.addActionListener(menuListener);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    public int findSong(String title, String artist, String album) {
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getTitle().equals(title)
                    && songs.get(i).getArtist().equals(artist)
                    && songs.get(i).getAlbum().equals(album)) {
                return i;
            }
        }
        return -1;
    }

    public int getRow(String filename) {
        for (int i = 0; i < table.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(filename)) {
                return i;
            }
        }
        return -1;
    }

    public void removeUnchanged() {
        for (int i = 0; i < songs.size(); i++) {
            if (!songs.get(i).equals(edited_songs.get(i))) {
                updateConsole(songs.get(i).getTitle() + " was modified. Keeping.");
            } else {
                updateConsole(songs.get(i).getTitle() + " wasn't modified. Removing.");
                songs.remove(i);
                edited_songs.remove(i);
            }
        }
    }
    
    public void submitChanges() {
        
        Set<Integer> keys = songs.keySet();
        for (int key : keys) {
            File old_file = songs.get(key).getFile().getFile();
            File new_file = edited_songs.get(key).getFile().getFile();
            if(new_file.exists()) {
                updateConsole("ERROR: " + new_file + " already exists!");
            } else {
                old_file.renameTo(new_file);
            }
        }
    }

    /**
     * Sets the specified column width
     *
     * @param column, the column to set
     * @param width, width in pixels
     */
    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        if (width < 0) {
            // use the preferred width of the header..
            JLabel label = new JLabel((String) tableColumn.getHeaderValue());
            Dimension preferred = label.getPreferredSize();
            // altered 10->14 as per camickr comment.
            width = (int) preferred.getWidth() + 14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
 /*
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
         */
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            //new Frame().setVisible(true);
            Frame frame = new Frame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea console;
    private javax.swing.JScrollPane consoleSP;
    private javax.swing.JPanel container;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JButton saveButton;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableSP;
    // End of variables declaration//GEN-END:variables

}
