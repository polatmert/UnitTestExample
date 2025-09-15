package org.example.service;

import org.example.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

/**
 * EmailService için alternatif Mockito testleri
 * Bu sınıf manual spy oluşturma ve partial mocking örnekleri içerir
 * Java 22 uyumluluğu için @Spy annotation yerine manuel spy kullanır
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Mockito İleri Düzey Testleri")
class EmailServiceMockitoTest {

    private EmailService emailService;
    private EmailService spyEmailService;
    private User testUser;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
        spyEmailService = spy(emailService);
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
    }

    @Nested
    @DisplayName("Manuel Spy Testleri")
    class ManualSpyTests {

        @Test
        @DisplayName("Spy ile partial mocking")
        void testSpyWithPartialMocking() {
            // Given - Manuel spy kullanımı
            // Gerçek metodu çağıracak ama davranışını gözlemleyebiliriz
            
            // When
            boolean result = spyEmailService.sendWelcomeEmail(testUser);

            // Then
            assertTrue(result, "Spy ile email gönderimi başarılı olmalı");
            
            // Spy ile metod çağrılarını doğrulayabiliriz
            verify(spyEmailService, times(1)).sendWelcomeEmail(testUser);
            
            // Gerçek implementasyon çalıştığı için email gönderilmiş olmalı
            assertEquals(1, spyEmailService.getSentEmailCount(), "Gerçek email gönderilmelidirli");
        }

        @Test
        @DisplayName("Spy ile metod çağrı sırası doğrulama")
        void testSpyMethodCallOrder() {
            // When
            spyEmailService.sendWelcomeEmail(testUser);
            spyEmailService.sendNotificationEmail(testUser, "test message");

            // Then - Çağrı sırasını doğrulayabiliriz
            var inOrder = inOrder(spyEmailService);
            inOrder.verify(spyEmailService).sendWelcomeEmail(testUser);
            inOrder.verify(spyEmailService).sendNotificationEmail(testUser, "test message");
        }

        @Test
        @DisplayName("Spy ile argüman doğrulama")
        void testSpyArgumentVerification() {
            // Given
            String testMessage = "Özel test mesajı";

            // When
            spyEmailService.sendNotificationEmail(testUser, testMessage);

            // Then - Argümanları doğrulayabiliriz
            verify(spyEmailService).sendNotificationEmail(
                argThat(user -> user.getEmail().equals("test@example.com")),
                eq(testMessage)
            );
        }
    }

    @Nested
    @DisplayName("Mock vs Spy Karşılaştırması")
    class MockVsSpyComparison {

        @Test
        @DisplayName("Mock ile tam davranış kontrolü")
        void testFullMockBehavior() {
            // Given - Tam mock oluştur
            EmailService mockEmailService = mock(EmailService.class);
            
            // Tüm davranışları tanımlamalıyız
            when(mockEmailService.sendWelcomeEmail(any(User.class))).thenReturn(true);
            when(mockEmailService.getSentEmailCount()).thenReturn(1);

            // When
            boolean result = mockEmailService.sendWelcomeEmail(testUser);

            // Then
            assertTrue(result, "Mock'lanan davranış true dönmelidirli");
            assertEquals(1, mockEmailService.getSentEmailCount(), "Mock'lanan davranış 1 dönmelidirli");
            
            verify(mockEmailService).sendWelcomeEmail(testUser);
        }

        @Test
        @DisplayName("Spy ile varsayılan davranış korunması")
        void testSpyDefaultBehaviorPreservation() {
            // Given - Spy gerçek implementasyonu korur
            // Hiçbir davranış tanımlamadık

            // When
            boolean result = spyEmailService.sendWelcomeEmail(testUser);

            // Then
            assertTrue(result, "Spy gerçek implementasyonu çalıştırır");
            assertEquals(1, spyEmailService.getSentEmailCount(), "Gerçek email gönderilir");
            
            // Email log'larını da kontrol edebiliriz
            List<EmailService.EmailLog> sentEmails = spyEmailService.getSentEmails();
            assertEquals(1, sentEmails.size(), "1 email log'u olmalı");
        }
    }

    @Nested
    @DisplayName("Exception Handling ile Spy")
    class SpyExceptionHandling {

        @Test
        @DisplayName("Spy ile exception simulation")
        void testSpyExceptionSimulation() {
            // Given - Spy'da belirli durumda exception fırlatmasını sağlayabiliriz
            doThrow(new RuntimeException("Email service down"))
                .when(spyEmailService).sendNotificationEmail(any(User.class), eq("error"));

            // When & Then
            assertThrows(RuntimeException.class, () -> 
                spyEmailService.sendNotificationEmail(testUser, "error"));

            // Diğer çağrılar normal çalışmalı
            assertTrue(spyEmailService.sendWelcomeEmail(testUser), 
                "Normal çağrılar çalışmalı");
        }

        @Test
        @DisplayName("Spy ile void metod verification")
        void testSpyVoidMethodVerification() {
            // When
            spyEmailService.clearEmailLogs();
            spyEmailService.setEmailServiceEnabled(false);

            // Then - Void metodları da verify edebiliriz
            verify(spyEmailService).clearEmailLogs();
            verify(spyEmailService).setEmailServiceEnabled(false);
            
            // Durumu kontrol et
            assertFalse(spyEmailService.isEmailServiceEnabled(), "Service disabled olmalı");
        }
    }

    @Nested
    @DisplayName("Advanced Mockito Features")
    class AdvancedMockitoFeatures {

        @Test
        @DisplayName("Captor ile argüman yakalama")
        void testArgumentCaptor() {
            // Given
            var userCaptor = ArgumentCaptor.forClass(User.class);
            var messageCaptor = ArgumentCaptor.forClass(String.class);

            // When
            spyEmailService.sendNotificationEmail(testUser, "Captured message");

            // Then
            verify(spyEmailService).sendNotificationEmail(userCaptor.capture(), messageCaptor.capture());
            
            assertEquals(testUser.getEmail(), userCaptor.getValue().getEmail(), 
                "Yakalanan user email'i doğru olmalı");
            assertEquals("Captured message", messageCaptor.getValue(), 
                "Yakalanan mesaj doğru olmalı");
        }

        @Test
        @DisplayName("Timeout verification")
        void testTimeoutVerification() {
            // When
            spyEmailService.sendWelcomeEmail(testUser);

            // Then - Belirli süre içinde çağrıldığını doğrula
            verify(spyEmailService, timeout(1000)).sendWelcomeEmail(testUser);
        }
    }

    @Test
    @DisplayName("Spy vs gerçek implementasyon karşılaştırması")
    void testSpyVsRealImplementationComparison() {
        // Given
        EmailService realService = new EmailService();
        
        // When - Aynı işlemi hem spy hem gerçek service'te yapalım
        boolean spyResult = spyEmailService.sendWelcomeEmail(testUser);
        boolean realResult = realService.sendWelcomeEmail(testUser);

        // Then - Her ikisi de aynı sonucu vermeli
        assertEquals(realResult, spyResult, "Spy ve gerçek implementasyon aynı sonucu vermelidirli");
        
        // Spy'da verification yapabiliriz, gerçek service'te yapamayız
        verify(spyEmailService).sendWelcomeEmail(testUser);
        
        // Ama sonuçlar aynı olmalı
        assertEquals(realService.getSentEmailCount(), spyEmailService.getSentEmailCount(),
            "Email sayıları aynı olmalı");
    }
}