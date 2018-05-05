package moose;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.mpatric.mp3agic.NotSupportedException;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class Frame extends javax.swing.JFrame {

    HashMap<Integer, Song> songs = new HashMap<>();

    DefaultTableModel model;
    ActionListener menuListener;

    private static final int DEFAULT = 0;
    private static final int EDITED = 1;
    private static final int SAVED = 2;

    /**
     * Creates new form Frame
     */
    public Frame() {
        this.menuListener = (ActionEvent event) -> {
            if (event.getActionCommand().equals("Add")) {
                addAlbumArt();
            } else if (event.getActionCommand().equals("Remove")) {
                removeAlbumArt();
            } else if (event.getActionCommand().equals("Remove from list")) {
                model.removeRow(table.getSelectedRow());
            } else if (event.getActionCommand().equals("Play")) {
                try {
                    File file = (File) model.getValueAt(table.getSelectedRow(), 1);
                    Desktop desktop = Desktop.getDesktop();
                    if(file.exists()) desktop.open(file);
                } catch (IOException ex) {
                    System.err.println(ex);
                }
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

        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableCellListener tcl = (TableCellListener) e.getSource();

                setRowIcon(EDITED, tcl.getRow());

                int r = tcl.getRow();
                int c = tcl.getColumn();
                int index = Integer.valueOf(model.getValueAt(r, 11).toString());
                
                switch (c) {
                    case 2:     // filename was changed
                        File old_file = (File) model.getValueAt(r, 1);
                        String path = old_file.getPath().replace(old_file.getName(), "");
                        String fileName = model.getValueAt(r, c).toString();
                        File new_file = new File(path + "//" + fileName + ".mp3");
                        songs.get(index).setFile(new_file);
                        model.setValueAt(new_file, r, 1);
                        
                        old_file.renameTo(new_file);
                        
                        break;
                    case 3:     // title was changed
                        songs.get(index).setTitle(tcl.getNewValue().toString());
                        break;
                    case 4:     // artist was changed
                        songs.get(index).setArtist(model.getValueAt(r, c).toString());
                        break;
                    case 5:     // album was changed
                        songs.get(index).setAlbum(model.getValueAt(r, c).toString());
                        break;
                    case 6:     // album artist was changed
                        songs.get(index).setAlbumartist(model.getValueAt(r, c).toString());
                        break;
                    case 7:     // genre was changed
                        songs.get(index).setGenre(model.getValueAt(r, c).toString());
                        break;
                    case 8:     // tracks was changed
                        songs.get(index).setFullTrack(model.getValueAt(r, c).toString());
                        break;
                    case 9:     // disks was changed
                        songs.get(index).setFullDisk(model.getValueAt(r, c).toString());

                        Icon thumbnail_icon = null;
                        byte[] bytes = songs.get(index).getArtwork_bytes();
                        try {
                            // getting the image from the byte array
                            ImageIcon icon = new ImageIcon(bytes);
                            Image img = icon.getImage();
                            Image thumbnail = img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                            thumbnail_icon = new ImageIcon(thumbnail);
                            //this.artwork = new ImageIcon(img_scaled);

                            iipreview.setIcon(thumbnail_icon);
                        } catch (NullPointerException ex) {
                            System.err.println(ex);
                        }
                        break;
                    default:    // not accounted for
                        break;
                }
            }
        };

        TableCellListener tcl2 = new TableCellListener(table, action);
    }

    public void setRowIcon(int icon, int row) {
        switch (icon) {
            case DEFAULT:
                model.setValueAt(new ImageIcon("img//default.png"), row, 0);
                break;
            case EDITED:
                model.setValueAt(new ImageIcon("img//edit.png"), row, 0);
                break;
            case SAVED:
                model.setValueAt(new ImageIcon("img//check.png"), row, 0);
                break;
        }
    }

    public void addFileToTable(File file) {

        if (!file.getAbsolutePath().endsWith(".mp3")) {
            updateConsole(file.getName() + " is not an mp3!");
        } else {

            Mp3File mp3file = null;
            try {
                mp3file = new Mp3File(file.getAbsolutePath());
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

            // create a song object
            Song s = new Song(file, title, artist, album, albumartist, genre, track, disk, artwork_bytes);

            // make an index
            int index = songs.size() + 1;

            // add the song to the list
            songs.put(index, s);

            Icon thumbnail_icon = null;
            try {
                // getting the image from the byte array
                ImageIcon icon = new ImageIcon(artwork_bytes);
                Image img = icon.getImage();
                Image thumbnail = img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                thumbnail_icon = new ImageIcon(thumbnail);
                //this.artwork = new ImageIcon(img_scaled);
            } catch (NullPointerException e) {
                //System.err.println(e);
                // there was no album image or something
                //thumbnail_icon = new Icon();
            }

            //add the song to the table
            if (thumbnail_icon != null) {
                model.addRow(new Object[]{
                    new ImageIcon("img//default.png"),
                    s.getFile(),
                    s.getFile().getName(),
                    s.getTitle(),
                    s.getArtist(),
                    s.getAlbum(),
                    s.getAlbumartist(),
                    s.getGenre(),
                    s.getFullTrackString(),
                    s.getFullDiskString(),
                    thumbnail_icon,
                    index
                });
            } else {
                model.addRow(new Object[]{
                    new ImageIcon("img//default.png"),
                    s.getFile(),
                    s.getFile().getName(),
                    s.getTitle(),
                    s.getArtist(),
                    s.getAlbum(),
                    s.getAlbumartist(),
                    s.getGenre(),
                    s.getFullTrackString(),
                    s.getFullDiskString(),
                    null,
                    index
                });
            }

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
        iipreview = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Moose");
        setResizable(false);

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
                if (column == 10 || column == 0) { return ImageIcon.class; }
                else if (column == 12) { return byte[].class; }
                else { return Object.class; }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                if(column == 10) {
                    return false;
                } else {
                    return true;
                }
            }
        };
        table.setModel(tableModel);
        table.setRowHeight(20);
        tableModel.addColumn("");
        tableModel.addColumn("File");
        tableModel.addColumn("Filename");
        tableModel.addColumn("Title");
        tableModel.addColumn("Artist");
        tableModel.addColumn("Album");
        tableModel.addColumn("Album Artist");
        tableModel.addColumn("Genre");
        tableModel.addColumn("Track");
        tableModel.addColumn("Disk");
        tableModel.addColumn("Artwork");
        tableModel.addColumn("Index");
        tableModel.addColumn("bytes");
        table.setAutoCreateRowSorter(true);
        table.removeColumn(table.getColumnModel().getColumn(1));
        table.removeColumn(table.getColumnModel().getColumn(10));
        table.removeColumn(table.getColumnModel().getColumn(10));
        setColumnWidth(0, 12);
        setColumnWidth(7, 50);
        setColumnWidth(8, 50);
        setColumnWidth(9, 110);
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

        iipreview.setText(" ");

        javax.swing.GroupLayout containerLayout = new javax.swing.GroupLayout(container);
        container.setLayout(containerLayout);
        containerLayout.setHorizontalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(containerLayout.createSequentialGroup()
                        .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tableSP)
                            .addGroup(containerLayout.createSequentialGroup()
                                .addComponent(consoleSP, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 875, Short.MAX_VALUE)
                                .addComponent(iipreview, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                    .addComponent(iipreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                    if (col == 9) {
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
                    if (table.getSelectedColumn() == 9) {
                        showArtworkPopup(evt);
                    }
                }

                break;

            // if it's a scroll click
            case java.awt.event.MouseEvent.BUTTON2:
                System.out.println("Scroll click");
                //File f = (File) model.getValueAt(table.getSelectedRow(), 1);
                //updateConsole(f.getPath());
                break;

            default:
                break;
        }


    }//GEN-LAST:event_tableMouseClicked

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed

        saveAll();

    }//GEN-LAST:event_saveButtonActionPerformed

    public void saveAll() {

        for (int i = 1; i < songs.size() + 1; i++) {

            Song s = songs.get(i);
            File file = s.getFile();
            Mp3File mp3file = null;

            try {
                mp3file = new Mp3File(file.getAbsolutePath());
            } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                System.err.println(ex);
            }

            mp3file.getId3v2Tag().setTitle(s.getTitle());
            mp3file.getId3v2Tag().setArtist(s.getArtist());
            mp3file.getId3v2Tag().setAlbum(s.getAlbum());
            mp3file.getId3v2Tag().setAlbumArtist(s.getAlbumartist());
            mp3file.getId3v2Tag().setGenreDescription(s.getGenre());
            mp3file.getId3v2Tag().setTrack(s.getFullTrackString());
            mp3file.getId3v2Tag().setPartOfSet(s.getFullDiskString());

            //String type = mp3file.getId3v2Tag().getAlbumImageMimeType();
            String type = "image/jpeg";
            mp3file.getId3v2Tag().clearAlbumImage();
            mp3file.getId3v2Tag().setAlbumImage(s.getArtwork_bytes(), type);

            //} else {
            //tag = null;
            //}
            //mp3file.setId3v2Tag(tag);
            save(mp3file, file);

        }

    }

    public void removeAlbumArt() {
        int index = Integer.valueOf(model.getValueAt(table.getSelectedRow(), 11).toString());
        File file = songs.get(index).getFile();
        Mp3File mp3file = null;
        try {
            mp3file = new Mp3File(file.getAbsolutePath());
        } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
            System.err.println(ex);
        }

        ID3v2 id3v2Tag;
        if (mp3file.hasId3v2Tag()) {
            id3v2Tag = mp3file.getId3v2Tag();
        } else {
            id3v2Tag = null;
        }

        id3v2Tag.clearAlbumImage();

        model.setValueAt(null, table.getSelectedRow(), 10);

        //save(mp3file, file);
    }

    public void addAlbumArt() {

        try {
            //File file = new File("/Users/pat/Music/Library/Stephen/Stay/Stay (ft. Lindsey Cook).mp3");
            int index = Integer.valueOf(model.getValueAt(table.getSelectedRow(), 11).toString());
            File file = songs.get(index).getFile();
            //Mp3File mp3file = null;
            Mp3File mp3file = new Mp3File(file.getAbsolutePath());
            ID3v2 id3v2Tag = null;

            if (mp3file.hasId3v2Tag()) {
                id3v2Tag = mp3file.getId3v2Tag();
            } else {
                // do nothing
            }

            JFileChooser fc = new JFileChooser(new File(file.getAbsolutePath()));
            //fc.addChoosableFileFilter();
            fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "tif"));
            int returnVal = fc.showOpenDialog(null);

            File img_file = null;
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                img_file = fc.getSelectedFile();
            } else {
                return;
            }

            RandomAccessFile ra_file = new RandomAccessFile(img_file.getAbsolutePath(), "r");
            byte[] bytes = new byte[(int) ra_file.length()];
            ra_file.read(bytes);
            ra_file.close();

            Icon thumbnail_icon = null;
            try {
                // getting the image from the byte array
                ImageIcon icon = new ImageIcon(bytes);
                Image img = icon.getImage();
                Image thumbnail = img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                thumbnail_icon = new ImageIcon(thumbnail);
                //this.artwork = new ImageIcon(img_scaled);

                //iipreview.setIcon(thumbnail_icon);
            } catch (NullPointerException e) {
                System.err.println(e);
            }

            model.setValueAt(thumbnail_icon, table.getSelectedRow(), 10);

            songs.get(index).setArtwork_bytes(bytes);

        } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void save(Mp3File mp3file, File file) {
        try {
            // save the new mp3file
            mp3file.save(file.getAbsolutePath().replace(".mp3", "_.mp3"));

            // delete the old file
            file.delete();

            // rename the new file to match the old
            File newFile = new File(file.getAbsolutePath().replace(".mp3", "_.mp3"));
            newFile.renameTo(file);

        } catch (IOException | NotSupportedException ex) {
            System.err.println(ex);
        }
    }

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
        popup.add(item = new JMenuItem("Play"));
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
    private javax.swing.JLabel iipreview;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JButton saveButton;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableSP;
    // End of variables declaration//GEN-END:variables

}
