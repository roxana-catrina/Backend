package Licenta.Licenta.Repository;



import Licenta.Licenta.Model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByEmailAndCodeAndUsedFalse(String email, String code);

    Optional<PasswordResetToken> findTopByEmailOrderByExpiryDateDesc(String email);

    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :dateTime")
    void deleteByExpiryDateBefore(@Param("dateTime") LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.email = :email")
    void deleteByEmail(@Param("email") String email);
}