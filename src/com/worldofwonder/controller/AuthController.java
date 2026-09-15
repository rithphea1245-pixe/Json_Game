package com.worldofwonder.controller;

import com.worldofwonder.model.User;
import com.worldofwonder.model.UserRepository;

/**
 * Controller handling user authentication, session state, and input validation.
 */
public class AuthController {

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;

        public AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }

    private final UserRepository userRepository;
    private User currentUser;

    public AuthController() {
        this(new UserRepository());
    }

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResult login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, "Username cannot be empty", null);
        }
        if (password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Password cannot be empty", null);
        }

        String trimmedUser = username.trim();

        // Hardcoded admin quick-access check
        if ("admin".equalsIgnoreCase(trimmedUser) && "hengheng168".equals(password)) {
            User admin = userRepository.findByUsername("admin");
            if (admin == null) {
                admin = new User(1, "admin", "admin@worldofwonder.com", "hengheng168", 100);
                admin.setAdmin(true);
            }
            this.currentUser = admin;
            return new AuthResult(true, "Admin login successful", admin);
        }

        User user = userRepository.findByUsername(trimmedUser);
        if (user == null) {
            return new AuthResult(false, "User not found", null);
        }

        if (!password.equals(user.getPassword())) {
            return new AuthResult(false, "Incorrect password", null);
        }

        this.currentUser = user;
        return new AuthResult(true, "Login successful", user);
    }

    public AuthResult register(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, "Username is required", null);
        }
        if (email == null || email.trim().isEmpty()) {
            return new AuthResult(false, "Email is required", null);
        }
        if (password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Password is required", null);
        }

        String cleanUsername = username.trim();
        String cleanEmail = email.trim();

        if (cleanUsername.length() < 3) {
            return new AuthResult(false, "Username must be at least 3 characters", null);
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return new AuthResult(false, "Invalid email format", null);
        }
        if (password.length() < 4) {
            return new AuthResult(false, "Password must be at least 4 characters", null);
        }

        try {
            User newUser = new User(cleanUsername, cleanEmail, password);
            userRepository.createUser(newUser);
            this.currentUser = newUser;
            return new AuthResult(true, "Registration successful", newUser);
        } catch (IllegalArgumentException e) {
            return new AuthResult(false, e.getMessage(), null);
        } catch (Exception e) {
            return new AuthResult(false, "Failed to register: " + e.getMessage(), null);
        }
    }

    public User createGuestSession() {
        this.currentUser = new User(0, "Guest", "", "", 0);
        return currentUser;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }
}

