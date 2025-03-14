package Licenta.Licenta.Service;

import Licenta.Licenta.Model.Imagine;
import Licenta.Licenta.Repository.ImagineRepository;
import Licenta.Licenta.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ImagineService {
    @Autowired
    private ImagineRepository imagineRepository;

    @Autowired
    private UserRepository userRepository;

   public List<Imagine> getAllImaginIByIdUser(Long idUser){
        List<Imagine> imagini= imagineRepository.findByUserId(idUser);
        return imagini;

    }
    public boolean deteleImagineByImagineAndUser(byte[] imagine, Long idUser){
        Optional<Imagine> image = imagineRepository.findByImagineAndUser( imagine,userRepository.findById(idUser).get());
        if (image.isPresent()) {
            imagineRepository.delete(image.get());
            return true;
        }
        return false;
    }
    }
