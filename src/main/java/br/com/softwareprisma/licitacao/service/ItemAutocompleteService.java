package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.ItemSugestaoDTO;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.domain.CatItem;
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
    public List<ItemSugestaoDTO> buscarSugestoesAgrupadas(String termo, Area area) {
        return buscarSugestoesAgrupadas(termo, area, null);
    }

    @Transactional(readOnly = true)
    public List<ItemSugestaoDTO> buscarSugestoesAgrupadas(String termo, Area area, List<String> itensJaAdicionados) {
        if (termo == null || termo.trim().length() < 2) {
            return List.of();
        }

        List<CatItem> itens = area != null
                ? catItemRepository.buscarItensPorTermoParaAutocomplete(termo, area)
                : catItemRepository.buscarItensPorTermoParaAutocomplete(termo);

        // Gerar chaves normalizadas dos itens já adicionados
        java.util.Set<String> chavesJaAdicionadas = new java.util.HashSet<>();
        if (itensJaAdicionados != null) {
            for (String itemJaAdicionado : itensJaAdicionados) {
                String[] partes = itemJaAdicionado.split("\\|");
                if (partes.length == 2) {
                    String chave = descricaoMatcher.gerarChave(partes[0], partes[1]);
                    chavesJaAdicionadas.add(chave);
                }
            }
        }

        // Agrupar por chave normalizada (descricao normalizada | unidade normalizada)
        Map<String, GrupoItemSugestao> grupos = new LinkedHashMap<>();

        for (CatItem item : itens) {
            String chave = descricaoMatcher.gerarChave(item.getDescricao(), item.getUnidade());
            
            // Pular se já foi adicionado à análise
            if (chavesJaAdicionadas.contains(chave)) {
                continue;
            }
            
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

    @Transactional(readOnly = true)
    public List<ItemSugestaoDTO> buscarItensRecentes(Area area) {
        // 1. Buscar AnaliseItems recentes (apenas para identificar quais itens foram usados)
        List<AnaliseItem> analiseItemsRecentes = analiseItemRepository.buscarRecentes(area);
        
        if (analiseItemsRecentes.isEmpty()) {
            return List.of();
        }
        
        // 2. Extrair itens lógicos únicos usando DescricaoMatcher
        Set<String> chavesUnicas = new HashSet<>();
        for (AnaliseItem ai : analiseItemsRecentes) {
            String chave = descricaoMatcher.gerarChave(ai.getDescricao(), ai.getUnidade());
            chavesUnicas.add(chave);
        }
        
        // 3. Buscar TODOS os CatItems da área
        List<CatItem> todosCatItems = area != null
                ? catItemRepository.buscarTodosPorArea(area)
                : catItemRepository.buscarTodos();
        
        // 4. Agrupar por chave normalizada e somar
        Map<String, GrupoItemSugestao> grupos = new LinkedHashMap<>();
        for (CatItem item : todosCatItems) {
            String chave = descricaoMatcher.gerarChave(item.getDescricao(), item.getUnidade());
            
            // Incluir apenas se está nos itens recentes
            if (!chavesUnicas.contains(chave)) {
                continue;
            }
            
            GrupoItemSugestao grupo = grupos.computeIfAbsent(chave, k -> 
                new GrupoItemSugestao(item.getDescricao(), item.getUnidade()));
            grupo.adicionarQuantidade(item.getQuantidade());
        }
        
        // 5. Converter para DTOs
        List<ItemSugestaoDTO> resultado = new ArrayList<>();
        for (GrupoItemSugestao grupo : grupos.values()) {
            resultado.add(new ItemSugestaoDTO(
                    grupo.descricao(),
                    grupo.unidade(),
                    grupo.quantidadeTotal()
            ));
        }
        
        // 6. Ordenar por data de criação da análise mais recente
        // Usamos a ordem dos AnaliseItems recentes como referência
        resultado.sort((a, b) -> {
            int indexA = -1;
            int indexB = -1;
            for (int i = 0; i < analiseItemsRecentes.size(); i++) {
                AnaliseItem ai = analiseItemsRecentes.get(i);
                String chaveA = descricaoMatcher.gerarChave(a.descricao(), a.unidade());
                String chaveB = descricaoMatcher.gerarChave(b.descricao(), b.unidade());
                String chaveAi = descricaoMatcher.gerarChave(ai.getDescricao(), ai.getUnidade());
                if (indexA == -1 && chaveA.equals(chaveAi)) indexA = i;
                if (indexB == -1 && chaveB.equals(chaveAi)) indexB = i;
                if (indexA != -1 && indexB != -1) break;
            }
            return Integer.compare(indexA, indexB);
        });
        
        return resultado.stream().limit(10).toList();
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
