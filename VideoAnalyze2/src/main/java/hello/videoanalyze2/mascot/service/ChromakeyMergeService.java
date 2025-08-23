package hello.videoanalyze2.mascot.service;

import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ChromakeyMergeService {

    public void mergeAnimationWithBakcgroundVideo(String BGVFolder, String MASCOTAFolder, String outputVideoPath) throws Exception {

        String filter =
                "[1:v]colorkey=0x00FF00:0.1:0.1,format=yuva420p[fg];" +
                        "[fg]scale=320:-1[fg_scaled];" +
                        "[0:v][fg_scaled]overlay=W-w-30:(H-h)/2[v]";


        String[] command = {
                "ffmpeg", "-y",
                "-stream_loop", "-1",
                "-i", BGVFolder,
                "-i", MASCOTAFolder,
                "-filter_complex", filter,
                "-map", "[v]",
                "-map", "1:a?",
                "-shortest",
                "-c:v", "libx264",
                "-c:a", "copy",
                "-pix_fmt", "yuv420p",
                outputVideoPath
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process p = pb.start();

        int exit = p.waitFor();
        if (exit != 0) {
            throw new RuntimeException("FFmpeg chromakey merge failed with exit " + exit);
        }

        File mascotFile = new File(MASCOTAFolder);
        if (mascotFile.exists()) {
            mascotFile.delete();
        }
    }
}