package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.Pacient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacientRepository extends MongoRepository<Pacient, String> {
    List<Pacient> findByUserId(String userId);
    Optional<Pacient> findByUserIdAndId(String userId, String id);
}

