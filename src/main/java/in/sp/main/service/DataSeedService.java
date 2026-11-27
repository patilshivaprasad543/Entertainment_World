package in.sp.main.service;

import in.sp.main.entity.*;
import in.sp.main.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds initial Super Admin, 3 Subscription Passes, curated Artists, and sample demo data
 * for Entertainment World (with zero references to any external brand).
 */
@Component
public class DataSeedService implements CommandLineRunner {

    private final SuperAdminRepository adminRepo;
    private final SubscriptionPassRepository passRepo;
    private final ArtistProfileRepository artistRepo;
    private final CastingCompanyRepository companyRepo;
    private final CastingProjectRepository projectRepo;
    private final ProjectApplicationRepository appRepo;
    private final ArtistFolderRepository folderRepo;
    private final ArtistFolderItemRepository folderItemRepo;
    private final SubscriptionPaymentRepository paymentRepo;
    private final ContactPortfolioHistoryRepository historyRepo;
    private final NotificationService notificationService;
    private final PasswordService passwordService;

    public DataSeedService(SuperAdminRepository adminRepo,
                           SubscriptionPassRepository passRepo,
                           ArtistProfileRepository artistRepo,
                           CastingCompanyRepository companyRepo,
                           CastingProjectRepository projectRepo,
                           ProjectApplicationRepository appRepo,
                           ArtistFolderRepository folderRepo,
                           ArtistFolderItemRepository folderItemRepo,
                           SubscriptionPaymentRepository paymentRepo,
                           ContactPortfolioHistoryRepository historyRepo,
                           NotificationService notificationService,
                           PasswordService passwordService) {
        this.adminRepo = adminRepo;
        this.passRepo = passRepo;
        this.artistRepo = artistRepo;
        this.companyRepo = companyRepo;
        this.projectRepo = projectRepo;
        this.appRepo = appRepo;
        this.folderRepo = folderRepo;
        this.folderItemRepo = folderItemRepo;
        this.paymentRepo = paymentRepo;
        this.historyRepo = historyRepo;
        this.notificationService = notificationService;
        this.passwordService = passwordService;
    }

    @Override
    public void run(String... args) {
        seedSuperAdmin();
        seedPasses();
        List<ArtistProfile> artists = seedArtists();
        seedDemoCompanies(artists);
    }

    private void seedSuperAdmin() {
        if (adminRepo.count() > 0) return;
        SuperAdmin admin = new SuperAdmin();
        admin.setAdminCode("EW-ADMIN-01");
        admin.setFullName("Vikramaditya Rao");
        admin.setEmail("admin@entertainmentworld.in");
        admin.setPasswordHash(passwordService.hash("Admin@123"));
        admin.setRoleTitle("Super Admin");
        admin.setActive(true);
        adminRepo.save(admin);
    }

