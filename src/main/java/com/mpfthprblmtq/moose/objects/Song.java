/*
 *  Proj:   Moose
 *  File:   Song.java
 *  Desc:   Pojo for the Song information
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects;

// imports
import com.mpfthprblmtq.commons.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

// class Song
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
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

    private int index;

    public Song(File file, File newFile) {
        setFile(file);
        setNewFile(newFile);
    }

    public Song(
            File file,
            String title,
            String artist,
            String album,
            String albumArtist,
            String genre,
            String year,
            String track,
            String disk,
            byte[] artwork_bytes,
            int bitrate,
            int sampleRate,
            long len,
            String comment) {

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
        if (StringUtils.isEmpty(track) && StringUtils.isEmpty(totalTracks)) {
            return "";
        } else {
            return track + "/" + totalTracks;
        }
    }

    /**
     * @return the full disk string
     */
    public String getFullDiskString() {
        if (StringUtils.isEmpty(disk) && StringUtils.isEmpty(totalDisks)) {
            return "";
        } else {
            return disk + "/" + totalDisks;
        }
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

    /**
     * Supplemental function to compare song data, but this one has an optional includeFile parameter
     * @param o, the object to compare with
     * @param includeFile, a boolean to include the file in the check or not
     * @return the result of the check
     */
    public boolean equals(Object o, boolean includeFile) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        boolean result = Objects.equals(title, song.title) &&
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

        if (result && includeFile && Objects.equals(file, song.file)) {
            result = false;
        }
        return result;
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
