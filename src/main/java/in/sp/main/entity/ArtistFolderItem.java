package in.sp.main.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Mapping between a CastingCompany's shortlist folder and a saved ArtistProfile.
 */
@Entity
@Table(name = "artist_folder_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"companyId", "folderId", "artistId"}))
public class ArtistFolderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long folderId;

    @Column(nullable = false)
    private Long artistId;

    @Column(nullable = false)
    private LocalDateTime savedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }

    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}
