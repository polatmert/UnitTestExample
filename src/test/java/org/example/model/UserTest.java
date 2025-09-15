package org.example.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User model sınıfı için JUnit 5 testleri
 * Bu sınıf temel JUnit 5 özelliklerini gösterir:
 * - @Test, @BeforeEach, @AfterEach, @BeforeAll, @AfterAll
 * - @DisplayName ile test açıklamaları
 * - @ParameterizedTest ile parametreli testler
 * - Assertions metodları
 */
@DisplayName("User Model Testleri")
class UserTest {

    private User user;

    @BeforeAll
    static void setUpClass() {
        System.out.println("User testleri başlıyor...");
    }

    @AfterAll
    static void tearDownClass() {
        System.out.println("User testleri tamamlandı.");
    }

    @BeforeEach
    void setUp() {
        // Her test öncesi çalışır
        user = new User("testuser", "test@example.com", "password123");
    }

    @AfterEach
    void tearDown() {
        // Her test sonrası çalışır
        user = null;
    }

    @Test
    @DisplayName("Varsayılan constructor ile user oluşturma")
    void testDefaultConstructor() {
        User newUser = new User();
        
        assertAll("Varsayılan değerler kontrolü",
            () -> assertNull(newUser.getId(), "ID başlangıçta null olmalı"),
            () -> assertTrue(newUser.isActive(), "Kullanıcı varsayılan olarak aktif olmalı"),
            () -> assertNotNull(newUser.getCreatedAt(), "Oluşturulma tarihi null olmamalı"),
            () -> assertTrue(newUser.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)), 
                "Oluşturulma tarihi şu anki zamana yakın olmalı")
        );
    }

    @Test
    @DisplayName("Parametreli constructor ile user oluşturma")
    void testParameterizedConstructor() {
        String expectedUsername = "testuser";
        String expectedEmail = "test@example.com";
        String expectedPassword = "password123";

        assertEquals(expectedUsername, user.getUsername(), "Username doğru atanmalı");
        assertEquals(expectedEmail, user.getEmail(), "Email doğru atanmalı");
        assertEquals(expectedPassword, user.getPassword(), "Password doğru atanmalı");
        assertTrue(user.isActive(), "Yeni kullanıcı aktif olmalı");
        assertNotNull(user.getCreatedAt(), "Oluşturulma tarihi null olmamalı");
    }

    @Test
    @DisplayName("Email validasyonu - geçerli email")
    void testValidEmail() {
        user.setEmail("valid@example.com");
        assertTrue(user.hasValidEmail(), "Geçerli email true dönmeli");
    }

    @ParameterizedTest
    @DisplayName("Email validasyonu - geçersiz emailler")
    @ValueSource(strings = {"", "invalid", "invalid@", "invalid.com"})
    void testInvalidEmails(String invalidEmail) {
        user.setEmail(invalidEmail);
        assertFalse(user.hasValidEmail(), 
            "Geçersiz email false dönmeli: " + invalidEmail);
    }

    @ParameterizedTest
    @DisplayName("Email validasyonu - geçerli emailler")
    @ValueSource(strings = {"test@example.com", "user@domain.org", "name.surname@company.co.uk"})
    void testValidEmails(String validEmail) {
        user.setEmail(validEmail);
        assertTrue(user.hasValidEmail(), 
            "Geçerli email true dönmeli: " + validEmail);
    }

    @Test
    @DisplayName("Null email durumu")
    void testNullEmail() {
        user.setEmail(null);
        assertFalse(user.hasValidEmail(), "Null email false dönmeli");
    }

    @Test
    @DisplayName("Display name - username varken")
    void testGetDisplayNameWithUsername() {
        user.setUsername("JohnDoe");
        assertEquals("JohnDoe", user.getDisplayName(), 
            "Username varken display name username olmalı");
    }

    @Test
    @DisplayName("Display name - username null iken")
    void testGetDisplayNameWithNullUsername() {
        user.setUsername(null);
        assertEquals("Unknown User", user.getDisplayName(), 
            "Username null iken 'Unknown User' dönmeli");
    }

    @ParameterizedTest
    @DisplayName("Display name - farklı username değerleri")
    @CsvSource({
        "'', Unknown User",
        "John, John",
        "Jane Doe, Jane Doe",
        "user123, user123"
    })
    void testGetDisplayNameWithDifferentValues(String username, String expectedDisplayName) {
        if (username.isEmpty()) {
            user.setUsername(null);
        } else {
            user.setUsername(username);
        }
        assertEquals(expectedDisplayName, user.getDisplayName());
    }

    @Test
    @DisplayName("Kullanıcı aktivasyonu")
    void testUserActivation() {
        // Başlangıçta aktif
        assertTrue(user.isActive(), "Yeni kullanıcı aktif olmalı");

        // Pasif yap
        user.setActive(false);
        assertFalse(user.isActive(), "Pasif yapılan kullanıcı aktif olmamalı");

        // Tekrar aktif yap
        user.setActive(true);
        assertTrue(user.isActive(), "Tekrar aktif yapılan kullanıcı aktif olmalı");
    }

    @Test
    @DisplayName("ID atama ve alma")
    void testIdAssignment() {
        assertNull(user.getId(), "Yeni kullanıcının ID'si null olmalı");

        Long expectedId = 123L;
        user.setId(expectedId);
        assertEquals(expectedId, user.getId(), "Atanan ID doğru alınmalı");
    }

    @Test
    @DisplayName("Password güncelleme")
    void testPasswordUpdate() {
        String newPassword = "newPassword456";
        user.setPassword(newPassword);
        assertEquals(newPassword, user.getPassword(), "Password güncellenebilmeli");
    }

    @Test
    @DisplayName("Equals metodu - aynı nesneler")
    void testEqualsWithSameObject() {
        assertTrue(user.equals(user), "Aynı nesne kendisine eşit olmalı");
    }

    @Test
    @DisplayName("Equals metodu - null ile karşılaştırma")
    void testEqualsWithNull() {
        assertFalse(user.equals(null), "Nesne null'a eşit olmamalı");
    }

    @Test
    @DisplayName("Equals metodu - farklı sınıf")
    void testEqualsWithDifferentClass() {
        assertFalse(user.equals("string"), "Farklı sınıf nesnesi eşit olmamalı");
    }

    @Test
    @DisplayName("Equals metodu - aynı ID ve email")
    void testEqualsWithSameIdAndEmail() {
        User user1 = new User("user1", "test@example.com", "pass1");
        User user2 = new User("user2", "test@example.com", "pass2");
        
        user1.setId(1L);
        user2.setId(1L);

        assertTrue(user1.equals(user2), 
            "Aynı ID ve email'e sahip kullanıcılar eşit olmalı");
    }

    @Test
    @DisplayName("HashCode tutarlılığı")
    void testHashCodeConsistency() {
        user.setId(1L);
        int hash1 = user.hashCode();
        int hash2 = user.hashCode();
        
        assertEquals(hash1, hash2, 
            "Aynı nesne için hashCode tutarlı olmalı");
    }

    @Test
    @DisplayName("ToString metodu")
    void testToString() {
        user.setId(1L);
        String toString = user.toString();
        
        assertAll("ToString kontrolü",
            () -> assertNotNull(toString, "ToString null olmamalı"),
            () -> assertTrue(toString.contains("User{"), "ToString 'User{' içermeli"),
            () -> assertTrue(toString.contains("id=1"), "ToString ID içermeli"),
            () -> assertTrue(toString.contains("testuser"), "ToString username içermeli"),
            () -> assertTrue(toString.contains("test@example.com"), "ToString email içermeli")
        );
    }

    @Test
    @DisplayName("Oluşturulma zamanı kontrolü")
    void testCreatedAtTime() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        User newUser = new User();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertTrue(newUser.getCreatedAt().isAfter(before), 
            "Oluşturulma zamanı test başlangıcından sonra olmalı");
        assertTrue(newUser.getCreatedAt().isBefore(after), 
            "Oluşturulma zamanı test bitiminden önce olmalı");
    }

    @Test
    @DisplayName("Tüm alanların setter/getter testi")
    void testAllSettersAndGetters() {
        Long id = 999L;
        String username = "newusername";
        String email = "newemail@test.com";
        String password = "newpassword";
        boolean active = false;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setActive(active);
        user.setCreatedAt(createdAt);

        assertAll("Tüm alanlar doğru atanmalı",
            () -> assertEquals(id, user.getId()),
            () -> assertEquals(username, user.getUsername()),
            () -> assertEquals(email, user.getEmail()),
            () -> assertEquals(password, user.getPassword()),
            () -> assertEquals(active, user.isActive()),
            () -> assertEquals(createdAt, user.getCreatedAt())
        );
    }
}