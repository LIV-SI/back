package hello.videoanalyze2.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class VideoResult {

    private String concept;
    private List<Scene> scenes;

    @Data
    public static class Scene {
        @JsonProperty("sceneNumber")
        private int sceneNumber;

        @JsonProperty("originalTimestamp")
        private String originalTimestamp;

        private String content;
    }
}