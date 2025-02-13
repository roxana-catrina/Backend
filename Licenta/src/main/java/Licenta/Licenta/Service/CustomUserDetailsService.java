package Licenta.Licenta.Service;

import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

@Service
public class CustomUserDetailsService implements UserDetailsService{
    private UserRepository userRepository;
  /*  @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user= userRepository.findByEmail(username);
        if(user==null){
            throw new UsernameNotFoundException("User  or password not found");
        }
        return new CustomUserDetails(user.getEmail()
                ,user.getParola()
                 ,authorities()
                ,user.getPrenume()
        ,user.getNume()
        ,user.getSex()
        ,user.getData_nasterii()
        ,user.getNumar_telefon()
        ,user.getTara());
    }*/

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User or password not found"));

        return new CustomUserDetails(user.getEmail(),
                user.getParola(),
                authorities(),
                user.getPrenume(),
                user.getNume(),
                user.getSex(),
                user.getData_nasterii(),
                user.getNumar_telefon(),
                user.getTara());
    }


    public Collection<? extends GrantedAuthority> authorities() {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));

    }
}
