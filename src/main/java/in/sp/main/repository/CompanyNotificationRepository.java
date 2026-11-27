package in.sp.main.repository;

import in.sp.main.entity.CompanyNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanyNotificationRepository extends JpaRepository<CompanyNotification, Long> {
    List<CompanyNotification> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    long countByCompanyIdAndReadFlagFalse(Long companyId);
}
