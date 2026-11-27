package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_schedules")
public class EventScheduleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organizer_account_id", nullable = false)
    private UserAccount organizer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false)
    private LocalDate scheduleDate;

    @Column(nullable = false, length = 20)
    private String startTime;

    @Column(nullable = false, length = 20)
    private String endTime;

    private Long assignedProfessionalId;

    @Column(length = 120)
    private String assignedProfessionalName;

    @Column(length = 500)
    private String description;

    @Column(length = 24)
    private String status = "PLANNED"; // PLANNED, IN_PROGRESS, COMPLETED, CANCELLED

    private Integer sortOrder = 0;

    private LocalDateTime createdAt;

    public EventScheduleItem() {}

    public Long getId() { return id; }
    public UserAccount getOrganizer() { return organizer; }
    public void setOrganizer(UserAccount organizer) { this.organizer = organizer; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Long getAssignedProfessionalId() { return assignedProfessionalId; }
    public void setAssignedProfessionalId(Long assignedProfessionalId) { this.assignedProfessionalId = assignedProfessionalId; }
    public String getAssignedProfessionalName() { return assignedProfessionalName; }
    public void setAssignedProfessionalName(String assignedProfessionalName) { this.assignedProfessionalName = assignedProfessionalName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
