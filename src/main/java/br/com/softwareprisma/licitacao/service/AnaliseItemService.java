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
        System.out.println("=== AnaliseItemService.salvar() INICIO ===");
        System.out.println("Analise ID: " + analiseId);
        
        Analise analise = analiseService.buscarDetalhadaPorId(analiseId);
        
        System.out.println("Analise carregada - Quantidade de itens ANTES do save: " + analise.getItens().size());
        System.out.println("Itens ANTES do save:");
        for (AnaliseItem i : analise.getItens()) {
            System.out.println("  - ID: " + i.getId() + ", Descricao: " + i.getDescricao());
        }
        
        item.setAnalise(analise);
        AnaliseItem saved = analiseItemRepository.save(item);
        
        System.out.println("Item salvo - ID: " + saved.getId());
        System.out.println("Quantidade de itens DEPOIS do save: " + analise.getItens().size());
        System.out.println("Itens DEPOIS do save:");
        for (AnaliseItem i : analise.getItens()) {
            System.out.println("  - ID: " + i.getId() + ", Descricao: " + i.getDescricao());
        }
        System.out.println("=== AnaliseItemService.salvar() FIM ===");
        
        return saved;
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
