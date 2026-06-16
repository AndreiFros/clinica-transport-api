package com.andrei.clinica.service;

import com.andrei.clinica.enums.*;
import com.andrei.clinica.exception.BusinessException;
import com.andrei.clinica.exception.NotFoundException;
import com.andrei.clinica.model.equipe.Profissional;
import com.andrei.clinica.model.operacao.*;
import com.andrei.clinica.model.veiculo.*;
import com.andrei.clinica.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperacaoService {

    private final OperacaoRepository operacaoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final VeiculoRepository veiculoRepository;

    // ── Listar ────────────────────────────────────────────────────────────────

    public List<OperacaoClinica> listarTodas() {
        return operacaoRepository.findAll();
    }

    public List<OperacaoClinica> listarPorStatus(StatusOperacao status) {
        return operacaoRepository.findByStatus(status);
    }

    // Cada contratante vê só as suas operações
    public List<OperacaoClinica> listarDoContratante(String email) {
        return operacaoRepository.findByContratante(email);
    }

    public OperacaoClinica buscarPorId(Long id) {
        return operacaoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Operação não encontrada: id " + id));
    }

    // ── SOLICITAR — coração do sistema ───────────────────────────────────────
    //
    // @Transactional garante que tudo acontece junto:
    // se qualquer passo falhar, NADA é salvo no banco.
    // Ex: se encontrou profissionais mas não tem veículo disponível,
    // a operação NÃO é criada e os profissionais NÃO são marcados como ocupados.
    @Transactional
    public OperacaoClinica solicitar(OperacaoClinica operacao, String emailContratante) {

        // 1. Vincula ao contratante
        operacao.setEmailContratante(emailContratante);

        // 2. Gera código único (OP001, OP002...)
        operacao.setCodigo(gerarCodigo());

        // 3. Seleciona equipe automaticamente pela urgência da operação
        selecionarEquipe(operacao);

        // 4. Seleciona veículo compatível com o tipo de operação
        selecionarVeiculo(operacao);

        // 5. Salva no banco e retorna
        return operacaoRepository.save(operacao);
    }

    // ── Fluxo de status ───────────────────────────────────────────────────────

    @Transactional
    public OperacaoClinica aprovar(Long id) {
        OperacaoClinica op = buscarPorId(id);
        op.aprovar();
        return operacaoRepository.save(op);
    }

    @Transactional
    public OperacaoClinica iniciar(Long id) {
        OperacaoClinica op = buscarPorId(id);
        op.iniciar();
        // Marca todos os profissionais da equipe como ocupados
        op.getEquipe().forEach(p -> {
            p.setDisponivel(false);
            profissionalRepository.save(p);
        });
        // Marca o veículo como ocupado
        if (op.getVeiculo() != null) {
            op.getVeiculo().setDisponivel(false);
            veiculoRepository.save(op.getVeiculo());
        }
        return operacaoRepository.save(op);
    }

    @Transactional
    public OperacaoClinica concluir(Long id) {
        OperacaoClinica op = buscarPorId(id);
        op.concluir();
        // Libera profissionais e veículo para novas operações
        op.getEquipe().forEach(p -> {
            p.setDisponivel(true);
            profissionalRepository.save(p);
        });
        if (op.getVeiculo() != null) {
            op.getVeiculo().setDisponivel(true);
            veiculoRepository.save(op.getVeiculo());
        }
        return operacaoRepository.save(op);
    }

    @Transactional
    public OperacaoClinica cancelar(Long id) {
        OperacaoClinica op = buscarPorId(id);
        op.cancelar();
        // Se estava em execução, libera recursos
        if (op.getStatus() == StatusOperacao.EM_EXECUCAO) {
            op.getEquipe().forEach(p -> {
                p.setDisponivel(true);
                profissionalRepository.save(p);
            });
            if (op.getVeiculo() != null) {
                op.getVeiculo().setDisponivel(true);
                veiculoRepository.save(op.getVeiculo());
            }
        }
        return operacaoRepository.save(op);
    }

    // ── SELEÇÃO AUTOMÁTICA DE EQUIPE ─────────────────────────────────────────
    //
    // Aqui está a lógica central do sistema:
    // cada tipo de operação tem requisitos diferentes de equipe.
    // O método analisa o tipo e monta a equipe com profissionais disponíveis.
    private void selecionarEquipe(OperacaoClinica operacao) {

        // Motorista é obrigatório em TODAS as operações
        Profissional motorista = buscarProfissionalDisponivel(TipoProfissional.MOTORISTA,
                "Nenhum motorista disponível no momento.");
        operacao.adicionarProfissional(motorista);

        // Regras específicas por tipo de operação
        if (operacao instanceof RemocaoPaciente rp) {
            selecionarEquipeRemocao(rp, operacao);

        } else if (operacao instanceof TransporteMedicamentoControlado tm) {
            if (tm.isControlado()) {
                Profissional farmac = buscarProfissionalDisponivel(TipoProfissional.FARMACEUTICO,
                        "Nenhum farmacêutico disponível. Operação requer farmacêutico para medicamento controlado.");
                operacao.adicionarProfissional(farmac);
            }

        } else if (operacao instanceof TransporteAmostraBiologica ta) {
            if (ta.getNivelUrgencia() == NivelUrgencia.CRITICA && ta.isMaterialRiscoBiologico()) {
                Profissional enf = buscarProfissionalDisponivel(TipoProfissional.ENFERMEIRO,
                        "Nenhum enfermeiro disponível. Amostra crítica com risco biológico exige enfermeiro.");
                operacao.adicionarProfissional(enf);
            }

        } else if (operacao instanceof TransporteEquipamentoMedico te) {
            if (te.isPrecisaTecnico()) {
                Profissional tec = buscarProfissionalDisponivel(TipoProfissional.TECNICO_EQUIPAMENTO,
                        "Nenhum técnico de equipamento disponível.");
                operacao.adicionarProfissional(tec);
            }
        }
    }

    // Regras de equipe para remoção de paciente, separadas por nível clínico
    private void selecionarEquipeRemocao(RemocaoPaciente rp, OperacaoClinica operacao) {
        switch (rp.getNivelClinico()) {
            case CRITICO -> {
                // Crítico: médico + enfermeiro obrigatórios
                Profissional medico = buscarProfissionalDisponivel(TipoProfissional.MEDICO,
                        "Nenhum médico disponível. Remoção crítica exige médico.");
                Profissional enf = buscarProfissionalDisponivel(TipoProfissional.ENFERMEIRO,
                        "Nenhum enfermeiro disponível. Remoção crítica exige enfermeiro.");
                operacao.adicionarProfissional(medico);
                operacao.adicionarProfissional(enf);
            }
            case GRAVE -> {
                // Grave: enfermeiro obrigatório
                Profissional enf = buscarProfissionalDisponivel(TipoProfissional.ENFERMEIRO,
                        "Nenhum enfermeiro disponível. Remoção grave exige enfermeiro.");
                operacao.adicionarProfissional(enf);
            }
            case OBSERVACAO, ESTAVEL -> {
                // Estável/observação: só motorista já basta (adicionado antes)
            }
        }
    }

    // ── SELEÇÃO AUTOMÁTICA DE VEÍCULO ────────────────────────────────────────
    //
    // Cada tipo de operação precisa de um veículo compatível.
    // A lógica prioriza o veículo mais adequado para a situação.
    private void selecionarVeiculo(OperacaoClinica operacao) {

        Veiculo veiculo = null;

        if (operacao instanceof RemocaoPaciente rp) {
            // Paciente crítico → precisa de UTI
            // Paciente estável → ambulância simples já basta
            if (rp.getNivelClinico() == NivelClinico.CRITICO || rp.isPrecisaUTI()) {
                veiculo = buscarVeiculoDisponivel(AmbulanciaUTI.class,
                        "Nenhuma ambulância UTI disponível para remoção crítica.");
            } else {
                veiculo = buscarVeiculoDisponivel(AmbulanciaSimples.class,
                        "Nenhuma ambulância disponível para remoção.");
            }

        } else if (operacao instanceof TransporteMedicamentoControlado tm) {
            // Medicamento com refrigeração → van refrigerada
            // Sem refrigeração → utilitário de carga
            if (tm.isPrecisaRefrigeracao()) {
                veiculo = buscarVeiculoDisponivel(VanRefrigerada.class,
                        "Nenhuma van refrigerada disponível para medicamento que precisa de refrigeração.");
            } else {
                veiculo = buscarVeiculoDisponivel(UtilitarioCarga.class,
                        "Nenhum utilitário disponível para transporte de medicamento.");
            }

        } else if (operacao instanceof TransporteAmostraBiologica) {
            // Amostra biológica → ambulância simples (precisa de cuidado no transporte)
            veiculo = buscarVeiculoDisponivel(AmbulanciaSimples.class,
                    "Nenhuma ambulância disponível para transporte de amostra biológica.");

        } else if (operacao instanceof TransporteEquipamentoMedico) {
            // Equipamento → utilitário de carga
            veiculo = buscarVeiculoDisponivel(UtilitarioCarga.class,
                    "Nenhum utilitário disponível para transporte de equipamento.");
        }

        if (veiculo == null) {
            throw new BusinessException("Não foi possível alocar um veículo para esta operação.");
        }

        operacao.setVeiculo(veiculo);
    }

    // ── Métodos auxiliares ────────────────────────────────────────────────────

    // Busca o primeiro profissional disponível de um tipo
    // Se não encontrar, lança exceção com a mensagem informada
    private Profissional buscarProfissionalDisponivel(TipoProfissional tipo, String mensagemErro) {
        return profissionalRepository
                .findByTipoAndDisponivel(tipo, true)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(mensagemErro));
    }

    // Busca o primeiro veículo disponível de um tipo específico (AmbulanciaUTI, etc.)
    private <T extends Veiculo> T buscarVeiculoDisponivel(Class<T> tipo, String mensagemErro) {
        return veiculoRepository.findAll().stream()
                .filter(v -> tipo.isInstance(v) && v.isDisponivel())
                .map(tipo::cast)
                .findFirst()
                .orElseThrow(() -> new BusinessException(mensagemErro));
    }

    // Gera o próximo código sequencial (OP001, OP002...)
    private String gerarCodigo() {
        long total = operacaoRepository.count();
        return String.format("OP%03d", total + 1);
    }
}
