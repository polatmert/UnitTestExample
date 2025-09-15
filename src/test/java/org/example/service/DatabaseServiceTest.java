package org.example.service;

import org.example.model.User;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DatabaseService için unit testleri
 * Bu sınıf gerçek bir service sınıfını test eder (mock kullanmaz)
 * Test isolation için @BeforeEach ve @AfterEach kullanır
 */
@DisplayName("DatabaseService Unit Testleri")
class DatabaseServiceTest {

    private DatabaseService databaseService;
    private User testUser;

    @BeforeEach
    void setUp() {
        databaseService = new DatabaseService();
        testUser = new User("testuser", "test@example.com", "password123");
    }

    @AfterEach
    void tearDown() {
        // Her test sonrası veritabanını temizle
        databaseService.clearAll();
    }

    @Nested
    @DisplayName("Kullanıcı Kaydetme Testleri")
    class SaveUserTests {

        @Test
        @DisplayName("Başarılı kullanıcı kaydetme")
        void testSaveUserSuccessfully() {
            // When
            User savedUser = databaseService.saveUser(testUser);

            // Then
            assertAll("Kaydetme kontrolü",
                () -> assertNotNull(savedUser.getId(), "ID atanmalı"),
                () -> assertEquals(1L, savedUser.getId(), "İlk kullanıcının ID'si 1 olmalı"),
                () -> assertEquals(testUser.getUsername(), savedUser.getUsername(), "Username korunmalı"),
                () -> assertEquals(testUser.getEmail(), savedUser.getEmail(), "Email korunmalı"),
                () -> assertEquals(1, databaseService.getUserCount(), "Kullanıcı sayısı 1 olmalı")
            );
        }

        @Test
        @DisplayName("Null kullanıcı - exception")
        void testSaveNullUser() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> databaseService.saveUser(null));