    private void seedPasses() {
        if (passRepo.count() > 0) return;

        SubscriptionPass p1 = new SubscriptionPass();
        p1.setPassCode("PASS_1_CONTACT");
        p1.setPassName("Contact View Pass");
        p1.setSubtitle("Ideal for one-time projects");
        p1.setBasePriceInr(3999);
        p1.setDefaultMonths(1);
        p1.setContactViewsLimit(50);
        p1.setContactViewsLabel("50");
        p1.setPortfolioViewsLimit(150);
        p1.setPortfolioViewsLabel("150");
        p1.setInviteAuditionLimit(50);
        p1.setZeroCommission(true);
        p1.setOrganiseFolders(true);
        p1.setSocialShare(true);
        p1.setProjectPromotionQuota(0);
        p1.setProjectPromotionLabel("—");
        p1.setCallbackSupport("—");
        p1.setDisplayOrder(1);

        SubscriptionPass p2 = new SubscriptionPass();
        p2.setPassCode("PASS_2_PROMOTION");
        p2.setPassName("Project Promotion Pass");
        p2.setSubtitle("Get up to 10x more artist applications");
        p2.setBasePriceInr(4999);
        p2.setDefaultMonths(1);
        p2.setContactViewsLimit(250);
        p2.setContactViewsLabel("All applications of Promoted Project");
        p2.setPortfolioViewsLimit(1000);
        p2.setPortfolioViewsLabel("All applications of Promoted Project");
        p2.setInviteAuditionLimit(100);
        p2.setZeroCommission(true);
        p2.setOrganiseFolders(true);
        p2.setSocialShare(true);
        p2.setProjectPromotionQuota(1);
        p2.setProjectPromotionLabel("1");
        p2.setCallbackSupport("—");
        p2.setDisplayOrder(2);

        SubscriptionPass p3 = new SubscriptionPass();
        p3.setPassCode("PASS_3_SUBSCRIPTION");
        p3.setPassName("Subscription Pass");
        p3.setSubtitle("For Contact View, Project Promotion & Unlimited Benefits");
        p3.setBadgeText("Recommended for regular requirements");
        p3.setBasePriceInr(15000);
        p3.setDefaultMonths(3);
        p3.setContactViewsLimit(1500);
        p3.setContactViewsLabel("500 / month");
        p3.setPortfolioViewsLimit(-1);
        p3.setPortfolioViewsLabel("Unlimited");
        p3.setInviteAuditionLimit(2000);
        p3.setZeroCommission(true);
        p3.setOrganiseFolders(true);
        p3.setSocialShare(true);
        p3.setProjectPromotionQuota(1);
        p3.setProjectPromotionLabel("1 / quarter");
        p3.setCallbackSupport("Supported");
        p3.setDisplayOrder(3);

        passRepo.saveAll(List.of(p1, p2, p3));
    }

