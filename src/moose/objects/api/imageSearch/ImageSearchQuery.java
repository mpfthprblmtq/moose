/*
   Proj:   Moose
   File:   ImageSearchQuery.java
   Desc:   Pojo for a bundle of search information to provide to the Album Art Finder service

   Copyright Pat Ripley 2018
 */

// package
package moose.objects.api.imageSearch;

// imports
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.List;

// class ImageSearchQuery
@Data
@AllArgsConstructor
public class ImageSearchQuery {
    private String artist;
    private String album;
    private File dir;
    private List<Integer> rows;

    public static boolean contains(List<ImageSearchQuery> queries, String artist, String album) {
        for (ImageSearchQuery imageSearchQuery : queries) {
            if (imageSearchQuery.getArtist().equals(artist) && imageSearchQuery.getAlbum().equals(album)) {
                return true;
            }
        }
        return false;
    }

    public static int getIndex(List<ImageSearchQuery> queries, String artist, String album) {
        for (int i = 0; i < queries.size(); i++) {
            if (queries.get(i).getArtist().equals(artist) && queries.get(i).getAlbum().equals(album)) {
                return i;
            }
        }
        return -1;
    }
}
