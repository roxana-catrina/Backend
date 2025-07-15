package Licenta.Licenta.Controller;


import Licenta.Licenta.Dto.AuthenticationRequest;
import Licenta.Licenta.Dto.AuthenticationResponse;
import Licenta.Licenta.Dto.UserDto;
import Licenta.Licenta.Model.LoginRequest;
import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.UserRepository;
import Licenta.Licenta.Service.CustomUserDetailsService;
import Licenta.Licenta.Utils.JwtUtil;
import Licenta.Licenta.Service.UserService;
//import ch.qos.logback.core.model.Model;
import jakarta.persistence.Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserDetailsService userDetailsService;
    
    private final UserService userService;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
   @Autowired
   private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;



    @PostMapping("/user")
    public User postUser(@RequestBody User user){
        try{
            user.setNume(user.getNume().toUpperCase());
           // if(user.getData_nasterii().isAfter( LocalDate.now()))
               // throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data de nastere incorecta" );
            return userService.postUser(user);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Eroare la salvarea utilizatorului. Detalii: " + e.getMessage(), e);
        }}
    @GetMapping("/users")
    public List<User> getAllUsers(){
        return  userService.getAllUsers();
    }
    @GetMapping("/user/{id}")
    public ResponseEntity<User>  getUserById(@PathVariable Long id){
        User user = new User();
        user=userService.getUserById(id);
        if(user!=null)
            return ResponseEntity.ok(user);
        else
            return ResponseEntity.notFound().build();


    }

    @PutMapping("/user/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        // Găsim utilizatorul în baza de date
        User existingUser = userService.getUserById(id);

        if (existingUser == null) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }

        // Verificăm dacă email-ul trimis este diferit și există deja în baza de date
        if (!existingUser.getEmail().equals(user.getEmail()) && userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409 Conflict
        }
        user.setNume(user.getNume().toUpperCase());
        // Actualizăm datele utilizatorului
        existingUser.setNume(user.getNume());
        existingUser.setPrenume(user.getPrenume());
        existingUser.setEmail(user.getEmail());
       // existingUser.setParola(passwordEncoder.encode(user.getParola()));
        existingUser.setData_nasterii(user.getData_nasterii());
        existingUser.setSex(user.getSex());
        existingUser.setTara(user.getTara());
        existingUser.setNumar_telefon(user.getNumar_telefon());

        // Salvăm utilizatorul actualizat
        User updatedUser = userService.updateUser(existingUser);
        return ResponseEntity.ok(updatedUser);
    }
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User existingUser= userService.getUserById(id);
        if(existingUser!=null){
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }

   @PostMapping("/authenticate")
   public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
       // 1. Verifică dacă utilizatorul există înainte de autentificare
       Optional<User> optionalUser = userRepository.findByEmail(authenticationRequest.getEmail());
       if (optionalUser.isEmpty()) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND)
                   .body(Collections.singletonMap("message", "Utilizatorul nu există"));
       }

       // 2. Dacă utilizatorul există, încercăm autentificarea
       try {
           authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                   authenticationRequest.getEmail(),
                   authenticationRequest.getParola()
           ));
       } catch (BadCredentialsException e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body(Collections.singletonMap("message", "Parola este incorectă"));
       }

       // 3. Generăm token-ul și returnăm utilizatorul
       User user = optionalUser.get();
       final String token = jwtUtil.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));

       AuthenticationResponse authenticationResponse = new AuthenticationResponse();
       authenticationResponse.setJwt(token);
       authenticationResponse.setId(user.getId());
       authenticationResponse.setNume(user.getNume());

       return ResponseEntity.ok(authenticationResponse);
   }



}
