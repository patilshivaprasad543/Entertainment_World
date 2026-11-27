package in.sp.main.repository;

import in.sp.main.entity.SearchHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SearchHistoryEntryRepository extends JpaRepository<SearchHistoryEntry, Long> {
    List<SearchHistoryEntry> findTop10ByCompanyIdOrderBySearchedAtDesc(Long companyId);
}
