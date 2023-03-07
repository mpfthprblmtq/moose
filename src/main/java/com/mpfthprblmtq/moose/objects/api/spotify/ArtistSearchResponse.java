/*
 *  Proj:   Moose
 *  File:   ArtistSearchResponse.java
 *  Desc:   Pojo for the search response from the Artist search in Spotify
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects.api.spotify;

// imports
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// class ArtistSearchResponse
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class ArtistSearchResponse {
    @JsonProperty("items")
    List<Artist> artists;
}
