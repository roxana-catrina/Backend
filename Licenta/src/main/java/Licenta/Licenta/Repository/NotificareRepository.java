package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.Notificare;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificareRepository extends MongoRepository<Notificare, String> {

    // Find all notifications for a user
    List<Notificare> findByUserIdOrderByDataCreareDesc(String userId);

    // Find unread notifications for a user
    List<Notificare> findByUserIdAndCititOrderByDataCreareDesc(String userId, Boolean citit);

    // Count unread notifications
    Long countByUserIdAndCitit(String userId, Boolean citit);

    // Find notifications by type
    List<Notificare> findByUserIdAndTipOrderByDataCreareDesc(String userId, String tip);

    // Delete old read notifications
    void deleteByUserIdAndCititAndDataCreareBefore(String userId, Boolean citit, java.time.LocalDateTime date);
}
