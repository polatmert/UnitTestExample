package org.example;

import org.example.model.User;
import org.example.service.DatabaseService;
import org.example.service.EmailService;
import org.example.service.UserService;

/**
 * JUnit ve Mockito Öğrenme Projesi
 * 
 * Bu proje Java'da test yazma tekniklerini öğrenmek için tasarlanmıştır.
 * Ana odak noktaları:
 * - JUnit 5 ile unit testing
 * - Mockito ile mocking
 * - Integration testing
 * - Test best practices
 */
public class Main {
    public static void main(String[] args) {
        
        DatabaseService databaseService = new DatabaseService();
        EmailService emailService = new EmailService();
        UserService userService = new UserService(databaseService, emailService);
        
        try {
            // Örnek kullanıcı işlemleri
            System.out.println("1. Kullanıcı kaydı yapılıyor...");
            User user = userService.registerUser("örnek_kullanıcı", "test@example.com", "password123");
            System.out.println("✓ Kullanıcı kaydedildi: " + user.getDisplayName());
            
            System.out.println("\n2. Kullanıcı girişi test ediliyor...");
            var loginResult = userService.loginUser("test@example.com", "password123");
            if (loginResult.isPresent()) {
                System.out.println("✓ Giriş başarılı: " + loginResult.get().getDisplayName());
            } else {
                System.out.println("✗ Giriş başarısız");
            }
            
            System.out.println("\n3. Profil güncelleniyor...");
            User updatedUser = userService.updateUserProfile(user.getId(), "yeni_kullanıcı_adı", null);
            System.out.println("✓ Profil güncellendi: " + updatedUser.getDisplayName());
            
            System.out.println("\n4. Kullanıcı istatistikleri:");
            var stats = userService.getUserStats();
            System.out.println("   - Toplam kullanıcı: " + stats.getTotalUsers());
            System.out.println("   - Aktif kullanıcı: " + stats.getActiveUsers());
            System.out.println("   - Pasif kullanıcı: " + stats.getInactiveUsers());
            
            System.out.println("\n5. Email logları:");
            var emailLogs = emailService.getSentEmails();
            System.out.println("   - Gönderilen email sayısı: " + emailLogs.size());
            for (var emailLog : emailLogs) {
                System.out.println("   - " + emailLog.getSubject() + " -> " + emailLog.getTo());
            }
            
        } catch (Exception e) {
            System.err.println("Hata: " + e.getMessage());
        }
    }
}