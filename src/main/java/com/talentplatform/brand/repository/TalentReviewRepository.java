package com.talentplatform.brand.repository;
import com.talentplatform.brand.model.TalentReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TalentReviewRepository extends JpaRepository<TalentReview, Long> { List<TalentReview> findByBrandId(Long brandId); boolean existsByHireRequestId(Long hireRequestId); }
