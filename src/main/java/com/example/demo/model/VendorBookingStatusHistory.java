package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_booking_status_history")
public class VendorBookingStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private VendorBooking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24, columnDefinition = "VARCHAR(24)")
    private BookingStatus status;

    @Column(length = 500)
    private String note;

    @Column(length = 40)
    private String changedByRole;

    private LocalDateTime changedAt;

    public VendorBookingStatusHistory() {
    }

    public VendorBookingStatusHistory(VendorBooking booking, BookingStatus status, String note, String changedByRole) {
        this.booking = booking;
        this.status = status;
        this.note = note;
        this.changedByRole = changedByRole;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public VendorBooking getBooking() {
        return booking;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public String getChangedByRole() {
        return changedByRole;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
