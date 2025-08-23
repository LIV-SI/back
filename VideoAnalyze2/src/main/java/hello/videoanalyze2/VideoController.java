package hello.videoanalyze2;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping("/video-analyze")
    public String analyze(@RequestParam MultipartFile video,
           @RequestParam String sigunguEnglish, @RequestParam String voicePack) throws Exception {

        videoService.analyze(video, sigunguEnglish, voicePack);
        return "ok";
    }


}
