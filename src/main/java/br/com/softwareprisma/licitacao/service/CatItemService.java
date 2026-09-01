package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.Empresa;
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
    public CatItem buscarDetalhadoPorIdEEmpresa(Long id, Empresa empresa) {
        return catItemRepository.buscarDetalhadoPorIdEEmpresa(id, empresa)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item da CAT nao encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<CatItem> listarPaginado(Long catId, String q, Pageable pageable) {
        catService.buscarDetalhadaPorId(catId);

        if (q == null || q.isBlank()) {
            return catItemRepository.findByCatId(catId, pageable);
        }
        return catItemRepository.findByCatIdAndDescricaoContainingIgnoreCase(catId, q, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CatItem> listarPaginadoEEmpresa(Long catId, Empresa empresa, String q, Pageable pageable) {
        catService.buscarDetalhadaPorIdEEmpresa(catId, empresa);

        if (q == null || q.isBlank()) {
            return catItemRepository.findByCatIdAndEmpresa(catId, empresa, pageable);
        }
        return catItemRepository.findByCatIdAndDescricaoContainingIgnoreCaseAndEmpresa(catId, q, empresa, pageable);
    }

    @Transactional
    public CatItem salvar(Long catId, CatItem item) {
        Cat cat = catService.buscarDetalhadaPorId(catId);

        for (CatItem existente : cat.getItens()) {
            if (descricaoMatcher.corresponde(
                    item.getDescricao(),
                    item.getUnidade(),
                    existente.getDescricao(),
                    existente.getUnidade())) {
                existente.setQuantidade(existente.getQuantidade().add(item.getQuantidade()));
                return catItemRepository.save(existente);
            }
        }

        item.setCat(cat);
        return catItemRepository.save(item);
    }

    @Transactional
    public CatItem salvarEEmpresa(Long catId, CatItem item, Empresa empresa) {
        Cat cat = catService.buscarDetalhadaPorIdEEmpresa(catId, empresa);

        for (CatItem existente : cat.getItens()) {
            if (descricaoMatcher.corresponde(
                    item.getDescricao(),
                    item.getUnidade(),
                    existente.getDescricao(),
                    existente.getUnidade())) {
                existente.setQuantidade(existente.getQuantidade().add(item.getQuantidade()));
                return catItemRepository.save(existente);
            }
        }

        item.setCat(cat);
        return catItemRepository.save(item);
    }

    @Transactional
    public List<CatItem> salvarEmLote(Long catId, List<CatItem> itens) {
        Cat cat = catService.buscarDetalhadaPorId(catId);

        Map<String, CatItem> itensAgrupados = new LinkedHashMap<>();

        for (CatItem existente : cat.getItens()) {
            String chave = descricaoMatcher.gerarChave(existente.getDescricao(), existente.getUnidade());
            itensAgrupados.put(chave, existente);
        }

        for (CatItem item : itens) {
            String chave = descricaoMatcher.gerarChave(item.getDescricao(), item.getUnidade());
            CatItem agrupado = itensAgrupados.get(chave);

            if (agrupado != null) {
                agrupado.setQuantidade(agrupado.getQuantidade().add(item.getQuantidade()));
            } else {
                item.setCat(cat);
                itensAgrupados.put(chave, item);
            }
        }

        return catItemRepository.saveAll(itensAgrupados.values());
    }

    @Transactional
    public List<CatItem> salvarEmLoteEEmpresa(Long catId, List<CatItem> itens, Empresa empresa) {
        Cat cat = catService.buscarDetalhadaPorIdEEmpresa(catId, empresa);

        Map<String, CatItem> itensAgrupados = new LinkedHashMap<>();

        for (CatItem existente : cat.getItens()) {
            String chave = descricaoMatcher.gerarChave(existente.getDescricao(), existente.getUnidade());
            itensAgrupados.put(chave, existente);
        }

        for (CatItem item : itens) {
            String chave = descricaoMatcher.gerarChave(item.getDescricao(), item.getUnidade());
            CatItem agrupado = itensAgrupados.get(chave);

            if (agrupado != null) {
                agrupado.setQuantidade(agrupado.getQuantidade().add(item.getQuantidade()));
            } else {
                item.setCat(cat);
                itensAgrupados.put(chave, item);
            }
        }

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
    public CatItem atualizarEEmpresa(Long itemId, CatItem formulario, Empresa empresa) {
        CatItem item = buscarDetalhadoPorIdEEmpresa(itemId, empresa);
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

    @Transactional
    public void excluirEEmpresa(Long itemId, Empresa empresa) {
        CatItem item = buscarDetalhadoPorIdEEmpresa(itemId, empresa);
        catItemRepository.delete(item);
    }
}