    private List<ArtistProfile> seedArtists() {
        if (artistRepo.count() > 0) {
            return artistRepo.findAll();
        }
        List<ArtistProfile> list = List.of(
            artist("Aarav Mehta", "Actor", "Actor / Model", "Male", 26, "Mumbai", "Entertainment World", "Professional",
                   "Hindi, English, Marathi", "Method actor with 4 feature films and 12 national commercials.",
                   "/assets/images/cat-actor.jpg", true, "Monologue 1 - Dramatic Scene", "/assets/video/audition-1.mp4",
                   true, true, true, false, "aarav.mehta@artists.ew.in", "+91 98201 44101", 980, "drama film theatre lead"),
            artist("Rhea Kapoor", "Model", "Model / Actor", "Female", 24, "Mumbai", "Entertainment World", "Professional",
                   "Hindi, English, Punjabi", "Editorial & runway model featured in Vogue India campaigns.",
                   "/assets/images/cat-model.jpg", true, "Commercial Lookbook Self-Tape", "/assets/video/audition-3.mp4",
                   true, true, false, false, "rhea.kapoor@artists.ew.in", "+91 98201 44102", 910, "runway fashion print luxury"),
            artist("Ananya Deshmukh", "Singer", "Singer / Performer", "Female", 23, "Mumbai", "Independent", "Professional",
                   "Hindi, Marathi, English", "Playback & live concert vocalist specializing in soulful melodies.",
                   "/assets/images/cat-singer.jpg", true, "Vocal Showreel - Live Acoustic", "/assets/video/audition-2.mp4",
                   true, false, true, true, "ananya.d@artists.ew.in", "+91 98201 44103", 845, "playback classical pop indie"),
            artist("Meera Nair", "Voice-over Artist", "Anchor / Voice-over Artist", "Female", 27, "Delhi NCR", "Entertainment World", "Professional",
                   "English, Hindi, Malayalam", "Bilingual commercial VO artist for OTT promos and documentaries.",
                   "/assets/images/cat-voiceover.jpg", true, "Commercial Narration Reel", "/assets/video/audition-2.mp4",
                   true, true, false, false, "meera.nair@artists.ew.in", "+91 98111 44104", 890, "dubbing corporate trailer narration"),
            artist("Kabir Sen", "Dancer", "Dancer / Choreographer", "Male", 25, "Bengaluru", "Partner Studio", "Professional",
                   "Hindi, English, Bengali", "Contemporary & hip-hop movement specialist for music videos.",
                   "/assets/images/cat-dancer.jpg", true, "Contemporary Dance Reel", "/assets/video/audition-1.mp4",
                   true, false, true, true, "kabir.sen@artists.ew.in", "+91 99001 44105", 760, "contemporary freestyle choreography"),
            artist("Vikram Joshi", "Photographer", "Photographer / Cinematographer", "Male", 30, "Delhi NCR", "Independent", "Professional",
                   "Hindi, English", "Editorial fashion photographer and commercial DOP.",
                   "/assets/images/cat-photographer.jpg", false, null, null,
                   false, true, false, false, "vikram.j@artists.ew.in", "+91 98111 44106", 670, "camera dop lookbook lighting"),
            artist("Rohan Kulkarni", "Musician", "Musician / Composer", "Male", 28, "Pune", "Entertainment World", "Professional",
                   "Hindi, Marathi, English", "Session guitarist & background score composer for indie cinema.",
                   "/assets/images/cat-musician.jpg", true, "Acoustic Guitar Studio Reel", "/assets/video/audition-1.mp4",
                   false, false, true, false, "rohan.k@artists.ew.in", "+91 98221 44107", 630, "guitar score indie composer"),
            artist("Siddharth Verma", "Stand-up Comedian", "Stand-up Comedian / Host", "Male", 29, "Mumbai", "Partner Studio", "Professional",
                   "Hindi, English", "Bilingual live stand-up comic and corporate show host.",
                   "/assets/images/cat-comedian.jpg", true, "Live Crowd Work Set", "/assets/video/audition-1.mp4",
                   true, false, true, false, "sid.verma@artists.ew.in", "+91 98201 44108", 740, "comedy improv host anchor"),
            artist("Devansh Rao", "Writer", "Writer / Script Supervisor", "Male", 33, "Mumbai", "Entertainment World", "Professional",
                   "Hindi, English, Telugu", "Screenplay and dialogue writer for OTT thrillers.",
                   "/assets/images/cat-writer.jpg", false, null, null,
                   false, true, false, false, "devansh.rao@artists.ew.in", "+91 98201 44109", 590, "screenplay thriller dialogues ott"),
            artist("Ishaan Malhotra", "Filmmaker", "Filmmaker / Assistant Director", "Male", 31, "Mumbai", "Independent", "Professional",
                   "Hindi, English, Punjabi", "First AD and commercial ad filmmaker with 20+ brand shoots.",
                   "/assets/images/artist-director.jpg", true, "Director Showreel 2026", "/assets/video/audition-1.mp4",
                   true, true, false, false, "ishaan.m@artists.ew.in", "+91 98201 44110", 720, "director commercial production unit"),
            artist("Naina Sharma", "Singer", "Singer / Actor", "Female", 24, "Jaipur", "Entertainment World", "Intermediate",
                   "Hindi, Rajasthani, English", "Folk-fusion singer and expressive camera performer.",
                   "/assets/images/artist-singer-2.jpg", true, "Sufi Fusion Audition", "/assets/video/audition-2.mp4",
                   false, false, true, true, "naina.s@artists.ew.in", "+91 98290 44111", 650, "folk sufi stage melody"),
            artist("Pooja Menon", "Voice-over Artist", "Anchor / Voice-over Artist", "Female", 26, "Hyderabad", "Entertainment World", "Professional",
                   "English, Hindi, Tamil, Telugu", "Multi-lingual promo voice & podcast narrator.",
                   "/assets/images/artist-vo-2.jpg", true, "Multilingual Promo Tape", "/assets/video/audition-2.mp4",
                   true, false, false, false, "pooja.menon@artists.ew.in", "+91 98490 44112", 815, "promo voiceover tamil telugu"),
            artist("Arjun Khanna", "Actor", "Actor / Influencer", "Male", 27, "Delhi NCR", "Entertainment World", "Professional",
                   "Hindi, English, Punjabi", "Lead romantic & action actor with martial arts training.",
                   "/assets/images/cat-actor.jpg", true, "Action Monologue Cut", "/assets/video/audition-1.mp4",
                   true, true, true, false, "arjun.k@artists.ew.in", "+91 98111 44113", 950, "action stunt thriller hero"),
            artist("Tara Sengupta", "Model", "Model / Dancer", "Female", 22, "Kolkata", "Partner Studio", "Intermediate",
                   "Bengali, Hindi, English", "Print model & expressive digital creator for beauty brands.",
                   "/assets/images/cat-model.jpg", true, "Beauty Campaign Reel", "/assets/video/audition-3.mp4",
                   true, false, true, true, "tara.s@artists.ew.in", "+91 98300 44114", 710, "beauty skincare print digital"),
            artist("Karanbir Gill", "Musician", "Musician / Singer", "Male", 29, "Chandigarh", "Independent", "Professional",
                   "Punjabi, Hindi, English", "Acoustic composer & live stage vocalist.",
                   "/assets/images/cat-musician.jpg", true, "Original Track Demo", "/assets/video/audition-1.mp4",
                   false, false, true, false, "karanbir.g@artists.ew.in", "+91 98140 44115", 605, "punjabi folk acoustic track"),
            artist("Zoya Qureshi", "Voice-over Artist", "Voice-over Artist / Actor", "Female", 28, "Mumbai", "Entertainment World", "Professional",
                   "Hindi, Urdu, English", "Character dubbing & audiobook specialist.",
                   "/assets/images/cat-voiceover.jpg", true, "Audiobook Excerpt", "/assets/video/audition-2.mp4",
                   true, true, false, false, "zoya.q@artists.ew.in", "+91 98201 44116", 775, "urdu audiobook character dubbing"),
            artist("Nikhil Chawla", "Photographer", "Photographer / Stylist", "Male", 32, "Ahmedabad", "Independent", "Professional",
                   "Gujarati, Hindi, English", "Lookbook photographer for bridal & commercial campaigns.",
                   "/assets/images/cat-photographer.jpg", false, null, null,
                   false, false, true, false, "nikhil.c@artists.ew.in", "+91 98250 44117", 540, "bridal commercial product studio"),
            artist("Rishabh Tiwari", "Actor", "Actor / Theatre", "Male", 25, "Varanasi", "Entertainment World", "Intermediate",
                   "Hindi, Bhojpuri, English", "NSD-trained theatre actor specializing in intense realism.",
                   "/assets/images/cat-actor.jpg", true, "Theatre Soliloquy Tape", "/assets/video/audition-1.mp4",
                   false, false, false, true, "rishabh.t@artists.ew.in", "+91 94150 44118", 685, "theatre realism dialect hindi")
        );
        return artistRepo.saveAll(list);
    }

