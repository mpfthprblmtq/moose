/*
 *  Proj:   Moose
 *  File:   ImageSearchResult.java
 *  Desc:   Pojo for a search result with information to provide to the Google CSE
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects.api.imageSearch;

// imports
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.awt.image.BufferedImage;

// class ImageSearchResult
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageSearchResult {
    @JsonProperty("link")
    String url;
    @JsonProperty("mime")
    String mime;
    @JsonProperty("image")
    ImageDimensions imageDimensions;

    // a BufferedImage object we'll use to set later
    BufferedImage bufferedImage;
}

