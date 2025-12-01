package Licenta.Licenta.Service;

import Licenta.Licenta.Model.Imagine;
import Licenta.Licenta.Repository.ImagineRepository;
import Licenta.Licenta.Repository.PacientRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ImagineService {
    @Autowired
    private ImagineRepository imagineRepository;

    @Autowired
    private PacientRepository pacientRepository;

    public List<Imagine> getAllImaginiByPacientId(String pacientId) {
        return imagineRepository.findByPacientId(pacientId);
    }

    public Optional<Imagine> findByPacientIdAndId(String pacientId, String imagineId) {
        return imagineRepository.findByPacientIdAndId(pacientId, imagineId);
    }

    public Imagine saveImagine(Imagine imagine) {
        return imagineRepository.save(imagine);
    }

    public void deleteImagine(Imagine imagine) {
        imagineRepository.delete(imagine);
    }

    public Optional<Imagine> findById(String id) {
        return imagineRepository.findById(id);
    }
}
