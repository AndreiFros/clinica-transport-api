package com.andrei.clinica.controller;

import com.andrei.clinica.enums.TipoProfissional;
import com.andrei.clinica.model.equipe.Profissional;
import com.andrei.clinica.service.ProfissionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ════════════════════════════════════════════════════════════════
// ProfissionalController — gerencia o cadastro e disponibilidade
// dos profissionais que compõem as equipes clínicas
//
// POR QUE ESSE CONTROLLER EXISTE SEPARADO:
// O contratante vai precisar ver quais profissionais estão
// disponíveis antes de solicitar uma operação. Também precisamos
// de endpoints para cadastrar novos profissionais no sistema
// (isso seria feito por um administrador).
//
// PADRÃO REST APLICADO AQUI:
// GET    /api/profissionais          → lista todos
// GET    /api/profissionais/{id}     → busca um específico
// GET    /api/profissionais/disponiveis → filtra disponíveis
// POST   /api/profissionais          → cadastra novo
// PATCH  /api/profissionais/{id}/disponibilidade → atualiza disponibilidade
//
// Repare: usamos PATCH (não PUT) para atualizar disponibilidade,
// porque PATCH significa "atualização parcial" — só um campo muda.
// PUT seria para substituir o objeto inteiro.
// ════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    // ── GET /api/profissionais ────────────────────────────────────────────────
    //
    // Aceita query param opcional: ?tipo=MEDICO
    // Se vier o parâmetro, filtra por tipo. Se não vier, retorna todos.
    //
    // @RequestParam(required = false) = o parâmetro é opcional.
    // Sem isso, o Spring exigiria ?tipo= em toda requisição.
    //
    // POR QUE UM SÓ ENDPOINT COM PARÂMETRO OPCIONAL É MELHOR
    // DO QUE DOIS ENDPOINTS (/todos e /por-tipo):
    // Menos endpoints = menos código = menos chance de bug.
    // O contratante aprende uma URL só e controla o filtro pelo parâmetro.
    @GetMapping
    public ResponseEntity<List<Profissional>> listar(
            @RequestParam(required = false) TipoProfissional tipo) {
        if (tipo != null) {
            return ResponseEntity.ok(profissionalService.listarPorTipo(tipo));
        }
        return ResponseEntity.ok(profissionalService.listarTodos());
    }

    // ── GET /api/profissionais/disponiveis ────────────────────────────────────
    //
    // Rota separada para disponíveis porque é um caso de uso muito comum:
    // antes de solicitar uma operação, o contratante quer saber
    // quem está livre agora.
    //
    // A ordem das anotações @GetMapping importa:
    // "/disponiveis" precisa vir ANTES de "/{id}" para o Spring
    // não confundir a palavra "disponiveis" com um ID numérico.
    @GetMapping("/disponiveis")
    public ResponseEntity<List<Profissional>> listarDisponiveis() {
        return ResponseEntity.ok(profissionalService.listarDisponiveis());
    }

    // ── GET /api/profissionais/{id} ───────────────────────────────────────────
    //
    // @PathVariable extrai o {id} da URL.
    // Ex: GET /api/profissionais/3 → id = 3L
    //
    // O service lança NotFoundException se não existir,
    // o GlobalExceptionHandler converte para 404 automaticamente.
    // O controller não precisa tratar isso — é eficiente porque
    // você escreve o tratamento de erro UMA vez, não em cada método.
    @GetMapping("/{id}")
    public ResponseEntity<Profissional> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(profissionalService.buscarPorId(id));
    }

    // ── POST /api/profissionais ───────────────────────────────────────────────
    //
    // Cadastra um novo profissional no sistema.
    // Body esperado:
    // { "nome": "Dr. Ana", "registro": "MED-010", "tipo": "MEDICO" }
    //
    // Retorna 201 Created com o objeto salvo (incluindo o id gerado pelo banco).
    // Por que retornar o objeto inteiro e não só uma mensagem "sucesso"?
    // Porque quem chamou a API precisa do id para fazer chamadas futuras
    // (ex: PATCH /api/profissionais/7/disponibilidade).
    @PostMapping
    public ResponseEntity<Profissional> cadastrar(@RequestBody Profissional profissional) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profissionalService.cadastrar(profissional));
    }

    // ── PATCH /api/profissionais/{id}/disponibilidade ─────────────────────────
    //
    // Marca um profissional como disponível ou ocupado.
    // Body esperado: { "disponivel": true } ou { "disponivel": false }
    //
    // Por que não PUT /api/profissionais/{id} mandando o objeto inteiro?
    // Porque PATCH para uma mudança parcial é mais seguro:
    // o front-end não precisa conhecer todos os campos do Profissional
    // para só mudar a disponibilidade. Menos dados trafegando = mais seguro.
    //
    // Retorna 204 No Content — por convenção REST, quando você atualiza
    // algo e não precisa retornar o objeto, use 204. Economiza banda.
    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<Void> atualizarDisponibilidade(
            @PathVariable Long id,
            @RequestParam boolean disponivel) {
        profissionalService.marcarDisponivel(id, disponivel);
        return ResponseEntity.noContent().build();
    }
}
