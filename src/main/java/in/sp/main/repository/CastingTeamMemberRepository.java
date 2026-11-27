package in.sp.main.repository;

import in.sp.main.entity.CastingTeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CastingTeamMemberRepository extends JpaRepository<CastingTeamMember, Long> {
    List<CastingTeamMember> findByCompanyIdOrderByIdDesc(Long companyId);
    Optional<CastingTeamMember> findByIdAndCompanyId(Long id, Long companyId);
}
