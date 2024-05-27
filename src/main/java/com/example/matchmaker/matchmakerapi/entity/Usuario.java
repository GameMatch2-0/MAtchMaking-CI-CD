package com.example.matchmaker.matchmakerapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {
    @Id
    @Column(name = "idUsuario")
    private String id = String.valueOf(UUID.randomUUID());
    private String nome;
    private String sobrenome;
    private LocalDate dtNascimento;
    private String celular;
    private String email;
    private String senha;
    private String identidadeGenero;
    private LocalDateTime dtCadastro = LocalDateTime.now();
    private boolean deleted = false;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Perfil perfil;

}
