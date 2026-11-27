package in.sp.main.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Casting payment or production expense entry tracked per project and artist.
 */
@Entity
@Table(name = "production_expenses")
public class ProductionExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    private Long projectId;

    private Long artistId;

    @Column(nullable = false, length = 60)
    private String category = "ARTIST_FEE"; // ARTIST_FEE, AUDITION_VENUE, TRAVEL_FOOD, PROMOTION, CREW_MISC

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountInr;

    @Column(nullable = false, length = 30)
    private String paymentStatus = "PENDING"; // PENDING, PAID, REIMBURSED

    @Column(length = 60)
    private String paymentMethod = "UPI";

    private LocalDate expenseDate = LocalDate.now();

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

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getAmountInr() { return amountInr; }
    public void setAmountInr(BigDecimal amountInr) { this.amountInr = amountInr; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
