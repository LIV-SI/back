package hello.livsi_0820.service;

import hello.livsi_0820.entity.Job;
import com.amazonaws.services.s3.AmazonS3;
import hello.livsi_0820.entity.Video;
import hello.livsi_0820.repository.JobRepository;
import hello.livsi_0820.repository.VideoRepository;
import hello.livsi_0820.status.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.net.URL;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {


    private final JobRepository jobRepository;
    private final VideoTaskWorker videoTaskWorker;
    private final VideoRepository videoRepository;
    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;



    @Transactional
    public String requestAnalysis(MultipartFile videoFile, Video videoInfo, String sigunguEnglish, String voicePack) throws IOException {
        String jobId = UUID.randomUUID().toString();

        File tempFile = File.createTempFile("upload-", videoFile.getOriginalFilename());
        videoFile.transferTo(tempFile);

        Job newJob = Job.builder()
                .jobId(jobId)
                .status(JobStatus.PROCESSING)
                .build();
        jobRepository.save(newJob);

        // Worker에게 임시 파일과 함께 영상 정보(videoInfo)도 전달합니다.
        videoTaskWorker.analyze(jobId, tempFile, videoInfo, sigunguEnglish, voicePack);

        return jobId;
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

    @Transactional
    public void deleteVideo(Long videoId) {
        // 1. DB에서 비디오 정보를 조회합니다.
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("해당 비디오를 찾을 수 없습니다. id=" + videoId));

        String videoUrl = video.getVideoUrl();

        // 2. S3에서 실제 파일을 삭제합니다.
        if (videoUrl != null && !videoUrl.isEmpty()) {
            try {
                String fileKey = extractFileKeyFromUrl(videoUrl);
                amazonS3.deleteObject(bucketName, fileKey);
                log.info("S3 파일 삭제 성공: {}", fileKey);
            } catch (Exception e) {
                log.error("S3 파일 삭제 실패: {}", videoUrl, e);
                throw new RuntimeException("S3 파일 삭제 중 오류가 발생했습니다.");
            }
        }

        // 3. DB에서 비디오 정보를 최종적으로 삭제합니다.
        videoRepository.delete(video);
    }

    private String extractFileKeyFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        return url.getPath().substring(1);
    }

    public Optional<Video> save(Video video) {
        return Optional.of(videoRepository.save(video));
    }
}