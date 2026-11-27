package com.example.demo.service;

import com.example.demo.dto.VendorDtos.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.util.SimplePdfGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class VendorPortalService {

    public static final Path LOGO_DIR = Paths.get("uploads", "vendor-logos").toAbsolutePath().normalize();
    public static final Path COVER_DIR = Paths.get("uploads", "vendor-covers").toAbsolutePath().normalize();
    public static final Path SERVICE_IMG_DIR = Paths.get("uploads", "service-images").toAbsolutePath().normalize();

    private static final List<BookingStatus> INVENTORY_RESERVING_STATUSES =
            List.of(BookingStatus.ACCEPTED, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS);

    private static final BigDecimal GST_RATE = new BigDecimal("0.18");

    private final UserAccountRepository userAccountRepository;
    private final VendorProfileRepository profileRepository;
    private final VendorServiceRepository serviceRepository;
    private final VendorBookingRepository bookingRepository;
    private final VendorBookingStatusHistoryRepository historyRepository;
    private final VendorInvoiceRepository invoiceRepository;
    private final VendorPaymentRepository paymentRepository;
    private final VendorNotificationRepository notificationRepository;

    public VendorPortalService(
            UserAccountRepository userAccountRepository,
            VendorProfileRepository profileRepository,
            VendorServiceRepository serviceRepository,
            VendorBookingRepository bookingRepository,
            VendorBookingStatusHistoryRepository historyRepository,
            VendorInvoiceRepository invoiceRepository,
            VendorPaymentRepository paymentRepository,
            VendorNotificationRepository notificationRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.profileRepository = profileRepository;
        this.serviceRepository = serviceRepository;
        this.bookingRepository = bookingRepository;
        this.historyRepository = historyRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
    }

    // ==================== DASHBOARD ====================

    public DashboardStatsDto getDashboardStats(UserAccount vendor) {
        long totalServices = serviceRepository.countByVendor(vendor);
        long activeServices = serviceRepository.countByVendorAndActiveTrue(vendor);
        long totalBookings = bookingRepository.countByVendor(vendor);
        long pending = bookingRepository.countByVendorAndStatus(vendor, BookingStatus.PENDING);
        long accepted = bookingRepository.countByVendorAndStatus(vendor, BookingStatus.ACCEPTED)
                + bookingRepository.countByVendorAndStatus(vendor, BookingStatus.CONFIRMED)
                + bookingRepository.countByVendorAndStatus(vendor, BookingStatus.IN_PROGRESS);
        long completed = bookingRepository.countByVendorAndStatus(vendor, BookingStatus.COMPLETED);

        List<VendorInvoice> invoices = invoiceRepository.findByVendorOrderByIdDesc(vendor);
        BigDecimal totalEarnings = invoices.stream()
                .map(VendorInvoice::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingPayments = invoices.stream()
                .filter(inv -> inv.getPaymentStatus() != PaymentStatus.PAID)
                .map(inv -> inv.getTotalAmount().subtract(inv.getPaidAmount()).max(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardStatsDto(
                totalServices, activeServices, totalBookings, pending, accepted, completed, totalEarnings, pendingPayments
        );
    }

    // ==================== PROFILE ====================

    public ProfileDto getOrCreateProfile(UserAccount vendor) {
        VendorProfile profile = profileRepository.findByUserAccount(vendor)
                .orElseGet(() -> createDefaultProfile(vendor));
        return toProfileDto(profile, vendor);
    }

    @Transactional
    public ProfileDto updateProfile(UserAccount vendor, ProfileUpdateRequest req) {
        if (!Boolean.TRUE.equals(req.acceptedTerms()) || !Boolean.TRUE.equals(req.acceptedPrivacyPolicy())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please accept both Terms & Conditions and Privacy Policy before saving.");
        }
        VendorProfile profile = profileRepository.findByUserAccount(vendor)
                .orElseGet(() -> createDefaultProfile(vendor));

        if (isNotBlank(req.companyName())) profile.setCompanyName(req.companyName().trim());
        if (isNotBlank(req.contactPerson())) profile.setContactPerson(req.contactPerson().trim());
        profile.setDesignation(trimOrNull(req.designation()));
        if (isNotBlank(req.businessEmail())) profile.setBusinessEmail(req.businessEmail().trim());
        profile.setPhone(trimOrNull(req.phone()));
        profile.setAlternatePhone(trimOrNull(req.alternatePhone()));
        profile.setYearsInBusiness(req.yearsInBusiness());
        profile.setVenuesManaged(req.venuesManaged());
        profile.setAddress(trimOrNull(req.address()));
        profile.setCountry(trimOrNull(req.country()));
        profile.setState(trimOrNull(req.state()));
        profile.setCity(trimOrNull(req.city()));
        profile.setPincode(trimOrNull(req.pincode()));
        profile.setWebsiteUrl(trimOrNull(req.websiteUrl()));
        profile.setGstNumber(trimOrNull(req.gstNumber()));
        profile.setBio(trimOrNull(req.bio()));
        profile.setAboutUs(trimOrNull(req.aboutUs()));
        profile.setDescription(trimOrNull(req.description()));
        profile.setAcceptedTerms(true);
        profile.setAcceptedPrivacyPolicy(true);
        profile.setUpdatedAt(LocalDateTime.now());

        return toProfileDto(profileRepository.save(profile), vendor);
    }

    @Transactional
    public ProfileDto uploadLogo(UserAccount vendor, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a logo image file.");
        }
        VendorProfile profile = profileRepository.findByUserAccount(vendor)
                .orElseGet(() -> createDefaultProfile(vendor));
        try {
            String stored = storeFile(file, LOGO_DIR);
            profile.setLogoFileName(stored);
            profile.setUpdatedAt(LocalDateTime.now());
            return toProfileDto(profileRepository.save(profile), vendor);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save logo.");
        }
    }

    @Transactional
    public ProfileDto uploadCoverImage(UserAccount vendor, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a cover image file.");
        }
        VendorProfile profile = profileRepository.findByUserAccount(vendor)
                .orElseGet(() -> createDefaultProfile(vendor));
        try {
            String stored = storeFile(file, COVER_DIR);
            profile.setCoverImageFileName(stored);
            profile.setUpdatedAt(LocalDateTime.now());
            return toProfileDto(profileRepository.save(profile), vendor);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save cover image.");
        }
    }

    // ==================== SERVICES ====================

    public List<ServiceDto> listVendorServices(UserAccount vendor) {
        return serviceRepository.findByVendorOrderByIdDesc(vendor).stream()
                .map(this::toServiceDto)
                .toList();
    }

    @Transactional
    public ServiceDto createService(
            UserAccount vendor,
            String serviceName,
            String category,
            String description,
            BigDecimal pricePerDay,
            BigDecimal pricePerEvent,
            Integer availableQuantity,
            String location,
            MultipartFile imageFile
    ) {
        validateServiceInputs(serviceName, category, pricePerDay, availableQuantity, location);

        VendorService service = new VendorService();
        service.setVendor(vendor);
        service.setServiceName(serviceName.trim());
        service.setCategory(category.trim());
        service.setDescription(trimOrNull(description));
        service.setPricePerDay(pricePerDay);
        service.setPricePerEvent(pricePerEvent);
        service.setAvailableQuantity(availableQuantity);
        service.setLocation(location.trim());
        service.setActive(true);
        service.setCreatedAt(LocalDateTime.now());

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                service.setImageFileName(storeFile(imageFile, SERVICE_IMG_DIR));
            } catch (IOException ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save service image.");
            }
        }
        return toServiceDto(serviceRepository.save(service));
    }

    @Transactional
    public ServiceDto updateService(
            UserAccount vendor,
            Long serviceId,
            String serviceName,
            String category,
            String description,
            BigDecimal pricePerDay,
            BigDecimal pricePerEvent,
            Integer availableQuantity,
            String location,
            MultipartFile imageFile
    ) {
        VendorService service = requireOwnedService(vendor, serviceId);
        validateServiceInputs(serviceName, category, pricePerDay, availableQuantity, location);

        service.setServiceName(serviceName.trim());
        service.setCategory(category.trim());
        service.setDescription(trimOrNull(description));
        service.setPricePerDay(pricePerDay);
        service.setPricePerEvent(pricePerEvent);
        service.setAvailableQuantity(availableQuantity);
        service.setLocation(location.trim());

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                service.setImageFileName(storeFile(imageFile, SERVICE_IMG_DIR));
            } catch (IOException ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save service image.");
            }
        }
        return toServiceDto(serviceRepository.save(service));
    }

    @Transactional
    public ServiceDto toggleServiceActive(UserAccount vendor, Long serviceId) {
        VendorService service = requireOwnedService(vendor, serviceId);
        service.setActive(!service.isActive());
        return toServiceDto(serviceRepository.save(service));
    }

    @Transactional
    public ServiceDto updateAvailability(UserAccount vendor, Long serviceId, AvailabilityUpdateRequest req) {
        VendorService service = requireOwnedService(vendor, serviceId);
        if (req.availableQuantity() != null) {
            if (req.availableQuantity() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity cannot be negative.");
            }
            service.setAvailableQuantity(req.availableQuantity());
        }
        if (req.unavailableDates() != null) {
            service.setUnavailableDates(req.unavailableDates());
        }
        return toServiceDto(serviceRepository.save(service));
    }

    @Transactional
    public void deleteService(UserAccount vendor, Long serviceId) {
        VendorService service = requireOwnedService(vendor, serviceId);
        boolean hasBookings = !bookingRepository.findByServiceAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                service, INVENTORY_RESERVING_STATUSES, LocalDate.of(2099, 12, 31), LocalDate.of(2000, 1, 1)
        ).isEmpty();
        if (hasBookings) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete a service with active or accepted bookings. Deactivate it instead.");
        }
        serviceRepository.delete(service);
    }

    // ==================== MARKETPLACE (CROSS-DASHBOARD) ====================

    public List<ServiceDto> browseApprovedServices(String query, String category, BigDecimal maxPrice, String location) {
        String q = query == null ? "" : query.trim().toLowerCase();
        String cat = category == null ? "" : category.trim().toLowerCase();
        String loc = location == null ? "" : location.trim().toLowerCase();

        return serviceRepository.findByActiveTrueOrderByIdDesc().stream()
                .filter(s -> s.getVendor().getVerificationStatus() == VerificationStatus.APPROVED)
                .filter(s -> !s.getVendor().isSuspended())
                .filter(s -> !s.isFlagged())
                .filter(s -> q.isEmpty() || s.getServiceName().toLowerCase().contains(q)
                        || (s.getDescription() != null && s.getDescription().toLowerCase().contains(q)))
                .filter(s -> cat.isEmpty() || s.getCategory().equalsIgnoreCase(cat))
                .filter(s -> loc.isEmpty() || s.getLocation().toLowerCase().contains(loc))
                .filter(s -> maxPrice == null || s.getPricePerDay().compareTo(maxPrice) <= 0)
                .map(this::toServiceDto)
                .toList();
    }

    public ServiceDto getPublicService(Long id) {
        VendorService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found."));
        if (!service.isActive() || service.getVendor().getVerificationStatus() != VerificationStatus.APPROVED
                || service.getVendor().isSuspended()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service is not currently available.");
        }
        return toServiceDto(service);
    }

    // ==================== BOOKINGS (CROSS-DASHBOARD + VENDOR) ====================

    @Transactional
    public BookingDto createBookingRequest(UserAccount requester, BookingCreateRequest req) {
        if (req.serviceId() == null || req.requiredQuantity() == null || req.requiredQuantity() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid service and quantity (>= 1) are required.");
        }
        if (req.startDate() == null || req.endDate() == null || req.endDate().isBefore(req.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date.");
        }
        if (!isNotBlank(req.deliveryLocation())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery location is required.");
        }

        VendorService service = serviceRepository.findById(req.serviceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor service not found."));

        if (!service.isActive() || service.getVendor().isSuspended()
                || service.getVendor().getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This vendor service is currently unavailable.");
        }

        if (bookingRepository.existsByRequesterAndServiceAndStartDateAndEndDateAndStatus(
                requester, service, req.startDate(), req.endDate(), BookingStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You already have a pending booking request for this service on these dates.");
        }

        ensureInventoryAvailable(service, req.startDate(), req.endDate(), req.requiredQuantity());

        long days = ChronoUnit.DAYS.between(req.startDate(), req.endDate()) + 1;
        BigDecimal estimated = service.getPricePerDay()
                .multiply(BigDecimal.valueOf(days))
                .multiply(BigDecimal.valueOf(req.requiredQuantity()));

        String orgName = isNotBlank(req.requestingOrganization())
                ? req.requestingOrganization().trim()
                : (isNotBlank(requester.getOrganizationName()) ? requester.getOrganizationName() : requester.getFullName());
        String contact = isNotBlank(req.contactPerson()) ? req.contactPerson().trim() : requester.getFullName();

        VendorBooking booking = new VendorBooking();
        booking.setBookingCode("BK-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        booking.setVendor(service.getVendor());
        booking.setRequester(requester);
        booking.setRequestingModule(requester.getRole());
        booking.setRequestingOrganization(orgName);
        booking.setContactPerson(contact);
        booking.setContactEmail(requester.getEmail());
        booking.setService(service);
        booking.setRequiredQuantity(req.requiredQuantity());
        booking.setStartDate(req.startDate());
        booking.setEndDate(req.endDate());
        booking.setDeliveryLocation(req.deliveryLocation().trim());
        booking.setSpecialRequirements(trimOrNull(req.specialRequirements()));
        booking.setTotalEstimatedPrice(estimated);
        booking.setStatus(BookingStatus.PENDING);
        booking.setDeliveryStatus("NOT_DISPATCHED");
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        VendorBooking saved = bookingRepository.save(booking);
        historyRepository.save(new VendorBookingStatusHistory(
                saved, BookingStatus.PENDING, "Booking request submitted by " + requester.getRole().getDisplayName(),
                requester.getRole().name()
        ));

        notifyUser(
                service.getVendor(),
                "New Booking Request (" + saved.getBookingCode() + ")",
                orgName + " requested " + req.requiredQuantity() + "x " + service.getServiceName()
                        + " (" + req.startDate() + " to " + req.endDate() + ").",
                "BOOKING_NEW",
                saved.getId()
        );

        return toBookingDto(saved);
    }

    public List<BookingDto> listVendorBookings(UserAccount vendor) {
        return bookingRepository.findByVendorOrderByIdDesc(vendor).stream()
                .map(this::toBookingDto)
                .toList();
    }

    public List<BookingDto> listRequesterBookings(UserAccount requester) {
        return bookingRepository.findByRequesterOrderByIdDesc(requester).stream()
                .map(this::toBookingDto)
                .toList();
    }

    public BookingDto getVendorBooking(UserAccount vendor, Long bookingId) {
        VendorBooking booking = requireVendorBooking(vendor, bookingId);
        return toBookingDto(booking);
    }

    public BookingDto getRequesterBooking(UserAccount requester, Long bookingId) {
        VendorBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        if (!booking.getRequester().getId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this booking.");
        }
        return toBookingDto(booking);
    }

    @Transactional
    public BookingDto acceptBooking(UserAccount vendor, Long bookingId) {
        VendorBooking booking = requireVendorBooking(vendor, bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING bookings can be accepted.");
        }
        ensureInventoryAvailable(booking.getService(), booking.getStartDate(), booking.getEndDate(), booking.getRequiredQuantity());
        return transitionBooking(booking, BookingStatus.ACCEPTED, null, null, "Accepted by vendor", vendor.getRole());
    }

    @Transactional
    public BookingDto rejectBooking(UserAccount vendor, Long bookingId, String reason) {
        VendorBooking booking = requireVendorBooking(vendor, bookingId);
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reject booking in status " + booking.getStatus());
        }
        String note = isNotBlank(reason) ? reason.trim() : "Rejected by vendor";
        return transitionBooking(booking, BookingStatus.REJECTED, null, null, note, vendor.getRole());
    }

    @Transactional
    public BookingDto updateBookingStatus(UserAccount vendor, Long bookingId, BookingStatusUpdateRequest req) {
        VendorBooking booking = requireVendorBooking(vendor, bookingId);
        BookingStatus nextStatus = booking.getStatus();

        if (isNotBlank(req.status())) {
            try {
                nextStatus = BookingStatus.valueOf(req.status().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking status: " + req.status());
            }
        }

        if (nextStatus == BookingStatus.ACCEPTED || nextStatus == BookingStatus.CONFIRMED) {
            if (booking.getStatus() == BookingStatus.PENDING) {
                ensureInventoryAvailable(booking.getService(), booking.getStartDate(), booking.getEndDate(), booking.getRequiredQuantity());
            }
        }

        String note = isNotBlank(req.note()) ? req.note().trim() : "Updated by vendor";
        return transitionBooking(booking, nextStatus, req.deliveryStatus(), req.clarificationNote(), note, vendor.getRole());
    }

    // ==================== INVOICES & PAYMENTS ====================

    public List<InvoiceDto> listVendorInvoices(UserAccount vendor) {
        return invoiceRepository.findByVendorOrderByIdDesc(vendor).stream()
                .map(this::toInvoiceDto)
                .toList();
    }

    @Transactional
    public InvoiceDto generateInvoice(UserAccount vendor, Long bookingId) {
        VendorBooking booking = requireVendorBooking(vendor, bookingId);
        if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.REJECTED
                || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invoices can only be generated for accepted, confirmed, in-progress, or completed bookings.");
        }

        Optional<VendorInvoice> existing = invoiceRepository.findByBooking(booking);
        if (existing.isPresent()) {
            return toInvoiceDto(existing.get());
        }

        VendorProfile profile = profileRepository.findByUserAccount(vendor).orElseGet(() -> createDefaultProfile(vendor));
        BigDecimal subtotal = booking.getTotalEstimatedPrice();
        BigDecimal tax = subtotal.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);
        BigDecimal unitPrice = subtotal.divide(BigDecimal.valueOf(booking.getRequiredQuantity()), 2, RoundingMode.HALF_UP);

        VendorInvoice invoice = new VendorInvoice();
        invoice.setInvoiceNumber("INV-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        invoice.setBooking(booking);
        invoice.setVendor(vendor);
        invoice.setVendorCompanyName(profile.getCompanyName());
        invoice.setVendorEmail(profile.getBusinessEmail());
        invoice.setCustomerOrganization(booking.getRequestingOrganization());
        invoice.setCustomerContact(booking.getContactPerson());
        invoice.setCustomerEmail(booking.getContactEmail());
        invoice.setServiceDescription(booking.getService().getServiceName() + " (" + booking.getStartDate() + " to " + booking.getEndDate() + ")");
        invoice.setQuantity(booking.getRequiredQuantity());
        invoice.setUnitPrice(unitPrice);
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(tax);
        invoice.setTotalAmount(total);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(14));
        invoice.setPaymentStatus(PaymentStatus.PENDING);

        VendorInvoice saved = invoiceRepository.save(invoice);

        notifyUser(
                booking.getRequester(),
                "Invoice Generated (" + saved.getInvoiceNumber() + ")",
                "Invoice " + saved.getInvoiceNumber() + " for " + booking.getBookingCode() + " (INR " + total + ") is ready.",
                "INVOICE_CREATED",
                saved.getId()
        );
        notifyUser(
                vendor,
                "Invoice Created (" + saved.getInvoiceNumber() + ")",
                "Generated invoice " + saved.getInvoiceNumber() + " for " + booking.getRequestingOrganization() + ".",
                "INVOICE_CREATED",
                saved.getId()
        );

        return toInvoiceDto(saved);
    }

    @Transactional
    public InvoiceDto recordPayment(UserAccount vendor, Long invoiceId, PaymentRecordRequest req) {
        VendorInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found."));
        if (!invoice.getVendor().getId().equals(vendor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this invoice.");
        }

        if (req.amount() != null && req.amount().compareTo(BigDecimal.ZERO) > 0) {
            VendorPayment payment = new VendorPayment();
            payment.setInvoice(invoice);
            payment.setAmount(req.amount());
            payment.setPaymentMethod(isNotBlank(req.paymentMethod()) ? req.paymentMethod().trim() : "MANUAL_RECORD");
            payment.setReferenceNumber(trimOrNull(req.referenceNumber()));
            payment.setNote(trimOrNull(req.note()));
            payment.setRecordedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            invoice.setPaidAmount(invoice.getPaidAmount().add(req.amount()));
        }

        if (isNotBlank(req.paymentStatus())) {
            try {
                invoice.setPaymentStatus(PaymentStatus.valueOf(req.paymentStatus().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment status.");
            }
        } else {
            if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
                invoice.setPaymentStatus(PaymentStatus.PAID);
            } else if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                invoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
            }
        }

        VendorInvoice saved = invoiceRepository.save(invoice);
        notifyUser(
                saved.getBooking().getRequester(),
                "Payment Status Updated (" + saved.getInvoiceNumber() + ")",
                "Payment status for " + saved.getInvoiceNumber() + " is now " + saved.getPaymentStatus().name() + ".",
                "PAYMENT_UPDATED",
                saved.getId()
        );

        return toInvoiceDto(saved);
    }

    public byte[] generateInvoicePdfForVendor(UserAccount vendor, Long invoiceId) {
        VendorInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found."));
        if (!invoice.getVendor().getId().equals(vendor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }
        return SimplePdfGenerator.generateInvoicePdf(invoice);
    }

    public byte[] generateInvoicePdfForRequester(UserAccount requester, Long bookingId) {
        VendorBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        if (!booking.getRequester().getId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }
        VendorInvoice invoice = invoiceRepository.findByBooking(booking)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not yet generated for this booking."));
        return SimplePdfGenerator.generateInvoicePdf(invoice);
    }

    // ==================== NOTIFICATIONS ====================

    public List<NotificationDto> listNotifications(UserAccount user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(n -> new NotificationDto(n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.getReferenceId(), n.isReadFlag(), n.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void markNotificationRead(UserAccount user, Long notificationId) {
        VendorNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }
        notification.setReadFlag(true);
        notificationRepository.save(notification);
    }

    // ==================== ADMIN MONITORING ====================

    public List<Map<String, Object>> listAllVendorsForAdmin() {
        return userAccountRepository.findByRoleInOrderByIdDesc(List.of(UserRole.VENDOR)).stream()
                .map(v -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", v.getId());
                    row.put("fullName", v.getFullName());
                    row.put("email", v.getEmail());
                    row.put("organizationName", v.getOrganizationName());
                    row.put("verificationStatus", v.getVerificationStatus() == null ? "APPROVED" : v.getVerificationStatus().name());
                    row.put("suspended", v.isSuspended());
                    row.put("serviceCount", serviceRepository.countByVendor(v));
                    row.put("bookingCount", bookingRepository.countByVendor(v));
                    return row;
                })
                .toList();
    }

    public Map<String, Object> getAdminVendorStatistics() {
        List<UserAccount> vendors = userAccountRepository.findByRoleInOrderByIdDesc(List.of(UserRole.VENDOR));
        long activeVendors = vendors.stream()
                .filter(v -> v.getVerificationStatus() == VerificationStatus.APPROVED && !v.isSuspended())
                .count();
        long suspendedVendors = vendors.stream().filter(UserAccount::isSuspended).count();
        long totalServices = serviceRepository.count();
        long flaggedServices = serviceRepository.findAll().stream().filter(VendorService::isFlagged).count();
        long totalBookings = bookingRepository.count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalVendors", vendors.size());
        stats.put("activeVendors", activeVendors);
        stats.put("suspendedVendors", suspendedVendors);
        stats.put("totalServices", totalServices);
        stats.put("flaggedServices", flaggedServices);
        stats.put("totalBookings", totalBookings);
        return stats;
    }

    @Transactional
    public Map<String, Object> setVendorSuspended(Long vendorId, boolean suspended) {
        UserAccount vendor = userAccountRepository.findById(vendorId)
                .filter(u -> u.getRole() == UserRole.VENDOR)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found."));
        vendor.setSuspended(suspended);
        userAccountRepository.save(vendor);
        return Map.of("id", vendor.getId(), "suspended", vendor.isSuspended());
    }

    public List<ServiceDto> listAllServicesForAdmin() {
        return serviceRepository.findAll().stream().map(this::toServiceDto).toList();
    }

    @Transactional
    public ServiceDto flagService(Long serviceId, boolean flagged, String reason) {
        VendorService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found."));
        service.setFlagged(flagged);
        service.setFlagReason(flagged ? trimOrNull(reason) : null);
        return toServiceDto(serviceRepository.save(service));
    }

    // ==================== HELPERS ====================

    private BookingDto transitionBooking(
            VendorBooking booking,
            BookingStatus nextStatus,
            String deliveryStatus,
            String clarificationNote,
            String note,
            UserRole changedBy
    ) {
        booking.setStatus(nextStatus);
        if (isNotBlank(deliveryStatus)) {
            booking.setDeliveryStatus(deliveryStatus.trim().toUpperCase());
        }
        if (clarificationNote != null) {
            booking.setClarificationNote(trimOrNull(clarificationNote));
        }
        booking.setUpdatedAt(LocalDateTime.now());
        VendorBooking saved = bookingRepository.save(booking);

        historyRepository.save(new VendorBookingStatusHistory(saved, nextStatus, note, changedBy.name()));

        notifyUser(
                saved.getRequester(),
                "Booking " + saved.getBookingCode() + " -> " + nextStatus.name(),
                "Status: " + nextStatus.name() + " | Delivery: " + saved.getDeliveryStatus()
                        + (isNotBlank(note) ? " (" + note + ")" : ""),
                "BOOKING_UPDATED",
                saved.getId()
        );

        return toBookingDto(saved);
    }

    private void ensureInventoryAvailable(VendorService service, LocalDate startDate, LocalDate endDate, int requestedQty) {
        if (requestedQty > service.getAvailableQuantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Requested quantity (" + requestedQty + ") exceeds available inventory (" + service.getAvailableQuantity() + ").");
        }

        Set<String> blockedDates = service.getUnavailableDates();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            if (blockedDates.contains(cursor.toString())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Service is marked unavailable on " + cursor + ".");
            }
            cursor = cursor.plusDays(1);
        }

        List<VendorBooking> overlapping = bookingRepository
                .findByServiceAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        service, INVENTORY_RESERVING_STATUSES, endDate, startDate
                );
        int reserved = overlapping.stream().mapToInt(VendorBooking::getRequiredQuantity).sum();
        if (reserved + requestedQty > service.getAvailableQuantity()) {
            int remaining = Math.max(0, service.getAvailableQuantity() - reserved);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Insufficient inventory for selected dates. Remaining available quantity: " + remaining + ".");
        }
    }

    private VendorProfile createDefaultProfile(UserAccount vendor) {
        VendorProfile p = new VendorProfile();
        p.setUserAccount(vendor);
        p.setCompanyName(isNotBlank(vendor.getOrganizationName()) ? vendor.getOrganizationName() : vendor.getFullName());
        p.setContactPerson(vendor.getFullName());
        p.setBusinessEmail(vendor.getEmail());
        p.setPhone(vendor.getPhone());
        p.setCity(vendor.getCity());
        p.setUpdatedAt(LocalDateTime.now());
        return profileRepository.save(p);
    }

    private VendorService requireOwnedService(UserAccount vendor, Long serviceId) {
        VendorService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found."));
        if (!service.getVendor().getId().equals(vendor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage your own services.");
        }
        return service;
    }

    private VendorBooking requireVendorBooking(UserAccount vendor, Long bookingId) {
        VendorBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        if (!booking.getVendor().getId().equals(vendor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this booking.");
        }
        return booking;
    }

    private void notifyUser(UserAccount recipient, String title, String message, String type, Long refId) {
        notificationRepository.save(new VendorNotification(recipient, title, message, type, refId));
    }

    private void validateServiceInputs(String name, String category, BigDecimal pricePerDay, Integer qty, String location) {
        if (!isNotBlank(name) || !isNotBlank(category) || !isNotBlank(location)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service name, category, and location are required.");
        }
        if (pricePerDay == null || pricePerDay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price per day must be greater than 0.");
        }
        if (qty == null || qty < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Available quantity must be at least 1.");
        }
    }

    private ProfileDto toProfileDto(VendorProfile p, UserAccount vendor) {
        String logoUrl = isNotBlank(p.getLogoFileName()) ? "/api/vendors/logo/" + p.getLogoFileName() : null;
        String coverUrl = isNotBlank(p.getCoverImageFileName()) ? "/api/vendors/cover/" + p.getCoverImageFileName() : null;
        String docUrl = isNotBlank(vendor.getDocumentFileName()) ? "/api/admin/verifications/" + vendor.getId() + "/document" : null;
        return new ProfileDto(
                vendor.getId(),
                p.getCompanyName(),
                p.getContactPerson(),
                p.getDesignation(),
                p.getBusinessEmail(),
                p.getPhone(),
                p.getAlternatePhone(),
                p.getYearsInBusiness(),
                p.getVenuesManaged(),
                p.getAddress(),
                p.getCountry(),
                p.getState(),
                p.getCity(),
                p.getPincode(),
                p.getWebsiteUrl(),
                p.getGstNumber(),
                p.getBio(),
                p.getAboutUs(),
                p.getDescription(),
                logoUrl,
                coverUrl,
                p.isAcceptedTerms(),
                p.isAcceptedPrivacyPolicy(),
                vendor.getVerificationStatus() == null ? "APPROVED" : vendor.getVerificationStatus().name(),
                vendor.getDocumentType(),
                vendor.getDocumentNumber(),
                docUrl,
                vendor.isSuspended()
        );
    }

    private ServiceDto toServiceDto(VendorService s) {
        String company = profileRepository.findByUserAccount(s.getVendor())
                .map(VendorProfile::getCompanyName)
                .orElse(isNotBlank(s.getVendor().getOrganizationName()) ? s.getVendor().getOrganizationName() : s.getVendor().getFullName());
        String city = profileRepository.findByUserAccount(s.getVendor())
                .map(VendorProfile::getCity)
                .orElse(s.getLocation());
        String imgUrl = isNotBlank(s.getImageFileName()) ? "/api/vendors/services/image/" + s.getImageFileName() : null;

        return new ServiceDto(
                s.getId(),
                s.getVendor().getId(),
                company,
                city,
                s.getServiceName(),
                s.getCategory(),
                s.getDescription(),
                s.getPricePerDay(),
                s.getPricePerEvent(),
                s.getAvailableQuantity(),
                s.getLocation(),
                imgUrl,
                s.isActive(),
                s.isFlagged(),
                s.getFlagReason(),
                s.getUnavailableDates()
        );
    }

    private BookingDto toBookingDto(VendorBooking b) {
        String vendorCompany = profileRepository.findByUserAccount(b.getVendor())
                .map(VendorProfile::getCompanyName)
                .orElse(isNotBlank(b.getVendor().getOrganizationName()) ? b.getVendor().getOrganizationName() : b.getVendor().getFullName());

        List<StatusHistoryDto> history = historyRepository.findByBookingOrderByChangedAtAsc(b).stream()
                .map(h -> new StatusHistoryDto(h.getStatus().name(), h.getNote(), h.getChangedByRole(), h.getChangedAt()))
                .toList();

        InvoiceDto invoice = invoiceRepository.findByBooking(b).map(this::toInvoiceDto).orElse(null);

        return new BookingDto(
                b.getId(),
                b.getBookingCode(),
                b.getVendor().getId(),
                vendorCompany,
                b.getRequestingModule().name(),
                b.getRequestingOrganization(),
                b.getContactPerson(),
                b.getContactEmail(),
                b.getService().getId(),
                b.getService().getServiceName(),
                b.getService().getCategory(),
                b.getRequiredQuantity(),
                b.getStartDate(),
                b.getEndDate(),
                b.getDeliveryLocation(),
                b.getSpecialRequirements(),
                b.getTotalEstimatedPrice(),
                b.getStatus().name(),
                b.getDeliveryStatus(),
                b.getClarificationNote(),
                b.getCreatedAt(),
                b.getUpdatedAt(),
                history,
                invoice
        );
    }

    private InvoiceDto toInvoiceDto(VendorInvoice inv) {
        List<PaymentDto> payments = paymentRepository.findByInvoiceOrderByRecordedAtDesc(inv).stream()
                .map(p -> new PaymentDto(p.getId(), p.getAmount(), p.getPaymentMethod(), p.getReferenceNumber(), p.getNote(), p.getRecordedAt()))
                .toList();

        return new InvoiceDto(
                inv.getId(),
                inv.getInvoiceNumber(),
                inv.getBooking().getId(),
                inv.getBooking().getBookingCode(),
                inv.getVendorCompanyName(),
                inv.getVendorEmail(),
                inv.getCustomerOrganization(),
                inv.getCustomerContact(),
                inv.getCustomerEmail(),
                inv.getServiceDescription(),
                inv.getQuantity(),
                inv.getUnitPrice(),
                inv.getSubtotal(),
                inv.getTaxAmount(),
                inv.getTotalAmount(),
                inv.getPaidAmount(),
                inv.getInvoiceDate(),
                inv.getDueDate(),
                inv.getPaymentStatus().name(),
                "/api/vendors/invoices/" + inv.getId() + "/pdf",
                payments
        );
    }

    private static String storeFile(MultipartFile file, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        String original = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String safeName = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
        Files.copy(file.getInputStream(), targetDir.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);
        return safeName;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String trimOrNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
