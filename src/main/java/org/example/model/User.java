package org.example.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Kullanıcı bilgilerini temsil eden model sınıfı
 */
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean active;
    private LocalDateTime createdAt;

    // Varsayılan constructor
    public User() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    // Parametreli constructor
    public User(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getter ve Setter metodları
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Email validasyonu için yardımcı metod
    public boolean hasValidEmail() {
        return email != null && email.contains("@") && email.contains(".");
    }

    // Kullanıcının tam adını döndüren metod
    public String getDisplayName() {
        return username != null ? username : "Unknown User";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}