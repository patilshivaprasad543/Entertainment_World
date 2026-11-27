package com.example.demo.controller;

import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.service.CategoryVendorService;
import com.example.demo.service.SessionAuthService;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category-vendors")
public class CategoryVendorController {

    private final SessionAuthService sessionAuthService;
    private final CategoryVendorService categoryVendorService;

    public CategoryVendorController(SessionAuthService sessionAuthService, CategoryVendorService categoryVendorService) {
        this.sessionAuthService = sessionAuthService;
        this.categoryVendorService = categoryVendorService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> register(
            @RequestParam String vendorCategory, @RequestParam String businessName, @RequestParam String contactPerson,
            @RequestParam String email, @RequestParam String phone, @RequestParam String password,
            @RequestParam String confirmPassword, @RequestParam String country, @RequestParam String state,
            @RequestParam String city, @RequestParam String areaLocality, @RequestParam String address,
            @RequestParam String pincode, @RequestParam String mapLink, @RequestParam String aboutBusiness,
            @RequestParam(required = false) String categoryDetailsJson, @RequestParam(required = false) MultipartFile logoFile
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryVendorService.registerCategoryVendor(
                vendorCategory, businessName, contactPerson, email, phone, password, confirmPassword,
                country, state, city, areaLocality, address, pincode, mapLink, aboutBusiness, categoryDetailsJson, logoFile
        ));
    }

    @GetMapping("/logo/{fileName}")
    public ResponseEntity<Resource> getLogo(@PathVariable String fileName) throws IOException {
        return serveFile(CategoryVendorService.CAT_LOGO_DIR, fileName);
    }

    @GetMapping("/documents/file/{fileName}")
    public ResponseEntity<Resource> getDoc(@PathVariable String fileName) throws IOException {
        return serveFile(CategoryVendorService.CAT_DOC_DIR, fileName);
    }

