package hello.livsi_0820.request;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiReqDto {

    private List<Content> contents;

//    @JsonProperty("generation_config")
//    private GenerationConfig generationConfig;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part{
        private String text;
//        @JsonProperty("file_data")
        private FileData file_data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileData{
//        @JsonProperty("mime_type")
        private String mime_type;
//        @JsonProperty("file_uri")
        private String file_uri;
    }


    public static GeminiReqDto createVideoRequest(String mimeType, String fileUri, String prompt) {
        Part filePart = new Part(null, new FileData(mimeType, fileUri));
        Part textPart = new Part(prompt, null);
        Content content = new Content(List.of(filePart, textPart));
        return new GeminiReqDto(List.of(content));
    }
}
