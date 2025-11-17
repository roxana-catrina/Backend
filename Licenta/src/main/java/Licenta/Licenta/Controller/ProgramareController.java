package Licenta.Licenta.Controller;

import Licenta.Licenta.Dto.ProgramareDTO;
import Licenta.Licenta.Model.Programare;
import Licenta.Licenta.Service.ProgramareService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/programari")
@AllArgsConstructor
@Slf4j
public class ProgramareController {

    private final ProgramareService programareService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProgramareDTO dto) {
        try {
            Programare programare = programareService.createProgramare(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(programare);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Inner class for error responses
    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @GetMapping("/user/{userId}/month")
    public ResponseEntity<List<Programare>> getByMonth(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        log.info("GET /api/programari/user/{}/month?year={}&month={}", userId, year, month);
        List<Programare> programari = programareService.getProgramariByDoctorAndMonth(userId, year, month);
        log.info("Returning {} programari for doctor", programari.size());
        return ResponseEntity.ok(programari);
    }

    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<Programare>> getUpcoming(@PathVariable Long userId) {
        log.info("GET /api/programari/user/{}/upcoming", userId);
        List<Programare> programari = programareService.getProgramariViitoareByDoctor(userId);
        log.info("Returning {} programari", programari.size());
        return ResponseEntity.ok(programari);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Programare> update(@PathVariable Long id, @RequestBody ProgramareDTO dto) {
        Programare programare = programareService.updateProgramare(id, dto);
        return ResponseEntity.ok(programare);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        programareService.deleteProgramare(id);
        return ResponseEntity.noContent().build();
    }
}