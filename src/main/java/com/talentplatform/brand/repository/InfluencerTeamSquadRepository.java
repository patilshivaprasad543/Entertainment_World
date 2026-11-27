package com.talentplatform.brand.repository;
import com.talentplatform.brand.model.InfluencerTeamSquad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface InfluencerTeamSquadRepository extends JpaRepository<InfluencerTeamSquad, Long> { List<InfluencerTeamSquad> findByBrandId(Long brandId); }
