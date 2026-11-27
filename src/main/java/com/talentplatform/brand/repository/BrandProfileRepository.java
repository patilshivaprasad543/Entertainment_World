package com.talentplatform.brand.repository;
import com.talentplatform.brand.model.BrandProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface BrandProfileRepository extends JpaRepository<BrandProfile, Long> {
    Optional<BrandProfile> findByAccountEmailIgnoreCase(String accountEmail);
    List<BrandProfile> findByAccountEmailIsNull();
}