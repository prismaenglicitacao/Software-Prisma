package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.repository.EngenheiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class EngenheiroService {

    private final EngenheiroRepository engenheiroRepository;

    @Transactional(readOnly = true)
    public List<Engenheiro> listarTodos() {
        return engenheiroRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<Engenheiro> listarParaSelecao() {
        return engenheiroRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }

    @Transactional(readOnly = true)
    public Engenheiro buscarPorId(Long id) {
        return engenheiroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Engenheiro nao encontrado"));
    }

    @Transactional
    public Engenheiro salvar(Engenheiro engenheiro) {
        return engenheiroRepository.save(engenheiro);
    }

    @Transactional
    public Engenheiro atualizar(Long id, Engenheiro formulario) {
        Engenheiro engenheiro = buscarPorId(id);
        engenheiro.setNome(formulario.getNome());
        engenheiro.setArea(formulario.getArea());
        return engenheiroRepository.save(engenheiro);
    }

    @Transactional
    public void excluir(Long id) {
        Engenheiro engenheiro = buscarPorId(id);
        engenheiroRepository.delete(engenheiro);
    }
}
