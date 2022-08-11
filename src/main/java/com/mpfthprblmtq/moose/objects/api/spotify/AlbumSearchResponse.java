package com.mpfthprblmtq.moose.objects.api.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class AlbumSearchResponse {
    @JsonProperty("items")
    List<Album> albums;
    @JsonProperty("next")
    String next;
}
