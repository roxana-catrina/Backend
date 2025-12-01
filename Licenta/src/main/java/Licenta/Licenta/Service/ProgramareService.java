package Licenta.Licenta.Service;

import Licenta.Licenta.Dto.ProgramareDTO;
import Licenta.Licenta.Model.Pacient;
import Licenta.Licenta.Model.Programare;
import Licenta.Licenta.Model.StatusProgramare;
import Licenta.Licenta.Repository.PacientRepository;
import Licenta.Licenta.Repository.ProgramareRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ProgramareService {

    private final ProgramareRepository programareRepository;
    private final PacientRepository pacientRepository;

    public Programare createProgramare(ProgramareDTO dto) {
        log.info("Creating programare for pacientId: {}", dto.getPacientId());

        // Fetch the pacient first
        Pacient pacient = pacientRepository.findById(dto.getPacientId())
                .orElseThrow(() -> {
                    log.error("Pacient not found with id: {}", dto.getPacientId());
                    return new RuntimeException("Pacient not found with id: " + dto.getPacientId());
                });

        log.info("Found pacient: {} {}", pacient.getNumePacient(), pacient.getPrenumePacient());

        // Create Programare
        Programare programare = new Programare(
                null,  // id
                pacient.getId(),  // pacient ID
                dto.getPacientNume(),
                dto.getPacientPrenume(),
                dto.getPacientCnp(),
                dto.getDataProgramare(),
                dto.getDurataMinute() != null ? dto.getDurataMinute() : 30,
                dto.getTipConsultatie(),
                StatusProgramare.PROGRAMAT,
                dto.getDetalii()
        );

        Programare saved = programareRepository.save(programare);
        log.info("Programare created successfully with id: {}", saved.getId());
        return saved;
    }

    public List<Programare> getProgramariByMonth(String pacientId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        return programareRepository.findByPacientIdAndDataProgramareBetween(pacientId, start, end);
    }

    public List<Programare> getProgramariViitoare(String pacientId) {
        log.info("Getting upcoming programari for pacientId: {}", pacientId);
        LocalDateTime now = LocalDateTime.now();
        List<Programare> programari = programareRepository
                .findByPacientIdAndDataProgramareAfterOrderByDataProgramareAsc(pacientId, now);
        log.info("Found {} upcoming programari", programari.size());
        return programari;
    }

    // Methods for doctor (User) - get all programari for their patients
    public List<Programare> getProgramariByDoctorAndMonth(String userId, int year, int month) {
        log.info("Getting programari for doctor userId: {} in {}/{}", userId, year, month);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // Get all pacienti for this user
        List<Pacient> pacienti = pacientRepository.findByUserId(userId);
        List<String> pacientIds = pacienti.stream().map(Pacient::getId).toList();

        // Get programari for all pacienti in the date range
        return programareRepository.findByDataProgramareBetweenOrderByDataProgramareAsc(start, end)
                .stream()
                .filter(p -> pacientIds.contains(p.getPacientId()))
                .toList();
    }

    public List<Programare> getProgramariViitoareByDoctor(String userId) {
        log.info("Getting upcoming programari for doctor userId: {}", userId);
        LocalDateTime now = LocalDateTime.now();

        // Get all pacienti for this user
        List<Pacient> pacienti = pacientRepository.findByUserId(userId);
        List<String> pacientIds = pacienti.stream().map(Pacient::getId).toList();

        // Get all future programari and filter by pacient IDs
        return programareRepository.findByDataProgramareBetweenOrderByDataProgramareAsc(now, now.plusYears(1))
                .stream()
                .filter(p -> pacientIds.contains(p.getPacientId()))
                .toList();
    }

    public Programare updateProgramare(String id, ProgramareDTO dto) {
        Programare programare = programareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programare not found with id: " + id));

        programare.setPacientNume(dto.getPacientNume());
        programare.setPacientPrenume(dto.getPacientPrenume());
        programare.setPacientCnp(dto.getPacientCnp());
        programare.setDataProgramare(dto.getDataProgramare());
        programare.setDurataMinute(dto.getDurataMinute() != null ? dto.getDurataMinute() : 30);
        programare.setTipConsultatie(dto.getTipConsultatie());
        programare.setDetalii(dto.getDetalii());

        return programareRepository.save(programare);
    }

    public void deleteProgramare(String id) {
        if (!programareRepository.existsById(id)) {
            throw new RuntimeException("Programare not found with id: " + id);
        }
        programareRepository.deleteById(id);
    }

    public Programare updateStatus(String id, StatusProgramare status) {
        Programare programare = programareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programare not found with id: " + id));

        programare.setStatus(status);
        return programareRepository.save(programare);
    }
}