            assertTrue(exception.getMessage().contains("null olamaz"), "Exception mesajı uygun olmalı");
            assertEquals(0, databaseService.getUserCount(), "Kullanıcı sayısı 0 kalmalı");
        }

        @Test
        @DisplayName("Boş username - exception")
        void testSaveUserWithEmptyUsername() {
            // Given
            testUser.setUsername("");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> databaseService.saveUser(testUser));

            assertTrue(exception.getMessage().contains("boş olamaz"), "Exception mesajı uygun olmalı");
        }

        @Test
        @DisplayName("Null username - exception")
        void testSaveUserWithNullUsername() {
            // Given
            testUser.setUsername(null);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> databaseService.saveUser(testUser));

            assertTrue(exception.getMessage().contains("boş olamaz"), "Exception mesajı uygun olmalı");
        }

        @Test
        @DisplayName("Geçersiz email - exception")
        void testSaveUserWithInvalidEmail() {
            // Given
            testUser.setEmail("invalid-email");

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> databaseService.saveUser(testUser));

            assertTrue(exception.getMessage().contains("Geçerli bir email"), "Exception mesajı uygun olmalı");
        }

        @Test
        @DisplayName("Null email - exception")
        void testSaveUserWithNullEmail() {
            // Given
            testUser.setEmail(null);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> databaseService.saveUser(testUser));

            assertTrue(exception.getMessage().contains("Geçerli bir email"), "Exception mesajı uygun olmalı");
        }

        @Test
        @DisplayName("Duplicate email - exception")
        void testSaveUserWithDuplicateEmail() {
            // Given
            databaseService.saveUser(testUser);
            User duplicateUser = new User("anotheruser", "test@example.com", "password");

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> databaseService.saveUser(duplicateUser));

            assertTrue(exception.getMessage().contains("zaten kayıtlı"), "Exception mesajı uygun olmalı");
            assertEquals(1, databaseService.getUserCount(), "Kullanıcı sayısı 1 kalmalı");
        }

        @Test
        @DisplayName("Sıralı ID atama")
        void testSequentialIdAssignment() {
            // Given
            User user1 = new User("user1", "user1@example.com", "pass");
            User user2 = new User("user2", "user2@example.com", "pass");
            User user3 = new User("user3", "user3@example.com", "pass");

            // When
            User saved1 = databaseService.saveUser(user1);
            User saved2 = databaseService.saveUser(user2);
            User saved3 = databaseService.saveUser(user3);

            // Then
            assertAll("Sıralı ID kontrolü",
                () -> assertEquals(1L, saved1.getId(), "İlk kullanıcı ID'si 1 olmalı"),
                () -> assertEquals(2L, saved2.getId(), "İkinci kullanıcı ID'si 2 olmalı"),
                () -> assertEquals(3L, saved3.getId(), "Üçüncü kullanıcı ID'si 3 olmalı")
            );
        }
    }

    @Nested
    @DisplayName("Kullanıcı Bulma Testleri")
    class FindUserTests {

        @BeforeEach
        void setUpUsers() {
            databaseService.saveUser(testUser);
        }

        @Test
        @DisplayName("ID ile kullanıcı bulma - başarılı")
        void testFindUserByIdSuccessfully() {
            // When
            Optional<User> result = databaseService.findUserById(1L);

            // Then
            assertTrue(result.isPresent(), "Kullanıcı bulunmalı");
            assertEquals(testUser.getEmail(), result.get().getEmail(), "Email doğru olmalı");
        }

        @Test
        @DisplayName("ID ile kullanıcı bulma - bulunamadı")
        void testFindUserByIdNotFound() {
            // When
            Optional<User> result = databaseService.findUserById(999L);

            // Then
            assertFalse(result.isPresent(), "Kullanıcı bulunmamalı");
        }

        @Test
        @DisplayName("Null ID ile kullanıcı bulma")
        void testFindUserByNullId() {
            // When
            Optional<User> result = databaseService.findUserById(null);

            // Then
            assertFalse(result.isPresent(), "Null ID için empty dönmeli");
        }

        @Test
        @DisplayName("Email ile kullanıcı bulma - başarılı")
        void testFindUserByEmailSuccessfully() {
            // When
            Optional<User> result = databaseService.findUserByEmail("test@example.com");

            // Then
            assertTrue(result.isPresent(), "Kullanıcı bulunmalı");
            assertEquals(testUser.getUsername(), result.get().getUsername(), "Username doğru olmalı");
        }

        @Test
        @DisplayName("Email ile kullanıcı bulma - bulunamadı")
        void testFindUserByEmailNotFound() {
            // When
            Optional<User> result = databaseService.findUserByEmail("notfound@example.com");

            // Then
            assertFalse(result.isPresent(), "Kullanıcı bulunmamalı");
        }

        @Test
        @DisplayName("Null email ile kullanıcı bulma")
        void testFindUserByNullEmail() {
            // When
            Optional<User> result = databaseService.findUserByEmail(null);

            // Then
            assertFalse(result.isPresent(), "Null email için empty dönmeli");
        }
    }

    @Nested
    @DisplayName("Kullanıcı Listeleme Testleri")
    class ListUsersTests {

        @BeforeEach
        void setUpUsers() {
            // Aktif kullanıcılar
            User activeUser1 = new User("active1", "active1@example.com", "pass");
            User activeUser2 = new User("active2", "active2@example.com", "pass");
            
            // Pasif kullanıcı
            User inactiveUser = new User("inactive", "inactive@example.com", "pass");
            inactiveUser.setActive(false);

            databaseService.saveUser(activeUser1);
            databaseService.saveUser(activeUser2);
            databaseService.saveUser(inactiveUser);
        }

        @Test
        @DisplayName("Tüm kullanıcıları getirme")
        void testGetAllUsers() {
            // When
            List<User> allUsers = databaseService.getAllUsers();

            // Then
            assertEquals(3, allUsers.size(), "3 kullanıcı olmalı");
        }

        @Test
        @DisplayName("Aktif kullanıcıları getirme")
        void testGetActiveUsers() {
            // When
            List<User> activeUsers = databaseService.getActiveUsers();

            // Then
            assertEquals(2, activeUsers.size(), "2 aktif kullanıcı olmalı");
            assertTrue(activeUsers.stream().allMatch(User::isActive), "Tüm kullanıcılar aktif olmalı");
        }

        @Test
        @DisplayName("Boş veritabanında listeleme")
        void testListUsersInEmptyDatabase() {
            // Given
            databaseService.clearAll();

            // When
            List<User> allUsers = databaseService.getAllUsers();
            List<User> activeUsers = databaseService.getActiveUsers();

            // Then
            assertTrue(allUsers.isEmpty(), "Boş veritabanında tüm kullanıcılar listesi boş olmalı");
            assertTrue(activeUsers.isEmpty(), "Boş veritabanında aktif kullanıcılar listesi boş olmalı");
        }
    }

    @Nested
    @DisplayName("Kullanıcı Güncelleme Testleri")
    class UpdateUserTests {

        @BeforeEach
        void setUpUsers() {
            databaseService.saveUser(testUser);
        }

        @Test
        @DisplayName("Başarılı kullanıcı güncelleme")
        void testUpdateUserSuccessfully() {
            // Given
            testUser.setUsername("updatedusername");
            testUser.setEmail("updated@example.com");

            // When
            User updatedUser = databaseService.updateUser(testUser);

            // Then
            assertEquals("updatedusername", updatedUser.getUsername(), "Username güncellenmelidirli");
            assertEquals("updated@example.com", updatedUser.getEmail(), "Email güncellenmelidirli");

            // Veritabanından kontrol et
            Optional<User> fromDb = databaseService.findUserById(testUser.getId());
            assertTrue(fromDb.isPresent(), "Kullanıcı veritabanında bulunmalı");
            assertEquals("updatedusername", fromDb.get().getUsername(), "DB'deki username güncellenmelidirli");
        }

        @Test
        @DisplayName("Null kullanıcı güncelleme - exception")
        void testUpdateNullUser() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> databaseService.updateUser(null));

            assertTrue(exception.getMessage().contains("null olamaz"), "Exception mesajı uygun olmalı");
        }

        @Test
        @DisplayName("Null ID ile güncelleme - exception")
        void testUpdateUserWithNullId() {
            // Given
            testUser.setId(null);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> databaseService.updateUser(testUser));

            assertTrue(exception.getMessage().contains("null olamaz"), "Exception mesajı uygun olmalı");
        }

        @Test
        @DisplayName("Var olmayan kullanıcı güncelleme - exception")
        void testUpdateNonExistentUser() {
            // Given
            User nonExistentUser = new User("nonexistent", "non@example.com", "pass");
            nonExistentUser.setId(999L);

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> databaseService.updateUser(nonExistentUser));

            assertTrue(exception.getMessage().contains("bulunamadı"), "Exception mesajı uygun olmalı");
        }
    }

    @Nested
    @DisplayName("Kullanıcı Silme Testleri")
    class DeleteUserTests {

        @BeforeEach
        void setUpUsers() {
            databaseService.saveUser(testUser);
        }

        @Test
        @DisplayName("Başarılı kullanıcı silme")
        void testDeleteUserSuccessfully() {
            // Given
            Long userId = testUser.getId();

            // When
            boolean result = databaseService.deleteUser(userId);

            // Then
            assertTrue(result, "Silme işlemi başarılı olmalı");
            assertEquals(0, databaseService.getUserCount(), "Kullanıcı sayısı 0 olmalı");
            assertFalse(databaseService.findUserById(userId).isPresent(), "Kullanıcı bulunamaz olmalı");
        }

        @Test
        @DisplayName("Var olmayan kullanıcı silme")
        void testDeleteNonExistentUser() {
            // When
            boolean result = databaseService.deleteUser(999L);

            // Then
            assertFalse(result, "Var olmayan kullanıcı silme false dönmeli");
            assertEquals(1, databaseService.getUserCount(), "Kullanıcı sayısı değişmemelidirli");
        }

        @Test
        @DisplayName("Null ID ile silme")
        void testDeleteUserWithNullId() {
            // When
            boolean result = databaseService.deleteUser(null);

            // Then
            assertFalse(result, "Null ID için false dönmeli");
            assertEquals(1, databaseService.getUserCount(), "Kullanıcı sayısı değişmemelidirli");
        }
    }

    @Nested
    @DisplayName("Email Kontrol Testleri")
    class EmailExistsTests {

        @BeforeEach
        void setUpUsers() {
            databaseService.saveUser(testUser);
        }

        @Test
        @DisplayName("Var olan email kontrolü")
        void testEmailExists() {
            // When
            boolean exists = databaseService.isEmailExists("test@example.com");

            // Then
            assertTrue(exists, "Var olan email için true dönmeli");
        }

        @Test
        @DisplayName("Var olmayan email kontrolü")
        void testEmailNotExists() {
            // When
            boolean exists = databaseService.isEmailExists("notfound@example.com");

            // Then
            assertFalse(exists, "Var olmayan email için false dönmeli");
        }

        @Test
        @DisplayName("Null email kontrolü")
        void testNullEmailExists() {
            // When
            boolean exists = databaseService.isEmailExists(null);

            // Then
            assertFalse(exists, "Null email için false dönmeli");
        }
    }

    @Nested
    @DisplayName("Yardımcı Metodlar")
    class HelperMethods {

        @Test
        @DisplayName("Kullanıcı sayısı")
        void testGetUserCount() {
            // Initially empty
            assertEquals(0, databaseService.getUserCount(), "Başlangıçta 0 olmalı");

            // Add users
            databaseService.saveUser(testUser);
            assertEquals(1, databaseService.getUserCount(), "1 kullanıcı ekledikten sonra 1 olmalı");

            User user2 = new User("user2", "user2@example.com", "pass");
            databaseService.saveUser(user2);
            assertEquals(2, databaseService.getUserCount(), "2 kullanıcı ekledikten sonra 2 olmalı");

            // Delete user
            databaseService.deleteUser(testUser.getId());
            assertEquals(1, databaseService.getUserCount(), "1 kullanıcı sildikten sonra 1 olmalı");
        }

        @Test
        @DisplayName("Veritabanı temizleme")
        void testClearAll() {
            // Given
            databaseService.saveUser(testUser);
            User user2 = new User("user2", "user2@example.com", "pass");
            databaseService.saveUser(user2);
            assertEquals(2, databaseService.getUserCount(), "2 kullanıcı olmalı");

            // When
            databaseService.clearAll();

            // Then
            assertEquals(0, databaseService.getUserCount(), "Temizlendikten sonra 0 olmalı");
            assertTrue(databaseService.getAllUsers().isEmpty(), "Kullanıcı listesi boş olmalı");

            // ID counter should reset
            User newUser = new User("newuser", "new@example.com", "pass");
            User savedUser = databaseService.saveUser(newUser);
            assertEquals(1L, savedUser.getId(), "ID counter sıfırlanmalı");
        }

        @Test
        @DisplayName("Veritabanı bağlantı kontrolü")
        void testIsConnected() {
            // When
            boolean connected = databaseService.isConnected();

            // Then
            assertTrue(connected, "Test veritabanı her zaman bağlı olmalı");
        }
    }

    @Test
    @DisplayName("Çoklu işlem testi")
    void testMultipleOperations() {
        // Bu test birden fazla işlemi bir arada test eder
        
        // Kullanıcı kaydet
        User user1 = new User("user1", "user1@example.com", "pass1");
        User user2 = new User("user2", "user2@example.com", "pass2");
        
        User savedUser1 = databaseService.saveUser(user1);
        User savedUser2 = databaseService.saveUser(user2);
        
        // Kullanıcıları bul
        Optional<User> foundUser1 = databaseService.findUserById(savedUser1.getId());
        Optional<User> foundUser2 = databaseService.findUserByEmail("user2@example.com");
        
        // Güncelle
        savedUser1.setUsername("updateduser1");
        databaseService.updateUser(savedUser1);
        
        // Pasifleştir
        savedUser2.setActive(false);
        databaseService.updateUser(savedUser2);
        
        // Kontroller
        assertAll("Çoklu işlem kontrolü",
            () -> assertTrue(foundUser1.isPresent(), "User1 bulunmalı"),
            () -> assertTrue(foundUser2.isPresent(), "User2 bulunmalı"),
            () -> assertEquals(2, databaseService.getUserCount(), "Toplam 2 kullanıcı olmalı"),
            () -> assertEquals(1, databaseService.getActiveUsers().size(), "1 aktif kullanıcı olmalı"),
            () -> assertEquals("updateduser1", 
                databaseService.findUserById(savedUser1.getId()).get().getUsername(), 
                "Username güncellenmelidirli"),
            () -> assertFalse(
                databaseService.findUserById(savedUser2.getId()).get().isActive(), 
                "User2 pasif olmalı")
        );
    }
}