package com.mpfthprblmtq.moose.objects.api.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Image {
    @JsonProperty("height")
    Integer dimension;
    @JsonProperty("url")
    String url;
}
