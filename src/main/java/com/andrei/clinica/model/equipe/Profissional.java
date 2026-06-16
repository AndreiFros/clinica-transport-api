package com.andrei.clinica.model.equipe;

import com.andrei.clinica.enums.TipoProfissional;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// @Entity diz ao JPA: "esta classe representa uma tabela no banco"
// O JPA vai criar a tabela "profissionais" automaticamente
@Entity
@Table(name = "profissionais")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profissional {

    // @Id = chave primária da tabela
    // @GeneratedValue = o banco gera o número automaticamente (1, 2, 3...)
    // No seu projeto original o ID era o "registro" (ex: "MOT-001")
    // Aqui separamos: id numérico para o banco + registro para o negócio
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(nullable = false) = campo obrigatório no banco
    @Column(nullable = false)
    private String nome;

    // O registro original (MOT-001, ENF-001...) fica único no banco
    @Column(nullable = false, unique = true)
    private String registro;

    // @Enumerated(EnumType.STRING) salva "MOTORISTA" em vez de "0"
    // Muito melhor para legibilidade do banco
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoProfissional tipo;

    // Campo novo: controla se o profissional está livre para uma operação
    // No projeto original não existia — agora é essencial para a seleção automática
    @Builder.Default
    private boolean disponivel = true;

    // Construtor compatível com o seu projeto original
    // Assim você pode criar igual antes: new Profissional("Carlos", "MOT-001", MOTORISTA)
    public Profissional(String nome, String registro, TipoProfissional tipo) {
        this.nome = nome;
        this.registro = registro;
        this.tipo = tipo;
        this.disponivel = true;
    }

    @Override
    public String toString() {
        return nome + " [" + registro + "] - " + tipo;
    }
}
