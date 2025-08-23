package hello.livsi_0820.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "region")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "sido", nullable = false, length = 300)
    private String sido;

    @Column(name = "sigungu", nullable = false, length = 300)
    private String sigungu;

    @Lob
    @Column(name = "mascot_img")
    private byte[] mascotImg;
}



