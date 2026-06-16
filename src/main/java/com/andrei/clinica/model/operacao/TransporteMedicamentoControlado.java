package com.andrei.clinica.model.operacao;

import com.andrei.clinica.enums.TipoProfissional;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("TRANSPORTE_MEDICAMENTO")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TransporteMedicamentoControlado extends OperacaoClinica {

    private String nomeMedicamento;
    private boolean precisaRefrigeracao;
    private double temperaturaMinima;
    private double temperaturaMaxima;
    private boolean controlado;

    public TransporteMedicamentoControlado(String codigo, String origem, String destino,
                                           int distanciaKm, String localDestino,
                                           String nomeMedicamento,
                                           boolean precisaRefrigeracao,
                                           double temperaturaMinima,
                                           double temperaturaMaxima,
                                           boolean controlado) {
        super(codigo, origem, destino, distanciaKm, localDestino);
        this.nomeMedicamento = nomeMedicamento;
        this.precisaRefrigeracao = precisaRefrigeracao;
        this.temperaturaMinima = temperaturaMinima;
        this.temperaturaMaxima = temperaturaMaxima;
        this.controlado = controlado;
    }

    // Medicamento controlado exige farmacêutico
    @Override
    public boolean validar() {
        if (getVeiculo() == null) return false;
        if (!temProfissionalDoTipo(TipoProfissional.MOTORISTA)) return false;
        if (controlado && !temProfissionalDoTipo(TipoProfissional.FARMACEUTICO)) return false;
        return true;
    }

    @Override
    public double calcularCusto() {
        double base = getVeiculo() != null
            ? getVeiculo().calcularCustoOperacao(getDistanciaKm()) : 0;
        return base + (controlado ? 300 : 0) + (precisaRefrigeracao ? 150 : 0);
    }

    @Override
    public int calcularPrioridade() {
        return controlado ? 70 : 30;
    }
}
