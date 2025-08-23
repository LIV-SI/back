package hello.livsi_0820.repository;


import hello.livsi_0820.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findBySido(String sido);
    List<Video> findBySigungu(String sigungu);

}
