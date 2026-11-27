package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "venue_enquiries", indexes = {
        @Index(name = "idx_venue_enq_vendor", columnList = "vendor_account_id"),
        @Index(name = "idx_venue_enq_venue", columnList = "venue_id")
})
public class VenueEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String enquiryCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendor_account_id", nullable = false)
    private UserAccount vendor;

    @ManyToOne
    @JoinColumn(name = "requester_account_id")
    private UserAccount requester;

    @Column(length = 40)
    private String requestingModule;

    private Long eventId;

    @Column(length = 150)
    private String eventName;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false, length = 150)
    private String customerEmail;

    @Column(length = 30)
    private String customerMobile;

    @Column(length = 100)
    private String eventType;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(length = 50)
    private String eventTime;

    private Integer guestCount;

    @Column(precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(length = 1000)
    private String message;

    @Column(nullable = false, length = 24)
    private String status = "NEW"; // NEW, UNDER_REVIEW, QUOTATION_SENT, ACCEPTED, REJECTED, EXPIRED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VenueEnquiry() {
    }

    public Long getId() { return id; }
    public String getEnquiryCode() { return enquiryCode; }
    public void setEnquiryCode(String enquiryCode) { this.enquiryCode = enquiryCode; }
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
    public UserAccount getVendor() { return vendor; }
    public void setVendor(UserAccount vendor) { this.vendor = vendor; }
    public UserAccount getRequester() { return requester; }
    public void setRequester(UserAccount requester) { this.requester = requester; }
    public String getRequestingModule() { return requestingModule; }
    public void setRequestingModule(String requestingModule) { this.requestingModule = requestingModule; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerMobile() { return customerMobile; }
    public void setCustomerMobile(String customerMobile) { this.customerMobile = customerMobile; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