    private ResponseEntity<Resource> serveFile(Path dir, String fileName) throws IOException {
        Path file = dir.resolve(fileName).normalize();
        if (!file.startsWith(dir) || !Files.exists(file)) return ResponseEntity.notFound().build();
        String ct = Files.probeContentType(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, ct != null ? ct : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(new UrlResource(file.toUri()));
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@RequestParam(defaultValue = "30d") String range, HttpServletRequest request) {
        return categoryVendorService.getDashboard(sessionAuthService.requireUser(request, UserRole.VENDOR), range);
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfile(HttpServletRequest request) {
        return categoryVendorService.getProfile(sessionAuthService.requireUser(request, UserRole.VENDOR));
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return categoryVendorService.updateProfile(sessionAuthService.requireUser(request, UserRole.VENDOR), body);
    }

    @PostMapping(value = "/profile/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadMedia(@RequestParam(defaultValue = "LOGO") String kind, @RequestParam MultipartFile file, HttpServletRequest request) {
        return categoryVendorService.uploadProfileImage(sessionAuthService.requireUser(request, UserRole.VENDOR), kind, file);
    }

    @PostMapping("/account/password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        return categoryVendorService.changePassword(sessionAuthService.requireUser(request, UserRole.VENDOR), body.get("currentPassword"), body.get("newPassword"));
    }

    @GetMapping("/services")
    public List<Map<String, Object>> listServices(HttpServletRequest request) {
        return categoryVendorService.listServices(sessionAuthService.requireUser(request, UserRole.VENDOR));
    }

    @PostMapping("/services")
    public ResponseEntity<Map<String, Object>> createService(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryVendorService.saveService(sessionAuthService.requireUser(request, UserRole.VENDOR), null, body));
    }

    @PutMapping("/services/{id}")
    public Map<String, Object> updateService(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        return categoryVendorService.saveService(sessionAuthService.requireUser(request, UserRole.VENDOR), id, body);
    }

    @PatchMapping("/services/{id}/toggle")
    public Map<String, Object> toggleService(@PathVariable Long id, HttpServletRequest request) {
        return categoryVendorService.toggleService(sessionAuthService.requireUser(request, UserRole.VENDOR), id);
    }

    @DeleteMapping("/services/{id}")
    public Map<String, Boolean> deleteService(@PathVariable Long id, HttpServletRequest request) {
        categoryVendorService.deleteService(sessionAuthService.requireUser(request, UserRole.VENDOR), id);
        return Map.of("deleted", true);
    }

    @GetMapping("/bookings")
    public List<Map<String, Object>> listBookings(HttpServletRequest request) {
        return categoryVendorService.listBookings(sessionAuthService.requireUser(request, UserRole.VENDOR));
    }

    @PostMapping("/bookings")
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryVendorService.createBookingOrEnquiry(sessionAuthService.requireUser(request, UserRole.VENDOR), body));
    }

    @PatchMapping("/bookings/{id}")
    public Map<String, Object> updateBooking(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        return categoryVendorService.updateBookingStatus(sessionAuthService.requireUser(request, UserRole.VENDOR), id, body);
    }

    @GetMapping("/quotations")
    public List<Map<String, Object>> listQuotations(HttpServletRequest request) {
        return categoryVendorService.listQuotations(sessionAuthService.requireUser(request, UserRole.VENDOR));
    }

    @PostMapping("/quotations")
    public ResponseEntity<Map<String, Object>> createQuotation(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryVendorService.saveQuotation(sessionAuthService.requireUser(request, UserRole.VENDOR), null, body));
    }

    @GetMapping("/quotations/{id}/pdf")
    public ResponseEntity<byte[]> quotationPdf(@PathVariable Long id, HttpServletRequest request) {
        byte[] pdf = categoryVendorService.generateQuotationPdf(sessionAuthService.requireUser(request, UserRole.VENDOR), id);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quotation-" + id + ".pdf\"").body(pdf);
    }

    @GetMapping("/projects")
    public List<Map<String, Object>> listProjects(HttpServletRequest request) {
        return categoryVendorService.listProjects(sessionAuthService.requireUser(request, UserRole.VENDOR));
    }

    @PostMapping("/projects")
    public ResponseEntity<Map<String, Object>> createProject(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryVendorService.saveProject(sessionAuthService.requireUser(request, UserRole.VENDOR), null, body));
    }

    @PutMapping("/projects/{id}")
    public Map<String, Object> updateProject(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        return categoryVendorService.saveProject(sessionAuthService.requireUser(request, UserRole.VENDOR), id, body);
    }

    @GetMapping("/invoices")
    public List<Map<String, Object>> listInvoices(HttpServletRequest request) {
        return categoryVendorService.listInvoices(sessionAuthService.requireUser(request, UserRole.VENDOR));
    }

    @PostMapping("/invoices")
    public ResponseEntity<Map<String, Object>> createInvoice(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryVendorService.saveInvoice(sessionAuthService.requireUser(request, UserRole.VENDOR), null, body));
    }

    @GetMapping("/invoices/{id}/pdf")
    public ResponseEntity<byte[]> invoicePdf(@PathVariable Long id, HttpServletRequest request) {
        byte[] pdf = categoryVendorService.generateInvoicePdf(sessionAuthService.requireUser(request, UserRole.VENDOR), id);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + id + ".pdf\"").body(pdf);
    }

    @GetMapping("/documents")
    public List<Map<String, Object>> listDocuments(HttpServletRequest request) {
        return categoryVendorService.listDocuments(sessionAuthService.requireUser(request, UserRole.VENDOR));
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam String title, @RequestParam String docType,
            @RequestParam(required = false) String expiryDate, @RequestParam MultipartFile file, HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryVendorService.uploadDocument(
                sessionAuthService.requireUser(request, UserRole.VENDOR), title, docType, expiryDate, file));
    }

    @DeleteMapping("/documents/{id}")
    public Map<String, Boolean> deleteDocument(@PathVariable Long id, HttpServletRequest request) {
        categoryVendorService.deleteDocument(sessionAuthService.requireUser(request, UserRole.VENDOR), id);
        return Map.of("deleted", true);
    }

    @GetMapping("/team")
    public List<Map<String, Object>> listTeam(HttpServletRequest request) {
        return categoryVendorService.listTeam(sessionAuthService.requireUser(request, UserRole.VENDOR));
    }

    @PostMapping("/team")
    public ResponseEntity<Map<String, Object>> addTeam(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryVendorService.saveTeamMember(sessionAuthService.requireUser(request, UserRole.VENDOR), body));
    }

    @DeleteMapping("/team/{id}")
    public Map<String, Boolean> deleteTeam(@PathVariable Long id, HttpServletRequest request) {
        categoryVendorService.deleteTeamMember(sessionAuthService.requireUser(request, UserRole.VENDOR), id);
        return Map.of("deleted", true);
    }

    @GetMapping("/clients")
    public List<Map<String, Object>> listClients(HttpServletRequest request) {
        return categoryVendorService.listClients(sessionAuthService.requireUser(request, UserRole.VENDOR));
    }

    @PostMapping("/clients")
    public ResponseEntity<Map<String, Object>> saveClient(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryVendorService.saveClient(sessionAuthService.requireUser(request, UserRole.VENDOR), body));
    }

    @GetMapping("/availability")
    public List<Map<String, Object>> listAvailability(HttpServletRequest request) {
        return categoryVendorService.listAvailability(sessionAuthService.requireUser(request, UserRole.VENDOR));
    }

    @PutMapping("/availability")
    public Map<String, Object> setAvailability(@RequestBody Map<String, String> body, HttpServletRequest request) {
        return categoryVendorService.setAvailability(sessionAuthService.requireUser(request, UserRole.VENDOR),
                LocalDate.parse(body.get("date")), body.get("status"), body.get("note"));
    }
}
