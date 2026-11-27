package in.sp.main.repository;

import in.sp.main.entity.ProductionExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductionExpenseRepository extends JpaRepository<ProductionExpense, Long> {
    List<ProductionExpense> findByCompanyIdOrderByExpenseDateDescIdDesc(Long companyId);
    Optional<ProductionExpense> findByIdAndCompanyId(Long id, Long companyId);
}
