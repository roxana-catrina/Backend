package Licenta.Licenta.Repository;


import Licenta.Licenta.Model.User;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    boolean existsByEmail(String email);
   User findByEmail(String email);
   // Optional<User> findByEmail(String email);

}
