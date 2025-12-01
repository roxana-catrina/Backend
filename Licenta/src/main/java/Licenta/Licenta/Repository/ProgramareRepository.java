package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.Programare;
import Licenta.Licenta.Model.StatusProgramare;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProgramareRepository extends MongoRepository<Programare, String> {
    // Find by pacient id
    List<Programare> findByPacientIdAndDataProgramareBetween(
            String pacientId, LocalDateTime start, LocalDateTime end);

    List<Programare> findByPacientIdAndStatusIn(
            String pacientId, List<StatusProgramare> statuses);

    List<Programare> findByPacientIdAndDataProgramareAfterOrderByDataProgramareAsc(
            String pacientId, LocalDateTime now);

    // Find by date range
    List<Programare> findByDataProgramareBetweenOrderByDataProgramareAsc(
            LocalDateTime start, LocalDateTime end);

    // Find by status
    List<Programare> findByStatusOrderByDataProgramareAsc(StatusProgramare status);
}
