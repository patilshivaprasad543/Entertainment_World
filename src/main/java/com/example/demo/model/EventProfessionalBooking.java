package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_professional_bookings", indexes = {
        @Index(name = "idx_epb_org", columnList = "organizer_account_id"),
        @Index(name = "idx_epb_prof", columnList = "professional_account_id")
})
public class EventProfessionalBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String bookingCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organizer_account_id", nullable = false)
    private UserAccount organizer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professional_account_id", nullable = false)
    private UserAccount professional;

    @Column(nullable = false, length = 80)
    private String requiredCategory;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(length = 20)
    private String startTime;

    @Column(length = 20)
    private String endTime;

    @Column(length = 200)
    private String eventLocation;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal offeredFee = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal counterOfferFee;

    @Column(length = 1000)
    private String requirements;

    @Column(length = 1000)
    private String message;

    @Column(nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, ACCEPTED, REJECTED, COUNTEROFFER, RESCHEDULE_REQUESTED, CANCELLATION_REQUESTED, CONFIRMED, CANCELLED, COMPLETED

    @Column(length = 2000)
    private String historyLog;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EventProfessionalBooking() {}

    public Long getId() { return id; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public UserAccount getOrganizer() { return organizer; }
    public void setOrganizer(UserAccount organizer) { this.organizer = organizer; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public UserAccount getProfessional() { return professional; }
    public void setProfessional(UserAccount professional) { this.professional = professional; }
    public String getRequiredCategory() { return requiredCategory; }
    public void setRequiredCategory(String requiredCategory) { this.requiredCategory = requiredCategory; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getEventLocation() { return eventLocation; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }
    public BigDecimal getOfferedFee() { return offeredFee; }
    public void setOfferedFee(BigDecimal offeredFee) { this.offeredFee = offeredFee; }
    public BigDecimal getCounterOfferFee() { return counterOfferFee; }
    public void setCounterOfferFee(BigDecimal counterOfferFee) { this.counterOfferFee = counterOfferFee; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getHistoryLog() { return historyLog; }
    public void setHistoryLog(String historyLog) { this.historyLog = historyLog; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
