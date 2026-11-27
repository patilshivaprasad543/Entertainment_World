package in.sp.main.entity;

import jakarta.persistence.*;

/**
 * Custom folder in "My Artists" for categorizing shortlisted talent.
 */
@Entity
@Table(name = "artist_folders")
public class ArtistFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 120)
    private String folderName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }
}
