package com.andrei.clinica.repository;

import com.andrei.clinica.model.veiculo.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    // Busca veículos disponíveis
    List<Veiculo> findByDisponivel(boolean disponivel);

    // Busca pela placa
    Optional<Veiculo> findByPlaca(String placa);

    // Busca veículos de um tipo específico usando o discriminador JPA
    // @Query permite escrever JPQL (parecido com SQL mas usa nomes de classe, não de tabela)
    // TYPE() retorna o tipo real da entidade — AmbulanciaUTI, VanRefrigerada, etc.
    @Query("SELECT v FROM Veiculo v WHERE TYPE(v) = :tipo AND v.disponivel = true")
    <T extends Veiculo> List<T> findDisponiveisPorTipo(Class<T> tipo);

    boolean existsByPlaca(String placa);
}
