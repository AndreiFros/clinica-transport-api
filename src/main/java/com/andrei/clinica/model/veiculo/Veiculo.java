package com.andrei.clinica.model.veiculo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

// @Inheritance(strategy = SINGLE_TABLE) = todas as subclasses
// ficam em UMA só tabela "veiculos" no banco.
// O JPA usa a coluna @DiscriminatorColumn para saber qual tipo é cada linha.
// Ex: linha com dtype="AMBULANCIA_UTI" → objeto AmbulanciaUTI
//
// É a estratégia mais simples para iniciantes — uma tabela, sem joins.
@Entity
@Table(name = "veiculos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_veiculo", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
public abstract class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos que existiam no seu projeto original
    @Column(nullable = false, unique = true)
    private String placa;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private double custoPorKm;

    // Campo novo: controla disponibilidade para alocação
    private boolean disponivel = true;

    public Veiculo(String placa, String modelo, double custoPorKm) {
        this.placa = placa;
        this.modelo = modelo;
        this.custoPorKm = custoPorKm;
        this.disponivel = true;
    }

    // Método abstrato: cada subclasse calcula seu custo específico
    // Mantém o polimorfismo do seu projeto original
    public abstract double calcularCustoOperacao(int distanciaKm);

    @Override
    public String toString() {
        return modelo + " [" + placa + "]";
    }
}
