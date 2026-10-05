package com.example.huganstorev2.models.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.huganstorev2.models.user.entity.User;

import java.util.List;
import java.time.LocalDateTime;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByEmail(String email);
    User findByFullName(String fullName);
    List<User> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(String email, String fullName);

    @Modifying
    @Query("""
        UPDATE User u
        SET u.activeSessionId = :sessionId, u.sessionLastSeenAt = :now
        WHERE u.id = :userId
          AND (u.activeSessionId IS NULL OR u.sessionLastSeenAt IS NULL OR u.sessionLastSeenAt < :expiredBefore)
        """)
    int claimSession(
        @Param("userId") Long userId,
        @Param("sessionId") String sessionId,
        @Param("now") LocalDateTime now,
        @Param("expiredBefore") LocalDateTime expiredBefore
    );

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

    @Modifying
    @Query("""
        UPDATE User u
        SET u.activeSessionId = NULL, u.sessionLastSeenAt = NULL
        WHERE u.email = :email AND u.activeSessionId = :sessionId
        """)
    int releaseSession(@Param("email") String email, @Param("sessionId") String sessionId);

    boolean existsByEmailAndActiveSessionIdAndSessionLastSeenAtGreaterThanEqual(
        String email,
        String sessionId,
        LocalDateTime expiredBefore
    );
}
