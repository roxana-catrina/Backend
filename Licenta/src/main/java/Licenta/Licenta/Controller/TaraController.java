package Licenta.Licenta.Controller;

import Licenta.Licenta.Model.Tara;
import Licenta.Licenta.Service.TaraService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaraController {

    @Autowired
    private final TaraService taraService;



    @GetMapping("/countries")
    public List<Tara> getCountries(){
        // Auto-initialize collection if empty

        return taraService.getCountries();
    }

    @PostMapping("/countries/initialize")
    public ResponseEntity<?> initializeCountries() {

        long count = taraService.getCountries().size();
        return ResponseEntity.ok(Map.of(
            "message", "Countries collection initialized",
            "count", count
        ));
    }

    @DeleteMapping("/countries/reset")
    public ResponseEntity<?> resetCountries() {

        long count = taraService.getCountries().size();
        return ResponseEntity.ok(Map.of(
            "message", "Countries collection reset",
            "count", count
        ));
    }
}
