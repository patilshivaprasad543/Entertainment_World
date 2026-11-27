package in.sp.main.repository;

import in.sp.main.entity.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {
    List<SubscriptionPayment> findByCompanyIdOrderByPaidAtDesc(Long companyId);
}
