package com.example.demo.service;

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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VenueManagementService {

    public static final Path VENUE_MEDIA_DIR = Paths.get("uploads", "venue-media").toAbsolutePath().normalize();

    private static final Set<String> ALLOWED_PHOTO_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_VIDEO_EXT = Set.of("mp4", "webm", "mov");
    private static final long MAX_PHOTO_BYTES = 10L * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 50L * 1024 * 1024;

    private final VenueRepository venueRepository;
    private final VenueMediaRepository mediaRepository;
    private final VenueAvailabilityRepository availabilityRepository;
    private final VenueEnquiryRepository enquiryRepository;
    private final VenueQuotationRepository quotationRepository;
    private final VenueBookingRepository bookingRepository;
    private final VendorLocationRepository locationRepository;
    private final VendorTeamMemberRepository teamRepository;
    private final VendorProfileRepository profileRepository;
    private final VendorNotificationRepository notificationRepository;
    private final SubscriptionService subscriptionService;
    private final BookingReviewRepository reviewRepository;
    private final EventPaymentRecordRepository paymentRecordRepository;
    private final EventRepository eventRepository;

    public VenueManagementService(
            VenueRepository venueRepository,
            VenueMediaRepository mediaRepository,
            VenueAvailabilityRepository availabilityRepository,
            VenueEnquiryRepository enquiryRepository,
            VenueQuotationRepository quotationRepository,
            VenueBookingRepository bookingRepository,
            VendorLocationRepository locationRepository,
            VendorTeamMemberRepository teamRepository,
            VendorProfileRepository profileRepository,
            VendorNotificationRepository notificationRepository,
            SubscriptionService subscriptionService,
            BookingReviewRepository reviewRepository,
            EventPaymentRecordRepository paymentRecordRepository,
            EventRepository eventRepository
    ) {
        this.venueRepository = venueRepository;
        this.mediaRepository = mediaRepository;
        this.availabilityRepository = availabilityRepository;
        this.enquiryRepository = enquiryRepository;
        this.quotationRepository = quotationRepository;
        this.bookingRepository = bookingRepository;
        this.locationRepository = locationRepository;
        this.teamRepository = teamRepository;
        this.profileRepository = profileRepository;
        this.notificationRepository = notificationRepository;
        this.subscriptionService = subscriptionService;
        this.reviewRepository = reviewRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.eventRepository = eventRepository;
    }

    // ==================== SUBSCRIPTION LIMITS ====================

    public record PlanLimits(
            String tier,
            String planName,
            int maxVenues,
            int maxPhotosPerVenue,
            int maxTeamAccounts,
            int maxLocations,
            boolean allow360Tour,
            boolean allowAdvancedBooking,
            boolean allowMultipleLocations,
            String analyticsLevel,
            String status,
            LocalDate startDate,
            LocalDate endDate
    ) {}

    public PlanLimits getVendorPlanLimits(UserAccount vendor) {
        UserSubscription sub = subscriptionService.ensureActiveOrDefaultFree(vendor);
        String tier = sub != null && sub.getPlan() != null ? sub.getPlan().getTier().toUpperCase() : "FREE";
        String planName = sub != null && sub.getPlan() != null ? sub.getPlan().getPlanName() : "Vendors Free";
        String status = sub != null ? sub.getStatus() : "ACTIVE";
        LocalDate start = sub != null ? sub.getStartDate() : LocalDate.now();
        LocalDate end = sub != null ? sub.getEndDate() : LocalDate.now().plusDays(30);

        return switch (tier) {
            case "PRO" -> new PlanLimits(tier, planName, 10, 50, 10, 10, true, true, true, "DETAILED", status, start, end);
            case "PREMIUM" -> new PlanLimits(tier, planName, 3, 20, 3, 3, true, true, true, "DETAILED", status, start, end);
            default -> new PlanLimits("FREE", planName, 1, 5, 1, 0, false, false, false, "BASIC", status, start, end);
        };
    }

    // ==================== DASHBOARD OVERVIEW ====================

    public Map<String, Object> getDashboardOverview(UserAccount vendor) {
        PlanLimits limits = getVendorPlanLimits(vendor);
        List<Venue> venues = venueRepository.findByVendorOrderByIdDesc(vendor);
        long totalVenues = venues.size();
        long activeVenues = venues.stream().filter(Venue::isActive).count();

        List<VenueEnquiry> enquiries = enquiryRepository.findByVendorOrderByIdDesc(vendor);
        long pendingEnquiries = enquiries.stream()
                .filter(e -> "NEW".equals(e.getStatus()) || "UNDER_REVIEW".equals(e.getStatus()))
                .count();

        List<VenueBooking> bookings = bookingRepository.findByVendorOrderByIdDesc(vendor);
        long confirmedBookings = bookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus())).count();
        long pendingBookings = bookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count();

        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                .map(b -> b.getTotalAmount() == null ? BigDecimal.ZERO : b.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Available venues: active venues that are not booked today
        LocalDate today = LocalDate.now();
        long availableToday = venues.stream()
                .filter(Venue::isActive)
                .filter(v -> !bookingRepository.existsByVenueAndEventDateAndStatus(v, today, "CONFIRMED"))
                .count();

        List<Map<String, Object>> recentEnquiries = enquiries.stream().limit(5).map(this::toEnquiryMap).toList();
        List<Map<String, Object>> upcomingBookings = bookings.stream()
                .filter(b -> !b.getEventDate().isBefore(today) && ("CONFIRMED".equals(b.getStatus()) || "PENDING".equals(b.getStatus())))
                .sorted(Comparator.comparing(VenueBooking::getEventDate))
                .limit(5)
                .map(this::toBookingMap)
                .toList();
        List<Map<String, Object>> recentNotifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(vendor).stream()
                .limit(6)
                .map(n -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", n.getId());
                    m.put("title", n.getTitle());
                    m.put("message", n.getMessage());
                    m.put("type", n.getType());
                    m.put("read", n.isReadFlag());
                    m.put("createdAt", n.getCreatedAt());
                    return m;
                }).toList();

        Map<String, Long> bookingStatusCounts = new LinkedHashMap<>();
        bookingStatusCounts.put("PENDING", bookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count());
        bookingStatusCounts.put("CONFIRMED", bookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus())).count());
        bookingStatusCounts.put("COMPLETED", bookings.stream().filter(b -> "COMPLETED".equals(b.getStatus())).count());
        bookingStatusCounts.put("REJECTED", bookings.stream().filter(b -> "REJECTED".equals(b.getStatus())).count());
        bookingStatusCounts.put("CANCELLED", bookings.stream().filter(b -> "CANCELLED".equals(b.getStatus())).count());

        // Monthly revenue (last 6 months)
        List<Map<String, Object>> monthlyRevenue = buildMonthlyRevenue(bookings, 6);
        List<Map<String, Object>> venuePerformance = buildVenuePerformance(venues, enquiries, bookings);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("totalVenues", totalVenues);
        res.put("activeVenues", activeVenues);
        res.put("availableVenues", availableToday);
        res.put("pendingEnquiries", pendingEnquiries);
        res.put("confirmedBookings", confirmedBookings);
        res.put("pendingBookings", pendingBookings);
        res.put("totalRevenue", totalRevenue);
        res.put("currentPlan", limits.planName());
        res.put("planLimits", limits);
        res.put("recentEnquiries", recentEnquiries);
        res.put("upcomingBookings", upcomingBookings);
        res.put("recentActivities", recentNotifications);
        res.put("bookingStatusOverview", bookingStatusCounts);
        res.put("monthlyRevenueChart", monthlyRevenue);
        res.put("venuePerformanceChart", venuePerformance);
        return res;
    }

    // ==================== VENUES CRUD ====================

    public List<Map<String, Object>> listVendorVenues(UserAccount vendor) {
        return venueRepository.findByVendorOrderByIdDesc(vendor).stream()
                .map(v -> toVenueMap(v, true))
                .toList();
    }

    public Map<String, Object> getVendorVenue(UserAccount vendor, Long id) {
        Venue venue = requireOwnedVenue(vendor, id);
        return toVenueMap(venue, true);
    }

    @Transactional
    public Map<String, Object> createVenue(UserAccount vendor, Map<String, Object> body) {
        PlanLimits limits = getVendorPlanLimits(vendor);
        long count = venueRepository.countByVendor(vendor);
        if (count >= limits.maxVenues()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Venue listing limit reached (" + count + "/" + limits.maxVenues() + ") for your " + limits.planName() + ". Please upgrade your subscription plan.");
        }

        Venue v = new Venue();
        v.setVendor(vendor);
        applyVenueFields(v, body);
        v.setActive(true);
        v.setCreatedAt(LocalDateTime.now());
        v.setUpdatedAt(LocalDateTime.now());
        Venue saved = venueRepository.save(v);
        return toVenueMap(saved, true);
    }

    @Transactional
    public Map<String, Object> updateVenue(UserAccount vendor, Long id, Map<String, Object> body) {
        Venue v = requireOwnedVenue(vendor, id);
        applyVenueFields(v, body);
        v.setUpdatedAt(LocalDateTime.now());
        return toVenueMap(venueRepository.save(v), true);
    }

    @Transactional
    public Map<String, Object> toggleVenueStatus(UserAccount vendor, Long id, boolean active) {
        Venue v = requireOwnedVenue(vendor, id);
        v.setActive(active);
        v.setUpdatedAt(LocalDateTime.now());
        return toVenueMap(venueRepository.save(v), true);
    }

    @Transactional
    public void deleteVenue(UserAccount vendor, Long id) {
        Venue v = requireOwnedVenue(vendor, id);
        bookingRepository.deleteByVenue(v);
        quotationRepository.deleteByVenue(v);
        enquiryRepository.deleteByVenue(v);
        availabilityRepository.deleteByVenue(v);
        mediaRepository.deleteByVenue(v);
        venueRepository.delete(v);
    }

    // ==================== PRICING MANAGEMENT ====================

    @Transactional
    public Map<String, Object> updateVenuePricing(UserAccount vendor, Long id, Map<String, Object> body) {
        Venue v = requireOwnedVenue(vendor, id);
        if (body.containsKey("pricePerDay")) v.setPricePerDay(toDecimal(body.get("pricePerDay"), v.getPricePerDay()));
        if (body.containsKey("pricePerHour")) v.setPricePerHour(toDecimal(body.get("pricePerHour"), BigDecimal.ZERO));
        if (body.containsKey("weekdayPrice")) v.setWeekdayPrice(toDecimal(body.get("weekdayPrice"), BigDecimal.ZERO));
        if (body.containsKey("weekendPrice")) v.setWeekendPrice(toDecimal(body.get("weekendPrice"), BigDecimal.ZERO));
        if (body.containsKey("seasonalPrice")) v.setSeasonalPrice(toDecimal(body.get("seasonalPrice"), BigDecimal.ZERO));
        if (body.containsKey("specialEventPrice")) v.setSpecialEventPrice(toDecimal(body.get("specialEventPrice"), BigDecimal.ZERO));
        if (body.containsKey("additionalCharges")) v.setAdditionalCharges(toDecimal(body.get("additionalCharges"), BigDecimal.ZERO));
        if (body.containsKey("decorationCharges")) v.setDecorationCharges(toDecimal(body.get("decorationCharges"), BigDecimal.ZERO));
        if (body.containsKey("cateringCharges")) v.setCateringCharges(toDecimal(body.get("cateringCharges"), BigDecimal.ZERO));
        if (body.containsKey("advanceAmount")) v.setAdvanceAmount(toDecimal(body.get("advanceAmount"), BigDecimal.ZERO));
        v.setUpdatedAt(LocalDateTime.now());
        return toVenueMap(venueRepository.save(v), true);
    }

    // ==================== GALLERY & 360 TOUR MEDIA ====================

    public List<Map<String, Object>> listVenueMedia(UserAccount vendor, Long venueId) {
        Venue venue = requireOwnedVenue(vendor, venueId);
        return mediaRepository.findByVenueOrderByIdDesc(venue).stream()
                .map(this::toMediaMap)
                .toList();
    }

    public List<Map<String, Object>> listAll360Tours(UserAccount vendor) {
        return mediaRepository.findByVendorOrderByIdDesc(vendor).stream()
                .filter(m -> "TOUR_360".equals(m.getMediaType()))
                .map(this::toMediaMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> uploadVenueMedia(UserAccount vendor, Long venueId, String mediaTypeRaw, String title, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file upload is not allowed.");
        }
        Venue venue = requireOwnedVenue(vendor, venueId);
        PlanLimits limits = getVendorPlanLimits(vendor);

        String mediaType = mediaTypeRaw == null ? "PHOTO" : mediaTypeRaw.trim().toUpperCase();
        if (!Set.of("PHOTO", "VIDEO", "TOUR_360").contains(mediaType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid media type. Use PHOTO, VIDEO, or TOUR_360.");
        }

        if ("TOUR_360".equals(mediaType) && !limits.allow360Tour()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "360° Venue Tour requires a Premium (₹999) or Pro (₹2,499) subscription plan.");
        }

        String originalName = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String ext = getExtension(originalName);

        if ("VIDEO".equals(mediaType)) {
            if (!ALLOWED_VIDEO_EXT.contains(ext)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed video types: mp4, webm, mov.");
            }
            if (file.getSize() > MAX_VIDEO_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Video size exceeds 50 MB limit.");
            }
        } else {
            if (!ALLOWED_PHOTO_EXT.contains(ext)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed image types: jpg, jpeg, png, webp, gif.");
            }
            if (file.getSize() > MAX_PHOTO_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image size exceeds 10 MB limit.");
            }
            if ("PHOTO".equals(mediaType)) {
                long photoCount = mediaRepository.countByVenueAndMediaType(venue, "PHOTO");
                if (photoCount >= limits.maxPhotosPerVenue()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Photo limit reached (" + photoCount + "/" + limits.maxPhotosPerVenue() + " per venue) on " + limits.planName() + ". Please upgrade your plan.");
                }
            }
        }

        try {
            String storedName = storeFile(file, VENUE_MEDIA_DIR);
            VenueMedia media = new VenueMedia();
            media.setVenue(venue);
            media.setVendor(vendor);
            media.setMediaType(mediaType);
            media.setFileName(storedName);
            media.setOriginalName(originalName);
            media.setContentType(file.getContentType());
            media.setFileSize(file.getSize());
            media.setTitle(trimOrNull(title) != null ? title.trim() : originalName);
            media.setCreatedAt(LocalDateTime.now());

            // Auto-set first photo as cover if none exists
            if ("PHOTO".equals(mediaType) && (venue.getCoverImageFileName() == null || venue.getCoverImageFileName().isBlank())) {
                media.setCover(true);
                venue.setCoverImageFileName(storedName);
                venueRepository.save(venue);
            }

            return toMediaMap(mediaRepository.save(media));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store uploaded media.");
        }
    }

    @Transactional
    public Map<String, Object> setCoverMedia(UserAccount vendor, Long venueId, Long mediaId) {
        Venue venue = requireOwnedVenue(vendor, venueId);
        VenueMedia media = mediaRepository.findByIdAndVendor(mediaId, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media item not found."));
        if (!media.getVenue().getId().equals(venue.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Media does not belong to this venue.");
        }

        mediaRepository.findByVenueOrderByIdDesc(venue).forEach(m -> {
            if (m.isCover()) {
                m.setCover(false);
                mediaRepository.save(m);
            }
        });
        media.setCover(true);
        venue.setCoverImageFileName(media.getFileName());
        venueRepository.save(venue);
        return toMediaMap(mediaRepository.save(media));
    }

    @Transactional
    public void deleteVenueMedia(UserAccount vendor, Long mediaId) {
        VenueMedia media = mediaRepository.findByIdAndVendor(mediaId, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media item not found."));
        Venue venue = media.getVenue();
        if (media.isCover() && venue.getCoverImageFileName() != null && venue.getCoverImageFileName().equals(media.getFileName())) {
            venue.setCoverImageFileName(null);
            venueRepository.save(venue);
        }
        mediaRepository.delete(media);
    }

    // ==================== AVAILABILITY CALENDAR ====================

    public List<Map<String, Object>> getVenueAvailability(Long venueId, LocalDate start, LocalDate end) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found."));
        LocalDate from = start != null ? start : LocalDate.now().withDayOfMonth(1);
        LocalDate to = end != null ? end : from.plusMonths(2);
        return availabilityRepository.findByVenueAndAvailabilityDateBetweenOrderByAvailabilityDateAsc(venue, from, to).stream()
                .map(this::toAvailabilityMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> setVenueDateStatus(UserAccount vendor, Long venueId, LocalDate date, String statusRaw, String note) {
        Venue venue = requireOwnedVenue(vendor, venueId);
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Availability can only be set from today's date onwards.");
        }
        String status = statusRaw == null ? "BLOCKED" : statusRaw.trim().toUpperCase();
        if (!Set.of("AVAILABLE", "BLOCKED").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manual status must be AVAILABLE or BLOCKED.");
        }
        if (bookingRepository.existsByVenueAndEventDateAndStatus(venue, date, "CONFIRMED")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot override date " + date + ": a confirmed booking already exists.");
        }

        VenueAvailability avail = availabilityRepository.findByVenueAndAvailabilityDate(venue, date)
                .orElseGet(VenueAvailability::new);
        avail.setVenue(venue);
        avail.setAvailabilityDate(date);
        avail.setStatus(status);
        avail.setNote(trimOrNull(note));
        avail.setUpdatedAt(LocalDateTime.now());
        return toAvailabilityMap(availabilityRepository.save(avail));
    }

    // ==================== BOOKING ENQUIRIES ====================

    @Transactional
    public Map<String, Object> submitPublicEnquiry(Long venueId, UserAccount requester, Map<String, Object> body) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found."));
        if (!venue.isActive() || venue.isFlagged()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venue is currently unavailable for enquiries.");
        }
        String customerName = str(body.get("customerName"));
        String customerEmail = str(body.get("customerEmail"));
        LocalDate eventDate = parseDate(body.get("eventDate"));
        if (!isNotBlank(customerName) && requester != null) customerName = requester.getFullName();
        if (!isNotBlank(customerEmail) && requester != null) customerEmail = requester.getEmail();

        if (!isNotBlank(customerName) || !isNotBlank(customerEmail) || eventDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer name, email, and event date are required.");
        }
        if (eventDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event date cannot be in the past.");
        }

        VenueEnquiry enq = new VenueEnquiry();
        enq.setEnquiryCode("ENQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        enq.setVenue(venue);
        enq.setVendor(venue.getVendor());
        enq.setRequester(requester);
        enq.setRequestingModule(requester != null ? requester.getRole().name() : strOrDefault(body.get("requestingModule"), "PUBLIC"));
        enq.setEventId(toLong(body.get("eventId")));
        enq.setEventName(trimOrNull(str(body.get("eventName"))));
        enq.setCustomerName(customerName.trim());
        enq.setCustomerEmail(customerEmail.trim());
        enq.setCustomerMobile(trimOrNull(str(body.get("customerMobile"))));
        enq.setEventType(strOrDefault(body.get("eventType"), "Event"));
        enq.setEventDate(eventDate);
        enq.setEventTime(trimOrNull(str(body.get("eventTime"))));
        enq.setGuestCount(toInt(body.get("guestCount"), 100));
        enq.setBudget(toDecimal(body.get("budget"), venue.getPricePerDay()));
        enq.setMessage(trimOrNull(str(body.get("message"))));
        enq.setStatus("NEW");
        enq.setCreatedAt(LocalDateTime.now());
        enq.setUpdatedAt(LocalDateTime.now());

        VenueEnquiry saved = enquiryRepository.save(enq);

        // Notify vendor
        notificationRepository.save(new VendorNotification(
                venue.getVendor(),
                "New Venue Enquiry (" + saved.getEnquiryCode() + ")",
                saved.getCustomerName() + " enquired for " + venue.getVenueName() + " on " + saved.getEventDate(),
                "ENQUIRY_NEW",
                saved.getId()
        ));
        if (requester != null) {
            notificationRepository.save(new VendorNotification(
                    requester,
                    "Enquiry Sent (" + saved.getEnquiryCode() + ")",
                    "Your enquiry for " + venue.getVenueName() + " on " + saved.getEventDate() + " was received.",
                    "ENQUIRY_SENT",
                    saved.getId()
            ));
        }
        return toEnquiryMap(saved);
    }

    public List<Map<String, Object>> listVendorEnquiries(UserAccount vendor) {
        return enquiryRepository.findByVendorOrderByIdDesc(vendor).stream()
                .map(this::toEnquiryMap)
                .toList();
    }

    public List<Map<String, Object>> listRequesterEnquiries(UserAccount requester) {
        return enquiryRepository.findByRequesterOrderByIdDesc(requester).stream()
                .map(this::toEnquiryMap)
                .toList();
    }

    public List<Map<String, Object>> listOrganizerQuotations(UserAccount organizer) {
        return quotationRepository.findByEnquiryRequesterOrderByIdDesc(organizer).stream()
                .map(this::toQuotationMap)
                .toList();
    }

    public List<Map<String, Object>> listOrganizerBookings(UserAccount organizer) {
        return bookingRepository.findByRequesterOrderByIdDesc(organizer).stream()
                .map(this::toBookingMap)
                .toList();
    }

    public List<Map<String, Object>> listVendorReviews(UserAccount vendor) {
        List<Long> venueIds = venueRepository.findByVendorOrderByIdDesc(vendor).stream().map(Venue::getId).toList();
        if (venueIds.isEmpty()) return List.of();
        return reviewRepository.findByTargetTypeAndTargetIdInOrderByIdDesc("VENUE", venueIds).stream()
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "venueId", r.getTargetId(),
                        "reviewerName", r.getReviewer().getFullName(),
                        "rating", r.getRating(),
                        "title", r.getTitle(),
                        "description", r.getDescription() == null ? "" : r.getDescription(),
                        "createdAt", String.valueOf(r.getCreatedAt())
                ))
                .toList();
    }

    @Transactional
    public Map<String, Object> respondToVenueQuotation(UserAccount organizer, Long quotationId, String actionRaw, BigDecimal counterOffer, String note) {
        VenueQuotation q = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quotation not found."));
        if (q.getEnquiry() == null || q.getEnquiry().getRequester() == null
                || !q.getEnquiry().getRequester().getId().equals(organizer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this quotation.");
        }
        String action = actionRaw == null ? "" : actionRaw.trim().toUpperCase();
        if ("ACCEPT".equals(action) || "ACCEPTED".equals(action)) {
            q.setStatus("ACCEPTED");
            q.setOrganizerNote(trimOrNull(note));
            q.setUpdatedAt(LocalDateTime.now());
            VenueQuotation saved = quotationRepository.save(q);
            VenueBooking pending = createPendingBookingFromQuotation(saved);

            // Also create a payment record for the organizer
            EventPaymentRecord pr = new EventPaymentRecord();
            pr.setOrganizer(organizer);
            if (saved.getEnquiry().getEventId() != null) {
                eventRepository.findById(saved.getEnquiry().getEventId()).ifPresent(pr::setEvent);
            }
            pr.setBookingReference(pending.getBookingCode());
            pr.setPayeeName(saved.getVenue().getVenueName());
            pr.setPayeeType("VENUE");
            pr.setBookingAmount(saved.getTotalAmount());
            pr.setAdvanceAmount(saved.getAdvanceAmount());
            pr.setPaidAmount(BigDecimal.ZERO);
            pr.setRemainingBalance(saved.getTotalAmount());
            pr.setPaymentStatus("PENDING");
            pr.setContractStatus("SIGNED");
            pr.setPaymentMode("MANUAL_RECORD");
            pr.setCreatedAt(LocalDateTime.now());
            pr.setUpdatedAt(LocalDateTime.now());
            paymentRecordRepository.save(pr);

            notificationRepository.save(new VendorNotification(
                    saved.getVendor(),
                    "Quotation Accepted (" + saved.getQuotationCode() + ")",
                    organizer.getFullName() + " accepted your quotation for " + saved.getVenue().getVenueName() + ". Booking " + pending.getBookingCode() + " is ready for confirmation.",
                    "QUOTATION_ACCEPTED",
                    saved.getId()
            ));
            return Map.of("quotation", toQuotationMap(saved), "booking", toBookingMap(pending));
        } else if ("REJECT".equals(action) || "REJECTED".equals(action)) {
            q.setStatus("REJECTED");
            q.setOrganizerNote(trimOrNull(note));
            q.setUpdatedAt(LocalDateTime.now());
            return Map.of("quotation", toQuotationMap(quotationRepository.save(q)));
        } else if ("COUNTEROFFER".equals(action)) {
            q.setCounterOfferAmount(counterOffer);
            q.setOrganizerNote(trimOrNull(note));
            q.setUpdatedAt(LocalDateTime.now());
            notificationRepository.save(new VendorNotification(
                    q.getVendor(),
                    "Counteroffer on " + q.getQuotationCode(),
                    organizer.getFullName() + " proposed INR " + counterOffer + " for " + q.getVenue().getVenueName(),
                    "QUOTATION_COUNTEROFFER",
                    q.getId()
            ));
            return Map.of("quotation", toQuotationMap(quotationRepository.save(q)));
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action must be ACCEPT, REJECT, or COUNTEROFFER.");
    }

    @Transactional
    public Map<String, Object> confirmVenueBookingByOrganizer(UserAccount organizer, Long bookingId) {
        VenueBooking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        if (b.getRequester() == null || !b.getRequester().getId().equals(organizer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }
        ensureDateAvailableForConfirmation(b.getVenue(), b.getEventDate(), b.getId());
        b.setStatus("CONFIRMED");
        b.setPaymentStatus("ADVANCE_PAID");
        b.setUpdatedAt(LocalDateTime.now());
        b.setHistoryLog((b.getHistoryLog() == null ? "" : b.getHistoryLog() + "\n") + LocalDateTime.now() + " - CONFIRMED by Organizer");
        VenueBooking saved = bookingRepository.save(b);
        syncAvailabilityWithBooking(saved);

        notificationRepository.save(new VendorNotification(
                saved.getVendor(),
                "Venue Booking CONFIRMED (" + saved.getBookingCode() + ")",
                organizer.getFullName() + " confirmed booking for " + saved.getVenue().getVenueName() + " on " + saved.getEventDate(),
                "BOOKING_CONFIRMED",
                saved.getId()
        ));
        notificationRepository.save(new VendorNotification(
                organizer,
                "Venue Booking CONFIRMED (" + saved.getBookingCode() + ")",
                "Your booking for " + saved.getVenue().getVenueName() + " on " + saved.getEventDate() + " is confirmed.",
                "BOOKING_CONFIRMED",
                saved.getId()
        ));
        return toBookingMap(saved);
    }

    @Transactional
    public Map<String, Object> updateEnquiryStatus(UserAccount vendor, Long enquiryId, String statusRaw) {
        VenueEnquiry enq = enquiryRepository.findByIdAndVendor(enquiryId, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enquiry not found."));
        String status = statusRaw == null ? "" : statusRaw.trim().toUpperCase();
        if (!Set.of("NEW", "UNDER_REVIEW", "QUOTATION_SENT", "ACCEPTED", "REJECTED", "EXPIRED").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid enquiry status.");
        }
        enq.setStatus(status);
        enq.setUpdatedAt(LocalDateTime.now());
        VenueEnquiry saved = enquiryRepository.save(enq);

        if ("ACCEPTED".equals(status)) {
            // Create a PENDING booking if not already created for this enquiry
            createPendingBookingFromEnquiry(saved);
        }
        return toEnquiryMap(saved);
    }

    // ==================== QUOTATIONS ====================

    public List<Map<String, Object>> listVendorQuotations(UserAccount vendor) {
        return quotationRepository.findByVendorOrderByIdDesc(vendor).stream()
                .map(this::toQuotationMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> saveQuotation(UserAccount vendor, Long quotationId, Map<String, Object> body) {
        VenueQuotation q = quotationId != null
                ? quotationRepository.findByIdAndVendor(quotationId, vendor)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quotation not found."))
                : new VenueQuotation();

        Long venueId = toLong(body.get("venueId"));
        Venue venue = venueId != null ? requireOwnedVenue(vendor, venueId) : q.getVenue();
        if (venue == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venue is required.");
        }

        Long enquiryId = toLong(body.get("enquiryId"));
        if (enquiryId != null) {
            VenueEnquiry enq = enquiryRepository.findByIdAndVendor(enquiryId, vendor)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enquiry not found."));
            q.setEnquiry(enq);
        }

        if (q.getQuotationCode() == null) {
            q.setQuotationCode("QTN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            q.setCreatedAt(LocalDateTime.now());
        }
        q.setVendor(vendor);
        q.setVenue(venue);
        q.setCustomerName(strOrDefault(body.get("customerName"), q.getEnquiry() != null ? q.getEnquiry().getCustomerName() : "Customer"));
        q.setCustomerEmail(strOrDefault(body.get("customerEmail"), q.getEnquiry() != null ? q.getEnquiry().getCustomerEmail() : ""));
        q.setCustomerMobile(trimOrNull(str(body.get("customerMobile"))));
        LocalDate eventDate = parseDate(body.get("eventDate"));
        if (eventDate == null && q.getEnquiry() != null) eventDate = q.getEnquiry().getEventDate();
        if (eventDate == null) eventDate = LocalDate.now().plusDays(7);
        q.setEventDate(eventDate);

        BigDecimal rental = toDecimal(body.get("venueRentalPrice"), venue.getPricePerDay());
        BigDecimal additional = toDecimal(body.get("additionalCharges"), BigDecimal.ZERO);
        BigDecimal decor = toDecimal(body.get("decorationCharges"), BigDecimal.ZERO);
        BigDecimal catering = toDecimal(body.get("cateringCharges"), BigDecimal.ZERO);
        BigDecimal discount = toDecimal(body.get("discount"), BigDecimal.ZERO);
        BigDecimal tax = toDecimal(body.get("taxAmount"), BigDecimal.ZERO);
        BigDecimal advance = toDecimal(body.get("advanceAmount"), BigDecimal.ZERO);

        BigDecimal total = rental.add(additional).add(decor).add(catering).subtract(discount).add(tax).max(BigDecimal.ZERO);
        BigDecimal remaining = total.subtract(advance).max(BigDecimal.ZERO);

        q.setVenueRentalPrice(rental);
        q.setAdditionalCharges(additional);
        q.setDecorationCharges(decor);
        q.setCateringCharges(catering);
        q.setDiscount(discount);
        q.setTaxAmount(tax);
        q.setTotalAmount(total);
        q.setAdvanceAmount(advance);
        q.setRemainingAmount(remaining);
        LocalDate validUntil = parseDate(body.get("validUntil"));
        q.setValidUntil(validUntil != null ? validUntil : LocalDate.now().plusDays(14));
        q.setTermsAndConditions(trimOrNull(str(body.get("termsAndConditions"))));
        if (body.containsKey("status")) {
            String st = str(body.get("status")).toUpperCase();
            if (Set.of("DRAFT", "SENT", "ACCEPTED", "REJECTED", "EXPIRED").contains(st)) {
                q.setStatus(st);
            }
        }
        q.setUpdatedAt(LocalDateTime.now());

        VenueQuotation saved = quotationRepository.save(q);
        if ("SENT".equals(saved.getStatus()) && saved.getEnquiry() != null) {
            VenueEnquiry enq = saved.getEnquiry();
            enq.setStatus("QUOTATION_SENT");
            enq.setUpdatedAt(LocalDateTime.now());
            enquiryRepository.save(enq);
            if (enq.getRequester() != null) {
                notificationRepository.save(new VendorNotification(
                        enq.getRequester(),
                        "Quotation Received (" + saved.getQuotationCode() + ")",
                        "Quotation of INR " + saved.getTotalAmount() + " sent for " + venue.getVenueName(),
                        "QUOTATION_SENT",
                        saved.getId()
                ));
            }
        }
        return toQuotationMap(saved);
    }

    @Transactional
    public Map<String, Object> updateQuotationStatus(UserAccount vendor, Long quotationId, String statusRaw) {
        VenueQuotation q = quotationRepository.findByIdAndVendor(quotationId, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quotation not found."));
        String st = statusRaw == null ? "" : statusRaw.trim().toUpperCase();
        if (!Set.of("DRAFT", "SENT", "ACCEPTED", "REJECTED", "EXPIRED").contains(st)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quotation status.");
        }
        q.setStatus(st);
        q.setUpdatedAt(LocalDateTime.now());
        VenueQuotation saved = quotationRepository.save(q);

        if ("SENT".equals(st) && saved.getEnquiry() != null) {
            VenueEnquiry enq = saved.getEnquiry();
            enq.setStatus("QUOTATION_SENT");
            enq.setUpdatedAt(LocalDateTime.now());
            enquiryRepository.save(enq);
        } else if ("ACCEPTED".equals(st)) {
            // Create a PENDING booking ready for vendor confirmation (do NOT auto-confirm!)
            createPendingBookingFromQuotation(saved);
            notificationRepository.save(new VendorNotification(
                    vendor,
                    "Quotation Accepted (" + saved.getQuotationCode() + ")",
                    saved.getCustomerName() + " accepted the quotation for " + saved.getVenue().getVenueName(),
                    "QUOTATION_ACCEPTED",
                    saved.getId()
            ));
        }
        return toQuotationMap(saved);
    }

    public byte[] generateQuotationPdf(UserAccount vendor, Long quotationId) {
        VenueQuotation q = quotationRepository.findByIdAndVendor(quotationId, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quotation not found."));
        List<String> lines = List.of(
                "Quotation Code: " + q.getQuotationCode(),
                "Status: " + q.getStatus(),
                "Venue: " + q.getVenue().getVenueName() + " (" + q.getVenue().getCity() + ")",
                "Customer: " + q.getCustomerName() + " | " + q.getCustomerEmail(),
                "Event Date: " + q.getEventDate(),
                "Valid Until: " + (q.getValidUntil() != null ? q.getValidUntil() : "N/A"),
                "----------------------------------------",
                "Venue Rental Price: INR " + q.getVenueRentalPrice(),
                "Additional Charges: INR " + q.getAdditionalCharges(),
                "Decoration Charges: INR " + q.getDecorationCharges(),
                "Catering Charges: INR " + q.getCateringCharges(),
                "Discount: INR " + q.getDiscount(),
                "Tax: INR " + q.getTaxAmount(),
                "Total Amount: INR " + q.getTotalAmount(),
                "Advance Amount: INR " + q.getAdvanceAmount(),
                "Remaining Amount: INR " + q.getRemainingAmount(),
                "Terms: " + (q.getTermsAndConditions() != null ? q.getTermsAndConditions() : "Standard venue terms apply.")
        );
        return SimplePdfGenerator.buildSimplePdf("Entertainment World - Venue Quotation", lines);
    }

    // ==================== VENUE BOOKINGS ====================

    public List<Map<String, Object>> listVendorBookings(UserAccount vendor) {
        return bookingRepository.findByVendorOrderByIdDesc(vendor).stream()
                .map(this::toBookingMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> createBooking(UserAccount vendor, Map<String, Object> body) {
        Long venueId = toLong(body.get("venueId"));
        Venue venue = requireOwnedVenue(vendor, venueId);
        LocalDate eventDate = parseDate(body.get("eventDate"));
        if (eventDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event date is required.");
        }
        String targetStatus = strOrDefault(body.get("status"), "PENDING").toUpperCase();
        if ("CONFIRMED".equals(targetStatus)) {
            ensureDateAvailableForConfirmation(venue, eventDate, null);
        }

        VenueBooking b = new VenueBooking();
        b.setBookingCode("VBK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        b.setVenue(venue);
        b.setVendor(vendor);
        b.setCustomerName(strOrDefault(body.get("customerName"), "Walk-in Customer"));
        b.setCustomerEmail(strOrDefault(body.get("customerEmail"), ""));
        b.setCustomerMobile(trimOrNull(str(body.get("customerMobile"))));
        b.setEventType(strOrDefault(body.get("eventType"), "Event"));
        b.setEventDate(eventDate);
        b.setGuestCount(toInt(body.get("guestCount"), venue.getSeatingCapacity()));
        BigDecimal total = toDecimal(body.get("totalAmount"), venue.getPricePerDay());
        BigDecimal adv = toDecimal(body.get("advancePaid"), BigDecimal.ZERO);
        b.setTotalAmount(total);
        b.setAdvancePaid(adv);
        b.setPaymentStatus(adv.compareTo(total) >= 0 && total.compareTo(BigDecimal.ZERO) > 0 ? "PAID" : (adv.compareTo(BigDecimal.ZERO) > 0 ? "PARTIAL" : "PENDING"));
        b.setStatus(targetStatus);
        b.setNotes(trimOrNull(str(body.get("notes"))));
        b.setCreatedAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        b.setHistoryLog(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - Created as " + targetStatus);

        VenueBooking saved = bookingRepository.save(b);
        syncAvailabilityWithBooking(saved);
        return toBookingMap(saved);
    }

    @Transactional
    public Map<String, Object> updateBookingStatus(UserAccount vendor, Long bookingId, String statusRaw, String note) {
        VenueBooking b = bookingRepository.findByIdAndVendor(bookingId, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        String target = statusRaw == null ? "" : statusRaw.trim().toUpperCase();
        if (!Set.of("PENDING", "CONFIRMED", "REJECTED", "CANCELLED", "COMPLETED").contains(target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking status.");
        }

        if ("CONFIRMED".equals(target) && !"CONFIRMED".equals(b.getStatus())) {
            ensureDateAvailableForConfirmation(b.getVenue(), b.getEventDate(), b.getId());
        }

        b.setStatus(target);
        b.setUpdatedAt(LocalDateTime.now());
        String entry = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - " + target
                + (isNotBlank(note) ? " (" + note.trim() + ")" : "");
        b.setHistoryLog(b.getHistoryLog() == null ? entry : b.getHistoryLog() + "\n" + entry);
        VenueBooking saved = bookingRepository.save(b);

        syncAvailabilityWithBooking(saved);

        // Notifications
        String notifType = "BOOKING_" + target;
        notificationRepository.save(new VendorNotification(
                vendor,
                "Booking " + target + " (" + saved.getBookingCode() + ")",
                saved.getVenue().getVenueName() + " on " + saved.getEventDate() + " is now " + target,
                notifType,
                saved.getId()
        ));
        if (saved.getRequester() != null) {
            notificationRepository.save(new VendorNotification(
                    saved.getRequester(),
                    "Venue Booking " + target + " (" + saved.getBookingCode() + ")",
                    saved.getVenue().getVenueName() + " on " + saved.getEventDate() + " was marked " + target,
                    notifType,
                    saved.getId()
            ));
        }
        return toBookingMap(saved);
    }

    // ==================== MULTIPLE LOCATIONS ====================

    public List<Map<String, Object>> listLocations(UserAccount vendor) {
        return locationRepository.findByVendorOrderByIdDesc(vendor).stream()
                .map(this::toLocationMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> saveLocation(UserAccount vendor, Long id, Map<String, Object> body) {
        PlanLimits limits = getVendorPlanLimits(vendor);
        if (id == null) {
            if (!limits.allowMultipleLocations()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Multiple Location Management requires a Premium (₹999) or Pro (₹2,499) plan.");
            }
            long count = locationRepository.countByVendor(vendor);
            if (count >= limits.maxLocations()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Location limit reached (" + count + "/" + limits.maxLocations() + ") on " + limits.planName());
            }
        }
        VendorLocation loc = id != null
                ? locationRepository.findByIdAndVendor(id, vendor)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found."))
                : new VendorLocation();
        loc.setVendor(vendor);
        loc.setLocationName(strOrDefault(body.get("locationName"), "Branch Location"));
        loc.setBranchCode(trimOrNull(str(body.get("branchCode"))));
        loc.setAddress(trimOrNull(str(body.get("address"))));
        loc.setCity(strOrDefault(body.get("city"), ""));
        loc.setState(trimOrNull(str(body.get("state"))));
        loc.setCountry(strOrDefault(body.get("country"), "India"));
        loc.setPincode(trimOrNull(str(body.get("pincode"))));
        loc.setContactPerson(trimOrNull(str(body.get("contactPerson"))));
        loc.setContactPhone(trimOrNull(str(body.get("contactPhone"))));
        loc.setLinkedVenueId(toLong(body.get("linkedVenueId")));
        if (loc.getCreatedAt() == null) loc.setCreatedAt(LocalDateTime.now());
        return toLocationMap(locationRepository.save(loc));
    }

    @Transactional
    public void deleteLocation(UserAccount vendor, Long id) {
        VendorLocation loc = locationRepository.findByIdAndVendor(id, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found."));
        locationRepository.delete(loc);
    }

    // ==================== TEAM MANAGEMENT ====================

    public List<Map<String, Object>> listTeamMembers(UserAccount vendor) {
        return teamRepository.findByVendorOrderByIdDesc(vendor).stream()
                .map(this::toTeamMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> saveTeamMember(UserAccount vendor, Long id, Map<String, Object> body) {
        PlanLimits limits = getVendorPlanLimits(vendor);
        if (id == null) {
            long count = teamRepository.countByVendor(vendor);
            if (count >= limits.maxTeamAccounts()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Team member limit reached (" + count + "/" + limits.maxTeamAccounts() + ") for your " + limits.planName() + ". Please upgrade your subscription.");
            }
        }
        VendorTeamMember m = id != null
                ? teamRepository.findByIdAndVendor(id, vendor)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team member not found."))
                : new VendorTeamMember();
        m.setVendor(vendor);
        m.setFullName(strOrDefault(body.get("fullName"), "Team Member"));
        m.setEmail(strOrDefault(body.get("email"), ""));
        m.setPhone(trimOrNull(str(body.get("phone"))));
        String role = strOrDefault(body.get("role"), "STAFF").toUpperCase();
        if (!Set.of("OWNER", "MANAGER", "BOOKING_MANAGER", "STAFF").contains(role)) role = "STAFF";
        m.setRole(role);
        m.setPermissions(strOrDefault(body.get("permissions"), "VENUES,CALENDAR,ENQUIRIES,BOOKINGS"));
        if (m.getInvitedAt() == null) m.setInvitedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        return toTeamMap(teamRepository.save(m));
    }

    @Transactional
    public Map<String, Object> toggleTeamMember(UserAccount vendor, Long id) {
        VendorTeamMember m = teamRepository.findByIdAndVendor(id, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team member not found."));
        m.setActive(!m.isActive());
        m.setUpdatedAt(LocalDateTime.now());
        return toTeamMap(teamRepository.save(m));
    }

    @Transactional
    public void deleteTeamMember(UserAccount vendor, Long id) {
        VendorTeamMember m = teamRepository.findByIdAndVendor(id, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team member not found."));
        teamRepository.delete(m);
    }

    // ==================== ANALYTICS ====================

    public Map<String, Object> getAnalytics(UserAccount vendor, String range, LocalDate fromDate, LocalDate toDate) {
        LocalDate end = toDate != null ? toDate : LocalDate.now();
        LocalDate start = fromDate != null ? fromDate : switch (range == null ? "30d" : range.toLowerCase()) {
            case "7d" -> end.minusDays(7);
            case "3m" -> end.minusMonths(3);
            case "6m" -> end.minusMonths(6);
            default -> end.minusDays(30);
        };

        List<VenueEnquiry> enquiries = enquiryRepository.findByVendorOrderByIdDesc(vendor).stream()
                .filter(e -> e.getCreatedAt() != null && !e.getCreatedAt().toLocalDate().isBefore(start) && !e.getCreatedAt().toLocalDate().isAfter(end))
                .toList();
        List<VenueBooking> bookings = bookingRepository.findByVendorOrderByIdDesc(vendor).stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().toLocalDate().isBefore(start) && !b.getCreatedAt().toLocalDate().isAfter(end))
                .toList();

        long totalEnq = enquiries.size();
        long confirmed = bookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus())).count();
        long rejected = bookings.stream().filter(b -> "REJECTED".equals(b.getStatus())).count();
        long cancelled = bookings.stream().filter(b -> "CANCELLED".equals(b.getStatus())).count();

        double conversionRate = totalEnq == 0 ? 0.0 : BigDecimal.valueOf((confirmed * 100.0) / totalEnq)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        BigDecimal revenue = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                .map(b -> b.getTotalAmount() == null ? BigDecimal.ZERO : b.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Venue> venues = venueRepository.findByVendorOrderByIdDesc(vendor);
        List<Map<String, Object>> venuePerf = buildVenuePerformance(venues, enquiries, bookings);
        String mostPopular = venuePerf.stream()
                .max(Comparator.comparingLong(m -> ((Number) m.get("bookings")).longValue()))
                .map(m -> (String) m.get("venueName"))
                .orElse("N/A");

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("startDate", start);
        res.put("endDate", end);
        res.put("totalEnquiries", totalEnq);
        res.put("confirmedBookings", confirmed);
        res.put("rejectedBookings", rejected);
        res.put("cancelledBookings", cancelled);
        res.put("conversionRate", conversionRate);
        res.put("totalRevenue", revenue);
        res.put("mostPopularVenue", mostPopular);
        res.put("monthlyTrends", buildMonthlyRevenue(bookings, 6));
        res.put("venuePerformance", venuePerf);
        res.put("analyticsLevel", getVendorPlanLimits(vendor).analyticsLevel());
        return res;
    }

    // ==================== PUBLIC VENUE SEARCH ====================

    public List<Map<String, Object>> searchPublicVenues(String city, String venueType, Integer guests, BigDecimal maxBudget, String query) {
        return venueRepository.findByActiveTrueAndFlaggedFalseOrderByIdDesc().stream()
                .filter(v -> v.getVendor().getVerificationStatus() == VerificationStatus.APPROVED && !v.getVendor().isSuspended())
                .filter(v -> !isNotBlank(city) || (v.getCity() != null && v.getCity().toLowerCase().contains(city.trim().toLowerCase())))
                .filter(v -> !isNotBlank(venueType) || (v.getVenueType() != null && v.getVenueType().equalsIgnoreCase(venueType.trim())))
                .filter(v -> guests == null || (v.getMaxCapacity() != null && v.getMaxCapacity() >= guests))
                .filter(v -> maxBudget == null || (v.getPricePerDay() != null && v.getPricePerDay().compareTo(maxBudget) <= 0))
                .filter(v -> !isNotBlank(query) || matchesQuery(v, query.trim().toLowerCase()))
                .map(v -> toPublicVenueMap(v))
                .toList();
    }

    public Map<String, Object> getPublicVenueDetail(Long id) {
        Venue v = venueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found."));
        if (!v.isActive() || v.isFlagged()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue is not available.");
        }
        return toPublicVenueMap(v);
    }

    // ==================== HELPERS ====================

    private Venue requireOwnedVenue(UserAccount vendor, Long venueId) {
        if (venueId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venue ID is required.");
        }
        return venueRepository.findByIdAndVendor(venueId, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found or access denied."));
    }

    private void ensureDateAvailableForConfirmation(Venue venue, LocalDate date, Long currentBookingId) {
        boolean alreadyConfirmed = bookingRepository.findByVenueOrderByIdDesc(venue).stream()
                .anyMatch(b -> "CONFIRMED".equals(b.getStatus())
                        && date.equals(b.getEventDate())
                        && (currentBookingId == null || !b.getId().equals(currentBookingId)));
        if (alreadyConfirmed) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Overlap prevented: " + venue.getVenueName() + " is already CONFIRMED for " + date + ".");
        }
        Optional<VenueAvailability> avail = availabilityRepository.findByVenueAndAvailabilityDate(venue, date);
        if (avail.isPresent() && "BLOCKED".equals(avail.get().getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Date " + date + " is manually BLOCKED for " + venue.getVenueName() + ".");
        }
    }

    private void syncAvailabilityWithBooking(VenueBooking b) {
        VenueAvailability avail = availabilityRepository.findByVenueAndAvailabilityDate(b.getVenue(), b.getEventDate())
                .orElseGet(VenueAvailability::new);
        avail.setVenue(b.getVenue());
        avail.setAvailabilityDate(b.getEventDate());
        avail.setBookingId(b.getId());
        avail.setUpdatedAt(LocalDateTime.now());
        if ("CONFIRMED".equals(b.getStatus())) {
            avail.setStatus("BOOKED");
            avail.setNote("Confirmed: " + b.getBookingCode() + " (" + b.getCustomerName() + ")");
        } else if ("PENDING".equals(b.getStatus())) {
            avail.setStatus("PENDING");
            avail.setNote("Pending: " + b.getBookingCode());
        } else {
            avail.setStatus("AVAILABLE");
            avail.setBookingId(null);
            avail.setNote("Released from " + b.getBookingCode());
        }
        availabilityRepository.save(avail);
    }

    private VenueBooking createPendingBookingFromEnquiry(VenueEnquiry enq) {
        VenueBooking b = new VenueBooking();
        b.setBookingCode("VBK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        b.setVenue(enq.getVenue());
        b.setVendor(enq.getVendor());
        b.setRequester(enq.getRequester());
        b.setEventId(enq.getEventId());
        b.setEventName(enq.getEventName());
        b.setEventTime(enq.getEventTime());
        b.setCustomerName(enq.getCustomerName());
        b.setCustomerEmail(enq.getCustomerEmail());
        b.setCustomerMobile(enq.getCustomerMobile());
        b.setEventType(enq.getEventType());
        b.setEventDate(enq.getEventDate());
        b.setGuestCount(enq.getGuestCount());
        b.setTotalAmount(enq.getBudget() != null ? enq.getBudget() : enq.getVenue().getPricePerDay());
        b.setStatus("PENDING");
        b.setCreatedAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        b.setHistoryLog("Created from Enquiry " + enq.getEnquiryCode());
        VenueBooking saved = bookingRepository.save(b);
        syncAvailabilityWithBooking(saved);
        return saved;
    }

    private VenueBooking createPendingBookingFromQuotation(VenueQuotation q) {
        VenueBooking b = new VenueBooking();
        b.setBookingCode("VBK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        b.setVenue(q.getVenue());
        b.setVendor(q.getVendor());
        b.setRequester(q.getEnquiry() != null ? q.getEnquiry().getRequester() : null);
        b.setQuotation(q);
        if (q.getEnquiry() != null) {
            b.setEventId(q.getEnquiry().getEventId());
            b.setEventName(q.getEnquiry().getEventName());
            b.setEventTime(q.getEnquiry().getEventTime());
        }
        b.setCustomerName(q.getCustomerName());
        b.setCustomerEmail(q.getCustomerEmail());
        b.setCustomerMobile(q.getCustomerMobile());
        b.setEventType(q.getEnquiry() != null ? q.getEnquiry().getEventType() : "Event");
        b.setEventDate(q.getEventDate());
        b.setGuestCount(q.getEnquiry() != null ? q.getEnquiry().getGuestCount() : q.getVenue().getSeatingCapacity());
        b.setTotalAmount(q.getTotalAmount());
        b.setAdvancePaid(q.getAdvanceAmount());
        b.setStatus("PENDING");
        b.setCreatedAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        b.setHistoryLog("Created from accepted Quotation " + q.getQuotationCode());
        VenueBooking saved = bookingRepository.save(b);
        syncAvailabilityWithBooking(saved);
        return saved;
    }

    private void applyVenueFields(Venue v, Map<String, Object> body) {
        String name = str(body.get("venueName"));
        String type = str(body.get("venueType"));
        String description = str(body.get("description"));
        String country = str(body.get("country"));
        String state = str(body.get("state"));
        String city = str(body.get("city"));
        String areaLocality = str(body.get("areaLocality"));
        String address = str(body.get("address"));
        String pincode = str(body.get("pincode"));
        String mapLink = str(body.get("mapLink"));
        String countryCode = str(body.get("contactCountryCode"));
        String contactNumber = str(body.get("contactNumber"));
        String venueCategory = str(body.get("venueCategory"));

        if (!isNotBlank(name) || !isNotBlank(type) || !isNotBlank(description)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venue Name, Venue Type, and Venue Description are required.");
        }
        if (!isNotBlank(country) || !isNotBlank(state) || !isNotBlank(city)
                || !isNotBlank(areaLocality) || !isNotBlank(address) || !isNotBlank(pincode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All location fields (Country, State, City, Area/Locality, Complete Address, PIN Code) are required.");
        }
        if (!isNotBlank(mapLink)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google Maps Location URL is required.");
        }
        if (!isValidMapsUrl(mapLink.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid Google Maps URL (starting with http:// or https://).");
        }
        if (!isValidPostalCode(country.trim(), pincode.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid PIN / Postal Code format for " + country.trim() + ".");
        }
        String digitsOnlyPhone = contactNumber == null ? "" : contactNumber.replaceAll("[\\s\\-()]", "");
        if (!isNotBlank(countryCode) || !digitsOnlyPhone.matches("^[0-9]{7,15}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid Country Code and Contact Phone Number (7-15 digits) are required.");
        }
        if (!isNotBlank(venueCategory) || !(venueCategory.trim().equalsIgnoreCase("Indoor") || venueCategory.trim().equalsIgnoreCase("Outdoor"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venue Category must be Indoor or Outdoor.");
        }

        v.setVenueName(name.trim());
        v.setVenueType(type.trim());
        v.setDescription(description.trim());
        v.setOwnerName(trimOrNull(str(body.get("ownerName"))));
        v.setContactCountryCode(countryCode.trim());
        v.setContactNumber(digitsOnlyPhone);
        v.setEmail(trimOrNull(str(body.get("email"))));
        v.setAddress(address.trim());
        v.setAreaLocality(areaLocality.trim());
        v.setCity(city.trim());
        v.setState(state.trim());
        v.setCountry(country.trim());
        v.setPincode(pincode.trim());
        v.setMapLink(mapLink.trim());
        v.setVenueCategory(venueCategory.trim().equalsIgnoreCase("Outdoor") ? "Outdoor" : "Indoor");

        v.setMinCapacity(toInt(body.get("minCapacity"), 50));
        v.setMaxCapacity(toInt(body.get("maxCapacity"), 500));
        v.setSeatingCapacity(toInt(body.get("seatingCapacity"), 350));
        v.setFloatingCapacity(toInt(body.get("floatingCapacity"), 500));

        v.setParking(toBool(body.get("parking"), true));
        v.setAirConditioning(toBool(body.get("airConditioning"), true));
        v.setCateringArea(toBool(body.get("cateringArea"), true));
        v.setStage(toBool(body.get("stage"), true));
        v.setDiningArea(toBool(body.get("diningArea"), true));
        v.setDecoration(toBool(body.get("decoration"), false));
        v.setPowerBackup(toBool(body.get("powerBackup"), true));
        v.setWifi(toBool(body.get("wifi"), true));
        v.setChangingRooms(toBool(body.get("changingRooms"), true));
        v.setSecurity(toBool(body.get("security"), true));
        v.setGenerator(toBool(body.get("generator"), true));
        v.setOtherFacilities(trimOrNull(str(body.get("otherFacilities"))));

        v.setPricePerDay(toDecimal(body.get("pricePerDay"), new BigDecimal("50000.00")));
        v.setPricePerHour(toDecimal(body.get("pricePerHour"), BigDecimal.ZERO));
        v.setWeekdayPrice(toDecimal(body.get("weekdayPrice"), v.getPricePerDay()));
        v.setWeekendPrice(toDecimal(body.get("weekendPrice"), v.getPricePerDay()));
        v.setAdvanceAmount(toDecimal(body.get("advanceAmount"), BigDecimal.ZERO));
    }

    private static boolean isValidMapsUrl(String url) {
        return url.matches("^https?://.+");
    }

    private static boolean isValidPostalCode(String country, String code) {
        return switch (country.toLowerCase()) {
            case "india" -> code.matches("^[1-9][0-9]{5}$");
            case "united states", "usa" -> code.matches("^\\d{5}(-\\d{4})?$");
            case "united kingdom", "uk" -> code.matches("^[A-Za-z0-9\\s-]{3,10}$");
            case "canada" -> code.matches("^[A-Za-z]\\d[A-Za-z][ -]?\\d[A-Za-z]\\d$");
            case "australia" -> code.matches("^\\d{4}$");
            default -> code.matches("^[A-Za-z0-9\\s-]{3,12}$");
        };
    }

    private List<Map<String, Object>> buildMonthlyRevenue(List<VenueBooking> bookings, int months) {
        YearMonth now = YearMonth.now();
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            long count = bookings.stream()
                    .filter(b -> ("CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                            && b.getEventDate() != null && YearMonth.from(b.getEventDate()).equals(ym))
                    .count();
            BigDecimal rev = bookings.stream()
                    .filter(b -> ("CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                            && b.getEventDate() != null && YearMonth.from(b.getEventDate()).equals(ym))
                    .map(b -> b.getTotalAmount() == null ? BigDecimal.ZERO : b.getTotalAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", ym.toString());
            m.put("bookings", count);
            m.put("revenue", rev);
            list.add(m);
        }
        return list;
    }

    private List<Map<String, Object>> buildVenuePerformance(List<Venue> venues, List<VenueEnquiry> enquiries, List<VenueBooking> bookings) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Venue v : venues) {
            long enqCount = enquiries.stream().filter(e -> e.getVenue().getId().equals(v.getId())).count();
            long bookCount = bookings.stream().filter(b -> b.getVenue().getId().equals(v.getId()) && ("CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))).count();
            BigDecimal rev = bookings.stream()
                    .filter(b -> b.getVenue().getId().equals(v.getId()) && ("CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus())))
                    .map(b -> b.getTotalAmount() == null ? BigDecimal.ZERO : b.getTotalAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("venueId", v.getId());
            m.put("venueName", v.getVenueName());
            m.put("enquiries", enqCount);
            m.put("bookings", bookCount);
            m.put("revenue", rev);
            list.add(m);
        }
        return list;
    }

    private Map<String, Object> toVenueMap(Venue v, boolean includeMedia) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("vendorId", v.getVendor().getId());
        m.put("venueName", v.getVenueName());
        m.put("venueType", v.getVenueType());
        m.put("description", v.getDescription());
        m.put("ownerName", v.getOwnerName());
        m.put("contactCountryCode", v.getContactCountryCode());
        m.put("contactNumber", v.getContactNumber());
        m.put("venueCategory", v.getVenueCategory());
        m.put("email", v.getEmail());
        m.put("address", v.getAddress());
        m.put("areaLocality", v.getAreaLocality());
        m.put("city", v.getCity());
        m.put("state", v.getState());
        m.put("country", v.getCountry());
        m.put("pincode", v.getPincode());
        m.put("mapLink", v.getMapLink());
        m.put("minCapacity", v.getMinCapacity());
        m.put("maxCapacity", v.getMaxCapacity());
        m.put("seatingCapacity", v.getSeatingCapacity());
        m.put("floatingCapacity", v.getFloatingCapacity());
        m.put("parking", v.isParking());
        m.put("airConditioning", v.isAirConditioning());
        m.put("cateringArea", v.isCateringArea());
        m.put("stage", v.isStage());
        m.put("diningArea", v.isDiningArea());
        m.put("decoration", v.isDecoration());
        m.put("powerBackup", v.isPowerBackup());
        m.put("wifi", v.isWifi());
        m.put("changingRooms", v.isChangingRooms());
        m.put("security", v.isSecurity());
        m.put("generator", v.isGenerator());
        m.put("otherFacilities", v.getOtherFacilities());
        m.put("pricePerDay", v.getPricePerDay());
        m.put("pricePerHour", v.getPricePerHour());
        m.put("weekdayPrice", v.getWeekdayPrice());
        m.put("weekendPrice", v.getWeekendPrice());
        m.put("seasonalPrice", v.getSeasonalPrice());
        m.put("specialEventPrice", v.getSpecialEventPrice());
        m.put("additionalCharges", v.getAdditionalCharges());
        m.put("decorationCharges", v.getDecorationCharges());
        m.put("cateringCharges", v.getCateringCharges());
        m.put("advanceAmount", v.getAdvanceAmount());
        m.put("coverImageUrl", isNotBlank(v.getCoverImageFileName()) ? "/api/public/venues/media/" + v.getCoverImageFileName() : null);
        m.put("active", v.isActive());
        m.put("flagged", v.isFlagged());
        if (includeMedia) {
            m.put("media", mediaRepository.findByVenueOrderByIdDesc(v).stream().map(this::toMediaMap).toList());
        }
        return m;
    }

    private Map<String, Object> toPublicVenueMap(Venue v) {
        Map<String, Object> m = toVenueMap(v, true);
        // Strip sensitive vendor personal info on public endpoint
        m.remove("email");
        m.remove("contactNumber");
        String company = profileRepository.findByUserAccount(v.getVendor())
                .map(VendorProfile::getCompanyName)
                .orElse(v.getVendor().getOrganizationName() != null ? v.getVendor().getOrganizationName() : "Verified Venue Partner");
        m.put("vendorCompanyName", company);
        return m;
    }

    private Map<String, Object> toMediaMap(VenueMedia vm) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", vm.getId());
        m.put("venueId", vm.getVenue().getId());
        m.put("venueName", vm.getVenue().getVenueName());
        m.put("mediaType", vm.getMediaType());
        m.put("url", "/api/public/venues/media/" + vm.getFileName());
        m.put("originalName", vm.getOriginalName());
        m.put("title", vm.getTitle());
        m.put("fileSize", vm.getFileSize());
        m.put("cover", vm.isCover());
        m.put("createdAt", vm.getCreatedAt());
        return m;
    }

    private Map<String, Object> toAvailabilityMap(VenueAvailability a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("venueId", a.getVenue().getId());
        m.put("date", a.getAvailabilityDate().toString());
        m.put("status", a.getStatus());
        m.put("note", a.getNote());
        m.put("bookingId", a.getBookingId());
        return m;
    }

    private Map<String, Object> toEnquiryMap(VenueEnquiry e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("enquiryCode", e.getEnquiryCode());
        m.put("venueId", e.getVenue().getId());
        m.put("venueName", e.getVenue().getVenueName());
        m.put("eventId", e.getEventId());
        m.put("eventName", e.getEventName());
        m.put("requestingModule", e.getRequestingModule());
        m.put("customerName", e.getCustomerName());
        m.put("customerEmail", e.getCustomerEmail());
        m.put("customerMobile", e.getCustomerMobile());
        m.put("eventType", e.getEventType());
        m.put("eventDate", e.getEventDate().toString());
        m.put("eventTime", e.getEventTime());
        m.put("guestCount", e.getGuestCount());
        m.put("budget", e.getBudget());
        m.put("message", e.getMessage());
        m.put("status", e.getStatus());
        m.put("createdAt", e.getCreatedAt());
        return m;
    }

    private Map<String, Object> toQuotationMap(VenueQuotation q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", q.getId());
        m.put("quotationCode", q.getQuotationCode());
        m.put("venueId", q.getVenue().getId());
        m.put("venueName", q.getVenue().getVenueName());
        m.put("enquiryId", q.getEnquiry() != null ? q.getEnquiry().getId() : null);
        m.put("customerName", q.getCustomerName());
        m.put("customerEmail", q.getCustomerEmail());
        m.put("customerMobile", q.getCustomerMobile());
        m.put("eventDate", q.getEventDate().toString());
        m.put("venueRentalPrice", q.getVenueRentalPrice());
        m.put("additionalCharges", q.getAdditionalCharges());
        m.put("decorationCharges", q.getDecorationCharges());
        m.put("cateringCharges", q.getCateringCharges());
        m.put("discount", q.getDiscount());
        m.put("taxAmount", q.getTaxAmount());
        m.put("totalAmount", q.getTotalAmount());
        m.put("advanceAmount", q.getAdvanceAmount());
        m.put("remainingAmount", q.getRemainingAmount());
        m.put("validUntil", q.getValidUntil() != null ? q.getValidUntil().toString() : null);
        m.put("termsAndConditions", q.getTermsAndConditions());
        m.put("status", q.getStatus());
        m.put("pdfUrl", "/api/vendor/quotations/" + q.getId() + "/pdf");
        return m;
    }

    private Map<String, Object> toBookingMap(VenueBooking b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("bookingCode", b.getBookingCode());
        m.put("venueId", b.getVenue().getId());
        m.put("venueName", b.getVenue().getVenueName());
        m.put("venueCity", b.getVenue().getCity());
        m.put("customerName", b.getCustomerName());
        m.put("customerEmail", b.getCustomerEmail());
        m.put("customerMobile", b.getCustomerMobile());
        m.put("eventType", b.getEventType());
        m.put("eventId", b.getEventId());
        m.put("eventName", b.getEventName());
        m.put("eventTime", b.getEventTime());
        m.put("eventDate", b.getEventDate().toString());
        m.put("guestCount", b.getGuestCount());
        m.put("totalAmount", b.getTotalAmount());
        m.put("advancePaid", b.getAdvancePaid());
        m.put("paymentStatus", b.getPaymentStatus());
        m.put("status", b.getStatus());
        m.put("notes", b.getNotes());
        m.put("historyLog", b.getHistoryLog());
        m.put("createdAt", b.getCreatedAt());
        return m;
    }

    private Map<String, Object> toLocationMap(VendorLocation l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("locationName", l.getLocationName());
        m.put("branchCode", l.getBranchCode());
        m.put("address", l.getAddress());
        m.put("city", l.getCity());
        m.put("state", l.getState());
        m.put("country", l.getCountry());
        m.put("pincode", l.getPincode());
        m.put("contactPerson", l.getContactPerson());
        m.put("contactPhone", l.getContactPhone());
        m.put("linkedVenueId", l.getLinkedVenueId());
        m.put("active", l.isActive());
        return m;
    }

    private Map<String, Object> toTeamMap(VendorTeamMember t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("fullName", t.getFullName());
        m.put("email", t.getEmail());
        m.put("phone", t.getPhone());
        m.put("role", t.getRole());
        m.put("permissions", t.getPermissions());
        m.put("active", t.isActive());
        m.put("invitedAt", t.getInvitedAt());
        return m;
    }

    private boolean matchesQuery(Venue v, String q) {
        return (v.getVenueName() != null && v.getVenueName().toLowerCase().contains(q))
                || (v.getVenueType() != null && v.getVenueType().toLowerCase().contains(q))
                || (v.getCity() != null && v.getCity().toLowerCase().contains(q))
                || (v.getDescription() != null && v.getDescription().toLowerCase().contains(q));
    }

    private static String storeFile(MultipartFile file, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        String original = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String safeName = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
        Files.copy(file.getInputStream(), targetDir.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);
        return safeName;
    }

    private static String getExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 && idx < name.length() - 1 ? name.substring(idx + 1).toLowerCase() : "";
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
    private static String strOrDefault(Object o, String def) {
        String s = str(o);
        return isNotBlank(s) ? s.trim() : def;
    }
    private static boolean isNotBlank(String s) { return s != null && !s.trim().isEmpty(); }
    private static String trimOrNull(String s) { return isNotBlank(s) ? s.trim() : null; }
    private static Integer toInt(Object o, Integer def) {
        if (o == null || String.valueOf(o).isBlank()) return def;
        return Integer.parseInt(String.valueOf(o).trim());
    }
    private static Long toLong(Object o) {
        if (o == null || String.valueOf(o).isBlank()) return null;
        return Long.parseLong(String.valueOf(o).trim());
    }
    private static BigDecimal toDecimal(Object o, BigDecimal def) {
        if (o == null || String.valueOf(o).isBlank()) return def;
        return new BigDecimal(String.valueOf(o).trim());
    }
    private static boolean toBool(Object o, boolean def) {
        if (o == null) return def;
        if (o instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(o));
    }
    private static LocalDate parseDate(Object o) {
        if (o == null || String.valueOf(o).isBlank()) return null;
        return LocalDate.parse(String.valueOf(o).trim());
    }
}
