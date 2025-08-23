package hello.livsi_0820;

import hello.livsi_0820.response.VideoResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ShortsMaker {

    public List<File> makeShorts(File video, VideoResult videoResult) throws IOException, InterruptedException {
        List<File> clipList = new ArrayList<>();
        String inputFile = video.getAbsolutePath();


        for (VideoResult.Scene scene : videoResult.getScenes()) {
            String[] times = scene.getOriginalTimestamp().split("-");
            String start = toHms(times[0]);
            String end = toHms(times[1]);

            // 결과 저장할 폴더(현재는 Spring 실행위치/output -> 배포 시 )
            File clipFile = File.createTempFile("clip"+scene.getSceneNumber()+"_", ".mp4");
            cutScene(inputFile, clipFile.getAbsolutePath(), start, end);
            clipList.add(clipFile);
        }

        log.info("clipList: {}", clipList);
        return clipList;
    }

    // 시:분:초 포맷 변환
    // 현재 로직 -> 10분이상 영상은 변환 불가
    private String toHms(String t) {
        String[] parts = t.split(":");
        if (parts.length == 2) {
            if(parts[0].equals("0")) {
                return "00:0"+ t; // 0:SS -> 00:00:SS
            }
            return "00:" + t;  // MM:SS → 00:MM:SS
        }
        if (parts.length == 1) return "00:00:0" + t; // SS → 00:00:SS
        return t; // 이미 HH:MM:SS
    }

    public void cutScene(String inputPath, String outputPath, String start, String end) throws InterruptedException, IOException {
//        // 실행은 되나 클립별 마지막 1초 구간이 맘에 안듬(안 잘린 듯?), 6개 자르는데 4초
//        ProcessBuilder pb = new ProcessBuilder(
//                "ffmpeg", "-y",
//                "-i", inputPath,
//                "-ss", start,
//                "-to", end,
//                "-c", "copy",
//                outputPath
//        );

        // 구간별 추출 후 재인코딩 방식 -> 빠름 but 6개 자르는데 1분걸림
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", inputPath,           // 입력 파일
                "-ss", start,              // 시작 시간 (00:00:03)
                "-to", end,                // 끝 시간 (00:00:07)
                "-c:v", "libx264",         // 비디오 재인코딩 (정확한 컷팅 가능)
                "-an",                     // 오디오 삭제
//                "-c:a", "aac",             // 오디오 재인코딩
                "-preset", "fast",         // 인코딩 속도 설정
                outputPath
        );
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg failed with exit code " + exitCode);
        }
    }

}
