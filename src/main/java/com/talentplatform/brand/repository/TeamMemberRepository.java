package com.talentplatform.brand.repository;
import com.talentplatform.brand.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> { List<TeamMember> findByBrandId(Long brandId); long countByBrandId(Long brandId); }
