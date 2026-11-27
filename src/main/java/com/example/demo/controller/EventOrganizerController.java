package com.example.demo.controller;

import com.example.demo.model.SavedWishlistItem;
import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.service.EventManagementService;
import com.example.demo.service.SessionAuthService;
import com.example.demo.service.SubscriptionService;
import com.example.demo.service.VendorPortalService;
import com.example.demo.service.VenueManagementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EventOrganizerController {

    private final SessionAuthService sessionAuthService;
    private final EventManagementService eventService;
    private final VenueManagementService venueService;
    private final VendorPortalService vendorPortalService;
    private final SubscriptionService subscriptionService;

    public EventOrganizerController(
            SessionAuthService sessionAuthService,
            EventManagementService eventService,
            VenueManagementService venueService,
            VendorPortalService vendorPortalService,
            SubscriptionService subscriptionService
    ) {
        this.sessionAuthService = sessionAuthService;
        this.eventService = eventService;
        this.venueService = venueService;
        this.vendorPortalService = vendorPortalService;
        this.subscriptionService = subscriptionService;
    }

    // ==================== DASHBOARD & PROFILE ====================

    @GetMapping("/organizer/dashboard")
    public Map<String, Object> dashboard(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.getDashboardHome(org);
    }

    @GetMapping("/organizer/profile")
    public Map<String, Object> getProfile(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.getProfile(org);
    }

    @PutMapping("/organizer/profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.updateProfile(org, body);
    }

    @PostMapping(value = "/organizer/profile/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadMedia(
            @RequestParam(defaultValue = "LOGO") String kind,
            @RequestParam MultipartFile file,
            HttpServletRequest request
    ) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.uploadProfileMedia(org, kind, file);
    }

    @GetMapping("/organizer/media/{fileName}")
    public ResponseEntity<Resource> serveMedia(@PathVariable String fileName) throws IOException {
        Path file = EventManagementService.ORG_MEDIA_DIR.resolve(fileName).normalize();
        if (!file.startsWith(EventManagementService.ORG_MEDIA_DIR) || !Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        String ct = Files.probeContentType(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, ct != null ? ct : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(new UrlResource(file.toUri()));
    }

    // ==================== EVENT POSTS ====================

    @GetMapping("/organizer/posts")
    public List<Map<String, Object>> listPosts(HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.listPosts();
    }

    @PostMapping("/organizer/posts")
    public ResponseEntity<Map<String, Object>> createPost(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createPost(org, body));
    }

    @PostMapping("/organizer/posts/{id}/like")
    public Map<String, Object> likePost(@PathVariable Long id, HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.likePost(id);
    }

    @PostMapping("/organizer/posts/{id}/comment")
    public Map<String, Object> commentPost(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.commentPost(org, id, body.get("comment"));
    }

    @DeleteMapping("/organizer/posts/{id}")
    public Map<String, Boolean> deletePost(@PathVariable Long id, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        eventService.deletePost(org, id);
        return Map.of("deleted", true);
    }

    // ==================== EVENTS CRUD ====================

    @GetMapping("/events")
    public List<Map<String, Object>> listEvents(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.listEvents(org);
    }

    @GetMapping("/events/{id}")
    public Map<String, Object> eventDetails(@PathVariable Long id, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.getEventFullDetails(org, id);
    }

    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> createEvent(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.saveEvent(org, null, body));
    }

    @PutMapping("/events/{id}")
    public Map<String, Object> updateEvent(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.saveEvent(org, id, body);
    }

    @PatchMapping("/events/{id}/status")
    public Map<String, Object> updateEventStatus(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.updateEventStatus(org, id, body.get("status"));
    }

    @DeleteMapping("/events/{id}")
    public Map<String, Boolean> deleteEvent(@PathVariable Long id, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        eventService.deleteDraftEvent(org, id);
        return Map.of("deleted", true);
    }

    // ==================== PROFESSIONALS, SHORTLIST & BOOKINGS ====================

    @GetMapping("/organizer/professionals")
    public List<Map<String, Object>> searchProfessionals(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String query,
            HttpServletRequest request
    ) {
        sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.searchProfessionals(category, city, query);
    }

    @GetMapping("/organizer/shortlist")
    public List<Map<String, Object>> listShortlist(@RequestParam(required = false) Long eventId, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.listShortlist(org, eventId);
    }

    @PostMapping("/organizer/shortlist")
    public ResponseEntity<Map<String, Object>> addShortlist(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.addToShortlist(org, body));
    }

    @DeleteMapping("/organizer/shortlist/{id}")
    public Map<String, Boolean> removeShortlist(@PathVariable Long id, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        eventService.removeShortlist(org, id);
        return Map.of("deleted", true);
    }

    @GetMapping("/organizer/bookings")
    public List<Map<String, Object>> listProfBookings(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.listProfBookings(org);
    }

    @PostMapping("/organizer/bookings")
    public ResponseEntity<Map<String, Object>> createProfBooking(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createProfBooking(org, body));
    }

    @PatchMapping("/organizer/bookings/{id}")
    public Map<String, Object> updateProfBooking(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.updateProfBookingStatus(org, id, body);
    }

    // ==================== VENUE SEARCH, ENQUIRY, QUOTATION & BOOKING INTEGRATION ====================

    @GetMapping("/organizer/venues/search")
    public List<Map<String, Object>> searchVenues(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String venueType,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String venueCategory,
            HttpServletRequest request
    ) {
        sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        List<Map<String, Object>> res = venueService.searchPublicVenues(city, venueType, guests, maxBudget, query);
        if (venueCategory != null && !venueCategory.isBlank()) {
            return res.stream().filter(v -> venueCategory.equalsIgnoreCase(String.valueOf(v.get("venueCategory")))).toList();
        }
        return res;
    }

    @PostMapping("/organizer/venues/{venueId}/enquiries")
    public ResponseEntity<Map<String, Object>> sendVenueEnquiry(
            @PathVariable Long venueId,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.submitPublicEnquiry(venueId, org, body));
    }

    @GetMapping("/organizer/venues/enquiries")
    public List<Map<String, Object>> myVenueEnquiries(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return venueService.listRequesterEnquiries(org);
    }

    @GetMapping("/organizer/venues/quotations")
    public List<Map<String, Object>> myVenueQuotations(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return venueService.listOrganizerQuotations(org);
    }

    @PostMapping("/organizer/venues/quotations/{id}/respond")
    public Map<String, Object> respondToQuotation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        String action = body.get("action") == null ? "" : String.valueOf(body.get("action"));
        BigDecimal counter = body.get("counterOfferAmount") == null || String.valueOf(body.get("counterOfferAmount")).isBlank()
                ? null : new BigDecimal(String.valueOf(body.get("counterOfferAmount")));
        String note = body.get("note") == null ? null : String.valueOf(body.get("note"));
        return venueService.respondToVenueQuotation(org, id, action, counter, note);
    }

    @GetMapping("/organizer/venues/bookings")
    public List<Map<String, Object>> myVenueBookings(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return venueService.listOrganizerBookings(org);
    }

    @PostMapping("/organizer/venues/bookings/{id}/confirm")
    public Map<String, Object> confirmVenueBooking(@PathVariable Long id, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return venueService.confirmVenueBookingByOrganizer(org, id);
    }

    // ==================== WISHLIST, TEAM BUILDER, BUDGET, SCHEDULE ====================

    @PostMapping("/organizer/wishlist")
    public SavedWishlistItem addWishlist(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.addWishlistWithLimit(org, body);
    }

    @GetMapping("/organizer/team/{eventId}")
    public List<Map<String, Object>> listEventTeam(@PathVariable Long eventId, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.listEventTeam(org, eventId);
    }

    @PostMapping("/organizer/team/{eventId}")
    public ResponseEntity<Map<String, Object>> addEventTeam(@PathVariable Long eventId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.addEventTeamMember(org, eventId, body));
    }

    @DeleteMapping("/organizer/team/member/{id}")
    public Map<String, Boolean> removeEventTeam(@PathVariable Long id, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        eventService.removeEventTeamMember(org, id);
        return Map.of("deleted", true);
    }

    @GetMapping("/organizer/budget/{eventId}")
    public Map<String, Object> getBudget(@PathVariable Long eventId, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.getBudgetPlanner(org, eventId);
    }

    @PostMapping("/organizer/budget/{eventId}")
    public ResponseEntity<Map<String, Object>> addExpense(@PathVariable Long eventId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.saveExpense(org, eventId, null, body));
    }

    @DeleteMapping("/organizer/budget/expense/{id}")
    public Map<String, Boolean> deleteExpense(@PathVariable Long id, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        eventService.deleteExpense(org, id);
        return Map.of("deleted", true);
    }

    @GetMapping("/organizer/schedule/{eventId}")
    public List<Map<String, Object>> getSchedule(@PathVariable Long eventId, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.listSchedule(org, eventId);
    }

    @PostMapping("/organizer/schedule/{eventId}")
    public ResponseEntity<Map<String, Object>> addSchedule(@PathVariable Long eventId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.saveScheduleItem(org, eventId, null, body));
    }

    @DeleteMapping("/organizer/schedule/item/{id}")
    public Map<String, Boolean> deleteSchedule(@PathVariable Long id, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        eventService.deleteScheduleItem(org, id);
        return Map.of("deleted", true);
    }

    // ==================== REVIEWS, PAYMENTS, REPORTS, TEAM ACCOUNTS, NOTIFICATIONS ====================

    @GetMapping("/organizer/reviews")
    public List<Map<String, Object>> listReviews(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.listMyReviews(org);
    }

    @PostMapping("/organizer/reviews")
    public ResponseEntity<Map<String, Object>> submitReview(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.submitReview(org, body));
    }

    @GetMapping("/organizer/payments")
    public List<Map<String, Object>> listPayments(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.listPayments(org);
    }

    @PostMapping("/organizer/payments")
    public Map<String, Object> recordPayment(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.recordPayment(org, body);
    }

    @GetMapping("/organizer/reports")
    public Map<String, Object> reports(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.getReports(org);
    }

    @GetMapping("/organizer/subscription")
    public Map<String, Object> subscription(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        Map<String, Object> data = subscriptionService.getMySubscriptionDashboard(org);
        data.put("limits", eventService.getPlanLimits(org));
        return data;
    }

    @GetMapping("/organizer/team-accounts")
    public List<Map<String, Object>> listTeamAccounts(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return eventService.listTeamAccounts(org);
    }

    @PostMapping("/organizer/team-accounts")
    public ResponseEntity<Map<String, Object>> addTeamAccount(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.addTeamAccount(org, body));
    }

    @DeleteMapping("/organizer/team-accounts/{id}")
    public Map<String, Boolean> deleteTeamAccount(@PathVariable Long id, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        eventService.deleteTeamAccount(org, id);
        return Map.of("deleted", true);
    }

    @GetMapping("/organizer/notifications")
    public Object notifications(HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        return vendorPortalService.listNotifications(org);
    }

    @PostMapping("/organizer/notifications/{id}/read")
    public Map<String, Boolean> markNotifRead(@PathVariable Long id, HttpServletRequest request) {
        UserAccount org = sessionAuthService.requireUser(request, UserRole.EVENT_MANAGEMENT);
        vendorPortalService.markNotificationRead(org, id);
        return Map.of("read", true);
    }
}
