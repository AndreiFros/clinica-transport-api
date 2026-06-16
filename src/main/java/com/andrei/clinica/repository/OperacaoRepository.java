package com.andrei.clinica.repository;

import com.andrei.clinica.enums.StatusOperacao;
import com.andrei.clinica.model.operacao.OperacaoClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperacaoRepository extends JpaRepository<OperacaoClinica, Long> {

    // Lista todas as operações de um status específico
    // Ex: findByStatus(StatusOperacao.SOLICITADA) → todas aguardando aprovação
    List<OperacaoClinica> findByStatus(StatusOperacao status);

    // Busca pelo código legível (OP001, OP002...)
    Optional<OperacaoClinica> findByCodigo(String codigo);

    // Verifica se o código já existe (para não duplicar)
    boolean existsByCodigo(String codigo);

    // Busca operações ordenadas por prioridade decrescente
    // Usado para listar as mais urgentes primeiro
    // "OrderBy" no nome do método gera o ORDER BY automaticamente
    List<OperacaoClinica> findByStatusOrderByStatusAsc(StatusOperacao status);

    // Busca operações de um contratante específico pelo email
    // Quando o contratante fizer login e listar "minhas operações"
    @Query("SELECT o FROM OperacaoClinica o WHERE o.emailContratante = :email ORDER BY o.dataCriacao DESC")
    List<OperacaoClinica> findByContratante(@Param("email") String email);

    // Conta quantas operações estão em execução agora
    long countByStatus(StatusOperacao status);
}
