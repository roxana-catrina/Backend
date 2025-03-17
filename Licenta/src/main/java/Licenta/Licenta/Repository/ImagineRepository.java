package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.Imagine;
import Licenta.Licenta.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImagineRepository extends JpaRepository<Imagine, Integer> {
   List<Imagine> findByUserId(Long userId);

   Optional<Imagine> findByImagineAndUser(byte[] imagine, User user);
  // Optional <Imagine> findByUserAndId(User user,Long imagineId);
   Optional<Imagine> findByUserIdAndId(Long idUser, Long id);
   Optional<Imagine> findById(Long id);
}
