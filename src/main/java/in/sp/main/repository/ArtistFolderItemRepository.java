package in.sp.main.repository;

import in.sp.main.entity.ArtistFolderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArtistFolderItemRepository extends JpaRepository<ArtistFolderItem, Long> {
    List<ArtistFolderItem> findByCompanyIdOrderBySavedAtDesc(Long companyId);
    List<ArtistFolderItem> findByCompanyIdAndFolderIdOrderBySavedAtDesc(Long companyId, Long folderId);
    Optional<ArtistFolderItem> findByCompanyIdAndFolderIdAndArtistId(Long companyId, Long folderId, Long artistId);
    void deleteByCompanyIdAndFolderId(Long companyId, Long folderId);
    void deleteByCompanyIdAndArtistId(Long companyId, Long artistId);
}
