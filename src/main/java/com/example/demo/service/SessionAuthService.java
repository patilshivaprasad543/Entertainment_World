package com.example.demo.service;

import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.model.VerificationStatus;
import com.example.demo.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

/**
 * Server-side HttpSession authorization helper.
 * Never trusts role or user IDs supplied by browser headers or parameters.
 */
@Service
public class SessionAuthService {

    public static final String SESSION_USER_ID = "EW_USER_ID";
    public static final String SESSION_USER_ROLE = "EW_USER_ROLE";

    private final UserAccountRepository userAccountRepository;

    public SessionAuthService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public void establishSession(HttpServletRequest request, UserAccount user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_ID, user.getId());
        session.setAttribute(SESSION_USER_ROLE, user.getRole().name());
    }

    public void clearSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public java.util.Optional<UserAccount> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER_ID) == null) {
            return java.util.Optional.empty();
        }
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        return userAccountRepository.findById(userId).filter(u -> !u.isSuspended());
    }

    public UserAccount requireUser(HttpServletRequest request, UserRole... allowedRoles) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER_ID) == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is no longer valid."));

        if (user.isSuspended()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account is currently suspended by Admin.");
        }

        if (allowedRoles != null && allowedRoles.length > 0) {
            boolean matches = Arrays.stream(allowedRoles).anyMatch(r -> r == user.getRole());
            if (!matches) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + user.getRole().getDisplayName());
            }
        }
        return user;
    }
}
