package in.sp.main.repository;

import in.sp.main.entity.CommunicationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommunicationMessageRepository extends JpaRepository<CommunicationMessage, Long> {
    List<CommunicationMessage> findByCompanyIdOrderBySentAtDesc(Long companyId);
    List<CommunicationMessage> findByCompanyIdAndArtistIdOrderBySentAtDesc(Long companyId, Long artistId);
}
