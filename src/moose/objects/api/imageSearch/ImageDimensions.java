package moose.objects.api.imageSearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageDimensions {
    @JsonProperty("height")
    int height;
    @JsonProperty("width")
    int width;
}
