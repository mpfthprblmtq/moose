/**
 *  Proj:   Moose
 *  File:   Song.java
 *  Desc:   Object class for the Song object
 *
 *  Copyright Pat Ripley 2018
 */

// package
package moose.objects;

// imports
import java.io.File;

// class Song
public class Song {

    private File file;
    private String title;
    private String artist;
    private String album;
    private String albumartist;
    private String genre;
    private String year;
    private String track;
    private String totaltracks;
    private String disk;
    private String totaldisks;
    private byte[] artwork_bytes;

    public Song() {

    }

    public Song(File file, String title, String artist, String album, String albumartist, String genre, String year, String track, String disk, byte[] artwork_bytes) {

        // standard string stuff
        this.file = file;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumartist = albumartist;
        this.genre = genre;
        this.year = year;

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

        this.artwork_bytes = artwork_bytes;
        
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
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }
    
    /**
     * @return the artwork_bytes
     */
    public byte[] getArtwork_bytes() {
        return artwork_bytes;
    }

    /**
     * @param artwork_bytes the artwork_bytes to set
     */
    public void setArtwork_bytes(byte[] artwork_bytes) {
        this.artwork_bytes = artwork_bytes;
    }
    
    public void setFullTrack(String t) {
        String[] arr = t.split("/");
        this.track = arr[0];
        this.totaltracks = arr[1];
    }
    
    public void setFullDisk(String d) {
        String[] arr = d.split("/");
        this.disk = arr[0];
        this.totaldisks = arr[1];
    }

    @Override
    public String toString() {
        return "Song{" + "tit=" + title + ", art=" + artist + ", alb=" + album + ", aa=" + albumartist + ", gen=" + genre + ", t=" + track + ", tt=" + totaltracks + ", d=" + disk + ", td=" + totaldisks + '}';
        //return this.file.getName();
    }

    /**
     * @return the year
     */
    public String getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }
}
