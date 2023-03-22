/*
 *  Proj:   Moose
 *  File:   Image.java
 *  Desc:   Pojo for a Image retrieved from Spotify
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects.api.spotify;

// imports
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// class Image
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Image {
    @JsonProperty("height")
    Integer dimension;
    @JsonProperty("url")
    String url;
}
