package com.example.demo.repository;

import com.example.demo.model.SavedWishlistItem;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedWishlistItemRepository extends JpaRepository<SavedWishlistItem, Long> {

    List<SavedWishlistItem> findByUserOrderByCreatedAtDesc(UserAccount user);

    boolean existsByUserAndItemTypeAndTargetId(UserAccount user, String itemType, Long targetId);
}
