package hello.livsi_0820.controller;

import hello.livsi_0820.entity.Video;
import hello.livsi_0820.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Video API", description = "영상 생성, 업로드 및 정보 관리를 위한 API")
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "영상 및 메타데이터 업로드", description = "영상 파일(videoFile)과 영상 정보(video)를 multipart/form-data 형식으로 함께 업로드합니다.")
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
    @Operation(summary = "시/도 별 영상 조회", description = "특정 시/도에 해당하는 영상 목록을 조회합니다.")
    public List<Video> getVideosBySIdo(@RequestParam String sido) {
        return videoService.findBySido(sido);
    }

    @GetMapping("/district")
    @Operation(summary = "시/군/구 별 영상 조회", description = "특정 시/군/구에 해당하는 영상 목록을 조회합니다.")
    public List<Video> getVideosBySigungu(@RequestParam String sigungu) {
        return videoService.findBySigungu(sigungu);
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 영상 조회 (ID)", description = "ID를 사용하여 특정 영상을 조회합니다.")
    public Optional<Video> getVideoById(@PathVariable Long id) {
        return videoService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "영상 정보 수정", description = "ID를 사용하여 기존 영상의 정보를 수정합니다.")
    public Optional<Video> updateVideo(@PathVariable Long id, @RequestBody Video updatedVideo) {
        return videoService.updateVideo(id, updatedVideo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "영상 삭제", description = "ID를 사용하여 특정 영상을 삭제합니다.")
    public String deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return "Video with id " + id + " deleted successfully.";
    }


    @PostMapping("/video-analyze")
    @Operation(summary = "영상 생성 요청", description = "영상을 업로드하여 생성, 다음API의 sigunguEnglish값, 남성,여성")
    public String analyze(@RequestParam MultipartFile video,
                          @RequestParam String sigunguEnglish, @RequestParam String voicePack) throws Exception {

        videoService.analyze(video, sigunguEnglish, voicePack);
        return "ok";
    }
}