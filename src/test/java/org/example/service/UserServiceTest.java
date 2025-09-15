package org.example.service;

import org.example.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * UserService için Mockito kullanılarak yazılmış test sınıfı
 * Bu sınıf Mockito'nun temel özelliklerini gösterir:
 * 
 * MOCKITO ANNOTATIONS:
 * - @Mock: Mock objeler oluşturur (sahte objeler)
 * - @InjectMocks: Mock'ları gerçek objeye enjekte eder (dependency injection)
 * - @Captor: ArgumentCaptor'ları otomatik oluşturur
 * 
 * MOCKITO FEATURES:
 * - Mock behavior tanımlama (when/thenReturn)
 * - Verify işlemleri (verify, verifyNoInteractions)
 * - ArgumentCaptor kullanımı
 * - Exception mocking
 */
@ExtendWith(MockitoExtension.class)  // MockitoExtension annotation'ları etkinleştirir
@DisplayName("UserService Mockito Testleri")
class UserServiceTest {

    @Mock  // DatabaseService için sahte obje oluşturur
    private DatabaseService databaseService;

    @Mock  // EmailService için sahte obje oluşturur  
    private EmailService emailService;

    @InjectMocks  // Mock'ları UserService constructor'ına enjekte eder
    private UserService userService;

    @Captor  // User tipindeki argümanları yakalar
    private ArgumentCaptor<User> userCaptor;

