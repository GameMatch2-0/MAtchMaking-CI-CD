package com.example.matchmaker.matchmakerapi.entity.repository;

import com.example.matchmaker.matchmakerapi.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    // Busca por usuarios pelo id
    public Optional<Usuario> findById(String id);
    // Busca por usuarios pelo nome e que não foram apagados
    public Optional<Usuario> findByNomeIgnoreCaseAndDeletedFalse(String nome);
    // Busca por todos usuarios que não foram apagados
    public List<Usuario> findAllByDeletedFalse();
    // Busca por todos usuarios que foram apagados
    public List<Usuario> findAllByDeletedTrue();
    // Busca por usuarios pelo email e que não foram apagados
    public Optional<Usuario> findByEmailAndDeletedFalse(String email);

    public Optional<Usuario> findByPerfil(Long perfil);

}
