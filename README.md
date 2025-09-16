# JUnit ve Mockito Öğrenme Projesi

Bu proje Java'da **JUnit 5** ve **Mockito** framework'lerini öğrenmek için tasarlanmış kapsamlı bir örnek uygulamadır. Proje, gerçek dünya senaryolarını simüle eden kullanıcı yönetimi sistemi üzerinden test yazma tekniklerini gösterir.

## 📋 İçindekiler

- [Proje Hakkında](#proje-hakkında)
- [Teknolojiler](#teknolojiler)
- [Proje Yapısı](#proje-yapısı)
- [Kurulum](#kurulum)
- [Testleri Çalıştırma](#testleri-çalıştırma)
- [Test Türleri](#test-türleri)
- [Öğrenilen Konular](#öğrenilen-konular)
- [Sınıf Açıklamaları](#sınıf-açıklamaları)

## 🎯 Proje Hakkında

Bu proje aşağıdaki test kavramlarını öğretir:

- **Unit Testing**: Tek bir sınıfın/metodun izole test edilmesi
- **Integration Testing**: Birden fazla bileşenin birlikte test edilmesi
- **Mocking**: Dış bağımlılıkları simüle etme
- **Test Doubles**: Mock, Spy, Stub kavramları
- **Assertion Techniques**: Farklı doğrulama yöntemleri

## 🛠 Teknolojiler

- **Java 22**
- **Maven** - Proje yönetimi
- **JUnit 5** - Test framework'ü
- **Mockito 5** - Mocking framework'ü
- **AssertJ** - Fluent assertion library

## 📁 Proje Yapısı

```
src/
├── main/java/org/example/
│   ├── model/
│   │   ├── User.java              # Kullanıcı model sınıfı
│   │   └── Order.java             # Sipariş model sınıfı
│   └── service/
│       ├── UserService.java       # Ana kullanıcı servisi
│       ├── DatabaseService.java   # Veritabanı işlemleri
│       └── EmailService.java      # Email gönderimi
└── test/java/org/example/
    ├── model/
    │   ├── UserTest.java           # User model testleri
    │   └── OrderTest.java          # Order model testleri
    ├── service/
    │   ├── UserServiceTest.java    # Mockito ile UserService testleri
    │   ├── EmailServiceTest.java   # Gerçek service testleri
    │   ├── EmailServiceMockitoTest.java # İleri düzey Mockito/Spy örnekleri
    │   └── DatabaseServiceTest.java # Gerçek service testleri
    └── integration/
        └── UserServiceIntegrationTest.java # Integration testleri
```

## ⚙️ Kurulum

1. **Projeyi klonlayın:**
   ```bash
   git clone <repo-url>
   cd Junit-Example
   ```

2. **Maven bağımlılıklarını yükleyin:**
   ```bash
   mvn clean install
   ```

3. **IDE'nizde projeyi açın** (IntelliJ IDEA, Eclipse, VS Code)

## 🚀 Testleri Çalıştırma

### Tüm testleri çalıştırma:
```bash
mvn test
```

### Sadece unit testleri çalıştırma:
```bash
mvn test -Dtest="*Test"
```

### Sadece integration testleri çalıştırma:
```bash
mvn test -Dtest="*IntegrationTest"
```

### Belirli bir test sınıfını çalıştırma:
```bash
mvn test -Dtest="UserServiceTest"
```

### Belirli bir test metodunu çalıştırma:
```bash
mvn test -Dtest="UserServiceTest#testSuccessfulUserRegistration"
```

## 🧪 Test Türleri

### 1. Model Testleri (Unit Tests)
- **UserTest**: Temel JUnit 5 özelliklerini gösterir
  - `@Test`, `@BeforeEach`, `@AfterEach` annotations
  - `@DisplayName` ile test açıklamaları
  - `@ParameterizedTest` ile parametreli testler
  - `assertAll()` ile grouped assertions

- **OrderTest**: Nested test sınıfları ve karmaşık test senaryoları
  - `@Nested` ile test gruplandırma
  - Enum testing
  - Complex business logic testing

### 2. Service Testleri (Mockito Tests)
- **UserServiceTest**: Kapsamlı Mockito örnekleri
  - `@Mock` ve `@InjectMocks` annotations
  - `@Captor` ile ArgumentCaptor kullanımı
  - `when().thenReturn()` ile mock behavior tanımlama
  - `verify()` ile method call doğrulama
  - Exception testing

- **EmailServiceTest**: Gerçek service testing
  - Gerçek EmailService instance kullanımı  
  - Service state management testing
  - Thread interruption testing

- **EmailServiceMockitoTest**: İleri düzey Mockito özellikleri
  - Manuel spy oluşturma (Java 22 uyumluluğu)
  - Partial mocking ve behavior verification
  - ArgumentCaptor ve Answer interface kullanımı

- **DatabaseServiceTest**: Gerçek service testing
  - Mock kullanmadan gerçek sınıf testleri
  - Test isolation teknikleri
  - Multiple operation testing

### 3. Integration Testleri
- **UserServiceIntegrationTest**: End-to-end test senaryoları
  - Tüm bileşenlerin birlikte çalışması
  - `@TestMethodOrder` ile test sırası
  - `@EnabledOnOs` ile koşullu testler
  - Karmaşık iş akışı testleri

## 📚 Öğrenilen Konular

### JUnit 5 Temelleri
- Test yaşam döngüsü (`@BeforeEach`, `@AfterEach`, `@BeforeAll`, `@AfterAll`)
- Assertion metodları (`assertEquals`, `assertTrue`, `assertAll`, etc.)
- Parametreli testler (`@ParameterizedTest`, `@ValueSource`, `@CsvSource`)
- Test gruplandırma (`@Nested`)
- Koşullu testler (`@EnabledOnOs`)
- Exception testing (`assertThrows`)

### Mockito Teknikleri
- Mock objeler (`@Mock`)
- Dependency injection (`@InjectMocks`)
- Mock behavior (`when().thenReturn()`, `when().thenThrow()`)
- Method call verification (`verify()`)
- Argument capturing (`@Captor`)
- Spy objeler (`@Spy`)
- Partial mocking

### Test Best Practices
- Test isolation (her test bağımsız)
- Arrange-Act-Assert pattern
- Meaningful test names
- Test data setup
- Exception handling in tests
- Performance considerations

## 📖 Sınıf Açıklamaları

### Model Sınıfları

#### User.java
Kullanıcı bilgilerini temsil eden model sınıfı:
- Temel kullanıcı özellikleri (id, username, email, password)
- Email validasyonu
- Aktif/pasif durum yönetimi
- equals/hashCode implementasyonu

#### Order.java
Sipariş sistemini temsil eden karmaşık model:
- Sipariş durumu enum'u
- OrderItem iç sınıfı
- Toplam tutar hesaplama
- İş mantığı metodları (iptal etme, ürün ekleme/çıkarma)

### Service Sınıfları

#### UserService.java
Ana kullanıcı işlemleri servisi:
- Kullanıcı kaydı (DatabaseService ve EmailService dependency'leri)
- Giriş işlemleri
- Profil güncelleme
- Şifre sıfırlama
- Toplu işlemler
- **Mocking için mükemmel örnek** (external dependencies)

#### DatabaseService.java
Veritabanı işlemlerini simüle eden servis:
- CRUD işlemleri
- In-memory database simülasyonu
- Data validation
- **Gerçek service testing için ideal**

#### EmailService.java
Email gönderimi servisi:
- Çeşitli email türleri (hoş geldin, şifre sıfırlama, bildirim)
- Email logging
- Service enable/disable
- **Spy testing için mükemmel örnek**

## 💡 Test Yazma İpuçları

### 1. Test Adlandırma
```java
@DisplayName("Başarılı kullanıcı kaydı")
void testSuccessfulUserRegistration() { ... }
```

### 2. Grouped Assertions
```java
assertAll("Kullanıcı kontrolü",
    () -> assertEquals(expectedId, user.getId()),
    () -> assertEquals(expectedEmail, user.getEmail()),
    () -> assertTrue(user.isActive())
);
```

### 3. Parametreli Testler
```java
@ParameterizedTest
@ValueSource(strings = {"", "invalid", "invalid@"})
void testInvalidEmails(String invalidEmail) { ... }
```

### 4. Mock Behavior
```java
when(databaseService.saveUser(any(User.class))).thenReturn(testUser);
when(emailService.sendEmail(anyString())).thenThrow(new RuntimeException());
```

### 5. Verification
```java
verify(databaseService).saveUser(userCaptor.capture());
verify(emailService, times(1)).sendWelcomeEmail(testUser);
verify(databaseService, never()).deleteUser(anyLong());
```

## 🎓 Öğrenme Sırası Önerisi

1. **Başlangıç**: `UserTest.java` ile JUnit 5 temellerini öğrenin
2. **Model Testing**: `OrderTest.java` ile karmaşık testleri inceleyin
3. **Mocking**: `UserServiceTest.java` ile Mockito'yu öğrenin
4. **Spy Testing**: `EmailServiceTest.java` ile partial mocking'i keşfedin
5. **Real Service**: `DatabaseServiceTest.java` ile gerçek servis testlerini görün
6. **Integration**: `UserServiceIntegrationTest.java` ile end-to-end testleri öğrenin

## 🚨 Önemli Notlar

- Her test independent olmalı (test isolation)
- Mock'lar sadece external dependencies için kullanılmalı
- Integration testler daha yavaş ama daha gerçekçidir
- Test data setup'ına özen gösterin
- Exception scenariolarını da test edin
- Test coverage %100 olmak zorunda değil, kritik kısımları kapsamak önemli

### Java 22 Uyumluluğu
- Proje Java 22 ile test edilmiştir
- Mockito 5.12.0 ve Byte Buddy 1.14.15 Java 22 desteği sağlar
- @Spy annotation yerine manuel spy() kullanımı önerilir
- EmailServiceMockitoTest sınıfı Java 22 uyumlu Spy örnekleri içerir

## 🔧 IDE Konfigürasyonu

### IntelliJ IDEA
- JUnit 5 test runner'ı etkinleştirin
- Mockito plugin'ini yükleyin
- Code coverage tool'unu aktifleştirin

### VS Code
- Java Test Runner extension'ı yükleyin
- Maven support extension'ı aktifleştirin

---

Bu proje ile JUnit ve Mockito'yu öğrenmek için gereken tüm temel bilgileri edinebilirsiniz. 
Her test sınıfı farklı konulara odaklanır ve progresif bir öğrenme deneyimi sunar.

**İyi çalışmalar! 🚀**