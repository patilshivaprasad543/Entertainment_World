package in.sp.main.repository;

import in.sp.main.entity.SubscriptionPass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriptionPassRepository extends JpaRepository<SubscriptionPass, Long> {
    List<SubscriptionPass> findAllByOrderByDisplayOrderAsc();
    Optional<SubscriptionPass> findByPassCode(String passCode);
}
