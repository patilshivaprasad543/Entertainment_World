package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.SessionAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/platform")
public class PlatformSocialController {

    private final SessionAuthService sessionAuthService;
    private final UserAccountRepository userAccountRepository;
    private final SavedWishlistItemRepository wishlistRepository;
    private final UserFollowRepository followRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final VendorNotificationRepository notificationRepository;

    public PlatformSocialController(
            SessionAuthService sessionAuthService,
            UserAccountRepository userAccountRepository,
            SavedWishlistItemRepository wishlistRepository,
            UserFollowRepository followRepository,
            ChatMessageRepository chatMessageRepository,
            SupportTicketRepository supportTicketRepository,
            VendorNotificationRepository notificationRepository
    ) {
        this.sessionAuthService = sessionAuthService;
        this.userAccountRepository = userAccountRepository;
        this.wishlistRepository = wishlistRepository;
        this.followRepository = followRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.notificationRepository = notificationRepository;
    }

    // ==================== WISHLIST / SAVED ====================

    @GetMapping("/wishlist")
    public List<SavedWishlistItem> listWishlist(HttpServletRequest request) {
        UserAccount user = sessionAuthService.requireUser(request);
        return wishlistRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @PostMapping("/wishlist")
    public SavedWishlistItem saveToWishlist(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount user = sessionAuthService.requireUser(request);
        String itemType = String.valueOf(body.getOrDefault("itemType", "SERVICE"));
        Long targetId = Long.valueOf(String.valueOf(body.get("targetId")));
        if (wishlistRepository.existsByUserAndItemTypeAndTargetId(user, itemType, targetId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already saved in your wishlist.");
        }
        SavedWishlistItem item = new SavedWishlistItem();
        item.setUser(user);
        item.setItemType(itemType);
        item.setTargetId(targetId);
        item.setTitle(String.valueOf(body.getOrDefault("title", "Saved Item")));
        item.setSubtitle(body.get("subtitle") == null ? null : String.valueOf(body.get("subtitle")));
        item.setCreatedAt(LocalDateTime.now());
        return wishlistRepository.save(item);
    }

    @DeleteMapping("/wishlist/{id}")
    public Map<String, Boolean> removeWishlist(@PathVariable Long id, HttpServletRequest request) {
        UserAccount user = sessionAuthService.requireUser(request);
        SavedWishlistItem item = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved item not found."));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }
        wishlistRepository.delete(item);
        return Map.of("deleted", true);
    }

    // ==================== FOLLOWERS & FOLLOWING ====================

    @GetMapping("/directory")
    public List<Map<String, Object>> directory(HttpServletRequest request) {
        UserAccount me = sessionAuthService.requireUser(request);
        return userAccountRepository.findAll().stream()
                .filter(u -> !u.getId().equals(me.getId()))
                .filter(u -> u.getRole() != UserRole.ADMIN && u.getRole() != UserRole.INFLUENCER)
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("fullName", u.getFullName());
                    m.put("organizationName", u.getOrganizationName());
                    m.put("role", u.getRole().name());
                    m.put("roleDisplayName", u.getRole().getDisplayName());
                    m.put("followersCount", followRepository.countByFollowing(u));
                    m.put("following", followRepository.findByFollowerAndFollowing(me, u).isPresent());
                    return m;
                })
                .toList();
    }

    @PostMapping("/follow/{targetId}")
    public Map<String, Object> follow(@PathVariable Long targetId, HttpServletRequest request) {
        UserAccount me = sessionAuthService.requireUser(request);
        UserAccount target = userAccountRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        if (me.getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself.");
        }
        if (followRepository.findByFollowerAndFollowing(me, target).isEmpty()) {
            followRepository.save(new UserFollow(me, target));
            notificationRepository.save(new VendorNotification(
                    target,
                    "New Follower",
                    me.getFullName() + " (" + me.getRole().getDisplayName() + ") started following you.",
                    "NEW_FOLLOWER",
                    me.getId()
            ));
        }
        return Map.of("following", true, "followersCount", followRepository.countByFollowing(target));
    }

    @DeleteMapping("/follow/{targetId}")
    public Map<String, Object> unfollow(@PathVariable Long targetId, HttpServletRequest request) {
        UserAccount me = sessionAuthService.requireUser(request);
        UserAccount target = userAccountRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        followRepository.findByFollowerAndFollowing(me, target).ifPresent(followRepository::delete);
        return Map.of("following", false, "followersCount", followRepository.countByFollowing(target));
    }

    // ==================== SHARED CHAT ====================

    @GetMapping("/chat/{otherUserId}")
    public List<Map<String, Object>> getConversation(@PathVariable Long otherUserId, HttpServletRequest request) {
        UserAccount me = sessionAuthService.requireUser(request);
        UserAccount other = userAccountRepository.findById(otherUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient not found."));
        return chatMessageRepository.findConversation(me, other).stream()
                .map(m -> Map.<String, Object>of(
                        "id", m.getId(),
                        "senderId", m.getSender().getId(),
                        "senderName", m.getSender().getFullName(),
                        "messageText", m.getMessageText(),
                        "sentAt", String.valueOf(m.getSentAt())
                ))
                .toList();
    }

    @PostMapping("/chat")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        UserAccount me = sessionAuthService.requireUser(request);
        Long recipientId = Long.valueOf(String.valueOf(body.get("recipientId")));
        String text = String.valueOf(body.getOrDefault("messageText", "")).trim();
        if (text.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty.");
        }
        UserAccount recipient = userAccountRepository.findById(recipientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient not found."));

        ChatMessage msg = new ChatMessage();
        msg.setSender(me);
        msg.setRecipient(recipient);
        msg.setContextType(String.valueOf(body.getOrDefault("contextType", "GENERAL")));
        msg.setMessageText(text);
        msg.setSentAt(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(msg);

        notificationRepository.save(new VendorNotification(
                recipient,
                "New Message from " + me.getFullName(),
                text.length() > 100 ? text.substring(0, 100) + "..." : text,
                "NEW_MESSAGE",
                me.getId()
        ));

        return Map.of(
                "id", saved.getId(),
                "senderId", me.getId(),
                "senderName", me.getFullName(),
                "messageText", saved.getMessageText(),
                "sentAt", String.valueOf(saved.getSentAt())
        );
    }

    // ==================== SUPPORT TICKETS ====================

    @GetMapping("/support")
    public List<SupportTicket> myTickets(HttpServletRequest request) {
        UserAccount me = sessionAuthService.requireUser(request);
        return supportTicketRepository.findByUserOrderByCreatedAtDesc(me);
    }

    @PostMapping("/support")
    public SupportTicket createTicket(@RequestBody Map<String, String> body, HttpServletRequest request) {
        UserAccount me = sessionAuthService.requireUser(request);
        SupportTicket t = new SupportTicket();
        t.setUser(me);
        t.setSubject(body.getOrDefault("subject", "Support Request"));
        t.setDescription(body.getOrDefault("description", ""));
        t.setStatus("OPEN");
        t.setCreatedAt(LocalDateTime.now());
        return supportTicketRepository.save(t);
    }
}
