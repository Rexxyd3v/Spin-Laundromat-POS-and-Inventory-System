package src.model;

import java.time.LocalDate;

/**
 * User model class
 */
public class User {
    private String userId;
    private String name;
    private String contactNumber;
    private UserRole role;
    private boolean active;
    private LocalDate createdDate;
    private LocalDate lastLoginDate;

    public enum UserRole {
        ADMIN("Admin"),
        STAFF("Staff");

        private final String displayName;

        UserRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public User(String userId, String name, String contactNumber, UserRole role,
            boolean active, LocalDate createdDate, LocalDate lastLoginDate) {
        this.userId = userId;
        this.name = name;
        this.contactNumber = contactNumber;
        this.role = role;
        this.active = active;
        this.createdDate = createdDate;
        this.lastLoginDate = lastLoginDate;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public LocalDate getLastLoginDate() {
        return lastLoginDate;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastLoginDate(LocalDate lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getInitials() {
        if (name == null || name.isEmpty())
            return "?";
        String[] parts = name.split(" ");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
