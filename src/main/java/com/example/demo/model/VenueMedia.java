package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "venue_media", indexes = {
        @Index(name = "idx_venue_media_venue", columnList = "venue_id")
})
public class VenueMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendor_account_id", nullable = false)
    private UserAccount vendor;

    @Column(nullable = false, length = 20)
    private String mediaType; // PHOTO, VIDEO, TOUR_360

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 255)
    private String originalName;

    @Column(length = 100)
    private String contentType;

    private Long fileSize;

    @Column(length = 150)
    private String title;

    @Column(nullable = false)
    private boolean cover = false;

    private LocalDateTime createdAt;

    public VenueMedia() {
    }

    public Long getId() { return id; }
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
    public UserAccount getVendor() { return vendor; }
    public void setVendor(UserAccount vendor) { this.vendor = vendor; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public boolean isCover() { return cover; }
    public void setCover(boolean cover) { this.cover = cover; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
