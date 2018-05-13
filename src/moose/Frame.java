package moose;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v22Tag;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.mpatric.mp3agic.NotSupportedException;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

public class Frame extends javax.swing.JFrame {

    HashMap<Integer, Song> songs = new HashMap<>();
    ArrayList edited_songs = new ArrayList();

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
                    if (file.exists()) {
                        desktop.open(file);
                    }
                } catch (IOException ex) {
                    System.err.println(ex);
                }
            }

        };
        initComponents();

        // taken from the FileDrop example
        FileDrop fileDrop = new FileDrop(System.out, tableSP, (java.io.File[] files) -> {

            int succ_mp3Count = 0;   // lets count the number of successful files imported
            int unsucc_mp3Count = 0; // lets count the number of all files attempted to import

            System.out.println(files[0].getPath());

            ArrayList<File> fileList = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    fileList = listFiles(files[i], fileList);
                } else {
                    fileList.add(files[i]);
                }
            }

            for (int i = 0; i < fileList.size(); i++) {
                if (addFileToTable(fileList.get(i))) {
                    succ_mp3Count++;
                } else {
                    unsucc_mp3Count++;
                }
            }
            if (succ_mp3Count == 0) {
                updateConsole("No mp3 files found!");
            } else if (succ_mp3Count > 1 && unsucc_mp3Count == 0) {
                updateConsole(succ_mp3Count + " mp3 files loaded!");
            } else if (succ_mp3Count == 1) {
                updateConsole("1 mp3 file imported.");
            } else if (succ_mp3Count > 1 && unsucc_mp3Count == 1) {
                updateConsole(succ_mp3Count + " mp3 files loaded, 1 file wasn't an mp3!");
            } else if (succ_mp3Count > 1 && unsucc_mp3Count > 1) {
                updateConsole(succ_mp3Count + " mp3 files loaded, " + unsucc_mp3Count + " unknown files not loaded!");
            } else {
                updateConsole("Unknown case, go look at fileDrop()");
                System.out.println("succ: " + succ_mp3Count);
                System.out.println("blow: " + unsucc_mp3Count);
            }
        });

        model = (DefaultTableModel) table.getModel();

        Action action = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TableCellListener tcl = (TableCellListener) e.getSource();

                int r = tcl.getRow();
                int c = tcl.getColumn();
                int index = Integer.valueOf(model.getValueAt(r, 12).toString());

                setRowIcon(EDITED, tcl.getRow());
                edited_songs.add(index);

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
                        setTitle(index, tcl.getNewValue().toString());
                        break;
                    case 4:     // artist was changed
                        setArtist(index, tcl.getNewValue().toString());
                        break;
                    case 5:     // album was changed
                        setAlbum(index, tcl.getNewValue().toString());
                        break;
                    case 6:     // album artist was changed
                        setAlbumArtist(index, tcl.getNewValue().toString());
                        break;
                    case 7:     // year was changed
                        setYear(index, tcl.getNewValue().toString());
                        break;
                    case 8:     // genre was changed
                        setGenre(index, tcl.getNewValue().toString());
                        break;
                    case 9:     // tracks was changed
                        setTrack(index, tcl.getNewValue().toString());
                        break;
                    case 10:     // disks was changed
                        setDisk(index, tcl.getNewValue().toString());
                        break;
                    case 11:    // artwork was changed
                        //setAlbumImage(index, tcl.getNewValue().toString());
                    default:    // not accounted for
                        break;
                }
            }
        };
        TableCellListener tcl2 = new TableCellListener(table, action);

        // temporary code to add files to table
        //File temp = new File("/Users/pat/Music/Library/Kasbo/Umbrella Club");
        File temp = new File("/Users/pat/Music/Library/Kasbo/Places We Don't Know");
        File[] directoryFiles = temp.listFiles();
        for (File directoryFile : directoryFiles) {
            addFileToTable(directoryFile);
        }
        // end temporary code

    }
    
    public void setTitle(int index, String title) {
        songs.get(index).setTitle(title);
    }
    
    public void setArtist(int index, String artist) {
        songs.get(index).setArtist(artist);
    }
    
    public void setAlbum(int index, String album) {
        songs.get(index).setAlbum(album);
    }
    
    public void setAlbumArtist(int index, String albumartist) {
        songs.get(index).setAlbumartist(albumartist);
    }
    
    public void setGenre(int index, String genre) {
        songs.get(index).setGenre(genre);
    }
    
    public void setYear(int index, String year) {
        songs.get(index).setYear(year);
    }
    
    public void setTrack(int index, String track) {
        songs.get(index).setTrack(track);
    }
    
    public void setDisk(int index, String disk) {
        songs.get(index).setDisk(disk);
    }
    
    public void setAlbumImage(int index, byte[] bytes) {
        songs.get(index).setArtwork_bytes(bytes);
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

    public boolean addFileToTable(File file) {

        if (!file.getAbsolutePath().endsWith(".mp3")) {
            //updateConsole(file.getName() + " is not an mp3!");
            return false;
        } else {

            Mp3File mp3file;
            try {
                mp3file = new Mp3File(file.getAbsolutePath());
                if (!mp3file.hasId3v2Tag()) {
                    ID3v2 tag = new ID3v24Tag();
                    mp3file.setId3v2Tag(tag);
                } else {

                }
            } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                mp3file = null;
                System.err.println(ex);
            }

            // get the id3v2 info
            String title = mp3file.getId3v2Tag().getTitle();
            String artist = mp3file.getId3v2Tag().getArtist();
            String album = mp3file.getId3v2Tag().getAlbum();
            String albumartist = mp3file.getId3v2Tag().getAlbumArtist();
            String genre = mp3file.getId3v2Tag().getGenreDescription();
            String year = mp3file.getId3v2Tag().getYear();
            String track = mp3file.getId3v2Tag().getTrack();
            String disk = mp3file.getId3v2Tag().getPartOfSet();
            byte[] artwork_bytes = mp3file.getId3v2Tag().getAlbumImage();

            if (title == null) {
                title = "";
            }
            if (artist == null) {
                artist = "";
            }
            if (album == null) {
                album = "";
            }
            if (albumartist == null) {
                albumartist = "";
            }
            if (genre == null) {
                genre = "";
            }
            if (year == null) {
                year = "";
            }
            if (track == null) {
                track = "";
            }
            if (disk == null) {
                disk = "";
            }
            if (artwork_bytes == null) {
                // handle the default image here
            }

            // create a song object
            Song s = new Song(file, title, artist, album, albumartist, genre, year, track, disk, artwork_bytes);

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
                    s.getYear(),
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
                    s.getYear(),
                    s.getGenre(),
                    s.getFullTrackString(),
                    s.getFullDiskString(),
                    null,
                    index
                });
            }

        }
        return true;
    }

    public void updateConsole(String s) {
        console.append(s + "\n");
    }

    DefaultTableModel tableModel = new DefaultTableModel() {
        @Override
        public Class
                getColumnClass(int column) {
            if (column == 11 || column == 0) {
                return ImageIcon.class;

            } else {
                return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 11 || column == 0) {
                return false;
            } else {
                return true;
            }
        }
    };
    
    

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
        table = new javax.swing.JTable() {

            public void changeSelection(final int row, final int column, boolean toggle, boolean extend)  {
                super.changeSelection(row, column, toggle, extend);

                if (editCellAt(row, column)) {
                    getEditorComponent().requestFocusInWindow();
                } else {
                    if(row < table.getRowCount()) {
                        changeSelection(row+1, 1, toggle, extend);
                    } else {
                        changeSelection(0, 1, toggle, extend);
                    }
                }
            }

        };
        consoleSP = new javax.swing.JScrollPane();
        console = new javax.swing.JTextArea();
        multPanel = new javax.swing.JPanel();
        L1 = new javax.swing.JLabel();
        L2 = new javax.swing.JLabel();
        L3 = new javax.swing.JLabel();
        L4 = new javax.swing.JLabel();
        L5 = new javax.swing.JLabel();
        L6 = new javax.swing.JLabel();
        L7 = new javax.swing.JLabel();
        L8 = new javax.swing.JLabel();
        L9 = new javax.swing.JLabel();
        multTitle = new javax.swing.JTextField();
        multArtist = new javax.swing.JTextField();
        multAlbum = new javax.swing.JTextField();
        multAlbumArtist = new javax.swing.JTextField();
        multGenre = new javax.swing.JTextField();
        multYear = new javax.swing.JTextField();
        multTrack = new javax.swing.JTextField();
        multDisk = new javax.swing.JTextField();
        multImage = new javax.swing.JLabel();
        multUpdateButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Moose");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel1.setText("Moose");

        saveButton.setText("Save");
        saveButton.setFocusable(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        table.setAutoCreateRowSorter(true);
        table.setModel(tableModel);
        table.setRequestFocusEnabled(false);
        table.setRowHeight(20);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setShowGrid(true);
        tableModel.addColumn("");
        tableModel.addColumn("File");
        tableModel.addColumn("Filename");
        tableModel.addColumn("Title");
        tableModel.addColumn("Artist");
        tableModel.addColumn("Album");
        tableModel.addColumn("Album Artist");
        tableModel.addColumn("Year");
        tableModel.addColumn("Genre");
        tableModel.addColumn("Track");
        tableModel.addColumn("Disk");
        tableModel.addColumn("Artwork");
        tableModel.addColumn("Index");
        table.setAutoCreateRowSorter(true);
        table.removeColumn(table.getColumnModel().getColumn(1));
        table.removeColumn(table.getColumnModel().getColumn(11));
        setColumnWidth(0, 12);
        setColumnWidth(3, 150);
        setColumnWidth(4, 150);
        setColumnWidth(5, 150);
        setColumnWidth(6, 80);
        setColumnWidth(7, 150);
        setColumnWidth(8, 50);
        setColumnWidth(9, 50);
        setColumnWidth(10, 110);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableMousePressed(evt);
            }
        });
        tableSP.setViewportView(table);

        console.setEditable(false);
        console.setColumns(20);
        console.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        console.setRows(5);
        consoleSP.setViewportView(console);

        multPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        L1.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        L1.setText("Edit Multiple Items:");

        L2.setText("Title:");

        L3.setText("Artist:");

        L4.setText("Album:");

        L5.setText("Album Artist:");

        L6.setText("Genre:");

        L7.setText("Year:");

        L8.setText("Track:");

        L9.setText("Disk:");

        multImage.setText(" ");
        multImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        multUpdateButton.setText("Update Fields");
        multUpdateButton.setFocusable(false);
        multUpdateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multUpdateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout multPanelLayout = new javax.swing.GroupLayout(multPanel);
        multPanel.setLayout(multPanelLayout);
        multPanelLayout.setHorizontalGroup(
            multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(multPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(multPanelLayout.createSequentialGroup()
                        .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(multPanelLayout.createSequentialGroup()
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(L2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(L3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(L4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(L5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(multTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                                    .addComponent(multArtist)
                                    .addComponent(multAlbum)
                                    .addComponent(multAlbumArtist))
                                .addGap(18, 18, 18)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(L8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(L6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(L7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(L9, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(multGenre, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(multTrack, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(multDisk, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(multYear, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(65, 65, 65))
                            .addGroup(multPanelLayout.createSequentialGroup()
                                .addComponent(L1)
                                .addGap(18, 18, 18)))
                        .addComponent(multImage, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, multPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(multUpdateButton)))
                .addContainerGap())
        );
        multPanelLayout.setVerticalGroup(
            multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(multPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(multPanelLayout.createSequentialGroup()
                        .addGap(0, 36, Short.MAX_VALUE)
                        .addComponent(multImage, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(multUpdateButton))
                    .addGroup(multPanelLayout.createSequentialGroup()
                        .addComponent(L1)
                        .addGap(9, 9, 9)
                        .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(multPanelLayout.createSequentialGroup()
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L2)
                                    .addComponent(multTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(multPanelLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(L3))
                                    .addGroup(multPanelLayout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(multArtist, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L4)
                                    .addComponent(multAlbum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(multPanelLayout.createSequentialGroup()
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L6)
                                    .addComponent(multGenre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L7)
                                    .addComponent(multYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L8)
                                    .addComponent(multTrack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(multPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(L9)
                                    .addComponent(multDisk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(multAlbumArtist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(L5)))))))
        );

        javax.swing.GroupLayout containerLayout = new javax.swing.GroupLayout(container);
        container.setLayout(containerLayout);
        containerLayout.setHorizontalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(tableSP, javax.swing.GroupLayout.PREFERRED_SIZE, 1400, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(containerLayout.createSequentialGroup()
                            .addComponent(consoleSP)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(multPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        containerLayout.setVerticalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tableSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(multPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(consoleSP))
                .addContainerGap())
        );

        jMenu1.setText("File");

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_MASK));
        openMenuItem.setText("Open...");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(openMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(container, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(container, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        saveAll();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            ArrayList<File> files = new ArrayList<>();
            files = listFiles(fc.getSelectedFile(), files);

            for (File file : files) {
                if (file.getName().endsWith(".mp3")) {
                    addFileToTable(file);
                }

            }
        } else {
            updateConsole("No file(s) chosen!");
        }


    }//GEN-LAST:event_openMenuItemActionPerformed

    private void multUpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multUpdateButtonActionPerformed
        updateMultPanelFields();
    }//GEN-LAST:event_multUpdateButtonActionPerformed

    private void tableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMousePressed

        // check what type of click
        switch (evt.getButton()) {

            // if it's a right click
            case java.awt.event.MouseEvent.BUTTON3:
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0) {
                    if (col == 10) {
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
                    if (table.getSelectedColumn() == 10) {
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

        // find out what to do when row(s) are selected
        if (table.getSelectedRowCount() > 1) {
            //System.out.println("multiple rows selected");
            enableMultPanel(true);
            setMultiplePanelFields();
            //table.editCellAt();
//            if (table.isEditing()) {
//                table.getCellEditor().stopCellEditing();
//            }
            //multTitle.requestFocus();
        } else if (table.getSelectedRowCount() == 1) {
            //System.out.println("only one row selected");
            enableMultPanel(false);
        } else if (table.getSelectedRowCount() < 1) {
            // no rows selected
        }

    }//GEN-LAST:event_tableMousePressed

    public void setMultiplePanelFields() {

        // get the indices of the selected rows
        int[] selectedrows = table.getSelectedRows();
        int rows = table.getSelectedRowCount();

        // make the arrays of values
        String[] titles = new String[rows];
        String[] artists = new String[rows];
        String[] albums = new String[rows];
        String[] albumartists = new String[rows];
        String[] genres = new String[rows];
        String[] years = new String[rows];
        String[] tracks = new String[rows];
        String[] disks = new String[rows];

        // fill the arrays
        for (int i = 0; i < selectedrows.length; i++) {
            titles[i] = table.getValueAt(selectedrows[i], 2).toString();
            artists[i] = table.getValueAt(selectedrows[i], 3).toString();
            albums[i] = table.getValueAt(selectedrows[i], 4).toString();
            albumartists[i] = table.getValueAt(selectedrows[i], 5).toString();
            years[i] = table.getValueAt(selectedrows[i], 6).toString();
            genres[i] = table.getValueAt(selectedrows[i], 7).toString();
            tracks[i] = table.getValueAt(selectedrows[i], 8).toString();
            disks[i] = table.getValueAt(selectedrows[i], 9).toString();
        }

        // fill the fields
        if (checkIfSame(titles[0], titles)) {
            multTitle.setText(titles[0]);
        } else {
            multTitle.setText("-");
        }

        if (checkIfSame(artists[0], artists)) {
            multArtist.setText(artists[0]);
        } else {
            multArtist.setText("-");
        }

        if (checkIfSame(albums[0], albums)) {
            multAlbum.setText(albums[0]);
        } else {
            multAlbum.setText("-");
        }

        if (checkIfSame(albumartists[0], albumartists)) {
            multAlbumArtist.setText(albumartists[0]);
        } else {
            multAlbumArtist.setText("-");
        }

        if (checkIfSame(genres[0], genres)) {
            multGenre.setText(genres[0]);
        } else {
            multGenre.setText("-");
        }

        if (checkIfSame(years[0], years)) {
            multYear.setText(years[0]);
        } else {
            multYear.setText("-");
        }

        if (checkIfSame(tracks[0], tracks)) {
            multTrack.setText(tracks[0]);
        } else {
            multTrack.setText("-");
        }

        if (checkIfSame(disks[0], disks)) {
            multDisk.setText(disks[0]);
        } else {
            multDisk.setText("-");
        }
    }

    public void updateMultPanelFields() {
        int[] selectedRows = table.getSelectedRows();

        String title = multTitle.getText();
        String artist = multArtist.getText();
        String album = multArtist.getText();
        String albumArtist = multAlbumArtist.getText();
        String genre = multGenre.getText();
        String year = multYear.getText();
        String track = multTrack.getText();
        String disk = multDisk.getText();

        if (!title.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {
                
                // get the index of the song in the table
                int index = getIndex(selectedRows[i]);
                
                // set the value in the table to the new value
                table.setValueAt(title, selectedRows[i], 2);
                
                // set the value in the songs array
                setTitle(index, title);
                
                // add the song to edited_songs and update the row icon
                edited_songs.add(index);
                setRowIcon(EDITED, selectedRows[i]);
            }
        }
        
        if (!artist.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {
                
                // get the index of the song in the table
                int index = getIndex(selectedRows[i]);
                
                // set the value in the table to the new value
                table.setValueAt(artist, selectedRows[i], 3);
                
                // set the value in the songs array
                setArtist(index, artist);
                
                // add the song to edited_songs and update the row icon
                edited_songs.add(index);
                setRowIcon(EDITED, selectedRows[i]);
            }
        }
        
        if (!album.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {
                
                // get the index of the song in the table
                int index = getIndex(selectedRows[i]);
                
                // set the value in the table to the new value
                table.setValueAt(album, selectedRows[i], 4);
                
                // set the value in the songs array
                setAlbum(index, album);
                
                // add the song to edited_songs and update the row icon
                edited_songs.add(index);
                setRowIcon(EDITED, selectedRows[i]);
            }
        }
        
        if (!albumArtist.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {
                
                // get the index of the song in the table
                int index = getIndex(selectedRows[i]);
                
                // set the value in the table to the new value
                table.setValueAt(albumArtist, selectedRows[i], 5);
                
                // set the value in the songs array
                setAlbumArtist(index, albumArtist);
                
                // add the song to edited_songs and update the row icon
                edited_songs.add(index);
                setRowIcon(EDITED, selectedRows[i]);
            }
        }
        
        if (!year.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {
                
                // get the index of the song in the table
                int index = getIndex(selectedRows[i]);
                
                // set the value in the table to the new value
                table.setValueAt(year, selectedRows[i], 6);
                
                // set the value in the songs array
                setYear(index, year);
                
                // add the song to edited_songs and update the row icon
                edited_songs.add(index);
                setRowIcon(EDITED, selectedRows[i]);
            }
        }
        
        if (!genre.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {
                
                // get the index of the song in the table
                int index = getIndex(selectedRows[i]);
                
                // set the value in the table to the new value
                table.setValueAt(genre, selectedRows[i], 7);
                
                // set the value in the songs array
                setGenre(index, genre);
                
                // add the song to edited_songs and update the row icon
                edited_songs.add(index);
                setRowIcon(EDITED, selectedRows[i]);
            }
        }
        
        if (!track.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {
                
                // get the index of the song in the table
                int index = getIndex(selectedRows[i]);
                
                // set the value in the table to the new value
                table.setValueAt(track, selectedRows[i], 8);
                
                // set the value in the songs array
                setTrack(index, track);
                
                // add the song to edited_songs and update the row icon
                edited_songs.add(index);
                setRowIcon(EDITED, selectedRows[i]);
            }
        }
        
        if (!disk.equals("-")) {
            for (int i = 0; i < selectedRows.length; i++) {
                
                // get the index of the song in the table
                int index = getIndex(selectedRows[i]);
                
                // set the value in the table to the new value
                table.setValueAt(disk, selectedRows[i], 9);
                
                // set the value in the songs array
                setDisk(index, disk);
                
                // add the song to edited_songs and update the row icon
                edited_songs.add(index);
                setRowIcon(EDITED, selectedRows[i]);
            }
        }
    }

    public ArrayList<File> listFiles(File directory, ArrayList<File> files) {

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listFiles(file, files);
            }
        }
        return files;
    }

    public void enableMultPanel(boolean bool) {

        multTitle.setText("");
        multArtist.setText("");
        multAlbum.setText("");
        multAlbumArtist.setText("");
        multGenre.setText("");
        multYear.setText("");
        multTrack.setText("");
        multDisk.setText("");

        multTitle.setEnabled(bool);
        multArtist.setEnabled(bool);
        multAlbum.setEnabled(bool);
        multAlbumArtist.setEnabled(bool);
        multGenre.setEnabled(bool);
        multYear.setEnabled(bool);
        multTrack.setEnabled(bool);
        multDisk.setEnabled(bool);
        multImage.setIcon(null);
        multUpdateButton.setEnabled(bool);
    }

    public boolean checkIfSame(String str, String[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (!arr[i].equals(str)) {
                return false;
            }
        }
        return true;
    }

    public void saveAll() {

        for (int i = 1; i < songs.size() + 1; i++) {

            if (edited_songs.contains(i)) {
                Song s = songs.get(i);
                File file = s.getFile();
                Mp3File mp3file = null;

                try {
                    mp3file = new Mp3File(file.getAbsolutePath());
                    ID3v2 tag = new ID3v24Tag();
                    mp3file.setId3v2Tag(tag);
                } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                    System.err.println(ex);
                }

                try {
                    mp3file.getId3v2Tag().setTitle(s.getTitle());
                    mp3file.getId3v2Tag().setArtist(s.getArtist());
                    mp3file.getId3v2Tag().setAlbum(s.getAlbum());
                    mp3file.getId3v2Tag().setAlbumArtist(s.getAlbumartist());
                    mp3file.getId3v2Tag().setGenreDescription(s.getGenre());
                    mp3file.getId3v2Tag().setYear(s.getYear());
                    mp3file.getId3v2Tag().setTrack(s.getFullTrackString());
                    mp3file.getId3v2Tag().setPartOfSet(s.getFullDiskString());
                } catch (IllegalArgumentException ex) {
                    System.err.println("genre not recognized, oh well");
                }

                String type = "image/jpeg";
                mp3file.getId3v2Tag().clearAlbumImage();
                mp3file.getId3v2Tag().setAlbumImage(s.getArtwork_bytes(), type);

                save(mp3file, file);

                setRowIcon(SAVED, getRow(i));
            } else {
                // skip it, no changes
            }
        }

    }

    public int getRow(int index) {
        for (int i = 1; i <= table.getRowCount(); i++) {
            if (i == index) {
                return i - 1;
            }
        }
        return -1;
    }
    
    public int getIndex(int row) {
        return Integer.valueOf(model.getValueAt(row, 12).toString());
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

    }

    public void addAlbumArt() {

        try {
            //File file = new File("/Users/pat/Music/Library/Stephen/Stay/Stay (ft. Lindsey Cook).mp3");
            int index = Integer.valueOf(model.getValueAt(table.getSelectedRow(), 12).toString());
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

            songs.get(index).setArtwork_bytes(bytes);

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

            model.setValueAt(thumbnail_icon, table.getSelectedRow(), 11);

            edited_songs.add(index);

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
    private javax.swing.JLabel L1;
    private javax.swing.JLabel L2;
    private javax.swing.JLabel L3;
    private javax.swing.JLabel L4;
    private javax.swing.JLabel L5;
    private javax.swing.JLabel L6;
    private javax.swing.JLabel L7;
    private javax.swing.JLabel L8;
    private javax.swing.JLabel L9;
    private javax.swing.JTextArea console;
    private javax.swing.JScrollPane consoleSP;
    private javax.swing.JPanel container;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JTextField multAlbum;
    private javax.swing.JTextField multAlbumArtist;
    private javax.swing.JTextField multArtist;
    private javax.swing.JTextField multDisk;
    private javax.swing.JTextField multGenre;
    private javax.swing.JLabel multImage;
    private javax.swing.JPanel multPanel;
    private javax.swing.JTextField multTitle;
    private javax.swing.JTextField multTrack;
    private javax.swing.JButton multUpdateButton;
    private javax.swing.JTextField multYear;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableSP;
    // End of variables declaration//GEN-END:variables

//    public class MyCellEditor extends AbstractCellEditor implements TableCellEditor {
//
//        private static final long serialVersionUID = 1L;
//
//        JTextField textField = new JTextField("");
//
//        @Override
//        public boolean isCellEditable(EventObject e) {
//            if (super.isCellEditable(e)) {
//                if (e instanceof MouseEvent) {
//                    MouseEvent me = (MouseEvent) e;
//                    return me.getClickCount() >= 2;
//                }
//                if (e instanceof KeyEvent) {
//                    KeyEvent ke = (KeyEvent) e;
//                    return ke.getKeyCode() == KeyEvent.VK_F2;
//                }
//            }
//            return false;
//        }
//
//        @Override
//        public Object getCellEditorValue() {
//            return this.textField.getText();
//        }
//
//        @Override
//        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//            //this.textField.setFont(table.getFont());
//            this.textField.setText(value.toString());
////            SwingUtilities.invokeLater(new Runnable() {
////                public void run() {
////                    this.textField.selectAll();
////                }
////            });
//            this.textField.selectAll();
//            return this.textField;
//        }
//
////return false;
//    }
}
