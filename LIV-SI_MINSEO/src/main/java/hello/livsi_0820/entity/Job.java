package hello.livsi_0820.entity;

import hello.livsi_0820.status.JobStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Job {

    @Id
    @Column(name = "job_id", length = 36)
    private String jobId; // UUID를 저장하기 위해 String 타입 사용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(name = "result_url", length = 2048)
    private String resultUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Job(String jobId, JobStatus status, String resultUrl) {
        this.jobId = jobId;
        this.status = status;
        this.resultUrl = resultUrl;
    }
}