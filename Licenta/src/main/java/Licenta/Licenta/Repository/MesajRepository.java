package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.Mesaj;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesajRepository extends JpaRepository<Mesaj, Long> {

    // Găsește toate mesajele între doi utilizatori
    @Query("SELECT m FROM Mesaj m WHERE " +
            "(m.expeditor.id = :user1Id AND m.destinatar.id = :user2Id) OR " +
            "(m.expeditor.id = :user2Id AND m.destinatar.id = :user1Id) " +
            "ORDER BY m.dataTrimitere ASC")
    List<Mesaj> findConversation(@Param("user1Id") Long user1Id,
                                 @Param("user2Id") Long user2Id);

    // Numără mesajele necitite pentru un utilizator
    @Query("SELECT COUNT(m) FROM Mesaj m WHERE m.destinatar.id = :userId AND m.citit = false")
    Long countUnreadMessages(@Param("userId") Long userId);

    // Găsește mesajele necitite de la un expeditor specific
    @Query("SELECT m FROM Mesaj m WHERE m.destinatar.id = :userId " +
            "AND m.expeditor.id = :expeditorId AND m.citit = false")
    List<Mesaj> findUnreadMessagesFrom(@Param("userId") Long userId,
                                       @Param("expeditorId") Long expeditorId);

    // Ultimul mesaj din fiecare conversație
    @Query("SELECT m FROM Mesaj m WHERE m.id IN " +
            "(SELECT MAX(m2.id) FROM Mesaj m2 WHERE " +
            "m2.expeditor.id = :userId OR m2.destinatar.id = :userId " +
            "GROUP BY CASE " +
            "WHEN m2.expeditor.id = :userId THEN m2.destinatar.id " +
            "ELSE m2.expeditor.id END) " +
            "ORDER BY m.dataTrimitere DESC")
    List<Mesaj> findRecentConversations(@Param("userId") Long userId);
}
