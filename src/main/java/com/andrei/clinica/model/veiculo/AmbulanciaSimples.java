package com.andrei.clinica.model.veiculo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

// @DiscriminatorValue = o valor que vai na coluna tipo_veiculo para esse tipo
// Quando o JPA buscar uma linha com tipo_veiculo="AMBULANCIA_SIMPLES",
// vai criar um objeto AmbulanciaSimples
@Entity
@DiscriminatorValue("AMBULANCIA_SIMPLES")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AmbulanciaSimples extends Veiculo {

    private boolean temOxigenio;
    private boolean temDesfibrilador;

    // Construtor igual ao seu projeto original
    public AmbulanciaSimples(String placa, String modelo, double custoPorKm,
                              boolean temOxigenio, boolean temDesfibrilador) {
        super(placa, modelo, custoPorKm);
        this.temOxigenio = temOxigenio;
        this.temDesfibrilador = temDesfibrilador;
    }

    // Lógica de custo mantida do seu projeto
    @Override
    public double calcularCustoOperacao(int distanciaKm) {
        double base = getCustoPorKm() * distanciaKm;
        double adicional = (temOxigenio ? 50 : 0) + (temDesfibrilador ? 80 : 0);
        return base + adicional;
    }
}
