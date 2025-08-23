package hello.livsi_0820.service;

//import com.amazonaws.HttpMethod;
import org.springframework.http.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import hello.livsi_0820.ShortsMaker;
import hello.livsi_0820.entity.Member;
import hello.livsi_0820.entity.Region;
import hello.livsi_0820.entity.Store;
import hello.livsi_0820.entity.Video;
import hello.livsi_0820.repository.MemberRepository;
import hello.livsi_0820.repository.StoreRepository;
import hello.livsi_0820.repository.VideoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import hello.livsi_0820.mascot.model.SceneData;
import hello.livsi_0820.mascot.service.VideoGenerationService;
import hello.livsi_0820.request.GeminiReqDto;
import hello.livsi_0820.response.GeminiResDto;
import hello.livsi_0820.response.VideoResult;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final VideoRepository videoRepository;
    private final RegionService regionService;
    private final AmazonS3 amazonS3;

    @Value("${gemini.key}")
    String GEMINI_API_KEY;

    @Value("${gemini.requestText}")
    String requestText;

    @Value("${google.cloud.key}")
    String serviceAccountPath;

    // 최적화 : 스프링빈 등록 -> RestTemplate 싱글톤화
    private final RestTemplate restTemplate;
    private final ShortsMaker shortsMaker;
    private final VideoGenerationService videoGenerationService;
//    public VideoService(RestTemplate restTemplate,  ShortsMaker shortsMaker, VideoGenerationService videoGenerationService) {
//        this.restTemplate = restTemplate;
//        this.shortsMaker = shortsMaker;
//        this.videoGenerationService = videoGenerationService;
//    }

    ObjectMapper objectMapper = new ObjectMapper();


    public void analyze(MultipartFile video, String sigunguEnglish, String voicePack) throws Exception {
        // api 요청용 토큰
//        String accessToken = getAccessToken(serviceAccountPath);

        // temp 파일에 영상 저장
        File tempFile = File.createTempFile("upload-", UUID.randomUUID() + "");
        video.transferTo(tempFile);

        // Gemini 서버에 임시 업로드(영상 주소 받기 -> api 요청에 사용)
        String fileId = uploadFile(tempFile);


        // Gemini 영상 분석 요청
        String geminiURL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                + GEMINI_API_KEY;
        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

//        GenerationConfig config = GenerationConfig.builder()
//                .temperature(0.3f)          // 창의성 낮춤 (0.2 ~ 0.5 추천)
//                .maxOutputTokens(8192)      // 최대 출력 토큰 길이 (넉넉하게 설정)
//                .build();
        GeminiReqDto request = GeminiReqDto.createVideoRequest("video/mp4", fileId, requestText);

        // --- 디버깅 코드 추가 ---
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // request 객체를 JSON 문자열로 예쁘게 출력 (pretty print)
            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            log.info("실제 전송될 JSON 요청 본문:\n{}", jsonRequest);
        } catch (Exception e) {
            log.error("JSON 변환 중 오류 발생", e);
        }
        // --- 디버깅 코드 끝 ---
        HttpEntity<GeminiReqDto> requestEntity = new HttpEntity<>(request, headers);

        // do-while -> gemini가 response에 제대로 응답할 때까지 계속 질문
        ResponseEntity<GeminiResDto> response;
        response = restTemplate.exchange(geminiURL,HttpMethod.POST, requestEntity, GeminiResDto.class);
        log.info("response: {}", response);

        String jsonResult = response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();
        log.info("jsonResult={}", jsonResult);

        // jsonResult의 백틱 제거
        if (jsonResult.startsWith("```")) {
            jsonResult = jsonResult.substring(jsonResult.indexOf("{"));
        }
        if (jsonResult.endsWith("```")) {
            jsonResult = jsonResult.substring(0, jsonResult.lastIndexOf("}") + 1);
        }        // 맨 뒤 ``` 제거

        VideoResult videoResult = objectMapper.readValue(jsonResult, VideoResult.class);
        log.info("Concept: {}", videoResult.getConcept());
        for (VideoResult.Scene scene : videoResult.getScenes()) {
            log.info("Scene {}: {} [{}]", scene.getSceneNumber(), scene.getContent(), scene.getOriginalTimestamp());
        }

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

        for(SceneData sceneData : sceneDataList) {
            log.info("sceneData: {}",sceneData);
        }
        videoGenerationService.generateMultiSceneVideo(sigunguEnglish, voicePack, sceneDataList);
    }

    /**
     * 서비스 계정 JSON으로 Access Token 발급
     */
    private String getAccessToken(String serviceAccountPath) throws IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(serviceAccountPath))
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

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

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Transactional
    public Video saveVideo(MultipartFile videoFile, Video video) throws IOException {

        String originalFilename = videoFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = "videos/" + UUID.randomUUID() + fileExtension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(videoFile.getSize());
        metadata.setContentType(videoFile.getContentType());

        amazonS3.putObject(bucketName, fileName, videoFile.getInputStream(), metadata);

        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, fileName)
                        .withMethod(com.amazonaws.HttpMethod.GET)
                        .withExpiration(expiration);

        String videoUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
        video.setVideoUrl(videoUrl);

        Member memberRequest = video.getMember();
        if (memberRequest == null || memberRequest.getEmail() == null) {
            throw new IllegalArgumentException("회원 정보가 없습니다.");
        }
        Member member = memberRepository.findByEmail(memberRequest.getEmail())
                .orElseGet(() -> memberRepository.save(memberRequest));
        video.setMember(member);

        Region region;
        if (video.getSido() != null && video.getSigungu() != null) {
            region = regionService.findBySidoAndSigungu(video.getSido(), video.getSigungu())
                    .orElseGet(() -> {
                        Region newRegion = new Region();
                        newRegion.setSido(video.getSido());
                        newRegion.setSigungu(video.getSigungu());
                        return regionService.save(newRegion);
                    });
        } else {
            throw new IllegalArgumentException("지역 정보가 없습니다.");
        }
        video.setRegion(region);

        Store storeRequest = video.getStore();
        if (storeRequest == null || storeRequest.getStoreName() == null) {
            throw new IllegalArgumentException("가게 정보가 없습니다.");
        }
        storeRequest.setRegion(region);
        Store savedStore = storeRepository.save(storeRequest);
        video.setStore(savedStore);

        return videoRepository.save(video);
    }

    public List<Video> findAll() {
        return videoRepository.findAll();
    }

    public List<Video> findBySido(String sido) {
        return videoRepository.findBySido(sido);
    }

    public List<Video> findBySigungu(String sigungu) {
        return videoRepository.findBySigungu(sigungu);
    }

    public Optional<Video> findById(Long id) {
        return videoRepository.findById(id);
    }

    public Optional<Video> updateVideo(Long id, Video updatedVideo) {
        return videoRepository.findById(id)
                .map(video -> {
                    video.setTitle(updatedVideo.getTitle());
                    video.setThumbnailUrl(updatedVideo.getThumbnailUrl());
                    video.setVideoUrl(updatedVideo.getVideoUrl());
                    video.setSido(updatedVideo.getSido());
                    video.setSigungu(updatedVideo.getSigungu());

                    if (video.getRegion() != null) {
                        video.getRegion().setSido(updatedVideo.getSido());
                        video.getRegion().setSigungu(updatedVideo.getSigungu());
                    }

                    return videoRepository.save(video);
                });
    }

    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }
}