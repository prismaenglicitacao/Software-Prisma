package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;
import br.com.softwareprisma.licitacao.repository.AnaliseRepository;
import br.com.softwareprisma.licitacao.repository.CatRepository;
import br.com.softwareprisma.licitacao.service.matcher.DescricaoMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AnaliseService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final AnaliseRepository analiseRepository;
    private final CatRepository catRepository;
    private final DescricaoMatcher descricaoMatcher;

    @Transactional(readOnly = true)
    public AnaliseResultado buscarResultadoPersistido(Long id) {
        Analise analise = buscarDetalhadaPorId(id);
        
        if (analise.getResultado() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Análise ainda não foi processada.");
        }
        
        // Criar resultado baseado nos dados persistidos
        List<AnaliseResultadoItem> itens = criarItensResultadoPersistidos(analise.getItens());
        
        List<AnaliseResultadoItem> faltantes = itens.stream()
                .filter(i -> !i.atende())
                .toList();
        
        return new AnaliseResultado(
                analise.getResultado(),
                List.of(), // Engenheiros não persistidos
                List.of(), // CATs não persistidos
                itens,
                faltantes,
                analise.getCobertura() != null ? analise.getCobertura() : BigDecimal.ZERO,
                analise.getResultado() == ResultadoAnalise.ATENDE);
    }
    
    private List<AnaliseResultadoItem> criarItensResultadoPersistidos(List<AnaliseItem> itensAnalise) {
        List<AnaliseResultadoItem> resultado = new ArrayList<>();
        
        for (AnaliseItem item : itensAnalise) {
            resultado.add(
                    new AnaliseResultadoItem(
                            item.getDescricao(),
                            item.getUnidade(),
                            item.getQuantidade(),
                            BigDecimal.ZERO, // Não temos valor encontrado persistido
                            false, // Não podemos determinar sem recalcular
                            List.of())); // Sem origens persistidas
        }
        
        return resultado;
    }

    @Transactional(readOnly = true)
    public Analise buscarDetalhadaPorId(Long id) {
        return analiseRepository.buscarDetalhadaPorId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Analise nao encontrada"));
    }

    @Transactional
    public Analise criar(Area area) {
        Analise analise = new Analise();
        analise.setArea(area);
        return analiseRepository.save(analise);
    }

    @Transactional
    public AnaliseResultado prepararAnalise(Long id) {
        Analise analise = buscarDetalhadaPorId(id);
        if (analise.getItens().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Adicione ao menos um item antes de analisar.");
        }

        AnaliseResultado resultado = comparar(analise);
        analise.setResultado(resultado.resultado());
        analise.setCobertura(resultado.cobertura());
        analiseRepository.save(analise);
        return resultado;
    }

    @Transactional(readOnly = true)
    public AnaliseResultado comparar(Long id) {
        Analise analise = buscarDetalhadaPorId(id);
        return comparar(analise);
    }

    private AnaliseResultado comparar(Analise analise) {

        BigDecimal totalExigido = analise.getItens()
                .stream()
                .map(AnaliseItem::getQuantidade)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<EngenheiroInfo> engenheiros = agruparEngenheirosPorArea(analise.getArea());

        if (engenheiros.isEmpty()) {
            return criarResultado(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    totalExigido,
                    analise.getItens());
        }

        Optional<AnaliseResultado> buscaCompleta = buscarMelhorCombinacaoValida(
                engenheiros,
                totalExigido,
                analise.getItens());

        if (buscaCompleta.isPresent()) {
            return buscaCompleta.get();
        }

        return buscarMelhorCombinacaoInvalida(
                engenheiros,
                totalExigido,
                analise.getItens());
    }

    private List<RequisicaoAnalise> criarRequisicoes(List<AnaliseItem> itens) {

        List<RequisicaoAnalise> lista = new ArrayList<>();

        for (AnaliseItem item : itens) {

            lista.add(new RequisicaoAnalise(
                    item.getDescricao(),
                    item.getUnidade(),
                    item.getQuantidade()));
        }

        return lista;
    }

    private List<EngenheiroInfo> agruparEngenheirosPorArea(Area area) {
        Map<Long, EngenheiroInfo> lista = new TreeMap<>();
        for (Cat cat : catRepository.listarTodasComEngenheiroEItens()) {
            if (cat.getEngenheiro() == null || cat.getEngenheiro().getArea() == null) {
                continue;
            }
            if (!Objects.equals(cat.getEngenheiro().getArea(), area)) {
                continue;
            }
            Long engenheiroId = cat.getEngenheiro().getId();
            EngenheiroInfo info = lista.computeIfAbsent(
                    engenheiroId,
                    id -> new EngenheiroInfo(
                            engenheiroId,
                            cat.getEngenheiro().getNome(),
                            new ArrayList<>(),
                            new ArrayList<>()));
            info.adicionarCat(cat);
        }

        return lista.values().stream()
                .sorted(Comparator.comparing(EngenheiroInfo::nome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private Optional<AnaliseResultado> buscarMelhorCombinacaoValida(
            List<EngenheiroInfo> engenheiros,
            BigDecimal totalExigido,
            List<AnaliseItem> itensAnalise) {
        for (int tamanho = 1; tamanho <= engenheiros.size(); tamanho++) {
            AnaliseResultado melhor = null;
            for (int mask = 1; mask < (1 << engenheiros.size()); mask++) {
                if (Integer.bitCount(mask) != tamanho) {
                    continue;
                }
                Candidate candidato = construirCandidato(engenheiros, mask);
                if (!atendeTodosOsItens(candidato.itens(), itensAnalise)) {
                    continue;
                }
                AnaliseResultado resultado = criarResultado(
                        candidato,
                        totalExigido,
                        itensAnalise);
                ;
                if (melhor == null || compararMelhorCandidato(resultado, melhor)) {
                    melhor = resultado;
                }
            }
            if (melhor != null) {
                return Optional.of(melhor);
            }
        }
        return Optional.empty();
    }

    private AnaliseResultado buscarMelhorCombinacaoInvalida(
            List<EngenheiroInfo> engenheiros,
            BigDecimal totalExigido,
            List<AnaliseItem> itensAnalise) {
        AnaliseResultado melhor = null;
        for (int mask = 1; mask < (1 << engenheiros.size()); mask++) {
            Candidate candidato = construirCandidato(engenheiros, mask);
            AnaliseResultado resultado = criarResultado(
                    candidato,
                    totalExigido,
                    itensAnalise);
            if (melhor == null || compararMelhorCandidatoNaoAtende(resultado, melhor)) {
                melhor = resultado;
            }
        }
        if (melhor == null) {
            return criarResultado(Collections.emptyList(), Collections.emptyList(), totalExigido,
                    itensAnalise);
        }
        return melhor;
    }

    private Candidate construirCandidato(List<EngenheiroInfo> engenheiros, int mask) {

        List<EngenheiroInfo> selecionados = new ArrayList<>();

        List<String> catNomes = new ArrayList<>();

        List<CatItem> itens = new ArrayList<>();

        for (int index = 0; index < engenheiros.size(); index++) {

            if ((mask & (1 << index)) == 0) {
                continue;
            }

            EngenheiroInfo info = engenheiros.get(index);

            selecionados.add(info);

            catNomes.addAll(info.catNomes());

            itens.addAll(info.itens());

        }

        return new Candidate(
                selecionados,
                catNomes,
                itens);

    }

    private boolean atendeTodosOsItens(
            List<CatItem> itensCat,
            List<AnaliseItem> itensAnalise) {

        for (AnaliseItem requisito : itensAnalise) {

            BigDecimal quantidadeEncontrada = BigDecimal.ZERO;

            for (CatItem item : itensCat) {

                if (!descricaoMatcher.corresponde(
                        requisito.getDescricao(),
                        requisito.getUnidade(),
                        item.getDescricao(),
                        item.getUnidade())) {
                    continue;
                }

                quantidadeEncontrada = quantidadeEncontrada.add(item.getQuantidade());

            }

            if (quantidadeEncontrada.compareTo(requisito.getQuantidade()) < 0) {
                return false;
            }

        }

        return true;
    }

    private boolean compararMelhorCandidato(AnaliseResultado candidato, AnaliseResultado atual) {
        int engenheirosCandidato = candidato.engenheiros().size();
        int engenheirosAtual = atual.engenheiros().size();
        if (engenheirosCandidato != engenheirosAtual) {
            return engenheirosCandidato < engenheirosAtual;
        }
        int catsCandidato = candidato.cats().size();
        int catsAtual = atual.cats().size();
        return catsCandidato < catsAtual;
    }

    private boolean compararMelhorCandidatoNaoAtende(AnaliseResultado candidato, AnaliseResultado atual) {
        int cobertura = candidato.cobertura().compareTo(atual.cobertura());
        if (cobertura != 0) {
            return cobertura > 0;
        }
        int engenheiros = candidato.engenheiros().size() - atual.engenheiros().size();
        if (engenheiros != 0) {
            return engenheiros < 0;
        }
        return candidato.cats().size() < atual.cats().size();
    }

    private AnaliseResultado criarResultado(
            Candidate candidato,
            BigDecimal totalExigido,
            List<AnaliseItem> itensAnalise) {

        List<String> engenheiros = candidato.engenheiros()
                .stream()
                .map(EngenheiroInfo::nome)
                .toList();

        List<AnaliseResultadoItem> itens = criarItensResultado(
                itensAnalise,
                candidato.itens());

        BigDecimal cobertura = calcularCobertura(
                itensAnalise,
                candidato.itens(),
                totalExigido);

        List<AnaliseResultadoItem> faltantes = itens.stream()
                .filter(i -> !i.atende())
                .toList();

        ResultadoAnalise resultado = faltantes.isEmpty()
                ? ResultadoAnalise.ATENDE
                : ResultadoAnalise.NAO_ATENDE;

        List<String> catsUtilizadas = identificarCatsUtilizadas(itensAnalise, candidato.itens());

        return new AnaliseResultado(
                resultado,
                engenheiros,
                catsUtilizadas,
                itens,
                faltantes,
                cobertura,
                faltantes.isEmpty());

    }

    private List<String> identificarCatsUtilizadas(List<AnaliseItem> itensAnalise, List<CatItem> itensCat) {
        Set<String> catsUtilizadas = new java.util.HashSet<>();
        
        for (AnaliseItem requisito : itensAnalise) {
            for (CatItem item : itensCat) {
                if (descricaoMatcher.corresponde(
                        requisito.getDescricao(),
                        requisito.getUnidade(),
                        item.getDescricao(),
                        item.getUnidade())) {
                    if (item.getCat() != null && item.getCat().getNome() != null) {
                        catsUtilizadas.add(item.getCat().getNome());
                    }
                }
            }
        }
        
        return new ArrayList<>(catsUtilizadas);
    }

    private AnaliseResultado criarResultado(
            List<String> engenheiros,
            List<String> cats,
            BigDecimal totalExigido,
            List<AnaliseItem> itensAnalise) {

        List<AnaliseResultadoItem> itens = criarItensResultado(
                itensAnalise,
                List.of());

        BigDecimal cobertura = calcularCobertura(
                itensAnalise,
                List.of(),
                totalExigido);

        List<AnaliseResultadoItem> faltantes = itens.stream()
                .filter(i -> !i.atende())
                .toList();

        return new AnaliseResultado(
                ResultadoAnalise.NAO_ATENDE,
                engenheiros,
                cats,
                itens,
                faltantes,
                cobertura,
                false);

    }

    private List<AnaliseResultadoItem> criarItensResultado(
            List<AnaliseItem> itensAnalise,
            List<CatItem> itensCat) {

        List<AnaliseResultadoItem> resultado = new ArrayList<>();

        for (AnaliseItem requisito : itensAnalise) {

            BigDecimal encontrado = BigDecimal.ZERO;
            List<String> origens = new ArrayList<>();

            for (CatItem item : itensCat) {

                if (!descricaoMatcher.corresponde(
                        requisito.getDescricao(),
                        requisito.getUnidade(),
                        item.getDescricao(),
                        item.getUnidade())) {
                    continue;
                }

                encontrado = encontrado.add(item.getQuantidade());

                // Registrar origem: CAT - Engenheiro
                if (item.getCat() != null) {
                    String catNome = item.getCat().getNome();
                    String engenheiroNome = item.getCat().getEngenheiro() != null 
                            ? item.getCat().getEngenheiro().getNome() 
                            : "N/A";
                    String origem = catNome + " - " + engenheiroNome;
                    if (!origens.contains(origem)) {
                        origens.add(origem);
                    }
                }

            }

            resultado.add(
                    new AnaliseResultadoItem(
                            requisito.getDescricao(),
                            requisito.getUnidade(),
                            requisito.getQuantidade(),
                            encontrado,
                            encontrado.compareTo(requisito.getQuantidade()) >= 0,
                            origens));

        }

        return resultado;

    }

    private BigDecimal calcularCobertura(
            List<AnaliseItem> itensAnalise,
            List<CatItem> itensCat,
            BigDecimal totalExigido) {

        if (totalExigido.compareTo(ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }

        BigDecimal encontrado = BigDecimal.ZERO;

        for (AnaliseItem requisito : itensAnalise) {

            BigDecimal quantidade = BigDecimal.ZERO;

            for (CatItem item : itensCat) {

                if (!descricaoMatcher.corresponde(
                        requisito.getDescricao(),
                        requisito.getUnidade(),
                        item.getDescricao(),
                        item.getUnidade())) {
                    continue;
                }

                quantidade = quantidade.add(item.getQuantidade());
            }

            encontrado = encontrado.add(
                    quantidade.min(requisito.getQuantidade()));
        }

        return encontrado
                .divide(totalExigido, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private CatItem localizarItemCorrespondente(
            String descricao,
            String unidade,
            List<CatItem> itens) {

        CatItem melhor = null;
        double melhorScore = 0;

        for (CatItem item : itens) {

            if (!descricaoMatcher.corresponde(
                    descricao,
                    unidade,
                    item.getDescricao(),
                    item.getUnidade())) {
                continue;
            }

            double score = descricaoMatcher.similaridade(
                    descricao,
                    item.getDescricao());

            if (score > melhorScore) {
                melhorScore = score;
                melhor = item;
            }
        }

        return melhor;
    }

    private record EngenheiroInfo(
            Long id,
            String nome,
            List<String> catNomes,
            List<CatItem> itens) {

        void adicionarCat(Cat cat) {

            this.catNomes.add(cat.getNome());

            this.itens.addAll(cat.getItens());

        }
    }

    private record Candidate(
            List<EngenheiroInfo> engenheiros,
            List<String> catNomes,
            List<CatItem> itens) {
    }
}
