package com.andrei.clinica.model.veiculo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("VAN_REFRIGERADA")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class VanRefrigerada extends Veiculo {

    private double temperaturaMinima;

    public VanRefrigerada(String placa, String modelo, double custoPorKm,
                           double temperaturaMinima) {
        super(placa, modelo, custoPorKm);
        this.temperaturaMinima = temperaturaMinima;
    }

    @Override
    public double calcularCustoOperacao(int distanciaKm) {
        double base = getCustoPorKm() * distanciaKm;
        return base + 150; // taxa de refrigeração fixa
    }
}
