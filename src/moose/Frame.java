package moose;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.mpatric.mp3agic.ID3v22Tag;
import com.mpatric.mp3agic.NotSupportedException;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
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
                addAlbumArt();
            } else if (event.getActionCommand().equals("Remove")) {
                removeAlbumArt();
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

                int r = tcl.getRow();
                int c = tcl.getColumn();

                File old_file = (File) model.getValueAt(r, 1);
                String path = old_file.getPath().replace(old_file.getName(), "");
                File new_file = new File(path + model.getValueAt(r, 0));

                String title = model.getValueAt(r, 2).toString();
                String artist = model.getValueAt(r, 3).toString();
                String album = model.getValueAt(r, 4).toString();
                String albumartist = model.getValueAt(r, 5).toString();
                String genre = model.getValueAt(r, 6).toString();
                String track = model.getValueAt(r, 7).toString();
                String disk = model.getValueAt(r, 8).toString();
                ImageIcon artwork = (ImageIcon) model.getValueAt(r, 9);

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
                edited_songs.put(r, new Song(new_file, title, artist, album, albumartist, genre, track, disk, artwork));
            }
        };

        TableCellListener tcl = new TableCellListener(table, action);

        addFileToTable(new File("songs//Happy Now.mp3"));

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

            // create a song object
            Song s = new Song(file, title, artist, album, albumartist, genre, track, disk, artwork_bytes);

            // add the song to the list
            songs.put(songs.size(), s);

            // add the file to the files list and the artwork to the artwork list
            files.add(file);
            covers.add(s.getArtwork());

            // get the table model from the table
            //model = (DefaultTableModel) table.getModel();
            //add the song to the table
            model.addRow(new Object[]{
                s.getFile().getName(),
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
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
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
                if (column == 9) { return ImageIcon.class; }
                //else if (column == 0) { return Phile.class; }
                else { return Object.class; }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                if(column == 9) {
                    return false;
                } else {
                    return true;
                }
            }
        };
        table.setModel(tableModel);
        table.setRowHeight(20);
        tableModel.addColumn("Filename");
        tableModel.addColumn("File");
        tableModel.addColumn("Title");
        tableModel.addColumn("Artist");
        tableModel.addColumn("Album");
        tableModel.addColumn("Album Artist");
        tableModel.addColumn("Genre");
        tableModel.addColumn("Track");
        tableModel.addColumn("Disk");
        tableModel.addColumn("Artwork");
        table.setAutoCreateRowSorter(true);
        table.removeColumn(table.getColumnModel().getColumn(1));
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

        jButton1.setText("jButton1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("jButton2");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout containerLayout = new javax.swing.GroupLayout(container);
        container.setLayout(containerLayout);
        containerLayout.setHorizontalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerLayout.createSequentialGroup()
                        .addComponent(tableSP)
                        .addContainerGap())
                    .addGroup(containerLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(containerLayout.createSequentialGroup()
                        .addComponent(consoleSP, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(saveButton)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 965, Short.MAX_VALUE))))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
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

        //updateConsole("Row: " + table.getSelectedRow() + ", Col: " + table.getSelectedColumn());
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
//                    } else if (table.getSelectedColumn() == 0) {
//                        int r = table.getSelectedRow();
//                        if(table.getValueAt(r, 0) instanceof File) {
//                            File f = (File) model.getValueAt(r, 0);
//                            table.setValueAt(f.getName(), r, 0);
//                            table.editCellAt(r, 1);
//                        }
//                        
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
//        for (int i = 0; i < model.getRowCount(); i++) {
//
//            File file = (File) model.getValueAt(i, 1);
//            //File f = (File) model.getValueAt(model.getRowCount() - 1, 0);
//            //Phile phile = (Phile) model.getValueAt(i, 0);
//            String title = model.getValueAt(i, 2).toString();
//            String artist = model.getValueAt(i, 3).toString();
//            String album = model.getValueAt(i, 4).toString();
//            String albumartist = model.getValueAt(i, 5).toString();
//            String genre = model.getValueAt(i, 6).toString();
//            String track = model.getValueAt(i, 7).toString();
//            String disk = model.getValueAt(i, 8).toString();
//            ImageIcon artwork = (ImageIcon) model.getValueAt(i, 9);
//
////            byte[] byteArray = null;
////            try {
////                ImageIcon artwork = (ImageIcon) model.getValueAt(i, 8);
////                BufferedImage bi = getBufferedImage(artwork.getImage());
////                ByteArrayOutputStream baos = new ByteArrayOutputStream();
////                ImageIO.write(bi, "png", baos);
////                byteArray = baos.toByteArray();
////
////            //preparedStatement.setBytes(1, byteArray);
////            } catch (IOException ex) {
////                System.err.println(ex);
////            }
//            //File f = new File(songs.get(i).getFile().getAbsolutePath().replace(songs.get(i).getFile().getName(), "") + filename);
//            edited_songs.put(i, new Song(file, title, artist, album, albumartist, genre, track, disk, artwork));
//
//        }
        //removeUnchanged();

        submitChanges();


    }//GEN-LAST:event_saveButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        File file = songs.get(0).getFile();
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

        id3v2Tag.setAlbum("cheese");
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


    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        File file = songs.get(0).getFile();
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

        try {
            byte[] bytes;
            try (RandomAccessFile ra_file = new RandomAccessFile("DnB.png", "r")) {
                bytes = new byte[(int)ra_file.length()];
                ra_file.read(bytes);
            }
            String type = mp3file.getId3v2Tag().getAlbumImageMimeType();
            id3v2Tag.clearAlbumImage();
            id3v2Tag.setAlbumImage(bytes, type);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
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

    }//GEN-LAST:event_jButton2ActionPerformed
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
    
    public void removeAlbumArt() {
        File file = songs.get(table.getSelectedRow()).getFile();
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
        
        model.setValueAt(null, table.getSelectedRow(), 9);
        
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
    
    public void addAlbumArt() {
        File file = songs.get(table.getSelectedRow()).getFile();
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

        //id3v2Tag.clearAlbumImage();
        
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose a album cover...");
        
        int returnVal = fc.showOpenDialog(null);
        
        File img_file = null;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            img_file = fc.getSelectedFile();
        } else {
            // no file chosen
        }
        
        Song s = songs.get(table.getSelectedRow());
        //model.setValueAt(s.getArtwork(), table.getSelectedRow(), 9);
        byte[] bytes = null;
        try {
            
            try (RandomAccessFile ra_file = new RandomAccessFile(img_file.getAbsolutePath(), "r")) {
                bytes = new byte[(int)ra_file.length()];
                ra_file.read(bytes);
            }
            String type = mp3file.getId3v2Tag().getAlbumImageMimeType();
            id3v2Tag.clearAlbumImage();
            id3v2Tag.setAlbumImage(bytes, type);
            //model.setValueAt(Song.getIcon(bytes), table.getSelectedRow(), 9);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
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

    public byte[] extractBytes(String ImageName) {
        // open image
        File imgPath = new File(ImageName);
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(imgPath);
        } catch (IOException ex) {
            System.err.println(ex);
        }

        // get DataBufferBytes from Raster
        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte data = (DataBufferByte) raster.getDataBuffer();

        return (data.getData());
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

        Set<Integer> keys = edited_songs.keySet();
        keys.forEach((Integer key) -> {

            // get the file objects
            File old_file = songs.get(key).getFile();
            File new_file = edited_songs.get(key).getFile();

            Mp3File old_mp3 = null;
            Mp3File new_mp3 = null;

            Song old_song = songs.get(key);
            Song new_song = edited_songs.get(key);

            // create mp3file objects for tag parsing
            try {
                old_mp3 = new Mp3File(old_file);
                new_mp3 = new Mp3File(new_file);
            } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                System.err.println(ex);
            }

            // tag stuff
            //String title = mp3file.getId3v2Tag().getTitle();
            ID3v2 old_id3 = old_mp3.getId3v2Tag();
            ID3v2 new_id3 = new_mp3.getId3v2Tag();

            old_id3.setTitle(new_song.getTitle());
            old_id3.setArtist(new_song.getArtist());
            old_id3.setAlbum(new_song.getAlbum());
            old_id3.setAlbumArtist(new_song.getAlbumartist());
            old_id3.setGenreDescription(new_song.getGenre());
            old_id3.setTrack(new_song.getFullTrackString());
            old_id3.setPartOfSet(new_song.getFullDiskString());
            old_id3.setAlbumImage(new_song.getArtwork_bytes(), "");
//            try {
//                //old_id3.setAlbumImage(new_song.getArtwork());
//                
//                if(new_file.exists()) {
//                    // file wasn't renamed
//                    old_file.delete();
//                    old_mp3.save(new_file.getPath());
//                } else {
//                    old_mp3.save(new_file.getAbsolutePath());
//                }
//                
//                old_mp3.save(new_file.getPath());
//            } catch (IOException | NotSupportedException ex) {
//                System.err.println(ex);
//            }
            try {
                //old_mp3.setId3v2Tag(old_id3);
                old_mp3.save(old_file.getPath().replace(".mp3", "_moose.mp3"));

                // lots of work for the album art
//            ImageIcon icon = (ImageIcon)new_song.getArtwork();
//            //BufferedImage bi = (BufferedImage)icon.getImage();
//            
//            BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
//            Graphics g = bi.createGraphics();
//            icon.paintIcon(null, g, 0, 0);
//            g.dispose();
//            
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            try {
//                ImageIO.write(bi, "png", baos);
//            } catch (IOException ex) {
//                System.err.println(ex);
//            }
//            byte[] dataToEncode = baos.toByteArray();
//            byte[] base64Data = Base64.encode(dataToEncode);
//            
//            old_id3.setAlbumImage(base64Data, "what");
// file renaming
//            if (new_file.exists()) {
//                // file name wasn't changed
//            } else {
//                old_file.renameTo(new_file);
//            }
            } catch (IOException ex) {
                Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NotSupportedException ex) {
                Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
            }

        });
    }

    public byte[] imageToBase64(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(null, "png", baos);
            byte[] dataToEncode = baos.toByteArray();
            byte[] base64Data = Base64.encode(dataToEncode);
            return base64Data;
        } catch (IOException ex) {
            System.err.println(ex);
            return null;
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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JButton saveButton;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableSP;
    // End of variables declaration//GEN-END:variables

}
