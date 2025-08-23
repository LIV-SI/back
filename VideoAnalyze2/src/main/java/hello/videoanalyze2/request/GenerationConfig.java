package hello.videoanalyze2.request;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

// GenerationConfig 객체를 위한 DTO
// JsonInclude 어노테이션은 값이 null일 경우 JSON으로 변환할 때 아예 필드를 포함하지 않게 해줍니다.
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenerationConfig {

    private Float temperature; // 0.0 ~ 1.0 사이의 값
    private Integer topK;
    private Integer topP;
    private Integer maxOutputTokens;
    // private List<String> stopSequences;
}
