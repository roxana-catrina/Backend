package Licenta.Licenta.Service;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private  String email;
    private String parola;
    private Collection<? extends GrantedAuthority> authorities;
    private String prenume;
    @Getter
    private String nume;
    @Getter
    private LocalDate data_nasterii;
    @Getter
    private String sex;
    @Getter
    private String numar_telefon;
    @Getter
    private String tara;

    public CustomUserDetails(String email, String parola, Collection<? extends GrantedAuthority> authorities, String prenume, String nume, String sex, LocalDate data_nasterii, String numar_telefon, String tara) {
        this.email = email;
        this.parola = parola;
        this.authorities = authorities;
        this.prenume = prenume;
        this.nume = nume;
        this.sex = sex;
        this.data_nasterii = data_nasterii;
        this.numar_telefon = numar_telefon;
        this.tara = tara;
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return parola;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
