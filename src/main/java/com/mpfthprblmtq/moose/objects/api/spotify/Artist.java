/*
 *  Proj:   Moose
 *  File:   Artist.java
 *  Desc:   Pojo for a Artist retrieved from Spotify
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects.api.spotify;

// imports
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// class Artist
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Artist {
    @JsonProperty("id")
    String id;
    @JsonProperty("name")
    String name;
    @JsonProperty("images")
    List<Image> images;
}
