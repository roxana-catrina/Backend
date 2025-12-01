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
    public ResponseEntity<?> getByMonth(
            @PathVariable String userId,
            @RequestParam int year,
            @RequestParam int month) {
        log.info("GET /api/programari/user/{}/month?year={}&month={}", userId, year, month);
        log.debug("Received userId type: {}, value: '{}'", userId.getClass().getName(), userId);

        // Validate userId
        if (userId == null || userId.trim().isEmpty() || "null".equals(userId) || "undefined".equals(userId)) {
            log.error("Invalid userId received: '{}'", userId);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid userId: " + userId));
        }

        try {
            List<Programare> programari = programareService.getProgramariByDoctorAndMonth(userId, year, month);
            log.info("Returning {} programari for doctor with userId: {}", programari.size(), userId);
            return ResponseEntity.ok(programari);
        } catch (Exception e) {
            log.error("Error fetching programari for userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching programari: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<?> getUpcoming(@PathVariable String userId) {
        log.info("GET /api/programari/user/{}/upcoming", userId);
        log.debug("Received userId type: {}, value: '{}'", userId.getClass().getName(), userId);

        // Validate userId
        if (userId == null || userId.trim().isEmpty() || "null".equals(userId) || "undefined".equals(userId)) {
            log.error("Invalid userId received: '{}'", userId);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid userId: " + userId));
        }

        try {
            List<Programare> programari = programareService.getProgramariViitoareByDoctor(userId);
            log.info("Returning {} upcoming programari for userId: {}", programari.size(), userId);
            return ResponseEntity.ok(programari);
        } catch (Exception e) {
            log.error("Error fetching upcoming programari for userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching programari: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Programare> update(@PathVariable String id, @RequestBody ProgramareDTO dto) {
        Programare programare = programareService.updateProgramare(id, dto);
        return ResponseEntity.ok(programare);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        programareService.deleteProgramare(id);
        return ResponseEntity.noContent().build();
    }
}