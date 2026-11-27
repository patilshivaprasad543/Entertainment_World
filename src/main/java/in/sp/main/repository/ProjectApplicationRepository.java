package in.sp.main.repository;

import in.sp.main.entity.ProjectApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {
    List<ProjectApplication> findByProjectIdOrderByAppliedAtDesc(Long projectId);
    List<ProjectApplication> findByProjectIdInOrderByAppliedAtDesc(List<Long> projectIds);
    List<ProjectApplication> findByArtistIdOrderByAppliedAtDesc(Long artistId);
    long countByProjectId(Long projectId);
    Optional<ProjectApplication> findByProjectIdAndArtistId(Long projectId, Long artistId);
    void deleteByProjectId(Long projectId);
}
