package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.Imagine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImagineRepository extends MongoRepository<Imagine, String> {
    List<Imagine> findByPacientId(String pacientId);
    Optional<Imagine> findByPacientIdAndId(String pacientId, String id);
}
