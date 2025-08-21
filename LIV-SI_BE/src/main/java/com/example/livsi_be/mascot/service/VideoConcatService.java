package com.example.talkingcharacter.mascot.service;

import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class VideoConcatService {
    public void concatVideos(List<String> videoPaths, String finalOutputVideoPath) throws IOException, InterruptedException {
        // FFmpeg concat demuxer에 사용할 임시 파일 리스트 생성
        File listFile = createTempVideoListFile(videoPaths);

        String[] command = {
                "ffmpeg", "-y",
                "-f", "concat",
                "-safe", "0",
                "-i", listFile.getAbsolutePath(),
                "-c", "copy",
                finalOutputVideoPath
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg video concatenation failed with exit code " + exitCode);
        }

        // 임시 파일 삭제
        listFile.delete();
    }

    private File createTempVideoListFile(List<String> videoPaths) throws IOException {
        File tempFile = File.createTempFile("ffmpeg_list_", ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (String path : videoPaths) {
                writer.write("file '" + new File(path).getAbsolutePath().replace("\\", "/") + "'");
                writer.newLine();
            }
        }
        return tempFile;
    }
}