    @Captor  // String tipindeki argümanları yakalar
    private ArgumentCaptor<String> stringCaptor;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
    }

    @Nested
    @DisplayName("Kullanıcı Kayıt Testleri")
    class UserRegistrationTests {

        @Test
        @DisplayName("Başarılı kullanıcı kaydı - @Mock/@InjectMocks örneği")
        void testSuccessfulUserRegistration() {
            // Given - @Mock annotation'ları ile oluşturulan mock'ları kullan
            String username = "newuser";
            String email = "newuser@example.com";
            String password = "password123";

            // @Mock DatabaseService'in davranışını tanımla
            when(databaseService.isEmailExists(email)).thenReturn(false);
            when(databaseService.saveUser(any(User.class))).thenReturn(testUser);
            
            // @Mock EmailService'in davranışını tanımla  
            when(emailService.sendWelcomeEmail(any(User.class))).thenReturn(true);

            // When - @InjectMocks UserService'i kullan (mock'lar otomatik enjekte edildi)
            User result = userService.registerUser(username, email, password);

            // Then
            assertNotNull(result, "Kayıt sonucu null olmamalı");
            assertEquals(testUser.getId(), result.getId(), "ID doğru dönmeli");

            // Verify interactions - @Mock'ların çağrıldığını doğrula
            verify(databaseService).isEmailExists(email);
            verify(databaseService).saveUser(userCaptor.capture());
            verify(emailService).sendWelcomeEmail(testUser);

            // @Captor ile yakalanan argümanı kontrol et
            User capturedUser = userCaptor.getValue();
            assertEquals(username, capturedUser.getUsername(), "Username doğru geçilmeli");
            assertEquals(email, capturedUser.getEmail(), "Email doğru geçilmeli");
            assertEquals(password, capturedUser.getPassword(), "Password doğru geçilmeli");
        }

        @Test
        @DisplayName("Email zaten kayıtlı - exception fırlatma")
        void testUserRegistrationWithExistingEmail() {
            // Given
            String email = "existing@example.com";
            when(databaseService.isEmailExists(email)).thenReturn(true);

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userService.registerUser("user", email, "password"));

            assertTrue(exception.getMessage().contains("zaten kayıtlı"),
                "Exception mesajı uygun olmalı");

            // Verify
            verify(databaseService).isEmailExists(email);
            verify(databaseService, never()).saveUser(any(User.class));
            verify(emailService, never()).sendWelcomeEmail(any(User.class));
        }

        @Test
        @DisplayName("Geçersiz username - exception fırlatma")
        void testUserRegistrationWithInvalidUsername() {
            // When & Then
            assertAll("Geçersiz username senaryoları",
                () -> assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(null, "test@example.com", "password")),
                () -> assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser("", "test@example.com", "password")),
                () -> assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser("   ", "test@example.com", "password"))
            );

            // Hiçbir database işlemi yapılmamalı
            verify(databaseService, never()).isEmailExists(anyString());
            verify(databaseService, never()).saveUser(any(User.class));
        }

        @Test
        @DisplayName("Geçersiz password - exception fırlatma")
        void testUserRegistrationWithInvalidPassword() {
            // When & Then
            assertAll("Geçersiz password senaryoları",
                () -> assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser("user", "test@example.com", null)),
                () -> assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser("user", "test@example.com", "123"))
            );

            // Hiçbir database işlemi yapılmamalı
            verify(databaseService, never()).isEmailExists(anyString());
            verify(databaseService, never()).saveUser(any(User.class));
        }

        @Test
        @DisplayName("Email gönderimi başarısız - kullanıcı kaydı devam etmeli")
        void testUserRegistrationWithEmailFailure() {
            // Given
            when(databaseService.isEmailExists(anyString())).thenReturn(false);
            when(databaseService.saveUser(any(User.class))).thenReturn(testUser);
            when(emailService.sendWelcomeEmail(any(User.class))).thenThrow(new RuntimeException("Email service down"));

            // When
            User result = assertDoesNotThrow(() ->
                userService.registerUser("user", "test@example.com", "password"));

            // Then
            assertNotNull(result, "Email hatası kullanıcı kaydını engellememelidirli");
            verify(databaseService).saveUser(any(User.class));
            verify(emailService).sendWelcomeEmail(any(User.class));
        }
    }

    @Nested
    @DisplayName("Kullanıcı Giriş Testleri")
    class UserLoginTests {

        @Test
        @DisplayName("Başarılı giriş")
        void testSuccessfulLogin() {
            // Given
            String email = "test@example.com";
            String password = "password123";
            when(databaseService.findUserByEmail(email)).thenReturn(Optional.of(testUser));

            // When
            Optional<User> result = userService.loginUser(email, password);

            // Then
            assertTrue(result.isPresent(), "Başarılı giriş user dönemeli");
            assertEquals(testUser.getId(), result.get().getId(), "User ID doğru olmalı");

            verify(databaseService).findUserByEmail(email);
        }

        @Test
        @DisplayName("Yanlış password")
        void testLoginWithWrongPassword() {
            // Given
            String email = "test@example.com";
            String wrongPassword = "wrongpassword";
            when(databaseService.findUserByEmail(email)).thenReturn(Optional.of(testUser));

            // When
            Optional<User> result = userService.loginUser(email, wrongPassword);

            // Then
            assertFalse(result.isPresent(), "Yanlış password ile giriş başarısız olmalı");
            verify(databaseService).findUserByEmail(email);
        }

        @Test
        @DisplayName("Kullanıcı bulunamadı")
        void testLoginWithNonExistentUser() {
            // Given
            String email = "nonexistent@example.com";
            when(databaseService.findUserByEmail(email)).thenReturn(Optional.empty());

            // When
            Optional<User> result = userService.loginUser(email, "password");

            // Then
            assertFalse(result.isPresent(), "Bulunmayan kullanıcı için giriş başarısız olmalı");
            verify(databaseService).findUserByEmail(email);
        }

        @Test
        @DisplayName("Pasif kullanıcı girişi")
        void testLoginWithInactiveUser() {
            // Given
            testUser.setActive(false);
            when(databaseService.findUserByEmail(anyString())).thenReturn(Optional.of(testUser));

            // When
            Optional<User> result = userService.loginUser("test@example.com", "password123");

            // Then
            assertFalse(result.isPresent(), "Pasif kullanıcı giriş yapamamalı");
        }

        @Test
        @DisplayName("Null parametreler")
        void testLoginWithNullParameters() {
            // When & Then
            assertAll("Null parameter senaryoları",
                () -> assertFalse(userService.loginUser(null, "password").isPresent()),
                () -> assertFalse(userService.loginUser("email", null).isPresent()),
                () -> assertFalse(userService.loginUser(null, null).isPresent())
            );

            // Database çağrısı yapılmamalı
            verify(databaseService, never()).findUserByEmail(anyString());
        }
    }

    @Nested
    @DisplayName("Profil Güncelleme Testleri")
    class ProfileUpdateTests {

        @Test
        @DisplayName("Username güncelleme")
        void testUpdateUsername() {
            // Given
            Long userId = 1L;
            String newUsername = "newusername";
            when(databaseService.findUserById(userId)).thenReturn(Optional.of(testUser));
            when(databaseService.updateUser(any(User.class))).thenReturn(testUser);

            // When
            User result = userService.updateUserProfile(userId, newUsername, null);

            // Then
            assertNotNull(result, "Güncelleme sonucu null olmamalı");

            verify(databaseService).findUserById(userId);
            verify(databaseService).updateUser(userCaptor.capture());

            User updatedUser = userCaptor.getValue();
            assertEquals(newUsername, updatedUser.getUsername(), "Username güncellenmelidirli");
        }

        @Test
        @DisplayName("Email güncelleme")
        void testUpdateEmail() {
            // Given
            Long userId = 1L;
            String newEmail = "newemail@example.com";
            when(databaseService.findUserById(userId)).thenReturn(Optional.of(testUser));
            when(databaseService.isEmailExists(newEmail)).thenReturn(false);
            when(databaseService.updateUser(any(User.class))).thenReturn(testUser);
            when(emailService.sendNotificationEmail(any(User.class), anyString())).thenReturn(true);

            // When
            User result = userService.updateUserProfile(userId, null, newEmail);

            // Then
            assertNotNull(result, "Güncelleme sonucu null olmamalı");

            verify(databaseService).findUserById(userId);
            verify(databaseService).isEmailExists(newEmail);
            verify(databaseService).updateUser(any(User.class));
            verify(emailService).sendNotificationEmail(eq(testUser), stringCaptor.capture());

            String capturedMessage = stringCaptor.getValue();
            assertTrue(capturedMessage.contains("güncellendi"), "Bildirim mesajı uygun olmalı");
        }

        @Test
        @DisplayName("Kullanıcı bulunamadı")
        void testUpdateNonExistentUser() {
            // Given
            Long userId = 999L;
            when(databaseService.findUserById(userId)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserProfile(userId, "newname", null));

            assertTrue(exception.getMessage().contains("bulunamadı"), "Exception mesajı uygun olmalı");

            verify(databaseService).findUserById(userId);
            verify(databaseService, never()).updateUser(any(User.class));
        }

        @Test
        @DisplayName("Email zaten kullanımda")
        void testUpdateWithExistingEmail() {
            // Given
            Long userId = 1L;
            String existingEmail = "existing@example.com";
            when(databaseService.findUserById(userId)).thenReturn(Optional.of(testUser));
            when(databaseService.isEmailExists(existingEmail)).thenReturn(true);

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userService.updateUserProfile(userId, null, existingEmail));

            assertTrue(exception.getMessage().contains("kullanılıyor"), "Exception mesajı uygun olmalı");

            verify(databaseService).findUserById(userId);
            verify(databaseService).isEmailExists(existingEmail);
            verify(databaseService, never()).updateUser(any(User.class));
        }
    }

    @Nested
    @DisplayName("Şifre Sıfırlama Testleri")
    class PasswordResetTests {

        @Test
        @DisplayName("Başarılı şifre sıfırlama isteği")
        void testSuccessfulPasswordResetRequest() {
            // Given
            String email = "test@example.com";
            when(databaseService.findUserByEmail(email)).thenReturn(Optional.of(testUser));
            when(emailService.sendPasswordResetEmail(eq(testUser), anyString())).thenReturn(true);

            // When
            String resetToken = userService.requestPasswordReset(email);

            // Then
            assertNotNull(resetToken, "Reset token null olmamalı");
            assertFalse(resetToken.isEmpty(), "Reset token boş olmamalı");
            assertEquals(16, resetToken.length(), "Reset token 16 karakter olmalı");

            verify(databaseService).findUserByEmail(email);
            verify(emailService).sendPasswordResetEmail(eq(testUser), eq(resetToken));
        }

        @Test
        @DisplayName("Kullanıcı bulunamadı")
        void testPasswordResetForNonExistentUser() {
            // Given
            String email = "nonexistent@example.com";
            when(databaseService.findUserByEmail(email)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.requestPasswordReset(email));

            assertTrue(exception.getMessage().contains("bulunamadı"), "Exception mesajı uygun olmalı");

            verify(databaseService).findUserByEmail(email);
            verify(emailService, never()).sendPasswordResetEmail(any(User.class), anyString());
        }

        @Test
        @DisplayName("Pasif kullanıcı için şifre sıfırlama")
        void testPasswordResetForInactiveUser() {
            // Given
            testUser.setActive(false);
            when(databaseService.findUserByEmail(anyString())).thenReturn(Optional.of(testUser));

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userService.requestPasswordReset("test@example.com"));

            assertTrue(exception.getMessage().contains("Pasif kullanıcılar"), "Exception mesajı uygun olmalı");

            verify(emailService, never()).sendPasswordResetEmail(any(User.class), anyString());
        }

        @Test
        @DisplayName("Email gönderimi başarısız")
        void testPasswordResetWithEmailFailure() {
            // Given
            when(databaseService.findUserByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(emailService.sendPasswordResetEmail(any(User.class), anyString())).thenReturn(false);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.requestPasswordReset("test@example.com"));

            assertTrue(exception.getMessage().contains("gönderilemedi"), "Exception mesajı uygun olmalı");

            verify(emailService).sendPasswordResetEmail(eq(testUser), anyString());
        }
    }

    @Nested
    @DisplayName("Kullanıcı Durum Yönetimi")
    class UserStatusManagement {

        @Test
        @DisplayName("Kullanıcı pasifleştirme")
        void testDeactivateUser() {
            // Given
            Long userId = 1L;
            when(databaseService.findUserById(userId)).thenReturn(Optional.of(testUser));
            when(databaseService.updateUser(any(User.class))).thenReturn(testUser);
            when(emailService.sendNotificationEmail(any(User.class), anyString())).thenReturn(true);

            // When
            userService.deactivateUser(userId);

            // Then
            verify(databaseService).findUserById(userId);
            verify(databaseService).updateUser(userCaptor.capture());
            verify(emailService).sendNotificationEmail(eq(testUser), stringCaptor.capture());

            User updatedUser = userCaptor.getValue();
            assertFalse(updatedUser.isActive(), "Kullanıcı pasif olmalı");

            String notificationMessage = stringCaptor.getValue();
            assertTrue(notificationMessage.contains("pasifleştirildi"), "Bildirim mesajı uygun olmalı");
        }

        @Test
        @DisplayName("Kullanıcı aktifleştirme")
        void testActivateUser() {
            // Given
            Long userId = 1L;
            testUser.setActive(false);
            when(databaseService.findUserById(userId)).thenReturn(Optional.of(testUser));
            when(databaseService.updateUser(any(User.class))).thenReturn(testUser);
            when(emailService.sendNotificationEmail(any(User.class), anyString())).thenReturn(true);

            // When
            userService.activateUser(userId);

            // Then
            verify(databaseService).updateUser(userCaptor.capture());
            verify(emailService).sendNotificationEmail(eq(testUser), stringCaptor.capture());

            User updatedUser = userCaptor.getValue();
            assertTrue(updatedUser.isActive(), "Kullanıcı aktif olmalı");

            String notificationMessage = stringCaptor.getValue();
            assertTrue(notificationMessage.contains("aktifleştirildi"), "Bildirim mesajı uygun olmalı");
        }
    }

    @Nested
    @DisplayName("Diğer İşlemler")
    class OtherOperations {

        @Test
        @DisplayName("Kullanıcı ID ile bulma")
        void testGetUserById() {
            // Given
            Long userId = 1L;
            when(databaseService.findUserById(userId)).thenReturn(Optional.of(testUser));

            // When
            Optional<User> result = userService.getUserById(userId);

            // Then
            assertTrue(result.isPresent(), "Kullanıcı bulunmalı");
            assertEquals(testUser.getId(), result.get().getId(), "ID doğru olmalı");
            verify(databaseService).findUserById(userId);
        }

        @Test
        @DisplayName("Tüm aktif kullanıcıları getirme")
        void testGetAllActiveUsers() {
            // Given
            List<User> activeUsers = List.of(testUser, new User("user2", "user2@test.com", "pass"));
            when(databaseService.getActiveUsers()).thenReturn(activeUsers);

            // When
            List<User> result = userService.getAllActiveUsers();

            // Then
            assertEquals(2, result.size(), "2 aktif kullanıcı olmalı");
            verify(databaseService).getActiveUsers();
        }

        @Test
        @DisplayName("Toplu bildirim gönderme")
        void testSendBulkNotification() {
            // Given
            List<User> activeUsers = List.of(testUser);
            when(databaseService.getActiveUsers()).thenReturn(activeUsers);
            when(emailService.sendBulkEmail(eq(activeUsers), anyString(), anyString())).thenReturn(1);

            // When
            int result = userService.sendBulkNotification("Subject", "Message");

            // Then
            assertEquals(1, result, "1 email gönderilmelidirli");
            verify(databaseService).getActiveUsers();
            verify(emailService).sendBulkEmail(eq(activeUsers), eq("Subject"), eq("Message"));
        }

        @Test
        @DisplayName("Kullanıcı istatistikleri")
        void testGetUserStats() {
            // Given
            List<User> allUsers = List.of(testUser, new User("user2", "user2@test.com", "pass"));
            List<User> activeUsers = List.of(testUser);
            when(databaseService.getAllUsers()).thenReturn(allUsers);
            when(databaseService.getActiveUsers()).thenReturn(activeUsers);

            // When
            UserService.UserStats stats = userService.getUserStats();

            // Then
            assertAll("Kullanıcı istatistikleri",
                () -> assertEquals(2, stats.getTotalUsers(), "Toplam kullanıcı sayısı 2 olmalı"),
                () -> assertEquals(1, stats.getActiveUsers(), "Aktif kullanıcı sayısı 1 olmalı"),
                () -> assertEquals(1, stats.getInactiveUsers(), "Pasif kullanıcı sayısı 1 olmalı")
            );

            verify(databaseService).getAllUsers();
            verify(databaseService).getActiveUsers();
        }
    }

    @Test
    @DisplayName("@Mock ve @InjectMocks annotation'larının çalışması")
    void testMockAndInjectMocksAnnotations() {
        // Given - @Mock ve @InjectMocks annotation'larının doğru çalıştığını kontrol et
        
        // 1. @InjectMocks ile oluşturulan UserService null olmamalı
        assertNotNull(userService, "UserService @InjectMocks ile oluşturulmalı");
        
        // 2. @Mock ile oluşturulan mock'lar null olmamalı
        assertNotNull(databaseService, "DatabaseService @Mock ile oluşturulmalı");
        assertNotNull(emailService, "EmailService @Mock ile oluşturulmalı");
        
        // 3. Mock davranışı tanımla - sadece kullanılacak mock'ları
        when(databaseService.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // When - @InjectMocks ile enjekte edilen mock'ları kullanan gerçek metod çağır
        Optional<User> result = userService.getUserByEmail("test@example.com");
        
        // Then - Mock injection'ın çalıştığını doğrula
        assertTrue(result.isPresent(), "@Mock'lanan DatabaseService çalışmalı");
        assertEquals(testUser.getEmail(), result.get().getEmail(), "Mock'dan dönen kullanıcı doğru olmalı");
        
        // 4. Mock'ların çağrıldığını verify et
        verify(databaseService).findUserByEmail("test@example.com");
        
        // 5. Kullanılmayan mock'ın hiç çağrılmadığını doğrula
        verifyNoInteractions(emailService);
        
        // 6. Mock'ların gerçek objeler olmadığını göster
        assertTrue(Mockito.mockingDetails(databaseService).isMock(), "databaseService bir mock olmalı");
        assertTrue(Mockito.mockingDetails(emailService).isMock(), "emailService bir mock olmalı");
        assertFalse(Mockito.mockingDetails(userService).isMock(), "userService gerçek obje olmalı");
    }
}