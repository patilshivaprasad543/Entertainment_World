package com.talentplatform.brand.repository;
import com.talentplatform.brand.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> { List<WishlistItem> findByBrandId(Long brandId); boolean existsByBrandIdAndTalentIdAndFolderName(Long brandId, Long talentId, String folderName); }
