package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.Tara;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaraRepository extends MongoRepository<Tara, String> {
    Tara findByPrefix(String prefix);
    Tara findByCod(String cod);
}
