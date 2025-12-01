package Licenta.Licenta.Service;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    @Getter
    @Setter
    private  String email;
    @Getter
    @Setter
    private String parola;
    private Collection<? extends GrantedAuthority> authorities;
    @Getter
    @Setter
    private String prenume;
    @Getter
    @Setter
    private String nume;
    @Getter
    @Setter
    private LocalDate data_nasterii;
    @Getter
    @Setter
    private String sex;
    @Getter
    @Setter
    private String numar_telefon;
    @Getter
    @Setter
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

    @Override
    public String toString() {
        return "{" +
                "email='" + email + '\'' +
                ", parola='" + parola + '\'' +
                ", prenume='" + prenume + '\'' +
                ", nume='" + nume + '\'' +
                ", data_nasterii=" + data_nasterii +
                ", sex='" + sex + '\'' +
                ", numar_telefon='" + numar_telefon + '\'' +
                ", tara='" + tara + '\'' +
                '}';
    }
}
