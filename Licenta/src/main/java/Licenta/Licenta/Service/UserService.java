package Licenta.Licenta.Service;


import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
//@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    PasswordEncoder passwordEncoder;

    public User postUser(User user) {
        user.setParola(passwordEncoder.encode(user.getParola()));
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id.toString());
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            return null;
        }
    }
    public User updateUser(User user){
        return userRepository.save(user);
    }


    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void deleteUser(Long id){
        userRepository.deleteById(id.toString());
    }

}
