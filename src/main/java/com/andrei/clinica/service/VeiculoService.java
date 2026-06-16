package com.andrei.clinica.service;

import com.andrei.clinica.exception.BusinessException;
import com.andrei.clinica.exception.NotFoundException;
import com.andrei.clinica.model.veiculo.Veiculo;
import com.andrei.clinica.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    public List<Veiculo> listarTodos() {
        return veiculoRepository.findAll();
    }

    public List<Veiculo> listarDisponiveis() {
        return veiculoRepository.findByDisponivel(true);
    }

    public Veiculo buscarPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado: id " + id));
    }

    public Veiculo cadastrar(Veiculo veiculo) {
        if (veiculoRepository.existsByPlaca(veiculo.getPlaca())) {
            throw new BusinessException("Placa já cadastrada: " + veiculo.getPlaca());
        }
        return veiculoRepository.save(veiculo);
    }

    public void marcarDisponivel(Long id, boolean disponivel) {
        Veiculo v = buscarPorId(id);
        v.setDisponivel(disponivel);
        veiculoRepository.save(v);
    }
}
