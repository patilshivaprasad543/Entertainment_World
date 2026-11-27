package in.sp.main.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks Portfolio Views and Contact Views (Free / Paid) for Transaction History.
 */
@Entity
@Table(name = "contact_portfolio_history")
public class ContactPortfolioHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long artistId;

    @Column(nullable = false, length = 30)
    private String viewType; // CONTACT_VIEW or PORTFOLIO_VIEW

    @Column(nullable = false, length = 20)
    private String tierType; // PAID or FREE

    @Column(nullable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }

    public String getViewType() { return viewType; }
    public void setViewType(String viewType) { this.viewType = viewType; }

    public String getTierType() { return tierType; }
    public void setTierType(String tierType) { this.tierType = tierType; }

    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}
