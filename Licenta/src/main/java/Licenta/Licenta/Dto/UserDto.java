package Licenta.Licenta.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

public class UserDto {

    private  String email;


    private String parola;


    private String prenume;


    private String nume;


    private LocalDate data_nasterii;


    private String sex;


    private String numar_telefon;


    private String tara;

    public UserDto(String email, String parola, String nume, String prenume, LocalDate data_nasterii, String sex, String numar_telefon, String tara) {
        this.email = email;
        this.parola = parola;
        this.nume = nume;
        this.prenume = prenume;
        this.data_nasterii = data_nasterii;
        this.sex = sex;
        this.numar_telefon = numar_telefon;
        this.tara = tara;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getParola() {
        return parola;
    }

    public void setParola(String parola) {
        this.parola = parola;
    }

    public String getPrenume() {
        return prenume;
    }

    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public LocalDate getData_nasterii() {
        return data_nasterii;
    }

    public void setData_nasterii(LocalDate data_nasterii) {
        this.data_nasterii = data_nasterii;
    }

    public String getNumar_telefon() {
        return numar_telefon;
    }

    public void setNumar_telefon(String numar_telefon) {
        this.numar_telefon = numar_telefon;
    }

    public String getTara() {
        return tara;
    }

    public void setTara(String tara) {
        this.tara = tara;
    }

    public UserDto() {
    }

    @Override
    public String toString() {
        return '{' +
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
