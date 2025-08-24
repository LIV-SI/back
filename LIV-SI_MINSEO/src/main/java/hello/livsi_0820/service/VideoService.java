package hello.livsi_0820.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import hello.livsi_0820.entity.Job;
import hello.livsi_0820.entity.Member;
import hello.livsi_0820.entity.Region;
import hello.livsi_0820.entity.Store;
import hello.livsi_0820.entity.Video;
import hello.livsi_0820.repository.JobRepository;
import hello.livsi_0820.repository.MemberRepository;
import hello.livsi_0820.repository.StoreRepository;
import hello.livsi_0820.repository.VideoRepository;
import hello.livsi_0820.status.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    // --- 비동기 처리에 필요한 의존성 ---
    private final JobRepository jobRepository;
    private final VideoTaskWorker videoTaskWorker; // 비동기 작업자를 주입받음

    // --- 기존의 다른 기능에 필요한 의존성 ---
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final VideoRepository videoRepository;
    private final RegionService regionService;
    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;


    @Transactional
    public String requestAnalysis(MultipartFile video, String sigunguEnglish, String voicePack) throws IOException {
        String jobId = UUID.randomUUID().toString();

        // 1. 임시 파일 생성
        File tempFile = File.createTempFile("upload-", video.getOriginalFilename());
        video.transferTo(tempFile);

        // 2. DB에 작업 상태 기록
        Job newJob = Job.builder()
                .jobId(jobId)
                .status(JobStatus.PROCESSING)
                .build();
        jobRepository.save(newJob);

        // 3. 실제 작업은 Worker에게 위임
        videoTaskWorker.analyze(jobId, tempFile, sigunguEnglish, voicePack);

        // 4. 즉시 jobId 반환
        return jobId;
    }


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