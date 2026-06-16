package com.andrei.clinica.model.veiculo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("UTILITARIO_CARGA")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UtilitarioCarga extends Veiculo {

    private double capacidadeKg;
    private boolean temPlataformaElevadora;

    public UtilitarioCarga(String placa, String modelo, double custoPorKm,
                            double capacidadeKg, boolean temPlataformaElevadora) {
        super(placa, modelo, custoPorKm);
        this.capacidadeKg = capacidadeKg;
        this.temPlataformaElevadora = temPlataformaElevadora;
    }

    @Override
    public double calcularCustoOperacao(int distanciaKm) {
        double base = getCustoPorKm() * distanciaKm;
        return base + (temPlataformaElevadora ? 100 : 0);
    }
}
