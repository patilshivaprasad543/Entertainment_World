package in.sp.main.repository;

import in.sp.main.entity.ArtistContract;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArtistContractRepository extends JpaRepository<ArtistContract, Long> {
    List<ArtistContract> findByCompanyIdOrderByIdDesc(Long companyId);
    Optional<ArtistContract> findByIdAndCompanyId(Long id, Long companyId);
}
