package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Transactional
    public CatItem salvar(Long catId, CatItem item) {
        Cat cat = catService.buscarPorId(catId);
        item.setCat(cat);
        return catItemRepository.save(item);
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
