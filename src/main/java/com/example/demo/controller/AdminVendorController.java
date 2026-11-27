package com.example.demo.controller;

import com.example.demo.dto.VendorDtos.ServiceDto;
import com.example.demo.model.UserRole;
import com.example.demo.service.SessionAuthService;
import com.example.demo.service.VendorPortalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vendors")
public class AdminVendorController {

    private final SessionAuthService sessionAuthService;
    private final VendorPortalService vendorPortalService;
    private final com.example.demo.repository.VenueRepository venueRepository;

    public AdminVendorController(
            SessionAuthService sessionAuthService,
            VendorPortalService vendorPortalService,
            com.example.demo.repository.VenueRepository venueRepository
    ) {
        this.sessionAuthService = sessionAuthService;
        this.vendorPortalService = vendorPortalService;
        this.venueRepository = venueRepository;
    }

    @GetMapping
    public List<Map<String, Object>> listVendors(HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return vendorPortalService.listAllVendorsForAdmin();
    }

    @GetMapping("/statistics")
    public Map<String, Object> statistics(HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return vendorPortalService.getAdminVendorStatistics();
    }

    @PostMapping("/{id}/suspend")
    public Map<String, Object> suspendVendor(@PathVariable Long id, HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return vendorPortalService.setVendorSuspended(id, true);
    }

    @PostMapping("/{id}/reactivate")
    public Map<String, Object> reactivateVendor(@PathVariable Long id, HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return vendorPortalService.setVendorSuspended(id, false);
    }

    @GetMapping("/services")
    public List<ServiceDto> listAllServices(HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return vendorPortalService.listAllServicesForAdmin();
    }

    @PostMapping("/services/{serviceId}/flag")
    public ServiceDto flagService(
            @PathVariable Long serviceId,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request
    ) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        String reason = body == null ? "Flagged by Admin" : body.getOrDefault("reason", "Flagged by Admin");
        return vendorPortalService.flagService(serviceId, true, reason);
    }

    @PostMapping("/services/{serviceId}/unflag")
    public ServiceDto unflagService(@PathVariable Long serviceId, HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return vendorPortalService.flagService(serviceId, false, null);
    }

    @GetMapping("/venues")
    public List<Map<String, Object>> listAllVenues(HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return venueRepository.findAll().stream().map(v -> Map.<String, Object>of(
                "id", v.getId(),
                "venueName", v.getVenueName(),
                "venueType", v.getVenueType(),
                "city", v.getCity(),
                "pricePerDay", v.getPricePerDay(),
                "vendorEmail", v.getVendor().getEmail(),
                "active", v.isActive(),
                "flagged", v.isFlagged()
        )).toList();
    }
}
