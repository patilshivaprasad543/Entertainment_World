package com.example.demo.model;

public enum UserRole {
    ADMIN("Admin", "/dashboards/admin.html"),
    ACTOR("Professionals", "/dashboards/actors.html"),
    VENDOR("Vendors Management", "/dashboards/vendors.html"),
    EVENT_MANAGEMENT("Event Management", "/dashboards/events.html"),
    CASTING("Casting Productions", "/dashboards/casting.html"),
    BRAND("Brand Promotions", "/dashboards/brands.html");

    private final String displayName;
    private final String dashboardUrl;

    UserRole(String displayName, String dashboardUrl) {
        this.displayName = displayName;
        this.dashboardUrl = dashboardUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }
}
