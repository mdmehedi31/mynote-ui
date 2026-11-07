package com.mynote.config;


import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean isAuthenticated() {
        Authentication auth = getAuthentication();
        return auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser");
    }

    public String getCurrentUsername() {
        Authentication auth = getAuthentication();
        if (auth != null && isAuthenticated()) {
            return auth.getName();
        }
        return null;
    }

    public String getCurrentUserId() {
        Object userId = VaadinSession.getCurrent().getAttribute("user_id");
        return userId != null ? userId.toString() : null;
    }

    public boolean hasRole(String role) {
        Authentication auth = getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role)
                        || a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}
