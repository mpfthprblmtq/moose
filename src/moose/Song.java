/**
 *
 */
// package
package moose;

// imports
import java.awt.Image;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;

// class Song
public class Song {

    //private String filepath;
    //private String filename;
    private Phile file;
    private String title;
    private String artist;
    private String album;
    private String albumartist;
    private String genre;
    private String track;
    private String totaltracks;
    private String disk;
    private String totaldisks;
    private Icon artwork;

    public Song() {

    }

    public Song(Phile file, String title, String artist, String album, String albumartist, String genre, String track, String disk, byte[] artwork_bytes) {

        // standard string stuff
        this.file = file;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumartist = albumartist;
        this.genre = genre;

        // parsing track and disk for total tracks/disks
        if (track != null && track.contains("/")) {
            String[] t = track.split("/");
            this.track = t[0];
            this.totaltracks = t[1];
        } else {
            this.track = "0";
            this.totaltracks = "0";
        }
        if (disk != null && disk.contains("/")) {
            String[] d = disk.split("/");
            this.disk = d[0];
            this.totaldisks = d[1];
        } else {
            this.disk = "0";
            this.totaldisks = "0";
        }

        try {
            // getting the image from the byte array
            ImageIcon icon = new ImageIcon(artwork_bytes);
            Image img = icon.getImage();
            Image img_scaled = img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
            this.artwork = new ImageIcon(img_scaled);
        } catch (NullPointerException e) {

        }
    }
    
    public Song(Phile file, String title, String artist, String album, String albumartist, String genre, String track, String disk, ImageIcon artwork) {

        // standard string stuff
        this.file = file;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumartist = albumartist;
        this.genre = genre;

        // parsing track and disk for total tracks/disks
        if (track != null && track.contains("/")) {
            String[] t = track.split("/");
            this.track = t[0];
            this.totaltracks = t[1];
        } else {
            this.track = "0";
            this.totaltracks = "0";
        }
        if (disk != null && disk.contains("/")) {
            String[] d = disk.split("/");
            this.disk = d[0];
            this.totaldisks = d[1];
        } else {
            this.disk = "0";
            this.totaldisks = "0";
        }
        
        this.artwork = artwork;
    }

    /**
     * @return the full track string
     */
    public String getFullTrackString() {
        if (track.equals("0") && totaltracks.equals("0")) {
            return "";
        } else {
            return track + "/" + totaltracks;
        }
    }

    /**
     * @return the full disk string
     */
    public String getFullDiskString() {
        if (disk.equals("0") && totaldisks.equals("0")) {
            return "";
        } else {
            return disk + "/" + totaldisks;
        }
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * @param artist the artist to set
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * @return the album
     */
    public String getAlbum() {
        return album;
    }

    /**
     * @param album the album to set
     */
    public void setAlbum(String album) {
        this.album = album;
    }

    /**
     * @return the albumartist
     */
    public String getAlbumartist() {
        return albumartist;
    }

    /**
     * @param albumartist the albumartist to set
     */
    public void setAlbumartist(String albumartist) {
        this.albumartist = albumartist;
    }

    /**
     * @return the genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * @param genre the genre to set
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * @return the track
     */
    public String getTrack() {
        return track;
    }

    /**
     * @param track the track to set
     */
    public void setTrack(String track) {
        this.track = track;
    }

    /**
     * @return the totaltracks
     */
    public String getTotaltracks() {
        return totaltracks;
    }

    /**
     * @param totaltracks the totaltracks to set
     */
    public void setTotaltracks(String totaltracks) {
        this.totaltracks = totaltracks;
    }

    /**
     * @return the disk
     */
    public String getDisk() {
        return disk;
    }

    /**
     * @param disk the disk to set
     */
    public void setDisk(String disk) {
        this.disk = disk;
    }

    /**
     * @return the totaldisks
     */
    public String getTotaldisks() {
        return totaldisks;
    }

    /**
     * @param totaldisks the totaldisks to set
     */
    public void setTotaldisks(String totaldisks) {
        this.totaldisks = totaldisks;
    }

    /**
     * @return the artwork
     */
    public Icon getArtwork() {
        return artwork;
    }

    /**
     * @param artwork the artwork to set
     */
    public void setArtwork(Icon artwork) {
        this.artwork = artwork;
    }

    /**
     * @return the file
     */
    public Phile getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(Phile file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "Song{" + "title=" + title + ", artist=" + artist + ", album=" + album + ", albumartist=" + albumartist + ", genre=" + genre + ", track=" + track + ", totaltracks=" + totaltracks + ", disk=" + disk + ", totaldisks=" + totaldisks + ", artwork=" + artwork + '}';
        //return this.file.getName();
    }
    
    public boolean equals(Song s) {
        if(s.getFile() == this.file
                && s.getTitle().equals(this.title)
                && s.getArtist().equals(this.artist)
                && s.getAlbum().equals(this.album)
                && s.getAlbumartist().equals(this.albumartist)
                && s.getGenre().equals(this.genre)
                && s.getFullTrackString().equals(this.getFullTrackString())
                && s.getFullDiskString().equals(this.getFullDiskString())
                && s.getArtwork() == this.getArtwork()) {
            return true;
        } else {
            return false;
        }
    }

}
