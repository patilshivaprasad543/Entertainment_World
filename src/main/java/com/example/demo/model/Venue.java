package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "venues", indexes = {
        @Index(name = "idx_venues_vendor", columnList = "vendor_account_id"),
        @Index(name = "idx_venues_city_type", columnList = "city,venueType")
})
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendor_account_id", nullable = false)
    private UserAccount vendor;

    @Column(nullable = false, length = 150)
    private String venueName;

    @Column(nullable = false, length = 80)
    private String venueType;

    @Column(length = 2000)
    private String description;

    @Column(length = 120)
    private String ownerName;

    @Column(length = 30)
    private String contactNumber;

    @Column(length = 16)
    private String contactCountryCode = "+91";

    @Column(length = 20)
    private String venueCategory = "Indoor";

    @Column(length = 150)
    private String email;

    // Location
    @Column(length = 255)
    private String address;

    @Column(length = 120)
    private String areaLocality;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(length = 80)
    private String state;

    @Column(length = 80)
    private String country = "India";

    @Column(length = 20)
    private String pincode;

    @Column(length = 500)
    private String mapLink;

    // Capacity
    private Integer minCapacity = 50;
    private Integer maxCapacity = 500;
    private Integer seatingCapacity = 350;
    private Integer floatingCapacity = 500;

    // Facilities
    private boolean parking = true;
    private boolean airConditioning = true;
    private boolean cateringArea = true;
    private boolean stage = true;
    private boolean diningArea = true;
    private boolean decoration = false;
    private boolean powerBackup = true;
    private boolean wifi = true;
    private boolean changingRooms = true;
    private boolean security = true;
    private boolean generator = true;

    @Column(length = 500)
    private String otherFacilities;

    // Pricing
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerDay = new BigDecimal("50000.00");

    @Column(precision = 12, scale = 2)
    private BigDecimal pricePerHour = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal weekdayPrice = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal weekendPrice = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal seasonalPrice = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal specialEventPrice = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal additionalCharges = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal decorationCharges = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal cateringCharges = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal advanceAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal halfDayPrice = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal securityDeposit = BigDecimal.ZERO;

    @Column(length = 255)
    private String coverImageFileName;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean flagged = false;

    @Column(length = 255)
    private String flagReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Venue() {
    }

    public Long getId() { return id; }
    public UserAccount getVendor() { return vendor; }
    public void setVendor(UserAccount vendor) { this.vendor = vendor; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public String getVenueType() { return venueType; }
    public void setVenueType(String venueType) { this.venueType = venueType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getContactCountryCode() { return contactCountryCode; }
    public void setContactCountryCode(String contactCountryCode) { this.contactCountryCode = contactCountryCode; }
    public String getVenueCategory() { return venueCategory; }
    public void setVenueCategory(String venueCategory) { this.venueCategory = venueCategory; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAreaLocality() { return areaLocality; }
    public void setAreaLocality(String areaLocality) { this.areaLocality = areaLocality; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getMapLink() { return mapLink; }
    public void setMapLink(String mapLink) { this.mapLink = mapLink; }
    public Integer getMinCapacity() { return minCapacity; }
    public void setMinCapacity(Integer minCapacity) { this.minCapacity = minCapacity; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public Integer getSeatingCapacity() { return seatingCapacity; }
    public void setSeatingCapacity(Integer seatingCapacity) { this.seatingCapacity = seatingCapacity; }
    public Integer getFloatingCapacity() { return floatingCapacity; }
    public void setFloatingCapacity(Integer floatingCapacity) { this.floatingCapacity = floatingCapacity; }
    public boolean isParking() { return parking; }
    public void setParking(boolean parking) { this.parking = parking; }
    public boolean isAirConditioning() { return airConditioning; }
    public void setAirConditioning(boolean airConditioning) { this.airConditioning = airConditioning; }
    public boolean isCateringArea() { return cateringArea; }
    public void setCateringArea(boolean cateringArea) { this.cateringArea = cateringArea; }
    public boolean isStage() { return stage; }
    public void setStage(boolean stage) { this.stage = stage; }
    public boolean isDiningArea() { return diningArea; }
    public void setDiningArea(boolean diningArea) { this.diningArea = diningArea; }
    public boolean isDecoration() { return decoration; }
    public void setDecoration(boolean decoration) { this.decoration = decoration; }
    public boolean isPowerBackup() { return powerBackup; }
    public void setPowerBackup(boolean powerBackup) { this.powerBackup = powerBackup; }
    public boolean isWifi() { return wifi; }
    public void setWifi(boolean wifi) { this.wifi = wifi; }
    public boolean isChangingRooms() { return changingRooms; }
    public void setChangingRooms(boolean changingRooms) { this.changingRooms = changingRooms; }
    public boolean isSecurity() { return security; }
    public void setSecurity(boolean security) { this.security = security; }
    public boolean isGenerator() { return generator; }
    public void setGenerator(boolean generator) { this.generator = generator; }
    public String getOtherFacilities() { return otherFacilities; }
    public void setOtherFacilities(String otherFacilities) { this.otherFacilities = otherFacilities; }
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal pricePerHour) { this.pricePerHour = pricePerHour; }
    public BigDecimal getWeekdayPrice() { return weekdayPrice; }
    public void setWeekdayPrice(BigDecimal weekdayPrice) { this.weekdayPrice = weekdayPrice; }
    public BigDecimal getWeekendPrice() { return weekendPrice; }
    public void setWeekendPrice(BigDecimal weekendPrice) { this.weekendPrice = weekendPrice; }
    public BigDecimal getSeasonalPrice() { return seasonalPrice; }
    public void setSeasonalPrice(BigDecimal seasonalPrice) { this.seasonalPrice = seasonalPrice; }
    public BigDecimal getSpecialEventPrice() { return specialEventPrice; }
    public void setSpecialEventPrice(BigDecimal specialEventPrice) { this.specialEventPrice = specialEventPrice; }
    public BigDecimal getAdditionalCharges() { return additionalCharges; }
    public void setAdditionalCharges(BigDecimal additionalCharges) { this.additionalCharges = additionalCharges; }
    public BigDecimal getDecorationCharges() { return decorationCharges; }
    public void setDecorationCharges(BigDecimal decorationCharges) { this.decorationCharges = decorationCharges; }
    public BigDecimal getCateringCharges() { return cateringCharges; }
    public void setCateringCharges(BigDecimal cateringCharges) { this.cateringCharges = cateringCharges; }
    public BigDecimal getAdvanceAmount() { return advanceAmount; }
    public void setAdvanceAmount(BigDecimal advanceAmount) { this.advanceAmount = advanceAmount; }
    public BigDecimal getHalfDayPrice() { return halfDayPrice; }
    public void setHalfDayPrice(BigDecimal halfDayPrice) { this.halfDayPrice = halfDayPrice; }
    public BigDecimal getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(BigDecimal securityDeposit) { this.securityDeposit = securityDeposit; }
    public String getCoverImageFileName() { return coverImageFileName; }
    public void setCoverImageFileName(String coverImageFileName) { this.coverImageFileName = coverImageFileName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
    public String getFlagReason() { return flagReason; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
