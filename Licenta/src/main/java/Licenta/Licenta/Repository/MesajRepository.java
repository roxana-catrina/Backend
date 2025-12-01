package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.Mesaj;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesajRepository extends MongoRepository<Mesaj, String> {

    // Find all messages between two users - sorted by date
    @Query("{'$or': [{'expeditorId': ?0, 'destinatarId': ?1}, {'expeditorId': ?1, 'destinatarId': ?0}]}")
    List<Mesaj> findConversation(String user1Id, String user2Id);

    // Count unread messages for a user
    Long countByDestinatarIdAndCitit(String userId, Boolean citit);

    // Find unread messages from a specific sender
    List<Mesaj> findByDestinatarIdAndExpeditorIdAndCitit(String userId, String expeditorId, Boolean citit);

    // Find all messages for a user (as recipient)
    List<Mesaj> findByDestinatarIdOrderByDataTrimitereDesc(String userId);

    // Find all sent messages
    List<Mesaj> findByExpeditorIdOrderByDataTrimitereDesc(String userId);
}

