package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizer_shortlists")
public class ProfessionalShortlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organizer_account_id", nullable = false)
    private UserAccount organizer;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professional_account_id", nullable = false)
    private UserAccount professional;

    @Column(precision = 12, scale = 2)
    private BigDecimal quotedCharge = BigDecimal.ZERO;

    @Column(length = 500)
    private String notes;

    private boolean preferred = false;

    private LocalDateTime createdAt;

    public ProfessionalShortlist() {}

    public Long getId() { return id; }
    public UserAccount getOrganizer() { return organizer; }
    public void setOrganizer(UserAccount organizer) { this.organizer = organizer; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public UserAccount getProfessional() { return professional; }
    public void setProfessional(UserAccount professional) { this.professional = professional; }
    public BigDecimal getQuotedCharge() { return quotedCharge; }
    public void setQuotedCharge(BigDecimal quotedCharge) { this.quotedCharge = quotedCharge; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isPreferred() { return preferred; }
    public void setPreferred(boolean preferred) { this.preferred = preferred; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
