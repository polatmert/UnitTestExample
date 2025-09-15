package org.example.service;

import org.example.model.User;
import org.junit.jupiter.api.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EmailService için testleri
 * Java 22 uyumluluğu için @Spy yerine gerçek instance kullanılıyor
 */
@DisplayName("EmailService Testleri")
class EmailServiceTest {

    private EmailService emailService;
    private User testUser;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
    }

    @Nested
    @DisplayName("Hoş Geldin Email Testleri")
    class WelcomeEmailTests {

        @Test
        @DisplayName("Başarılı hoş geldin emaili")
        void testSuccessfulWelcomeEmail() {
            // When
            boolean result = emailService.sendWelcomeEmail(testUser);

            // Then
            assertTrue(result, "Email gönderimi başarılı olmalı");
            assertEquals(1, emailService.getSentEmailCount(), "1 email gönderilmelidirli");
            
            List<EmailService.EmailLog> sentEmails = emailService.getSentEmails();
            EmailService.EmailLog emailLog = sentEmails.get(0);
            
            assertAll("Email içeriği kontrolü",
                () -> assertEquals("test@example.com", emailLog.getTo(), "Alıcı doğru olmalı"),
                () -> assertEquals("Hoş Geldiniz!", emailLog.getSubject(), "Konu doğru olmalı"),
                () -> assertTrue(emailLog.getContent().contains("testuser"), "İçerik kullanıcı adını içermeli"),
                () -> assertTrue(emailLog.getContent().contains("hoş geldiniz"), "İçerik hoş geldin mesajı içermeli")
            );
        }

        @Test
        @DisplayName("Null kullanıcı - exception")
        void testWelcomeEmailWithNullUser() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailService.sendWelcomeEmail(null));
            
            assertTrue(exception.getMessage().contains("null olamaz"), "Exception mesajı uygun olmalı");
            assertEquals(0, emailService.getSentEmailCount(), "Email gönderilmemelidirli");
        }

        @Test
        @DisplayName("Geçersiz email - exception")
        void testWelcomeEmailWithInvalidEmail() {
            // Given
            testUser.setEmail("invalid-email");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailService.sendWelcomeEmail(testUser));
            
            assertTrue(exception.getMessage().contains("Geçerli email"), "Exception mesajı uygun olmalı");
        }

        @Test
        @DisplayName("Email servisi kapalı")
        void testWelcomeEmailWithServiceDisabled() {
            // Given
            emailService.setEmailServiceEnabled(false);

            // When
            boolean result = emailService.sendWelcomeEmail(testUser);

            // Then
            assertFalse(result, "Servis kapalıyken email gönderilmemelidirli");
            assertEquals(0, emailService.getSentEmailCount(), "Email sayısı 0 olmalı");
        }
    }

    @Nested
    @DisplayName("Şifre Sıfırlama Email Testleri")
    class PasswordResetEmailTests {

        @Test
        @DisplayName("Başarılı şifre sıfırlama emaili")
        void testSuccessfulPasswordResetEmail() {
            // Given
            String resetToken = "abc123def456";

            // When
            boolean result = emailService.sendPasswordResetEmail(testUser, resetToken);

            // Then
            assertTrue(result, "Email gönderimi başarılı olmalı");
            
            List<EmailService.EmailLog> sentEmails = emailService.getSentEmailsTo("test@example.com");
            assertEquals(1, sentEmails.size(), "1 email gönderilmelidirli");
            
            EmailService.EmailLog emailLog = sentEmails.get(0);
            assertAll("Email içeriği kontrolü",
                () -> assertEquals("Şifre Sıfırlama", emailLog.getSubject(), "Konu doğru olmalı"),
                () -> assertTrue(emailLog.getContent().contains(resetToken), "İçerik token içermeli"),
                () -> assertTrue(emailLog.getContent().contains("testuser"), "İçerik kullanıcı adını içermeli")
            );
        }

        @Test
        @DisplayName("Null kullanıcı - exception")
        void testPasswordResetEmailWithNullUser() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailService.sendPasswordResetEmail(null, "token"));
            
            assertTrue(exception.getMessage().contains("null olamaz"), "Exception mesajı uygun olmalı");
        }

        @Test
        @DisplayName("Null token - exception")
        void testPasswordResetEmailWithNullToken() {
            // When & Then
            assertAll("Null token senaryoları",
                () -> assertThrows(IllegalArgumentException.class,
                    () -> emailService.sendPasswordResetEmail(testUser, null)),
                () -> assertThrows(IllegalArgumentException.class,
                    () -> emailService.sendPasswordResetEmail(testUser, "")),
                () -> assertThrows(IllegalArgumentException.class,
                    () -> emailService.sendPasswordResetEmail(testUser, "   "))
            );
        }

        @Test
        @DisplayName("Geçersiz email adresi")
        void testPasswordResetEmailWithInvalidEmail() {
            // Given
            testUser.setEmail("invalid");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailService.sendPasswordResetEmail(testUser, "token123"));
            
            assertTrue(exception.getMessage().contains("Geçerli email"), "Exception mesajı uygun olmalı");
        }
    }

    @Nested
    @DisplayName("Bildirim Email Testleri")
    class NotificationEmailTests {

        @Test
        @DisplayName("Başarılı bildirim emaili")
        void testSuccessfulNotificationEmail() {
            // Given
            String message = "Hesabınız güncellendi";

            // When
            boolean result = emailService.sendNotificationEmail(testUser, message);

            // Then
            assertTrue(result, "Email gönderimi başarılı olmalı");
            
            EmailService.EmailLog emailLog = emailService.getSentEmails().get(0);
            assertAll("Email içeriği kontrolü",
                () -> assertEquals("Bildirim", emailLog.getSubject(), "Konu 'Bildirim' olmalı"),
                () -> assertEquals(message, emailLog.getContent(), "İçerik mesaj ile aynı olmalı")
            );
        }

        @Test
        @DisplayName("Null parametreler")
        void testNotificationEmailWithNullParameters() {
            // When & Then
            assertAll("Null parameter senaryoları",
                () -> assertThrows(IllegalArgumentException.class,
                    () -> emailService.sendNotificationEmail(null, "message")),
                () -> assertThrows(IllegalArgumentException.class,
                    () -> emailService.sendNotificationEmail(testUser, null))
            );
        }

        @Test
        @DisplayName("Geçersiz email - false dönmeli")
        void testNotificationEmailWithInvalidEmail() {
            // Given
            testUser.setEmail("invalid");

            // When
            boolean result = emailService.sendNotificationEmail(testUser, "message");

            // Then
            assertFalse(result, "Geçersiz email ile false dönmeli");
            assertEquals(0, emailService.getSentEmailCount(), "Email gönderilmemelidirli");
        }

        @Test
        @DisplayName("Email servisi kapalı")
        void testNotificationEmailWithServiceDisabled() {
            // Given
            emailService.setEmailServiceEnabled(false);

            // When
            boolean result = emailService.sendNotificationEmail(testUser, "message");

            // Then
            assertFalse(result, "Servis kapalıyken false dönmeli");
        }
    }

    @Nested
    @DisplayName("Toplu Email Testleri")
    class BulkEmailTests {

        @Test
        @DisplayName("Başarılı toplu email gönderimi")
        void testSuccessfulBulkEmail() {
            // Given
            User user2 = new User("user2", "user2@example.com", "pass");
            User user3 = new User("user3", "user3@example.com", "pass");
            List<User> users = List.of(testUser, user2, user3);

            // When
            int result = emailService.sendBulkEmail(users, "Toplu Bildirim", "Test mesajı");

            // Then
            assertEquals(3, result, "3 email gönderilmelidirli");
            assertEquals(3, emailService.getSentEmailCount(), "Toplam email sayısı 3 olmalı");
            
            // Her kullanıcıya email gönderildiğini kontrol et
            assertEquals(1, emailService.getSentEmailsTo("test@example.com").size());
            assertEquals(1, emailService.getSentEmailsTo("user2@example.com").size());
            assertEquals(1, emailService.getSentEmailsTo("user3@example.com").size());
        }

        @Test
        @DisplayName("Geçersiz email'li kullanıcıları atlama")
        void testBulkEmailWithInvalidEmails() {
            // Given
            User invalidUser = new User("invalid", "invalid-email", "pass");
            List<User> users = List.of(testUser, invalidUser);

            // When
            int result = emailService.sendBulkEmail(users, "Test", "Message");

            // Then
            assertEquals(1, result, "Sadece 1 geçerli email gönderilmelidirli");
            assertEquals(1, emailService.getSentEmailCount(), "Toplam email sayısı 1 olmalı");
        }

        @Test
        @DisplayName("Null liste - 0 dönmeli")
        void testBulkEmailWithNullList() {
            // When
            int result = emailService.sendBulkEmail(null, "Subject", "Content");

            // Then
            assertEquals(0, result, "Null liste için 0 dönmeli");
        }

        @Test
        @DisplayName("Boş liste - 0 dönmeli")
        void testBulkEmailWithEmptyList() {
            // When
            int result = emailService.sendBulkEmail(List.of(), "Subject", "Content");

            // Then
            assertEquals(0, result, "Boş liste için 0 dönmeli");
        }

        @Test
        @DisplayName("Email servisi kapalı")
        void testBulkEmailWithServiceDisabled() {
            // Given
            emailService.setEmailServiceEnabled(false);
            List<User> users = List.of(testUser);

            // When
            int result = emailService.sendBulkEmail(users, "Subject", "Content");

            // Then
            assertEquals(0, result, "Servis kapalıyken 0 dönmeli");
        }
    }

    @Nested
    @DisplayName("Email Servisi Yönetimi")
    class EmailServiceManagement {

        @Test
        @DisplayName("Email servisi durumu kontrolü")
        void testEmailServiceStatus() {
            // Initially enabled
            assertTrue(emailService.isEmailServiceEnabled(), "Başlangıçta aktif olmalı");
            assertTrue(emailService.isHealthy(), "Sağlıklı olmalı");

            // Disable
            emailService.setEmailServiceEnabled(false);
            assertFalse(emailService.isEmailServiceEnabled(), "Pasifleştirildikten sonra false olmalı");
            assertFalse(emailService.isHealthy(), "Sağlıksız olmalı");

            // Enable again
            emailService.setEmailServiceEnabled(true);
            assertTrue(emailService.isEmailServiceEnabled(), "Tekrar aktifleştirildikten sonra true olmalı");
        }
    }

    @Nested
    @DisplayName("Email Log Yönetimi")
    class EmailLogManagement {

        @Test
        @DisplayName("Email log temizleme")
        void testClearEmailLogs() {
            // Given - Send some emails
            emailService.sendWelcomeEmail(testUser);
            emailService.sendNotificationEmail(testUser, "test message");
            assertEquals(2, emailService.getSentEmailCount(), "2 email gönderilmelidirli");

            // When
            emailService.clearEmailLogs();

            // Then
            assertEquals(0, emailService.getSentEmailCount(), "Log temizlendikten sonra 0 olmalı");
            assertTrue(emailService.getSentEmails().isEmpty(), "Email listesi boş olmalı");
        }

        @Test
        @DisplayName("Belirli email adresine gönderilen emailleri getirme")
        void testGetSentEmailsToSpecificAddress() {
            // Given
            User user2 = new User("user2", "user2@example.com", "pass");
            
            emailService.sendWelcomeEmail(testUser);
            emailService.sendWelcomeEmail(user2);
            emailService.sendNotificationEmail(testUser, "message");

            // When
            List<EmailService.EmailLog> emailsToTestUser = emailService.getSentEmailsTo("test@example.com");
            List<EmailService.EmailLog> emailsToUser2 = emailService.getSentEmailsTo("user2@example.com");

            // Then
            assertEquals(2, emailsToTestUser.size(), "testuser'a 2 email gönderilmelidirli");
            assertEquals(1, emailsToUser2.size(), "user2'ye 1 email gönderilmelidirli");
        }

        @Test
        @DisplayName("EmailLog özellikleri")
        void testEmailLogProperties() {
            // Given
            long beforeTime = System.currentTimeMillis();
            
            // When
            emailService.sendWelcomeEmail(testUser);
            
            // Then
            EmailService.EmailLog emailLog = emailService.getSentEmails().get(0);
            long afterTime = System.currentTimeMillis();
            
            assertAll("EmailLog özellikleri",
                () -> assertEquals("test@example.com", emailLog.getTo(), "Alıcı doğru olmalı"),
                () -> assertEquals("Hoş Geldiniz!", emailLog.getSubject(), "Konu doğru olmalı"),
                () -> assertNotNull(emailLog.getContent(), "İçerik null olmamalı"),
                () -> assertTrue(emailLog.getTimestamp() >= beforeTime, "Timestamp test başlangıcından sonra olmalı"),
                () -> assertTrue(emailLog.getTimestamp() <= afterTime, "Timestamp test bitiminden önce olmalı")
            );
        }
    }

    @Nested
    @DisplayName("Gerçek Service İşlemleri")
    class RealServiceOperations {

        @Test
        @DisplayName("Email servisi ile gerçek metod çağrıları")
        void testRealServiceMethodCalls() {
            // When
            boolean result1 = emailService.sendWelcomeEmail(testUser);
            boolean result2 = emailService.sendNotificationEmail(testUser, "test message");

            // Then
            assertTrue(result1, "Hoş geldin emaili başarılı olmalı");
            assertTrue(result2, "Bildirim emaili başarılı olmalı");
            assertEquals(2, emailService.getSentEmailCount(), "2 email gönderilmelidirli");
        }

        @Test
        @DisplayName("Service state kontrolü")
        void testServiceStateManagement() {
            // Given
            emailService.sendWelcomeEmail(testUser);
            assertEquals(1, emailService.getSentEmailCount(), "1 email gönderilmelidirli");

            // When
            emailService.setEmailServiceEnabled(false);
            boolean result = emailService.sendNotificationEmail(testUser, "test");

            // Then
            assertFalse(result, "Servis kapalıyken email gönderilmemelidirli");
            assertEquals(1, emailService.getSentEmailCount(), "Email sayısı değişmemelidirli");
        }
    }

    @Test
    @DisplayName("Thread interrupt simülasyonu")
    void testThreadInterruptSimulation() {
        // Bu test Thread.sleep ile InterruptedException durumunu test eder
        
        // Thread'i interrupt edelim
        Thread.currentThread().interrupt();
        
        try {
            // Email göndermeye çalışalım
            boolean result = emailService.sendWelcomeEmail(testUser);
            
            // Normal durumda email gönderimi başarılı olmalı
            // Bu test interrupt handling'i göstermek için
            assertTrue(result || Thread.currentThread().isInterrupted(), 
                "Email gönderimi başarılı olmalı veya interrupt durumu tespit edilmelidirli");
            
        } finally {
            // Interrupt flag'ini temizle
            Thread.interrupted();
        }
    }
}