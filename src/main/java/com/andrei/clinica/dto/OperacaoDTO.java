package com.andrei.clinica.dto;

import com.andrei.clinica.enums.NivelClinico;
import com.andrei.clinica.enums.NivelUrgencia;
import com.andrei.clinica.enums.StatusOperacao;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// DTO = Data Transfer Object
//
// POR QUE USAR DTO E NÃO MANDAR A ENTIDADE DIRETAMENTE?
//
// 1. SEGURANÇA: a entidade OperacaoClinica tem campos internos
//    (emailContratante, dataCriacao, status, equipe alocada) que
//    o contratante NÃO deve poder setar manualmente. Se você
//    recebesse a entidade direto, um usuário mal-intencionado poderia
//    mandar { "status": "CONCLUIDA", "emailContratante": "outro@email.com" }
//    e corromper o sistema.
//
// 2. CLAREZA: o DTO mostra exatamente o que a API espera receber.
//    A entidade tem 15+ campos — o contratante só precisa preencher 5.
//
// 3. FLEXIBILIDADE: você pode mudar a entidade sem quebrar a API,
//    e vice-versa. São contratos independentes.
public class OperacaoDTO {

    // ── O que o contratante ENVIA ao solicitar ────────────────────────────────
    @Data
    public static class SolicitacaoRequest {

        // Tipo define qual subclasse criar: REMOCAO_PACIENTE, TRANSPORTE_MEDICAMENTO, etc.
        @NotNull(message = "Tipo de operação é obrigatório")
        private TipoOperacao tipo;

        @NotBlank(message = "Origem é obrigatória")
        private String origem;

        @NotBlank(message = "Destino é obrigatório")
        private String destino;

        @NotBlank(message = "Local de destino é obrigatório")
        private String localDestino;

        @Min(value = 1, message = "Distância deve ser maior que zero")
        private int distanciaKm;

        // ── Campos específicos de RemocaoPaciente ──
        private String nomePaciente;
        private Integer idadePaciente;
        private NivelClinico nivelClinico;
        private boolean precisaMonitoramento;
        private boolean precisaVentilacao;
        private boolean precisaUTI;

        // ── Campos específicos de TransporteMedicamento ──
        private String nomeMedicamento;
        private boolean precisaRefrigeracao;
        private Double temperaturaMinima;
        private Double temperaturaMaxima;
        private boolean controlado;

        // ── Campos específicos de TransporteAmostra ──
        private String tipoAmostra;
        private NivelUrgencia nivelUrgencia;
        private boolean materialRiscoBiologico;
        private Integer tempoMaximoMinutos;

        // ── Campos específicos de TransporteEquipamento ──
        private String nomeEquipamento;
        private Double pesoKg;
        private Double valorEquipamento;
        private boolean precisaInstalacao;
        private boolean precisaTecnico;
    }

    // Enum interno para o tipo de operação solicitada
    public enum TipoOperacao {
        REMOCAO_PACIENTE,
        TRANSPORTE_MEDICAMENTO,
        TRANSPORTE_AMOSTRA,
        TRANSPORTE_EQUIPAMENTO
    }

    // ── O que a API RETORNA ao listar operações ───────────────────────────────
    //
    // Versão resumida para listagens — não expõe todos os detalhes internos
    @Data
    public static class ResumoResponse {
        private Long id;
        private String codigo;
        private String tipo;
        private String origem;
        private String destino;
        private StatusOperacao status;
        private double custo;
        private int prioridade;
        private String dataCriacao;
        private int totalProfissionais;
        private String veiculo;
    }
}
