package hello.livsi_0820.controller;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import hello.livsi_0820.entity.Video;
import hello.livsi_0820.service.VideoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Video> uploadVideo(@RequestPart("videoFile") MultipartFile videoFile,
                                             @RequestPart("video") String videoJsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Video video = objectMapper.readValue(videoJsonString, Video.class);

            Video savedVideo = videoService.saveVideo(videoFile, video);
            return new ResponseEntity<>(savedVideo, HttpStatus.CREATED);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sido")
    public List<Video> getVideosBySIdo(@RequestParam String sido) {
        return videoService.findBySido(sido);
    }

    @GetMapping("/district")
    public List<Video> getVideosBySigungu(@RequestParam String sigungu) {
        return videoService.findBySigungu(sigungu);
    }

    @GetMapping("/{id}")
    public Optional<Video> getVideoById(@PathVariable Long id) {
        return videoService.findById(id);
    }

    @PutMapping("/{id}")
    public Optional<Video> updateVideo(@PathVariable Long id, @RequestBody Video updatedVideo) {
        return videoService.updateVideo(id, updatedVideo);
    }

    @DeleteMapping("/{id}")
    public String deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return "Video with id " + id + " deleted successfully.";
    }


    @PostMapping("/video-analyze")
    public String analyze(@RequestParam MultipartFile video,
                          @RequestParam String sigunguEnglish, @RequestParam String voicePack) throws Exception {

        videoService.analyze(video, sigunguEnglish, voicePack);
        return "ok";
    }
}