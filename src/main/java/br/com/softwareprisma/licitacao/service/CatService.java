package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.repository.CatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CatService {

    private final CatRepository catRepository;
    private final EngenheiroService engenheiroService;

    @Transactional(readOnly = true)
    public List<Cat> listarTodas() {
        return catRepository.listarTodasComEngenheiroEItens();
    }

    @Transactional(readOnly = true)
    public Cat buscarPorId(Long id) {
        return catRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "CAT nao encontrada"));
    }

    @Transactional(readOnly = true)
    public Cat buscarDetalhadaPorId(Long id) {
        return catRepository.buscarDetalhadaPorId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "CAT nao encontrada"));
    }

    @Transactional
    public Cat salvar(Cat cat, Long engenheiroId) {
        Engenheiro engenheiro = engenheiroService.buscarPorId(engenheiroId);
        cat.setEngenheiro(engenheiro);
        return catRepository.save(cat);
    }

    @Transactional
    public Cat atualizar(Long id, Cat formulario, Long engenheiroId) {
        Cat cat = buscarPorId(id);
        Engenheiro engenheiro = engenheiroService.buscarPorId(engenheiroId);
        cat.setEngenheiro(engenheiro);
        cat.setNome(formulario.getNome());
        cat.setNumeroCat(formulario.getNumeroCat());
        cat.setMunicipio(formulario.getMunicipio());
        cat.setObservacoes(formulario.getObservacoes());
        return catRepository.save(cat);
    }

    @Transactional
    public void excluir(Long id) {
        Cat cat = buscarPorId(id);
        catRepository.delete(cat);
    }
}
