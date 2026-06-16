package com.andrei.clinica.repository;

import com.andrei.clinica.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositório dos contratantes (quem faz login e solicita operações)
// Igual ao que você fez na task-api — reutiliza o mesmo padrão
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
