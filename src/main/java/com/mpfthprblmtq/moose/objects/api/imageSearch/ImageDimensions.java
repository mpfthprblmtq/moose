/*
 *  Proj:   Moose
 *  File:   ImageDimensions.java
 *  Desc:   Pojo for image dimensions retrieved from the Google CSE
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects.api.imageSearch;

// imports
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// class ImageDimensions
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageDimensions {
    @JsonProperty("height")
    int height;
    @JsonProperty("width")
    int width;
}
