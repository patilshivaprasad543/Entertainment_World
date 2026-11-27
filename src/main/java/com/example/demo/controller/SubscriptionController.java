package com.example.demo.controller;

import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.service.SessionAuthService;
import com.example.demo.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController("demoSubscriptionController")
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SessionAuthService sessionAuthService;
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SessionAuthService sessionAuthService, SubscriptionService subscriptionService) {
        this.sessionAuthService = sessionAuthService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/plans")
    public List<Map<String, Object>> publicPlans(@RequestParam(required = false) String role) {
        UserRole parsed = (role == null || role.isBlank()) ? null : UserRole.valueOf(role.trim().toUpperCase());
        return subscriptionService.getPublicPlans(parsed);
    }

    @GetMapping("/my")
    public Map<String, Object> mySubscription(HttpServletRequest request) {
        UserAccount user = sessionAuthService.requireUser(request);
        return subscriptionService.getMySubscriptionDashboard(user);
    }

    @PostMapping("/subscribe")
    public Map<String, Object> subscribe(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount user = sessionAuthService.requireUser(request);
        Long planId = Long.valueOf(String.valueOf(body.get("planId")));
        String billingCycle = body.get("billingCycle") == null ? "MONTHLY" : String.valueOf(body.get("billingCycle"));
        String paymentReference = body.get("paymentReference") == null ? null : String.valueOf(body.get("paymentReference"));
        return subscriptionService.subscribe(user, planId, billingCycle, paymentReference);
    }

    @PostMapping("/cancel")
    public Map<String, Object> cancel(HttpServletRequest request) {
        UserAccount user = sessionAuthService.requireUser(request);
        return subscriptionService.cancelActiveSubscription(user);
    }
}
