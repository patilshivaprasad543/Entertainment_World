package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "venue_bookings", indexes = {
        @Index(name = "idx_venue_book_vendor", columnList = "vendor_account_id"),
        @Index(name = "idx_venue_book_venue_date", columnList = "venue_id,eventDate")
})
public class VenueBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String bookingCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendor_account_id", nullable = false)
    private UserAccount vendor;

    @ManyToOne
    @JoinColumn(name = "requester_account_id")
    private UserAccount requester;

    @ManyToOne
    @JoinColumn(name = "quotation_id")
    private VenueQuotation quotation;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false, length = 150)
    private String customerEmail;

    @Column(length = 30)
    private String customerMobile;

    @Column(length = 100)
    private String eventType;

    private Long eventId;

    @Column(length = 150)
    private String eventName;

    @Column(length = 40)
    private String eventTime;

    @Column(nullable = false)
    private LocalDate eventDate;

    private Integer guestCount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal advancePaid = BigDecimal.ZERO;

    @Column(length = 24)
    private String paymentStatus = "PENDING"; // PENDING, PARTIAL, PAID

    @Column(nullable = false, length = 24)
    private String status = "PENDING"; // PENDING, CONFIRMED, REJECTED, CANCELLED, COMPLETED

    @Column(length = 1000)
    private String notes;

    @Column(length = 1500)
    private String historyLog;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VenueBooking() {
    }

    public Long getId() { return id; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
    public UserAccount getVendor() { return vendor; }
    public void setVendor(UserAccount vendor) { this.vendor = vendor; }
    public UserAccount getRequester() { return requester; }
    public void setRequester(UserAccount requester) { this.requester = requester; }
    public VenueQuotation getQuotation() { return quotation; }
    public void setQuotation(VenueQuotation quotation) { this.quotation = quotation; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerMobile() { return customerMobile; }
    public void setCustomerMobile(String customerMobile) { this.customerMobile = customerMobile; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getAdvancePaid() { return advancePaid; }
    public void setAdvancePaid(BigDecimal advancePaid) { this.advancePaid = advancePaid; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getHistoryLog() { return historyLog; }
    public void setHistoryLog(String historyLog) { this.historyLog = historyLog; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
