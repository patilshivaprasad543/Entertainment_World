package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_bookings")
public class VendorBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String bookingCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendor_account_id", nullable = false)
    private UserAccount vendor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_account_id", nullable = false)
    private UserAccount requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, columnDefinition = "VARCHAR(32)")
    private UserRole requestingModule;

    @Column(nullable = false, length = 150)
    private String requestingOrganization;

    @Column(nullable = false, length = 120)
    private String contactPerson;

    @Column(nullable = false, length = 150)
    private String contactEmail;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private VendorService service;

    @Column(nullable = false)
    private Integer requiredQuantity;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 200)
    private String deliveryLocation;

    @Column(length = 1000)
    private String specialRequirements;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEstimatedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24, columnDefinition = "VARCHAR(24)")
    private BookingStatus status = BookingStatus.PENDING;

    @Column(length = 40)
    private String deliveryStatus = "NOT_DISPATCHED";

    @Column(length = 500)
    private String clarificationNote;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public VendorBooking() {
    }

    public Long getId() {
        return id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public UserAccount getVendor() {
        return vendor;
    }

    public void setVendor(UserAccount vendor) {
        this.vendor = vendor;
    }

    public UserAccount getRequester() {
        return requester;
    }

    public void setRequester(UserAccount requester) {
        this.requester = requester;
    }

    public UserRole getRequestingModule() {
        return requestingModule;
    }

    public void setRequestingModule(UserRole requestingModule) {
        this.requestingModule = requestingModule;
    }

    public String getRequestingOrganization() {
        return requestingOrganization;
    }

    public void setRequestingOrganization(String requestingOrganization) {
        this.requestingOrganization = requestingOrganization;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public VendorService getService() {
        return service;
    }

    public void setService(VendorService service) {
        this.service = service;
    }

    public Integer getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(Integer requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getSpecialRequirements() {
        return specialRequirements;
    }

    public void setSpecialRequirements(String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }

    public BigDecimal getTotalEstimatedPrice() {
        return totalEstimatedPrice;
    }

    public void setTotalEstimatedPrice(BigDecimal totalEstimatedPrice) {
        this.totalEstimatedPrice = totalEstimatedPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getClarificationNote() {
        return clarificationNote;
    }

    public void setClarificationNote(String clarificationNote) {
        this.clarificationNote = clarificationNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
