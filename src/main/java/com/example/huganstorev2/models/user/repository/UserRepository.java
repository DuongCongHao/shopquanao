package com.example.huganstorev2.models.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.huganstorev2.models.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmail(String email);

    User findByFullName(String fullName);

    List<User> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
        String email,
        String fullName
    );

    // Đăng nhập:
    // Session mới luôn thay thế session cũ.
    // Nhờ vậy nếu người dùng đóng tab/trình duyệt mà không Logout,
    // họ vẫn có thể đăng nhập lại ngay.
    @Modifying
    @Query("""
        UPDATE User u
        SET u.activeSessionId = :sessionId,
            u.sessionLastSeenAt = :now
        WHERE u.id = :userId
        """)
    int claimSession(
        @Param("userId") Long userId,
        @Param("sessionId") String sessionId,
        @Param("now") LocalDateTime now,
        @Param("expiredBefore") LocalDateTime expiredBefore
    );

    // Cập nhật thời gian hoạt động của đúng session hiện tại.
    @Modifying
    @Query("""
        UPDATE User u
        SET u.sessionLastSeenAt = :now
        WHERE u.email = :email
          AND u.activeSessionId = :sessionId
          AND u.sessionLastSeenAt >= :expiredBefore
        """)
    int refreshSession(
        @Param("email") String email,
        @Param("sessionId") String sessionId,
        @Param("now") LocalDateTime now,
        @Param("expiredBefore") LocalDateTime expiredBefore
    );

    // Logout:
    // Chỉ session hiện tại mới có quyền xóa chính nó.
    @Modifying
    @Query("""
        UPDATE User u
        SET u.activeSessionId = NULL,
            u.sessionLastSeenAt = NULL
        WHERE u.email = :email
          AND u.activeSessionId = :sessionId
        """)
    int releaseSession(
        @Param("email") String email,
        @Param("sessionId") String sessionId
    );

    // Kiểm tra JWT có thuộc session đang hoạt động hiện tại hay không.
    boolean existsByEmailAndActiveSessionIdAndSessionLastSeenAtGreaterThanEqual(
        String email,
        String sessionId,
        LocalDateTime expiredBefore
    );
}