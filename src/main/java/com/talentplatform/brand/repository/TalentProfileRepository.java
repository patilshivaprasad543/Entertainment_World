package com.talentplatform.brand.repository;
import com.talentplatform.brand.model.TalentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface TalentProfileRepository extends JpaRepository<TalentProfile, Long>, JpaSpecificationExecutor<TalentProfile> { }
