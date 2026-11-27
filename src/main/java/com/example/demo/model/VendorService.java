package com.example.demo.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vendor_services")
public class VendorService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendor_account_id", nullable = false)
    private UserAccount vendor;

    @Column(nullable = false, length = 150)
    private String serviceName;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerDay;

    @Column(precision = 12, scale = 2)
    private BigDecimal pricePerEvent;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false, length = 120)
    private String location;

    @Column(length = 255)
    private String imageFileName;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean flagged = false;

    @Column(length = 255)
    private String flagReason;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vendor_service_unavailable_dates", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "unavailable_date", length = 20)
    private Set<String> unavailableDates = new HashSet<>();

    private LocalDateTime createdAt;

    public VendorService() {
    }

    public Long getId() {
        return id;
    }

    public UserAccount getVendor() {
        return vendor;
    }

    public void setVendor(UserAccount vendor) {
        this.vendor = vendor;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public BigDecimal getPricePerEvent() {
        return pricePerEvent;
    }

    public void setPricePerEvent(BigDecimal pricePerEvent) {
        this.pricePerEvent = pricePerEvent;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public String getFlagReason() {
        return flagReason;
    }

    public void setFlagReason(String flagReason) {
        this.flagReason = flagReason;
    }

    public Set<String> getUnavailableDates() {
        return unavailableDates;
    }

    public void setUnavailableDates(Set<String> unavailableDates) {
        this.unavailableDates = unavailableDates == null ? new HashSet<>() : new HashSet<>(unavailableDates);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
