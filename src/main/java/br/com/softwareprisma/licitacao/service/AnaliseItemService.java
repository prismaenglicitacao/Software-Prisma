package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.repository.AnaliseItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AnaliseItemService {

    private final AnaliseItemRepository analiseItemRepository;
    private final AnaliseService analiseService;

    @Transactional(readOnly = true)
    public AnaliseItem buscarDetalhadoPorId(Long id) {
        return analiseItemRepository.buscarDetalhadoPorId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item da analise nao encontrado"));
    }

    @Transactional
    public AnaliseItem salvar(Long analiseId, AnaliseItem item) {
        Analise analise = analiseService.buscarDetalhadaPorId(analiseId);
        item.setAnalise(analise);
        return analiseItemRepository.save(item);
    }

    @Transactional
    public AnaliseItem atualizar(Long itemId, AnaliseItem formulario) {
        AnaliseItem item = buscarDetalhadoPorId(itemId);
        item.setDescricao(formulario.getDescricao());
        item.setQuantidade(formulario.getQuantidade());
        item.setUnidade(formulario.getUnidade());
        return analiseItemRepository.save(item);
    }

    @Transactional
    public void excluir(Long itemId) {
        AnaliseItem item = buscarDetalhadoPorId(itemId);
        analiseItemRepository.delete(item);
    }
}
