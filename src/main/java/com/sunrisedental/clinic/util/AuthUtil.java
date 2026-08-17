package com.sunrisedental.clinic.util;

import com.sunrisedental.clinic.model.User;
import jakarta.servlet.http.HttpSession;

public final class AuthUtil {

    private AuthUtil() {
    }

    public static User requireUser(HttpSession session) {
        if (session == null) {
            throw new SecurityException("Session expired. Please log in again.");
        }
        User user = (User) session.getAttribute(SessionConstants.LOGGED_IN_USER);
        if (user == null) {
            throw new SecurityException("Please log in to continue.");
        }
        return user;
    }

    public static User requireAdmin(HttpSession session) {
        User user = requireUser(session);
        if (!RoleConstants.isAdmin(user.getRole())) {
            throw new SecurityException("Admin access required.");
        }
        return user;
    }
}
