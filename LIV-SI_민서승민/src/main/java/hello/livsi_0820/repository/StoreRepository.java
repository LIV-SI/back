package hello.livsi_0820.repository;

import hello.livsi_0820.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
