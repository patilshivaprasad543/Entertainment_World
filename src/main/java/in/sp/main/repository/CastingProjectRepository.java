package in.sp.main.repository;

import in.sp.main.entity.CastingProject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CastingProjectRepository extends JpaRepository<CastingProject, Long> {
    List<CastingProject> findByCompanyIdOrderByPostedDateDescIdDesc(Long companyId);
    List<CastingProject> findByStatusIgnoreCaseOrderByPostedDateDescIdDesc(String status);
    Optional<CastingProject> findByIdAndCompanyId(Long id, Long companyId);
    long countByCompanyIdAndStatus(Long companyId, String status);
}
