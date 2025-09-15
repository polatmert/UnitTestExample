package org.example.integration;

import org.example.model.User;
import org.example.service.DatabaseService;
import org.example.service.EmailService;
import org.example.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService Integration Testleri
 * Bu testler gerçek service sınıflarını birlikte test eder (mock kullanmaz)
 * Tüm bileşenlerin birlikte çalışıp çalışmadığını kontrol eder
 */
@DisplayName("UserService Integration Testleri")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceIntegrationTest {

    private static UserService userService;
    private static DatabaseService databaseService;
    private static EmailService emailService;

    @BeforeAll
    static void setUpClass() {
        // Gerçek service'leri oluştur (mock yok)
        databaseService = new DatabaseService();
        emailService = new EmailService();
        userService = new UserService(databaseService, emailService);
        
        System.out.println("Integration testler başlıyor...");
    }

    @AfterAll
    static void tearDownClass() {
        System.out.println("Integration testler tamamlandı.");
    }

    @BeforeEach
    void setUp() {
        // Her test öncesi temizlik
        databaseService.clearAll();
        emailService.clearEmailLogs();
        emailService.setEmailServiceEnabled(true);
    }

    @Nested
    @DisplayName("Kullanıcı Kayıt Akışı")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UserRegistrationFlow {

        @Test
        @Order(1)
        @DisplayName("Başarılı kullanıcı kaydı - tam akış")
        void testCompleteUserRegistrationFlow() {
            // Given
            String username = "integrationuser";
            String email = "integration@example.com";
            String password = "password123";

            // When
            User registeredUser = userService.registerUser(username, email, password);

            // Then - Kullanıcı kaydı kontrolü
            assertAll("Kullanıcı kayıt kontrolü",
                () -> assertNotNull(registeredUser, "Kayıtlı kullanıcı null olmamalı"),
                () -> assertNotNull(registeredUser.getId(), "ID atanmalı"),
                () -> assertEquals(username, registeredUser.getUsername(), "Username doğru olmalı"),
                () -> assertEquals(email, registeredUser.getEmail(), "Email doğru olmalı"),
                () -> assertTrue(registeredUser.isActive(), "Kullanıcı aktif olmalı")
            );

            // Then - Veritabanı kontrolü
            Optional<User> fromDb = databaseService.findUserById(registeredUser.getId());
            assertTrue(fromDb.isPresent(), "Kullanıcı veritabanına kaydedilmelidirli");
            assertEquals(email, fromDb.get().getEmail(), "DB'deki email doğru olmalı");

            // Then - Email kontrolü
            List<EmailService.EmailLog> sentEmails = emailService.getSentEmails();
            assertEquals(1, sentEmails.size(), "1 hoş geldin emaili gönderilmelidirli");
            
            EmailService.EmailLog welcomeEmail = sentEmails.get(0);
            assertAll("Hoş geldin email kontrolü",
                () -> assertEquals(email, welcomeEmail.getTo(), "Email alıcısı doğru olmalı"),
                () -> assertEquals("Hoş Geldiniz!", welcomeEmail.getSubject(), "Email konusu doğru olmalı"),
                () -> assertTrue(welcomeEmail.getContent().contains(username), "Email içeriği username içermeli")
            );

            // Then - Kullanıcı sayısı kontrolü
            assertEquals(1, databaseService.getUserCount(), "Toplam kullanıcı sayısı 1 olmalı");
            assertEquals(1, databaseService.getActiveUsers().size(), "Aktif kullanıcı sayısı 1 olmalı");
        }

        @Test
        @Order(2)
        @DisplayName("Email servisi kapalıyken kayıt")
        void testRegistrationWithEmailServiceDown() {
            // Given
            emailService.setEmailServiceEnabled(false);

            // When
            User registeredUser = assertDoesNotThrow(() -> 
                userService.registerUser("user", "user@example.com", "password"));

            // Then - Kullanıcı kaydedilmelidirli
            assertNotNull(registeredUser.getId(), "Email servisi kapalı olsa da kullanıcı kaydedilmelidirli");
            assertEquals(1, databaseService.getUserCount(), "Kullanıcı sayısı 1 olmalı");

            // Then - Email gönderilmemelidirli
            assertEquals(0, emailService.getSentEmailCount(), "Email gönderilmemelidirli");
        }
    }

    @Nested
    @DisplayName("Kullanıcı Giriş Akışı")
    class UserLoginFlow {

        private User testUser;

        @BeforeEach
        void setUpUser() {
            testUser = userService.registerUser("loginuser", "login@example.com", "password123");
            emailService.clearEmailLogs(); // Kayıt emailini temizle
        }

        @Test
        @DisplayName("Başarılı giriş")
        void testSuccessfulLogin() {
            // When
            Optional<User> loggedInUser = userService.loginUser("login@example.com", "password123");

            // Then
            assertTrue(loggedInUser.isPresent(), "Giriş başarılı olmalı");
            assertEquals(testUser.getId(), loggedInUser.get().getId(), "Doğru kullanıcı dönemeli");
        }

        @Test
        @DisplayName("Yanlış şifre ile giriş")
        void testLoginWithWrongPassword() {
            // When
            Optional<User> result = userService.loginUser("login@example.com", "wrongpassword");

            // Then
            assertFalse(result.isPresent(), "Yanlış şifre ile giriş başarısız olmalı");
        }

        @Test
        @DisplayName("Pasif kullanıcı girişi")
        void testInactiveUserLogin() {
            // Given
            userService.deactivateUser(testUser.getId());

            // When
            Optional<User> result = userService.loginUser("login@example.com", "password123");

            // Then
            assertFalse(result.isPresent(), "Pasif kullanıcı giriş yapamamalı");
        }
    }

    @Nested
    @DisplayName("Profil Güncelleme Akışı")
    class ProfileUpdateFlow {

        private User testUser;

        @BeforeEach
        void setUpUser() {
            testUser = userService.registerUser("updateuser", "update@example.com", "password123");
            emailService.clearEmailLogs(); // Kayıt emailini temizle
        }

        @Test
        @DisplayName("Username güncelleme")
        void testUsernameUpdate() {
            // When
            User updatedUser = userService.updateUserProfile(testUser.getId(), "newusername", null);

            // Then
            assertEquals("newusername", updatedUser.getUsername(), "Username güncellenmelidirli");
            
            // DB kontrolü
            Optional<User> fromDb = databaseService.findUserById(testUser.getId());
            assertTrue(fromDb.isPresent(), "Kullanıcı DB'de bulunmalı");
            assertEquals("newusername", fromDb.get().getUsername(), "DB'deki username güncellenmelidirli");

            // Email gönderilmemelidirli (sadece username değişti)
            assertEquals(0, emailService.getSentEmailCount(), "Username güncellemesi için email gönderilmemelidirli");
        }

        @Test
        @DisplayName("Email güncelleme")
        void testEmailUpdate() {
            // When
            User updatedUser = userService.updateUserProfile(testUser.getId(), null, "newemail@example.com");

            // Then
            assertEquals("newemail@example.com", updatedUser.getEmail(), "Email güncellenmelidirli");

            // Email bildirim kontrolü
            assertEquals(1, emailService.getSentEmailCount(), "Email güncelleme bildirimi gönderilmelidirli");
            
            EmailService.EmailLog notification = emailService.getSentEmails().get(0);
            assertAll("Email güncelleme bildirimi",
                () -> assertEquals("newemail@example.com", notification.getTo(), "Bildirim yeni email'e gönderilmelidirli"),
                () -> assertEquals("Bildirim", notification.getSubject(), "Bildirim konusu doğru olmalı"),
                () -> assertTrue(notification.getContent().contains("güncellendi"), "İçerik güncelleme mesajı içermeli")
            );
        }

        @Test
        @DisplayName("Hem username hem email güncelleme")
        void testBothUsernameAndEmailUpdate() {
            // When
            User updatedUser = userService.updateUserProfile(testUser.getId(), "newname", "newemail@example.com");

            // Then
            assertAll("Çoklu güncelleme kontrolü",
                () -> assertEquals("newname", updatedUser.getUsername(), "Username güncellenmelidirli"),
                () -> assertEquals("newemail@example.com", updatedUser.getEmail(), "Email güncellenmelidirli")
            );

            // Email bildirim kontrolü (sadece email değişikliği için)
            assertEquals(1, emailService.getSentEmailCount(), "1 bildirim emaili gönderilmelidirli");
        }
    }

    @Nested
    @DisplayName("Şifre Sıfırlama Akışı")
    class PasswordResetFlow {

        private User testUser;

        @BeforeEach
        void setUpUser() {
            testUser = userService.registerUser("resetuser", "reset@example.com", "password123");
            emailService.clearEmailLogs(); // Kayıt emailini temizle
        }

        @Test
        @DisplayName("Başarılı şifre sıfırlama isteği")
        void testSuccessfulPasswordResetRequest() {
            // When
            String resetToken = userService.requestPasswordReset("reset@example.com");

            // Then
            assertNotNull(resetToken, "Reset token null olmamalı");
            assertEquals(16, resetToken.length(), "Reset token 16 karakter olmalı");

            // Email kontrolü
            assertEquals(1, emailService.getSentEmailCount(), "Şifre sıfırlama emaili gönderilmelidirli");
            
            EmailService.EmailLog resetEmail = emailService.getSentEmails().get(0);
            assertAll("Şifre sıfırlama email kontrolü",
                () -> assertEquals("reset@example.com", resetEmail.getTo(), "Email alıcısı doğru olmalı"),
                () -> assertEquals("Şifre Sıfırlama", resetEmail.getSubject(), "Email konusu doğru olmalı"),
                () -> assertTrue(resetEmail.getContent().contains(resetToken), "Email token içermeli"),
                () -> assertTrue(resetEmail.getContent().contains("resetuser"), "Email username içermeli")
            );
        }

        @Test
        @DisplayName("Pasif kullanıcı için şifre sıfırlama")
        void testPasswordResetForInactiveUser() {
            // Given
            userService.deactivateUser(testUser.getId());
            emailService.clearEmailLogs(); // Pasifleştirme emailini temizle

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                userService.requestPasswordReset("reset@example.com"));

            assertTrue(exception.getMessage().contains("Pasif kullanıcılar"), "Exception mesajı uygun olmalı");
            assertEquals(0, emailService.getSentEmailCount(), "Email gönderilmemelidirli");
        }
    }

    @Nested
    @DisplayName("Kullanıcı Durumu Yönetimi")
    class UserStatusManagement {

        private User testUser;

        @BeforeEach
        void setUpUser() {
            testUser = userService.registerUser("statususer", "status@example.com", "password123");
            emailService.clearEmailLogs(); // Kayıt emailini temizle
        }

        @Test
        @DisplayName("Kullanıcı pasifleştirme")
        void testUserDeactivation() {
            // When
            userService.deactivateUser(testUser.getId());

            // Then
            Optional<User> deactivatedUser = databaseService.findUserById(testUser.getId());
            assertTrue(deactivatedUser.isPresent(), "Kullanıcı DB'de bulunmalı");
            assertFalse(deactivatedUser.get().isActive(), "Kullanıcı pasif olmalı");

            // Email kontrolü
            assertEquals(1, emailService.getSentEmailCount(), "Pasifleştirme bildirimi gönderilmelidirli");
            
            EmailService.EmailLog notification = emailService.getSentEmails().get(0);
            assertTrue(notification.getContent().contains("pasifleştirildi"), 
                "Bildirim pasifleştirme mesajı içermeli");

            // Aktif kullanıcı sayısı kontrolü
            assertEquals(0, databaseService.getActiveUsers().size(), "Aktif kullanıcı sayısı 0 olmalı");
        }

        @Test
        @DisplayName("Kullanıcı aktifleştirme")
        void testUserActivation() {
            // Given
            userService.deactivateUser(testUser.getId());
            emailService.clearEmailLogs(); // Pasifleştirme emailini temizle

            // When
            userService.activateUser(testUser.getId());

            // Then
            Optional<User> activatedUser = databaseService.findUserById(testUser.getId());
            assertTrue(activatedUser.isPresent(), "Kullanıcı DB'de bulunmalı");
            assertTrue(activatedUser.get().isActive(), "Kullanıcı aktif olmalı");

            // Email kontrolü
            assertEquals(1, emailService.getSentEmailCount(), "Aktifleştirme bildirimi gönderilmelidirli");
            
            EmailService.EmailLog notification = emailService.getSentEmails().get(0);
            assertTrue(notification.getContent().contains("aktifleştirildi"), 
                "Bildirim aktifleştirme mesajı içermeli");

            // Aktif kullanıcı sayısı kontrolü
            assertEquals(1, databaseService.getActiveUsers().size(), "Aktif kullanıcı sayısı 1 olmalı");
        }
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    @DisplayName("Unix sistemlerinde çalışan test")
    void testOnUnixSystems() {
        // Bu test sadece Mac ve Linux'ta çalışır
        User user = userService.registerUser("unixuser", "unix@example.com", "password");
        assertNotNull(user, "Unix sistemlerinde kullanıcı kaydı çalışmalı");
    }

    @Test
    @DisplayName("Karmaşık senaryö - tam entegrasyon")
    void testComplexIntegrationScenario() {
        // Bu test birden fazla işlemi birlikte test eder
        
        // 1. Kullanıcı kaydet
        User user = userService.registerUser("complex", "complex@example.com", "password123");
        assertEquals(1, databaseService.getUserCount(), "1 kullanıcı kaydedilmelidirli");
        assertEquals(1, emailService.getSentEmailCount(), "Hoş geldin emaili gönderilmelidirli");

        // 2. Giriş yap
        Optional<User> loggedIn = userService.loginUser("complex@example.com", "password123");
        assertTrue(loggedIn.isPresent(), "Giriş başarılı olmalı");

        // 3. Profil güncelle
        emailService.clearEmailLogs();
        userService.updateUserProfile(user.getId(), "complexupdated", "complexnew@example.com");
        assertEquals(1, emailService.getSentEmailCount(), "Email güncelleme bildirimi gönderilmelidirli");

        // 4. Şifre sıfırlama iste
        String resetToken = userService.requestPasswordReset("complexnew@example.com");
        assertNotNull(resetToken, "Reset token alınmalı");
        assertEquals(2, emailService.getSentEmailCount(), "Toplam 2 email gönderilmelidirli");

        // 5. Kullanıcıyı pasifleştir
        userService.deactivateUser(user.getId());
        assertEquals(0, databaseService.getActiveUsers().size(), "Aktif kullanıcı kalmamalı");
        assertEquals(3, emailService.getSentEmailCount(), "Pasifleştirme bildirimi de gönderilmelidirli");

        // 6. Pasif kullanıcıyla giriş denemesi
        Optional<User> inactiveLogin = userService.loginUser("complexnew@example.com", "password123");
        assertFalse(inactiveLogin.isPresent(), "Pasif kullanıcı giriş yapamamalı");

        // 7. Tekrar aktifleştir
        userService.activateUser(user.getId());
        assertEquals(1, databaseService.getActiveUsers().size(), "1 aktif kullanıcı olmalı");

        // 8. İstatistikleri kontrol et
        UserService.UserStats finalStats = userService.getUserStats();
        assertAll("Final istatistikler",
            () -> assertEquals(1, finalStats.getTotalUsers(), "Toplam 1 kullanıcı olmalı"),
            () -> assertEquals(1, finalStats.getActiveUsers(), "1 aktif kullanıcı olmalı"),
            () -> assertEquals(0, finalStats.getInactiveUsers(), "0 pasif kullanıcı olmalı")
        );
    }
}