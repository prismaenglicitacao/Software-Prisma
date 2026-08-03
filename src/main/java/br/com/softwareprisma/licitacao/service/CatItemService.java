package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import br.com.softwareprisma.licitacao.service.matcher.DescricaoMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CatItemService {

    private final CatItemRepository catItemRepository;
    private final CatService catService;
    private final DescricaoMatcher descricaoMatcher;

    @Transactional(readOnly = true)
    public CatItem buscarDetalhadoPorId(Long id) {
        return catItemRepository.buscarDetalhadoPorId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item da CAT nao encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<CatItem> listarPaginado(Long catId, String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return catItemRepository.findByCatId(catId, pageable);
        }
        return catItemRepository.findByCatIdAndDescricaoContainingIgnoreCase(catId, q, pageable);
    }

    @Transactional
    public CatItem salvar(Long catId, CatItem item) {
        Cat cat = catService.buscarPorId(catId);
        
        // Verificar se já existe item equivalente na CAT
        for (CatItem existente : cat.getItens()) {
            if (descricaoMatcher.corresponde(
                    item.getDescricao(),
                    item.getUnidade(),
                    existente.getDescricao(),
                    existente.getUnidade())) {
                // Item equivalente encontrado - somar quantidade
                existente.setQuantidade(existente.getQuantidade().add(item.getQuantidade()));
                return catItemRepository.save(existente);
            }
        }
        
        // Nenhum item equivalente - criar novo
        item.setCat(cat);
        return catItemRepository.save(item);
    }

    @Transactional
    public List<CatItem> salvarEmLote(Long catId, List<CatItem> itens) {
        Cat cat = catService.buscarPorId(catId);
        
        // Agrupar itens equivalentes por chave normalizada
        Map<String, CatItem> itensAgrupados = new LinkedHashMap<>();
        
        // Primeiro, adicionar itens existentes da CAT ao mapa
        for (CatItem existente : cat.getItens()) {
            String chave = descricaoMatcher.gerarChave(existente.getDescricao(), existente.getUnidade());
            itensAgrupados.put(chave, existente);
        }
        
        // Depois, processar novos itens agrupando com existentes
        for (CatItem item : itens) {
            String chave = descricaoMatcher.gerarChave(item.getDescricao(), item.getUnidade());
            CatItem agrupado = itensAgrupados.get(chave);
            
            if (agrupado != null) {
                // Item equivalente existe - somar quantidade
                agrupado.setQuantidade(agrupado.getQuantidade().add(item.getQuantidade()));
            } else {
                // Novo item - adicionar ao mapa
                item.setCat(cat);
                itensAgrupados.put(chave, item);
            }
        }
        
        // Salvar todos os itens do mapa (incluindo atualizações e novos)
        return catItemRepository.saveAll(itensAgrupados.values());
    }

    @Transactional
    public CatItem atualizar(Long itemId, CatItem formulario) {
        CatItem item = buscarDetalhadoPorId(itemId);
        item.setDescricao(formulario.getDescricao());
        item.setQuantidade(formulario.getQuantidade());
        item.setUnidade(formulario.getUnidade());
        return catItemRepository.save(item);
    }

    @Transactional
    public void excluir(Long itemId) {
        CatItem item = buscarDetalhadoPorId(itemId);
        catItemRepository.delete(item);
    }
}
