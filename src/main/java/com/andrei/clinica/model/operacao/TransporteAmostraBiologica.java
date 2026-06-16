package com.andrei.clinica.model.operacao;

import com.andrei.clinica.enums.NivelUrgencia;
import com.andrei.clinica.enums.TipoProfissional;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("TRANSPORTE_AMOSTRA")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TransporteAmostraBiologica extends OperacaoClinica {

    private String tipoAmostra;

    @Enumerated(EnumType.STRING)
    private NivelUrgencia nivelUrgencia;

    private boolean materialRiscoBiologico;
    private int tempoMaximoMinutos;

    public TransporteAmostraBiologica(String codigo, String origem, String destino,
                                      int distanciaKm, String localDestino,
                                      String tipoAmostra, NivelUrgencia nivelUrgencia,
                                      boolean materialRiscoBiologico,
                                      int tempoMaximoMinutos) {
        super(codigo, origem, destino, distanciaKm, localDestino);
        this.tipoAmostra = tipoAmostra;
        this.nivelUrgencia = nivelUrgencia;
        this.materialRiscoBiologico = materialRiscoBiologico;
        this.tempoMaximoMinutos = tempoMaximoMinutos;
    }

    // Amostra crítica com risco biológico exige enfermeiro
    @Override
    public boolean validar() {
        if (getVeiculo() == null) return false;
        if (!temProfissionalDoTipo(TipoProfissional.MOTORISTA)) return false;
        if (nivelUrgencia == NivelUrgencia.CRITICA && materialRiscoBiologico) {
            if (!temProfissionalDoTipo(TipoProfissional.ENFERMEIRO)) return false;
        }
        return true;
    }

    @Override
    public double calcularCusto() {
        double base = getVeiculo() != null
            ? getVeiculo().calcularCustoOperacao(getDistanciaKm()) : 0;
        double urgencia = switch (nivelUrgencia) {
            case CRITICA -> 400;
            case ALTA    -> 250;
            case MEDIA   -> 100;
            default      -> 0;
        };
        return base + urgencia + (materialRiscoBiologico ? 200 : 0);
    }

    @Override
    public int calcularPrioridade() {
        return switch (nivelUrgencia) {
            case CRITICA -> 90;
            case ALTA    -> 65;
            case MEDIA   -> 40;
            default      -> 15;
        };
    }
}
