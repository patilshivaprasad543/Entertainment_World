package in.sp.main.repository;

import in.sp.main.entity.CastingCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CastingCompanyRepository extends JpaRepository<CastingCompany, Long> {
    Optional<CastingCompany> findByCompanyEmailIgnoreCase(String companyEmail);
    boolean existsByCompanyEmailIgnoreCase(String companyEmail);
    List<CastingCompany> findAllByOrderByCreatedAtDesc();
    List<CastingCompany> findByAccountStatusOrderByCreatedAtDesc(String accountStatus);
}
