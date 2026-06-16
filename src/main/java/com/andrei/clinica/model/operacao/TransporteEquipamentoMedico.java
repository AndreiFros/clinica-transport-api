package com.andrei.clinica.model.operacao;

import com.andrei.clinica.enums.TipoProfissional;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("TRANSPORTE_EQUIPAMENTO")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TransporteEquipamentoMedico extends OperacaoClinica {

    private String nomeEquipamento;
    private double pesoKg;
    private double valorEquipamento;
    private boolean precisaInstalacao;
    private boolean precisaTecnico;

    public TransporteEquipamentoMedico(String codigo, String origem, String destino,
                                       int distanciaKm, String localDestino,
                                       String nomeEquipamento, double pesoKg,
                                       double valorEquipamento,
                                       boolean precisaInstalacao,
                                       boolean precisaTecnico) {
        super(codigo, origem, destino, distanciaKm, localDestino);
        this.nomeEquipamento = nomeEquipamento;
        this.pesoKg = pesoKg;
        this.valorEquipamento = valorEquipamento;
        this.precisaInstalacao = precisaInstalacao;
        this.precisaTecnico = precisaTecnico;
    }

    // Equipamento que precisa de técnico exige TECNICO_EQUIPAMENTO
    @Override
    public boolean validar() {
        if (getVeiculo() == null) return false;
        if (!temProfissionalDoTipo(TipoProfissional.MOTORISTA)) return false;
        if (precisaTecnico && !temProfissionalDoTipo(TipoProfissional.TECNICO_EQUIPAMENTO)) return false;
        return true;
    }

    @Override
    public double calcularCusto() {
        double base = getVeiculo() != null
            ? getVeiculo().calcularCustoOperacao(getDistanciaKm()) : 0;
        double seguro = valorEquipamento * 0.01; // 1% do valor como taxa de seguro
        return base + seguro + (precisaInstalacao ? 250 : 0);
    }

    @Override
    public int calcularPrioridade() {
        if (valorEquipamento > 50000) return 60;
        if (valorEquipamento > 10000) return 40;
        return 20;
    }
}
