package com.example.demo.controller;

import com.example.demo.dto.VendorDtos.*;
import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.service.SessionAuthService;
import com.example.demo.service.VendorPortalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MarketplaceBookingController {

    private final SessionAuthService sessionAuthService;
    private final VendorPortalService vendorPortalService;

    public MarketplaceBookingController(SessionAuthService sessionAuthService, VendorPortalService vendorPortalService) {
        this.sessionAuthService = sessionAuthService;
        this.vendorPortalService = vendorPortalService;
    }

    @GetMapping("/vendor-services")
    public List<ServiceDto> browseServices(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String location,
            HttpServletRequest request
    ) {
        sessionAuthService.requireUser(request,
                UserRole.EVENT_MANAGEMENT, UserRole.CASTING, UserRole.BRAND, UserRole.ACTOR, UserRole.ADMIN, UserRole.VENDOR);
        return vendorPortalService.browseApprovedServices(query, category, maxPrice, location);
    }

    @GetMapping("/vendor-services/{id}")
    public ServiceDto getService(@PathVariable Long id, HttpServletRequest request) {
        sessionAuthService.requireUser(request,
                UserRole.EVENT_MANAGEMENT, UserRole.CASTING, UserRole.BRAND, UserRole.ACTOR, UserRole.ADMIN, UserRole.VENDOR);
        return vendorPortalService.getPublicService(id);
    }

    @PostMapping("/vendor-bookings")
    public ResponseEntity<BookingDto> createBooking(@RequestBody BookingCreateRequest body, HttpServletRequest request) {
        UserAccount requester = sessionAuthService.requireUser(request,
                UserRole.EVENT_MANAGEMENT, UserRole.CASTING, UserRole.BRAND);
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorPortalService.createBookingRequest(requester, body));
    }

    @GetMapping("/vendor-bookings/my-requests")
    public List<BookingDto> myRequests(HttpServletRequest request) {
        UserAccount requester = sessionAuthService.requireUser(request,
                UserRole.EVENT_MANAGEMENT, UserRole.CASTING, UserRole.BRAND);
        return vendorPortalService.listRequesterBookings(requester);
    }

    @GetMapping("/vendor-bookings/{id}")
    public BookingDto getMyBooking(@PathVariable Long id, HttpServletRequest request) {
        UserAccount requester = sessionAuthService.requireUser(request,
                UserRole.EVENT_MANAGEMENT, UserRole.CASTING, UserRole.BRAND);
        return vendorPortalService.getRequesterBooking(requester, id);
    }

    @GetMapping("/vendor-bookings/{id}/invoice/pdf")
    public ResponseEntity<byte[]> downloadMyInvoicePdf(@PathVariable Long id, HttpServletRequest request) {
        UserAccount requester = sessionAuthService.requireUser(request,
                UserRole.EVENT_MANAGEMENT, UserRole.CASTING, UserRole.BRAND);
        byte[] pdf = vendorPortalService.generateInvoicePdfForRequester(requester, id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"booking-" + id + "-invoice.pdf\"")
                .body(pdf);
    }

    @GetMapping("/module-notifications")
    public List<NotificationDto> moduleNotifications(HttpServletRequest request) {
        UserAccount user = sessionAuthService.requireUser(request,
                UserRole.EVENT_MANAGEMENT, UserRole.CASTING, UserRole.BRAND);
        return vendorPortalService.listNotifications(user);
    }

    @PostMapping("/module-notifications/{id}/read")
    public ResponseEntity<Map<String, Boolean>> markModuleNotificationRead(@PathVariable Long id, HttpServletRequest request) {
        UserAccount user = sessionAuthService.requireUser(request,
                UserRole.EVENT_MANAGEMENT, UserRole.CASTING, UserRole.BRAND);
        vendorPortalService.markNotificationRead(user, id);
        return ResponseEntity.ok(Map.of("read", true));
    }
}
