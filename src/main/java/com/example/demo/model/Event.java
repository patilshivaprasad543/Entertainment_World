package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizer_events", indexes = {
        @Index(name = "idx_org_events_org", columnList = "organizer_account_id")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organizer_account_id", nullable = false)
    private UserAccount organizer;

    @Column(nullable = false, length = 150)
    private String eventName;

    @Column(nullable = false, length = 80)
    private String eventType;

    @Column(length = 2000)
    private String description;

    @Column(length = 255)
    private String imageFileName;

    @Column(length = 80)
    private String country = "India";

    @Column(length = 80)
    private String state;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(length = 120)
    private String area;

    @Column(length = 500)
    private String address;

    @Column(length = 500)
    private String mapLink;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(length = 20)
    private String startTime;

    @Column(length = 20)
    private String endTime;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer expectedGuests = 100;

    @Column(length = 1500)
    private String requirements;

    @Column(nullable = false, length = 20)
    private String status = "DRAFT"; // DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Event() {}

    public Long getId() { return id; }
    public UserAccount getOrganizer() { return organizer; }
    public void setOrganizer(UserAccount organizer) { this.organizer = organizer; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageFileName() { return imageFileName; }
    public void setImageFileName(String imageFileName) { this.imageFileName = imageFileName; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getMapLink() { return mapLink; }
    public void setMapLink(String mapLink) { this.mapLink = mapLink; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public Integer getExpectedGuests() { return expectedGuests; }
    public void setExpectedGuests(Integer expectedGuests) { this.expectedGuests = expectedGuests; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
