package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.ItemSugestaoDTO;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.repository.AnaliseItemRepository;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import br.com.softwareprisma.licitacao.service.matcher.DescricaoMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ItemAutocompleteService {

    private final CatItemRepository catItemRepository;
    private final AnaliseItemRepository analiseItemRepository;
    private final DescricaoMatcher descricaoMatcher;

    @Transactional(readOnly = true)
    public List<ItemSugestaoDTO> buscarSugestoesAgrupadas(String termo, Area area,
                                                           List<String> itensJaAdicionados, Empresa empresa) {
        if (termo == null || termo.trim().length() < 2) {
            return List.of();
        }

        List<CatItem> itens;
        if (empresa != null) {
            itens = catItemRepository.buscarItensPorTermoParaAutocompleteEEmpresa(termo, area, empresa);
        } else if (area != null) {
            itens = catItemRepository.buscarItensPorTermoParaAutocomplete(termo, area);
        } else {
            itens = catItemRepository.buscarItensPorTermoParaAutocomplete(termo);
        }

        Set<String> chavesJaAdicionadas = new HashSet<>();
        if (itensJaAdicionados != null) {
            for (String itemJaAdicionado : itensJaAdicionados) {
                String[] partes = itemJaAdicionado.split("\\|");
                if (partes.length == 2) {
                    chavesJaAdicionadas.add(descricaoMatcher.gerarChave(partes[0], partes[1]));
                }
            }
        }

        Map<String, GrupoItemSugestao> grupos = new LinkedHashMap<>();
        for (CatItem item : itens) {
            String chave = descricaoMatcher.gerarChave(item.getDescricao(), item.getUnidade());
            if (chavesJaAdicionadas.contains(chave)) continue;
            grupos.computeIfAbsent(chave, k -> new GrupoItemSugestao(item.getDescricao(), item.getUnidade()))
                  .adicionarQuantidade(item.getQuantidade());
        }

        List<ItemSugestaoDTO> resultado = new ArrayList<>();
        for (GrupoItemSugestao grupo : grupos.values()) {
            resultado.add(new ItemSugestaoDTO(grupo.descricao(), grupo.unidade(), grupo.quantidadeTotal()));
        }

        resultado.sort((a, b) -> {
            String termoLower = termo.toLowerCase();
            boolean aComeca = a.descricao().toLowerCase().startsWith(termoLower);
            boolean bComeca = b.descricao().toLowerCase().startsWith(termoLower);
            if (aComeca && !bComeca) return -1;
            if (!aComeca && bComeca) return 1;
            return a.descricao().compareToIgnoreCase(b.descricao());
        });

        return resultado.stream().limit(100).toList();
    }

    @Transactional(readOnly = true)
    public List<ItemSugestaoDTO> buscarItensRecentes(Area area, Empresa empresa) {
        List<AnaliseItem> analiseItemsRecentes = empresa != null
                ? analiseItemRepository.buscarRecentesPorEmpresa(area, empresa)
                : analiseItemRepository.buscarRecentes(area);

        if (analiseItemsRecentes.isEmpty()) return List.of();

        Set<String> chavesUnicas = new HashSet<>();
        for (AnaliseItem ai : analiseItemsRecentes) {
            chavesUnicas.add(descricaoMatcher.gerarChave(ai.getDescricao(), ai.getUnidade()));
        }

        List<CatItem> todosCatItems;
        if (empresa != null) {
            todosCatItems = catItemRepository.buscarTodosPorAreaEEmpresa(area, empresa);
        } else if (area != null) {
            todosCatItems = catItemRepository.buscarTodosPorArea(area);
        } else {
            todosCatItems = catItemRepository.buscarTodos();
        }

        Map<String, GrupoItemSugestao> grupos = new LinkedHashMap<>();
        for (CatItem item : todosCatItems) {
            String chave = descricaoMatcher.gerarChave(item.getDescricao(), item.getUnidade());
            if (!chavesUnicas.contains(chave)) continue;
            grupos.computeIfAbsent(chave, k -> new GrupoItemSugestao(item.getDescricao(), item.getUnidade()))
                  .adicionarQuantidade(item.getQuantidade());
        }

        List<ItemSugestaoDTO> resultado = new ArrayList<>();
        for (GrupoItemSugestao grupo : grupos.values()) {
            resultado.add(new ItemSugestaoDTO(grupo.descricao(), grupo.unidade(), grupo.quantidadeTotal()));
        }

        resultado.sort((a, b) -> {
            int indexA = -1, indexB = -1;
            for (int i = 0; i < analiseItemsRecentes.size(); i++) {
                AnaliseItem ai = analiseItemsRecentes.get(i);
                String chaveAi = descricaoMatcher.gerarChave(ai.getDescricao(), ai.getUnidade());
                if (indexA == -1 && descricaoMatcher.gerarChave(a.descricao(), a.unidade()).equals(chaveAi)) indexA = i;
                if (indexB == -1 && descricaoMatcher.gerarChave(b.descricao(), b.unidade()).equals(chaveAi)) indexB = i;
                if (indexA != -1 && indexB != -1) break;
            }
            return Integer.compare(indexA, indexB);
        });

        return resultado.stream().limit(10).toList();
    }

    private static class GrupoItemSugestao {
        private final String descricao;
        private final String unidade;
        private BigDecimal quantidadeTotal = BigDecimal.ZERO;

        GrupoItemSugestao(String descricao, String unidade) {
            this.descricao = descricao;
            this.unidade = unidade;
        }

        String descricao() { return descricao; }
        String unidade() { return unidade; }
        BigDecimal quantidadeTotal() { return quantidadeTotal; }

        void adicionarQuantidade(BigDecimal quantidade) {
            if (quantidade != null) this.quantidadeTotal = this.quantidadeTotal.add(quantidade);
        }
    }
}
