package hello.livsi_0820.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.livsi_0820.ShortsMaker;
import hello.livsi_0820.entity.Job;
import hello.livsi_0820.mascot.model.SceneData;
import hello.livsi_0820.mascot.service.VideoGenerationService;
import hello.livsi_0820.repository.JobRepository;
import hello.livsi_0820.request.GeminiReqDto;
import hello.livsi_0820.response.GeminiResDto;
import hello.livsi_0820.response.VideoResult;
import hello.livsi_0820.status.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoTaskWorker {

    private final JobRepository jobRepository;
    private final RestTemplate restTemplate;
    private final ShortsMaker shortsMaker;
    private final VideoGenerationService videoGenerationService;
    private final ObjectMapper objectMapper;

    @Value("${gemini.key}")
    private String GEMINI_API_KEY;

    @Value("${gemini.requestText}")
    private String requestText;

    @Async
    @Transactional
    public void analyze(String jobId, File tempFile, String sigunguEnglish, String voicePack) {
        try {
            log.info("Job ID [{}] - 실제 비동기 작업 시작...", jobId);

            // 1. Gemini 서버에 임시 업로드
            String fileId = uploadFile(tempFile);

            // 2. Gemini 영상 분석 요청
            String geminiURL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + GEMINI_API_KEY;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            GeminiReqDto request = GeminiReqDto.createVideoRequest("video/mp4", fileId, requestText);

            // 디버깅용 요청 본문 로그
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                // request 객체를 JSON 문자열로 예쁘게 출력 (pretty print)
                String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
                log.info("실제 전송될 JSON 요청 본문:\n{}", jsonRequest);
            } catch (Exception e) {
                log.error("JSON 변환 중 오류 발생", e);
            }
            HttpEntity<GeminiReqDto> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<GeminiResDto> response = restTemplate.exchange(geminiURL, HttpMethod.POST, requestEntity, GeminiResDto.class);
            log.info("Job ID [{}] - Gemini 응답 수신", jobId);

            // 3. Gemini 응답에서 순수 JSON만 추출
            String rawResponseText = response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();
            log.info("========== RAW GEMINI RESPONSE START (Job ID: {}) ==========", jobId);
            log.info(rawResponseText);
            log.info("========== RAW GEMINI RESPONSE END (Job ID: {}) ==========", jobId);

            int startIndex = rawResponseText.indexOf("{");
            int endIndex = rawResponseText.lastIndexOf("}");
            if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) {
                throw new IOException("Gemini 응답에서 유효한 JSON을 찾을 수 없습니다: " + rawResponseText);
            }
            String jsonResult = rawResponseText.substring(startIndex, endIndex + 1);

            // 4. 영상 제작 로직 실행
            VideoResult videoResult = objectMapper.readValue(jsonResult, VideoResult.class);
            List<File> clips = shortsMaker.makeShorts(tempFile, videoResult);
            List<String> contents = new ArrayList<>();
            for(VideoResult.Scene scene : videoResult.getScenes()) {
                contents.add(scene.getContent());
            }

            List<SceneData> sceneDataList = new ArrayList<>();
            for(int i=0; i<clips.size(); i++) {
                SceneData sceneData = new SceneData();
                sceneData.setBGVN(clips.get(i).getAbsolutePath());
                sceneData.setText(contents.get(i));
                sceneDataList.add(sceneData);
            }

            videoGenerationService.generateMultiSceneVideo(sigunguEnglish, voicePack, sceneDataList);

            // 5. 성공 처리
            String finalVideoUrl = "https://your-s3-bucket.s3.ap-northeast-2.amazonaws.com/videos/" + jobId + ".mp4"; // 최종 결과물 URL
            Job job = jobRepository.findById(jobId).orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));
            job.setStatus(JobStatus.COMPLETED);
            job.setResultUrl(finalVideoUrl);
            jobRepository.save(job);
            log.info("Job ID [{}] - 영상 제작 성공.", jobId);

        } catch (Exception e) {
            log.error("Job ID [{}] - 영상 제작 실패", jobId, e);
            // 6. 실패 처리
            Job job = jobRepository.findById(jobId).orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));
            job.setStatus(JobStatus.FAILED);
            // (선택) job에 에러 메시지를 저장하는 필드가 있다면 여기에 저장
            jobRepository.save(job);
        } finally {
            // 7. 임시 파일 삭제
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // `uploadFile`과 `waitUntilFileisActive` 메서드 (기존과 동일)
    public String uploadFile(File file) throws IOException, InterruptedException {
        String url = "https://generativelanguage.googleapis.com/upload/v1beta/files?key="
                +GEMINI_API_KEY;


        // === 1. 업로드 세션 시작 ===
        String startUrl = "https://generativelanguage.googleapis.com/upload/v1beta/files?key=" + GEMINI_API_KEY;

        HttpHeaders startHeaders = new HttpHeaders();
        startHeaders.add("X-Goog-Upload-Protocol", "resumable");
        startHeaders.add("X-Goog-Upload-Command", "start");
        startHeaders.add("X-Goog-Upload-Header-Content-Length", String.valueOf(file.length()));
        startHeaders.add("X-Goog-Upload-Header-Content-Type", "video/mp4");

        HttpEntity<String> startRequest = new HttpEntity<>(startHeaders);

        ResponseEntity<String> startResponse = restTemplate.exchange(startUrl, HttpMethod.POST, startRequest, String.class);

        // 업로드 세션 URL 획득
        String uploadUrl = startResponse.getHeaders().getFirst("X-Goog-Upload-URL");
        log.info("Upload session URL={}", uploadUrl);

        // === 2. 실제 파일 업로드 ===
        HttpHeaders uploadHeaders = new HttpHeaders();
        uploadHeaders.setContentType(MediaType.parseMediaType("video/mp4"));
        uploadHeaders.add("X-Goog-Upload-Command", "upload, finalize");
        uploadHeaders.add("X-Goog-Upload-Offset", "0");

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        HttpEntity<byte[]> uploadRequest = new HttpEntity<>(fileBytes, uploadHeaders);

        ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, uploadRequest, String.class);
        // 업로드한 파일이 ACTIVE 상태가 될 때까지 기다리기
        waitUntilFileisActive(response);

        log.info("Upload response: {}", response.getBody());

        JsonNode node = objectMapper.readTree(response.getBody());
        log.info("node: {}", node);
        String fileId = node.get("file").get("uri").asText();  // 예: "files/abc123"
        log.info("Uploaded fileId={}", fileId);
        return fileId;
    }

    private void waitUntilFileisActive(ResponseEntity<String> response) throws JsonProcessingException, InterruptedException {
        int retry = 0;
        while (retry < 10) {
            JsonNode fileNode = objectMapper.readTree(response.getBody()).get("file");
            String state = fileNode.get("state").asText();
            if ("ACTIVE".equals(state)) {
                break;
            }
            Thread.sleep(1000); // 1초 대기
            retry++;
        }
    }
}