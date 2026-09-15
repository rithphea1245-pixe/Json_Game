package com.worldofwonder.model;

import com.worldofwonder.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Model repository managing user accounts and persistence in data/users.json using ArrayLists.
 */
public class UserRepository {

    private final List<User> users = new CopyOnWriteArrayList<>();
    private final File usersFile;

    public UserRepository() {
        File dataDir = GameRepository.resolveDataDirectory();
        this.usersFile = new File(dataDir, "users.json");
        loadUsers();
    }

    private synchronized void loadUsers() {
        users.clear();
        if (usersFile.exists()) {
            try {
                String json = JsonUtil.readFile(usersFile);
                List<Map<String, Object>> list = JsonUtil.parseList(json);
                for (Map<String, Object> map : list) {
                    User user = new User();
                    user.setId(JsonUtil.getAsInt(map, "id", 0));
                    user.setUsername(JsonUtil.getAsString(map, "username", ""));
                    user.setEmail(JsonUtil.getAsString(map, "email", ""));
                    user.setPassword(JsonUtil.getAsString(map, "password", ""));
                    user.setTotalPoints(JsonUtil.getAsInt(map, "totalPoints", 0));
                    user.setAdmin(JsonUtil.getAsBoolean(map, "isAdmin", false));
                    user.setLastClaimDate(JsonUtil.getAsString(map, "lastClaimDate", null));
                    users.add(user);
                }
            } catch (IOException e) {
                System.err.println("Failed to load users.json: " + e.getMessage());
            }
        }

        // Default initial admin & demo user if file is empty
        if (users.isEmpty()) {
            User admin = new User(1, "admin", "admin@example.com", "admin67", 30);
            admin.setAdmin(true);
            User bob = new User(2, "bob", "bob@example.com", "password456", 10);
            bob.setAdmin(false);
            users.add(admin);
            users.add(bob);
            saveUsers();
        }
    }

    private synchronized void saveUsers() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("email", u.getEmail());
            map.put("password", u.getPassword());
            map.put("totalPoints", u.getTotalPoints());
            map.put("isAdmin", u.isAdmin());
            if (u.getLastClaimDate() != null) {
                map.put("lastClaimDate", u.getLastClaimDate());
            }
            list.add(map);
        }
        try {
            JsonUtil.writeToFile(usersFile, JsonUtil.toPrettyJson(list));
        } catch (IOException e) {
            System.err.println("Failed to save users.json: " + e.getMessage());
        }
    }

    public User findById(int id) {
        for (User u : users) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    public User findByUsername(String username) {
        if (username == null) return null;
        for (User u : users) {
            if (username.equalsIgnoreCase(u.getUsername())) {
                return u;
            }
        }
        return null;
    }

    public synchronized boolean createUser(User user) {
        for (User existing : users) {
            if (user.getUsername() != null && user.getUsername().equalsIgnoreCase(existing.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(existing.getEmail())) {
                throw new IllegalArgumentException("Email is already registered");
            }
        }

        int maxId = 0;
        for (User existing : users) {
            if (existing.getId() > maxId) {
                maxId = existing.getId();
            }
        }
        user.setId(maxId + 1);
        users.add(user);
        saveUsers();
        return true;
    }

    public synchronized boolean updateTotalPoints(int userId, int totalPoints) {
        User user = findById(userId);
        if (user != null) {
            user.setTotalPoints(totalPoints);
            saveUsers();
            return true;
        }
        return false;
    }

    public synchronized int claimDailyBonus(int userId, int bonusPoints) {
        User user = findById(userId);
        if (user == null) {
            return -1;
        }
        String today = java.time.LocalDate.now().toString();
        if (today.equals(user.getLastClaimDate())) {
            return -2; // Already claimed today
        }
        user.setLastClaimDate(today);
        user.setTotalPoints(user.getTotalPoints() + bonusPoints);
        saveUsers();
        return user.getTotalPoints();
    }

    public synchronized boolean deleteById(int userId) {
        User target = findById(userId);
        if (target != null) {
            users.remove(target);
            saveUsers();
            return true;
        }
        return false;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public synchronized boolean updateUser(User updated) {
        if (updated == null) return false;
        User existing = findById(updated.getId());
        if (existing == null) {
            return false;
        }
        for (User u : users) {
            if (u.getId() != updated.getId()) {
                if (updated.getUsername() != null && updated.getUsername().equalsIgnoreCase(u.getUsername())) {
                    throw new IllegalArgumentException("Username already in use by another account");
                }
                if (updated.getEmail() != null && !updated.getEmail().trim().isEmpty() && updated.getEmail().equalsIgnoreCase(u.getEmail())) {
                    throw new IllegalArgumentException("Email already in use by another account");
                }
            }
        }
        if (updated.getUsername() != null && !updated.getUsername().trim().isEmpty()) {
            existing.setUsername(updated.getUsername());
        }
        if (updated.getEmail() != null) {
            existing.setEmail(updated.getEmail());
        }
        if (updated.getPassword() != null && !updated.getPassword().trim().isEmpty()) {
            existing.setPassword(updated.getPassword());
        }
        existing.setTotalPoints(updated.getTotalPoints());
        existing.setAdmin(updated.isAdmin());
        if (updated.getLastClaimDate() != null) {
            existing.setLastClaimDate(updated.getLastClaimDate());
        }
        saveUsers();
        return true;
    }

    public List<User> getLeaderboard(int limit) {
        List<User> eligible = new ArrayList<>();
        for (User u : users) {
            if (!u.isAdmin()) {
                eligible.add(u);
            }
        }
        eligible.sort(Comparator.comparingInt(User::getTotalPoints).reversed()
                .thenComparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));

        if (limit > 0 && eligible.size() > limit) {
            return new ArrayList<>(eligible.subList(0, limit));
        }
        return eligible;
    }
}

