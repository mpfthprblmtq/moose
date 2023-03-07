/*
 *  Proj:   Moose
 *  File:   Album.java
 *  Desc:   Pojo for an album retrieved from Spotify
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects.api.spotify;

// import
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Comparator;
import java.util.List;

// class Album
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Album {
    @JsonProperty("artists")
    List<Artist> artists;
    @JsonProperty("images")
    List<Image> images;
    @JsonProperty("name")
    String name;

    /**
     * Gets the image with the largest dimensions
     * @return the largest image
     */
    public Image getImage() {
        getImages().sort(Comparator.comparingInt(Image::getDimension).reversed());
        return getImages().get(0);
    }
}
