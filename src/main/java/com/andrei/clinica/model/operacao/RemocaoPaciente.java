package com.andrei.clinica.model.operacao;

import com.andrei.clinica.enums.NivelClinico;
import com.andrei.clinica.enums.TipoProfissional;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("REMOCAO_PACIENTE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RemocaoPaciente extends OperacaoClinica {

    private String nomePaciente;
    private int idadePaciente;

    @Enumerated(EnumType.STRING)
    private NivelClinico nivelClinico;

    private boolean precisaMonitoramento;
    private boolean precisaVentilacao;
    private boolean precisaUTI;

    // Construtor igual ao seu projeto original
    public RemocaoPaciente(String codigo, String origem, String destino,
                           int distanciaKm, String localDestino,
                           String nomePaciente, int idadePaciente,
                           NivelClinico nivelClinico,
                           boolean precisaMonitoramento,
                           boolean precisaVentilacao,
                           boolean precisaUTI) {
        super(codigo, origem, destino, distanciaKm, localDestino);
        this.nomePaciente = nomePaciente;
        this.idadePaciente = idadePaciente;
        this.nivelClinico = nivelClinico;
        this.precisaMonitoramento = precisaMonitoramento;
        this.precisaVentilacao = precisaVentilacao;
        this.precisaUTI = precisaUTI;
    }

    // Regra de validação: paciente crítico exige médico + enfermeiro
    @Override
    public boolean validar() {
        if (getVeiculo() == null) return false;
        if (getEquipe().isEmpty()) return false;
        if (!temProfissionalDoTipo(TipoProfissional.MOTORISTA)) return false;
        if (nivelClinico == NivelClinico.CRITICO || nivelClinico == NivelClinico.GRAVE) {
            if (!temProfissionalDoTipo(TipoProfissional.MEDICO)) return false;
            if (!temProfissionalDoTipo(TipoProfissional.ENFERMEIRO)) return false;
        }
        return true;
    }

    // Custo baseado no veículo + adicional por nível clínico
    @Override
    public double calcularCusto() {
        double base = getVeiculo() != null
            ? getVeiculo().calcularCustoOperacao(getDistanciaKm()) : 0;
        double adicional = switch (nivelClinico) {
            case CRITICO    -> 800;
            case GRAVE      -> 500;
            case OBSERVACAO -> 200;
            default         -> 0;
        };
        return base + adicional;
    }

    // Prioridade: quanto mais crítico, maior o número
    @Override
    public int calcularPrioridade() {
        return switch (nivelClinico) {
            case CRITICO    -> 100;
            case GRAVE      -> 75;
            case OBSERVACAO -> 50;
            default         -> 25;
        };
    }
}
