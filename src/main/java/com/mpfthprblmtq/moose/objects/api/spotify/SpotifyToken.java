package com.mpfthprblmtq.moose.objects.api.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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