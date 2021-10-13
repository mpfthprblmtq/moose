/*
   Proj:   Moose
   File:   Song.java
   Desc:   Object class for the Song object

   Copyright Pat Ripley 2018
 */

// package
package moose.objects;

// imports
import java.io.File;
import java.util.Arrays;
import java.util.Objects;

// class Song
public class Song {

    private File file;
    private File newFile;
    private String title;
    private String artist;
    private String album;
    private String albumArtist;
    private String genre;
    private String year;
    private String track;
    private String totalTracks;
    private String disk;
    private String totalDisks;
    private byte[] artwork_bytes;
    
    private String bitrate;
    private String sampleRate;
    private String length;
    private String comment;

    public Song() {}

    public Song(File file, File newFile) {
        setFile(file);
        setNewFile(newFile);
    }

    public Song(File file, String title, String artist, String album, String albumArtist, String genre, String year, String track, String disk, byte[] artwork_bytes, int bitrate, int sampleRate, long len, String comment) {

        // standard string stuff
        this.file = file;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumArtist = albumArtist;
        this.genre = genre;
        this.year = year;

        // parsing track and disk for total tracks/disks
        if (track != null && track.contains("/")) {
            String[] t = track.split("/");
            this.track = t[0];
            this.totalTracks = t[1];
        } else {
            this.track = "";
            this.totalTracks = "";
        }
        if (disk != null && disk.contains("/")) {
            String[] d = disk.split("/");
            this.disk = d[0];
            this.totalDisks = d[1];
        } else {
            this.disk = "";
            this.totalDisks = "";
        }

        this.artwork_bytes = artwork_bytes;
        
        // parsing some ints and longs
        this.bitrate = bitrate + " Kbps";
        this.sampleRate = sampleRate + " Hz";
        this.length = getLengthString(len);
        
        this.comment = comment;
        
    }
    
    /**
     * @param len, the long to convert
     * @return the full length string #:##
     */
    public String getLengthString(long len) {
        String minutes;
        String seconds;
        minutes = String.valueOf((len - (len % 60))/60);
        seconds = String.valueOf(len % 60);
        if(Integer.parseInt(seconds) < 10) {
            seconds = "0" + seconds;
        }
        return minutes + ":" + seconds;
    }

    /**
     * @return the full track string
     */
    public String getFullTrackString() {
        if (track.equals("") && totalTracks.equals("")) {
            return "";
        } else {
            return track + "/" + totalTracks;
        }
    }

    /**
     * @return the full disk string
     */
    public String getFullDiskString() {
        if (disk.equals("") && totalDisks.equals("")) {
            return "";
        } else {
            return disk + "/" + totalDisks;
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
     * @return the albumArtist
     */
    public String getAlbumArtist() {
        return albumArtist;
    }

    /**
     * @param albumArtist the albumArtist to set
     */
    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
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
     * @return the totalTracks
     */
    public String getTotalTracks() {
        return totalTracks;
    }

    /**
     * @param totalTracks the totalTracks to set
     */
    public void setTotalTracks(String totalTracks) {
        this.totalTracks = totalTracks;
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
     * @return the totalDisks
     */
    public String getTotalDisks() {
        return totalDisks;
    }

    /**
     * @param totalDisks the totalDisks to set
     */
    public void setTotalDisks(String totalDisks) {
        this.totalDisks = totalDisks;
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
     * @return the newFile
     */
    public File getNewFile() {
        return newFile;
    }

    /**
     * @param newFile the newFile to set
     */
    public void setNewFile(File newFile) {
        this.newFile = newFile;
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

    /**
     * @return the bitrate
     */
    public String getBitrate() {
        return bitrate;
    }

    /**
     * @return the sampleRate
     */
    public String getSampleRate() {
        return sampleRate;
    }

    /**
     * @param sampleRate the sampleRate to set
     */
    public void setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * @return the length
     */
    public String getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(String length) {
        this.length = length;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(file, song.file) &&
                Objects.equals(title, song.title) &&
                Objects.equals(artist, song.artist) &&
                Objects.equals(album, song.album) &&
                Objects.equals(albumArtist, song.albumArtist) &&
                Objects.equals(genre, song.genre) &&
                Objects.equals(year, song.year) &&
                Objects.equals(track, song.track) &&
                Objects.equals(totalTracks, song.totalTracks) &&
                Objects.equals(disk, song.disk) &&
                Objects.equals(totalDisks, song.totalDisks) &&
                Arrays.equals(artwork_bytes, song.artwork_bytes) &&
                Objects.equals(bitrate, song.bitrate) &&
                Objects.equals(sampleRate, song.sampleRate) &&
                Objects.equals(length, song.length) &&
                Objects.equals(comment, song.comment);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(file, title, artist, album, albumArtist, genre, year, track, totalTracks, disk, totalDisks, bitrate, sampleRate, length, comment);
        result = 31 * result + Arrays.hashCode(artwork_bytes);
        return result;
    }

    @Override
    public String toString() {
        return "Song{" + "tit=" + title + ", art=" + artist + ", alb=" + album + ", aa=" + albumArtist + ", gen=" + genre + ", t=" + track + ", tt=" + totalTracks + ", d=" + disk + ", td=" + totalDisks + '}';
        //return this.file.getName();
    }
}
