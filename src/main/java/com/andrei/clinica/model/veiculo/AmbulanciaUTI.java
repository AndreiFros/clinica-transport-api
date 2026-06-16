package com.andrei.clinica.model.veiculo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("AMBULANCIA_UTI")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AmbulanciaUTI extends Veiculo {

    private boolean temOxigenio;
    private boolean temDesfibrilador;
    private boolean temVentiladorMecanico;

    public AmbulanciaUTI(String placa, String modelo, double custoPorKm,
                          boolean temOxigenio, boolean temDesfibrilador,
                          boolean temVentiladorMecanico) {
        super(placa, modelo, custoPorKm);
        this.temOxigenio = temOxigenio;
        this.temDesfibrilador = temDesfibrilador;
        this.temVentiladorMecanico = temVentiladorMecanico;
    }

    @Override
    public double calcularCustoOperacao(int distanciaKm) {
        double base = getCustoPorKm() * distanciaKm;
        double adicional = (temOxigenio ? 50 : 0)
                         + (temDesfibrilador ? 80 : 0)
                         + (temVentiladorMecanico ? 200 : 0);
        return base + adicional + 300; // taxa UTI fixa
    }
}
