package com.worldofwonder.model;

public class User {

    private int id;
    private String username;
    private String email;
    private String password;
    private int totalPoints;
    private boolean isAdmin;
    private String lastClaimDate;

    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(int id, String username, String email, String password, int totalPoints) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.totalPoints = totalPoints;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getLastClaimDate() {
        return lastClaimDate;
    }

    public void setLastClaimDate(String lastClaimDate) {
        this.lastClaimDate = lastClaimDate;
    }

    public String getRankTitle() {
        if (totalPoints >= 600) return com.worldofwonder.util.I18n.get("rank_legendary");
        if (totalPoints >= 300) return com.worldofwonder.util.I18n.get("rank_gold");
        if (totalPoints >= 150) return com.worldofwonder.util.I18n.get("rank_silver");
        if (totalPoints >= 50) return com.worldofwonder.util.I18n.get("rank_bronze");
        return com.worldofwonder.util.I18n.get("rank_novice");
    }
}
