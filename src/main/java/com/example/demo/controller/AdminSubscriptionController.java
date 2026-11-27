package com.example.demo.controller;

import com.example.demo.model.PlatformCommission;
import com.example.demo.model.SupportTicket;
import com.example.demo.model.UserRole;
import com.example.demo.repository.SupportTicketRepository;
import com.example.demo.service.SessionAuthService;
import com.example.demo.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminSubscriptionController {

    private final SessionAuthService sessionAuthService;
    private final SubscriptionService subscriptionService;
    private final SupportTicketRepository supportTicketRepository;

    public AdminSubscriptionController(
            SessionAuthService sessionAuthService,
            SubscriptionService subscriptionService,
            SupportTicketRepository supportTicketRepository
    ) {
        this.sessionAuthService = sessionAuthService;
        this.subscriptionService = subscriptionService;
        this.supportTicketRepository = supportTicketRepository;
    }

    @GetMapping("/subscriptions/plans")
    public List<Map<String, Object>> listPlans(HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return subscriptionService.adminListAllPlans();
    }

    @PostMapping("/subscriptions/plans")
    public Map<String, Object> createPlan(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return subscriptionService.adminSavePlan(null, body);
    }

    @PutMapping("/subscriptions/plans/{id}")
    public Map<String, Object> updatePlan(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return subscriptionService.adminSavePlan(id, body);
    }

    @PatchMapping("/subscriptions/plans/{id}/toggle")
    public Map<String, Object> togglePlan(@PathVariable Long id, HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return subscriptionService.adminTogglePlan(id);
    }

    @GetMapping("/subscriptions/users")
    public List<Map<String, Object>> listUserSubscriptions(HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return subscriptionService.adminListUserSubscriptions();
    }

    @GetMapping("/subscriptions/commissions")
    public List<PlatformCommission> listCommissions(HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return subscriptionService.adminListCommissions();
    }

    @PutMapping("/subscriptions/commissions/{id}")
    public PlatformCommission updateCommission(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        BigDecimal pct = body.get("commissionPercent") == null ? null : new BigDecimal(String.valueOf(body.get("commissionPercent")));
        String desc = body.get("description") == null ? null : String.valueOf(body.get("description"));
        return subscriptionService.adminUpdateCommission(id, pct, desc);
    }

    @GetMapping("/subscriptions/analytics")
    public Map<String, Object> analytics(HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return subscriptionService.adminSubscriptionAnalytics();
    }

    @GetMapping("/support-tickets")
    public List<Map<String, Object>> listSupportTickets(HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        return supportTicketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "userName", t.getUser().getFullName(),
                        "userRole", t.getUser().getRole().getDisplayName(),
                        "subject", t.getSubject(),
                        "description", t.getDescription(),
                        "status", t.getStatus(),
                        "adminResponse", t.getAdminResponse() == null ? "" : t.getAdminResponse(),
                        "createdAt", String.valueOf(t.getCreatedAt())
                ))
                .toList();
    }

    @PostMapping("/support-tickets/{id}/resolve")
    public SupportTicket resolveTicket(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        sessionAuthService.requireUser(request, UserRole.ADMIN);
        SupportTicket ticket = supportTicketRepository.findById(id).orElseThrow();
        ticket.setStatus("RESOLVED");
        ticket.setAdminResponse(body == null ? "Resolved by Super Admin" : body.getOrDefault("response", "Resolved by Super Admin"));
        return supportTicketRepository.save(ticket);
    }
}
