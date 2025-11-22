package Licenta.Licenta.Repository;




import Licenta.Licenta.Model.Notificare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificareRepository extends JpaRepository<Notificare, Long> {

    // Găsește toate notificările pentru un utilizator
    @Query("SELECT n FROM Notificare n WHERE n.user.id = :userId ORDER BY n.dataCreare DESC")
    List<Notificare> findByUserId(@Param("userId") Long userId);

    // Găsește notificările necitite pentru un utilizator
    @Query("SELECT n FROM Notificare n WHERE n.user.id = :userId AND n.citit = false ORDER BY n.dataCreare DESC")
    List<Notificare> findUnreadByUserId(@Param("userId") Long userId);

    // Numără notificările necitite
    @Query("SELECT COUNT(n) FROM Notificare n WHERE n.user.id = :userId AND n.citit = false")
    Long countUnreadByUserId(@Param("userId") Long userId);

    // Găsește notificări după tip
    @Query("SELECT n FROM Notificare n WHERE n.user.id = :userId AND n.tip = :tip ORDER BY n.dataCreare DESC")
    List<Notificare> findByUserIdAndTip(@Param("userId") Long userId, @Param("tip") String tip);

    // Șterge notificări vechi (mai vechi de X zile)
    @Query("DELETE FROM Notificare n WHERE n.dataCreare < :dataLimita")
    void deleteOldNotifications(@Param("dataLimita") LocalDateTime dataLimita);

    // Găsește notificări pentru un mesaj specific
    @Query("SELECT n FROM Notificare n WHERE n.mesaj.id = :mesajId")
    List<Notificare> findByMesajId(@Param("mesajId") Long mesajId);

    // Marchează toate notificările ca citite pentru un user
    @Query("UPDATE Notificare n SET n.citit = true WHERE n.user.id = :userId AND n.citit = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}
