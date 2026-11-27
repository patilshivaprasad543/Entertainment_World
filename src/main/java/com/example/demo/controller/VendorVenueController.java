package com.example.demo.controller;

import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.service.SessionAuthService;
import com.example.demo.service.SubscriptionService;
import com.example.demo.service.VenueManagementService;
import com.example.demo.service.VendorPortalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor")
public class VendorVenueController {

    private final SessionAuthService sessionAuthService;
    private final VenueManagementService venueService;
    private final VendorPortalService vendorPortalService;
    private final SubscriptionService subscriptionService;

    public VendorVenueController(
            SessionAuthService sessionAuthService,
            VenueManagementService venueService,
            VendorPortalService vendorPortalService,
            SubscriptionService subscriptionService
    ) {
        this.sessionAuthService = sessionAuthService;
        this.venueService = venueService;
        this.vendorPortalService = vendorPortalService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.getDashboardOverview(vendor);
    }

    @GetMapping("/profile")
    public Object getProfile(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.getOrCreateProfile(vendor);
    }

    // ==================== VENUES ====================

    @GetMapping("/venues")
    public List<Map<String, Object>> listVenues(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.listVendorVenues(vendor);
    }

    @GetMapping("/venues/{id}")
    public Map<String, Object> getVenue(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.getVendorVenue(vendor, id);
    }

    @PostMapping("/venues")
    public ResponseEntity<Map<String, Object>> createVenue(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createVenue(vendor, body));
    }

    @PutMapping("/venues/{id}")
    public Map<String, Object> updateVenue(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.updateVenue(vendor, id, body);
    }

    @PatchMapping("/venues/{id}/status")
    public Map<String, Object> toggleVenueStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return venueService.toggleVenueStatus(vendor, id, active);
    }

    @DeleteMapping("/venues/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteVenue(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        venueService.deleteVenue(vendor, id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ==================== PRICING ====================

    @PutMapping("/venues/{id}/pricing")
    public Map<String, Object> updatePricing(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.updateVenuePricing(vendor, id, body);
    }

    // ==================== MEDIA & 360 TOUR ====================

    @GetMapping("/venues/{id}/media")
    public List<Map<String, Object>> listMedia(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.listVenueMedia(vendor, id);
    }

    @PostMapping(value = "/venues/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadMedia(
            @PathVariable Long id,
            @RequestParam(defaultValue = "PHOTO") String mediaType,
            @RequestParam(required = false) String title,
            @RequestParam MultipartFile file,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(venueService.uploadVenueMedia(vendor, id, mediaType, title, file));
    }

    @PatchMapping("/venues/{id}/media/{mediaId}/cover")
    public Map<String, Object> setCover(@PathVariable Long id, @PathVariable Long mediaId, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.setCoverMedia(vendor, id, mediaId);
    }

    @DeleteMapping("/venues/media/{mediaId}")
    public ResponseEntity<Map<String, Boolean>> deleteMedia(@PathVariable Long mediaId, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        venueService.deleteVenueMedia(vendor, mediaId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    @GetMapping("/tours-360")
    public List<Map<String, Object>> list360Tours(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.listAll360Tours(vendor);
    }

    // ==================== AVAILABILITY CALENDAR ====================

    @GetMapping("/venues/{id}/availability")
    public List<Map<String, Object>> getAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            HttpServletRequest request
    ) {
        sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.getVenueAvailability(id, start, end);
    }

    @PutMapping("/venues/{id}/availability")
    public Map<String, Object> setAvailability(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        LocalDate date = LocalDate.parse(body.get("date"));
        return venueService.setVenueDateStatus(vendor, id, date, body.get("status"), body.get("note"));
    }

    // ==================== ENQUIRIES ====================

    @GetMapping("/enquiries")
    public List<Map<String, Object>> listEnquiries(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.listVendorEnquiries(vendor);
    }

    @PatchMapping("/enquiries/{id}")
    public Map<String, Object> updateEnquiryStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.updateEnquiryStatus(vendor, id, body.get("status"));
    }

    // ==================== QUOTATIONS ====================

    @GetMapping("/quotations")
    public List<Map<String, Object>> listQuotations(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.listVendorQuotations(vendor);
    }

    @PostMapping("/quotations")
    public ResponseEntity<Map<String, Object>> createQuotation(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.saveQuotation(vendor, null, body));
    }

    @PutMapping("/quotations/{id}")
    public Map<String, Object> updateQuotation(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.saveQuotation(vendor, id, body);
    }

    @PatchMapping("/quotations/{id}/status")
    public Map<String, Object> updateQuotationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.updateQuotationStatus(vendor, id, body.get("status"));
    }

    @GetMapping("/quotations/{id}/pdf")
    public ResponseEntity<byte[]> downloadQuotationPdf(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        byte[] pdf = venueService.generateQuotationPdf(vendor, id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quotation-" + id + ".pdf\"")
                .body(pdf);
    }

    // ==================== VENUE BOOKINGS ====================

    @GetMapping("/bookings")
    public List<Map<String, Object>> listBookings(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.listVendorBookings(vendor);
    }

    @PostMapping("/bookings")
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createBooking(vendor, body));
    }

    @PatchMapping("/bookings/{id}")
    public Map<String, Object> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.updateBookingStatus(vendor, id, body.get("status"), body.get("note"));
    }

    // ==================== MULTIPLE LOCATIONS ====================

    @GetMapping("/locations")
    public List<Map<String, Object>> listLocations(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.listLocations(vendor);
    }

    @PostMapping("/locations")
    public ResponseEntity<Map<String, Object>> createLocation(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.saveLocation(vendor, null, body));
    }

    @PutMapping("/locations/{id}")
    public Map<String, Object> updateLocation(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.saveLocation(vendor, id, body);
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteLocation(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        venueService.deleteLocation(vendor, id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ==================== TEAM MANAGEMENT ====================

    @GetMapping("/team")
    public List<Map<String, Object>> listTeam(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.listTeamMembers(vendor);
    }

    @PostMapping("/team")
    public ResponseEntity<Map<String, Object>> addTeamMember(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.saveTeamMember(vendor, null, body));
    }

    @PutMapping("/team/{id}")
    public Map<String, Object> updateTeamMember(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.saveTeamMember(vendor, id, body);
    }

    @PatchMapping("/team/{id}/toggle")
    public Map<String, Object> toggleTeamMember(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.toggleTeamMember(vendor, id);
    }

    @DeleteMapping("/team/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteTeamMember(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        venueService.deleteTeamMember(vendor, id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ==================== ANALYTICS & SUBSCRIPTIONS ====================

    @GetMapping("/analytics")
    public Map<String, Object> analytics(
            @RequestParam(defaultValue = "30d") String range,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.getAnalytics(vendor, range, from, to);
    }

    @GetMapping("/subscriptions")
    public Map<String, Object> subscriptions(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        Map<String, Object> data = subscriptionService.getMySubscriptionDashboard(vendor);
        data.put("limits", venueService.getVendorPlanLimits(vendor));
        return data;
    }

    @GetMapping("/notifications")
    public Object notifications(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.listNotifications(vendor);
    }

    @GetMapping("/reviews")
    public List<Map<String, Object>> reviews(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return venueService.listVendorReviews(vendor);
    }
}
