package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class VendorDtos {

    private VendorDtos() {
    }

    public record DashboardStatsDto(
            long totalServices,
            long activeServices,
            long totalBookingRequests,
            long pendingRequests,
            long acceptedBookings,
            long completedBookings,
            BigDecimal totalEarnings,
            BigDecimal pendingPayments
    ) {
    }

    public record ProfileDto(
            Long userId,
            String companyName,
            String contactPerson,
            String designation,
            String businessEmail,
            String phone,
            String alternatePhone,
            Integer yearsInBusiness,
            Integer venuesManaged,
            String address,
            String country,
            String state,
            String city,
            String pincode,
            String websiteUrl,
            String gstNumber,
            String bio,
            String aboutUs,
            String description,
            String logoUrl,
            String coverImageUrl,
            boolean acceptedTerms,
            boolean acceptedPrivacyPolicy,
            String verificationStatus,
            String documentType,
            String documentNumber,
            String documentUrl,
            boolean suspended
    ) {
    }

    public record ProfileUpdateRequest(
            String companyName,
            String contactPerson,
            String designation,
            String businessEmail,
            String phone,
            String alternatePhone,
            Integer yearsInBusiness,
            Integer venuesManaged,
            String address,
            String country,
            String state,
            String city,
            String pincode,
            String websiteUrl,
            String gstNumber,
            String bio,
            String aboutUs,
            String description,
            Boolean acceptedTerms,
            Boolean acceptedPrivacyPolicy
    ) {
    }

    public record ServiceDto(
            Long id,
            Long vendorId,
            String vendorCompanyName,
            String vendorCity,
            String serviceName,
            String category,
            String description,
            BigDecimal pricePerDay,
            BigDecimal pricePerEvent,
            Integer availableQuantity,
            String location,
            String imageUrl,
            boolean active,
            boolean flagged,
            String flagReason,
            Set<String> unavailableDates
    ) {
    }

    public record AvailabilityUpdateRequest(
            Integer availableQuantity,
            Set<String> unavailableDates
    ) {
    }

    public record BookingCreateRequest(
            Long serviceId,
            Integer requiredQuantity,
            LocalDate startDate,
            LocalDate endDate,
            String deliveryLocation,
            String specialRequirements,
            String requestingOrganization,
            String contactPerson
    ) {
    }

    public record BookingStatusUpdateRequest(
            String status,
            String deliveryStatus,
            String clarificationNote,
            String note
    ) {
    }

    public record StatusHistoryDto(
            String status,
            String note,
            String changedByRole,
            LocalDateTime changedAt
    ) {
    }

    public record BookingDto(
            Long id,
            String bookingCode,
            Long vendorId,
            String vendorCompanyName,
            String requestingModule,
            String requestingOrganization,
            String contactPerson,
            String contactEmail,
            Long serviceId,
            String serviceName,
            String serviceCategory,
            Integer requiredQuantity,
            LocalDate startDate,
            LocalDate endDate,
            String deliveryLocation,
            String specialRequirements,
            BigDecimal totalEstimatedPrice,
            String status,
            String deliveryStatus,
            String clarificationNote,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<StatusHistoryDto> statusHistory,
            InvoiceDto invoice
    ) {
    }

    public record PaymentDto(
            Long id,
            BigDecimal amount,
            String paymentMethod,
            String referenceNumber,
            String note,
            LocalDateTime recordedAt
    ) {
    }

    public record PaymentRecordRequest(
            BigDecimal amount,
            String paymentMethod,
            String referenceNumber,
            String note,
            String paymentStatus
    ) {
    }

    public record InvoiceDto(
            Long id,
            String invoiceNumber,
            Long bookingId,
            String bookingCode,
            String vendorCompanyName,
            String vendorEmail,
            String customerOrganization,
            String customerContact,
            String customerEmail,
            String serviceDescription,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            LocalDate invoiceDate,
            LocalDate dueDate,
            String paymentStatus,
            String pdfUrl,
            List<PaymentDto> payments
    ) {
    }

    public record NotificationDto(
            Long id,
            String title,
            String message,
            String type,
            Long referenceId,
            boolean readFlag,
            LocalDateTime createdAt
    ) {
    }
}
