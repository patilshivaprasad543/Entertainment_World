package com.example.demo.config;

import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.model.VerificationStatus;
import com.example.demo.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemoUserSeeder implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;

    public DemoUserSeeder(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public void run(String... args) {
        UserAccount actor = new UserAccount("Alex Rivera", "actor@entertainmentworld.com", "actor123", UserRole.ACTOR);
        actor.setProfessionalCategory("Actor");
        actor.setActingStartAge(18);
        actor.setPhone("+91 9876543210");
        actor.setCity("Mumbai");
        actor.setPortfolioOrBio("Feature film and theatre actor.");

        UserAccount vendor = new UserAccount("Prime Stage Vendors", "vendor@entertainmentworld.com", "vendor123", UserRole.VENDOR);
        vendor.setOrganizationName("Prime Stage Vendors Pvt Ltd");
        vendor.setDocumentType("Business License");
        vendor.setDocumentNumber("VND-2026-001");

        UserAccount eventMgmt = new UserAccount("Starlight Events", "events@entertainmentworld.com", "events123", UserRole.EVENT_MANAGEMENT);
        eventMgmt.setOrganizationName("Starlight Event Management");
        eventMgmt.setDocumentType("Event Management Permit");
        eventMgmt.setDocumentNumber("EVT-2026-001");

        UserAccount casting = new UserAccount("Apex Casting Studio", "casting@entertainmentworld.com", "casting123", UserRole.CASTING);
        casting.setOrganizationName("Apex Casting Studio");
        casting.setDocumentType("Production Registration");
        casting.setDocumentNumber("CAST-2026-001");

        UserAccount brand = new UserAccount("Nova Brand House", "brand@entertainmentworld.com", "brand123", UserRole.BRAND);
        brand.setOrganizationName("Nova Brand House");
        brand.setDocumentType("Brand Authorization Letter");
        brand.setDocumentNumber("BRN-2026-001");

        List<UserAccount> defaults = List.of(
                new UserAccount("System Admin", "admin@entertainmentworld.com", "admin123", UserRole.ADMIN),
                actor,
                vendor,
                eventMgmt,
                casting,
                brand
        );

        for (UserAccount account : defaults) {
            userAccountRepository.findByEmailAndRole(account.getEmail(), account.getRole())
                    .ifPresentOrElse(existing -> {
                        if (existing.getVerificationStatus() == null) {
                            existing.setVerificationStatus(VerificationStatus.APPROVED);
                            userAccountRepository.save(existing);
                        }
                    }, () -> userAccountRepository.save(account));
        }
    }
}
