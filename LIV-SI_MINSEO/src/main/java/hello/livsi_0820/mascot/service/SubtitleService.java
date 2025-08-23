package hello.livsi_0820.mascot.service;

import hello.livsi_0820.mascot.model.SpeechSegment;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class SubtitleService {

    private static final String FFMPEG_BIN = "ffmpeg";

    private static final String FONT_PATH = "/usr/local/share/fonts/custom";

    public void addSubtitles(String videoPath, List<SpeechSegment> segments, String outputVideoPath) throws IOException, InterruptedException {
        // 1. SRT 파일 임시로 생성
        String srtFilePath = createSrtFile(segments);

        String input = videoPath.replace("\\", "/");
        String out   = outputVideoPath.replace("\\", "/");

        // 2. FFmpeg 필터에 맞게 자막 파일 경로를 이스케이프 처리하는 새로운 로직
        String escapedSrtPath = srtFilePath.replace("\\", "/");
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            escapedSrtPath = escapedSrtPath.replaceFirst(":", "\\\\:");
        }

        // 3. subtitles 필터를 사용하는 FFMpeg 명령어 구성
        String subtitleFilter = String.format("subtitles='%s':force_style='FontFile=%s,FontSize=18'",
                escapedSrtPath, FONT_PATH);

        List<String> command = List.of(
                FFMPEG_BIN, "-y",
                "-i", input,
                "-vf", subtitleFilter,
                "-c:a", "copy",
                out
        );


        System.out.println("Executing FFMPEG: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder log = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                log.append(line).append("\n");
            }
        }
        int exit = process.waitFor();

        // 4. 임시로 생성한 SRT 파일 삭제
        new File(srtFilePath).delete();

        if (exit != 0) {
            System.out.println("FFmpeg log: ");
            System.out.println(log);
            throw new RuntimeException("FFmpeg failed: \n" + log);
        }
    }

    private String createSrtFile(List<SpeechSegment> segments) throws IOException {
        File tempFile = File.createTempFile("subtitles", ".srt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile, StandardCharsets.UTF_8))) { // UTF-8 명시
            for (int i = 0; i < segments.size(); i++) {
                SpeechSegment s = segments.get(i);
                System.out.println("Segment " + i + ": start=" + s.getStartTime() + ", end=" + s.getEndTime() + ", text=" + s.getText());
                bw.write(String.valueOf(i + 1));
                bw.newLine();
                bw.write(formatTime(s.getStartTime()) + " --> " + formatTime(s.getEndTime()));
                bw.newLine();
                bw.write(s.getText());
                bw.newLine();
                bw.newLine();
            }
        }
        return tempFile.getAbsolutePath();
    }

    private String formatTime(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        int millis = (int) ((seconds - Math.floor(seconds)) * 1000);
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, secs, millis);
    }
}