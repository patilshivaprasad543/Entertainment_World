package in.sp.main.repository;

import in.sp.main.entity.ArtistProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArtistProfileRepository extends JpaRepository<ArtistProfile, Long> {
    List<ArtistProfile> findByPlatinumTierTrueOrFeaturedTierTrueOrderByViewsCountDesc();
    List<ArtistProfile> findByRisingStarTrue();
    Optional<ArtistProfile> findByEmailIgnoreCase(String email);
}
