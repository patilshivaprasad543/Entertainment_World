package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EventManagementService {

    public static final Path ORG_MEDIA_DIR = Paths.get("uploads", "organizer-media").toAbsolutePath().normalize();

    private final OrganizerProfileRepository profileRepository;
    private final EventRepository eventRepository;
    private final EventPostRepository postRepository;
    private final EventProfessionalBookingRepository profBookingRepository;
    private final ProfessionalShortlistRepository shortlistRepository;
    private final EventTeamMemberRepository teamRepository;
    private final EventExpenseRepository expenseRepository;
    private final EventScheduleItemRepository scheduleRepository;
    private final BookingReviewRepository reviewRepository;
    private final EventPaymentRecordRepository paymentRepository;
    private final OrganizerTeamMemberRepository orgTeamRepository;
    private final UserAccountRepository userAccountRepository;
    private final SavedWishlistItemRepository wishlistRepository;
    private final VendorNotificationRepository notificationRepository;
    private final VenueBookingRepository venueBookingRepository;
    private final VenueEnquiryRepository venueEnquiryRepository;
    private final VenueRepository venueRepository;
    private final SubscriptionService subscriptionService;

    public EventManagementService(
            OrganizerProfileRepository profileRepository,
            EventRepository eventRepository,
            EventPostRepository postRepository,
            EventProfessionalBookingRepository profBookingRepository,
            ProfessionalShortlistRepository shortlistRepository,
            EventTeamMemberRepository teamRepository,
            EventExpenseRepository expenseRepository,
            EventScheduleItemRepository scheduleRepository,
            BookingReviewRepository reviewRepository,
            EventPaymentRecordRepository paymentRepository,
            OrganizerTeamMemberRepository orgTeamRepository,
            UserAccountRepository userAccountRepository,
            SavedWishlistItemRepository wishlistRepository,
            VendorNotificationRepository notificationRepository,
            VenueBookingRepository venueBookingRepository,
            VenueEnquiryRepository venueEnquiryRepository,
            VenueRepository venueRepository,
            SubscriptionService subscriptionService
    ) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
        this.postRepository = postRepository;
        this.profBookingRepository = profBookingRepository;
        this.shortlistRepository = shortlistRepository;
        this.teamRepository = teamRepository;
        this.expenseRepository = expenseRepository;
        this.scheduleRepository = scheduleRepository;
        this.reviewRepository = reviewRepository;
        this.paymentRepository = paymentRepository;
        this.orgTeamRepository = orgTeamRepository;
        this.userAccountRepository = userAccountRepository;
        this.wishlistRepository = wishlistRepository;
        this.notificationRepository = notificationRepository;
        this.venueBookingRepository = venueBookingRepository;
        this.venueEnquiryRepository = venueEnquiryRepository;
        this.venueRepository = venueRepository;
        this.subscriptionService = subscriptionService;
    }

    public record OrganizerPlanLimits(
            String tier,
            String planName,
            int maxActiveEvents,
            int maxWishlist,
            int maxTeamAccounts,
            int aiMatchingLimit,
            boolean allowTeamBuilder,
            boolean allowMultiEvent,
            String budgetLevel,
            String reportLevel
    ) {}

    public OrganizerPlanLimits getPlanLimits(UserAccount organizer) {
        UserSubscription sub = subscriptionService.ensureActiveOrDefaultFree(organizer);
        String tier = sub != null && sub.getPlan() != null ? sub.getPlan().getTier().toUpperCase() : "FREE";
        String planName = sub != null && sub.getPlan() != null ? sub.getPlan().getPlanName() : "Event Management Free";
        return switch (tier) {
            case "PRO" -> new OrganizerPlanLimits(tier, planName, 20, 500, 10, 100, true, true, "ADVANCED", "DETAILED");
            case "PREMIUM" -> new OrganizerPlanLimits(tier, planName, 5, 100, 3, 20, true, true, "ADVANCED", "DETAILED");
            default -> new OrganizerPlanLimits("FREE", planName, 1, 20, 1, 0, false, false, "BASIC", "BASIC");
        };
    }

    // ==================== DASHBOARD HOME ====================

    public Map<String, Object> getDashboardHome(UserAccount org) {
        OrganizerPlanLimits limits = getPlanLimits(org);
        List<Event> events = eventRepository.findByOrganizerOrderByIdDesc(org);
        LocalDate today = LocalDate.now();
        long upcoming = events.stream().filter(e -> "PUBLISHED".equals(e.getStatus()) && e.getEventDate().isAfter(today)).count();
        long ongoing = events.stream().filter(e -> "ONGOING".equals(e.getStatus()) || ("PUBLISHED".equals(e.getStatus()) && e.getEventDate().equals(today))).count();
        long completed = events.stream().filter(e -> "COMPLETED".equals(e.getStatus())).count();

        List<EventProfessionalBooking> profBookings = profBookingRepository.findByOrganizerOrderByIdDesc(org);
        long pendingProf = profBookings.stream().filter(b -> "PENDING".equals(b.getStatus()) || "COUNTEROFFER".equals(b.getStatus())).count();
        long confirmedProf = profBookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus())).count();

        List<VenueEnquiry> venueEnqs = venueEnquiryRepository.findByRequesterOrderByIdDesc(org);
        long pendingVenueEnq = venueEnqs.stream().filter(e -> "NEW".equals(e.getStatus()) || "UNDER_REVIEW".equals(e.getStatus()) || "QUOTATION_SENT".equals(e.getStatus())).count();

        List<VenueBooking> venueBookings = venueBookingRepository.findByRequesterOrderByIdDesc(org);
        long confirmedVenue = venueBookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus())).count();

        BigDecimal totalBudget = events.stream().map(e -> e.getBudget() == null ? BigDecimal.ZERO : e.getBudget()).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<EventPaymentRecord> payments = paymentRepository.findByOrganizerOrderByIdDesc(org);
        BigDecimal totalPayments = payments.stream().map(p -> p.getPaidAmount() == null ? BigDecimal.ZERO : p.getPaidAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<VendorNotification> notifs = notificationRepository.findByRecipientOrderByCreatedAtDesc(org);
        long unreadNotifs = notifs.stream().filter(n -> !n.isReadFlag()).count();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("totalEvents", events.size());
        res.put("upcomingEvents", upcoming);
        res.put("ongoingEvents", ongoing);
        res.put("completedEvents", completed);
        res.put("pendingProfessionalRequests", pendingProf);
        res.put("confirmedProfessionalBookings", confirmedProf);
        res.put("pendingVenueEnquiries", pendingVenueEnq);
        res.put("confirmedVenueBookings", confirmedVenue);
        res.put("totalEventBudget", totalBudget);
        res.put("totalPayments", totalPayments);
        res.put("unreadNotifications", unreadNotifs);
        res.put("planLimits", limits);
        res.put("upcomingEventsList", events.stream().filter(e -> !e.getEventDate().isBefore(today)).limit(5).map(this::toEventMap).toList());
        res.put("pendingRequests", profBookings.stream().filter(b -> "PENDING".equals(b.getStatus())).limit(5).map(this::toProfBookingMap).toList());
        res.put("upcomingSchedule", scheduleRepository.findByOrganizerOrderByScheduleDateAscStartTimeAsc(org).stream().limit(5).map(this::toScheduleMap).toList());
        res.put("recentPayments", payments.stream().limit(5).map(this::toPaymentMap).toList());
        return res;
    }

    // ==================== PROFILE & VERIFICATION ====================

    public Map<String, Object> getProfile(UserAccount org) {
        OrganizerProfile p = profileRepository.findByUserAccount(org).orElseGet(() -> {
            OrganizerProfile np = new OrganizerProfile();
            np.setUserAccount(org);
            np.setCompanyName(isNotBlank(org.getOrganizationName()) ? org.getOrganizationName() : org.getFullName());
            np.setEmail(org.getEmail());
            np.setContactNumber(org.getPhone());
            np.setCity(org.getCity());
            np.setUpdatedAt(LocalDateTime.now());
            return profileRepository.save(np);
        });
        return toProfileMap(p, org);
    }

    @Transactional
    public Map<String, Object> updateProfile(UserAccount org, Map<String, Object> body) {
        OrganizerProfile p = profileRepository.findByUserAccount(org).orElseGet(() -> {
            OrganizerProfile np = new OrganizerProfile();
            np.setUserAccount(org);
            return np;
        });
        p.setCompanyName(strOrDefault(body.get("companyName"), org.getFullName()));
        p.setAboutUs(trimOrNull(str(body.get("aboutUs"))));
        p.setBio(trimOrNull(str(body.get("bio"))));
        p.setSpecialization(trimOrNull(str(body.get("specialization"))));
        p.setCountry(strOrDefault(body.get("country"), "India"));
        p.setState(trimOrNull(str(body.get("state"))));
        p.setCity(trimOrNull(str(body.get("city"))));
        p.setArea(trimOrNull(str(body.get("area"))));
        p.setAddress(trimOrNull(str(body.get("address"))));
        p.setContactNumber(trimOrNull(str(body.get("contactNumber"))));
        p.setEmail(strOrDefault(body.get("email"), org.getEmail()));
        p.setWebsite(trimOrNull(str(body.get("website"))));
        p.setSocialLinks(trimOrNull(str(body.get("socialLinks"))));
        p.setUpdatedAt(LocalDateTime.now());
        return toProfileMap(profileRepository.save(p), org);
    }

    @Transactional
    public Map<String, Object> uploadProfileMedia(UserAccount org, String kind, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please choose an image file.");
        }
        OrganizerProfile p = profileRepository.findByUserAccount(org).orElseGet(() -> {
            OrganizerProfile np = new OrganizerProfile();
            np.setUserAccount(org);
            np.setCompanyName(org.getFullName());
            return np;
        });
        try {
            String stored = storeFile(file, ORG_MEDIA_DIR);
            if ("COVER".equalsIgnoreCase(kind)) p.setCoverImageFileName(stored);
            else p.setLogoFileName(stored);
            p.setUpdatedAt(LocalDateTime.now());
            return toProfileMap(profileRepository.save(p), org);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save image.");
        }
    }

    // ==================== EVENT POSTS ====================

    public List<Map<String, Object>> listPosts() {
        return postRepository.findAllByOrderByIdDesc().stream().map(this::toPostMap).toList();
    }

    @Transactional
    public Map<String, Object> createPost(UserAccount org, Map<String, Object> body) {
        String desc = str(body.get("description"));
        if (!isNotBlank(desc)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post description is required.");
        EventPost p = new EventPost();
        p.setOrganizer(org);
        p.setTitle(trimOrNull(str(body.get("title"))));
        p.setDescription(desc.trim());
        p.setPostType(strOrDefault(body.get("postType"), "ANNOUNCEMENT"));
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return toPostMap(postRepository.save(p));
    }

    @Transactional
    public Map<String, Object> likePost(Long id) {
        EventPost p = postRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));
        p.setLikesCount(p.getLikesCount() + 1);
        return toPostMap(postRepository.save(p));
    }

    @Transactional
    public Map<String, Object> commentPost(UserAccount org, Long id, String comment) {
        if (!isNotBlank(comment)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment cannot be empty.");
        EventPost p = postRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));
        String entry = org.getFullName() + ": " + comment.trim();
        p.setCommentsLog(p.getCommentsLog() == null ? entry : p.getCommentsLog() + "\n" + entry);
        return toPostMap(postRepository.save(p));
    }

    @Transactional
    public void deletePost(UserAccount org, Long id) {
        EventPost p = postRepository.findByIdAndOrganizer(id, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the post owner can delete this post."));
        postRepository.delete(p);
    }

    // ==================== EVENTS CRUD ====================

    public List<Map<String, Object>> listEvents(UserAccount org) {
        return eventRepository.findByOrganizerOrderByIdDesc(org).stream().map(this::toEventMap).toList();
    }

    public Map<String, Object> getEventFullDetails(UserAccount org, Long eventId) {
        Event e = eventRepository.findByIdAndOrganizer(eventId, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        Map<String, Object> res = new LinkedHashMap<>(toEventMap(e));
        res.put("venueBookings", venueBookingRepository.findByEventIdOrderByIdDesc(eventId).stream().map(b -> Map.<String, Object>of(
                "id", b.getId(),
                "bookingCode", b.getBookingCode(),
                "venueId", b.getVenue().getId(),
                "venueName", b.getVenue().getVenueName(),
                "eventDate", b.getEventDate().toString(),
                "totalAmount", b.getTotalAmount(),
                "advancePaid", b.getAdvancePaid(),
                "status", b.getStatus()
        )).toList());
        res.put("professionalBookings", profBookingRepository.findByEventOrderByIdDesc(e).stream().map(this::toProfBookingMap).toList());
        res.put("teamMembers", teamRepository.findByEventAndOrganizerOrderByIdDesc(e, org).stream().map(this::toEventTeamMap).toList());
        res.put("expenses", expenseRepository.findByEventAndOrganizerOrderByIdDesc(e, org).stream().map(this::toExpenseMap).toList());
        res.put("schedule", scheduleRepository.findByEventAndOrganizerOrderByScheduleDateAscSortOrderAscStartTimeAsc(e, org).stream().map(this::toScheduleMap).toList());
        return res;
    }

    @Transactional
    public Map<String, Object> saveEvent(UserAccount org, Long id, Map<String, Object> body) {
        OrganizerPlanLimits limits = getPlanLimits(org);
        String targetStatus = strOrDefault(body.get("status"), "DRAFT").toUpperCase();
        if (!Set.of("DRAFT", "PUBLISHED", "ONGOING", "COMPLETED", "CANCELLED").contains(targetStatus)) {
            targetStatus = "DRAFT";
        }

        if ("PUBLISHED".equals(targetStatus)) {
            long activeCount = eventRepository.countByOrganizerAndStatusIn(org, List.of("PUBLISHED", "ONGOING"));
            boolean alreadyActive = id != null && eventRepository.findByIdAndOrganizer(id, org)
                    .map(ev -> "PUBLISHED".equals(ev.getStatus()) || "ONGOING".equals(ev.getStatus())).orElse(false);
            if (!alreadyActive && activeCount >= limits.maxActiveEvents()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Active event limit reached (" + activeCount + "/" + limits.maxActiveEvents() + ") on " + limits.planName() + ". Please upgrade your plan.");
            }
        }

        String name = str(body.get("eventName"));
        String type = str(body.get("eventType"));
        String city = str(body.get("city"));
        LocalDate date = parseDate(body.get("eventDate"));
        Integer guests = toInt(body.get("expectedGuests"), 0);
        BigDecimal budget = toDecimal(body.get("budget"), BigDecimal.ZERO);

        if (!isNotBlank(name) || !isNotBlank(type) || !isNotBlank(city) || date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event Name, Type, City, and Event Date are required.");
        }
        if (guests <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected guests must be a positive number.");
        }
        if (budget.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Budget must be zero or positive.");
        }
        String start = trimOrNull(str(body.get("startTime")));
        String end = trimOrNull(str(body.get("endTime")));
        if (start != null && end != null && end.compareTo(start) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after Start time.");
        }

        Event ev = id != null
                ? eventRepository.findByIdAndOrganizer(id, org).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."))
                : new Event();
        ev.setOrganizer(org);
        ev.setEventName(name.trim());
        ev.setEventType(type.trim());
        ev.setDescription(trimOrNull(str(body.get("description"))));
        ev.setCountry(strOrDefault(body.get("country"), "India"));
        ev.setState(trimOrNull(str(body.get("state"))));
        ev.setCity(city.trim());
        ev.setArea(trimOrNull(str(body.get("area"))));
        ev.setAddress(trimOrNull(str(body.get("address"))));
        ev.setMapLink(trimOrNull(str(body.get("mapLink"))));
        ev.setEventDate(date);
        ev.setStartTime(start);
        ev.setEndTime(end);
        ev.setBudget(budget);
        ev.setExpectedGuests(guests);
        ev.setRequirements(trimOrNull(str(body.get("requirements"))));
        ev.setStatus(targetStatus);
        if (ev.getCreatedAt() == null) ev.setCreatedAt(LocalDateTime.now());
        ev.setUpdatedAt(LocalDateTime.now());
        return toEventMap(eventRepository.save(ev));
    }

    @Transactional
    public Map<String, Object> updateEventStatus(UserAccount org, Long id, String status) {
        Event ev = eventRepository.findByIdAndOrganizer(id, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        String st = status == null ? "" : status.trim().toUpperCase();
        if (!Set.of("DRAFT", "PUBLISHED", "ONGOING", "COMPLETED", "CANCELLED").contains(st)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid event status.");
        }
        if ("PUBLISHED".equals(st) && !"PUBLISHED".equals(ev.getStatus())) {
            OrganizerPlanLimits limits = getPlanLimits(org);
            long activeCount = eventRepository.countByOrganizerAndStatusIn(org, List.of("PUBLISHED", "ONGOING"));
            if (activeCount >= limits.maxActiveEvents()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Active event limit reached (" + activeCount + "/" + limits.maxActiveEvents() + "). Upgrade your plan.");
            }
        }
        ev.setStatus(st);
        ev.setUpdatedAt(LocalDateTime.now());
        return toEventMap(eventRepository.save(ev));
    }

    @Transactional
    public void deleteDraftEvent(UserAccount org, Long id) {
        Event ev = eventRepository.findByIdAndOrganizer(id, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        if (!"DRAFT".equals(ev.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only DRAFT events can be deleted. Cancel published events instead.");
        }
        eventRepository.delete(ev);
    }

    // ==================== PROFESSIONALS SEARCH, SHORTLIST & BOOKINGS ====================

    public List<Map<String, Object>> searchProfessionals(String category, String city, String query) {
        return userAccountRepository.findByRoleInOrderByIdDesc(List.of(UserRole.ACTOR)).stream()
                .filter(u -> u.getVerificationStatus() == VerificationStatus.APPROVED && !u.isSuspended())
                .filter(u -> !isNotBlank(category) || (u.getProfessionalCategory() != null && u.getProfessionalCategory().equalsIgnoreCase(category.trim())))
                .filter(u -> !isNotBlank(city) || (u.getCity() != null && u.getCity().toLowerCase().contains(city.trim().toLowerCase())))
                .filter(u -> !isNotBlank(query) || matchesProfQuery(u, query.trim().toLowerCase()))
                .map(u -> {
                    List<BookingReview> revs = reviewRepository.findByTargetTypeAndTargetIdOrderByIdDesc("PROFESSIONAL", u.getId());
                    double avgRating = revs.isEmpty() ? 0.0 : revs.stream().mapToInt(BookingReview::getRating).average().orElse(0.0);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("fullName", u.getFullName());
                    m.put("category", u.getProfessionalCategory() != null ? u.getProfessionalCategory() : "Entertainment Professional");
                    m.put("city", u.getCity() != null ? u.getCity() : "India");
                    m.put("bio", u.getPortfolioOrBio() != null ? u.getPortfolioOrBio() : "");
                    m.put("phone", u.getPhone());
                    m.put("rating", Math.round(avgRating * 10.0) / 10.0);
                    m.put("reviewsCount", revs.size());
                    return m;
                }).toList();
    }

    public List<Map<String, Object>> listShortlist(UserAccount org, Long eventId) {
        List<ProfessionalShortlist> list = eventId != null
                ? shortlistRepository.findByOrganizerAndEventOrderByIdDesc(org, eventRepository.findByIdAndOrganizer(eventId, org).orElse(null))
                : shortlistRepository.findByOrganizerOrderByIdDesc(org);
        return list.stream().map(this::toShortlistMap).toList();
    }

    @Transactional
    public Map<String, Object> addToShortlist(UserAccount org, Map<String, Object> body) {
        Long profId = toLong(body.get("professionalId"));
        Long eventId = toLong(body.get("eventId"));
        UserAccount prof = userAccountRepository.findById(profId)
                .filter(u -> u.getRole() == UserRole.ACTOR)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professional not found."));
        Event ev = eventId != null ? eventRepository.findByIdAndOrganizer(eventId, org).orElse(null) : null;
        if (ev != null && shortlistRepository.existsByOrganizerAndEventAndProfessional(org, ev, prof)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Professional already shortlisted for this event.");
        }
        ProfessionalShortlist s = new ProfessionalShortlist();
        s.setOrganizer(org);
        s.setEvent(ev);
        s.setProfessional(prof);
        s.setQuotedCharge(toDecimal(body.get("quotedCharge"), BigDecimal.ZERO));
        s.setNotes(trimOrNull(str(body.get("notes"))));
        s.setPreferred(toBool(body.get("preferred"), false));
        s.setCreatedAt(LocalDateTime.now());
        return toShortlistMap(shortlistRepository.save(s));
    }

    @Transactional
    public void removeShortlist(UserAccount org, Long id) {
        ProfessionalShortlist s = shortlistRepository.findByIdAndOrganizer(id, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shortlist entry not found."));
        shortlistRepository.delete(s);
    }

    public List<Map<String, Object>> listProfBookings(UserAccount org) {
        return profBookingRepository.findByOrganizerOrderByIdDesc(org).stream().map(this::toProfBookingMap).toList();
    }

    @Transactional
    public Map<String, Object> createProfBooking(UserAccount org, Map<String, Object> body) {
        Long eventId = toLong(body.get("eventId"));
        Long profId = toLong(body.get("professionalId"));
        Event ev = eventRepository.findByIdAndOrganizer(eventId, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        UserAccount prof = userAccountRepository.findById(profId)
                .filter(u -> u.getRole() == UserRole.ACTOR)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professional not found."));

        if (profBookingRepository.existsByEventAndProfessionalAndStatusNotIn(ev, prof, List.of("CANCELLED", "REJECTED", "COMPLETED"))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active booking request already exists for this professional and event.");
        }

        EventProfessionalBooking b = new EventProfessionalBooking();
        b.setBookingCode("PBK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        b.setOrganizer(org);
        b.setEvent(ev);
        b.setProfessional(prof);
        b.setRequiredCategory(strOrDefault(body.get("requiredCategory"), prof.getProfessionalCategory() != null ? prof.getProfessionalCategory() : "Professional"));
        LocalDate d = parseDate(body.get("eventDate"));
        b.setEventDate(d != null ? d : ev.getEventDate());
        b.setStartTime(strOrDefault(body.get("startTime"), ev.getStartTime()));
        b.setEndTime(strOrDefault(body.get("endTime"), ev.getEndTime()));
        b.setEventLocation(strOrDefault(body.get("eventLocation"), ev.getCity()));
        b.setOfferedFee(toDecimal(body.get("offeredFee"), BigDecimal.ZERO));
        b.setRequirements(trimOrNull(str(body.get("requirements"))));
        b.setMessage(trimOrNull(str(body.get("message"))));
        b.setStatus("PENDING");
        b.setCreatedAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        b.setHistoryLog(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - Requested by " + org.getFullName());

        EventProfessionalBooking saved = profBookingRepository.save(b);
        notificationRepository.save(new VendorNotification(
                prof,
                "New Event Booking Request (" + saved.getBookingCode() + ")",
                org.getFullName() + " invited you for " + ev.getEventName() + " on " + saved.getEventDate(),
                "PROF_BOOKING_NEW",
                saved.getId()
        ));
        return toProfBookingMap(saved);
    }

    @Transactional
    public Map<String, Object> updateProfBookingStatus(UserAccount org, Long id, Map<String, Object> body) {
        EventProfessionalBooking b = profBookingRepository.findByIdAndOrganizer(id, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        String status = strOrDefault(body.get("status"), "").toUpperCase();
        if (!Set.of("PENDING", "ACCEPTED", "REJECTED", "COUNTEROFFER", "RESCHEDULE_REQUESTED", "CANCELLATION_REQUESTED", "CONFIRMED", "CANCELLED", "COMPLETED").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking status.");
        }
        if (body.containsKey("counterOfferFee") && body.get("counterOfferFee") != null) {
            b.setCounterOfferFee(toDecimal(body.get("counterOfferFee"), b.getOfferedFee()));
        }
        b.setStatus(status);
        b.setUpdatedAt(LocalDateTime.now());
        String note = trimOrNull(str(body.get("note")));
        String entry = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " - " + status + (note != null ? " (" + note + ")" : "");
        b.setHistoryLog(b.getHistoryLog() == null ? entry : b.getHistoryLog() + "\n" + entry);
        EventProfessionalBooking saved = profBookingRepository.save(b);

        if ("CONFIRMED".equals(status)) {
            createPaymentRecordIfMissing(org, saved.getEvent(), saved.getBookingCode(), saved.getProfessional().getFullName(), "PROFESSIONAL",
                    saved.getCounterOfferFee() != null ? saved.getCounterOfferFee() : saved.getOfferedFee(), BigDecimal.ZERO);
        }
        return toProfBookingMap(saved);
    }

    // ==================== TEAM BUILDER, BUDGET, SCHEDULE ====================

    public List<Map<String, Object>> listEventTeam(UserAccount org, Long eventId) {
        Event ev = eventRepository.findByIdAndOrganizer(eventId, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        return teamRepository.findByEventAndOrganizerOrderByIdDesc(ev, org).stream().map(this::toEventTeamMap).toList();
    }

    @Transactional
    public Map<String, Object> addEventTeamMember(UserAccount org, Long eventId, Map<String, Object> body) {
        OrganizerPlanLimits limits = getPlanLimits(org);
        if (!limits.allowTeamBuilder()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Event Team Builder requires Premium (₹799) or Pro (₹1,999) plan.");
        }
        Event ev = eventRepository.findByIdAndOrganizer(eventId, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        EventTeamMember m = new EventTeamMember();
        m.setOrganizer(org);
        m.setEvent(ev);
        m.setMemberType(strOrDefault(body.get("memberType"), "PROFESSIONAL").toUpperCase());
        m.setReferenceId(toLong(body.get("referenceId")));
        m.setMemberName(strOrDefault(body.get("memberName"), "Team Member"));
        m.setAssignedRole(strOrDefault(body.get("assignedRole"), "Anchor"));
        m.setCharges(toDecimal(body.get("charges"), BigDecimal.ZERO));
        m.setBookingStatus(strOrDefault(body.get("bookingStatus"), "PLANNED"));
        m.setCreatedAt(LocalDateTime.now());
        return toEventTeamMap(teamRepository.save(m));
    }

    @Transactional
    public void removeEventTeamMember(UserAccount org, Long id) {
        EventTeamMember m = teamRepository.findByIdAndOrganizer(id, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team member not found."));
        teamRepository.delete(m);
    }

    public Map<String, Object> getBudgetPlanner(UserAccount org, Long eventId) {
        Event ev = eventRepository.findByIdAndOrganizer(eventId, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        List<EventExpense> expenses = expenseRepository.findByEventAndOrganizerOrderByIdDesc(ev, org);
        BigDecimal totalEst = expenses.stream().map(EventExpense::getEstimatedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalActual = expenses.stream().map(EventExpense::getActualAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = ev.getBudget().subtract(totalActual);
        return Map.of(
                "eventBudget", ev.getBudget(),
                "totalEstimated", totalEst,
                "totalActual", totalActual,
                "remainingBudget", remaining,
                "expenses", expenses.stream().map(this::toExpenseMap).toList()
        );
    }

    @Transactional
    public Map<String, Object> saveExpense(UserAccount org, Long eventId, Long expenseId, Map<String, Object> body) {
        Event ev = eventRepository.findByIdAndOrganizer(eventId, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        EventExpense ex = expenseId != null
                ? expenseRepository.findByIdAndOrganizer(expenseId, org).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found."))
                : new EventExpense();
        ex.setOrganizer(org);
        ex.setEvent(ev);
        ex.setCategory(strOrDefault(body.get("category"), "Other Expenses"));
        ex.setTitle(strOrDefault(body.get("title"), ex.getCategory()));
        ex.setEstimatedAmount(toDecimal(body.get("estimatedAmount"), BigDecimal.ZERO));
        ex.setActualAmount(toDecimal(body.get("actualAmount"), BigDecimal.ZERO));
        ex.setNotes(trimOrNull(str(body.get("notes"))));
        if (ex.getCreatedAt() == null) ex.setCreatedAt(LocalDateTime.now());
        return toExpenseMap(expenseRepository.save(ex));
    }

    @Transactional
    public void deleteExpense(UserAccount org, Long id) {
        EventExpense ex = expenseRepository.findByIdAndOrganizer(id, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found."));
        expenseRepository.delete(ex);
    }

    public List<Map<String, Object>> listSchedule(UserAccount org, Long eventId) {
        Event ev = eventRepository.findByIdAndOrganizer(eventId, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        return scheduleRepository.findByEventAndOrganizerOrderByScheduleDateAscSortOrderAscStartTimeAsc(ev, org)
                .stream().map(this::toScheduleMap).toList();
    }

    @Transactional
    public Map<String, Object> saveScheduleItem(UserAccount org, Long eventId, Long itemId, Map<String, Object> body) {
        Event ev = eventRepository.findByIdAndOrganizer(eventId, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        String title = str(body.get("title"));
        LocalDate date = parseDate(body.get("scheduleDate"));
        if (date == null) date = ev.getEventDate();
        String start = strOrDefault(body.get("startTime"), "09:00");
        String end = strOrDefault(body.get("endTime"), "10:00");
        if (!isNotBlank(title)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule title is required.");
        if (end.compareTo(start) <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after Start time.");

        Long profId = toLong(body.get("assignedProfessionalId"));
        if (profId != null) {
            List<EventScheduleItem> existing = scheduleRepository.findByAssignedProfessionalIdAndScheduleDate(profId, date);
            for (EventScheduleItem s : existing) {
                if ((itemId == null || !s.getId().equals(itemId)) && timesOverlap(start, end, s.getStartTime(), s.getEndTime())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Schedule conflict: assigned professional already has a slot (" + s.getStartTime() + "-" + s.getEndTime() + ").");
                }
            }
        }

        EventScheduleItem item = itemId != null
                ? scheduleRepository.findByIdAndOrganizer(itemId, org).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule item not found."))
                : new EventScheduleItem();
        item.setOrganizer(org);
        item.setEvent(ev);
        item.setTitle(title.trim());
        item.setScheduleDate(date);
        item.setStartTime(start);
        item.setEndTime(end);
        item.setAssignedProfessionalId(profId);
        item.setAssignedProfessionalName(trimOrNull(str(body.get("assignedProfessionalName"))));
        item.setDescription(trimOrNull(str(body.get("description"))));
        item.setStatus(strOrDefault(body.get("status"), "PLANNED"));
        item.setSortOrder(toInt(body.get("sortOrder"), 0));
        if (item.getCreatedAt() == null) item.setCreatedAt(LocalDateTime.now());
        return toScheduleMap(scheduleRepository.save(item));
    }

    @Transactional
    public void deleteScheduleItem(UserAccount org, Long id) {
        EventScheduleItem item = scheduleRepository.findByIdAndOrganizer(id, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule item not found."));
        scheduleRepository.delete(item);
    }

    // ==================== REVIEWS, PAYMENTS, REPORTS, TEAM ====================

    @Transactional
    public Map<String, Object> submitReview(UserAccount org, Map<String, Object> body) {
        String targetType = strOrDefault(body.get("targetType"), "VENUE").toUpperCase();
        Long targetId = toLong(body.get("targetId"));
        Long bookingId = toLong(body.get("bookingId"));
        Integer rating = toInt(body.get("rating"), 5);
        String title = str(body.get("title"));

        if (targetId == null || bookingId == null || !isNotBlank(title)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target, Booking ID, and Review Title are required.");
        }
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5.");
        }
        if (reviewRepository.existsByReviewerAndTargetTypeAndBookingId(org, targetType, bookingId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this completed booking.");
        }

        if ("VENUE".equals(targetType)) {
            VenueBooking vb = venueBookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue booking not found."));
            if (vb.getRequester() == null || !vb.getRequester().getId().equals(org.getId()) || !"COMPLETED".equals(vb.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only review COMPLETED venue bookings.");
            }
        } else {
            EventProfessionalBooking pb = profBookingRepository.findByIdAndOrganizer(bookingId, org)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professional booking not found."));
            if (!"COMPLETED".equals(pb.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only review COMPLETED professional bookings.");
            }
        }

        BookingReview r = new BookingReview();
        r.setReviewer(org);
        r.setTargetType(targetType);
        r.setTargetId(targetId);
        r.setBookingId(bookingId);
        r.setRating(rating);
        r.setTitle(title.trim());
        r.setDescription(trimOrNull(str(body.get("description"))));
        r.setCreatedAt(LocalDateTime.now());
        return toReviewMap(reviewRepository.save(r));
    }

    public List<Map<String, Object>> listMyReviews(UserAccount org) {
        return reviewRepository.findByReviewerOrderByIdDesc(org).stream().map(this::toReviewMap).toList();
    }

    public List<Map<String, Object>> listPayments(UserAccount org) {
        return paymentRepository.findByOrganizerOrderByIdDesc(org).stream().map(this::toPaymentMap).toList();
    }

    @Transactional
    public Map<String, Object> recordPayment(UserAccount org, Map<String, Object> body) {
        Long id = toLong(body.get("id"));
        EventPaymentRecord rec = id != null
                ? paymentRepository.findByIdAndOrganizer(id, org).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment record not found."))
                : new EventPaymentRecord();

        Long eventId = toLong(body.get("eventId"));
        if (eventId != null) {
            eventRepository.findByIdAndOrganizer(eventId, org).ifPresent(rec::setEvent);
        }
        rec.setOrganizer(org);
        if (rec.getBookingReference() == null) {
            rec.setBookingReference(strOrDefault(body.get("bookingReference"), "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()));
        }
        if (body.containsKey("payeeName")) rec.setPayeeName(strOrDefault(body.get("payeeName"), "Vendor / Professional"));
        if (body.containsKey("payeeType")) rec.setPayeeType(strOrDefault(body.get("payeeType"), "VENUE"));

        BigDecimal bookingAmt = toDecimal(body.get("bookingAmount"), rec.getBookingAmount());
        BigDecimal advAmt = toDecimal(body.get("advanceAmount"), rec.getAdvanceAmount());
        BigDecimal paidAmt = toDecimal(body.get("paidAmount"), rec.getPaidAmount());
        BigDecimal rem = bookingAmt.subtract(paidAmt).max(BigDecimal.ZERO);

        rec.setBookingAmount(bookingAmt);
        rec.setAdvanceAmount(advAmt);
        rec.setPaidAmount(paidAmt);
        rec.setRemainingBalance(rem);
        String pStatus = rem.compareTo(BigDecimal.ZERO) == 0 && bookingAmt.compareTo(BigDecimal.ZERO) > 0
                ? "PAID"
                : (paidAmt.compareTo(BigDecimal.ZERO) > 0 ? "PARTIALLY_PAID" : "PENDING");
        if (body.containsKey("paymentStatus")) pStatus = strOrDefault(body.get("paymentStatus"), pStatus).toUpperCase();
        rec.setPaymentStatus(pStatus);
        rec.setContractStatus(strOrDefault(body.get("contractStatus"), "SIGNED"));
        rec.setPaymentMode("MANUAL_RECORD");
        rec.setNotes(trimOrNull(str(body.get("notes"))));
        if (rec.getCreatedAt() == null) rec.setCreatedAt(LocalDateTime.now());
        rec.setUpdatedAt(LocalDateTime.now());
        return toPaymentMap(paymentRepository.save(rec));
    }

    public void createPaymentRecordIfMissing(UserAccount org, Event ev, String bookingRef, String payeeName, String payeeType, BigDecimal total, BigDecimal advance) {
        boolean exists = paymentRepository.findByOrganizerOrderByIdDesc(org).stream()
                .anyMatch(p -> bookingRef.equals(p.getBookingReference()));
        if (exists) return;
        EventPaymentRecord rec = new EventPaymentRecord();
        rec.setOrganizer(org);
        rec.setEvent(ev);
        rec.setBookingReference(bookingRef);
        rec.setPayeeName(payeeName);
        rec.setPayeeType(payeeType);
        rec.setBookingAmount(total);
        rec.setAdvanceAmount(advance);
        rec.setPaidAmount(BigDecimal.ZERO);
        rec.setRemainingBalance(total);
        rec.setPaymentStatus("PENDING");
        rec.setContractStatus("SIGNED");
        rec.setPaymentMode("MANUAL_RECORD");
        rec.setCreatedAt(LocalDateTime.now());
        rec.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(rec);
    }

    public Map<String, Object> getReports(UserAccount org) {
        List<Event> events = eventRepository.findByOrganizerOrderByIdDesc(org);
        List<EventExpense> expenses = expenseRepository.findByOrganizerOrderByIdDesc(org);
        List<EventPaymentRecord> payments = paymentRepository.findByOrganizerOrderByIdDesc(org);
        List<VenueBooking> venueBookings = venueBookingRepository.findByRequesterOrderByIdDesc(org);
        List<EventProfessionalBooking> profBookings = profBookingRepository.findByOrganizerOrderByIdDesc(org);

        BigDecimal totalBudget = events.stream().map(Event::getBudget).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalActual = expenses.stream().map(EventExpense::getActualAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("totalEvents", events.size());
        res.put("upcomingEvents", events.stream().filter(e -> "PUBLISHED".equals(e.getStatus())).count());
        res.put("completedEvents", events.stream().filter(e -> "COMPLETED".equals(e.getStatus())).count());
        res.put("cancelledEvents", events.stream().filter(e -> "CANCELLED".equals(e.getStatus())).count());
        res.put("totalBudget", totalBudget);
        res.put("totalActualCost", totalActual);
        res.put("venueBookingsCount", venueBookings.size());
        res.put("professionalBookingsCount", profBookings.size());
        res.put("paymentsCount", payments.size());
        res.put("reportLevel", getPlanLimits(org).reportLevel());
        return res;
    }

    public List<Map<String, Object>> listTeamAccounts(UserAccount org) {
        return orgTeamRepository.findByOrganizerOrderByIdDesc(org).stream().map(this::toOrgTeamMap).toList();
    }

    @Transactional
    public Map<String, Object> addTeamAccount(UserAccount org, Map<String, Object> body) {
        OrganizerPlanLimits limits = getPlanLimits(org);
        long count = orgTeamRepository.countByOrganizer(org);
        if (count >= limits.maxTeamAccounts()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Team account limit reached (" + count + "/" + limits.maxTeamAccounts() + ") on " + limits.planName() + ".");
        }
        OrganizerTeamMember m = new OrganizerTeamMember();
        m.setOrganizer(org);
        m.setFullName(strOrDefault(body.get("fullName"), "Team Member"));
        m.setEmail(strOrDefault(body.get("email"), ""));
        m.setPhone(trimOrNull(str(body.get("phone"))));
        m.setRole(strOrDefault(body.get("role"), "COORDINATOR"));
        m.setPermissions(strOrDefault(body.get("permissions"), "EVENTS,SEARCH,BOOKINGS,BUDGET,SCHEDULE"));
        m.setCreatedAt(LocalDateTime.now());
        return toOrgTeamMap(orgTeamRepository.save(m));
    }

    @Transactional
    public void deleteTeamAccount(UserAccount org, Long id) {
        OrganizerTeamMember m = orgTeamRepository.findByIdAndOrganizer(id, org)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team member not found."));
        orgTeamRepository.delete(m);
    }

    // ==================== WISHLIST WITH PLAN LIMIT ====================

    @Transactional
    public SavedWishlistItem addWishlistWithLimit(UserAccount org, Map<String, Object> body) {
        OrganizerPlanLimits limits = getPlanLimits(org);
        long count = wishlistRepository.findByUserOrderByCreatedAtDesc(org).size();
        if (count >= limits.maxWishlist()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Wishlist limit reached (" + count + "/" + limits.maxWishlist() + ") on " + limits.planName() + ".");
        }
        String itemType = String.valueOf(body.getOrDefault("itemType", "VENUE"));
        Long targetId = Long.valueOf(String.valueOf(body.get("targetId")));
        if (wishlistRepository.existsByUserAndItemTypeAndTargetId(org, itemType, targetId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already saved in your wishlist.");
        }
        SavedWishlistItem item = new SavedWishlistItem();
        item.setUser(org);
        item.setItemType(itemType);
        item.setTargetId(targetId);
        item.setTitle(String.valueOf(body.getOrDefault("title", "Saved Item")));
        item.setSubtitle(body.get("subtitle") == null ? null : String.valueOf(body.get("subtitle")));
        item.setCreatedAt(LocalDateTime.now());
        return wishlistRepository.save(item);
    }

    // ==================== MAPPERS & HELPERS ====================

    private Map<String, Object> toProfileMap(OrganizerProfile p, UserAccount org) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("companyName", p.getCompanyName());
        m.put("aboutUs", p.getAboutUs());
        m.put("bio", p.getBio());
        m.put("specialization", p.getSpecialization());
        m.put("country", p.getCountry());
        m.put("state", p.getState());
        m.put("city", p.getCity());
        m.put("area", p.getArea());
        m.put("address", p.getAddress());
        m.put("contactNumber", p.getContactNumber());
        m.put("email", p.getEmail());
        m.put("website", p.getWebsite());
        m.put("socialLinks", p.getSocialLinks());
        m.put("logoUrl", isNotBlank(p.getLogoFileName()) ? "/api/organizer/media/" + p.getLogoFileName() : null);
        m.put("coverImageUrl", isNotBlank(p.getCoverImageFileName()) ? "/api/organizer/media/" + p.getCoverImageFileName() : null);
        m.put("organizationName", org.getOrganizationName());
        m.put("documentType", org.getDocumentType());
        m.put("documentNumber", org.getDocumentNumber());
        m.put("verificationStatus", org.getVerificationStatus() != null ? org.getVerificationStatus().name() : "APPROVED");
        m.put("documentUrl", isNotBlank(org.getDocumentFileName()) ? "/api/auth/documents/" + org.getDocumentFileName() : null);
        return m;
    }

    private Map<String, Object> toEventMap(Event e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("eventName", e.getEventName());
        m.put("eventType", e.getEventType());
        m.put("description", e.getDescription());
        m.put("country", e.getCountry());
        m.put("state", e.getState());
        m.put("city", e.getCity());
        m.put("area", e.getArea());
        m.put("address", e.getAddress());
        m.put("mapLink", e.getMapLink());
        m.put("eventDate", e.getEventDate().toString());
        m.put("startTime", e.getStartTime());
        m.put("endTime", e.getEndTime());
        m.put("budget", e.getBudget());
        m.put("expectedGuests", e.getExpectedGuests());
        m.put("requirements", e.getRequirements());
        m.put("status", e.getStatus());
        return m;
    }

    private Map<String, Object> toPostMap(EventPost p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("organizerId", p.getOrganizer().getId());
        m.put("organizerName", p.getOrganizer().getFullName());
        m.put("title", p.getTitle());
        m.put("description", p.getDescription());
        m.put("postType", p.getPostType());
        m.put("likesCount", p.getLikesCount());
        m.put("commentsLog", p.getCommentsLog());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }

    private Map<String, Object> toProfBookingMap(EventProfessionalBooking b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("bookingCode", b.getBookingCode());
        m.put("eventId", b.getEvent().getId());
        m.put("eventName", b.getEvent().getEventName());
        m.put("professionalId", b.getProfessional().getId());
        m.put("professionalName", b.getProfessional().getFullName());
        m.put("requiredCategory", b.getRequiredCategory());
        m.put("eventDate", b.getEventDate().toString());
        m.put("startTime", b.getStartTime());
        m.put("endTime", b.getEndTime());
        m.put("eventLocation", b.getEventLocation());
        m.put("offeredFee", b.getOfferedFee());
        m.put("counterOfferFee", b.getCounterOfferFee());
        m.put("requirements", b.getRequirements());
        m.put("message", b.getMessage());
        m.put("status", b.getStatus());
        m.put("historyLog", b.getHistoryLog());
        return m;
    }

    private Map<String, Object> toShortlistMap(ProfessionalShortlist s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("eventId", s.getEvent() != null ? s.getEvent().getId() : null);
        m.put("eventName", s.getEvent() != null ? s.getEvent().getEventName() : "General");
        m.put("professionalId", s.getProfessional().getId());
        m.put("professionalName", s.getProfessional().getFullName());
        m.put("category", s.getProfessional().getProfessionalCategory());
        m.put("city", s.getProfessional().getCity());
        m.put("quotedCharge", s.getQuotedCharge());
        m.put("notes", s.getNotes());
        m.put("preferred", s.isPreferred());
        return m;
    }

    private Map<String, Object> toEventTeamMap(EventTeamMember t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("eventId", t.getEvent().getId());
        m.put("memberType", t.getMemberType());
        m.put("referenceId", t.getReferenceId());
        m.put("memberName", t.getMemberName());
        m.put("assignedRole", t.getAssignedRole());
        m.put("charges", t.getCharges());
        m.put("bookingStatus", t.getBookingStatus());
        return m;
    }

    private Map<String, Object> toExpenseMap(EventExpense e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("eventId", e.getEvent().getId());
        m.put("category", e.getCategory());
        m.put("title", e.getTitle());
        m.put("estimatedAmount", e.getEstimatedAmount());
        m.put("actualAmount", e.getActualAmount());
        m.put("notes", e.getNotes());
        return m;
    }

    private Map<String, Object> toScheduleMap(EventScheduleItem s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("eventId", s.getEvent().getId());
        m.put("eventName", s.getEvent().getEventName());
        m.put("title", s.getTitle());
        m.put("scheduleDate", s.getScheduleDate().toString());
        m.put("startTime", s.getStartTime());
        m.put("endTime", s.getEndTime());
        m.put("assignedProfessionalId", s.getAssignedProfessionalId());
        m.put("assignedProfessionalName", s.getAssignedProfessionalName());
        m.put("description", s.getDescription());
        m.put("status", s.getStatus());
        m.put("sortOrder", s.getSortOrder());
        return m;
    }

    private Map<String, Object> toReviewMap(BookingReview r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("reviewerName", r.getReviewer().getFullName());
        m.put("targetType", r.getTargetType());
        m.put("targetId", r.getTargetId());
        m.put("bookingId", r.getBookingId());
        m.put("rating", r.getRating());
        m.put("title", r.getTitle());
        m.put("description", r.getDescription());
        m.put("createdAt", r.getCreatedAt());
        return m;
    }

    private Map<String, Object> toPaymentMap(EventPaymentRecord p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("eventId", p.getEvent() != null ? p.getEvent().getId() : null);
        m.put("eventName", p.getEvent() != null ? p.getEvent().getEventName() : "General");
        m.put("bookingReference", p.getBookingReference());
        m.put("payeeName", p.getPayeeName());
        m.put("payeeType", p.getPayeeType());
        m.put("bookingAmount", p.getBookingAmount());
        m.put("advanceAmount", p.getAdvanceAmount());
        m.put("paidAmount", p.getPaidAmount());
        m.put("remainingBalance", p.getRemainingBalance());
        m.put("paymentStatus", p.getPaymentStatus());
        m.put("contractStatus", p.getContractStatus());
        m.put("paymentMode", p.getPaymentMode());
        m.put("notes", p.getNotes());
        return m;
    }

    private Map<String, Object> toOrgTeamMap(OrganizerTeamMember t) {
        return Map.of(
                "id", t.getId(),
                "fullName", t.getFullName(),
                "email", t.getEmail(),
                "phone", t.getPhone() == null ? "" : t.getPhone(),
                "role", t.getRole(),
                "permissions", t.getPermissions(),
                "active", t.isActive()
        );
    }

    private boolean matchesProfQuery(UserAccount u, String q) {
        return (u.getFullName() != null && u.getFullName().toLowerCase().contains(q))
                || (u.getProfessionalCategory() != null && u.getProfessionalCategory().toLowerCase().contains(q))
                || (u.getCity() != null && u.getCity().toLowerCase().contains(q))
                || (u.getPortfolioOrBio() != null && u.getPortfolioOrBio().toLowerCase().contains(q));
    }

    private static boolean timesOverlap(String s1, String e1, String s2, String e2) {
        return s1.compareTo(e2) < 0 && s2.compareTo(e1) < 0;
    }

    private static String storeFile(MultipartFile file, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        String original = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String safeName = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
        Files.copy(file.getInputStream(), targetDir.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);
        return safeName;
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
    private static String strOrDefault(Object o, String def) { String s = str(o); return isNotBlank(s) ? s.trim() : def; }
    private static boolean isNotBlank(String s) { return s != null && !s.trim().isEmpty(); }
    private static String trimOrNull(String s) { return isNotBlank(s) ? s.trim() : null; }
    private static Integer toInt(Object o, Integer def) { return (o == null || String.valueOf(o).isBlank()) ? def : Integer.parseInt(String.valueOf(o).trim()); }
    private static Long toLong(Object o) { return (o == null || String.valueOf(o).isBlank()) ? null : Long.parseLong(String.valueOf(o).trim()); }
    private static BigDecimal toDecimal(Object o, BigDecimal def) { return (o == null || String.valueOf(o).isBlank()) ? def : new BigDecimal(String.valueOf(o).trim()); }
    private static boolean toBool(Object o, boolean def) { return o == null ? def : (o instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(o))); }
    private static LocalDate parseDate(Object o) { return (o == null || String.valueOf(o).isBlank()) ? null : LocalDate.parse(String.valueOf(o).trim()); }
}
