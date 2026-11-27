package com.talentplatform.brand.repository;
import com.talentplatform.brand.model.HireRequest;
import com.talentplatform.brand.enums.HireRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface HireRequestRepository extends JpaRepository<HireRequest, Long> {
    List<HireRequest> findByBrandId(Long brandId);
    List<HireRequest> findByStatusAndSentAtBefore(HireRequestStatus status, LocalDateTime before);
    List<HireRequest> findByStatusAndSentAtBetween(HireRequestStatus status, LocalDateTime from, LocalDateTime to);
    long countByBrandIdAndStatus(Long brandId, HireRequestStatus status);
}
