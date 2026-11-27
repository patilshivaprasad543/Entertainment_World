package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_team_members")
public class EventTeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organizer_account_id", nullable = false)
    private UserAccount organizer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 20)
    private String memberType; // PROFESSIONAL, VENUE

    private Long referenceId;

    @Column(nullable = false, length = 150)
    private String memberName;

    @Column(nullable = false, length = 80)
    private String assignedRole;

    @Column(precision = 12, scale = 2)
    private BigDecimal charges = BigDecimal.ZERO;

    @Column(length = 30)
    private String bookingStatus = "PLANNED";

    private LocalDateTime createdAt;

    public EventTeamMember() {}

    public Long getId() { return id; }
    public UserAccount getOrganizer() { return organizer; }
    public void setOrganizer(UserAccount organizer) { this.organizer = organizer; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public String getMemberType() { return memberType; }
    public void setMemberType(String memberType) { this.memberType = memberType; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getAssignedRole() { return assignedRole; }
    public void setAssignedRole(String assignedRole) { this.assignedRole = assignedRole; }
    public BigDecimal getCharges() { return charges; }
    public void setCharges(BigDecimal charges) { this.charges = charges; }
    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
