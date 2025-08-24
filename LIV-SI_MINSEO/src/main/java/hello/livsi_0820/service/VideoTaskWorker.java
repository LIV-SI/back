package hello.livsi_0820.service;

import hello.livsi_0820.entity.Job;
import hello.livsi_0820.repository.JobRepository;
import hello.livsi_0820.status.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoTaskWorker {

    private final JobRepository jobRepository;
    // (VideoGenerationService, ShortsMaker 등 실제 작업에 필요한 다른 서비스들을 여기에 주입)

    @Async
    @Transactional
    public void analyze(String jobId, File tempFile, String sigunguEnglish, String voicePack) {
        try {
            log.info("Job ID [{}] - 영상 분석 및 제작 시작...", jobId);

            // 여기에 기존의 모든 비디오 처리 로직을 넣습니다.
            // 예: Gemini API 호출, ShortsMaker 호출, VideoGenerationService 호출 등
            Thread.sleep(10000); // 테스트용 10초 대기

            String finalVideoUrl = "https://s3..." + jobId + ".mp4";

            // 성공 시 DB 업데이트
            Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
            job.setStatus(JobStatus.COMPLETED);
            job.setResultUrl(finalVideoUrl);
            jobRepository.save(job);

            log.info("Job ID [{}] - 영상 제작 성공. URL: {}", jobId, finalVideoUrl);

        } catch (Exception e) {
            log.error("Job ID [{}] - 영상 제작 실패", jobId, e);

            // 실패 시 DB 업데이트
            Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
            job.setStatus(JobStatus.FAILED);
            jobRepository.save(job);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }
}