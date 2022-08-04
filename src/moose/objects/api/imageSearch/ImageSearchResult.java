package moose.objects.api.imageSearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.awt.image.BufferedImage;

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

