package com.andrei.clinica;

import com.andrei.clinica.enums.TipoProfissional;
import com.andrei.clinica.model.equipe.Profissional;
import com.andrei.clinica.model.veiculo.*;
import com.andrei.clinica.repository.ProfissionalRepository;
import com.andrei.clinica.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// CommandLineRunner = executa esse código uma vez ao iniciar a aplicação
// Popula o banco com profissionais e veículos de exemplo
// para você poder testar os endpoints imediatamente
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProfissionalRepository profissionalRepository;
    private final VeiculoRepository veiculoRepository;

    @Override
    public void run(String... args) {

        // ── Profissionais (os mesmos do seu projeto original) ─────────────────
        profissionalRepository.saveAll(List.of(
            new Profissional("Carlos Silva",   "MOT-001", TipoProfissional.MOTORISTA),
            new Profissional("Ana Souza",      "MOT-002", TipoProfissional.MOTORISTA),
            new Profissional("Lucas Melo",     "ENF-001", TipoProfissional.ENFERMEIRO),
            new Profissional("Dra. Paula",     "MED-001", TipoProfissional.MEDICO),
            new Profissional("João Farma",     "FAR-001", TipoProfissional.FARMACEUTICO),
            new Profissional("Pedro Técnico",  "TEC-001", TipoProfissional.TECNICO_EQUIPAMENTO)
        ));

        // ── Veículos (os mesmos do seu projeto original) ──────────────────────
        veiculoRepository.saveAll(List.of(
            new AmbulanciaSimples("ABC-1234", "Sprinter Simples", 500, true, true),
            new AmbulanciaUTI    ("DEF-5678", "Sprinter UTI",     700, true, true, true),
            new VanRefrigerada   ("GHI-9012", "Van Frio",         400, 4.0),
            new UtilitarioCarga  ("JKL-3456", "Fiorino Carga",    600, 2000, true)
        ));

        System.out.println("✓ Banco populado: 6 profissionais e 4 veículos prontos.");
    }
}
