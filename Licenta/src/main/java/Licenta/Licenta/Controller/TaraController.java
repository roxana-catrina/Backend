package Licenta.Licenta.Controller;

import Licenta.Licenta.Model.Tara;
import Licenta.Licenta.Service.TaraService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaraController {
    @Autowired
    private final TaraService taraService;


    @GetMapping("/countries")
    public List<Tara> getCountries(){
         return taraService.getCountries();
    }
}
