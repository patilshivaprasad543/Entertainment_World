package in.sp.main.repository;

import in.sp.main.entity.ArtistFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArtistFolderRepository extends JpaRepository<ArtistFolder, Long> {
    List<ArtistFolder> findByCompanyIdOrderByIdAsc(Long companyId);
    Optional<ArtistFolder> findByIdAndCompanyId(Long id, Long companyId);
    Optional<ArtistFolder> findByCompanyIdAndFolderNameIgnoreCase(Long companyId, String folderName);
}
