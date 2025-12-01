package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByEmailAndCodeAndUsedFalse(String email, String code);

    Optional<PasswordResetToken> findTopByEmailOrderByExpiryDateDesc(String email);

    // Delete expired tokens
    void deleteByExpiryDateBefore(LocalDateTime date);

    // Delete used tokens
    void deleteByUsedTrue();

    // Delete by email
    void deleteByEmail(String email);
}