package com.andrei.clinica.model.operacao;

import com.andrei.clinica.enums.StatusOperacao;
import com.andrei.clinica.interfaces.*;
import com.andrei.clinica.model.equipe.Profissional;
import com.andrei.clinica.model.veiculo.Veiculo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Mesma estratégia de herança dos veículos:
// uma tabela "operacoes" com coluna "tipo_operacao" para distinguir os tipos
@Entity
@Table(name = "operacoes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_operacao", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
public abstract class OperacaoClinica
        implements Validavel, Custeavel, Priorizavel, Rastreavel, Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código legível (OP001, OP002...) mantido do projeto original
    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String origem;

    @Column(nullable = false)
    private String destino;

    @Column(nullable = false)
    private int distanciaKm;

    @Column(nullable = false)
    private String localDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOperacao status;

    // Quando a operação foi criada — preenchido automaticamente
    private LocalDateTime dataCriacao;

    // Email do contratante que solicitou a operação
    // Permite filtrar "minhas operações" depois do login
    private String emailContratante;

    // Relacionamento com Veiculo:
    // @ManyToOne = muitas operações podem usar o mesmo veículo (em momentos diferentes)
    // @JoinColumn = cria a coluna "veiculo_id" na tabela operacoes
    @ManyToOne
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    // Relacionamento com Profissional:
    // @ManyToMany = uma operação tem vários profissionais,
    //               um profissional pode participar de várias operações
    // O JPA cria uma tabela intermediária "operacao_profissionais" automaticamente
    @ManyToMany
    @JoinTable(
        name = "operacao_profissionais",
        joinColumns = @JoinColumn(name = "operacao_id"),
        inverseJoinColumns = @JoinColumn(name = "profissional_id")
    )
    private List<Profissional> equipe = new ArrayList<>();

    // Construtor igual ao seu projeto original
    public OperacaoClinica(String codigo, String origem, String destino,
                           int distanciaKm, String localDestino) {
        this.codigo = codigo;
        this.origem = origem;
        this.destino = destino;
        this.distanciaKm = distanciaKm;
        this.localDestino = localDestino;
        this.status = StatusOperacao.SOLICITADA;
        this.dataCriacao = LocalDateTime.now();
    }

    // ── Métodos de fluxo de status (igual ao seu projeto) ────────────────────

    public void aprovar() {
        if (status != StatusOperacao.SOLICITADA) {
            System.out.println("Operação " + codigo + " não pode ser aprovada — status atual: " + status);
            return;
        }
        this.status = StatusOperacao.APROVADA;
        System.out.println("Operação " + codigo + " aprovada.");
    }

    public void iniciar() {
        if (status != StatusOperacao.APROVADA) {
            System.out.println("Operação " + codigo + " não pode ser iniciada — status atual: " + status);
            return;
        }
        if (!validar()) {
            System.out.println("Operação " + codigo + " inválida — verifique veículo e equipe.");
            return;
        }
        this.status = StatusOperacao.EM_EXECUCAO;
        System.out.println("Operação " + codigo + " iniciada.");
    }

    public void concluir() {
        if (status != StatusOperacao.EM_EXECUCAO) {
            System.out.println("Operação " + codigo + " não pode ser concluída — status atual: " + status);
            return;
        }
        this.status = StatusOperacao.CONCLUIDA;
        System.out.println("Operação " + codigo + " concluída.");
    }

    public void cancelar() {
        if (status == StatusOperacao.CONCLUIDA || status == StatusOperacao.CANCELADA) {
            System.out.println("Operação " + codigo + " não pode ser cancelada — status atual: " + status);
            return;
        }
        this.status = StatusOperacao.CANCELADA;
        System.out.println("Operação " + codigo + " cancelada.");
    }

    // ── Métodos auxiliares mantidos do projeto original ───────────────────────

    public void adicionarProfissional(Profissional p) {
        this.equipe.add(p);
    }

    protected boolean temProfissionalDoTipo(com.andrei.clinica.enums.TipoProfissional tipo) {
        return equipe.stream().anyMatch(p -> p.getTipo() == tipo);
    }

    // ── Implementações padrão das interfaces ──────────────────────────────────

    @Override
    public String obterDescricaoRastreamento() {
        return "[" + codigo + "] " + origem + " → " + destino
             + " | Status: " + status
             + " | Veículo: " + (veiculo != null ? veiculo : "não alocado")
             + " | Equipe: " + equipe.size() + " profissional(is)";
    }

    @Override
    public String gerarLogAuditoria() {
        return "LOG [" + codigo + "] criado em " + dataCriacao
             + " | Custo: R$" + String.format("%.2f", calcularCusto())
             + " | Prioridade: " + calcularPrioridade();
    }
}
