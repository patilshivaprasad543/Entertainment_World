package in.sp.main.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Talent engagement contract managed by the Casting Company.
 */
@Entity
@Table(name = "artist_contracts")
public class ArtistContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long artistId;

    @Column(length = 120)
    private String roleName;

    @Column(nullable = false, length = 180)
    private String contractTitle;

    @Column(precision = 12, scale = 2)
    private BigDecimal compensationAmount;

    @Column(length = 160)
    private String paymentTerms = "50% advance, 50% on wrap";

    @Column(nullable = false, length = 30)
    private String contractStatus = "DRAFT"; // DRAFT, SENT, SIGNED, ACTIVE, COMPLETED, TERMINATED

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 500)
    private String documentPath;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getContractTitle() { return contractTitle; }
    public void setContractTitle(String contractTitle) { this.contractTitle = contractTitle; }

    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal compensationAmount) { this.compensationAmount = compensationAmount; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public String getContractStatus() { return contractStatus; }
    public void setContractStatus(String contractStatus) { this.contractStatus = contractStatus; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
