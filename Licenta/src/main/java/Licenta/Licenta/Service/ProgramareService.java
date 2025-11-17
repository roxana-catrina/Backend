package Licenta.Licenta.Service;

import Licenta.Licenta.Dto.ProgramareDTO;
import Licenta.Licenta.Model.Imagine;
import Licenta.Licenta.Model.Programare;
import Licenta.Licenta.Model.StatusProgramare;
import Licenta.Licenta.Repository.ImagineRepository;
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
    private final ImagineRepository imagineRepository;

    public Programare createProgramare(ProgramareDTO dto) {
        log.info("Creating programare for userId: {}", dto.getUserId());

        // Fetch the pacient (Imagine) first
        Imagine pacient = imagineRepository.findFirstByUserId(dto.getUserId())
                .orElseThrow(() -> {
                    log.error("Pacient not found with id: {}", dto.getUserId());
                    return new RuntimeException("Pacient not found with id: " + dto.getUserId());
                });

        log.info("Found pacient: {} {}", pacient.getNumePacient(), pacient.getPrenumePacient());

        // Create Programare using constructor
        Programare programare = new Programare(
                null,  // id
                pacient,  // pacient
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
    public List<Programare> getProgramariByMonth(Long pacientId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        return programareRepository.findByPacientIdAndDataProgramareBetween(pacientId, start, end);
    }

    public List<Programare> getProgramariViitoare(Long pacientId) {
        log.info("Getting upcoming programari for pacientId: {}", pacientId);
        LocalDateTime now = LocalDateTime.now();
        List<Programare> programari = programareRepository
                .findByPacientIdAndDataProgramareAfterOrderByDataProgramareAsc(pacientId, now);
        log.info("Found {} upcoming programari", programari.size());
        return programari;
    }

    // Methods for doctor (User) - get all programari for their patients
    public List<Programare> getProgramariByDoctorAndMonth(Long userId, int year, int month) {
        log.info("Getting programari for doctor userId: {} in {}/{}", userId, year, month);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Programare> programari = programareRepository
                .findByPacientUserIdAndDataProgramareBetween(userId, start, end);
        log.info("Found {} programari for doctor", programari.size());
        return programari;
    }

    public List<Programare> getProgramariViitoareByDoctor(Long userId) {
        log.info("Getting upcoming programari for doctor userId: {}", userId);
        LocalDateTime now = LocalDateTime.now();
        List<Programare> programari = programareRepository
                .findByPacientUserIdAndDataProgramareAfterOrderByDataProgramareAsc(userId, now);
        log.info("Found {} upcoming programari for doctor", programari.size());
        return programari;
    }

    public Programare updateProgramare(Long id, ProgramareDTO dto) {
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

    public void deleteProgramare(Long id) {
        if (!programareRepository.existsById(id)) {
            throw new RuntimeException("Programare not found with id: " + id);
        }
        programareRepository.deleteById(id);
    }

    public Programare updateStatus(Long id, StatusProgramare status) {
        Programare programare = programareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programare not found with id: " + id));

        programare.setStatus(status);
        return programareRepository.save(programare);
    }
}
