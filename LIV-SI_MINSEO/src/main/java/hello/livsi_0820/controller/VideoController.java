package hello.livsi_0820.controller;

import hello.livsi_0820.entity.Job;
import hello.livsi_0820.entity.Video;
import hello.livsi_0820.repository.JobRepository;
import hello.livsi_0820.service.VideoService;
import hello.livsi_0820.status.JobStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Video API", description = "영상 생성, 업로드 및 정보 관리를 위한 API")
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/video-analyze/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "영상 생성 요청 접수")
    public ResponseEntity<Map<String, String>> requestAnalyze(
            @RequestPart("videoFile") MultipartFile videoFile,
            @RequestPart("video") String videoJsonString, // 영상 정보를 JSON 문자열로 받음
            @RequestParam String sigunguEnglish,
            @RequestParam String voicePack) throws IOException {

        // JSON 문자열을 Video 객체로 변환
        Video videoInfo = objectMapper.readValue(videoJsonString, Video.class);

        // 서비스에 파일과 영상 정보를 모두 전달
        String jobId = videoService.requestAnalysis(videoFile, videoInfo, sigunguEnglish, voicePack);

        Map<String, String> response = new HashMap<>();
        response.put("jobId", jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/video-analyze/status/{jobId}")
    @Operation(summary = "영상 생성 상태 확인")
    public ResponseEntity<Map<String, Object>> getAnalyzeStatus(@PathVariable String jobId) {

        // DB에서 Job 엔티티를 조회합니다.
        Optional<Job> optionalJob = jobRepository.findById(jobId);

        if (optionalJob.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Job job = optionalJob.get();
        Map<String, Object> response = new HashMap<>();
        response.put("jobId", job.getJobId());
        response.put("status", job.getStatus());
        response.put("createdAt", job.getCreatedAt());
        response.put("updatedAt", job.getUpdatedAt());

        // 작업이 완료되었으면 결과 URL도 함께 전달
        if (job.getStatus() == JobStatus.COMPLETED) {
            response.put("resultUrl", job.getResultUrl());
        }

        return ResponseEntity.ok(response);
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

}