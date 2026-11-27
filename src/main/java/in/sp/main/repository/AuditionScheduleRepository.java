package in.sp.main.repository;

import in.sp.main.entity.AuditionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AuditionScheduleRepository extends JpaRepository<AuditionSchedule, Long> {
    List<AuditionSchedule> findByCompanyIdOrderByAuditionDateAscIdDesc(Long companyId);
    Optional<AuditionSchedule> findByIdAndCompanyId(Long id, Long companyId);
    void deleteByProjectIdAndCompanyId(Long projectId, Long companyId);
}
