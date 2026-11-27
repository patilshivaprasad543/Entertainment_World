package com.example.demo.controller;

import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.service.SessionAuthService;
import com.example.demo.service.VenueManagementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/venues")
public class PublicVenueController {

    private final VenueManagementService venueService;
    private final SessionAuthService sessionAuthService;

    public PublicVenueController(VenueManagementService venueService, SessionAuthService sessionAuthService) {
        this.venueService = venueService;
        this.sessionAuthService = sessionAuthService;
    }

    @GetMapping
    public List<Map<String, Object>> listAll(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String venueType,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) String query
    ) {
        return venueService.searchPublicVenues(city, venueType, guests, maxBudget, query);
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String venueType,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) String query
    ) {
        return venueService.searchPublicVenues(city, venueType, guests, maxBudget, query);
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        return venueService.getPublicVenueDetail(id);
    }

    @GetMapping("/{id}/availability")
    public List<Map<String, Object>> availability(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) {
        return venueService.getVenueAvailability(id, start, end);
    }

    @PostMapping("/{id}/enquiries")
    public ResponseEntity<Map<String, Object>> sendEnquiry(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        UserAccount requester = sessionAuthService.getCurrentUser(request).orElse(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(venueService.submitPublicEnquiry(id, requester, body));
    }

    @GetMapping("/my-enquiries")
    public List<Map<String, Object>> myEnquiries(HttpServletRequest request) {
        UserAccount requester = sessionAuthService.requireUser(request,
                UserRole.EVENT_MANAGEMENT, UserRole.CASTING, UserRole.BRAND, UserRole.ACTOR, UserRole.ADMIN, UserRole.VENDOR);
        return venueService.listRequesterEnquiries(requester);
    }

    @GetMapping("/media/{fileName}")
    public ResponseEntity<Resource> getMedia(@PathVariable String fileName) throws IOException {
        Path file = VenueManagementService.VENUE_MEDIA_DIR.resolve(fileName).normalize();
        if (!file.startsWith(VenueManagementService.VENUE_MEDIA_DIR) || !Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(file);
        if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(new UrlResource(file.toUri()));
    }
}
