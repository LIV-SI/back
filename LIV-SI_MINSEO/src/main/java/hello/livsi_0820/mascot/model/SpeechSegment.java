package hello.livsi_0820.mascot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeechSegment {
    private Double startTime;
    private Double endTime;
    private String text;

    public SpeechSegment(String text, double startTime, double endTime) {
        this.text = text;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