    private ArtistProfile artist(String name, String cat, String sec, String gender, int age, String city,
                                 String agency, String exp, String langs, String bio, String avatar,
                                 boolean hasVid, String vidTitle, String vidUrl, boolean showcase,
                                 boolean platinum, boolean featured, boolean rising, String email,
                                 String phone, int views, String kw) {
        ArtistProfile a = new ArtistProfile();
        a.setFullName(name);
        a.setCategory(cat);
        a.setSecondaryRoles(sec);
        a.setGender(gender);
        a.setAge(age);
        a.setCity(city);
        a.setAgencyName(agency);
        a.setExpertise(exp);
        a.setLanguages(langs);
        a.setBio(bio);
        a.setAvatarUrl(avatar);
        a.setHasVideo(hasVid);
        a.setVideoTitle(vidTitle);
        a.setVideoUrl(vidUrl);
        a.setShowcaseBadge(showcase);
        a.setPlatinumTier(platinum);
        a.setFeaturedTier(featured);
        a.setRisingStar(rising);
        a.setEmail(email);
        a.setPhone(phone);
        a.setViewsCount(views);
        a.setKeywords(kw);
        return a;
    }

    private void seedDemoCompanies(List<ArtistProfile> artists) {
        if (companyRepo.count() > 0) return;

        // 1. Sample Pending Registration (for testing Super Admin Approval -> Payment -> Dashboard)
        CastingCompany pending = new CastingCompany();
        pending.setRepresentativeName("Rohan Deshpande");
        pending.setPasswordHash(passwordService.hash("Demo@123"));
        pending.setOrganizationType("Casting Agency");
        pending.setTalentSought("Actor, Model, Voice-over Artist");
        pending.setProjectTypesInterest("Feature Film, Web Series / OTT");
        pending.setCompanyName("Aether Prism Casting");
        pending.setCompanyLogoPath("/assets/images/cat-photographer.jpg");
        pending.setCompanyEmail("pending@aetherprism.in");
        pending.setContactNumber("+91 98204 77810");
        pending.setWebsite("https://aetherprism.example.in");
        pending.setCompanyAddress("402, Silver Crest Studio Lane, Andheri West");
        pending.setCity("Mumbai");
        pending.setState("Maharashtra");
        pending.setCountry("India");
        pending.setYearEstablished(2021);
        pending.setCompanyDescription("Boutique casting studio focused on realistic regional cinema and digital series.");
        pending.setGstNumber("27AABCA9281K1Z5");
        pending.setPanNumber("AABCA9281K");
        pending.setAccountStatus("Pending");
        pending.setEmailVerification("Not verified");
        pending.setMobileVerification("Not verified");
        pending.setDocumentVerification("Pending");
        pending.setAccountSuspension("Active");
        pending.setVerifiedBadge(false);
        pending.setSubscriptionActive(false);
        companyRepo.save(pending);
        createDefaultFolders(pending.getId());
        notificationService.notifyCompany(pending.getId(), pending.getCompanyEmail(),
                "Registration Received",
                "Your company registration is pending Super Admin verification.");

        // 2. Sample Approved + Paid Company (for immediate preview of the full Dashboard)
        CastingCompany active = new CastingCompany();
        active.setRepresentativeName("Tess Kapoor");
        active.setPasswordHash(passwordService.hash("Demo@123"));
        active.setOrganizationType("Production House");
        active.setTalentSought("Actor, Voice-over Artist, Singer");
        active.setProjectTypesInterest("Feature Film, Web Series / OTT, Ad Film");
        active.setCompanyName("Moonlight Cinema Casting");
        active.setCompanyLogoPath("/assets/images/hero-spotlight.jpg");
        active.setCompanyEmail("studio@moonlightcinema.in");
        active.setContactNumber("+91 98209 55100");
        active.setWebsite("https://moonlightcinema.example.in");
        active.setCompanyAddress("Plot 18, Film City Annex, Goregaon East");
        active.setCity("Mumbai");
        active.setState("Maharashtra");
        active.setCountry("India");
        active.setYearEstablished(2017);
        active.setCompanyDescription("Award-winning feature film and OTT casting house discovering pan-India performers across drama, commercial, and voice talent.");
        active.setGstNumber("27AAACM4119F1Z8");
        active.setPanNumber("AAACM4119F");
        active.setAccountStatus("Approved");
        active.setEmailVerification("Verified");
        active.setMobileVerification("Verified");
        active.setDocumentVerification("Verified");
        active.setAdminRemarks("All corporate KYC documents, website, and representative credentials verified.");
        active.setVerifiedBy("Vikramaditya Rao (EW-ADMIN-01)");
        active.setVerificationDate(LocalDate.now().minusDays(3));
        active.setAccountSuspension("Active");
        active.setVerifiedBadge(true);
        active.setFollowersCount(148);
        active.setActivePassCode("PASS_3_SUBSCRIPTION");
        active.setActivePassName("Subscription Pass");
        active.setSubscriptionActive(true);
        active.setSubscriptionExpiry(LocalDate.now().plusMonths(3));
        active.setContactViewsTotal(1500);
        active.setContactViewsUsed(4);
        active.setPortfolioViewsTotal(-1);
        active.setPortfolioViewsUsed(12);
        active.setAuditionInvitesTotal(2000);
        active.setAuditionInvitesUsed(6);
        active.setProjectPromotionsRemaining(1);
        active.setShowreelVideosJson("[{\"title\":\"Studio Showreel 2026\",\"thumb\":\"/assets/images/hero-spotlight.jpg\",\"url\":\"/assets/video/entertainment-world-intro.mp4\"},{\"title\":\"How We Audition\",\"thumb\":\"/assets/images/cat-actor.jpg\",\"url\":\"/assets/video/audition-1.mp4\"}]");
        active.setPortfolioPhotosJson("[\"/assets/images/cat-actor.jpg\",\"/assets/images/cat-model.jpg\",\"/assets/images/cat-singer.jpg\",\"/assets/images/cat-voiceover.jpg\"]");
        companyRepo.save(active);

        SubscriptionPayment pay = new SubscriptionPayment();
        pay.setCompanyId(active.getId());
        pay.setPassCode("PASS_3_SUBSCRIPTION");
        pay.setPassName("Subscription Pass");
        pay.setDurationMonths(3);
        pay.setBaseAmountInr(new BigDecimal("15000.00"));
        pay.setGstAmountInr(new BigDecimal("2700.00"));
        pay.setTotalPaidInr(new BigDecimal("17700.00"));
        pay.setPaymentMethod("UPI");
        pay.setTransactionRef("EW-TXN-20261123-1001");
        pay.setPaymentStatus("PAID");
        pay.setValidFrom(LocalDate.now().minusDays(3));
        pay.setValidUntil(LocalDate.now().plusMonths(3));
        paymentRepo.save(pay);

        List<ArtistFolder> folders = createDefaultFolders(active.getId());
        for (int i = 0; i < Math.min(6, artists.size()); i++) {
            ArtistFolderItem item = new ArtistFolderItem();
            item.setCompanyId(active.getId());
            item.setFolderId(folders.get(i % folders.size()).getId());
            item.setArtistId(artists.get(i).getId());
            folderItemRepo.save(item);
        }

        // Seed Casting Projects & Applications
        CastingProject prj1 = new CastingProject();
        prj1.setCompanyId(active.getId());
        prj1.setProjectTitle("Seeking Voice-Over Artist for Brand Promo Film");
        prj1.setProjectType("Advertisement");
        prj1.setProductionHouseName("Moonlight Pictures Banner");
        prj1.setCastingCompanyName(active.getCompanyName());
        prj1.setProjectLanguage("English, Hindi");
        prj1.setProjectStage("Casting Open");
        prj1.setTargetCategory("Voice-over Artist");
        prj1.setTargetGender("Female");
        prj1.setAgeMin(20);
        prj1.setAgeMax(38);
        prj1.setPreferredAppearance("Voice-only (clear diction)");
        prj1.setExperienceLevel("Experienced");
        prj1.setArtistsRequiredCount(2);
        prj1.setRoleDescription("Lead narrator with warm conversational tone for luxury travel campaign.");
        prj1.setProjectDetails("We are seeking a warm, articulate bilingual voice-over artist to narrate a 90-second luxury travel campaign film.");
        prj1.setTags("voice-over, narration, commercial, english, hindi");
        prj1.setDurationType("Date-specific");
        prj1.setAuditionLocation("Online Self-Tape Studio");
        prj1.setAuditionDate(LocalDate.now().plusDays(5));
        prj1.setAuditionType("Online");
        prj1.setShootingStartDate(LocalDate.now().plusDays(12));
        prj1.setShootingDuration("3 Days");
        prj1.setAvailabilityRequirements("Flexible 4-hour studio slot");
        prj1.setPaymentType("Paid");
        prj1.setBudget("Rs. 45,000");
        prj1.setPaymentBasis("Per project");
        prj1.setAccommodationProvided("No");
        prj1.setFoodProvided("Yes");
        prj1.setTravelExpensesCovered("Yes");
        prj1.setLocation("Mumbai");
        prj1.setAuditionProcess("Round 1: Voice sample review · Round 2: Live studio voice check");
        prj1.setAuditionInstructions("Record in a quiet room without background music or echo.");
        prj1.setSelfIntroVideoRequired("Yes");
        prj1.setPortfolioRequired("Yes");
        prj1.setOnlineAuditionEnabled(true);
        prj1.setAuditionScript("Welcome to the horizon where stillness speaks louder than speed. Read with warm measured pacing.");
        prj1.setLastDateToApply(LocalDate.now().plusDays(26));
        prj1.setRequiredPhotographs("2 Studio Headshots");
        prj1.setIntroVideoRequirement("30-second intro slate");
        prj1.setActingPortfolioRequirement("Commercial voice reel");
        prj1.setPreviousWorkLinksReq("YouTube or Drive reel link");
        prj1.setApplicantContactReq("Phone, WhatsApp & Email");
        prj1.setCastingDirectorName(active.getRepresentativeName());
        prj1.setContactEmail(active.getCompanyEmail());
        prj1.setContactNumber(active.getContactNumber());
        prj1.setWhatsappNumber(active.getContactNumber());
        prj1.setWebsiteOrSocialLinks(active.getWebsite());
        prj1.setProjectPosterPath("/assets/images/hero-spotlight.jpg");
        prj1.setPromoted(true);
        prj1.setPostedDate(LocalDate.now().minusDays(4));
        prj1.setExpiryDate(LocalDate.now().plusDays(26));
        projectRepo.save(prj1);

        CastingProject prj2 = new CastingProject();
        prj2.setCompanyId(active.getId());
        prj2.setProjectTitle("Lead Actors for Monsoon Noir Web Series");
        prj2.setProjectType("Web Series");
        prj2.setProductionHouseName("Moonlight Originals");
        prj2.setCastingCompanyName(active.getCompanyName());
        prj2.setProjectLanguage("Hindi");
        prj2.setProjectStage("Pre-production");
        prj2.setTargetCategory("Actor");
        prj2.setTargetGender("Any");
        prj2.setAgeMin(22);
        prj2.setAgeMax(35);
        prj2.setHeightRequirement("5'5\" - 6'1\"");
        prj2.setPreferredAppearance("Intense expressive eyes, urban silhouette");
        prj2.setExperienceLevel("Both");
        prj2.setArtistsRequiredCount(2);
        prj2.setRoleDescription("Two sharp investigative leads navigating Mumbai monsoon nights.");
        prj2.setCharacterName("Detective Kabir / Meera");
        prj2.setProjectDetails("Casting two charismatic leads for an 8-episode mystery thriller shooting in Mumbai and Pune.");
        prj2.setTags("ott, thriller, lead actor, drama");
        prj2.setDurationType("Full-Time");
        prj2.setAuditionLocation("Goregaon Studio / Online");
        prj2.setAuditionDate(LocalDate.now().plusDays(7));
        prj2.setAuditionType("Online");
        prj2.setShootingStartDate(LocalDate.now().plusDays(25));
        prj2.setShootingEndDate(LocalDate.now().plusDays(70));
        prj2.setShootingDuration("45 Days");
        prj2.setAvailabilityRequirements("Continuous 45-day schedule availability");
        prj2.setPaymentType("Paid");
        prj2.setBudget("Rs. 1,80,000");
        prj2.setPaymentBasis("Per episode");
        prj2.setAccommodationProvided("Yes");
        prj2.setFoodProvided("Yes");
        prj2.setTravelExpensesCovered("Yes");
        prj2.setLocation("Mumbai");
        prj2.setAuditionProcess("Self-tape submission followed by in-person chemistry read");
        prj2.setAuditionInstructions("Naturalistic performance; avoid theatrical projection.");
        prj2.setSelfIntroVideoRequired("Yes");
        prj2.setPortfolioRequired("Yes");
        prj2.setOnlineAuditionEnabled(true);
        prj2.setAuditionScript("INT. METRO PLATFORM - NIGHT. Detective Kabir lowers his umbrella and speaks softly into the recorder.");
        prj2.setLastDateToApply(LocalDate.now().plusDays(28));
        prj2.setRequiredPhotographs("3 Unedited Portraits (Front, Left, Right)");
        prj2.setIntroVideoRequirement("45-second intro stating height, city & theatre credits");
        prj2.setActingPortfolioRequirement("Dramatic showreel");
        prj2.setPreviousWorkLinksReq("IMDb / Showreel links");
        prj2.setApplicantContactReq("Phone & Email");
        prj2.setCastingDirectorName(active.getRepresentativeName());
        prj2.setContactEmail(active.getCompanyEmail());
        prj2.setContactNumber(active.getContactNumber());
        prj2.setWhatsappNumber(active.getContactNumber());
        prj2.setWebsiteOrSocialLinks(active.getWebsite());
        prj2.setProjectPosterPath("/assets/images/cat-actor.jpg");
        prj2.setPostedDate(LocalDate.now().minusDays(2));
        prj2.setExpiryDate(LocalDate.now().plusDays(28));
        projectRepo.save(prj2);

        String[] statuses = {"APPLIED", "SHORTLISTED", "INVITED", "SUGGESTED"};
        for (int i = 0; i < Math.min(12, artists.size()); i++) {
            ArtistProfile a = artists.get(i);
            ProjectApplication pa = new ProjectApplication();
            pa.setProjectId(i < 5 ? prj1.getId() : prj2.getId());
            pa.setArtistId(a.getId());
            pa.setRoleName(i < 5 ? "Voice-over Artist" : "Lead Actor");
            pa.setStatus(statuses[i % statuses.length]);
            pa.setAuditionSubmissionTitle(a.isHasVideo() ? a.getVideoTitle() : "Portfolio Submission");
            pa.setAuditionVideoUrl(a.getVideoUrl() != null ? a.getVideoUrl() : "/assets/video/audition-1.mp4");
            pa.setAppliedAt(LocalDateTime.now().minusHours(i * 3L));
            appRepo.save(pa);
        }

        // Seed Transaction History (Contact Views & Portfolio Views)
        for (int i = 0; i < Math.min(4, artists.size()); i++) {
            ContactPortfolioHistory h = new ContactPortfolioHistory();
            h.setCompanyId(active.getId());
            h.setArtistId(artists.get(i).getId());
            h.setViewType("CONTACT_VIEW");
            h.setTierType("PAID");
            h.setViewedAt(LocalDateTime.now().minusHours(i * 5L));
            historyRepo.save(h);
        }
        for (int i = 0; i < Math.min(6, artists.size()); i++) {
            ContactPortfolioHistory h = new ContactPortfolioHistory();
            h.setCompanyId(active.getId());
            h.setArtistId(artists.get(i).getId());
            h.setViewType("PORTFOLIO_VIEW");
            h.setTierType(i < 4 ? "PAID" : "FREE");
            h.setViewedAt(LocalDateTime.now().minusHours(i * 2L));
            historyRepo.save(h);
        }

        notificationService.notifyCompany(active.getId(), active.getCompanyEmail(),
                "Super Admin Approved & Blue Tick Activated",
                "Your company verification is complete and your Subscription Pass is active.");
        notificationService.notifyCompany(active.getId(), active.getCompanyEmail(),
                "New Video Auditions Received",
                "12 artists submitted portfolios & self-tapes across your active casting projects.");
    }

    public List<ArtistFolder> createDefaultFolders(Long companyId) {
        if (!folderRepo.findByCompanyIdOrderByIdAsc(companyId).isEmpty()) {
            return folderRepo.findByCompanyIdOrderByIdAsc(companyId);
        }
        List<String> names = List.of("Actors / Models", "Writers", "Anchors / VO Artist", "Photographers", "Singers");
        return names.stream().map(name -> {
            ArtistFolder f = new ArtistFolder();
            f.setCompanyId(companyId);
            f.setFolderName(name);
            return folderRepo.save(f);
        }).toList();
    }
}
