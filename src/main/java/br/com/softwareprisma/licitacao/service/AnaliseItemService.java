package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.repository.AnaliseItemRepository;
import br.com.softwareprisma.licitacao.service.matcher.DescricaoMatcher;
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
    private final DescricaoMatcher descricaoMatcher;

    @Transactional(readOnly = true)
    public AnaliseItem buscarDetalhadoPorId(Long id) {
        return analiseItemRepository.buscarDetalhadoPorId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item da analise nao encontrado"));
    }

    @Transactional
    public AnaliseItem salvar(Long analiseId, AnaliseItem item) {
        Analise analise = analiseService.buscarDetalhadaPorId(analiseId);
        
        // Verificar se já existe item equivalente na análise
        for (AnaliseItem existente : analise.getItens()) {
            if (descricaoMatcher.corresponde(
                    item.getDescricao(),
                    item.getUnidade(),
                    existente.getDescricao(),
                    existente.getUnidade())) {
                // Item equivalente encontrado - somar quantidade
                existente.setQuantidade(existente.getQuantidade().add(item.getQuantidade()));
                return analiseItemRepository.save(existente);
            }
        }
        
        // Nenhum item equivalente - criar novo
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
