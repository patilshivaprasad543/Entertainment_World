package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_vendor_documents")
public class CategoryVendorDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendor_account_id", nullable = false)
    private UserAccount vendor;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 40)
    private String docType = "SERVICE_AGREEMENT";

    @Column(nullable = false, length = 255)
    private String fileName;

    private LocalDate expiryDate;

    @Column(length = 24)
    private String status = "ACTIVE";

    private LocalDateTime createdAt;

    public CategoryVendorDocument() {}

    public Long getId() { return id; }
    public UserAccount getVendor() { return vendor; }
    public void setVendor(UserAccount vendor) { this.vendor = vendor; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
