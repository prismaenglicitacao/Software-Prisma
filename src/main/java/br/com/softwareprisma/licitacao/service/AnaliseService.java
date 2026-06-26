package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;
import br.com.softwareprisma.licitacao.repository.AnaliseRepository;
import br.com.softwareprisma.licitacao.repository.CatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
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
        Map<String, BigDecimal> requisicoes = agruparRequisitos(analise.getItens());
        BigDecimal totalExigido = requisicoes.values().stream().reduce(ZERO, BigDecimal::add);

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

    private Map<String, BigDecimal> agruparRequisitos(List<AnaliseItem> itens) {
        Map<String, BigDecimal> requisicoes = new HashMap<>();
        for (AnaliseItem item : itens) {
            String key = normalizarChave(item.getDescricao(), item.getUnidade());
            requisicoes.merge(key, item.getQuantidade(), BigDecimal::add);
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
                    new HashMap<>()))
                    .adicionarCat(cat);
        }

        return lista.values().stream()
                .sorted(Comparator.comparing(EngenheiroInfo::nome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private Optional<AnaliseResultado> buscarMelhorCombinacaoValida(List<EngenheiroInfo> engenheiros,
                                                                    Map<String, BigDecimal> requisicoes,
                                                                    BigDecimal totalExigido,
                                                                    List<AnaliseItem> itensAnalise) {
        for (int tamanho = 1; tamanho <= engenheiros.size(); tamanho++) {
            AnaliseResultado melhor = null;
            for (int mask = 1; mask < (1 << engenheiros.size()); mask++) {
                if (Integer.bitCount(mask) != tamanho) {
                    continue;
                }
                Candidate candidato = construirCandidato(engenheiros, mask);
                if (!atendeTodosOsItens(candidato.totais(), requisicoes)) {
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
                                                            Map<String, BigDecimal> requisicoes,
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
        Map<String, BigDecimal> totais = new HashMap<>();
        List<EngenheiroInfo> selecionados = new ArrayList<>();
        List<String> catNomes = new ArrayList<>();

        for (int index = 0; index < engenheiros.size(); index++) {
            if ((mask & (1 << index)) == 0) {
                continue;
            }
            EngenheiroInfo info = engenheiros.get(index);
            selecionados.add(info);
            catNomes.addAll(info.catNomes());
            for (Map.Entry<String, BigDecimal> entry : info.totais().entrySet()) {
                totais.merge(entry.getKey(), entry.getValue(), BigDecimal::add);
            }
        }

        return new Candidate(selecionados, catNomes, totais);
    }

    private boolean atendeTodosOsItens(Map<String, BigDecimal> totais, Map<String, BigDecimal> requisicoes) {
        for (Map.Entry<String, BigDecimal> requisito : requisicoes.entrySet()) {
            if (buscarTotalPorChave(requisito.getKey(), totais).compareTo(requisito.getValue()) < 0) {
                return false;
            }
        }
        return true;
    }

    private BigDecimal buscarTotalPorChave(String chave, Map<String, BigDecimal> totais) {
        if (totais.containsKey(chave)) {
            return totais.get(chave);
        }
        for (Map.Entry<String, BigDecimal> entry : totais.entrySet()) {
            if (mesmoMaterial(chave, entry.getKey())) {
                return entry.getValue();
            }
        }
        return ZERO;
    }

    boolean mesmoMaterial(String chaveA, String chaveB) {
        String[] partesA = chaveA.split("\\|", 2);
        String[] partesB = chaveB.split("\\|", 2);
        if (partesA.length != 2 || partesB.length != 2) {
            return false;
        }

        if (!partesA[1].equals(partesB[1])) {
            return false;
        }

        var tokensA = separarTokens(partesA[0]);
        var tokensB = separarTokens(partesB[0]);
        if (tokensA.isEmpty() || tokensB.isEmpty()) {
            return tokensA.equals(tokensB);
        }

        return tokensA.containsAll(tokensB) || tokensB.containsAll(tokensA);
    }

    private static Set<String> separarTokens(String texto) {
        if (texto == null || texto.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(texto.split("\\s+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toSet());
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
                                            Map<String, BigDecimal> requisicoes,
                                            BigDecimal totalExigido,
                                            List<AnaliseItem> itensAnalise) {
        List<String> engenheiros = candidato.engenheiros().stream()
                .map(EngenheiroInfo::nome)
                .collect(Collectors.toList());
        List<String> cats = Collections.unmodifiableList(new ArrayList<>(candidato.catNomes()));
        List<AnaliseResultadoItem> itens = criarItensResultado(itensAnalise, requisicoes, candidato.totais());
        BigDecimal cobertura = calcularCobertura(requisicoes, candidato.totais(), totalExigido);
        List<String> itensFaltantes = itens.stream()
                .filter(item -> !item.atende())
                .map(item -> item.descricao() + " " + item.unidade())
                .collect(Collectors.toList());
        ResultadoAnalise resultado = itensFaltantes.isEmpty() ? ResultadoAnalise.ATENDE : ResultadoAnalise.NAO_ATENDE;
        return new AnaliseResultado(resultado, engenheiros, cats, itens, itensFaltantes, cobertura, itensFaltantes.isEmpty());
    }

    private AnaliseResultado criarResultado(List<String> engenheiros,
                                            List<String> cats,
                                            Map<String, BigDecimal> requisicoes,
                                            BigDecimal totalExigido,
                                            List<AnaliseItem> itensAnalise) {
        List<AnaliseResultadoItem> itens = criarItensResultado(itensAnalise, requisicoes, Collections.emptyMap());
        BigDecimal cobertura = calcularCobertura(requisicoes, Collections.emptyMap(), totalExigido);
        List<String> itensFaltantes = itens.stream()
                .filter(item -> !item.atende())
                .map(item -> item.descricao() + " " + item.unidade())
                .collect(Collectors.toList());
        return new AnaliseResultado(ResultadoAnalise.NAO_ATENDE, engenheiros, cats, itens, itensFaltantes, cobertura, false);
    }

    private List<AnaliseResultadoItem> criarItensResultado(List<AnaliseItem> itensAnalise,
                                                           Map<String, BigDecimal> requisicoes,
                                                           Map<String, BigDecimal> totais) {
        return itensAnalise.stream()
                .map(item -> {
                    String key = normalizarChave(item.getDescricao(), item.getUnidade());
                    BigDecimal exigido = buscarTotalPorChave(key, requisicoes);
                    BigDecimal encontrado = buscarTotalPorChave(key, totais);
                    boolean atende = encontrado.compareTo(exigido) >= 0;
                    return new AnaliseResultadoItem(item.getDescricao(), item.getUnidade(), item.getQuantidade(), encontrado, atende);
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calcularCobertura(Map<String, BigDecimal> requisicoes,
                                         Map<String, BigDecimal> totais,
                                         BigDecimal totalExigido) {
        if (totalExigido.compareTo(ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        BigDecimal encontrado = requisicoes.entrySet().stream()
                .map(entry -> {
                    BigDecimal valorEncontrado = buscarTotalPorChave(entry.getKey(), totais);
                    return valorEncontrado.min(entry.getValue());
                })
                .reduce(ZERO, BigDecimal::add);
        return encontrado.divide(totalExigido, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    static String normalizarChave(String descricao, String unidade) {
        return normalizarTexto(descricao) + "|" + normalizarUnidade(unidade);
    }

    private static String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String apenasAlfaNum = semAcento.replaceAll("[^\\p{Alnum}]+", " ");
        return apenasAlfaNum.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static String normalizarUnidade(String unidade) {
        if (unidade == null || unidade.isBlank()) {
            return "";
        }
        return normalizarTexto(unidade).replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private record EngenheiroInfo(Long id, String nome, List<String> catNomes, Map<String, BigDecimal> totais) {

        void adicionarCat(Cat cat) {
            this.catNomes.add(cat.getNome());
            for (CatItem item : cat.getItens()) {
                String key = normalizarChave(item.getDescricao(), item.getUnidade());
                this.totais.merge(key, item.getQuantidade(), BigDecimal::add);
            }
        }
    }

    private record Candidate(List<EngenheiroInfo> engenheiros, List<String> catNomes, Map<String, BigDecimal> totais) {
    }
}
