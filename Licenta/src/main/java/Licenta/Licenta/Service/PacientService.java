package Licenta.Licenta.Service;

import Licenta.Licenta.Model.Pacient;
import Licenta.Licenta.Repository.PacientRepository;
import Licenta.Licenta.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PacientService {
    @Autowired
    private PacientRepository pacientRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Pacient> getAllPacientsByUserId(String userId) {
        return pacientRepository.findByUserId(userId);
    }

    public Optional<Pacient> findByUserIdAndId(String userId, String pacientId) {
        return pacientRepository.findByUserIdAndId(userId, pacientId);
    }

    public Pacient savePacient(Pacient pacient) {
        return pacientRepository.save(pacient);
    }

    public void deletePacient(Pacient pacient) {
        pacientRepository.delete(pacient);
    }

    public Optional<Pacient> findById(String id) {
        return pacientRepository.findById(id);
    }
}

