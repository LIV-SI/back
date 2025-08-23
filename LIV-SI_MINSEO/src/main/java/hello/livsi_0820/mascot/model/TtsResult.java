package hello.livsi_0820.mascot.model;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

@Getter
@Setter
public class TtsResult {
    private File audioFile;
    private List<SpeechSegment> segments;

    public TtsResult(File audioFile, List<SpeechSegment> segments) {
        this.audioFile = audioFile;
        this.segments = segments;
    }
}