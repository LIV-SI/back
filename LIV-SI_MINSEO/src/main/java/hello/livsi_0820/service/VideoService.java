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


    private final JobRepository jobRepository;
    private final VideoTaskWorker videoTaskWorker;
    private final VideoRepository videoRepository;



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

    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }

    public Optional<Video> save(Video video) {
        return Optional.of(videoRepository.save(video));
    }
}