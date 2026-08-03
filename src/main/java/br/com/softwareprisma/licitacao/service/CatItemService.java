package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CatItemService {

    private final CatItemRepository catItemRepository;
    private final CatService catService;

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
        item.setCat(cat);
        return catItemRepository.save(item);
    }

    @Transactional
    public List<CatItem> salvarEmLote(Long catId, List<CatItem> itens) {
        Cat cat = catService.buscarPorId(catId);
        for (CatItem item : itens) {
            item.setCat(cat);
        }
        return catItemRepository.saveAll(itens);
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
