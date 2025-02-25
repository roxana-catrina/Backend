package Licenta.Licenta.Service;

import Licenta.Licenta.Model.Tara;
import Licenta.Licenta.Repository.TaraRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
//@NoArgsConstructor
@AllArgsConstructor
public class TaraService {
    @Autowired
    private final TaraRepository taraRepository ;
   /* public TaraService(TaraRepository taraRepository) {
        this.taraRepository = taraRepository;
    }*/
     public List<Tara> getCountries(){
         return taraRepository.findAll();
     }

}
