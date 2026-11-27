package com.talentplatform.brand.repository;
import com.talentplatform.brand.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByBrandIdOrderByCreatedAtDesc(Long brandId);
    boolean existsByBrandIdAndTypeAndMessage(Long brandId, String type, String message);
}
