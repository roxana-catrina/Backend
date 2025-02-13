package Licenta.Licenta.Controller;


import Licenta.Licenta.Model.User;
import Licenta.Licenta.Service.UserService;
//import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserDetailsService userDetailsService;;
    
    private final UserService userService;
   /* public UserController(UserService userService) {
        this.userService = userService;
    }*/
    
    @PostMapping("/user")
    public User postUser(@RequestBody User user){
        try{
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

        // Actualizăm datele utilizatorului
        existingUser.setNume(user.getNume());
        existingUser.setPrenume(user.getPrenume());
        existingUser.setEmail(user.getEmail());
        existingUser.setParola(user.getParola());
        existingUser.setData_nasterii(user.getData_nasterii());
        existingUser.setSex(user.getSex());
        existingUser.setTara(user.getTara());

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
    @GetMapping("/authenticate")
    public ResponseEntity<String> authenticate() {
        return ResponseEntity.ok("Autentificare reușită!");
    }
    /*public String authenticate(Model model, User user) {
        model.addAttribute("USER", user); // corectare: adăugăm atributul în model
        return "login"; // returnăm numele view-ului (login.html)
    }*/


}
