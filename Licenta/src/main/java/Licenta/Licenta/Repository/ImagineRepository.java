package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.Imagine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagineRepository extends JpaRepository<Imagine, Integer> {
   List<Imagine> findByUserId(Long userId);
}
