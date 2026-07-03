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
import java.util.HashSet;
import java.util.List;
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
        List<RequisicaoAnalise> requisicoes = agruparRequisitos(analise.getItens());
        BigDecimal totalExigido = requisicoes.stream()
                .map(RequisicaoAnalise::quantidade)
                .reduce(ZERO, BigDecimal::add);

        List<EngenheiroInfo> engenheiros = agruparEngenheirosPorArea(analise.getArea());
        if (engenheiros.isEmpty()) {
            return criarResultado(Collections.emptyList(), Collections.emptyList(), requisicoes, totalExigido, analise.getItens());
        }

        Optional<AnaliseResultado> buscaCompleta = buscarMelhorCombinacaoValida(engenheiros, requisicoes, totalExigido, analise.getItens());
        if (buscaCompleta.isPresent()) {
            return buscaCompleta.get();
        }

        return buscarMelhorCombinacaoInvalida(engenheiros, requisicoes, totalExigido, analise.getItens());
    }

    private List<RequisicaoAnalise> agruparRequisitos(List<AnaliseItem> itens) {
        List<RequisicaoAnalise> requisicoes = new ArrayList<>();
        for (AnaliseItem item : itens) {
            requisicoes.add(new RequisicaoAnalise(
                    item.getDescricao(),
                    item.getUnidade(),
                    item.getQuantidade(),
                    null // itens do edital não têm CAT
            ));
        }
        return requisicoes;
    }

    private List<EngenheiroInfo> agruparEngenheirosPorArea(Area area) {
        Map<Long, EngenheiroInfo> lista = new TreeMap<>();
        for (Cat cat : catRepository.listarTodasComEngenheiroEItens()) {
            if (!Objects.equals(cat.getEngenheiro().getArea(), area)) {
                continue;
            }
            Long engenheiroId = cat.getEngenheiro().getId();
            lista.computeIfAbsent(engenheiroId, id -> new EngenheiroInfo(
                    engenheiroId,
                    cat.getEngenheiro().getNome(),
                    new ArrayList<>(),
                    new ArrayList<>()))
                    .adicionarCat(cat);
        }

        return lista.values().stream()
                .sorted(Comparator.comparing(EngenheiroInfo::nome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private Optional<AnaliseResultado> buscarMelhorCombinacaoValida(List<EngenheiroInfo> engenheiros,
                                                                    List<RequisicaoAnalise> requisicoes,
                                                                    BigDecimal totalExigido,
                                                                    List<AnaliseItem> itensAnalise) {
        for (int tamanho = 1; tamanho <= engenheiros.size(); tamanho++) {
            AnaliseResultado melhor = null;
            for (int mask = 1; mask < (1 << engenheiros.size()); mask++) {
                if (Integer.bitCount(mask) != tamanho) {
                    continue;
                }
                Candidate candidato = construirCandidato(engenheiros, mask);
                if (!atendeTodosOsItens(candidato.itens(), requisicoes)) {
                    continue;
                }
                AnaliseResultado resultado = criarResultado(candidato, requisicoes, totalExigido, itensAnalise);
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

    private AnaliseResultado buscarMelhorCombinacaoInvalida(List<EngenheiroInfo> engenheiros,
                                                            List<RequisicaoAnalise> requisicoes,
                                                            BigDecimal totalExigido,
                                                            List<AnaliseItem> itensAnalise) {
        AnaliseResultado melhor = null;
        for (int mask = 1; mask < (1 << engenheiros.size()); mask++) {
            Candidate candidato = construirCandidato(engenheiros, mask);
            AnaliseResultado resultado = criarResultado(candidato, requisicoes, totalExigido, itensAnalise);
            if (melhor == null || compararMelhorCandidatoNaoAtende(resultado, melhor)) {
                melhor = resultado;
            }
        }
        if (melhor == null) {
            return criarResultado(Collections.emptyList(), Collections.emptyList(), requisicoes, totalExigido, itensAnalise);
        }
        return melhor;
    }

    private Candidate construirCandidato(List<EngenheiroInfo> engenheiros, int mask) {
        List<RequisicaoAnalise> itens = new ArrayList<>();
        List<EngenheiroInfo> selecionados = new ArrayList<>();
        List<String> catNomes = new ArrayList<>();

        for (int index = 0; index < engenheiros.size(); index++) {
            if ((mask & (1 << index)) == 0) {
                continue;
            }
            EngenheiroInfo info = engenheiros.get(index);
            selecionados.add(info);
            catNomes.addAll(info.catNomes());
            itens.addAll(info.itens());
        }

        return new Candidate(selecionados, catNomes, itens);
    }

    private boolean atendeTodosOsItens(List<RequisicaoAnalise> itensCat, List<RequisicaoAnalise> requisicoes) {
        for (RequisicaoAnalise requisito : requisicoes) {
            BigDecimal totalEncontrado = ZERO;
            for (RequisicaoAnalise itemCat : itensCat) {
                if (descricaoMatcher.corresponde(
                        requisito.descricao(),
                        requisito.unidade(),
                        itemCat.descricao(),
                        itemCat.unidade()
                )) {
                    totalEncontrado = totalEncontrado.add(itemCat.quantidade());
                }
            }
            if (totalEncontrado.compareTo(requisito.quantidade()) < 0) {
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

    private AnaliseResultado criarResultado(Candidate candidato,
                                            List<RequisicaoAnalise> requisicoes,
                                            BigDecimal totalExigido,
                                            List<AnaliseItem> itensAnalise) {
        List<String> engenheiros = candidato.engenheiros().stream()
                .map(EngenheiroInfo::nome)
                .collect(Collectors.toList());
        
        // Extrair apenas as CATs que foram realmente utilizadas
        Set<String> catsUtilizadas = extrairCatsUtilizadas(itensAnalise, candidato.itens());
        List<String> cats = Collections.unmodifiableList(new ArrayList<>(catsUtilizadas));
        
        List<AnaliseResultadoItem> itens = criarItensResultado(itensAnalise, requisicoes, candidato.itens());
        BigDecimal cobertura = calcularCobertura(requisicoes, candidato.itens(), totalExigido);
        List<String> itensFaltantes = itens.stream()
                .filter(item -> !item.atende())
                .map(item -> item.descricao() + " " + item.unidade())
                .collect(Collectors.toList());
        ResultadoAnalise resultado = itensFaltantes.isEmpty() ? ResultadoAnalise.ATENDE : ResultadoAnalise.NAO_ATENDE;
        return new AnaliseResultado(resultado, engenheiros, cats, itens, itensFaltantes, cobertura, itensFaltantes.isEmpty());
    }

    private AnaliseResultado criarResultado(List<String> engenheiros,
                                            List<String> cats,
                                            List<RequisicaoAnalise> requisicoes,
                                            BigDecimal totalExigido,
                                            List<AnaliseItem> itensAnalise) {
        List<AnaliseResultadoItem> itens = criarItensResultado(itensAnalise, requisicoes, Collections.emptyList());
        BigDecimal cobertura = calcularCobertura(requisicoes, Collections.emptyList(), totalExigido);
        List<String> itensFaltantes = itens.stream()
                .filter(item -> !item.atende())
                .map(item -> item.descricao() + " " + item.unidade())
                .collect(Collectors.toList());
        return new AnaliseResultado(ResultadoAnalise.NAO_ATENDE, engenheiros, cats, itens, itensFaltantes, cobertura, false);
    }

    private List<AnaliseResultadoItem> criarItensResultado(List<AnaliseItem> itensAnalise,
                                                           List<RequisicaoAnalise> requisicoes,
                                                           List<RequisicaoAnalise> itensCat) {
        return itensAnalise.stream()
                .map(item -> {
                    BigDecimal exigido = item.getQuantidade();
                    BigDecimal encontrado = ZERO;
                    for (RequisicaoAnalise itemCat : itensCat) {
                        if (descricaoMatcher.corresponde(
                                item.getDescricao(),
                                item.getUnidade(),
                                itemCat.descricao(),
                                itemCat.unidade()
                        )) {
                            encontrado = encontrado.add(itemCat.quantidade());
                        }
                    }
                    boolean atende = encontrado.compareTo(exigido) >= 0;
                    return new AnaliseResultadoItem(item.getDescricao(), item.getUnidade(), exigido, encontrado, atende);
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calcularCobertura(List<RequisicaoAnalise> requisicoes,
                                         List<RequisicaoAnalise> itensCat,
                                         BigDecimal totalExigido) {
        if (totalExigido.compareTo(ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        BigDecimal encontrado = ZERO;
        for (RequisicaoAnalise requisicao : requisicoes) {
            BigDecimal encontradoParaRequisicao = ZERO;
            for (RequisicaoAnalise itemCat : itensCat) {
                if (descricaoMatcher.corresponde(
                        requisicao.descricao(),
                        requisicao.unidade(),
                        itemCat.descricao(),
                        itemCat.unidade()
                )) {
                    encontradoParaRequisicao = encontradoParaRequisicao.add(itemCat.quantidade());
                }
            }
            encontrado = encontrado.add(encontradoParaRequisicao.min(requisicao.quantidade()));
        }
        return encontrado.divide(totalExigido, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }


    private record EngenheiroInfo(Long id, String nome, List<String> catNomes, List<RequisicaoAnalise> itens) {

        void adicionarCat(Cat cat) {
            this.catNomes.add(cat.getNome());
            for (CatItem item : cat.getItens()) {
                this.itens.add(new RequisicaoAnalise(
                        item.getDescricao(),
                        item.getUnidade(),
                        item.getQuantidade(),
                        cat.getNome()
                ));
            }
        }
    }

    private record Candidate(List<EngenheiroInfo> engenheiros, List<String> catNomes, List<RequisicaoAnalise> itens) {
    }

    private Set<String> extrairCatsUtilizadas(List<AnaliseItem> itensAnalise, List<RequisicaoAnalise> itensCat) {
        Set<String> catsUtilizadas = new HashSet<>();
        for (AnaliseItem item : itensAnalise) {
            for (RequisicaoAnalise itemCat : itensCat) {
                if (descricaoMatcher.corresponde(
                        item.getDescricao(),
                        item.getUnidade(),
                        itemCat.descricao(),
                        itemCat.unidade()
                )) {
                    if (itemCat.nomeCat() != null) {
                        catsUtilizadas.add(itemCat.nomeCat());
                    }
                }
            }
        }
        return catsUtilizadas;
    }
}
