package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_vendor_availability", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cat_vendor_date", columnNames = {"vendor_account_id", "availableDate"})
})
public class CategoryVendorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendor_account_id", nullable = false)
    private UserAccount vendor;

    @Column(nullable = false)
    private LocalDate availableDate;

    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE"; // AVAILABLE, BOOKED, BLOCKED

    @Column(length = 255)
    private String note;

    private LocalDateTime updatedAt;

    public CategoryVendorAvailability() {}

    public Long getId() { return id; }
    public UserAccount getVendor() { return vendor; }
    public void setVendor(UserAccount vendor) { this.vendor = vendor; }
    public LocalDate getAvailableDate() { return availableDate; }
    public void setAvailableDate(LocalDate availableDate) { this.availableDate = availableDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
