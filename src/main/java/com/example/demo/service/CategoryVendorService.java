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
import java.util.*;

@Service
public class CategoryVendorService {

    public static final Path CAT_LOGO_DIR = Paths.get("uploads", "category-vendor-logos").toAbsolutePath().normalize();
    public static final Path CAT_DOC_DIR = Paths.get("uploads", "category-vendor-docs").toAbsolutePath().normalize();

    public static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "Production & Camera Services",
            "Video Editing & Post-Production",
            "Audio & Music Production",
            "Lighting & Equipment Rental",
            "Art, Set, Costume & Makeup",
            "Event & Logistics Services",
            "Marketing & Promotion Services",
            "Other Entertainment Services"
    );

    private final UserAccountRepository userAccountRepository;
    private final CategoryVendorProfileRepository profileRepository;
    private final CategoryVendorServiceItemRepository serviceRepository;
    private final CategoryVendorBookingRepository bookingRepository;
    private final CategoryVendorAvailabilityRepository availabilityRepository;
    private final CategoryVendorProjectRepository projectRepository;
    private final CategoryVendorQuotationRepository quotationRepository;
    private final CategoryVendorInvoiceRepository invoiceRepository;
    private final CategoryVendorDocumentRepository documentRepository;
    private final CategoryVendorTeamMemberRepository teamRepository;
    private final CategoryVendorClientRepository clientRepository;
    private final VendorNotificationRepository notificationRepository;
    private final SubscriptionService subscriptionService;

    public CategoryVendorService(
            UserAccountRepository userAccountRepository,
            CategoryVendorProfileRepository profileRepository,
            CategoryVendorServiceItemRepository serviceRepository,
            CategoryVendorBookingRepository bookingRepository,
            CategoryVendorAvailabilityRepository availabilityRepository,
            CategoryVendorProjectRepository projectRepository,
            CategoryVendorQuotationRepository quotationRepository,
            CategoryVendorInvoiceRepository invoiceRepository,
            CategoryVendorDocumentRepository documentRepository,
            CategoryVendorTeamMemberRepository teamRepository,
            CategoryVendorClientRepository clientRepository,
            VendorNotificationRepository notificationRepository,
            SubscriptionService subscriptionService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.profileRepository = profileRepository;
        this.serviceRepository = serviceRepository;
        this.bookingRepository = bookingRepository;
        this.availabilityRepository = availabilityRepository;
        this.projectRepository = projectRepository;
        this.quotationRepository = quotationRepository;
        this.invoiceRepository = invoiceRepository;
        this.documentRepository = documentRepository;
        this.teamRepository = teamRepository;
        this.clientRepository = clientRepository;
        this.notificationRepository = notificationRepository;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    public Map<String, Object> registerCategoryVendor(
            String vendorCategory, String businessName, String contactPerson, String emailRaw,
            String phone, String password, String confirmPassword, String country, String state,
            String city, String areaLocality, String address, String pincode, String mapLink,
            String aboutBusiness, String categoryDetailsJson, MultipartFile logoFile
    ) {
        if (!isNotBlank(vendorCategory) || !ALLOWED_CATEGORIES.contains(vendorCategory.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a valid new vendor category.");
        }
        if (!isNotBlank(businessName) || !isNotBlank(contactPerson) || !isNotBlank(emailRaw) || !isNotBlank(phone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business Name, Contact Person, Email, and Mobile Number are required.");
        }
        String email = emailRaw.trim().toLowerCase();
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid email address.");
        }
        if (password == null || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password and Confirm Password do not match.");
        }
        if (!isNotBlank(country) || !isNotBlank(state) || !isNotBlank(city) || !isNotBlank(areaLocality) || !isNotBlank(address) || !isNotBlank(pincode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All address fields are required.");
        }
        if (!isNotBlank(mapLink) || !mapLink.trim().matches("^https?://.+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid Google Maps Location URL is required.");
        }
        if (!isNotBlank(aboutBusiness)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "About Business is required.");
        }
        if (userAccountRepository.existsByEmailAndRole(email, UserRole.VENDOR)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists in Vendors.");
        }

        UserAccount account = new UserAccount(contactPerson.trim(), email, password, UserRole.VENDOR);
        account.setOrganizationName(businessName.trim());
        account.setPhone(phone.trim());
        account.setCity(city.trim());
        account.setPortfolioOrBio(aboutBusiness.trim().substring(0, Math.min(aboutBusiness.trim().length(), 480)));
        account.setVerificationStatus(VerificationStatus.APPROVED);
        UserAccount savedAccount = userAccountRepository.save(account);

        CategoryVendorProfile profile = new CategoryVendorProfile();
        profile.setUserAccount(savedAccount);
        profile.setVendorCategory(vendorCategory.trim());
        profile.setBusinessName(businessName.trim());
        profile.setContactPerson(contactPerson.trim());
        profile.setEmail(email);
        profile.setPhone(phone.trim());
        profile.setCountry(country.trim());
        profile.setState(state.trim());
        profile.setCity(city.trim());
        profile.setAreaLocality(areaLocality.trim());
        profile.setAddress(address.trim());
        profile.setPincode(pincode.trim());
        profile.setMapLink(mapLink.trim());
        profile.setAboutBusiness(aboutBusiness.trim());
        profile.setCategoryDetailsJson(isNotBlank(categoryDetailsJson) ? categoryDetailsJson : "{}");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                profile.setLogoFileName(storeFile(logoFile, CAT_LOGO_DIR));
            } catch (IOException ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save business logo.");
            }
        }

        CategoryVendorProfile savedProfile = profileRepository.save(profile);
        subscriptionService.ensureActiveOrDefaultFree(savedAccount);

        return Map.of(
                "success", true,
                "message", "Registered successfully under " + savedProfile.getVendorCategory() + "!",
                "email", savedAccount.getEmail(),
                "vendorCategory", savedProfile.getVendorCategory(),
                "redirectUrl", "/dashboards/service-vendor.html"
        );
    }

    public CategoryVendorProfile requireCategoryProfile(UserAccount vendor) {
        return profileRepository.findByUserAccount(vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "This account is not registered under the new Vendor Categories."));
    }

    // ==================== DASHBOARD OVERVIEW ====================

    public Map<String, Object> getDashboard(UserAccount vendor, String range) {
        CategoryVendorProfile profile = requireCategoryProfile(vendor);
        List<CategoryVendorServiceItem> services = serviceRepository.findByVendorOrderByIdDesc(vendor);
        List<CategoryVendorBooking> bookings = bookingRepository.findByVendorOrderByIdDesc(vendor);
        List<CategoryVendorQuotation> quotations = quotationRepository.findByVendorOrderByIdDesc(vendor);
        List<CategoryVendorProject> projects = projectRepository.findByVendorOrderByIdDesc(vendor);
        List<CategoryVendorInvoice> invoices = invoiceRepository.findByVendorOrderByIdDesc(vendor);
        UserSubscription sub = subscriptionService.ensureActiveOrDefaultFree(vendor);

        long newEnq = bookings.stream().filter(b -> "ENQUIRY".equals(b.getStatus())).count();
        long unreadEnq = bookings.stream().filter(b -> "ENQUIRY".equals(b.getStatus()) || "VIEWED".equals(b.getStatus())).count();
        long pendQuot = quotations.stream().filter(q -> "DRAFT".equals(q.getStatus()) || "SENT".equals(q.getStatus())).count();
        long confirmed = bookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus()) || "IN_PROGRESS".equals(b.getStatus())).count();
        long activeProj = projects.stream().filter(p -> !"COMPLETED".equals(p.getStatus())).count();
        long compProj = projects.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();

        BigDecimal monthlyRev = invoices.stream()
                .map(i -> i.getPaidAmount() == null ? BigDecimal.ZERO : i.getPaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingPay = invoices.stream()
                .filter(i -> !"PAID".equals(i.getPaymentStatus()))
                .map(i -> i.getTotalAmount().subtract(i.getPaidAmount() == null ? BigDecimal.ZERO : i.getPaidAmount()).max(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double convRate = bookings.isEmpty() ? 0.0 : BigDecimal.valueOf((confirmed * 100.0) / bookings.size())
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        List<Map<String, Object>> monthlyChart = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            long bCount = bookings.stream().filter(b -> b.getEventDate() != null && YearMonth.from(b.getEventDate()).equals(ym)).count();
            BigDecimal rev = bookings.stream()
                    .filter(b -> b.getEventDate() != null && YearMonth.from(b.getEventDate()).equals(ym)
                            && ("CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus())))
                    .map(b -> b.getQuotedAmount() == null ? BigDecimal.ZERO : b.getQuotedAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyChart.add(Map.of("month", ym.toString(), "bookings", bCount, "revenue", rev));
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("profile", toProfileMap(profile, vendor));
        res.put("profileCompletion", computeProfileCompletion(profile));
        res.put("totalServices", services.size());
        res.put("activeServices", services.stream().filter(CategoryVendorServiceItem::isActive).count());
        res.put("newEnquiries", newEnq);
        res.put("unreadEnquiries", unreadEnq);
        res.put("pendingQuotations", pendQuot);
        res.put("confirmedBookings", confirmed);
        res.put("activeProjects", activeProj);
        res.put("completedProjects", compProj);
        res.put("monthlyRevenue", monthlyRev);
        res.put("pendingPayments", pendingPay);
        res.put("conversionRate", convRate);
        res.put("currentPlan", sub != null && sub.getPlan() != null ? sub.getPlan().getPlanName() : "Vendors Free");
        res.put("monthlyChart", monthlyChart);
        res.put("recentBookings", bookings.stream().limit(6).map(this::toBookingMap).toList());
        res.put("topServices", services.stream().limit(5).map(this::toServiceMap).toList());
        res.put("notifications", notificationRepository.findByRecipientOrderByCreatedAtDesc(vendor).stream().limit(8).map(n -> Map.<String, Object>of(
                "id", n.getId(), "title", n.getTitle(), "message", n.getMessage(), "read", n.isReadFlag(), "createdAt", String.valueOf(n.getCreatedAt())
        )).toList());
        return res;
    }

    // ==================== PROFILE & SETTINGS ====================

    public Map<String, Object> getProfile(UserAccount vendor) {
        CategoryVendorProfile p = requireCategoryProfile(vendor);
        Map<String, Object> m = toProfileMap(p, vendor);
        m.put("profileCompletion", computeProfileCompletion(p));
        return m;
    }

    @Transactional
    public Map<String, Object> updateProfile(UserAccount vendor, Map<String, Object> body) {
        CategoryVendorProfile p = requireCategoryProfile(vendor);
        if (isNotBlank(str(body.get("businessName")))) p.setBusinessName(str(body.get("businessName")).trim());
        if (isNotBlank(str(body.get("contactPerson")))) p.setContactPerson(str(body.get("contactPerson")).trim());
        if (isNotBlank(str(body.get("phone")))) p.setPhone(str(body.get("phone")).trim());
        if (body.containsKey("alternatePhone")) p.setAlternatePhone(str(body.get("alternatePhone")));
        if (body.containsKey("yearsExperience")) p.setYearsExperience(toInt(body.get("yearsExperience"), 0));
        if (isNotBlank(str(body.get("country")))) p.setCountry(str(body.get("country")).trim());
        if (isNotBlank(str(body.get("state")))) p.setState(str(body.get("state")).trim());
        if (isNotBlank(str(body.get("city")))) p.setCity(str(body.get("city")).trim());
        if (isNotBlank(str(body.get("areaLocality")))) p.setAreaLocality(str(body.get("areaLocality")).trim());
        if (isNotBlank(str(body.get("address")))) p.setAddress(str(body.get("address")).trim());
        if (isNotBlank(str(body.get("pincode")))) p.setPincode(str(body.get("pincode")).trim());
        if (isNotBlank(str(body.get("mapLink")))) p.setMapLink(str(body.get("mapLink")).trim());
        if (isNotBlank(str(body.get("aboutBusiness")))) p.setAboutBusiness(str(body.get("aboutBusiness")).trim());
        if (body.containsKey("websiteUrl")) p.setWebsiteUrl(str(body.get("websiteUrl")));
        if (body.containsKey("socialLinks")) p.setSocialLinks(str(body.get("socialLinks")));
        if (body.containsKey("workingHours")) p.setWorkingHours(str(body.get("workingHours")));
        if (body.containsKey("serviceAreas")) p.setServiceAreas(str(body.get("serviceAreas")));
        if (body.containsKey("languagesSupported")) p.setLanguagesSupported(str(body.get("languagesSupported")));
        if (body.containsKey("categoryDetailsJson")) p.setCategoryDetailsJson(str(body.get("categoryDetailsJson")));
        if (body.containsKey("featuredListing")) p.setFeaturedListing(Boolean.parseBoolean(String.valueOf(body.get("featuredListing"))));
        if (body.containsKey("profileVisible")) p.setProfileVisible(Boolean.parseBoolean(String.valueOf(body.get("profileVisible"))));
        if (body.containsKey("notifyNewEnquiry")) p.setNotifyNewEnquiry(Boolean.parseBoolean(String.valueOf(body.get("notifyNewEnquiry"))));
        if (body.containsKey("notifyMessages")) p.setNotifyMessages(Boolean.parseBoolean(String.valueOf(body.get("notifyMessages"))));
        p.setUpdatedAt(LocalDateTime.now());
        Map<String, Object> m = toProfileMap(profileRepository.save(p), vendor);
        m.put("profileCompletion", computeProfileCompletion(p));
        return m;
    }

    @Transactional
    public Map<String, Object> uploadProfileImage(UserAccount vendor, String kind, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select an image file.");
        CategoryVendorProfile p = requireCategoryProfile(vendor);
        try {
            String stored = storeFile(file, CAT_LOGO_DIR);
            if ("COVER".equalsIgnoreCase(kind)) p.setCoverImageFileName(stored);
            else p.setLogoFileName(stored);
            p.setUpdatedAt(LocalDateTime.now());
            return toProfileMap(profileRepository.save(p), vendor);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save image.");
        }
    }

    @Transactional
    public Map<String, Object> changePassword(UserAccount vendor, String currentPass, String newPass) {
        if (!vendor.getPassword().equals(currentPass)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
        }
        if (newPass == null || newPass.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 6 characters.");
        }
        vendor.setPassword(newPass);
        userAccountRepository.save(vendor);
        return Map.of("success", true, "message", "Password updated successfully.");
    }

    // ==================== SERVICES ====================

    public List<Map<String, Object>> listServices(UserAccount vendor) {
        requireCategoryProfile(vendor);
        return serviceRepository.findByVendorOrderByIdDesc(vendor).stream().map(this::toServiceMap).toList();
    }

    @Transactional
    public Map<String, Object> saveService(UserAccount vendor, Long id, Map<String, Object> body) {
        CategoryVendorProfile p = requireCategoryProfile(vendor);
        String name = str(body.get("serviceName"));
        if (!isNotBlank(name)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service Name is required.");

        CategoryVendorServiceItem item = id != null
                ? serviceRepository.findByIdAndVendor(id, vendor).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found."))
                : new CategoryVendorServiceItem();
        item.setVendor(vendor);
        item.setVendorCategory(p.getVendorCategory());
        item.setServiceName(name.trim());
        item.setSubCategory(str(body.get("subCategory")));
        item.setDescription(str(body.get("description")));
        item.setPrice(toDecimal(body.get("price"), BigDecimal.ZERO));
        item.setPricingUnit(isNotBlank(str(body.get("pricingUnit"))) ? str(body.get("pricingUnit")).trim() : "PER_DAY");
        item.setQuantity(toInt(body.get("quantity"), 1));
        item.setLocation(isNotBlank(str(body.get("location"))) ? str(body.get("location")).trim() : p.getCity());
        item.setDetailsJson(str(body.get("detailsJson")));
        item.setPortfolioUrl(str(body.get("portfolioUrl")));
        if (item.getCreatedAt() == null) item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return toServiceMap(serviceRepository.save(item));
    }

    @Transactional
    public Map<String, Object> toggleService(UserAccount vendor, Long id) {
        CategoryVendorServiceItem item = serviceRepository.findByIdAndVendor(id, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found."));
        item.setActive(!item.isActive());
        item.setUpdatedAt(LocalDateTime.now());
        return toServiceMap(serviceRepository.save(item));
    }

    @Transactional
    public void deleteService(UserAccount vendor, Long id) {
        CategoryVendorServiceItem item = serviceRepository.findByIdAndVendor(id, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found."));
        serviceRepository.delete(item);
    }

    // ==================== ENQUIRIES & BOOKINGS ====================

    public List<Map<String, Object>> listBookings(UserAccount vendor) {
        requireCategoryProfile(vendor);
        return bookingRepository.findByVendorOrderByIdDesc(vendor).stream().map(this::toBookingMap).toList();
    }

    @Transactional
    public Map<String, Object> createBookingOrEnquiry(UserAccount vendor, Map<String, Object> body) {
        CategoryVendorProfile p = requireCategoryProfile(vendor);
        CategoryVendorBooking b = new CategoryVendorBooking();
        b.setBookingCode("CVB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        b.setVendor(vendor);
        b.setVendorCategory(p.getVendorCategory());
        Long sid = toLong(body.get("serviceItemId"));
        if (sid != null) serviceRepository.findByIdAndVendor(sid, vendor).ifPresent(b::setServiceItem);
        String custName = isNotBlank(str(body.get("customerName"))) ? str(body.get("customerName")).trim() : "Client";
        String custEmail = isNotBlank(str(body.get("customerEmail"))) ? str(body.get("customerEmail")).trim() : "";
        String custPhone = str(body.get("customerPhone"));
        b.setCustomerName(custName);
        b.setCustomerEmail(custEmail);
        b.setCustomerPhone(custPhone);
        b.setEventName(isNotBlank(str(body.get("eventName"))) ? str(body.get("eventName")).trim() : "Project");
        b.setProjectLocation(str(body.get("projectLocation")));
        LocalDate d = isNotBlank(str(body.get("eventDate"))) ? LocalDate.parse(str(body.get("eventDate")).trim()) : LocalDate.now().plusDays(7);
        b.setEventDate(d);
        if (isNotBlank(str(body.get("endDate")))) b.setEndDate(LocalDate.parse(str(body.get("endDate")).trim()));
        b.setRequirements(str(body.get("requirements")));
        b.setQuotedAmount(toDecimal(body.get("quotedAmount"), BigDecimal.ZERO));
        b.setAdvancePaid(toDecimal(body.get("advancePaid"), BigDecimal.ZERO));
        b.setStatus(isNotBlank(str(body.get("status"))) ? str(body.get("status")).trim().toUpperCase() : "ENQUIRY");
        b.setNotes(str(body.get("notes")));
        b.setCreatedAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        CategoryVendorBooking saved = bookingRepository.save(b);

        // Auto-add client to CRM if not present
        boolean clientExists = clientRepository.findByVendorOrderByIdDesc(vendor).stream()
                .anyMatch(c -> custEmail.equalsIgnoreCase(c.getEmail()));
        if (!clientExists && isNotBlank(custName)) {
            CategoryVendorClient cl = new CategoryVendorClient();
            cl.setVendor(vendor);
            cl.setClientName(custName);
            cl.setEmail(custEmail);
            cl.setPhone(custPhone);
            cl.setTotalBusinessValue(saved.getQuotedAmount());
            cl.setCreatedAt(LocalDateTime.now());
            clientRepository.save(cl);
        }
        return toBookingMap(saved);
    }

    @Transactional
    public Map<String, Object> updateBookingStatus(UserAccount vendor, Long id, Map<String, Object> body) {
        CategoryVendorBooking b = bookingRepository.findByIdAndVendor(id, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        String st = str(body.get("status"));
        if (isNotBlank(st)) {
            String upper = st.trim().toUpperCase();
            if ("CONFIRMED".equals(upper) && b.getServiceItem() != null) {
                long sameDateCount = bookingRepository.findByVendorOrderByIdDesc(vendor).stream()
                        .filter(x -> !x.getId().equals(b.getId()) && x.getServiceItem() != null
                                && x.getServiceItem().getId().equals(b.getServiceItem().getId())
                                && b.getEventDate().equals(x.getEventDate())
                                && ("CONFIRMED".equals(x.getStatus()) || "IN_PROGRESS".equals(x.getStatus())))
                        .count();
                int qty = b.getServiceItem().getQuantity() == null ? 1 : b.getServiceItem().getQuantity();
                if (sameDateCount >= qty) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Inventory/slot limit reached for " + b.getServiceItem().getServiceName() + " on " + b.getEventDate());
                }
            }
            b.setStatus(upper);
        }
        if (body.containsKey("quotedAmount") && body.get("quotedAmount") != null) {
            b.setQuotedAmount(toDecimal(body.get("quotedAmount"), b.getQuotedAmount()));
        }
        if (body.containsKey("vendorReply")) b.setVendorReply(str(body.get("vendorReply")));
        if (body.containsKey("eventDate") && isNotBlank(str(body.get("eventDate")))) {
            b.setEventDate(LocalDate.parse(str(body.get("eventDate")).trim()));
        }
        b.setUpdatedAt(LocalDateTime.now());
        return toBookingMap(bookingRepository.save(b));
    }

    // ==================== QUOTATIONS + PDF ====================

    public List<Map<String, Object>> listQuotations(UserAccount vendor) {
        requireCategoryProfile(vendor);
        return quotationRepository.findByVendorOrderByIdDesc(vendor).stream().map(this::toQuotationMap).toList();
    }

    @Transactional
    public Map<String, Object> saveQuotation(UserAccount vendor, Long id, Map<String, Object> body) {
        requireCategoryProfile(vendor);
        CategoryVendorQuotation q = id != null
                ? quotationRepository.findByIdAndVendor(id, vendor).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quotation not found."))
                : new CategoryVendorQuotation();
        if (q.getQuotationCode() == null) {
            q.setQuotationCode("CVQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            q.setCreatedAt(LocalDateTime.now());
        }
        q.setVendor(vendor);
        q.setBookingId(toLong(body.get("bookingId")));
        q.setCustomerName(isNotBlank(str(body.get("customerName"))) ? str(body.get("customerName")).trim() : "Customer");
        q.setCustomerEmail(str(body.get("customerEmail")));
        q.setServiceName(isNotBlank(str(body.get("serviceName"))) ? str(body.get("serviceName")).trim() : "Service Package");
        int qty = toInt(body.get("quantity"), 1);
        BigDecimal unit = toDecimal(body.get("unitPrice"), BigDecimal.ZERO);
        BigDecimal add = toDecimal(body.get("additionalCharges"), BigDecimal.ZERO);
        BigDecimal disc = toDecimal(body.get("discount"), BigDecimal.ZERO);
        BigDecimal tax = toDecimal(body.get("taxAmount"), BigDecimal.ZERO);
        BigDecimal grand = unit.multiply(BigDecimal.valueOf(qty)).add(add).subtract(disc).add(tax).max(BigDecimal.ZERO);

        q.setQuantity(qty);
        q.setUnitPrice(unit);
        q.setAdditionalCharges(add);
        q.setDiscount(disc);
        q.setTaxAmount(tax);
        q.setGrandTotal(grand);
        if (isNotBlank(str(body.get("validUntil")))) q.setValidUntil(LocalDate.parse(str(body.get("validUntil")).trim()));
        q.setDeliveryTimeline(str(body.get("deliveryTimeline")));
        q.setTermsAndConditions(str(body.get("termsAndConditions")));
        q.setNotes(str(body.get("notes")));
        if (isNotBlank(str(body.get("status")))) q.setStatus(str(body.get("status")).trim().toUpperCase());
        q.setUpdatedAt(LocalDateTime.now());
        return toQuotationMap(quotationRepository.save(q));
    }

    public byte[] generateQuotationPdf(UserAccount vendor, Long id) {
        CategoryVendorQuotation q = quotationRepository.findByIdAndVendor(id, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quotation not found."));
        return SimplePdfGenerator.buildSimplePdf("Entertainment World - Service Quotation", List.of(
                "Quotation Code: " + q.getQuotationCode(),
                "Status: " + q.getStatus(),
                "Customer: " + q.getCustomerName() + " (" + (q.getCustomerEmail() == null ? "" : q.getCustomerEmail()) + ")",
                "Service: " + q.getServiceName() + " x " + q.getQuantity(),
                "Unit Price: INR " + q.getUnitPrice(),
                "Additional Charges: INR " + q.getAdditionalCharges(),
                "Discount: INR " + q.getDiscount(),
                "Tax: INR " + q.getTaxAmount(),
                "Grand Total: INR " + q.getGrandTotal(),
                "Delivery Timeline: " + (q.getDeliveryTimeline() == null ? "Standard" : q.getDeliveryTimeline()),
                "Valid Until: " + (q.getValidUntil() == null ? "N/A" : q.getValidUntil().toString()),
                "Terms: " + (q.getTermsAndConditions() == null ? "Standard terms apply." : q.getTermsAndConditions())
        ));
    }

    // ==================== PROJECTS & TASKS ====================

    public List<Map<String, Object>> listProjects(UserAccount vendor) {
        requireCategoryProfile(vendor);
        return projectRepository.findByVendorOrderByIdDesc(vendor).stream().map(this::toProjectMap).toList();
    }

    @Transactional
    public Map<String, Object> saveProject(UserAccount vendor, Long id, Map<String, Object> body) {
        requireCategoryProfile(vendor);
        CategoryVendorProject pr = id != null
                ? projectRepository.findByIdAndVendor(id, vendor).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."))
                : new CategoryVendorProject();
        if (pr.getProjectCode() == null) {
            pr.setProjectCode("PRJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            pr.setCreatedAt(LocalDateTime.now());
        }
        pr.setVendor(vendor);
        pr.setProjectName(isNotBlank(str(body.get("projectName"))) ? str(body.get("projectName")).trim() : "New Project");
        pr.setClientName(str(body.get("clientName")));
        if (isNotBlank(str(body.get("startDate")))) pr.setStartDate(LocalDate.parse(str(body.get("startDate")).trim()));
        if (isNotBlank(str(body.get("deadline")))) pr.setDeadline(LocalDate.parse(str(body.get("deadline")).trim()));
        pr.setStatus(isNotBlank(str(body.get("status"))) ? str(body.get("status")).trim().toUpperCase() : "TO_DO");
        pr.setProgressPercent(toInt(body.get("progressPercent"), 0));
        pr.setAssignedTeam(str(body.get("assignedTeam")));
        pr.setTasksJson(str(body.get("tasksJson")));
        pr.setDeliverables(str(body.get("deliverables")));
        pr.setNotes(str(body.get("notes")));
        pr.setUpdatedAt(LocalDateTime.now());
        return toProjectMap(projectRepository.save(pr));
    }

    // ==================== INVOICES & PAYMENTS ====================

    public List<Map<String, Object>> listInvoices(UserAccount vendor) {
        requireCategoryProfile(vendor);
        return invoiceRepository.findByVendorOrderByIdDesc(vendor).stream().map(this::toInvoiceMap).toList();
    }

    @Transactional
    public Map<String, Object> saveInvoice(UserAccount vendor, Long id, Map<String, Object> body) {
        requireCategoryProfile(vendor);
        CategoryVendorInvoice inv = id != null
                ? invoiceRepository.findByIdAndVendor(id, vendor).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found."))
                : new CategoryVendorInvoice();
        if (inv.getInvoiceNumber() == null) {
            inv.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            inv.setInvoiceDate(LocalDate.now());
            inv.setCreatedAt(LocalDateTime.now());
        }
        inv.setVendor(vendor);
        inv.setCustomerName(isNotBlank(str(body.get("customerName"))) ? str(body.get("customerName")).trim() : "Client");
        inv.setCustomerEmail(str(body.get("customerEmail")));
        inv.setServiceName(isNotBlank(str(body.get("serviceName"))) ? str(body.get("serviceName")).trim() : "Service");
        BigDecimal sub = toDecimal(body.get("subtotal"), BigDecimal.ZERO);
        BigDecimal tax = toDecimal(body.get("taxAmount"), BigDecimal.ZERO);
        BigDecimal disc = toDecimal(body.get("discount"), BigDecimal.ZERO);
        BigDecimal total = sub.add(tax).subtract(disc).max(BigDecimal.ZERO);
        BigDecimal paid = toDecimal(body.get("paidAmount"), BigDecimal.ZERO);
        inv.setSubtotal(sub);
        inv.setTaxAmount(tax);
        inv.setDiscount(disc);
        inv.setTotalAmount(total);
        inv.setPaidAmount(paid);
        String pSt = str(body.get("paymentStatus"));
        if (!isNotBlank(pSt)) {
            pSt = paid.compareTo(total) >= 0 && total.compareTo(BigDecimal.ZERO) > 0 ? "PAID"
                    : (paid.compareTo(BigDecimal.ZERO) > 0 ? "PARTIALLY_PAID" : "PENDING");
        }
        inv.setPaymentStatus(pSt.toUpperCase());
        inv.setNotes(str(body.get("notes")));
        return toInvoiceMap(invoiceRepository.save(inv));
    }

    public byte[] generateInvoicePdf(UserAccount vendor, Long id) {
        CategoryVendorInvoice inv = invoiceRepository.findByIdAndVendor(id, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found."));
        return SimplePdfGenerator.buildSimplePdf("Entertainment World - Service Invoice", List.of(
                "Invoice Number: " + inv.getInvoiceNumber(),
                "Invoice Date: " + inv.getInvoiceDate(),
                "Customer: " + inv.getCustomerName(),
                "Service: " + inv.getServiceName(),
                "Subtotal: INR " + inv.getSubtotal(),
                "Tax: INR " + inv.getTaxAmount(),
                "Discount: INR " + inv.getDiscount(),
                "Total Amount: INR " + inv.getTotalAmount(),
                "Paid Amount: INR " + inv.getPaidAmount(),
                "Payment Status: " + inv.getPaymentStatus()
        ));
    }

    // ==================== DOCUMENTS, TEAM, CLIENTS, AVAILABILITY ====================

    public List<Map<String, Object>> listDocuments(UserAccount vendor) {
        requireCategoryProfile(vendor);
        return documentRepository.findByVendorOrderByIdDesc(vendor).stream().map(d -> Map.<String, Object>of(
                "id", d.getId(), "title", d.getTitle(), "docType", d.getDocType(),
                "status", d.getStatus(), "expiryDate", d.getExpiryDate() == null ? "" : d.getExpiryDate().toString(),
                "downloadUrl", "/api/category-vendors/documents/file/" + d.getFileName()
        )).toList();
    }

    @Transactional
    public Map<String, Object> uploadDocument(UserAccount vendor, String title, String docType, String expiry, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a document file.");
        requireCategoryProfile(vendor);
        try {
            String stored = storeFile(file, CAT_DOC_DIR);
            CategoryVendorDocument doc = new CategoryVendorDocument();
            doc.setVendor(vendor);
            doc.setTitle(isNotBlank(title) ? title.trim() : file.getOriginalFilename());
            doc.setDocType(isNotBlank(docType) ? docType.trim() : "SERVICE_AGREEMENT");
            doc.setFileName(stored);
            if (isNotBlank(expiry)) doc.setExpiryDate(LocalDate.parse(expiry.trim()));
            doc.setCreatedAt(LocalDateTime.now());
            CategoryVendorDocument saved = documentRepository.save(doc);
            return Map.of("id", saved.getId(), "title", saved.getTitle(), "docType", saved.getDocType(),
                    "downloadUrl", "/api/category-vendors/documents/file/" + saved.getFileName());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload document.");
        }
    }

    @Transactional
    public void deleteDocument(UserAccount vendor, Long id) {
        CategoryVendorDocument doc = documentRepository.findByIdAndVendor(id, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found."));
        documentRepository.delete(doc);
    }

    public List<Map<String, Object>> listTeam(UserAccount vendor) {
        requireCategoryProfile(vendor);
        return teamRepository.findByVendorOrderByIdDesc(vendor).stream().map(t -> Map.<String, Object>of(
                "id", t.getId(), "fullName", t.getFullName(), "role", t.getRole() == null ? "" : t.getRole(),
                "email", t.getEmail() == null ? "" : t.getEmail(), "phone", t.getPhone() == null ? "" : t.getPhone(),
                "skills", t.getSkills() == null ? "" : t.getSkills(), "availability", t.getAvailability(), "active", t.isActive()
        )).toList();
    }

    @Transactional
    public Map<String, Object> saveTeamMember(UserAccount vendor, Map<String, Object> body) {
        requireCategoryProfile(vendor);
        CategoryVendorTeamMember m = new CategoryVendorTeamMember();
        m.setVendor(vendor);
        m.setFullName(isNotBlank(str(body.get("fullName"))) ? str(body.get("fullName")).trim() : "Member");
        m.setRole(str(body.get("role")));
        m.setEmail(str(body.get("email")));
        m.setPhone(str(body.get("phone")));
        m.setSkills(str(body.get("skills")));
        m.setAssignedProjects(str(body.get("assignedProjects")));
        m.setAvailability(isNotBlank(str(body.get("availability"))) ? str(body.get("availability")) : "AVAILABLE");
        m.setCreatedAt(LocalDateTime.now());
        CategoryVendorTeamMember saved = teamRepository.save(m);
        return Map.of("id", saved.getId(), "fullName", saved.getFullName(), "role", saved.getRole() == null ? "" : saved.getRole());
    }

    @Transactional
    public void deleteTeamMember(UserAccount vendor, Long id) {
        CategoryVendorTeamMember m = teamRepository.findByIdAndVendor(id, vendor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team member not found."));
        teamRepository.delete(m);
    }

    public List<Map<String, Object>> listClients(UserAccount vendor) {
        requireCategoryProfile(vendor);
        return clientRepository.findByVendorOrderByIdDesc(vendor).stream().map(c -> Map.<String, Object>of(
                "id", c.getId(), "clientName", c.getClientName(), "companyName", c.getCompanyName() == null ? "" : c.getCompanyName(),
                "email", c.getEmail() == null ? "" : c.getEmail(), "phone", c.getPhone() == null ? "" : c.getPhone(),
                "totalBusinessValue", c.getTotalBusinessValue(), "followUpDate", c.getFollowUpDate() == null ? "" : c.getFollowUpDate().toString(),
                "notes", c.getNotes() == null ? "" : c.getNotes()
        )).toList();
    }

    @Transactional
    public Map<String, Object> saveClient(UserAccount vendor, Map<String, Object> body) {
        requireCategoryProfile(vendor);
        CategoryVendorClient c = new CategoryVendorClient();
        c.setVendor(vendor);
        c.setClientName(isNotBlank(str(body.get("clientName"))) ? str(body.get("clientName")).trim() : "Client");
        c.setCompanyName(str(body.get("companyName")));
        c.setEmail(str(body.get("email")));
        c.setPhone(str(body.get("phone")));
        c.setTotalBusinessValue(toDecimal(body.get("totalBusinessValue"), BigDecimal.ZERO));
        if (isNotBlank(str(body.get("followUpDate")))) c.setFollowUpDate(LocalDate.parse(str(body.get("followUpDate")).trim()));
        c.setNotes(str(body.get("notes")));
        c.setCreatedAt(LocalDateTime.now());
        CategoryVendorClient saved = clientRepository.save(c);
        return Map.of("id", saved.getId(), "clientName", saved.getClientName());
    }

    public List<Map<String, Object>> listAvailability(UserAccount vendor) {
        requireCategoryProfile(vendor);
        return availabilityRepository.findByVendorOrderByAvailableDateAsc(vendor).stream().map(a -> Map.<String, Object>of(
                "id", a.getId(), "date", a.getAvailableDate().toString(), "status", a.getStatus(), "note", a.getNote() == null ? "" : a.getNote()
        )).toList();
    }

    @Transactional
    public Map<String, Object> setAvailability(UserAccount vendor, LocalDate date, String statusRaw, String note) {
        requireCategoryProfile(vendor);
        if (date == null || date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Availability can only be set from today's date onwards.");
        }
        CategoryVendorAvailability av = availabilityRepository.findByVendorAndAvailableDate(vendor, date)
                .orElseGet(CategoryVendorAvailability::new);
        av.setVendor(vendor);
        av.setAvailableDate(date);
        av.setStatus(isNotBlank(statusRaw) ? statusRaw.trim().toUpperCase() : "BLOCKED");
        av.setNote(note);
        av.setUpdatedAt(LocalDateTime.now());
        CategoryVendorAvailability saved = availabilityRepository.save(av);
        return Map.of("id", saved.getId(), "date", saved.getAvailableDate().toString(), "status", saved.getStatus(), "note", saved.getNote() == null ? "" : saved.getNote());
    }

    // ==================== MAPPERS & HELPERS ====================

    private int computeProfileCompletion(CategoryVendorProfile p) {
        int score = 0;
        if (isNotBlank(p.getBusinessName())) score += 15;
        if (isNotBlank(p.getContactPerson())) score += 10;
        if (isNotBlank(p.getPhone())) score += 10;
        if (isNotBlank(p.getCity()) && isNotBlank(p.getAddress())) score += 15;
        if (isNotBlank(p.getAboutBusiness())) score += 15;
        if (isNotBlank(p.getLogoFileName())) score += 10;
        if (isNotBlank(p.getCoverImageFileName())) score += 10;
        if (isNotBlank(p.getWebsiteUrl())) score += 5;
        if (isNotBlank(p.getServiceAreas())) score += 10;
        return Math.min(100, score);
    }

    private Map<String, Object> toProfileMap(CategoryVendorProfile p, UserAccount v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("vendorCategory", p.getVendorCategory());
        m.put("businessName", p.getBusinessName());
        m.put("contactPerson", p.getContactPerson());
        m.put("email", p.getEmail());
        m.put("phone", p.getPhone());
        m.put("alternatePhone", p.getAlternatePhone());
        m.put("yearsExperience", p.getYearsExperience());
        m.put("country", p.getCountry());
        m.put("state", p.getState());
        m.put("city", p.getCity());
        m.put("areaLocality", p.getAreaLocality());
        m.put("address", p.getAddress());
        m.put("pincode", p.getPincode());
        m.put("mapLink", p.getMapLink());
        m.put("websiteUrl", p.getWebsiteUrl());
        m.put("socialLinks", p.getSocialLinks());
        m.put("workingHours", p.getWorkingHours());
        m.put("serviceAreas", p.getServiceAreas());
        m.put("languagesSupported", p.getLanguagesSupported());
        m.put("aboutBusiness", p.getAboutBusiness());
        m.put("categoryDetailsJson", p.getCategoryDetailsJson());
        m.put("featuredListing", p.isFeaturedListing());
        m.put("profileVisible", p.isProfileVisible());
        m.put("notifyNewEnquiry", p.isNotifyNewEnquiry());
        m.put("notifyMessages", p.isNotifyMessages());
        m.put("logoUrl", isNotBlank(p.getLogoFileName()) ? "/api/category-vendors/logo/" + p.getLogoFileName() : null);
        m.put("coverImageUrl", isNotBlank(p.getCoverImageFileName()) ? "/api/category-vendors/logo/" + p.getCoverImageFileName() : null);
        m.put("verificationStatus", v.getVerificationStatus() != null ? v.getVerificationStatus().name() : "APPROVED");
        return m;
    }

    private Map<String, Object> toServiceMap(CategoryVendorServiceItem s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("vendorCategory", s.getVendorCategory());
        m.put("serviceName", s.getServiceName());
        m.put("subCategory", s.getSubCategory());
        m.put("description", s.getDescription());
        m.put("price", s.getPrice());
        m.put("pricingUnit", s.getPricingUnit());
        m.put("quantity", s.getQuantity());
        m.put("location", s.getLocation());
        m.put("detailsJson", s.getDetailsJson());
        m.put("portfolioUrl", s.getPortfolioUrl());
        m.put("active", s.isActive());
        return m;
    }

    private Map<String, Object> toBookingMap(CategoryVendorBooking b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("bookingCode", b.getBookingCode());
        m.put("vendorCategory", b.getVendorCategory());
        m.put("serviceName", b.getServiceItem() != null ? b.getServiceItem().getServiceName() : "Custom Package");
        m.put("customerName", b.getCustomerName());
        m.put("customerEmail", b.getCustomerEmail());
        m.put("customerPhone", b.getCustomerPhone());
        m.put("eventName", b.getEventName());
        m.put("projectLocation", b.getProjectLocation());
        m.put("eventDate", b.getEventDate().toString());
        m.put("requirements", b.getRequirements());
        m.put("quotedAmount", b.getQuotedAmount());
        m.put("advancePaid", b.getAdvancePaid());
        m.put("status", b.getStatus());
        m.put("vendorReply", b.getVendorReply());
        m.put("notes", b.getNotes());
        return m;
    }

    private Map<String, Object> toQuotationMap(CategoryVendorQuotation q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", q.getId());
        m.put("quotationCode", q.getQuotationCode());
        m.put("customerName", q.getCustomerName());
        m.put("customerEmail", q.getCustomerEmail());
        m.put("serviceName", q.getServiceName());
        m.put("quantity", q.getQuantity());
        m.put("unitPrice", q.getUnitPrice());
        m.put("grandTotal", q.getGrandTotal());
        m.put("validUntil", q.getValidUntil() == null ? null : q.getValidUntil().toString());
        m.put("deliveryTimeline", q.getDeliveryTimeline());
        m.put("status", q.getStatus());
        m.put("pdfUrl", "/api/category-vendors/quotations/" + q.getId() + "/pdf");
        return m;
    }

    private Map<String, Object> toProjectMap(CategoryVendorProject p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("projectCode", p.getProjectCode());
        m.put("projectName", p.getProjectName());
        m.put("clientName", p.getClientName());
        m.put("startDate", p.getStartDate() == null ? "" : p.getStartDate().toString());
        m.put("deadline", p.getDeadline() == null ? "" : p.getDeadline().toString());
        m.put("status", p.getStatus());
        m.put("progressPercent", p.getProgressPercent());
        m.put("assignedTeam", p.getAssignedTeam());
        m.put("deliverables", p.getDeliverables());
        return m;
    }

    private Map<String, Object> toInvoiceMap(CategoryVendorInvoice i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("invoiceNumber", i.getInvoiceNumber());
        m.put("customerName", i.getCustomerName());
        m.put("serviceName", i.getServiceName());
        m.put("totalAmount", i.getTotalAmount());
        m.put("paidAmount", i.getPaidAmount());
        m.put("paymentStatus", i.getPaymentStatus());
        m.put("invoiceDate", i.getInvoiceDate() == null ? "" : i.getInvoiceDate().toString());
        m.put("pdfUrl", "/api/category-vendors/invoices/" + i.getId() + "/pdf");
        return m;
    }

    private static String storeFile(MultipartFile file, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        String original = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String safeName = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
        Files.copy(file.getInputStream(), targetDir.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);
        return safeName;
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
    private static boolean isNotBlank(String s) { return s != null && !s.trim().isEmpty(); }
    private static Integer toInt(Object o, Integer def) { return (o == null || String.valueOf(o).isBlank()) ? def : Integer.parseInt(String.valueOf(o).trim()); }
    private static Long toLong(Object o) { return (o == null || String.valueOf(o).isBlank()) ? null : Long.parseLong(String.valueOf(o).trim()); }
    private static BigDecimal toDecimal(Object o, BigDecimal def) { return (o == null || String.valueOf(o).isBlank()) ? def : new BigDecimal(String.valueOf(o).trim()); }
}
