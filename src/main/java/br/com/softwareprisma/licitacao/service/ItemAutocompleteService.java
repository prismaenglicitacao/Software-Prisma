package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.ItemSugestaoDTO;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import br.com.softwareprisma.licitacao.service.matcher.DescricaoMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemAutocompleteService {

    private final CatItemRepository catItemRepository;
    private final DescricaoMatcher descricaoMatcher;

    @Transactional(readOnly = true)
    public List<ItemSugestaoDTO> buscarSugestoesAgrupadas(String termo, Area area) {
        if (termo == null || termo.trim().length() < 2) {
            return List.of();
        }

        List<CatItem> itens = area != null
                ? catItemRepository.buscarItensPorTermoParaAutocomplete(termo, area)
                : catItemRepository.buscarItensPorTermoParaAutocomplete(termo);

        // Agrupar por chave normalizada (descricao normalizada | unidade normalizada)
        Map<String, GrupoItemSugestao> grupos = new LinkedHashMap<>();

        for (CatItem item : itens) {
            String chave = descricaoMatcher.gerarChave(item.getDescricao(), item.getUnidade());
            
            GrupoItemSugestao grupo = grupos.computeIfAbsent(chave, k -> {
                // Usar a descrição original do primeiro item do grupo
                return new GrupoItemSugestao(item.getDescricao(), item.getUnidade());
            });
            
            // Somar quantidade
            grupo.adicionarQuantidade(item.getQuantidade());
        }

        // Converter para DTOs
        List<ItemSugestaoDTO> resultado = new ArrayList<>();
        for (GrupoItemSugestao grupo : grupos.values()) {
            resultado.add(new ItemSugestaoDTO(
                    grupo.descricao(),
                    grupo.unidade(),
                    grupo.quantidadeTotal()
            ));
        }

        // Ordenar por relevância (começa com o termo primeiro)
        resultado.sort((a, b) -> {
            String termoLower = termo.toLowerCase();
            boolean aComecaCom = a.descricao().toLowerCase().startsWith(termoLower);
            boolean bComecaCom = b.descricao().toLowerCase().startsWith(termoLower);
            
            if (aComecaCom && !bComecaCom) return -1;
            if (!aComecaCom && bComecaCom) return 1;
            
            return a.descricao().compareToIgnoreCase(b.descricao());
        });

        return resultado.stream().limit(100).toList();
    }

    private static class GrupoItemSugestao {
        private final String descricao;
        private final String unidade;
        private BigDecimal quantidadeTotal;

        GrupoItemSugestao(String descricao, String unidade) {
            this.descricao = descricao;
            this.unidade = unidade;
            this.quantidadeTotal = BigDecimal.ZERO;
        }

        String descricao() {
            return descricao;
        }

        String unidade() {
            return unidade;
        }

        BigDecimal quantidadeTotal() {
            return quantidadeTotal;
        }

        void adicionarQuantidade(BigDecimal quantidade) {
            if (quantidade != null) {
                this.quantidadeTotal = this.quantidadeTotal.add(quantidade);
            }
        }
    }
}
