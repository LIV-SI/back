package hello.videoanalyze2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.videoanalyze2.mascot.model.SceneData;
import hello.videoanalyze2.mascot.service.VideoGenerationService;
import hello.videoanalyze2.request.GeminiReqDto;
import hello.videoanalyze2.response.GeminiResDto;
import hello.videoanalyze2.response.VideoResult;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class VideoService {


    // 최적화 : API KEY 환경변수 설정으로 변경
    @Value("${gemini.key}")
    String GEMINI_API_KEY;

    @Value("${gemini.requestText}")
    String requestText;

    // 최적화 : 스프링빈 등록 -> RestTemplate 싱글톤화
    private final RestTemplate restTemplate;
    private final ClipCutter clipCutter;
    private final VideoGenerationService videoGenerationService;
    public VideoService(RestTemplate restTemplate, ClipCutter clipCutter, VideoGenerationService videoGenerationService) {
        this.restTemplate = restTemplate;
        this.clipCutter = clipCutter;
        this.videoGenerationService = videoGenerationService;
    }

    ObjectMapper objectMapper = new ObjectMapper();


    public void analyze(MultipartFile video, String sigunguEnglish, String voicePack) throws Exception {
        // temp 파일에 영상 저장
        File tempFile = File.createTempFile("upload-", UUID.randomUUID() + "");
        video.transferTo(tempFile);

        // Gemini 서버에 임시 업로드(영상 주소 받기 -> api 요청에 사용)
        String fileId = uploadFile(tempFile);


        // Gemini 영상 분석 요청
        String geminiURL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                + GEMINI_API_KEY;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        GeminiReqDto request = GeminiReqDto.createVideoRequest("video/mp4", fileId, requestText);

        // --- 로깅 코드 추가 ---
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // request 객체를 JSON 문자열로 예쁘게 출력 (pretty print)
            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            log.info("실제 전송될 JSON 요청 본문:\n{}", jsonRequest);
        } catch (Exception e) {
            log.error("JSON 변환 중 오류 발생", e);
        }
        // --- 로깅 코드 끝 ---

        HttpEntity<GeminiReqDto> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<GeminiResDto> response;
            response = restTemplate.exchange(
                    geminiURL,
                    HttpMethod.POST,
                    requestEntity,
                    GeminiResDto.class
            );
        String jsonResult = response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();

        // JSON 형식으로 변환
        jsonResult = convertJSON(jsonResult);

        // --- 로깅 코드 추가 ---
        VideoResult videoResult = objectMapper.readValue(jsonResult, VideoResult.class);
        log.info("Concept: {}", videoResult.getConcept());
        for (VideoResult.Scene scene : videoResult.getScenes()) {
            log.info("Scene {}: {} [{}]", scene.getSceneNumber(), scene.getContent(), scene.getOriginalTimestamp());
        }
        // --- 로깅 코드 끝 ---

        List<File> clips = clipCutter.makeShorts(tempFile, videoResult);
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

        // --- 로깅 코드 추가 ---
        for(SceneData sceneData : sceneDataList) {
            log.info("sceneData: {}",sceneData);
        }
        // --- 로깅 코드 끝 ---

        // 영상 분석 정보 + 사용자 입력 정보 토대로 최종 영상 생성
        videoGenerationService.generateMultiSceneVideo(sigunguEnglish, voicePack, sceneDataList);
    }

    @NotNull
    private static String convertJSON(String jsonResult) {
        // jsonResult의 백틱 제거
        if (jsonResult.startsWith("```")) {
            jsonResult = jsonResult.substring(jsonResult.indexOf("{"));
        }
        if (jsonResult.endsWith("```")) {
            jsonResult = jsonResult.substring(0, jsonResult.lastIndexOf("}") + 1);
        }        // 맨 뒤 ``` 제거
        return jsonResult;
    }

    public String uploadFile(File file) throws IOException, InterruptedException {
        String url = "https://generativelanguage.googleapis.com/upload/v1beta/files?key="
                +GEMINI_API_KEY;


        // 1. 업로드 세션 시작
        String startUrl = "https://generativelanguage.googleapis.com/upload/v1beta/files?key=" + GEMINI_API_KEY;

        HttpHeaders startHeaders = new HttpHeaders();
        startHeaders.add("X-Goog-Upload-Protocol", "resumable");
        startHeaders.add("X-Goog-Upload-Command", "start");
        startHeaders.add("X-Goog-Upload-Header-Content-Length", String.valueOf(file.length()));
        startHeaders.add("X-Goog-Upload-Header-Content-Type", "video/mp4");

        HttpEntity<String> startRequest = new HttpEntity<>(startHeaders);

        ResponseEntity<String> startResponse = restTemplate.exchange(
                startUrl,
                HttpMethod.POST,
                startRequest,
                String.class
        );

        // 업로드 세션 URL 획득
        String uploadUrl = startResponse.getHeaders().getFirst("X-Goog-Upload-URL");
        log.info("Upload session URL={}", uploadUrl);

        // 2. 실제 파일 업로드
        HttpHeaders uploadHeaders = new HttpHeaders();
        uploadHeaders.setContentType(MediaType.parseMediaType("video/mp4"));
        uploadHeaders.add("X-Goog-Upload-Command", "upload, finalize");
        uploadHeaders.add("X-Goog-Upload-Offset", "0");

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        HttpEntity<byte[]> uploadRequest = new HttpEntity<>(fileBytes, uploadHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                uploadRequest,
                String.class
        );
        // 업로드한 파일이 ACTIVE 상태가 될 때까지 기다리기(최대 10초)
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
