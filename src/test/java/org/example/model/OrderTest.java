package org.example.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Order model sınıfı için JUnit 5 testleri
 * Nested test classes ve daha karmaşık assertion'lar gösterilir
 */
@DisplayName("Order Model Testleri")
class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(123L);
    }

    @Nested
    @DisplayName("Constructor Testleri")
    class ConstructorTests {

        @Test
        @DisplayName("Varsayılan constructor")
        void testDefaultConstructor() {
            Order newOrder = new Order();
            
            assertAll("Varsayılan değerler",
                () -> assertNull(newOrder.getUserId(), "UserID null olmalı"),
                () -> assertNotNull(newOrder.getItems(), "Items listesi null olmamalı"),
                () -> assertTrue(newOrder.getItems().isEmpty(), "Items listesi boş olmalı"),
                () -> assertEquals(BigDecimal.ZERO, newOrder.getTotalAmount(), "Toplam tutar 0 olmalı"),
                () -> assertEquals(Order.OrderStatus.PENDING, newOrder.getStatus(), "Durum PENDING olmalı"),
                () -> assertNotNull(newOrder.getCreatedAt(), "Oluşturulma tarihi null olmamalı"),
                () -> assertNotNull(newOrder.getUpdatedAt(), "Güncellenme tarihi null olmamalı")
            );
        }

        @Test
        @DisplayName("Parametreli constructor")
        void testParameterizedConstructor() {
            Long userId = 456L;
            Order newOrder = new Order(userId);
            
            assertEquals(userId, newOrder.getUserId(), "UserID doğru atanmalı");
            assertEquals(Order.OrderStatus.PENDING, newOrder.getStatus(), "Varsayılan durum PENDING olmalı");
        }
    }

    @Nested
    @DisplayName("Item İşlemleri")
    class ItemOperations {

        @Test
        @DisplayName("Item ekleme")
        void testAddItem() {
            Order.OrderItem item = new Order.OrderItem("Laptop", new BigDecimal("1500.00"), 1);
            
            order.addItem(item);
            
            assertAll("Item eklendikten sonra",
                () -> assertEquals(1, order.getItems().size(), "Item sayısı 1 olmalı"),
                () -> assertTrue(order.getItems().contains(item), "Eklenen item listede olmalı"),
                () -> assertEquals(new BigDecimal("1500.00"), order.getTotalAmount(), "Toplam tutar güncellenmelidirli")
            );
        }

        @Test
        @DisplayName("Birden fazla item ekleme")
        void testAddMultipleItems() {
            Order.OrderItem item1 = new Order.OrderItem("Laptop", new BigDecimal("1500.00"), 1);
            Order.OrderItem item2 = new Order.OrderItem("Mouse", new BigDecimal("25.50"), 2);
            
            order.addItem(item1);
            order.addItem(item2);
            
            assertAll("Birden fazla item eklendikten sonra",
                () -> assertEquals(2, order.getItems().size(), "Item sayısı 2 olmalı"),
                () -> assertEquals(new BigDecimal("1551.00"), order.getTotalAmount(), "Toplam tutar doğru hesaplanmalı"),
                () -> assertEquals(3, order.getItemCount(), "Toplam ürün adedi 3 olmalı")
            );
        }

        @Test
        @DisplayName("Item çıkarma")
        void testRemoveItem() {
            Order.OrderItem item1 = new Order.OrderItem("Laptop", new BigDecimal("1500.00"), 1);
            Order.OrderItem item2 = new Order.OrderItem("Mouse", new BigDecimal("25.50"), 2);
            
            order.addItem(item1);
            order.addItem(item2);
            order.removeItem(item1);
            
            assertAll("Item çıkarıldıktan sonra",
                () -> assertEquals(1, order.getItems().size(), "Item sayısı 1 olmalı"),
                () -> assertFalse(order.getItems().contains(item1), "Çıkarılan item listede olmamalı"),
                () -> assertEquals(new BigDecimal("51.00"), order.getTotalAmount(), "Toplam tutar güncellenmelidirli")
            );
        }

        @Test
        @DisplayName("Boş sipariş kontrolü")
        void testEmptyOrder() {
            assertTrue(order.isEmpty(), "Yeni sipariş boş olmalı");
            
            Order.OrderItem item = new Order.OrderItem("Test", new BigDecimal("10.00"), 1);
            order.addItem(item);
            
            assertFalse(order.isEmpty(), "Item eklendikten sonra boş olmamalı");
        }
    }

    @Nested
    @DisplayName("Durum Yönetimi")
    class StatusManagement {

        @Test
        @DisplayName("Durum değiştirme")
        void testStatusChange() {
            LocalDateTime beforeUpdate = order.getUpdatedAt();
            
            // Durum değiştirme zaman alabilir, bu yüzden kısa bir bekleme
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            order.setStatus(Order.OrderStatus.CONFIRMED);
            
            assertAll("Durum değiştirildikten sonra",
                () -> assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus(), "Durum CONFIRMED olmalı"),
                () -> assertTrue(order.getUpdatedAt().isAfter(beforeUpdate), "Güncellenme tarihi ileri alınmalı")
            );
        }

        @ParameterizedTest
        @DisplayName("Tüm durum değerleri")
        @EnumSource(Order.OrderStatus.class)
        void testAllStatusValues(Order.OrderStatus status) {
            order.setStatus(status);
            assertEquals(status, order.getStatus(), "Durum doğru atanmalı: " + status);
        }

        @Test
        @DisplayName("İptal edilebilir durum kontrolü")
        void testCanBeCancelled() {
            // PENDING durumunda iptal edilebilir
            order.setStatus(Order.OrderStatus.PENDING);
            assertTrue(order.canBeCancelled(), "PENDING durumunda iptal edilebilmeli");

            // CONFIRMED durumunda iptal edilebilir
            order.setStatus(Order.OrderStatus.CONFIRMED);
            assertTrue(order.canBeCancelled(), "CONFIRMED durumunda iptal edilebilmeli");

            // SHIPPED durumunda iptal edilemez
            order.setStatus(Order.OrderStatus.SHIPPED);
            assertFalse(order.canBeCancelled(), "SHIPPED durumunda iptal edilememeli");

            // DELIVERED durumunda iptal edilemez
            order.setStatus(Order.OrderStatus.DELIVERED);
            assertFalse(order.canBeCancelled(), "DELIVERED durumunda iptal edilememeli");

            // CANCELLED durumunda iptal edilemez
            order.setStatus(Order.OrderStatus.CANCELLED);
            assertFalse(order.canBeCancelled(), "CANCELLED durumunda iptal edilememeli");
        }

        @Test
        @DisplayName("Başarılı iptal işlemi")
        void testSuccessfulCancellation() {
            order.setStatus(Order.OrderStatus.PENDING);
            
            assertDoesNotThrow(() -> order.cancel(), "İptal işlemi exception fırlatmamalı");
            assertEquals(Order.OrderStatus.CANCELLED, order.getStatus(), "Durum CANCELLED olmalı");
        }

        @ParameterizedTest
        @DisplayName("Başarısız iptal işlemi")
        @ValueSource(strings = {"SHIPPED", "DELIVERED", "CANCELLED"})
        void testFailedCancellation(String statusName) {
            Order.OrderStatus status = Order.OrderStatus.valueOf(statusName);
            order.setStatus(status);
            
            IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> order.cancel(), "İptal edilemeyen durumda exception fırlatmalı");
            
            assertTrue(exception.getMessage().contains("iptal edilemez"), 
                "Exception mesajı 'iptal edilemez' içermeli");
        }
    }

    @Nested
    @DisplayName("Hesaplama Testleri")
    class CalculationTests {

        @Test
        @DisplayName("Toplam tutar hesaplama")
        void testTotalAmountCalculation() {
            Order.OrderItem item1 = new Order.OrderItem("Product1", new BigDecimal("100.50"), 2);
            Order.OrderItem item2 = new Order.OrderItem("Product2", new BigDecimal("75.25"), 3);
            
            order.addItem(item1);
            order.addItem(item2);
            
            BigDecimal expectedTotal = new BigDecimal("100.50")
                .multiply(BigDecimal.valueOf(2))
                .add(new BigDecimal("75.25").multiply(BigDecimal.valueOf(3)));
            
            assertEquals(expectedTotal, order.getTotalAmount(), 
                "Toplam tutar doğru hesaplanmalı");
        }

        @Test
        @DisplayName("Ürün adedi hesaplama")
        void testItemCountCalculation() {
            order.addItem(new Order.OrderItem("Product1", new BigDecimal("10.00"), 5));
            order.addItem(new Order.OrderItem("Product2", new BigDecimal("20.00"), 3));
            order.addItem(new Order.OrderItem("Product3", new BigDecimal("30.00"), 2));
            
            assertEquals(10, order.getItemCount(), "Toplam ürün adedi 10 olmalı");
        }

        @Test
        @DisplayName("Manuel toplam hesaplama")
        void testManualCalculateTotalAmount() {
            order.addItem(new Order.OrderItem("Product", new BigDecimal("50.00"), 2));
            
            // Manuel hesaplama çağrısı
            order.calculateTotalAmount();
            
            assertEquals(new BigDecimal("100.00"), order.getTotalAmount(), 
                "Manuel hesaplama doğru çalışmalı");
        }
    }

    @Nested
    @DisplayName("OrderItem Testleri")
    class OrderItemTests {

        @Test
        @DisplayName("OrderItem oluşturma")
        void testOrderItemCreation() {
            String productName = "Test Product";
            BigDecimal price = new BigDecimal("99.99");
            int quantity = 3;
            
            Order.OrderItem item = new Order.OrderItem(productName, price, quantity);
            
            assertAll("OrderItem özellikleri",
                () -> assertEquals(productName, item.getProductName(), "Ürün adı doğru atanmalı"),
                () -> assertEquals(price, item.getPrice(), "Fiyat doğru atanmalı"),
                () -> assertEquals(quantity, item.getQuantity(), "Miktar doğru atanmalı")
            );
        }

        @Test
        @DisplayName("OrderItem toplam fiyat hesaplama")
        void testOrderItemTotalPrice() {
            Order.OrderItem item = new Order.OrderItem("Product", new BigDecimal("25.50"), 4);
            BigDecimal expectedTotal = new BigDecimal("102.00");
            
            assertEquals(expectedTotal, item.getTotalPrice(), 
                "OrderItem toplam fiyatı doğru hesaplanmalı");
        }

        @Test
        @DisplayName("OrderItem equals metodu")
        void testOrderItemEquals() {
            Order.OrderItem item1 = new Order.OrderItem("Product", new BigDecimal("10.00"), 2);
            Order.OrderItem item2 = new Order.OrderItem("Product", new BigDecimal("10.00"), 2);
            Order.OrderItem item3 = new Order.OrderItem("Product", new BigDecimal("15.00"), 2);
            
            assertEquals(item1, item2, "Aynı özelliklere sahip itemlar eşit olmalı");
            assertNotEquals(item1, item3, "Farklı fiyatlı itemlar eşit olmamalı");
        }

        @Test
        @DisplayName("OrderItem hashCode")
        void testOrderItemHashCode() {
            Order.OrderItem item = new Order.OrderItem("Product", new BigDecimal("10.00"), 2);
            int hash1 = item.hashCode();
            int hash2 = item.hashCode();
            
            assertEquals(hash1, hash2, "Aynı nesne için hashCode tutarlı olmalı");
        }
    }

    @Test
    @DisplayName("Order equals metodu")
    void testOrderEquals() {
        Order order1 = new Order(100L);
        Order order2 = new Order(200L);
        
        order1.setId(1L);
        order2.setId(1L);
        
        assertEquals(order1, order2, "Aynı ID'ye sahip siparişler eşit olmalı");
    }

    @Test
    @DisplayName("Order toString metodu")
    void testOrderToString() {
        order.setId(123L);
        order.addItem(new Order.OrderItem("Product", new BigDecimal("10.00"), 1));
        
        String toString = order.toString();
        
        assertAll("ToString kontrolü",
            () -> assertNotNull(toString, "ToString null olmamalı"),
            () -> assertTrue(toString.contains("Order{"), "ToString 'Order{' içermeli"),
            () -> assertTrue(toString.contains("id=123"), "ToString ID içermeli"),
            () -> assertTrue(toString.contains("userId=123"), "ToString userID içermeli")
        );
    }

}