/*
   Proj:   Moose
   File:   ImageSearchResponse.java
   Desc:   Pojo for the response we get back from the Google CSE API call

   Copyright Pat Ripley 2018-2023
 */

// package
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
