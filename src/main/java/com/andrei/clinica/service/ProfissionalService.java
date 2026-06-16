package com.andrei.clinica.service;

import com.andrei.clinica.enums.TipoProfissional;
import com.andrei.clinica.exception.BusinessException;
import com.andrei.clinica.exception.NotFoundException;
import com.andrei.clinica.model.equipe.Profissional;
import com.andrei.clinica.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;

    public List<Profissional> listarTodos() {
        return profissionalRepository.findAll();
    }

    public List<Profissional> listarDisponiveis() {
        return profissionalRepository.findByDisponivel(true);
    }

    public List<Profissional> listarPorTipo(TipoProfissional tipo) {
        return profissionalRepository.findByTipo(tipo);
    }

    public Profissional buscarPorId(Long id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profissional não encontrado: id " + id));
    }

    public Profissional cadastrar(Profissional profissional) {
        if (profissionalRepository.existsByRegistro(profissional.getRegistro())) {
            throw new BusinessException("Registro já cadastrado: " + profissional.getRegistro());
        }
        return profissionalRepository.save(profissional);
    }

    public void marcarDisponivel(Long id, boolean disponivel) {
        Profissional p = buscarPorId(id);
        p.setDisponivel(disponivel);
        profissionalRepository.save(p);
    }
}
