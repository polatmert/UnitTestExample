package org.example.service;

import org.example.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kullanıcı işlemlerini yöneten ana service sınıfı
 * Bu sınıf DatabaseService ve EmailService'i kullanır (Dependency Injection)
 */
public class UserService {
    private final DatabaseService databaseService;
    private final EmailService emailService;

    public UserService(DatabaseService databaseService, EmailService emailService) {
        this.databaseService = databaseService;
        this.emailService = emailService;
    }

    /**
     * Yeni kullanıcı kaydı yapar
     */
    public User registerUser(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Kullanıcı adı boş olamaz");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Şifre en az 6 karakter olmalı");
        }

        // Email zaten kayıtlı mı kontrol et
        if (databaseService.isEmailExists(email)) {
            throw new IllegalStateException("Bu email adresi zaten kayıtlı");
        }

        // Yeni kullanıcı oluştur
        User user = new User(username, email, password);
        
        // Veritabanına kaydet
        User savedUser = databaseService.saveUser(user);
        
        // Hoş geldin emaili gönder
        try {
            emailService.sendWelcomeEmail(savedUser);
        } catch (Exception e) {
            // Email gönderimi başarısız olsa da kullanıcı kaydı devam eder
            System.err.println("Hoş geldin emaili gönderilemedi: " + e.getMessage());
        }

        return savedUser;
    }

    /**
     * Kullanıcı girişi yapar
     */
    public Optional<User> loginUser(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }

        Optional<User> userOpt = databaseService.findUserByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Basit şifre kontrolü (gerçek uygulamada hash karşılaştırması yapılır)
            if (user.getPassword().equals(password) && user.isActive()) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    /**
     * Kullanıcı profilini günceller
     */
    public User updateUserProfile(Long userId, String newUsername, String newEmail) {
        User user = databaseService.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        boolean emailChanged = false;
        
        // Username güncelleme
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            user.setUsername(newUsername);
        }

        // Email güncelleme
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (!user.hasValidEmail()) {
                throw new IllegalArgumentException("Geçerli email adresi gerekli");
            }
            
            // Yeni email zaten kayıtlı mı kontrol et
            if (databaseService.isEmailExists(newEmail)) {
                throw new IllegalStateException("Bu email adresi zaten kullanılıyor");
            }
            
            user.setEmail(newEmail);
            emailChanged = true;
        }

        User updatedUser = databaseService.updateUser(user);

        // Email değiştiyse bildirim gönder
        if (emailChanged) {
            try {
                emailService.sendNotificationEmail(updatedUser, "Email adresiniz başarıyla güncellendi.");
            } catch (Exception e) {
                System.err.println("Email güncelleme bildirimi gönderilemedi: " + e.getMessage());
            }
        }

        return updatedUser;
    }

    /**
     * Şifre sıfırlama token'ı oluşturur ve email gönderir
     */
    public String requestPasswordReset(String email) {
        User user = databaseService.findUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Bu email adresi ile kayıtlı kullanıcı bulunamadı"));

        if (!user.isActive()) {
            throw new IllegalStateException("Pasif kullanıcılar için şifre sıfırlama yapılamaz");
        }

        // Reset token oluştur
        String resetToken = generateResetToken();
        
        // Email gönder
        boolean emailSent = emailService.sendPasswordResetEmail(user, resetToken);
        
        if (!emailSent) {
            throw new RuntimeException("Şifre sıfırlama emaili gönderilemedi");
        }

        return resetToken;
    }

    /**
     * Kullanıcıyı pasif yapar (soft delete)
     */
    public void deactivateUser(Long userId) {
        User user = databaseService.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        user.setActive(false);
        databaseService.updateUser(user);

        try {
            emailService.sendNotificationEmail(user, "Hesabınız pasifleştirildi.");
        } catch (Exception e) {
            System.err.println("Pasifleştirme bildirimi gönderilemedi: " + e.getMessage());
        }
    }

    /**
     * Kullanıcıyı aktif yapar
     */
    public void activateUser(Long userId) {
        User user = databaseService.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        user.setActive(true);
        databaseService.updateUser(user);

        try {
            emailService.sendNotificationEmail(user, "Hesabınız tekrar aktifleştirildi.");
        } catch (Exception e) {
            System.err.println("Aktifleştirme bildirimi gönderilemedi: " + e.getMessage());
        }
    }

    /**
     * Kullanıcı detaylarını getirir
     */
    public Optional<User> getUserById(Long userId) {
        return databaseService.findUserById(userId);
    }

    /**
     * Email adresine göre kullanıcı getirir
     */
    public Optional<User> getUserByEmail(String email) {
        return databaseService.findUserByEmail(email);
    }

    /**
     * Tüm aktif kullanıcıları getirir
     */
    public List<User> getAllActiveUsers() {
        return databaseService.getActiveUsers();
    }

    /**
     * Toplu email gönderimi yapar
     */
    public int sendBulkNotification(String subject, String message) {
        List<User> activeUsers = databaseService.getActiveUsers();
        return emailService.sendBulkEmail(activeUsers, subject, message);
    }

    /**
     * Kullanıcı istatistiklerini getirir
     */
    public UserStats getUserStats() {
        List<User> allUsers = databaseService.getAllUsers();
        List<User> activeUsers = databaseService.getActiveUsers();
        
        return new UserStats(
                allUsers.size(),
                activeUsers.size(),
                allUsers.size() - activeUsers.size()
        );
    }

    /**
     * Şifre sıfırlama token'ı oluşturur
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Kullanıcı istatistiklerini tutan sınıf
     */
    public static class UserStats {
        private final int totalUsers;
        private final int activeUsers;
        private final int inactiveUsers;

        public UserStats(int totalUsers, int activeUsers, int inactiveUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
        }

        public int getTotalUsers() {
            return totalUsers;
        }

        public int getActiveUsers() {
            return activeUsers;
        }

        public int getInactiveUsers() {
            return inactiveUsers;
        }

        @Override
        public String toString() {
            return String.format("UserStats{toplam=%d, aktif=%d, pasif=%d}", 
                    totalUsers, activeUsers, inactiveUsers);
        }
    }
}