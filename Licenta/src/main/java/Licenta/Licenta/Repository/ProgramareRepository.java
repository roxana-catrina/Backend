package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.Programare;
import Licenta.Licenta.Model.StatusProgramare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProgramareRepository extends JpaRepository<Programare, Long> {
    // Find by pacient (Imagine) id
    List<Programare> findByPacientIdAndDataProgramareBetween(
            Long pacientId, LocalDateTime start, LocalDateTime end);

    List<Programare> findByPacientIdAndStatusIn(
            Long pacientId, List<StatusProgramare> statuses);

    List<Programare> findByPacientIdAndDataProgramareAfterOrderByDataProgramareAsc(
            Long pacientId, LocalDateTime now);

    // Find by doctor (User) id through pacient.user relationship
    List<Programare> findByPacientUserIdAndDataProgramareBetween(
            Long userId, LocalDateTime start, LocalDateTime end);

    List<Programare> findByPacientUserIdAndDataProgramareAfterOrderByDataProgramareAsc(
            Long userId, LocalDateTime now);
}
