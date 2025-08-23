package hello.videoanalyze2.mascot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SceneData {
    @JsonProperty("text")
    private String text;

    @JsonProperty("BGVN")
    private String BGVN;
}