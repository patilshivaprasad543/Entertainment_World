package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizer_event_posts")
public class EventPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organizer_account_id", nullable = false)
    private UserAccount organizer;

    @Column(length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 30)
    private String postType = "ANNOUNCEMENT"; // ANNOUNCEMENT, UPDATE, PHOTO, VIDEO

    @Column(length = 255)
    private String mediaFileName;

    private int likesCount = 0;

    @Column(length = 2000)
    private String commentsLog;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EventPost() {}

    public Long getId() { return id; }
    public UserAccount getOrganizer() { return organizer; }
    public void setOrganizer(UserAccount organizer) { this.organizer = organizer; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }
    public String getMediaFileName() { return mediaFileName; }
    public void setMediaFileName(String mediaFileName) { this.mediaFileName = mediaFileName; }
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public String getCommentsLog() { return commentsLog; }
    public void setCommentsLog(String commentsLog) { this.commentsLog = commentsLog; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
