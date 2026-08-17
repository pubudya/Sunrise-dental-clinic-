package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.dao.UserDao;
import com.sunrisedental.clinic.model.User;
import com.sunrisedental.clinic.util.PasswordUtil;
import com.sunrisedental.clinic.util.RoleConstants;
import com.sunrisedental.clinic.util.ValidationUtil;

import java.util.List;
import java.util.Map;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> getAllUsers() {
        return userDao.findAllStaff();
    }

    public User updateStaff(User incoming, int adminId) {
        User existing = userDao.findById(incoming.getId())
                .orElseThrow(() -> new IllegalArgumentException("Staff account not found."));

        if (existing.getId() == adminId && !incoming.isActive()) {
            throw new IllegalArgumentException("You cannot deactivate your own account.");
        }
        if (RoleConstants.isAdmin(existing.getRole()) && existing.getId() != adminId) {
            throw new IllegalArgumentException("Only one admin account can be managed safely at a time.");
        }

        existing.setFullName(ValidationUtil.requireNonBlank(incoming.getFullName(), "Full name"));
        existing.setMobileNumber(ValidationUtil.requireMobile(incoming.getMobileNumber(), "Mobile number"));
        existing.setActive(incoming.isActive());
        userDao.update(existing);
        return existing;
    }

    public void resetPassword(int userId, String newPassword, int adminId) {
        if (userId == adminId) {
            throw new IllegalArgumentException("Use profile settings to change your own password.");
        }
        if (ValidationUtil.isBlank(newPassword) || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        userDao.findById(userId).orElseThrow(() -> new IllegalArgumentException("Staff account not found."));
        userDao.updatePassword(userId, PasswordUtil.hash(newPassword));
    }

    public void deleteStaff(int userId, int adminId) {
        if (userId == adminId) {
            throw new IllegalArgumentException("You cannot delete your own account.");
        }
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Staff account not found."));
        if (RoleConstants.isAdmin(user.getRole())) {
            throw new IllegalArgumentException("Admin account cannot be deleted.");
        }
        userDao.delete(userId);
    }

    public Map<String, Object> toPublicView(User user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "mobileNumber", user.getMobileNumber(),
                "role", user.getRole(),
                "active", user.isActive()
        );
    }
}
