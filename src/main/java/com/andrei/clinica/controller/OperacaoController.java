package com.andrei.clinica.controller;

import com.andrei.clinica.dto.OperacaoDTO;
import com.andrei.clinica.enums.*;
import com.andrei.clinica.exception.BusinessException;
import com.andrei.clinica.model.operacao.*;
import com.andrei.clinica.service.OperacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// ════════════════════════════════════════════════════════════════
// OperacaoController — o controller mais importante do sistema
//
// É aqui que o contratante interage com o sistema clínico:
// solicita operações, acompanha o status, aprova e conclui.
//
// ENDPOINTS:
// POST   /api/operacoes              → solicitar nova operação
// GET    /api/operacoes              → listar minhas operações
// GET    /api/operacoes/{id}         → ver detalhes de uma operação
// PUT    /api/operacoes/{id}/aprovar → aprovar
// PUT    /api/operacoes/{id}/iniciar → iniciar
// PUT    /api/operacoes/{id}/concluir→ concluir
// DELETE /api/operacoes/{id}         → cancelar
// ════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/operacoes")
@RequiredArgsConstructor
public class OperacaoController {

    private final OperacaoService operacaoService;

    // ── POST /api/operacoes ───────────────────────────────────────────────────
    //
    // É aqui que acontece a mágica: o contratante manda o tipo e urgência,
    // o sistema seleciona equipe e veículo automaticamente.
    //
    // @AuthenticationPrincipal UserDetails userDetails:
    //   → O Spring extrai automaticamente quem está logado a partir do token JWT.
    //   → Você não precisa parsear o token manualmente — o Spring já fez isso
    //     no JwtAuthFilter antes de chegar aqui.
    //   → userDetails.getUsername() retorna o email do contratante logado.
    //
    // POR QUE ISSO É SEGURO:
    //   O contratante não consegue solicitar uma operação no nome de outro.
    //   O email vem do token (que foi assinado pelo servidor), não do body
    //   da requisição. Mesmo que o usuário mande { "emailContratante": "outro" }
    //   no body, o sistema ignora e usa o email do token.
    @PostMapping
    public ResponseEntity<OperacaoClinica> solicitar(
            @Valid @RequestBody OperacaoDTO.SolicitacaoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Converte o DTO em uma entidade do domínio clínico
        // Esse método decide qual subclasse criar baseado no "tipo" do DTO
        OperacaoClinica operacao = converterDTOParaEntidade(request);

        // Passa para o service com o email de quem está logado
        String emailContratante = userDetails.getUsername();
        OperacaoClinica criada = operacaoService.solicitar(operacao, emailContratante);

        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    // ── GET /api/operacoes ────────────────────────────────────────────────────
    //
    // Retorna só as operações do contratante logado.
    // Mesmo endpoint para todos — cada um vê só o que é seu.
    //
    // Por que retornar ResumoResponse e não a entidade inteira?
    // Listagens com muitos objetos ficam pesadas se cada um carregar
    // a equipe completa, o veículo, todos os campos internos.
    // O resumo traz só o essencial para o contratante decidir
    // em qual operação quer ver o detalhe.
    // Isso se chama "paginação por conteúdo" — reduz dados trafegados.
    @GetMapping
    public ResponseEntity<List<OperacaoDTO.ResumoResponse>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<OperacaoClinica> operacoes = operacaoService
                .listarDoContratante(userDetails.getUsername());

        List<OperacaoDTO.ResumoResponse> resumos = operacoes.stream()
                .map(this::converterParaResumo)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resumos);
    }

    // ── GET /api/operacoes/{id} ───────────────────────────────────────────────
    //
    // Retorna a entidade completa de uma operação específica.
    // O contratante usa esse endpoint quando quer ver todos os detalhes:
    // equipe alocada, veículo, custo, rastreamento, etc.
    @GetMapping("/{id}")
    public ResponseEntity<OperacaoClinica> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(operacaoService.buscarPorId(id));
    }

    // ── PUT /api/operacoes/{id}/aprovar ───────────────────────────────────────
    //
    // Por que PUT em vez de POST ou PATCH?
    // PUT em uma sub-rota de ação (/aprovar, /iniciar, /concluir) é um
    // padrão REST para "acionar uma transição de estado".
    // Não cria nada (POST) nem atualiza parcialmente (PATCH) —
    // executa uma ação específica que tem regras de negócio.
    //
    // A regra de negócio (só pode aprovar se estiver SOLICITADA) fica
    // no service/entidade, não aqui. O controller só aciona.
    @PutMapping("/{id}/aprovar")
    public ResponseEntity<OperacaoClinica> aprovar(@PathVariable Long id) {
        return ResponseEntity.ok(operacaoService.aprovar(id));
    }

    // ── PUT /api/operacoes/{id}/iniciar ───────────────────────────────────────
    //
    // Quando iniciada, o service automaticamente:
    // 1. Marca profissionais da equipe como indisponíveis
    // 2. Marca o veículo como indisponível
    // Tudo isso acontece no service com @Transactional —
    // o controller não precisa saber desses detalhes.
    @PutMapping("/{id}/iniciar")
    public ResponseEntity<OperacaoClinica> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(operacaoService.iniciar(id));
    }

    // ── PUT /api/operacoes/{id}/concluir ──────────────────────────────────────
    //
    // Quando concluída, o service libera profissionais e veículo
    // para novas operações automaticamente.
    @PutMapping("/{id}/concluir")
    public ResponseEntity<OperacaoClinica> concluir(@PathVariable Long id) {
        return ResponseEntity.ok(operacaoService.concluir(id));
    }

    // ── DELETE /api/operacoes/{id} ────────────────────────────────────────────
    //
    // Por convenção REST, DELETE representa cancelamento ou remoção.
    // Aqui não apagamos do banco — só mudamos o status para CANCELADA.
    // Isso é importante: manter o histórico de operações canceladas
    // para auditoria. O nome do método HTTP é DELETE, mas a ação
    // é um soft delete (cancelamento lógico).
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        operacaoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS — conversão entre DTO e entidade
    //
    // POR QUE CONVERTER AQUI E NÃO NO SERVICE?
    // O service trabalha com entidades do domínio — ele não deveria
    // conhecer DTOs. O controller faz a "tradução" entre o mundo HTTP
    // (DTOs) e o mundo do domínio (entidades). Cada camada tem seu papel.
    // ════════════════════════════════════════════════════════════════════════

    private OperacaoClinica converterDTOParaEntidade(OperacaoDTO.SolicitacaoRequest r) {
        // switch expression do Java 14+ — mais limpo que if/else encadeado
        // Cada tipo cria a subclasse correta com os campos específicos
        return switch (r.getTipo()) {

            case REMOCAO_PACIENTE -> {
                validarCampoObrigatorio(r.getNomePaciente(), "nomePaciente");
                validarCampoObrigatorio(r.getNivelClinico(), "nivelClinico");
                yield new RemocaoPaciente(
                    null, r.getOrigem(), r.getDestino(),
                    r.getDistanciaKm(), r.getLocalDestino(),
                    r.getNomePaciente(),
                    r.getIdadePaciente() != null ? r.getIdadePaciente() : 0,
                    r.getNivelClinico(),
                    r.isPrecisaMonitoramento(),
                    r.isPrecisaVentilacao(),
                    r.isPrecisaUTI()
                );
            }

            case TRANSPORTE_MEDICAMENTO -> {
                validarCampoObrigatorio(r.getNomeMedicamento(), "nomeMedicamento");
                yield new TransporteMedicamentoControlado(
                    null, r.getOrigem(), r.getDestino(),
                    r.getDistanciaKm(), r.getLocalDestino(),
                    r.getNomeMedicamento(),
                    r.isPrecisaRefrigeracao(),
                    r.getTemperaturaMinima() != null ? r.getTemperaturaMinima() : 0,
                    r.getTemperaturaMaxima() != null ? r.getTemperaturaMaxima() : 0,
                    r.isControlado()
                );
            }

            case TRANSPORTE_AMOSTRA -> {
                validarCampoObrigatorio(r.getTipoAmostra(), "tipoAmostra");
                validarCampoObrigatorio(r.getNivelUrgencia(), "nivelUrgencia");
                yield new TransporteAmostraBiologica(
                    null, r.getOrigem(), r.getDestino(),
                    r.getDistanciaKm(), r.getLocalDestino(),
                    r.getTipoAmostra(),
                    r.getNivelUrgencia(),
                    r.isMaterialRiscoBiologico(),
                    r.getTempoMaximoMinutos() != null ? r.getTempoMaximoMinutos() : 60
                );
            }

            case TRANSPORTE_EQUIPAMENTO -> {
                validarCampoObrigatorio(r.getNomeEquipamento(), "nomeEquipamento");
                yield new TransporteEquipamentoMedico(
                    null, r.getOrigem(), r.getDestino(),
                    r.getDistanciaKm(), r.getLocalDestino(),
                    r.getNomeEquipamento(),
                    r.getPesoKg() != null ? r.getPesoKg() : 0,
                    r.getValorEquipamento() != null ? r.getValorEquipamento() : 0,
                    r.isPrecisaInstalacao(),
                    r.isPrecisaTecnico()
                );
            }
        };
    }

    // Converte a entidade completa para um resumo leve para listagens
    private OperacaoDTO.ResumoResponse converterParaResumo(OperacaoClinica op) {
        OperacaoDTO.ResumoResponse r = new OperacaoDTO.ResumoResponse();
        r.setId(op.getId());
        r.setCodigo(op.getCodigo());
        r.setTipo(op.getClass().getSimpleName()); // "RemocaoPaciente", etc.
        r.setOrigem(op.getOrigem());
        r.setDestino(op.getDestino());
        r.setStatus(op.getStatus());
        r.setCusto(op.calcularCusto());
        r.setPrioridade(op.calcularPrioridade());
        r.setDataCriacao(op.getDataCriacao() != null ? op.getDataCriacao().toString() : "");
        r.setTotalProfissionais(op.getEquipe().size());
        r.setVeiculo(op.getVeiculo() != null ? op.getVeiculo().toString() : "não alocado");
        return r;
    }

    // Validação simples para campos obrigatórios por tipo de operação
    private void validarCampoObrigatorio(Object campo, String nomeCampo) {
        if (campo == null || (campo instanceof String s && s.isBlank())) {
            throw new BusinessException("Campo obrigatório para este tipo de operação: " + nomeCampo);
        }
    }
}
