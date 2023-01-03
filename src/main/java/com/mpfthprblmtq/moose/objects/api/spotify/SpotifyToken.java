/*
 *  Proj:   Moose
 *  File:   SpotifyToken.java
 *  Desc:   Pojo for a token retrieved from Spotify's auth service
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects.api.spotify;

// imports
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// class SpotifyToken
@Data
public class SpotifyToken {
    @JsonProperty("access_token")
    private String token;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private int expiration;

    private Long timestamp;
}