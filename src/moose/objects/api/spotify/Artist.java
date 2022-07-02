package moose.objects.api.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Artist {
    @JsonProperty("id")
    String id;
    @JsonProperty("name")
    String name;
}
