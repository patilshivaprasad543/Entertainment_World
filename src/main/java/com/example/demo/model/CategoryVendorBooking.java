package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_vendor_bookings")
public class CategoryVendorBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String bookingCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendor_account_id", nullable = false)
    private UserAccount vendor;

    @ManyToOne
    @JoinColumn(name = "service_item_id")
    private CategoryVendorServiceItem serviceItem;

    @Column(nullable = false, length = 80)
    private String vendorCategory;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false, length = 150)
    private String customerEmail;

    @Column(length = 30)
    private String customerPhone;

    @Column(length = 150)
    private String eventName;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(length = 1000)
    private String requirements;

    @Column(precision = 12, scale = 2)
    private BigDecimal quotedAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal advancePaid = BigDecimal.ZERO;

    @Column(length = 150)
    private String projectLocation;

    private LocalDate endDate;

    @Column(nullable = false, length = 24)
    private String status = "ENQUIRY"; // ENQUIRY, VIEWED, IN_DISCUSSION, QUOTATION_SENT, NEGOTIATION, CONFIRMED, IN_PROGRESS, COMPLETED, REJECTED, CANCELLED, DISPUTED

    @Column(length = 500)
    private String vendorReply;

    @Column(length = 500)
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryVendorBooking() {}

    public Long getId() { return id; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public UserAccount getVendor() { return vendor; }
    public void setVendor(UserAccount vendor) { this.vendor = vendor; }
    public CategoryVendorServiceItem getServiceItem() { return serviceItem; }
    public void setServiceItem(CategoryVendorServiceItem serviceItem) { this.serviceItem = serviceItem; }
    public String getVendorCategory() { return vendorCategory; }
    public void setVendorCategory(String vendorCategory) { this.vendorCategory = vendorCategory; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public BigDecimal getQuotedAmount() { return quotedAmount; }
    public void setQuotedAmount(BigDecimal quotedAmount) { this.quotedAmount = quotedAmount; }
    public BigDecimal getAdvancePaid() { return advancePaid; }
    public void setAdvancePaid(BigDecimal advancePaid) { this.advancePaid = advancePaid; }
    public String getProjectLocation() { return projectLocation; }
    public void setProjectLocation(String projectLocation) { this.projectLocation = projectLocation; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVendorReply() { return vendorReply; }
    public void setVendorReply(String vendorReply) { this.vendorReply = vendorReply; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
