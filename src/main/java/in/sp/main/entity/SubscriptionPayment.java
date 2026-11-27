package in.sp.main.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payment & pass purchase ledger for Casting Company subscriptions.
 */
@Entity
@Table(name = "subscription_payments")
public class SubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 60)
    private String passCode;

    @Column(nullable = false, length = 120)
    private String passName;

    @Column(nullable = false)
    private int durationMonths;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmountInr;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal gstAmountInr;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPaidInr;

    @Column(nullable = false, length = 50)
    private String paymentMethod;

    @Column(nullable = false, unique = true, length = 80)
    private String transactionRef;

    @Column(nullable = false, length = 30)
    private String paymentStatus = "PAID";

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Column(nullable = false)
    private LocalDateTime paidAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getPassCode() { return passCode; }
    public void setPassCode(String passCode) { this.passCode = passCode; }

    public String getPassName() { return passName; }
    public void setPassName(String passName) { this.passName = passName; }

    public int getDurationMonths() { return durationMonths; }
    public void setDurationMonths(int durationMonths) { this.durationMonths = durationMonths; }

    public BigDecimal getBaseAmountInr() { return baseAmountInr; }
    public void setBaseAmountInr(BigDecimal baseAmountInr) { this.baseAmountInr = baseAmountInr; }

    public BigDecimal getGstAmountInr() { return gstAmountInr; }
    public void setGstAmountInr(BigDecimal gstAmountInr) { this.gstAmountInr = gstAmountInr; }

    public BigDecimal getTotalPaidInr() { return totalPaidInr; }
    public void setTotalPaidInr(BigDecimal totalPaidInr) { this.totalPaidInr = totalPaidInr; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
