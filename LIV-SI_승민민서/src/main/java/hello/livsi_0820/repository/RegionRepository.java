package hello.livsi_0820.repository;

import hello.livsi_0820.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findBySidoAndSigungu(String sido, String sigungu);
}