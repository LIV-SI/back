package hello.livsi_0820.service;

import hello.livsi_0820.entity.Region;
import hello.livsi_0820.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    public Optional<Region> findBySidoAndSigungu(String sido, String sigungu) {
        return regionRepository.findBySidoAndSigungu(sido, sigungu);
    }

    public Region save(Region region) {
        return regionRepository.save(region);
    }
}