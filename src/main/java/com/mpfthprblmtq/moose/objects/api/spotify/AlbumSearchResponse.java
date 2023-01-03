/*
 *  Proj:   Moose
 *  File:   AlbumSearchResponse.java
 *  Desc:   Pojo for the search response from Spotify
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects.api.spotify;

// imports
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// class AlbumSearchResponse
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class AlbumSearchResponse {
    @JsonProperty("items")
    List<Album> albums;
    @JsonProperty("next")
    String next;
}
