package com.andrei.clinica.repository;

import com.andrei.clinica.enums.TipoProfissional;
import com.andrei.clinica.model.equipe.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// JpaRepository<Profissional, Long>:
//   - Profissional = qual entidade esse repositório gerencia
//   - Long         = tipo do @Id dessa entidade
//
// Só de declarar essa interface, o Spring gera automaticamente:
// save(), findById(), findAll(), deleteById() e mais 10+ métodos
@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    // O Spring lê o nome do método e gera o SQL sozinho:
    // "findBy" + "Tipo" → SELECT * FROM profissionais WHERE tipo = ?
    List<Profissional> findByTipo(TipoProfissional tipo);

    // "findBy" + "Disponivel" → SELECT * FROM profissionais WHERE disponivel = ?
    List<Profissional> findByDisponivel(boolean disponivel);

    // Combina os dois: profissionais de um tipo que estejam livres
    // Usado pela seleção automática de equipe
    List<Profissional> findByTipoAndDisponivel(TipoProfissional tipo, boolean disponivel);

    // Busca pelo registro (MOT-001, ENF-001...)
    Optional<Profissional> findByRegistro(String registro);

    // Verifica se já existe um profissional com esse registro
    boolean existsByRegistro(String registro);
}
