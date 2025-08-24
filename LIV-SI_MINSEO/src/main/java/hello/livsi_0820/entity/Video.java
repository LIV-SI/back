package hello.livsi_0820.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "video")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_id", nullable = true)
    private Long videoId;

    @Column(nullable = true, length = 100)
    private String title;

    @Column(nullable = true, length = 200)
    private String description;

    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    @Column(nullable = false, length = 50)
    private String sido;

    @Column(nullable = false, length = 50)
    private String sigungu;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = true)
    private Store store;
}
