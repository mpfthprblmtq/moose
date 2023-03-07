/*
 *  Proj:   Moose
 *  File:   ImageSearchResponse.java
 *  Desc:   Pojo for a bundle of search information retrieved with the Google CSE
 *
 *  Copyright Pat Ripley (mpfthprblmtq) 2018-2023
 */

package com.mpfthprblmtq.moose.objects.api.imageSearch;

// imports
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// class ImageSearchResponse
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageSearchResponse {
    @JsonProperty("items")
    List<ImageSearchResult> items;
}
