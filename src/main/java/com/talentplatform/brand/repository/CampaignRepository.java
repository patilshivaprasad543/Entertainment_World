package com.talentplatform.brand.repository;
import com.talentplatform.brand.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CampaignRepository extends JpaRepository<Campaign, Long> { List<Campaign> findByBrandId(Long brandId); long countByBrandIdAndStatus(Long brandId, String status); }
