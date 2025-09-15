package org.example.service;

import org.example.model.User;
import java.util.*;

/**
 * Veritabanı işlemlerini simüle eden service sınıfı
 * Bu sınıf test amaçlı basit bir in-memory database simülasyonu yapar
 */
public class DatabaseService {
    private final Map<Long, User> users = new HashMap<>();
    private long nextUserId = 1L;

    /**
     * Kullanıcıyı veritabanına kaydeder
     */
    public User saveUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Kullanıcı null olamaz");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Kullanıcı adı boş olamaz");
        }
        
        if (user.getEmail() == null || !user.hasValidEmail()) {
            throw new IllegalArgumentException("Geçerli bir email adresi gerekli");
        }

        // Email zaten kayıtlı mı kontrol et
        if (isEmailExists(user.getEmail())) {
            throw new IllegalStateException("Bu email adresi zaten kayıtlı");
        }

        user.setId(nextUserId++);
        users.put(user.getId(), user);
        return user;
    }

    /**
     * ID'ye göre kullanıcı bulur
     */
    public Optional<User> findUserById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(id));
    }

    /**
     * Email adresine göre kullanıcı bulur
     */
    public Optional<User> findUserByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        
        return users.values().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst();
    }

    /**
     * Tüm kullanıcıları getirir
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    /**
     * Aktif kullanıcıları getirir
     */
    public List<User> getActiveUsers() {
        return users.values().stream()
                .filter(User::isActive)
                .toList();
    }

    /**
     * Kullanıcıyı günceller
     */
    public User updateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Kullanıcı veya ID null olamaz");
        }

        if (!users.containsKey(user.getId())) {
            throw new IllegalStateException("Güncellenecek kullanıcı bulunamadı");
        }

        users.put(user.getId(), user);
        return user;
    }

    /**
     * Kullanıcıyı siler
     */
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            return false;
        }
        return users.remove(userId) != null;
    }

    /**
     * Email adresinin zaten kayıtlı olup olmadığını kontrol eder
     */
    public boolean isEmailExists(String email) {
        return findUserByEmail(email).isPresent();
    }

    /**
     * Veritabanındaki toplam kullanıcı sayısını döndürür
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * Veritabanını temizler (test amaçlı)
     */
    public void clearAll() {
        users.clear();
        nextUserId = 1L;
    }

    /**
     * Veritabanı bağlantısını simüle eder
     */
    public boolean isConnected() {
        // Gerçek uygulamada veritabanı bağlantısını kontrol ederdi
        return true;
    }
}