package com.sunrisedental.clinic.service;

import com.sunrisedental.clinic.dao.UserDao;
import com.sunrisedental.clinic.model.StaffRegistrationForm;
import com.sunrisedental.clinic.model.User;
import com.sunrisedental.clinic.util.PasswordUtil;
import com.sunrisedental.clinic.util.RoleConstants;
import com.sunrisedental.clinic.util.ValidationUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthService {

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> authenticate(String username, String password) {
        if (ValidationUtil.isBlank(username) || ValidationUtil.isBlank(password)) {
            return Optional.empty();
        }
        Optional<User> user = userDao.findByUsername(username.trim());
        if (user.isEmpty() || !user.get().isActive()) {
            return Optional.empty();
        }
        if (!PasswordUtil.matches(password, user.get().getPassword())) {
            return Optional.empty();
        }
        return user;
    }

    public User registerStaff(StaffRegistrationForm form) {
        validateRegistration(form);

        if (userDao.existsByUsername(form.getUsername().trim())) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userDao.existsByMobile(form.getMobileNumber().trim())) {
            throw new IllegalArgumentException("Mobile number is already registered.");
        }

        User user = new User();
        user.setUsername(form.getUsername().trim());
        user.setPassword(PasswordUtil.hash(form.getPassword()));
        user.setFullName(form.getFullName().trim());
        user.setMobileNumber(form.getMobileNumber().trim());
        user.setRole(RoleConstants.STAFF);
        user.setActive(true);
        userDao.insert(user);
        return user;
    }

    public Map<String, Object> toSessionUser(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("fullName", user.getFullName());
        data.put("mobileNumber", user.getMobileNumber());
        data.put("role", user.getRole());
        data.put("active", user.isActive());
        data.put("dashboardPath", RoleConstants.isAdmin(user.getRole())
                ? "admin-dashboard.html" : "dashboard.html");
        return data;
    }

    private void validateRegistration(StaffRegistrationForm form) {
        ValidationUtil.requireNonBlank(form.getUsername(), "Username");
        ValidationUtil.requireNonBlank(form.getPassword(), "Password");
        ValidationUtil.requireNonBlank(form.getConfirmPassword(), "Confirm password");
        ValidationUtil.requireNonBlank(form.getFullName(), "Full name");
        ValidationUtil.requireMobile(form.getMobileNumber(), "Mobile number");

        if (form.getUsername().trim().length() < 4) {
            throw new IllegalArgumentException("Username must be at least 4 characters.");
        }
        if (form.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match.");
        }
    }
}
