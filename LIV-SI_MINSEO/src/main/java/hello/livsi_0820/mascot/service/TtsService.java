package hello.livsi_0820.mascot.service;

import hello.livsi_0820.mascot.model.SpeechSegment;
import hello.livsi_0820.mascot.model.TtsResult;
import com.google.cloud.texttospeech.v1.*;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class TtsService {
    public TtsResult generateTts(String text, String voiceName) throws Exception {
        try (TextToSpeechClient client = TextToSpeechClient.create()) {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            String modelName;

            if ("남성".equalsIgnoreCase(voiceName)) {
                modelName = "ko-KR-Chirp3-HD-Charon";
            } else if ("여성".equalsIgnoreCase(voiceName)) {
                modelName = "ko-KR-Chirp3-HD-Kore";
            } else {
                modelName = "ko-KR-Chirp3-HD-Kore"; // 기본값으로 여성을 설정
            }

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("ko-KR")
                    .setName(modelName)
                    .build();
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16)
                    .build();

            SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);

            File audioDir = new File("Audio");
            if (!audioDir.exists()) {
                boolean created = audioDir.mkdirs();
                if (!created) {
                    throw new RuntimeException("Failed to create audio directory: " + audioDir.getAbsolutePath());
                }
            }

            File wavFile = new File(audioDir, "output.wav");
            try (FileOutputStream out = new FileOutputStream(wavFile)) {
                out.write(response.getAudioContent().toByteArray());
            }

            List<SpeechSegment> segments = new ArrayList<>();

            // 정규식: 부호(?<=[.,?!]) 뒤의 공백(\\s+)을 기준으로 문장을 자름
            String[] sentences = text.trim().split("(?<=[.,?!])\\s+");

            for (String sentence : sentences) {
                // 각 문장을 별도의 SpeechSegment로 추가 (시간 정보는 아직 없음)
                segments.add(new SpeechSegment(sentence, 0, 0));
            }

            return new TtsResult(wavFile, segments);
        }
    }

    public double getAudioDurationSeconds(File file) throws Exception {
        try (org.bytedeco.javacv.FFmpegFrameGrabber grabber = new org.bytedeco.javacv.FFmpegFrameGrabber(file)) {
            grabber.start();
            double duration = grabber.getLengthInTime() / 1_000_000.0;
            grabber.stop();
            return duration;
        }
    }
}