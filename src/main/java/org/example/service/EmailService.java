package org.example.service;

import org.example.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Email gönderimi işlemlerini yöneten service sınıfı
 * Gerçek uygulamada SMTP veya email API'leri kullanılır
 */
public class EmailService {
    private final List<EmailLog> sentEmails = new ArrayList<>();
    private boolean emailServiceEnabled = true;

    /**
     * Kullanıcıya hoş geldin emaili gönderir
     */
    public boolean sendWelcomeEmail(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Kullanıcı null olamaz");
        }

        if (!emailServiceEnabled) {
            return false;
        }

        if (user.getEmail() == null || !user.hasValidEmail()) {
            throw new IllegalArgumentException("Geçerli email adresi gerekli");
        }

        String subject = "Hoş Geldiniz!";
        String content = String.format("Merhaba %s, sistemimize hoş geldiniz!", user.getDisplayName());
        
        return sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Kullanıcıya şifre sıfırlama emaili gönderir
     */
    public boolean sendPasswordResetEmail(User user, String resetToken) {
        if (user == null) {
            throw new IllegalArgumentException("Kullanıcı null olamaz");
        }

        if (resetToken == null || resetToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Reset token boş olamaz");
        }

        if (!emailServiceEnabled) {
            return false;
        }

        if (!user.hasValidEmail()) {
            throw new IllegalArgumentException("Geçerli email adresi gerekli");
        }

        String subject = "Şifre Sıfırlama";
        String content = String.format("Merhaba %s, şifrenizi sıfırlamak için token: %s", 
                user.getDisplayName(), resetToken);
        
        return sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Kullanıcıya bildirim emaili gönderir
     */
    public boolean sendNotificationEmail(User user, String message) {
        if (user == null || message == null) {
            throw new IllegalArgumentException("Kullanıcı ve mesaj null olamaz");
        }

        if (!emailServiceEnabled) {
            return false;
        }

        if (!user.hasValidEmail()) {
            return false;
        }

        String subject = "Bildirim";
        return sendEmail(user.getEmail(), subject, message);
    }

    /**
     * Toplu email gönderir
     */
    public int sendBulkEmail(List<User> users, String subject, String content) {
        if (users == null || users.isEmpty()) {
            return 0;
        }

        if (!emailServiceEnabled) {
            return 0;
        }

        int successCount = 0;
        for (User user : users) {
            if (user.hasValidEmail()) {
                if (sendEmail(user.getEmail(), subject, content)) {
                    successCount++;
                }
            }
        }
        return successCount;
    }

    /**
     * Temel email gönderme metodu
     */
    private boolean sendEmail(String to, String subject, String content) {
        try {
            // Gerçek uygulamada burada SMTP veya email API çağrısı yapılır
            // Simülasyon için sleep ekleyebiliriz
            Thread.sleep(10); // Email gönderimi simülasyonu
            
            // Email logunu kaydet
            EmailLog emailLog = new EmailLog(to, subject, content);
            sentEmails.add(emailLog);
            
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Email servisini aktif/pasif yapar
     */
    public void setEmailServiceEnabled(boolean enabled) {
        this.emailServiceEnabled = enabled;
    }

    /**
     * Email servisinin durumunu kontrol eder
     */
    public boolean isEmailServiceEnabled() {
        return emailServiceEnabled;
    }

    /**
     * Gönderilen emailları getirir (test amaçlı)
     */
    public List<EmailLog> getSentEmails() {
        return new ArrayList<>(sentEmails);
    }

    /**
     * Belirli bir email adresine gönderilen emailleri getirir
     */
    public List<EmailLog> getSentEmailsTo(String emailAddress) {
        return sentEmails.stream()
                .filter(email -> email.getTo().equals(emailAddress))
                .toList();
    }

    /**
     * Email loglarını temizler (test amaçlı)
     */
    public void clearEmailLogs() {
        sentEmails.clear();
    }

    /**
     * Gönderilen email sayısını döndürür
     */
    public int getSentEmailCount() {
        return sentEmails.size();
    }

    /**
     * Email servisinin sağlık durumunu kontrol eder
     */
    public boolean isHealthy() {
        // Gerçek uygulamada email servisinin erişilebilirliğini kontrol eder
        return emailServiceEnabled;
    }

    /**
     * Email log bilgilerini tutan iç sınıf
     */
    public static class EmailLog {
        private final String to;
        private final String subject;
        private final String content;
        private final long timestamp;

        public EmailLog(String to, String subject, String content) {
            this.to = to;
            this.subject = subject;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }

        public String getTo() {
            return to;
        }

        public String getSubject() {
            return subject;
        }

        public String getContent() {
            return content;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}