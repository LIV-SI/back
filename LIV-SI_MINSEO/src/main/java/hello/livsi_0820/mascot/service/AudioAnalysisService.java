package hello.livsi_0820.mascot.service;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import hello.livsi_0820.mascot.model.SpeechSegment;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class AudioAnalysisService {

    public List<SpeechSegment> analyzeSpeech(File wavFile) throws Exception {
        List<SpeechSegment> segments = new ArrayList<>();

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(
                wavFile.getAbsolutePath(),
                16000,   // sample rate
                1024,    // buffer size
                0);      // overlap

        dispatcher.addAudioProcessor(new AudioProcessor() {

            final double RMS_THRESHOLD = 0.018;       // 말하고 있는지 판별할 임계치
            final double MIN_SEGMENT_DURATION = 0.2;  // 0.2초 이하의 segment는 무시
            final double MERGE_GAP_THRESHOLD = 0.25;  // 0.25초 이하의 gap은 붙임

            private boolean wasSpeaking = false;
            private double segmentStartTime = 0;

            // 병합을 위한 이전 segment 끝
            private Double lastSegmentEnd = null;

            @Override
            public boolean process(AudioEvent audioEvent) {
                double rms = audioEvent.getRMS();
                double currentTime = audioEvent.getTimeStamp();
                boolean isSpeaking = rms > RMS_THRESHOLD;

                if (isSpeaking && !wasSpeaking) {
                    segmentStartTime = currentTime;
                }

                if (!isSpeaking && wasSpeaking) {
                    double segStart = segmentStartTime;
                    double segEnd   = currentTime;
                    double dur      = segEnd - segStart;

                    if (dur > MIN_SEGMENT_DURATION) {
                        // Gap 병합: 직전 segment와 간격이 매우 짧으면 하나로 이어붙임
                        if (lastSegmentEnd != null && segStart - lastSegmentEnd < MERGE_GAP_THRESHOLD) {
                            SpeechSegment last = segments.get(segments.size() - 1);
                            last.setEndTime(segEnd);
                            lastSegmentEnd = segEnd;
                        } else {
                            SpeechSegment s = new SpeechSegment("TTS", segStart, segEnd);
                            segments.add(s);
                            lastSegmentEnd = segEnd;
                        }
                    }
                }

                wasSpeaking = isSpeaking;
                return true;
            }

            @Override
            public void processingFinished() {
                // 오디오가 말하면서 끝났을 경우 마지막 세그먼트 처리
                if (wasSpeaking) {
                    try {
                        double total = getAudioDurationSeconds(wavFile);
                        double dur   = total - segmentStartTime;

                        if (dur > MIN_SEGMENT_DURATION) {
                            if (lastSegmentEnd != null && segmentStartTime - lastSegmentEnd < MERGE_GAP_THRESHOLD) {
                                SpeechSegment last = segments.get(segments.size() - 1);
                                last.setEndTime(total);
                            } else {
                                SpeechSegment s = new SpeechSegment("TTS", segmentStartTime, total);
                                segments.add(s);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        dispatcher.run();
        return segments;
    }

    public double getAudioDurationSeconds(File wavFile) throws Exception {
        AudioFileFormat format = AudioSystem.getAudioFileFormat(wavFile);
        long frames = format.getFrameLength();
        float frameRate = format.getFormat().getFrameRate();
        if (frameRate <= 0) frameRate = 16000f;
        return frames / frameRate;
    }
}