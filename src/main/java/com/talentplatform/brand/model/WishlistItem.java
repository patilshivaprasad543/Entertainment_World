package com.talentplatform.brand.model;

import jakarta.persistence.*;

@Entity @Table(name = "wishlist_items", uniqueConstraints = @UniqueConstraint(columnNames = {"brand_id", "talent_id", "folder_name"}))
public class WishlistItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "brand_id") private Long brandId;
    @Column(name = "talent_id") private Long talentId;
    @Column(name = "folder_name") private String folderName = "My shortlist";
    public Long getId() { return id; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public Long getTalentId() { return talentId; }
    public void setTalentId(Long talentId) { this.talentId = talentId; }
    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }
}
