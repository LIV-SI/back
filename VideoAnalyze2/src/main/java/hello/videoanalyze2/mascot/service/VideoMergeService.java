package hello.videoanalyze2.mascot.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;


@Service
public class VideoMergeService {
    public void mergeFramesWithAudio(String framesFolder, String audioPath, String outputVideoPath) throws IOException, InterruptedException {
        // 프레임 폴더 존재 확인
        File frameDir = new File(framesFolder);
        if (!frameDir.exists()) frameDir.mkdirs();

        String[] command = {
                "ffmpeg", "-y",
                "-framerate", "30",
                "-i", framesFolder + "/frame_%04d.png",
                "-i", audioPath,
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                "-c:a", "aac",
                "-shortest",
                outputVideoPath
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO(); // FFmpeg 콘솔 출력 확인
        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg failed with exit code " + exitCode);
        }

        // 임시 프레임 삭제
        clearDirectory(frameDir);
    }

    private void clearDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    clearDirectory(file);
                }
                file.delete();
            }
        }
    }
}