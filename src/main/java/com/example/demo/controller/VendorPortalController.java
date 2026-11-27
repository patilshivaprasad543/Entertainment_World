package com.example.demo.controller;

import com.example.demo.dto.VendorDtos.*;
import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.service.SessionAuthService;
import com.example.demo.service.VendorPortalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/api/vendors")
public class VendorPortalController {

    private final SessionAuthService sessionAuthService;
    private final VendorPortalService vendorPortalService;

    public VendorPortalController(SessionAuthService sessionAuthService, VendorPortalService vendorPortalService) {
        this.sessionAuthService = sessionAuthService;
        this.vendorPortalService = vendorPortalService;
    }

    @GetMapping("/dashboard")
    public DashboardStatsDto dashboard(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.getDashboardStats(vendor);
    }

    @GetMapping("/profile")
    public ProfileDto getProfile(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.getOrCreateProfile(vendor);
    }

    @PutMapping("/profile")
    public ProfileDto updateProfile(@RequestBody ProfileUpdateRequest body, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.updateProfile(vendor, body);
    }

    @PostMapping(value = "/profile/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDto uploadLogo(@RequestParam MultipartFile logoFile, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.uploadLogo(vendor, logoFile);
    }

    @GetMapping("/logo/{fileName}")
    public ResponseEntity<Resource> getLogo(@PathVariable String fileName) throws IOException {
        return serveImage(VendorPortalService.LOGO_DIR, fileName);
    }

    @PostMapping(value = "/profile/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDto uploadCoverImage(@RequestParam MultipartFile coverFile, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.uploadCoverImage(vendor, coverFile);
    }

    @GetMapping("/cover/{fileName}")
    public ResponseEntity<Resource> getCoverImage(@PathVariable String fileName) throws IOException {
        return serveImage(VendorPortalService.COVER_DIR, fileName);
    }

    @GetMapping("/services")
    public List<ServiceDto> listServices(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.listVendorServices(vendor);
    }

    @PostMapping(value = "/services", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceDto createService(
            @RequestParam String serviceName,
            @RequestParam String category,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal pricePerDay,
            @RequestParam(required = false) BigDecimal pricePerEvent,
            @RequestParam Integer availableQuantity,
            @RequestParam String location,
            @RequestParam(required = false) MultipartFile imageFile,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.createService(
                vendor, serviceName, category, description, pricePerDay, pricePerEvent, availableQuantity, location, imageFile);
    }

    @PutMapping(value = "/services/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceDto updateService(
            @PathVariable Long id,
            @RequestParam String serviceName,
            @RequestParam String category,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal pricePerDay,
            @RequestParam(required = false) BigDecimal pricePerEvent,
            @RequestParam Integer availableQuantity,
            @RequestParam String location,
            @RequestParam(required = false) MultipartFile imageFile,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.updateService(
                vendor, id, serviceName, category, description, pricePerDay, pricePerEvent, availableQuantity, location, imageFile);
    }

    @PatchMapping("/services/{id}/toggle")
    public ServiceDto toggleService(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.toggleServiceActive(vendor, id);
    }

    @PutMapping("/services/{id}/availability")
    public ServiceDto updateAvailability(
            @PathVariable Long id,
            @RequestBody AvailabilityUpdateRequest body,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.updateAvailability(vendor, id, body);
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteService(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        vendorPortalService.deleteService(vendor, id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    @GetMapping("/services/image/{fileName}")
    public ResponseEntity<Resource> getServiceImage(@PathVariable String fileName) throws IOException {
        return serveImage(VendorPortalService.SERVICE_IMG_DIR, fileName);
    }

    @GetMapping("/bookings")
    public List<BookingDto> listBookings(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.listVendorBookings(vendor);
    }

    @GetMapping("/bookings/{id}")
    public BookingDto getBooking(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.getVendorBooking(vendor, id);
    }

    @PostMapping("/bookings/{id}/accept")
    public BookingDto acceptBooking(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.acceptBooking(vendor, id);
    }

    @PostMapping("/bookings/{id}/reject")
    public BookingDto rejectBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        String reason = body == null ? null : body.get("reason");
        return vendorPortalService.rejectBooking(vendor, id, reason);
    }

    @PutMapping("/bookings/{id}/status")
    public BookingDto updateBookingStatus(
            @PathVariable Long id,
            @RequestBody BookingStatusUpdateRequest body,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.updateBookingStatus(vendor, id, body);
    }

    @GetMapping("/invoices")
    public List<InvoiceDto> listInvoices(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.listVendorInvoices(vendor);
    }

    @PostMapping("/bookings/{bookingId}/invoice")
    public InvoiceDto generateInvoice(@PathVariable Long bookingId, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.generateInvoice(vendor, bookingId);
    }

    @PostMapping("/invoices/{invoiceId}/payments")
    public InvoiceDto recordPayment(
            @PathVariable Long invoiceId,
            @RequestBody PaymentRecordRequest body,
            HttpServletRequest request
    ) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.recordPayment(vendor, invoiceId, body);
    }

    @GetMapping("/invoices/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long invoiceId, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        byte[] pdf = vendorPortalService.generateInvoicePdfForVendor(vendor, invoiceId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + invoiceId + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/notifications")
    public List<NotificationDto> listNotifications(HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        return vendorPortalService.listNotifications(vendor);
    }

    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<Map<String, Boolean>> markRead(@PathVariable Long id, HttpServletRequest request) {
        UserAccount vendor = sessionAuthService.requireUser(request, UserRole.VENDOR);
        vendorPortalService.markNotificationRead(vendor, id);
        return ResponseEntity.ok(Map.of("read", true));
    }

    private ResponseEntity<Resource> serveImage(Path baseDir, String fileName) throws IOException {
        Path filePath = baseDir.resolve(fileName).normalize();
        if (!filePath.startsWith(baseDir) || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(filePath);
        MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(mediaType).body(new UrlResource(filePath.toUri()));
    }
}
