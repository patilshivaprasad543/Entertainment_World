package com.talentplatform.brand.config;

import com.talentplatform.brand.enums.PlanType;
import com.talentplatform.brand.model.BrandProfile;
import com.talentplatform.brand.model.TalentProfile;
import com.talentplatform.brand.repository.BrandProfileRepository;
import com.talentplatform.brand.repository.TalentProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DemoDataInitializer {
    @Bean
    CommandLineRunner seedDemoData(BrandProfileRepository brands, TalentProfileRepository talents) {
        return args -> {
            if (brands.count() == 0) {
                BrandProfile brand = new BrandProfile(); brand.setBrandName("Atelier North");
                brand.setLegalEntityName("Atelier North Private Limited"); brand.setIndustryCategory("Fashion & Apparel");
                brand.setPlanType(PlanType.PREMIUM_1499); brands.save(brand);
            }
            if (talents.count() == 0) {
                List<TalentProfile> demo = List.of(
                    talent("Aanya Rao", "Influencer", "Food Influencer", "Female", "Bengaluru", "English, Hindi, Kannada", "68000", "4.9", true, "@aanyarao"),
                    talent("Rohan Mehta", "Actor", "Lifestyle Creator", "Male", "Mumbai", "English, Hindi", "52000", "4.8", true, "@rohanmehta"),
                    talent("Diya Kapoor", "Model", "Fashion Creator", "Female", "Delhi NCR", "English, Hindi", "74000", "5.0", true, "@diyak"),
                    talent("Kavya Iyer", "UGC Creator", "Makeup Creator", "Female", "Chennai", "English, Tamil", "28000", "4.7", false, "@kavyamakes"),
                    talent("Arjun Nair", "Makeup Artist", "Makeup Creator", "Male", "Hyderabad", "English, Hindi, Telugu", "35000", "4.8", true, "@arjunartistry"),
                    talent("Meera Shah", "Influencer", "Food Influencer", "Female", "Pan-India", "English, Hindi, Gujarati", "92000", "4.9", true, "@meerashah")
                ); talents.saveAll(demo);
            }
        };
    }

    private TalentProfile talent(String name, String category, String specialty, String gender, String city, String languages,
            String rate, String rating, boolean verified, String handle) {
        TalentProfile t = new TalentProfile(); t.setFullName(name); t.setCategory(category); t.setSpecialty(specialty); t.setGender(gender);
        t.setCity(city); t.setLanguages(languages); t.setContractTypes("Per-Shoot, Monthly Retainer, Barter, Day-Rate");
        t.setBaseRate(new BigDecimal(rate)); t.setAverageRating(Double.valueOf(rating)); t.setTotalReviews(10);
        t.setVerified(verified); t.setPhotoUrl(handle); t.setEngagementRate(new BigDecimal("4.8")); return t;
    }
}
