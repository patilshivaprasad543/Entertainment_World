package in.sp.main.repository;

import in.sp.main.entity.ContactPortfolioHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactPortfolioHistoryRepository extends JpaRepository<ContactPortfolioHistory, Long> {
    List<ContactPortfolioHistory> findByCompanyIdAndViewTypeOrderByViewedAtDesc(Long companyId, String viewType);
    List<ContactPortfolioHistory> findByCompanyIdOrderByViewedAtDesc(Long companyId);
    boolean existsByCompanyIdAndArtistIdAndViewType(Long companyId, Long artistId, String viewType);
    long countByCompanyIdAndViewType(Long companyId, String viewType);
}
